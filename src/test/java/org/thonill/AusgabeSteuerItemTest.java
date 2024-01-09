package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.actions.FileCreator;
import org.thonill.logger.LOG;

/**
 * Test class for FileCreator
 */
public class AusgabeSteuerItemTest extends SqlTest {

	@BeforeAll
	public static void AusgabeSteuerItemTestInit() {
		AppTestinit();
	}

	@Test
	void testCreateAusgabeDateien() {
		try {
			try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
				FileCreator.createAusgabeDateien("src\\test\\resources\\Steuerung.xls", conn);
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Creations of files failed");

		}
	}
}
