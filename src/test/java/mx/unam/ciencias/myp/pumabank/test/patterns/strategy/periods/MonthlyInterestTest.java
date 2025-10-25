package mx.unam.ciencias.myp.pumabank.test.patterns.strategy.periods;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.MonthlyInterest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MonthlyInterestTest {

  private static final double DELTA = 1e-9;

  @Test
  void returnsZeroWhenBalanceIsBelowMinimum() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 1000.0);
    assertEquals(0.0, mi.calculate(999.99), DELTA);
  }

  @Test
  void returnsZeroWhenBalanceIsZero() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 1.0);
    assertEquals(0.0, mi.calculate(0.0), DELTA);
  }

  @Test
  void returnsZeroWhenBalanceIsNegative() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 1.0);
    assertEquals(0.0, mi.calculate(-50.0), DELTA);
  }

  @Test
  void paysWhenBalanceEqualsMinimum() {
    MonthlyInterest mi = new MonthlyInterest(0.03, 1000.0);
    assertEquals(1000.0 * 0.03, mi.calculate(1000.0), DELTA);
  }

  @Test
  void paysWhenBalanceIsAboveMinimum() {
    MonthlyInterest mi = new MonthlyInterest(0.015, 500.0);
    assertEquals(2000.0 * 0.015, mi.calculate(2000.0), DELTA);
  }

  @Test
  void handlesVerySmallRatesWithDelta() {
    MonthlyInterest mi = new MonthlyInterest(1e-6, 1.0);
    assertEquals(10_000.0 * 1e-6, mi.calculate(10_000.0), DELTA);
  }

  @Test
  void returnsZeroForNaNBalanceBecauseComparisonsFail() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 100.0);
    assertEquals(0.0, mi.calculate(Double.NaN), DELTA);
  }

  @Test
  void minimumZeroStillRequiresPositiveBalance() {
    MonthlyInterest mi = new MonthlyInterest(0.05, 0.0);
    assertEquals(200.0 * 0.05, mi.calculate(200.0), DELTA);
    assertEquals(0.0, mi.calculate(0.0), DELTA);
    assertEquals(0.0, mi.calculate(-1.0), DELTA);
  }
}
