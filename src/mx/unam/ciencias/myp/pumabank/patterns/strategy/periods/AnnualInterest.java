package mx.unam.ciencias.myp.pumabank.patterns.strategy.periods;

import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;

/**
 * Represents a stateful annual interest calculation strategy.
 * <p>
 * This strategy calculates interest once per year. It accumulates monthly balances
 * and only pays out interest in the 12th month, provided that the average
 * annual balance meets a specified threshold.
 * </p>
 * <p>
 * <strong>Important:</strong> This class requires external management. The owning
 * {@code Account} must call {@link #recordMonthBalance(double)} and
 * {@link #setCurrentMonth(int)} every month before invoking {@link #calculate(double)}
 * to ensure correct behavior. This design avoids changing the signature of the
 * {@code InterestCalculation} interface.
 * </p>
 *
 * @author Cesar
 */
public class AnnualInterest implements InterestCalculation {
    private final double annualRate;
    private final double thresholdAverage;
    private double runningTotal = 0.0;
    private int monthsRecorded = 0;
    private int currentMonth = 1;

    /**
     * Constructs a new {@code AnnualInterest} strategy.
     *
     * @param annualRate       The interest rate to apply if conditions are met.
     * @param thresholdAverage The minimum average annual balance required to earn interest.
     */
    public AnnualInterest(double annualRate, double thresholdAverage) {
        this.annualRate = annualRate;
        this.thresholdAverage = thresholdAverage;
    }

    /**
     * Records the balance for the current month to be used in the annual average calculation.
     * <p>
     * This method must be called by the managing account each month with the month-end balance.
     * </p>
     *
     * @param balance The account balance for the current month.
     */
    public void recordMonthBalance(double balance) {
        if (!Double.isNaN(balance) && balance > 0) {
            runningTotal += balance;
            monthsRecorded++;
        }
    }

    /**
     * Sets the current month of the year (1-12).
     * <p>
     * The managing account is responsible for updating this value monthly.
     * </p>
     *
     * @param month The current month, from 1 (January) to 12 (December).
     * @throws IllegalArgumentException if the month is not between 1 and 12.
     */
    public void setCurrentMonth(int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("month must be 1..12");
        this.currentMonth = month;
    }

    /**
     * Calculates the annual interest, but only pays out in the 12th month.
     * <p>
     * For months 1 through 11, this method returns 0.0. In the 12th month, it calculates
     * the average balance for the year. If the average meets the threshold, it applies
     * the annual interest rate to the current balance. After the calculation, it resets
     * its internal state for the next year.
     * </p>
     *
     * @param balance The current account balance.
     * @return The calculated interest amount if it is the 12th month and conditions are met; otherwise, 0.0.
     */
    @Override
    public double calculate(double balance) {
        if (currentMonth == 12 && monthsRecorded > 0) {
            double average = runningTotal / monthsRecorded;
            double interest = 0.0;
            if (average >= thresholdAverage && balance > 0) {
                interest = balance * annualRate;
            }
            runningTotal = 0.0;
            monthsRecorded = 0;
            return interest;
        }
        return 0.0;
    }
}
