// Author: Mabandla Nkosi
// Student number: 260407763
 
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame implements ActionListener {

    private final Bank bank;              
    private JTextField accountField;      
    private JPasswordField pinField;      
    private JButton loginButton;          
    private JButton clearButton;          
    private JButton exitButton;           
    private JLabel messageLabel;          

    public LoginFrame(Bank bank) {
        this.bank = bank;
        initComponents();             
    }

    private void initComponents() {
        setTitle("CPUT Smart ATM - Login");
        setSize(520, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ATMStyle.LIGHT);

        add(ATMStyle.createHeader("Welcome - Please Log In"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        
        // Quick demo hint displayed at the bottom for testing and grading purposes
        JLabel hintLabel = new JLabel("Demo card: 1234567890   |   PIN: 1234", SwingConstants.CENTER);
        hintLabel.setFont(ATMStyle.SMALL_FONT);
        hintLabel.setForeground(ATMStyle.GREY);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));
        add(hintLabel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);   // Pressing Enter triggers the login button
        ATMStyle.centreOnScreen(this);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ATMStyle.LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Account number field label and text input
        gbc.gridy = 0;
        form.add(ATMStyle.createLabel("Account number (10 digits)", ATMStyle.TEXT_FONT, ATMStyle.NAVY), gbc);

        accountField = new JTextField();
        accountField.setFont(ATMStyle.TEXT_FONT);
        accountField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 1;
        form.add(accountField, gbc);

        // PIN field label and password input
        gbc.gridy = 2;
        gbc.insets = new Insets(18, 0, 6, 0);
        form.add(ATMStyle.createLabel("PIN (4 digits)", ATMStyle.TEXT_FONT, ATMStyle.NAVY), gbc);

        pinField = new JPasswordField();
        pinField.setFont(ATMStyle.TEXT_FONT);
        pinField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(pinField, gbc);

        // Error or status message label
        messageLabel = ATMStyle.createLabel(" ", ATMStyle.SMALL_FONT, ATMStyle.RED);
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 0, 12, 0);
        form.add(messageLabel, gbc);

        // Action buttons layout
        loginButton = ATMStyle.createButton("Login", ATMStyle.GREEN);
        clearButton = ATMStyle.createButton("Clear", ATMStyle.GREY);
        exitButton  = ATMStyle.createButton("Exit", ATMStyle.RED);
        loginButton.addActionListener(this);
        clearButton.addActionListener(this);
        exitButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setBackground(ATMStyle.LIGHT);
        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        form.add(buttonPanel, gbc);

        // Vertical glue constraint to push form components toward the top
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        form.add(new JLabel(), gbc);

        return form;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();          

        if (source == loginButton) {
            handleLogin();
        } else if (source == clearButton) {
            clearFields();
        } else if (source == exitButton) {
            System.exit(0);
        }
    }

    // Validates inputs and handles authentication through the Bank model
    private void handleLogin() {
        String accountNumber = accountField.getText().trim();
        String pin = new String(pinField.getPassword());

        // Basic format and presence validation
        if (accountNumber.isEmpty() || pin.isEmpty()) {
            showMessage("Please enter your account number and PIN.");
            return;
        }
        if (!ATMStyle.isDigitsOnly(accountNumber) || accountNumber.length() != 10) {
            showMessage("The account number must be exactly 10 digits.");
            return;
        }
        if (!ATMStyle.isDigitsOnly(pin) || pin.length() != 4) {
            showMessage("The PIN must be exactly 4 digits.");
            return;
        }

        // Authenticate credentials against the bank registry
        int result = bank.authenticate(accountNumber, pin);

        switch (result) {
            case Bank.AUTH_SUCCESS: {
                Account account = bank.findAccount(accountNumber);
                MainMenuFrame menu = new MainMenuFrame(account);   // Open main menu screen
                menu.setVisible(true);
                dispose();                                         // Close login window
                break;
            }
            case Bank.AUTH_NO_ACCOUNT:
                showMessage("Account not found. Please check the number.");
                break;
            case Bank.AUTH_WRONG_PIN: {
                Account account = bank.findAccount(accountNumber);
                showMessage("Incorrect PIN. Attempts left: " + account.getRemainingAttempts());
                pinField.setText("");
                break;
            }
            case Bank.AUTH_LOCKED:
                pinField.setText("");
                showMessage("This card is blocked.");
                ATMStyle.showError(this, "Your card has been blocked after "
                        + Account.MAX_PIN_ATTEMPTS + " wrong PIN attempts.\n"
                        + "Please contact your bank.\n"
                        + "(Demo: restart the program to unblock the card.)");
                break;
            default:
                showMessage("Unknown error. Please try again.");
                break;
        }
    }

    private void clearFields() {
        accountField.setText("");
        pinField.setText("");
        messageLabel.setText(" ");
        accountField.requestFocus();
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}