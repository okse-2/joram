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
 * The <code>AdminItf</code> interface defines the set of methods for
 * administering Joram server(s).
 * <p>
 * This class is a prototype written in the context of Joram's
 * administration refactoring.
 */
public interface AdminItf
{
  /**
   * Connects an administration session with the Joram server running on
   * a given host and listening to a given port.
   *
   * @param hostName  The name or IP address of the host the server is running
   *          on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @exception ConnectException  If the connection to the server fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String hostName, int port, String name,
                      String password, int cnxTimer)
              throws ConnectException, AdminException;

  /**
   * Connects an administration session with the Joram server running on
   * the default "locahost" host and listening to the default 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @exception ConnectException  If the connection to the server fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String name, String password, int cnxTimer)
              throws ConnectException, AdminException;

  /** Disconnects an administration session from the Joram server. */
  public void close();


  /**
   * Changes the administrator's password.
   *
   * @param newName  The administrator's new name.
   * @param newPassword  The administrator's new password.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void updateAdminId(String newName, String newPassword)
              throws ConnectException;

  /**
   * Creates and deploys a queue destination on a given server.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @return  A <code>javax.jms.Queue</code> instance wrapping the physical
   *          queue name.
   * @exception ConnectException  If the connection with the server failed.
   */
  public javax.jms.Queue createQueue(int serverId) throws ConnectException;
  /**
   * Creates and deploy a topic destination on a given server.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @return  A <code>javax.jms.Topic</code> instance wrapping the physical
   *          topic name.
   * @exception ConnectException  If the connection with the server failed.
   */
  public javax.jms.Topic createTopic(int serverId) throws ConnectException;
  /**
   * Creates and deploys a dead message queue on a given server.
   *
   * @param serverId  The identifier of the server where deploying the dmq.
   * @return  A <code>DeadMQueue</code> instance wrapping the physical
   *          dmq name.
   * @exception ConnectException  If the connection with the server failed.
   */
  public fr.dyade.aaa.joram.admin.DeadMQueue createDeadMQueue(int serverId)
         throws ConnectException;
  /**
   * Removes a destination from the server hosting it.
   *
   * @param destination  The <code>javax.jms.Destination</code> to remove.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void deleteDestination(javax.jms.Destination destination)
              throws ConnectException;

  /**
   * Sets a given dead message queue as the default DMQ for a given server.
   *
   * @param serverId  The identifier of the server.
   * @param dmq  The dead message queue to be set as the default one.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setDefaultDMQ(int serverId,
                            fr.dyade.aaa.joram.admin.DeadMQueue dmq)
              throws ConnectException;
  /**
   * Sets a given dead message queue as the DMQ for a given destination.
   *
   * @param destination  The destination.
   * @param dmq  The dead message queue to be set.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setDestinationDMQ(javax.jms.Destination destination,
                                fr.dyade.aaa.joram.admin.DeadMQueue dmq)
              throws ConnectException;
  /**
   * Unsets the default dead message queue of a given server.
   *
   * @param serverId  The identifier of the server.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetDefaultDMQ(int serverId) throws ConnectException;
  /**
   * Unsets the dead message queue of a given destination.
   *
   * @param destination  The destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetDestinationDMQ(javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Sets a given value as the default threshold for a given server.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setDefaultThreshold(int serverId, int threshold)
              throws ConnectException;
  /**
   * Sets a given value as the threshold for a given queue.
   *
   * @param queue  The queue.
   * @param threshold  The threshold value to be set.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setQueueThreshold(javax.jms.Queue queue, int threshold)
              throws ConnectException;
  /**
   * Unsets the default threshold of a given server.
   *
   * @param serverId  The identifier of the server.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetDefaultThreshold(int serverId) throws ConnectException;
  /**
   * Unsets the threshold of a given queue.
   *
   * @param threshold  The threshold value to be set.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetQueueThreshold(javax.jms.Queue queue)
              throws ConnectException;

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.ConnectionFactory
         createConnectionFactory(String hostName, int port);
  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueConnectionFactory(String hostName, int port);
  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicConnectionFactory(String hostName, int port);
  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.XAConnectionFactory
         createXAConnectionFactory(String hostName, int port);
  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.XAQueueConnectionFactory
         createXAQueueConnectionFactory(String hostName, int port);
  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * connecting to a given server listening to a given port.
   *
   * @param hostName  Name or IP address of the server to give access to.
   * @param port  Port number through which accessing the server.
   */ 
  public javax.jms.XATopicConnectionFactory
         createXATopicConnectionFactory(String hostName, int port);
 
  /**
   * Sets a user for a given server; if the user already exists, simply
   * instanciates a <code>fr.dyade.aaa.joram.admin.User</code> instance.
   *
   * @param serverId  The identifier of the user's server.
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @exception ConnectException  If the connection with the server failed.
   */ 
  public User setUser(int serverId, String name, String password)
         throws ConnectException;
  /**
   * Updates a user identification.
   *
   * @param user  The user to update.
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void updateUser(User user, String newName, String newPassword)
              throws ConnectException;
  /**
   * Removes a user.
   *
   * @param user  The user to remove.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void deleteUser(User user) throws ConnectException;

  /**
   * Sets free reading access on a given destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setFreeReading(javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Unsets free reading access on a given destination. 
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetFreeReading(javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Sets free writing access on a given destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setFreeWriting(javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Unsets free writing access on a given destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetFreeWriting(javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Sets a given user as a reader on a destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setReader(User user, javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Unsets a given user as a reader on a destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetReader(User user, javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Sets a given user as a writer on a destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void setWriter(User user, javax.jms.Destination destination)
              throws ConnectException;
  /**
   * Unsets a given user as a writer on a destination.
   * @exception ConnectException  If the connection with the server failed.
   */
  public void unsetWriter(User user, javax.jms.Destination destination)
              throws ConnectException;
}
