package org.thonill.ole.jacob;

import org.thonill.logger.LOG;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

/**
 * ExcelPivotTableUpdater updates all pivot tables in an Excel workbook.
 */

public class ExcelPivotTableUpdater {

	public static void main(String[] args) {
		ComThread.InitSTA();

		ActiveXComponent excel = new ActiveXComponent("Excel.Application");
		excel.setProperty("Visible", true);

		try {
			// Öffne eine Arbeitsmappe mit einer Pivot-Tabelle
			Dispatch workbooks = excel.getProperty("Workbooks").toDispatch();
			Dispatch workbook = Dispatch.call(workbooks, "Open", "C:\\Path\\To\\Your\\Workbook.xlsx").toDispatch();

			// Aktiviere das Arbeitsblatt mit der Pivot-Tabelle (angenommen, es ist das
			// erste Arbeitsblatt)
			Dispatch sheets = Dispatch.get(workbook, "Sheets").toDispatch();
			Dispatch sheet = Dispatch.call(sheets, "Item", 1).toDispatch();
			Dispatch.call(sheet, "Activate");

			// Aktualisiere alle Pivot-Tabellen im Arbeitsblatt
			Dispatch pivotTables = Dispatch.get(sheet, "PivotTables").toDispatch();
			int count = Dispatch.call(pivotTables, "Count").getInt();
			for (int i = 1; i <= count; i++) {
				Dispatch pivotTable = Dispatch.call(pivotTables, "Item", i).toDispatch();
				Dispatch.call(pivotTable, "PivotCache").toDispatch(); // Hier wird die Aktualisierung ausgelöst
			}

			// Speichere und schließe die Arbeitsmappe
			Dispatch.call(workbook, "Save");
			Dispatch.call(workbook, "Close", false);

		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		} finally {
			ComThread.Release();
		}
	}
}
