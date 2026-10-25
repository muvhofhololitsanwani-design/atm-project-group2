/*
 * File:        WithdrawFrame.java
 * Author:      Kgonyane Makgoale
 * Role:        Member 6, Withdraw screen
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Lets the customer withdraw cash. The amount is validated,
 *              then the ATM applies the cash rules (whole notes, balance,
 *              daily limit). Success and every kind of error are shown with
 *              a clear message.
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

public class WithdrawFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    private final ATM atm;
    private JTextField txtAmount;      // the amount to withdraw
    private JButton btnConfirm;
    private JButton btnBack;
    private JLabel lblLimit;           // how much may still be withdrawn today
    private JLabel lblMessage;         // success or error feedback

    /** Builds and shows the withdraw screen. */
    public WithdrawFrame(ATM atm) {
        this.atm = atm;

        UIStyle.styleFrame(this, "Withdraw");

        JPanel pnlMain = UIStyle.createScreenPanel();
        pnlMain.add(UIStyle.createHeader("Withdraw",
                "Notes only: enter a multiple of R" + (int) AppConstants.NOTE_DENOMINATION),
                BorderLayout.NORTH);
        pnlMain.add(buildForm(), BorderLayout.CENTER);
        pnlMain.add(buildFooter(), BorderLayout.SOUTH);
        add(pnlMain);

        // Button logic
        btnConfirm.addActionListener(e -> handleConfirm());
        btnBack.addActionListener(e -> goBack());
        getRootPane().setDefaultButton(btnConfirm);      // Enter confirms

        refreshLimit();
        setVisible(true);
        txtAmount.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    private JPanel buildForm() {
        txtAmount = UIStyle.createTextField();
        lblLimit = UIStyle.createHintLabel(" ");

        JPanel card = UIStyle.createCardPanel();
        card.add(UIStyle.createLabel("Amount (R)"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(txtAmount);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(UIStyle.createQuickAmountPanel(txtAmount, AppConstants.getQuickAmounts()));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(lblLimit);

        JPanel holder = UIStyle.createColumn();
        holder.add(card);
        return holder;
    }

    private JPanel buildFooter() {
        lblMessage = UIStyle.createMessageLabel();
        btnConfirm = UIStyle.createButton("Confirm withdrawal");
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

    /** Validates the amount, asks the ATM to withdraw and reports the outcome. */
    private void handleConfirm() {
        try {
            double amount = InputValidator.parseAmount(txtAmount.getText());
            atm.withdraw(amount);

            UIStyle.showSuccess(lblMessage, "Withdrawal successful: " + UIStyle.formatMoney(amount)
                    + "<br>New balance: " + UIStyle.formatMoney(atm.getBalance()));
            txtAmount.setText("");

        } catch (InvalidAmountException ex) {
            // Not a valid amount, not whole notes, or over the daily limit
            UIStyle.showError(lblMessage, ex.getMessage());

        } catch (InsufficientFundsException ex) {
            UIStyle.showError(lblMessage, "Insufficient funds.<br>Available balance: "
                    + UIStyle.formatMoney(ex.getAvailable()));

        } finally {
            // Runs after every attempt, so the limit shown is always up to date
            refreshLimit();
            txtAmount.requestFocusInWindow();
        }
    }

    /** Shows how much more may be withdrawn today. */
    private void refreshLimit() {
        lblLimit.setText("Still available to withdraw today: "
                + UIStyle.formatMoney(atm.getRemainingDailyLimit()));
    }

    /** Returns to the main menu. */
    private void goBack() {
        dispose();
        new MainMenuFrame(atm);
    }
}
