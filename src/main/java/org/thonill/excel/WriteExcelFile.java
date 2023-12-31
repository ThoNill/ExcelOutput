package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.thonill.logger.LOG;
import org.thonill.sql.ResultOfStatments;

/**
 * This class provides functionality to write data from a ResultSet to an Excel
 * .xls file.
 */

public class WriteExcelFile {

	private FileInputStream in;

	public WriteExcelFile() {
		super();
	}

	private Workbook createWorkbook(String excelFileName) throws  EncryptedDocumentException, IOException {
		// Open Excel workbook
		in = new FileInputStream(excelFileName);
		return WorkbookFactory.create(in);
	}

	public void writeResultSetToExcel(String ausgabeDatei, String vorlageDatei, ResultOfStatments result)
			throws  EncryptedDocumentException, IOException {
		checkNotNull(result, "WriteExcel_xls.writeResultSetToExcel: result is null");
		checkNotNull(vorlageDatei, "WriteExcel_xls.writeResultSetToExcel: excelFileName is null");
		checkFileExists(vorlageDatei, "WriteExcel_xls.writeResultSetToExcel", "excelFileName");

		// Open Excel workbook
		LOG.info("// Open Excel workbook");

		try (Workbook workbook = createWorkbook(vorlageDatei)) {

			AreaManager areaManager = new AreaManager(workbook, result);
			areaManager.write();

			writeToFile(ausgabeDatei, workbook);
			in.close();
			// workbook.close(); don't close the workbook!

		}

	}

	private void writeToFile(String ausgabeDatei, Workbook workbook) throws IOException {
		// Write output
		LOG.info("// Write output");
		FileOutputStream fileOut = new FileOutputStream(ausgabeDatei);
		workbook.write(fileOut);
		fileOut.close();
	}

}