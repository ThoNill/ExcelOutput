package org.thonill.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.thonill.excel.WriteCSVFile;
import org.thonill.excel.WriteExcelFile;
import org.thonill.values.ArrayValue;

public class ExecutableStatementSet extends ArrayList<ExecutableStatement> {

	public ExecutableStatementSet() {
		super();
	}

	public void checkQuerys(Connection conn) throws  SQLException {
		for (ExecutableStatement statement : this) {
			statement.checkQuery(conn);
		}
	}

	public ResultOfStatments execute(Connection conn) throws  SQLException {
		ResultOfStatments results = new ResultOfStatments();
		for (ExecutableStatement statement : this) {
			statement.exportToResults(conn, results);
		}
		return results;
	}

	public void close() {
		for (ExecutableStatement statement : this) {
			statement.close();
		}
	}

	public void writeToOutputFile(Connection conn, String ausgabeDatei, String vorlageDatei) throws  SQLException, IOException {
		checkQuerys(conn);
		ResultOfStatments results = execute(conn);

		if (ausgabeDatei.endsWith(".csv")) {
			ArrayValue arrayValue = results.getArrays().iterator().next();
			WriteCSVFile.writeResultSetToCSV(ausgabeDatei, false, arrayValue);
		}
		if (ausgabeDatei.endsWith(".xls") || ausgabeDatei.endsWith(".xlsx")) {
			WriteExcelFile writer = new WriteExcelFile();
			writer.writeResultSetToExcel(ausgabeDatei, vorlageDatei, results);
		}
		close();
	}

}
