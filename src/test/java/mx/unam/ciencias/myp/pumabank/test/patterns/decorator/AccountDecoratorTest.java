package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
class AccountDecoratorTest {
    static class RecordingAccount implements IAccount {
        double lastDepositAmount;
        String lastDepositPin;
        double lastWithdrawAmount;
        String lastWithdrawPin;
        boolean processMonthCalled;
        double balanceToReturn = 123.45;
        String lastCheckBalancePin;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();
        @Override
        public void deposit(double amount, String pin) {
            this.lastDepositAmount = amount;
            this.lastDepositPin = pin;
        }
        @Override
        public void withdraw(double amount, String pin) {
            this.lastWithdrawAmount = amount;
            this.lastWithdrawPin = pin;
        }
        @Override
        public double checkBalance(String pin) {
            this.lastCheckBalancePin = pin;
            return balanceToReturn;
        }
        @Override
        public void processMonth() {
            this.processMonthCalled = true;
        }
        public void addHistory(String event) {
            history.add(event);
        }
        public void notify(String message) {
            notifications.add(message);
        }
    }
    static class NoHookAccount implements IAccount {
        @Override public void deposit(double amount, String pin) {}
        @Override public void withdraw(double amount, String pin) {}
        @Override public double checkBalance(String pin) { return 0; }
        @Override public void processMonth() {}
    }
    static class ThrowingHookAccount implements IAccount {
        @Override public void deposit(double amount, String pin) {}
        @Override public void withdraw(double amount, String pin) {}
        @Override public double checkBalance(String pin) { return 0; }
        @Override public void processMonth() {}
        public void addHistory(String event) { throw new RuntimeException(); }
        public void notify(String message) { throw new RuntimeException(); }
    }
    static class CaptureDecorator extends AccountDecorator {
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
        int historySize() { return capturedHistory.size(); }
        String historyAt(int i) { return capturedHistory.get(i); }
        int notificationsSize() { return capturedNotifications.size(); }
        String notificationAt(int i) { return capturedNotifications.get(i); }
    }
    static class TestDecorator extends AccountDecorator {
        public TestDecorator(IAccount decorated) {
            super(decorated);
        }
        public void callAddHistory(String event) {
            super.addHistory(event);
        }
        public void callNotify(String message) {
            super.notify(message);
        }
    }

    @Nested
    @DisplayName("Delegation")
    class Delegation {
        @Test
        @DisplayName("deposit delegates amount and pin")
        void depositDelegates() {
            RecordingAccount base = new RecordingAccount();
            AccountDecorator deco = new TestDecorator(base);
            deco.deposit(50.0, "1234");
            assertAll(() -> assertEquals(50.0, base.lastDepositAmount),() -> assertEquals("1234", base.lastDepositPin));
        }

        @Test
        @DisplayName("withdraw delegates amount and pin")
        void withdrawDelegates() {
            RecordingAccount base = new RecordingAccount();
            AccountDecorator deco = new TestDecorator(base);
            deco.withdraw(20.0, "9999");
            assertAll(() -> assertEquals(20.0, base.lastWithdrawAmount),() -> assertEquals("9999", base.lastWithdrawPin));
        }

        @Test
        @DisplayName("checkBalance delegates pin and returns base value")
        void checkBalanceDelegates() {
            RecordingAccount base = new RecordingAccount();
            base.balanceToReturn = 777.77;
            AccountDecorator deco = new TestDecorator(base);
            double result = deco.checkBalance("abcd");
            assertAll(() -> assertEquals("abcd", base.lastCheckBalancePin),() -> assertEquals(777.77, result, 1e-9));
        }

        @Test
        @DisplayName("processMonth delegates")
        void processMonthDelegates() {
            RecordingAccount base = new RecordingAccount();
            AccountDecorator deco = new TestDecorator(base);
            deco.processMonth();
            assertTrue(base.processMonthCalled);
        }
    }

    @Nested
    @DisplayName("Hooks: addHistory/notify")
    class Hooks {
        @Test
        @DisplayName("addHistory invokes inner decorator hook when present")
        void addHistoryInvokesInnerDecorator() {
            CaptureDecorator inner = new CaptureDecorator(new NoHookAccount());
            TestDecorator outer = new TestDecorator(inner);
            outer.callAddHistory("event:x");
            assertAll(() -> assertEquals(1, inner.historySize()),() -> assertEquals("event:x", inner.historyAt(0)));
        }

        @Test
        @DisplayName("notify invokes inner decorator hook when present")
        void notifyInvokesInnerDecorator() {
            CaptureDecorator inner = new CaptureDecorator(new NoHookAccount());
            TestDecorator outer = new TestDecorator(inner);
            outer.callNotify("MODELADO Y P");
            assertAll(() -> assertEquals(1, inner.notificationsSize()),() -> assertEquals("MODELADO Y P", inner.notificationAt(0)));
        }
    }

    @Nested
    @DisplayName("Error swallowing in hooks")
    class ErrorSwallowing {
        @Test
        @DisplayName("addHistory no-ops when hook is missing")
        void addHistoryMissingMethodIsNoOp() {
            NoHookAccount base = new NoHookAccount();
            TestDecorator deco = new TestDecorator(base);
            assertDoesNotThrow(() -> deco.callAddHistory("ignored"));
        }

        @Test
        @DisplayName("notify no-ops when hook is missing")
        void notifyMissingMethodIsNoOp() {
            NoHookAccount base = new NoHookAccount();
            TestDecorator deco = new TestDecorator(base);
            assertDoesNotThrow(() -> deco.callNotify("ignored"));
        }

        @Test
        @DisplayName("addHistory does not propagate underlying runtime errors")
        void addHistoryUnderlyingThrowsIsSwallowed() {
            ThrowingHookAccount base = new ThrowingHookAccount();
            TestDecorator deco = new TestDecorator(base);
            assertDoesNotThrow(() -> deco.callAddHistory("ignored"));
        }
        @Test
        @DisplayName("notify does not propagate underlying runtime errors")
        void notifyUnderlyingThrowsIsSwallowed() {
            ThrowingHookAccount base = new ThrowingHookAccount();
            TestDecorator deco = new TestDecorator(base);
            assertDoesNotThrow(() -> deco.callNotify("ignored"));
        }
    }
}
