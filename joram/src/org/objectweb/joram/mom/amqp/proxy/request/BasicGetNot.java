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

import org.objectweb.joram.mom.amqp.GetListener;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

/**
 * Transient notification.
 */
public class BasicGetNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String queueName;
  private boolean noAck;
  private GetListener callback;

  /**
   * @param channelNumber
   * @param ticket
   * @param queue
   * @param noAck
   */
  public BasicGetNot(int channelNumber, int ticket, String queue, boolean noAck, GetListener callback) {
    super();
    this.channelId = channelNumber;
    this.ticket = ticket;
    this.queueName = queue;
    this.noAck = noAck;
    this.callback = callback;
  }

  public int getChannelId() {
    return channelId;
  }

  public int getTicket() {
    return ticket;
  }

  public String getQueueName() {
    return queueName;
  }

  public boolean isNoAck() {
    return noAck;
  }
  
  public void basicGet(AgentId proxyId) throws Exception {
    invoke(proxyId);
  }

  public void Return() {
    Return(null);
  }

  public GetListener getCallback() {
    return callback;
  }

}
