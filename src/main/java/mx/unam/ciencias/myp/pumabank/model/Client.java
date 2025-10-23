package mx.unam.ciencias.myp.pumabank.model;

import java.util.Objects;

/**
 * Represents a bank client with an identifier and name.
 * 
 * <p>
 * Each client is associated with one or more accounts and serves as the account holder in the system. This class is immutable, meaning client data cannot be changed once created.
 * 
 * </p>
 * 
 */
public class Client {


    private final String name;
    private final String clientId;

    /**
     * Creates a new {@code Client} with the given name and identifier.
     *
     * @param name the client's full name (must not be null or blank)
     * @param clientId the unique client identifier (must not be null or blank)
     * @throws NullPointerException if name or clientId is null
     * @throws IllegalArgumentException if name or clientId is empty or blank
     */
    public Client(String name, String clientId) {
        this.name = validateNotBlank(Objects.requireNonNull(name, "name cannot be null"), "name");
        this.clientId = validateNotBlank(Objects.requireNonNull(clientId, "clientId cannot be null"), "clientId");
    }

    /**
     * Validates that a string is not empty or composed only of whitespace.
     */
    private static String validateNotBlank(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty or blank");
        }
        return value;
    }


    /**
     * Returns the client's name.
     * @return the client's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the client's unique identifier.
     * @return the client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns a string representation of the client.
     * @return a string with client details
     * 
     */
    @Override
    public String toString() {
        return name + " (ID: " + clientId + ")";
    }
}
