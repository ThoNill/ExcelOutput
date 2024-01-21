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
		try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
				FileCreator fc = new FileCreator();
				HashMap<String, String> map = new HashMap<>();
				map.put("kunde", "^ *[0-9]+ *$");
				map.put("kunden", "^ *[0-9]+ *(, *[0-9]+)* *$");
				map.put("monat", "^ *(1[0-2]|[0-9]) *(,1[0-2]|,[1-9])* *$");
				map.put("jahr", "^ *20[0-9][0-9] *(,20[0-9][0-9])* *$");

				map.put("sqlDatei", "^[0-9a-zA-Z\\\\/]*\\.sql$");
				map.put("ausgabeDatei", "^[0-9a-zA-Z\\\\/]*\\.(xls|xlsx|csv)$");
				map.put("excelVorlage", "^[0-9a-zA-Z\\\\/]*\\.(xls|xlsx)$");
				MapCheck check = new DefaultMapCheck(map);
				fc.setCheckDaten(check);
				fc.createAusgabeDateien(new File("src\\test\\resources\\Steuerung.xls"), conn);
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("Creations of files failed");

		}
	}
}
