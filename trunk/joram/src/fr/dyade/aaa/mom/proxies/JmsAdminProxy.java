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
package fr.dyade.aaa.mom.proxies;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.dest.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.jms.*;

/**
 * A <code>JmsAdminProxy</code> agent is a proxy for JMS administrators,
 * automatically created and deployed at the initialization of each server
 * <code>ConnectionFactory</code> service.
 * <p>
 * That means that each server holds a <code>JmsAdminProxy</code> proxy so
 * that administrators can administrate MOM agents on this server.
 */ 
public class JmsAdminProxy extends ConnectionFactory
{
  /** Static reference to the admin proxy instance. */
  static JmsAdminProxy ref;
  /**
   * Table of users.
   * <p>
   * <b>Key:</b> user's name<br>
   * <b>Object:</b> corresponding <code>UserContext</code>
   */
  Hashtable usersTable;
  /**
   * Table of destinations.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> dest agent Id
   */
  private Hashtable destsTable;
  /**
   * Table of clusters.
   * <p>
   * <b>Key:</b> cluster name<br>
   * <b>Object:</b> ids vector of the dests part of the cluster
   */
  private Hashtable clustersTable;


  /**
   * Constructs a <code>JmsAdminProxy</code> agent.
   */
  public JmsAdminProxy()
  {
    super();
    super.multiConn = true;
    usersTable = new Hashtable();
    destsTable = new Hashtable();
    clustersTable = new Hashtable();

    // Create default administrator:
    usersTable.put("root", new UserContext("root", true));

    /*try {
      JmsClientProxy defaultUserProxy = new JmsClientProxy(this.getId());
      defaultUserProxy.deploy();
      UserContext uc = new UserContext("anonymous", false);
      usersTable.put("anonymous", uc);
      uc.proxyId = defaultUserProxy.getId();
    }
    catch (IOException iE) {}*/

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string image of this admin proxy. */
  public String toString()
  {
    return "JmsAdminProxy:" + this.getId().toString();
  }

  /**
   * Initializes the <code>JmsAdminProxy</code> at creation and each
   * time it is reloaded.
   * <p>
   * This method updates the static reference to the proxy instance.
   * 
   * @exception Exception  See superclass.
   */
  public void initialize(boolean firstTime) throws Exception
  {
    super.initialize(firstTime);
    ref = this;
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified InputStream
   * into a <code>JmsInputStream</code>.
   *
   * @param in   An InputStream for this proxy.
   * @return  A NotificationInputStream for this proxy.
   */
  protected NotificationInputStream setInputFilters(InputStream in)
    throws StreamCorruptedException, IOException
  {
    return (new JmsInputStream(in));
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * OutputStream into a <code>JmsOutputStream</code>.
   *
   * @param out  An OutputStream for this proxy.
   * @return  A NotificationOutputStream for this proxy.
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out)
    throws IOException
  {
    return (new JmsOutputStream(out));
  }

  /**
   * This method overrides the <code>ProxyAgent</code> class
   * <code>driverReact</code> method called by the drivers "in".
   * <p>
   * Drivers "in" wrap things into DriverNotifications and send them to
   * the proxy.
   */
  protected void driverReact(int key, Notification not)
  {
    sendTo(this.getId(), new DriverNotification(key, not));
  }
 
  /**
   * This method overrides the <code>ConnectionFactory</code> class
   * <code>react</code> for providing the <code>JmsAdminProxy</code> class
   * with its specific behaviour.
   * <p>
   * Admin proxies react to <code>JmsAdminRequest</code> requests.
   *
   * @exception Exception  When receiving an unexpected request or thrown
   *              by super-classes.
   */ 
  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof DriverNotification) {
      DriverNotification dNot = (DriverNotification) not;

      if (dNot.getNotification() instanceof InputNotification) {
        InputNotification iNot = (InputNotification) dNot.getNotification();

        if (iNot.getObj() instanceof JmsAdminRequest)
          doReact(dNot.getDriverKey(), (JmsAdminRequest) iNot.getObj());

        // Throwing an exception when receiving an unexpected request:
        else
          throw new Exception("Invalid request received by admin proxy: "
                              + this.getId() + ": "
                              + not.getClass().getName());
      }
      // As a DriverIn necessarily wraps the incoming request in an
      // InputMessage, this case can't occur.
      else {}
    }
    else
      super.react(from, not);
  }

  /**
   * Distributes the requests to the appropriate reactions.
   * <p>
   * Admin proxies accept the following requests:
   * <ul>
   *   <li><code>CreateQueue</code></li>
   *   <li><code>CreateTopic</code></li>
   *   <li><code>GetQueue</code></li>
   *   <li><code>GetTopic</code></li>
   *   <li><code>CreateUser</code></li>
   *   <li><code>GetUser</code></li>
   *   <li><code>SetSubTopic</code></li>
   *   <li><code>CreateCluster</code></li>
   *   <li><code>AddAdminId</code></li>
   *   <li><code>DelAdminId</code></li>
   *   <li><code>SetUserRight</code></li>
   *   <li><code>ModifyUserId</code></li>
   *   <li><code>DismantleCluster</code></li>
   *   <li><code>DeleteUser</code></li>
   *   <li><code>DestroyDest</code></li>
   *   <li><code>CreateDeadMQueue</code></li>
   *   <li><code>SetDeadMQueue</code><li>
   *   <li><code>SetThreshold</code></li>
   * </ul>
   */ 
  private void doReact(int key, JmsAdminRequest request)
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this + ": got "
                              + request.getClass().getName());

    if (request instanceof CreateQueue)
      doReact(key, (CreateQueue) request);
    else if (request instanceof GetQueue)
      doReact(key, (GetQueue) request);
    else if (request instanceof CreateTopic)
      doReact(key, (CreateTopic) request);
    else if (request instanceof GetTopic)
      doReact(key, (GetTopic) request);
    else if (request instanceof CreateUser)
      doReact(key, (CreateUser) request);
    else if (request instanceof GetUser)
      doReact(key, (GetUser) request);
    else if (request instanceof SetSubTopic) 
      doReact(key, (SetSubTopic) request);
    else if (request instanceof CreateCluster)
      doReact(key, (CreateCluster) request);
    else if (request instanceof AddAdminId)
      doReact(key, (AddAdminId) request);
    else if (request instanceof DelAdminId)
      doReact(key, (DelAdminId) request);
    else if (request instanceof SetUserRight)
      doReact(key, (SetUserRight) request);
    else if (request instanceof ModifyUserId)
      doReact(key, (ModifyUserId) request);
    else if (request instanceof DismantleCluster)
      doReact(key, (DismantleCluster) request);
    else if (request instanceof DeleteUser)
      doReact(key, (DeleteUser) request);
    else if (request instanceof DestroyDest)
      doReact(key, (DestroyDest) request);
    else if (request instanceof CreateDeadMQueue)
      doReact(key, (CreateDeadMQueue) request);
    else if (request instanceof SetDeadMQueue)
      doReact(key, (SetDeadMQueue) request);
    else if (request instanceof SetThreshold)
      doReact(key, (SetThreshold) request);
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>CreateQueue</code> request requesting the creation of a
   * queue.
   * <p>
   * The request fails if the queue name is already taken or if its
   * deployment fails.
   */ 
  private void doReact(int key, CreateQueue req)
  {
    String name = req.getName();

    if (destsTable.containsKey(name))
      doReply(key, req, false, "Can't create queue as name " + name
              + " is already taken.");
    else {
      Queue queue = new Queue(this.getId());
      try {
        queue.deploy();
        destsTable.put(name, queue.getId());
        doReply(key, req, true, queue.getId().toString());
      } 
      catch (IOException iE) {
        queue = null;
        doReply(key, req, false, "Can't deploy queue " + name + ": " + iE);
      }
    }
  }

  /**
   * Method implementing the admin proxy reaction to a <code>GetQueue</code>
   * request requesting the identifier of a queue agent.
   * <p>
   * The request fails if the queue does not exist.
   */ 
  private void doReact(int key, GetQueue req)
  {
    String name = req.getName();
    AgentId queueId = (AgentId) destsTable.get(name);

    if (queueId == null)
      doReply(key, req, false, "Queue " + name + " does not exist.");
    else
      doReply(key, req, true, queueId.toString());
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>CreateTopic</code> request requesting the creation of a
   * topic.
   * <p>
   * The request fails if the topic name is already taken or if its
   * deployement fails.
   */ 
  private void doReact(int key, CreateTopic req)
  {
    String name = req.getName();

    if (destsTable.containsKey(name))
      doReply(key, req, false, "Can't create topic as name " + name
              + " is already taken.");
    else {
      Topic topic = new Topic(this.getId());
      try {
        topic.deploy();
        destsTable.put(name, topic.getId());
        doReply(key, req, true, topic.getId().toString());
      } 
      catch (IOException iE) {
        topic = null;
        doReply(key, req, false, "Can't deploy topic " + name + ": " + iE);
      }
    }
  }

  /**
   * Method implementing the admin proxy reaction to a <code>GetTopic</code>
   * request requesting the identifier of a topic agent.
   * <p>
   * The request fails if the topic does not exist.
   */ 
  private void doReact(int key, GetTopic req)
  {
    String name = req.getName();
    AgentId topicId = (AgentId) destsTable.get(name);

    if (topicId == null)
      doReply(key, req, false, "Topic " + name + " does not exist.");
    else
      doReply(key, req, true, topicId.toString());
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>DestroyDest</code> request requesting the deletion of a destination
   * agent.
   * <p>
   * The request fails if the destination does not exist.
   */
  private void doReact(int key, DestroyDest req)
  {
    String name = req.getName();
    AgentId destId = (AgentId) destsTable.remove(name);

    if (destId == null) {
      doReply(key, req, false, "Can't delete non existing destination named "
              + name);
      return;
    }

    sendTo(destId, new DeleteNot());
    doReply(key, req, true, "Destination " + name + " is deleted.");
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>SetSubTopic</code> request requesting to set a given topic as the
   * subtopic of an administered one.
   * <p>
   * The request fails if the administered topic does not exist.
   */
  private void doReact(int key, SetSubTopic req)
  {
    String topicName = req.getTopicName();
    AgentId topicId = (AgentId) destsTable.get(topicName);

    if (topicId == null) {
      doReply(key, req, false, "Topic " + topicName + " not administered "
                               + "by this proxy.");
      return;
    }

    AgentId subTopicId = AgentId.fromString(req.getSubTopicId());

    SetSubTopicRequest not =
      new SetSubTopicRequest(key, req.getRequestId(), topicId, subTopicId);

    sendTo(topicId, not);
    sendTo(subTopicId, not);

    doReply(key, req, true, "Topics notified of hierarchy.");
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>CreateCluster</code> request requesting to cluster dests
   * together.
   * <p>
   * This request fails if the cluster name is already taken, or if none of
   * the cluster's dests are administered by this proxy.
   */
  private void doReact(int key, CreateCluster req)
  {
    String name = req.getName();

    if (clustersTable.containsKey(name)) {
      doReply(key, req, false, "Can't create cluster " + name + " as the"
              + " name is already taken.");
      return;
    }

    ClusterRequest not = new ClusterRequest(req.getRequestId());
    String topicName;
    AgentId topicId;
    Vector clusterTopics = new Vector();
    Vector targets = new Vector();

    // Getting the topic identifiers:
    while ((topicName = req.getTopic()) != null) {
      topicId = AgentId.fromString(topicName);
      not.addTopic(topicId);
      clusterTopics.add(topicId);
      // Selecting the dests administered by this proxy:
      if (destsTable.contains(topicId))
        targets.add(topicId);
    } 
    // If none, request failed:
    if (targets.isEmpty()) {
      doReply(key, req, false, "Can't create cluster " + name + " as none"
              + " of its dests are administered by this proxy.");
      return;
    }
    // Otherwise, sending to each the cluster request:
    for (int i = 0; i < targets.size(); i++) {
      topicId = (AgentId) targets.get(i);
      sendTo(topicId, not);
    }
    clustersTable.put(name, targets);
    doReply(key, req, true, "Cluster " + name + " successfully created and"
            + " notified to administered dests " + targets);
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>DismantleCluster</code> request requesting to dismantle a cluster
   * of dests.
   * <p>
   * This request fails if the cluster does not exist.
   */
  private void doReact(int key, DismantleCluster req)
  {
    String name = req.getName();
    Vector targets = (Vector) clustersTable.remove(name);

    if (targets == null) {
      doReply(key, req, false, "Can't dismantle non existing cluster "
              + name);
      return;
    }

    AgentId topicId;
    UnclusterRequest not = new UnclusterRequest(req.getRequestId());

    // Sending the uncluster request to the cluster dests this
    // proxy administers:
    for (int i = 0; i < targets.size(); i++) { 
      topicId = (AgentId) targets.get(i);
      sendTo(topicId, not);
    }
    doReply(key, req, true, "Cancelling of cluster " + name + " has been"
            + " notified to administered dests.");
  }

  /**
   * Method implementing the admin proxy reaction to an
   * <code>AddAdminId</code> request requesting to add an administrator
   * identification.
   * <p>
   * The request fails if the new administrator name already exists.
   */ 
  private void doReact(int key, AddAdminId req)
  {
    String name = req.getName();
    String pass = req.getPass();

    UserContext uc = (UserContext) usersTable.get(name);
    
    if (uc != null && ! pass.equals(uc.password)) {
      doReply(key, req, false, "Can't add admin id " + name + " as it"
              + " is already taken.");
      return;
    }
    if (uc != null)
      uc.admin = true;
    else
      usersTable.put(name, new UserContext(pass, true));
      
    doReply(key, req, true, "Admin id " + name + " / " + pass + " added.");
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>DelAdminId</code> request requesting to delete an administrator
   * identification.
   * <p>
   * The request fails if the administrator id to delete does not exist.
   */ 
  private void doReact(int key, DelAdminId req)
  {
    String name = req.getName();

    UserContext uc = (UserContext) usersTable.remove(name);
    
    if (uc == null) {
      doReply(key, req, false, "Can't delete admin " + name + " as it does"
              + " not exist.");
      return;
    }

    doReply(key, req, true, "Admin " + name + " has been deleted.");
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>CreateUser</code> request requesting the creation of a
   * <code>ClientProxy</code> agent for a given user.
   * <p>
   * The request fails if the user already exists, or if the deployment
   * of the proxy agent fails.
   */ 
  private void doReact(int key, CreateUser req)
  {
    String name = req.getName();
    String pass = req.getPass();
    JmsClientProxy jP;

    UserContext uc = (UserContext) usersTable.get(name);

    if (uc != null
        && (! pass.equals(uc.password) || uc.proxyId != null)) {
      doReply(key, req, false, "Can't create user " + name + " as it already"
              + " exists.");
      return;
    }
    try {
      jP = new JmsClientProxy(this.getId());
      jP.deploy();
      if (uc == null) {
        uc = new UserContext(pass, false);
        usersTable.put(name, uc);
      }
      uc.proxyId = jP.getId();
      doReply(key, req, true, jP.getId().toString());
    }
    catch (IOException iE) {
      jP = null;
      doReply(key, req, false, "Can't deploy proxy for new user " + name);
    }
  }

  /**
   * Method implementing the admin proxy reaction to a <code>GetUser</code>
   * request requesting the name of a user's proxy.
   * <p>
   * The request fails if the user does not exist.
   */ 
  private void doReact(int key, GetUser req)
  {
    String name = req.getName();
    
    UserContext uc = (UserContext) usersTable.get(name);

    if (uc == null || uc.proxyId == null) {
      doReply(key, req, false, "User " + name + " does not exist.");
      return;
    }
    doReply(key, req, true, uc.proxyId.toString());
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>ModifyUserId</code> request requesting the modification of a
   * user identification.
   * <p>
   * The request fails if the user's new id is already taken.
   */ 
  private void doReact(int key, ModifyUserId req)
  {
    String name = req.getName();
    String newName = req.getNewName();
    String newPass = req.getNewPass();

    UserContext uc = (UserContext) usersTable.remove(name);

    if (usersTable.containsKey(newName)) {
      doReply(key, req, false, "Can't set user " + name + " new identity to "
              + newName + " as it is already taken.");
      return;
    }
    uc.password = newPass;
    usersTable.put(newName, uc);
    doReply(key, req, true, "User " + name + " has new identity " + newName);
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>SetUserRight</code> request requesting a user to be granted
   * or removed a right on a destination.
   * <p>
   * The request fails if the destination is not administered by this admin
   * proxy.
   */ 
  private void doReact(int key, SetUserRight req)
  {
    AgentId client = AgentId.fromString(req.getProxyName());
    String dest = req.getDest();
    int right = req.getRight();

    AgentId destId = (AgentId) destsTable.get(dest);

    if (destId == null) {
      doReply(key, req, false, "Can't set right on non existing dest "
              + dest);
      return;
    }
    
    SetRightRequest not = new SetRightRequest(req.getRequestId(), client,
                                              right);
    sendTo(destId, not);
    doReply(key, req, true, "Client " + client + " right " + right + " has"
            + " been sent to dest " + dest);
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>DeleteUser</code> request requesting the deletion of
   * a user and of its proxy.
   * <p>
   * The request fails if the user does not exist.
   */ 
  private void doReact(int key, DeleteUser req)
  {
    String name = req.getName();

    UserContext uc = (UserContext) usersTable.remove(name);

    sendTo(uc.proxyId, new DeleteNot());
    
    doReply(key, req, true, "User " + name + " deleted.");
  }

  
  /**
   * Method implementing the admin proxy reaction to a
   * <code>CreateDeadMQueue</code> request requesting the creation of a
   * dead message queue.
   * <p>
   * The request fails if the queue name is already taken, or if the queue
   * deployement fails.
   */ 
  private void doReact(int key, CreateDeadMQueue req)
  {
    String name = req.getName();

    if (destsTable.containsKey(name))
      doReply(key, req, false, "Can't create queue as name " + name
              + " is already taken.");
    else {
      DeadMQueue queue = new DeadMQueue(this.getId());
      try {
        queue.deploy();
        destsTable.put(name, queue.getId());
        doReply(key, req, true, queue.getId().toString());
      } 
      catch (IOException iE) {
        queue = null;
        doReply(key, req, false, "Can't deploy queue " + name + ": " + iE);
      }
    }
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>SetDeadMQueue</code> request requesting to set a DMQ.
   * <p>
   * The request fails if one of its parameters is incorrect.
   */
  private void doReact(int key, SetDeadMQueue req)
  {
    try {
      String name = req.getName();
      AgentId dmqId = AgentId.fromString(req.getDMQId());

      // Default setting:
      if (name == null)
        DeadMQueueImpl.id = dmqId;
      // User setting:
      else if (req.toUser()) {
        AgentId proxyId = AgentId.fromString(name);
        sendTo(proxyId, new SetDMQRequest(req.getRequestId(), dmqId));
      }
      // Destination setting:
      else {
        AgentId destId = (AgentId) destsTable.get(name);
        if (destId == null) {
          doReply(key, req, false, "Destination " + name + " not" +
                  " administered by this proxy.");
        }
        else
          sendTo(destId, new SetDMQRequest(req.getRequestId(), dmqId));
      }
      doReply(key, req, true, "DMQ has been successfuly set.");
    }
    catch (IllegalArgumentException exc) {
      doReply(key, req, false, "Invalid request: " + exc);
    }
  }

  /**
   * Method implementing the admin proxy reaction to a
   * <code>SetThreshold</code> request requesting to set a threshold.
   * <p>
   * The request fails if one of its parameters is incorrect.
   */
  private void doReact(int key, SetThreshold req)
  {
    String name = req.getName();
    Integer threshold = req.getThreshold();
    AgentId id = null;

    try {
      // Default setting:
      if (name == null) {
        DeadMQueueImpl.threshold = threshold;
        doReply(key, req, true, "Threshold has been successfuly set.");
      }
      else {
        id = AgentId.fromString(name);

        // User setting:
        if (req.toUser()) {
          sendTo(id, new SetThreshRequest(req.getRequestId(), threshold));
          doReply(key, req, true, "Threshold has been successfuly set.");
        }
        // Destination setting:
        else {
          if (destsTable.containsValue(id)) {
            sendTo(id, new SetThreshRequest(req.getRequestId(), threshold));
            doReply(key, req, true, "Threshold has been successfuly set.");
          }
          else {
            doReply(key, req, false, "Destination not administered by this" +
                    " proxy.");
          }
        }
      }
    }
    catch (IllegalArgumentException exc) {
      doReply(key, req, false, "Invalid user proxy identifier: " + exc);
    }
  }

  /**
   * Method used for acknowledging a <code>AdminRequest</code> by
   * sending back an <code>AbstractReply</code>.
   *
   * @param key  Key of the connection involved in the administrator - proxy
   *          communication.
   * @param req  The request being answered.
   * @param success  <code>true</code> if the request was successfull.
   * @param info  Info to send back to the administrator.
   */
  private void doReply(int key, JmsAdminRequest request, boolean success,
                       String info)
  {
    if (success) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Request successfull: "
                                + info);
    }
    else {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Request failed: "
                                + info);
    }
    JmsAdminReply reply = new JmsAdminReply(request, success, info);
    OutputNotification oN = new OutputNotification(reply);

    try { 
      super.sendOut(key, oN);
    }
    // Broken connection
    catch (Exception e) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Could not send"
                                + " the reply back through conn " + key);
    }
  } 
}
