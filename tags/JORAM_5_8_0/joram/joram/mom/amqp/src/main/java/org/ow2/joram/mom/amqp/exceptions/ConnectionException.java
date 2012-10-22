package org.ow2.joram.mom.amqp.exceptions;

/**
 * These exceptions are all associated with failures that preclude any further
 * activity on the connection and require its closing.
 */
public abstract class ConnectionException extends AMQPException {

  private static final long serialVersionUID = 1L;

  public ConnectionException(String message) {
    super(message);
  }

}
