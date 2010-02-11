package org.ow2.joram.mom.amqp;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

public class Deliver implements Serializable {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  AMQP.Basic.Deliver deliver;

  AMQP.Basic.BasicProperties properties;

  byte[] body;
  
  short serverId;
  long proxyId;
  String queueName;
  long msgId;

  public Deliver(AMQP.Basic.Deliver deliver, BasicProperties properties, byte[] body, long msgId, short serverId, long proxyId, String queueName) {
    this.deliver = deliver;
    this.properties = properties;
    this.body = body;
    this.serverId = serverId;
    this.proxyId = proxyId;
    this.queueName = queueName;
    this.msgId = msgId;
  }

}
