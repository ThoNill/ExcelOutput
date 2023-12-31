package org.thonill.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

/**
 * ExecutableStatement represents a SQL statement that can be executed against a
 * database. It handles executing the statement, checking if it is a SELECT
 * statement, validating that it can be prepared, and exporting result sets to
 * Excel.
 */

public class ExecutableStatement implements ArrayValue {

	private String name;

	private String query;
	private transient Statement stmt;
	private transient ResultSet rs;
	private boolean isSelectStatement = false;
	private boolean isOk = false;
	private boolean isExecuted = false;
	private boolean hasData = false;

	public boolean isSelectStatement() {
		return isSelectStatement;
	}

	public boolean isOk() {
		return isOk;
	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public boolean hasData() {
		return hasData;
	}

	public ExecutableStatement(String query) {
		this.query = query;
		checkNotNull(query, "ExecutableStatement.constructor: query is null");

	}

	public String getQueryString() {
		return query;
	}

	public boolean checkIsSelectStatement(Connection conn) throws SQLException {
		checkNotNull(conn, "ExecutableStatement.executeStatement: conn is null");
		open(conn);
		return isSelectStatement;

	}

	private void open(Connection conn) throws SQLException {
		if (!isExecuted) {
			isExecuted = true;
			isOk = false;
			try {
				stmt = conn.createStatement();
				isSelectStatement = stmt.execute(this.query);
				if (isSelectStatement) {
					rs = stmt.getResultSet();
					hasData = rs.next();
					isOk = true;
				}
			} catch (SQLException e) {
				if (stmt != null) {
					stmt.close();
				}
			}

		}
	}

	public void checkQuery(Connection conn) throws SQLException {
		checkNotNull(conn, "ExecutableStatement.canPrepareQuery: conn is null");

		if (this.query != null && !this.query.isEmpty()) {
			// Prüfe, ob die Query vorbereitet werden kann
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.close();
		} else {
			throw new RuntimeException("ExecutableStatement.canPrepareQuery: query is null or empty");
		}

	}

	/*
	 * public void exportCvsExcel(Connection conn, String ausgabeDatei, String
	 * vorlageDatei) throws Exception { checkNotNull(conn,
	 * "ExecutableStatement.exportToExcel: conn is null");
	 * checkNotNull(ausgabeDatei,
	 * "ExecutableStatement.exportToExcel: filename is null");
	 * checkNotNull(vorlageDatei,
	 * "ExecutableStatement.exportToExcel: filename is null");
	 * checkFileExists(vorlageDatei, "ExecutableStatement.exportToExcel",
	 * "vorlageDatei");
	 *
	 * try (Statement stmt = conn.createStatement()) { boolean isResultSet =
	 * stmt.execute(this.query); if (isResultSet) { try (ResultSet rs =
	 * stmt.getResultSet()) { writeCsvExcel(ausgabeDatei, vorlageDatei, rs); } } }
	 *
	 * }
	 */
	public void exportToResults(Connection conn, ResultOfStatments result) throws Exception {
		checkNotNull(conn, "ExecutableStatement.exportToExcel: conn is null");
		checkNotNull(result, "ExecutableStatement.exportToExcel: result is null");
		open(conn);
		if (isExecuted && isOk && hasData && isSelectStatement) {
			addResult(result);
		}

	}

	@Override
	public Value getPosition(int i) {

		if (rs != null) {
			return new ResultSetValue("Column" + i, rs, i);
		}
		throw new RuntimeException("ExecutableStatement.getPosition: ResultSet is null");
	}

	@Override
	public boolean next() {
		if (rs != null) {
			try {
				hasData = rs.next();
				return hasData();
			} catch (SQLException e) {
				throw new RuntimeException("ExecutableStatement.next: " + e.getMessage());
			}
		} else {
			throw new RuntimeException("ExecutableStatement.next: ResultSet is null");
		}
	}

	@Override
	public int size() {
		try {
			return rs.getMetaData().getColumnCount();
		} catch (SQLException e) {
			throw new RuntimeException("ExecutableStatement.size: " + e.getMessage());
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public void addResult(ResultOfStatments data) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		String label = rsmd.getColumnLabel(1);

		if (!label.startsWith("_")) {
			data.putArray("data", this);
		} else if (label.startsWith("__")) {
			name = label.substring(2);
			data.putArray(name, this);
		} else {
			for (int i = 0; i < columnCount; i++) {
				String columnLabel = rsmd.getColumnLabel(i + 1);
				if (columnLabel.startsWith("_")) {
					String columnName = columnLabel.substring(1).toUpperCase();
					data.putSingle(columnName, new ResultSetValue(columnName, rs, i));
				}
			}
		}
	}

	public void close() {
		if (isOk) {
			closeResultSet();
			closeStatement();
		}
	}

	private void closeResultSet() {
		try {
			rs.close();
		} catch (SQLException e) {
			closeStatement();
			throw new RuntimeException("ExecutableStatement.close: " + e.getMessage());
		}
	}

	private void closeStatement() {
		try {
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException("ExecutableStatement.close: " + e.getMessage());
		}
	}

}
