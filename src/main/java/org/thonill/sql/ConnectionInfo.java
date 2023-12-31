package org.thonill.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.thonill.exceptions.ApplicationException;
import org.thonill.logger.LOG;

/**
 * ConnectionInfo encapsulates the information needed to connect to a database.
 * It can be constructed by providing all the connection details, or by loading
 * them from a properties file.
 */

public class ConnectionInfo {

	private String connectionName;
	private String connectionUrl;
	private String connectionUser;
	private String connectionPassword;
	private String connectionDriver;
	private String connectionInfoPath;

	public ConnectionInfo(String connectionName, String connectionDriver, String connectionUrl, String connectionUser,
			String connectionPassword, String connectionInfoPath) {
		this.connectionName = connectionName;
		this.connectionUrl = connectionUrl;
		this.connectionUser = connectionUser;
		this.connectionPassword = connectionPassword;
		this.connectionDriver = connectionDriver;
		this.connectionInfoPath = connectionInfoPath;

		checkNotNull(connectionName, "ConnectionInfo.constructor: connectionName is null");
		checkNotNull(connectionUrl, "ConnectionInfo.constructor: connectionUrl is null");
		checkNotNull(connectionUser, "ConnectionInfo.constructor: connectionUser is null");
		checkNotNull(connectionPassword, "ConnectionInfo.constructor: connectionPassword is null");
		checkNotNull(connectionDriver, "ConnectionInfo.constructor: connectionDriver is null");
		checkNotNull(connectionInfoPath, "ConnectionInfo.constructor: connectionInfoPath is null");

	}

	public ConnectionInfo(String connectionName, String connectionUser, String connectionPassword,
			String connectionInfoPath) throws ApplicationException {
		this.connectionName = connectionName;
		this.connectionUser = connectionUser;
		this.connectionPassword = connectionPassword;
		this.connectionInfoPath = connectionInfoPath;
		checkNotNull(connectionName, "ConnectionInfo.constructor: connectionName is null");
		checkNotNull(connectionUser, "ConnectionInfo.constructor: connectionUser is null");
		checkNotNull(connectionPassword, "ConnectionInfo.constructor: connectionPassword is null");
		checkNotNull(connectionInfoPath, "ConnectionInfo.constructor: connectionInfoPath is null");

		loadPropertiesFromFile();
	}

	public Connection getConnection() throws ApplicationException {
		try {
			return DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
		} catch (Exception e) {
			createDebugInfo(e);
		}
		return null;
	}

	private void createDebugInfo(Exception e) throws ApplicationException {
		LOG.info("ConnectionInfo.getConnection: exception= {0}", e.getMessage());
		File f = getPropertiesFile();
		LOG.info("ConnectionInfo.getInputStream: {0}" , f.getAbsolutePath());

		LOG.info("ConnectionInfo.getConnection: connectionDriver={0}" , connectionDriver);
		LOG.info("ConnectionInfo.getConnection: connectionUrl={0}" , connectionUrl);
		LOG.info("ConnectionInfo.getConnection: connectionUser{0}=" ,connectionUser);

		throw new ApplicationException(e);
	}

	private void loadPropertiesFromFile() throws ApplicationException {
		try {
			InputStream in = getInputStream();
			checkNotNull(in, "ConnectionInfo.loadPropertiesFromFile: can not find " + getPropertiesFilename());

			Properties properties = new Properties();

			properties.load(in);

			// Lese die Werte aus der Properties-Datei
			connectionUrl = properties.getProperty("url");
			connectionDriver = properties.getProperty("driver");
			checkNotNull(connectionUrl,
					"ConnectionInfo.loadPropertiesFromFile: connectionUrl is null in " + getPropertiesFilename());
			checkNotNull(connectionDriver,
					"ConnectionInfo.loadPropertiesFromFile: connectionDriver is null in " + getPropertiesFilename());
		} catch (Exception e) {
			createDebugInfo(e);
		}
	}

	private InputStream getInputStream() throws IOException {
		File f = getPropertiesFile();

		if (f.exists()) {
			return new FileInputStream(f);
		}
		return this.getClass().getResourceAsStream(getPropertiesFilename());
	}

	private File getPropertiesFile() {
		return new File(connectionInfoPath, getPropertiesFilename());
	}

	private String getPropertiesFilename() {
		return connectionName + ".properties";
	}
}
