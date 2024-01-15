/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.logger.LOG;
import org.thonill.sql.ConnectionInfo;

/**
 * SqlTest provides tests for database connectivity and operations.
 */

public class SqlTest {
	private static final String PASSWORD = "";
	private static final String USER = "sa";
	public static final String url = "jdbc:h2:./build/tmp/test/h2test";
	private static final Random random = new Random();

	@BeforeAll
	public static void AppTestinit() {

		try {
			fillDb();
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	public static void AppTestinit1() {

		try {
			ConnectionInfo info = new ConnectionInfo(USER, PASSWORD,
					new File("src\\test\\resources\\testDb.properties"));
			try (Connection conn = info.createConnection()) {
				fillDb(conn);
			} catch (Exception e) {
				LOG.severe(e.getLocalizedMessage());
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	public static void fillDb() throws SQLException {

		// Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {

			fillDb(conn);

		}

	}

	private static void fillDb(Connection conn) throws SQLException {
		if (!conn.getMetaData().getTables(null, null, "KUNDEN", null).next()) {
			try (Statement stmt = conn.createStatement()) {
				createKundenTabelle(stmt);

			}

		} else {
			LOG.info("Table KUNDEN exists");
		}
		if (!conn.getMetaData().getTables(null, null, "RECHNUNG", null).next()) {
			try (Statement stmt = conn.createStatement()) {

				createRechnungTabelle(stmt);

			}

		} else {
			LOG.info("Table RECHNUNG exists");
		}
	}

	private static void createKundenTabelle(Statement stmt) throws SQLException {
		execute(stmt, "CREATE TABLE kunden (id INTEGER PRIMARY KEY, name VARCHAR(255), ort VARCHAR(255))");

		String[] names = { "thomas", "emil", "anna", "martha", "friedrich", "emil", "kurt", "tobias", "marhta",
				"anke" };

		for (int i = 0; i < 10; i++) {
			stmt.executeUpdate(
					"INSERT INTO kunden (id, name, ort) VALUES (" + i + ", '" + names[i] + "', 'Ort " + i + "')");
		}
	}

	private static void createRechnungTabelle(Statement stmt) throws SQLException {
		execute(stmt,
				"CREATE TABLE rechnung (id INTEGER PRIMARY KEY, kunde INTEGER, netto DECIMAL(10,2), mwst DECIMAL(10,2), skonto DECIMAL(10,2), rabatt DECIMAL(10,2))");

		for (int i = 1; i <= 100; i++) {
			int kunde = random.nextInt(10) + 1;
			BigDecimal netto = BigDecimal.valueOf(random.nextDouble() * 100).setScale(2, RoundingMode.HALF_UP).abs();
			BigDecimal mwst = netto.multiply(BigDecimal.valueOf(0.19)).setScale(2, RoundingMode.HALF_UP).abs();
			BigDecimal skonto = netto.multiply(BigDecimal.valueOf(random.nextDouble() * 0.1))
					.setScale(2, RoundingMode.HALF_UP).abs();
			BigDecimal rabatt = netto.multiply(BigDecimal.valueOf(random.nextDouble() * 0.1))
					.setScale(2, RoundingMode.HALF_UP).abs();

			String insertSql = "INSERT INTO rechnung (id, kunde, netto, mwst, skonto, rabatt) VALUES (" + i + ", "
					+ kunde + ", " + netto + ", " + mwst + ", " + skonto + ", " + rabatt + ")";
			stmt.executeUpdate(insertSql);
		}
	}

	@Test
	public void doSomething() {
		try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs = stmt.executeQuery("SELECT * FROM kunden")) {

					while (rs.next()) {
						LOG.info(rs.getInt("ID") + ": " + rs.getString("NAME"));
					}
				} catch (Exception e) {
					fail();
				}

				String searchName = "emil";
				try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM kunden WHERE name = ?")) {
					ps.setString(1, searchName);
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							LOG.info(rs.getInt("ID") + ": " + rs.getString("NAME") + " from " + rs.getString("ORT"));
						}
					} catch (Exception e) {
						fail();
					}
				} catch (Exception e) {
					fail();
				}
			} catch (Exception e) {
				fail();
			}
		} catch (Exception e) {
			fail();
		}
	}

	public static void execute(Statement sqlStatement, String query) throws SQLException {
		LOG.info(query);
		try {
			sqlStatement.execute(query);
		} catch (SQLException e) {
			LOG.info("Query failed");
			LOG.severe(e.getLocalizedMessage());
			fail("connection to db failed");
			throw e;
		}
	}
}