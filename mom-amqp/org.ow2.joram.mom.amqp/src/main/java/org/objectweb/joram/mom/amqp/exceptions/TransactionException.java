package org.objectweb.joram.mom.amqp.exceptions;

public class TransactionException extends InternalErrorException {

  private static final long serialVersionUID = 1L;

  public TransactionException(String message) {
    super(message);
  }

}
