package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

public class InternalErrorException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public InternalErrorException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.INTERNAL_ERROR;
  }

}
