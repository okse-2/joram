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
package fr.dyade.aaa.joram;

import java.util.Vector;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public abstract class ConnectionFactory
                extends fr.dyade.aaa.joram.admin.AdministeredObject
                implements javax.jms.ConnectionFactory
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

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
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
  public abstract javax.jms.Connection
                  createConnection(String name, String password)
                  throws JMSException;

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection() throws JMSException
  {
    return createConnection("anonymous", "anonymous");
  }


  /** Returns the factory's configuration parameters. */
  public FactoryParameters getParameters()
  {
    return params;
  } 

  
  /** Sets the naming reference of a connection factory. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("cFactory.host", params.getHost()));
    ref.add(new StringRefAddr("cFactory.port",
                              (new Integer(params.getPort())).toString()));
    ref.add(
      new StringRefAddr("cFactory.cnxT",
                        (new Integer(params.connectingTimer)).toString()));
    ref.add(
      new StringRefAddr("cFactory.txT",
                        (new Integer(params.txPendingTimer)).toString()));
    return ref;
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
    h.put("soapCnxPendingTimer",new Integer(params.soapCnxPendingTimer));
    return h;
  }

  /**
   * Implements the <code>decode</code> abstract method defined in the
   * <code>fr.dyade.aaa.jndi2.soap.SoapObjectItf</code> interface.
   * <p>
   * Actual implementation of the method is located in the 
   * tcp and soap sub classes.
   */
  public Object decode(Hashtable h) {
    return null;
  }
}
