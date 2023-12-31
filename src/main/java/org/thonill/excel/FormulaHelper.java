package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * https://stackoverflow.com/questions/1636759/poi-excel-applying-formulas-in-a-relative-way
 */
/**
 * Helper class for working with Excel formulas. Allows copying a formula to
 * another cell while adjusting relative cell references. Uses POI classes for
 * parsing and rendering formulas.
 */

public class FormulaHelper {

	private Ptg[] ptgs;
	private int currentRow;
	private int currentColumn;
	private EvaluationWorkbook workbookWrapper;

	public FormulaHelper(Cell org) {

		checkNotNull(org, "FormulaHelper constructor org is null");

		Sheet sheet = org.getSheet();
		if (sheet == null || org.getCellType() != CellType.FORMULA || org.isPartOfArrayFormulaGroup())
			return;
		this.currentRow = org.getRowIndex();
		this.currentColumn = org.getColumnIndex();

		String formula = org.getCellFormula();
		workbookWrapper = createWorkbookWrapper(sheet);
		this.ptgs = FormulaParser.parse(formula, (FormulaParsingWorkbook) workbookWrapper, FormulaType.CELL,
				sheet.getWorkbook().getSheetIndex(sheet));
	}

	public void copyFormula(Cell dest) {
		checkNotNull(dest, "FormulaHelper dest is null");

		int shiftRows = dest.getRowIndex() - currentRow;
		int shiftCols = dest.getColumnIndex() - currentColumn;
		currentRow = dest.getRowIndex();
		currentColumn = dest.getColumnIndex();
		for (Ptg ptg : ptgs) {
			if (ptg instanceof RefPtgBase) // base class for cell references
			{
				RefPtgBase ref = (RefPtgBase) ptg;
				if (ref.isColRelative())
					ref.setColumn(ref.getColumn() + shiftCols);
				if (ref.isRowRelative())
					ref.setRow(ref.getRow() + shiftRows);
			} else if (ptg instanceof AreaPtg) // base class for range references
			{
				AreaPtg ref = (AreaPtg) ptg;
				if (ref.isFirstColRelative())
					ref.setFirstColumn(ref.getFirstColumn() + shiftCols);
				if (ref.isLastColRelative())
					ref.setLastColumn(ref.getLastColumn() + shiftCols);
				if (ref.isFirstRowRelative())
					ref.setFirstRow(ref.getFirstRow() + shiftRows);
				if (ref.isLastRowRelative())
					ref.setLastRow(ref.getLastRow() + shiftRows);
			}
		}
		String formula = FormulaRenderer.toFormulaString((FormulaRenderingWorkbook) workbookWrapper, ptgs);
		dest.setCellFormula(formula);
	}

	private EvaluationWorkbook createWorkbookWrapper(Sheet sheet) {

		Workbook workbook = sheet.getWorkbook();
		if (workbook instanceof XSSFWorkbook) {
			return XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook);
		}
		if (workbook instanceof HSSFWorkbook) {
			return HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook);
		}
		throw new IllegalArgumentException("Unsupported workbook type: " + workbook.getClass());

	}

}
