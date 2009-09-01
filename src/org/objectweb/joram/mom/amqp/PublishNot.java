/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.Notification;

public class PublishNot extends Notification {
  
  private String routingKey;

  private String exchange;
  
  private BasicProperties properties;
  
  private byte[] body;

  /**
   * @param routingKey
   * @param exchange
   * @param properties
   * @param body
   */
  public PublishNot(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    super();
    this.routingKey = routingKey;
    this.exchange = exchange;
    this.properties = properties;
    this.body = body;
  }
  
  public byte[] getBody() {
    return body;
  }
  
  public BasicProperties getProperties() {
    return properties;
  }
  
  public String getRoutingKey() {
    return routingKey;
  }
  
  public String getExchange() {
    return exchange;
  }
}
