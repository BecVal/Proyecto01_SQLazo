package mx.unam.ciencias.myp.pumabank.test.patterns.strategy.periods;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.AnnualInterest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for annual interest payout behavior according to recorded monthly balances and current month.
 */
class AnnualInterestTest {

  private static final double DELTA = 1e-9;

  /**
   * Helper method to record multiple monthly balances.
   * 
   */
  private static void record(AnnualInterest ai, double... balances) {

    for (double b : balances) ai.recordMonthBalance(b);

  }

  /**
   * Ensures no annual interest is paid before month 12.
   */
  @Test
  void returnsZeroBeforeDecember() {
    AnnualInterest ai = new AnnualInterest(0.10, 1000.0);

    record(ai, 1200, 1300, 1400);
    ai.setCurrentMonth(11);

    assertEquals(0.0, ai.calculate(2000.0), DELTA);
  }

  /**
   * Ensures interest is paid in December when the average balance meets the threshold.
   */
  @Test

  void paysOutInDecemberWhenAverageMeetsThreshold() {
    AnnualInterest ai = new AnnualInterest(0.10, 1000.0);

    record(ai, 1200, 1500, 1800);
    ai.setCurrentMonth(12);

    double payout = ai.calculate(2000.0);
    assertEquals(2000.0 * 0.10, payout, DELTA);
  }

  /**
   * Ensures no payout occurs in December when the average balance is below the threshold.
   */
  @Test
  void returnsZeroInDecemberIfAverageBelowThreshold() {
    AnnualInterest ai = new AnnualInterest(0.10, 2000.0);
    record(ai, 1200, 1500, 1800);
    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(5000.0), DELTA);

  }


  /**
   * Ensures no payout occurs when the current balance is non-positive, even in December.
   */
  @Test
  void returnsZeroInDecemberIfCurrentBalanceIsNonPositive() {

    AnnualInterest ai = new AnnualInterest(0.10, 1000.0);
    record(ai, 3000, 3000, 3000);
    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(0.0), DELTA);
    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(-50.0), DELTA);

  }

  /**
   * Ensures zero, negative, and NaN balance records are ignored.
   * 
   */
  @Test
  void ignoresZeroNegativeAndNaNWhenRecording() {
    AnnualInterest ai = new AnnualInterest(0.10, 100.0);
    record(ai, -10, 0, Double.NaN, 200);
    ai.setCurrentMonth(12);
    assertEquals(1000.0 * 0.10, ai.calculate(1000.0), DELTA);

  }

  /**
   * Ensures no payout occurs in December when no monthly balances have been recorded.
   * 
   */
  @Test
  void doesNotPayoutIfNoMonthsRecordedEvenInDecember() {

    AnnualInterest ai = new AnnualInterest(0.10, 100.0);
    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(1000.0), DELTA);

  }

  /**
   * Ensures internal state resets after a December payout.
   */
  @Test
  void resetsInternalStateAfterDecemberPayout() {
    AnnualInterest ai = new AnnualInterest(0.10, 100.0);
    record(ai, 200, 200);
    ai.setCurrentMonth(12);
    assertEquals(2000.0 * 0.10, ai.calculate(2000.0), DELTA);

    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(2000.0), DELTA);

    record(ai, 500, 700, 900);
    ai.setCurrentMonth(12);
    assertEquals(3000.0 * 0.10, ai.calculate(3000.0), DELTA);

  }

  /**
   * Ensures setCurrentMonth only accepts values from 1 to 12.
   */
  @Test
  void setCurrentMonthRejectsOutOfRange() {

    AnnualInterest ai = new AnnualInterest(0.10, 1000.0);
    assertThrows(IllegalArgumentException.class, () -> ai.setCurrentMonth(0));
    assertThrows(IllegalArgumentException.class, () -> ai.setCurrentMonth(13));

    assertDoesNotThrow(() -> ai.setCurrentMonth(1));
    assertDoesNotThrow(() -> ai.setCurrentMonth(12));
  }

  /**
   * Ensures December payout is denied if current balance is NaN or negative.
   */
  @Test
  void decemberReturnsZeroIfAverageIsValidButBalanceIsNaNOrNegative() {
    AnnualInterest ai = new AnnualInterest(0.05, 100.0);

    record(ai, 200, 300);
    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(Double.NaN), DELTA);

    ai.setCurrentMonth(12);
    assertEquals(0.0, ai.calculate(-1.0), DELTA);
  }
  
}
