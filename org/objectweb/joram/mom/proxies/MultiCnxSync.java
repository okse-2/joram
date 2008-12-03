/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.proxies;

import java.util.Vector;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;

/**
 * 
 *
 */
public class MultiCnxSync {
  
  private AgentId proxyId;
  
  private Vector syncRequests = new Vector();
  
  private SyncRound currentRound = new SyncRound();
  
  public MultiCnxSync(AgentId pid) {
    proxyId = pid;
  }
  
  public synchronized void send(RequestNot request) {
    SyncRound localRound = currentRound;
    syncRequests.addElement(request);
    try {
      wait(ConnectionManager.getMultiThreadSyncDelay());
    } catch (InterruptedException ie) {
    }
    if ((!localRound.done) && syncRequests.size() > 0) {
      RequestNot[] requests = new RequestNot[syncRequests.size()];
      syncRequests.copyInto(requests);
      syncRequests.clear();
      Channel.sendTo(proxyId, new ProxyRequestGroupNot(requests));
      localRound.done = true;
      currentRound = new SyncRound();
      notifyAll();
    }
    // else do nothing.
  }
  
  private static class SyncRound {
    private volatile boolean done = false;
  }
}
