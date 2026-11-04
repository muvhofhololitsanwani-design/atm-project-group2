 /*  Author      : Ndzalama Mthombeni
 *  Student No. : 260749273
 */

package atm;

public class InsufficientFundsException extends Exception {

    private final double requested;   
    private final double balance;    

    public InsufficientFundsException(double requested, double balance) {
        super("Insufficient funds. You asked for " + ATMStyle.formatMoney(requested)
                + " but your available balance is " + ATMStyle.formatMoney(balance) + ".");
        this.requested = requested;
        this.balance = balance;
    }

    public double getRequested() {
        return requested;
    }

    public double getBalance() {
        return balance;
    }

    // Calculates how much additional money is needed to cover the requested withdrawal
    public double getShortfall() {
        return requested - balance;
    }
}
