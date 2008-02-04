/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.*;

/**
 * Transient notification
 */
public class GetProxyIdNot extends SyncNotification {
  
  private String userName;

  private String password;

  private String inaddr;

  public GetProxyIdNot(String userName,
		       String password,
                       String inaddr) {
    this.userName = userName;
    this.password = password;
    this.inaddr = inaddr;
  }

  public final String getUserName() {
    return userName;
  }

  public final String getPassword() {
    return password;
  }

  public final String getInAddr() {
    return inaddr;
  }

  public void Return(AgentId proxyId) {
    Return(new Object[]{proxyId});
  }

  public final AgentId getProxyId() {
    return (AgentId)getValue(0);
  }
}

