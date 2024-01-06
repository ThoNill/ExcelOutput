package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.thonill.actions.AusgabeSteuerItem;
import org.thonill.exceptions.ApplicationException;
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
	public void clear() {
		this.arguments = new HashMap<>();

	}

	@Override
	public void run() {
		LOG.info("Starte run");
		try {
			checkArguments();
			createFiles();
		} catch (Exception e) {
			throw e;
		}
	}

	private void checkArguments() {
		LOG.info("Starte checkArguments");
		String connectionFilePath = getFilePath(DB_DATEI);
		checkNotNull(connectionFilePath, "we need -dbDatei ");
		String sqlFilePath = getFilePath(SQL_DATEI);
		checkNotNull(sqlFilePath, "we need -sqlDatei ");
		checkNotNull(arguments.get(USER), "user is not defined");
		checkNotNull(arguments.get(PASSWORD), "password is not defined");
		LOG.info("nach checkArguments");
	}

	private void createFiles() {

		try (Connection conn = createConnectionInfo().createConnection()) {
			createFiles(conn);
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			throw new ApplicationException(e.getMessage());
		}
	}

	private void createFiles(Connection conn) {
		try {
			LOG.info("start createFiles");
			String steuerFilePath = getFilePath(STEUER_DATEI);
			LOG.info("steuerFilePath = {0}", steuerFilePath);
			String templateFilePath = getFilePath(EXCEL_VORLAGE);
			LOG.info("templateFilePath = {0}", templateFilePath);
			String outputFileDir = getFilePath(AUSGABE_DIR);
			LOG.info("outputFileDir = {0}", outputFileDir);

			if (steuerFilePath != null) {
				LOG.info("AusgabeSteuerItem.createAusgabeDateien");
				LOG.severe();
				AusgabeSteuerItem.createAusgabeDateien(steuerFilePath, conn);
			} else {
				String outputFilePath = getOutputFilePath(templateFilePath);
				LOG.info("outputFilePath = {0} ", outputFilePath);
				arguments.put(AUSGABE_DATEI, outputFilePath);
				AusgabeSteuerItem item = new AusgabeSteuerItem(arguments);
				LOG.info("item.createAusgabeDatei");
				LOG.severe();
				item.createAusgabeDatei(conn);
			}
			LOG.info("end createFiles");
			LOG.severe();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe(e.getLocalizedMessage());
			throw new ApplicationException(e.getMessage());
		}
	}

	protected String getFilePath(String argName) {
		String filePath = getArgument(argName, null);
		if (filePath != null) {
			checkFileExists(filePath, "ExcelOutputApplication.main", argName + "FilePath");
		}
		return filePath;
	}

	private String getOutputFilePath(String templateFilePath) {
		String outputFilePath = getArgument(AUSGABE_DATEI, null);
		if (outputFilePath == null) {
			outputFilePath = concatenateDirAndFilename(templateFilePath);
		}
		return outputFilePath;
	}

	private String concatenateDirAndFilename(String templateFilePath) {
		LOG.info("concatenateDirAndFilename");
		String outputDir = getArgument(AUSGABE_DIR, ".");
		LOG.info("outputFileDir = {0}", outputDir);
		String defaultFileName = "output" + System.currentTimeMillis() + getPostfix(templateFilePath);
		String outputFileName = getArgument(AUSGABE_DATEI_NAME, defaultFileName);
		LOG.info("outputFileName = {0}", outputFileName);
		String outputFilePath = new File(outputDir, outputFileName).getAbsolutePath();
		LOG.info("outputFilePath = {0}", outputFilePath);
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

	@Override
	public Connection createConnection() {
		return createConnectionInfo().createConnection();
	}

	@Override
	public void stop() {

	}

}
