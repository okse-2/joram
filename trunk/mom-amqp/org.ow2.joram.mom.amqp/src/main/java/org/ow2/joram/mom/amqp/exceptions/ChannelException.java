package org.ow2.joram.mom.amqp.exceptions;

/**
 * These exceptions are all associated with failures that affect the current
 * channel but not other channels in the same connection;
 */
public abstract class ChannelException extends AMQPException {

  private static final long serialVersionUID = 1L;

  public ChannelException(String message) {
    super(message);
  }

}
