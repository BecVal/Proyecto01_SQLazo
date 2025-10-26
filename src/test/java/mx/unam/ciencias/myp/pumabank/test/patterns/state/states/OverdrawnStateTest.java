package mx.unam.ciencias.myp.pumabank.test.patterns.state.states;

import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.ActiveState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.OverdrawnState;

import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests for {@link OverdrawnState}, covering fee application, deposit transitions, withdrawal denial, monthly processing logic, and state restoration back to ActiveState.
 * 
 */
class OverdrawnStateTest {
    /**
     * Facade stub used to suppress side effects during testing.
     */
    static class DummyFacade extends PumaBankFacade {
        @Override public void recordFeeCollection(double fee) {}

        @Override public void recordInterestPayment(double interest) {}
    }

    /**
     * Fake account used to track state transitions, balance, history, notifications, and fee recording without involving real system behavior.
     */
    static class FakeAccount extends Account {
        private static final Client DUMMY = new Client("Client", "X-1");
        private static final AccountState NOOP = new AccountState() {

            @Override public void deposit(double amount, Account account) {}
            @Override public void withdraw(double amount, Account account) {}
            @Override public void processMonth(Account account) {}
            @Override public void unfreeze(Account account) {}
        }; private static final InterestCalculation ZERO = b -> 0.0;

        FakeAccount(double initial) {
            super(DUMMY, 0.0, NOOP, ZERO, new DummyFacade());
            this.balance = initial;
        }

        double balance;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();

        AccountState lastState = null;
        int changeStateCalls = 0;

        int recordFeeCalls = 0;
        double lastRecordedFee = 0.0;

        @Override public double getBalance() { return balance; }
        @Override public void setBalance(double b) { balance = b; }
        @Override public void addHistory(String e) { history.add(e); }


        @Override public void notify(String m) { notifications.add(m); }
        @Override public void changeState(AccountState s) { changeStateCalls++; lastState = s; }
        @Override public void recordFee(double fee) { recordFeeCalls++; lastRecordedFee = fee; }



    }

    @Nested
    @DisplayName("deposit()")
    class DepositTests {

        /**
         * Verifies that the overdraft fee is applied only once and state remains Overdrawn when still negative.
         * 
         */
        @Test
        void depositAppliesFeeOnceAndStaysOverdrawnIfNegative() {

            FakeAccount acc = new FakeAccount(-50.0);
            OverdrawnState st = new OverdrawnState();
            st.deposit(20.0, acc);

            assertAll(() -> assertEquals(-130.0, acc.getBalance(), 1e-9),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Overdraft fee applied: $100.0"))),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Deposit while overdrawn: $20.0 | Balance: $-130.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.startsWith("OVERDRAFT_FEE: $100.00 | Balance Before Fee: $-50.00 | Balance After Fee: $-150.00"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.startsWith("DEPOSIT_OVERDRAWN: $20.00 | Total Balance After: $-130.00"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("Deposit received, account remains Overdrawn. Balance $-130.0"))),() -> assertEquals(0, acc.changeStateCalls),() -> assertNull(acc.lastState));

        }

        /**
         * Verifies that fee is not applied again and account transitions back to ActiveState when balance recovers.
         */

        @Test

        void secondDepositDoesNotReapplyFeeAndTransitionsWhenRecovered() {
            FakeAccount acc = new FakeAccount(-50.0);

            OverdrawnState st = new OverdrawnState();

            st.deposit(20.0, acc);
            st.deposit(200.0, acc);

            assertAll(() -> assertEquals(70.0, acc.getBalance(), 1e-9),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Deposit while overdrawn: $200.0 | Balance: $70.0"))),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("State changed -> ActiveState (balance recovered)."))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.startsWith("DEPOSIT_OVERDRAWN: $200.00 | Total Balance After: $70.00"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("STATE_CHANGE: OverdrawnState -> ActiveState | Reason: Balance recovered to non-negative"))),() -> assertEquals(1, acc.changeStateCalls),() -> assertNotNull(acc.lastState),() -> assertEquals(ActiveState.class.getSimpleName(), acc.lastState.getClass().getSimpleName()));
        }
    }


    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        /**
         * Ensures withdrawals are denied entirely while overdrawn.
         */
        @Test
        void withdrawIsDenied() {
            FakeAccount acc = new FakeAccount(-123.45);
            OverdrawnState st = new OverdrawnState();

            st.withdraw(80.0, acc);

            
            assertAll(() -> assertEquals(-123.45, acc.getBalance(), 1e-9),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Withdrawal denied: account is overdrawn. Attempted: $80.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("WITHDRAWAL_DENIED: $80.00 | Reason: Account overdrawn | Current Balance: $-123.45"))),() -> assertEquals(0, acc.changeStateCalls));
        }
    }

    @Nested
    @DisplayName("processMonth()")
    class ProcessMonthTests {

        /**
         * Applies month-end overdraft fee and remains in OverdrawnState.
         * 
         */
        @Test
        void appliesMonthlyFeeAndRemainsOverdrawn() {

            FakeAccount acc = new FakeAccount(-20.0);
            OverdrawnState st = new OverdrawnState();

            st.processMonth(acc);

            assertAll(() -> assertEquals(-120.0, acc.getBalance(), 1e-9),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Month-end overdraft fee applied: $100.0 | Balance: $-120.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("MONTHLY_OVERDRAFT_FEE: $100.00 | Balance Before: $-20.00 | Balance After: $-120.00"))),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Monthly processing: account remains overdrawn. No interests applied."))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("MONTHLY_SUMMARY: Account remains overdrawn | Balance: $-120.00"))),() -> assertEquals(1, acc.recordFeeCalls),() -> assertEquals(100.0, acc.lastRecordedFee, 1e-9),() -> assertEquals(0, acc.changeStateCalls));

        }

        /**
         * If balance becomes non-negative, interest is applied and account returns to ActiveState.
         * 
         */
        @Test
        void becomesNonNegativeAppliesInterestAndTransitions() {

            FakeAccount acc = new FakeAccount(120.0);

            OverdrawnState st = new OverdrawnState();

            InterestCalculation fivePercent = b -> b * 0.05;
            acc.setInterestPolicy(fivePercent);

            st.processMonth(acc);

            assertAll(() -> assertEquals(21.0, acc.getBalance(), 1e-9),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Month-end overdraft fee applied: $100.0 | Balance: $20.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("MONTHLY_OVERDRAFT_FEE: $100.00 | Balance Before: $120.00 | Balance After: $20.00"))),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Monthly interest applied: $1.0 | Balance: $21.0"))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("INTEREST_APPLIED: $1.00 | Balance Before: $20.00 | Balance After: $21.00"))),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("State changed -> ActiveState."))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("STATE_CHANGE: OverdrawnState -> ActiveState | Reason: Month-end balance recovery"))),() -> assertEquals(1, acc.recordFeeCalls),() -> assertEquals(100.0, acc.lastRecordedFee, 1e-9),() -> assertEquals(1, acc.changeStateCalls),() -> assertNotNull(acc.lastState),() -> assertEquals(ActiveState.class.getSimpleName(), acc.lastState.getClass().getSimpleName()));

        }

        /**
         * Ensures fee is not reapplied if the overdraft fee was already applied earlier.
         */
        @Test

        void doesNotReapplyFeeIfAlreadyApplied() {

            FakeAccount acc = new FakeAccount(-10.0);
            OverdrawnState st = new OverdrawnState();
            st.deposit(5.0, acc);

            assertEquals(-105.0, acc.getBalance(), 1e-9);
            st.processMonth(acc);

            long feeLines = acc.history.stream().filter(s -> s.startsWith("Month-end overdraft fee applied")).count();

            assertAll(() -> assertEquals(-105.0, acc.getBalance(), 1e-9, "Debe permanecer igual, sin fee extra"),() -> assertEquals(0, feeLines, "No debe reaplicar el fee mensual"),() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Monthly processing: account remains overdrawn. No interests applied."))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("MONTHLY_SUMMARY: Account remains overdrawn | Balance: $-105.00"))));
        }
    }

    @Nested
    @DisplayName("unfreeze()")
    class UnfreezeTests {

        /**
         * Ensures unfreeze is always denied while overdrawn.
         */
        @Test
        void unfreezeDenied() {
            FakeAccount acc = new FakeAccount(-1.0);
            OverdrawnState st = new OverdrawnState();

            st.unfreeze(acc);
            assertAll(() -> assertTrue(acc.history.stream().anyMatch(s -> s.equals("Unfreeze operation denied: account is overdrawn."))),() -> assertTrue(acc.notifications.stream().anyMatch(s -> s.equals("UNFREEZE_DENIED: Cannot unfreeze an overdrawn account"))),() -> assertEquals(-1.0, acc.getBalance(), 1e-9),() -> assertEquals(0, acc.changeStateCalls));
        }

    }

}