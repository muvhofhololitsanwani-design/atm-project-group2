/*
 *  Author: Muvhofhololi Tsanwani
 *  Student number: 261021222
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

public class WithdrawFrame extends JFrame implements ActionListener
{

    private final MainMenuFrame menu; // Screen to return to
    private final Account account; // The logged-in customer
    private final int[] quickAmounts = {100, 200, 500, 1000, 2000, 5000}; // Quick cash preset options
    private JButton[] quickButtons; // Buttons for quick cash amounts
    private JTextField amountField; // Text field for custom withdrawal amounts
    private JButton withdrawButton;
    private JButton clearButton;
    private JButton backButton;
    private JLabel balanceLabel; // Displays the current account balance

    public WithdrawFrame(MainMenuFrame menu, Account account) {
        this.menu = menu;
        this.account = account;
        initComponents();
    }

    private void initComponents() {
        setTitle("CPUT Smart ATM - Withdraw");
        setSize(520, 640);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Override window close button (X) to return to the main menu instead of closing abruptly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goBack();
            }
        });
        setLayout(new BorderLayout());
        getContentPane().setBackground(ATMStyle.LIGHT);

        add(ATMStyle.createHeader("Withdraw Cash"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ATMStyle.LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Balance display at the top of the form
        balanceLabel = new JLabel("", SwingConstants.CENTER);
        balanceLabel.setFont(ATMStyle.HEADING_FONT);
        balanceLabel.setForeground(ATMStyle.GREEN);
        updateBalanceLabel();
        gbc.gridy = 0;
        form.add(balanceLabel, gbc);

        // Quick cash preset buttons arranged in a 2x3 grid
        gbc.gridy = 1;
        form.add(ATMStyle.createLabel("Quick cash - tap an amount:", ATMStyle.TEXT_FONT, ATMStyle.NAVY), gbc);

        JPanel quickPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickPanel.setBackground(ATMStyle.LIGHT);
        quickButtons = new JButton[quickAmounts.length];
        for (int i = 0; i < quickAmounts.length; i++) {
            quickButtons[i] = ATMStyle.createButton(ATMStyle.formatMoney(quickAmounts[i]), ATMStyle.BLUE);
            quickButtons[i].addActionListener(this);
            quickPanel.add(quickButtons[i]);
        }
        gbc.gridy = 2;
        form.add(quickPanel, gbc);

        // Custom amount input prompt referencing note denomination rules
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 6, 0);
        form.add(ATMStyle.createLabel("Or type another amount (multiples of R10):",
                ATMStyle.TEXT_FONT, ATMStyle.NAVY), gbc);

        amountField = new JTextField();
        amountField.setFont(ATMStyle.HEADING_FONT);
        amountField.setPreferredSize(new Dimension(300, 44));
        gbc.gridy = 4;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(amountField, gbc);

        // Action buttons
        withdrawButton = ATMStyle.createButton("Withdraw", ATMStyle.GREEN);
        clearButton    = ATMStyle.createButton("Clear", ATMStyle.GREY);
        backButton     = ATMStyle.createButton("Back", ATMStyle.RED);
        withdrawButton.addActionListener(this);
        clearButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setBackground(ATMStyle.LIGHT);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        gbc.gridy = 5;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(buttonPanel, gbc);

        // Vertical glue constraint to push form components toward the top
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        form.add(new JLabel(), gbc);

        add(form, BorderLayout.CENTER);
        getRootPane().setDefaultButton(withdrawButton); // Pressing Enter triggers the withdraw button
        ATMStyle.centreOnScreen(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == withdrawButton) {
            processWithdrawal();
        } else if (source == clearButton) {
            amountField.setText("");
        } else if (source == backButton) {
            goBack();
        } else {
            // Identify which quick cash button was clicked and populate the input field
            for (int i = 0; i < quickButtons.length; i++) {
                if (source == quickButtons[i]) {
                    amountField.setText(String.valueOf(quickAmounts[i]));
                }
            }
        }
    }

    // Validates input, enforces withdrawal limits and note rules, and updates account balance
    private void processWithdrawal() {
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
            account.withdraw(amount); // Enforces transaction limits, note multiples, and funds

            updateBalanceLabel();
            amountField.setText("");
            ATMStyle.showInfo(this, "Withdrawal successful!\n"
                    + "Please take your cash: " + ATMStyle.formatMoney(amount) + "\n"
                    + "Remaining balance: " + ATMStyle.formatMoney(account.getBalance()));

        } catch (NumberFormatException ex) {
            ATMStyle.showError(this, "That is not a valid number.");
        } catch (InvalidAmountException ex) {
            ATMStyle.showError(this, ex.getMessage());
        } catch (InsufficientFundsException ex) {
            ATMStyle.showError(this, ex.getMessage());
        }
    }

    // Refreshes the balance label with the latest account balance
    private void updateBalanceLabel() {
        balanceLabel.setText("Available balance: " + ATMStyle.formatMoney(account.getBalance()));
    }

    // Returns to the main menu screen and disposes of the withdrawal frame
    private void goBack() {
        menu.setVisible(true);
        dispose();
    }
}
