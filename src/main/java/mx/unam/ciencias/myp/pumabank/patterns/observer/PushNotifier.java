
package mx.unam.ciencias.myp.pumabank.patterns.observer;

/**
 * Observer implementation that simulates sending real-time notifications.
 * <p>
 * When an account event occurs, it prints a notification message to the console.
 * 
 * </p>
 */
public class PushNotifier implements Observer {

    /**
     * 
     * Displays a notification message for the received event.
     * @param event description of the account event
     */
    @Override
    public void update(String event) {

        System.out.println("Notification: " + event);
    }
}
