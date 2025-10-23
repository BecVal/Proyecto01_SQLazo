package mx.unam.ciencias.myp.pumabank.patterns.strategy.periods;

import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;

/**
 * Represents a premium interest calculation strategy.
 * <p>
 * This strategy applies a base monthly interest rate and adds a tiered bonus
 * for accounts with higher balances. Unlike other plans, it does not require a
 * minimum balance to earn interest.
 * </p>
 * @author Cesar
 */
public class PremiumInterest implements InterestCalculation {
    private final double baseMonthlyRate;
    private final double bonusThreshold1;
    private final double bonusThreshold2;
    private final double bonusRate1;
    private final double bonusRate2;

    /**
     * Constructs a new {@code PremiumInterest} strategy with specified rates and thresholds.
     *
     * @param baseMonthlyRate The base interest rate applied monthly.
     * @param bonusThreshold1 The balance threshold to qualify for the first bonus rate.
     * @param bonusThreshold2 The balance threshold to qualify for the second, higher bonus rate.
     * @param bonusRate1      The additional interest rate for balances exceeding {@code bonusThreshold1}.
     * @param bonusRate2      The additional interest rate for balances exceeding {@code bonusThreshold2}.
     */
    public PremiumInterest(double baseMonthlyRate,
                           double bonusThreshold1, double bonusThreshold2,
                           double bonusRate1, double bonusRate2) {
        this.baseMonthlyRate = baseMonthlyRate;
        this.bonusThreshold1 = bonusThreshold1;
        this.bonusThreshold2 = bonusThreshold2;
        this.bonusRate1 = bonusRate1;
        this.bonusRate2 = bonusRate2;
    }

    /**
     * Calculates the total interest for the current month based on the account balance.
     * <p>
     * The final rate is the sum of the base rate and any applicable bonus rate based on the balance.
     * No interest is applied if the balance is zero or negative.
     * </p>
     *
     * @param balance The current account balance.
     * @return The calculated interest amount for the month.
     */
    @Override
    public double calculate(double balance) {
        if (balance <= 0) return 0.0;
        double rate = baseMonthlyRate;
        if (balance >= bonusThreshold2) {
            rate += bonusRate2;
        } else if (balance >= bonusThreshold1) {
            rate += bonusRate1;
        }
        return balance * rate;
    }
}
