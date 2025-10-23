package mx.unam.ciencias.myp.pumabank.facade;

import mx.unam.ciencias.myp.pumabank.model.Account;
import mx.unam.ciencias.myp.pumabank.model.Client;
import mx.unam.ciencias.myp.pumabank.model.IAccount;
import mx.unam.ciencias.myp.pumabank.patterns.decorator.*;
import mx.unam.ciencias.myp.pumabank.patterns.observer.MonthlyLogger;
import mx.unam.ciencias.myp.pumabank.patterns.observer.Observer;
import mx.unam.ciencias.myp.pumabank.patterns.observer.PushNotifier;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.AccountProxy;
import mx.unam.ciencias.myp.pumabank.patterns.proxy.PinAuthenticator;
import mx.unam.ciencias.myp.pumabank.patterns.state.AccountState;
import mx.unam.ciencias.myp.pumabank.patterns.state.states.ActiveState;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.InterestCalculation;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.AnnualInterest;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.MonthlyInterest;
import mx.unam.ciencias.myp.pumabank.patterns.strategy.periods.PremiumInterest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main facade for the PumaBank system.
 *
 * <p>This class provides a simplified, high-level API for common banking
 * operations such as client registration, account creation, deposits,
 * withdrawals and monthly processing. It wires together domain objects and
 * cross-cutting concerns such as security proxies, decorators, observer
 * notifications and monthly logging.</p>
 *
 * <p>The facade keeps internal registries of clients, decorated accounts and
 * account proxies. It also collects monthly metrics (transactions, fees,
 * interest) and delegates detailed logging to a {@code MonthlyLogger}.</p>
 */
public class PumaBankFacade {
    private Map<String, Client> clients;
    private Map<String, List<IAccount>> clientAccounts;
    private Map<String, AccountProxy> accountProxies;
    private List<Observer> globalObservers;
    private MonthlyLogger monthlyLogger;
    
    private int monthlyTransactions;
    private double totalFeesCollected;
    private double totalInterestPaid;

    /**
     * Constructs a new PumaBankFacade and initializes internal registries,
     * global observers and the monthly logger.
     */
    public PumaBankFacade() {
        this.clients = new HashMap<>();
        this.clientAccounts = new HashMap<>();
        this.accountProxies = new HashMap<>();
        this.globalObservers = new ArrayList<>();
        this.monthlyLogger = new MonthlyLogger();
        this.monthlyTransactions = 0;
        this.totalFeesCollected = 0;
        this.totalInterestPaid = 0;
        
        registerGlobalObservers();
    }

    /**
     * Registers global observers used across all accounts (for example the
     * monthly logger and push notifications). This is called during
     * construction.
     */
    private void registerGlobalObservers() {
        monthlyLogger.clearLog();
        globalObservers.add(monthlyLogger);
        globalObservers.add(new PushNotifier());
        
        monthlyLogger.logSystemOperation("SYSTEM_START", "PumaBank system initialized");
    }

    /**
     * Registers a new client in the system.
     *
     * @param name     the full name of the client
     * @param clientId a unique identifier for the client
     * @return the newly created {@link Client} instance
     * @throws IllegalArgumentException if {@code clientId} duplicates an
     *                                  existing client (behavior: currently
     *                                  existing client will be overwritten)
     */
    public Client registerClient(String name, String clientId) {
        Client client = new Client(name, clientId);
        clients.put(clientId, client);
        clientAccounts.put(clientId, new ArrayList<>());
        
        monthlyLogger.logSystemOperation("CLIENT_REGISTERED", 
            "Client: " + name + " (ID: " + clientId + ")");
        System.out.println("Cliente registrado: " + client);
        return client;
    }

    /**
     * Creates a new account for an existing client.
     *
     * <p>This method composes several patterns to create a production-ready
     * account: it creates an interest strategy, an account object in an
     * initial {@link ActiveState}, registers global observers on the account,
     * wraps the account with a security {@link AccountProxy} that delegates
     * authentication to a {@link PinAuthenticator}, and then applies any
     * requested decorators (anti-fraud, premium alerts, rewards) on top of
     * the proxy.</p>
     *
     * @param clientId       the identifier of the client who will own the
     *                       account
     * @param initialBalance initial balance for the account
     * @param pin            PIN used by the {@link PinAuthenticator}
     * @param interestType   one of: "MONTHLY", "ANNUAL", "PREMIUM" (case
     *                       insensitive)
     * @param services       optional list of decorator service names
     * @return the {@link AccountProxy} wrapping the created account
     * @throws IllegalArgumentException if the client does not exist or if
     *                                  {@code interestType} is invalid
     */
    public AccountProxy createAccount(String clientId, double initialBalance, String pin, 
                                     String interestType, List<String> services) {
        Client client = clients.get(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Cliente no encontrado: " + clientId);
        }

        InterestCalculation interestPolicy = createInterestPolicy(interestType);
        AccountState initialState = new ActiveState();
        

        Account account = new Account(client, initialBalance, initialState, interestPolicy, this);
        
        for (Observer observer : globalObservers) {
            account.addObserver(observer);
        }

        PinAuthenticator authenticator = new PinAuthenticator(pin);
        AccountProxy accountProxy = new AccountProxy(account, authenticator);

        IAccount decoratedAccount = accountProxy;
        
        if (services != null && !services.isEmpty()) {
            for (String service : services) {
                switch (service.toUpperCase()) {
                    case "ANTI_FRAUD":
                        decoratedAccount = new AntiFraudDecorator(decoratedAccount);
                        break;
                    case "PREMIUM_ALERTS":
                        decoratedAccount = new PremiumAlertsDecorator(decoratedAccount);
                        break;
                    case "REWARDS":
                        decoratedAccount = new RewardsProgramDecorator(decoratedAccount);
                        break;
                    default:
                        System.err.println("Servicio desconocido: " + service);
                }
            }
        }

        String accountId = generateAccountId(clientId);
        accountProxies.put(accountId, accountProxy);
        clientAccounts.get(clientId).add(decoratedAccount);
        
        String servicesText = services != null && !services.isEmpty() ? 
            String.join(", ", services) : "No additional services";
        
        monthlyLogger.logSystemOperation("ACCOUNT_CREATED", 
            String.format("Account: %s | Client: %s | Balance: $%.2f | Interest: %s | Services: %s",
                accountId, client.getName(), initialBalance, interestType, servicesText));
        
        System.out.println("Cuenta creada para " + client.getName() + 
                         " - Saldo inicial: $" + initialBalance);
        
        return accountProxy;
    }

    /**
     * Creates an interest calculation policy based on a textual type.
     *
     * @param interestType one of "MONTHLY", "ANNUAL", or "PREMIUM"
     * @return an {@link InterestCalculation} implementation
     * @throws IllegalArgumentException if the supplied type is not supported
     */
    private InterestCalculation createInterestPolicy(String interestType) {
        switch (interestType.toUpperCase()) {
            case "MONTHLY":
                return new MonthlyInterest(0.01, 1000.0);
            case "ANNUAL":
                AnnualInterest annual = new AnnualInterest(0.12, 50000.0);
                annual.setCurrentMonth(12); // Configurar para que pague en diciembre
                return annual;
            case "PREMIUM":
                return new PremiumInterest(0.015, 100000.0, 500000.0, 0.005, 0.01);
            default:
                throw new IllegalArgumentException("Tipo de interés no válido: " + interestType);
        }
    }

    /**
     * Deposits an amount into the decorated account identified by
     * {@code accountId}.
     *
     * @param accountId account identifier previously returned by
     *                  {@link #generateAccountId(String)}
     * @param amount    amount to deposit (must be positive according to
     *                  underlying account rules)
     * @param pin       authentication PIN used by the security proxy
     * @throws IllegalArgumentException if the account cannot be found
     */
    public void deposit(String accountId, double amount, String pin) {
        IAccount account = findDecoratedAccount(accountId);
        if (account != null) {
            monthlyLogger.logSystemOperation("DEPOSIT_ATTEMPT", 
                String.format("Account: %s | Amount: $%.2f", accountId, amount));
            account.deposit(amount, pin);
            recordTransaction();
        } else {
            throw new IllegalArgumentException("Cuenta no encontrada: " + accountId);
        }
    }

    /**
     * Withdraws an amount from the decorated account identified by
     * {@code accountId}.
     *
     * @param accountId account identifier
     * @param amount    amount to withdraw
     * @param pin       authentication PIN
     * @throws IllegalArgumentException if the account cannot be found
     */
    public void withdraw(String accountId, double amount, String pin) {
        IAccount account = findDecoratedAccount(accountId);
        if (account != null) {
            monthlyLogger.logSystemOperation("WITHDRAWAL_ATTEMPT", 
                String.format("Account: %s | Amount: $%.2f", accountId, amount));
            account.withdraw(amount, pin);
            recordTransaction();
        } else {
            throw new IllegalArgumentException("Cuenta no encontrada: " + accountId);
        }
    }

    /**
     * Returns the current balance of the decorated account.
     *
     * @param accountId account identifier
     * @param pin       authentication PIN
     * @return the account balance as a {@code double}
     * @throws IllegalArgumentException if the account cannot be found
     */
    public double checkBalance(String accountId, String pin) {
        IAccount account = findDecoratedAccount(accountId);
        if (account != null) {
            monthlyLogger.logSystemOperation("BALANCE_CHECK", 
                String.format("Account: %s", accountId));
            double balance = account.checkBalance(pin);
            recordTransaction();
            return balance;
        } else {
            throw new IllegalArgumentException("Cuenta no encontrada: " + accountId);
        }
    }

    /**
     * Processes monthly operations for all accounts.
     *
     * <p>This will iterate every decorated account and call
     * {@code processMonth()} on the {@link IAccount} interface to allow
     * decorators, strategies and states to apply monthly fees, interest and
     * state transitions. The method also collects aggregated metrics and
     * produces a monthly report via {@link MonthlyLogger}.</p>
     */
    public void processMonthlyOperations() {

        monthlyLogger.startMonthlyReport();
        monthlyLogger.logSystemOperation("MONTHLY_PROCESSING_START", 
            "Starting monthly operations for " + getTotalAccounts() + " accounts");
        
        monthlyTransactions = 0;
        totalFeesCollected = 0;
        totalInterestPaid = 0;
        
        System.out.println("=== INICIANDO PROCESOS MENSUALES PUMA BANK ===");
        
        for (List<IAccount> accounts : clientAccounts.values()) {
            for (IAccount account : accounts) {
                String accountId = getAccountId(account);
                
                try {
                    monthlyLogger.logSystemOperation("ACCOUNT_PROCESSING_START", 
                        "Processing account: " + accountId);
                    
                    System.out.println("Procesando cuenta: " + accountId);
                    account.processMonth();
                    
                    monthlyTransactions++;
                    monthlyLogger.logSystemOperation("ACCOUNT_PROCESSING_END", 
                        "Completed processing account: " + accountId);
                        
                } catch (Exception e) {
                    monthlyLogger.logSystemOperation("ACCOUNT_PROCESSING_ERROR", 
                        "Error processing account " + accountId + ": " + e.getMessage());
                    System.err.println("Error procesando cuenta " + accountId + ": " + e.getMessage());
                }
            }
        }

        monthlyLogger.logSystemOperation("MONTHLY_PROCESSING_END", 
            "Completed monthly operations. Transactions: " + monthlyTransactions);
        monthlyLogger.endMonthlyReport(
            getTotalAccounts(), 
            monthlyTransactions, 
            totalFeesCollected, 
            totalInterestPaid
        );
        
        System.out.println("=== PROCESOS MENSUALES COMPLETADOS ===");
        System.out.println("Reporte detallado guardado en: monthly_operations_log.txt");
    }

    /**
     * Finds the decorated {@link IAccount} for a given account identifier.
     *
     * @param accountId account identifier in the form {@code clientId-ACC-n}
     * @return the decorated {@link IAccount} or {@code null} if not found
     */
    private IAccount findDecoratedAccount(String accountId) {
        String clientId = accountId.split("-")[0];
        List<IAccount> accounts = clientAccounts.get(clientId);
        if (accounts != null) {
            for (IAccount account : accounts) {
                if (getAccountId(account).equals(accountId)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the account id corresponding to a decorated
     * {@link IAccount} instance.
     *
     * <p>The method traverses decorator wrappers until it reaches the
     * underlying {@link AccountProxy} and then looks up the proxy in the
     * internal registry to return its identifier.</p>
     *
     * @param account a decorated {@link IAccount}
     * @return the account identifier or {@code "UNKNOWN-ACCOUNT"} if the
     *         mapping cannot be resolved
     */
    private String getAccountId(IAccount account) {
        IAccount current = account;
        while (current instanceof AccountDecorator) {
            current = ((AccountDecorator) current).decoratedAccount;
        }
        
        if (current instanceof AccountProxy) {
            for (Map.Entry<String, AccountProxy> entry : accountProxies.entrySet()) {
                if (entry.getValue() == current) {
                    return entry.getKey();
                }
            }
        }
        
        return "UNKNOWN-ACCOUNT";
    }

    /**
     * Returns the total number of registered accounts (decorated instances)
     * across all clients.
     */
    private int getTotalAccounts() {
        return clientAccounts.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Records a collected fee amount for monthly metrics and logs the
     * operation.
     *
     * @param fee the fee amount to add to the monthly total
     */
    public void recordFeeCollection(double fee) {
        this.totalFeesCollected += fee;
        monthlyLogger.logSystemOperation("FEE_RECORDED", 
            String.format("Fee: $%.2f | Total Fees: $%.2f", fee, totalFeesCollected));
    }

    /**
     * Records an interest payment amount for monthly metrics and logs the
     * operation.
     *
     * @param interest amount of interest paid
     */
    public void recordInterestPayment(double interest) {
        this.totalInterestPaid += interest;
        monthlyLogger.logSystemOperation("INTEREST_RECORDED", 
            String.format("Interest: $%.2f | Total Interest: $%.2f", interest, totalInterestPaid));
    }

    /**
     * Increments the monthly transaction counter by one. This is called by
     * facade operations that represent user-triggered interactions.
     */
    public void recordTransaction() {
        this.monthlyTransactions++;
    }

    /**
     * Retrieves the real underlying account from a decorated
     * {@link IAccount}, bypassing decorators and proxies.
     *
     * @param account a decorated {@link IAccount}
     * @return the underlying {@link IAccount} instance
     */
    private IAccount getRealAccount(IAccount account) {
    IAccount current = account;
    
    while (current instanceof AccountDecorator) {
        current = ((AccountDecorator) current).decoratedAccount;
    }
    
    if (current instanceof AccountProxy) {
        try {
            Field realAccountField = AccountProxy.class.getDeclaredField("realAccount");
            realAccountField.setAccessible(true);
            return (IAccount) realAccountField.get(current);
        } catch (Exception e) {
            return current;
        }
    }
    
    return current;
}

    /**
     * Returns a simplified portfolio summary for the given client.
     *
     * @param clientId the client identifier
     * @return a map containing portfolio information (keys: "client",
     *         "totalAccounts", "accounts")
     * @throws IllegalArgumentException if the client is not registered
     */
    public Map<String, Object> getClientPortfolio(String clientId) {
        List<IAccount> accounts = clientAccounts.get(clientId);
        if (accounts == null) {
            throw new IllegalArgumentException("Cliente no encontrado: " + clientId);
        }

        double totalBalance = 0.0;
        for (IAccount account : accounts) {
            try {

                IAccount realAccount = getRealAccount(account);
                if (realAccount instanceof Account) {
                    double balance = ((Account) realAccount).getBalance();
                    totalBalance += balance;
                }
            } catch (Exception e) {
                System.err.println("Error obteniendo balance para portafolio: " + e.getMessage());
            }
        }

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("client", clients.get(clientId));
        portfolio.put("totalAccounts", accounts.size());
        portfolio.put("totalBalance", totalBalance);
        portfolio.put("accounts", new ArrayList<>(accounts));

        monthlyLogger.logSystemOperation("PORTFOLIO_QUERY", 
            String.format("Client: %s | Accounts: %d | Total Balance: $%.2f", 
                clientId, accounts.size(), totalBalance));

        return portfolio;
    }

    /**
     * Generates a unique account identifier for a client's next account.
     *
     * @param clientId the client identifier
     * @return a string in the format {@code clientId-ACC-n}
     */
    private String generateAccountId(String clientId) {
        int accountNumber = clientAccounts.get(clientId).size() + 1;
        return clientId + "-ACC-" + accountNumber;
    }

    /**
     * Returns the list of decorated accounts for a client. If the client
     * has no accounts or does not exist, an empty list is returned.
     *
     * @param clientId the client identifier
     * @return list of {@link IAccount} instances (may be empty)
     */
    public List<IAccount> getClientAccounts(String clientId) {
        return clientAccounts.getOrDefault(clientId, new ArrayList<>());
    }

    /**
     * Returns the {@link AccountProxy} registered under {@code accountId},
     * or {@code null} if not found.
     *
     * @param accountId the account identifier
     * @return the {@link AccountProxy} or {@code null}
     */
    public AccountProxy findAccount(String accountId) {
        return accountProxies.get(accountId);
    }

    /**
     * Returns a snapshot list of all registered clients.
     *
     * @return a list containing every {@link Client} currently registered
     */
    public List<Client> getAllClients() {
        return new ArrayList<>(clients.values());
    }

    /**
     * Returns the total fees collected during the current monthly cycle.
     *
     * @return total fees collected
     */
    public double getTotalFeesCollected() {
        return totalFeesCollected;
    }

    /**
     * Returns the total interest paid during the current monthly cycle.
     *
     * @return total interest paid
     */
    public double getTotalInterestPaid() {
        return totalInterestPaid;
    }

    /**
     * Returns the number of transactions processed during the current monthly cycle.
     *
     * @return number of monthly transactions
     */
    public int getMonthlyTransactions() {
        return monthlyTransactions;
    }
}