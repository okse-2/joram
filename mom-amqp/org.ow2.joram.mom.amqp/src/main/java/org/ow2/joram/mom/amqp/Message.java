package org.ow2.joram.mom.amqp;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

/**
 * A Joram AMQP MOM message.
 */
public class Message implements Serializable, Comparable<Message> {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  String exchange;
  String routingKey;
  BasicProperties properties;
  byte[] body;
  long queueMsgId;
  boolean redelivered;
  int queueSize;
  String queueName;

  /**
   * @param properties
   * @param body
   */
  public Message(String exchange, String routingKey, BasicProperties properties, byte[] body,
      long deliveryTag, boolean redelivered) {
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.properties = properties;
    this.body = body;
    this.queueMsgId = deliveryTag;
    this.redelivered = redelivered;
  }

  public int compareTo(Message o) {
    long diff = this.queueMsgId - o.queueMsgId;
    if (diff > 0) {
      return 1;
    } else if (diff < 0) {
      return -1;
    } else {
      return 0;
    }
  }

}
