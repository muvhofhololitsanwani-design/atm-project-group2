/*
 * File:        Transaction.java
 * Author:      Aubrey Magolego
 * Role:        Member 1, Data model
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: One immutable line of an account's history (a deposit or a
 *              withdrawal). Used for the mini statement and for working
 *              out how much has been withdrawn today.
 */
package atm;

import java.time.LocalDateTime;

public class Transaction {

    /** The two kinds of money movement this ATM supports. */
    public enum Type {
        DEPOSIT("Deposit"),
        WITHDRAWAL("Withdrawal");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        /** Human-friendly name shown on screen. */
        public String getLabel() {
            return label;
        }
    }

    // All fields are final: a recorded transaction can never be altered
    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;

    /** Records a transaction that happened right now. */
    public Transaction(Type type, double amount, double balanceAfter) {
        this(type, amount, balanceAfter, LocalDateTime.now());
    }

    /** Records a transaction with an explicit time (used by the tests). */
    public Transaction(Type type, double amount, double balanceAfter, LocalDateTime timestamp) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
