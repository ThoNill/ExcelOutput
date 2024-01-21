package org.thonill.actions;

import java.sql.Connection;

import org.thonill.actions.ExportArt;

public interface ActiveArguments extends Runnable {
	void put(String key, String value);

	void clear();

	Connection createConnection();

	void stop();

	public void setUser(String user);

	public void setPassword(String password);

	public void setDbFile(String dbFile);

	public void setAusgabeDir(String ausgabeDir);

	public void setAusgabeDatei(String ausgabeDatei);

	public void setTemplateFile(String templateFile);

	public void setSqlFile(String sqlFile);

	public void setExportArt(ExportArt exportArt);
}
