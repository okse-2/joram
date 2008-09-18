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

import java.util.Vector;
import java.util.Hashtable;

import com.scalagent.kjoram.excepts.JMSException;


public abstract class ConnectionFactory
  extends com.scalagent.kjoram.admin.AdministeredObject
{
  /** Object containing the factory's parameters. */
  protected FactoryParameters params;


  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public ConnectionFactory(String host, int port)
  {
    super(host + ":" + port);
    params = new FactoryParameters(host, port);

    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, this + ": created.");
  }

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   */
  public ConnectionFactory()
  {}


  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "CF:" + params.getHost() + "-" + params.getPort();
  }


  /**
   * API method, implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract Connection
                  createConnection(String name, String password)
                  throws JMSException;

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection createConnection() throws JMSException
  {
    return createConnection("anonymous", "anonymous");
  }

  /**
   * Returns the factory's configuration parameters.
   */
  public FactoryParameters getParameters()
  {
    return params;
  } 

  public void setParameters(FactoryParameters params) {
    this.params = params;
  }

  /**
   * Codes a <code>ConnectionFactory</code> as a Hashtable for travelling
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = super.code();
    h.put("host",params.getHost());
    h.put("port",new Integer(params.getPort()));
    h.put("connectingTimer",new Integer(params.connectingTimer));
    h.put("txPendingTimer",new Integer(params.txPendingTimer));
    h.put("cnxPendingTimer",new Integer(params.soapCnxPendingTimer));
    return h;
  }

  public static Object decode(Hashtable h) {
    return null;
  }
}
