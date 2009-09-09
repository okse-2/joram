/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.security.Identity;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;


/**
 * An <code>AdminNotification</code> is sent by an administrator's proxy for
 * registering to the local administration topic.
 */
public class AdminNotification extends Notification {

  private static final long serialVersionUID = 1L;
  /** The proxy's <code>AgentId</code> identifier. */
  private AgentId proxyId;
  /** The administrator's Identity. */
  private Identity identity;

  /**
   * Constructs an <code>AdminNotification</code> instance.
   *
   * @param proxyId  The proxy's identifier.
   * @param identity administrator identity.
   */
  AdminNotification(AgentId proxyId, Identity identity) {
    this.proxyId = proxyId;
    this.identity = identity;
  }

  
  /** Returns the <code>AgentId</code> of the proxy. */
  public AgentId getProxyId() {
    return proxyId;
  }

  /** Returns the identity of the administrator. */
  public Identity getIdentity() {
    return identity;
  }
}
