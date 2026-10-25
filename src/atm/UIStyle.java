/*
 * File:        UIStyle.java
 * Author:      Muvhofhololi Tsanwani
 * Role:        Member 4, Team leader and Login screen
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: The shared look of the whole application. Every screen asks
 *              this class for its window setup, colours, fonts, buttons,
 *              labels and text fields, so all five screens feel like one
 *              app. Nothing here knows about banking: it only draws.
 */
package atm;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class UIStyle {

    // ------------------------------------------------------------------
    // Window size shared by every screen
    // ------------------------------------------------------------------
    public static final int WINDOW_WIDTH = 420;
    public static final int WINDOW_HEIGHT = 540;

    // ------------------------------------------------------------------
    // Colour palette: deep navy background with a mint accent
    // ------------------------------------------------------------------
    public static final Color BG_TOP = new Color(0x0B1B2B);
    public static final Color BG_BOTTOM = new Color(0x143556);
    public static final Color ACCENT = new Color(0x1FD1A0);
    public static final Color ACCENT_HOVER = new Color(0x4BE3B9);
    public static final Color ACCENT_PRESSED = new Color(0x15A57F);
    public static final Color TEXT = new Color(0xF1F5F9);
    public static final Color TEXT_MUTED = new Color(0x9FB3C8);
    public static final Color ERROR = new Color(0xFF7B7B);
    public static final Color SUCCESS = new Color(0x5BE49B);
    public static final Color DANGER = new Color(0xE5484D);

    // Translucent fills let the gradient show through cards and buttons
    private static final Color CARD_FILL = new Color(255, 255, 255, 20);
    private static final Color CARD_BORDER = new Color(255, 255, 255, 38);
    private static final Color INPUT_FILL = new Color(0x0D2239);
    private static final Color INPUT_BORDER = new Color(0x2E4A68);
    private static final int CORNER = 16;

    // ------------------------------------------------------------------
    // Fonts: the nicest installed family, otherwise Java's generic sans-serif
    // ------------------------------------------------------------------
    private static final String FONT_FAMILY = pickFontFamily();
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 26);
    private static final Font FONT_BRAND = new Font(FONT_FAMILY, Font.BOLD, 12);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_HINT = new Font(FONT_FAMILY, Font.PLAIN, 12);
    private static final Font FONT_INPUT = new Font(FONT_FAMILY, Font.PLAIN, 17);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 15);
    private static final Font FONT_CHIP = new Font(FONT_FAMILY, Font.BOLD, 13);
    private static final Font FONT_MESSAGE = new Font(FONT_FAMILY, Font.BOLD, 13);

    // Taskbar icon drawn in code, so the project needs no image files
    private static final BufferedImage APP_ICON = createAppIcon();

    // Runs once: make pop-up dialogs match the rest of the application
    static {
        UIManager.put("OptionPane.background", BG_BOTTOM);
        UIManager.put("Panel.background", BG_BOTTOM);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_CHIP);
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", BG_TOP);
        UIManager.put("Button.select", ACCENT_PRESSED);
    }

    /** Private constructor: every method is static, nobody creates a UIStyle. */
    private UIStyle() {
    }

    // ==================================================================
    // Windows
    // ==================================================================

    /**
     * Gives a screen the standard size, position, background and behaviour.
     * Call this first in every screen's constructor.
     */
    public static void styleFrame(JFrame frame, String title) {
        frame.setTitle("Group 2 ATM  |  " + title);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(APP_ICON);
        frame.setContentPane(new GradientPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ==================================================================
    // Panels
    // ==================================================================

    /** A see-through panel, so the gradient background shows behind it. */
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        return panel;
    }

    /** The outer panel of a screen: border layout with comfortable padding. */
    public static JPanel createScreenPanel() {
        JPanel panel = createPanel();
        panel.setLayout(new BorderLayout(0, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));
        return panel;
    }

    /** A rounded, softly lit container for grouped content such as a form. */
    public static JPanel createCardPanel() {
        return new CardPanel();
    }

    /** Stacks children vertically; children are left-aligned by default. */
    public static JPanel createColumn() {
        JPanel column = createPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        return column;
    }

    /** Screen heading: small brand line, large title and a subtitle. */
    public static JPanel createHeader(String title, String subtitle) {
        JLabel brand = createLabel("GROUP 2  ATM");
        brand.setFont(FONT_BRAND);
        brand.setForeground(ACCENT);

        JPanel header = createColumn();
        header.add(brand);
        header.add(createTitleLabel(title));
        header.add(createHintLabel(subtitle));
        return header;
    }

    // ==================================================================
    // Labels
    // ==================================================================

    /** Large heading text. */
    public static JLabel createTitleLabel(String text) {
        return buildLabel(text, FONT_TITLE, TEXT);
    }

    /** Normal body or field-caption text. */
    public static JLabel createLabel(String text) {
        return buildLabel(text, FONT_BODY, TEXT);
    }

    /** Small, quiet text for hints and subtitles. */
    public static JLabel createHintLabel(String text) {
        return buildLabel(text, FONT_HINT, TEXT_MUTED);
    }

    /** An empty, centred label reserved for success and error messages. */
    public static JLabel createMessageLabel() {
        JLabel label = new JLabel(" ", SwingConstants.CENTER);
        label.setFont(FONT_MESSAGE);
        label.setForeground(TEXT);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        label.setPreferredSize(new Dimension(320, 44));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return label;
    }

    /** Shows a red message. The text may contain simple HTML such as line breaks. */
    public static void showError(JLabel label, String message) {
        setMessage(label, message, ERROR);
    }

    /** Shows a green message. The text may contain simple HTML such as line breaks. */
    public static void showSuccess(JLabel label, String message) {
        setMessage(label, message, SUCCESS);
    }

    /** Empties a message label while keeping its height reserved. */
    public static void clearMessage(JLabel label) {
        label.setText(" ");
    }

    // ==================================================================
    // Buttons
    // ==================================================================

    /** The main call-to-action button (mint). */
    public static JButton createButton(String text) {
        return new RoundedButton(text, FONT_BUTTON, ACCENT, ACCENT_HOVER, ACCENT_PRESSED, BG_TOP);
    }

    /** A quieter button for secondary actions such as Back. */
    public static JButton createSecondaryButton(String text) {
        return new RoundedButton(text, FONT_BUTTON, new Color(255, 255, 255, 30),
                new Color(255, 255, 255, 52), new Color(255, 255, 255, 18), TEXT);
    }

    /** A red button for leaving or destructive actions such as Exit. */
    public static JButton createDangerButton(String text) {
        return new RoundedButton(text, FONT_BUTTON, DANGER, new Color(0xF0666A),
                new Color(0xC23B40), Color.WHITE);
    }

    /**
     * A row of quick-select amount buttons. Clicking one types that amount
     * into the target field, so the customer need not use the keyboard.
     */
    public static JPanel createQuickAmountPanel(JTextField target, double... amounts) {
        JPanel row = createPanel();
        row.setLayout(new GridLayout(1, amounts.length, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        for (double amount : amounts) {
            String plain = (amount == Math.floor(amount))
                    ? String.valueOf((long) amount) : String.valueOf(amount);

            JButton chip = new RoundedButton("R" + plain, FONT_CHIP, new Color(255, 255, 255, 30),
                    new Color(255, 255, 255, 56), new Color(255, 255, 255, 18), TEXT);
            chip.setPreferredSize(new Dimension(60, 38));
            chip.addActionListener(e -> {
                target.setText(plain);
                target.requestFocusInWindow();
            });
            row.add(chip);
        }
        return row;
    }

    // ==================================================================
    // Input fields
    // ==================================================================

    /** A styled single-line text field. */
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    /** A styled password field that hides what is typed. */
    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        styleInput(field);
        return field;
    }

    /** A check box that matches the dark theme. */
    public static JCheckBox createCheckBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setOpaque(false);
        box.setFont(FONT_HINT);
        box.setForeground(TEXT_MUTED);
        box.setFocusPainted(false);
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
        return box;
    }

    // ==================================================================
    // Money
    // ==================================================================

    /** Formats rand the South African way: 10000 becomes "R10 000.00". */
    public static String formatMoney(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');

        String digits = new DecimalFormat("#,##0.00", symbols).format(Math.abs(amount));
        return (amount < 0 ? "-R" : "R") + digits;
    }

    // ==================================================================
    // Private helpers
    // ==================================================================

    private static JLabel buildLabel(String text, Font font, Color colour) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(colour);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return label;
    }

    private static void setMessage(JLabel label, String message, Color colour) {
        label.setForeground(colour);
        // <center> lets Swing centre and wrap the text against the label's real width
        label.setText("<html><center>" + message + "</center></html>");
    }

    /** Applies the shared look to a text or password field. */
    private static void styleInput(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setBackground(INPUT_FILL);
        field.setSelectionColor(ACCENT_PRESSED);
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(inputBorder(false));
        field.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setPreferredSize(new Dimension(320, 46));

        // The border lights up while the field has the keyboard focus
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(inputBorder(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(inputBorder(false));
            }
        });
    }

    private static Border inputBorder(boolean focused) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(focused ? ACCENT : INPUT_BORDER, 2, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    /** Picks the first installed family from a short wish list. */
    private static String pickFontFamily() {
        String[] wishList = {"Segoe UI", "SF Pro Text", "Helvetica Neue", "Noto Sans", "DejaVu Sans"};
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

        for (String family : wishList) {
            if (installed.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Draws a mint rounded square with a rand "R", used as the window icon. */
    private static BufferedImage createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(ACCENT);
        g2.fillRoundRect(4, 4, 56, 56, 18, 18);

        g2.setColor(BG_TOP);
        g2.setFont(new Font(FONT_FAMILY, Font.BOLD, 38));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (64 - metrics.stringWidth("R")) / 2;
        int y = (64 - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString("R", x, y);

        g2.dispose();
        return image;
    }

    // ==================================================================
    // Custom components (nested classes, each with its own constructor)
    // ==================================================================

    /** Window background: a soft top-to-bottom gradient. */
    private static final class GradientPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        GradientPanel() {
            super(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** A rounded translucent card. */
    private static final class CardPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        CardPanel() {
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        /** Lets the card grow to the full width of its column (height stays natural). */
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_FILL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CORNER, CORNER);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CORNER, CORNER);
            g2.dispose();
        }
    }

    /** A flat rounded button with hover, pressed and keyboard-focus states. */
    private static final class RoundedButton extends JButton {

        private static final long serialVersionUID = 1L;

        private final Color normal;
        private final Color hover;
        private final Color pressed;

        RoundedButton(String text, Font font, Color normal, Color hover, Color pressed, Color textColour) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.pressed = pressed;

            setFont(font);
            setForeground(textColour);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 6, 0, 6));      // small margin: long labels must not be cut
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(120, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Pick the fill for the current state
            Color fill = normal;
            if (getModel().isPressed()) {
                fill = pressed;
            } else if (getModel().isRollover()) {
                fill = hover;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER, CORNER);

            // A thin outline shows which button has the keyboard focus
            if (hasFocus()) {
                g2.setColor(TEXT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, CORNER, CORNER);
            }
            g2.dispose();

            super.paintComponent(g);   // draws the button text
        }
    }
}
