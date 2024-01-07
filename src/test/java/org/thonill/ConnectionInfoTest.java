package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.logger.LOG;
import org.thonill.sql.ConnectionInfo;

/**
 * Test class for ConnectionInfo
 */

public class ConnectionInfoTest extends SqlTest {

	@BeforeAll
	public static void ConnectionInfoTestInit() {
		AppTestinit();
		LOG.info("ConnectionInfoTestInit");

	}

	@Test
	void testGetConnection() {
		try {
			ConnectionInfo info = new ConnectionInfo("sa", "", "src\\test\\resources\\testDb.properties");
			try (Connection conn = info.createConnection()) {
				assert (conn != null);
			} catch (Exception e) {
				LOG.severe(e.getLocalizedMessage());
				fail("connection to db failed");
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("connection to db failed");
		}

	}

	@Test
	void testGetWrongUserConnection() {
		try {
			ConnectionInfo info = new ConnectionInfo("user", "", "src\\test\\resources\\testDb.properties");
			try (Connection conn = info.createConnection()) {
				assert (conn != null);
				fail("connection to db does not faile");
			} catch (Exception e) {
				LOG.severe(e.getLocalizedMessage());
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("connection to db failed");
		}

	}

}
