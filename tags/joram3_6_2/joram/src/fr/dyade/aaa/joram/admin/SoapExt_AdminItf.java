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
