/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;

/**
 * Implements the <code>javax.jms.QueueConnection</code> interface.
 */
public class QueueConnection extends Connection
                             implements javax.jms.QueueConnection
{
  /**
   * Constructs a <code>QueueConnection</code> instance and opens a TCP
   * connection with a given agent server.
   *
   * @param qcf  The factory this connection is created by.
   * @param serverAddr  Address of the server to connect to.
   * @param port  Port the server is listening to.
   * @param name  User's name.
   * @param password  User's password.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  QueueConnection(QueueConnectionFactory qcf, java.net.InetAddress serverAddr,
                  int port, String name, String password) throws JMSException
  {
    super(qcf, serverAddr, port, name, password);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.QueueSession
       createQueueSession(boolean transacted, int acknowledgeMode)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new QueueSession(nextSessionId(), this, transacted,
                            acknowledgeMode);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target queue does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the queue.
   * @exception JMSException  If one of the parameters is wrong.
   */
  public javax.jms.ConnectionConsumer
       createConnectionConsumer(javax.jms.Queue queue, String selector,
                                javax.jms.ServerSessionPool sessionPool,
                                int maxMessages) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new QueueConnectionConsumer(this, ((Queue) queue).getName(),
                                       selector, sessionPool, maxMessages);
  }
}