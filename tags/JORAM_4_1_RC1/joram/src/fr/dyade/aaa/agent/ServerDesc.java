/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

import fr.dyade.aaa.util.Strings;

/**
 * Description of an agent server. It is used by <code>Channel</code> and
 * <code>Network</code> objects. Be careful, this structure is initialized
 * in AgentServer, but it can be viewed outside of the agent package, so
 * it's very important to make all modifiers package.
 */
public final class ServerDesc implements Serializable {
  /**  Server unique identifier. */
  short sid;
  /** Server name. */
  String name;
  /** Host name. */
  String hostname;
  /**
   * The communication port. This variable is set only if the server is
   * directly accessible from this node, in this case it corresponds to the
   * communication port of the server in the adjoining domain.
   */
  int port = -1;
  /** Host address, use getAddr() method instead. */
  private transient InetAddress addr = null;
  /**
   * Description of services running on this server.
   */
  transient ServiceDesc[] services = null;
  /**
   * Server Id. of a gateway server for this server if it is not in a
   * adjoining domain.
   */
  short gateway = -1;
  /**
   * Domain description of this server.
   */
  transient MessageConsumer domain = null;

  /** True if there is no waiting messages for this server. */
  transient volatile boolean active = true;
  /** Date of the last unsuccessful connection to this server. */
  transient volatile long last = 0L;
  /** Number of unsuccessful connection to this server. */
  transient volatile int retry = 0;
    
  /**
   * Constructs a new node for a persistent agent server.
   * @param	name		server name
   * @param	hostname	host name
   */
  public ServerDesc(short sid,
		    String name,
		    String hostname) {
    this.sid = sid;
    this.name = name;
    this.hostname = hostname;
  }

  /**
   * Gets server id. for this server.
   *
   * @return the server id.
   */
  public short getServerId() {
    return sid;
  }

  /**
   * Gets server name for this server.
   *
   * @return the server name.
   */
  public String getServerName() {
    return name;
  }

  /**
   * Gets host name for this server.
   *
   * @return the host name.
   */
  public String getHostname() {
    return hostname;
  }

  void setHostname(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Returns an IP address for its server.
   * 
   * @return	an IP address for this server.
   */
  public InetAddress getAddr() {
    if (addr == null) {
      try {
	addr = InetAddress.getByName(hostname);
      } catch (UnknownHostException exc) {
	addr = null;
      }
    }
    return addr;
  }

  /**
   * Resolves an IP address for its server, don't use an eventually caching
   * address.
   * 
   * @return	an IP address for this server.
   */
  public InetAddress resetAddr() {
    try {
      addr = InetAddress.getByName(hostname);
    } catch (UnknownHostException exc) {
      addr = null;
    }
    return addr;
  }

  public int getPort() {
    return port;
  }

  void setPort(int port) {
    this.port = port;
  }

  /**
   * Gets the description of services running on this server.
   *
   * @return the description of services.
   */
  public ServiceDesc[] getServices() {
    return services;
  }

  public short getGateway() {
    return gateway;
  }

  public String getDomainName() {
    return domain.getDomainName();
  }


  public Class getDomainType() {
    return domain.getClass();
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",sid=").append(sid);
    strBuf.append(",name=").append(name);
    strBuf.append(",hostname=").append(hostname);
    strBuf.append(",addr=").append(addr);
    strBuf.append(",services=");
    Strings.toString(strBuf, services);
    strBuf.append(",active=").append(active);
    strBuf.append(",last=").append(last);
    strBuf.append(",gateway=").append(gateway);
    strBuf.append(",port=").append(port);
    strBuf.append(",domain=").append(domain);
    strBuf.append(")");
    return strBuf.toString();
  }
}
