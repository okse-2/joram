package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

/**
 * A {@link ConsumeMessage} is sent to the proxy on <code>Basic.Consume</code>
 * method, in order to request a message on the specified queue. This message
 * will be requeued as long as there are more messages on the queue.
 */
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
