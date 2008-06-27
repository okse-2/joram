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
 * 
 * Created on 16 mai 2006
 *
 */
package org.objectweb.joram.mom.proxies;

import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.RequestGroupNot;
import org.objectweb.joram.shared.client.ProducerMessages;

import fr.dyade.aaa.agent.AgentId;

/**
 *
 */
public class RequestBuffer {
  
  private ProxyAgentItf proxyAgent;
  
  private Hashtable nots = new Hashtable();
  
  public RequestBuffer(ProxyAgentItf proxy) {
    proxyAgent = proxy;
  }
  
  public void put(int key, ProducerMessages req) {
    AgentId to = AgentId.fromString(req.getTarget());
    RequestGroupNot not = (RequestGroupNot) nots.get(to);
    if (not == null) {
      not = new RequestGroupNot();
      nots.put(to, not);
    }
    ClientMessages cm = new ClientMessages(key, req.getRequestId(), req
        .getMessages());
    if (to.getTo() == proxyAgent.getId().getTo()) {
      cm.setPersistent(false);
    }
    if (req.getAsyncSend()) {
      cm.setAsyncSend(true);
    }
    not.addClientMessages(cm);
  }

  public void flush() {
    if (nots.size() > 0) {
      Enumeration ids = nots.keys();
      Enumeration notifs = nots.elements();
      while (notifs.hasMoreElements()) {
        AgentId to = (AgentId) ids.nextElement();
        RequestGroupNot not = (RequestGroupNot) notifs.nextElement();
        if (to.getTo() == proxyAgent.getId().getTo()) {
          not.setPersistent(false);
          proxyAgent.sendNot(to, not);
        } else {
          proxyAgent.sendNot(to, not);
        }
      }
      nots.clear();
    }
  }
}
