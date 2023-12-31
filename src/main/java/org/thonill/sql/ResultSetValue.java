package org.thonill.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.thonill.values.Value;

/**
 * ResultSetValue class implements the Value interface to wrap result set values
 * from a database query. This allows treating result set values as Value
 * objects.
 */
public class ResultSetValue implements Value {
	private String name;
	private ResultSet rs;
	private int pos;

	public ResultSetValue(String name, ResultSet rs, int pos) {
		this.name = name;
		this.rs = rs;
		this.pos = pos + 1;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getString() {
		try {
			return rs.getString(pos);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble() {
		try {
			return rs.getDouble(pos);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean() {
		try {
			return rs.getBoolean(pos);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCSVValue() {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			switch (metaData.getColumnType(pos)) {
			case Types.VARCHAR:
			case Types.CHAR:
				return rs.getString(pos);
			case Types.INTEGER:
				return String.valueOf(rs.getInt(pos));
			case Types.BIGINT:
				return String.valueOf(rs.getLong(pos));
			case Types.DOUBLE:
			case Types.FLOAT:
				return String.valueOf(rs.getDouble(pos));
			case Types.DATE:
			case Types.TIMESTAMP:
				return String.valueOf(rs.getDate(pos));
			default:
				return rs.getString(pos);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
