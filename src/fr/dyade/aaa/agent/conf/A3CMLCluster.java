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
 *
 * Initial developer(s): Nicolas Tachker (Scalagent)
 * Contributor(s):
 */
package fr.dyade.aaa.agent.conf;

import java.io.*;
import java.util.*;

public class A3CMLCluster implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public short sid = -1;
  public String name = null;
  public String jvmArgs = null;
  public Hashtable servers = null;
  public Hashtable properties = null;

  public A3CMLCluster(short sid,
                      String name) throws Exception {
    this.sid = sid;
    this.name = name;
    this.properties = new Hashtable();
    this.servers = new Hashtable();
  }

  /**
   * Adds a server.
   *
   * @param server	The description of added server.
   * @exception DuplicateServerException
   *			If the server already exist.
   */
  public final void addServer(A3CMLServer server) throws DuplicateServerException {
    Short id = new Short(server.sid);
    if (servers.containsKey(id))
      throw new DuplicateServerException("Duplicate server id. #" + server.sid);
    server.name = "cluster_" + sid + "_" + server.sid;
    server.sid = sid;
    servers.put(id, server);
  }
  
  /**
   * Removes a server.
   *
   * @param sid  	The unique server identifier.
   * @return	 	The server description if exists.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer removeServer(short sid) throws UnknownServerException {
    A3CMLServer server = null;
    Short id = new Short(sid);
    if (servers.containsKey(id))
      server = (A3CMLServer) servers.remove(id);
    else
      throw new UnknownServerException("Unknown server id. #" + sid);
    return server;
  }
  
  /**
   * Remove a server.
   *
   * @param name 	The server name.
   * @return	 	The server description if exists.
   * @exception UnknownServerException
   *			If the server does not exist.
   */
  public final A3CMLServer removeServer(String name) throws UnknownServerException {
    return removeServer(getServerIdByName(name));
  }
  
  /**
   * Returns true if the configuration contains a server with specified id.
   *
   * @param sid  server id
   * @return	 true if contain sid; false otherwise.
   */
  public final boolean containsServer(short sid) {
    return servers.containsKey(new Short(sid));
  }
  
  /**
   * Gets a server identifier from its name.
   *
   * @param name 	The server name.
   * @return	 	The server identifier.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public short getServerIdByName(String name) throws UnknownServerException {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.name.equals(name)) return server.sid;
    }
    throw new UnknownServerException("Unknown server " + name);
  }

  /**
   * Returns true if the configuration contains a server with specified name.
   *
   * @param name server name
   * @return	 true if contain name; false otherwise.
   */
  public final boolean containsServer(String name) {
    try {
      getServerIdByName(name);
    } catch (UnknownServerException exc) {
      return false;
    }
    return true;
  }

  /**
   * Returns the description of a server.
   *
   * @param name 	The server identifier.
   * @return	 	The server description if exist.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer getServer(short sid) throws UnknownServerException {
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);
    return server;
  }

  /**
   * Returns the description of a server.
   *
   * @param name 	The server name.
   * @return	 	The server description if exist.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer getServer(String name) throws UnknownServerException {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.name.equals(name)) return server;
    }
    throw new UnknownServerException("Unknown server id for server " + name);
  }

  /**
   * add property
   *
   * @param prop A3CMLProperty
   * @return	 the previous value of the specified prop.name in
   *             this hashtable, or null if it did not have one.
   * @exception	Exception
   */
  public final A3CMLProperty addProperty(A3CMLProperty prop) throws Exception {
    return (A3CMLProperty) properties.put(prop.name, prop);
  }

  /**
   * remove property
   *
   * @param name property name
   * @return	 the value to which the name had been mapped in 
   *             this hashtable, or null if the name did not have a mapping.
   */
  public final A3CMLProperty removeProperty(String name) {
    return (A3CMLProperty) properties.remove(name);
  }

  /**
   * contains property
   *
   * @param name property name
   * @return	 true if contain name; false otherwise.
   */
  public final boolean containsProperty(String name) {
    return properties.containsKey(name);
  }

  /**
   * Returns the specified property.
   */
  public final A3CMLProperty getProperty(String name) {
    return (A3CMLProperty) properties.get(name);
  }

  /**
   * Get the JVM argument for a particular agent server identified by its id.
   *
   * @param id		agent server identifier.
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *			The specified server does not exist.
   */
  public final String getJvmArgs(short sid) throws UnknownServerException {
    A3CMLServer server = getServer(sid);
    return server.getJvmArgs();
  }
  
  /**
   * Get the JVM argument for a particular agent server identified by its name.
   *
   * @param name	agent server name.
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *			The specified server does not exist.
   */
  public final String getJvmArgs(String name) throws UnknownServerException {
    A3CMLServer server = getServer(name);
    return server.getJvmArgs();
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",sid=").append(sid);
    strBuf.append(",name=").append(name);
    strBuf.append(",jvmArgs=").append(jvmArgs);
    strBuf.append(",properties=").append(properties);
    strBuf.append(",servers=").append(servers);
    strBuf.append(")");

    return strBuf.toString();
  }
  
  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLCluster) {
      A3CMLCluster cluster = (A3CMLCluster) obj;

      if (sid == cluster.sid &&
          name.equals(cluster.name) &&
          servers.equals(cluster.servers) &&
          properties.equals(cluster.properties))
        return true;
    }
    return false;
  }
}
