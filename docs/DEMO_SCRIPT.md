# Demo video script (3 to 5 minutes)

Target length: **4 min 30 s**. Record the screen with voice-over (OBS Studio or Xbox Game Bar on Windows).
Run `scripts/run.sh` (or `scripts\run.bat`) before recording. Demo account: **1001**, PIN **1234**.

| Time | Who | Show on screen | Say (key points) |
|---|---|---|---|
| 0:00 | Siyolise | Title, then start the app from the terminal | Group 2, SDN150S, Java ATM. Three layers: screens, ATM controller, Account data. One shared `ATM` object |
| 0:20 | Muvhofhololi | **Login**: press Log in empty; type `10a1`; PIN `12`; then wrong PIN `0000` twice (window wobbles, attempts count down); then the correct PIN. Tick "Show PIN" once | Input is validated before the ATM is asked. Wrong PIN shows attempts left. Shared look comes from `UIStyle` |
| 0:55 | Athenkosi | **Main menu**: point at greeting and the four buttons, hover a button, press Alt+D to show the shortcut | Four required options. Buttons are wired with one `switch` on the action command |
| 1:15 | Kgonyane | **Withdraw**: `155` (rejected: notes), `abc`, `-20`, `0`, `15000` (insufficient funds), `6000` (daily limit), click **R500** then Confirm (success, new balance R9 500.00) | try / catch / finally. Three different error paths. Daily limit label updates |
| 2:00 | Ndzalama | **Deposit**: `R 1 250.75` (spaces and R accepted), `5.555` (rejected), quick button R200 | Deposit validation. Balance label refreshes after each deposit |
| 2:30 | Siyolise | **Check Balance**: big balance and the mini statement (green deposits, red withdrawals) | Statement comes from the account history, newest first |
| 2:55 | Aubrey and Mabandla | Switch to the editor: `Account.java` (salted hash in `validatePin`), then `ATM.authenticate` (lockout) | PIN is never stored as text. After 3 wrong PINs the card is blocked |
| 3:25 | Vhukhudo | Terminal: run `scripts/test.sh` and let the result line show | `InputValidator` and the two exceptions. **105 checks, all pass** |
| 3:50 | Muvhofhololi | Back in the app: press **Exit**, click Yes (program closes with `System.exit(0)`). Start again and enter 3 wrong PINs (card blocked, program closes) | Exit and lockout both end the program, as agreed in the README |
| 4:15 | Muvhofhololi | GitHub: commit history and pull requests | Version control: regular, meaningful commits by all 8 members |
| 4:30 | Everyone | End card | Thank you |

Tips

- Keep the mouse still for a moment after each action so the viewer can read the message.
- The app signs the customer out after 120 seconds without mouse or keyboard activity. Keep moving.
- Do not mention the inactivity timeout live unless you want to show it. To show it quickly, set
  `SESSION_TIMEOUT_SECONDS` to 10 in `AppConstants.java`, record that clip, then set it back to 120.
