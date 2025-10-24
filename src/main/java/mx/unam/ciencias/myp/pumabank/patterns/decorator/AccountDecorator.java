package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.AccountProxy;

import java.lang.reflect.Method;

/**
 * Abstract decorator class for {@link IAccount} implementations.
 * <p>
 * This class follows the {@code Decorator} design pattern, allowing for dynamic addition of responsibilities to {@link IAccount} objects.
 * It implements the {@link IAccount} interface and contains a reference to an {@link IAccount} object to which it delegates method calls.
 * </p>
 */
public abstract class AccountDecorator implements IAccount {
    public IAccount decoratedAccount;

    /**
     * Constructs an {@code AccountDecorator} that wraps the specified {@link IAccount}.
     *
     * @param decoratedAccount the {@link IAccount} instance to be decorated
     */
    public AccountDecorator(IAccount decoratedAccount) {
        this.decoratedAccount = decoratedAccount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deposit(double amount, String pin) {
        decoratedAccount.deposit(amount, pin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void withdraw(double amount, String pin) {
        decoratedAccount.withdraw(amount, pin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double checkBalance(String pin) {
        return decoratedAccount.checkBalance(pin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processMonth() {
        decoratedAccount.processMonth();
    }

    /**
     * Adds an event to the account's history.
     *
     * @param event the event to be added
     */
    protected void addHistory(String event) {
        if (decoratedAccount instanceof AccountProxy) {
            ((AccountProxy) decoratedAccount).addHistory(event);
            return;
        }

        if (decoratedAccount instanceof AccountDecorator) {
            ((AccountDecorator) decoratedAccount).addHistory(event);
            return;
        }
    }

    /**
     * Notifies observers with a message.
     *
     * @param message the message to be sent to observers
     */
    protected void notify(String message) {
        if (decoratedAccount instanceof AccountProxy) {
            ((AccountProxy) decoratedAccount).notifyObservers(message);
            return;
        }

        if (decoratedAccount instanceof AccountDecorator) {
            ((AccountDecorator) decoratedAccount).notify(message);
            return;
        }
    }

    /**
     * Records a fee charged to the account.
     *
     * @param fee the fee amount to be recorded
     */
    protected void recordFee(double fee) {
    IAccount current = decoratedAccount;
        while (current instanceof AccountDecorator) {
            current = ((AccountDecorator) current).decoratedAccount;
        }

        if (current instanceof AccountProxy) {
            ((AccountProxy) current).recordFee(fee);
            return;
        }

        try {
            Method m = current.getClass().getMethod("recordFee", double.class);
            m.invoke(current, fee);
        } catch (Exception e) {
            System.err.println("Could not record fee: " + e.getMessage());
        }
    }

    /**
     * Records interest earned.
     * @param interest the interest amount to be recorded
     */
    protected void recordInterest(double interest) {
        IAccount current = decoratedAccount;
        while (current instanceof AccountDecorator) {
            current = ((AccountDecorator) current).decoratedAccount;
        }

        if (current instanceof AccountProxy) {
            ((AccountProxy) current).recordInterest(interest);
            return;
        }

        try {
            Method m = current.getClass().getMethod("recordInterest", double.class);
            m.invoke(current, interest);
        } catch (Exception e) {
            System.err.println("Could not record interest: " + e.getMessage());
        }
    }
}