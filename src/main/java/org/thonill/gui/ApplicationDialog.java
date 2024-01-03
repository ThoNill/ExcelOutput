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
import org.thonill.sql.ConnectionInfo;

/**
 * ApplicationDialog provides a dialog for selecting a file and connecting to a
 * database.
 */

public class ApplicationDialog implements Runnable {
	private Map<String, String> arguments;
	private boolean run = true;
	private boolean export = false;
	private ConnectionInfo connectionInfo;
	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	public ApplicationDialog() {
		super();

	}

	protected void showChooser(String key, String pathString) {
		run = true;
		LOG.info("showChooser {0} ",pathString);
		FileChooser choose = new FileChooser(new SetMy<String>() {

			public void setValue(String path) {
				arguments.put(key, path);
				run = false;
			}
		}

				, pathString);
		choose.showDialog();
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
		SwingUtilities.invokeLater(() -> {
			try {
				inGui(connectionFilePath);
			} catch (Exception e) {
				msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
				LOG.severe(e.getLocalizedMessage());
			}
		});
		waitUntilTheEnd();
		createFiles();
	}

	private void inGui(String connectionFilePath) {
		try {
			LOG.info("Start login Dialog");
			LoginDialog loginDialog = new LoginDialog(

					new SetMy<ConnectionInfo>() {
						@Override
						public void setValue(ConnectionInfo cInfo) {
							connectionInfo = cInfo;
							export = (cInfo != null);
							run = false;
						}
					}

					, connectionFilePath);
			loginDialog.setVisible(true);
			loginDialog.requestFocus();

		} catch (Exception e) {
			msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
			LOG.severe(e.getLocalizedMessage());
		}

		String steuerFilePath = getFilePath(arguments, "steuerDatei");

		String templateFilePath = getFilePath(arguments, "excelVorlage");
		LOG.info("steuerFilePath {0} ",steuerFilePath);
		LOG.info("templateFilePath {0} ",templateFilePath);
		if (steuerFilePath == null && templateFilePath == null) {
			try {
				run = true;
				showChooser("steuerDatei", ".");
			} catch (Exception e) {
				msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
				LOG.severe(e.getLocalizedMessage());
			}

		}
	}

	private void createFiles() {
		if (!export)
			return;

		try (Connection conn = connectionInfo.createConnection()) {
			createFiles(arguments, conn);
		} catch (Exception e) {
			msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
			LOG.severe(e.getLocalizedMessage());
		}
	}

	private void waitUntilTheEnd() {
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
