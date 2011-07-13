package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the client tried to use functionality that is not
 * implemented in the server.
 */
public class NotImplementedException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public NotImplementedException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_IMPLEMENTED;
  }

}
