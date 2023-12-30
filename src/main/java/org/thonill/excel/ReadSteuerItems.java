package org.thonill.excel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.thonill.actions.AusgabeSteuerItem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

/**
 * This class reads tax items from an Excel file.
 */

public class ReadSteuerItems {
     private static final Logger LOG = Logger.getLogger(AreaManager.class.getName());

    private String excelFileName;
    private List<String> header = new ArrayList<>();

    public ReadSteuerItems(String excelFileName) {
        this.excelFileName = excelFileName;
        checkNotNull(excelFileName, "ReadSteuerItems.ReadSteuerItems: excelFileName is null");
        checkFileExists(excelFileName, "ReadSteuerItems.ReadSteuerItems", "excelFileName");

    }

    public List<AusgabeSteuerItem> readSteuerItemsFromExcel() throws Exception {
        List<AusgabeSteuerItem> items = new ArrayList<>();

        // Open Excel workbook
        LOG.info("// Open Excel workbook");

        InputStream ExcelFileToRead = new FileInputStream(excelFileName);

        try (HSSFWorkbook workbook = new HSSFWorkbook(ExcelFileToRead)) {
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
    private void addItem(List<AusgabeSteuerItem> items, HSSFRow row, FormulaEvaluator evaluator) {
        checkNotNull(items, "ReadSteuerItems.addItem: items is null");
        checkNotNull(evaluator, "ReadSteuerItems.addItem: evaluator is null");

        // checkNotNull(row,"ReadSteuerItems.addItem: row is null");

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
                        LOG.info("key= " + header.get(column));
                        LOG.info("value= " + getValue(row, column + 1, evaluator));
                        data.put(header.get(column), getValue(row, column + 1, evaluator));
                    } else {
                        LOG.info("NN " + column);
                    }
                }
                items.add(new AusgabeSteuerItem(data));
                break;
            default:
                ;
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
                ;
        }
        return text.trim();
    }

}
