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

import fr.dyade.aaa.mom.admin.*;

import java.net.*;
import java.util.Vector;


/**
 * Old administration class.
 *
 * @deprecated  This class is temporary kept but the methods of the new
 *              <code>AdminItf</code> interface should be used instead.
 */
public class Admin
{
  private AdminImpl adminImpl;
  private String adminName;
  private int serverId;
  private String host;
  private int port;
  private boolean disconnected = true;

  private javax.naming.Context jndiCtx = null;

  
  /**
   * Constructs an <code>Admin</code> instance connected to a given server
   * with a given administrator identification.
   *
   * @param hostName  Name of the host to connect to.
   * @param port  Port the server is listening on. 
   * @param adminName  Administrator name.
   * @param adminPass  Administrator password.
   * @param timer  Time is seconds allowed for connecting.
   * @exception ConnectException  In case of a wrong server url or if the
   *              server is not listening.
   * @exception AdminException  If the admin identification is incorrect.
   */
  public Admin(String hostName, int port, String adminName,
               String adminPass, int timer) throws Exception
  {
    try {
      adminImpl = new AdminImpl();
      adminImpl.connect(hostName, port, adminName, adminPass, timer);

      this.adminName = adminName;

      disconnected = false;

      jndiCtx = new javax.naming.InitialContext();

      serverId = adminImpl.getServerId();
    }
    catch (UnknownHostException exc) {
      throw new ConnectException("Unknown host: " + exc);
    }
    catch (javax.naming.NamingException exc) {
      throw new AdminException("Can't access any naming server");
    }
  }
 
  /**
   * Constructs an <code>Admin</code> instance connected to the local server
   * listening on the default port 16010 with a given administrator
   * identification.
   *
   * @param adminName  Administrator name.
   * @param adminPass  Administrator password.
   * @param timer  Time in seconds allowed for re-connecting.
   * @exception UnknownHostException  If the local host name can't be
   *              retrieved.
   * @exception ConnectException  If the server is not listening, or if failing
   *              to build its default url.
   * @exception AdminException  If the admin identification is incorrect.
   */
  public Admin(String adminName, String adminPass, int timer) throws Exception
  {
    this(InetAddress.getLocalHost().getHostName(), 16010, adminName,
         adminPass, timer);
  }


  /**
   * Adds an admin identification for connecting to the admin proxy.
   *
   * @param name  Administrator name.
   * @param pass  Administrator password.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the admin id already exists.
   */
  public void addAdminId(String name, String pass) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.doRequest(new OldAddAdminId(serverId, name, pass));
  }

  /**
   * Removes an admin identification.
   *
   * @param name  Administrator name.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or
   *              if the admin name does not exist, or if trying to remove
   *              the currently used identification.
   */
  public void delAdminId(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    if (name.equals(adminName))
      throw new AdminException("Can't remove the currently used "
                               + name + " identification.");

    adminImpl.doRequest(new OldDelAdminId(serverId, name));
  }

  /**
   * Creates and deploys a queue agent and instanciates the corresponding
   * JMS <code>Queue</code> object.
   *
   * @param name  Name of the queue to create.
   * @return  A Joram queue.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or
   *              if the queue could not be deployed, or if its name is
   *              already taken by a destination on this server.
   */
  public javax.jms.Queue createQueue(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    String mName = serverId + ":oldAdmin:" + name;

    try {
      jndiCtx.lookup(mName);
      throw new AdminException("Name [" + name + "] is already taken");
    }
    catch (javax.naming.NamingException exc) {
      javax.jms.Queue queue = adminImpl.createQueue(serverId);
      jndiCtx.bind(mName, queue);
      return queue;
    }
  }

  /**
   * Retrieves a queue agent and instanciates the corresponding
   * JMS <code>Queue</code> object.
   *
   * @param name  Name of the queue to retrieve.
   * @return  A Joram queue.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or
   *              if the queue does not exist.
   */
  public javax.jms.Queue getQueue(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    try {
      String mName = serverId + ":oldAdmin:" + name;

      Object obj = jndiCtx.lookup(mName);

      if (! (obj instanceof javax.jms.Queue))
        throw new AdminException("Queue [" + name + "] does not exist");

      return (javax.jms.Queue) obj;
    }
    catch (javax.naming.NamingException exc) {
      throw new AdminException("Queue [" + name + "] does not exist");
    }
  }

  /**
   * Creates and deploys a topic agent and instanciates the corresponding
   * JMS <code>Topic</code> object.
   *
   * @param name  Name of the topic to create.
   * @return  A Joram topic.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or
   *              if the topic could not be deployed, or if its name is
   *              already taken by a destination on this server.
   */
  public javax.jms.Topic createTopic(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    String mName = serverId + ":oldAdmin:" + name;

    try {
      jndiCtx.lookup(mName);
      throw new AdminException("Name [" + name + "] is already taken");
    }
    catch (javax.naming.NamingException exc) {
      javax.jms.Topic topic = adminImpl.createTopic(serverId);
      jndiCtx.bind(mName, topic);
      return topic;
    }
  }

  /**
   * Retrieves a topic agent and instanciates the corresponding
   * JMS <code>Topic</code> object.
   *
   * @param name  Name of the topic to retrieve.
   * @return  A Joram topic.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or
   *              if the topic does not exist.
   */
  public javax.jms.Topic getTopic(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    try {
      String mName = serverId + ":oldAdmin:" + name;

      Object obj = jndiCtx.lookup(mName);

      if (! (obj instanceof javax.jms.Topic))
        throw new AdminException("Topic [" + name + "] does not exist");

      return (javax.jms.Topic) obj;
    }
    catch (javax.naming.NamingException exc) {
      throw new AdminException("Topic [" + name + "] does not exist");
    }
  }

  
  /**
   * Creates and deploys a dead message queue agent and instanciates the
   * corresponding <code>DeadMQueue</code> object.
   *
   * @param name  Name of the queue to create.
   * @return  A Joram DeadMQueue.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or
   *              if the queue could not be deployed, or if its name is
   *              already taken by a destination on this server.
   */
  public DeadMQueue createDeadMQueue(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    String mName = serverId + ":oldAdmin:" + name;

    try {
      jndiCtx.lookup(mName);
      throw new AdminException("Name [" + name + "] is already taken");
    }
    catch (javax.naming.NamingException exc) {
      DeadMQueue queue = adminImpl.createDeadMQueue(serverId);
      jndiCtx.bind(mName, queue);
      return queue;
    }
  }

  /**
   * Sets a <code>DeadMQueue</code> instance as the default DMQ for the
   * destinations and the users hosted by the administered server.
   *
   * @param dmq  The DeadMQueue instance.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void setDefaultDMQ(DeadMQueue dmq) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.setDefaultDMQ(serverId, dmq);
  }

  /**
   * Sets a <code>DeadMQueue</code> instance as the DMQ for a given
   * destination.
   *
   * @param name  The name of the destination to attribute the DMQ to.
   * @param dmq  The DeadMQueue instance.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if the
   *              destination is not administered by this administrator.
   */
  public void setDestinationDMQ(String name, DeadMQueue dmq) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination dest = null;

    try {
      dest = getQueue(name);
    }
    catch (AdminException exc) {
      dest = getTopic(name);
    }

    adminImpl.setDestinationDMQ(dest, dmq);
  }

  /**
   * Unsets the default DMQ for the destinations and the users hosted by the
   * administered server.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void unsetDefaultDMQ() throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.unsetDefaultDMQ(serverId);
  }

  /**
   * Unsets the DMQ of a given destination.
   *
   * @param name  The name of the destination which DMQ must be unset.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if the
   *              destination is not administered by this administrator.
   */
  public void unsetDestinationDMQ(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination dest = null;

    try {
      dest = getQueue(name);
    }
    catch (AdminException exc) {
      dest = getTopic(name);
    }

    adminImpl.unsetDestinationDMQ(dest);
  }

  /**
   * Sets a threshold value of authorized delivery attempts before sending
   * a message to the DMQ, at the server's level.
   *
   * @param threshold  The number of authorized delivery attempts.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void setDefaultThreshold(int threshold) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.setDefaultThreshold(serverId, threshold);
  }

  /**
   * Sets a threshold value of authorized delivery attempts before sending
   * a message to the DMQ, for a given queue.
   *
   * @param queue  The queue.
   * @param threshold  The number of authorized delivery attempts.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the queue is not administered by this administrator.
   */
  public void setQueueThreshold(javax.jms.Queue queue, int threshold)
              throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.setQueueThreshold(queue, threshold);
  }

  /**
   * Unsets the default threshold value of the administered server.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void unsetDefaultThreshold() throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.unsetDefaultThreshold(serverId);
  }

  /**
   * Unsets the threshold value of a given queue.
   *
   * @param queue  The queue.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the queue is not administered by this administrator.
   */
  public void unsetQueueThreshold(javax.jms.Queue queue) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    adminImpl.unsetQueueThreshold(queue);
  }

  /**
   * Creates a <code>ConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.ConnectionFactory createConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Creates a <code>QueueConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.QueueConnectionFactory createQueueConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createQueueConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Creates a <code>TopicConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.TopicConnectionFactory createTopicConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createTopicConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Creates an <code>XAConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.XAConnectionFactory createXAConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createXAConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Creates an <code>XAQueueConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.XAQueueConnectionFactory createXAQueueConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createXAQueueConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Creates an <code>XATopicConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public javax.jms.XATopicConnectionFactory createXATopicConnectionFactory()
         throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return adminImpl.createXATopicConnectionFactory(host, port);
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
  }

  /**
   * Sets a given topic as the subtopic of an administered one.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if
   *              there is a problem with the administered topic.
   */
  public void setSubTopic(String topicName, javax.jms.Topic subTopic)
              throws Exception
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");

    javax.jms.Topic topic = getTopic(topicName);
    adminImpl.setFather(topic, subTopic);
  }

  /**
   * Clusters topics together.
   * 
   * @param cluster  <code>Cluster</code> instance describing the cluster.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if 
   *              the cluster name is already taken.
   */
  public void createCluster(Cluster cluster) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    if (cluster.locked)
      return;

    String initId = (String) cluster.topics.get(0);
    String joiningId;
    for (int i = 1; i < cluster.topics.size(); i++) {
      joiningId = (String) cluster.topics.get(i);
      adminImpl.doRequest(new SetCluster(initId, joiningId));
    }
    cluster.locked = true;

    try {
      jndiCtx.bind(serverId + ":oldAdmin:" + cluster.id, cluster);
    }
    catch (javax.naming.NamingException exc) {}
  }

  /**
   * Unclusters a given cluster.
   *
   * @param name  Name of cluster to destroy.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if the
   *              cluster does not exist.
   */
  public void destroyCluster(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    try {
      String mName = serverId + ":oldAdmin:" + name;
      Object obj = jndiCtx.lookup(mName);

      if (obj == null || ! (obj instanceof Cluster))
        throw new AdminException("Cluster [" + name + "] does not exist");

      Cluster cluster = (Cluster) obj;

      String id;
      for (int i = 0; i < cluster.topics.size(); i++) {
        id = (String) cluster.topics.get(i);
        adminImpl.doRequest(new UnsetCluster(id));
      }
      
      jndiCtx.unbind(mName);
    }
    catch (javax.naming.NamingException exc) {}
  }

  /**
   * Deletes a destination.
   *
   * @param name  Name of the destination to delete.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if
   *              the admin does not "know" this destination.
   */
  public void deleteDestination(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination dest = null;

    try {
      dest = getQueue(name);
    }
    catch (AdminException exc) {
      dest = getTopic(name);
    }

    adminImpl.deleteDestination(dest);

    jndiCtx.unbind(serverId + ":oldAdmin:" + name);
  }

  /**
   * Creates and deploys a JMS proxy agent for a given user identification.
   *
   * @param name  User name.
   * @param pass User pasword.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if
   *              the user name is already taken.
   */
  public User createUser(String name, String pass) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");
   
    String mName = serverId + ":oldAdmin:" + name;

    try {
      jndiCtx.lookup(mName);

      throw new AdminException("Name [" + name + "] is already taken");
    }
    catch (javax.naming.NamingException exc) {
      User user = adminImpl.createUser(name, pass, serverId);
      user.adminImpl = adminImpl;
      jndiCtx.bind(mName, user);
      return user;
    }
  }

  /**
   * Retrieves a user references.
   *
   * @param name  User name.
   * @return  The corresponding <code>User</code> instance.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed or if
   *              the user does not exist.
   */
  public User getUser(String name) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");
    
    try {
      String mName = serverId + ":oldAdmin:" + name;

      Object obj = jndiCtx.lookup(mName);

      if (obj == null || ! (obj instanceof User))
        throw new AdminException("User [" + name + "] does not exist");

      ((User) obj).adminImpl = adminImpl;
      return (User) obj;
    }
    catch (javax.naming.NamingException exc) {
      return null;
    }
  }

  /**
   * Sets a user as a READER on a destination.
   *
   * @param user  The <code>User</code> instance.
   * @param dest  Name of the destination.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void setReader(User user, String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.setReader(user, jDest);
  }

  /**
   * Unsets a user as a READER on a destination.
   *
   * @param user  The <code>User</code> instance.
   * @param dest  Name of the destination.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void unsetReader(User user, String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.unsetReader(user, jDest);
  }

  /**
   * Sets all users as READERS on a destination.
   *
   * @param dest  Name of the destination.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void setFreeReading(String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.setFreeReading(jDest);
  }

  /**
   * Unsets all users as READERS on a destination.
   *
   * @param dest  Name of the destination.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void unsetFreeReading(String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.unsetFreeReading(jDest);
  }

  /**
   * Sets a user as a WRITER on a destination.
   *
   * @param user  User, or <code>null</code> for all users.
   * @param dest  Name of the destination.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void setWriter(User user, String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.setWriter(user, jDest);
  }

  /**
   * Unsets a user as a WRITER on a destination.
   *
   * @param user  User, or <code>null</code> for all users.
   * @param dest  Name of the destination.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void unsetWriter(User user, String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.unsetWriter(user, jDest);
  }

  /**
   * Sets all users as WRITERS on a destination.
   *
   * @param dest  Name of the destination.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void setFreeWriting(String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.setFreeWriting(jDest);
  }

  /**
   * Unsets all users as WRITERS on a destination.
   *
   * @param dest  Name of the destination.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed, or if
   *              the destination is not administered by this administrator.
   */
  public void unsetFreeWriting(String dest) throws Exception
  {
    if (disconnected)
      throw new AdminException("Admin session has been closed.");

    javax.jms.Destination jDest = null;

    try {
      jDest = getQueue(dest);
    }
    catch (AdminException exc) {
      jDest = getTopic(dest);
    }

    adminImpl.unsetFreeWriting(jDest);
  }
 
  /** Closes the connection with the server. */
  public void close()
  {
    if (disconnected)
      return;

    adminImpl.disconnect();
  }

 
  /** Extracts the identifier of a server from a name. */ 
  private int getServer(String name)
  {
    try {
      int ind0 = name.indexOf(".");
      int ind1 = name.indexOf(".", ind0);

      return Integer.parseInt(name.substring(ind0 + 1, ind1));
    }
    catch (Exception exc) {
      return -1;
    }
  }
}
