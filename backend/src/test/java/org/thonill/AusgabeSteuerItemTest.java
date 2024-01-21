package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.actions.FileCreator;
import org.thonill.checks.DefaultMapCheck;
import org.thonill.checks.MapCheck;
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
				FileCreator fc = new FileCreator();
				HashMap<String, String> map = new HashMap<>();
				map.put("kunden", "^ *[0-9]+ *(, *[0-9]+)* *$"); 
				map.put("monat", "^ *(1[0-2]|[0-9]) *(,1[0-2]|,[1-9])* *$"); 
				map.put("jahr", "^ *20[0-9][0-9] *(,20[0-9][0-9])* *$");

				MapCheck check = new DefaultMapCheck(map);
				fc.setCheckDaten(check);
				fc.createAusgabeDateien(new File("src\\test\\resources\\Steuerung.xls"), conn);
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Creations of files failed");

		}
	}
}
