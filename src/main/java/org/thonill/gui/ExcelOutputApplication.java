package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.thonill.actions.FileCreator;
import org.thonill.exceptions.ApplicationException;
import org.thonill.logger.LOG;

/**
 * ExcelOutputApplication provides a dialog for selecting a file and connecting
 * to a database.
 */

public class ExcelOutputApplication extends FileCreator {

	private boolean run = true;
	Map<String, String> args;

	public ExcelOutputApplication() {
		super();

	}

	public static void main(String[] args) {
		try {
			Map<String, String> arguments = parseArgs(args);
			new ExcelOutputApplication().main(arguments);
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
	}

	public void main(Map<String, String> args) {
		this.args = args;
		clear();
		if (die_Parameter_reichen_zur_Ausführung()) {
			run();
		} else {
			runGui();
		}
	}

	public void runGui() {

		LOG.info("Start runGui");
		checkNotNull(daten, "ExcelOutputApplication.main daten is null");

		String connectionFilePath = getFilePath(DB_DATEI);
		checkNotNull(connectionFilePath, "we need -dbDatei ");
		String sqlFilePath = getFilePath(SQL_DATEI);
		checkNotNull(sqlFilePath, "we need -sqlDatei ");
		LOG.info("Vor login Dialog");
		SwingUtilities.invokeLater(() -> {
			try {
				inGui();
			} catch (Exception e) {
				msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
				LOG.severe(e.getLocalizedMessage());
			}
		});
		waitUntilTheEnd();

	}

	private void inGui() {
		try {
			ApplicationDialog app = new ApplicationDialog(this);
			app.setVisible(true);
		} catch (Exception e) {
			msgBox("Error: " + e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
			LOG.severe(e.getLocalizedMessage());
		}
	}

	private void waitUntilTheEnd() {
		while (run) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.severe(e.getLocalizedMessage());
				Thread.currentThread().interrupt();
			}
		}
	}

	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> argMap = new HashMap<>();

		for (int i = 0; i < args.length; i += 2) {
			if (args[i].startsWith("-")) {
				String key = args[i].substring(1);
				if (i < args.length - 1) {
					argMap.put(key, args[i + 1]);
				} else {
					throw new ApplicationException("ungerade Anzahl von Argumenten");
				}
			}
		}

		return argMap;
	}

	public void msgBox(String message, int messageType) {
		JOptionPane.showMessageDialog(new JFrame(), message, "Message", messageType);
	}

	@Override
	public void stop() {
		run = false;
	}

	@Override
	public void clear() {
		super.clear();

		for (Map.Entry<String, String> entry : args.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}

		setDbFile(args.get(DB_DATEI));
		setSqlFile(args.get(SQL_DATEI));
		setAusgabeDatei(args.get(AUSGABE_DATEI_NAME));
		setAusgabeDir(args.get(AUSGABE_DIR));
		setTemplateFile(args.get(VORLAGE));
		setUser(args.get(USER));

	}
}
