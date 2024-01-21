package org.thonill.excel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.thonill.actions.FileCreator;
import org.thonill.logger.LOG;

/**
 * This class reads tax items from an Excel file.
 */

public class ReadSteuerItems {

	private File excelFileName;
	private List<String> header = new ArrayList<>();

	public ReadSteuerItems(File excelFileName) {
		this.excelFileName = excelFileName;
		checkNotNull(excelFileName, "ReadSteuerItems.ReadSteuerItems: excelFileName is null");

	}

	public List<FileCreator> readSteuerItemsFromExcel() throws IOException {
		List<FileCreator> items = new ArrayList<>();

		// Open Excel workbook
		LOG.info("// Open Excel workbook");

		InputStream excelFileToRead = new FileInputStream(excelFileName);

		try (HSSFWorkbook workbook = new HSSFWorkbook(excelFileToRead)) {
			// Get first sheet

			checkArgument(workbook.getNumberOfSheets() > 0,
					"ReadSteuerItems.readSteuerItemsFromExcel: workbook.getNumberOfSheets() is not 0");

			LOG.info("// Get first sheet");
			HSSFSheet sheet = workbook.getSheetAt(0);

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			// Clears existing rows from the Excel sheet before writing new data.
			LOG.info("// Clears existing rows from the Excel sheet before writing new data.");
			int maxRow = sheet.getLastRowNum();

			checkArgument(maxRow > 0,
					"ReadSteuerItems.readSteuerItemsFromExcel: workbook.getNumberOfSheets() is not 0");

			for (int i = 0; i < maxRow; i++) {
				HSSFRow row = sheet.getRow(i);
				if (row != null) {
					addItem(items, row, evaluator);
				}

			}

		}
		return items;
	}

	/**
	 * @param items
	 * @param row
	 */
	private void addItem(List<FileCreator> items, HSSFRow row, FormulaEvaluator evaluator) {
		checkNotNull(items, "ReadSteuerItems.addItem: items is null");
		checkNotNull(evaluator, "ReadSteuerItems.addItem: evaluator is null");

		if (row == null || row.getLastCellNum() == 0) {
			return;
		}
		String item = getValue(row, 0, evaluator);
		switch (item) {
		case "Felder":
			for (int column = 1; column < row.getLastCellNum(); column++) {
				header.add(getValue(row, column, evaluator));
			}
			break;
		case "Item":
			HashMap<String, String> data = new HashMap<>();
			for (int column = 0; column < row.getLastCellNum(); column++) {
				if (column < header.size()) {
					LOG.info("key= {0}", header.get(column));
					LOG.info("value= {0} ", getValue(row, column + 1, evaluator));
					data.put(header.get(column), getValue(row, column + 1, evaluator));
				} else {
					LOG.info("NN {0} ", column);
				}
			}
			items.add(new FileCreator(data));
			break;
		default:
			break;
		}
	}

	private static String getValue(HSSFRow row, int column, FormulaEvaluator evaluator) {
		checkNotNull(row, "ReadSteuerItems.getValue: row is null");
		checkNotNull(evaluator, "ReadSteuerItems.getValue: evaluator is null");

		if (column > row.getLastCellNum()) {
			return "";
		}
		HSSFCell cell = row.getCell(column);
		if (cell == null) {
			return "";
		}
		checkNotNull(cell, "ReadSteuerItems.getValue: cell is null");

		String text = "Error";
		CellType ct;

		if (cell.getCellType() == CellType.FORMULA) {
			ct = evaluator.evaluateFormulaCell(cell);
		} else {
			ct = cell.getCellType();
		}
		switch (ct) {
		case BOOLEAN:
			text = "" + cell.getBooleanCellValue();
			break;
		case NUMERIC:
			text = "" + cell.getNumericCellValue();
			break;
		case STRING:
			text = cell.getStringCellValue();
			break;
		default:
			break;
		}
		return text.trim();
	}

}
