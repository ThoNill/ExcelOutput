package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.sql.Connection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.logger.LOG;
import org.thonill.sql.ConnectionCreator;

/**
 * Test class for ConnectionCreator
 */

public class ConnectionCreatorTest extends SqlTest {

	@BeforeAll
	public static void ConnectionInfoTestInit() {
		AppTestinit();
		LOG.info("ConnectionInfoTestInit");

	}

	@Test
	void testGetConnection() {
		try {
			ConnectionCreator info = createConnectionInfo();
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
			ConnectionCreator info = createConnectionInfo();
			info.setUserAndPassword("sa1"," ");
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
