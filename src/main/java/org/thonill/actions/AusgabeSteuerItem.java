package org.thonill.actions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

/**
 * AusgabeSteuerItem handles the output of tax items to an Excel file.
 * It takes in a data map, SQL file, and Excel template to
 * generate the output file.
 */

import java.util.logging.Logger;

import org.thonill.excel.ReadSteuerItems;
import org.thonill.replace.RawSqlStatement;
import org.thonill.replace.ReplaceDescription;
import org.thonill.sql.ExecutableStatementSet;

public class AusgabeSteuerItem {
	private static final Logger LOG = Logger.getLogger(AusgabeSteuerItem.class.getName());

	String ausgabeDatei;
	String kunden;
	String sqlDatei;
	String excelVorlage;
	HashMap<String, String> daten;

	/**
	 * Constructor for AusgabeSteuerItem. Initializes the object's fields from the
	 * provided data map. Validates required fields and file paths.
	 */
	public AusgabeSteuerItem(HashMap<String, String> daten) {
		this.daten = daten;
		this.ausgabeDatei = getValue(daten, "ausgabeDatei", true);
		this.kunden = getValue(daten, "kunden", true);
		this.sqlDatei = getValue(daten, "sqlDatei", true);
		checkFileExists(sqlDatei, "AusgabeSteuerItem.constructor", "sqlDatei");

		boolean b = !ausgabeDatei.endsWith("csv");
		if (b) {
			this.excelVorlage = getValue(daten, "excelVorlage", b);
			checkFileExists(excelVorlage, "AusgabeSteuerItem.constructor", "excelVorlage");
		}
	}

	/**
	 * Gets a value from the given data map for the specified key. Throws an
	 * exception if the key is not found and b is true. Returns an empty string if
	 * the key is not found and b is false.
	 */
	private static String getValue(HashMap<String, String> data, String key, boolean b) {
		checkNotNull(data, "AusgabeSteuerItem.extracted: data is null");
		checkNotNull(key, "AusgabeSteuerItem.extracted: key is null");

		if (data.containsKey(key)) {
			return data.get(key);
		} else {
			if (b) {
				throw new RuntimeException("AusgabeSteuerItem.extracted: key " + key + " not found");
			}
		}
		return "";
	}

	/**
	 * Creates the output file by executing the SQL statements and writing to the
	 * output file.
	 *
	 * @param conn Database connection to use for executing SQL statements.
	 * @throws Exception If there is an error generating the output file.
	 */
	public void createAusgabeDatei(Connection conn) throws Exception {
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
	 * @throws Exception If there is an error generating the output files.
	 */
	public static void createAusgabeDateien(List<AusgabeSteuerItem> steuerItems, Connection conn) throws Exception {
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
	 * @throws Exception If there is an error reading the input or generating the
	 *                   output.
	 */
	public static void createAusgabeDateien(String steuerDatei, Connection conn) throws Exception {
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