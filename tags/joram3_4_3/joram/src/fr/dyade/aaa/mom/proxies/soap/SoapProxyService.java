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
package fr.dyade.aaa.mom.proxies.soap;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.mom.MomTracing;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>SoapProxyService</code> class implements the SOAP service
 * accessed by the JORAM clients using a <code>SoapConnection</code> for
 * connecting to the MOM. 
 * <p>
 * It actually links the client with its <code>SoapProxy</code> agent.
 *
 * @see SoapProxy
 */
public class SoapProxyService
{
  /**
   * Service method: called by the SOAP client for instanciating the SOAP
   * service and forcing the embedded JORAM server to be started as well.
   *
   * @param serverId  Identifier of the embedded server.
   * @param serverName  Name of the embedded server.
   *
   * @exception Exception  If the embedded server could not start.
   */
  public void start(int serverId, String serverName) throws Exception
  {
    String[] args = {"" + serverId, serverName};
    AgentServer.init(args);
    AgentServer.start();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "SoapProxyService started.");
  }

  /**
   * Service method: returns the identifier of a given user connection, 
   * or -1 if it is not a valid user of the SOAP proxy.
   *
   * @param name  User's name.
   * @param password  User's password.
   * @param timeout  Duration in seconds during which a connection might
   *          be inactive before considered as dead (0 for never).
   *
   * @exception Exception  If the proxy is not deployed.
   */
  public int setConnection(String name, String password, int timeout)
             throws Exception
  {
    if (SoapProxy.ref == null)
      throw new Exception("SoapProxy not deployed.");

    return SoapProxy.ref.setConnection(name, password, timeout);
  }

  /**
   * Service method: passes a vector containing an
   * <code>AbstractJmsRequest</code> client request or MOM messages to the
   * proxy.
   *
   * @param cnxId  The sending connection.
   * @param vec  Vector containing a request or MOM messages.
   *
   * @exception Exception  If the connection has been closed.
   */
  public void send(int cnxId, java.util.Vector vec) throws Exception
  {
    SoapProxy.ref.serviceReact(cnxId, vec);
  }

  /**
   * Service method: returns a vector containing an
   * <code>AbstractJmsReply</code> reply or MOM messages destinated to a
   * given connection context.
   *
   * @param cnxId  The identifier of the requesting connection.
   *
   * @exception Exception  If the connection has been closed.
   */
  public java.util.Vector getReply(int cnxId) throws Exception
  {
    return SoapProxy.ref.getReply(cnxId);
  }
}
