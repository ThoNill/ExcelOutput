package org.thonill.gui;

import java.sql.Connection;

public interface ActiveArguments extends Runnable {
	void put(String key, String value);

	void remove(String key);

	Connection createConnection();

	void stop();
}
