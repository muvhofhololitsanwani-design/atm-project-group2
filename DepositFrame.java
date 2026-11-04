/*
 *  Author;      Vhukhudo Ravele
 *  Student No.; 260048496
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class DepositFrame extends JFrame implements ActionListener {

    private final MainMenuFrame menu;
    private final Account account;
    private JTextField amountField;
    private JButton depositButton;
    private JButton clearButton;
    private JButton backButton;
    private JLabel balanceLabel;

    public DepositFrame(MainMenuFrame menu, Account account) {
        this.menu = menu;
        this.account = account;
        initComponents();
    }

    private void initComponents() {
        setTitle("CPUT Smart ATM - Deposit");
        setSize(520, 520);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Override window close button (X) to return to the menu instead of exiting
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goBack();
            }
        });
        setLayout(new BorderLayout());
        getContentPane().setBackground(ATMStyle.LIGHT);

        add(ATMStyle.createHeader("Deposit Cash"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ATMStyle.LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Balance label at the top of the form
        balanceLabel = new JLabel("", SwingConstants.CENTER);
        balanceLabel.setFont(ATMStyle.HEADING_FONT);
        balanceLabel.setForeground(ATMStyle.GREEN);
        updateBalanceLabel();
        gbc.gridy = 0;
        form.add(balanceLabel, gbc);

        // Instruction label showing max deposit limits
        gbc.gridy = 1;
        gbc.insets = new Insets(24, 0, 6, 0);
        form.add(ATMStyle.createLabel("Enter the amount to deposit (max "
                + ATMStyle.formatMoney(Account.MAX_DEPOSIT) + "):",
                ATMStyle.TEXT_FONT, ATMStyle.NAVY), gbc);

        // Amount input text field
        amountField = new JTextField();
        amountField.setFont(ATMStyle.HEADING_FONT);
        amountField.setPreferredSize(new Dimension(300, 44));
        gbc.gridy = 2;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(amountField, gbc);

        depositButton = ATMStyle.createButton("Deposit", ATMStyle.GREEN);
        clearButton   = ATMStyle.createButton("Clear", ATMStyle.GREY);
        backButton    = ATMStyle.createButton("Back", ATMStyle.RED);
        depositButton.addActionListener(this);
        clearButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setBackground(ATMStyle.LIGHT);
        buttonPanel.add(depositButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        gbc.gridy = 3;
        gbc.insets = new Insets(22, 0, 0, 0);
        form.add(buttonPanel, gbc);

        // Glue component to push form contents toward the top
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        form.add(new JLabel(), gbc);

        add(form, BorderLayout.CENTER);
        getRootPane().setDefaultButton(depositButton);
        ATMStyle.centreOnScreen(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == depositButton) {
            processDeposit();
        } else if (source == clearButton) {
            amountField.setText("");
        } else if (source == backButton) {
            goBack();
        }
    }

    // Validates user input, processes the cash deposit, and handles potential exceptions
    private void processDeposit() {
        String text = amountField.getText().trim();

        if (text.isEmpty()) {
            ATMStyle.showError(this, "Please enter an amount first.");
            return;
        }
        if (!ATMStyle.isAmountText(text)) {
            ATMStyle.showError(this, "Please use numbers only, for example 500 or 250.50.");
            return;
        }

        try {
            double amount = Double.parseDouble(text);
            account.deposit(amount); // Throws InvalidAmountException if limits/rules are violated

            updateBalanceLabel();
            amountField.setText("");
            ATMStyle.showInfo(this, "Deposit successful!\n"
                    + "Amount deposited: " + ATMStyle.formatMoney(amount) + "\n"
                    + "New balance: " + ATMStyle.formatMoney(account.getBalance()));

        } catch (NumberFormatException ex) {
            ATMStyle.showError(this, "That is not a valid number.");
        } catch (InvalidAmountException ex) {
            ATMStyle.showError(this, ex.getMessage());
        }
    }

    // Refreshes the balance label with the latest account balance
    private void updateBalanceLabel() {
        balanceLabel.setText("Available balance: " + ATMStyle.formatMoney(account.getBalance()));
    }

    // Returns to the main menu screen and disposes of the deposit frame
    private void goBack() {
        menu.setVisible(true);
        dispose();
    }
}