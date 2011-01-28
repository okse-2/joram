package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.AMQPRequestNot;

/**
 * This class is used in an {@link AMQPRequestNot} to notify the distant queue
 * to remove its bindings with the specified exchange, because of the exchange
 * deletion or <code>Queue.Unbind</code> method.
 */
public class RemoveBoundExchange implements Serializable {

  private static final long serialVersionUID = 1L;

  private String queueName;

  private String exchangeName;

  public RemoveBoundExchange(String queueName, String exchangeName) {
    this.queueName = queueName;
    this.exchangeName = exchangeName;
  }

  public String getQueueName() {
    return queueName;
  }

  public String getExchangeName() {
    return exchangeName;
  }

}
