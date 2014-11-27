/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.AMQPRequestNot;
import org.ow2.joram.mom.amqp.Message;
import org.ow2.joram.mom.amqp.Queue;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

/**
 * This class is used in an {@link AMQPRequestNot} to publish a message to a
 * distant queue after exchange routing.
 */
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
