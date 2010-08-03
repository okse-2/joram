/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
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

/**
 * An <code>AdminRequest</code> is used by a client agent for sending an
 * administration request to a destination agent.
 */
public abstract class AdminRequestNot extends AbstractNotification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Field used for identifying the request. */
  private String id;

  /**
   * Constructs an <code>AdminRequest</code>.
   *
   * @param id  Identifier of the request, may be null.
   */
  public AdminRequestNot(String id) {
    this.id = id;
  }


  /** Returns the request identifier, null if not used. */
  public String getId() {
    return id;
  }
}
