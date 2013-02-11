/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom R&D
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.admin;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;


/**
 * The <code>AdministeredObject</code> class is the parent class of all
 * JORAM administered objects.
 */
public abstract class  AdministeredObject implements Serializable, javax.naming.Referenceable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public final Reference getReference() throws NamingException {
    Reference ref = null;
    ref = new Reference(this.getClass().getName(), ObjectFactory.class.getName(), null);
    toReference(ref);
    return ref;
  }

  /** Sets the naming reference of an administered object. */
  public abstract void toReference(Reference ref) throws NamingException;

  /** Restores the administered object from a naming reference. */
  public abstract void fromReference(Reference ref) throws NamingException;
}
