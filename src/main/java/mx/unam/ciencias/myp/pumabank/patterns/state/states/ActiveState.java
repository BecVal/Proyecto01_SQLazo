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

        account.setBalance(account.getBalance() + amount);
        account.addHistory("Deposited: " + amount + ", New Balance: " + account.getBalance());
        account.notify("Deposit recorded. New balance: $" + account.getBalance());

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
            account.setBalance(newBalance);
            account.addHistory("Withdrawal: $" + amount + " | Balance: $" + newBalance);
            account.notify("Withdrawal executed. New balance: $" + newBalance);

        } else {
            double newBalance = current - amount;
            account.setBalance(newBalance);
            account.addHistory("Withdrawal exceeded funds. Overdraft triggered. Amount: $" 
                    + amount + " | Balance: $" + newBalance);
            account.changeState(new OverdrawnState());
            account.addHistory("State changed -> OverdrawnState");
            account.notify("Account entered OverdrawnState with balance $" + newBalance);

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
            account.changeState(new OverdrawnState());
            account.addHistory("Detected negative balance during month-end. Switched -> OverdrawnState");
            account.notify("Delegating month-end processing to OverdrawnState.");
            account.processMonth();
            return;
        }

        double interest = 0.0;
        if (account.getInterestPolicy() != null) {
            interest = account.getInterestPolicy().calculate(account.getBalance());
        }

        if (interest != 0.0) {
            account.setBalance(account.getBalance() + interest);
            account.addHistory("Monthly interest applied: $" + interest  
                    + " | Balance: $" + account.getBalance());
        } else {
            account.addHistory("Monthly processing: no interest applied.");
        }

        account.notify("Monthly summary: balance $" + account.getBalance());
    }

    /**
     * Called when attempting to unfreeze an already active account.
     *
     * @param account the account being processed
     */
    @Override
    public void unfreeze(Account account) {
        account.notify("Account is already active.");
        account.addHistory("Unfreeze called, but account is already in ActiveState.");
    }
}
