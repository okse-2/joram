package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the client attempted to work with a server entity that
 * does not exist.
 */
public class NotFoundException extends ChannelException {

  private static final long serialVersionUID = 1L;

  public NotFoundException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_FOUND;
  }

}
