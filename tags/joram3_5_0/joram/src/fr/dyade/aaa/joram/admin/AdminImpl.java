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

import fr.dyade.aaa.joram.Queue;
import fr.dyade.aaa.joram.Topic;
import fr.dyade.aaa.joram.TopicConnectionFactory;
import fr.dyade.aaa.joram.tcp.TcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.QueueTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.TopicTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XATcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XAQueueTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XATopicTcpConnectionFactory;
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
  /** The identifier of the server the client is connected to. */
  int localServer;

  /** The connection used to link the administrator and the platform. */
  private TopicConnection cnx = null;
  /** The session in which the administrator works. */
  private TopicSession sess;
  /** The admin topic to send admin requests to. */
  private javax.jms.Topic topic;
  /** The requestor for sending the synchronous requests. */
  private TopicRequestor requestor;

  /** ObjectMessage sent to the platform. */
  private ObjectMessage requestMsg;
  /** ObjectMessage received from the platform. */
  private ObjectMessage replyMsg;

  /** Reply object received from the platform. */
  protected AdminReply reply;
  
  /** The host name or IP address this client is connected to. */
  protected String localHost;
  /** The port number of the client connection. */
  protected int localPort;


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
  public void connect(javax.jms.TopicConnectionFactory cnxFact, String name,
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

      fr.dyade.aaa.joram.FactoryParameters params = 
        ((fr.dyade.aaa.joram.ConnectionFactory) cnxFact).getParameters();
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
  public void connect(String hostName, int port, String name,
                      String password, int cnxTimer)
              throws UnknownHostException, ConnectException, AdminException
  {
    TopicConnectionFactory cnxFact =
      new TopicTcpConnectionFactory(hostName, port);

    ((fr.dyade.aaa.joram.ConnectionFactory) cnxFact).getParameters().
        connectingTimer = cnxTimer;

    this.connect(cnxFact, name, password);
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
   * Stops a given server of the platform.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  Identifier of the server to stop.
   *
   * @exception AdminException  If the request fails.
   */
  public void stopServer(int serverId) throws AdminException
  {
    try {
      doRequest(new StopServerRequest(serverId));
    }
    catch (Exception exc) {}
  }
 
  /**
   * Creates and deploys a queue destination on a given server, instanciates
   * the corresponding <code>javax.jms.Queue</code> object.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
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
    return new Queue(((CreateDestinationReply) reply).getDestId());
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
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
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
    return new Topic(((CreateDestinationReply) reply).getDestId());
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
    return this.createTopic(localServer);
  }

  /**
   * Creates and deploys a dead message queue on a given server, instanciates
   * the corresponding <code>DeadMQueue</code> object.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
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
    return new DeadMQueue(((CreateDestinationReply) reply).getDestId());
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
    doRequest(new DeleteDestination(getDestinationName(dest)));
  }

  /**
   * Adds a topic to a cluster.
   * <p>
   * The request fails if one or both of the topics are deleted, or
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
   * The request fails if the topic does not exist or is not part of any 
   * cluster.
   *
   * @param topic  Topic leaving the cluster it is part of.
   * 
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void leaveCluster(javax.jms.Topic topic)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetCluster(getDestinationName(topic)));
  }

  /**
   * Sets a given topic as the father of an other topic.
   * <p>
   * The request fails if one of the topics does not exist or can't be part
   * of a hierarchy.
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
   * The request fails if the topic does not exist or is not part of any
   * hierarchy.
   *
   * @param topic  Topic which father is unset.
   *
   * @exception IllegalArgumentException  If the topic is not a valid 
   *              JORAM topic.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFather(javax.jms.Topic topic)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetFather(getDestinationName(topic)));
  }

  /**
   * Creates a user for a given server and instanciates the corresponding
   * <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
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
    return new User(name, ((CreateUserReply) reply).getProxId());
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
   *
   * @param user  The user to remove.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void deleteUser(User user) throws ConnectException, AdminException
  {
    doRequest(new DeleteUser(user.name, user.proxyId));
  }

  /**
   * Sets free reading access on a given destination.
   * <p>
   * The request fails if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doRequest(new SetReader(null, getDestinationName(dest)));
  }

  /**
   * Sets free writing access on a given destination.
   * <p>
   * The request fails if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doRequest(new SetWriter(null, getDestinationName(dest)));
  }

  /**
   * Unsets free reading access on a given destination.
   * <p>
   * The request fails if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeReading(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetReader(null, getDestinationName(dest)));
  }

  /**
   * Unsets free writing access on a given destination.
   * <p>
   * The request fails if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeWriting(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetWriter(null, getDestinationName(dest)));
  }

  /**
   * Sets a given user as a reader on a given destination.
   * <p>
   * The request fails if the destination is deleted.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetReader(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Sets a given user as a writer on a given destination.
   * <p>
   * The request fails if the destination is deleted.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetWriter(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Unsets a given user as a reader on a given destination.
   * <p>
   * The request fails if the destination is deleted.
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
              throws ConnectException, AdminException
  {
    doRequest(new UnsetReader(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Unsets a given user as a writer on a given destination.
   * <p>
   * The request fails if the destination is deleted.
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
              throws ConnectException, AdminException
  {
    doRequest(new UnsetWriter(user.proxyId, getDestinationName(dest)));
  }

  /**
   * Sets a given dead message queue as the default DMQ for a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQ(int serverId, DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    doRequest(new SetDefaultDMQ(serverId, getDestinationName(dmq)));
  }

  /**
   * Sets a given dead message queue as the default DMQ for the local server.
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
   * The request fails if the target server does not belong to the platform.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetDestinationDMQ(getDestinationName(dest),
                                    getDestinationName(dmq)));
  }

  /**
   * Sets a given dead message queue as the DMQ for a given user.
   * <p>
   * The request fails if the user is deleted.
   *
   * @param user  The user.
   * @param dmq  The dead message queue to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setUserDMQ(User user, DeadMQueue dmq)
              throws ConnectException, AdminException
  {
    doRequest(new SetUserDMQ(user.proxyId, getDestinationName(dmq)));
  } 

  /**
   * Unsets the default dead message queue of a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDefaultDMQ(int serverId)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetDefaultDMQ(serverId));
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
   * The request fails if the destination is deleted.
   *
   * @param dest  The destination.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetDestinationDMQ(javax.jms.Destination dest)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetDestinationDMQ(getDestinationName(dest)));
  }

  /**
   * Unsets the dead message queue of a given user.
   *  <p>
   * The request fails if the user is deleted.
   *
   * @param user  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetUserDMQ(User user) throws ConnectException, AdminException
  {
    doRequest(new UnsetUserDMQ(user.proxyId));
  }

  /**
   * Sets a given value as the default threshold for a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultThreshold(int serverId, int threshold)
              throws ConnectException, AdminException
  {
    doRequest(new SetDefaultThreshold(serverId, threshold));
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
   * The request fails if the queue is deleted.
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
              throws ConnectException, AdminException
  {
    doRequest(new SetQueueThreshold(getDestinationName(queue), threshold));
  } 

  /**
   * Sets a given value as the threshold for a given user.
   * <p>
   * The request fails if the user is deleted.
   *
   * @param userName  The user.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setUserThreshold(User user, int threshold)
              throws ConnectException, AdminException
  {
    doRequest(new SetUserThreshold(user.proxyId, threshold));
  } 

  /**
   * Unsets the default threshold of a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void unsetDefaultThreshold(int serverId)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetDefaultThreshold(serverId));
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
   * The request fails if the queue is deleted.
   *
   * @param queue  The queue.
   *
   * @exception IllegalArgumentException  If the queue is not a valid 
   *              JORAM queue.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void unsetQueueThreshold(javax.jms.Queue queue)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetQueueThreshold(getDestinationName(queue)));
  }

  /**
   * Unsets the threshold of a given user.
   * <p>
   * The request fails if the user is deleted.
   *
   * @param userName  The user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails..
   */
  public void unsetUserThreshold(User user)
              throws ConnectException, AdminException
  {
    doRequest(new UnsetUserThreshold(user.proxyId));
  } 

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.ConnectionFactory
         createConnectionFactory(String host, int port)
  {
    return new TcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.ConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.ConnectionFactory createConnectionFactory()
  {
    return this.createConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.QueueConnectionFactory
         createQueueConnectionFactory(String host, int port)
  {
    return new QueueTcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.QueueConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.QueueConnectionFactory createQueueConnectionFactory()
  {
    return this.createQueueConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.TopicConnectionFactory
         createTopicConnectionFactory(String host, int port)
  {
    return new TopicTcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.TopicConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.TopicConnectionFactory createTopicConnectionFactory()
  {
    return this.createTopicConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XAConnectionFactory
         createXAConnectionFactory(String host, int port)
  {
    return new XATcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XAConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XAConnectionFactory createXAConnectionFactory()
  {
    return this.createXAConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XAQueueConnectionFactory
         createXAQueueConnectionFactory(String host, int port)
  {
    return new XAQueueTcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XAQueueConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XAQueueConnectionFactory createXAQueueConnectionFactory()
  {
    return this.createXAQueueConnectionFactory(localHost, localPort);
  }

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public javax.jms.XATopicConnectionFactory
         createXATopicConnectionFactory(String host, int port)
  {
    return new XATopicTcpConnectionFactory(host, port);
  }

  /**
   * Creates a <code>javax.jms.XATopicConnectionFactory</code> instance for
   * creating TCP connections with the local server.
   */ 
  public javax.jms.XATopicConnectionFactory createXATopicConnectionFactory()
  {
    return this.createXATopicConnectionFactory(localHost, localPort);
  }


  /**
   * Returns the name of a given JORAM destination.
   *
   * @exception IllegalArgumentException  If the destination is null or not
   *               a JORAM destination.
   */ 
  String getDestinationName(javax.jms.Destination dest)
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
  
  /** Temporary method kept for maintaining the old Admin class. */
  int getServerId()
  {
    return localServer;
  }
} 
