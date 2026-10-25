/*
 * File:        GuiWalkthrough.java
 * Author:      Kgonyane Makgoale and Ndzalama Mthombeni
 * Role:        Members 6 and 7, Results and analysis (testing)
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Robot-style test of the real screens. It types into the fields,
 *              clicks the buttons, checks the messages that appear and saves a
 *              screenshot of every important state into docs/screenshots/.
 *              The last step blocks the card with three wrong PINs, which makes
 *              the program close with System.exit(0), exactly as in real use.
 *
 * Needs a display (a normal desktop is fine; on a server use xvfb-run).
 * Run from the project folder:
 *     javac -d out src/atm/*.java test/atm/*.java
 *     java -cp out atm.GuiWalkthrough
 */
package atm;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GuiWalkthrough {

    private static final String SHOT_FOLDER = "docs/screenshots/";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        new File(SHOT_FOLDER).mkdirs();
        ATM atm = new ATM();

        // Make sure the summary is printed even though the last step ends in System.exit(0)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("RESULT: " + passed + " passed, " + failed + " failed, "
                    + (passed + failed) + " checks in total");
            Runtime.getRuntime().halt(failed == 0 ? 0 : 1);   // keeps the real pass/fail exit code
        }));

        testLogin(atm);
        testMainMenu(atm);
        testWithdraw(atm);
        testDeposit(atm);
        testBalance(atm);
        testInactivityTimeout(atm);
        testCardBlocked();      // always last: it closes the program
    }

    // ------------------------------------------------------------------
    // Scenarios
    // ------------------------------------------------------------------

    private static void testLogin(ATM atm) throws Exception {
        section("Login screen");
        onScreen(() -> new LoginFrame(atm));
        shot("01-login");

        click("Log in");
        check("empty fields ask for both details", message().contains("enter your account number"));
        setText(textField(), "10a1");
        setPin("1234");
        click("Log in");
        check("letters in the account number are refused", message().contains("digits only"));
        setText(textField(), "1001");
        setPin("12");
        click("Log in");
        check("a 2-digit PIN is refused", message().contains("exactly 4 digits"));
        setPin("0000");
        click("Log in");
        check("wrong PIN shows 2 attempts remaining", message().contains("2 attempts remaining"));
        shot("02-login-error");
        check("the wrong PIN field was cleared for safety", new String(pinField().getPassword()).isEmpty());
        setPin("0000");
        click("Log in");
        check("second wrong PIN shows 1 attempt remaining", message().contains("1 attempt remaining"));
        check("the ATM has not logged anyone in", !atm.isLoggedIn());

        setPin("1234");
        click("Log in");
        check("correct details open the main menu", titleContains("Main Menu"));
        check("the ATM session is open", atm.isLoggedIn());
    }

    private static void testMainMenu(ATM atm) throws Exception {
        section("Main menu");
        shot("03-main-menu");
        check("all four options are on screen", hasButton("Withdraw") && hasButton("Deposit")
                && hasButton("Check Balance") && hasButton("Exit"));

        // Exit asks first. Answer "No" and make sure the session is still alive.
        answerNextDialog("13-exit-confirm", "No");
        click("Exit");
        pause(600);
        check("answering No keeps the session open", atm.isLoggedIn() && titleContains("Main Menu"));
    }

    private static void testWithdraw(ATM atm) throws Exception {
        section("Withdraw screen");
        click("Withdraw");
        check("withdraw screen opens", titleContains("Withdraw"));
        shot("04-withdraw");

        withdraw("155");
        check("R155 is refused (notes only)", message().contains("multiple of R10"));
        shot("05-withdraw-error");
        withdraw("abc");
        check("letters are refused", message().contains("digits only"));
        withdraw("-20");
        check("a negative amount is refused", message().contains("negative"));
        withdraw("0");
        check("zero is refused", message().contains("greater than zero"));
        withdraw("15000");
        check("more than the balance gives insufficient funds and shows the balance",
                message().contains("Insufficient funds") && message().contains("R10 000.00"));
        shot("06-withdraw-insufficient");
        withdraw("6000");
        check("more than the daily limit is refused", message().contains("daily withdrawal limit"));
        check("refused withdrawals change nothing", atm.getBalance() == 10000.00);

        click("R500");
        check("a quick-amount button fills the field", textField().getText().equals("500"));
        click("Confirm withdrawal");
        check("R500 is withdrawn, new balance R9 500.00", message().contains("R9 500.00"));
        check("the amount field is cleared after success", textField().getText().isEmpty());
        shot("07-withdraw-success");

        click("Back to menu");
        check("Back returns to the main menu", titleContains("Main Menu"));
    }

    private static void testDeposit(ATM atm) throws Exception {
        section("Deposit screen");
        click("Deposit");
        check("deposit screen opens", titleContains("Deposit"));
        shot("08-deposit");

        setText(textField(), "R 1 250.75");
        click("Confirm deposit");
        check("'R 1 250.75' is accepted, new balance R10 750.75", message().contains("R10 750.75"));
        shot("09-deposit-success");
        setText(textField(), "5.555");
        click("Confirm deposit");
        check("three decimals are refused", message().contains("two decimal"));
        click("Back to menu");
        check("Back returns to the main menu", titleContains("Main Menu"));
    }

    private static void testBalance(ATM atm) throws Exception {
        section("Balance screen");
        // Add a little more history so the statement has several lines
        atm.withdraw(200);
        atm.deposit(50);

        click("Check Balance");
        check("balance screen opens", titleContains("Balance"));
        check("the balance on screen is correct", hasLabel("R10 600.75"));
        check("deposits and withdrawals are listed", hasLabel("Deposit") && hasLabel("Withdrawal"));
        shot("10-balance");
        click("Back to menu");
        check("Back returns to the main menu", titleContains("Main Menu"));
    }

    private static void testInactivityTimeout(ATM atm) throws Exception {
        section("Inactivity sign-out (2 second test timer)");
        answerNextDialog("11-timeout-dialog", "OK");
        onScreen(() -> new SessionMonitor(atm, 2).start());
        pause(4500);

        check("the idle customer was logged out", !atm.isLoggedIn());
        check("the login screen is back", titleContains("Login"));
        check("only one screen is open", visibleFrames() == 1);
    }

    private static void testCardBlocked() throws Exception {
        section("Card blocked after three wrong PINs (program exits)");
        answerNextDialog("12-card-blocked", "OK");
        setText(textField(), "1001");
        for (int attempt = 0; attempt < 3; attempt++) {
            setPin("9999");
            click("Log in");
        }
        pause(3000);

        // Reaching this line means System.exit(0) was NOT called
        check("the program closed after the card was blocked", false);
        System.exit(1);
    }

    // ------------------------------------------------------------------
    // Helpers: finding components and driving them
    // ------------------------------------------------------------------

    private static void withdraw(String text) throws Exception {
        setText(textField(), text);
        click("Confirm withdrawal");
    }

    private static JFrame currentFrame() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isShowing()) {
                return (JFrame) window;
            }
        }
        throw new IllegalStateException("No screen is showing.");
    }

    private static int visibleFrames() {
        int count = 0;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isShowing()) {
                count++;
            }
        }
        return count;
    }

    private static <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) {
                found.add(type.cast(child));
            }
            if (child instanceof Container) {
                found.addAll(findAll((Container) child, type));
            }
        }
        return found;
    }

    private static JButton findButton(Container root, String text) {
        for (JButton button : findAll(root, JButton.class)) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        return null;
    }

    private static boolean hasButton(String text) {
        return findButton(currentFrame().getRootPane(), text) != null;
    }

    /** The first ordinary text field (a password field is a text field too, so skip it). */
    private static JTextField textField() {
        for (JTextField field : findAll(currentFrame().getRootPane(), JTextField.class)) {
            if (!(field instanceof JPasswordField)) {
                return field;
            }
        }
        throw new IllegalStateException("No text field on this screen.");
    }

    private static JPasswordField pinField() {
        return findAll(currentFrame().getRootPane(), JPasswordField.class).get(0);
    }

    private static void setPin(String pin) throws Exception {
        JPasswordField field = pinField();
        SwingUtilities.invokeAndWait(() -> field.setText(pin));
    }

    private static void setText(JTextField field, String text) throws Exception {
        SwingUtilities.invokeAndWait(() -> field.setText(text));
    }

    private static void click(String buttonText) throws Exception {
        JButton button = findButton(currentFrame().getRootPane(), buttonText);
        if (button == null) {
            throw new IllegalStateException("No button '" + buttonText + "' on " + currentFrame().getTitle());
        }
        SwingUtilities.invokeAndWait(button::doClick);
        pause(150);
    }

    /** All the coloured message text currently shown (HTML labels), without the tags. */
    private static String message() {
        StringBuilder text = new StringBuilder();
        for (JLabel label : findAll(currentFrame().getRootPane(), JLabel.class)) {
            String content = label.getText();
            if (content != null && content.startsWith("<html>")) {
                text.append(content.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
            }
        }
        return text.toString();
    }

    private static boolean hasLabel(String contains) {
        for (JLabel label : findAll(currentFrame().getRootPane(), JLabel.class)) {
            if (label.getText() != null && label.getText().contains(contains)) {
                return true;
            }
        }
        return false;
    }

    private static boolean titleContains(String text) {
        return currentFrame().getTitle().contains(text);
    }

    private static void onScreen(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }

    /** Waits for the next pop-up dialog, saves a screenshot of it and presses one of its buttons. */
    private static void answerNextDialog(String shotName, String buttonText) {
        Thread watcher = new Thread(() -> {
            try {
                for (int i = 0; i < 60; i++) {
                    pause(200);
                    for (Window window : Window.getWindows()) {
                        if (window instanceof JDialog && window.isShowing()) {
                            savePng(window, shotName);
                            SwingUtilities.invokeLater(() -> {
                                JButton button = findButton((Container) window, buttonText);
                                if (button != null) {
                                    button.doClick();
                                } else {
                                    window.dispose();
                                }
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("  dialog watcher stopped: " + e);
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    // ------------------------------------------------------------------
    // Helpers: screenshots, pauses and reporting
    // ------------------------------------------------------------------

    private static void shot(String name) throws Exception {
        pause(150);
        savePng(currentFrame(), name);
    }

    private static void savePng(Window window, String name) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                BufferedImage image = new BufferedImage(window.getWidth(), window.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                window.paint(g2);
                g2.dispose();
                ImageIO.write(image, "png", new File(SHOT_FOLDER + name + ".png"));
            } catch (Exception e) {
                throw new IllegalStateException("Could not save screenshot " + name, e);
            }
        });
    }

    private static void pause(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }
}
