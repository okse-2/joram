/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Thrown when a <code>ContextRecord</code> has not been found
 * in a naming context. This may happen if the name asked in the
 * JNDI request doesn't exist. That's why this exception includes
 * a <code>NameNotFoundException</code> ready to be thrown forward.
 * But the missing record may be resolved. For example, 
 * in a distributed configuration a missing record may indicate 
 * that the local JNDI server (where the record is missing) is not
 * up to date according to a remote server where the record has
 * been created. In this case, instead of throwing a
 * <code>NameNotFoundException</code> the JNDI request 
 * may be blocked until the update arrives 
 * and creates the missing record.
 */
public class MissingRecordException extends NamingException {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private NamingContextId namingContextId;

  private Object ownerId;

  private NameNotFoundException nnfe;

  /**
   * Constructs a <code>MissingRecordException</code>.
   *
   * @param namingContextId the identifier of the context where
   * the record is missing.
   *
   * @param nnfe the naming exception to be thrown if the
   * record really doesn't exist.
   */
  public MissingRecordException(
    NamingContextId namingContextId,
    Object ownerId,
    NameNotFoundException nnfe) {
    this.namingContextId = namingContextId;
    this.ownerId = ownerId;
    this.nnfe = nnfe;
  }
  
  public final NamingContextId getNamingContextId() {
    return namingContextId;
  }

  public final Object getOwnerId() {
    return ownerId;
  }

  public final NameNotFoundException getNameNotFoundException() {
    return nnfe;
  }
}
