package mx.unam.ciencias.myp.pumabank.patterns.strategy;

/**
 * Defines the contract for an interest calculation strategy.
 * <p>
 * This interface is part of the Strategy design pattern, allowing different
 * interest calculation algorithms (e.g., monthly, annual, premium) to be
 * used interchangeably by an {@code Account}.
 * </p>
 *
 * @author Cesar
 */
public interface InterestCalculation {
    /**
     * Calculates the interest amount for the current period based on the account's balance.
     * <p>
     * Each implementation is responsible for its own logic. For instance, an
     * annual interest strategy might return 0.0 for 11 out of 12 months, while a
     * monthly strategy would perform its calculation every time.
     * </p>
     *
     * @param balance The current account balance on which to calculate interest.
     * @return The calculated interest amount. This value should be non-negative.
     */
    double calculate(double balance);
}