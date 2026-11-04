/*
 *  AUTHOR: Athenkosi Khau
 *  STUDENT NUMBER: 215107020
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class BalanceFrame extends JFrame implements ActionListener {

    private final MainMenuFrame menu;
    private final Account account;
    private JTextArea statementArea;
    private JButton backButton;

    public BalanceFrame(MainMenuFrame menu, Account account) {
        this.menu = menu;
        this.account = account;
        initComponents();
        loadStatement(); // Populates the mini-statement text area on load
    }

    private void initComponents() {
        setTitle("CPUT Smart ATM - Check Balance");
        setSize(520, 640);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Override window close button (X) to return to the main menu instead of closing the application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goBack();
            }
        });
        setLayout(new BorderLayout());
        getContentPane().setBackground(ATMStyle.LIGHT);

        add(ATMStyle.createHeader("Account Balance"), BorderLayout.NORTH);

        // Panel displaying account holder details, masked account number, and current balance
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        infoPanel.setBackground(ATMStyle.LIGHT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(22, 20, 10, 20));

        infoPanel.add(centredLabel(account.getHolderName(), ATMStyle.HEADING_FONT, ATMStyle.NAVY));
        infoPanel.add(centredLabel("Account: " + ATMStyle.maskAccountNumber(account.getAccountNumber()),
                ATMStyle.SMALL_FONT, ATMStyle.GREY));
        infoPanel.add(centredLabel("Available balance", ATMStyle.TEXT_FONT, ATMStyle.NAVY));
        infoPanel.add(centredLabel(ATMStyle.formatMoney(account.getBalance()),
                ATMStyle.MONEY_FONT, ATMStyle.GREEN));

        // Setup scrollable text area for the mini statement
        statementArea = new JTextArea();
        statementArea.setEditable(false);
        statementArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        statementArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(statementArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mini statement (last 5 transactions)"));

        JPanel statementPanel = new JPanel(new BorderLayout());
        statementPanel.setBackground(ATMStyle.LIGHT);
        statementPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 25));
        statementPanel.add(scrollPane, BorderLayout.CENTER);

        backButton = ATMStyle.createButton("Back", ATMStyle.RED);
        backButton.addActionListener(this);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(ATMStyle.LIGHT);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
        bottomPanel.add(backButton);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(ATMStyle.LIGHT);
        centre.add(infoPanel, BorderLayout.NORTH);
        centre.add(statementPanel, BorderLayout.CENTER);
        centre.add(bottomPanel, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        getRootPane().setDefaultButton(backButton);
        ATMStyle.centreOnScreen(this);
    }

    // Helper method to create centered labels with specified font and color
    private JLabel centredLabel(String text, Font font, Color colour) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(colour);
        return label;
    }

    // Fetches recent transactions from the account and displays them in the mini statement box
    private void loadStatement() {
        ArrayList<Transaction> recent = account.getLastTransactions(5);

        if (recent.isEmpty()) {
            statementArea.setText("No transactions yet.");
            return;
        }

        String text = "";
        for (Transaction transaction : recent) {
            text = text + transaction.toString() + "\n";
        }
        statementArea.setText(text);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            goBack();
        }
    }

    // Returns to the main menu screen and disposes of the balance frame
    private void goBack() {
        menu.setVisible(true);
        dispose();
    }
}