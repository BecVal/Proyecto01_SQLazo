package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.PremiumAlertsDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
class PremiumAlertsDecoratorTest {

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
            lastWithdrawAmount = amount;
            lastWithdrawPin = pin;
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


    @Nested
    @DisplayName("Deposit behavior")
    class DepositBehavior {

        @Test
        @DisplayName("Delegates deposit and sends a premium alert")
        void depositDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(base);
            deco.deposit(250.0, "1234");

            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(250.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin),() -> assertEquals(1, base.notifications.size(), "One premium alert expected"),() -> assertTrue(base.notifications.get(0).startsWith("PREMIUM ALERT: Deposit of $250.0")));
        }
    }

    @Nested
    @DisplayName("Withdraw behavior")
    class WithdrawBehavior {

        @Test
        @DisplayName("Delegates withdraw and sends a premium alert")
        void withdrawDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(base);
            deco.withdraw(80.0, "0000");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(80.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin),() -> assertEquals(1, base.notifications.size(), "One premium alert expected"),() -> assertTrue(base.notifications.get(0).startsWith("PREMIUM ALERT: Withdrawal of $80.0")));
        }
    }

    @Nested
    @DisplayName("Balance check behavior")
    class BalanceBehavior {

        @Test
        @DisplayName("Delegates checkBalance, returns value, and sends a premium alert with balance")
        void checkBalanceDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount();
            base.balanceToReturn = 4321.99;
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(base);
            double result = deco.checkBalance("pin-xyz");
            assertAll(() -> assertEquals("pin-xyz", base.lastCheckBalancePin),() -> assertEquals(4321.99, result, 1e-9),() -> assertEquals(1, base.notifications.size(), "One premium alert expected"),() -> assertTrue(base.notifications.get(0).startsWith("PREMIUM ALERT: Balance checked - $4321.99")));
        }
    }

    @Nested
    @DisplayName("Monthly processing")
    class MonthlyProcessing {
        @Test
        @DisplayName("Applies alerts fee, records history/notification, and delegates processMonth")
        void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount();
            PremiumAlertsDecorator deco = new PremiumAlertsDecorator(base);
            deco.processMonth();

            assertAll(() -> assertEquals(1, base.withdrawCalls, "One fee withdraw expected"),() -> assertEquals(25.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls, "Base processMonth should be invoked once"));
            assertAll(
                () -> assertTrue(base.history.stream().anyMatch(s -> s.equals("Premium alerts service fee applied: $25.0")),"History should contain the premium alerts fee record"),
                () -> assertTrue(base.notifications.stream().anyMatch(s -> s.equals("PREMIUM ALERT: Monthly service fee applied: $25.0")),"Notification should announce the monthly premium fee"));
        }
    }
}
