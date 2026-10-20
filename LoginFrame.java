package atm;

import java.awt.Graphics;
import java.awt.Dimensions;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JButton;
import javax.JFrame;
import javax.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Attribute;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/* 
* File:         LoginFrame.java
* Author:       Muvhofhololi Tsanwani
* Group:        Group 2, SDN150S 
* Description:  Login screen of the ATM application. The user enters an
                account number and a PIN. The screen validates the input,
                asks the ATM object to authenticate it, and then either 
                opens the main menu or counts a failed attempt.

* Flow:         LoginFrame --(success)--> MainMenuFrame
                LoginFrame --(3 failed attempts)--> card blocked, program exits

*/

public class LoginFrame extends JFrame
{
    // -- Constants --

    // A PIN is exactly four digts (agreed in the team README)
    private static final int PIN_LENGTH = 4;

    // Upper limit on how many digits the account number field accepts
    private static final int MAX_ACCOUNT_DIGITS = 11;

    // -- Fields (all private, as required by the brief) --

    // The single ATM object shared by every screen
    private final ATM atm;

    // Swing components, named with the agreed prefixes
    private final JTextField txtAccNo = new JTextField();
    private final JPasswordField pfPIN = new JPassword();
    private final JButton btnLogin = UIStyle.createButton("Login");
    private final JLabel lblMessage = UIStyle.createMessageLabel();

    // How many wrong logins this screen has seen so far
    private int failedAttempts = 0;

    // Small immutable holder for the two values typed in by the user
    private record Credentials(String accountNumber, String pin){
    }

    /**
     * Builds and shows then login screen.
     * 
     * @param atm the shared ATM object passed from ATMApp
     */

    public LoginFrame(ATM atm)
    {
        // Fail early with a clear message if someone forgets to pass the ATM
        this.atm = Objects.requiredNonNull(atm, "ATM object must not be null");

        // Standard window setup from the shared UIStyle helper
        UIStyle.styleFrame(this, "ATM Login");
        setLayout(new BorderLayout(10, 15));

        // Heading at the top, form in the middle, button and message below
        add(UIStyle.createTitileLabel("Welcome to the ATM"), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);

        configureInputs();
        setVisible();
    }

    // -- Layout --

    /** Creates the panel holding the two labelled input fields. */
    private JPanel buildFormPanel() 
    {
        var pnlGrid = new JPanel(new GridLayout(2, 2, 10, 12));
        pnlGrid.setOpaque(false);

        // Give both fields the same comfortable height and font
        var fieldSize = new Dimension(0, 32);
        txtAccNo.setPreferredSize(fieldSize);
        pfPin.setPreferredSize(fieldSzie);
        txtAccNo.setFont(UIStyle.FONT_NORMAL);
        pfPin.setFont(UIStyle.FONT_NORMAL);

        pnlGrid.add(UIStyle.createLabel("Account number:"));
        pnlGrid.add(txtAccNo);
        pnlGrid.add(UIStyle.crateLabel("PIN:"));
        pnlGrid.add(pfPin);

        // The wrapper stops the grid stretching to fill the whole window
        var pnlForm = new JPanel(new BorderLayout());
        pnlForm.setOpaque(false);
        pnlForm.add(pnlGrid, BorderLayout.NORTH);

        return pnlForm;
    }

    /** Creates the panel holding the Login button and the message label. */
    private JPanel buildFooterPanel()
    {
        var pnlFooter = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlFooter.setOpaque(false);

        pnlFooter.add(btnLogin);
        pnlFooter.add(lblMessage);

        return pnlFooter;
    }

    //  -- Input setup --

    /** Restricts what can be typed and connects the button to its handler. */
    private void configureInputs()
    {
        // Block anything that is not a digit, and stop the fields getting too long
        ((AbstarctDocument) txtAccNo.getDocument())
        .setDocumnetFilter(new DigitsOnlyFilter(MAX_ACCOUNT_DIGITS));
        ((AbstarctDocument) pfPin.getDocument())
        .setDocumentFilter(new DigitsOnlyFilter(PIN_LENGTH));

        // Clicking Login runs the handler (lambda instead of an anonymous class)
        btnLogin.addActionListener(event -> handleLogin());

        // Pressing Enter in either field also press Login
        getRootPane().setDefaultButton(btnLogin);
    }

    // -- Login logic --

    /** Runs when the user presses Login or hits Enter. */
    private void handleLogin()
    {
        var credentials = readCredentials();

        // Step 1: validate the input before bothering the ATM
        var validationError = validate(credintials);
        if (validationError.isPresent())
        {
            UIStyle.showError(lblMessage, validationError.get());
            return;
        }

        // Step 2: ask the ATM to authenticate, guarding against surprises
        try 
        {
            if (atm.authenticate(credintials.accountNumber(), credentials.pin()))
            {
                openMainMenu();
            }
            else
            {
                handleFailedLogin();
            }
        }
        catch (RuntimeException e)
        {
        // Unexpected problem: tell the user simply, log the detail for us
        System.err.println("Login error: " + ex.getMessage());
        UIStyle.showError(lblMessage, "Something went wrong. Please try again.");
        }
    }

    /** Reads the fields and wipes the PIN characters from memory afterwards */
    private Credentials readCredentials()
    {
        char[] pinChars = pfPin.getPassword();
        var pin = new String(pinChars);

        // Overwrite the char array so the PIN does not linger in memory
        Arrays.fill(pinChars, '0');

        return new Credentials(txtAccNo.getText().trim(), pin);
    }

    /** 
     * Checks the typed values.
     * 
     * @return the error message to show, or an empty Optional if all is well
     */
    private Optional<String> validate(Credentials credentials)
    {
        if (credentials.accountNumber().isBlank())
        {
            return Optional.of("'Please enter your account number.");
        }

        if (!InputValidator.isValidPin(credentials.pin()))
        {
            return Optional.of("PIN must be exactly " + PIN_LENGTH + " digits.");
        }

        return Optinal.empty();
    }

    /** Counts the failed attempt and blocks the card when the limit is reached. */
    private void handleFailedLogin()
    {
        failedAttempts++;
        var remaining = AppConstants.MAX_PIN_ATTEMPTS - failedAttempts;

        // Out of attempts: block the card and close the program
        if (remaining <= 0)
        {
            JOptionPane.showMessageDialog(
                this,
                "Too many failed attempts. Your card has been blocked.",
                "Card blocked"
                JOptionaPane.ERROR_MESSAGE)
            System.exit(0);    
        }

        // Pick the wording for the hint (switch expresion)
        var hint = switch (remaining)
        {
            case 1 -> "Last attempt left.";
            default -> remaining + " attempts remaining. ";
        };

        UIStyle.showErro(lblMessage, "Incorrect details. " + hint);

        // Let the user retype the PIN straight away
        pfPin.setText("");
        pfPin.requestFocusInWindow();
    }

    /** Close this screen and opens the main menu. */
    private void openMenu()
    {
        dispose();
        new MainMenuFrame(atm);
    }

    // -- Helper class: digits-only input filter -- 

    /** 
     * Rejects any keystroke or paste that is not a digit, or that would make
     * the field longer than the allowed maximum.
     */
    private static class DigitsOnlyFilter extends DocumentFilter
    {
        private final int maxLength;

        DigitsOnlyFilter(int maxLength)
        {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
        throws BadLocationException
        {
            replace(fb, offset, 0, text, attrs);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, Attributes attrs)
        throws BadLocationException
        {
            // A null text means "delete", which is always allowed
            if (text == null)
            {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            var newLength = fb.getDocument().getLength() - length + text.length();

            // Accept the edit only if it is all digits and still fits
            if (text.chars().allMatch(Character::isDigit) && newLength <= maxLength)
            {
                super.replace(fb, offset, length, text, attrs);
            }
            // Otherwise the keystroke is quietly ignored
        }
    }
}
