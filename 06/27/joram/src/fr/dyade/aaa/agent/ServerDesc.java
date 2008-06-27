/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Vector;

import fr.dyade.aaa.util.SocketAddress;
import fr.dyade.aaa.util.Strings;

/**
 * Description of an agent server. It is used by <code>Channel</code> and
 * <code>Network</code> objects. Be careful, this structure is initialized
 * in AgentServer, but it can be viewed outside of the agent package, so
 * it's very important to make all modifiers package.
 */
public final class ServerDesc implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**  Server unique identifier. */
  short sid;
  /** Server name. */
  String name;
  /**
   * The IP address of the server.
   * It contains hostname and port ({@link fr.dyade.aaa.util.SocketAddress
   * <code>SocketAddress</code>}) of remote server. The communication port
   * is set only if the server is directly accessible from this node; in
   * this case it corresponds to the communication port of the server in the
   * adjoining domain.
   * The descriptor of an HA server contains one <code>SocketAddress</code>
   * for each of its constituent.
   */
  private Vector sockAddrs = null;
  /**
   * Description of services running on this server.
   */
  transient ServiceDesc[] services = null;
  /**
   * Server Id. of a gateway server for this server if it is not in an
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
   * @param	sid		the server unique id
   * @param	name		the server name
   * @param	hostname	the server hostname
   * @param	port		the server port
   */
  public ServerDesc(short sid,
		    String name,
		    String hostname,
                    int port) {
    this.sid = sid;
    this.name = name;
    sockAddrs = new Vector();
    sockAddrs.addElement(new SocketAddress(hostname,port));
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
   * Gets hostname for this server.
   *
   * @return the hostname.
   */
  public String getHostname() {
    return  ((SocketAddress) sockAddrs.firstElement()).getHostname();
  }

  /**
   * Gets port for this server.
   *
   * @return the port.
   */
  public int getPort() {
    return ((SocketAddress) sockAddrs.firstElement()).getPort();
  }

  /**
   * Returns an IP address for its server.
   * 
   * @return	an IP address for this server.
   */
  public InetAddress getAddr() {
    return ((SocketAddress) sockAddrs.firstElement()).getAddress();
  }

  /**
   * Resolves an IP address for its server, don't use an eventually caching
   * address.
   * 
   * @return	an IP address for this server.
   */
  public InetAddress resetAddr() {
    ((SocketAddress) sockAddrs.firstElement()).resetAddr();
    return getAddr();
  }

  void addSockAddr(String hostname, int port) {
    sockAddrs.addElement(new SocketAddress(hostname, port));
  }

  void updateSockAddr(String hostname, int port) {
    sockAddrs.remove(0);
    sockAddrs.insertElementAt(new SocketAddress(hostname,port), 0);
  }

  /**
   * In case of an HA server, selects the IP address as this of the master
   * component of the HA configuration.
   */
  void moveToFirst(SocketAddress addr) {
    if (sockAddrs.indexOf(addr) > 0) {
      if (sockAddrs.remove(addr))
        sockAddrs.insertElementAt(addr,0);
    }
  }

  /**
   * In case of an HA server, gets the IP address of all the components
   * of the HA configuration.
   */
  Enumeration getSockAddrs() {
    return sockAddrs.elements();
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
    strBuf.append(",services=");
    Strings.toString(strBuf, services);
    strBuf.append(",active=").append(active);
    strBuf.append(",last=").append(last);
    strBuf.append(",gateway=").append(gateway);
    strBuf.append(",sockAddrs=").append(sockAddrs);
    strBuf.append(",domain=").append(domain);
    strBuf.append(")");
    return strBuf.toString();
  }
}
