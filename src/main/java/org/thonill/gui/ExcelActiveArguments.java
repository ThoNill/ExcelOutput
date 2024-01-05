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
	public void remove(String key) {
		this.arguments.remove(key);

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
			LOG.info();
			LOG.info("start createFiles");
			String steuerFilePath = getFilePath(STEUER_DATEI);
			LOG.info("steuerFilePath = {0}",steuerFilePath);
			String templateFilePath = getFilePath(EXCEL_VORLAGE);
			LOG.info("templateFilePath = {0}",templateFilePath);
			String outputFileDir = getFilePath(AUSGABE_DIR);
			LOG.info("outputFileDir = {0}",outputFileDir);

			if (steuerFilePath != null) {
				LOG.info("AusgabeSteuerItem.createAusgabeDateien");
				LOG.severe();
				AusgabeSteuerItem.createAusgabeDateien(steuerFilePath, conn);
			} else {
				String outputFilePath = getOutputFilePath(templateFilePath);
				LOG.info("outputFilePath = {0} ",outputFilePath);
				arguments.put(AUSGABE_DATEI, outputFilePath);
				AusgabeSteuerItem item = new AusgabeSteuerItem(arguments);
				LOG.info("item.createAusgabeDatei");
				LOG.severe();
				item.createAusgabeDatei(conn);
			}
			LOG.info();
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
		String outputFilePath = concatenateDirAndFilename();
		if (outputFilePath == null) {
			outputFilePath = "output" + System.currentTimeMillis();
			outputFilePath += getPostfix(templateFilePath);
		}
		return outputFilePath;
	}

	private String concatenateDirAndFilename() {
		String outputFileDir = getArgument(AUSGABE_DIR, null);
		LOG.info("outputFileDir = {0}",outputFileDir);
		String outputFile = getArgument(AUSGABE_DATEI, null);
		LOG.info("outputFile = {0}",outputFile);
		String outputFilePath = null;
		if (outputFile != null && outputFileDir != null) {
			if (outputFile.startsWith(outputFileDir)) {
				outputFilePath = outputFile;
			} else {
				outputFilePath = new File(outputFileDir, outputFile).getAbsolutePath();
			}
		}
		LOG.info("outputFilePath = {0}",outputFilePath);
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
