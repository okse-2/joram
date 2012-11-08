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

import fr.dyade.aaa.agent.Notification;

/**
 * Transient notification.
 */
public class ConsumeNot extends Notification {
  
  private int channelId;
  private DeliveryListener callback;
  private ProxyAgent proxy;
  private String consumerTag;
  private boolean noAck;

  /**
   * @param consumerTag
   * @param callback
   * @param proxyAgent
   */
  public ConsumeNot(int channelId, DeliveryListener callback, ProxyAgent proxyAgent, String consumerTag,
      boolean noAck) {
    super();
    this.channelId = channelId;
    this.callback = callback;
    this.consumerTag = consumerTag;
    this.noAck = noAck;
    this.proxy = proxyAgent;
    persistent = false;
  }

  public DeliveryListener getCallback() {
    return callback;
  }

  public String getConsumerTag() {
    return consumerTag;
  }

  public boolean isNoAck() {
    return noAck;
  }

  public ProxyAgent getProxy() {
    return proxy;
  }

  public int getChannelId() {
    return channelId;
  }
}
