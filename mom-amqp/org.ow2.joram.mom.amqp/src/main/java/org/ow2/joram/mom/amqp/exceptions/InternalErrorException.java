package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the server could not complete the method because of an
 * internal error. The server may require intervention by an operator in order
 * to resume normal operations.
 */
public class InternalErrorException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public InternalErrorException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.INTERNAL_ERROR;
  }

}
