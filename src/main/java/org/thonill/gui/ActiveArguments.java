package org.thonill.gui;

import java.sql.Connection;

public interface ActiveArguments extends Runnable {
	void put(String key, String value);

	void clear();

	Connection createConnection();

	void stop();
}
