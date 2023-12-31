package org.thonill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.thonill.actions.AusgabeSteuerItem;
import org.thonill.excel.ReadSteuerItems;

/**
 * Test class for reading SteuerItems from Excel
 */

public class ReadSteuerItemsTest {

	private static final Logger LOG = Logger.getLogger(ReadSteuerItemsTest.class.getName());

	@Test
	void testReadSteuerItemsFromExcel() {
		try {
			ReadSteuerItems readSteuerItems = new ReadSteuerItems("src\\test\\resources\\Steuerung.xls");
			List<AusgabeSteuerItem> items = readSteuerItems.readSteuerItemsFromExcel();
			assertEquals(4, items.size());
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("readSteuerItemsFromExcel failed");

		}
	}
}
