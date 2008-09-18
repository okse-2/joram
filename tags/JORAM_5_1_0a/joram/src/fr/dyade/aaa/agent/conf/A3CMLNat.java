/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies 
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

import java.io.*;

/**
 * The class <code>A3CMLNat</code> describes a 
 * network address translation.
 */
public class A3CMLNat implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** server id. */
  public short sid = -1;
  /** Value of the translation host. */
  public String host = null;
  /** Value of the translation port. */
  public int port = -1;

  public A3CMLNat(short sid, 
                  String host,
                  int port) {
    this.sid = sid;
    this.host = host;
    this.port = port;
  }
  
  public A3CMLNat duplicate() throws Exception {
    A3CMLNat clone = new A3CMLNat(sid, host, port);
    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append(super.toString());
    strBuf.append(",sid=").append(sid);
    strBuf.append(",host=").append(host);
    strBuf.append(",port=").append(port);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLNat) {
      A3CMLNat nat = (A3CMLNat) obj;
      if ((sid == nat.sid) &&
          ((host == nat.host) ||
           ((host != null) && host.equals(nat.host))) &&
          (port == nat.port))
        return true;
    }
    return false;
  }
}
