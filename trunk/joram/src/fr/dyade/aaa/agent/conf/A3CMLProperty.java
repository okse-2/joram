/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies 
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
 */
package fr.dyade.aaa.agent.conf;

import java.io.Serializable;

/**
 * The class <code>A3CMLProperty</code> describes a property.
 */
public class A3CMLProperty implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Name of the property. */
  public String name = null;
  /** Value of the property. */
  public String value = null;

  public A3CMLProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  public A3CMLProperty duplicate() throws Exception {
    A3CMLProperty clone = new A3CMLProperty(name, value);
    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append(super.toString());
    strBuf.append(",").append(name).append("=").append(value);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLProperty) {
      A3CMLProperty prop = (A3CMLProperty) obj;
      if (((name == prop.name) ||
           ((name != null) && name.equals(prop.name))) &&
          ((value == prop.value) ||
           ((value != null) && value.equals(prop.value))))
        return true;
    }
    return false;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }
}
