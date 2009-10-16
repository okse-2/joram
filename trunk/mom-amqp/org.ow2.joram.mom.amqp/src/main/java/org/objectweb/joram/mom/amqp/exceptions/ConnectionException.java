package org.objectweb.joram.mom.amqp.exceptions;

public abstract class ConnectionException extends AMQPException {

  private static final long serialVersionUID = 1L;

  public ConnectionException(String message) {
    super(message);
  }

}
