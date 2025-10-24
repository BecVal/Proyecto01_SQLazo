package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;

public class AntiFraudDecorator extends AccountDecorator{

    private static final double ANTI_FRAUD_FEE = 50.0;

    public AntiFraudDecorator(IAccount decoratedAccount) {
        super(decoratedAccount);
    }

    /**
     * Deposits the specified amount into the account and sends an anti-fraud alert.
     *
     * @param amount the amount to deposit
     * @param pin    the PIN for authentication
     */
    @Override
    public void deposit(double amount, String pin) {
        validateTransaction(amount, "deposit");
        super.deposit(amount, pin);
    }

    /**
     * Withdraws the specified amount from the account and sends an anti-fraud alert.
     *
     * @param amount the amount to withdraw
     * @param pin    the PIN for authentication
     */
    @Override
    public void withdraw(double amount, String pin) {
        validateTransaction(amount, "withdraw");
        super.withdraw(amount, pin);
    }

    /**
     * Processes the monthly operations, including applying the anti-fraud service fee.
     */
    @Override
    public void processMonth() {
        notify(String.format("SERVICE_FEE_PENDING: Anti-Fraud Protection - $%.2f", ANTI_FRAUD_FEE));
        
        super.withdraw(ANTI_FRAUD_FEE, "SYSTEM");
        
        recordFee(ANTI_FRAUD_FEE);
        
        addHistory("Anti-fraud service fee applied: $" + ANTI_FRAUD_FEE);
        
        notify(String.format("SERVICE_FEE_APPLIED: Anti-Fraud Protection - $%.2f", ANTI_FRAUD_FEE));
        notify("Anti-fraud protection active. Monthly fee: $" + ANTI_FRAUD_FEE);
        
        super.processMonth();
    }

    /**
     * Validates transactions for potential fraud.
     *
     * @param amount the transaction amount
     * @param operationType the type of operation (deposit or withdraw)
     */
    private void validateTransaction(double amount, String operationType) {
        if (amount > 10000) {
            addHistory("Suspicious " + operationType + " detected: $" + amount);
            notify(String.format("FRAUD_ALERT: Large %s of $%.2f requires verification", 
                operationType, amount));
        }
    }
}