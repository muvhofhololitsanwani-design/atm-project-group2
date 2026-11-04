/* Student Number;260763314
 * Written by; Kgonyane Makgoale
 */
package atm;

import java.util.ArrayList;

public class Bank {

    // Authentication result codes
    public static final int AUTH_SUCCESS    = 0;
    public static final int AUTH_NO_ACCOUNT = 1;
    public static final int AUTH_LOCKED     = 2;
    public static final int AUTH_WRONG_PIN  = 3;

    private final ArrayList<Account> accounts;

    public Bank() {
        accounts = new ArrayList<>();
        loadDemoAccounts();
    }

    // Loads predefined demo accounts for testing and demonstration purposes
    private void loadDemoAccounts() {
        addAccount(new Account("1234567890", "Siyolise Mhlauli", "1234"));
        addAccount(new Account("0987654321", "Athenkosi Khau", "4321"));
        addAccount(new Account("1122334455", "Kgonyane Makgoale", "2468"));
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    // Searches for an account by its number, returning null if it doesn't exist
    public Account findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    // Validates login credentials and enforces the 3-attempt card lockout rule
    public int authenticate(String accountNumber, String pin) {
        Account account = findAccount(accountNumber);

        if (account == null) {
            return AUTH_NO_ACCOUNT;
        }
        if (account.isLocked()) {
            return AUTH_LOCKED;
        }
        if (account.checkPin(pin)) {
            account.resetFailedAttempts(); // Clear failed count upon successful login
            return AUTH_SUCCESS;
        }

        // Increment failed attempt counter; blocks the card on the 3rd failure
        account.recordFailedAttempt();
        if (account.isLocked()) {
            return AUTH_LOCKED;
        }
        return AUTH_WRONG_PIN;
    }

    public int getAccountCount() {
        return accounts.size();
    }
}