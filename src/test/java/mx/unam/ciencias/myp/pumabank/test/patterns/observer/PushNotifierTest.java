package mx.unam.ciencias.myp.pumabank.test.patterns.observer;
import mx.unam.ciencias.myp.pumabank.patterns.observer.PushNotifier;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PushNotifier}, verifying notification output behavior and correct handling of normal, repeated, null, and empty message events.
 */
class PushNotifierTest {

    private final PrintStream originalOut = System.out;

    private ByteArrayOutputStream outContent;

    /**
     * 
     * Redirects System.out into a buffer before each test to capture printed output.
     * 
     */
    @BeforeEach

    void redirectStdout() {

        outContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
    }

    /**
     * 
     * Restores System.out to its original stream after each test.
     */
    @AfterEach
    void restoreStdout() {

        System.setOut(originalOut);
    }

    /**
     * Ensures update() prints a notification message with the expected format.
     */
    @Test
    @DisplayName("Prints expected notification line for a normal event")

    void printsNotification() {
        PushNotifier notifier = new PushNotifier();
        String event = "Deposit completed";
        notifier.update(event);

        String expected = "Notification: " + event + System.lineSeparator();
        assertEquals(expected, outContent.toString());
    }

    /**
     * Ensures each update call prints on a new line when called consecutively.
     * 
     */
    @Test
    @DisplayName("Prints a new line for each consecutive update")
    void printsConsecutiveUpdates() {
        PushNotifier notifier = new PushNotifier();
        notifier.update("A");
        notifier.update("B");
        String sep = System.lineSeparator();
        String expected = "Notification: A" + sep + "Notification: B" + sep;

        assertEquals(expected, outContent.toString());
    }

    /**
     * Ensures null and empty event messages do not cause exceptions and are printed in a consistent format.
     * 
     * 
     */
    @Test
    @DisplayName("Handles null and empty events without throwing")
    void handlesNullAndEmpty() {

        PushNotifier notifier = new PushNotifier();
        assertDoesNotThrow(() -> notifier.update(null));
        assertDoesNotThrow(() -> notifier.update(""));

        String sep = System.lineSeparator();
        String expected = "Notification: null" + sep + "Notification: " + sep;
        assertEquals(expected, outContent.toString());

    }

}




