package org.objectweb.joram.mom.amqp.structures;

import java.io.Serializable;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

public class Returned implements Serializable {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  public AMQP.Basic.Return returned;

  public AMQP.Basic.BasicProperties properties;

  public byte[] body;
  
  public short serverId;
  public long proxyId;

  public Returned(AMQP.Basic.Return returned, BasicProperties properties, byte[] body, short serverId,
      long proxyId) {
    this.returned = returned;
    this.properties = properties;
    this.body = body;
    this.serverId = serverId;
    this.proxyId = proxyId;
  }

  public Returned(AMQP.Basic.Return returned, BasicProperties properties, byte[] body) {
    this.returned = returned;
    this.properties = properties;
    this.body = body;
  }

}
