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
 */
package org.objectweb.joram.client.jms.connection;

import java.util.Timer;
import java.util.Vector;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;

/**
 * Class wrapping the <code>RequestChannel</code> in order to group the
 * requests. It allows best performances with multiples senders.
 */
public class MultiThreadSyncChannel implements RequestChannel {
  /**
   * Synchronization round.
   */
  private SyncRound currentRound;
  
  /**
   * Synchronized requests.
   */
  private Vector syncRequests;
  
  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * Either they wake up (wait time out) or they are notified (by the
   * first woken up thread).
   */
  private int multiThreadSyncDelay;

  /**
   * The maximum numbers of threads that hang if 'multiThreadSync' is true.
   */
  private int multiThreadSyncThreshold;
  
  /** The related RequestChannel. */
  private RequestChannel channel;
  
  MultiThreadSyncChannel(RequestChannel rc, int delay, int threshold) {
    channel = rc;
    multiThreadSyncDelay = delay;
    multiThreadSyncThreshold = threshold;
    currentRound = new SyncRound();
    syncRequests = new Vector();
  }

  public synchronized void send(AbstractJmsRequest request) throws Exception {
    SyncRound localRound = currentRound;
    syncRequests.addElement(request);
    if (syncRequests.size() < multiThreadSyncThreshold) {
      try {
        wait(multiThreadSyncDelay);
      } catch (InterruptedException ie) {
      }
    }
    if (!localRound.done) {
      // syncRequests.size() must be > 0
      AbstractJmsRequest[] requests = 
        new AbstractJmsRequest[syncRequests.size()];
      syncRequests.copyInto(requests);
      syncRequests.clear();
      localRound.done = true;
      currentRound = new SyncRound();
      channel.send(new JmsRequestGroup(requests));
      notifyAll();
    }
    // else do nothing
  }

  /*
   * @see org.objectweb.joram.client.jms.connection.RequestChannel#setTimer(java.util.Timer)
   */
  public void setTimer(Timer timer) {
    channel.setTimer(timer);
  }

  /*
   * @see org.objectweb.joram.client.jms.connection.RequestChannel#connect()
   */
  public void connect() throws Exception {
    channel.connect();
  }

  /*
   * @see org.objectweb.joram.client.jms.connection.RequestChannel#receive()
   */
  public AbstractJmsReply receive() throws Exception {
    return channel.receive();
  }

  /*
   * @see org.objectweb.joram.client.jms.connection.RequestChannel#close()
   */
  public void close() {
    channel.close();
  }
  
  private static class SyncRound {
    private volatile boolean done = false;
  }
}
