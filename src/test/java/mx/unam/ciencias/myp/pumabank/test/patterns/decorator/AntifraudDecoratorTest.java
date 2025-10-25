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

    static class NoHookAccount implements IAccount {
        @Override public void deposit(double amount, String pin) {}
        @Override public void withdraw(double amount, String pin) {}
        @Override public double checkBalance(String pin) { return 0; }
        @Override public void processMonth() {}
    }

    static class CaptureDecorator extends mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator {
        final List<String> capturedHistory = new ArrayList<>();
        final List<String> capturedNotifications = new ArrayList<>();
        public CaptureDecorator(IAccount decorated) {
            super(decorated);
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
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertTrue(base.history.isEmpty()),() -> assertTrue(base.notifications.isEmpty()));
        }

        @Test
        @DisplayName("Triggers hooks when amount > 10000")
        void triggersHooksAboveThreshold() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            AntiFraudDecorator deco = new AntiFraudDecorator(mid);
            double amount = 15000.0;
            deco.deposit(amount, "9999");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(1, mid.capturedHistory.size()),() -> assertTrue(mid.capturedHistory.get(0).contains("Suspicious")&& mid.capturedHistory.get(0).toLowerCase().contains("deposit")&& mid.capturedHistory.get(0).contains(String.valueOf(amount))),() -> assertEquals(1, mid.capturedNotifications.size()),() -> assertTrue(mid.capturedNotifications.get(0).contains("FRAUD_ALERT")&& mid.capturedNotifications.get(0).toLowerCase().contains("deposit")&& mid.capturedNotifications.get(0).contains(String.valueOf(amount))));
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
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(200.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin));
        }

        @Test
        @DisplayName("No hooks when amount <= 10000")
        void noHooksAtOrBelowThreshold() {
            RecordingAccount base = new RecordingAccount();
            AntiFraudDecorator deco = new AntiFraudDecorator(base);
            deco.withdraw(10000.0, "pin");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertTrue(base.history.isEmpty()),() -> assertTrue(base.notifications.isEmpty()));
        }
        @Test
        @DisplayName("Triggers hooks when amount > 10000")
        void triggersHooksAboveThreshold() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            AntiFraudDecorator deco = new AntiFraudDecorator(mid);
            double amount = 20000.0;
            deco.withdraw(amount, "1111");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertTrue(mid.capturedHistory.get(0).contains("Suspicious")&& mid.capturedHistory.get(0).toLowerCase().contains("withdraw")&& mid.capturedHistory.get(0).contains(String.valueOf(amount))),() -> assertTrue(mid.capturedNotifications.get(0).contains("FRAUD_ALERT")&& mid.capturedNotifications.get(0).toLowerCase().contains("withdraw")&& mid.capturedNotifications.get(0).contains(String.valueOf(amount))));
        }
    }

    @Nested
    @DisplayName("Monthly processing")
    class MonthlyProcessing {
        @Test
        @DisplayName("Applies anti-fraud fee, records history/notification, and delegates processMonth")
        void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount();
            CaptureDecorator mid = new CaptureDecorator(base);
            AntiFraudDecorator deco = new AntiFraudDecorator(mid);
            deco.processMonth();
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(50.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls));
            assertAll(() -> assertTrue(mid.capturedHistory.get(0).startsWith("Anti-fraud service fee applied: $50.0")),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.startsWith("SERVICE_FEE_PENDING: Anti-Fraud Protection") && s.contains("$50.0"))),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.startsWith("SERVICE_FEE_APPLIED: Anti-Fraud Protection") && s.contains("$50.0"))),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.contains("Anti-fraud protection active") && s.contains("$50.0"))));
        }
    }
}
