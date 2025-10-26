package mx.unam.ciencias.myp.pumabank.test.patterns.proxy;

import mx.unam.ciencias.myp.pumabank.patterns.proxy.PinAuthenticator;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PinAuthenticator}, verifying correct validation behavior,null handling, and case-sensitive PIN comparison.
 */
class PinAuthenticatorTest {

    @Nested
    @DisplayName("validate() behavior")

    class ValidateTests {

        /**
         * Ensures validate returns true when provided PIN matches the stored PIN exactly.
         * 
         */
        @Test
        @DisplayName("Returns true when input PIN matches stored PIN")
        void returnsTrueOnMatch() {

            PinAuthenticator auth = new PinAuthenticator("1234");
            assertTrue(auth.validate("1234"), "Expected true when PINs match exactly");

        }

        /**
         * Ensures validate returns false when the input PIN does not match the stored PIN.
         * 
         */
        @Test
        @DisplayName("Returns false when input PIN is different")
        void returnsFalseOnMismatch() {

            PinAuthenticator auth = new PinAuthenticator("1234");
            assertFalse(auth.validate("9999"), "Expected false when PINs do not match");
        }

        /**
         * 
         * Ensures null input never validates successfully.
         */
        @Test
        @DisplayName("Returns false when input PIN is null")
        void returnsFalseOnNullInput() {

            PinAuthenticator auth = new PinAuthenticator("1234");
            assertFalse(auth.validate(null), "Null input should not match any stored PIN");
        }

        /**
         * Ensures stored PIN being null prevents any successful validation.
         */
        @Test
        @DisplayName("Returns false when stored PIN is null")
        void returnsFalseWhenStoredPinIsNull() {

            PinAuthenticator auth = new PinAuthenticator(null);

            assertFalse(auth.validate("1234"), "Should fail validation when stored PIN is null");
        }

        /**
         * Verifies that validation requires exact string match, including character case.
         */
        @Test
        @DisplayName("Returns true only for exact string equality (case-sensitive)")
        void isCaseSensitive() {
            PinAuthenticator auth = new PinAuthenticator("ABcd");

            assertTrue(auth.validate("ABcd"), "Exact match should pass");

            assertFalse(auth.validate("abcd"), "Different case should fail");
        }
    }

    @Nested
    @DisplayName("Constructor behavior")
    class ConstructorTests {

        /**
         * Ensures constructor sets stored PIN and validation works immediately after creation.
         * 
         */
        @Test
        @DisplayName("Stores the PIN correctly and can validate immediately")
        void storesPinCorrectly() {
            PinAuthenticator auth = new PinAuthenticator("7777");
            assertTrue(auth.validate("7777"));

            assertFalse(auth.validate("0000"));
        }

        /**
         * Ensures constructor allows null stored PIN without throwing.
         */
        @Test
        @DisplayName("Accepts null PIN without throwing an exception")
        void acceptsNullWithoutException() {
            
            assertDoesNotThrow(() -> new PinAuthenticator(null),"Constructor should allow null storedPin");
        }
    }
}
