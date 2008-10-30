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

import org.objectweb.joram.mom.amqp.DeliveryListener;
import org.objectweb.joram.mom.amqp.marshalling.AMQP;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class BasicConsumeNot extends SyncNotification {
  
  private int channelId;
  private int ticket;
  private String queue;
  private boolean noAck;
  private String consumerTag;
  private DeliveryListener callback;
  
  /**
   * @param channelId
   * @param ticket
   * @param queue
   * @param noAck
   * @param consumerTag
   * @param callback
   */
  public BasicConsumeNot(int channelId, int ticket, String queue,
      boolean noAck, String consumerTag, DeliveryListener callback) {
    super();
    this.channelId = channelId;
    this.ticket = ticket;
    this.queue = queue;
    this.noAck = noAck;
    this.consumerTag = consumerTag;
    this.callback = callback;
  }
  
  public DeliveryListener getCallback() {
    return callback;
  }
  public int getChannelId() {
    return channelId;
  }
  public String getConsumerTag() {
    return consumerTag;
  }
  public boolean isNoAck() {
    return noAck;
  }
  public String getQueue() {
    return queue;
  }
  public int getTicket() {
    return ticket;
  }
  
  public AMQP.Basic.ConsumeOk basicConsume(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Basic.ConsumeOk) res[0];
  }
  
  public void Return(AMQP.Basic.ConsumeOk res) {
    Return(new Object[]{res});
  }

}
