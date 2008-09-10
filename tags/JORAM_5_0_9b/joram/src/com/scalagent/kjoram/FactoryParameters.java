/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

/**
 * A <code>FactoryParameters</code> instance holds a
 * <code>&lt;XA&gt;ConnectionFactory</code> configuration parameters.
 */
public class FactoryParameters
{
  /** Name of host hosting the server to create connections with. */
  private String host; 
  /** Port to be used for accessing the server. */
  private int port; 

  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 0;
  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  public int txPendingTimer = 0;
  /** 
   * Duration in seconds during which a SOAP connection might pending server
   * side considered as dead (0 for never); a SOAP connections is pending
   * server side when a client does not properly disconnect.
   */
  public int soapCnxPendingTimer = 0;


  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param host  Name of host hosting the server to create connections with.
   * @param port  Port to be used for accessing the server.
   */
  public FactoryParameters(String host, int port)
  {
    this.host = host;
    this.port = port;
  }


  /**
   * Returns the name of host hosting the server to create connections with.
   */
  public String getHost()
  {
    return host;
  }

  /** Returns the port to be used for accessing the server. */
  public int getPort()
  {
    return port;
  }
}
