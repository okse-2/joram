package org.objectweb.joram.mom.amqp.exceptions;

public abstract class ChannelException extends AMQPException {

  private static final long serialVersionUID = 1L;

  public ChannelException(String message) {
    super(message);
  }

}
