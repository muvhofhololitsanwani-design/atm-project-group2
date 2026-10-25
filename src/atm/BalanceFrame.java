/*
 * File:        BalanceFrame.java
 * Author:      Siyolise Mhlauli
 * Role:        Member 8, Integration lead
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Shows the customer's current balance and a mini statement of
 *              the most recent transactions (deposits in green, withdrawals
 *              in red).
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class BalanceFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Date style for the statement, for example "23 Oct, 14:32"
    private static final DateTimeFormatter WHEN_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.ENGLISH);

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    private final ATM atm;
    private JLabel lblBalance;       // the big balance figure
    private JButton btnBack;

    /** Builds and shows the balance screen. */
    public BalanceFrame(ATM atm) {
        this.atm = atm;

        UIStyle.styleFrame(this, "Balance");

        JPanel pnlMain = UIStyle.createScreenPanel();
        pnlMain.add(UIStyle.createHeader("Your balance",
                "Account " + atm.getCurrentAccountNumber()), BorderLayout.NORTH);
        pnlMain.add(buildBody(), BorderLayout.CENTER);
        pnlMain.add(buildFooter(), BorderLayout.SOUTH);
        add(pnlMain);

        btnBack.addActionListener(e -> goBack());
        getRootPane().setDefaultButton(btnBack);

        setVisible(true);
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    /** Balance card on top, mini statement below. */
    private JPanel buildBody() {
        // Large balance figure
        lblBalance = new JLabel(UIStyle.formatMoney(atm.getBalance()));
        lblBalance.setFont(lblBalance.getFont().deriveFont(Font.BOLD, 36f));
        lblBalance.setForeground(UIStyle.ACCENT);
        lblBalance.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JPanel balanceCard = UIStyle.createCardPanel();
        balanceCard.add(UIStyle.createHintLabel("Available balance"));
        balanceCard.add(Box.createRigidArea(new Dimension(0, 4)));
        balanceCard.add(lblBalance);

        JPanel body = UIStyle.createColumn();
        body.add(balanceCard);
        body.add(Box.createRigidArea(new Dimension(0, 18)));
        body.add(UIStyle.createLabel("Recent transactions"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(buildStatement());
        return body;
    }

    /** One row per recent transaction, newest first. */
    private JPanel buildStatement() {
        List<Transaction> recent = atm.getRecentTransactions(AppConstants.MINI_STATEMENT_SIZE);
        JPanel card = UIStyle.createCardPanel();

        if (recent.isEmpty()) {
            card.add(UIStyle.createHintLabel("No transactions yet. Deposits and withdrawals appear here."));
        }
        for (Transaction t : recent) {
            card.add(buildRow(t));
        }
        return card;
    }

    /** A single statement line: type, date and a coloured amount. */
    private JPanel buildRow(Transaction t) {
        boolean isDeposit = t.getType() == Transaction.Type.DEPOSIT;

        JLabel lblType = UIStyle.createLabel(t.getType().getLabel());
        lblType.setPreferredSize(new Dimension(96, 26));

        JLabel lblWhen = UIStyle.createHintLabel(t.getTimestamp().format(WHEN_FORMAT));

        JLabel lblAmount = UIStyle.createLabel((isDeposit ? "+" : "-") + UIStyle.formatMoney(t.getAmount()));
        lblAmount.setForeground(isDeposit ? UIStyle.SUCCESS : UIStyle.ERROR);
        lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel row = UIStyle.createPanel();
        row.setLayout(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(lblType, BorderLayout.WEST);
        row.add(lblWhen, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);
        return row;
    }

    private JPanel buildFooter() {
        btnBack = UIStyle.createSecondaryButton("Back to menu");

        JPanel footer = UIStyle.createColumn();
        footer.add(btnBack);
        return footer;
    }

    // ------------------------------------------------------------------
    // Button logic
    // ------------------------------------------------------------------

    /** Returns to the main menu. */
    private void goBack() {
        dispose();
        new MainMenuFrame(atm);
    }
}
