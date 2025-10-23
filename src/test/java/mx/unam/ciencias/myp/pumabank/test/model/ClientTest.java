package mx.unam.ciencias.myp.pumabank.test.model;
import mx.unam.ciencias.myp.pumabank.model.Client;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClientTest {

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Stores name and clientId correctly")
        void constructorStoresValues() {
            Client c = new Client("Victor", "C-001");
            assertAll(() -> assertEquals("Victor", c.getName()), () -> assertEquals("C-001", c.getClientId()));
        }

        @Test
        @DisplayName("Rejects null name")
        void rejectsNullName() {
            NullPointerException ex = assertThrows(NullPointerException.class,() -> new Client(null, "C-001"));
            assertEquals("name cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("Rejects null clientId")
        void rejectsNullClientId() {
            NullPointerException ex = assertThrows(NullPointerException.class,() -> new Client("Victor", null));
            assertEquals("clientId cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("Rejects empty name")
        void rejectsEmptyName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Client("", "C-001"));
            assertEquals("name cannot be empty or blank", ex.getMessage());
        }

        @Test
        @DisplayName("Rejects blank name (whitespace only)")
        void rejectsBlankName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> new Client("   \t\n", "C-001"));
            assertEquals("name cannot be empty or blank", ex.getMessage());
        }

        @Test
        @DisplayName("Rejects empty clientId")
        void rejectsEmptyClientId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> new Client("Victor", ""));
            assertEquals("clientId cannot be empty or blank", ex.getMessage());
        }

        @Test
        @DisplayName("Rejects blank clientId (whitespace only)")
        void rejectsBlankClientId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> new Client("Victor", "   "));
            assertEquals("clientId cannot be empty or blank", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("toString method")
    class ToStringTests {

        @Test
        @DisplayName("Returns expected format: 'Name (ID: XYZ)'")
        void correctFormat() {
            Client c = new Client("Victor", "A-123");
            assertEquals("Victor (ID: A-123)", c.toString());
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("Fields 'name' and 'clientId' are final")
        void fieldsAreFinal() throws Exception {
            Field nameField = Client.class.getDeclaredField("name");
            Field idField = Client.class.getDeclaredField("clientId");

            assertAll(() -> assertTrue(Modifier.isFinal(nameField.getModifiers()), "'name' should be final"),() -> assertTrue(Modifier.isFinal(idField.getModifiers()), "'clientId' should be final"));
        }

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

        @Test
        @DisplayName("Distinct instances are not equal by default")
        void distinctInstancesNotEqual() {
            Client a = new Client("Victor", "X");
            Client b = new Client("Victor", "X");
            assertNotEquals(a, b, "equals() is not overridden; distinct objects should not be equal");
        }
    }
}
