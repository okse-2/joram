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

public class ExchangeDeclareNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String exchange;
  private String type;
  private boolean passive; 
  private boolean durable; 
  private boolean autoDelete; 
  private Map arguments;
  
  /**
   * @param channelId
   * @param ticket
   * @param exchange
   * @param type
   * @param passive
   * @param durable
   * @param autoDelete
   * @param arguments
   */
  public ExchangeDeclareNot(int channelId, int ticket, String exchange,
      String type, boolean passive, boolean durable, boolean autoDelete,
      Map arguments) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.exchange = exchange;
    this.type = type;
    this.passive = passive;
    this.durable = durable;
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
  public String getExchange() {
    return exchange;
  }
  public boolean isPassive() {
    return passive;
  }
  public int getTicket() {
    return ticket;
  }
  public String getType() {
    return type;
  }
  
  public AMQP.Exchange.DeclareOk exchangeDeclare(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Exchange.DeclareOk) res[0];
  }
  
  public void Return(AMQP.Exchange.DeclareOk res) {
    Return(new Object[]{res});
  }

}
