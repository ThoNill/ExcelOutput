package org.thonill.excel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

import com.google.common.base.Charsets;

/**
 * WriteCSVFile provides utility methods for writing a ResultSet to a CSV file.
 */

public class WriteCSVFile {

	private WriteCSVFile() {
		super();
	}

	public static void writeResultSetToCSV(String filePath, boolean writeHeaders, ArrayValue arrayValue)
			throws IOException, SQLException {
		checkNotNull(arrayValue, "WriteCSVFile.writeResultSetToCSV: arrayValue is null");
		checkNotNull(filePath, "WriteCSVFile.writeResultSetToCSV: filePath is null");

		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(';').setRecordSeparator("\n").build();
		try (FileWriter fileWriter = new FileWriter(filePath, Charsets.UTF_8);
				CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFormat)) {

			int columnCount = arrayValue.size();
			Value[] values = arrayValue.getValues();
			writeHeadersToCSV(csvPrinter, values, writeHeaders);

			do {
				Object[] row = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
					row[i] = values[i].getCSVValue();
				}
				csvPrinter.printRecord(row);
			} while (arrayValue.next());

		}

	}

	private static void writeHeadersToCSV(CSVPrinter csvPrinter, Value[] values, boolean writeHeaders)
			throws SQLException, IOException {
		if (writeHeaders) {
			Object[] headers = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				headers[i - 1] = values[i].getName();
			}
			csvPrinter.printRecord(headers);
		}
	}

}
