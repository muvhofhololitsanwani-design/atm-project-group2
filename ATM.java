/*
 * File:        ATM.java
 * Author:      Mabandla Nkosi
 * Role:        Member 2, Controller
 * Group:       Group 2, SDN150S  (Software Design 1), CPUT
 * Description: The brain of the machine. It checks logins (and blocks the
 *              card after too  many wrong attempts), enforces the cash rules
 *              (whole  notes, daily limit) and passes the transactions on to
 *              the Account. Screens only call  this class, they contain no
 *              banking rules of their own .
 */
package atm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ATM {

    private final List<Account> accounts ;
    private Account currentAccount;      
    private int failedAttempts;

    public ATM() {
        accounts = new ArrayList<>();
        accounts.add(new Account(AppConstants.DEMO_ACCOUNT_NUMBER,
                AppConstants.DEMO_PIN, AppConstants.INITIAL_BALANCE));

        currentAccount = null;
        failedAttempts = 0;}

   
    public boolean authenticate(String accNo, String pin) {
        if (isLocked()) {
            return false;        
            
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

    public void logout() {
        currentAccount = null;
    }

    public boolean isLoggedIn() {
        return currentAccount != null;
    }
    
    public boolean isLocked() {
        return failedAttempts >= AppConstants.MAX_PIN_ATTEMPTS;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(0, AppConstants.MAX_PIN_ATTEMPTS - failedAttempts);
    }


    public void withdraw(double  amount) throws InsufficientFundsException, InvalidAmountException {
        Account account = requireLogin();

        if (!isWholeNotes(amount)) {
            throw new InvalidAmountException("The ATM dispenses notes only. "+ "Enter a multiple of R" + (int) AppConstants.NOTE_DENOMINATION + ".");
        }
        if (amount > account.getBalance()) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }
        if (amount > getRemainingDailyLimit()) {
            throw new InvalidAmountException(" This would pass your daily withdrawal limit");
        }

        account.withdraw( amount);
    }


    public void deposit(double amount) {
        requireLogin().deposit(amount);

    }

    public double getBalance() {

        return requireLogin().getBalance();
    }

    public String getCurrentAccountNumber() {
        return requireLogin().getAccountNumber();

    }

    public double getRemainingDailyLimit( ) {
        double withdrawnToday = requireLogin().getWithdrawnOn(LocalDate.now() );
        double remaining = AppConstants.DAILY_WITHDRAWAL_LIMIT - withdrawnToday;
        return Math.max(0, Math.round(remaining * 100.0) / 100.0);
    }

    //latest and the newest trans
    public List<Transaction> getRecentTransactions(int count) {
        List<Transaction> all = requireLogin( ).getHistory();
        List<Transaction> recent = new ArrayList<>();

        for (int i = all.size() - 1; i >= 0 && recent.size() < count; i--) {
            recent.add(all.get(i));
        }
        return recent;
    }


    private Account findAccount( String accNo) {
        for (Account account : accounts) {

        if (account.getAccountNumber().equals(accNo)) {
                return account;
        }
        }
        return null;
    }
    private Account requireLogin() {
        if (currentAccount ==  null) {
            throw new IllegalStateException("No customer is logged in, Please login.");
        }
        return currentAccount;
    }
    private  boolean isWholeNotes(double amount) {
        long cents = Math.round(amount * 100.0);
        long noteCents = Math.round(AppConstants.NOTE_DENOMINATION * 100.0);
        return cents % noteCents  == 0;
    }
}
