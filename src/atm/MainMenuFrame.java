/*
 *  Author: Muvhofhololi Tsanwani
 *  Student number: 261021222
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuFrame extends JFrame implements ActionListener {

    private final Account account;        // The customer currently logged in
    private JButton withdrawButton;
    private JButton depositButton;
    private JButton balanceButton;
    private JButton exitButton;

    public MainMenuFrame(Account account) {
        this.account = account;
        initComponents();
    }

    private void initComponents() {
        setTitle("CPUT Smart ATM - Main Menu");
        setSize(520, 560);
        setResizable(false);
        // Intercept window close (X button) to prompt for session confirmation
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        setLayout(new BorderLayout());
        getContentPane().setBackground(ATMStyle.LIGHT);

        add(ATMStyle.createHeader("Main Menu"), BorderLayout.NORTH);

        // Welcome panel displaying holder name, masked account number, and prompt
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1, 0, 4));
        welcomePanel.setBackground(ATMStyle.LIGHT);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(22, 20, 6, 20));
        welcomePanel.add(centredLabel("Welcome, " + account.getHolderName(),
                ATMStyle.HEADING_FONT, ATMStyle.NAVY));
        welcomePanel.add(centredLabel("Account: " + ATMStyle.maskAccountNumber(account.getAccountNumber()),
                ATMStyle.SMALL_FONT, ATMStyle.GREY));
        welcomePanel.add(centredLabel("Please choose a transaction", ATMStyle.TEXT_FONT, ATMStyle.NAVY));

        // Four primary menu option buttons arranged in a 2x2 grid
        withdrawButton = ATMStyle.createButton("Withdraw", ATMStyle.BLUE);
        depositButton  = ATMStyle.createButton("Deposit", ATMStyle.BLUE);
        balanceButton  = ATMStyle.createButton("Check Balance", ATMStyle.BLUE);
        exitButton     = ATMStyle.createButton("Exit", ATMStyle.RED);

        JButton[] allButtons = {withdrawButton, depositButton, balanceButton, exitButton};
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBackground(ATMStyle.LIGHT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        for (JButton button : allButtons) {
            button.setFont(ATMStyle.HEADING_FONT);
            button.addActionListener(this);          // This frame listens for button clicks
            buttonPanel.add(button);
        }

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(ATMStyle.LIGHT);
        centre.add(welcomePanel, BorderLayout.NORTH);
        centre.add(buttonPanel, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        ATMStyle.centreOnScreen(this);
    }

    // Helper method to create centered labels
    private JLabel centredLabel(String text, Font font, Color colour) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(colour);
        return label;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == withdrawButton) {
            WithdrawFrame screen = new WithdrawFrame(this, account);
            openScreen(screen);
        } else if (source == depositButton) {
            DepositFrame screen = new DepositFrame(this, account);
            openScreen(screen);
        } else if (source == balanceButton) {
            BalanceFrame screen = new BalanceFrame(this, account);
            openScreen(screen);
        } else if (source == exitButton) {
            confirmExit();
        }
    }

    // Opens a specific transaction screen and hides the main menu until returning
    private void openScreen(JFrame screen) {
        screen.setVisible(true);
        this.setVisible(false);
    }

    // Prompts the user for confirmation before ending the session and closing the application
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to end your session and exit?",
                "Exit", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ATMStyle.showInfo(this, "Thank you for using " + ATMStyle.BANK_NAME
                    + ".\nPlease remember to take your card.");
            System.exit(0);            // Terminate program
        }
    }
}