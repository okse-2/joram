package org.objectweb.joram.mom.amqp.structures;

import java.io.Serializable;

public class ConsumeMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  public String queueName;

  public String consumerTag;

  public int channelNumber;

  public boolean noAck;

  public short consumerServerId;

  public ConsumeMessage(String queueName, String consumerTag, int channelNumber, boolean noAck) {
    super();
    this.queueName = queueName;
    this.consumerTag = consumerTag;
    this.channelNumber = channelNumber;
    this.noAck = noAck;
  }

  public ConsumeMessage(String queueName, String consumerTag, int channelNumber, boolean noAck,
      short consumerServerId) {
    super();
    this.queueName = queueName;
    this.consumerTag = consumerTag;
    this.channelNumber = channelNumber;
    this.noAck = noAck;
    this.consumerServerId = consumerServerId;
  }


}
