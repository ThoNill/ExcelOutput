package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.thonill.actions.AusgabeSteuerItem;
import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;
import org.thonill.sql.ConnectionInfo;

public class ExcelActiveArguments extends StandardKeys implements ActiveArguments {
	protected Map<String, String> arguments;

	public ExcelActiveArguments() {
		super();
		this.arguments = new HashMap<>();
	}

	@Override
	public void put(String key, String value) {
		this.arguments.put(key, value);

	}

	@Override
	public void remove(String key) {
		this.arguments.remove(key);

	}

	@Override
	public void run() {
		checkArguments();
		createFiles();
	}

	private void checkArguments() {
		String connectionFilePath = getFilePath(DB_DATEI);
		checkNotNull(connectionFilePath, "we need -dbDatei ");
		String sqlFilePath = getFilePath(SQL_DATEI);
		checkNotNull(sqlFilePath, "we need -sqlDatei ");
		checkNotNull(arguments.get(USER), "user is not defined");
		checkNotNull(arguments.get(PASSWORD), "password is not defined");
		LOG.info("Vor login Dialog");
	}

	private void createFiles() {

		try (Connection conn = createConnectionInfo().createConnection()) {
			createFiles(conn);
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	private void createFiles(Connection conn) {
		try {
			String steuerFilePath = getFilePath(STEUER_DATEI);

			String templateFilePath = getFilePath(EXCEL_VORLAGE);

			if (steuerFilePath != null) {
				AusgabeSteuerItem.createAusgabeDateien(steuerFilePath, conn);
			} else {
				String outputFilePath = getOutputFilePath(templateFilePath);

				arguments.put(AUSGABE_DATEI, outputFilePath);
				AusgabeSteuerItem item = new AusgabeSteuerItem(arguments);
				item.createAusgabeDatei(conn);
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	protected String getFilePath(String argName) {
		String filePath = getArgument(argName, null);
		if (filePath == null) {
			checkFileExists(filePath, "ExcelOutputApplication.main", argName + "FilePath");
		}
		return filePath;
	}

	private String getOutputFilePath(String templateFilePath) {
		String outputFilePath = concatenateDirAndFilename();
		if (outputFilePath == null) {
			outputFilePath = "output" + System.currentTimeMillis();
			outputFilePath += getPostfix(templateFilePath);
		}
		return outputFilePath;
	}

	private String concatenateDirAndFilename() {
		String outputFileDir = getArgument(AUSGABE_DIR, null);
		String outputFile = getArgument(AUSGABE_DATEI, null);
		String outputFilePath = null;
		if (outputFile != null && outputFileDir != null) {
			if (outputFile.startsWith(outputFileDir)) {
				outputFilePath = outputFile;
			} else {
				outputFilePath = new File(outputFileDir, outputFile).getAbsolutePath();
			}
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

	private String getArgument(String key, String defaultValue) {
		String value = arguments.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	private ConnectionInfo createConnectionInfo() {
		return new ConnectionInfo(arguments.get(USER), arguments.get(PASSWORD), arguments.get(DB_DATEI));
	}

	public Connection createConnection() {
		return createConnectionInfo().createConnection();
	}

	@Override
	public void stop() {

	}

}
