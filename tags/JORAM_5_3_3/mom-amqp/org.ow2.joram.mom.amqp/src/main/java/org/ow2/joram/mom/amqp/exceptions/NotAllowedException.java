package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

public class NotAllowedException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public NotAllowedException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_ALLOWED;
  }

}
