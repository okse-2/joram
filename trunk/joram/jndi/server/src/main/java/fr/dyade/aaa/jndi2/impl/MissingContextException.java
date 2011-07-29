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

import javax.naming.CompositeName;
import javax.naming.NamingException;

/**
 * Thrown when a naming context has not been found
 * whereas its parent naming context contains a
 * <code>ContextRecord</code> indicating that
 * the naming context exists.
 * This may happen in a distributed JNDI configuration
 * when a naming context has not been
 * locally created yet. For example if the context
 * /A has been created on the server 0 and the context
 * /A/B on the server 1. If the server 2 starts, it gets 
 * from the server 0 the naming context /A 
 * containing a <code>ContextRecord</code> named B. 
 * If a JNDI request is asked about B (e.g. bind /A/B/C)
 * then the server 2 can't find the naming context B because
 * it still didn't get the naming data from server 1. So a 
 * <code>MissingContextException</code> is thrown.
 */
public class MissingContextException extends NamingException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * The identifier of the missing context
   */ 
  private NamingContextId missingContextId;

  private CompositeName name;

  /**
   * Constructs a <code>MissingContextException</code>.
   *
   * @param missingContextId the identifier of the missing context
   */
  public MissingContextException(
    NamingContextId missingContextId,
    CompositeName name) {
    this.missingContextId = missingContextId;
    this.name = name;
  }
  
  public final NamingContextId getMissingContextId() {
    return missingContextId;
  }

  public final CompositeName getName() {
    return name;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",missingContextId=" + missingContextId + 
      ",name=" + name + ')';
  }
}
