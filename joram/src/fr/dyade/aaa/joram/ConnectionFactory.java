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

import java.net.*;

import javax.jms.JMSException;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public class ConnectionFactory
             extends fr.dyade.aaa.joram.admin.AdministeredObject
             implements javax.jms.ConnectionFactory
{
  /** Object containing the factory's configuration parameters. */
  protected FactoryConfiguration config;


  /**
   * Constructs a <code>ConnectionFactory</code> instance wrapping a given
   * server's parameters.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is unknown.
   */
  public ConnectionFactory(String host, int port) throws UnknownHostException
  {
    super((new JoramUrl(host, port, null)).toString());

    config = new FactoryConfiguration();

    config.serverAddr = InetAddress.getByName(host);
    config.port = port;
    config.serverUrl = new JoramUrl(host, port, null);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a <code>ConnectionFactory</code> instance wrapping a given
   * server's url.
   *
   * @param url  The server's url.
   *
   * @exception MalformedURLException  If the url is incorrect.
   * @exception UnknownHostException  If the host is unknown.
   */
  public ConnectionFactory(String url) throws Exception
  {
    this((new JoramUrl(url)).getHost(), (new JoramUrl(url)).getPort());
  }

  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "CF:" + config.serverAddr.toString() + "-" + config.port;
  }


  /**
   * API method.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password)
         throws JMSException
  {
    return new Connection(config, name, password);
  }

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

  /**
   * Sets the connecting timer.
   *
   * @param timer  Time in seconds for connecting.
   */
  public void setCnxTimer(int timer)
  {
    if (timer >= 0)
      config.cnxTimer = timer;
  } 

  /**
   * Sets the transaction timer.
   *
   * @param timer  Maximum time in seconds for a transaction to finish.
   */
  public void setTxTimer(int timer)
  {
    if (timer >= 0)
      config.txTimer = timer;
  }

  /** Sets the naming reference of a connection factory. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("cFactory.url", config.serverUrl.toString()));
    ref.add(new StringRefAddr("cFactory.cnxT",
                              (new Integer(config.cnxTimer)).toString()));
    ref.add(new StringRefAddr("cFactory.txT",
                              (new Integer(config.txTimer)).toString()));
    return ref;
  }
}
