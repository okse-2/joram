/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (Bull SA), Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.TopicConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.shared.admin.*;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.jms.*;


/**
 * The <code>AdminModule</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 */
public class AdminModule
{
  /** The identifier of the server the module is connected to. */
  private static int localServer;
  /** The host name or IP address this client is connected to. */
  protected static String localHost;
  /** The port number of the client connection. */
  protected static int localPort;

  /** The connection used to link the administrator and the platform. */
  private static TopicConnection cnx = null;
  /** The session in which the administrator works. */
  private static TopicSession sess;
  /** The admin topic to send admin requests to. */
  private static javax.jms.Topic topic;
  /** The requestor for sending the synchronous requests. */
  private static TopicRequestor requestor;

  /** ObjectMessage sent to the platform. */
  private static ObjectMessage requestMsg;
  /** ObjectMessage received from the platform. */
  private static ObjectMessage replyMsg;

  /** Reply object received from the platform. */
  protected static AdminReply reply;
  

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(javax.jms.TopicConnectionFactory cnxFact, 
                             String name,
                             String password)
         throws ConnectException, AdminException
  {
    if (cnx != null)
      return;
    
    try {
      cnx = cnxFact.createTopicConnection(name, password);
      sess = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      
      topic = sess.createTopic("#AdminTopic");
      
      requestor = new TopicRequestor(sess, topic);
      
      cnx.start();
     
      org.objectweb.joram.client.jms.FactoryParameters params = null;

      if (cnxFact instanceof javax.jms.XATopicConnectionFactory) 
        params = ((org.objectweb.joram.client.jms.XAConnectionFactory)
                  cnxFact).getParameters();
      else
        params = ((org.objectweb.joram.client.jms.ConnectionFactory)
                  cnxFact).getParameters();

      localHost = params.getHost();
      localPort = params.getPort();
      
      // Getting the id of the local server:
      try {
        String topicName = topic.getTopicName();
        int ind0 = topicName.indexOf(".");
        int ind1 = topicName.indexOf(".", ind0 + 1);
        localServer = Integer.parseInt(topicName.substring(ind0 + 1, ind1));
      }
      catch (JMSException exc) {}
    }
    catch (JMSSecurityException exc) {
      throw new AdminException(exc.getMessage());
    }
    catch (JMSException exc) {
      throw new ConnectException("Connecting failed: " + exc);
    }
  }

  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(String hostName,
                             int port,
                             String name,
                             String password,
                             int cnxTimer)
         throws UnknownHostException, ConnectException, AdminException
  {
    javax.jms.TopicConnectionFactory cnxFact =
      TopicTcpConnectionFactory.create(hostName, port);
    
    ((org.objectweb.joram.client.jms.ConnectionFactory)
     cnxFact).getParameters().connectingTimer = cnxTimer;
    
    connect(cnxFact, name, password);
  }
  
  /**
   * Opens a TCP connection with the Joram server running on the default
   * "locahost" host and listening to the default 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception UnknownHostException  Never thrown.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(String name, String password, int cnxTimer)
         throws UnknownHostException, ConnectException, AdminException
  {
    connect("localhost", 16010, name, password, cnxTimer);
  }

  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void collocatedConnect(String name, String password)
         throws ConnectException, AdminException
  {
    connect(TopicLocalConnectionFactory.create(), name, password);
  }

  /** Closes the administration connection. */
  public static void disconnect()
  {
    try {
      if (cnx == null)
        return;

      cnx.close();
    }
    catch (JMSException exc) {}

    cnx = null;
  }


  
  /**
   * Stops a given server of the platform.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  Identifier of the server to stop.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void stopServer(int serverId)
         throws ConnectException, AdminException
  {
    try {
      doRequest(new StopServerRequest(serverId));

      if (serverId == localServer)
        cnx = null;
    }
    // ConnectException is intercepted if stopped server is local server.
    catch (ConnectException exc) {
      if (serverId != localServer)
        throw exc;

      cnx = null;
    }
  }

  /**
   * Stops the platform local server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void stopServer() throws ConnectException, AdminException
  {
    stopServer(localServer);
  }

  /**
   * Sets a given dead message queue as the default DMQ for a given server
   * (<code>null</code> for unsetting previous DMQ).
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void setDefaultDMQ(int serverId, DeadMQueue dmq)
                throws ConnectException, AdminException
  {
    doRequest(new SetDefaultDMQ(serverId, dmq.getName()));
  }

  /**
   * Sets a given dead message queue as the default DMQ for the local server
   * (<code>null</code> for unsetting previous DMQ).
   *
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void setDefaultDMQ(DeadMQueue dmq)
                throws ConnectException, AdminException
  {
    setDefaultDMQ(localServer, dmq);
  }

  /**
   * Sets a given value as the default threshold for a given server (-1 for
   * unsetting previous value).
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void setDefaultThreshold(int serverId, int threshold)
                throws ConnectException, AdminException
  {
    doRequest(new SetDefaultThreshold(serverId, threshold));
  }

  /**
   * Sets a given value as the default threshold for the local server (-1 for
   * unsetting previous value).
   *
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void setDefaultThreshold(int threshold)
                throws ConnectException, AdminException
  {
    setDefaultThreshold(localServer, threshold);
  }

  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static List getServersIds() throws ConnectException, AdminException
  {
    Monitor_GetServersIds request =
      new Monitor_GetServersIds(AdminModule.getLocalServer());
    Monitor_GetServersIdsRep reply =
      (Monitor_GetServersIdsRep) doRequest(request);
    return reply.getIds();
  }

  /** 
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static DeadMQueue getDefaultDMQ(int serverId)
                throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getDMQName() == null)
      return null;
    else
      return new DeadMQueue(reply.getDMQName());
  }

  /** 
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static DeadMQueue getDefaultDMQ()
                throws ConnectException, AdminException
  {
    return getDefaultDMQ(localServer);
  }
    
  /**
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static int getDefaultThreshold(int serverId)
                throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
  }

  /**
   * Returns the default threshold value for the local server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static int getDefaultThreshold()
                throws ConnectException, AdminException
  {
    return getDefaultThreshold(localServer);
  }

  /**
   * Returns the list of all destinations that exist on a given server,
   * or an empty list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static List getDestinations(int serverId)
                throws ConnectException, AdminException
  {
    Monitor_GetDestinations request = new Monitor_GetDestinations(serverId);
    Monitor_GetDestinationsRep reply =
      (Monitor_GetDestinationsRep) doRequest(request);

    Vector dests;
    Vector list = new Vector();

    // Adding the queues, if any:
    dests = reply.getQueues();
    String id;
    String name;
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++) {
        id = (String) dests.get(i);
        name = reply.getName(id);
        list.add(new org.objectweb.joram.client.jms.Queue(id, name));
      }
    }
    // Adding the dead messages queues, if any:
    dests = reply.getDeadMQueues();
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++) {
        id = (String) dests.get(i);
        name = reply.getName(id);
        list.add(new DeadMQueue(id, name));
      }
    }
    // Adding the topics, if any:
    dests = reply.getTopics();
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++) {
        id = (String) dests.get(i);
        name = reply.getName(id);
        list.add(new org.objectweb.joram.client.jms.Topic(id, name));
      }
    }
    return list;
  }

  /**
   * Returns the list of all destinations that exist on the local server,
   * or an empty list if none exist.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  Never thrown.
   */
  public static List getDestinations() throws ConnectException, AdminException
  {
    return getDestinations(localServer);
  }

  /**
   * Returns the list of all users that exist on a given server, or an empty
   * list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static List getUsers(int serverId)
                throws ConnectException, AdminException
  {
    Monitor_GetUsers request = new Monitor_GetUsers(serverId);
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    String name;
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      list.add(new User(name, (String) users.get(name)));
    }
    return list;
  }

  /**
   * Returns the list of all users that exist on the local server, or an empty
   * list if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static List getUsers() throws ConnectException, AdminException
  {
    return getUsers(localServer);
  }


  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static int getLocalServer() throws ConnectException
  {
    if (cnx == null)
      throw new ConnectException("Administrator not connected.");

    return localServer;
  }

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static String getLocalHost() throws ConnectException
  {
    if (cnx == null)
      throw new ConnectException("Administrator not connected.");

    return localHost;
  }

  /**
   * Returns the port number of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static int getLocalPort() throws ConnectException
  {
    if (cnx == null)
      throw new ConnectException("Administrator not connected.");

    return localPort;
  }
 
  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */  
  public static AdminReply doRequest(AdminRequest request)
         throws AdminException, ConnectException
  {
    if (cnx == null)
      throw new ConnectException("Admin connection not established.");

    try {
      requestMsg = sess.createObjectMessage(request);
      replyMsg = (ObjectMessage) requestor.request(requestMsg);
      reply = (AdminReply) replyMsg.getObject();

      if (! reply.succeeded())
        throw new AdminException(reply.getInfo());

      return reply;
    }
    catch (JMSException exc) {
      throw new ConnectException("Connection failed: " + exc.getMessage());
    }
    catch (ClassCastException exc) {
      throw new AdminException("Invalid server reply: " + exc.getMessage());
    }
  }
} 
