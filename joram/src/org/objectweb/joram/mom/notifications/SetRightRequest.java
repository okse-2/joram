/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.AgentId;


/**
 * A <code>SetRightRequest</code> instance is used by a client agent
 * for setting users right on a destination.
 */
public class SetRightRequest extends AdminRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Identifier of the user, <code>null</code> stands for all users. */
  private AgentId client;
  /**
   * Right to set, (-)3 for (un)setting an admin right, (-)2 for
   * (un)setting a writing permission, (-)1 for (un)setting a reading
   * permission, and 0 for removing all the user's permissions.
   */
  private int right;

  
  /**
   * Constructs a <code>SetRightRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param client  AgentId of client which right is to be set,
   *          <code>null</code> for all users.
   * @param right  Right to grant, authorized values: -3, -2, -1, 1, 2, 3.
   */
  public SetRightRequest(String id, AgentId client, int right) {
    super(id);
    this.client = client;
    this.right = right;
  }

 
  /** Returns the AgentId of the client which right is set. */
  public AgentId getClient() {
    return client;
  }

  /**
   * Returns the right to set, (-)3 for (un)setting an admin right, (-)2 for
   * (un)setting a writing permission, (-)1 for (un)setting a reading
   * permission, and 0 for removing all the user's permissions.
   */
  public int getRight() {
    return right;
  }
} 
