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

import fr.dyade.aaa.joram.Queue;
import fr.dyade.aaa.joram.Topic;
import fr.dyade.aaa.joram.ConnectionFactory;
import fr.dyade.aaa.joram.QueueConnectionFactory;
import fr.dyade.aaa.joram.TopicConnectionFactory;
import fr.dyade.aaa.joram.XAConnectionFactory;
import fr.dyade.aaa.joram.XAQueueConnectionFactory;
import fr.dyade.aaa.joram.XATopicConnectionFactory;
import fr.dyade.aaa.mom.admin.*;

import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.jms.*;

/**
 * This class is the reference implementation of the <code>AdminItf</code> 
 * interface.
 */
public class AdminImpl implements AdminItf
{
  /** The host name or IP address this client is connected to. */
  private String localHost;
  /** The port number of the client connection. */
  private int localPort;
  /** The identifier of the server the client is connected to. */
  private int localServer;

  /** The connection used to link the administrator and the platform. */
  private TopicConnection cnx = null;
  /** The session in which the administrator works. */
  private TopicSession sess;
  /** The admin topic to send admin requests to. */
  private javax.jms.Topic topic;
  /** The producer for sending requests to the admin topic. */
  private MessageProducer producer;
  /** The requestor for sending the synchronous requests. */
  private TopicRequestor requestor;

  /** ObjectMessage sent to the platform. */
  private ObjectMessage requestMsg;
  /** ObjectMessage received from the platform. */
  private ObjectMessage replyMsg;
  /** Reply object received from the platform. */
  private AdminReply reply;
  
  /**
   * Opens a connection with the Joram server running on a given host and
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
  public void connect(String hostName, int port, String name,
                      String password, int cnxTimer)
              throws UnknownHostException, ConnectException, AdminException
  {
    if (cnx != null)
      return;

    try {
      javax.jms.TopicConnectionFactory cnxFact =
        new TopicConnectionFactory(hostName, port);

      ((fr.dyade.aaa.joram.ConnectionFactory) cnxFact).setCnxTimer(cnxTimer);

      cnx = cnxFact.createTopicConnection(name, password);
      sess = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      topic = sess.createTopic("#AdminTopic");

      producer = sess.createProducer(topic);
      requestor = new TopicRequestor(sess, topic);

      cnx.start();

      localHost = hostName;
      localPort = port;

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
   * Opens a connection with the Joram server running on the default
   * "locahost" host and listening to the default 16010 port.
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
    try {
      this.connect("localhost", 16010, name, password, cnxTimer);
    }
    catch (UnknownHostException exc) {}
  }

  /** Closes the administration connection. */
  public void disconnect()
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
   * Creates and deploys a queue destination on a given server, instanciates
   * the corresponding <code>javax.jms.Queue</code> object.
   * <p>
   * The method never returns if the server does not belong to the platform.
   * It fails if the destination deployement fails server side.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Queue createQueue(int serverId)
                         throws ConnectException, AdminException
  {
    reply = doRequest(new CreateQueueRequest(serverId));

    if (reply instanceof CreateDestinationReply)
      return new Queue(((CreateDestinationReply) reply).getDestId());
    else
      throw new AdminException(reply.getInfo());
  }

  /**
   * Creates and deploys a queue destination on the local server, instanciates
   * the corresponding <code>javax.jms.Queue</code> object.
   * <p>
   * The method fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Queue createQueue() throws ConnectException, AdminException
  {
    return this.createQueue(localServer);
  }

  /**
   * Creates and deploys a topic destination on a given server, intanciates
   * the corresponding <code>javax.jms.Topic</code> object.
   * <p>
   * The method never returns if the server does not belong to the platform.
   * It fails if the destination deployement fails server side.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic createTopic(int serverId)
                         throws ConnectException, AdminException
  {
    reply = doRequest(new CreateTopicRequest(serverId));

    if (reply instanceof CreateDestinationReply)
      return new Topic(((CreateDestinationReply) reply).getDestId());
    else
      throw new AdminException(reply.getInfo());
  }

  /**
   * Creates and deploys a topic destination on the local server, intanciates
   * the corresponding <code>javax.jms.Topic</code> object.
   * <p>
   * The method fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic createTopic() throws ConnectException, AdminException
  {
    return  this.createTopic(localServer);
  }

  /**
   * Creates and deploys a dead message queue on a given server, instanciates
   * the corresponding <code>DeadMQueue</code> object.
   * <p>
   * The method never returns if the server does not belong to the platform.
   * It fails if the destination deployement fails server side.
   *
   * @param serverId  The identifier of the server where deploying the dmq.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue createDeadMQueue(int serverId)
                    throws ConnectException, AdminException
  {
    reply = doRequest(new CreateDMQRequest(serverId));

    if (reply instanceof CreateDestinationReply)
      return new DeadMQueue(((CreateDestinationReply) reply).getDestId());
    else
      throw new AdminException(reply.getInfo());
  }

  /**
   * Creates and deploys a dead message queue on the local server, instanciates
   * the corresponding <code>DeadMQueue</code> object.
   * <p>
   * The method fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue createDeadMQueue() throws ConnectException, AdminException
  {
    return this.createDeadMQueue(localServer);
  }

  /**
   * Removes a given destination from the platform.
   * <p>
   * The request is not effective if the destination has already been deleted.
   *
   * @param dest  The destination to remove.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void deleteDestination(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new DeleteDestination(getDestinationName(dest)));
  }

  /**
   * Adds a topic to a cluster.
   * <p>
   * The request fails one or both of the topics are deleted, or
   * can't belong to a cluster.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetCluster(getDestinationName(clusterTopic),
                             getDestinationName(joiningTopic)));
  }

  /**
   * Removes a topic from the cluster it is part of.
   * <p>
   * The request is not effective if the topic is deleted or not part
   * of any cluster.
   *
   * @param topic  Topic leaving the cluster it is part of.
   * 
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void leaveCluster(javax.jms.Topic topic)
              throws ConnectException, AdminException
  {
    doSend(new UnsetCluster(getDestinationName(topic)));
  }

  /**
   * Sets a given topic as the father of an other topic.
   * <p>
   * The request fails if one or both of the topics are deleted,
   * or can't belong to a hierarchy.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetFather(getDestinationName(father),
                            getDestinationName(son)));
  }

  /**
   * Unsets the father of a given topic.
   * <p>
   * The request is not effective if the topic is deleted or not part of any
   * hierarchy.
   *
   * @param topic  Topic which father is unset.
   *
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetFather(javax.jms.Topic topic)
              throws ConnectException, AdminException
  {
    doSend(new UnsetFather(getDestinationName(topic)));
  }

  /**
   * Creates a user for a given server and instanciates the corresponding
   * <code>User</code> object.
   * <p>
   * The method never returns if the server does not belong to the platform.
   * If the user has already been set on this server, it simply returns
   * the corresponding <code>User</code> object. It fails if a proxy could not
   * be deployed server side for a new user. 
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public User createUser(String name, String password, int serverId)
              throws ConnectException, AdminException
  {
    reply = doRequest(new CreateUserRequest(name, password, serverId));

    if (reply instanceof CreateUserReply)
      return new User(name, ((CreateUserReply) reply).getProxId());
    else
      throw new AdminException(reply.getInfo());
  }

  /**
   * Creates a user on the local server and instanciates the corresponding
   * <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. It fails if a
   * proxy could not be deployed server side for a new user. 
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public User createUser(String name, String password)
              throws ConnectException, AdminException
  {
    return this.createUser(name, password, localServer);
  }

  /**
   * Updates a given user identification.
   * <p>
   * The request fails if the user does not exist server side, or if the new
   * identification is already taken by a user on the same server.
   *
   * @param user  The user to update.
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void updateUser(User user, String newName, String newPassword)
              throws ConnectException, AdminException
  {
    reply = doRequest(new UpdateUser(user.name, user.proxyId,
                                     newName, newPassword));
    user.name = newName;
  }

  /**
   * Removes a given user.
   * <p>
   * The request is not effective if the user is already deleted.
   *
   * @param user  The user to remove.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void deleteUser(User user) throws ConnectException, AdminException
  {
    doSend(new DeleteUser(user.name, user.proxyId));
  }

  /**
   * Sets free reading access on a given destination.
   * <p>
   * The request is not effective if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new SetReader(null, getDestinationName(dest)));
  }

  /**
   * Sets free writing access on a given destination.
   * <p>
   * The request is not effective if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new SetWriter(null, getDestinationName(dest)));
  }

  /**
   * Unsets free reading access on a given destination.
   * <p>
   * The request is not effective if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new UnsetReader(null, getDestinationName(dest)));
  }

  /**
   * Unsets free writing access on a given destination.
   * <p>
   * The request is not effective if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new UnsetWriter(null, getDestinationName(dest)));
  }

  /**
   * Sets a given user as a reader on a given destination.
   * <p>
   * The request is not effective if the user or the destination is deleted.
   *
   * @param user  User to be set as a reader.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setReader(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new SetReader(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Sets a given user as a writer on a given destination.
   * <p>
   * The request is not effective if the user or the destination is deleted.
   *
   * @param user  User to be set as a writer.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setWriter(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new SetWriter(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Unsets a given user as a reader on a given destination.
   * <p>
   * The request is not effective if the user or the destination is deleted.
   *
   * @param user  Reader to be unset.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetReader(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new UnsetReader(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Unsets a given user as a writer on a given destination.
   * <p>
   * The request is not effective if the user or the destination is deleted.
   *
   * @param user  Writer to be unset.
   * @param dest  Destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetWriter(User user, javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new UnsetWriter(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Sets a given dead message queue as the default DMQ for a given server.
   * <p>
   * The request is not effective if the server is unknown in the platform,
   * or if the dmq is deleted.
   *
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDefaultDMQ(int serverId, DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    doSend(new SetDefaultDMQ(serverId, getDestinationName(dmq)));
  }

  /**
   * Sets a given dead message queue as the default DMQ for the local server.
   * <p>
   * The request is not effective if the dmq is deleted.
   *
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDefaultDMQ(DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    this.setDefaultDMQ(localServer, dmq);
  }

  /**
   * Sets a given dead message queue as the DMQ for a given destination.
   * <p>
   * The request is not effective if the destination or the DMQ is deleted.
   *
   * @param dest  The destination.
   * @param dmq  The dead message queue to be set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDestinationDMQ(javax.jms.Destination dest, DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    doSend(new SetDestinationDMQ(getDestinationName(dest),
                                 getDestinationName(dmq)));
  }

  /**
   * Sets a given dead message queue as the DMQ for a given user.
   * <p>
   * The request is not effective if the user or the DMQ is deleted.
   *
   * @param user  The user.
   * @param dmq  The dead message queue to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setUserDMQ(User user, DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    doSend(new SetUserDMQ(user.proxyId, getDestinationName(dmq)));
  } 

  /**
   * Unsets the default dead message queue of a given server.
   * <p>
   * The request is not effective if the server is unknown in the platform.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDefaultDMQ(int serverId)
              throws ConnectException, AdminException
  {
    doSend(new UnsetDefaultDMQ(serverId));
  }

  /**
   * Unsets the default dead message queue of the local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDefaultDMQ() throws ConnectException, AdminException
  {
    this.unsetDefaultDMQ(localServer);
  }

  /**
   * Unsets the dead message queue of a given destination.
   * <p>
   * The request is not effective if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDestinationDMQ(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doSend(new UnsetDestinationDMQ(getDestinationName(dest)));
  }

  /**
   * Unsets the dead message queue of a given user.
   *  <p>
   * The request is not effective if the user is deleted.
   *
   * @param user  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetUserDMQ(User user) throws ConnectException, AdminException
  {
    doSend(new UnsetUserDMQ(user.proxyId));
  }

  /**
   * Sets a given value as the default threshold for a given server.
   * <p>
   * The request is not effective if the server is unknown in the platform.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDefaultThreshold(int serverId, int threshold)
              throws ConnectException, AdminException
  {
    doSend(new SetDefaultThreshold(serverId, threshold));
  }

  /**
   * Sets a given value as the default threshold for the local server.
   *
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDefaultThreshold(int threshold)
              throws ConnectException, AdminException
  {
    this.setDefaultThreshold(localServer, threshold);
  }

  /**
   * Sets a given value as the threshold for a given queue.
   * <p>
   * The request is not effective if the queue is deleted.
   *
   * @param queue  The queue.
   * @param threshold  The threshold value to be set.
   *
   * @exception IllegalArgumentException  If the queue is not a valid 
   *              JORAM queue.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setQueueThreshold(javax.jms.Queue queue, int threshold)
              throws ConnectException, AdminException
  {
    doSend(new SetQueueThreshold(getDestinationName(queue), threshold));
  } 

  /**
   * Sets a given value as the threshold for a given user.
   * <p>
   * The request is not effective if the user is deleted.
   *
   * @param userName  The user.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setUserThreshold(User user, int threshold)
              throws ConnectException, AdminException
  {
    doSend(new SetUserThreshold(user.proxyId, threshold));
  } 

  /**
   * Unsets the default threshold of a given server.
   * <p>
   * The request is not effective if the server is unknown in the platform.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDefaultThreshold(int serverId)
              throws ConnectException, AdminException
  {
    doSend(new UnsetDefaultThreshold(serverId));
  }

  /**
   * Unsets the default threshold of the local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDefaultThreshold() throws ConnectException, AdminException
  {
    this.unsetDefaultThreshold(localServer);
  }

  /**
   * Unsets the threshold of a given queue.
   * <p>
   * The request is not effective if the queue is deleted.
   *
   * @param queue  The queue.
   *
   * @exception IllegalArgumentException  If the queue is not a valid 
   *              JORAM queue.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetQueueThreshold(javax.jms.Queue queue)
              throws ConnectException, AdminException
  {
    doSend(new UnsetQueueThreshold(getDestinationName(queue)));
  }

  /**
   * Unsets the threshold of a given user.
   * <p>
   * The request is not effective if the user is deleted.
   *
   * @param userName  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetUserThreshold(User user)
              throws ConnectException, AdminException
  {
    doSend(new UnsetUserThreshold(user.proxyId));
  } 

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.ConnectionFactory
         createConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new ConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.ConnectionFactory createConnectionFactory()
         throws UnknownHostException
  {
    return this.createConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new QueueConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.QueueConnectionFactory createQueueConnectionFactory()
         throws UnknownHostException
  {
    return this.createQueueConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new TopicConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.TopicConnectionFactory createTopicConnectionFactory()
         throws UnknownHostException
  {
    return this.createTopicConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.XAConnectionFactory
         createXAConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new XAConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.XAConnectionFactory createXAConnectionFactory()
         throws UnknownHostException
  {
    return this.createXAConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.XAQueueConnectionFactory
         createXAQueueConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new XAQueueConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.XAQueueConnectionFactory createXAQueueConnectionFactory()
         throws UnknownHostException
  {
    return this.createXAQueueConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * connecting to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   *
   * @exception UnknownHostException  If the host is incorrect.
   */ 
  public javax.jms.XATopicConnectionFactory
         createXATopicConnectionFactory(String host, int port)
         throws UnknownHostException
  {
    return new XATopicConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * connecting to the local server.
   *
   * @exception UnknownHostException  In case of a problem with localhost.
   */ 
  public javax.jms.XATopicConnectionFactory createXATopicConnectionFactory()
         throws UnknownHostException
  {
    return this.createXATopicConnectionFactory(localHost, localPort);
  }


  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */  
  AdminReply doRequest(AdminRequest request)
                     throws AdminException, ConnectException
  {
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

  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform.
   *
   * @exception ConnectException  If the connection to the platform fails.
   */  
  void doSend(AdminRequest request) throws ConnectException
  {
    try {
      requestMsg = sess.createObjectMessage(request);
      producer.send(requestMsg);
    }
    catch (JMSException exc) {
      throw new ConnectException("Connection failed: " + exc.getMessage());
    }
  }

  /** Temporary method kept for maintaining the old Admin class. */
  int getServerId()
  {
    return localServer;
  }

 
  /**
   * Returns the name of a given JORAM destination.
   *
   * @exception IllegalArgumentException  If the destination is null or not
   *               a JORAM destination.
   */ 
  private String getDestinationName(javax.jms.Destination dest)
  {
    if (dest == null)
      throw new IllegalArgumentException("Invalid null destination");

    String className = dest.getClass().getName();
    if (! className.equals("fr.dyade.aaa.joram.Queue")
        && ! className.equals("fr.dyade.aaa.joram.Topic")
        && ! className.equals("fr.dyade.aaa.joram.admin.DeadMQueue")) {
      throw new IllegalArgumentException("Destination is not a JORAM"
                                         + " destination: " + className);
    }

    try {
      if (dest instanceof javax.jms.Queue)
        return ((javax.jms.Queue) dest).getQueueName();
      return ((javax.jms.Topic) dest).getTopicName();
    }
    catch (JMSException exc) {
      return null;
    }
  }
} 
