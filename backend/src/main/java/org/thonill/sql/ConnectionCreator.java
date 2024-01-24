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
 * ConnectionCreator encapsulates the information needed to connect to a database.
 * It can be constructed by providing all the connection details, or by loading
 * them from a properties file.
 */

public class ConnectionCreator {
	private String connectionUrl;
	private String connectionUser;
	private String connectionPassword;
	private String connectionDriver;

	public ConnectionCreator() {
		super();
	}

	public Connection createConnection() throws ApplicationException {
		try {
			return DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
		} catch (Exception e) {
			createDebugInfo(e);
		}
		return null;
	}

	public boolean die_Parameter_reichen_zur_Ausführung() {
		return this.connectionUser != null && this.connectionPassword != null && this.connectionDriver != null
				&& connectionUrl != null;
	}

	private void createDebugInfo(Exception e) throws ApplicationException {
		LOG.info("ConnectionCreator.getConnection: exception= {0}", e.getMessage());

		LOG.info("ConnectionCreator.getConnection: connectionDriver={0}", connectionDriver);
		LOG.info("ConnectionCreator.getConnection: connectionUrl={0}", connectionUrl);
		LOG.info("ConnectionCreator.getConnection: connectionUser{0}=", connectionUser);

		throw new ApplicationException(e);
	}

	public void setDriver(String connectionDriver, String connectionUrl) {
		checkNotNull(connectionUrl, "ConnectionCreator.constructor: connectionUrl is null");
		checkNotNull(connectionDriver, "ConnectionCreator.constructor: connectionDriver is null");
		this.connectionUrl = connectionUrl;
		this.connectionDriver = connectionDriver;
	}

	public void setUserAndPassword(String connectionUser, String connectionPassword) throws ApplicationException {
		checkNotNull(connectionUser, "ConnectionCreator.constructor: connectionUser is null");
		checkNotNull(connectionPassword, "ConnectionCreator.constructor: connectionPassword is null");
		this.connectionUser = connectionUser;
		this.connectionPassword = connectionPassword;

	}

	public void setDbFile(File connectionInfoPath) throws ApplicationException {
		try (InputStream in = getInputStream(connectionInfoPath)) {
			checkNotNull(in, "ConnectionCreator.loadPropertiesFromFile: can not find " + connectionInfoPath);

			Properties properties = new Properties();

			properties.load(in);

			// Lese die Werte aus der Properties-Datei
			setDriver(properties.getProperty("driver"), properties.getProperty("url"));

		} catch (Exception e) {
			createDebugInfo(e);
		}
	}

	private InputStream getInputStream(File connectionInfoPath) throws IOException {
		checkNotNull(connectionInfoPath, "ConnectionCreator.constructor: connectionInfoPath is null");
		if (connectionInfoPath.exists()) {
			return new FileInputStream(connectionInfoPath);
		}
		return this.getClass().getResourceAsStream(connectionInfoPath.getName());
	}

}
