 /*  Author      : Ndzalama Mthombeni
 *  Student No. : 260749273
 */

package atm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {

    // Date and time formatter for transaction logging
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    private final String type;              
    private final double amount;            
    private final double balanceAfter;      
    private final LocalDateTime dateTime;  

    //Automatically records the exact timestamp of the transaction
    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getFormattedTime() {
        return dateTime.format(TIME_FORMAT);
    }

    // Formats transaction details into a single structured line for the mini statement display
    @Override
    public String toString() {
        return getFormattedTime() + " | " + type + " " + ATMStyle.formatMoney(amount)
                + " | Balance " + ATMStyle.formatMoney(balanceAfter);
    }
}
