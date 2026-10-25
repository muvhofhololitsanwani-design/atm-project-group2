/*
 * File:        BackendTests.java
 * Author:      Kgonyane Makgoale and Ndzalama Mthombeni
 * Role:        Members 6 and 7, Results and analysis (testing)
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Self-contained test runner for the backend classes (Account,
 *              ATM, InputValidator, Transaction). No libraries needed: it
 *              prints PASS or FAIL for every check and a summary at the end.
 *              The output is used in the "Results and analysis" section of
 *              the technical report.
 *
 * Run from the project folder:
 *     javac -d out src/atm/*.java test/atm/BackendTests.java
 *     java -cp out atm.BackendTests
 */
package atm;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BackendTests {

    /** A block of code that is expected to throw. */
    private interface Action {
        void run() throws Exception;
    }

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        runSection("Account", BackendTests::testAccount);
        runSection("Transaction", BackendTests::testTransaction);
        runSection("InputValidator", BackendTests::testInputValidator);
        runSection("InputValidator messages", BackendTests::testValidatorMessages);
        runSection("ATM login", BackendTests::testAtmLogin);
        runSection("ATM transactions", BackendTests::testAtmTransactions);
        runSection("ATM rules", BackendTests::testAtmRules);
        runSection("End to end", BackendTests::testFullCustomerJourney);

        System.out.println();
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed, "
                + (passed + failed) + " checks in total");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Runs one group of tests; an unexpected exception counts as a failure, not a crash. */
    private static void runSection(String name, Action group) {
        try {
            group.run();
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] section \"" + name + "\" stopped early: " + e);
        }
    }

    // ------------------------------------------------------------------
    // Account
    // ------------------------------------------------------------------
    private static void testAccount() throws Exception {
        section("Account (data model)");

        Account acc = new Account("1001", "1234", 10000.00);
        check("constructor stores account number", acc.getAccountNumber().equals("1001"));
        check("constructor stores opening balance", acc.getBalance() == 10000.00);
        check("correct PIN accepted", acc.validatePin("1234"));
        check("wrong PIN rejected", !acc.validatePin("4321"));
        check("null PIN rejected", !acc.validatePin(null));
        check("PIN is not stored as plain text", pinNotStoredAsText(acc, "1234"));

        acc.deposit(250.50);
        check("deposit adds to balance", acc.getBalance() == 10250.50);

        Account tiny = new Account("2002", "1111", 0.10);
        tiny.deposit(0.20);
        check("cents never drift (0.10 + 0.20 = 0.30)", tiny.getBalance() == 0.30);

        acc.withdraw(250.50);
        check("withdraw subtracts from balance", acc.getBalance() == 10000.00);

        acc.withdraw(10000.00);
        check("withdrawing the whole balance leaves zero", acc.getBalance() == 0.00);

        Account poor = new Account("3003", "2222", 100.00);
        try {
            poor.withdraw(100.01);
            check("overdraft is refused", false);
        } catch (InsufficientFundsException e) {
            check("overdraft is refused", true);
            check("exception reports available balance", e.getAvailable() == 100.00);
            check("exception reports requested amount", e.getRequested() == 100.01);
            check("balance unchanged after refused withdrawal", poor.getBalance() == 100.00);
        }

        expectThrows("deposit of zero is rejected", IllegalArgumentException.class, () -> poor.deposit(0));
        expectThrows("deposit of negative is rejected", IllegalArgumentException.class, () -> poor.deposit(-5));
        expectThrows("deposit of NaN is rejected", IllegalArgumentException.class, () -> poor.deposit(Double.NaN));
        expectThrows("withdraw of negative is rejected", IllegalArgumentException.class, () -> poor.withdraw(-1));
        expectThrows("constructor rejects letters in PIN", IllegalArgumentException.class,
                () -> new Account("4004", "12a4", 0));
        expectThrows("constructor rejects 3-digit PIN", IllegalArgumentException.class,
                () -> new Account("4004", "123", 0));
        expectThrows("constructor rejects negative balance", IllegalArgumentException.class,
                () -> new Account("4004", "1234", -1));
        expectThrows("constructor rejects blank account number", IllegalArgumentException.class,
                () -> new Account("  ", "1234", 0));

        Account log = new Account("5005", "1234", 1000);
        log.deposit(100);
        log.withdraw(40);
        List<Transaction> history = log.getHistory();
        check("history keeps two entries in order", history.size() == 2
                && history.get(0).getType() == Transaction.Type.DEPOSIT
                && history.get(1).getType() == Transaction.Type.WITHDRAWAL);
        check("history records balance after each entry", history.get(1).getBalanceAfter() == 1060.00);
        expectThrows("history snapshot cannot be edited", UnsupportedOperationException.class,
                () -> log.getHistory().clear());
        check("withdrawn today is counted", log.getWithdrawnOn(LocalDate.now()) == 40.00);
        check("withdrawn on another day is zero", log.getWithdrawnOn(LocalDate.now().plusDays(1)) == 0.00);
    }

    // ------------------------------------------------------------------
    // Transaction
    // ------------------------------------------------------------------
    private static void testTransaction() {
        section("Transaction");

        LocalDateTime when = LocalDateTime.of(2026, 10, 23, 14, 30);
        Transaction t = new Transaction(Transaction.Type.DEPOSIT, 500.00, 10500.00, when);
        check("stores type, amount and balance", t.getType() == Transaction.Type.DEPOSIT
                && t.getAmount() == 500.00 && t.getBalanceAfter() == 10500.00);
        check("stores the given timestamp", t.getTimestamp().equals(when));
        check("type labels are readable", Transaction.Type.WITHDRAWAL.getLabel().equals("Withdrawal"));
    }

    // ------------------------------------------------------------------
    // InputValidator
    // ------------------------------------------------------------------
    private static void testInputValidator() throws Exception {
        section("InputValidator (input validation)");

        check("accepts 500", InputValidator.parseAmount("500") == 500.0);
        check("accepts decimals 99.95", InputValidator.parseAmount("99.95") == 99.95);
        check("accepts surrounding spaces", InputValidator.parseAmount("  250  ") == 250.0);
        check("accepts 'R 1 500.50' (rand sign and spaces)", InputValidator.parseAmount("R 1 500.50") == 1500.50);

        String[] bad = {"", "   ", "abc", "12abc", "-50", "0", "0.00", "10.555", "1e3", "NaN",
                "Infinity", "1,000", "50.5.5", "1234567890", "R", "--5", "5-"};
        for (String text : bad) {
            expectThrows("rejects \"" + text + "\"", InvalidAmountException.class,
                    () -> InputValidator.parseAmount(text));
        }
        expectThrows("rejects null", InvalidAmountException.class, () -> InputValidator.parseAmount(null));

        check("PIN 1234 is valid", InputValidator.isValidPin("1234"));
        check("PIN 123 is too short", !InputValidator.isValidPin("123"));
        check("PIN 12345 is too long", !InputValidator.isValidPin("12345"));
        check("PIN 12a4 has a letter", !InputValidator.isValidPin("12a4"));
        check("PIN null is invalid", !InputValidator.isValidPin(null));
        check("non-ASCII digits are not a valid PIN", !InputValidator.isValidPin("\u0661\u0662\u0663\u0664"));
        check("account 1001 is valid", InputValidator.isValidAccountNumber("1001"));
        check("account 10 is too short", !InputValidator.isValidAccountNumber("10"));
        check("account ab12 is invalid", !InputValidator.isValidAccountNumber("ab12"));
    }

    // ------------------------------------------------------------------
    // InputValidator: the customer must be told WHY an amount was refused
    // ------------------------------------------------------------------
    private static void testValidatorMessages() {
        section("InputValidator (error messages)");

        check("empty input asks for an amount", reason("").contains("enter an amount"));
        check("negative input says negative", reason("-50").contains("negative"));
        check("zero input says greater than zero", reason("0").contains("greater than zero"));
        check("3 decimals mentions decimal places", reason("10.555").contains("decimal"));
        check("huge input says too large", reason("1234567890").contains("too large"));
        check("letters say digits only", reason("abc").contains("digits only"));
    }

    /** The message the customer would see for a bad amount (empty if it was accepted). */
    private static String reason(String text) {
        try {
            InputValidator.parseAmount(text);
            return "";
        } catch (InvalidAmountException e) {
            return e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    // ATM: login and lockout
    // ------------------------------------------------------------------
    private static void testAtmLogin() {
        section("ATM (login and lockout)");

        ATM atm = new ATM();
        check("nobody is logged in at start", !atm.isLoggedIn());
        check("correct login succeeds", atm.authenticate("1001", "1234"));
        check("logged in after success", atm.isLoggedIn());
        check("demo balance is R10 000.00", atm.getBalance() == 10000.00);
        atm.logout();
        check("logout clears the session", !atm.isLoggedIn());

        ATM wrong = new ATM();
        check("wrong PIN fails", !wrong.authenticate("1001", "0000"));
        check("one attempt used, two remain", wrong.getRemainingAttempts() == 2);
        check("unknown account fails", !wrong.authenticate("9999", "1234"));
        check("failure count includes unknown accounts", wrong.getFailedAttempts() == 2);
        check("success resets the counter", wrong.authenticate("1001", "1234") && wrong.getFailedAttempts() == 0);

        ATM locked = new ATM();
        locked.authenticate("1001", "1");
        locked.authenticate("1001", "2");
        check("card is not blocked after two failures", !locked.isLocked());
        locked.authenticate("1001", "3");
        check("card is blocked after three failures", locked.isLocked());
        check("blocked card refuses the correct PIN", !locked.authenticate("1001", "1234"));
        check("remaining attempts never negative", locked.getRemainingAttempts() == 0);
    }

    // ------------------------------------------------------------------
    // ATM: deposit, withdraw, balance, statement
    // ------------------------------------------------------------------
    private static void testAtmTransactions() throws Exception {
        section("ATM (transactions)");

        ATM atm = new ATM();
        atm.authenticate("1001", "1234");

        atm.withdraw(500);
        check("withdraw R500 leaves R9 500.00", atm.getBalance() == 9500.00);
        atm.deposit(1250.75);
        check("deposit R1 250.75 gives R10 750.75", atm.getBalance() == 10750.75);

        try {
            atm.withdraw(20000);
            check("withdrawing more than the balance is refused", false);
        } catch (InsufficientFundsException e) {
            check("withdrawing more than the balance is refused", true);
            check("refusal reports the balance", e.getAvailable() == 10750.75);
        }
        check("balance unchanged after refusal", atm.getBalance() == 10750.75);

        atm.withdraw(10);
        atm.deposit(5);
        atm.withdraw(20);
        List<Transaction> recent = atm.getRecentTransactions(3);
        check("mini statement returns the requested count", recent.size() == 3);
        check("mini statement is newest first", recent.get(0).getAmount() == 20.00
                && recent.get(1).getAmount() == 5.00 && recent.get(2).getAmount() == 10.00);
        check("mini statement never exceeds the history", atm.getRecentTransactions(50).size() == 5);

        atm.logout();
        expectThrows("balance needs a login", IllegalStateException.class, atm::getBalance);
        expectThrows("deposit needs a login", IllegalStateException.class, () -> atm.deposit(10));
        expectThrows("withdraw needs a login", IllegalStateException.class, () -> atm.withdraw(10));
    }

    // ------------------------------------------------------------------
    // ATM: machine rules (notes and daily limit)
    // ------------------------------------------------------------------
    private static void testAtmRules() throws Exception {
        section("ATM (cash rules)");

        ATM atm = new ATM();
        atm.authenticate("1001", "1234");

        expectThrows("R155 is refused (not whole notes)", InvalidAmountException.class, () -> atm.withdraw(155));
        expectThrows("R10.50 is refused (not whole notes)", InvalidAmountException.class, () -> atm.withdraw(10.50));
        check("refused withdrawals change nothing", atm.getBalance() == 10000.00);
        check("full daily limit available at start", atm.getRemainingDailyLimit() == 5000.00);

        expectThrows("R5 010 is refused (over daily limit)", InvalidAmountException.class, () -> atm.withdraw(5010));
        atm.withdraw(3000);
        check("remaining limit drops to R2 000.00", atm.getRemainingDailyLimit() == 2000.00);
        expectThrows("R2 500 is refused after R3 000 (over limit)", InvalidAmountException.class,
                () -> atm.withdraw(2500));
        atm.withdraw(2000);
        check("limit can be used up exactly", atm.getRemainingDailyLimit() == 0.00);
        expectThrows("any further withdrawal is refused", InvalidAmountException.class, () -> atm.withdraw(10));
        atm.deposit(100);
        check("deposits are not limited", atm.getBalance() == 5100.00);

        ATM broke = new ATM();
        broke.authenticate("1001", "1234");
        try {
            broke.withdraw(15000);
            check("insufficient funds is reported before the daily limit", false);
        } catch (InsufficientFundsException e) {
            check("insufficient funds is reported before the daily limit", true);
        }
    }

    // ------------------------------------------------------------------
    // End to end: the scenario in the project brief
    // ------------------------------------------------------------------
    private static void testFullCustomerJourney() throws Exception {
        section("End to end (scenario from the project brief)");

        ATM atm = new ATM();
        boolean in = atm.authenticate("1001", "1234");
        double start = atm.getBalance();
        atm.deposit(2000);
        atm.withdraw(500);
        double finish = atm.getBalance();
        atm.logout();

        check("login, deposit R2 000, withdraw R500", in && start == 10000.00 && finish == 11500.00);
        check("session closed at the end", !atm.isLoggedIn());
    }

    // ------------------------------------------------------------------
    // Tiny test framework
    // ------------------------------------------------------------------
    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }

    private static void expectThrows(String name, Class<? extends Throwable> type, Action action) {
        try {
            action.run();
            check(name, false);
        } catch (Exception e) {
            check(name, type.isInstance(e));
        }
    }

    /** Uses reflection to prove no field of the account holds the PIN as text. */
    private static boolean pinNotStoredAsText(Account account, String pin) throws Exception {
        for (Field field : Account.class.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(account);
            if (value instanceof String && value.equals(pin)) {
                return false;
            }
        }
        return true;
    }
}
