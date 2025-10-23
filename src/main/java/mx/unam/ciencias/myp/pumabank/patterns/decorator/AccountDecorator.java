package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;

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
     * Attempts to call the {@code addHistory(String)} method on the decorated account.
     * <p>
     * 
     * This protected hook uses reflection to find and invoke a method named {@code addHistory} (with a single {@code String} parameter) on the decorated account.
     * If such a method does not exist or any reflective exception occurs, the exception is swallowed.
     * 
     * </p>
     *
     * @param event the event description to pass to the underlying account.
     */
    protected void addHistory(String event) {
        invokeHook("addHistory", event);
    }

    /**
     * Attempts to call a notification hook on the decorated account.
     * <p>
     * This method first looks for a method named {@code notify(String)} on the decorated account and invokes it using reflection. If no such method exists, it will then try {@code notifyObservers(String)} instead.
     * All reflective exceptions are swallowed.
     * 
     * </p>
     *
     * 
     * @param message the message to send to the underlying account, if applicable
     */
    protected void notify(String message) {
        if (!invokeHook("notify", message)) invokeHook("notifyObservers", message);
    }

    /**
     * Utility method that performs the actual reflective lookup and invocation.
     * 
     * <p>
     * This helper method searches the decorated account's class hierarchy for a declared method with the given name and a single {@code String} parameter.
     * If found, it makes the method accessible and invokes it. If the method does not exist or an exception occurs during invocation, the method returns {@code false}.
     * 
     * </p>
     *
     * @param methodName the name of the method to look for
     * 
     * @param arg the string argument to pass to the invoked method
     * @return {@code true} if the method was found and successfully invoked; {@code false} otherwise
     * 
     * 
     */
    private boolean invokeHook(String methodName, String arg) {
        Class<?> c = decoratedAccount.getClass();
        while (c != null) {
            try {
                var m = c.getDeclaredMethod(methodName, String.class);
                m.setAccessible(true);
                m.invoke(decoratedAccount, arg);
                return true;
            } catch (NoSuchMethodException e) {
                c = c.getSuperclass();
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }
        return false;
    }

}
