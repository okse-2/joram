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
package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.joram.*;
import fr.dyade.aaa.mom.jms.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>Admin</code> instance is used by an administrator for performing
 * administrative tasks on a server through the server's
 * <code>JmsAdminProxy</code> agent.
 * <p>
 * An <code>Admin</code> instance is connected to a specific proxy agent
 * and holds a given administrator identification. If this identification is
 * incorrect, the connection to the server is refused.
 * <p>
 * Each server administering is done through its local
 * <code>JmsAdminProxy</code> agent. That means that it is not possible from a
 * proxy located on a server to administer agents located on an other server.
 * <p>
 * An <code>Admin</code> instance communicates with its
 * <code>JmsAdminProxy</code> proxy through a request
 * (<code>JmsAdminRequest</code> notifications) / reply
 * (<code>JmsAdminReply</code> notification) mechanism.
 */
public class Admin
{
  /** Administrator name. */
  private String adminName;
  /** Administrator password. */
  private String adminPass;

  /** IP address of the server to administer. */
  private InetAddress serverAddr;
  /** Listening port of the server. */
  private int serverPort;
  /** JoramUrl address of the server. */
  private JoramUrl serverUrl;
  /** Time in seconds allowed for connecting to the server. */
  private int timer;
 
  /** Connection socket. */ 
  private Socket socket = null;
  /** Connection data output stream. */
  private DataOutputStream dos = null;
  /** Connection data input stream. */
  private DataInputStream dis = null;
  /** Connection object output stream. */
  private ObjectOutputStream oos = null;
  /** Connection object input stream. */
  private ObjectInputStream ois = null;
  /** <code>true</code> if the admin is disconnected. */
  private boolean disconnected;

  /** Requests counter. */
  private long requestsCounter = 0; 

  
  /**
   * Constructs an <code>Admin</code> instance connected to a given server
   * with a given administrator identification.
   *
   * @param url  The server's url.
   * @param adminName  Administrator name.
   * @param adminPass  Administrator password.
   * @param timer  Time is seconds allowed for connecting.
   * @exception ConnectException  In case of a wrong server url or if the
   *              server is not listening.
   * @exception AdminException  If the admin identification is incorrect.
   */
  public Admin(String url, String adminName, String adminPass,
               int timer) throws Exception
  {
    if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "--- Connecting admin"
                                + " session: " + this);
    try {
      // Setting the server url:
      serverUrl = new JoramUrl(url);
      serverAddr = InetAddress.getByName(serverUrl.getHost());
      serverPort = serverUrl.getPort();
    }
    catch (MalformedURLException mE) {
      ConnectException cE = new ConnectException("Can't open admin as"
                                                 + " server's url is incorrect"
                                                 + ": " + url);
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgAdmin.log(BasicLevel.ERROR, "Exception in " + this
                                  + ": " + cE);
      throw cE;
    }
    catch (UnknownHostException uE) {
      ConnectException cE = new ConnectException("Can't open admin as the host"
                                                 + " is incorrect in server's"
                                                 + " url: " + url);
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgAdmin.log(BasicLevel.ERROR, "Exception in " + this
                                  + ": " + cE);
      throw cE;
    }

    this.adminName = adminName;
    this.adminPass = adminPass;
    if (timer >= 0)
      this.timer = timer;
    else
      this.timer = 60;

    try {
      connect();

      disconnected = false;

      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, this + ": connected.");
    }
    catch (Exception e) {
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgAdmin.log(BasicLevel.ERROR, e);
     
      throw e;
    }
  }

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
    this("joram://" + hostName + ":" + port, adminName, adminPass, timer);
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

  /** Returns a String image of this <code>Admin</code> instance. */
  public String toString()
  {
    return "Admin:serverAddr:" + adminName;
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
    AddAdminId addAd = new AddAdminId(name, pass);
    sendRequest(addAd);
    getReply();
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
    if (name.equals(adminName))
      throw new AdminException("Can't remove the currently used "
                               + name + " identification.");

    DelAdminId delAd = new DelAdminId(name);
    sendRequest(delAd);
    getReply();
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
  public Queue createQueue(String name) throws Exception
  {
    CreateQueue createQ = new CreateQueue(name);
    sendRequest(createQ);
    return new Queue(getReply());
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
  public Queue getQueue(String name) throws Exception
  {
    GetQueue getQ = new GetQueue(name);
    sendRequest(getQ);
    return new Queue(getReply());
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
  public Topic createTopic(String name) throws Exception
  {
    CreateTopic createT = new CreateTopic(name);
    sendRequest(createT);
    return new Topic(getReply());
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
  public Topic getTopic(String name) throws Exception
  {
    GetTopic getT = new GetTopic(name);
    sendRequest(getT);
    return new Topic(getReply());
  }

  /**
   * Creates a <code>QueueConnectionFactory</code> instance.
   *
   * @exception AdminException  If the admin session has been closed.
   */
  public QueueConnectionFactory createQueueConnectionFactory()
       throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return new QueueConnectionFactory(serverUrl.toString());
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
  public TopicConnectionFactory createTopicConnectionFactory()
       throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return new TopicConnectionFactory(serverUrl.toString());
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
  public XAQueueConnectionFactory createXAQueueConnectionFactory()
       throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return new XAQueueConnectionFactory(serverUrl.toString());
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
  public XATopicConnectionFactory createXATopicConnectionFactory()
       throws AdminException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as the admin has"
                               + " disconnected.");
    try {
      return new XATopicConnectionFactory(serverUrl.toString());
    }
    catch (Exception e) {
      // Can't happen as the url parameter has already been checked by
      // this Admin instance.
      return null;
    }
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
    cluster.locked = true;
    CreateCluster createC = new CreateCluster(cluster.getName(),
                                              cluster.getTopics());
    sendRequest(createC);
    getReply();
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
    DismantleCluster disC = new DismantleCluster(name);
    sendRequest(disC);
    getReply();
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
    DestroyDest destrD = new DestroyDest(name);
    sendRequest(destrD);
    getReply();
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
    CreateUser createU = new CreateUser(name, pass);
    sendRequest(createU);
    return new User(this, name, getReply());
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
    GetUser getU = new GetUser(name);
    sendRequest(getU);
    return new User(this, name, getReply());
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
    setUserRight(user, dest, 1);
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
    setUserRight(user, dest, -1);
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
    setUserRight(null, dest, 1);
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
    setUserRight(null, dest, -1);
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
    setUserRight(user, dest, 2);
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
    setUserRight(user, dest, -2);
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
    setUserRight(null, dest, 2);
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
    setUserRight(null, dest, -2);
  }
 
  /** Closes the connection with the server. */
  public void close()
  {
    if (disconnected)
      return;

    if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "--- " + this
                                + " disconnecting...");

    disconnected = true;

    try {
      dos.close();
    }
    catch (IOException iE) {}
    try {
      dis.close();
    }
    catch (IOException iE) {}
    try {
      oos.close();
    }
    catch (IOException iE) {}
    try {
      ois.close();
    }
    catch (IOException iE) {}
    try {
      socket.close();
    }
    catch (IOException iE) {}

    if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, this + ": disconnected...");
  }

  /**
   * Actually tries to open the TCP connection with the server.
   *
   * @exception AdminException  If the admin identification is incorrect.
   * @exception ConnectException  If the connection attempt fails for any
   *              other reason.
   */
  private void connect() throws AdminException, ConnectException
  {
    // Setting the timer values:
    long startTime = System.currentTimeMillis();
    long endTime = startTime + timer * 1000;
    long currentTime;
    long nextSleep = 2000;
    int attemptsC = 0;

    while (true) {
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "Trying to connect to the"
                                  + " server.");
      attemptsC++;
      try {
        // Opening the connection with the server:
        socket = new Socket(serverAddr, serverPort);
        if (socket != null) {
          socket.setTcpNoDelay(true);
          socket.setSoTimeout(0);
          socket.setSoLinger(true, 1000);

          dos = new DataOutputStream(socket.getOutputStream());
          dis = new DataInputStream(socket.getInputStream());

          // Sending the connection request to the server:
          dos.writeUTF("ADMIN: " + adminName + " " + adminPass);
          String reply = (String) dis.readUTF();

          // Processing the server's reply:
          int status = Integer.parseInt(reply.substring(0,1));
          int index = reply.indexOf("INFO: ");
          String info = null;
          if (index != -1)
            info = reply.substring(index + 6);

          // If successful, opening the connection with the admin proxy:
          if (status == 0) {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "Connected!");

            break;
          }
          // If unsuccessful because of an admin id error, throwing an
          // AdminException:
          if (status == 1)
            throw new AdminException("Can't open the connection with"
                                     + " server " + serverAddr.toString()
                                     + " on port " + serverPort
                                     + ": " + info);
        }
        // If socket can't be created, throwing an IO exception:
        else
          throw new IOException("Can't create the socket connected to"
                                + " server " + serverAddr.toString()
                                + ", port " + serverPort);
      }
      catch (Exception e) {
        // IOExceptions notify that the connection could not be opened,
        // possibly because the server is not listening: trying again.
        if (e instanceof IOException) {
          // Keep on trying as long as timer is ok:
          currentTime = System.currentTimeMillis();
          if (currentTime < endTime) {

            if (currentTime + nextSleep > endTime)
              nextSleep = endTime - currentTime;

            // Sleeping for a while:
            try {
              Thread.sleep(nextSleep);
            }
            catch (InterruptedException iE) {}

            // Trying again!
            nextSleep = nextSleep * 2;
            continue;
          }
          // If timer is over, throwing a ConnectException:
          else {
            long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
            throw new ConnectException("Could not open the connection with"
                                       + " server " + serverAddr.toString()
                                       + " on port " + serverPort + " after "
                                       + attemptsC + " attempts during "
                                       + attemptsT + " secs: server is not"
                                       + " listening" );
          }
        }
        else if (e instanceof AdminException)
          throw (AdminException) e;
      }
    }
  }

  /**
   * Actually builds and sends the request for setting users rights.
   *
   * @param user  User, or <code>null</code> for all.
   * @param dest  Name of the destination.
   * @param right  Rights to set.
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the request fails.
   */
  private void setUserRight(User user, String dest, int right) throws Exception
  {
    SetUserRight setU = null;

    if (user != null)
      setU = new SetUserRight(user.getProxyName(), dest, right);
    else
      setU = new SetUserRight(null, dest, right);

    sendRequest(setU);
    getReply();
  }

  /** Increments the requests counter and returns a string image of it. */
  String nextRequestId()
  {
    if (requestsCounter == Long.MAX_VALUE)
      requestsCounter = 0;
    return (new Long(requestsCounter++)).toString();
  }

  /**
   * Sends an admin request to the admin proxy.
   *
   * @param request  The request to send to the proxy.
   * @exception AdminException  If the administrator did disconnect.
   * @exception ConnectException  If the connection with the server is lost
   *              and can't be re-opened.
   */
  void sendRequest(JmsAdminRequest request)
     throws AdminException, ConnectException
  {
    if (disconnected)
      throw new AdminException("Forbidden method call as " + this
                               + " has disconnected.");

    request.setIdentifier(nextRequestId());
  
    try {
      oos.writeObject(request);
      oos.flush();
      oos.reset();

      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, this + ": sent request "
                                  + request.getClass().getName()
                                  + " with id: " + request.getRequestId());
    }
    // Connection is lost:
    catch (IOException iE) {
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgAdmin.log(BasicLevel.WARN, "Lost the connection"
                                  + " with the server.");
      // Trying to reconnect:
      try {
        connect();
      }
      catch (Exception e) {
        ConnectException cE = new ConnectException("Could not send request"
                                                   + " because the connection"
                                                   + " has been lost.");
        if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgAdmin.log(BasicLevel.WARN, cE);
      }
      // If reconnected, trying again to send the request:
      sendRequest(request);
    }
  }

  /**
   * Gets a reply back from the admin proxy.
   *
   * @return  The information contained in the reply.
   * @exception AdminException  If the reply notifies an unsuccessful request.
   * @exception ConnectException  In case of an unexpected object found in the
   *              connection or if it is broken.
   */
  String getReply() throws AdminException, ConnectException
  {
    try {
      // Getting the reply:
      JmsAdminReply reply = (JmsAdminReply) ois.readObject();
      String info = reply.getInfo();

      // Successful reply, returning the information:
      if (reply.succeeded()) {
        if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "... received"
                                    + " successful reply to request "
                                    + reply.getCorrelationId() + ": "
                                    + info);
        return info;
      }
      // Unsuccessful reply, throwing an AdminException:
      else {
        if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgAdmin.log(BasicLevel.WARN, "... received"
                                    + " unsuccessful reply to request "
                                    + reply.getCorrelationId() + ": "
                                    + info);
        throw new AdminException((String) info);
      }
    }
    catch (Exception e) {
      ConnectException cE = null;
      if (e instanceof AdminException)
        throw (AdminException) e;
      // Broken connection: 
      else if (e instanceof IOException)
        cE = new ConnectException("Could not get the reply from the server"
                                  + " as the connection is broken.");
      else 
        cE = new ConnectException("Unexpected reply received from the"
                                  + " server: " + e);

      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgAdmin.log(BasicLevel.ERROR, cE); 
      throw cE;
    }
  }
}
