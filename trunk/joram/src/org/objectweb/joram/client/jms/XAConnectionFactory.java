/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.util.Vector;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XAConnectionFactory</code> interface.
 */
public abstract class XAConnectionFactory
                extends org.objectweb.joram.client.jms.admin.AdministeredObject
                implements javax.jms.XAConnectionFactory
{
  /** Factory's parameters object. */
  protected FactoryParameters params;

  /** Reliable class name, for exemple use by ssl. */
  protected String reliableClass = null;

  /**
   * Constructs an <code>XAConnectionFactory</code> dedicated to a given
   * server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public XAConnectionFactory(String host, int port) {
    params = new FactoryParameters(host, port);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of the connection factory. */
  public String toString() {
    return "XACF:" + params.getHost() + "-" + params.getPort();
  }

  public void setReliableClass(String reliableClass) {
    this.reliableClass = reliableClass;
  }

  /**
   * API method, implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.XAConnection
                  createXAConnection(String name, String password)
                  throws JMSException;

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection() throws JMSException {
    return createXAConnection(ConnectionFactory.getDefaultLogin(),
                              ConnectionFactory.getDefaultPassword());
  }

  /**
   * Returns the factory's configuration parameters.
   */
  public FactoryParameters getParameters() {
    return params;
  } 

  /** Sets the naming reference of an XA connection factory. */
  public Reference getReference() throws NamingException {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("cFactory.host", params.getHost()));
    ref.add(new StringRefAddr("cFactory.port",
                              (new Integer(params.getPort())).toString()));
    ref.add(
      new StringRefAddr("cFactory.cnxT",
                        (new Integer(params.connectingTimer)).toString()));
    ref.add(new StringRefAddr("reliableClass",reliableClass));
    return ref;
  }

  /**
   * Codes an <code>XAConnectionFactory</code> as a Hashtable for travelling
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    h.put("host",params.getHost());
    h.put("port",new Integer(params.getPort()));
    h.put("connectingTimer",new Integer(params.connectingTimer));
    return h;
  }

 
  /**
   * Implements the <code>decode</code> abstract method defined in the
   * <code>fr.dyade.aaa.jndi2.soap.SoapObjectItf</code> interface.
   * <p>
   * Actual implementation of the method is located in the 
   * tcp and soap sub classes.
   */ 
  public void decode(Hashtable h) {
    params = new FactoryParameters((String)h.get("host"),
                                   ((Integer)h.get("port")).intValue());
    params.connectingTimer = ((Integer)h.get("connectingTimer")).intValue();
    params.cnxPendingTimer = ((Integer)h.get("cnxPendingTimer")).intValue();
  }
}
