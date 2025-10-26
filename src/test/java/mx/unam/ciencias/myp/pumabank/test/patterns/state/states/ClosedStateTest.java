

package mx.unam.ciencias.myp.pumabank.test.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.ClosedState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ClosedState} ensuring all operations are denied and logged without side effects.
 * 
 * 
 */
class ClosedStateTest {
    /** Facade stub to avoid recording side effects in tests. */
    static class DummyFacade extends PumaBankFacade {

        @Override public void recordFeeCollection(double fee) {}

        @Override public void recordInterestPayment(double interest) {}

    }

    /** Fake account capturing calls, counters, and messages for verification. */
    static class FakeAccount extends Account {
        private static final Client DUMMY_CLIENT = new Client("Bruno", "ID-1");

        private static final AccountState NOOP_STATE = new AccountState() {
            @Override public void deposit(double amount, Account account) {}

            @Override public void withdraw(double amount, Account account) {}

            @Override public void processMonth(Account account) {}
            @Override public void unfreeze(Account account) {}
        };

        private static final InterestCalculation ZERO_INTEREST = balance -> 0.0;

        FakeAccount(double initial) {

            super(DUMMY_CLIENT, 0.0, NOOP_STATE, ZERO_INTEREST, new DummyFacade());
            this.balance = initial;
        }

        double balance;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();

        AccountState lastStateChange = null;
        int setBalanceCalls = 0;
        int changeStateCalls = 0;
        int processMonthCalls = 0;
        int withdrawCalls = 0;
        int depositCalls = 0;

        double lastWithdrawAmount = 0.0;
        String lastWithdrawPin = null;
        double lastDepositAmount = 0.0;
        String lastDepositPin = null;
        @Override public double getBalance() { return balance; }
        @Override public void setBalance(double b) { setBalanceCalls++; balance = b; }
        @Override public void addHistory(String e) { history.add(e); }
        @Override public void notify(String m) { notifications.add(m); }

        @Override public void changeState(AccountState s) { changeStateCalls++; lastStateChange = s; }
        @Override public void processMonth() { processMonthCalls++; }
        @Override public void withdraw(double amount, String pin) { withdrawCalls++; lastWithdrawAmount = amount; lastWithdrawPin = pin; }
        @Override public void deposit(double amount, String pin) { depositCalls++; lastDepositAmount = amount; lastDepositPin = pin; }

    }


    /** Harness exposing the fake account and quick accessors for assertions.
    */
    static class AccountHarness {

        final FakeAccount account = new FakeAccount(100.0);
        final List<String> history = account.history;

        final List<String> notifications = account.notifications;

        AccountState lastStateChange() { return account.lastStateChange; }

        double balance() { return account.balance; }
    }

    @Nested
    @DisplayName("deposit()")
    class DepositTests {

        /**
         * 
         * Denies deposits and logs; balance and state remain unchanged.
         * 
         */
        @Test
        @DisplayName("Denies deposits: records history and notifies; no balance/state changes")
        void depositDenied() {

            AccountHarness h = new AccountHarness();
            ClosedState state = new ClosedState();

            state.deposit(250.0, h.account);

            assertAll(() -> assertTrue(h.history.stream().anyMatch(s ->s.equals("Cannot deposit to a closed account."))),() -> assertTrue(h.notifications.stream().anyMatch(s ->s.equals("The operation of depositing to a closed account was blocked successfully."))),() -> assertEquals(100.0, h.balance(), 1e-9),() -> assertNull(h.lastStateChange()));

            assertEquals(0, h.account.setBalanceCalls);
            
            assertEquals(0, h.account.changeStateCalls);
            assertEquals(0, h.account.processMonthCalls);

            assertEquals(0, h.account.withdrawCalls);
            assertEquals(0, h.account.depositCalls);

        }

    }

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        /**
         * Denies withdrawals and logs; balance and state remain unchanged.
         */
        @Test
        @DisplayName("Denies withdrawals: records history and notifies; no balance/state changes")
        void withdrawDenied() {

            AccountHarness h = new AccountHarness();
            ClosedState state = new ClosedState();

            state.withdraw(80.0, h.account);

            assertAll(() -> assertTrue(h.history.stream().anyMatch(s ->s.equals("Cannot withdraw from a closed account."))),() -> assertTrue(h.notifications.stream().anyMatch(s ->s.equals("The operation of withdrawing from a closed account was blocked successfully."))),() -> assertEquals(100.0, h.balance(), 1e-9),() -> assertNull(h.lastStateChange()));

            assertEquals(0, h.account.setBalanceCalls);
            assertEquals(0, h.account.changeStateCalls);
            assertEquals(0, h.account.processMonthCalls);
            assertEquals(0, h.account.withdrawCalls);
            assertEquals(0, h.account.depositCalls);
        }
    }

    @Nested
    @DisplayName("processMonth()")
    class ProcessMonthTests {

        /** Skips monthly processing for closed accounts; only logs and notifies. */
        @Test
        @DisplayName("Skips processing: records history and notifies; no balance/state changes")

        void processMonthNoOp() {
            AccountHarness h = new AccountHarness();
            ClosedState state = new ClosedState();

            state.processMonth(h.account);

            assertAll(() -> assertTrue(h.history.stream().anyMatch(s ->s.equals("No monthly processing for closed accounts."))),() -> assertTrue(h.notifications.stream().anyMatch(s ->s.equals("The operation of processing month for a closed account was blocked successfully."))),() -> assertEquals(100.0, h.balance(), 1e-9),() -> assertNull(h.lastStateChange()));

            assertEquals(0, h.account.setBalanceCalls);
            assertEquals(0, h.account.changeStateCalls);

            assertEquals(0, h.account.processMonthCalls);
            assertEquals(0, h.account.withdrawCalls);
            assertEquals(0, h.account.depositCalls);

        }

    }

    @Nested
    @DisplayName("unfreeze()")
    class UnfreezeTests {

        /** Denies unfreeze attempts; logs and notifies; no state change occurs. */
        @Test
        @DisplayName("Denies unfreeze: records history and notifies; no balance/state changes")
        void unfreezeDenied() {
            AccountHarness h = new AccountHarness();
            ClosedState state = new ClosedState();

            state.unfreeze(h.account);

            assertAll(() -> assertTrue(h.history.stream().anyMatch(s ->s.equals("Cannot unfreeze a closed account."))),() -> assertTrue(h.notifications.stream().anyMatch(s ->s.equals("The operation of unfreezing a closed account was blocked successfully."))),() -> assertEquals(100.0, h.balance(), 1e-9),() -> assertNull(h.lastStateChange()));

            assertEquals(0, h.account.setBalanceCalls);
            
            assertEquals(0, h.account.changeStateCalls);
            assertEquals(0, h.account.processMonthCalls);
            assertEquals(0, h.account.withdrawCalls);
            assertEquals(0, h.account.depositCalls);
        }
    }

}

