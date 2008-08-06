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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.mom.proxies;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;


/**
 * An <code>AdminNotification</code> is sent by an administrator's proxy for
 * registering to the local administration topic.
 */
public class AdminNotification extends Notification
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The proxy's <code>AgentId</code> identifier. */
  private AgentId proxyId;
  /** The administrator's name. */
  private String name;
  /** The administrator's password. */
  private String pass;

  /**
   * Constructs an <code>AdminNotification</code> instance.
   *
   * @param proxyId  The proxy's identifier.
   * @param name  The name of the administrator.
   * @param pass  The password of the administrator.
   */
  AdminNotification(AgentId proxyId, String name, String pass)
  {
    this.proxyId = proxyId;
    this.name = name;
    this.pass = pass;
  }

  
  /** Returns the <code>AgentId</code> of the proxy. */
  public AgentId getProxyId()
  {
    return proxyId;
  }

  /** Returns the name of the administrator. */
  public String getName()
  {
    return name;
  }

  /** Returns the password of the administrator. */
  public String getPass()
  {
    return pass;
  }
}
