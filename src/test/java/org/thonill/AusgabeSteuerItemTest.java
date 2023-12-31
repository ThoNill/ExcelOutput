package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.actions.AusgabeSteuerItem;

/**
 * Test class for AusgabeSteuerItem
 */
public class AusgabeSteuerItemTest extends SqlTest {
	private static final Logger LOG = Logger.getLogger(AusgabeSteuerItemTest.class.getName());

	@BeforeAll
	public static void AusgabeSteuerItemTestInit() {
		AppTestinit();
	}

	@Test
	void testCreateAusgabeDateien() {
		try {
			try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
				AusgabeSteuerItem.createAusgabeDateien("src\\test\\resources\\Steuerung.xls", conn);
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Creations of files failed");

		}
	}
}
