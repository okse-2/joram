package org.objectweb.joram.mom.amqp.exceptions;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public class NoConsumersException extends ChannelException {

  private static final long serialVersionUID = 1L;

  public NoConsumersException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NO_CONSUMERS;
  }

}
