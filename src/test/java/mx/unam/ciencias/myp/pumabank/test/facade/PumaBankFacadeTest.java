package mx.unam.ciencias.myp.pumabank.test.facade;

import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.AccountProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PumaBankFacadeTest {

    private PumaBankFacade newQuietFacade() {
        PumaBankFacade f = new PumaBankFacade();
        f.setQuietMode(true);
        return f;
    }

    @Nested
    @DisplayName("Client Registration")
    class ClientRegistration {

        @Test
        @DisplayName("registerClient stores and exposes the new client")
        void registersClient() {
            PumaBankFacade f = newQuietFacade();
            Client c = f.registerClient("Hi", "CLI-1");

            assertAll(() -> assertEquals("Hi", c.getName()),() -> assertEquals("CLI-1", c.getClientId()),() -> assertEquals(1, f.getAllClients().size()),() -> assertSame(c, f.getAllClients().get(0)));
        }
    }

    @Nested
    @DisplayName("Account Creation")
    class AccountCreation {

        @Test
        @DisplayName("createAccount creates a MONTHLY interest account without services")
        void createMonthlyAccount() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "G1");

            AccountProxy proxy = f.createAccount("G1", 1500.0, "1234", "MONTHLY", Collections.emptyList());
            String accountId = "G1-ACC-1";

            assertAll(() -> assertNotNull(proxy),() -> assertSame(proxy, f.findAccount(accountId)),() -> assertEquals(1, f.getClientAccounts("G1").size()));

            double balance = f.checkBalance(accountId, "1234");
            assertEquals(1500.0, balance, 1e-9);
        }

        @Test
        @DisplayName("createAccount with decorators still works properly")
        void createWithServices() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "L1");

            List<String> services = Arrays.asList("ANTI_FRAUD", "PREMIUM_ALERTS", "REWARDS");
            AccountProxy proxy = f.createAccount("L1", 800.0, "0000", "MONTHLY", services);
            assertNotNull(proxy);
            assertEquals(1, f.getClientAccounts("L1").size());
            assertDoesNotThrow(() -> f.deposit("L1-ACC-1", 50.0, "0000"));
        }

        @Test
        @DisplayName("createAccount throws for invalid client or interest type")
        void createErrors() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "N1");
            assertThrows(IllegalArgumentException.class,() -> f.createAccount("UNKNOWN", 100.0, "1", "MONTHLY", null));
            assertThrows(IllegalArgumentException.class,() -> f.createAccount("N1", 100.0, "1", "INVALID", null));
        }
    }

    @Nested
    @DisplayName("Facade Operations")
    class Operations {

        @Test
        @DisplayName("deposit, withdraw and checkBalance delegate correctly and record transactions")
        void depositWithdrawBalance() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "M1");
            f.createAccount("M1", 100.0, "1111", "MONTHLY", null);

            String id = "M1-ACC-1";
            int before = f.getMonthlyTransactions();
            f.deposit(id, 50.0, "1111");
            f.withdraw(id, 20.0, "1111");
            double balance = f.checkBalance(id, "1111");

            assertAll(() -> assertEquals(130.0, balance, 1e-9),() -> assertTrue(f.getMonthlyTransactions() >= before + 3));
        }

        @Test
        @DisplayName("Throws when trying to deposit, withdraw, or checkBalance with unknown accountId")
        void unknownAccountErrors() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "A1");
            f.createAccount("A1", 50.0, "p", "MONTHLY", null);

            assertAll(() -> assertThrows(IllegalArgumentException.class, () -> f.deposit("A1-ACC-99", 1, "p")),() -> assertThrows(IllegalArgumentException.class, () -> f.withdraw("A1-ACC-99", 1, "p")),() -> assertThrows(IllegalArgumentException.class, () -> f.checkBalance("A1-ACC-99", "p")));
        }
    }

    @Nested
    @DisplayName("Monthly Processing")
    class MonthlyProcessing {

        @Test
        @DisplayName("processMonthlyOperations applies monthly interest when balance >= threshold")
        void processesAllAndAppliesMonthlyInterest() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "H1");
            f.createAccount("H1", 2000.0, "pin", "MONTHLY", null);
            f.createAccount("H1", 500.0, "pin2", "MONTHLY", null);
            AccountProxy p1 = f.findAccount("H1-ACC-1");
            AccountProxy p2 = f.findAccount("H1-ACC-2");
            assertNotNull(p1);
            assertNotNull(p2);

            Account a1Before = p1.getUnderlyingAccount();
            Account a2Before = p2.getUnderlyingAccount();
            assertEquals(2000.0, a1Before.getBalance(), 1e-9);
            assertEquals(500.0, a2Before.getBalance(), 1e-9);
            f.processMonthlyOperations(7);

            Account a1After = p1.getUnderlyingAccount();
            Account a2After = p2.getUnderlyingAccount();

            assertAll(() -> assertEquals(2020.0, a1After.getBalance(), 1e-9),() -> assertEquals(500.0, a2After.getBalance(), 1e-9),() -> assertTrue(f.getMonthlyTransactions() >= 2));
        }
    }

    @Nested
    @DisplayName("Account Deletion")
    class Deletion {

        @Test
        @DisplayName("deleteAccount removes it and returns true; invalid or missing -> false")
        void deleteAccountBehavior() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "B1");
            f.createAccount("B1", 100.0, "x", "MONTHLY", null);
            f.createAccount("B1", 200.0, "y", "MONTHLY", null);
            assertEquals(2, f.getClientAccounts("B1").size());

            boolean removed = f.deleteAccount("B1-ACC-1");
            assertTrue(removed);
            assertNull(f.findAccount("B1-ACC-1"));
            assertEquals(1, f.getClientAccounts("B1").size());
            assertFalse(f.deleteAccount("B1-ACC-999"));
            assertFalse(f.deleteAccount("B1"));
        }
    }

    @Nested
    @DisplayName("Portfolio and Metrics")
    class PortfolioAndMetrics {

        @Test
        @DisplayName("getClientPortfolio summarizes total accounts and balance")
        void portfolioSummary() {
            PumaBankFacade f = newQuietFacade();
            f.registerClient("Hi", "T1");
            f.createAccount("T1", 100.0, "1", "MONTHLY", null);
            f.createAccount("T1", 250.5, "2", "MONTHLY", null);

            Map<String, Object> portfolio = f.getClientPortfolio("T1");

            assertAll(() -> assertEquals(2, portfolio.get("totalAccounts")),() -> assertEquals(350.5, (double) portfolio.get("totalBalance"), 1e-9),() -> assertTrue(portfolio.get("client") instanceof Client),() -> assertTrue(portfolio.get("accounts") instanceof List<?>));
        }

        @Test
        @DisplayName("recordFeeCollection and recordInterestPayment accumulate totals correctly")
        void feeAndInterestCounters() {
            PumaBankFacade f = newQuietFacade();
            f.recordFeeCollection(12.5);
            f.recordFeeCollection(7.5);
            f.recordInterestPayment(3.0);
            f.recordInterestPayment(2.0);

            assertAll(() -> assertEquals(20.0, f.getTotalFeesCollected(), 1e-9),() -> assertEquals(5.0, f.getTotalInterestPaid(), 1e-9));
        }

        @Test
        @DisplayName("getClientAccounts returns list")
        void getClientAccountsList() {
            PumaBankFacade f = newQuietFacade();

            f.registerClient("Hi", "D1");

            List<IAccount> empty = f.getClientAccounts("D1");
            assertTrue(empty.isEmpty());

            f.createAccount("D1", 10.0, "z", "MONTHLY", null);
            List<IAccount> one = f.getClientAccounts("D1");
            assertEquals(1, one.size());
        }

        @Test
        @DisplayName("getClientPortfolio throws if client not found")
        void portfolioUnknownClient() {
            PumaBankFacade f = newQuietFacade();
            assertThrows(IllegalArgumentException.class, () -> f.getClientPortfolio("NO"));
        }
    }
}
