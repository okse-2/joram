package org.objectweb.joram.mom.amqp;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.GetOk;

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
