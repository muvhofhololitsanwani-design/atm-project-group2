 /*  Author      : Ndzalama Mthombeni
 *  Student No. : 260749273
 */

package atm;

public class InvalidAmountException extends Exception {

    // Passes the error description to the Exception superclass
    public InvalidAmountException(String message) {
        super(message);
    }
}
