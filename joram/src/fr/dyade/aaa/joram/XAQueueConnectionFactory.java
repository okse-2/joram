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

import java.net.UnknownHostException;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.XAQueueConnectionFactory</code> interface.
 */
public class XAQueueConnectionFactory
             extends QueueConnectionFactory
             implements javax.jms.XAQueueConnectionFactory
{
  /**
   * Constructs an <code>XAQueueConnectionFactory</code> instance wrapping a
   * given server's parameters.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is unknown.
   */
  public XAQueueConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    super(host, port);
  }

  /**
   * Constructs an <code>XAQueueConnectionFactory</code> instance wrapping a
   * given server's url.
   *
   * @param url  The server's url.
   *
   * @exception MalformedURLException  If the url is incorrect.
   * @exception UnknownHostException  If the host is unknown.
   */
  public XAQueueConnectionFactory(String url) throws Exception
  {
    super(url);
  }

  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "XAQCF:" + config.serverAddr.toString();
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAQueueConnection
         createXAQueueConnection(String name, String password)
         throws JMSException
  {
    return new XAQueueConnection(config, name, password);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection()
         throws JMSException
  {
    return createXAQueueConnection("anonymous", "anonymous");
  }

  /**
   * Method inherited from interface <code>XAConnectionFactory</code>.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection
         createXAConnection(String name, String password) throws JMSException
  {
    return new XAConnection(config, name, password);
  }

  /**
   * Method inherited from interface <code>XAConnectionFactory</code>.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection() throws JMSException
  {
    return createXAConnection("anonymous", "anonymous");
  }
}
