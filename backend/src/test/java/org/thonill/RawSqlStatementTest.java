package org.thonill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.thonill.replace.RawSqlStatement;
import org.thonill.replace.ReplaceDescription;
import org.thonill.sql.ExecutableStatement;

/**
 * Test class for RawSqlStatement
 */
public class RawSqlStatementTest {
	private static final Logger LOG = Logger.getLogger(RawSqlStatementTest.class.getName());

	@Test
	void testCreateReplaceDescriptions() {

		RawSqlStatement statement = new RawSqlStatement("SELECT * FROM table WHERE id = {id}");

		HashMap<String, String> map = new HashMap<>();
		map.put("id", "12");
		testStmt("SELECT * FROM table WHERE id = 12 ", statement, map);

		map.put("id", "12,13");
		testStmt("SELECT * FROM table WHERE id  in (12,13) ", statement, map);
	}

	private void testStmt(String expected, RawSqlStatement statement, HashMap<String, String> map) {
		List<ReplaceDescription> desc = RawSqlStatement.createReplaceDescriptions(map);

		// Validate it extracted the {id} parameter
		assertEquals(1, desc.size());
		ExecutableStatement stmt = statement.replaceVariables(desc);
		assertEquals(expected, stmt.getQueryString());
	}

	@Test
	void testGetQueryString() {

	}

	@Test
	void testGetRawSqlStatements() {
		try {
			File f = new File("src\\test\\resources\\sqlTest1.sql");
			String sqlQuerys = Files.readString(f.toPath());
			List<RawSqlStatement> statements = RawSqlStatement
					.getRawSqlStatements(sqlQuerys);

			for (RawSqlStatement statement : statements) {
				LOG.info(statement.getQueryString());
			}

			assertEquals(5, statements.size());
		} catch (Exception e) {
			fail("Exception");
		}
	}

	@Test
	void testReplaceVariables() {

	}

	@Test
	void testReplaceVariables2() {

	}
}
