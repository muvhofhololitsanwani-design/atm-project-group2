/*
 * File:        LoginFrame.java
 * Author:      Muvhofhololi Tsanwani
 * Role:        Member 4, Team leader and Login screen
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: First screen the customer sees. Collects the account number
 *              and PIN, checks them with the ATM and opens the main menu.
 *              After too many wrong attempts the card is blocked and the
 *              program closes.
 */
package atm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------
    // Fields (private: only this screen touches its own components)
    // ------------------------------------------------------------------
    private final ATM atm;                 // the one shared ATM object
    private JTextField txtAccNo;           // account number input
    private JPasswordField pfPin;          // PIN input (hidden characters)
    private JCheckBox chkShowPin;          // reveals the PIN while ticked
    private JButton btnLogin;
    private JLabel lblMessage;             // error feedback
    private char defaultEcho;              // the dot character used to hide the PIN

    /** Builds and shows the login screen. */
    public LoginFrame(ATM atm) {
        this.atm = atm;

        UIStyle.styleFrame(this, "Login");

        // Screen = header on top, form in the middle, button and messages below
        JPanel pnlMain = UIStyle.createScreenPanel();
        pnlMain.add(UIStyle.createHeader("Welcome", "Enter your account number and PIN"),
                BorderLayout.NORTH);
        pnlMain.add(buildForm(), BorderLayout.CENTER);
        pnlMain.add(buildFooter(), BorderLayout.SOUTH);
        add(pnlMain);

        wireEvents();

        // Pressing Enter anywhere on the screen presses Login
        getRootPane().setDefaultButton(btnLogin);

        setVisible(true);
        txtAccNo.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    /** The card holding the two input fields. */
    private JPanel buildForm() {
        txtAccNo = UIStyle.createTextField();
        pfPin = UIStyle.createPasswordField();
        defaultEcho = pfPin.getEchoChar();
        chkShowPin = UIStyle.createCheckBox("Show PIN");

        JPanel card = UIStyle.createCardPanel();
        card.add(UIStyle.createLabel("Account number"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(txtAccNo);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(UIStyle.createLabel("PIN"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(pfPin);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(chkShowPin);

        // The card sits at the top of the centre area instead of stretching
        JPanel holder = UIStyle.createColumn();
        holder.add(card);
        return holder;
    }

    /** Message line, Login button and the demo hint. */
    private JPanel buildFooter() {
        lblMessage = UIStyle.createMessageLabel();
        btnLogin = UIStyle.createButton("Log in");

        JPanel footer = UIStyle.createColumn();
        footer.add(lblMessage);
        footer.add(Box.createRigidArea(new Dimension(0, 6)));
        footer.add(btnLogin);
        footer.add(Box.createRigidArea(new Dimension(0, 14)));
        footer.add(UIStyle.createHintLabel("Demo account: 1001  |  PIN: 1234"));
        return footer;
    }

    private void wireEvents() {
        btnLogin.addActionListener(e -> handleLogin());

        // Toggle between dots and visible digits
        chkShowPin.addActionListener(e ->
                pfPin.setEchoChar(chkShowPin.isSelected() ? (char) 0 : defaultEcho));
    }

    // ------------------------------------------------------------------
    // Button logic
    // ------------------------------------------------------------------

    /** Runs when the customer presses Log in (or Enter). */
    private void handleLogin() {
        String accNo = txtAccNo.getText().trim();
        String pin = new String(pfPin.getPassword());

        // 1. Cheap checks first: nothing is sent to the ATM until the input looks sane
        if (accNo.isEmpty() || pin.isEmpty()) {
            UIStyle.showError(lblMessage, "Please enter your account number and PIN.");
        } else if (!InputValidator.isValidAccountNumber(accNo)) {
            UIStyle.showError(lblMessage, "Account numbers contain digits only.");
        } else if (!InputValidator.isValidPin(pin)) {
            UIStyle.showError(lblMessage,
                    "Your PIN must be exactly " + AppConstants.PIN_LENGTH + " digits.");
        } else if (atm.authenticate(accNo, pin)) {
            // 2. Success: replace this screen with the main menu
            dispose();
            new MainMenuFrame(atm);
        } else if (atm.isLocked()) {
            // 3. Too many wrong attempts: block the card and close the program
            JOptionPane.showMessageDialog(this,
                    "Your card has been blocked after " + AppConstants.MAX_PIN_ATTEMPTS
                            + " incorrect attempts.\nPlease contact your bank.",
                    "Card blocked", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            // 4. Wrong details: tell the customer how many tries are left
            int left = atm.getRemainingAttempts();
            UIStyle.showError(lblMessage, "Incorrect details. " + left
                    + (left == 1 ? " attempt" : " attempts") + " remaining.");
            pfPin.setText("");
            pfPin.requestFocusInWindow();
            shakeWindow();
        }
    }

    /** A quick left-right wobble, like a bank app saying "no". */
    private void shakeWindow() {
        final Point origin = getLocation();
        final int[] offsets = {-10, 10, -8, 8, -5, 5, -2, 2, 0};
        final int[] step = {0};

        Timer timer = new Timer(25, null);
        timer.addActionListener(e -> {
            if (step[0] < offsets.length) {
                setLocation(origin.x + offsets[step[0]], origin.y);
                step[0]++;
            } else {
                setLocation(origin);
                timer.stop();
            }
        });
        timer.start();
    }
}
