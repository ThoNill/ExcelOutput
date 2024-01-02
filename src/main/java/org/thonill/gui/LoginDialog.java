package org.thonill.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.thonill.exceptions.ApplicationException;
import org.thonill.sql.ConnectionInfo;

/**
 * This class provides a login dialog GUI component. It contains fields for
 * username, password and buttons for login, test and cancel. It has methods to
 * start login, test the connection, cancel/exit. It also provides utility
 * methods to get database connections and show message boxes.
 */
public class LoginDialog extends JDialog {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String connectionInfoPath;

	private JFrame frame;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private transient Connection connection;

	public LoginDialog(String connectionInfoPath) {
		super();
		connection = null;
		this.connectionInfoPath = connectionInfoPath;
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		frame = new JFrame("Anmeldung");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(300, 150);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 2, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));
		usernameField = new JTextField();
		fieldPanel.add(usernameField);

		fieldPanel.add(new JLabel("Passwort:"));
		passwordField = new JPasswordField();
		fieldPanel.add(passwordField);

		// Buttons: Start, Test, Abbruch
		JButton startButton = new JButton("Start");
		JButton testButton = new JButton("Test");
		JButton cancelButton = new JButton("Abbruch");

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();

			}

		});

		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				testen();
				JOptionPane.showMessageDialog(frame, "Test-Button wurde geklickt!");
			}

		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abbrechen();

			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(startButton);
		buttonPanel.add(testButton);
		buttonPanel.add(cancelButton);

		frame.add(fieldPanel);
		frame.add(buttonPanel);
		frame.setVisible(true);
	}

	protected void start() {
		getConnection();
		frame.dispose();
	};

	// "app\\src\\test\\resources"

	private void testen() {
		try {
			Connection conn = createConnection();
			conn.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Es ist ein Verbindungsfehler aufgetreten!");
		}
	}

	private void abbrechen() {
		frame.dispose();
		System.exit(0);
	}

	public Connection getConnection() throws ApplicationException {
		if (connection == null) {
			connection = createConnection();
		}
		return connection;
	}

	private Connection createConnection() {
		ConnectionInfo info = new ConnectionInfo(usernameField.getText(), passwordField.getText(), connectionInfoPath);
		return info.getConnection();
	}

	public void msgBox(String message, int messageType) {
		JOptionPane.showMessageDialog(frame, message, "Message", messageType);
	}

}
