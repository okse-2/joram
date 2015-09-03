package org.ow2.joram.mom.amqp.exceptions;

public class InterruptedException extends InternalErrorException {

  private static final long serialVersionUID = 1L;

  public InterruptedException(String message) {
    super(message);
  }

}
