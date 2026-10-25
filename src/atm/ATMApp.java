/*
 * File:        ATMApp.java
 * Author:      Siyolise Mhlauli
 * Role:        Member 8, Integration lead
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Entry point of the program. Creates the single shared ATM
 *              object, shows the login screen and starts the inactivity
 *              monitor. All other screens are opened from there.
 *
 * Run:         javac -d out src/atm/*.java   then   java -cp out atm.ATMApp
 */
package atm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ATMApp {

    /** Private constructor: this class only exists to hold main(). */
    private ATMApp() {
    }

    public static void main(String[] args) {

        // Smoother text. These must be set before any window is created.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // The cross-platform look draws our colours the same on Windows, Mac and Linux
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Not fatal: the default look still works, only the styling is simpler
            System.err.println("Could not set the look and feel: " + e.getMessage());
        }

        // All Swing work belongs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ATM atm = new ATM();                 // one ATM, shared by every screen
            new LoginFrame(atm);
            new SessionMonitor(atm, AppConstants.SESSION_TIMEOUT_SECONDS).start();
        });
    }
}
