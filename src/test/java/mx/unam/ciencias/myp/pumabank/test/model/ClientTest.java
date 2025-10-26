package mx.unam.ciencias.myp.pumabank.test.model;
import mx.unam.ciencias.myp.pumabank.model.Client;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Client} model class.
 * 
 * Verifies constructor behavior, immutability, toString format, and basic equality semantics.
 */
class ClientTest {

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        /**
         * Ensures provided values are stored and returned correctly.
         */
        @Test
        @DisplayName("Stores name and clientId correctly")
        void constructorStoresValues() {
            Client c = new Client("Victor", "C-001");

            assertAll(() -> assertEquals("Victor", c.getName()),() -> assertEquals("C-001", c.getClientId()));
        }

        /**
         * Ensures nulls are allowed without causing exceptions.
         */
        @Test
        @DisplayName("Allows null values without throwing")
        void allowsNullValues() {

            assertDoesNotThrow(() -> new Client(null, null));

            Client c1 = new Client(null, "C-001");
            Client c2 = new Client("Victor", null);
            Client c3 = new Client(null, null);

            assertAll(() -> assertNull(c1.getName()),() -> assertEquals("C-001", c1.getClientId()),() -> assertEquals("Victor", c2.getName()),() -> assertNull(c2.getClientId()),() -> assertNull(c3.getName()),() -> assertNull(c3.getClientId()));
        }

        /**
         * Ensures empty or whitespace-only values are accepted as-is.
         */
        @Test
        @DisplayName("Allows empty or blank strings")
        void allowsEmptyOrBlank() {

            Client c1 = new Client("", "C-001");
            Client c2 = new Client("Victor", "");
            Client c3 = new Client("   \t\n", "   ");

            assertAll(() -> assertEquals("", c1.getName()),() -> assertEquals("C-001", c1.getClientId()),() -> assertEquals("Victor", c2.getName()),() -> assertEquals("", c2.getClientId()),() -> assertEquals("   \t\n", c3.getName()),() -> assertEquals("   ", c3.getClientId()));
        }
    }

    @Nested
    @DisplayName("toString method")
    class ToStringTests {

        /**
         * 
         * Verifies correct formatting of the toString output.
         */
        @Test
        @DisplayName("Returns expected format: 'Name (ID: XYZ)'")
        void correctFormat() {
            Client c = new Client("Victor", "A-123");
            assertEquals("Victor (ID: A-123)", c.toString());
        }

        /**
         * Ensures toString works when name or ID are null.
         */
        @Test
        @DisplayName("Gracefully handles nulls in toString")
        void toStringWithNulls() {

            Client c1 = new Client(null, "A-123");
            Client c2 = new Client("Victor", null);
            Client c3 = new Client(null, null);
            assertAll(() -> assertEquals("null (ID: A-123)", c1.toString()),() -> assertEquals("Victor (ID: null)", c2.toString()),() -> assertEquals("null (ID: null)", c3.toString()));
        }

        /**
         * Ensures blanks are preserved when formatting the string.
         */
        @Test
        @DisplayName("Handles blanks in toString")

        void toStringWithBlanks() {

            Client c = new Client("   ", "");
            assertEquals("    (ID: )", c.toString());
        }


    }
    @Nested
    @DisplayName("Immutability")
    class Immutability {
        /**
         * Verifies that client fields are final and cannot be reassigned.
         */
        @Test
        @DisplayName("Fields 'name' and 'clientId' are final")
        void fieldsAreFinal() throws Exception {
            Field nameField = Client.class.getDeclaredField("name");

            Field idField = Client.class.getDeclaredField("clientId");
            assertAll(() -> assertTrue(Modifier.isFinal(nameField.getModifiers()), "'name' should be final"),() -> assertTrue(Modifier.isFinal(idField.getModifiers()), "'clientId' should be final"));
        }

        /**
         * Ensures no internal mutable state is externally exposed.
         * 
         */
        @Test
        @DisplayName("Does not expose mutable state")
        void doesNotExposeMutableState() {

            Client c = new Client("Victor", "ID-1");
            assertAll(() -> assertEquals("Victor", c.getName()),() -> assertEquals("ID-1", c.getClientId()));

        }
    }

    @Nested
    @DisplayName("equals/hashCode semantics")
    class EqualsHashCode {

        /**
         * 
         * Ensures equality is reference-based since equals() is not overridden.
         */
        @Test
        @DisplayName("Distinct instances are not equal by default")
        void distinctInstancesNotEqual() {
            Client a = new Client("Victor", "X");
            Client b = new Client("Victor", "X");
            assertNotEquals(a, b, "equals() is not overridden; distinct objects should not be equal");
            
        }
    }
}
