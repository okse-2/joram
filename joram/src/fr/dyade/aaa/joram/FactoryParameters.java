/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;


/**
 * A <code>FactoryParameters</code> instance holds a
 * <code>&lt;XA&gt;ConnectionFactory</code> configuration parameters.
 */
public class FactoryParameters implements java.io.Serializable
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
