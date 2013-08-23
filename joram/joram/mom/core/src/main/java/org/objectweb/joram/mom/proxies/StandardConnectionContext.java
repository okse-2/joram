/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.excepts.MomException;

import fr.dyade.aaa.common.Queue;

/**
 * Standard implementation of the interface to abstract the communication between
 * the client and the MOM.
 */
public class StandardConnectionContext 
  implements ConnectionContext, java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private int key;

  private Queue queue;
  
  private boolean closed;
  
  public StandardConnectionContext() {}

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

  public void initialize(int key, OpenConnectionNot not) {
    this.key = key;
    queue = new Queue();
    closed = false;
  }
  
}
