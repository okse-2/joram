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

import java.util.Vector;

public class SoapObject {
  String namespace;
  String name;
  Vector info = new Vector ();
  Vector data = new Vector ();

  public SoapObject(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public boolean equals(Object o) {
    if (!(o instanceof SoapObject)) 
      return false;
    
    SoapObject so = (SoapObject) o;
    int cnt = data.size();
    if (cnt != so.data.size()) 
      return false;
    
    try {
      for (int i = 0; i < cnt; i++) 
        if (!data.elementAt(i).equals(
          so.getProperty(((PropertyInfo) info.elementAt(i)).name))) 
          return false;
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  public String getName() {
    return name;
  }
  
  public String getNamespace() {
    return namespace;
  }

  public Object getProperty(int index) {
    return data.elementAt(index);
  }

  public Object getProperty(String name) {
    for (int i = 0; i < data.size(); i++) {
      if (name.equals(((PropertyInfo) info.elementAt(i)).name))
        return data.elementAt(i);
    }
    throw new RuntimeException ("illegal property: "+name);
  }

  public int getPropertyCount() {
    return data.size();
  }

  public PropertyInfo getPropertyInfo(int index) {
    return (PropertyInfo) info.elementAt(index);
  }
  
  public SoapObject newInstance() {
    SoapObject o = new SoapObject(namespace,name);
    for (int i = 0; i < data.size(); i++) {
      PropertyInfo p = (PropertyInfo) info.elementAt(i);
      o.addProperty(p.name,data.elementAt(i));
    }
    return o;
  }

  public void setProperty(int index, Object value) {
    data.setSize(index+1);
    data.setElementAt(value,index);
  }
    
  public SoapObject addProperty(String name, Object value) {
    PropertyInfo prop = new PropertyInfo(name,new Object().getClass());
    if (value != null)
      prop = new PropertyInfo(name,value.getClass());
    return addProperty(prop,value);
  }

  public SoapObject addProperty(PropertyInfo p, Object value) {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapObject.addProperty(" + p + "," + value + ")");
    info.addElement(p);
    data.addElement(value);
    return this;
  }

  public String toString() {
    return "SoapObject (" + namespace +
      "," + name + 
      "," + info +
      "," + data +")";
  }
}
