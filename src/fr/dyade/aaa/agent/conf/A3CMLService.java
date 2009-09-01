/*
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
 */
package fr.dyade.aaa.agent.conf;

import java.io.Serializable;

/**
 * The class <code>A3CMLService</code> describes a service.
 */
public class A3CMLService implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public String classname = null;
  public String args = null;

  public A3CMLService(String classname,
                      String args) {
    this.classname = classname;
    this.args = args;
  }

  public A3CMLService duplicate() throws Exception {
    A3CMLService clone = new A3CMLService(classname, args);
    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append("classname=");
    strBuf.append(classname);
    strBuf.append(",args=");
    strBuf.append(args);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLService) {
      A3CMLService service = (A3CMLService) obj;
      if (((args == service.args) ||
           ((args != null) && args.equals(service.args))) &&
          ((classname == service.classname) ||
           ((classname != null) && classname.equals(service.classname))))
        return true;
    }
    return false;
  }
}
