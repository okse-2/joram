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

import java.net.*;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XAConnectionFactory</code> interface.
 */
public class XAConnectionFactory implements javax.jms.XAConnectionFactory,
                                            javax.naming.Referenceable,
                                            java.io.Serializable
{
  /**
   * Class table holding the <code>XAConnectionFactory</code> instances, needed
   * by the naming service.
   * <p>
   * <b>Key:</b> cf's hashcode<br>
   * <b>Object:</b> cf's instance
   */
  protected static Hashtable instancesTable = new Hashtable();

  /** Factory's configuration object. */
  protected FactoryConfiguration config;

  /**
   * Constructs a <code>XAConnectionFactory</code> instance wrapping a given
   * agent server url.
   *
   * @param url  Url of the agent server.
   * @exception ConnectException  If the url is incorrect.
   */
  public XAConnectionFactory(String url) throws ConnectException
  {
    config = new FactoryConfiguration();

    try {
      config.serverUrl = new JoramUrl(url);
      config.serverAddr = InetAddress.getByName(config.serverUrl.getHost());
    }
    catch (MalformedURLException mE) {
      throw new ConnectException("Incorrect server url: " + url);
    }
    catch (UnknownHostException uE) {
      throw new ConnectException("Unknown host in server url: " + url);
    }
    config.port = config.serverUrl.getPort();

    // Registering the instance in the table:
    instancesTable.put(this.getClass().getName() + "/" + url, this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created."); 
  }

  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "XACF:" + config.serverAddr.toString();
  }


  /**
   * API method.
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
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection() throws JMSException
  {
    return createXAConnection("anonymous", "anonymous");
  }

  /**
   * Sets the connecting timer.
   *
   * @param timer  Time in seconds for connecting.
   */
  public void setCnxTimer(int timer)
  {
    if (timer > 0)
      config.cnxTimer = timer;
  } 

  /** Sets the naming reference of this connection factory. */
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "fr.dyade.aaa.joram.ObjectFactory",
                                  null);
    ref.add(new StringRefAddr("cFactory.url", config.serverUrl.toString()));
    ref.add(new StringRefAddr("cFactory.cnxT",
                              (new Integer(config.cnxTimer)).toString()));
    return ref;
  }

  /** Returns this connection factory to the name service. */
  public static Object getInstance(String url)
  {
    return instancesTable.get(url);
  }
}
