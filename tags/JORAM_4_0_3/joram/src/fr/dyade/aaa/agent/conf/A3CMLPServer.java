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
import java.util.*;

/**
 * The class <code>PServer</code> describes a persistent agent server.
 */
public class A3CMLPServer extends A3CMLServer implements Serializable {
  /** Domain (1st hop) used to access this server from current node. */
  public String domain = null;
  /**
   * Communication port if the server is directly accessible by the root
   * server.
   */
  public int port = -1;

  public A3CMLPServer(short sid,
                      String name,
                      String hostname) throws Exception {
    super(sid, name, hostname);
  }

  public void addNetwork(A3CMLNetwork network) {
    networks.addElement(network);
  }

  public A3CMLPServer duplicate(Hashtable context) throws Exception {
    A3CMLPServer clone = null;
    Short serverSid = new Short(sid);
    if (!context.containsKey(serverSid)) {
      clone = duplicate();
      context.put(serverSid,clone);
    } else
      clone = (A3CMLPServer) context.get(serverSid);
    return clone;
  }

  public A3CMLPServer duplicate() throws Exception {
    A3CMLPServer clone = new A3CMLPServer(sid, 
                                          name, 
                                          hostname);
    if (networks != null) {
      for (Enumeration n = networks.elements(); n.hasMoreElements(); )
        clone.networks.addElement(
          ((A3CMLNetwork) n.nextElement()).duplicate());
    }
    clone.gateway = gateway;
    clone.domain = domain;
    clone.port = port;

    // super class
    clone.visited = visited;
    if (services != null) {
      for (Enumeration e = services.elements(); e.hasMoreElements(); )
        clone.services.addElement(
          ((A3CMLService) e.nextElement()).duplicate());
    }
    if (properties != null) {
      for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
        Short sid = (Short) e.nextElement();
        clone.properties.put(sid, 
                             ((A3CMLProperty) properties.get(sid)).duplicate());
      }
    }
    if (nat != null) {
      for (Enumeration e = nat.elements(); e.hasMoreElements(); )
        clone.addNat(
          ((A3CMLNat) e.nextElement()).duplicate());
    }
    clone.jvmArgs = jvmArgs;

    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append(super.toString());
    strBuf.append(",port=").append(port);
    strBuf.append(",domain=").append(domain);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLPServer) {
      A3CMLPServer server = (A3CMLPServer) obj;
      if (super.equals(server) &&
          ((domain == server.domain) ||
           ((domain != null) && domain.equals(server.domain))) &&
          (port == server.port))
      return true;
    }
    return false;
  }

//   private void writeObject(java.io.ObjectOutputStream out)
//     throws IOException {
//     out.writeObject(domain);
//     out.writeInt(port);
//   }
  
//   private void readObject(java.io.ObjectInputStream in)
//     throws IOException, ClassNotFoundException {
//     domain = (String) in.readObject();
//     port = in.readInt();
//     gateway = -1;
//     visited = false;
//   }
}
