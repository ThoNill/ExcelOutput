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
import java.io.IOException;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.thonill.keys.StandardKeys;
import org.thonill.logger.LOG;
import org.thonill.replace.VariableExtractor;

public class ApplicationDialog extends JFrame implements WindowListener {
	
	private static final long serialVersionUID = 1L;
	private boolean run = true;
	private ActiveArguments arguments;

	public enum ExportArt {
		VORLAGE, STEUERDATEI, CSV
	}

	private ExportArt art = ExportArt.VORLAGE;
	private JPanel dynamicPanel;

	
	public ApplicationDialog(ActiveArguments arguments) {
		super("Swing Anwendung");
		this.arguments = arguments;
		LOG.info("in Swing App");
		JTabbedPane tabbedPane = createTabbedPanel();

		JPanel buttonPanel = createButtonPanel();

		// Layout-Manager für die Hauptanwendung
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		addWindowListener(this);

		// Grundlegende Einstellungen für die Anwendung
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);

	}

	private JTabbedPane createTabbedPanel() {
		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel loginPanel = createLoginPanel();
		tabbedPane.addTab("Login", loginPanel);

		JFileChooser sqlFileChooser = createSqlFileChooser();
		tabbedPane.addTab("Sql Datei Auswahl", sqlFileChooser);

		JPanel templatePanel = createTemplatePanel();
		tabbedPane.addTab("Vorlagen", templatePanel);

		JPanel fileSelectionPanel = createOutputFileChooser();
		tabbedPane.addTab("Ausgabedatei", fileSelectionPanel);

		// Tab 5: Dynamisch veränderbares Panel
		dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new GridLayout(1, 1, 5, 5));

		JPanel dynamicParent = new JPanel();
		dynamicParent.setLayout(new BoxLayout(dynamicParent, BoxLayout.Y_AXIS));
		dynamicParent.add(dynamicPanel);
		addGlue(dynamicParent);
		tabbedPane.addTab("Felder aus SqlDatei", dynamicParent);
		return tabbedPane;
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
				arguments.stop();
			}
		});
		return buttonPanel;
	}

	private JPanel createOutputFileChooser() {
		// Tab 4: Dateiauswahl mit Verzeichnis und Dateiname
		JFileChooser directoryChooser = new JFileChooser();
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		configChooser(directoryChooser);

		directoryChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File outputDir = (File) evt.getNewValue();
					arguments.put(StandardKeys.AUSGABE_DIR, outputDir.getAbsolutePath());
				}
			}
		});

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(1, 3, 5, 5));
		JTextField fileNameTextField = new JTextField(20);
		fileNameTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String outputFilename = fileNameTextField.getText();
				arguments.put(StandardKeys.AUSGABE_DATEI, outputFilename);
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

		final JFileChooser templateFileChooser = new JFileChooser();
		templateFileChooser.setFileFilter(new FileNameExtensionFilter("Excel Dateien", "xls", "xlsx"));
		configChooser(templateFileChooser);

		templateFileChooser.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File templateFile = (File) evt.getNewValue();
					switch (art) {
					case CSV:
						break;
					case STEUERDATEI:
						arguments.put(StandardKeys.STEUER_DATEI, templateFile.getAbsolutePath());
						break;
					case VORLAGE:
						arguments.put(StandardKeys.EXCEL_VORLAGE, templateFile.getAbsolutePath());
						break;
					default:
						break;

					}
				}
			}
		});

		JRadioButton templateRadioButton = new JRadioButton("Vorlage", true);

		templateRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (templateRadioButton.isSelected()) {
					art = ExportArt.VORLAGE;
					templateFileChooser.setVisible(true);
					arguments.remove(StandardKeys.STEUER_DATEI);
					if (templateFileChooser.getSelectedFile() != null) {
						arguments.put(StandardKeys.EXCEL_VORLAGE,
								templateFileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			}
		});

		JRadioButton configFileRadioButton = new JRadioButton("Steuerdatei");
		configFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configFileRadioButton.isSelected()) {
					art = ExportArt.STEUERDATEI;
					templateFileChooser.setVisible(true);
					arguments.remove(StandardKeys.EXCEL_VORLAGE);
					if (templateFileChooser.getSelectedFile() != null) {
						arguments.put(StandardKeys.STEUER_DATEI,
								templateFileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			}
		});

		JRadioButton csvFileRadioButton = new JRadioButton("CSV Datei");
		csvFileRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (csvFileRadioButton.isSelected()) {
					art = ExportArt.CSV;
					templateFileChooser.setVisible(false);
					arguments.remove(StandardKeys.STEUER_DATEI);
					arguments.remove(StandardKeys.EXCEL_VORLAGE);
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
		// Tab 2: SQL Datei Auswahl
		JFileChooser sqlFileChooser = new JFileChooser();
		sqlFileChooser.setFileFilter(new FileNameExtensionFilter("Sql Dateien", "sql"));
		sqlFileChooser.addPropertyChangeListener(new PropertyChangeListener() {

			private JPanelAccumulator dynamicFields;

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
					// Aktionen bei Änderung der ausgewählten Datei
					File sqlFile = (File) evt.getNewValue();
					if (sqlFile != null) {
						arguments.put(StandardKeys.SQL_DATEI, sqlFile.getAbsolutePath());

						try {
							if (dynamicFields != null) {
								dynamicPanel.remove(dynamicFields);
							}
							dynamicFields = new JPanelAccumulator(
									VariableExtractor.extractFieldDescriptionsFromFile(sqlFile.getAbsolutePath()));
							dynamicPanel.add(dynamicFields);
							dynamicPanel.repaint();
						} catch (IOException e) {
							LOG.severe(e.getLocalizedMessage());
						}
					}

				}
			}
		});

		configChooser(sqlFileChooser);

		return sqlFileChooser;
	}

	private void configChooser(JFileChooser sqlFileChooser) {
		sqlFileChooser.setMultiSelectionEnabled(false);
		sqlFileChooser.setDialogTitle("Datei auswählen");
		sqlFileChooser.setApproveButtonText("Auswählen");
		sqlFileChooser.setApproveButtonToolTipText("Auswählen");
		sqlFileChooser.setApproveButtonMnemonic('A');
	}

	private JPanel createLoginPanel() {
		// Tab 1: Login

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 3, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));

		final JTextField usernameField = new JTextField(20);
		usernameField.setText("");

		usernameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				arguments.put(StandardKeys.USER, usernameField.getText());
			}
		});

		fieldPanel.add(usernameField);
		addGlue(fieldPanel);

		fieldPanel.add(new JLabel("Passwort:"));

		final JPasswordField passwordField = new JPasswordField(20);
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
					arguments.put(StandardKeys.USER, usernameField.getText());
					arguments.put(StandardKeys.PASSWORD, passwordField.getText());
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


}
