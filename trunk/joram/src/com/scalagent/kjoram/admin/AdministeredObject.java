/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.admin;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The <code>AdministeredObject</code> class is the parent class of all
 * JORAM administered objects.
 */
public abstract class AdministeredObject
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

  /** Retrieves an instance from the table. */
  public static Object getInstance(String name)
  {
    if (name == null)
      return null;
    return instancesTable.get(name);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void addInstanceTable(String key, Object value) {
    instancesTable.put(key,value);
  }

  /**
   * Codes a <code>AdministeredObject</code> as a Hashtable for travelling 
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    String className = this.getClass().getName();
    String end = className.substring(
      className.lastIndexOf((int)'.'),
      className.length());

    if (className.startsWith("com.scalagent.kjoram.admin")) {
      className = "org.objectweb.joram.client.jms.admin";
    } else if (className.startsWith("com.scalagent.kjoram.excepts")) {
      className = "org.objectweb.joram.shared.excepts";
    } else if (className.startsWith("com.scalagent.kjoram.jms")) {
      className = "org.objectweb.joram.shared.client";
    } else if (className.startsWith("com.scalagent.kjoram.ksoap")) {
      className = "org.objectweb.joram.client.jms.ksoap";
    } else if (className.startsWith("com.scalagent.kjoram.messages")) {
      className = "org.objectweb.joram.shared.messages";
    } else if (className.startsWith("com.scalagent.kjoram")) {
      className = "org.objectweb.joram.client.jms";
    }
    className = className + end;
    h.put("className",className);
    return h;
  }
}
