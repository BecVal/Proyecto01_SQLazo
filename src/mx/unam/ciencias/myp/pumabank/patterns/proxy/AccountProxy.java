package mx.unam.ciencias.myp.pumabank.patterns.proxy;

import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.IAccount;


public class AccountProxy implements IAccount {
    private Account realAccount;
    private PinAuthenticator authenticator;

    public AccountProxy(Account realAccount, PinAuthenticator authenticator) {
        this.realAccount = realAccount;
        this.authenticator = authenticator;
    }

    @Override
    public void deposit(double amount, String pin) {
        if (authenticator.validate(pin)) {
            realAccount.deposit(amount, pin);
        } else {
            System.out.println("[ACCESS DENIED] Incorrect PIN. Deposit not completed.");
            realAccount.notify("Failed deposit attempt due to incorrect PIN.");
        }
    }

    @Override
    public void withdraw(double amount, String pin) {
        if (authenticator.validate(pin)) {
            realAccount.withdraw(amount, pin);
        } else {
            System.out.println("[ACCESS DENIED] Incorrect PIN. Withdrawal not completed.");
            realAccount.notify("Failed withdrawal attempt due to incorrect PIN.");
        }
    }

    @Override
    public double checkBalance(String pin) {
        if (authenticator.validate(pin)) {
            return realAccount.checkBalance(pin);
        } else {
            System.out.println("[ACCESS DENIED] Incorrect PIN. Balance check not completed.");
            realAccount.notify("Failed balance check attempt due to incorrect PIN.");
            return -1;
        }
    }

    @Override
    public void processMonth() {
        realAccount.processMonth();
    }
}