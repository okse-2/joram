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
package fr.dyade.aaa.joram.soap;

import fr.dyade.aaa.joram.Connection;
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
   * overrides this <code>AdministeredObject</code> method for constructing
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

  /**
   * Codes a <code>TopicSoapConnectionFactory</code> as a vector for travelling
   * through the SOAP protocol.
   *
   * @exception NamingException  Never thrown.
   */
  public Vector code() throws NamingException
  {
    Vector vec = new Vector();
    vec.add("TopicSoapConnectionFactory");
    vec.add(params.getHost());
    vec.add("" + params.getPort());
    vec.add("" + params.connectingTimer);
    vec.add("" + params.txPendingTimer);
    vec.add("" + params.soapCnxPendingTimer);
    return vec;
  }

  /**
   * Decodes a coded <code>TopicSoapConnectionFactory</code>.
   *
   * @exception NamingException  If incorrectly coded.
   */
  public static fr.dyade.aaa.joram.admin.AdministeredObject decode(Vector vec)
                throws NamingException
  {
    try {
      String host = (String) vec.remove(0);
      int port = Integer.parseInt((String) vec.remove(0));
      int connectingTimer = Integer.parseInt((String) vec.remove(0));
      int txPendingTimer = Integer.parseInt((String) vec.remove(0));
      int timeout = Integer.parseInt((String) vec.remove(0));
      TopicSoapConnectionFactory cnxFact = 
        new TopicSoapConnectionFactory(host, port, timeout);
      cnxFact.getParameters().connectingTimer = connectingTimer;
      cnxFact.getParameters().txPendingTimer = txPendingTimer;
      return cnxFact;
    }
    catch (Exception exc) {
      throw new NamingException("Vector " + vec.toString()
                                + " incorrectly codes"
                                + " a TopicSoapConnectionFactory.");
    }
  }
}
