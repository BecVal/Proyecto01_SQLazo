package mx.unam.ciencias.myp.pumabank.test.patterns.proxy;
import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.AccountProxy;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.PinAuthenticator;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import org.junit.jupiter.api.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AccountProxyTest {

    static class FakeAccount extends Account {
        private static final Client DUMMY_CLIENT = new Client("Vic", "ID-1");
        private static final AccountState NOOP_STATE = new AccountState() { @Override public void deposit(double amount, Account account) {} @Override public void withdraw(double amount, Account account) {} @Override public void processMonth(Account account) {} @Override public void unfreeze(Account account) {} };
        private static final InterestCalculation ZERO_INTEREST = balance -> 0.0;
        FakeAccount() { super(DUMMY_CLIENT, 0.0, NOOP_STATE, ZERO_INTEREST); }
        int depositCalls = 0; double lastDepositAmount = 0.0; String lastDepositPin = null;
        int withdrawCalls = 0; double lastWithdrawAmount = 0.0; String lastWithdrawPin = null;
        int checkBalanceCalls = 0; String lastCheckPin = null; double balanceToReturn = 0.0;
        int processMonthCalls = 0; final List<String> notifications = new ArrayList<>();
        @Override public void deposit(double amount, String pin) { depositCalls++; lastDepositAmount = amount; lastDepositPin = pin; }
        @Override public void withdraw(double amount, String pin) { withdrawCalls++; lastWithdrawAmount = amount; lastWithdrawPin = pin; }
        @Override public double checkBalance(String pin) { checkBalanceCalls++; lastCheckPin = pin; return balanceToReturn; }
        @Override public void processMonth() { processMonthCalls++; }
        @Override public void notify(String message) { notifications.add(message); }
    }

    static class FakeAuthenticator extends PinAuthenticator {
        private final String validPin; int validateCalls = 0; String lastValidatedPin = null;
        FakeAuthenticator(String validPin) { super(validPin); this.validPin = validPin; }
        @Override public boolean validate(String inputPin) { validateCalls++; lastValidatedPin = inputPin; return validPin != null && validPin.equals(inputPin); }
    }

    @Nested @DisplayName("deposit") class Deposit {
        @Test @DisplayName("Delegates to real account when PIN is valid") void depositDelegatesOnValidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                Account real = new FakeAccount(); PinAuthenticator auth = new FakeAuthenticator("1234"); AccountProxy proxy = new AccountProxy((Account) real, auth); proxy.deposit(100.0, "1234");
                FakeAccount fa = (FakeAccount) real; FakeAuthenticator au = (FakeAuthenticator) auth;
                assertAll(() -> assertEquals(1, au.validateCalls),() -> assertEquals("1234", au.lastValidatedPin),() -> assertEquals(1, fa.depositCalls),() -> assertEquals(100.0, fa.lastDepositAmount, 1e-9),() -> assertEquals("1234", fa.lastDepositPin),() -> assertTrue(fa.notifications.isEmpty()),() -> assertEquals("", err.toString()));
            } finally { System.setErr(orig); }
        }
        @Test @DisplayName("Denies, prints error, and notifies when PIN is invalid") void depositDeniedOnInvalidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                Account real = new FakeAccount(); PinAuthenticator auth = new FakeAuthenticator("1234"); AccountProxy proxy = new AccountProxy((Account) real, auth); proxy.deposit(50.0, "bad");
                FakeAccount fa = (FakeAccount) real; FakeAuthenticator au = (FakeAuthenticator) auth;
                assertAll(() -> assertEquals(1, au.validateCalls),() -> assertEquals("bad", au.lastValidatedPin),() -> assertEquals(0, fa.depositCalls),() -> assertEquals(1, fa.notifications.size()),() -> assertEquals("[PROXY] Failed deposit attempt due to incorrect PIN.", fa.notifications.get(0)),() -> assertTrue(err.toString().contains("[ACCESS DENIED]") && err.toString().contains("Deposit")));
            } finally { System.setErr(orig); }
        }
    }

    @Nested @DisplayName("withdraw") class Withdraw {
        @Test @DisplayName("Delegates to real account when PIN is valid") void withdrawDelegatesOnValidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                Account real = new FakeAccount(); PinAuthenticator auth = new FakeAuthenticator("0000"); AccountProxy proxy = new AccountProxy((Account) real, auth); proxy.withdraw(80.0, "0000");
                FakeAccount fa = (FakeAccount) real; FakeAuthenticator au = (FakeAuthenticator) auth;
                assertAll(() -> assertEquals(1, au.validateCalls),() -> assertEquals("0000", au.lastValidatedPin),() -> assertEquals(1, fa.withdrawCalls),() -> assertEquals(80.0, fa.lastWithdrawAmount, 1e-9),() -> assertEquals("0000", fa.lastWithdrawPin),() -> assertTrue(fa.notifications.isEmpty()),() -> assertEquals("", err.toString()));
            } finally { System.setErr(orig); }
        }
        @Test @DisplayName("Denies, prints error, and notifies when PIN is invalid") void withdrawDeniedOnInvalidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                Account real = new FakeAccount(); PinAuthenticator auth = new FakeAuthenticator("ok"); AccountProxy proxy = new AccountProxy((Account) real, auth); proxy.withdraw(20.0, "bad");
                FakeAccount fa = (FakeAccount) real; FakeAuthenticator au = (FakeAuthenticator) auth;
                assertAll(() -> assertEquals(1, au.validateCalls),() -> assertEquals("bad", au.lastValidatedPin),() -> assertEquals(0, fa.withdrawCalls),() -> assertEquals(1, fa.notifications.size()),() -> assertEquals("[PROXY] Failed withdrawal attempt due to incorrect PIN.", fa.notifications.get(0)),() -> assertTrue(err.toString().contains("[ACCESS DENIED]") && err.toString().contains("Withdrawal")));
            } finally { System.setErr(orig); }
        }
    }

    @Nested @DisplayName("checkBalance") class CheckBalance {
        @Test @DisplayName("Delegates and returns balance when PIN is valid") void checkBalanceDelegatesOnValidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                FakeAccount real = new FakeAccount(); real.balanceToReturn = 123.45; FakeAuthenticator auth = new FakeAuthenticator("ok"); AccountProxy proxy = new AccountProxy(real, auth); double result = proxy.checkBalance("ok");
                assertAll(() -> assertEquals(1, auth.validateCalls),() -> assertEquals("ok", auth.lastValidatedPin),() -> assertEquals(1, real.checkBalanceCalls),() -> assertEquals("ok", real.lastCheckPin),() -> assertTrue(real.notifications.isEmpty()),() -> assertEquals(123.45, result, 1e-9),() -> assertEquals("", err.toString()));
            } finally { System.setErr(orig); }
        }
        @Test @DisplayName("Denies, prints error, notifies, and returns -1 when PIN is invalid") void checkBalanceDeniedOnInvalidPin() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                FakeAccount real = new FakeAccount(); FakeAuthenticator auth = new FakeAuthenticator("good"); AccountProxy proxy = new AccountProxy(real, auth); double result = proxy.checkBalance("bad");
                assertAll(() -> assertEquals(1, auth.validateCalls),() -> assertEquals("bad", auth.lastValidatedPin),() -> assertEquals(0, real.checkBalanceCalls),() -> assertEquals(1, real.notifications.size()),() -> assertEquals("[PROXY] Failed balance check attempt due to incorrect PIN.", real.notifications.get(0)),() -> assertTrue(err.toString().contains("[ACCESS DENIED]") && err.toString().contains("Balance")),() -> assertEquals(-1.0, result, 1e-9));
            } finally { System.setErr(orig); }
        }
    }

    @Nested @DisplayName("processMonth") class ProcessMonth {
        @Test @DisplayName("Delegates directly without authentication") void delegatesWithoutAuth() {
            PrintStream orig = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream(); System.setErr(new PrintStream(err));
            try {
                FakeAccount real = new FakeAccount(); FakeAuthenticator auth = new FakeAuthenticator("irrelevant"); AccountProxy proxy = new AccountProxy(real, auth); proxy.processMonth();
                assertAll(() -> assertEquals(1, real.processMonthCalls),() -> assertEquals(0, auth.validateCalls),() -> assertEquals("", err.toString()));
            } finally { System.setErr(orig); }
        }
    }
}
