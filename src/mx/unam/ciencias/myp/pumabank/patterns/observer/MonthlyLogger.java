package mx.unam.ciencias.myp.pumabank.patterns.observer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer implementation that records account events in a log file.
 * 
 * <p>
 * Each time an event occurs, it appends an entry to{@code monthly_log.txt}.
 * </p>
 */
public class MonthlyLogger implements Observer {


private static final String LOG_FILE = "monthly_operations_log.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Appends detailed event information to the monthly log file.
     * @param event description of the account event with context
     */
    @Override
    public void update(String event) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format("[%s] %s", timestamp, event);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Logs system-level operations (not triggered by account events).
     * @param operation the system operation description
     * @param details additional details about the operation
     */
    public void logSystemOperation(String operation, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format("[%s] [SYSTEM] %s - %s", timestamp, operation, details);
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing system log: " + e.getMessage());
        }
    }

    /**
     * Logs monthly summary for an account.
     * @param accountId the account identifier
     * @param previousBalance balance at start of month
     * @param currentBalance balance at end of month
     * @param interestEarned interest applied this month
     * @param feesCharged fees applied this month
     */
    public void logMonthlySummary(String accountId, double previousBalance, 
                                 double currentBalance, double interestEarned, 
                                 double feesCharged) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format(
            "[%s] [MONTHLY_SUMMARY] Account: %s | Previous: $%.2f | Current: $%.2f | " +
            "Interest: $%.2f | Fees: $%.2f | Net Change: $%.2f",
            timestamp, accountId, previousBalance, currentBalance, 
            interestEarned, feesCharged, (currentBalance - previousBalance)
        );
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println(logEntry);
            writer.println("[MONTHLY_SUMMARY] " + "=".repeat(80));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing monthly summary: " + e.getMessage());
        }
    }

    /**
     * Logs account state transitions.
     * @param accountId the account identifier
     * @param fromState previous state
     * @param toState new state
     * @param reason reason for state change
     */
    public void logStateChange(String accountId, String fromState, String toState, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format(
            "[%s] [STATE_CHANGE] Account: %s | %s -> %s | Reason: %s",
            timestamp, accountId, fromState, toState, reason
        );
        
        logToFile(logEntry);
    }

    /**
     * Logs security-related events.
     * @param accountId the account identifier
     * @param eventType type of security event
     * @param details event details
     */
    public void logSecurityEvent(String accountId, String eventType, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format(
            "[%s] [SECURITY] Account: %s | %s | %s",
            timestamp, accountId, eventType, details
        );
        
        logToFile(logEntry);
    }

    /**
     * Logs service-related events (fees, activations, etc.).
     * @param accountId the account identifier
     * @param serviceType type of service
     * @param action action performed
     * @param amount amount involved (if any)
     */
    public void logServiceEvent(String accountId, String serviceType, String action, double amount) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format(
            "[%s] [SERVICE] Account: %s | %s | %s | Amount: $%.2f",
            timestamp, accountId, serviceType, action, amount
        );
        
        logToFile(logEntry);
    }

    /**
     * Helper method to write to log file.
     */
    private void logToFile(String logEntry) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Creates a monthly report header.
     */
    public void startMonthlyReport() {
        startMonthlyReport(true);
    }

    /**
     * Starts the monthly report header. When {@code includeTimestamp} is false,
     * the header will not include the system date/time. This is useful for
     * deterministic simulations where external timestamps are undesired.
     *
     * @param includeTimestamp whether to include the current system timestamp in the header
     */
    public void startMonthlyReport(boolean includeTimestamp) {
        startMonthlyReport(0, includeTimestamp);
    }

    /**
     * Starts the monthly report header for a specific simulated month.
     *
     * @param simulatedMonth the simulated month number (1..12). If 0, month is unspecified.
     * @param includeTimestamp whether to include the current system timestamp in the header
     */
    public void startMonthlyReport(int simulatedMonth, boolean includeTimestamp) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println("=".repeat(100));
            writer.println("PUMA BANK - MONTHLY OPERATIONS REPORT");
            if (simulatedMonth > 0) {
                writer.println("Simulated Month: " + simulatedMonth);
            }
           
            if (includeTimestamp) {
                writer.println("Report Date: " + timestamp);
            }
            writer.println("=".repeat(100));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing report header: " + e.getMessage());
        }
    }

    /**
     * Creates a monthly report footer with summary.
     */
    public void endMonthlyReport(int totalAccounts, int transactionsProcessed, double totalFees, double totalInterest) {
        endMonthlyReport(totalAccounts, transactionsProcessed, totalFees, totalInterest, true);
    }

    /**
     * Writes the monthly report footer. When {@code includeTimestamp} is false,
     * the generated timestamp line will include a simulation marker instead of the system date.
     */
    public void endMonthlyReport(int totalAccounts, int transactionsProcessed, double totalFees, double totalInterest, boolean includeTimestamp) {
        endMonthlyReport(totalAccounts, transactionsProcessed, totalFees, totalInterest, includeTimestamp, 0);
    }

    /**
     * Writes the monthly report footer for a specific simulated month.
     *
     * @param totalAccounts number of accounts processed
     * @param transactionsProcessed total transactions processed
     * @param totalFees total fees collected
     * @param totalInterest total interest paid
     * @param includeTimestamp whether to include system timestamp
     * @param simulatedMonth the simulated month number (1..12). If 0, month is unspecified.
     */
    public void endMonthlyReport(int totalAccounts, int transactionsProcessed, double totalFees, double totalInterest, boolean includeTimestamp, int simulatedMonth) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.println("=".repeat(100));
            writer.println("MONTHLY REPORT SUMMARY");
            if (simulatedMonth > 0) {
                writer.println("Simulated Month: " + simulatedMonth);
            }

            if (includeTimestamp) {
                writer.println("Generated: " + timestamp);
            }
            writer.printf("Total Accounts Processed: %d%n", totalAccounts);
            writer.printf("Total Transactions: %d%n", transactionsProcessed);
            writer.printf("Total Fees Collected: $%.2f%n", totalFees);
            writer.printf("Total Interest Paid: $%.2f%n", totalInterest);
            writer.println("=".repeat(100));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing report footer: " + e.getMessage());
        }
    }

    /**
     * Clears the log file by truncating it. Use this at application startup
     * if you want each run to overwrite previous logs rather than append.
     */
    public void clearLog() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, false), StandardCharsets.UTF_8))) {
            // opening with append=false truncates the file
            writer.print("");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }
}