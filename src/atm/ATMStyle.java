/*  Student Number: 231255640
 *  Author: Aubrey Magolego 
 */
package atm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class ATMStyle {

    public static final String BANK_NAME = "CPUT Smart ATM";

    // Application color scheme constants
    public static final Color NAVY  = new Color(15, 44, 89);
    public static final Color BLUE  = new Color(0, 102, 204);
    public static final Color GREEN = new Color(30, 130, 76);
    public static final Color RED   = new Color(185, 40, 50);
    public static final Color GREY  = new Color(96, 108, 122);
    public static final Color LIGHT = new Color(236, 242, 250);
    public static final Color WHITE = Color.WHITE;

    // Standardized fonts across all ATM frames
    public static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD, 24);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font TEXT_FONT    = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font SMALL_FONT   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font MONEY_FONT   = new Font("SansSerif", Font.BOLD, 34);

    // Private constructor to prevent instantiation of this utility class
    private ATMStyle() {
    }

    // Formats numerical amounts into South African currency format (e.g., R10 000.00)
    public static String formatMoney(double amount) {
        String text = String.format(Locale.US, "%,.2f", amount);
        return "R" + text.replace(",", " ");
    }

    // Masks sensitive account numbers for display, leaving only the last 4 digits visible
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber.length() <= 4) {
            return accountNumber;
        }
        String stars = "";
        for (int i = 0; i < accountNumber.length() - 4; i++) {
            stars = stars + "*";
        }
        return stars + accountNumber.substring(accountNumber.length() - 4);
    }

    // Creates the standard navy header panel used at the top of GUI screens
    public static JPanel createHeader(String screenTitle) {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));

        JLabel bankLabel = new JLabel(BANK_NAME, SwingConstants.CENTER);
        bankLabel.setFont(SMALL_FONT);
        bankLabel.setForeground(new Color(160, 195, 235));

        JLabel titleLabel = new JLabel(screenTitle, SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE);

        header.add(bankLabel);
        header.add(titleLabel);
        return header;
    }

    // Helper to build styled buttons with custom hover color effects
    public static JButton createButton(String text, final Color background) {
        final JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 46));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
        return button;
    }

    public static JLabel createLabel(String text, Font font, Color colour) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(colour);
        return label;
    }

    // Centers any JFrame window on the user's desktop screen
    public static void centreOnScreen(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - frame.getWidth()) / 2;
        int y = (screen.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    // Validates that user input fields contain strictly numeric characters
    public static boolean isDigitsOnly(String text) {
        if (text.length() == 0) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    // Validates deposit and withdrawal text inputs to ensure correct decimal formatting
    public static boolean isAmountText(String text) {
        if (text.length() == 0 || text.equals(".")) {
            return false;
        }
        int dotCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.') {
                dotCount++;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        return dotCount <= 1;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, BANK_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}