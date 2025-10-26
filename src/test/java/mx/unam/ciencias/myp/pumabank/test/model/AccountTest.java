package mx.unam.ciencias.myp.pumabank.test.model;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.patterns.observer.Observer;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
/**
 * Tests for {@link Account} behavior including state delegation, interest updates, observer notifications, and balance operations.
 */
class AccountTest {

    /**
     * State implementation used to record and observe method calls.
     */
    static class RecordingState implements AccountState {
        boolean depositCalled;
        boolean withdrawCalled;

        boolean processCalled;
        boolean unfreezeCalled;
        double lastAmount;

        @Override
        public void deposit(double amount, Account account) {

            depositCalled = true;
            lastAmount = amount;
            account.addHistory("state:deposit " + amount);
            account.setBalance(account.getBalance() + amount);
        }

        @Override
        public void withdraw(double amount, Account account) {

            withdrawCalled = true;
            lastAmount = amount;
            account.addHistory("state:withdraw " + amount);
            account.setBalance(account.getBalance() - amount);
        }

        @Override
        public void processMonth(Account account) {
            processCalled = true;
            account.addHistory("state:processMonth");
        }

        @Override
        public void unfreeze(Account account) {

            unfreezeCalled = true;
            account.addHistory("state:unfreeze");
        }
    }

    /**
     * 
     * Observer implementation that records incoming messages.
     * 
     */
    static class RecordingObserver implements Observer {
        final List<String> messages = new ArrayList<>();
        @Override public void update(String message) { messages.add(message); }
    }

    /**
     * 
     * Observer implementation that always throws to test failure resilience.
     */
    static class ThrowingObserver implements Observer {
        @Override public void update(String message) { throw new RuntimeException(); }
    }

    /**
     * Simple interest policy applying a fixed rate to the account balance.
     * 
     */
    static class FixedRateInterest implements InterestCalculation {
        private final double rate;
        FixedRateInterest(double rate) { this.rate = rate; }
        @Override public double calculate(double balance) { return balance * rate; }
    }

    private static Client anyClient() { return new Client("Test User", "ID-1"); }

    private static PumaBankFacade facade() { return new PumaBankFacade(); }

    /**
     * Ensures the constructor rejects null mandatory parameters.
     * 
     */
    @Test
    void constructorRejectsNulls() {
        RecordingState st = new RecordingState();
        InterestCalculation ic = new FixedRateInterest(0.01);
        Client c = anyClient();
        PumaBankFacade f = facade();
        assertThrows(NullPointerException.class, () -> new Account(null, 0, st, ic, f));
        assertThrows(NullPointerException.class, () -> new Account(c, 0, null, ic, f));

        assertThrows(NullPointerException.class, () -> new Account(c, 0, st, null, f));
    }

    /**
     * Verifies checkBalance returns the current stored balance.
     */
    @Test
    void checkBalanceReturnsCurrentBalance() {
        Account acc = new Account(anyClient(), 150.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        assertEquals(150.0, acc.checkBalance("ignored"), 1e-9);
    }

    /**
     * 
     * Ensures deposit delegates to state and updates balance.
     */
    @Test
    void depositWithPositiveAmountDelegatesToStateAndUpdatesBalance() {
        RecordingState st = new RecordingState();
        Account acc = new Account(anyClient(), 100.0, st, new FixedRateInterest(0.0), facade());
        acc.deposit(40.0, "ignored");
        assertTrue(st.depositCalled);
        assertEquals(140.0, acc.getBalance(), 1e-9);
        assertTrue(acc.getHistory().stream().anyMatch(s -> s.startsWith("state:deposit")));
    }

    /**
     * Ensures deposit with non-positive amounts throws exception.
     */
    @Test
    void depositNonPositiveAmountThrows() {
        Account acc = new Account(anyClient(), 0.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        assertThrows(IllegalArgumentException.class, () -> acc.deposit(0.0, "x"));

        assertThrows(IllegalArgumentException.class, () -> acc.deposit(-5.0, "x"));
    }

    /**
     * Ensures withdrawal delegates to state and updates balance.
     */
    @Test
    void withdrawWithPositiveAmountDelegatesToStateAndUpdatesBalance() {
        RecordingState st = new RecordingState();
        Account acc = new Account(anyClient(), 100.0, st, new FixedRateInterest(0.0), facade());

        acc.withdraw(30.0, "ignored");
        assertTrue(st.withdrawCalled);
        assertEquals(70.0, acc.getBalance(), 1e-9);
        assertTrue(acc.getHistory().stream().anyMatch(s -> s.startsWith("state:withdraw")));
    }

    /**
     * 
     * Ensures withdrawal with non-positive amounts throws exception.
     */
    @Test
    void withdrawNonPositiveAmountThrows() {

        Account acc = new Account(anyClient(), 0.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        assertThrows(IllegalArgumentException.class, () -> acc.withdraw(0.0, "x"));
        assertThrows(IllegalArgumentException.class, () -> acc.withdraw(-1.0, "x"));
    }



    /**
     * Validates that monthly processing delegates to current state.
     * 
     */
    @Test
    void processMonthDelegatesToState() {
        RecordingState st = new RecordingState();

        Account acc = new Account(anyClient(), 50.0, st, new FixedRateInterest(0.0), facade());
        acc.processMonth();
        assertTrue(st.processCalled);
        assertTrue(acc.getHistory().stream().anyMatch(s -> s.equals("state:processMonth")));
    }

    /**
     * Verifies that changing the state affects following operations.
     */
    @Test
    void changeStateAffectsSubsequentOperations() {
        RecordingState st1 = new RecordingState();
        RecordingState st2 = new RecordingState();

        Account acc = new Account(anyClient(), 10.0, st1, new FixedRateInterest(0.0), facade());
        acc.changeState(st2);
        acc.deposit(5.0, "x");

        assertFalse(st1.depositCalled);
        assertTrue(st2.depositCalled);
    }

    /**
     * Ensures updating the interest policy changes the stored strategy and logs the change.
     */
    @Test
    void setInterestPolicyUpdatesAndAddsHistory() {
        RecordingState st = new RecordingState();
        Account acc = new Account(anyClient(), 100.0, st, new FixedRateInterest(0.01), facade());

        InterestCalculation newPolicy = new FixedRateInterest(0.02);
        acc.setInterestPolicy(newPolicy);
        assertSame(newPolicy, acc.getInterestPolicy());
        assertTrue(acc.getHistory().stream().anyMatch(s -> s.contains("Interest policy changed")));
    }

    /**
     * Ensures notify calls all observers and ignores exceptions from failing observers.
     */

    @Test
    void notifyCallsAllObserversAndSwallowsExceptions() {
        Account acc = new Account(anyClient(), 0.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        RecordingObserver a = new RecordingObserver();
        RecordingObserver b = new RecordingObserver();
        acc.addObserver(a);
        acc.addObserver(new ThrowingObserver());
        acc.addObserver(b);
        acc.notify("hello");
        assertEquals(1, a.messages.size());
        assertEquals("hello", a.messages.get(0));
        assertEquals(1, b.messages.size());
    }

    /**
     * Verifies removing an observer prevents receiving further notifications.
     * 
     */
    @Test
    void removeObserverStopsReceivingNotifications() {

        Account acc = new Account(anyClient(), 0.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        RecordingObserver a = new RecordingObserver();
        acc.addObserver(a);
        acc.removeObserver(a);
        acc.notify("ignored");
        assertTrue(a.messages.isEmpty());
    }

    /**
     * Ensures getBalance and setBalance behave as expected.
     */
    @Test
    
    void setBalanceAndGetBalanceWork() {
        Account acc = new Account(anyClient(), 0.0, new RecordingState(), new FixedRateInterest(0.0), facade());
        acc.setBalance(123.45);
        assertEquals(123.45, acc.getBalance(), 1e-9);
    }
}
