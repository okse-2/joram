/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.admin;

import fr.dyade.aaa.jndi2.soap.SoapObjectItf;
import org.objectweb.joram.client.jms.JoramTracing;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import java.util.Hashtable;
import java.util.Vector;


/**
 * The <code>AdministeredObject</code> class is the parent class of all
 * JORAM administered objects.
 */
public abstract class AdministeredObject implements java.io.Serializable,
                                                    javax.naming.Referenceable,
                                                    SoapObjectItf
{
  /**
   * Class table holding the <code>AdministeredObject</code> instances.
   * <p>
   * <b>Key:</b> object's identifier<br>
   * <b>Object:</b> object's instance
   */
  protected static Hashtable instancesTable = new Hashtable();

  /** Identifier of the object. */
  protected String id;


  /**
   * Constructs an administered object.
   *
   * @param id  Identifier of the object.
   */ 
  protected AdministeredObject(String id)
  {
    this.id = this.getClass().getName() + ":" + id;

    // Registering this instance in the table:
    instancesTable.put(this.id, this);
  }

  /**
   * Constructs an empty administered object.
   */ 
  protected AdministeredObject()
  {}


  /** Sets the naming reference of an administered object. */
  public Reference getReference() throws NamingException
  {
    Reference ref =
      new Reference(this.getClass().getName(),
                    "org.objectweb.joram.client.jms.admin.ObjectFactory",
                    null);
    ref.add(new StringRefAddr("adminObj.id", id));
    return ref;
  }
  
  /** Retrieves an instance from the table. */
  public static Object getInstance(String name)
  {
    if (name == null)
      return null;
    return instancesTable.get(name);
  }


  /**
   * Codes an <code>AdministeredObject</code> as a Hashtable for travelling 
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    h.put("className", this.getClass().getName());
    return h;
  }
}
