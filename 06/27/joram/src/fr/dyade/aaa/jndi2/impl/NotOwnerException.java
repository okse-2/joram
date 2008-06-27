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

import javax.naming.NamingException;

/**
 * Thrown when a naming operation (bind, rebind, 
 * createSubcontext, destroySubcontext) is done on
 * a naming context which owner is not the local
 * naming server.
 */
public class NotOwnerException extends NamingException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Object owner;

  /**
   * Constructs a <code>NotOwnerException</code>.
   *
   * @param owner the identifier of the owner 
   * of the context on which the naming operation
   * is done.
   */
  public NotOwnerException(Object owner) {
    this.owner = owner;
  }
  
  public final Object getOwner() {
    return owner;
  }
}
