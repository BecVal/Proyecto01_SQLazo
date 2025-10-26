package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.patterns.decorator.PremiumAlertsDecorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
/**
 * Tests for {@link PremiumAlertsDecorator}, verifying that operations delegate to the underlying account and premium alert notifications are triggered.
 */
class PremiumAlertsDecoratorTest {

    /**
     * 
     * Base account used to record delegated calls and store values.
     */

    static class RecordingAccount implements IAccount {
        double lastDepositAmount;
        String lastDepositPin;
        int depositCalls;
        double lastWithdrawAmount;
        String lastWithdrawPin;
        int withdrawCalls;
        int processMonthCalls;
        double balanceToReturn = 100.0;
        String lastCheckBalancePin;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();
        @Override
        public void deposit(double amount, String pin) {
            depositCalls++;
            lastDepositAmount = amount;
            lastDepositPin = pin;
        }

        @Override
        public void withdraw(double amount, String pin) {
            withdrawCalls++;
            lastWithdrawAmount = amount;lastWithdrawPin = pin;
        }

        @Override
        public double checkBalance(String pin) {
            lastCheckBalancePin = pin;
            return balanceToReturn;
        }

        @Override
        public void processMonth() {
            processMonthCalls++;
        }

        public void addHistory(String event) { history.add(event); }
        public void notify(String message) { notifications.add(message); }
        public void notifyObservers(String message) { notifications.add(message); }
    }

    /**
     * Decorator used to capture history and notifications instead of forwarding.
     * 
     */
    static class CaptureDecorator extends AccountDecorator {
        final List<String> capturedHistory = new ArrayList<>();

        final List<String> capturedNotifications = new ArrayList<>();
        public CaptureDecorator(IAccount decoratedAccount) {

            super(decoratedAccount);
        }

        @Override
        protected void addHistory(String event) {
            capturedHistory.add(event);
        }

        @Override
        protected void notify(String message) {

            capturedNotifications.add(message);
        }
    }

    @Nested
    @DisplayName("Deposit behavior")
    class DepositBehavior {

        /**
         * Confirms deposit delegates to underlying account and premium notifications occur.
         * 
         */
        @Test
        @DisplayName("Delegates deposit and sends a premium alert")
        void depositDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid);
            
            deco.deposit(250.0, "1234");

            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(250.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin));
        }
    }

    @Nested
    @DisplayName("Withdraw behavior")
    class WithdrawBehavior {

        /**
         * Confirms withdraw delegates and triggers notification.
         */
        @Test

        @DisplayName("Delegates withdraw and sends a premium alert")
        void withdrawDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();

            CaptureDecorator mid = new CaptureDecorator(base);
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid);
            deco.withdraw(80.0, "0000");

            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(80.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin));
        }
    }

    @Nested
    @DisplayName("Balance check behavior")
    class BalanceBehavior {

        /**
         * 
         * Ensures balance checks delegate and a premium notification is sent.
         */
        @Test
        @DisplayName("Delegates checkBalance, returns value, and sends a premium alert with balance")
        void checkBalanceDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            base.balanceToReturn = 4321.99;
            CaptureDecorator mid = new CaptureDecorator(base);
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid);

            double result = deco.checkBalance("pin-xyz");
            assertAll(() -> assertEquals("pin-xyz", base.lastCheckBalancePin),() -> assertEquals(4321.99, result, 1e-9),() -> assertEquals(1, mid.capturedNotifications.size()),() -> assertTrue(mid.capturedNotifications.get(0).startsWith("PREMIUM_ALERT: Balance checked - $4321.99")));
        }
    }

    @Nested
    @DisplayName("Monthly processing")
    class MonthlyProcessing {

        /**
         * Verifies monthly fee and notification are applied, and normal delegation continues.
         */
        @Test
        @DisplayName("Applies alerts fee, records history/notification, and delegates processMonth")
        void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid);
            deco.processMonth();

            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(25.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls));


        }
    }

}



