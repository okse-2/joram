package org.objectweb.joram.mom.amqp.exceptions;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public class CommandInvalidException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public CommandInvalidException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.COMMAND_INVALID;
  }

}
