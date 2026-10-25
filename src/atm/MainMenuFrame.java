/*
 * File:        MainMenuFrame.java
 * Author:      Athenkosi Khau
 * Role:        Member 5, Menu screen
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: The home screen after login. Offers the four ATM options:
 *              Withdraw, Deposit, Check Balance and Exit. Exit ends the
 *              session and closes the program with System.exit(0).
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainMenuFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Action commands let one method handle all four buttons with a switch
    private static final String CMD_WITHDRAW = "WITHDRAW";
    private static final String CMD_DEPOSIT = "DEPOSIT";
    private static final String CMD_BALANCE = "BALANCE";
    private static final String CMD_EXIT = "EXIT";

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    private final ATM atm;
    private JButton btnWithdraw;
    private JButton btnDeposit;
    private JButton btnBalance;
    private JButton btnExit;

    /** Builds and shows the main menu for the logged-in customer. */
    public MainMenuFrame(ATM atm) {
        this.atm = atm;

        UIStyle.styleFrame(this, "Main Menu");

        JPanel pnlMain = UIStyle.createScreenPanel();
        pnlMain.add(UIStyle.createHeader(greeting(),
                "Account " + atm.getCurrentAccountNumber() + "  |  What would you like to do?"),
                BorderLayout.NORTH);
        pnlMain.add(buildButtons(), BorderLayout.CENTER);
        pnlMain.add(UIStyle.createHintLabel("Auto sign-out after "
                + AppConstants.SESSION_TIMEOUT_SECONDS + " seconds of inactivity."),
                BorderLayout.SOUTH);
        add(pnlMain);

        setVisible(true);
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    /** A friendly greeting that depends on the time of day. */
    private String greeting() {
        int hour = LocalTime.now().getHour();

        if (hour < 12) {
            return "Good morning";
        } else if (hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    /** Four large buttons, one per ATM option. */
    private JPanel buildButtons() {
        btnWithdraw = createChoice("Withdraw", CMD_WITHDRAW, KeyEvent.VK_W);
        btnDeposit = createChoice("Deposit", CMD_DEPOSIT, KeyEvent.VK_D);
        btnBalance = createChoice("Check Balance", CMD_BALANCE, KeyEvent.VK_B);

        // Exit is red so it is never pressed by accident
        btnExit = UIStyle.createDangerButton("Exit");
        btnExit.setActionCommand(CMD_EXIT);
        btnExit.setMnemonic(KeyEvent.VK_X);
        btnExit.addActionListener(e -> handleChoice(e.getActionCommand()));
        makeTall(btnExit);

        // Stack the buttons from the top with an even gap between them
        JPanel column = UIStyle.createColumn();
        column.add(btnWithdraw);
        column.add(Box.createRigidArea(new Dimension(0, 14)));
        column.add(btnDeposit);
        column.add(Box.createRigidArea(new Dimension(0, 14)));
        column.add(btnBalance);
        column.add(Box.createRigidArea(new Dimension(0, 14)));
        column.add(btnExit);
        return column;
    }

    /** Creates one main-menu button wired to handleChoice. */
    private JButton createChoice(String text, String command, int mnemonic) {
        JButton button = UIStyle.createButton(text);
        button.setActionCommand(command);
        button.setMnemonic(mnemonic);              // Alt+letter shortcut
        button.addActionListener(e -> handleChoice(e.getActionCommand()));
        makeTall(button);
        return button;
    }

    /** Menu buttons are taller than normal ones, like big touch-screen keys. */
    private void makeTall(JButton button) {
        button.setPreferredSize(new Dimension(340, 60));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    }

    // ------------------------------------------------------------------
    // Button logic
    // ------------------------------------------------------------------

    /** Opens the screen that matches the button pressed. */
    private void handleChoice(String command) {
        switch (command) {
            case CMD_WITHDRAW:
                dispose();
                new WithdrawFrame(atm);
                break;
            case CMD_DEPOSIT:
                dispose();
                new DepositFrame(atm);
                break;
            case CMD_BALANCE:
                dispose();
                new BalanceFrame(atm);
                break;
            case CMD_EXIT:
                confirmExit();
                break;
            default:
                // Cannot happen: only the four buttons above use this method
                break;
        }
    }

    /** Asks first, then logs out and closes the whole program. */
    private void confirmExit() {
        int answer = JOptionPane.showConfirmDialog(this,
                "End your session and exit the ATM?", "Exit", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            atm.logout();
            System.exit(0);
        }
    }
}
