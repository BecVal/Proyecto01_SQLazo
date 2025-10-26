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
        double balanceBefore = account.getBalance();
        double bal = account.getBalance();

        if (!feeApplied) {
            bal -= OVERDRAFT_FEE;
            feeApplied = true;
            account.addHistory("Overdraft fee applied: $" + OVERDRAFT_FEE);
        
            account.recordFee(OVERDRAFT_FEE); 
            account.notify(String.format("OVERDRAFT_FEE: $%.2f | Balance Before Fee: $%.2f | Balance After Fee: $%.2f", 
                OVERDRAFT_FEE, balanceBefore, bal));
        }

        bal += amount;
        account.setBalance(bal);
        account.addHistory("Deposit while overdrawn: $" + amount + " | Balance: $" + bal);

        account.notify(String.format("DEPOSIT_OVERDRAWN: $%.2f | Total Balance After: $%.2f", 
            amount, bal));

        
        if (bal >= 0) {
            String previousState = "OverdrawnState";
            account.changeState(new ActiveState());
            account.addHistory("State changed -> ActiveState (balance recovered).");
            
            account.notify(String.format("STATE_CHANGE: %s -> ActiveState | Reason: Balance recovered to non-negative", 
                previousState));
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
        account.notify(String.format("WITHDRAWAL_DENIED: $%.2f | Reason: Account overdrawn | Current Balance: $%.2f", 
            amount, account.getBalance()));
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
        double balanceBefore = account.getBalance();
        double bal = account.getBalance();

        if (!feeApplied) {
            bal -= OVERDRAFT_FEE;
            feeApplied = true;
            account.setBalance(bal);
            account.addHistory("Month-end overdraft fee applied: $" + OVERDRAFT_FEE + " | Balance: $" + bal);
            
            account.recordFee(OVERDRAFT_FEE);

            account.notify(String.format("MONTHLY_OVERDRAFT_FEE: $%.2f | Balance Before: $%.2f | Balance After: $%.2f", 
                OVERDRAFT_FEE, balanceBefore, bal));
        }

        if (bal >= 0) {
            double interest = 0.0;
            double interestBalanceBefore = account.getBalance();

            if (account.getInterestPolicy() != null) {
                interest = account.getInterestPolicy().calculate(bal);

                if (interest != 0.0) {
                    bal += interest;
                    account.setBalance(bal);
                    account.addHistory("Monthly interest applied: $" + interest + " | Balance: $" + bal);
                    
                    account.notify(String.format("INTEREST_APPLIED: $%.2f | Balance Before: $%.2f | Balance After: $%.2f", 
                        interest, interestBalanceBefore, bal));
                } else {
                    account.addHistory("Monthly processing: no interest applied.");
                    account.notify("MONTHLY_PROCESSING: No interest applied");
                }
            } else {
                account.addHistory("Monthly processing: no interest policy configured.");
                account.notify("MONTHLY_PROCESSING: No interest policy configured");
            }

            String previousState = "OverdrawnState";
            account.changeState(new ActiveState());
            account.addHistory("State changed -> ActiveState.");
            
            account.notify(String.format("STATE_CHANGE: %s -> ActiveState | Reason: Month-end balance recovery", 
                previousState));
        } else {
            account.addHistory("Monthly processing: account remains overdrawn. No interests applied.");
            account.notify(String.format("MONTHLY_SUMMARY: Account remains overdrawn | Balance: $%.2f", 
                account.getBalance()));
        }
    }

    /**
     * Denies unfreeze attempts, as an overdrawn account cannot be unfrozen.
     * @param account the affected account
     */
    @Override
    public void unfreeze(Account account) {
        account.addHistory("Unfreeze operation denied: account is overdrawn.");
        account.notify("UNFREEZE_DENIED: Cannot unfreeze an overdrawn account");
    }
}