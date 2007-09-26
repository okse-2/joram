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

import org.objectweb.util.monolog.api.*;

/**
 * The class <code>A3CMLDomain</code> describes an agent server domain.
 */
public class A3CMLDomain implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Name of the domain. */
  public String name = null;
  /** Full name of Java class */
  public String network = null;
  /** Description of alls servers in domain */
  public Vector servers = null;
  /**
   * Server Id. of router (1st hop) to access this domain from current node,
   * if -1 the domain is not accessible.
   */
  public short gateway = -1;
  /**
   * Logical distance between the server of this domain and the root one, i.e.
   * the number of hops to reach a server from the local one.
   */
  public int hops = -1;

//   transient MessageConsumer consumer = null;

  public A3CMLDomain(String name, String network) throws Exception {
    if (name.equals("local"))
      throw new Exception("Domain name \"" + name + "\" is reserved.");
    this.name = name;
    if ((network == null) || network.equals(""))
      this.network = "fr.dyade.aaa.agent.SimpleNetwork";
    else
      this.network = network;
//     consumer = (Network) Class.forName(network).newInstance();
  }
  
  public void addServer(A3CMLServer server) {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, 
                     "A3CMLDomain.addServer(" + server + ')');
    if (servers == null) servers = new Vector();
    servers.addElement(server);
  }

  public void removeServer(A3CMLServer server) {
    if (servers == null) return;
    servers.removeElement(server);
  }

  public void removeServer(short sid) {
    if (servers == null) return;
    for (int i = 0; i < servers.size(); i++) {
      A3CMLServer serv = (A3CMLServer) servers.elementAt(i);
      if (serv.sid == sid) {
        servers.removeElementAt(i);
      }
    }
  }

//   public boolean containsServer(A3CMLServer server) {
//     if (servers == null) return false;
//     return servers.contains(server);
//   }

  public short[] getServersId() {
    if (servers != null) {
      short[] domainSids = new short[servers.size()];
      for (int i=0; i<domainSids.length; i++) {
        domainSids[i] = ((A3CMLServer) servers.elementAt(i)).sid;
      }
      return domainSids;
    }
    return new short[0];
  }

  public A3CMLDomain duplicate() throws Exception {
    A3CMLDomain clone = new A3CMLDomain(name,network);
    if (servers != null) {
      for (Enumeration s = servers.elements(); s.hasMoreElements(); )
        clone.addServer(((A3CMLServer) s.nextElement()).duplicate());
    }
    clone.gateway = gateway;
    return clone;
  }

  public A3CMLDomain duplicate(Hashtable context) throws Exception {
    A3CMLDomain clone = new A3CMLDomain(name,network);
    if (servers != null) {
      for (Enumeration s = servers.elements(); s.hasMoreElements(); )
        clone.addServer(((A3CMLServer) s.nextElement()).duplicate(context));
    }
    clone.gateway = gateway;
    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",name=").append(name);
    strBuf.append(",network=").append(network);
    strBuf.append(",servers=").append(servers);
    strBuf.append(",gateway=").append(gateway);
    strBuf.append(",hops=").append(hops);
    strBuf.append(")");

    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLDomain) {
      A3CMLDomain domain = (A3CMLDomain) obj;
      if (name.equals(domain.name) &&
          network.equals(domain.network) &&
          ((servers == domain.servers) ||
           ((servers != null) && servers.equals(domain.servers))) &&
          (gateway == domain.gateway))
      return true;
    }
    return false;
  }

//   private void writeObject(java.io.ObjectOutputStream out)
//     throws IOException {
//     out.writeUTF(name);
//     out.writeUTF(network);
//     out.writeObject(servers);
//   }
  
//   private void readObject(java.io.ObjectInputStream in)
//     throws IOException, ClassNotFoundException, 
//     InstantiationException, IllegalAccessException {
//     name = in.readUTF();
//     network = in.readUTF();
//     servers = (Vector) in.readObject();
//     gateway = -1;
//     consumer = (Network) Class.forName(network).newInstance();
//   }
}
