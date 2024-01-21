/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.thonill;

import java.util.HashMap;
import java.util.logging.Level;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.checks.DefaultMapCheck;
import org.thonill.checks.MapCheck;
import org.thonill.gui.ExcelOutputApplication;
import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;

/**
 * SqlTest provides tests for database connectivity and operations.
 */

public class ApplicationDialogTest extends SqlGuiTest {
	public static final String url = "jdbc:h2:./build/tmp/test/h2test";

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
		ExcelOutputApplication app = new ExcelOutputApplication();
		setDatenChecker(app);
		app.main(args);
	}

	private void setDatenChecker(ExcelOutputApplication app) {
		HashMap<String,String> map = new HashMap();
		map.put("kunden", "^ *[0-9]+ *(, *[0-9]+)* *$"); 
		map.put("monat", "^ *(1[0-2]|[0-9]) *(,1[0-2]|,[1-9])* *$"); 
		map.put("jahr", "^ *20[0-9][0-9] *(,20[0-9][0-9])* *$");
		
		// zusätzlich zur Ausgabe von Steuerdateien
		map.put("kunde", "^ *[0-9]+ *$");
		map.put("sqlDatei", "^[0-9a-zA-Z\\\\/]*\\.sql$");
		map.put("dbDatei", "^[0-9a-zA-Z\\\\/]*\\.properties$");
		map.put("ausgabeDatei", "^[0-9a-zA-Z\\\\/]*\\.(xls|xlsx|csv)$");
		map.put("excelVorlage", "^[0-9a-zA-Z\\\\/]*\\.(xls|xlsx)$");
		
		MapCheck check = new DefaultMapCheck(map);
		app.setCheckDaten(check);
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