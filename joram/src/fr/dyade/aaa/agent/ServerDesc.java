/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

import fr.dyade.aaa.util.Strings;

/**
 * Description of a remote server.
 * @author	Andr* Freyssinet
 */
public final class ServerDesc implements Serializable {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: ServerDesc.java,v 1.3 2000-10-05 15:15:23 tachkeni Exp $";

  /**
   * Server unique identifier.
   */
  public short sid;
  /**
   * Server name.
   */
  public String name;
  /**
   * Host name.
   */
  public String hostname;
  /**
   * Host address, use getAddr() method instead.
   */
  private transient InetAddress addr = null;
  /**
   * Server port.
   */
  public int port = -1;
  /**
   * Listen port for transient agent servers to connect,
   * -1 when not applicable.
   */
  public int transientPort = -1;
  /**
   * Id of persistent proxy agent responsible for a transient server,
   * null when not applicable.
   */
  public AgentId proxyId = null;
  /**
   * Description of services running on this server.
   */
  public ServiceDesc[] services = null;
  /**
   * Server state
   */
  transient volatile boolean active;
  transient volatile long last;
  transient volatile int retry;

  /**
   * Server type (monitored or not)
   */
  public boolean administred = Server.ADMINISTRED;
  
  /**
   * Server actually monitored or not
   */
  public boolean admin = Server.admin;
    
  /**
   * Constructs a new node for a persistent agent server.
   * @param	name		server name
   * @param	hostname	host name
   * @param	port		server port
   */
  public ServerDesc(short sid,
		    String name,
		    String hostname,
		    int port) {
    this.sid = sid;
    this.name = name;
    this.hostname = hostname;
    this.port = port;
    try {
      this.addr = InetAddress.getByName(hostname);
    } catch (UnknownHostException exc) {
      this.addr = null;
      Debug.trace("Can't resolve \"" + hostname + "\" Inet address", exc);
    }
    this.services = null;
    this.active = true;
    this.last = 0L;
    this.retry = 0;
  }

  /**
   * Constructs a new node for a transient agent server.
   * @param	sid		unique server id
   * @param	name		server name
   * @param	hostname	host name
   * @param	id		unique id of proxy server
   */
  public ServerDesc(short sid,
		    String name,
		    String hostname,
		    short persistentId) {
    this.sid = sid;
    this.name = name;
    this.hostname = hostname;
    try {
      this.addr = InetAddress.getByName(hostname);
    } catch (UnknownHostException exc) {
      this.addr = null;
      Debug.trace("Can't resolve \"" + hostname + "\" Inet address", exc);
    }
    this.proxyId = new AgentId(persistentId,
			       persistentId,
			       AgentId.TransientProxyIdStamp);
    this.services = null;
  }

  /**
   * Returns an IP address for its server.
   * 
   * @return	an IP address for this server.
   * @exception	if no IP address for the host could be found.
   */
  public final InetAddress getAddr() throws UnknownHostException {
    if (addr == null) {
      try {
	addr = InetAddress.getByName(hostname);
      } catch (UnknownHostException exc) {
	addr = null;
	throw exc;
      }
    }
    return addr;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",sid=" + sid + 
      ",name=" + name +
      ",hostname=" + hostname +
      ",addr=" + addr +
      ",port=" + port +
      ",transientPort=" + transientPort +
      ",proxyId=" + proxyId +
      ",services=" + Strings.toString(services) +
      ",active=" + active +
      ",last=" + last +
      ",Administred=" + administred +
      ",admin=" + admin + ")";
  }
}
