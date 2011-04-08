package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown when the exchange cannot deliver to a consumer when the
 * immediate flag is set. As a result of pending data on the queue or the
 * absence of any consumers of the queue.
 */
public class NoConsumersException extends ChannelException {

  private static final long serialVersionUID = 1L;

  public NoConsumersException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.NO_CONSUMERS;
  }

}
