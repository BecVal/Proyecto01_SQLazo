package mx.unam.ciencias.myp.pumabank.test.patterns.decorator;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.RewardsProgramDecorator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import mx.unam.ciencias.myp.pumabank.model.IAccount;

class RewardsProgramDecoratorTest {

    static class RecordingAccount implements IAccount {
        double lastDepositAmount; String lastDepositPin; int depositCalls; double lastWithdrawAmount; String lastWithdrawPin; int withdrawCalls; int processMonthCalls; double balanceToReturn = 0.0; String lastCheckBalancePin;
        @Override public void deposit(double amount, String pin) { depositCalls++; lastDepositAmount = amount; lastDepositPin = pin; }
        @Override public void withdraw(double amount, String pin) { withdrawCalls++; lastWithdrawAmount = amount; lastWithdrawPin = pin; }
        @Override public double checkBalance(String pin) { lastCheckBalancePin = pin; return balanceToReturn; }
        @Override public void processMonth() { processMonthCalls++; }
    }

    static class CaptureDecorator extends mx.unam.ciencias.myp.pumabank.patterns.decorator.AccountDecorator {
        final List<String> history = new ArrayList<>(); final List<String> notifications = new ArrayList<>();
        public CaptureDecorator(IAccount decoratedAccount) { super(decoratedAccount); }
        @Override protected void addHistory(String event) { history.add(event); }
        @Override protected void notify(String message) { notifications.add(message); }
    }

    @Nested @DisplayName("Deposit behavior") class DepositBehavior {
        @Test @DisplayName("Delegates deposit and accrues points") void depositAccruesPointsAndNotifies() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.deposit(250.0, "1234");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(250.0, base.lastDepositAmount, 1e-9),() -> assertEquals("1234", base.lastDepositPin),() -> assertEquals(2, deco.getRewardPoints()),() -> assertTrue(mid.history.stream().anyMatch(s -> s.startsWith("Reward points earned: 2") && s.contains("Total: 2"))),() -> assertEquals(1, mid.notifications.size()),() -> assertTrue(mid.notifications.get(0).contains("REWARDS: Earned 2") && mid.notifications.get(0).contains("Total: 2 points")));
        }
        @Test @DisplayName("Records history but does not notify when pointsEarned == 0") void depositZeroPointsNoNotify() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.deposit(50.0, "pin");
            assertAll(() -> assertEquals(1, base.depositCalls),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.history.stream().anyMatch(s -> s.startsWith("Reward points earned: 0") && s.contains("Total: 0"))),() -> assertTrue(mid.notifications.isEmpty()));
        }
    }

    @Nested @DisplayName("Withdraw behavior") class WithdrawBehavior {
        @Test @DisplayName("Delegates withdraw and accrues points") void withdrawAccruesPoints() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.withdraw(200.0, "0000");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(200.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", base.lastWithdrawPin),() -> assertEquals(2, deco.getRewardPoints()),() -> assertTrue(mid.history.stream().anyMatch(s -> s.startsWith("Reward points earned: 2") && s.contains("Total: 2"))),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.contains("REWARDS: Earned 2") && s.contains("Total: 2 points"))));
        }
        @Test @DisplayName("Records history only when pointsEarned == 0") void withdrawZeroPointsNoNotify() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.withdraw(90.0, "1111");
            assertAll(() -> assertEquals(1, base.withdrawCalls),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.history.stream().anyMatch(s -> s.startsWith("Reward points earned: 0") && s.contains("Total: 0"))),() -> assertTrue(mid.notifications.isEmpty()));
        }
    }

    @Nested @DisplayName("Monthly processing") class MonthlyProcessing {
        @Test @DisplayName("Applies rewards fee, records history/notification, and delegates processMonth") void appliesMonthlyFeeAndDelegates() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.deposit(1000.0, "p"); deco.processMonth();
            assertAll(() -> assertEquals(1, base.withdrawCalls, "One fee withdraw expected"),() -> assertEquals(30.0, base.lastWithdrawAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastWithdrawPin),() -> assertEquals(1, base.processMonthCalls, "Base processMonth should be invoked once"));
            assertAll(() -> assertTrue(mid.history.stream().anyMatch(s -> s.equals("Rewards program fee applied: $30.0"))),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.startsWith("SERVICE_FEE_PENDING: Rewards Program") && s.contains("Current Points: 10"))),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.startsWith("SERVICE_FEE_APPLIED: Rewards Program") && s.contains("Points Balance: 10"))),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.startsWith("Rewards program: Monthly fee applied.") && s.contains("Current points: 10"))));
        }
    }

    @Nested @DisplayName("Redeem points") class RedeemPoints {
        @Test @DisplayName("Redeems when enough points: deposits cash, decreases points, records history and notification") void redeemSuccess() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.deposit(1200.0, "pin0"); assertEquals(12, deco.getRewardPoints()); deco.redeemPoints(7);
            assertAll(() -> assertEquals(2, base.depositCalls, "One original deposit + one cash redemption deposit"),() -> assertEquals(0.7, base.lastDepositAmount, 1e-9),() -> assertEquals("SYSTEM", base.lastDepositPin),() -> assertEquals(5, deco.getRewardPoints(), "12 - 7 points left"));
            assertAll(() -> assertTrue(mid.history.stream().anyMatch(s -> s.startsWith("Points redeemed: 7"))),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.contains("REWARDS_REDEMPTION") && s.contains("7 points redeemed"))));
        }
        @Test @DisplayName("Does not redeem when points are insufficient: only notifies") void redeemInsufficient() {
            RecordingAccount base = new RecordingAccount(); CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); deco.redeemPoints(5);
            assertAll(() -> assertEquals(0, base.depositCalls, "No cash deposit when insufficient points"),() -> assertEquals(0, deco.getRewardPoints()),() -> assertTrue(mid.notifications.stream().anyMatch(s -> s.contains("REWARDS_ERROR: Insufficient points") && s.contains("Available: 0"))),() -> assertTrue(mid.history.stream().noneMatch(s -> s.startsWith("Points redeemed:"))));
        }
    }

    @Nested @DisplayName("Check balance delegation") class BalanceBehavior {
        @Test @DisplayName("Delegates checkBalance and returns value") void checkBalanceDelegates() {
            RecordingAccount base = new RecordingAccount(); base.balanceToReturn = 321.5; CaptureDecorator mid = new CaptureDecorator(base); RewardsProgramDecorator deco = new RewardsProgramDecorator(mid); double result = deco.checkBalance("abc");
            assertAll(() -> assertEquals("abc", base.lastCheckBalancePin),() -> assertEquals(321.5, result, 1e-9),() -> assertEquals(0, deco.getRewardPoints()));
        }
    }
}
