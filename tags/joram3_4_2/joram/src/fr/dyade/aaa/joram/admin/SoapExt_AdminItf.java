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
package fr.dyade.aaa.joram.admin;

import java.net.ConnectException;

/**
 * The <code>SoapExt_AdminItf</code> interface defines an additional
 * set of methods needed for administering a platform which will accept SOAP 
 * connections.
 */
public interface SoapExt_AdminItf extends AdminItf
{
  /**
   * Creates a SOAP user for a given server and instanciates the corresponding
   * <code>User</code> object.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public User createSoapUser(String name, String password, int serverId)
              throws ConnectException, AdminException;

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating SOAP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.ConnectionFactory
         createSoapConnectionFactory(String host, int port, int timeout);

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating SOAP connections with the local server.
   *
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.ConnectionFactory createSoapConnectionFactory(int timeout);

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating SOAP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueSoapConnectionFactory(String host, int port, int timeout);

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating SOAP connections with the local server.
   *
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueSoapConnectionFactory(int timeout);

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating SOAP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicSoapConnectionFactory(String host, int port, int timeout);

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating SOAP connections with the local server.
   *
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicSoapConnectionFactory(int timeout);
}