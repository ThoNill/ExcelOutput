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

	private static final long serialVersionUID = 1L;
	private boolean run = true;
	private ActiveArguments arguments;

	private JTextField usernameField;
	private JPasswordField passwordField;

	public enum ExportArt {
		VORLAGE, STEUERDATEI, CSV
	}

	private ExportArt art = ExportArt.VORLAGE;
	private JPanel dynamicPanel;
	private JPanelAccumulator dynamicFields;
	private Properties properties = new Properties();
	private JTextField fileNameTextField;

	private void loadProperties() {
		File f = new File("./ExcelExport.properties");
		LOG.info("Read Properties from: {0} ", f.getAbsolutePath());
		if (f.exists()) {
			try (InputStream in = new FileInputStream(f)) {
				properties.load(in);
			} catch (Exception e) {
				msgBox("Fehler beim laden der Properties Datei", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			properties = new Properties();
		}
	}

	private void storeProperties() {
		properties.put(StandardKeys.USER, usernameField.getText());
		properties.put(StandardKeys.AUSGABE_DATEI_NAME, fileNameTextField.getText());
		dynamicFields.storeProperties(properties);

		File f = new File("./ExcelExport.properties");
		LOG.info("Write Properties to: {0} ", f.getAbsolutePath());
		try (OutputStream out = new FileOutputStream(f)) {
			properties.store(out, "ExcelOutput");
		} catch (Exception e) {
			msgBox("Fehler beim speichern der Properties Datei", JOptionPane.ERROR_MESSAGE);
		}

	}

	public ApplicationDialog(ActiveArguments arguments) {
		super("Swing Anwendung");
		loadProperties();
		this.arguments = arguments;
		LOG.info("createTabbedPanel");
		JTabbedPane tabbedPane = createTabbedPanel();

		LOG.info("createButtonPanel");
		JPanel buttonPanel = createButtonPanel();

		// Layout-Manager für die Hauptanwendung
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		addWindowListener(this);

		// Grundlegende Einstellungen für die Anwendung
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);
		LOG.info("all created");
	}

	private JTabbedPane createTabbedPanel() {
		JTabbedPane tabbedPane = new JTabbedPane();
		// Tab 5: Dynamisch veränderbares Panel
		// muss früh erzeugt weden, wegen PUpdates
		JPanel dynamicParent = creatDynamicPanel();

		JPanel loginPanel = createLoginPanel();
		tabbedPane.addTab("Login", loginPanel);

		JFileChooser sqlFileChooser = createSqlFileChooser();
		tabbedPane.addTab("Sql Datei Auswahl", sqlFileChooser);

		JPanel templatePanel = createTemplatePanel();
		tabbedPane.addTab("Vorlagen", templatePanel);

		JPanel fileSelectionPanel = createOutputFileChooser();
		tabbedPane.addTab("Ausgabedatei", fileSelectionPanel);

		tabbedPane.addTab("Felder aus SqlDatei", dynamicParent);
		return tabbedPane;
	}

	private JPanel creatDynamicPanel() {
		dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new GridLayout(1, 1, 5, 5));

		JPanel dynamicParent = new JPanel();
		dynamicParent.setLayout(new BoxLayout(dynamicParent, BoxLayout.Y_AXIS));
		dynamicParent.add(dynamicPanel);
		addGlue(dynamicParent);
		return dynamicParent;
	}

	private JPanel createButtonPanel() {
		// Button Panel am unteren Rand
		JPanel buttonPanel = new JPanel();
		JButton createButton = new JButton("Erstellen");
		JButton exitButton = new JButton("Beenden");
		buttonPanel.add(createButton);
		buttonPanel.add(exitButton);

		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					putUserInformation(usernameField, passwordField);
					dynamicFields.setActiveArgument(arguments);
					arguments.run();
					msgBox("Die Dateien wurden erstellt", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					msgBox("Es ist ein Fehler aufgetreten!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		return buttonPanel;
	}

	private JPanel createOutputFileChooser() {
		LOG.info("createOutputFileChooser");
		// Tab 4: Dateiauswahl mit Verzeichnis und Dateiname
		JFileChooser directoryChooser = new JFileChooser();
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		configChooser(directoryChooser, StandardKeys.AUSGABE_DIR);

		directoryChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File outputDir = (File) evt.getNewValue();
					if (outputDir != null) {
						String path = outputDir.getAbsolutePath();
						arguments.put(StandardKeys.AUSGABE_DIR, path);
						properties.put(StandardKeys.AUSGABE_DIR, path);
					}
				}
			}
		});

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(1, 3, 5, 5));
		fileNameTextField = new JTextField(20);
		setFieldWithProperty(fileNameTextField, StandardKeys.AUSGABE_DATEI_NAME);

		fileNameTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String outputFilename = fileNameTextField.getText();
				arguments.put(StandardKeys.AUSGABE_DATEI, outputFilename);
				properties.put(StandardKeys.AUSGABE_DATEI_NAME, outputFilename);
			}
		});

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

	private JPanel createTemplatePanel() {
		// Tab 3: Vorlagen
		LOG.info("createTemplatePanel");

		final JFileChooser templateFileChooser = new JFileChooser();
		templateFileChooser.setFileFilter(new FileNameExtensionFilter("Excel Dateien", "xls", "xlsx"));
		configChooser(templateFileChooser, StandardKeys.VORLAGE);
		templateFileChooser.setVisible(!isRadioSelected(ExportArt.CSV, false));

		templateFileChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File templateFile = (File) evt.getNewValue();
					if (templateFile != null) {
						String path = templateFile.getAbsolutePath();
						properties.put(StandardKeys.VORLAGE, path);

						switch (art) {
						case CSV:
							break;
						case STEUERDATEI:
							arguments.put(StandardKeys.STEUER_DATEI, path);
							break;
						case VORLAGE:
							arguments.put(StandardKeys.EXCEL_VORLAGE, path);
							changeFileNameSuffix(templateFileChooser);
							break;
						default:
							break;

						}
					}
				}
			}
		});

		JRadioButton templateRadioButton = new JRadioButton("Vorlage", isRadioSelected(ExportArt.VORLAGE, true));

		templateRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (templateRadioButton.isSelected()) {
					art = ExportArt.VORLAGE;
					properties.put(StandardKeys.EXPORT_ART, ExportArt.VORLAGE.name());
					templateFileChooser.setVisible(true);
					arguments.remove(StandardKeys.STEUER_DATEI);
					changeFileNameSuffix(templateFileChooser);
				}
			}

		});

		JRadioButton configFileRadioButton = new JRadioButton("Steuerdatei",
				isRadioSelected(ExportArt.STEUERDATEI, false));
		configFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configFileRadioButton.isSelected()) {
					art = ExportArt.STEUERDATEI;
					properties.put(StandardKeys.EXPORT_ART, ExportArt.STEUERDATEI.name());
					templateFileChooser.setVisible(true);
					arguments.remove(StandardKeys.EXCEL_VORLAGE);
					if (templateFileChooser.getSelectedFile() != null) {
						arguments.put(StandardKeys.STEUER_DATEI,
								templateFileChooser.getSelectedFile().getAbsolutePath());
					}
					String text = fileNameTextField.getText();
					fileNameTextField.setText(text.replaceAll("\\.csv$", ".xls"));
				}
			}
		});

		JRadioButton csvFileRadioButton = new JRadioButton("CSV Datei", isRadioSelected(ExportArt.CSV, false));
		csvFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (csvFileRadioButton.isSelected()) {
					art = ExportArt.CSV;
					properties.put(StandardKeys.EXPORT_ART, ExportArt.CSV.name());
					templateFileChooser.setVisible(false);
					arguments.remove(StandardKeys.STEUER_DATEI);
					arguments.remove(StandardKeys.EXCEL_VORLAGE);
					String text = fileNameTextField.getText();
					fileNameTextField.setText(text.replaceAll("\\.(xls|xlsx)$", ".csv"));
				}
			}
		});

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

		return templatePanel;
	}

	private JFileChooser createSqlFileChooser() {
		LOG.info("createSqlFileChooser");
		// Tab 2: SQL Datei Auswahl
		JFileChooser sqlFileChooser = new JFileChooser();
		sqlFileChooser.setFileFilter(new FileNameExtensionFilter("Sql Dateien", "sql"));
		sqlFileChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File sqlFile = (File) evt.getNewValue();
					if (sqlFile != null) {

						String path = sqlFile.getAbsolutePath();
						arguments.put(StandardKeys.SQL_DATEI, path);
						properties.put(StandardKeys.SQL_DATEI, path);

						try {
							if (dynamicFields != null) {
								dynamicFields.storeProperties(properties);
								dynamicPanel.remove(dynamicFields);
							}
							dynamicFields = new JPanelAccumulator(
									VariableExtractor.extractFieldDescriptionsFromFile(sqlFile.getAbsolutePath()));

							dynamicFields.loadProperties(properties);
							dynamicPanel.add(dynamicFields);
							dynamicPanel.repaint();
						} catch (IOException e) {
							LOG.severe(e.getLocalizedMessage());
						}
					}

				}
			}
		});
		configChooser(sqlFileChooser, StandardKeys.SQL_DATEI);

		return sqlFileChooser;
	}

	private boolean isRadioSelected(ExportArt exportArt, boolean defaultValue) {
		String a = properties.getProperty(StandardKeys.EXPORT_ART);
		return (a != null) ? exportArt.name().equals(a) : defaultValue;
	}

	private void configChooser(JFileChooser fileChooser, String key) {
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Datei auswählen");
		fileChooser.setApproveButtonText("Auswählen");
		fileChooser.setApproveButtonToolTipText("Auswählen");
		fileChooser.setApproveButtonMnemonic('A');
		LOG.info("Setze Chooser für Key {0} ", key);
		if (properties.containsKey(key)) {
			String p = properties.getProperty(key);
			if (p != null) {
				LOG.info("Datei {0} ", p);
				arguments.put(key, p);
				File f = new File(p);
				try {
					fileChooser.setSelectedFile(f);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				LOG.info("Chooser gesetzt für Key {0} ", key);
			}
		}

	}

	private JPanel createLoginPanel() {
		// Tab 1: Login
		LOG.info("createLoginPanel");

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 3, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));

		usernameField = new JTextField(20);
		setFieldWithProperty(usernameField, StandardKeys.USER);

		usernameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				arguments.put(StandardKeys.USER, usernameField.getText());
				properties.put(StandardKeys.USER, usernameField.getText());
			}
		});

		fieldPanel.add(usernameField);
		addGlue(fieldPanel);

		fieldPanel.add(new JLabel("Passwort:"));

		passwordField = new JPasswordField(20);
		passwordField.setText("");

		passwordField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				arguments.put(StandardKeys.PASSWORD, passwordField.getText());
			}
		});

		fieldPanel.add(passwordField);
		addGlue(fieldPanel);

		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
		loginPanel.add(fieldPanel);
		addGlue(loginPanel);

		JButton testConnectionButton = new JButton("Verbindung testen");
		testConnectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				try {
					putUserInformation(usernameField, passwordField);
					Connection conn = arguments.createConnection();
					conn.close();
					msgBox("Connection ist ok", JOptionPane.OK_OPTION);
				} catch (Exception e) {
					msgBox("Es ist ein Verbindungsfehler aufgetreten!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		JPanel buttonPanel = new JPanel();
		addGlue(buttonPanel);
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(testConnectionButton);
		addGlue(buttonPanel);

		loginPanel.add(buttonPanel);
		return loginPanel;
	}

	private void changeFileNameSuffix(final JFileChooser templateFileChooser) {
		String xls = ".xls";
		if (templateFileChooser.getSelectedFile() != null) {
			String template = templateFileChooser.getSelectedFile().getAbsolutePath();
			arguments.put(StandardKeys.EXCEL_VORLAGE, template);
			if (template.endsWith(".xlsx")) {
				xls = ".xlsx";
			}
		}
		String text = fileNameTextField.getText();
		fileNameTextField.setText(text.replaceAll("\\.(xlsx|xls|csv)$", xls));
	}

	private void setFieldWithProperty(JTextField field, String key) {
		String user = (properties.containsKey(key)) ? properties.getProperty(key) : "";
		field.setText((user == null) ? "" : user);
	}

	private void addGlue(JPanel panel) {
		JPanel dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new BorderLayout());
		panel.add(dynamicPanel);
		// panel.add(Box.createGlue());
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
				e.printStackTrace();
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

	private void putUserInformation(final JTextField usernameField, final JPasswordField passwordField) {
		arguments.put(StandardKeys.USER, usernameField.getText());
		properties.put(StandardKeys.USER, usernameField.getText());
		arguments.put(StandardKeys.PASSWORD, passwordField.getText());
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

}
