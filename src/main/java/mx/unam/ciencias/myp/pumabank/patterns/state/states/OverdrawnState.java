package mx.unam.ciencias.myp.pumabank.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.model.Account;

import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;

/**
 * Represents the overdrawn state of an account in the State design pattern.
 * <p>
 * In this state, the account has a negative balance. Withdrawals are blocked, and an overdraft fee is applied once per overdraft cycle.
 * Deposits or month-end processing can restore the balance to a non-negative value, automatically transitioning the account back to {@link ActiveState}.
 * </p>
 */
public class OverdrawnState implements AccountState {

    private static final double OVERDRAFT_FEE = 100.0;
    private boolean feeApplied = false;

    /**
     * Handles deposits made while the account is overdrawn.
     * <p>
     * Applies the overdraft fee if it has not yet been charged, then adds the deposit amount to the balance. If the balance becomes non-negative, the account transitions back to {@link ActiveState}.
     * 
     * </p>
     *
     * @param amount  the deposit amount
     * @param account the affected account
     */

    @Override
    public void deposit(double amount, Account account) {

        double bal = account.getBalance();

        if (!feeApplied) {
            bal -= OVERDRAFT_FEE;
            feeApplied = true;
            account.addHistory("Overdraft fee applied: $" + OVERDRAFT_FEE);
        }

        bal += amount;
        account.setBalance(bal);
        account.addHistory("Deposit while overdrawn: $" + amount + " | Balance: $" + bal);

        if (bal >= 0) {
            account.changeState(new ActiveState());
            account.addHistory("State changed -> ActiveState (balance recovered).");
            account.notify("Account recovered from overdraft. Balance $" + account.getBalance());

        } else {
            account.notify("Deposit received, account remains Overdrawn. Balance $" + bal);
        }

    }


    /**
     * Denies withdrawal attempts while the account is overdrawn.
     * @param amount  the attempted withdrawal amount
     * @param account the affected account
     */
    @Override
    public void withdraw(double amount, Account account) {
        account.addHistory("Withdrawal denied: account is overdrawn. Attempted: $" + amount);
        account.notify("Operation blocked: withdrawal on overdrawn account.");
    }

    /**
     * Processes monthly updates for overdrawn accounts.
     * <p>
     * Applies the overdraft fee (if not already applied), and if the balance becomes non-negative, applies interest and transitions to {@link ActiveState}.
     * </p>
     *
     * @param account the affected account
     */
    @Override
    public void processMonth(Account account) {
        double bal = account.getBalance();

        if (!feeApplied) {
            bal -= OVERDRAFT_FEE;
            feeApplied = true;
            account.setBalance(bal);
            account.addHistory("Month-end overdraft fee applied: $" + OVERDRAFT_FEE + " | Balance: $" + bal);
        }

        if (bal >= 0) {

            double interest = 0.0;

            if (account.getInterestPolicy() != null) {

                interest = account.getInterestPolicy().calculate(bal);

                if (interest != 0.0) {
                    bal += interest;
                    account.setBalance(bal);
                    account.addHistory("Monthly interest applied: $" + interest + " | Balance: $" + bal);

                } else {
                    account.addHistory("Monthly processing: no interest applied.");
                }
            } else {
                account.addHistory("Monthly processing: no interest policy configured.");
            }

            account.changeState(new ActiveState());
            account.addHistory("State changed -> ActiveState.");
            account.notify("Overdraft resolved at month-end. Balance $" + bal);
        } else {
            account.addHistory("Monthly processing: account remains overdrawn. No interests applied.");
            account.notify("Monthly summary: overdrawn. Balance $" + account.getBalance());
        }
    }

    /**
     * Denies unfreeze attempts, as an overdrawn account cannot be unfrozen.
     * @param account the affected account
     */
    @Override
    public void unfreeze(Account account) {
        account.addHistory("Unfreeze operation denied: account is overdrawn.");
        account.notify("Operation blocked: cannot unfreeze an overdrawn account.");
    }
}
