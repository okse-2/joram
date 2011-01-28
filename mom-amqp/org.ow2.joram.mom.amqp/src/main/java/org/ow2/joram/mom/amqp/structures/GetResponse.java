package org.ow2.joram.mom.amqp.structures;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.GetOk;

/**
 * Holds a complete <code>Basic.Get</code> response with headers and body.
 */
public class GetResponse {

  public AMQP.Basic.GetOk getOk;

  public AMQP.Basic.BasicProperties properties;

  public byte[] body;

  public GetResponse(GetOk getOk, BasicProperties properties, byte[] body) {
    super();
    this.getOk = getOk;
    this.properties = properties;
    this.body = body;
  }

}
