package mx.unam.ciencias.myp.pumabank.patterns.state.states;

import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;

import mx.unam.ciencias.myp.pumabank.model.Account;


/**
 * Represents the closed state of an account in the State design pattern.
 * <p>
 * 
 * In this state, all operations such as deposits, withdrawals, and monthly processing are blocked. The account is permanently inactive and cannot be unfrozen or reactivated from within this class.
 * </p>
 */
public class ClosedState implements AccountState {

    /**
     * Denies deposit operations on a closed account.
     * @param amount  the amount to deposit
     * @param account the affected account
     */
    @Override
    public void deposit(double amount, Account account) {
        account.addHistory("Cannot deposit to a closed account.");

        account.notify("The operation of depositing to a closed account was blocked successfully.");
    }

    /**
     * 
     * Denies withdrawal operations on a closed account.
     *
     * @param amount  the amount to withdraw
     * @param account the affected account
     */

    @Override
    public void withdraw(double amount, Account account) {
        account.addHistory("Cannot withdraw from a closed account.");
        account.notify("The operation of withdrawing from a closed account was blocked successfully.");
    }

    /**
     * Skips any monthly processing, since closed accounts are inactive.
     * @param account the affected account
     */
    @Override
    public void processMonth(Account account) {

        account.addHistory("No monthly processing for closed accounts.");
        account.notify("The operation of processing month for a closed account was blocked successfully.");

    }

    /**
     * Denies unfreeze operations, as a closed account cannot be reactivated.
     * @param account the affected account
     */
    @Override
    public void unfreeze(Account account) {
        
        account.addHistory("Cannot unfreeze a closed account.");
        account.notify("The operation of unfreezing a closed account was blocked successfully.");
    }
}