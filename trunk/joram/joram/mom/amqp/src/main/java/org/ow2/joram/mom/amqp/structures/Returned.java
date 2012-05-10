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
