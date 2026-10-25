# Pseudocode: Group 2 ATM

Plain-language description of every class, written to match the implemented Java code.
It uses no programming syntax (SDN150S rubric: *clear, descriptive, no ambiguity*).

## Program start (ATMApp)

```
START the program
    Choose the cross-platform look so colours are identical on every computer
    On the screen-drawing thread
        Create one ATM object, which sets up the demo account
        Show the Login screen, giving it the ATM object
        Start the inactivity monitor with the 120-second limit
END
```

## Account

```
WHEN a new Account is created
    Receive an account number, a PIN and a starting balance
    IF the account number is blank, the PIN is not four digits, or the balance is negative
        Refuse to create the account
    Make a random salt
    Mix the salt with the PIN and scramble it into a hash
    Store the account number, salt, hash and balance (rounded to cents)
    Start with an empty history

TO check a PIN
    IF no PIN was given
        Report FALSE
    Scramble the given PIN with the stored salt
    IF the result matches the stored hash
        Report TRUE
    ELSE
        Report FALSE

TO deposit an amount
    Round the amount to cents
    IF the amount is not a positive number
        Refuse the deposit
    Add the amount to the balance
    Record a "deposit" entry: amount, new balance and the time

TO withdraw an amount
    Round the amount to cents
    IF the amount is not a positive number
        Refuse the withdrawal
    IF the amount is more than the balance
        Raise an "insufficient funds" error holding the balance and the amount
    Subtract the amount from the balance
    Record a "withdrawal" entry: amount, new balance and the time

TO total the withdrawals made on a given day
    Set the total to zero
    FOR EACH entry in the history
        IF the entry is a withdrawal made on that day
            Add its amount to the total
    Report the total
```

## ATM (controller)

```
WHEN the ATM is created
    Create the demo account (1001, PIN 1234, R10 000.00) and add it to the account list
    Set the current account to nothing
    Set failed attempts to zero

TO authenticate a user (account number and PIN given)
    IF the card is already blocked
        Report FALSE
    Search the account list for the account number
    IF an account was found AND its PIN matches
        Set the current account to the account found
        Set failed attempts to zero
        Report TRUE
    ELSE
        Add one to failed attempts
        Report FALSE

TO check whether the card is blocked
    Report TRUE when failed attempts has reached 3

TO withdraw an amount
    Make sure a customer is logged in
    IF the amount is not a whole number of R10 notes
        Raise an "invalid amount" error
    IF the amount is more than the balance
        Raise an "insufficient funds" error
    IF the amount is more than what is left of today's R5 000 limit
        Raise an "invalid amount" error about the daily limit
    Ask the current account to withdraw the amount

TO work out the remaining daily limit
    Ask the current account how much was withdrawn today
    Subtract that from R5 000
    Report the result, never less than zero

TO deposit an amount
    Make sure a customer is logged in
    Ask the current account to deposit the amount

TO get the balance
    Make sure a customer is logged in
    Ask the current account for its balance and report it

TO list the most recent transactions (a count is given)
    Make sure a customer is logged in
    Start with an empty list
    Walk through the history from newest to oldest
        Add each entry until the count is reached or the history ends
    Report the list

TO log out
    Set the current account to nothing
```

## InputValidator

```
TO convert text to an amount
    Remove all spaces and one leading "R" from the text
    IF the text is empty
        Raise an error: please enter an amount
    ELSE IF the text starts with a minus sign
        Raise an error: an amount cannot be negative
    ELSE IF the text has more than two decimal places
        Raise an error: use at most two decimal places
    ELSE IF the text has more than nine digits before the decimal point
        Raise an error: that amount is too large
    ELSE IF the text is not digits with at most one decimal point
        Raise an error: use digits only
    Convert the text to a number
    IF the number is zero
        Raise an error: the amount must be greater than zero
    Report the number

TO check a PIN
    IF the PIN is exactly four digits
        Report TRUE
    ELSE
        Report FALSE

TO check an account number
    IF the account number is four to ten digits
        Report TRUE
    ELSE
        Report FALSE
```

## Login screen

```
WHEN the Login screen is created
    Receive the ATM object and keep it
    Draw the account number field, the PIN field (characters hidden),
        a "Show PIN" tick box, the Login button and a message line
    Make the Enter key press the Login button

WHEN the user ticks or unticks "Show PIN"
    IF it is ticked
        Show the PIN digits
    ELSE
        Hide the PIN digits behind dots

WHEN the user presses Login
    Read the account number and the PIN from the fields
    IF either field is empty
        Show a message asking for both details
    ELSE IF the account number is not digits only
        Show a message that account numbers contain digits only
    ELSE IF the PIN is not four digits
        Show a message that the PIN must be four digits
    ELSE IF the ATM accepts the details
        Close the Login screen
        Open the Main Menu screen
    ELSE IF the card is now blocked
        Show a "card blocked" message
        Exit the program
    ELSE
        Show an "incorrect details" message with the attempts remaining
        Clear the PIN field
        Wobble the window left and right
```

## Main Menu screen

```
WHEN the Main Menu is created
    Receive the ATM object and keep it
    IF the hour is before 12
        Greet with "Good morning"
    ELSE IF the hour is before 18
        Greet with "Good afternoon"
    ELSE
        Greet with "Good evening"
    Draw four buttons: Withdraw, Deposit, Check Balance, Exit

WHEN the user presses a button
    CASE Withdraw
        Close the menu and open the Withdraw screen
    CASE Deposit
        Close the menu and open the Deposit screen
    CASE Check Balance
        Close the menu and open the Balance screen
    CASE Exit
        Ask "End your session and exit the ATM?"
        IF the user answers yes
            Ask the ATM to log out
            Close the whole program
```

## Withdraw screen

```
WHEN the Withdraw screen is created
    Receive the ATM object and keep it
    Draw the amount field, the quick-amount buttons, a label for the
        remaining daily limit, a Confirm button, a Back button and a message line
    Show how much may still be withdrawn today

WHEN the user presses a quick-amount button
    Put that amount into the amount field

WHEN the user presses Confirm
    TRY
        Convert the text in the amount field to a number
        Ask the ATM to withdraw that amount
        Show a green "withdrawal successful" message with the new balance
        Clear the amount field
    IF the amount was invalid, not whole notes, or over the daily limit
        Show the reason in red
    IF there were insufficient funds
        Show a red "insufficient funds" message with the available balance
    ALWAYS
        Refresh the remaining daily limit label
        Put the cursor back in the amount field

WHEN the user presses Back
    Close this screen and open the Main Menu
```

## Deposit screen

```
WHEN the Deposit screen is created
    Receive the ATM object and keep it
    Draw the amount field, the quick-amount buttons, a label for the current
        balance, a Confirm button, a Back button and a message line
    Show the current balance

WHEN the user presses a quick-amount button
    Put that amount into the amount field

WHEN the user presses Confirm
    TRY
        Convert the text in the amount field to a number
        Ask the ATM to deposit that amount
        Show a green "deposit successful" message with the new balance
        Clear the amount field
    IF the amount was invalid
        Show the reason in red
    ALWAYS
        Refresh the current balance label
        Put the cursor back in the amount field

WHEN the user presses Back
    Close this screen and open the Main Menu
```

## Balance screen

```
WHEN the Balance screen is created
    Receive the ATM object and keep it
    Ask the ATM for the balance and show it large, formatted as rand with two decimals
    Ask the ATM for the 5 most recent transactions
    IF there are none
        Show "No transactions yet"
    ELSE
        FOR EACH transaction, newest first
            Draw one line: type, date and time, and the amount
            IF it is a deposit
                Colour the amount green with a plus sign
            ELSE
                Colour the amount red with a minus sign

WHEN the user presses Back
    Close this screen and open the Main Menu
```

## Inactivity monitor (SessionMonitor)

```
WHEN the monitor starts
    Start a countdown of 120 seconds
    WHENEVER the mouse moves, a mouse button is pressed or a key is pressed
        Restart the countdown

WHEN the countdown reaches zero
    IF nobody is logged in
        Restart the countdown
    ELSE
        Ask the ATM to log out
        Open a new Login screen
        Close every other window
        Tell the customer they were signed out for their own safety
```
