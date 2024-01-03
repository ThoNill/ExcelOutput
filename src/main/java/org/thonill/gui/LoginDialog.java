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
public class LoginDialog extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JFrame frame;
	private String connectionInfoPath;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private SetMy<ConnectionInfo> setMy;

	public LoginDialog(SetMy<ConnectionInfo> setMy, String connectionInfoPath) {
		super();
		this.setMy = setMy;
		this.connectionInfoPath = connectionInfoPath;
		createAndShowGUI();
	}

	private void createAndShowGUI() {

		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(2, 2, 5, 5));

		// Labels and TextFields for Username and Password
		fieldPanel.add(new JLabel("Benutzername:"));
		usernameField = new JTextField();
		usernameField.setText("");
		fieldPanel.add(usernameField);

		fieldPanel.add(new JLabel("Passwort:"));
		passwordField = new JPasswordField();
		passwordField.setText("");
		fieldPanel.add(passwordField);

		// Buttons: Start, Test, Abbruch
		JButton startButton = new JButton("Start");
		JButton testButton = new JButton("Test");
		JButton cancelButton = new JButton("Abbruch");

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == startButton) {
					start();
				}

			}

		});

		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == testButton) {
					testen();
				}
			}

		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == cancelButton) {
					abbrechen();
				}
			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(startButton);
		buttonPanel.add(testButton);
		buttonPanel.add(cancelButton);

		frame = new JFrame("Anmeldung");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 150);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		frame.add(fieldPanel);
		frame.add(buttonPanel);
		frame.setVisible(true);
	}

	protected void start() {
		frame.setVisible(false);
		setConnectionInfo();
		frame.dispose();
	};

	// "app\\src\\test\\resources"

	private void testen() {
		try {
			Connection conn = createConnection();
			conn.close();
			msgBox("Connection ist ok", JOptionPane.OK_OPTION);
		} catch (Exception e) {
			msgBox("Es ist ein Verbindungsfehler aufgetreten!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private Connection createConnection() {
		return createConnectionInfo().createConnection();
	}

	private void abbrechen() {
		frame.setVisible(false);
		frame.dispose();
		setMy.setValue(null);
	}

	public void setConnectionInfo() throws ApplicationException {
		setMy.setValue(createConnectionInfo());
	}

	private ConnectionInfo createConnectionInfo() {
		return new ConnectionInfo(usernameField.getText(), passwordField.getText(), connectionInfoPath);
	}

	public void msgBox(String message, int messageType) {
		JOptionPane.showMessageDialog(this, message, "Message", messageType);
	}

}
