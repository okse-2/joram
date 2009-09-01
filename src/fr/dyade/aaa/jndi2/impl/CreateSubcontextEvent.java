/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.impl;

import javax.naming.*;

public class CreateSubcontextEvent extends UpdateEvent {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private CompositeName path;
 
  private NamingContextId contextId;

  private Object ownerId;

  public CreateSubcontextEvent(NamingContextId updatedContextId,
                               String name,
                               CompositeName path,
                               NamingContextId contextId,
                               Object ownerId) {
    super(path,updatedContextId, name);
    this.path = path;
    this.contextId = contextId;
    this.ownerId = ownerId;
  }

    /* public final CompositeName getPath() {
    return path;
    }*/
 
  public final NamingContextId getContextId() {
    return contextId;
  }

  public final Object getOwnerId() {
    return ownerId;
  }

  public String toString() {
    return '(' + super.toString() +
      ",path=" + path +
      ",contextId=" + contextId + 
      ",ownerId=" + ownerId + ')';
  }
}
