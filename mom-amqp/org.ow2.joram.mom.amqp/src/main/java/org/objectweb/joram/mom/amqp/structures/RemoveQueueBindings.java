package org.objectweb.joram.mom.amqp.structures;

import java.io.Serializable;

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
