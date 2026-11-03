/*
 *  Author      : Muvhofhololi Tsanwani
 *  Student No. : 261021222
 */
package atm;

import java.util.ArrayList;

public class Account 
{
    public static final double INITIAL_BALANCE = 10000.00;  
    public static final double MAX_WITHDRAWAL  = 5000.00;   
    public static final double MAX_DEPOSIT     = 20000.00;   
    public static final int    NOTE_SIZE       = 10;        
    public static final int    MAX_PIN_ATTEMPTS = 3;        

    private final String accountNumber;                     
    private final String holderName;                        
    private final String pin;                               
    private long balanceInCents; // Stored in cents to prevent floating-point rounding errors
    private final ArrayList<Transaction> transactions;  
    private int failedAttempts;                             
    private boolean locked;                                 

    // Standard account creation with the default R10 000.00 opening balance
    public Account(String accountNumber, String holderName, String pin) {
        this(accountNumber, holderName, pin, INITIAL_BALANCE);
    }

    // Account creation with a custom opening balance
    public Account(String accountNumber, String holderName, String pin, double openingBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balanceInCents = Math.round(openingBalance * 100);
        this.transactions = new ArrayList<>();
        this.failedAttempts = 0;
        this.locked = false;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    // Converts balance back to Rands for display
    public double getBalance() {
        return (double) balanceInCents / 100.0;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getRemainingAttempts() {
        return MAX_PIN_ATTEMPTS - failedAttempts;
    }

    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public void recordFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= MAX_PIN_ATTEMPTS) {
            locked = true;
        }
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }

    public void deposit(double amount) throws InvalidAmountException {
        long cents = convertToCents(amount);        

        if (cents > (long) (MAX_DEPOSIT * 100)) {
            throw new InvalidAmountException("The maximum deposit per transaction is "
                    + ATMStyle.formatMoney(MAX_DEPOSIT) + ".");
        }

        balanceInCents += cents;   
        transactions.add(new Transaction("Deposit", (double) cents / 100.0, getBalance()));
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        long cents = convertToCents(amount);        

        // Ensure the amount matches the ATM note denominations (multiples of R10)
        if (cents % (NOTE_SIZE * 100L) != 0) {
            throw new InvalidAmountException("Please enter a multiple of R" + NOTE_SIZE
                    + ". The ATM only has R10, R20, R50, R100 and R200 notes.");
        }

        if (cents > (long) (MAX_WITHDRAWAL * 100)) {
            throw new InvalidAmountException("The maximum withdrawal per transaction is "
                    + ATMStyle.formatMoney(MAX_WITHDRAWAL) + ".");
        }

        if (cents > balanceInCents) {
            throw new InsufficientFundsException((double) cents / 100.0, getBalance());
        }

        balanceInCents -= cents; 
        transactions.add(new Transaction("Withdrawal", (double) cents / 100.0, getBalance()));
    }

    // Returns recent transactions for the mini statement display
    public ArrayList<Transaction> getLastTransactions(int count) {
        ArrayList<Transaction> recent = new ArrayList<>();
        int start = transactions.size() - count;
        if (start < 0) {
            start = 0;                          
        }
        for (int i = start; i < transactions.size(); i++) {
            recent.add(transactions.get(i));
        }
        return recent;
    }

    // Validates the amount and converts it to cents to avoid precision errors
    private long convertToCents(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("The amount must be greater than R0.00.");
        }
        return Math.round(amount * 100);
    }
}