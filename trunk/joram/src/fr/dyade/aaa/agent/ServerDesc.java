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
 * Description of an agent server. It is used by <code>Channel</code> and
 * <code>Network</code> objects.
 */
public final class ServerDesc implements Serializable {
  /** RCS version number of this file: $Revision: 1.12 $ */
  public static final String RCS_VERSION="@(#)$Id: ServerDesc.java,v 1.12 2003-03-19 15:16:06 fmaistre Exp $";

  /**  Server unique identifier. */
  short sid;
  /** Server name. */
  String name;
  /** Host name. */
  String hostname;
  /** Is the server transient? */
  boolean isTransient;
  /** Host address, use getAddr() method instead. */
  private transient InetAddress addr = null;
  /**
   * Description of services running on this server.
   */
  ServiceDesc[] services = null;
  /**
   * Server Id. of a gateway server for this server if it is not in a
   * adjoining domain.
   */
  short gateway = -1;
  /**
   * Domain description of this server.
   */
  MessageConsumer domain = null;

  /**
   * The communication port. This variable is set only if the server is
   * directly accessible from this node, in this case it corresponds to the
   * communication port of the server in the adjoining domain.
   */
  int port = -1;

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

  /**
   * Is the server transient?
   *
   * @return true if the server is transient, false otherwise.
   */
  public boolean isTransient() {
    return isTransient;
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
   * Gets the description of services running on this server.
   *
   * @return the description of services.
   */
  public ServiceDesc[] getServices() {
    return services;
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
      ",isTransient=" + isTransient +
      ",hostname=" + hostname +
      ",addr=" + addr +
      ",services=" + Strings.toString(services) +
      ",active=" + active +
      ",last=" + last + ")";
  }
}
