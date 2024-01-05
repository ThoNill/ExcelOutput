/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.thonill;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.gui.ExcelOutputApplication;
import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;

/**
 * SqlTest provides tests for database connectivity and operations.
 */

public class ApplicationDialogTest extends SqlTest {
	private static final String PASSWORD = "";
	private static final String USER = "sa";
	public static final String url = "jdbc:h2:./build/tmp/test/h2test";
	private static final Random random = new Random();

	@BeforeAll
	public static void AppTestinit() {

		try {
			LOG.setLevel(Level.SEVERE);
			fillDb();

		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}



	private void callMain(String sqlFile, String steuerFile, String templateFile, String outputFile) {
		HashMap<String, String> args = new HashMap<>();
		addResource(args, StandardKeys.DB_DATEI, "testDb.properties");
		addResource(args, StandardKeys.SQL_DATEI, sqlFile);
		addResource(args, StandardKeys.STEUER_DATEI, steuerFile);
		addResource(args, StandardKeys.EXCEL_VORLAGE, templateFile);
		addOutput(args, StandardKeys.AUSGABE_DATEI, outputFile);
		try {
			new ExcelOutputApplication().main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// new ApplicationDialog().main(new String[] {"a"});
	}

	@Test
	public void callMain() {
		callMain("selectRechnungen.sql", null, null, "out1.xls");
		// callMain("selectRechnungen.sql", null, "RechnungVorlage.xls", "out1.xls");
	}

	private void addResource(HashMap<String, String> args, String key, String fileName) {
		if (fileName != null) {
			args.put(key, getResourcePath(fileName));
		}
	}

	private String getResourcePath(String fileName) {
		return "src\\test\\resources\\" + fileName;
	}

	private void addOutput(HashMap<String, String> args, String key, String fileName) {
		if (fileName != null) {
			args.put(key, getOutputPath(fileName));
		}
	}

	private String getOutputPath(String fileName) {
		return "build/tmp/test" + fileName;
	}
}