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

public class UpdateEvent implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private NamingContextId updatedContextId;

  private String name;
  
  private CompositeName compositeName;

  public UpdateEvent(CompositeName compositeName, NamingContextId updatedContextId,
                     String name) {
    this.updatedContextId = updatedContextId;
    this.name = name;
    this.compositeName=compositeName;
  }

  public final NamingContextId getUpdatedContextId() {
    return updatedContextId;
  }

  public final String getName() {
    return name;
  }

 public final CompositeName  getPath() {
    return compositeName;
  }
  public String toString() {
    return '(' + super.toString() +
	",updatedCompositeName=" + compositeName + 
	",updatedContextId=" + updatedContextId + 
	",name=" + name + ')';
  }
}
