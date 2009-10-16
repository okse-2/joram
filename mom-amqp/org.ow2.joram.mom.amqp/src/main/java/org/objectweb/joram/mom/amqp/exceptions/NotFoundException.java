package org.objectweb.joram.mom.amqp.exceptions;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public class NotFoundException extends ChannelException {

  private static final long serialVersionUID = 1L;

  public NotFoundException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NOT_FOUND;
  }

}
