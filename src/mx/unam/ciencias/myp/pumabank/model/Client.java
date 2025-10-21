package mx.unam.ciencias.myp.pumabank.model;

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
     * @param name the client's full name
     * @param clientId the unique client identifier
     */
    public Client(String name, String clientId) {
        this.name = name;
        this.clientId = clientId;
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
