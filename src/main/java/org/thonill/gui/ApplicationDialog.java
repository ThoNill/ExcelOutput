package org.thonill.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;
import org.thonill.replace.VariableExtractor;

public class ApplicationDialog extends JFrame implements WindowListener {

	public enum ExportArt {
		VORLAGE, STEUERDATEI, CSV
	}

	private static final long serialVersionUID = 1L;
	private boolean run = true;
	private ActiveArguments arguments;

	private JTextField usernameField;
	private JPasswordField passwordField;
	private JTextField fileNameTextField;

	private JFileChooser directoryChooser;
	private JFileChooser templateFileChooser;
	private JFileChooser sqlFileChooser;

	private JPanel dynamicPanel;
	private JPanelAccumulator dynamicFields;
	private Properties properties = new Properties();

	private JRadioButton templateRadioButton;

	private JRadioButton configFileRadioButton;

	private JRadioButton csvFileRadioButton;

	public ApplicationDialog(ActiveArguments arguments) {
		super("Swing Anwendung");
		this.arguments = arguments;

		initFields();
		loadProperties();

		layoutDialog();
		addActions();

		LOG.info("all created");
	}

	private void initFields() {

		directoryChooser = new JFileChooser();
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		configChooser(directoryChooser, StandardKeys.AUSGABE_DIR);

		templateRadioButton = new JRadioButton("Vorlage", true);
		configFileRadioButton = new JRadioButton("Steuerdatei", false);
		csvFileRadioButton = new JRadioButton("CSV Datei", false);

		templateFileChooser = new JFileChooser();
		templateFileChooser.setFileFilter(new FileNameExtensionFilter("Excel Dateien", "xls", "xlsx"));
		configChooser(templateFileChooser, StandardKeys.VORLAGE);
		templateFileChooser.setVisible(!isRadioSelected(ExportArt.CSV, false));

		fileNameTextField = new JTextField(20);
		setFieldWithProperty(fileNameTextField, StandardKeys.AUSGABE_DATEI_NAME);

		sqlFileChooser = new JFileChooser();
		sqlFileChooser.setFileFilter(new FileNameExtensionFilter("Sql Dateien", "sql"));
		configChooser(sqlFileChooser, StandardKeys.SQL_DATEI);

		passwordField = new JPasswordField(20);
		passwordField.setText("");

		usernameField = new JTextField(20);
		setFieldWithProperty(usernameField, StandardKeys.USER);

		dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new GridLayout(1, 1, 5, 5));
		dynamicFields = null;

	}

	private void layoutDialog() {
		LOG.info("createTabbedPanel");
		JTabbedPane tabbedPane = createTabbedPanel();

		LOG.info("createButtonPanel");
		JPanel buttonPanel = createButtonPanel();

		// Layout-Manager für die Hauptanwendung
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		// Grundlegende Einstellungen für die Anwendung
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);
	}

	private JTabbedPane createTabbedPanel() {
		JTabbedPane tabbedPane = new JTabbedPane();
		// Tab 5: Dynamisch veränderbares Panel
		// muss früh erzeugt weden, wegen PUpdates

		tabbedPane.addTab("Login", createLoginPanel());

		tabbedPane.addTab("Sql Datei Auswahl", createSqlFileChooser());

		tabbedPane.addTab("Vorlagen", createTemplatePanel());

		tabbedPane.addTab("Ausgabedatei", createOutputFileChooser());

		tabbedPane.addTab("Felder aus SqlDatei", createDynamicPanel());

		return tabbedPane;
	}

	private JPanel createTemplatePanel() {
		// Tab 3: Vorlagen
		LOG.info("createTemplatePanel");

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(templateRadioButton);
		radioGroup.add(configFileRadioButton);
		radioGroup.add(csvFileRadioButton);

		JPanel radioGroupPanel = new JPanel();
		radioGroupPanel.setLayout(new BoxLayout(radioGroupPanel, BoxLayout.X_AXIS));
		radioGroupPanel.add(templateRadioButton);
		radioGroupPanel.add(configFileRadioButton);
		radioGroupPanel.add(csvFileRadioButton);

		JPanel templatePanel = new JPanel();
		templatePanel.setLayout(new BoxLayout(templatePanel, BoxLayout.Y_AXIS));
		templatePanel.add(radioGroupPanel);
		templatePanel.add(templateFileChooser);

		addActionVorlage(templateRadioButton);
		addActionSteuerdatei(configFileRadioButton);
		addActionCvs(csvFileRadioButton);

		return templatePanel;
	}

	private JPanel createOutputFileChooser() {
		LOG.info("createOutputFileChooser");
		// Tab 4: Dateiauswahl mit Verzeichnis und Dateiname

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(1, 3, 5, 5));

		fieldPanel.add(new JLabel(" Dateiname:"));
		fieldPanel.add(fileNameTextField);
		addGlue(fieldPanel);

		JPanel fileSelectionPanel = new JPanel();
		fileSelectionPanel.add(fieldPanel);
		fileSelectionPanel.setLayout(new BoxLayout(fileSelectionPanel, BoxLayout.Y_AXIS));
		fileSelectionPanel.add(directoryChooser);
		addGlue(fileSelectionPanel);
		return fileSelectionPanel;
	}

	private JPanel createButtonPanel() {
		// Button Panel am unteren Rand
		JPanel buttonPanel = new JPanel();
		JButton createButton = new JButton("Erstellen");
		JButton exitButton = new JButton("Beenden");
		buttonPanel.add(createButton);
		buttonPanel.add(exitButton);

		addActionRunButton(createButton);
		addActionQuitButton(exitButton);

		return buttonPanel;
	}

	private JPanel createDynamicPanel() {
		JPanel dynamicParent = new JPanel();
		dynamicParent.setLayout(new BoxLayout(dynamicParent, BoxLayout.Y_AXIS));
		dynamicParent.add(dynamicPanel);
		addGlue(dynamicParent);
		return dynamicParent;
	}

	private JFileChooser createSqlFileChooser() {
		return sqlFileChooser;
	}

	private JPanel createLoginPanel() {
		// Tab 1: Login
		LOG.info("createLoginPanel");

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 3, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));

		fieldPanel.add(usernameField);
		addGlue(fieldPanel);

		fieldPanel.add(new JLabel("Passwort:"));

		fieldPanel.add(passwordField);
		addGlue(fieldPanel);

		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
		loginPanel.add(fieldPanel);
		addGlue(loginPanel);

		JButton testConnectionButton = new JButton("Verbindung testen");
		addActionTestConnection(testConnectionButton);

		JPanel buttonPanel = new JPanel();
		addGlue(buttonPanel);
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(testConnectionButton);
		addGlue(buttonPanel);

		loginPanel.add(buttonPanel);
		return loginPanel;
	}

	private boolean isRadioSelected(ExportArt exportArt, boolean defaultValue) {
		ExportArt art = getExportArt();
		return (art != null) ? exportArt.equals(art) : defaultValue;
	}

	private void configChooser(JFileChooser fileChooser, String key) {
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Datei auswählen");
		fileChooser.setControlButtonsAreShown(false);
		LOG.info("Setze Chooser für Key {0} ", key);
		if (properties.containsKey(key)) {
			String p = properties.getProperty(key);
			if (p != null) {
				LOG.info("Datei {0} ", p);
				File f = new File(p);
				fileChooser.setSelectedFile(f);
				LOG.info("Chooser gesetzt für Key {0} ", key);
			}
		}

	}

	private void loadProperties() {
		File f = new File("./ExcelExport.properties");
		LOG.info("Read Properties from: {0} ", f.getAbsolutePath());
		if (f.exists()) {
			try (InputStream in = new FileInputStream(f)) {
				properties.load(in);
			} catch (Exception e) {
				LOG.severe(e.getLocalizedMessage());
				msgBox("Fehler beim laden der Properties Datei", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			properties = new Properties();
		}
		loadProperties(properties);
	}

	private void storeProperties() {
		storeProperties(properties);

		File f = new File("./ExcelExport.properties");
		LOG.info("Write Properties to: {0} ", f.getAbsolutePath());
		try (OutputStream out = new FileOutputStream(f)) {
			properties.store(out, "ExcelOutput");
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
			msgBox("Fehler beim speichern der Properties Datei", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void storeProperties(Properties properties) {

		properties.put(StandardKeys.USER, getUser());
		properties.put(StandardKeys.AUSGABE_DATEI_NAME, getFileName());
		properties.put(StandardKeys.AUSGABE_DIR, getOutputDir());
		properties.put(StandardKeys.SQL_DATEI, getSqlFile());
		properties.put(StandardKeys.VORLAGE, getTemplateFile());
		properties.put(StandardKeys.EXPORT_ART, getExportArt().name());
		dynamicFields.storeProperties(properties);
	}

	private void loadProperties(Properties properties) {
		setUser((String) properties.get(StandardKeys.USER));
		setFileName((String) properties.get(StandardKeys.AUSGABE_DATEI_NAME));
		setOutputDir((String) properties.get(StandardKeys.AUSGABE_DIR));
		setSqlFile((String) properties.get(StandardKeys.SQL_DATEI));
		setTemplateFile((String) properties.get(StandardKeys.VORLAGE));
		setExportArt(getExportArt((String) properties.get(StandardKeys.EXPORT_ART)));
		updateDynamicFields();

	}

	private void storeArguments() {
		arguments.clear();

		arguments.put(StandardKeys.USER, getUser());
		arguments.put(StandardKeys.PASSWORD, getPassword());
		arguments.put(StandardKeys.SQL_DATEI, getSqlFile());
		arguments.put(StandardKeys.VORLAGE, getTemplateFile());
		dynamicFields.storeArguments(arguments);

		putOutputFileNames();

		putVorlageInArguments();

	}

	private void putOutputFileNames() {
		String outputDir = getOutputDir();
		String outputFileName = getFileName();
		arguments.put(StandardKeys.AUSGABE_DATEI_NAME, outputFileName);
		arguments.put(StandardKeys.AUSGABE_DIR, outputDir);
		String outputFilePath = new File((outputDir == null) ? "." : outputDir,
				(outputFileName == null) ? "test.cvs" : outputFileName).getAbsolutePath();
		arguments.put(StandardKeys.AUSGABE_DATEI, outputFilePath);
	}

	private void putVorlageInArguments() {
		ExportArt art = getExportArt();
		String path = getTemplateFile();
		switch (art) {
		case CSV:
			break;
		case STEUERDATEI:
			arguments.put(StandardKeys.STEUER_DATEI, path);
			break;
		case VORLAGE:
			arguments.put(StandardKeys.EXCEL_VORLAGE, path);
			break;
		default:
			break;

		}
	}

	public void addActions() {
		addActionsSqlFile();
		addActionTemplateFile();
		addWindowListener(this);
	}

	private void addActionQuitButton(JButton exitButton) {
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
	}

	private void addActionRunButton(JButton createButton) {
		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					storeArguments();
					arguments.run();
					msgBox("Die Dateien wurden erstellt", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					//e.printStackTrace();
					LOG.severe(e.getLocalizedMessage());
					msgBox("Es ist ein Fehler aufgetreten!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private void addActionTemplateFile() {
		templateFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					LOG.info("Event: {0}", evt.toString());
					changeFileNameSuffix();

				}
			}
		});
	}

	private void addActionCvs(JRadioButton csvFileRadioButton) {
		csvFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (csvFileRadioButton.isSelected()) {
					templateFileChooser.setVisible(false);
					changeFileNameSuffix();
				}
			}
		});
	}

	private void addActionSteuerdatei(JRadioButton configFileRadioButton) {
		configFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configFileRadioButton.isSelected()) {
					templateFileChooser.setVisible(true);
					changeFileNameSuffix();
				}
			}
		});
	}

	private void addActionVorlage(JRadioButton templateRadioButton) {
		templateRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (templateRadioButton.isSelected()) {
					templateFileChooser.setVisible(true);
					changeFileNameSuffix();
				}
			}

		});
	}

	private void addActionsSqlFile() {
		sqlFileChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					updateDynamicFields();
				}
			}
		});
	}

	private void addActionTestConnection(JButton testConnectionButton) {
		testConnectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				try {
					arguments.put(StandardKeys.USER, getUser());
					arguments.put(StandardKeys.PASSWORD, getPassword());
					Connection conn = arguments.createConnection();
					conn.close();
					msgBox("Connection ist ok", JOptionPane.OK_OPTION);
				} catch (Exception e) {
					LOG.severe(e.getLocalizedMessage());
					msgBox("Es ist ein Verbindungsfehler aufgetreten!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
	}

	private void changeFileNameSuffix() {
		String xls = ".xls";
		String templateFile = getTemplateFile();
		if (templateFile != null) {
			if (templateFile.endsWith(".xlsx")) {
				xls = ".xlsx";
			}
		}
		String text = getFileName();
		String modifiedText = text;
		ExportArt art = getExportArt();
		switch (art) {
		case CSV:
			modifiedText = text.replaceAll("\\.(xls|xlsx)$", ".csv");
			break;
		case STEUERDATEI:
		case VORLAGE:
			modifiedText = text.replaceAll("\\.(xlsx|xls|csv)$", xls);
			break;
		default:
			break;
		}
		setFileName(modifiedText);
		properties.put(StandardKeys.AUSGABE_DATEI_NAME, modifiedText);
	}

	private void setFieldWithProperty(JTextField field, String key) {
		String user = (properties.containsKey(key)) ? properties.getProperty(key) : "";
		field.setText((user == null) ? "" : user);
	}

	private void addGlue(JPanel panel) {
		JPanel dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new BorderLayout());
		panel.add(dynamicPanel);
		// panel.add(Box.createGlue()); geht nicht so gut
	}

	public static void main(String[] args) {
		ExcelActiveArguments excel = new ExcelActiveArguments();
		ApplicationDialog app = new ApplicationDialog(excel);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.setVisible(true);
			}
		});
		app.waitUntilTheEnd();
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

	public void msgBox(String message, int messageType) {
		JOptionPane.showMessageDialog(this, message, "Message", messageType);
	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		LOG.info("Close");
		close();

	}

	private void close() {

		storeProperties();
		arguments.stop();
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	private void updateDynamicFields() {
		String sqlFile = getSqlFile();

		if (dynamicFields != null) {
			dynamicFields.storeProperties(properties);
			dynamicPanel.remove(dynamicFields);
			dynamicFields = null;
		}

		if (sqlFile != null) {
			properties.put(StandardKeys.SQL_DATEI, sqlFile);

			try {
				dynamicFields = new JPanelAccumulator(VariableExtractor.extractFieldDescriptionsFromFile(sqlFile));

				dynamicFields.loadProperties(properties);
				dynamicPanel.add(dynamicFields);
				dynamicPanel.repaint();
			} catch (IOException e) {
				LOG.severe(e.getLocalizedMessage());
			}
		}
	}

	private String getFileName() {
		return fileNameTextField.getText();
	}

	private void setFileName(String modifiedText) {
		fileNameTextField.setText(modifiedText);
	}

	private String getUser() {
		return usernameField.getText();
	}

	private void setUser(String user) {
		usernameField.setText(null2leer(user));
	}

	private String null2leer(String text) {
		return (text == null) ? "" : text;
	}

	private File null2CurrentDir(String path) {
		return new File((path == null) ? "." : path);
	}

	private String getPassword() {
		return passwordField.getText();
	}

	private String getOutputDir() {
		File f = directoryChooser.getSelectedFile();
		if (f != null) {
			return f.getAbsolutePath();
		}
		return ".";
	}

	private void setOutputDir(String path) {
		directoryChooser.setSelectedFile(null2CurrentDir(path));
	}

	private String getTemplateFile() {
		File f = templateFileChooser.getSelectedFile();
		if (f != null) {
			return f.getAbsolutePath();
		}
		return null;
	}

	private void setTemplateFile(String path) {
		if (path != null) {
			templateFileChooser.setSelectedFile(new File(path));
		}
	}

	private String getSqlFile() {
		File f = sqlFileChooser.getSelectedFile();
		if (f != null) {
			return f.getAbsolutePath();
		}
		return null;
	}

	private void setSqlFile(String path) {
		if (path != null) {
			sqlFileChooser.setSelectedFile(new File(path));
		}
	}

	public void setExportArt(ExportArt exportArt) {
		switch (exportArt) {
		case CSV:
			csvFileRadioButton.setSelected(true);
			break;
		case STEUERDATEI:
			configFileRadioButton.setSelected(true);
			break;
		case VORLAGE:
			templateRadioButton.setSelected(true);
			break;
		default:
			break;

		}
	}

	public ExportArt getExportArt() {
		if (csvFileRadioButton.isSelected())
			return ExportArt.CSV;
		if (configFileRadioButton.isSelected())
			return ExportArt.STEUERDATEI;
		if (templateRadioButton.isSelected())
			return ExportArt.VORLAGE;
		return ExportArt.CSV;
	}

	public ExportArt getExportArt(String name) {
		if (ExportArt.CSV.name().equals(name))
			return ExportArt.CSV;
		if (ExportArt.STEUERDATEI.name().equals(name))
			return ExportArt.STEUERDATEI;
		if (ExportArt.VORLAGE.name().equals(name))
			return ExportArt.VORLAGE;
		return ExportArt.CSV;
	}

}
