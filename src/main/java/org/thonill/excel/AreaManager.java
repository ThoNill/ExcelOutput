package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.thonill.exceptions.ApplicationException;
import org.thonill.logger.LOG;
import org.thonill.sql.ResultOfStatments;
import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

/**
 * The AreaManager class manages defined areas and cells in an Excel workbook.
 *
 * It initializes the areas and cells based on defined names in the workbook.
 * Areas represent ranges of cells defined by a name. Cells represent single
 * cell values defined by a name.
 *
 * The main functions are: - Constructor: Initializes areas and cells from
 * workbook names - write(): Writes the areas and cells back to the workbook
 *
 * It contains utility functions to: - Add a default area if no areas are
 * defined - Add areas and cells based on names - Search for an area name if
 * none are found
 */
public class AreaManager {

	private List<SheetArea> areas = new ArrayList<>();
	private List<ValueCell> cells = new ArrayList<>();

	public AreaManager(Workbook workbook, ResultOfStatments result) {
		checkNotNull(workbook, "The wworkbook is null");
		checkNotNull(result, " result is null");

		for (Name name : workbook.getAllNames()) {
			LOG.info("Name: {0} {1} " , name.getNameName(),name.getSheetIndex());

			addCell(workbook, result, name);

			addArea(workbook, result, name);
		}

		if (areas.isEmpty()) {
			ArrayValue arrayValue = result.getArrays().iterator().next();
			Name name = searchArea(workbook, result);
			if (name != null) {
				addArea(workbook, name, arrayValue);
			} else {
				addDATENArea(workbook, arrayValue);
			}
		}
	}

	public void write() {
		for (SheetArea area : areas) {
			area.writeToSheet();
		}
		for (ValueCell cell : cells) {
			cell.writeToSheet();
		}
	}

	private void addDATENArea(Workbook workbook, ArrayValue arrayValue) {
		LOG.info("No areas found");

		Name name = workbook.createName();
		name.setNameName("DATEN");
		name.setSheetIndex(0);

		Sheet sheet = workbook.getSheetAt(0);
		CellReference begin = new CellReference(0, 0, true, true);
		CellReference end = new CellReference(sheet.getLastRowNum(), arrayValue.size() - 1, true, true);

		AreaReference aref = new AreaReference(begin, end, workbook.getSpreadsheetVersion());

		LOG.info("AreaReferenz: {0} ",aref.formatAsString());
		name.setRefersToFormula(aref.formatAsString());
		addArea(workbook, name, arrayValue);
	}

	private void addCell(Workbook workbook, ResultOfStatments result, Name name) {
		Value value = result.getSingleObject(name.getNameName());
		if (value != null) {
			if (name.getSheetIndex() >= 0) {
				LOG.info("Name: " + name.getNameName() + " " + name.getSheetIndex() + " " + value.toString());
				cells.add(new ValueCell(workbook, name, value));
			} else {
				throw new ApplicationException("Name " + name.getNameName() + " has no sheet index");
			}
		}
	}

	private void addArea(Workbook workbook, ResultOfStatments result, Name name) {
		ArrayValue arrayValue = result.getArray(name.getNameName());
		if (arrayValue != null) {
			addArea(workbook, name, arrayValue);
		}
	}

	private void addArea(Workbook workbook, Name name, ArrayValue arrayValue) {
		if (name.getSheetIndex() >= 0) {
			LOG.info("Name: {0} {1} {2} " , name.getNameName() , name.getSheetIndex(),arrayValue.toString());
			areas.add(new SheetArea(workbook, name, arrayValue));
		} else {
			throw new ApplicationException("Name " + name.getNameName() + " has no sheet index");
		}
	}

	private Name searchArea(Workbook workbook, ResultOfStatments result) {
		for (Name name : workbook.getAllNames()) {
			LOG.info("Name: " + name.getNameName() + " " + name.getSheetIndex());

			Value value = result.getSingleObject(name.getNameName());
			if (value == null && name.getSheetIndex() == 0) {
				return name;
			}
		}
		return null;
	}

}
