package mx.unam.ciencias.myp.pumabank.patterns.proxy;

/**
 * A simple component responsible for authenticating a user's Personal Identification Number (PIN).
 * <p>
 * This class stores the correct PIN for an account and provides a method
 * to validate an input PIN against it. It is typically used by a security
 * proxy like {@link AccountProxy} to control access to sensitive operations.
 * </p>
 *
 * @author Cesar
 */
public class PinAuthenticator {
    private String storedPin;

    /**
     * Constructs a new {@code PinAuthenticator} with the correct PIN.
     *
     * @param storedPin The secret PIN that will be used for future validations.
     */
    public PinAuthenticator(String storedPin) {
        this.storedPin = storedPin;
    }

    /**
     * Validates a given input PIN against the stored PIN.
     *
     * @param inputPin The PIN provided by the user to attempt authentication.
     * @return {@code true} if the input PIN matches the stored PIN, {@code false} otherwise.
     */
    public boolean validate(String inputPin) {
        return storedPin != null && storedPin.equals(inputPin);
    }
}