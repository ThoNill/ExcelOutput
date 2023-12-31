package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.thonill.values.Value;

/**
 * The ValueCell class represents a cell that contains a value. It is used to
 * write values to cells in a sheet.
 */
public class ValueCell {
	private static final Logger LOG = Logger.getLogger(ValueCell.class.getName());

	private SpreadsheetVersion ssVersion = SpreadsheetVersion.EXCEL2007;
	private Sheet sheet;
	private Name bereichsName = null;
	private Value value = null;

	public ValueCell(Workbook workbook, Name bereichsName, Value value) {
		checkNotNull(workbook, "SafeStylesAndTypes workbook is null");
		checkNotNull(bereichsName, "SafeStylesAndTypes bereichsName is null");
		checkNotNull(value, "SafeStylesAndTypes value is null");

		this.value = value;
		this.sheet = workbook.getSheetAt(bereichsName.getSheetIndex());
		this.ssVersion = workbook.getSpreadsheetVersion();
		this.bereichsName = bereichsName;
	}

	/**
	 * Writes the value to the appropriate cell on the sheet based on the cell
	 * reference in the name. Gets or creates the row and cell if needed, logs the
	 * value and location, and sets the cell value using the safe style and type
	 * utility.
	 */
	public void writeToSheet() {

		String cellReference = bereichsName.getRefersToFormula();

		// Determine the type of the reference
		NameType nameType = CellReference.classifyCellReference(cellReference, ssVersion);
		// Assuming cellReference is something like "Sheet1!A1"
		if (nameType.equals(NameType.CELL) || nameType.equals(NameType.BAD_CELL_OR_NAMED_RANGE)) {
			CellReference reference = new CellReference(cellReference);
			int row = reference.getRow();
			int col = reference.getCol();

			// Get or create the row and cell
			Row sheetRow = sheet.getRow(row);
			if (sheetRow == null) {
				sheetRow = sheet.createRow(row);
			}
			Cell cell = sheetRow.getCell(col);
			if (cell == null) {
				cell = sheetRow.createCell(col);
			}
			LOG.info("value = " + value.getString());
			LOG.info("ort = " + row + " " + col);
			// Set the cell value
			SafeStyleAndType.setCellValue(cell.getCellType(), cell, value);
		}
	}

}
