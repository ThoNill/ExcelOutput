package org.thonill.gui;


import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.thonill.actions.AusgabeSteuerItem;
import org.thonill.logger.LOG;

/**
 * ApplicationDialog provides a dialog for selecting a file and connecting to a
 * database.
 */

public class ApplicationDialog extends LoginDialog {
	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	private String pathString;
	private String connectionName;

	public ApplicationDialog(String pathString, String connectionName) {
		super();
		this.pathString = pathString;
		this.connectionName = connectionName;

	}

	@Override
	protected void start() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Datei auswählen");
		fileChooser.setApproveButtonText("Auswählen");
		fileChooser.setApproveButtonToolTipText("Auswählen");
		fileChooser.setApproveButtonMnemonic('A');
		fileChooser.setFileHidingEnabled(false);
		String absolutePath = new File(pathString).getAbsolutePath();
		fileChooser.setCurrentDirectory(new File(absolutePath));

		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String steuerDatei = fileChooser.getSelectedFile().getAbsolutePath();
				this.pathString = new File(steuerDatei).getParent();

				Connection conn = getConnection(connectionName, pathString);
				AusgabeSteuerItem.createAusgabeDateien(steuerDatei, conn);
			} catch (Exception e) {
				msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
				LOG.severe(e.getLocalizedMessage());
			}

		}
	}

	public static void main(String[] args) {
		Map<String, String> arguments = parseArgs(args);
		String pathString = getArgument(arguments, "path", ".\\app\\src\\test\\resources");
		String connectionName = getArgument(arguments, "connection", "testDb");

		SwingUtilities.invokeLater(() -> {
			try {
				new ApplicationDialog(pathString, connectionName).start();
			} catch (Exception e) {
				LOG.severe(e.getLocalizedMessage());
			}
		});
	}

	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> argMap = new HashMap<>();

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				String key = args[i].substring(1);
				if (i < args.length - 1) {
					argMap.put(key, args[i + 1]);
					i++;
				} else {
					argMap.put(key, null);
				}
			}
		}

		return argMap;
	}

	private static String getArgument(Map<String, String> arguments, String key, String defaultValue) {
		String value = arguments.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
}
