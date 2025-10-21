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
    public void deposit(double amount) {
        super.deposit(amount);
        notify("PREMIUM ALERT: Deposit of $" + amount + " completed");
    }

    /**
     * Withdraws the specified amount from the account and sends a premium alert.
     *
     * @param amount the amount to withdraw
     * @param pin    the PIN for authentication
     */
    @Override
    public void withdraw(double amount, String pin) {
        super.withdraw(amount, pin);
        notify("PREMIUM ALERT: Withdrawal of $" + amount + " completed");
    }

    /**
     * Processes the monthly operations, including applying the premium alerts fee.
     */
    @Override
    public void processMonth() {
        // Aplica cargo mensual por alertas premium
        super.withdraw(ALERTS_FEE, "0000");
        addHistory("Premium alerts service fee applied: $" + ALERTS_FEE);
        notify("PREMIUM ALERT: Monthly service fee applied: $" + ALERTS_FEE);
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
        notify("PREMIUM ALERT: Balance checked - $" + balance);
        return balance;
    }
}