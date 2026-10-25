/*
 * File:        ATM.java
 * Author:      Mabandla Nkosi
 * Role:        Member 2, Controller
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: The brain of the machine. It checks logins (and blocks the
 *              card after too many wrong attempts), enforces the cash rules
 *              (whole notes, daily limit) and passes the transactions on to
 *              the Account. Screens only call this class, they contain no
 *              banking rules of their own.
 */
package atm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ATM {

    // ------------------------------------------------------------------
    // State: private, so no screen can change it behind our back
    // ------------------------------------------------------------------
    private final List<Account> accounts;
    private Account currentAccount;      // null while nobody is logged in
    private int failedAttempts;          // wrong logins since the last success

    /** Creates the machine and its demo account (1001, PIN 1234, R10 000.00). */
    public ATM() {
        accounts = new ArrayList<>();
        accounts.add(new Account(AppConstants.DEMO_ACCOUNT_NUMBER,
                AppConstants.DEMO_PIN, AppConstants.INITIAL_BALANCE));

        currentAccount = null;
        failedAttempts = 0;
    }

    // ------------------------------------------------------------------
    // Login and logout
    // ------------------------------------------------------------------

    /**
     * Checks an account number and PIN.
     * A wrong account and a wrong PIN look identical to the caller, so
     * an attacker cannot learn which account numbers exist.
     *
     * @return true if the login succeeded; false if it failed or the card is blocked
     */
    public boolean authenticate(String accNo, String pin) {
        if (isLocked()) {
            return false;          // blocked cards get no more tries
        }

        Account found = findAccount(accNo);

        if (found != null && found.validatePin(pin)) {
            currentAccount = found;
            failedAttempts = 0;
            return true;
        }

        failedAttempts++;
        return false;
    }

    /** Ends the session. The balance and history stay on the account. */
    public void logout() {
        currentAccount = null;
    }

    public boolean isLoggedIn() {
        return currentAccount != null;
    }

    /** True once MAX_PIN_ATTEMPTS wrong logins have happened in a row. */
    public boolean isLocked() {
        return failedAttempts >= AppConstants.MAX_PIN_ATTEMPTS;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(0, AppConstants.MAX_PIN_ATTEMPTS - failedAttempts);
    }

    // ------------------------------------------------------------------
    // Transactions
    // ------------------------------------------------------------------

    /**
     * Withdraws cash after applying the machine rules in this order:
     * whole notes, enough money in the account, then the daily limit.
     * (Funds are checked before the limit so that asking for more than the
     * balance always gives the clearest message: "insufficient funds".)
     *
     * @throws InvalidAmountException     not a multiple of the note size, or over the daily limit
     * @throws InsufficientFundsException more than the balance
     */
    public void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException {
        Account account = requireLogin();

        if (!isWholeNotes(amount)) {
            throw new InvalidAmountException("The ATM dispenses notes only. "
                    + "Enter a multiple of R" + (int) AppConstants.NOTE_DENOMINATION + ".");
        }
        if (amount > account.getBalance()) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }
        if (amount > getRemainingDailyLimit()) {
            throw new InvalidAmountException("This would pass your daily withdrawal limit.");
        }

        account.withdraw(amount);
    }

    /** Deposits money into the logged-in account. */
    public void deposit(double amount) {
        requireLogin().deposit(amount);
    }

    public double getBalance() {
        return requireLogin().getBalance();
    }

    public String getCurrentAccountNumber() {
        return requireLogin().getAccountNumber();
    }

    /** How much more the customer may withdraw today. */
    public double getRemainingDailyLimit() {
        double withdrawnToday = requireLogin().getWithdrawnOn(LocalDate.now());
        double remaining = AppConstants.DAILY_WITHDRAWAL_LIMIT - withdrawnToday;
        return Math.max(0, Math.round(remaining * 100.0) / 100.0);
    }

    /**
     * The latest transactions, newest first.
     *
     * @param count the most entries wanted
     */
    public List<Transaction> getRecentTransactions(int count) {
        List<Transaction> all = requireLogin().getHistory();
        List<Transaction> recent = new ArrayList<>();

        // Walk backwards from the newest entry until we have enough
        for (int i = all.size() - 1; i >= 0 && recent.size() < count; i--) {
            recent.add(all.get(i));
        }
        return recent;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /** Looks an account up by number, or returns null if there is none. */
    private Account findAccount(String accNo) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accNo)) {
                return account;
            }
        }
        return null;
    }

    /** Returns the logged-in account or fails loudly if nobody is logged in. */
    private Account requireLogin() {
        if (currentAccount == null) {
            throw new IllegalStateException("No customer is logged in.");
        }
        return currentAccount;
    }

    /** Works in whole cents, because double arithmetic on rand values is inexact. */
    private boolean isWholeNotes(double amount) {
        long cents = Math.round(amount * 100.0);
        long noteCents = Math.round(AppConstants.NOTE_DENOMINATION * 100.0);
        return cents % noteCents == 0;
    }
}
