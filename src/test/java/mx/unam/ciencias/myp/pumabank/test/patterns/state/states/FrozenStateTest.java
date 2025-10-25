package mx.unam.ciencias.myp.pumabank.test.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.ActiveState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.FrozenState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class FrozenStateTest {
    static class DummyFacade extends PumaBankFacade {
        @Override public void recordFeeCollection(double fee) {}
        @Override public void recordInterestPayment(double interest) {}
    }
    static class FakeAccount extends Account {
        private static final Client DUMMY_CLIENT = new Client("Diego", "ID-1");
        private static final AccountState NOOP_STATE = new AccountState() {
            @Override public void deposit(double amount, Account account) {}
            @Override public void withdraw(double amount, Account account) {}
            @Override public void processMonth(Account account) {}
            @Override public void unfreeze(Account account) {}
        };
        private static final InterestCalculation ZERO_INTEREST = balance -> 0.0;

        FakeAccount(double initialBalance) {
            super(DUMMY_CLIENT, 0.0, NOOP_STATE, ZERO_INTEREST, new DummyFacade());
            this.balance = initialBalance;
        }

        double balance;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();
        AccountState lastStateChange = null;
        int changeStateCalls = 0;

        @Override public double getBalance() { return balance; }
        @Override public void setBalance(double b) { this.balance = b; }
        @Override public void addHistory(String e) { history.add(e); }
        @Override public void notify(String m) { notifications.add(m); }
        @Override public void changeState(AccountState s) { changeStateCalls++; lastStateChange = s; }
    }

    @Nested
    @DisplayName("deposit() while frozen")
    class DepositFrozen {

        @Test
        @DisplayName("Denies and records history/notification; balance/state unchanged")
        void depositDenied() {
            FakeAccount acc = new FakeAccount(150.0);
            FrozenState state = new FrozenState();
            state.deposit(200.0, acc);
            assertAll(() -> assertTrue(acc.history.stream().anyMatch(s ->s.equals("Deposit denied: account is frozen. Attempted: $200.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s ->s.equals("The operation of deposit on frozen account was blocked."))),() -> assertEquals(150.0, acc.getBalance(), 1e-9),() -> assertEquals(0, acc.changeStateCalls),() -> assertNull(acc.lastStateChange));
        }

    }


    @Nested
    @DisplayName("withdraw() while frozen")
    class WithdrawFrozen {

        @Test
        @DisplayName("Denies and records history/notification; balance/state unchanged")
        void withdrawDenied() {

            FakeAccount acc = new FakeAccount(90.0);
            FrozenState state = new FrozenState();

            state.withdraw(40.0, acc);
            assertAll(() -> assertTrue(acc.history.stream().anyMatch(s ->s.equals("Withdrawal denied: account is frozen. Attempted: $40.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s ->s.equals("The operation of withdrawal on frozen account was blocked."))),() -> assertEquals(90.0, acc.getBalance(), 1e-9),() -> assertEquals(0, acc.changeStateCalls),() -> assertNull(acc.lastStateChange));

        }
    }

    @Nested
    @DisplayName("processMonth() while frozen")
    class ProcessMonthFrozen {

        @Test
        @DisplayName("Skips processing; records history and frozen summary with balance")
        void processMonthNoOp() {

            FakeAccount acc = new FakeAccount(250.0);
            FrozenState state = new FrozenState();
            state.processMonth(acc);

            assertAll(() -> assertTrue(acc.history.stream().anyMatch(s ->s.equals("Monthly processing: account frozen. No interests or fees applied."))),() -> assertTrue(acc.notifications.stream().anyMatch(s ->s.equals("Monthly summary: account FROZEN. Balance $250.0"))),() -> assertEquals(250.0, acc.getBalance(), 1e-9),() -> assertEquals(0, acc.changeStateCalls),() -> assertNull(acc.lastStateChange));

        }

    }

    @Nested
    @DisplayName("unfreeze()")
    class Unfreeze {

        @Test
        @DisplayName("Switches to ActiveState and records history/notification")
        void unfreezesToActive() {

            FakeAccount acc = new FakeAccount(300.0);
            FrozenState state = new FrozenState();

            state.unfreeze(acc);

            assertAll(() -> assertEquals(1, acc.changeStateCalls),() -> assertNotNull(acc.lastStateChange),() -> assertEquals(ActiveState.class.getSimpleName(), acc.lastStateChange.getClass().getSimpleName()),() -> assertTrue(acc.history.stream().anyMatch(s ->s.equals("Account unfrozen. State changed to active."))),() -> assertTrue(acc.notifications.stream().anyMatch(s ->s.equals("Account reactivated."))),() -> assertEquals(300.0, acc.getBalance(), 1e-9));

        }
    }
}
