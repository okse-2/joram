/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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

import com.rabbitmq.client.AMQP;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class QueueDeclareNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String queue;
  private boolean passive;
  private boolean durable;
  private boolean exclusive;
  private boolean autoDelete;
  private Map arguments;
  
  /**
   * @param channelId
   * @param ticket
   * @param queue
   * @param passive
   * @param durable
   * @param exclusive
   * @param autoDelete
   * @param arguments
   */
  public QueueDeclareNot(int channelId, int ticket, String queue,
      boolean passive, boolean durable, boolean exclusive, boolean autoDelete,
      Map arguments) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.queue = queue;
    this.passive = passive;
    this.durable = durable;
    this.exclusive = exclusive;
    this.autoDelete = autoDelete;
    this.arguments = arguments;
  }
  
  public Map getArguments() {
    return arguments;
  }
  public boolean isAutoDelete() {
    return autoDelete;
  }
  public int getChannelId() {
    return channelId;
  }
  public boolean isDurable() {
    return durable;
  }
  public boolean isExclusive() {
    return exclusive;
  }
  public boolean isPassive() {
    return passive;
  }
  public String getQueue() {
    return queue;
  }
  public int getTicket() {
    return ticket;
  }
  
  public AMQP.Queue.DeclareOk queueDeclare(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Queue.DeclareOk) res[0];
  }
  
  public void Return(AMQP.Queue.DeclareOk res) {
    Return(new Object[]{res});
  }

}
