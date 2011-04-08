package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

/**
 * Holds a complete <code>Basic.Deliver</code> response with headers and body.
 */
public class Deliver implements Serializable {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  public AMQP.Basic.Deliver deliver;

  public AMQP.Basic.BasicProperties properties;

  public byte[] body;
  
  public short serverId;
  public long proxyId;
  public String queueName;
  public long msgId;

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
