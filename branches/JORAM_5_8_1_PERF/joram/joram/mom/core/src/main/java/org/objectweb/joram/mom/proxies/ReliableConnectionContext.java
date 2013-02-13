/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * 
 * Created on 15 mai 2006
 *
 */
package org.objectweb.joram.mom.proxies;

import java.io.Serializable;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.excepts.MomException;

/**
 *
 */
public class ReliableConnectionContext implements ConnectionContext, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private int key;

  private long inputCounter;

  private long outputCounter;

  private AckedQueue queue;

  private int heartBeat;
  
  private boolean closed;

  ReliableConnectionContext(int key, int heartBeat) {
    this.key = key;
    this.heartBeat = heartBeat;
    inputCounter = -1;
    outputCounter = 0;
    queue = new AckedQueue();
    closed = false;
  }
  
  public int getKey() {
    return key;
  }
  
  public AckedQueue getQueue() {
    return queue;
  }
  
  public int getHeartBeat() {
    return heartBeat;
  }
  
  public long getInputCounter() {
    return inputCounter;
  }
  
  public AbstractJmsRequest getRequest(Object obj) {
    ProxyMessage msg = (ProxyMessage)obj;
    inputCounter = msg.getId();
    AbstractJmsRequest request = (AbstractJmsRequest) msg.getObject();
    // JORAM_PERF_BRANCH:
    // queue.ack(msg.getAckId());
    // JORAM_PERF_BRANCH.
    if (request instanceof CnxCloseRequest) {
      closed = true;
    }
    return request;
  }
  
  public void pushReply(AbstractJmsReply reply) {
    ProxyMessage msg = new ProxyMessage(outputCounter, inputCounter, reply);
    queue.push(msg);
    outputCounter++;
  }
  
  public void pushError(MomException exc) {
    queue.push(new ProxyMessage(-1, -1, new MomExceptionReply(exc)));
  }
  
  public boolean isClosed() {
    return closed;
  }

  // JORAM_PERF_BRANCH
  @Override
  public String toString() {
    return "ReliableConnectionContext [key=" + key + ", queue="
        + queue.size() + ", heartBeat=" + heartBeat + ", closed=" + closed + "]";
  }  
}
