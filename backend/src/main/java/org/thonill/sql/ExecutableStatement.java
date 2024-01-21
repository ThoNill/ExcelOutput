package org.thonill.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.thonill.exceptions.ApplicationException;
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
			PreparedStatement checkStmt = conn.prepareStatement(query);
			checkStmt.close();
		} else {
			throw new ApplicationException("ExecutableStatement.canPrepareQuery: query is null or empty");
		}

	}

	public void exportToResults(Connection conn, ResultOfStatments result) throws SQLException {
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
		throw new ApplicationException("ExecutableStatement.getPosition: ResultSet is null");
	}

	@Override
	public boolean next() {
		if (rs != null) {
			try {
				hasData = rs.next();
				return hasData();
			} catch (SQLException e) {
				throw new ApplicationException("ExecutableStatement.next: " + e.getMessage());
			}
		} else {
			throw new ApplicationException("ExecutableStatement.next: ResultSet is null");
		}
	}

	@Override
	public int size() {
		try {
			return rs.getMetaData().getColumnCount();
		} catch (SQLException e) {
			throw new ApplicationException("ExecutableStatement.size: " + e.getMessage());
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
			isOk = false;
		}
	}

	private void closeResultSet() {
		try {
			rs.close();
		} catch (SQLException e) {
			closeStatement();
			throw new ApplicationException("ExecutableStatement.close: " + e.getMessage());
		}
	}

	private void closeStatement() {
		try {
			stmt.close();
		} catch (SQLException e) {
			throw new ApplicationException("ExecutableStatement.close: " + e.getMessage());
		}
	}

}
