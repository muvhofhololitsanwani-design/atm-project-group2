/*
 * File:        InvalidAmountException.java
 * Author:      Vhukhudo Ravele
 * Role:        Member 3, Validation
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Thrown when an amount cannot be accepted: it is empty, not a
 *              number, zero, negative, has too many decimals, or breaks an
 *              ATM rule such as "whole notes only". The message is written
 *              for the customer and can be shown on screen as it is.
 */
package atm;

public class InvalidAmountException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidAmountException(String message) {
        super(message);
    }
}
