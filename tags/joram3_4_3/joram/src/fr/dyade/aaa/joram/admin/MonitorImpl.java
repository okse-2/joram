/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * Contributor(s): Alexander Fedorowicz  
 */
package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.mom.admin.*;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;


/**
 * This class is the reference implementation of the <code>MonitorItf</code> 
 * interface.
 */
public class MonitorImpl implements MonitorItf
{
  /** The monitor instance is in fact an AdminImpl instance. */
  private AdminImpl monitor;

  /**
   * Opens a connection dedicated to monitoring with the Joram server
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
  public void connect(javax.jms.TopicConnectionFactory cnxFact,
                      String name,
                      String password)
              throws ConnectException, AdminException
  {
    monitor = new AdminImpl();
    monitor.connect(cnxFact, name, password);
  }

  /**
   * Opens a connection dedicated to monitoring with the Joram server
   * running on a given host and listening to a given port.
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
  public void connect(String hostName, 
                      int port,
                      String name,
                      String password,
                      int cnxTimer)
              throws UnknownHostException, ConnectException, AdminException
  {
    monitor = new AdminImpl();
    monitor.connect(hostName, port, name, password, cnxTimer);
  }

  /**
   * Opens a connection dedicated to monitoring with the Joram server
   * running on the default "locahost" host and listening to the default
   * 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String name, String password, int cnxTimer)
              throws ConnectException, AdminException
  {
    monitor = new AdminImpl();
    monitor.connect(name, password, cnxTimer);
  }

  /** Closes the monitoring connection. */
  public void disconnect()
  {
    monitor.disconnect();
    monitor = null;
  }

  /**
   * Returns the list of all <code>javax.jms.Destination</code> that
   * exist on a given server, or an empty list if none exist.
   * <p>
   * The request never returns if the server is not part of the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getDestinations(int serverId)
         throws ConnectException, AdminException
  {
    Monitor_GetDestinations request = new Monitor_GetDestinations(serverId);
    Monitor_GetDestinationsRep reply =
      (Monitor_GetDestinationsRep) doRequest(request);

    Vector dests;
    Vector list = new Vector();

    // Adding the queues, if any:
    dests = reply.getQueues();
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++)
        list.add(new fr.dyade.aaa.joram.Queue((String) dests.get(i)));
    }
    // Adding the dead messages queues, if any:
    dests = reply.getDeadMQueues();
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++)
        list.add(new DeadMQueue((String) dests.get(i)));
    }
    // Adding the topics, if any:
    dests = reply.getTopics();
    if (dests != null) {
      for (int i = 0; i < dests.size(); i++)
        list.add(new fr.dyade.aaa.joram.Topic((String) dests.get(i)));
    }
    return list;
  }

  /**
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * exist on a given server, or an empty list if none exist.
   * <p>
   * The request never returns if the server is not part of the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getUsers(int serverId) throws ConnectException, AdminException
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
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * have a reading permission on a given destination, or an empty list if no
   * specific readers are set.
   * <p>
   * The request fails if the destination does not exist server side.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getReaders(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetReaders request =
      new Monitor_GetReaders(monitor.getDestinationName(dest));
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
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * have a writing permission on a given destination, or an empty list if no
   * specific writers are set.
   * <p>
   * The request fails if the destination does not exist server side.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getWriters(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetWriters request;
    request = new Monitor_GetWriters(monitor.getDestinationName(dest));
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
   * Returns <code>true</code> if a given destination provides free READ
   * access.
   * <p>
   * The request fails if the destination does not exist server side.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public boolean freelyReadable(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetFreeAccess request;
    request = new Monitor_GetFreeAccess(monitor.getDestinationName(dest));
    Monitor_GetFreeAccessRep reply;
    reply = (Monitor_GetFreeAccessRep) doRequest(request);

    return reply.getFreeReading();
  }

  /**
   * Returns <code>true</code> if a given destination provides free WRITE
   * access.
   * <p>
   * The request fails if the destination does not exist server side.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public boolean freelyWriteable(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetFreeAccess request;
    request = new Monitor_GetFreeAccess(monitor.getDestinationName(dest));
    Monitor_GetFreeAccessRep reply;
    reply = (Monitor_GetFreeAccessRep) doRequest(request);

    return reply.getFreeWriting();
  }

  /** 
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The method never returns if the server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public DeadMQueue getDefaultDMQ(int serverId)
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
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The method never returns if the server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getDefaultThreshold(int serverId)
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
   * Returns the dead message queue of a given destination, null if not
   * set.
   * <p>
   * The request fails if the destination does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(monitor.getDestinationName(dest));
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getDMQName() == null)
      return null;
    else
      return new DeadMQueue(reply.getDMQName());
  }

  /** 
   * Returns the threshold of a given queue, -1 if not set.
   * <p>
   * The request fails if the destination does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold(javax.jms.Queue dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(monitor.getDestinationName(dest));
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
  }

  /** 
   * Returns the dead message queue of a given user, null if not
   * set.
   * <p>
   * The request fails if the user does not exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ(User user)
         throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(user.getProxyId());
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getDMQName() == null)
      return null;
    else
      return new DeadMQueue(reply.getDMQName());
  }

  /** 
   * Returns the threshold of a given user, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold(User user)
         throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(user.getProxyId());
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
  }

  /**
   * Returns the hierarchical father of a given topic, null if none.
   * <p>
   * The request fails if the topic does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic getHierarchicalFather(javax.jms.Topic dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetFather request;
    request = new Monitor_GetFather(monitor.getDestinationName(dest));
    Monitor_GetFatherRep reply = (Monitor_GetFatherRep) doRequest(request);

    if (reply.getFatherId() == null)
      return null;
    else
      return new fr.dyade.aaa.joram.Topic(reply.getFatherId());
  }

  /**
   * Returns the list describing the cluster a given topic is part of.
   * <p>
   * The request fails if the topic does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getCluster(javax.jms.Topic dest)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetCluster request;
    request = new Monitor_GetCluster(monitor.getDestinationName(dest));
    Monitor_GetClusterRep reply = (Monitor_GetClusterRep) doRequest(request);

    Vector topics = reply.getTopics();
    Vector list = new Vector();
    for (int i = 0; i < topics.size(); i++)
      list.add(new fr.dyade.aaa.joram.Topic((String) topics.get(i)));
    return list;
  }

  /**
   * Returns the number of pending messages on a given queue.
   * <p>
   * The request fails if the queue does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getPendingMessages(javax.jms.Queue queue)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetPendingMessages request =
      new Monitor_GetPendingMessages(monitor.getDestinationName(queue));
    Monitor_GetNumberRep reply = (Monitor_GetNumberRep) doRequest(request);

    return reply.getNumber();
  }

  /**
   * Returns the number of pending requests on a given queue.
   * <p>
   * The request fails if the queue does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getPendingRequests(javax.jms.Queue queue)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetPendingRequests request =
      new Monitor_GetPendingRequests(monitor.getDestinationName(queue));
    Monitor_GetNumberRep reply = (Monitor_GetNumberRep) doRequest(request);

    return reply.getNumber();
  }

  /**
   * Returns the number of subscriptions on a given topic.
   * <p>
   * The request fails if the topic does not exist.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getSubscriptions(javax.jms.Topic topic)
         throws IllegalArgumentException, ConnectException, AdminException
  {
    Monitor_GetSubscriptions request;
    request = new Monitor_GetSubscriptions(monitor.getDestinationName(topic));
    Monitor_GetNumberRep reply = (Monitor_GetNumberRep) doRequest(request);

    return reply.getNumber();
  }


  /**
   * Method actually sending a <code>MonitoringRequest</code> instance to
   * the platform and getting a <code>MonitoringReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */  
  Monitor_Reply doRequest(Monitor_Request request)
  throws AdminException, ConnectException
  {
    try {
      return (Monitor_Reply) monitor.doRequest(request);
    }
    catch (ClassCastException exc) {
      throw new AdminException("Invalid server reply: " + exc.getMessage());
    }
  }
}
