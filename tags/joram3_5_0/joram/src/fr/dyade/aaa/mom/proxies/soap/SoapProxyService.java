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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
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
   * service and starting the embedded JORAM server.
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
   * Service method: passes a hashtable containing an
   * <code>AbstractJmsRequest</code> client request or MOM messages to the
   * proxy.
   *
   * @param cnxId  The sending connection.
   * @param h  Hashtable containing a request or MOM messages.
   *
   * @exception Exception  If the connection has been closed.
   */
  public void send(int cnxId, java.util.Hashtable h) throws Exception
  {
    SoapProxy.ref.serviceReact(cnxId, h);
  }

  /**
   * Service method: returns a Hashtable containing an
   * <code>AbstractJmsReply</code> reply or MOM messages destinated to a
   * given connection context.
   *
   * @param cnxId  The identifier of the requesting connection.
   *
   * @exception Exception  If the connection has been closed.
   */
  public java.util.Hashtable getReply(int cnxId) throws Exception
  {
    return SoapProxy.ref.getReply(cnxId);
  }
}
