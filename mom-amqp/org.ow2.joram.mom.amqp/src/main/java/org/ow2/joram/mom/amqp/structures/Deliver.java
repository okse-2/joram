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
  public boolean noAck;

  public Deliver(AMQP.Basic.Deliver deliver, BasicProperties properties, byte[] body, long msgId,
      short serverId, long proxyId, String queueName, boolean noAck) {
    this.deliver = deliver;
    this.properties = properties;
    this.body = body;
    this.serverId = serverId;
    this.proxyId = proxyId;
    this.queueName = queueName;
    this.msgId = msgId;
    this.noAck = noAck;
  }

}
