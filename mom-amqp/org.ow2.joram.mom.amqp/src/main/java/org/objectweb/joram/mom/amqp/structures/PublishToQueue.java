package org.objectweb.joram.mom.amqp.structures;

import java.io.Serializable;

import org.objectweb.joram.mom.amqp.Message;
import org.objectweb.joram.mom.amqp.Queue;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

public class PublishToQueue implements Serializable {

  private static final long serialVersionUID = 1L;

  private String queueName;

  private String exchangeName;

  private String routingKey;

  private BasicProperties properties;

  private byte[] body;

  private short serverId;

  private long proxyId;

  private boolean immediate;

  private int channelNumber;

  public PublishToQueue(String queueName, String exchangeName, String routingKey, boolean immediate,
      BasicProperties properties, byte[] body, int channelNumber, short serverId, long proxyId) {
    this.queueName = queueName;
    this.exchangeName = exchangeName;
    this.routingKey = routingKey;
    this.immediate = immediate;
    this.properties = properties;
    this.body = body;
    this.channelNumber = channelNumber;
    this.serverId = serverId;
    this.proxyId = proxyId;
  }

  public boolean isImmediate() {
    return immediate;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public BasicProperties getProperties() {
    return properties;
  }

  public byte[] getBody() {
    return body;
  }

  public short getServerId() {
    return serverId;
  }

  public long getProxyId() {
    return proxyId;
  }

  public String getQueueName() {
    return queueName;
  }

  public String getExchangeName() {
    return exchangeName;
  }

  public int getChannelNumber() {
    return channelNumber;
  }

  public Message getMessage() {
    return new Message(exchangeName, routingKey, properties, body, Queue.FIRST_DELIVERY, false);
  }

}
