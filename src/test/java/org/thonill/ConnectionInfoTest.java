package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.sql.ConnectionInfo;

/**
 * Test class for ConnectionInfo
 */

public class ConnectionInfoTest extends SqlTest {
	private static final Logger LOG = Logger.getLogger(ConnectionInfoTest.class.getName());

	@BeforeAll
	public static void ConnectionInfoTestInit() {
		AppTestinit();
		LOG.info("ConnectionInfoTestInit");

	}

	@Test
	void testGetConnection() {
		try {
			ConnectionInfo info = new ConnectionInfo("testDb", "sa", "", "src\\test\\resources");
			Connection conn = info.getConnection();
			conn.close();
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("connection to db failed");
		}

	}

}
