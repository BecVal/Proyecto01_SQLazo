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
        double lastDepositAmount; String lastDepositPin; int depositCalls; double lastWithdrawAmount; String lastWithdrawPin; int withdrawCalls; int processMonthCalls; double balanceToReturn = 100.0; String lastCheckBalancePin; final List<String> history = new ArrayList<>(); final List<String> notifications = new ArrayList<>();
        @Override public void deposit(double amount, String pin) { depositCalls++; lastDepositAmount = amount; lastDepositPin = pin; }
        @Override public void withdraw(double amount, String pin) { withdrawCalls++; lastWithdrawAmount = amount; lastWithdrawPin = pin; }
        @Override public double checkBalance(String pin) { lastCheckBalancePin = pin; return balanceToReturn; }
        @Override public void processMonth() { processMonthCalls++; }
        public void addHistory(String event) { history.add(event); }
        public void notify(String message) { notifications.add(message); }
        public void notifyObservers(String message) { notifications.add(message); }
    }

    static class CaptureDecorator extends mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator {
        final List<String> capturedHistory = new ArrayList<>(); final List<String> capturedNotifications = new ArrayList<>();
        public CaptureDecorator(IAccount decoratedAccount) { super(decoratedAccount); }
        @Override protected void addHistory(String event) { capturedHistory.add(event); }
        @Override protected void notify(String message) { capturedNotifications.add(message); }
    }

    @Nested @DisplayName("Deposit behavior") class DepositBehavior {
        @Test @DisplayName("Delegates deposit and sends a premium alert") void depositDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid); deco.deposit(250.0, "1234");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(250.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin),() -> assertEquals(1, mid.capturedNotifications.size()),() -> assertTrue(mid.capturedNotifications.get(0).startsWith("PREMIUM_ALERT: Deposit of $250.00 completed")));
        }
    }

    @Nested @DisplayName("Withdraw behavior") class WithdrawBehavior {
        @Test @DisplayName("Delegates withdraw and sends a premium alert") void withdrawDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid); deco.withdraw(80.0, "0000");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(80.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin),() -> assertEquals(1, mid.capturedNotifications.size()),() -> assertTrue(mid.capturedNotifications.get(0).startsWith("PREMIUM_ALERT: Withdrawal of $80.00 completed")));
        }
    }

    @Nested @DisplayName("Balance check behavior") class BalanceBehavior {
        @Test @DisplayName("Delegates checkBalance, returns value, and sends a premium alert with balance") void checkBalanceDelegatesAndNotifies() {
            RecordingAccount base = new RecordingAccount(); base.balanceToReturn = 4321.99; CaptureDecorator mid = new CaptureDecorator(base); PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid); double result = deco.checkBalance("pin-xyz");
            assertAll(() -> assertEquals("pin-xyz", base.lastCheckBalancePin),() -> assertEquals(4321.99, result, 1e-9),() -> assertEquals(1, mid.capturedNotifications.size()),() -> assertTrue(mid.capturedNotifications.get(0).startsWith("PREMIUM_ALERT: Balance checked - $4321.99")));
        }
    }

    @Nested @DisplayName("Monthly processing") class MonthlyProcessing {
        @Test @DisplayName("Applies alerts fee, records history/notification, and delegates processMonth") void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); PremiumAlertsDecorator deco = new PremiumAlertsDecorator(mid); deco.processMonth();
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(25.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls));
            assertAll(() -> assertTrue(mid.capturedHistory.stream().anyMatch(s -> s.equals("Premium alerts service fee applied: $25.0"))),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.equals("SERVICE_FEE_PENDING: Premium Alerts - $25.00"))),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.equals("SERVICE_FEE_APPLIED: Premium Alerts - $25.00"))),() -> assertTrue(mid.capturedNotifications.stream().anyMatch(s -> s.equals("PREMIUM ALERT: Monthly service fee applied: $25.0"))));
        }
    }
}
