package org.objectweb.joram.mom.amqp.exceptions;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public class InternalErrorException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public InternalErrorException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.INTERNAL_ERROR;
  }

}
