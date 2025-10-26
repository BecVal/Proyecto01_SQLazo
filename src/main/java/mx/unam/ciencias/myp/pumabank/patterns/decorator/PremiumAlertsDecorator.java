package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;

/**
 * Decorator class that adds premium alerts to an {@link IAccount}.
 * <p>
 * This class extends {@link AccountDecorator} and implements premium alerts for deposits, withdrawals, and balance checks.
 * It also applies a monthly fee for the premium alerts service.
 * </p>
 */
public class PremiumAlertsDecorator extends AccountDecorator {
    
    private static final double ALERTS_FEE = 25.0;

    /**
     * Constructs a {@code PremiumAlertsDecorator} that wraps the specified {@link IAccount}.
     *
     * @param decoratedAccount the {@link IAccount} instance to be decorated
     */
    public PremiumAlertsDecorator(IAccount decoratedAccount) {
        super(decoratedAccount);
    }

    /**
     * Deposits the specified amount into the account and sends a premium alert.
     *
     * @param amount the amount to deposit
     */
    @Override
    public void deposit(double amount, String pin) {
        double balanceBefore = getUnderlyingAccountBalance();
        super.deposit(amount, pin);
        if (getUnderlyingAccountBalance() > balanceBefore) {
            notify(String.format("PREMIUM_ALERT: Deposit of $%.2f completed", amount));
        }
    }

    /**
     * Withdraws the specified amount from the account and sends a premium alert.
     *
     * @param amount the amount to withdraw
     * @param pin    the PIN for authentication
     */
    @Override
    public void withdraw(double amount, String pin) {
        double balanceBefore = getUnderlyingAccountBalance();
        super.withdraw(amount, pin);
        if (getUnderlyingAccountBalance() < balanceBefore) {
            notify(String.format("PREMIUM_ALERT: Withdrawal of $%.2f completed", amount));
        }
    }

    /**
     * Processes the monthly operations, including applying the premium alerts fee.
     */
    @Override
    public void processMonth() {

        notify(String.format("SERVICE_FEE_PENDING: Premium Alerts - $%.2f", ALERTS_FEE));
        
        double balanceBefore = getUnderlyingAccountBalance();
        super.withdraw(ALERTS_FEE, "SYSTEM"); 
        double balanceAfter = getUnderlyingAccountBalance();

        if (balanceAfter < balanceBefore) { 
            recordFee(ALERTS_FEE);
            addHistory("Premium alerts service fee applied: $" + ALERTS_FEE);
            notify(String.format("SERVICE_FEE_APPLIED: Premium Alerts - $%.2f", ALERTS_FEE));
            notify("PREMIUM ALERT: Monthly service fee applied: $" + ALERTS_FEE);
        } else {
            addHistory("Premium alerts service fee could not be applied: $" + ALERTS_FEE + " (insufficient funds or overdrawn)");
            notify(String.format("SERVICE_FEE_DENIED: Premium Alerts - $%.2f | Reason: Insufficient funds or overdrawn", ALERTS_FEE));
        }
        
        super.processMonth();
    }

    /**
     * Checks the account balance and sends a premium alert.
     *
     * @param pin the PIN for authentication
     * @return the current balance
     */
    @Override
    public double checkBalance(String pin) {
        double balance = super.checkBalance(pin);
        if (balance != -1) { // Assuming -1 indicates a failed check from the proxy
            notify(String.format("PREMIUM_ALERT: Balance checked - $%.2f", balance));
        }
        return balance;
    }
}