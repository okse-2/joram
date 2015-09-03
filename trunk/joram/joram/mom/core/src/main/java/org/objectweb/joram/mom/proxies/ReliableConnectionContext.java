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

import java.io.Serializable;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Reliable implementation of the interface to abstract the communication between
 * the client and the MOM.
 */
public class ReliableConnectionContext implements ConnectionContext, Serializable {
  
  public static Logger logger = Debug.getLogger(ReliableConnectionContext.class.getName());

  private static final long serialVersionUID = 1L;

  private int key;

  private long inputCounter;

  private long outputCounter;

  private AckedQueue queue;

  private int heartBeat;
  
  private boolean closed;
  
  private boolean noAckedQueue;
  
  private QueueWorker queueWorker;
  
  public ReliableConnectionContext() {}

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
    if (!noAckedQueue) {
      inputCounter = msg.getId();
      queue.ack(msg.getAckId());
    }
    
    AbstractJmsRequest request = (AbstractJmsRequest) msg.getObject();

    if (request instanceof CnxCloseRequest) {
      closed = true;
    }
    return request;
  }
  
  public boolean isNoAckedQueue() {
    return noAckedQueue;
  }

  private void add(ProxyMessage msg) {
    queueWorker.send(msg);
    /*
    synchronized (queueWorker.queue) {
      queueWorker.queue.offer(msg);
      if (! queueWorker.running) {
        queueWorker.running = true;
        try {
          if (TcpProxyService.executorService == null) {
            queueWorker.ioctrl.send(msg);
            queueWorker.running = false;
          } else {
            TcpProxyService.execute(queueWorker);
          }
        } catch (Exception e) {
          logger.log(BasicLevel.ERROR, e);
        }
      }
    }*/
  }
  
  public QueueWorker getQueueWorker() {
    return queueWorker;
  }
  
  public void pushReply(AbstractJmsReply reply) {
    ProxyMessage msg = new ProxyMessage(outputCounter, inputCounter, reply);
    if (noAckedQueue) {
      add(msg);
    } else {
      queue.push(msg);
      if (!noAckedQueue) {
        outputCounter++;
      }
    }
  }

  public void pushError(MomException exc) {
    if (noAckedQueue) {
      add(new ProxyMessage(-1, -1, new MomExceptionReply(exc)));
    } else {
      queue.push(new ProxyMessage(-1, -1, new MomExceptionReply(exc)));
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public void initialize(int key, OpenConnectionNot not) {
    this.key = key;
    this.heartBeat = not.getHeartBeat();
    inputCounter = -1;
    outputCounter = 0;
    this.noAckedQueue = not.isNoAckedQueue();
    if (noAckedQueue) {
      queueWorker = new QueueWorker();
    } else {
      queue = new AckedQueue();
    }
    closed = false;
  }  
}
