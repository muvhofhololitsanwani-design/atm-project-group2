/*
 *  Author         : Siyolise Mhlauli
 *  Student Number : 261295438
 */
package atm;

public class ATMLogicTest {

    private static int passed = 0;
    private static int failed = 0;

    // Helper method to log test results and update pass/fail counters
    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + description);
        } else {
            failed++;
            System.out.println("FAIL  " + description);
        }
    }

    // Floating-point comparison helper with a small tolerance for cent rounding
    private static boolean sameMoney(double a, double b) {
        return Math.abs(a - b) < 0.005;
    }

    public static void main(String[] args) {

        System.out.println("=== Account tests ===");
        Account account = new Account("1111111111", "Test Customer", "1234");
        check("T01 new account starts with R10 000.00", sameMoney(account.getBalance(), 10000.00));

        try {
            account.deposit(500);
            check("T02 deposit R500 gives R10 500.00", sameMoney(account.getBalance(), 10500.00));
            account.deposit(250.50);
            check("T03 deposit R250.50 gives R10 750.50", sameMoney(account.getBalance(), 10750.50));
            account.withdraw(300);
            check("T04 withdraw R300 gives R10 450.50", sameMoney(account.getBalance(), 10450.50));
        } catch (Exception e) {
            check("T02-T04 valid deposits and withdrawals must not throw: " + e.getMessage(), false);
        }

        double before = account.getBalance();

        // Testing invalid withdrawal amounts and business constraints
        try {
            account.withdraw(15);
            check("T05 withdraw R15 is rejected (not a multiple of R10)", false);
        } catch (InvalidAmountException e) {
            check("T05 withdraw R15 is rejected (not a multiple of R10)", true);
        } catch (InsufficientFundsException e) {
            check("T05 withdraw R15 is rejected (not a multiple of R10)", false);
        }

        try {
            account.withdraw(6000);
            check("T06 withdraw R6 000 is rejected (above R5 000 limit)", false);
        } catch (InvalidAmountException e) {
            check("T06 withdraw R6 000 is rejected (above R5 000 limit)", true);
        } catch (InsufficientFundsException e) {
            check("T06 withdraw R6 000 is rejected (above R5 000 limit)", false);
        }

        try {
            account.withdraw(0);
            check("T07 withdraw R0 is rejected", false);
        } catch (InvalidAmountException e) {
            check("T07 withdraw R0 is rejected", true);
        } catch (InsufficientFundsException e) {
            check("T07 withdraw R0 is rejected", false);
        }

        try {
            account.withdraw(-50);
            check("T08 withdraw a negative amount is rejected", false);
        } catch (InvalidAmountException e) {
            check("T08 withdraw a negative amount is rejected", true);
        } catch (InsufficientFundsException e) {
            check("T08 withdraw a negative amount is rejected", false);
        }

        check("T09 rejected withdrawals did not change the balance", sameMoney(account.getBalance(), before));

        // Testing insufficient funds handling and shortfall calculation
        Account poor = new Account("2222222222", "Low Balance", "1111", 100.00);
        try {
            poor.withdraw(500);
            check("T10 withdraw R500 from R100 throws InsufficientFundsException", false);
        } catch (InsufficientFundsException e) {
            check("T10 withdraw R500 from R100 throws InsufficientFundsException", true);
            check("T11 shortfall is R400.00", sameMoney(e.getShortfall(), 400.00));
        } catch (InvalidAmountException e) {
            check("T10 withdraw R500 from R100 throws InsufficientFundsException", false);
        }
        check("T12 balance stays R100.00 after the failed withdrawal", sameMoney(poor.getBalance(), 100.00));

        // Testing edge cases for deposits using an array loop
        double[] badDeposits = {0, -20, 25000, 10.123};
        String[] badNames = {"R0", "-R20", "R25 000 (above R20 000 limit)", "R10.123 (3 decimals)"};
        for (int i = 0; i < badDeposits.length; i++) {
            try {
                account.deposit(badDeposits[i]);
                check("T" + (13 + i) + " deposit " + badNames[i] + " is rejected", false);
            } catch (InvalidAmountException e) {
                check("T" + (13 + i) + " deposit " + badNames[i] + " is rejected", true);
            }
        }

        try {
            Account rich = new Account("3333333333", "Rich Customer", "9999", 20000.00);
            rich.withdraw(5000);
            check("T17 withdraw exactly R5 000 is allowed", sameMoney(rich.getBalance(), 15000.00));
        } catch (Exception e) {
            check("T17 withdraw exactly R5 000 is allowed", false);
        }

        check("T18 history holds the 3 successful transactions", account.getLastTransactions(5).size() == 3);

        check("T19 correct PIN is accepted", account.checkPin("1234"));
        check("T20 wrong PIN is rejected", !account.checkPin("0000"));

        System.out.println();
        System.out.println("=== Bank tests ===");
        Bank bank = new Bank();
        check("T21 bank loads 3 demo accounts", bank.getAccountCount() == 3);
        check("T22 unknown account number is not found",
                bank.authenticate("0000000000", "1234") == Bank.AUTH_NO_ACCOUNT);
        check("T23 correct login succeeds",
                bank.authenticate("1234567890", "1234") == Bank.AUTH_SUCCESS);

        // Verifying that entering 3 incorrect PINs locks the account
        int first = bank.authenticate("0987654321", "0000");
        int second = bank.authenticate("0987654321", "0000");
        int third = bank.authenticate("0987654321", "0000");
        int afterBlock = bank.authenticate("0987654321", "4321");
        check("T24 3 wrong PINs block the card (even the correct PIN is then refused)",
                first == Bank.AUTH_WRONG_PIN && second == Bank.AUTH_WRONG_PIN
                && third == Bank.AUTH_LOCKED && afterBlock == Bank.AUTH_LOCKED);

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
