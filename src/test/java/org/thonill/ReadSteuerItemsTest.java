package org.thonill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.thonill.actions.FileCreator;
import org.thonill.excel.ReadSteuerItems;
import org.thonill.logger.LOG;

/**
 * Test class for reading SteuerItems from Excel
 */
public class ReadSteuerItemsTest {

	@Test
	void testReadSteuerItemsFromExcel() {
		try {
			ReadSteuerItems readSteuerItems = new ReadSteuerItems(new File("src\\test\\resources\\Steuerung.xls"));
			List<FileCreator> items = readSteuerItems.readSteuerItemsFromExcel();
			assertEquals(4, items.size());
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			fail("readSteuerItemsFromExcel failed");

		}
	}
}
