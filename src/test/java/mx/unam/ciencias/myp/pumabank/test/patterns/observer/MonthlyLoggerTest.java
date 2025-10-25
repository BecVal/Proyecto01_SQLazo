package mx.unam.ciencias.myp.pumabank.test.patterns.observer;
import mx.unam.ciencias.myp.pumabank.patterns.observer.MonthlyLogger;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.*;
class MonthlyLoggerTest {
    private static final Path LOG_PATH = Paths.get("monthly_operations_log.txt");
    @BeforeEach
    void cleanUpBefore() throws IOException {
        if (Files.exists(LOG_PATH)) Files.delete(LOG_PATH);
    }

    @AfterEach
    void cleanUpAfter() throws IOException {
        if (Files.exists(LOG_PATH)) Files.delete(LOG_PATH);
    }

    @Test
    @DisplayName("update writes one line with the event appended")
    void updateWritesOneLine() throws Exception {
        MonthlyLogger logger = new MonthlyLogger();
        String event = "Account opened";
        assertDoesNotThrow(() -> logger.update(event));

        assertTrue(Files.exists(LOG_PATH), "log file should be created");

        List<String> lines = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);
        assertAll(() -> assertEquals(1, lines.size(), "Exactly one line expected after single update"),() -> assertTrue(lines.get(0).contains(event)),() -> assertTrue(lines.get(0).startsWith("[")),() -> assertTrue(lines.get(0).contains("] ")));
    }

    @Test
    @DisplayName("Consecutive updates append lines rather than overwrite")
    void updatesAppend() throws Exception {
        MonthlyLogger logger = new MonthlyLogger();
        String e1 = "Deposit: $100";
        String e2 = "Withdrawal: $25";
        logger.update(e1);
        logger.update(e2);
        List<String> lines = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);

        assertAll(() -> assertEquals(2, lines.size(), "Two lines expected after two updates"),() -> assertTrue(lines.get(0).contains(e1)),() -> assertTrue(lines.get(1).contains(e2)));

    }

    @Test
    @DisplayName("Swallows IOException when path is a directory")
    void swallowsIOExceptionOnBadPath() throws Exception {
        Files.createDirectory(LOG_PATH);
        assertTrue(Files.isDirectory(LOG_PATH), "Precondition: path is a directory");
        MonthlyLogger logger = new MonthlyLogger();

        assertDoesNotThrow(() -> logger.update("This should not be written"));
        
        assertTrue(Files.isDirectory(LOG_PATH), "Path should remain a directory");
    }
}
