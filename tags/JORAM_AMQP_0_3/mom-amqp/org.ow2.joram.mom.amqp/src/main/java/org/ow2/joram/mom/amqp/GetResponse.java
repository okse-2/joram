package org.ow2.joram.mom.amqp;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.GetOk;

public class GetResponse {

  AMQP.Basic.GetOk getOk;

  AMQP.Basic.BasicProperties properties;

  byte[] body;

  public GetResponse(GetOk getOk, BasicProperties properties, byte[] body) {
    super();
    this.getOk = getOk;
    this.properties = properties;
    this.body = body;
  }

}
