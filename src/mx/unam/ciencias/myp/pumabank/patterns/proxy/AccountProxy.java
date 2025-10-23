package mx.unam.ciencias.myp.pumabank.patterns.proxy;

import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.IAccount;

/**
 * Acts as a security proxy for an {@link Account} object.
 * <p>
 * This class implements the {@link IAccount} interface and controls access to the
 * real account object ({@code realAccount}). It intercepts method calls to
 * perform PIN authentication before delegating the request. This is an
 * application of the Proxy design pattern.
 * </p>
 *
 * @author Cesar
 * @see Account
 * @see PinAuthenticator
 */
public class AccountProxy implements IAccount {
    private Account realAccount;
    private PinAuthenticator authenticator;

    /**
     * Constructs a new {@code AccountProxy}.
     *
     * @param realAccount   The actual {@link Account} instance to which operations will be delegated.
     * @param authenticator The {@link PinAuthenticator} used to validate the user's PIN.
     */
    public AccountProxy(Account realAccount, PinAuthenticator authenticator) {
        this.realAccount = realAccount;
        this.authenticator = authenticator;
    }

    /**
     * Authenticates the user and, if successful, deposits the specified amount.
     * <p>
     * If the PIN is valid, the call is forwarded to the real account. Otherwise,
     * an access denied message is printed and a failure notification is sent.
     * </p>
     *
     * @param amount The amount to deposit.
     * @param pin    The user's PIN for authentication.
     */
    @Override
    public void deposit(double amount, String pin) {
        if (authenticator.validate(pin)) {
            realAccount.deposit(amount, pin);
        } else {
            System.err.println("[ACCESS DENIED] Incorrect PIN. Deposit not completed.");
            realAccount.notify("Failed deposit attempt due to incorrect PIN.");
        }
    }

    /**
     * Authenticates the user and, if successful, withdraws the specified amount.
     * <p>
     * If the PIN is valid, the call is forwarded to the real account. Otherwise,
     * an access denied message is printed and a failure notification is sent.
     * </p>
     *
     * @param amount The amount to withdraw.
     * @param pin    The user's PIN for authentication.
     */
    @Override
    public void withdraw(double amount, String pin) {
        if (authenticator.validate(pin)) {
            realAccount.withdraw(amount, pin);
        } else {
            System.err.println("[ACCESS DENIED] Incorrect PIN. Withdrawal not completed.");
            realAccount.notify("Failed withdrawal attempt due to incorrect PIN.");
        }
    }

    /**
     * Authenticates the user and, if successful, returns the account balance.
     * <p>
     * If the PIN is valid, it returns the balance from the real account. Otherwise,
     * an access denied message is printed, a failure notification is sent, and it
     * returns {@code -1} to indicate failure.
     * </p>
     *
     * @param pin The user's PIN for authentication.
     * @return The account balance if authentication is successful; {@code -1} otherwise.
     */
    @Override
    public double checkBalance(String pin) {
        if (authenticator.validate(pin)) {
            return realAccount.checkBalance(pin);
        } else {
            System.err.println("[ACCESS DENIED] Incorrect PIN. Balance check not completed.");
            realAccount.notify("Failed balance check attempt due to incorrect PIN.");
            return -1;
        }
    }

    /**
     * Delegates the monthly processing call directly to the real account.
     * No authentication is required for this operation.
     */
    @Override
    public void processMonth() {
        realAccount.processMonth();
    }

    /**
     * Records a fee on the underlying account. This is exposed so decorators
     * can invoke fee recording through the proxy without needing reflection.
     *
     * @param fee the fee amount to record
     */
    public void recordFee(double fee) {
        try {
            realAccount.recordFee(fee);
        } catch (Exception e) {

            System.err.println("Error recording fee on account: " + e.getMessage());
        }
    }

    /**
     * Records an interest payment on the underlying account.
     *
     * @param interest the interest amount to record
     */
    public void recordInterest(double interest) {
        try {
            realAccount.recordInterest(interest);
        } catch (Exception e) {
            System.err.println("Error recording interest on account: " + e.getMessage());
        }
    }

    /**
     * Adds an entry to the underlying account history.
     * @param event the history event to add
     */
    public void addHistory(String event) {
        try {
            realAccount.addHistory(event);
        } catch (Exception e) {
            System.err.println("Error adding history to account: " + e.getMessage());
        }
    }

    /**
     * Notifies observers registered on the underlying account.
     * @param message the message to notify
     */
    public void notifyObservers(String message) {
        try {
            realAccount.notify(message);
        } catch (Exception e) {
            System.err.println("Error notifying observers: " + e.getMessage());
        }
    }

    /**
     * Returns the underlying real {@link Account} instance.
     * <p>
     * This accessor avoids the need for reflection in higher-level code
     * that needs to inspect or operate on the concrete account object.
     * </p>
     *
     * @return the wrapped {@link Account}
     */
    public Account getUnderlyingAccount() {
        return realAccount;
    }
}