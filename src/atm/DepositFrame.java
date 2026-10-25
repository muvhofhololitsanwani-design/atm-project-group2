/*
 * File:        DepositFrame.java
 * Author:      Ndzalama Mthombeni
 * Role:        Member 7, Deposit screen
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Lets the customer deposit money. The amount is validated
 *              before the ATM adds it to the balance, and the new balance
 *              is confirmed on screen.
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DepositFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    private final ATM atm;
    private JTextField txtAmount;      // the amount to deposit
    private JButton btnConfirm;
    private JButton btnBack;
    private JLabel lblBalance;         // current balance, refreshed after each deposit
    private JLabel lblMessage;         // success or error feedback

    /** Builds and shows the deposit screen. */
    public DepositFrame(ATM atm) {
        this.atm = atm;

        UIStyle.styleFrame(this, "Deposit");

        JPanel pnlMain = UIStyle.createScreenPanel();
        pnlMain.add(UIStyle.createHeader("Deposit", "Choose a quick amount or type your own"),
                BorderLayout.NORTH);
        pnlMain.add(buildForm(), BorderLayout.CENTER);
        pnlMain.add(buildFooter(), BorderLayout.SOUTH);
        add(pnlMain);

        // Button logic
        btnConfirm.addActionListener(e -> handleConfirm());
        btnBack.addActionListener(e -> goBack());
        getRootPane().setDefaultButton(btnConfirm);      // Enter confirms

        refreshBalance();
        setVisible(true);
        txtAmount.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    private JPanel buildForm() {
        txtAmount = UIStyle.createTextField();
        lblBalance = UIStyle.createHintLabel(" ");

        JPanel card = UIStyle.createCardPanel();
        card.add(UIStyle.createLabel("Amount (R)"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(txtAmount);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(UIStyle.createQuickAmountPanel(txtAmount, AppConstants.getQuickAmounts()));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(lblBalance);

        JPanel holder = UIStyle.createColumn();
        holder.add(card);
        return holder;
    }

    private JPanel buildFooter() {
        lblMessage = UIStyle.createMessageLabel();
        btnConfirm = UIStyle.createButton("Confirm deposit");
        btnBack = UIStyle.createSecondaryButton("Back to menu");

        JPanel footer = UIStyle.createColumn();
        footer.add(lblMessage);
        footer.add(Box.createRigidArea(new Dimension(0, 6)));
        footer.add(btnConfirm);
        footer.add(Box.createRigidArea(new Dimension(0, 10)));
        footer.add(btnBack);
        return footer;
    }

    // ------------------------------------------------------------------
    // Button logic
    // ------------------------------------------------------------------

    /** Validates the amount, asks the ATM to deposit and reports the outcome. */
    private void handleConfirm() {
        try {
            double amount = InputValidator.parseAmount(txtAmount.getText());
            atm.deposit(amount);

            UIStyle.showSuccess(lblMessage, "Deposit successful: " + UIStyle.formatMoney(amount)
                    + "<br>New balance: " + UIStyle.formatMoney(atm.getBalance()));
            txtAmount.setText("");

        } catch (InvalidAmountException ex) {
            UIStyle.showError(lblMessage, ex.getMessage());

        } finally {
            refreshBalance();
            txtAmount.requestFocusInWindow();
        }
    }

    /** Shows the current balance under the amount field. */
    private void refreshBalance() {
        lblBalance.setText("Current balance: " + UIStyle.formatMoney(atm.getBalance()));
    }

    /** Returns to the main menu. */
    private void goBack() {
        dispose();
        new MainMenuFrame(atm);
    }
}
