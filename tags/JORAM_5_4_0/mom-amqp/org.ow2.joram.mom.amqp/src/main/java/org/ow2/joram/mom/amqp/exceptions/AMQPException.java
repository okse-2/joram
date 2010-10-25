package org.ow2.joram.mom.amqp.exceptions;

public abstract class AMQPException extends Exception {

  private static final long serialVersionUID = 1L;

  public AMQPException(String message) {
    super(message);
  }

  public abstract int getCode();

}
