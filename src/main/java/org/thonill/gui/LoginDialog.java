package org.thonill.gui;

import javax.swing.*;
import org.thonill.sql.ConnectionInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

 /**
     * This class provides a login dialog GUI component.
     * It contains fields for username, password and buttons for login, test and
     * cancel.
     * It has methods to start login, test the connection, cancel/exit.
     * It also provides utility methods to get database connections and show message
     * boxes.
     */
public class LoginDialog extends JDialog {
    private JFrame frame;
    private JTextField usernameField, passwordField;

    public LoginDialog() {
        super();
        createAndShowGUI();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("Anmeldung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new BoxLayout( frame.getContentPane(), BoxLayout.Y_AXIS));



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
    
}
private void testen() {
       try {
            ConnectionInfo info = new ConnectionInfo("testDb", "sa", "","app\\src\\test\\resources");
            Connection conn = info.getConnection();
           conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Es ist ein Verbindungsfehler aufgetreten!");  
        }
}
private void abbrechen() {
    frame.dispose();
     System.exit(0);
}

public Connection getConnection(String connectionName,String connectionInfoPath) throws Exception {
    ConnectionInfo info = new ConnectionInfo(connectionName, usernameField.getText(), passwordField.getText(),connectionInfoPath); 
    return info.getConnection();
}


public void msgBox(String message,int messageType) {
     JOptionPane.showMessageDialog(frame,message,"Message",messageType);
}
   

}
