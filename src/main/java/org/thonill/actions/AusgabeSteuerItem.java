package org.thonill.actions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.thonill.excel.ReadSteuerItems;
import org.thonill.exceptions.ApplicationException;
import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;
import org.thonill.replace.RawSqlStatement;
import org.thonill.replace.ReplaceDescription;
import org.thonill.sql.ExecutableStatementSet;

public class AusgabeSteuerItem extends StandardKeys {

	String ausgabeDatei;
	String sqlDatei;
	String excelVorlage;
	Map<String, String> daten;

	/**
	 * Constructor for AusgabeSteuerItem. Initializes the object's fields from the
	 * provided data map. Validates required fields and file paths.
	 */
	public AusgabeSteuerItem(Map<String, String> daten) {
		this.daten = daten;
		this.ausgabeDatei = getValue(daten, AUSGABE_DATEI, true);
		this.sqlDatei = getValue(daten, SQL_DATEI, true);
		checkFileExists(sqlDatei, "AusgabeSteuerItem.constructor", SQL_DATEI);

		boolean b = !ausgabeDatei.endsWith("csv");
		if (b) {
			this.excelVorlage = getValue(daten, EXCEL_VORLAGE, b);
			checkFileExists(excelVorlage, "AusgabeSteuerItem.constructor", EXCEL_VORLAGE);
		}
	}

	/**
	 * Gets a value from the given data map for the specified key. Throws an
	 * exception if the key is not found and b is true. Returns an empty string if
	 * the key is not found and b is false.
	 */
	private static String getValue(Map<String, String> data, String key, boolean b) {
		checkNotNull(data, "AusgabeSteuerItem.extracted: data is null");
		checkNotNull(key, "AusgabeSteuerItem.extracted: key is null");

		if (data.containsKey(key)) {
			return data.get(key);
		} else {
			if (b) {
				throw new ApplicationException("AusgabeSteuerItem.extracted: key " + key + " not found");
			}
		}
		return "";
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
		checkNotNull(conn, "AusgabeSteuerItem.createAusgabeDatei: conn is null");
		showAbsolutePath(ausgabeDatei);
		showAbsolutePath(excelVorlage);

		List<RawSqlStatement> rawStatements = RawSqlStatement.getRawSqlStatements(sqlDatei);

		List<ReplaceDescription> descr = RawSqlStatement.createReplaceDescriptions(daten);

		ExecutableStatementSet executableStatements = RawSqlStatement.replaceVariables(rawStatements, descr);

		LOG.info("Generating output file");

		executableStatements.writeToOutputFile(conn, ausgabeDatei, excelVorlage);

	}

	/**
	 * Creates output files for each AusgabeSteuerItem in the given list by calling
	 * createAusgabeDatei on each one.
	 *
	 * @param steuerItems The list of AusgabeSteuerItem to process.
	 * @param conn        The database connection to use.
	 * @throws ApplicationException If there is an error generating the output
	 *                              files.
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void createAusgabeDateien(List<AusgabeSteuerItem> steuerItems, Connection conn)
			throws IOException, SQLException {
		checkNotNull(steuerItems, "AusgabeSteuerItem.createAusgabeDateien: steuerItems is null");
		checkNotNull(conn, "AusgabeSteuerItem.createAusgabeDateien: conn is null");

		for (AusgabeSteuerItem item : steuerItems) {
			item.createAusgabeDatei(conn);
		}
	}

	/**
	 * Creates output files for each AusgabeSteuerItem read from the given
	 * steuerDatei Excel file. Reads the steuerItems using ReadSteuerItems and then
	 * calls createAusgabeDateien to generate the output for each one, using the
	 * provided database connection.
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
	public static void createAusgabeDateien(String steuerDatei, Connection conn) throws IOException, SQLException {
		checkNotNull(steuerDatei, "AusgabeSteuerItem.createAusgabeDateien: steuerDatei is null");
		checkNotNull(conn, "AusgabeSteuerItem.createAusgabeDateien: conn is null");
		checkFileExists(steuerDatei, "AusgabeSteuerItem.createAusgabeDateien", "steuerDatei");

		ReadSteuerItems readSteuerItems = new ReadSteuerItems(steuerDatei);
		List<AusgabeSteuerItem> items = readSteuerItems.readSteuerItemsFromExcel();
		createAusgabeDateien(items, conn);
	}

	/**
	 * Logs the absolute path of the given filename to the logger.
	 *
	 * @param filename The file path to log.
	 */
	private void showAbsolutePath(String filename) {
		LOG.info(new File(filename).getAbsolutePath());
	}

}