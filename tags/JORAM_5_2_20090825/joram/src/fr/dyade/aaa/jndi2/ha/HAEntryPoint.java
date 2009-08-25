/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.jndi2.ha;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.jndi2.server.EntryPoint;
import fr.dyade.aaa.jndi2.server.TcpRequestNot;
import fr.dyade.aaa.jndi2.server.Trace;

public class HAEntryPoint implements EntryPoint {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private HARequestManager manager;

  public void setHARequestManager(HARequestManager manager) {
    this.manager = manager;
  }

   public boolean accept(AgentId from, Notification not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer.react(" + 
                       from + ',' + not + ')');
    if (not instanceof TcpRequestNot) {
      manager.doReact((TcpRequestNot)not);
    } else if (not instanceof GetRequestIdNot) {
      manager.doReact((GetRequestIdNot)not);
    } else return false;
    return true;
  }
}
