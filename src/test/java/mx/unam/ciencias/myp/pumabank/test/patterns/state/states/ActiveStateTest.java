package mx.unam.ciencias.myp.pumabank.test.patterns.state.states;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.ActiveState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.AnnualInterest;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.MonthlyInterest;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.PremiumInterest;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ActiveStateTest {
    static class DummyFacade extends PumaBankFacade {
        @Override public void recordFeeCollection(double fee) {}
        @Override public void recordInterestPayment(double interest) {}
    }

    static class FakeAccount extends Account {
        private static final Client DUMMY_CLIENT = new Client("Diego", "ID-1");
        private static final AccountState NOOP_STATE = new AccountState() {
            @Override public void deposit(double a, Account acc) {}
            @Override public void withdraw(double a, Account acc) {}
            @Override public void processMonth(Account acc) {}
            @Override public void unfreeze(Account acc) {}
        };
        private static final InterestCalculation ZERO_INTEREST = balance -> 0.0;
        FakeAccount() {
            super(DUMMY_CLIENT, 0.0, NOOP_STATE, ZERO_INTEREST, new DummyFacade());
        }
        double balance = 0.0;
        final List<String> history = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();

        AccountState lastStateChange = null;
        int processMonthCalls = 0;
        InterestCalculation policy = ZERO_INTEREST;
        @Override public double getBalance() { return balance; }
        @Override public void setBalance(double b) { balance = b; }
        @Override public void addHistory(String e) { history.add(e); }
        @Override public void notify(String m) { notifications.add(m); }

        @Override public void changeState(AccountState s) { lastStateChange = s; }
        @Override public void processMonth() { processMonthCalls++; }
        @Override public InterestCalculation getInterestPolicy() { return policy; }
    }

    static class AccountHarness {
        final FakeAccount account = new FakeAccount();
        AccountHarness(double initialBalance) {
            account.balance = initialBalance;
        }

        double balance() { return account.balance; }
        List<String> history() { return account.history; }

        List<String> notifications() { return account.notifications; }
    }

    @Nested
    @DisplayName("Basics: deposit / withdraw")
    class Basics {

        @Test
        @DisplayName("deposit adds to balance, records history and notifies")
        void deposit() {
            AccountHarness h = new AccountHarness(100.0);
            ActiveState state = new ActiveState();
            state.deposit(40.0, h.account);

            assertAll(() -> assertEquals(140.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s -> s.startsWith("Deposited: 40.0"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("DEPOSIT: $40.00")&& s.contains("Balance Before: $100.00")&& s.contains("Balance After: $140.00"))));

            assertNull(h.account.lastStateChange);
        }
        @Test
        @DisplayName("withdraw with sufficient funds subtracts and records")
        void withdrawSufficient() {

            AccountHarness h = new AccountHarness(200.0);
            ActiveState state = new ActiveState();
            state.withdraw(80.0, h.account);

            assertAll(() -> assertEquals(120.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.startsWith("Withdrawal: $80.0") && s.contains("Balance: $120.0"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("WITHDRAWAL: $80.00")&& s.contains("Balance Before: $200.00")&& s.contains("Balance After: $120.00"))));
            assertNull(h.account.lastStateChange);

        }

        @Test
        @DisplayName("withdraw with insufficient funds triggers OverdrawnState")
        void withdrawInsufficient() {
            AccountHarness h = new AccountHarness(50.0);
            ActiveState state = new ActiveState();
            state.withdraw(120.0, h.account);
            assertAll(() -> assertEquals(-70.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.startsWith("Withdrawal exceeded funds. Overdraft triggered. Amount: $120.0"))),() -> assertTrue(h.history().stream().anyMatch(s ->s.equals("State changed -> OverdrawnState"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("WITHDRAWAL_OVERDRAFT: $120.00") && s.contains("STATE: Active -> Overdrawn"))));

            assertNotNull(h.account.lastStateChange);

            assertEquals("OverdrawnState", h.account.lastStateChange.getClass().getSimpleName());

        }
    }

    @Nested
    @DisplayName("processMonth with MonthlyInterest")
    class MonthlyInterestTests {

        @Test
        @DisplayName("Applies monthly interest when balance >= minimum")
        void monthlyApplies() {
            AccountHarness h = new AccountHarness(1000.0);
            h.account.policy = new MonthlyInterest(0.01, 500.0);

            ActiveState state = new ActiveState();

            state.processMonth(h.account);

            assertAll(() -> assertEquals(1010.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.startsWith("Monthly interest applied: $10.0") && s.contains("Balance: $1010.0"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("INTEREST_APPLIED: $10.00")&& s.contains("Balance Before: $1000.00")&& s.contains("Balance After: $1010.00"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));

            assertEquals(0, h.account.processMonthCalls);

        }

        @Test
        @DisplayName("Records 'no interest' when below minimum or zero")
        void monthlyNoInterest() {
            AccountHarness h = new AccountHarness(400.0);
            h.account.policy = new MonthlyInterest(0.02, 500.0);
            ActiveState state = new ActiveState();
            state.processMonth(h.account);

            assertAll(() -> assertEquals(400.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.equals("Monthly processing: no interest applied."))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_PROCESSING: No interest applied to active account"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));

        }

    }

    @Nested
    @DisplayName("processMonth with PremiumInterest")
    class PremiumInterestTests {
        @Test
        @DisplayName("Applies base + tiered bonus on high balances")
        void premiumTiers() {
            AccountHarness h = new AccountHarness(20000.0);
            h.account.policy = new PremiumInterest(0.005, 10000, 20000, 0.003, 0.006);
            ActiveState state = new ActiveState();
            state.processMonth(h.account);

            assertAll(() -> assertEquals(20220.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.startsWith("Monthly interest applied: $220.0") && s.contains("Balance: $20220.0"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("INTEREST_APPLIED: $220.00")&& s.contains("Balance Before: $20000.00")&& s.contains("Balance After: $20220.00"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));
        }

        @Test
        @DisplayName("No interest when balance <= 0")
        void premiumZeroOrNegative() {
            AccountHarness h = new AccountHarness(0.0);
            h.account.policy = new PremiumInterest(0.01, 1000, 5000, 0.005, 0.01);
            ActiveState state = new ActiveState();

            state.processMonth(h.account);

            assertAll(() -> assertEquals(0.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.equals("Monthly processing: no interest applied."))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_PROCESSING: No interest applied to active account"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));

        }
    }

    @Nested
    @DisplayName("processMonth with AnnualInterest")
    class AnnualInterestTests {
        @Test
        @DisplayName("Months 1..11: returns 0.0 interest")
        void annualNonPayoutMonths() {

            AccountHarness h = new AccountHarness(3000.0);

            AnnualInterest calc = new AnnualInterest(0.12, 2000.0);

            calc.setCurrentMonth(5);
            calc.recordMonthBalance(2500.0);
            calc.recordMonthBalance(3000.0);
            h.account.policy = calc;
            ActiveState state = new ActiveState();


            state.processMonth(h.account);

            assertAll(() -> assertEquals(3000.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.equals("Monthly processing: no interest applied."))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_PROCESSING: No interest applied to active account"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));

        }

        @Test
        @DisplayName("Month 12 with avg above threshold pays annual interest on current balance")
        void annualPayoutMonth() {

            AccountHarness h = new AccountHarness(5000.0);
            AnnualInterest calc = new AnnualInterest(0.12, 2000.0);

            calc.recordMonthBalance(2500.0);
            calc.recordMonthBalance(3000.0);
            calc.recordMonthBalance(2000.0);
            calc.setCurrentMonth(12);
            h.account.policy = calc;
            ActiveState state = new ActiveState();

            state.processMonth(h.account);

            assertAll(() -> assertEquals(5600.0, h.balance(), 1e-9),() -> assertTrue(h.history().stream().anyMatch(s ->s.startsWith("Monthly interest applied: $600.0") && s.contains("Balance: $5600.0"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.startsWith("INTEREST_APPLIED: $600.00")&& s.contains("Balance Before: $5000.00")&& s.contains("Balance After: $5600.00"))),() -> assertTrue(h.notifications().stream().anyMatch(s ->s.equals("MONTHLY_SUMMARY: Active account processed"))));

        }
        
    }

    @Nested
    @DisplayName("Negative balance path")
    class NegativePath {

        @Test
        @DisplayName("If balance is negative, switch to OverdrawnState and delegate .processMonth()")
        void negativeBalanceDelegatesToOverdrawn() {

            AccountHarness h = new AccountHarness(-1.0);

            ActiveState state = new ActiveState();

            state.processMonth(h.account);

            assertNotNull(h.account.lastStateChange);
            assertEquals("OverdrawnState", h.account.lastStateChange.getClass().getSimpleName());
            assertTrue(h.history().stream().anyMatch(s ->s.contains("Detected negative balance") && s.contains("OverdrawnState")));
            assertTrue(h.notifications().stream().anyMatch(s ->s.contains("Delegating month-end processing to OverdrawnState.")));

            assertEquals(1, h.account.processMonthCalls);

        }
    }

    @Nested
    @DisplayName("unfreeze")
    class Unfreeze {

        @Test
        @DisplayName("Already active: only notifies and records")
        void alreadyActive() {
            AccountHarness h = new AccountHarness(100.0);
            ActiveState state = new ActiveState();

            state.unfreeze(h.account);

            assertTrue(h.notifications().stream().anyMatch(s ->s.equals("Account is already active.")));
            assertTrue(h.history().stream().anyMatch(s ->s.equals("Unfreeze called, but account is already in ActiveState.")));

            assertNull(h.account.lastStateChange);
        }
    }
    
}
