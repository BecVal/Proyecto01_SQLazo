package mx.unam.ciencias.myp.pumabank.patterns.observer;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDateTime;

/**
 * Observer implementation that records account events in a log file.
 * 
 * <p>
 * Each time an event occurs, it appends an entry to{@code monthly_log.txt}.
 * </p>
 */
public class MonthlyLogger implements Observer {

    /**
     * Appends the received event to the monthly log file.
     * @param event description of the account event
     */

    @Override
    public void update(String event) {
        try (FileWriter fw = new FileWriter("monthly_log.txt", true)) {
            fw.write(LocalDateTime.now() + " - " + event + "\n");
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }

    
}
