package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

public class SheetArea {

	private static final Logger LOG = Logger.getLogger(SheetArea.class.getName());

	private SpreadsheetVersion ssVersion = SpreadsheetVersion.EXCEL2007;
	private Sheet sheet;
	private Name bereichsName = null;
	private ArrayValue arrayValue;
	private int bereichFirstRow = 0;
	private int bereichLastRow = 0;
	private int bereichFirstCol = 0;

	public SheetArea(Workbook workbook, Name bereichsName, ArrayValue arrayValue) {
		checkNotNull(workbook, "SafeStylesAndTypes workbook is null");
		checkNotNull(bereichsName, "SafeStylesAndTypes bereichsName is null");
		checkNotNull(arrayValue, "SafeStylesAndTypes value is null");

		this.arrayValue = arrayValue;
		this.sheet = workbook.getSheetAt(bereichsName.getSheetIndex());

		this.ssVersion = workbook.getSpreadsheetVersion();
		this.bereichsName = bereichsName;
		init();
	}

	public void writeToSheet() {
		Sheet safeSheet = safeRowsAbove(bereichLastRow + 1);

		SafeStylesAndTypes safeCells = new SafeStylesAndTypes(sheet, bereichFirstRow + 1, bereichFirstCol);

		removeRowsAbove(bereichFirstRow);

		// Write data rows
		LOG.info("// Write data rows");
		int rowNum = bereichFirstRow + 1;
		Value[] values = arrayValue.getValues();
		do {
			safeCells.insertRow(sheet, rowNum++, bereichFirstCol, values);
		} while (arrayValue.next());

		restoreRowsAbove(rowNum, safeSheet);
		changeDimensions(rowNum, Math.max(values.length, safeCells.getSize()));

		resizeColumns(values);
	}

	private void resizeColumns(Value[] values) {
		LOG.info("// Auto-size columns");
		for (int i = bereichFirstCol; i < values.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void changeDimensions(int rowNum, int colNum) {
		LOG.info("// Change Berech Dimension");
		if (bereichsName != null) {

			CellReference begin = new CellReference(bereichFirstRow, bereichFirstCol, true, true);
			CellReference end = new CellReference(rowNum - 1, bereichFirstCol + colNum - 1, true, true);

			AreaReference aref = new AreaReference(begin, end, ssVersion);
			LOG.info("AreaReferenz: " + aref.formatAsString());
			bereichsName.setRefersToFormula(aref.formatAsString());
		}
	}

	private AreaReference getAreaReference() {

		String reference = bereichsName.getRefersToFormula();
		// Determine the type of the reference
		NameType nameType = CellReference.classifyCellReference(reference, ssVersion);
		// Assuming cellReference is something like "Sheet1!A1"
		if (nameType.equals(NameType.BAD_CELL_OR_NAMED_RANGE) || nameType.equals(NameType.NAMED_RANGE)) {
			AreaReference aref = new AreaReference(reference, ssVersion);
			return aref;
		}
		return null;
	}

	private Sheet safeRowsAbove(int row) {
		Sheet safeSheet = sheet.getWorkbook().createSheet();
		moveRowsAbove(0, safeSheet, row, sheet, true);
		return safeSheet;
	}

	private void restoreRowsAbove(int row, Sheet safeSheet) {
		moveRowsAbove(row, sheet, 0, safeSheet, false);
		Workbook workbook = safeSheet.getWorkbook();
		workbook.removeSheetAt(workbook.getSheetIndex(safeSheet));
	}

	private static void moveRowsAbove(int toRow, Sheet toSheet, int fromRow, Sheet fromSheet, boolean formulaRoString) {
		int maxRow = fromSheet.getLastRowNum();
		for (int i = fromRow; i <= maxRow; i++) {
			Row rowToBeCopied = fromSheet.getRow(i);
			if (rowToBeCopied != null) {
				Row newRow = toSheet.createRow(i - fromRow + toRow);
				for (int j = 0; j < rowToBeCopied.getLastCellNum(); j++) {
					Cell cellToBeCopied = rowToBeCopied.getCell(j);
					if (cellToBeCopied != null) {
						Cell cellToBeAdded = newRow.createCell(j);
						cellToBeAdded.setCellStyle(cellToBeCopied.getCellStyle());
						// cellToBeAdded.setCellType(cellToBeCopied.getCellType());
						switch (cellToBeCopied.getCellType()) {
						case NUMERIC:
							cellToBeAdded.setCellValue(cellToBeCopied.getNumericCellValue());
							break;
						case STRING:
							String text = cellToBeCopied.getStringCellValue();
							if (!formulaRoString && text.startsWith("FORMULA(")) {
								cellToBeAdded.setCellFormula(text.substring("FORMULA(".length()));
							} else {
								cellToBeAdded.setCellValue(text);
							}
							break;
						case BOOLEAN:
							cellToBeAdded.setCellValue(cellToBeCopied.getBooleanCellValue());
							break;
						case ERROR:
							cellToBeAdded.setCellErrorValue(cellToBeCopied.getErrorCellValue());
							break;
						case FORMULA:
							String formula = cellToBeCopied.getCellFormula();
							LOG.info("Formula: " + formula);
							if (formulaRoString) {
								cellToBeAdded.setCellValue("FORMULA(" + formula);
							} else {
								cellToBeAdded.setCellFormula(formula);
							}
							break;
						case BLANK:
							break;
						case _NONE:
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + cellToBeCopied.getCellType());

						}
					}
				}
			}
		}
	}

	private void removeRowsAbove(int startRow) {
		int maxRow = sheet.getLastRowNum();
		for (int i = maxRow; i > startRow; i--) {
			Row row = sheet.getRow(i);
			if (row != null) {
				sheet.removeRow(row);
			}
		}
	}

	private void init() {
		LOG.info("do something");
		AreaReference areaReference = getAreaReference();
		if (areaReference == null) {
			throw new IllegalArgumentException("AreaReference of " + bereichsName.getNameName() + " is not an area");
		}
		bereichFirstRow = areaReference.getFirstCell().getRow();
		bereichFirstCol = areaReference.getFirstCell().getCol();
		bereichLastRow = areaReference.getLastCell().getRow();

		LOG.info(" bereichFirstRow " + bereichFirstRow);
		LOG.info(" bereichLastRow " + bereichLastRow);
		LOG.info(" bereichFirstCol " + bereichFirstCol);
	}
}
