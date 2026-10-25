/*
 * File:        InsufficientFundsException.java
 * Author:      Vhukhudo Ravele
 * Role:        Member 3, Validation
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Thrown when a withdrawal is larger than the balance. It
 *              remembers both figures so the screen can tell the customer
 *              exactly how much is available.
 */
package atm;

public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;

    private final double available;
    private final double requested;

    public InsufficientFundsException(double available, double requested) {
        super("Insufficient funds for this withdrawal.");
        this.available = available;
        this.requested = requested;
    }

    /** The balance that was available when the withdrawal was refused. */
    public double getAvailable() {
        return available;
    }

    /** The amount the customer asked for. */
    public double getRequested() {
        return requested;
    }
}
