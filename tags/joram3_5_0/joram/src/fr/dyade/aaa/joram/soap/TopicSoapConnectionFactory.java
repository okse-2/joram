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
 * Contributor(s):
 */
package fr.dyade.aaa.joram.soap;

import fr.dyade.aaa.joram.Connection;
import fr.dyade.aaa.joram.FactoryParameters;
import fr.dyade.aaa.joram.TopicConnection;

import java.util.Vector;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;


/**
 * A <code>TopicSoapConnectionFactory</code> instance is a factory of
 * SOAP connections for Pub/Sub communication.
 */
public class TopicSoapConnectionFactory
             extends fr.dyade.aaa.joram.TopicConnectionFactory
{
  /**
   * Constructs a <code>TopicSoapConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */
  public TopicSoapConnectionFactory(String host, int port, int timeout)
  {
    super(host, port);
    params.soapCnxPendingTimer = timeout;
  }

  /**
   * Constructs an empty <code>TopicSoapConnectionFactory</code> instance.
   */
  public TopicSoapConnectionFactory()
  {}


  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection
         createTopicConnection(String name, String password)
         throws javax.jms.JMSException
  {
    return new TopicConnection(params,
                               new SoapConnection(params, name, password));
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password)
                              throws javax.jms.JMSException
  {
    return new Connection(params, new SoapConnection(params, name, password));
  }

  /**
   * Method inherited from the <code>SoapConnectionFactory</code> class;
   * overrides this <code>ConnectionFactory</code> method for constructing
   * the appropriate reference.
   */
  public Reference getReference() throws NamingException
  {
    Reference ref =
      new Reference(this.getClass().getName(),
                    "fr.dyade.aaa.joram.admin.SoapExt_ObjectFactory",
                    null);
    ref.add(new StringRefAddr("adminObj.id", id));
    ref.add(new StringRefAddr("cFactory.host", params.getHost()));
    ref.add(new StringRefAddr("cFactory.port",
                              (new Integer(params.getPort())).toString()));
    ref.add(
      new StringRefAddr("cFactory.cnxT",
                        (new Integer(params.connectingTimer)).toString()));
    ref.add(
      new StringRefAddr("cFactory.txT",
                        (new Integer(params.txPendingTimer)).toString()));
    ref.add(new StringRefAddr("cFactory.soapCnxT",
                              (new Integer(params.soapCnxPendingTimer))
                                .toString()));
    return ref;
  }
}
