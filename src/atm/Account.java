/*
 * File:        Account.java
 * Author:      Aubrey Magolego
 * Role:        Member 1, Data model
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: One bank account: its number, PIN, balance and history.
 *              The PIN is never stored as text. Only a salted PBKDF2 hash
 *              (a deliberately slow one-way function) is kept, so the real
 *              PIN cannot be read back from memory.
 *              This class knows nothing about screens or login attempts.
 */
package atm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Account {

    // One random generator shared by all accounts (used to create salts)
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    // PIN hashing settings. Slow on purpose: every guess costs an attacker the same time.
    // 120 000 rounds keeps the demo and the tests quick. A real bank would use far more.
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_ROUNDS = 120_000;
    private static final int HASH_BITS = 256;

    // ------------------------------------------------------------------
    // Fields: all private, reached only through the public methods below
    // ------------------------------------------------------------------
    private final String accountNumber;
    private final byte[] pinSalt;              // random bytes mixed into the hash
    private final byte[] pinHash;              // PBKDF2(PIN, salt), never the PIN itself
    private double balance;                    // always kept rounded to cents
    private final List<Transaction> history;   // oldest first

    /**
     * Creates an account.
     *
     * @param accountNumber the account number, for example "1001"
     * @param pin           exactly four digits
     * @param balance       opening balance in rand, zero or more
     * @throws IllegalArgumentException if any argument breaks those rules
     */
    public Account(String accountNumber, String pin, double balance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("An account number is required.");
        }
        if (!InputValidator.isValidPin(pin)) {
            throw new IllegalArgumentException(
                    "A PIN must be exactly " + AppConstants.PIN_LENGTH + " digits.");
        }
        if (Double.isNaN(balance) || Double.isInfinite(balance) || balance < 0) {
            throw new IllegalArgumentException("The opening balance cannot be negative.");
        }

        this.accountNumber = accountNumber;
        this.pinSalt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(this.pinSalt);
        this.pinHash = hashPin(pin, this.pinSalt);
        this.balance = roundToCents(balance);
        this.history = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // PIN check
    // ------------------------------------------------------------------

    /** True when the given PIN matches the stored hash. */
    public boolean validatePin(String pin) {
        // A stored PIN is always four digits, so anything else can never match
        if (!InputValidator.isValidPin(pin)) {
            return false;
        }
        // isEqual compares every byte, so timing does not reveal how close a guess was
        return MessageDigest.isEqual(pinHash, hashPin(pin, pinSalt));
    }

    // ------------------------------------------------------------------
    // Money movement
    // ------------------------------------------------------------------

    /** Adds money to the balance and records the transaction. */
    public void deposit(double amount) {
        double cents = requirePositive(amount);

        balance = roundToCents(balance + cents);
        history.add(new Transaction(Transaction.Type.DEPOSIT, cents, balance));
    }

    /**
     * Removes money from the balance and records the transaction.
     *
     * @throws InsufficientFundsException if the amount is more than the balance
     */
    public void withdraw(double amount) throws InsufficientFundsException {
        double cents = requirePositive(amount);

        if (cents > balance) {
            throw new InsufficientFundsException(balance, cents);
        }

        balance = roundToCents(balance - cents);
        history.add(new Transaction(Transaction.Type.WITHDRAWAL, cents, balance));
    }

    // ------------------------------------------------------------------
    // Read-only information
    // ------------------------------------------------------------------

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    /** A snapshot of the history, oldest first. Changing it changes nothing here. */
    public List<Transaction> getHistory() {
        return List.copyOf(history);
    }

    /** Total withdrawn on one calendar day (used for the daily limit). */
    public double getWithdrawnOn(LocalDate day) {
        double total = 0;
        for (Transaction t : history) {
            boolean isWithdrawal = t.getType() == Transaction.Type.WITHDRAWAL;
            if (isWithdrawal && t.getTimestamp().toLocalDate().equals(day)) {
                total += t.getAmount();
            }
        }
        return roundToCents(total);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /** Turns a PIN and a salt into a hash using PBKDF2 (built into Java, no libraries needed). */
    private static byte[] hashPin(String pin, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, HASH_ROUNDS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(HASH_ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Every Java platform provides PBKDF2, so this cannot normally happen
            throw new IllegalStateException("PIN hashing is not available.", e);
        } finally {
            spec.clearPassword();      // wipe the copy of the PIN held by the spec
        }
    }

    /** Rounds to whole cents so that repeated sums never drift (0.1 + 0.2 problem). */
    private static double roundToCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Defends the data model even if a screen forgot to validate. */
    private static double requirePositive(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || roundToCents(amount) <= 0) {
            throw new IllegalArgumentException("The amount must be a positive number.");
        }
        return roundToCents(amount);
    }
}
