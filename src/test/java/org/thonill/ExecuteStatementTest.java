package org.thonill;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.logger.LOG;
import org.thonill.sql.ExecutableStatement;
import org.thonill.sql.ExecutableStatementSet;
/**
 * Test class for executing SQL statements.
 */

public class ExecuteStatementTest extends SqlTest {

	private static final String PASSWORD = "";
	private static final String USER = "sa";

	@BeforeAll
	public static void ExecuteStatementTestInit() {
		AppTestinit();
	}

	@Test
	void testExecuteStatement() {
		// Test successful query execution
		try {
			try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
				assertValidWithResultSet(conn, "SELECT * FROM kunden");
				assertValidWithoutResultSet(conn, "UPDATE kunden set name = 'Test' where id = 1");
				assertInvalid(conn, "Uinvalid");
			}
		} catch (SQLException e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed");
		}
	}

	@Test
	void testCanPrepareQuery() {
		// Test successful query execution
		try {
			try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
				assertPrepare(conn, true, "SELECT * FROM kunden");
				assertPrepare(conn, true, "UPDATE kunden set name = 'Test' where id = 1");
				assertPrepare(conn, false, "Uinvalid");
			}
		} catch (SQLException e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed");
		}
	}

	private void assertPrepare(Connection conn, boolean valid, String query) {

		ExecutableStatement statement = new ExecutableStatement(query);
		try {
			statement.checkQuery(conn);
			if (!valid) {
				LOG.severe("Invalid Query: " +query);
				fail("Query is invalid");
			}
		} catch (Exception e) {
			if (valid) {
				LOG.severe(e.getLocalizedMessage());
				fail("Query is invalid");
			}
		}
	}

	private void assertValidWithResultSet(Connection conn, String query) {

		ExecutableStatement statement = new ExecutableStatement(query);
		try {
			assertTrue(statement.checkIsSelectStatement(conn), "Statment is not a select");
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query is invalid");
		}
	}

	private void assertValidWithoutResultSet(Connection conn, String query) {

		ExecutableStatement statement = new ExecutableStatement(query);
		try {
			assertFalse(statement.checkIsSelectStatement(conn), "Statment is a select");
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed");
		}
	}

	private void assertInvalid(Connection conn, String query) {

		ExecutableStatement statement = new ExecutableStatement(query);
		try {
			statement.checkIsSelectStatement(conn);
			if (statement.isOk()) {
				fail("Query is invalid");
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	@Test
	public void exportToExcelTest() {

		try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {

			ExecutableStatementSet statementSet = new ExecutableStatementSet();
			statementSet.add(new ExecutableStatement("SELECT id as __id, name, ort FROM kunden"));
			statementSet.writeToOutputFile(conn, "build\\tmp\\test\\TestAusgabe.xls",
					"src\\test\\resources\\TestVorlage.xls");

		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed" + e.getMessage());
		}
	}

	@Test
	public void exportToExcelTest2() {

		try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
			ExecutableStatementSet statementSet = new ExecutableStatementSet();
			statementSet.add(new ExecutableStatement("SELECT * FROM rechnung where kunde in (1,2) order by kunde "));
			statementSet.writeToOutputFile(conn, "build\\tmp\\test\\TestAusgabeRechnung.xls",
					"src\\test\\resources\\RechnungVorlage.xls");

			statementSet = new ExecutableStatementSet();
			statementSet.add(new ExecutableStatement("SELECT * FROM rechnung where kunde in (1,2) order by kunde "));
			statementSet.add(new ExecutableStatement("SELECT name as _KundenName FROM kunden  where id = 5 "));

			statementSet.writeToOutputFile(conn, "build\\tmp\\test\\TestAusgabeRechnungBereich.xls",
					"src\\test\\resources\\RechnungVorlageBereich.xls");

			statementSet = new ExecutableStatementSet();
			statementSet.add(new ExecutableStatement("SELECT * FROM rechnung where kunde in (1,2) order by kunde "));
			statementSet.writeToOutputFile(conn, "build\\tmp\\test\\TestAusgabeRechnung.xlsx",
					"src\\test\\resources\\RechnungVorlage.xlsx");
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed" + e.getMessage());
		}
	}

	@Test
	public void exportToCSV() {

		try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
			ExecutableStatementSet statementSet = new ExecutableStatementSet();
			statementSet.add(new ExecutableStatement("SELECT * FROM rechnung where kunde in (1,2) order by kunde "));
			statementSet.writeToOutputFile(conn, "build\\tmp\\test\\TestAusgabeRechnung.csv", null);

		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Query execution failed" + e.getMessage());
		}
	}
}
