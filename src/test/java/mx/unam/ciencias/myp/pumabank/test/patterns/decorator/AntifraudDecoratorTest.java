package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.AntiFraudDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;

class AntiFraudDecoratorTest {

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
        @DisplayName("Delegates deposit with amount and pin")
        void delegatesDeposit() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);

            deco.deposit(500.0, "1234");

            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(500.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin));
        }

        @Test
        @DisplayName("No hooks when amount <= 10000")
        void noHooksAtOrBelowThreshold() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            deco.deposit(10000.0, "pin");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertTrue(base.history.isEmpty(), "History should remain empty"),() -> assertTrue(base.notifications.isEmpty(), "Notifications should remain empty"));
        }

        @Test
        @DisplayName("Triggers hooks when amount > 10000")
        void triggersHooksAboveThreshold() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            double amount = 15000.0;
            deco.deposit(amount, "9999");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(1, base.history.size(), "Expect one suspicious entry when deposit > 10000"),() -> assertTrue(base.history.stream().anyMatch(s -> s.contains("Suspicious") && s.toLowerCase().contains("deposit") && s.contains(String.valueOf(amount)))),() -> assertEquals(1, base.notifications.size(), "One alert notification expected"),() -> assertTrue(base.notifications.get(0).contains("ALERT") && base.notifications.get(0).toLowerCase().contains("deposit") && base.notifications.get(0).contains(String.valueOf(amount))));
        }
    }

    @Nested
    @DisplayName("Withdraw behavior")
    class WithdrawBehavior {

        @Test
        @DisplayName("Delegates withdraw with amount and pin")
        void delegatesWithdraw() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            deco.withdraw(200.0, "0000");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(200.0, base.lastWithdrawAmount, 1e-9), () -> assertEquals("0000", base.lastWithdrawPin));
        }

        @Test
        @DisplayName("No hooks when amount <= 10000")
        void noHooksAtOrBelowThreshold() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            deco.withdraw(10000.0, "pin");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertTrue(base.history.isEmpty()), () -> assertTrue(base.notifications.isEmpty()));
        }

        @Test
        @DisplayName("Triggers hooks when amount > 10000")
        void triggersHooksAboveThreshold() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            double amount = 20000.0;
            deco.withdraw(amount, "1111");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertTrue(base.history.stream().anyMatch(s -> s.contains("Suspicious") && s.toLowerCase().contains("withdraw") && s.contains(String.valueOf(amount)))), () -> assertTrue(base.notifications.stream().anyMatch(s -> s.contains("ALERT") && s.toLowerCase().contains("withdraw") && s.contains(String.valueOf(amount)))));
        }
    }

    @Nested
    @DisplayName("Monthly processing")
    class MonthlyProcessing {
        @Test
        @DisplayName("Applies anti-fraud fee, records history/notification, and delegates processMonth")
        void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            deco.processMonth();
            assertAll(() -> assertEquals(1, base.withdrawCalls, "One fee withdraw expected"),() -> assertEquals(50.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls, "Base processMonth should be invoked once"));
            assertAll(() -> assertTrue(base.history.stream().anyMatch(s -> s.startsWith("Anti-fraud service fee applied: $50.0")),"History should record the monthly fee"),() -> assertTrue(base.notifications.stream().anyMatch(s -> s.contains("Anti-fraud protection active") && s.contains("$50.0")),"Notification should announce protection and fee"));
        }
    }
}
