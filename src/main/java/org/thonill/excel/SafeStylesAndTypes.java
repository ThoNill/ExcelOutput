package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.thonill.values.Value;

public class SafeStylesAndTypes {

	private SafeStyleAndType[] safe;

	public SafeStylesAndTypes(Sheet sheet, int rowNum, int colNum) {
		checkNotNull(sheet, "SafeStylesAndTypes sheet is null");
		Row exampleRow = sheet.getRow(rowNum);
		if (exampleRow != null) {
			int maxCell = exampleRow.getLastCellNum() - colNum;
			safe = new SafeStyleAndType[maxCell];
			for (int i = 0; i < maxCell; i++) {
				safe[i] = new SafeStyleAndType(exampleRow.getCell(colNum + i));
			}
		}
	}

	public void insertRow(Sheet sheet, int rowNum, int colNum, Value[] values) {
		checkNotNull(sheet, "SafeStylesAndTypes sheet is null");
		checkNotNull(values, "SafeStylesAndTypes values is null");

		Row row = sheet.createRow(rowNum);
		int i = 0;
		int iValue = 0;
		int max = Math.max(values.length, getSize());
		for (i = 0; i < max; i++) {
			Cell cell = row.createCell(colNum + i);
			if (safe != null && i < safe.length) {
				safe[i].setCell(cell, values[iValue]);
				if (!safe[i].isFormula() && iValue < values.length - 1) {
					iValue++;
				}
			} else {
				if (iValue < values.length - 1) {
					cell.setCellValue(values[i].getString());
					iValue++;
				} else {
					cell.setBlank(); // Kann nicht vorkommen
				}
			}
		}

	}

	public int getSize() {
		return (safe == null) ? 0 : safe.length;
	}

}
