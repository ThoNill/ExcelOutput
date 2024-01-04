package org.thonill.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SwingApp extends JFrame {
	private boolean run = true;
	
	public enum ExportArt {
        VORLAGE,STEUERDATEI,CSV
    }
	
	private ExportArt art = ExportArt.VORLAGE;
	
	private String user;
	private String password;
	
	private String outputFilename;
	
    public SwingApp() {
        super("Swing Anwendung");

        JTabbedPane tabbedPane = createTabbedPanel();

        JPanel buttonPanel = createButtonPanel();

        // Layout-Manager für die Hauptanwendung
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

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
        tabbedPane.addTab("Dateiauswahl", fileSelectionPanel);

        // Tab 5: Dynamisch veränderbares Panel
        JPanel dynamicPanel = new JPanel();
        tabbedPane.addTab("Dynamisch", dynamicPanel);
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
            public void actionPerformed(ActionEvent e) {
                // Aktionen beim Klicken des "Erstellen"-Buttons
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
		return buttonPanel;
	}

	private JPanel createOutputFileChooser() {
		// Tab 4: Dateiauswahl mit Verzeichnis und Dateiname
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        configChooser(directoryChooser);

        JPanel fieldPanel = new JPanel();
    	fieldPanel.setLayout(new GridLayout(1, 3, 5, 5));
        JTextField fileNameTextField = new JTextField(20);
        fileNameTextField.addActionListener(new ActionListener() {
            

			@Override
            public void actionPerformed(ActionEvent e) {
                outputFilename = fileNameTextField.getText();
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
		
		JFileChooser templateFileChooser = new JFileChooser();
        templateFileChooser.setFileFilter(new FileNameExtensionFilter("Excel Dateien", "xls", "xlsx"));
        configChooser(templateFileChooser);
        
        JRadioButton templateRadioButton = new JRadioButton("Vorlage",true);
        
        templateRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (templateRadioButton.isSelected()) {
                	art = ExportArt.VORLAGE;
                }
            }
        });
        
        JRadioButton configFileRadioButton = new JRadioButton("Steuerdatei");
        configFileRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (configFileRadioButton.isSelected()) {
                	art = ExportArt.STEUERDATEI;
                }
            }
        });

        
        JRadioButton csvFileRadioButton = new JRadioButton("CSV Datei");
        csvFileRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (csvFileRadioButton.isSelected()) {
                	art = ExportArt.CSV;
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
        JPanel sqlFilePanel = new JPanel();
        JFileChooser sqlFileChooser = new JFileChooser();
        sqlFileChooser.setFileFilter(new FileNameExtensionFilter("Sql Dateien", "sql"));
        
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
        JButton testConnectionButton = new JButton("Verbindung testen");
       
        JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 3, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));
		
		final JTextField usernameField = new JTextField(20);
		usernameField.setText("");
		
		usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               user = usernameField.getText();
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
               password = passwordField.getText();
            }
        });
		
		fieldPanel.add(passwordField);
		addGlue(fieldPanel);
		        
		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.add(fieldPanel);
        addGlue(loginPanel);
        
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
	}

    public static void main(String[] args) {
    	SwingApp app = new SwingApp();
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
}
