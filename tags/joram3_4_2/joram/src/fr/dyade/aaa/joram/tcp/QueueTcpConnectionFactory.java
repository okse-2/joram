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
package fr.dyade.aaa.joram.tcp;

import fr.dyade.aaa.joram.Connection;
import fr.dyade.aaa.joram.QueueConnection;

import java.util.Vector;

import javax.naming.NamingException;


/**
 * A <code>QueueTcpConnectionFactory</code> instance is a factory of
 * TCP connections for PTP communication.
 */
public class QueueTcpConnectionFactory
             extends fr.dyade.aaa.joram.QueueConnectionFactory
{
  /**
   * Constructs a <code>QueueTcpConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public QueueTcpConnectionFactory(String host, int port)
  {
    super(host, port);
  }

  /**
   * Method inherited from the <code>QueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.QueueConnection
         createQueueConnection(String name, String password)
         throws javax.jms.JMSException
  {
    return new QueueConnection(params,
                               new TcpConnection(params, name, password));
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
    return new Connection(params, new TcpConnection(params, name, password));
  }

  /**
   * Codes a <code>QueueTcpConnectionFactory</code> as a vector for travelling
   * through the SOAP protocol.
   *
   * @exception NamingException  Never thrown.
   */
  public Vector code() throws NamingException
  {
    Vector vec = new Vector();
    vec.add("QueueTcpConnectionFactory");
    vec.add(params.getHost());
    vec.add("" + params.getPort());
    vec.add("" + params.connectingTimer);
    vec.add("" + params.txPendingTimer);
    return vec;
  }

  /**
   * Decodes a coded <code>QueueTcpConnectionFactory</code>.
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
      QueueTcpConnectionFactory cnxFact =
        new QueueTcpConnectionFactory(host, port);
      cnxFact.getParameters().connectingTimer = connectingTimer;
      cnxFact.getParameters().txPendingTimer = txPendingTimer;
      return cnxFact;
    }
    catch (Exception exc) {
      throw new NamingException("Vector " + vec.toString()
                                + " incorrectly codes"
                                + " a QueueTcpConnectionFactory.");
    }
  }
}
