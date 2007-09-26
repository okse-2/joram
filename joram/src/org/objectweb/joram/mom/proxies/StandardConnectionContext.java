/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Created on 15 mai 2006
 *
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.CnxCloseRequest;

import fr.dyade.aaa.util.Queue;

/**
 *
 */
public class StandardConnectionContext 
  implements ConnectionContext, java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private int key;

  private Queue queue;
  
  private ProxyImpl proxyImpl;
  
  private boolean closed;

  StandardConnectionContext(ProxyImpl proxyImpl, int key) {
    this.key = key;
    this.proxyImpl = proxyImpl;
    queue = new Queue();
    closed = false;
  }
  
  public int getKey() {
    return key;
  }
  
  public void send(Object obj) {
    queue.push(obj);
  }
  
  public Queue getQueue() {
    return queue;
  }
  
  public void pushReply(AbstractJmsReply reply) {
    queue.push(reply);
  }
  
  public AbstractJmsRequest getRequest(Object req) {
    AbstractJmsRequest request = (AbstractJmsRequest) req;
    if (request instanceof CnxCloseRequest) {
      closed = true;
    }
    return request;
  }
  
  public void pushError(MomException exc) {
    queue.push(new MomExceptionReply(exc));
  }
  
  public boolean isClosed() {
    return closed;
  }
  
}
