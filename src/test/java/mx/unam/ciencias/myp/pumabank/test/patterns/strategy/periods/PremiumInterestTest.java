package mx.unam.ciencias.myp.pumabank.test.patterns.strategy.periods;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.PremiumInterest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PremiumInterestTest {

    private static final double DELTA = 1e-9;

    @Test
    void returnsZeroWhenBalanceIsZeroOrNegative() {
        PremiumInterest pi = new PremiumInterest(0.01, 1000.0, 5000.0, 0.005, 0.01);

        assertEquals(0.0, pi.calculate(0.0), DELTA);
        assertEquals(0.0, pi.calculate(-50.0), DELTA);
    }

    @Test
    void appliesOnlyBaseRateWhenBelowFirstThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 800.0;
        double expected = balance * 0.02;

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    @Test
    void appliesBonusRate1WhenBalanceReachesFirstThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 1000.0;
        double expected = balance * (0.02 + 0.005);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    @Test
    void appliesBonusRate1WhenBalanceIsBetweenFirstAndSecondThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 3000.0;
        double expected = balance * (0.02 + 0.005);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    @Test
    void appliesBonusRate2WhenBalanceReachesSecondThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 5000.0;
        double expected = balance * (0.02 + 0.01);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    @Test
    void appliesBonusRate2WhenBalanceExceedsSecondThreshold() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        double balance = 10_000.0;
        double expected = balance * (0.02 + 0.01);

        assertEquals(expected, pi.calculate(balance), DELTA);
    }

    @Test
    void returnsZeroForNaNBalanceBecauseComparisonsFailGracefully() {
        PremiumInterest pi = new PremiumInterest(0.02, 1000.0, 5000.0, 0.005, 0.01);

        assertEquals(0.0, pi.calculate(Double.NaN), DELTA);
    }
}
