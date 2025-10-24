package mx.unam.ciencias.myp.pumabank;

import java.util.*;
import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import mx.unam.ciencias.myp.pumabank.model.Client;

/**
 * Interactive CLI runner for PumaBank.
 *
 * This class lets the user register clients via the console, automatically create accounts for
 * those clients, and then run a 12-month randomized simulation where deposits, withdrawals,
 * failed-auth attempts and monthly processing occur. A detailed monthly report is written
 * to {@code monthly_operations_log.txt} by the {@code MonthlyLogger}.
 */
public class Main {

    private static final List<String> INTEREST_TYPES = Arrays.asList("MONTHLY", "ANNUAL", "PREMIUM");
    private static final List<String> SERVICE_OPTIONS = Arrays.asList("ANTI_FRAUD", "PREMIUM_ALERTS", "REWARDS");

    public static void main(String[] args) {
        System.out.println("=== PUMA BANK CLI ===\n");

        PumaBankFacade pumaBank = new PumaBankFacade();
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        boolean running = true;
        while (running) {
            System.out.println("Please choose an option:");
            System.out.println("1) Register User");
            System.out.println("2) Delete account");
            System.out.println("3) Consult users");
            System.out.println("4) Simulation");
            System.out.println("5) Exit");
            System.out.print("> ");

            String line = scanner.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException ignored) {}

            switch (choice) {
                case 1:
                    System.out.print("\n Client name: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) name = "Client-" + (pumaBank.getAllClients().size() + 1);
                    System.out.print("\n Client id (leave empty for auto): ");
                    String clientId = scanner.nextLine().trim();
                    if (clientId.isEmpty()) clientId = "CL" + String.format("%03d", pumaBank.getAllClients().size() + 1);
                    Client newClient = pumaBank.registerClient(name, clientId);

                    System.out.print("\n Do you want to enter account data manually (m) or auto-generate (a)? [a]: ");
                    String mode = scanner.nextLine().trim().toLowerCase();
                    if (mode.isEmpty()) mode = "a";

                    if (mode.startsWith("m")) {
                        // Manual entry
                        double initialBalance = 0.0;
                        while (true) {
                            System.out.print("\n Initial balance (e.g. 1000.00): ");
                            String bal = scanner.nextLine().trim();
                            try {
                                initialBalance = Double.parseDouble(bal);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid amount, please enter a numeric value.");
                            }
                        }

                        System.out.print("\n PIN for the account (4 digits, leave empty for auto 0000): ");
                        String pin = scanner.nextLine().trim();
                        if (pin.isEmpty()) pin = "0000";

                        System.out.println("Choose interest type:");
                        for (int i = 0; i < INTEREST_TYPES.size(); i++) {
                            System.out.printf("%d) %s\n", i + 1, INTEREST_TYPES.get(i));
                        }
                        String itChoice = scanner.nextLine().trim();
                        String interest = "MONTHLY";
                        try {
                            int idx = Integer.parseInt(itChoice) - 1;
                            if (idx >= 0 && idx < INTEREST_TYPES.size()) interest = INTEREST_TYPES.get(idx);
                        } catch (Exception ignored) {}

                        System.out.print("\n Add services (comma-separated: ANTI_FRAUD, PREMIUM_ALERTS, REWARDS) or leave empty: ");
                        String servicesLine = scanner.nextLine().trim();
                        List<String> services = null;
                        if (!servicesLine.isEmpty()) {
                            services = new ArrayList<>();
                            String[] parts = servicesLine.split(",");
                            for (String s : parts) {
                                String t = s.trim().toUpperCase();
                                if (SERVICE_OPTIONS.contains(t)) services.add(t);
                            }
                        }

                        pumaBank.createAccount(newClient.getClientId(), initialBalance, pin, interest, services);

                    } else {

                        double initialBalance = Math.round((100 + rand.nextDouble() * 20000) * 100.0) / 100.0;
                        String pin = String.format("%04d", rand.nextInt(10000));
                        String interest = INTEREST_TYPES.get(rand.nextInt(INTEREST_TYPES.size()));


                        List<String> services = new ArrayList<>();
                        int svcCount = rand.nextInt(SERVICE_OPTIONS.size() + 1);
                        List<Integer> idxs = new ArrayList<>();
                        for (int i = 0; i < SERVICE_OPTIONS.size(); i++) idxs.add(i);
                        Collections.shuffle(idxs, rand);
                        for (int i = 0; i < svcCount; i++) services.add(SERVICE_OPTIONS.get(idxs.get(i)));

                        pumaBank.createAccount(newClient.getClientId(), initialBalance, pin, interest, services.isEmpty() ? null : services);
                        System.out.printf("Auto-created account for %s | Balance: $%.2f | PIN: %s | Interest: %s | Services: %s\n",
                                newClient.getName(), initialBalance, pin, interest, services.isEmpty() ? "None" : String.join(", ", services));
                    }

                    break;
                case 2:
                    System.out.print("\n Account id to delete (format clientId-ACC-n): ");
                    String accountId = scanner.nextLine().trim();
                    if (pumaBank.deleteAccount(accountId)) {
                        System.out.println("Account deleted: " + accountId);
                    } else {
                        System.out.println("Account not found: " + accountId);
                    }
                    break;
                case 3:
                    System.out.println("\n Registered clients:");
                    for (Client client : pumaBank.getAllClients()) {
                        Map<String, Object> portfolio = pumaBank.getClientPortfolio(client.getClientId());
            System.out.printf("- %s (ID: %s) | Accounts: %d | Total Balance: $%.2f\n",
                client.getName(), client.getClientId(),
                ((Integer) portfolio.get("totalAccounts")).intValue(),
                ((Double) portfolio.get("totalBalance")).doubleValue());
                    }
                    break;
                case 4:

                    pumaBank.setSuppressLogTimestamps(true);

                    pumaBank.setQuietMode(true);

                    List<String> allAccountIds = collectAllAccountIds(pumaBank);
                    if (allAccountIds.isEmpty()) {
                        System.out.println("No accounts found to simulate.");
                        break;
                    }

                    System.out.print("\n Number of months to simulate (1-12) [12]: ");
                    int months = 12;
                    while (true) {
                        String monthsLine = scanner.nextLine().trim();
                        if (monthsLine.isEmpty()) {
                            months = 12;
                            break;
                        }
                        try {
                            months = Integer.parseInt(monthsLine);
                            if (months < 1 || months > 12) {
                                System.out.print("Please enter a number between 1 and 12: ");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.print("Invalid number, please enter 1-12: ");
                        }
                    }

                    System.out.println("\n Running randomized simulation for " + months + " month(s) (system timestamps suppressed)...");
                    for (int month = 1; month <= months; month++) {

                        int ops = 10 + rand.nextInt(21);
                        for (int op = 0; op < ops; op++) {
                            String accId = allAccountIds.get(rand.nextInt(allAccountIds.size()));
                            int action = rand.nextInt(5);
                            double amount = Math.round((10 + rand.nextDouble() * 2000) * 100.0) / 100.0;
                            try {
                                switch (action) {
                                    case 0:
                                        pumaBank.deposit(accId, amount, "0000");
                                        break;
                                    case 1:
                                        try { pumaBank.withdraw(accId, amount, pickRandomPin()); } catch (Exception ignored) {}
                                        break;
                                    case 2:
                                        try { pumaBank.checkBalance(accId, pickRandomPin()); } catch (Exception ignored) {}
                                        break;
                                    case 3:
                                        try { pumaBank.withdraw(accId, 1.0, "9999"); } catch (Exception ignored) {}
                                        break;
                                    default:
                                        pumaBank.deposit(accId, Math.round((1 + rand.nextDouble() * 100) * 100.0) / 100.0, "0000");
                                }
                            } catch (Exception ignored) {}
                        }
                        pumaBank.processMonthlyOperations(month);
                    }


                    String reportPath = new java.io.File("monthly_operations_log.txt").getAbsolutePath();
                    System.out.println("\n The simulation has been generated correctly. You can view it in the directory: " + reportPath);

                    pumaBank.setQuietMode(false);
                    break;
                case 5:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
            }
            System.out.println();
        }

        System.out.println("Exiting PumaBank CLI.");
        scanner.close();
    }



    private static List<String> collectAllAccountIds(PumaBankFacade facade) {
        List<String> ids = new ArrayList<>();
        for (Client client : facade.getAllClients()) {
            int count = facade.getClientAccounts(client.getClientId()).size();
            for (int i = 1; i <= count; i++) {
                ids.add(client.getClientId() + "-ACC-" + i);
            }
        }
        return ids;
    }

    private static String pickRandomPin() {
        Random r = new Random();
        if (r.nextDouble() < 0.7) return "0000";
        return String.format("%04d", r.nextInt(10000));
    }
}