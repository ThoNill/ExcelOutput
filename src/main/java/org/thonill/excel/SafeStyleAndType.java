package org.thonill.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.thonill.values.Value;

public class SafeStyleAndType {
	private CellStyle style;
	private CellType type;
	private FormulaHelper formulaHelper;

	public SafeStyleAndType(Cell cell) {
		style = cell.getCellStyle();
		type = cell.getCellType();
		if (cell.getCellType() == CellType.FORMULA) {
			formulaHelper = new FormulaHelper(cell);
		}
	}

	public boolean isFormula() {
		return formulaHelper != null;
	}

	public void setCell(Cell cell, Value value) {
		cell.setCellStyle(style);
		if (isFormula()) {
			formulaHelper.copyFormula(cell);
		} else {
			setCellValue(type, cell, value);
		}
	}

	public static void setCellValue(CellType safeCellType, Cell cell, Value value) {
		switch (safeCellType) {
		case STRING:
			cell.setCellValue(value.getString());
			break;
		case NUMERIC:
			cell.setCellValue(value.getDouble());
			break;
		case BOOLEAN:
			cell.setCellValue(value.getBoolean());
			break;
		case BLANK:
			cell.setBlank();
			break;
		case ERROR:
			cell.setCellFormula(value.getString());
			break;
		case FORMULA:
			cell.setCellFormula(value.getString());
			break;
		default:
			cell.setCellValue(value.getString());
			break;
		}
	}

}
