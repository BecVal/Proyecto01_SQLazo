package mx.unam.ciencias.myp.pumabank.test.patterns.strategy.periods;

import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.PremiumInterest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PremiumInterest}, verifying tiered interest application based on balance thresholds.
 */
class PremiumInterestTest {

    private static final double DELTA = 1e-9;
    /**
     * Ensures interest is zero when balance is zero or negative.*/
    @Test
    void returnsZeroWhenBalanceIsZeroOrNegative() {

        PremiumInterest pi = new PremiumInterest(0.01, 1000.0, 5000.0, 0.005, 0.01);

        assertEquals(0.0, pi.calculate(0.0), DELTA);
        assertEquals(0.0, pi.calculate(-50.0), DELTA);

    }

    /**
     * Applies only the base rate when balance is below the first tier threshold.
     */
    @Test

    void appliesOnlyBaseRateWhenBelowFirstThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 800.0;
        double expected = balance * 0.02;

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    /**
     * Applies base rate plus the first tier bonus when balance hits the first threshold.
     * 
     */
    @Test
    void appliesBonusRate1WhenBalanceReachesFirstThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 1000.0;
        double expected = balance * (0.02 + 0.005);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }


    /**
     * Applies base rate plus first tier bonus when balance lies between first and second thresholds.
     */
    @Test
    void appliesBonusRate1WhenBalanceIsBetweenFirstAndSecondThreshold() {

        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 3000.0;
        double expected = balance * (0.02 + 0.005);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    /**
     * Applies base rate plus second tier bonus when balance hits the second threshold.
     * 
     */
    @Test
    void appliesBonusRate2WhenBalanceReachesSecondThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 5000.0;
        double expected = balance * (0.02 + 0.01);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }
    /**
     * 
     * Applies highest bonus tier when balance exceeds second threshold.
     */
    @Test
    void appliesBonusRate2WhenBalanceExceedsSecondThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 10_000.0;
        double expected = balance * (0.02 + 0.01);

        assertEquals(expected, pi.calculate(balance), DELTA);

    }

}


