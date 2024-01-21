package org.thonill.actions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thonill.checks.Checks;
import org.thonill.checks.MapCheck;
import org.thonill.excel.ReadSteuerItems;
import org.thonill.exceptions.ApplicationException;
import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;
import org.thonill.replace.RawSqlStatement;
import org.thonill.replace.ReplaceDescription;
import org.thonill.sql.ConnectionInfo;
import org.thonill.sql.ExecutableStatementSet;

public class FileCreator implements ActiveArguments, StandardKeys {

	protected Map<String, String> daten;
	private String user;
	private String password;
	private File outputDir;
	private String outputFile;
	private File templateFile;
	private File sqlFile;
	private ExportArt exportArt;
	
	private MapCheck checkDaten;

	private File dbFile;
	private String sqlQuerys;

	public FileCreator() {
		super();
		this.daten = new HashMap<>();
	}

	/**
	 * Constructor for FileCreator. Initializes the object's fields from the
	 * provided data map. Validates required fields and file paths.
	 */
	public FileCreator(Map<String, String> datenMap) {
		setDaten(datenMap);
		setAusgabeDatei(getValue(datenMap, AUSGABE_DATEI, true));
		setSqlFile(getValue(datenMap, SQL_DATEI, true));
		setExportArt(datenMap);

		switch (exportArt) {
		case CSV:
			break;
		case STEUERDATEI:
			this.templateFile = Checks.checkFileExists(getValue(datenMap, STEUER_DATEI, true),
					"FileCreator.constructor", STEUER_DATEI);
			;
			break;
		case VORLAGE:
			this.templateFile = Checks.checkFileExists(getValue(datenMap, EXCEL_VORLAGE, true),
					"FileCreator.constructor", EXCEL_VORLAGE);
			break;
		default:
			break;
		}
		LOG.info("outputFile: {0}", outputFile);
	}

	protected boolean die_Parameter_reichen_zur_Ausführung() {
		return this.dbFile != null && this.user != null && this.password != null && this.outputFile != null
				&& (outputFile.endsWith(".csv") || (this.templateFile != null));
	}

	private void checkArguments() {
		LOG.info("Starte checkArguments");

		checkNotNull(this.checkDaten, "keine Pruefung der Eingabedaten in FileCreator.checkArguments ");
		
		checkNotNull(this.dbFile, "this.dbFilewe need -dbDatei ");
		checkNotNull(this.sqlQuerys, "we need -sqlFile or setSqlQuerys");
		checkNotNull(this.user, "user is not defined");
		checkNotNull(this.password, "password is not defined");

		LOG.info("nach checkArguments");
	}

	private void setExportArt(Map<String, String> datenMap) {
		if (outputFile.endsWith("csv")) {
			setExportArt(ExportArt.CSV);
		} else {
			String excelVorlage = getValue(datenMap, EXCEL_VORLAGE, false);
			String steuerDatei = getValue(datenMap, STEUER_DATEI, false);
			if (excelVorlage != null) {
				setExportArt(ExportArt.VORLAGE);
			}
			if (steuerDatei != null) {
				setExportArt(ExportArt.STEUERDATEI);
			}
		}
	}

	@Override
	public void run() {
		LOG.info("Starte run");
		checkArguments();
		createFiles();
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
			LOG.info("sqlFile = {0}", this.sqlFile);
			LOG.info("templateFilePath = {0}", this.templateFile);
			LOG.info("outputFile = {0}", getAusgabePath());

			switch (exportArt) {
			case CSV:
			case VORLAGE:
				createAusgabeDatei(conn);
				break;
			case STEUERDATEI:
				LOG.info("FileCreator.createAusgabeDateien");
				LOG.severe();
				createAusgabeDateien(templateFile, conn);
				break;
			default:
				break;

			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			throw new ApplicationException(e.getMessage());
		}
	}

	/**
	 * Creates the output file by executing the SQL statements and writing to the
	 * output file.
	 *
	 * @param conn Database connection to use for executing SQL statements.
	 * @throws ApplicationException If there is an error generating the output file.
	 * @throws IOException
	 * @throws SQLException
	 */

	public void createAusgabeDatei(Connection conn) throws IOException, SQLException {

		checkNotNull(conn, "FileCreator.createAusgabeDatei: conn is null");
		checkDaten();

		List<RawSqlStatement> rawStatements = RawSqlStatement.getRawSqlStatements(this.sqlQuerys);

		List<ReplaceDescription> descr = RawSqlStatement.createReplaceDescriptions(this.daten);

		ExecutableStatementSet executableStatements = RawSqlStatement.replaceVariables(rawStatements, descr);

		LOG.info("Generating output file");

		executableStatements.writeToOutputFile(conn, getAusgabePath(), this.templateFile);

	}

	private void checkDaten() {
		checkDaten.checkWithException(this.daten);
	}

	protected String getFilePath(String argName) {
		String filePath = getDatenValue(argName, null);
		if (filePath != null) {
			checkFileExists(filePath, "ExcelOutputApplication.main", argName + "FilePath");
		}
		return filePath;
	}

	private String getDatenValue(String key, String defaultValue) {
		String value = daten.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	private ConnectionInfo createConnectionInfo() {
		return new ConnectionInfo(user, password, dbFile);
	}

	@Override
	public Connection createConnection() {
		return createConnectionInfo().createConnection();
	}

	@Override
	public void stop() {
		// is empty
	}

	/**
	 * Gets a value from the given data map for the specified key. Throws an
	 * exception if the key is not found and b is true. Returns an empty string if
	 * the key is not found and b is false.
	 */
	private static String getValue(Map<String, String> data, String key, boolean b) {
		checkNotNull(data, "FileCreator.extracted: data is null");
		checkNotNull(key, "FileCreator.extracted: key is null");

		if (data.containsKey(key)) {
			return data.get(key);
		} else {
			if (b) {
				throw new ApplicationException("FileCreator.extracted: key " + key + " not found");
			}
		}
		return null;
	}

	/**
	 * Creates output files for each FileCreator in the given list by calling
	 * createAusgabeDatei on each one.
	 *
	 * @param steuerItems The list of FileCreator to process.
	 * @param conn        The database connection to use.
	 * @throws ApplicationException If there is an error generating the output
	 *                              files.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void createAusgabeDateien(List<FileCreator> steuerItems, Connection conn)
			throws IOException, SQLException {
		checkNotNull(steuerItems, "FileCreator.createAusgabeDateien: steuerItems is null");
		checkNotNull(conn, "FileCreator.createAusgabeDateien: conn is null");

		for (FileCreator item : steuerItems) {
			item.setCheckDaten(checkDaten);
			item.createAusgabeDatei(conn);
		}
	}

	/**
	 * Creates output files for each FileCreator read from the given steuerDatei
	 * Excel file. Reads the steuerItems using ReadSteuerItems and then calls
	 * createAusgabeDateien to generate the output for each one, using the provided
	 * database connection.
	 *
	 * @param steuerDatei Path to the Excel file containing the steuer items to
	 *                    process.
	 * @param conn        Database connection to use for generating the output
	 *                    files.
	 * @throws ApplicationException If there is an error reading the input or
	 *                              generating the output.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void createAusgabeDateien(File steuerDatei, Connection conn) throws IOException, SQLException {
		checkNotNull(steuerDatei, "FileCreator.createAusgabeDateien: steuerDatei is null");
		checkNotNull(conn, "FileCreator.createAusgabeDateien: conn is null");

		ReadSteuerItems readSteuerItems = new ReadSteuerItems(steuerDatei);
		List<FileCreator> items = readSteuerItems.readSteuerItemsFromExcel();
		createAusgabeDateien(items, conn);
	}

	@Override
	public void clear() {
		this.daten = new HashMap<>();

	}

	@Override
	public void put(String key, String value) {
		this.daten.put(key, value);
	}

	public void setDaten(Map<String, String> arguments) {
		this.daten = arguments;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setDbFile(String dbFile) {
		this.dbFile = Checks.checkFileExists(dbFile, "FileCreator.setDbFile", "setDbFile");
	}

	public File getAusgabePath() {
		if (outputDir == null) {
			return new File(outputFile);
		}
		return new File(outputDir, outputFile);
	}

	@Override
	public void setAusgabeDir(String ausgabeDir) {
		this.outputDir = Checks.checkFileExists(ausgabeDir, "FileCreator.setAusgabeDir", "setAusgabeDir");
	}

	@Override
	public void setAusgabeDatei(String ausgabeDatei) {
		this.outputFile = ausgabeDatei;
	}

	@Override
	public void setTemplateFile(String templateFile) {
		this.templateFile = Checks.checkFileExists(templateFile, "FileCreator.setTemplateFile", "templateFile");
	}

	@Override
	public void setSqlFile(String sqlFile) {
		this.sqlFile = Checks.checkFileExists(sqlFile, "FileCreator.setSqlFilesetTemplateFile", "sqlFile");
		try {
			sqlQuerys = Files.readString(this.sqlFile.toPath());
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void setExportArt(ExportArt exportArt) {
		this.exportArt = exportArt;
	}

	public void setCheckDaten(MapCheck checkDaten) {
		this.checkDaten = checkDaten;
	}

}
