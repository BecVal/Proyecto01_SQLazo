package mx.unam.ciencias.myp.pumabank.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mx.unam.ciencias.myp.pumabank.patterns.observer.Observer;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;;

/**
 * This represents a bank account within the PumaBank system.
 * 
 * <p>
 * This class works in the {@code State} design pattern, delegating its behavior to the current {@link AccountState} instance.
 * It also uses the {@code Strategy} pattern for interest calculation through the {@link InterestCalculation} interface, and follows the {@code Observer} pattern to notify registered observers when significant events occur.
 * </p>
 *
 */
public class Account implements IAccount {


    private double balance;

    private List<String> history;
    private AccountState state;
    private InterestCalculation interestPolicy;
    private Client client;

    private List<Observer> observers;
    private PumaBankFacade facade;


    /**
     * Constructs a new {@code Account}.
     *
     * @param client the client who owns the account
     * @param initialBalance the starting balance for the account
     * @param initialState the initial operational state
     * @param interestPolicy the strategy used for interest calculations
     * @throws NullPointerException if any of the non-null parameters are null
     */
    public Account(Client client, double initialBalance, AccountState initialState, InterestCalculation interestPolicy, PumaBankFacade facade) {
        this.client = Objects.requireNonNull(client);
        this.state = Objects.requireNonNull(initialState);
        this.interestPolicy = Objects.requireNonNull(interestPolicy);
        this.balance = initialBalance;
        this.history = new ArrayList<>();
        this.facade = Objects.requireNonNull(facade);
        this.observers = new ArrayList<>();
    }

    /**
     * Returns the current balance of the account.
     * <p>
     * PIN validation is handled by the {@code AccountProxy}; this method assumes that authentication has already occurred.
     * </p>
     *
     * @param pin the account PIN which we ignore in this context
     * @return the current balance
     * 
     */

    @Override
    public double checkBalance(String pin) {
        return balance;
    }

    /**
     * Deposits the specified amount into the account.
     * 
     * Delegates the actual behavior to the current {@link AccountState}.
     *
     * @param amount the amount to deposit
     */
    @Override
    public void deposit(double amount, String pin) {
        validatePositive(amount, "Deposit amount");

        if ("SYSTEM".equals(pin) || "0000".equals(pin)) {
            state.deposit(amount, this);
            addHistory("System deposit: $" + amount + " | Balance: $" + balance);
        } else {
            state.deposit(amount, this);
        }
    }

    /**
     * Withdraws the specified amount from the account.
     * Delegates the behavior to the current {@link AccountState}.
     *
     * @param amount the amount to withdraw
     * @param pin the PIN required for authentication
     * 
     */
    @Override
    public void withdraw(double amount, String pin) {
        validatePositive(amount, "Withdraw amount");

    if ("SYSTEM".equals(pin) || "0000".equals(pin)) {
            state.withdraw(amount, this);
            addHistory("System withdrawal: $" + amount + " | Balance: $" + balance);
        } else {
            state.withdraw(amount, this);
        }

    }

    /**
     * Triggers monthly processing.
     * Delegates the behavior to the current {@link AccountState}.
     * 
     */
    @Override
    public void processMonth() {
        state.processMonth(this);
    }

    /**
     * Ensures the provided amount is positive.
     *
     * @param amount the amount to validate
     * @param label  the label for the validation error message
     * @throws IllegalArgumentException if {@code amount} is not greater than zero
     */
    private static void validatePositive(double amount, String label) {

        if (amount <= 0) throw new IllegalArgumentException(label + " must be > 0");
    }

    /**
     * 
     * 
     * Notifies all registered observers of the specified event.
     *
     * @param event a description of the event that occurred
     */
    public void notify(String event){
        for(Observer o : observers){
            try{
                o.update(event);
            } catch(Exception ignore){
            }
        }
    }

    /**
     * Registers a new observer to receive event notifications.
     * @param o the observer to add
     */
    public void addObserver(Observer o){
        observers.add(o);
    }

    /**
     * Removes an observer from the notification list.
     *
     * @param o the observer to remove
     */
    public void removeObserver(Observer o){

        observers.remove(o);
    }

    /**
     * Changes the operational state of the account.
     *
     * @param newState the new state to transition into
     * @throws NullPointerException if {@code newState} is null
     */
    public void changeState(AccountState newState){
        this.state = Objects.requireNonNull(newState);
    }

    /**
     * Returns the interest calculation strategy used by this account.
     * @return the current interest calculation strategy
     */
    public InterestCalculation getInterestPolicy(){

        return interestPolicy;
    }

    /**
     * Updates the account’s interest calculation policy.
     * Adds an entry to the history indicating the change.
     * @param interestPolicy the new interest policy
     * @throws NullPointerException if {@code interestPolicy} is null
     */
    public void setInterestPolicy(InterestCalculation interestPolicy){
        this.interestPolicy = Objects.requireNonNull(interestPolicy);

        addHistory("Interest policy changed");
    }

    /**
     * Adds an event message to the account’s history.
     * @param event the event description to add
     */
    public void addHistory(String event){

        history.add(event);
    }

    /**
     * Records a fee applied for the monthly report.
     */
    public void recordFee(double fee) {
        if (facade != null) {
            facade.recordFeeCollection(fee);
        }
    }
    
    /**
     * Records an interest payment for the monthly report.
     */
    public void recordInterest(double interest) {
        if (facade != null) {
            facade.recordInterestPayment(interest);
        }
    }

    public Account(Client client, double initialBalance, AccountState initialState, InterestCalculation interestPolicy) {
        this(client, initialBalance, initialState, interestPolicy, null);
    }

    /**
     * Returns the list of all recorded events in this account’s history.
     *
     * @return the list of event descriptions
     */
    public List<String> getHistory(){
        return history;
    }

    /**
     * 
     * Returns the current account balance.
     *
     * @return the balance amount
     * 
     */
    
    public double getBalance(){
        return balance;
    }

    /**
     * 
     * Updates the account balance.
     *
     * @param balance the new balance amount
     */
    public void setBalance(double balance){
        this.balance = balance;
    }

    /**
     * Returns the client who owns this account.
     * @return the client associated with this account
     */
    public Client getClient(){
        return client;
    }
}
