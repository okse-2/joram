package fr.dyade.aaa.joram.admin;

/**
 * Signals that a method has been invoked while the administrator 
 * was not connected to the server
 */
public class NotConnectedException extends Exception {

    /**
     * Constructs a NotConnectedException with no detail message.
     */
    NotConnectedException() {
        super();
    }

    /**
     * Constructs an IllegalStateException with the specified detail message.
     *
     * @param message specified detail message
     */
    NotConnectedException(String message) {
        super(message);
    }
}
