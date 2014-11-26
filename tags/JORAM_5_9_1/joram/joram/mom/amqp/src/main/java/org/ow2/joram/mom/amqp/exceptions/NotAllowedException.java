package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the client tried to work with some entity in a manner
 * that is prohibited by the server, due to security settings or by some other
 * criteria.
 */
public class NotAllowedException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public NotAllowedException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_ALLOWED;
  }

}
