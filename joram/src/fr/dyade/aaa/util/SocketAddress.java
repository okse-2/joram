/*
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package fr.dyade.aaa.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class implements an IP Socket Address (IP address + port number)
 */
public class SocketAddress {
  /** The hostname of the Socket Address */
  private String hostname;
  /** The IP address of the Socket Address */
  private InetAddress addr;
  /** The port number of the Socket Address */
  private int port;

  /**
   * Creates a socket address from a hostname and a port number.
   *
   * @param	hostname the Host name
   * @param	port	The port number
   */
  public SocketAddress(String hostname, int port) {
    this.hostname = hostname;
    try {
      addr = InetAddress.getByName(this.hostname);
    } catch (UnknownHostException exc) {
      addr = null;
    }
    this.port = port;
  }

  /**
   * Resolves the IP address for this hostname, don't use an eventually
   * caching address.
   * 
   * @return	the resolved IP address.
   */
  public InetAddress resetAddr() {
    try {
      addr = InetAddress.getByName(getHostname());
    } catch (UnknownHostException exc) {
      addr = null;
    }
    return addr;
  }

  /**
   * Gets the port number.
   *
   * @return the port number.
   */
   public int getPort() {
    return port;
  }
  
  /**
   * Gets the <code>hostname</code>.
   *
   * @return	the hostname part of the address.
   */
  public String getHostname() {
    return hostname;
  }
  
  /**
   * Gets the <code>InetAddress</code>.
   *
   * @return the InetAdress or <code>null</code> if it is unresolved.
   */
  public InetAddress getAddress() {
    return addr;
  }
  
  /**
   * Compares this object against the specified object.
   *
   * @param   obj   the object to compare against.
   * @return  <code>true</code> if the objects are the same;
   *          <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof SocketAddress))
      return false;
    SocketAddress sa = (SocketAddress) obj;

    if ((hostname.equals(sa.hostname)) &&
        (addr != null && addr.equals(sa.addr)) &&
        (port == sa.port))
      return true;
    return false;
  }

  /**
   * Constructs a string representation of this InetSocketAddress.
   *
   * @return  a string representation of this object.
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",hostname=").append(hostname);
    strBuf.append(",port=").append(port);
    strBuf.append(",addr=").append(addr);
    strBuf.append(")");
    return strBuf.toString();
  }
}
