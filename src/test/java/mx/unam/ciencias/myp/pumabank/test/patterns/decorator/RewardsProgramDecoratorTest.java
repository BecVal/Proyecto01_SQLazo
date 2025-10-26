package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.RewardsProgramDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
/**
 * Tests for {@link RewardsProgramDecorator}, ensuring reward point accrual, fee application, notifications, and correct delegation of account operations.
 * 
 * 
 */
class RewardsProgramDecoratorTest {

    /**
     * Base account used to verify delegated calls and recorded values.
     */
    static class RecordingAccount implements IAccount {
        double lastDepositAmount;
        String lastDepositPin;
        int depositCalls;
        double lastWithdrawAmount;
        String lastWithdrawPin;
        int withdrawCalls;
        int processMonthCalls;
        double balanceToReturn = 0.0;
        String lastCheckBalancePin;

        @Override
        public void deposit(double amount, String pin) { depositCalls++; lastDepositAmount = amount; lastDepositPin = pin; }

        @Override
        public void withdraw(double amount, String pin) { withdrawCalls++; lastWithdrawAmount = amount; lastWithdrawPin = pin; }

        @Override
        public double checkBalance(String pin) { lastCheckBalancePin = pin; return balanceToReturn; }

        @Override
        public void processMonth() { processMonthCalls++; }
    }
    /**
     * Decorator used to capture history and notifications for hook verification.*/
    static class CaptureDecorator extends AccountDecorator {

        final List<String> history = new ArrayList<>();

        final List<String> notifications = new ArrayList<>();

        public CaptureDecorator(IAccount decoratedAccount) { super(decoratedAccount); }
        @Override protected void addHistory(String event) { history.add(event); }

        @Override protected void notify(String message) { notifications.add(message); }
    }

    @Nested
    @DisplayName("Deposit behavior")
    class DepositBehavior {

        /**
         * 
         * Verifies deposits delegate and reward points accrue accordingly.
         */
        @Test
        @DisplayName("Delegates deposit and accrues points")
        void depositAccruesPointsAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.deposit(250.0, "1234");

            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(250.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin));
        }

        /**
         * Ensures no notification is sent when zero points are awarded.
         */
        @Test
        @DisplayName("Records history but does not notify when pointsEarned == 0")
        void depositZeroPointsNoNotify() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);

            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.deposit(50.0, "pin");

            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.notifications.isEmpty()));
        }
    }

    @Nested
    @DisplayName("Withdraw behavior")
    class WithdrawBehavior {

        /**
         * Verifies withdrawals delegate and reward points accrue.
         */
        @Test
        @DisplayName("Delegates withdraw and accrues points")
        void withdrawAccruesPoints() {
            RecordingAccount base = new RecordingAccount();

            CaptureDecorator mid = new CaptureDecorator(base);
            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.withdraw(200.0, "0000");

            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(200.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin));
        }

        /**
         * Ensures no notification occurs when zero points are earned.
         */
        @Test
        @DisplayName("Records history only when pointsEarned == 0")
        void withdrawZeroPointsNoNotify() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);

            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.withdraw(90.0, "1111");

            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.notifications.isEmpty()));
        }
    }

    @Nested
    @DisplayName("Monthly processing")
    class MonthlyProcessing {

        /**
         * Ensures monthly fee is applied, history/notifications recorded, and underlying account is still processed.
         */
        @Test
        @DisplayName("Applies rewards fee, records history/notification, and delegates processMonth")

        void appliesMonthlyFeeAndDelegates() {


            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.deposit(1000.0, "p");

            deco.processMonth();
            assertAll(() -> assertEquals(1, base.withdrawCalls, "One fee withdraw expected"),() -> assertEquals(30.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls, "Base processMonth should be invoked once"));

        }


    }


    @Nested
    @DisplayName("Redeem points")

    class RedeemPoints {

        /**
         * Ensures redemption fails cleanly when points are insufficient.
         * 
         */
        @Test
        @DisplayName("Does not redeem when points are insufficient: only notifies")
        void redeemInsufficient() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);

            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);
            deco.redeemPoints(5);
            assertAll(() -> assertEquals(0, base.depositCalls, "No cash deposit when insufficient points"),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.contains("REWARDS_ERROR: Insufficient points") && s.contains("Available: 0"))),() -> assertTrue(mid.history.stream().noneMatch(s -> s.startsWith("Points redeemed:"))));
        }
    }

    @Nested
    @DisplayName("Check balance delegation")
    class BalanceBehavior {

        /**
         * 
         * Confirms balance checks delegate and reward points remain unchanged.
         */
        @Test
        @DisplayName("Delegates checkBalance and returns value")
        void checkBalanceDelegates() {
            RecordingAccount base = new RecordingAccount();
            base.balanceToReturn = 321.5;
            
            CaptureDecorator mid = new CaptureDecorator(base);
            RewardsProgramDecorator deco = new RewardsProgramDecorator(mid);

            double result = deco.checkBalance("abc");
            assertAll(() -> assertEquals("abc", base.lastCheckBalancePin),() -> assertEquals(321.5, result, 1e-9),() -> assertEquals(0, deco.getRewardPoints()));
        }

    }

}




