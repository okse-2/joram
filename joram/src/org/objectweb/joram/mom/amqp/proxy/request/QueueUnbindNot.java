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

import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class QueueUnbindNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String queue; 
  private String exchange;
  private String routingKey; 
  private Map arguments;
  
  /**
   * @param channelId
   * @param ticket
   * @param queue
   * @param exchange
   * @param routingKey
   * @param arguments
   */
  public QueueUnbindNot(int channelId, int ticket, String queue, String exchange,
      String routingKey, Map arguments) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.queue = queue;
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.arguments = arguments;
  }
  
  public int getChannelId() {
    return channelId;
  }
  public Map getArguments() {
    return arguments;
  }
  public String getExchange() {
    return exchange;
  }
  public String getQueue() {
    return queue;
  }
  public String getRoutingKey() {
    return routingKey;
  }
  public int getTicket() {
    return ticket;
  }
  
  public AMQP.Queue.UnbindOk queueUnbind(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Queue.UnbindOk) res[0];
  }
  
  public void Return(AMQP.Queue.UnbindOk res) {
    Return(new Object[]{res});
  }

}
