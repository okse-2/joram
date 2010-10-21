package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

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
