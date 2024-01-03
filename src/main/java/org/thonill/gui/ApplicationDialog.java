package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.thonill.actions.AusgabeSteuerItem;
import org.thonill.exceptions.ApplicationException;
import org.thonill.logger.LOG;

/**
 * ApplicationDialog provides a dialog for selecting a file and connecting to a
 * database.
 */

public class ApplicationDialog implements Runnable {
	private Map<String, String> arguments;
	private boolean run = true;
	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	public ApplicationDialog() {
		super();

	}

	protected void showChooser(String pathString, Connection conn) {
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

		int returnVal = fileChooser.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String steuerDatei = fileChooser.getSelectedFile().getAbsolutePath();
				AusgabeSteuerItem.createAusgabeDateien(steuerDatei, conn);
			} catch (Exception e) {
				msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
				LOG.severe(e.getLocalizedMessage());
			}

		}
	}

	public static void main(String[] args) {
		Map<String, String> arguments = parseArgs(args);

		new ApplicationDialog().main(arguments);
	}

	public void main(Map<String, String> arguments) {
		this.arguments = arguments;
		new Thread(this).run();
	}

	@Override
	public void run() {
		LOG.info("Start");
		checkNotNull(arguments, "ApplicationDialog.main arguments is null");

		String connectionFilePath = getFilePath(arguments, "dbDatei");
		checkNotNull(connectionFilePath, "we need -dbDatei ");
		String sqlFilePath = getFilePath(arguments, "sqlDatei");
		checkNotNull(sqlFilePath, "we need -sqlDatei ");
		LOG.info("Vor login Dialog");
		SwingUtilities.invokeLater(() ->

		{
			LOG.info("Start login Dialog");
			LoginDialog loginDialog = new LoginDialog(connectionFilePath);
			loginDialog.setVisible(true);
			Connection conn = loginDialog.getConnection();

			String gui = getArgument(arguments, "gui", null);

			if ("gui".equals(gui)) {

				SwingUtilities.invokeLater(() ->

				{
					try {
						new ApplicationDialog().showChooser(".", conn);
					} catch (Exception e) {
						LOG.severe(e.getLocalizedMessage());
					}
				});

			} else {

				createFiles(arguments, conn);
				run = false;
			}
		});
		while (run) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void createFiles(Map<String, String> arguments, Connection conn) {
		try {
			String steuerFilePath = getFilePath(arguments, "steuerDatei");

			String templateFilePath = getFilePath(arguments, "excelVorlage");

			if (steuerFilePath != null) {
				AusgabeSteuerItem.createAusgabeDateien(steuerFilePath, conn);
			} else {
				String outputFilePath = getOutputFilePath(arguments, templateFilePath);

				arguments.put("ausgabeDatei", outputFilePath);
				AusgabeSteuerItem item = new AusgabeSteuerItem(arguments);
				item.createAusgabeDatei(conn);
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	private static String getOutputFilePath(Map<String, String> arguments, String templateFilePath) {
		String outputFilePath = getArgument(arguments, "ausgabeDatei", null);

		if (outputFilePath == null) {
			outputFilePath = "output" + System.currentTimeMillis();
			outputFilePath += getPostfix(templateFilePath);
		}
		return outputFilePath;
	}

	private static String getPostfix(String templateFilePath) {
		boolean createExcelFile = (templateFilePath != null);
		if (createExcelFile) {
			return (templateFilePath.endsWith(".xls")) ? ".xls" : ".xlsx";
		} else {
			return ".csv";
		}
	}

	private static String getFilePath(Map<String, String> arguments, String argName) {
		String filePath = getArgument(arguments, argName, null);
		if (filePath == null) {
			checkFileExists(filePath, "ApplicationDialog.main", argName + "FilePath");
		}
		return filePath;
	}

	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> argMap = new HashMap<>();

		for (int i = 0; i < args.length; i += 2) {
			if (args[i].startsWith("-")) {
				String key = args[i].substring(1);
				if (i < args.length - 1) {
					argMap.put(key, args[i + 1]);
				} else {
					throw new ApplicationException("ungerade Anzahl von Argumenten");
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

	public void msgBox(String message, int messageType) {
		JOptionPane.showMessageDialog(new JFrame(), message, "Message", messageType);
	}

}
