package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.AMQPRequestNot;

/**
 * This class is used in an {@link AMQPRequestNot} to notify the distant
 * exchange to remove its bindings with the specified queue, because of the
 * queue deletion.
 */
public class RemoveQueueBindings implements Serializable {

  private static final long serialVersionUID = 1L;

  private String queueName;

  private String exchangeName;

  public RemoveQueueBindings(String exchangeName, String queueName) {
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
