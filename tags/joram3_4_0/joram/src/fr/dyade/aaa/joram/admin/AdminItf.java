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
import java.net.UnknownHostException;

/**
 * The <code>AdminItf</code> interface defines the set of methods needed
 * for administering a JORAM platform.
 */
public interface AdminItf
{
  /**
   * Opens a connection with the Joram server given a
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
  public void connect(fr.dyade.aaa.joram.TopicConnectionFactory cnxFact,
                      String name, String password)
              throws ConnectException, AdminException;

  /**
   * Opens a connection dedicated to administering with the Joram server
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
  public void connect(String hostName, int port, String name,
                      String password, int cnxTimer)
              throws UnknownHostException, ConnectException, AdminException;

  /**
   * Opens a connection dedicated to administering with the Joram server
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
              throws ConnectException, AdminException;

  /** Closes the administration connection. */
  public void disconnect();


  /**
   * Creates and deploys a queue destination on a given server, instanciates
   * the corresponding <code>javax.jms.Queue</code> object.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Queue createQueue(int serverId)
                         throws ConnectException, AdminException;

  /**
   * Creates and deploys a queue destination on the local server, instanciates
   * the corresponding <code>javax.jms.Queue</code> object.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Queue createQueue() throws ConnectException, AdminException;

  /**
   * Creates and deploys a topic destination on a given server, intanciates
   * the corresponding <code>javax.jms.Topic</code> object.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic createTopic(int serverId)
                         throws ConnectException, AdminException;

  /**
   * Creates and deploys a topic destination on the local server, intanciates
   * the corresponding <code>javax.jms.Topic</code> object.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic createTopic() throws ConnectException, AdminException;

  /**
   * Creates and deploys a dead message queue on a given server, instanciates
   * the corresponding <code>DeadMQueue</code> object.
   *
   * @param serverId  The identifier of the server where deploying the dmq.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue createDeadMQueue(int serverId)
                    throws ConnectException, AdminException;

  /**
   * Creates and deploys a dead message queue on the local server, instanciates
   * the corresponding <code>DeadMQueue</code> object.
   *
   * @param serverId  The identifier of the server where deploying the dmq.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue createDeadMQueue() throws ConnectException, AdminException;

  /**
   * Removes a given destination from the platform.
   *
   * @param dest  The destination to remove.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void deleteDestination(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Adds a topic to a cluster.
   *
   * @param clusterTopic  Topic part of the cluster, or chosen as the 
   *          initiator of the cluster.
   * @param joiningTopic  Topic joining the cluster.
   *
   * @exception IllegalArgumentException  If one of the topics is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setCluster(javax.jms.Topic clusterTopic,
                         javax.jms.Topic joiningTopic)
              throws ConnectException, AdminException;

  /**
   * Removes a topic from the cluster it is part of.
   *
   * @param topic  Topic leaving the cluster it is part of.
   * 
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void leaveCluster(javax.jms.Topic topic)
              throws ConnectException, AdminException;

  /**
   * Sets a given topic as the father of an other topic.
   *
   * @param father  Father.
   * @param son  Son.
   *
   * @exception IllegalArgumentException  If one of the topics is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setFather(javax.jms.Topic father, javax.jms.Topic son)
              throws ConnectException, AdminException;

  /**
   * Unsets the father of a given topic.
   *
   * @param topic  Topic which father is unset.
   *
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFather(javax.jms.Topic topic)
              throws ConnectException, AdminException;

  /**
   * Creates a user for a given server and instanciates the corresponding
   * <code>User</code> object.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public User createUser(String name, String password, int serverId)
              throws ConnectException, AdminException;

  /**
   * Creates a user for the local server and instanciates the corresponding
   * <code>User</code> object.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public User createUser(String name, String password)
              throws ConnectException, AdminException;

  /**
   * Updates a given user identification.
   *
   * @param user  The user to update.
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void updateUser(User user, String newName, String newPassword)
              throws ConnectException, AdminException;

  /**
   * Removes a given user.
   *
   * @param user  The user to remove.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void deleteUser(User user) throws ConnectException, AdminException;

  /**
   * Sets free reading access on a given destination.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Sets free writing access on a given destination.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Unsets free reading access on a given destination.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Unsets free writing access on a given destination.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Sets a given user as a reader on a given destination.
   *
   * @param user  User to be set as a reader.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setReader(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Sets a given user as a writer on a given destination.
   *
   * @param user  User to be set as a writer.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setWriter(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Unsets a given user as a reader on a given destination.
   *
   * @param user  Reader to be unset.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetReader(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Unsets a given user as a writer on a given destination.
   *
   * @param user  Writer to be unset.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetWriter(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for a given server.
   *
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQ(int serverId, DeadMQueue dmq)
              throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for the local server.
   *
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQ(DeadMQueue dmq)
              throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the DMQ for a given destination.
   *
   * @param dest  The destination.
   * @param dmq  The dead message queue to be set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDestinationDMQ(javax.jms.Destination dest, DeadMQueue dmq)
              throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the DMQ for a given user.
   *
   * @param user  The user.
   * @param dmq  The dead message queue to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setUserDMQ(User user, DeadMQueue dmq)
              throws ConnectException, AdminException;

  /**
   * Unsets the default dead message queue of a given server.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDefaultDMQ(int serverId)
              throws ConnectException, AdminException;

  /**
   * Unsets the default dead message queue of the local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDefaultDMQ() throws ConnectException, AdminException;

  /**
   * Unsets the dead message queue of a given destination.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDestinationDMQ(javax.jms.Destination dest)
              throws ConnectException, AdminException;

  /**
   * Unsets the dead message queue of a given user.
   *
   * @param user  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetUserDMQ(User user) throws ConnectException, AdminException;

  /**
   * Sets a given value as the default threshold for a given server.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultThreshold(int serverId, int threshold)
              throws ConnectException, AdminException;

  /**
   * Sets a given value as the default threshold for the local server.
   *
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultThreshold(int threshold)
              throws ConnectException, AdminException;

  /**
   * Sets a given value as the threshold for a given queue.
   *
   * @param queue  The queue.
   * @param threshold  The threshold value to be set.
   *
   * @exception IllegalArgumentException  If the queue is not a valid 
   *              JORAM queue.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setQueueThreshold(javax.jms.Queue queue, int threshold)
              throws ConnectException, AdminException;

  /**
   * Sets a given value as the threshold for a given user.
   *
   * @param userName  The user.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setUserThreshold(User user, int threshold)
              throws ConnectException, AdminException;

  /**
   * Unsets the default threshold of a given server.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDefaultThreshold(int serverId)
              throws ConnectException, AdminException;

  /**
   * Unsets the default threshold of the local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDefaultThreshold() throws ConnectException, AdminException;

  /**
   * Unsets the threshold of a given queue.
   *
   * @param queue  The queue.
   *
   * @exception IllegalArgumentException  If the queue is not a valid 
   *              JORAM queue.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetQueueThreshold(javax.jms.Queue queue)
              throws ConnectException, AdminException;

  /**
   * Unsets the threshold of a given user.
   *
   * @param userName  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetUserThreshold(User user)
              throws ConnectException, AdminException;

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.ConnectionFactory
         createConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.ConnectionFactory createConnectionFactory();

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.QueueConnectionFactory createQueueConnectionFactory();

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.TopicConnectionFactory createTopicConnectionFactory();

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XAConnectionFactory
         createXAConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XAConnectionFactory createXAConnectionFactory();

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XAQueueConnectionFactory
         createXAQueueConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XAQueueConnectionFactory createXAQueueConnectionFactory();

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XATopicConnectionFactory
         createXATopicConnectionFactory(String host, int port);

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XATopicConnectionFactory createXATopicConnectionFactory();
}
