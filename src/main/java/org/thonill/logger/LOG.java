package org.thonill.logger;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LOG {
	
	private LOG() {
		super();
	}

	private static final Logger logger = Logger.getLogger(LOG.class.getName());

	public static void log(Level level, String msg, Object... args) {
		if (logger.isLoggable(level)) {
			if (args == null || args.length == 0) {
				logger.log(level, msg);
			} else {
				logger.log(level, MessageFormat.format(msg, args));
			}
		}
	}

	public static void severe(String msg, Object... args) {
		log(Level.SEVERE, msg, args);
	}

	public static void info(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}

	public static void warning(String msg, Object... args) {
		log(Level.WARNING, msg, args);
	}

}
