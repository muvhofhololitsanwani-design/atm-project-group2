/*
 * File:        InputValidator.java
 * Author:      Vhukhudo Ravele
 * Role:        Member 3, Validation
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Turns raw text typed by the customer into safe values.
 *              Every check is strict: anything that is not a plain positive
 *              amount (letters, symbols, scientific notation, negatives,
 *              more than two decimals) is rejected with a clear message.
 */
package atm;

import java.util.regex.Pattern;

public final class InputValidator {

    // Whole rand with at most 2 decimals, up to 9 digits before the point
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d{1,9}(\\.\\d{1,2})?");

    // Same shape but with 3 or more decimals, only used to pick a better message
    private static final Pattern TOO_MANY_DECIMALS = Pattern.compile("\\d+\\.\\d{3,}");

    // Digits only, 10 or more of them
    private static final Pattern TOO_LARGE = Pattern.compile("\\d{10,}(\\.\\d+)?");

    // A PIN is exactly PIN_LENGTH digits (ASCII digits only)
    private static final Pattern PIN_PATTERN =
            Pattern.compile("\\d{" + AppConstants.PIN_LENGTH + "}");

    // Account numbers are 4 to 10 digits
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\d{4,10}");

    /** Private constructor: all methods are static, nobody needs an object. */
    private InputValidator() {
    }

    /**
     * Converts the text from an amount field into a valid positive amount.
     *
     * @param text what the customer typed (spaces and a leading "R" are allowed)
     * @return the amount as a double
     * @throws InvalidAmountException with a customer-friendly reason if rejected
     */
    public static double parseAmount(String text) throws InvalidAmountException {

        // Step 1: tidy the input ("R 1 500.50" becomes "1500.50")
        String cleaned = (text == null) ? "" : text.replaceAll("\\s+", "");
        if (cleaned.startsWith("R") || cleaned.startsWith("r")) {
            cleaned = cleaned.substring(1);
        }

        // Step 2: reject anything that is not a plain amount, with the best message
        if (cleaned.isEmpty()) {
            throw new InvalidAmountException("Please enter an amount.");
        } else if (cleaned.startsWith("-")) {
            throw new InvalidAmountException("An amount cannot be negative.");
        } else if (TOO_MANY_DECIMALS.matcher(cleaned).matches()) {
            throw new InvalidAmountException("Use at most two decimal places (cents).");
        } else if (TOO_LARGE.matcher(cleaned).matches()) {
            throw new InvalidAmountException("That amount is too large.");
        } else if (!AMOUNT_PATTERN.matcher(cleaned).matches()) {
            throw new InvalidAmountException("Use digits only, with an optional decimal point.");
        }

        // Step 3: safe to convert now, the pattern guarantees a valid number
        double amount = Double.parseDouble(cleaned);
        if (amount <= 0) {
            throw new InvalidAmountException("The amount must be greater than zero.");
        }
        return amount;
    }

    /** True when the PIN is exactly four digits (null and letters fail). */
    public static boolean isValidPin(String pin) {
        return pin != null && PIN_PATTERN.matcher(pin).matches();
    }

    /** True when the account number is 4 to 10 digits. */
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && ACCOUNT_PATTERN.matcher(accountNumber).matches();
    }
}
