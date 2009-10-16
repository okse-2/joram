package org.objectweb.joram.mom.amqp.exceptions;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public class NotImplementedException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public NotImplementedException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_IMPLEMENTED;
  }

}
