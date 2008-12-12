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
package org.objectweb.joram.mom.amqp.proxy.request;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.Notification;

/**
 * Transient notification.
 */
public class BasicPublishNot extends Notification {
  
  private int channelId;
  private int ticket;
  private String exchange;
  private String routingKey;
  private boolean mandatory;
  private boolean immediate;
  private BasicProperties props;
  private byte[] body;
  
  /**
   * @param channelId
   * @param ticket
   * @param exchange
   * @param routingKey
   * @param mandatory
   * @param immediate
   * @param props
   * @param body
   */
  public BasicPublishNot(int channelId, int ticket, String exchange,
      String routingKey, boolean mandatory,
      boolean immediate, BasicProperties props,
      byte[] body) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.mandatory = mandatory;
    this.immediate = immediate;
    this.props = props;
    this.body = body;
    persistent = false;
  }
  
  public byte[] getBody() {
    return body;
  }
  public int getChannelId() {
    return channelId;
  }
  public String getExchange() {
    return exchange;
  }
  public boolean isImmediate() {
    return immediate;
  }
  public boolean isMandatory() {
    return mandatory;
  }
  public BasicProperties getProps() {
    return props;
  }
  public String getRoutingKey() {
    return routingKey;
  }
  public int getTicket() {
    return ticket;
  }
  
/*
  public AccessRequestOk accessRequest(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AccessRequestOk)res[0];
  }
  
  public void Return(AccessRequestOk res) {
    Return(new Object[]{res});
  }
  */

}
