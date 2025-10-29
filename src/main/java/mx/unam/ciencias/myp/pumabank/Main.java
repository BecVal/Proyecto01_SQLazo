package mx.unam.ciencias.myp.pumabank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
    private static final String[] MONTH_NAMES = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public static void main(String[] args) {
        System.out.println("=== PUMA BANK CLI ===\n");
        System.out.println("  _____                       ____              _    ");
        System.out.println(" |  __ \\                     |  _ \\            | |   ");
        System.out.println(" | |__) |   _ _ __ ___   __ _| |_) | __ _ _ __ | | __");
        System.out.println(" |  ___/ | | | '_ ` _ \\ / _` |  _ < / _` | '_ \\| |/ /");
        System.out.println(" | |   | |_| | | | | | | (_| | |_) | (_| | | | |   < ");
        System.out.println(" |_|    \\__,_|_| |_| |_|\\__,_|____/ \\__,_|_| |_|_|\\_\\");



        System.out.println("─────────────────────────────────────────────▓▓█──────────────────────");
        System.out.println("───────────────────────────────────────────▒██▒▒█─────────────────────");
        System.out.println("──────────────────────────────────────────█▓▓▓░▒▓▓────────────────────");
        System.out.println("────────────────────────────────────────▒█▓▒█░▒▒▒█────────────────────");
        System.out.println("───────────────────────────────────────▒█▒▒▒█▒▒▒▒▓▒───────────────────");
        System.out.println("────────────────▓▓▒░──────────────────▓█▒▒▒▓██▓▒░▒█───────────────────");
        System.out.println("────────────────█▓▓██▓░──────────────▓█▒▒▒▒████▒▒▒█───────────────────");
        System.out.println("────────────────▓█▓▒▒▓██▓░──────────▒█▒▒▒▒▒██▓█▓░░▓▒──────────────────");
        System.out.println("────────────────▓█▓▒▒▓██▓░──────────▒█▒▒▒▒▒██▓█▓░░▓▒──────────────────");
        System.out.println("────────────────▓░█▒▒▒▒▒▒▒▓▓█▓█▓▓▓▓▒▒▒▒▒▒▒▒██▓██▒░▒█──────────────────");
        System.out.println("────────────────▓░▓█▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▓████▒▒▒█──────────────────");
        System.out.println("────────────────▓░▓██▒▒▒▒▒▒▒▒▒▒▒▒▒▓▓▒▒▒▒▒▒▒▒▒▓██░░░█──────────────────");
        System.out.println("────────────────▓░▓███▒▒▒▒▒▒▒▒▒▒▒▓█▒▒▒▒▒▒▒▒▒▒▒▒▓▓▓▒▓▓─────────────────");
        System.out.println("────────────────▒▒▒██▓▒▓█▓▒▒▒▒▒▒▒▓▒▒▒▒▒▒▓▓▓▒▒▒▒▒▒▒▓▒█─────────────────");
        System.out.println("─────────────────▓▒█▓▒▒▒▒▓▒▒▒▒▒▒▒▒▒▒▒▓█▓▓▓▓█▓▒▒▒▒▒▒▒▓▒────────────────");
        System.out.println("─────────────────▓▒█▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▓▓──────▓█▓▒▒▒▒▒▓█────────────────");
        System.out.println("─────────────────▒▒▓▒▒▒▓▓▓▒▒▒▒▒▒▒▒▒▓▓───░▓▓───█▓▒▒▒▒▒█────────────────");
        System.out.println("──────────────────█▒▒▓▓▓▒▒▓▓▒▒▒▒▒▒▓▓───█████▓──█▓▒▒▒▒▓▒───────────────");
        System.out.println("──────────────────▓▓█▒─────▒▓▒▒▒▒▒█───░██████──░█▒▒▒▒▓▓───────────────");
        System.out.println("──────────────────▓█▒──▒███─▒▓▒▒▒▒█────██████───▓▒▒▒▒▒▓───────────────");
        System.out.println("──────────────────██───█████─█▒▒▒▒█─────███▓────▓▓▒▒▒▒▓───────────────");
        System.out.println("──────────────────█▓───█████─▒▓▒▒▒█─────────────█▓▓▓▒▒▓───────────────");
        System.out.println("──────────────────█▓───░███──░▓▒▒▒▓█──────────░█▓▒▒▒▓▒▓───────────────");
        System.out.println("──────────────────██─────────▒▓▒▒▒▒▓▓──────░▒▓█▓────░▓▓───────────────");
        System.out.println("──────────────────▓█░────────█▓██▓▒▒▓█▓▓▓▓██▓▓▒▓▒░░▒▓▒▓───────────────");
        System.out.println("──────────────────▒██░──────▓▒███▓▒▒▒▒▓▓▓▓▒▒▒▒▒▒▓▓▓▓▒▓────────────────");
        System.out.println("───────────────────█▓█▓▓▒▒▓█▓▒░██▒▒▓▓█▓▒▒▒▒▒▒▒▒▒▒▒▒▓▓█▒───────────────");
        System.out.println("───────────────────▓─░▓▓▓▓▓▒▓▓▓▓▒▓▓▓▒▓▒▒▒▒▒▒▒▒▒▒▒▓▓▓▓▓▓───────────────");
        System.out.println("───────────────────▒▒▒▓▒▒▒▒▒▒▓█░─░░░─▓▓▒▒▒▒▒▒▒▒▒▒▒▓██▓▒───────────────");
        System.out.println("────────────────────█▓▒▒▒▒▒▒▒▒▓▓─░░░─▓▓▒▒▒▒▒▒▒▒▒▓▓▓▒▒▓▒───────────────");
        System.out.println("─────────────────────██▓▓▒▒▒▒▒▒█▒░░░░█▒▒▒▒▒▒▒▒▓█▓▓▒▒▒▒▒───────────────");
        System.out.println("────────────────────░─▒██▓▓▒▒▒▒▒█▓▒▒▓▒▒▒▒▒▒▓███▓▒▒▒▒▒▓▓───────────────");
        System.out.println("─────────────────────────░▒▓▓▓▓▒▒▓▓▓▓▓▓████▓▓█▒▒▒▒▒▓▓█░───────────────");






        PumaBankFacade pumaBank = new PumaBankFacade();
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        boolean running = true;
        while (running) {
            System.out.println("Please choose a menu:");
            System.out.println("1) Quick Menu (For development testing)");
            System.out.println("2) User Menu");
            System.out.println("3) Exit");
            System.out.print("> ");

            String line = scanner.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException ignored) {}

            switch (choice) {
                case 1:
                    runDeveloperMenu(pumaBank, scanner, rand);
                    break;
                case 2:
                    runUserMenu(pumaBank, scanner, rand);
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 3.");
            }
            System.out.println();
        }

        System.out.println("Exiting PumaBank CLI.");
        scanner.close();
    }

    /**
     * Runs the developer-focused menu with quick access to all features.
     */
    private static void runDeveloperMenu(PumaBankFacade pumaBank, Scanner scanner, Random rand) {
        boolean devMenuRunning = true;
        while (devMenuRunning) {
            System.out.println("\n--- Quick Menu (Development) ---");
            System.out.println("1) Register User");
            System.out.println("2) Delete account");
            System.out.println("3) Consult users");
            System.out.println("4) Run full simulation");
            System.out.println("5) Back to main menu");
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
                    List<Client> clients = pumaBank.getAllClients();
                    if (clients.isEmpty()) {
                        System.out.println("No clients registered yet.");
                    } else {
                        clients.forEach(c -> System.out.printf("- %s (ID: %s)\n", c.getName(), c.getClientId()));
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
                    devMenuRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
            }
            System.out.println();
        }
    }

    /**
     * Runs the user-focused menu, guiding them through account creation and basic operations.
     */
    private static void runUserMenu(PumaBankFacade pumaBank, Scanner scanner, Random rand) {
        System.out.println("\n--- User Menu ---");
        System.out.println("Welcome! Let's start by creating a new account for you.");

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "User-" + (pumaBank.getAllClients().size() + 1);

        String pin;
        while (true) {
            System.out.print("Create a 4-digit PIN for your account: ");
            pin = scanner.nextLine().trim();
            if (pin.matches("\\d{4}")) { 
                break;
            } else {
                System.out.println("Invalid PIN. Please enter exactly 4 digits.");
            }
        }

        double initialBalance = 0.0;
        while (true) {
            System.out.print("Enter initial deposit amount (e.g., 1500.00): ");
            String balStr = scanner.nextLine().trim();
            try {
                initialBalance = Double.parseDouble(balStr);
                if (initialBalance < 0) {
                    System.out.println("Initial balance cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a numeric value.");
            }
        }

        System.out.print("Add extra services? (comma-separated: ANTI_FRAUD, PREMIUM_ALERTS, REWARDS) or leave empty: ");
        String servicesLine = scanner.nextLine().trim().toUpperCase();
        List<String> services = new ArrayList<>();
        if (!servicesLine.isEmpty()) {
            for (String s : servicesLine.split(",")) {
                String service = s.trim();
                if (SERVICE_OPTIONS.contains(service)) {
                    services.add(service);
                }
            }
        }

        String clientId = "USR" + String.format("%03d", pumaBank.getAllClients().size() + 1);
        Client newClient = pumaBank.registerClient(name, clientId);
        pumaBank.createAccount(clientId, initialBalance, pin, "MONTHLY", services.isEmpty() ? null : services);
        String accountId = clientId + "-ACC-1"; // Manually construct the ID for the client's first account.

        System.out.printf("\nAccount created successfully for %s!\n", name);
        System.out.printf("  - Account ID: %s\n", accountId);
        System.out.println("------------------------------------");

        boolean userMenuRunning = true;
        int currentMonthIndex = 0;

        while (userMenuRunning) {
            System.out.printf("\n--- User Operations (Current Month: %s) ---\n", MONTH_NAMES[currentMonthIndex]);
            System.out.println("1) Make a deposit");
            System.out.println("2) Make a withdrawal");
            System.out.println("3) Check balance");
            System.out.println("4) View basic account information");
            System.out.println("5) Go to next month");
            System.out.println("6) Exit / End user simulation");
            System.out.print("> ");

            String line = scanner.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException ignored) {}

            try {
                switch (choice) {
                    case 1: 
                        System.out.print("Amount to deposit: ");
                        double depositAmount = Double.parseDouble(scanner.nextLine().trim());
                        String depositPin = promptForPin(scanner);
                        pumaBank.deposit(accountId, depositAmount, depositPin);
                        break;
                    case 2:
                        System.out.print("Amount to withdraw: ");
                        double withdrawAmount = Double.parseDouble(scanner.nextLine().trim());
                        String withdrawPin = promptForPin(scanner);
                        pumaBank.withdraw(accountId, withdrawAmount, withdrawPin);
                        break;
                    case 3: 
                        String balancePin = promptForPin(scanner);
                        double balance = pumaBank.checkBalance(accountId, balancePin);
                        if (balance != -1) {
                            System.out.printf("Current balance: $%.2f\n", balance);
                        }
                        break;
                    case 4: 
                        String infoPin = promptForPin(scanner);
                        double currentBalance = pumaBank.checkBalance(accountId, infoPin);
                        if (currentBalance != -1) {
                            System.out.println("Basic Account Information:");
                            System.out.printf("  - Holder: %s\n", name);
                            System.out.printf("  - Account ID: %s\n", accountId);
                            System.out.printf("  - Current Balance: $%.2f\n", currentBalance);
                        }
                        break;
                    case 5: 
                        pumaBank.processMonthlyOperations(currentMonthIndex + 1);
                        currentMonthIndex = (currentMonthIndex + 1) % 12;
                        System.out.printf("Processed end-of-month operations. Now in %s.\n", MONTH_NAMES[currentMonthIndex]);
                        if (currentMonthIndex == 0) {
                            System.out.println("A full year has passed. Annual interest (if applicable) has been applied.");
                        }
                        break;
                    case 6: 
                        userMenuRunning = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please choose a number from the menu.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
        System.out.println("Ending user session. Returning to main menu.");
    }

    /**
     * Prompts the user to enter their PIN for an operation.
     * @param scanner The scanner to read input from.
     * @return The PIN entered by the user.
     */
    private static String promptForPin(Scanner scanner) {
        System.out.print("Enter your PIN to confirm: ");
        return scanner.nextLine().trim();
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