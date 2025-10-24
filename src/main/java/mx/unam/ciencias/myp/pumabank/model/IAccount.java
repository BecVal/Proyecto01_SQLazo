
package mx.unam.ciencias.myp.pumabank.model;
/**
 * Defines the core operations of a bank account in the PumaBank system.
 * <p>
 * Implemented by both the real {@link Account} and its Proxy counterpart.
 * 
 * Supports the use of the State and Strategy design patterns.
 * 
 * </p>
 */
public interface IAccount {
    /**
     * Deposits the given amount into the account.
     * PIN validation is handled by the Proxy.
     * @param amount the amount to deposit
     * @param pin the client PIN for authentication
     */
    void deposit(double amount, String pin);

    /**
     * Withdraws the given amount from the account.
     * PIN validation is handled by the Proxy.
     * @param amount the amount to withdraw
     * @param pin the client PIN for authentication
     */

    void withdraw(double amount, String pin);

    /**
     * Returns the current balance of the account.
     * PIN verification is done in the Proxy.
     * @param pin the client PIN
     * @return the account balance
     */

    double checkBalance(String pin);

    /**
     * Processes monthly operations such as interest or fees.
     * 
     */
    void processMonth();
}