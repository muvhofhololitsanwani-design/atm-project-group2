/*
 *  Author      : Siyolise Mhlauli
 *  Student No. : 261295438
 */
package atm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // Force cross-platform look and feel so UI colors and borders across operating systems
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set look and feel: " + e.getMessage());
        }

        // Launch the application GUI on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Bank bank = new Bank();              // Create bank instance and load demo accounts
                LoginFrame login = new LoginFrame(bank); // Instantiate and display the initial login screen
                login.setVisible(true);
            }
        });
    }
}