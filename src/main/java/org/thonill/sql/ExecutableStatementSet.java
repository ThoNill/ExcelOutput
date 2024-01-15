package org.thonill.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.thonill.excel.WriteCSVFile;
import org.thonill.excel.WriteExcelFile;
import org.thonill.values.ArrayValue;

public class ExecutableStatementSet extends ArrayList<ExecutableStatement> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExecutableStatementSet() {
		super();
	}

	public void checkQuerys(Connection conn) throws SQLException {
		for (ExecutableStatement statement : this) {
			statement.checkQuery(conn);
		}
	}

	public ResultOfStatments execute(Connection conn) throws SQLException {
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

	public void writeToOutputFile(Connection conn, File ausgabeDatei, File vorlageDatei)
			throws SQLException, IOException {
		checkQuerys(conn);
		ResultOfStatments results = execute(conn);
		String fileName = ausgabeDatei.getName();
		if (fileName.endsWith(".csv")) {
			ArrayValue arrayValue = results.getArrays().iterator().next();
			WriteCSVFile.writeResultSetToCSV(ausgabeDatei, false, arrayValue);
		}
		if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
			WriteExcelFile writer = new WriteExcelFile();
			writer.writeResultSetToExcel(ausgabeDatei, vorlageDatei, results);
		}
		close();
	}

}
