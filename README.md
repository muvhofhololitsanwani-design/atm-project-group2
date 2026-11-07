# CPUT Smart ATM 
# SDN150S Group Project 2

This is Java ATM simulator project for **Software Design 1** at CPUT, built by first year Computer Engineering students.

**Due Date:** Sunday, 8 November 2026  
**Group Leader:** Muvhofhololi Tsanwani (`muvhofhololitsanwani-design`)  
**Repository:** `atm-project-group2`

---

## What our program does
We had to build a desktop ATM simulator using Java and the Swing GUI toolkit (`JFrame` forms). Here is how we implemented the requirements from the project brief:

* **Login (`LoginFrame`):** Asks for a 10-digit account number and a 4-digit PIN. `Bank.authenticate()` checks them. If you enter the wrong PIN 3 times, your card gets blocked!
* **Starting Balance:** Every demo account starts with `Account.INITIAL_BALANCE` = R10 000.00.
* **Withdraw (`WithdrawFrame`):** Use quick-cash buttons or type an amount. Must be in multiples of R10, between R10 and R5 000 per transaction.
* **Deposit (`DepositFrame`):** Add money up to R20 000 per transaction with a max of 2 decimal places.
* **Check Balance (`BalanceFrame`):** Shows the current total balance and a mini statement of the last 5 transactions.
* **Exit (`MainMenuFrame`):** Asks for confirmation when you click Exit, then calls `System.exit(0)`.
* **Error Handling:** We added custom exceptions (`InvalidAmountException` and `InsufficientFundsException`) with `try / catch` blocks so the app doesn't crash if someone types letters instead of numbers.

### Test Accounts (for demo & testing)
| Customer Name | Account Number | PIN |
|---|---|---|
| Siyolise Mhlauli | `1234567890` | `1234` |
| Athenkosi Khau | `0987654321` | `4321` |
| Kgonyane Makgoale | `1122334455` | `2468` |

---

## Project Structure
Here is how our files are organized inside the repo:

```text
atm-project-group2/
├── README.md
├── .gitignore
├── docs/                       <- UML diagrams, screenshots, final report PDF
└── src/atm/
    ├── Main.java               <- Entry point (starts login screen)
    ├── ATMStyle.java           <- Shared colors, fonts, helper methods
    ├── LoginFrame.java         <- JFrame login screen
    ├── MainMenuFrame.java      <- Main menu options
    ├── WithdrawFrame.java      <- Withdraw cash screen
    ├── DepositFrame.java       <- Deposit cash screen
    ├── BalanceFrame.java       <- Balance & mini statement screen
    ├── Account.java            <- Account logic, balance, PIN, rules
    ├── Bank.java               <- List of accounts & login checks
    ├── Transaction.java        <- Transaction record class
    ├── InvalidAmountException.java     <- Custom exception
    ├── InsufficientFundsException.java <- Custom exception
    └── ATMLogicTest.java       <- Console test program (24 checks)
