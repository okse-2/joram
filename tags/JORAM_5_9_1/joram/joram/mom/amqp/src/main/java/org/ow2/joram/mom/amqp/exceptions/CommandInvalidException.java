package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the client sent an invalid sequence of frames, attempting
 * to perform an operation that was considered invalid by the server. This
 * usually implies a programming error in the client.
 */
public class CommandInvalidException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public CommandInvalidException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.COMMAND_INVALID;
  }

}
