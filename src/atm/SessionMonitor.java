/*
 * File:        SessionMonitor.java
 * Author:      Siyolise Mhlauli
 * Role:        Member 8, Integration lead
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Safety net for a walk-away customer. If nobody touches the
 *              mouse or keyboard for the configured time, the session is
 *              ended, every open screen is closed and the login screen
 *              returns. Real ATMs do the same.
 */
package atm;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class SessionMonitor {

    private final ATM atm;
    private final int timeoutSeconds;
    private final Timer idleTimer;       // fires once after a quiet period

    /**
     * @param atm            the shared ATM whose session may be ended
     * @param timeoutSeconds seconds of silence before the customer is signed out
     */
    public SessionMonitor(ATM atm, int timeoutSeconds) {
        this.atm = atm;
        this.timeoutSeconds = timeoutSeconds;

        idleTimer = new Timer(timeoutSeconds * 1000, e -> handleTimeout());
        idleTimer.setRepeats(false);
    }

    /** Starts watching. Any mouse or keyboard activity restarts the countdown. */
    public void start() {
        long activity = AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> idleTimer.restart(), activity);
        idleTimer.start();
    }

    /** Runs when the quiet period is over. */
    private void handleTimeout() {
        // Nobody is logged in (login screen), so there is nothing to end
        if (!atm.isLoggedIn()) {
            idleTimer.restart();
            return;
        }

        atm.logout();

        // Open the new login screen first, then close the old windows
        Window[] oldWindows = Window.getWindows();
        new LoginFrame(atm);
        for (Window window : oldWindows) {
            window.dispose();
        }

        JOptionPane.showMessageDialog(null,
                "You were signed out after " + timeoutSeconds
                        + " seconds of inactivity to keep your account safe.",
                "Session timed out", JOptionPane.INFORMATION_MESSAGE);
    }
}
