package mx.unam.ciencias.myp.pumabank.patterns.strategy.periods;

import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;

/**
 * Represents a simple monthly interest calculation strategy.
 * <p>
 * This strategy applies a fixed monthly interest rate, but only if the account
 * balance is greater than or equal to a specified minimum balance.
 * </p>
 * @author Cesar
 */
public class MonthlyInterest implements InterestCalculation {
    private final double monthlyRate;
    private final double minimumBalance;

    /**
     * Constructs a new {@code MonthlyInterest} strategy.
     *
     * @param monthlyRate    The fixed interest rate to apply each month (e.g., 0.01 for 1%).
     * @param minimumBalance The minimum balance required for interest to be applied.
     */
    public MonthlyInterest(double monthlyRate, double minimumBalance) {
        this.monthlyRate = monthlyRate;
        this.minimumBalance = minimumBalance;
    }

    /**
     * Calculates the interest for the current month.
     * <p>
     * Interest is calculated only if the current balance is positive and meets or
     * exceeds the minimum required balance. Otherwise, it returns 0.0.
     * </p>
     *
     * @param balance The current account balance.
     * @return The calculated interest amount for the month, or 0.0 if the conditions are not met.
     */
    @Override
    public double calculate(double balance) {
        if (balance >= minimumBalance && balance > 0) {
            return balance * monthlyRate;
        }
        return 0.0;
    }
}
