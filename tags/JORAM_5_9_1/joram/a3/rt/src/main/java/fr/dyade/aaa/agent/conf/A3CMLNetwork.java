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
 * The class <code>A3CMLNetwork</code> describes a network component.
 */
public class A3CMLNetwork implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public String domain = null;
  public int port = -1;

  public A3CMLNetwork(String domain,
                      int port) {
    this.domain = domain;
    this.port = port;
  }

  public A3CMLNetwork duplicate() throws Exception {
    A3CMLNetwork clone = new A3CMLNetwork(domain, port);
    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",domain=").append(domain);
    strBuf.append(",port=").append(port);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLNetwork) {
      A3CMLNetwork network = (A3CMLNetwork) obj;
      if (((domain == network.domain) ||
           ((domain != null) && domain.equals(network.domain))) &&
          (port == network.port))
        return true;
    }
    return false;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result = prime * result + port;
    return result;
  }
}
