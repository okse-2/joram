package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

/**
 * Holds a complete <code>Basic.Return</code> response with headers and body.<br>
 * This class is used to return to the client an undeliverable message that was
 * published with the "immediate" flag set, or an unroutable message published
 * with the "mandatory" flag set. The reply code and text provide information
 * about the reason that the message was undeliverable.
 */
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
