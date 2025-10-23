package mx.unam.ciencias.myp.pumabank.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.model.Account;

/**
 * Represents the active state of an account within the State design pattern.
 * 
 * <p>
 * In this state, the account operates normally: it allows deposits, withdrawals, and monthly interest processing. If a withdrawal causes the balance to become negative, the account transitions automatically to {@link OverdrawnState}.
 * 
 * </p>
 */
public class ActiveState implements AccountState {

    /**
     * Adds the specified amount to the account balance and records the transaction.
     * @param amount  the amount to deposit
     * @param account the account being modified
     */
    @Override
    public void deposit(double amount, Account account) {
        double balanceBefore = account.getBalance();

        account.setBalance(account.getBalance() + amount);
        account.addHistory("Deposited: " + amount + ", New Balance: " + account.getBalance());
        account.notify(String.format("DEPOSIT: $%.2f | Balance Before: $%.2f | Balance After: $%.2f", 
            amount, balanceBefore, account.getBalance()));
    }

    /**
     * Withdraws the specified amount. If the withdrawal exceeds the balance, the account transitions to {@link OverdrawnState}.
     * @param amount  the amount to withdraw
     * @param account the account being modified
     */
    @Override
    public void withdraw(double amount, Account account) {
        double current = account.getBalance();
        if (current >= amount) {

            double newBalance = current - amount;
            double balanceBefore = account.getBalance();
            account.setBalance(newBalance);
            account.addHistory("Withdrawal: $" + amount + " | Balance: $" + newBalance);
             
            account.notify(String.format("WITHDRAWAL: $%.2f | Balance Before: $%.2f | Balance After: $%.2f", 
                amount, balanceBefore, newBalance));

        } else {
            double newBalance = current - amount;
            double balanceBefore = account.getBalance();
            account.setBalance(newBalance);
            account.addHistory("Withdrawal exceeded funds. Overdraft triggered. Amount: $" 
                    + amount + " | Balance: $" + newBalance);
            account.changeState(new OverdrawnState());
            account.addHistory("State changed -> OverdrawnState");
            
            account.notify(String.format("WITHDRAWAL_OVERDRAFT: $%.2f | Balance Before: $%.2f | Balance After: $%.2f | STATE: Active -> Overdrawn", 
                amount, balanceBefore, newBalance));
        }
    }


    /**
     * Processes the monthly update for the account.
     * If the balance is negative, the account transitions to {@link OverdrawnState}.
     * Otherwise, interest is applied based on the configured interest policy.
     * 
     * 
     * @param account the account being processed
     */
    @Override
    public void processMonth(Account account) {
        if (account.getBalance() < 0) {
            String previousState = "ActiveState";
            account.changeState(new OverdrawnState());
            account.addHistory("Detected negative balance during month-end. Switched -> OverdrawnState");
            
            account.notify(String.format("STATE_CHANGE: %s -> OverdrawnState | Reason: Negative balance detected", 
                previousState));
            
            account.notify("Delegating month-end processing to OverdrawnState.");
            account.processMonth();
            return;
        }

        double balanceBefore = account.getBalance();
        double interest = 0.0;
        if (account.getInterestPolicy() != null) {
            interest = account.getInterestPolicy().calculate(account.getBalance());
        }

        if (interest != 0.0) {
            account.setBalance(account.getBalance() + interest);
            account.addHistory("Monthly interest applied: $" + interest  
                    + " | Balance: $" + account.getBalance());
            
            account.recordInterest(interest);
                    
            account.notify(String.format("INTEREST_APPLIED: $%.2f | Balance Before: $%.2f | Balance After: $%.2f", 
                interest, balanceBefore, account.getBalance()));
        } else {
            account.addHistory("Monthly processing: no interest applied.");
            account.notify("MONTHLY_PROCESSING: No interest applied to active account");
        }

        account.notify("MONTHLY_SUMMARY: Active account processed");
    }

    /**
     * Unfreeze operation has no effect in ActiveState.
     * @param account the account being modified
     */
    @Override
    public void unfreeze(Account account) {
        account.notify("Account is already active.");
        account.addHistory("Unfreeze called, but account is already in ActiveState.");
    }
}