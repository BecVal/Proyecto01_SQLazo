package mx.unam.ciencias.myp.pumabank.test.patterns.strategy.periods;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.MonthlyInterest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;



/**
 * Tests for {@link MonthlyInterest}, validating minimum balance thresholds and interest calculations.
 */
class MonthlyInterestTest {


  private static final double DELTA = 1e-9;
  /**
   * Ensures interest is zero when balance is below the minimum required threshold.
   */
  @Test
  void returnsZeroWhenBalanceIsBelowMinimum() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 1000.0);

    assertEquals(0.0, mi.calculate(999.99), DELTA);
  }

  /**
   * Ensures zero interest when balance is exactly zero.
   * 
   */
  @Test
  void returnsZeroWhenBalanceIsZero() {
    MonthlyInterest mi = new MonthlyInterest(0.02, 1.0);

    assertEquals(0.0, mi.calculate(0.0), DELTA);
  }

  /**
   * Ensures zero interest when balance is negative.
   */
  @Test
  void returnsZeroWhenBalanceIsNegative() {

    MonthlyInterest mi = new MonthlyInterest(0.02, 1.0);
    assertEquals(0.0, mi.calculate(-50.0), DELTA);
  }


  /**
   * 
   * Ensures interest applies when balance equals the minimum threshold.
   */
  @Test
  void paysWhenBalanceEqualsMinimum() {
    MonthlyInterest mi = new MonthlyInterest(0.03, 1000.0);

    assertEquals(1000.0 * 0.03, mi.calculate(1000.0), DELTA);
  }

  /**
   * Ensures interest applies normally when balance exceeds the minimum threshold.
   */
  @Test
  void paysWhenBalanceIsAboveMinimum() {

    MonthlyInterest mi = new MonthlyInterest(0.015, 500.0);
    assertEquals(2000.0 * 0.015, mi.calculate(2000.0), DELTA);
  }
  /**
   * Ensures precision is respected with extremely small rates.*/
  @Test
  void handlesVerySmallRatesWithDelta() {

    MonthlyInterest mi = new MonthlyInterest(1e-6, 1.0);

    assertEquals(10_000.0 * 1e-6, mi.calculate(10_000.0), DELTA);
  }

  /**
   * Ensures NaN balance results in zero interest.
   */
  @Test
  void returnsZeroForNaNBalanceBecauseComparisonsFail() {

    MonthlyInterest mi = new MonthlyInterest(0.02, 100.0);
    assertEquals(0.0, mi.calculate(Double.NaN), DELTA);
  }

  /**
   * 
   * Ensures minimum of zero still requires a strictly positive balance to earn interest.
   */
  @Test
  void minimumZeroStillRequiresPositiveBalance() {
    MonthlyInterest mi = new MonthlyInterest(0.05, 0.0);
    assertEquals(200.0 * 0.05, mi.calculate(200.0), DELTA);
    assertEquals(0.0, mi.calculate(0.0), DELTA);

    assertEquals(0.0, mi.calculate(-1.0), DELTA);
  }

}


