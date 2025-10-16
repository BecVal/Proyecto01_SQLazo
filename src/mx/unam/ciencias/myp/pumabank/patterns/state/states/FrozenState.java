package mx.unam.ciencias.myp.pumabank.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.model.Account;

/**
 * Represents the frozen state of an account in the State design pattern.
 * <p>
 * In this state, all monetary operations are blocked due to suspicious or restricted activity. The account can only be reactivated manually through the {@link #unfreeze(Account)} method, which transitions it back to {@link ActiveState}.
 * </p>
 */

public class FrozenState implements AccountState {

    /**
     * Denies deposit attempts while the account is frozen.
     * @param amount  the attempted deposit amount
     * @param account the affected account
     */
    @Override
    public void deposit(double amount, Account account) {

        account.addHistory("Deposit denied: account is frozen. Attempted: $" + amount);
        account.notify("The operation of deposit on frozen account was blocked.");
    }

    /**
     * Denies withdrawal attempts while the account is frozen.
     * @param amount  the attempted withdrawal amount
     * @param account the affected account
     * 
     */
    @Override
    public void withdraw(double amount, Account account) {
        account.addHistory("Withdrawal denied: account is frozen. Attempted: $" + amount);
        account.notify("The operation of withdrawal on frozen account was blocked.");
    }

    /**
     * Disables monthly processing while the account is frozen.
     *
     * @param account the affected account
     * 
     */
    @Override
    public void processMonth(Account account) {
        account.addHistory("Monthly processing: account frozen. No interests or fees applied.");
        account.notify("Monthly summary: account FROZEN. Balance $" + account.getBalance());

    }

    /**
     * Reactivates the account by switching its state to {@link ActiveState}.
     * @param account the account being unfrozen
     */
    @Override
    public void unfreeze(Account account) {
        account.changeState(new ActiveState());
        account.addHistory("Account unfrozen. State changed to active.");
        account.notify("Account reactivated.");
    }
}
