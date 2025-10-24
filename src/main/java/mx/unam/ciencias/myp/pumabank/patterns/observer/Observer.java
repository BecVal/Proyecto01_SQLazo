package mx.unam.ciencias.myp.pumabank.patterns.observer;

/**
 * Defines the Observer interface used in the Observer design pattern.
 * <p>
 * Implementing classes receive notifications from subjects whenever relevant events occur.
 * 
 * </p>
 * 
 * 
 */
public interface Observer {

    /**
     * Called by the subject to notify the observer of an event.
     * @param message description of the event
     */
    void update(String message);

    
}