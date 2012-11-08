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

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class ExchangeDeleteNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String exchange;
  private boolean ifUnused;
  private boolean nowait;

  /**
   * @param channelId
   * @param ticket
   * @param exchange
   * @param ifUnused
   * @param nowait
   */
  public ExchangeDeleteNot(int channelId, int ticket, String exchange, boolean ifUnused, boolean nowait) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.exchange = exchange;
    this.ifUnused = ifUnused;
    this.nowait = nowait;
  }
  
  public AMQP.Exchange.DeleteOk exchangeDelete(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Exchange.DeleteOk) res[0];
  }
  
  public void Return(AMQP.Exchange.DeleteOk res) {
    Return(new Object[] { res });
  }

  public int getChannelId() {
    return channelId;
  }

  public int getTicket() {
    return ticket;
  }

  public String getExchange() {
    return exchange;
  }

  public boolean isIfUnused() {
    return ifUnused;
  }

  public boolean isNowait() {
    return nowait;
  }

}
