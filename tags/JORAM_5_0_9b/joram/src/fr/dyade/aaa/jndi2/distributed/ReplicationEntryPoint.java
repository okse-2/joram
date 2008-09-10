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
package fr.dyade.aaa.jndi2.distributed;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.jndi2.server.EntryPoint;
import fr.dyade.aaa.jndi2.server.JndiReplyNot;
import fr.dyade.aaa.jndi2.server.JndiScriptReplyNot;

public class ReplicationEntryPoint implements EntryPoint {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private ReplicationManager manager;
  
  public void setRequestManager(ReplicationManager manager) {
    this.manager = manager;
  }

  public boolean accept(AgentId from, Notification not) throws Exception {
    if (not instanceof JndiUpdateNot) {
      manager.doReact(from, (JndiUpdateNot)not);
    } else if (not instanceof JndiReplyNot) {
      manager.doReact(from, (JndiReplyNot)not);
    } else if (not instanceof JndiScriptReplyNot) {
      manager.doReact(from, (JndiScriptReplyNot)not);
    } else if (not instanceof InitJndiServerNot) {
      manager.doReact(from, (InitJndiServerNot)not);
    } else if (not instanceof SyncRequestNot) {
      manager.doReact(from, (SyncRequestNot)not);
    } else if (not instanceof SyncReplyNot) {
      manager.doReact(from, (SyncReplyNot)not);
    } else return false;
    return true;
  }
}
