/*
 * File:        AppConstants.java
 * Author:      Aubrey Magolego
 * Role:        Member 1, Data model
 * Group:       Group 2, SDN150S (Software Design 1), CPUT
 * Description: Single home for every fixed value in the ATM, so that no
 *              "magic numbers" are scattered through the code. Change a
 *              rule here and the whole application follows.
 */
package atm;

public final class AppConstants {

    // ------------------------------------------------------------------
    // Values agreed in the team contract (README section 4)
    // ------------------------------------------------------------------

    /** Opening balance of the demo account, in rand. */
    public static final double INITIAL_BALANCE = 10000.00;

    /** Wrong logins allowed before the card is blocked. */
    public static final int MAX_PIN_ATTEMPTS = 3;

    // ------------------------------------------------------------------
    // Demo account used for testing and the demo video
    // ------------------------------------------------------------------

    public static final String DEMO_ACCOUNT_NUMBER = "1001";
    public static final String DEMO_PIN = "1234";

    /** A PIN is exactly this many digits. */
    public static final int PIN_LENGTH = 4;

    // ------------------------------------------------------------------
    // Machine rules (a real ATM only dispenses whole notes and has limits)
    // ------------------------------------------------------------------

    /** Smallest note: withdrawals must be a multiple of this amount. */
    public static final double NOTE_DENOMINATION = 10.00;

    /** Most cash one account may withdraw in a single calendar day. */
    public static final double DAILY_WITHDRAWAL_LIMIT = 5000.00;

    /** Number of recent transactions shown on the balance screen. */
    public static final int MINI_STATEMENT_SIZE = 5;

    /** Seconds without activity before the customer is signed out. */
    public static final int SESSION_TIMEOUT_SECONDS = 120;

    // ------------------------------------------------------------------
    // Quick-select amounts offered on the withdraw and deposit screens
    // ------------------------------------------------------------------

    private static final double[] QUICK_AMOUNTS = {100, 200, 500, 1000};

    /** Returns a copy, so callers can never change the shared array. */
    public static double[] getQuickAmounts() {
        return QUICK_AMOUNTS.clone();
    }

    /** Private constructor: this is a constants holder, never instantiated. */
    private AppConstants() {
    }
}
