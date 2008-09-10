/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.ksoap;

import com.scalagent.ksoap.marshal.Marshal;

public class SoapPrimitive {

  String namespace;
  String name;
  Class clazz;
  Marshal marshal;

  public SoapPrimitive(String namespace, 
                       String name, 
                       Class clazz, 
                       Marshal marshal) {
    this.namespace = namespace;
    this.name = name;
    this.clazz = clazz;
    this.marshal = marshal;
  }

  public boolean equals(Object o) {
    if (!(o instanceof SoapPrimitive)) return false;
    SoapPrimitive p = (SoapPrimitive) o;
    return name.equals(p.name) && namespace.equals(p.namespace);
  }

  public int hashCode() {
    return name.hashCode() ^ namespace.hashCode();
  }

  public String getNameSpace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public String getClassName() {
    return clazz.getName();
  }

  public Marshal getMarshal() {
    return marshal;
  }

  public String toString() {
    return "SoapPrimitive(" + 
      namespace + "," +
      name + "," +
      clazz + "," +
      marshal + ")";
  }
}

