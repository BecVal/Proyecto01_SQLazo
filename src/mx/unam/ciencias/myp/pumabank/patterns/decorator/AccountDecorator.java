package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract decorator class for {@link IAccount} implementations.
 * <p>
 * This class follows the {@code Decorator} design pattern, allowing for dynamic addition of responsibilities to {@link IAccount} objects.
 * It implements the {@link IAccount} interface and contains a reference to an {@link IAccount} object to which it delegates method calls.
 * </p>
 */
public abstract class AccountDecorator implements IAccount {
    protected IAccount decoratedAccount;

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
    public void deposit(double amount) {
        decoratedAccount.deposit(amount);
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
        try {
            Method m = decoratedAccount.getClass().getMethod("addHistory", String.class);
            m.invoke(decoratedAccount, event);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            
        }
    }

    /**
     * Notifies observers with a message.
     *
     * @param message the message to be sent to observers
     */
    protected void notify(String message) {
        try {
            Method m = decoratedAccount.getClass().getMethod("notify", String.class);
            m.invoke(decoratedAccount, message);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
           
        }
    }
}
