/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.server;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.jndi2.msg.JndiReply;

public class TcpEntryPoint implements EntryPoint {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private RequestManager manager;

  public void setRequestManager(RequestManager manager) {
    this.manager = manager;
  }

  public boolean accept(AgentId from, Notification not) throws Exception {
    if (not instanceof TcpRequestNot) {
      doReact((TcpRequestNot)not);
    } else return false;
    return true;
  }

  /**
   * Reacts to a TCP connection request. This is the TCP entry point.
   *
   * @param not the TCP connection request
   */
  private void doReact(TcpRequestNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.doReact((TcpRequestNot)" + 
                       not + ')');
    RequestContext reqCtx = not.getRequestContext();
    JndiReply reply = manager.invoke(reqCtx);
    if (reply != null) {
      reqCtx.reply(reply);
    }
  }
}
