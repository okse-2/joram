/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.server;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;

public class AgentEntryPoint implements EntryPoint {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private RequestManager manager;

  public void setRequestManager(RequestManager manager) {
    this.manager = manager;
  }

  public boolean accept(AgentId from, Notification not) throws Exception {
    if (not instanceof JndiScriptRequestNot) {
      doReact(from, (JndiScriptRequestNot)not);
    } else return false;
    return true;
  }

  /**
   * Reacts to a JNDI script request. This is the notification
   * entry point.
   * 
   * @param not the JNDI script
   */
  private void doReact(AgentId from, JndiScriptRequestNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "AgentEntryPoint[" + manager.getId() + 
                       "].doReact(" + from +
                       ",(JndiScriptRequestNot)" + not + ')');
    JndiRequest[] requests = not.getRequests();
    JndiReply[] replies = new JndiReply[requests.length];
    for (int i = 0; i < requests.length; i++) {
      AgentRequestContext reqCtx = new AgentRequestContext(
        requests[i], from, not.reply());
      replies[i] = manager.invoke(reqCtx);
    }
    if (not.reply()) {
      // Reply to all the operations from the input
      // script except those that are asynchronous.
      // This can't happen in a centralized server.
      // But in a distributed JNDI configuration, this
      // server may be waiting for a notification reply 
      // from an other naming server. 
      // These asynchronous operations
      // are acknowledged in separate notifications.
      manager.sendTo(from, new JndiScriptReplyNot(replies));
    }
  }
}
