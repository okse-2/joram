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

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public abstract class ConnectionFactory implements javax.jms.ConnectionFactory,
                                                   javax.naming.Referenceable,
                                                   java.io.Serializable
{
  /**
   * Class table holding the <code>ConnectionFactory</code> instances, needed
   * by the naming service.
   * <p>
   * <b>Key:</b> cf's hashcode<br>
   * <b>Object:</b> cf's instance
   */
  protected static Hashtable instancesTable = new Hashtable();

  /** Address of the server clients will connect to. */
  protected InetAddress serverAddr;  
  /** Port on which the server is listening. */
  protected int port; 
  /** Url for connecting to the server. */
  protected JoramUrl serverUrl;

  /** Timer in seconds allowed to connections for connecting. */
  protected int cnxTimer = 60;
  /** Timer in seconds allowed to transactions for finishing. */
  protected int txTimer = 0;

 
  /**
   * Constructs a <code>ConnectionFactory</code> instance wrapping a given
   * agent server url.
   *
   * @param url  Url of the agent server.
   * @exception ConnectException  If the url is incorrect.
   */
  public ConnectionFactory(String url) throws ConnectException
  {
    try {
      serverUrl = new JoramUrl(url);
      serverAddr = InetAddress.getByName(serverUrl.getHost());
    }
    catch (MalformedURLException mE) {
      throw new ConnectException("Incorrect server url: " + url);
    }
    catch (UnknownHostException uE) {
      throw new ConnectException("Unknown host in server url: " + url);
    }
    port = serverUrl.getPort();

    // Registering the instance in the table:
    instancesTable.put(url, this);

    
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created."); 
  }

  /**
   * Sets the connecting timer.
   *
   * @param timer  Time in seconds for connecting.
   */
  public void setCnxTimer(int timer)
  {
    if (timer >= 0)
      this.cnxTimer = timer;
  } 

  /**
   * Sets the transaction timer.
   *
   * @param timer  Maximum time in seconds for a transaction to finish.
   */
  public void setTxTimer(int timer)
  {
    if (timer >= 0)
      this.txTimer = timer;
  }

  /** Sets the naming reference of this connection factory. */
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "fr.dyade.aaa.joram.ObjectFactory",
                                  null);
    ref.add(new StringRefAddr("cFactory.url", serverUrl.toString()));
    ref.add(new StringRefAddr("cFactory.cnxT",
                              (new Integer(cnxTimer)).toString()));
    ref.add(new StringRefAddr("cFactory.txT",
                              (new Integer(txTimer)).toString()));
    return ref;
  }

  /** Returns this connection factory to the name service. */
  public static Object getInstance(String url)
  {
    return instancesTable.get(url);
  }
}
