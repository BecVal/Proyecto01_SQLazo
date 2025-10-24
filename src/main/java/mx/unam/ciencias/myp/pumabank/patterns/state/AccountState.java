package mx.unam.ciencias.myp.pumabank.patterns.state;

import mx.unam.ciencias.myp.pumabank.model.Account;

/**
 * Defines the contract for all account states in the State design pattern.
 * <p>
 * Each implementation represents a specific account condition and provides its own behavior for deposits, withdrawals, monthly processing, and unfreezing.
 * </p>
 */
public interface AccountState {

    /**
     * Handles a deposit operation according to the current account state.
     * @param amount  the deposit amount
     * @param account the account being modified
     */
    void deposit(double amount, Account account);

    /**
     * Handles a withdrawal operation according to the current account state.
     *
     * @param amount  the withdrawal amount
     * 
     * @param account the account being modified
     */
    void withdraw(double amount, Account account);

    /**
     * 
     * Executes month-end operations such as interest calculation or fees.
     *
     * @param account the account being processed
     */
    void processMonth(Account account);

    /**
     * Reactivates the account if applicable (used mainly by FrozenState).
     *
     * @param account the account being unfrozen
     */
    void unfreeze(Account account);
}