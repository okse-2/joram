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
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.admin.*;
import fr.dyade.aaa.mom.admin.AdminRequest;
import fr.dyade.aaa.mom.admin.AdminReply;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.mom.proxies.AdminNotification;
import fr.dyade.aaa.mom.proxies.soap.SoapProxy;
import fr.dyade.aaa.mom.proxies.tcp.JmsProxy;
import fr.dyade.aaa.ns.*;

import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * The <code>AdminTopicImpl</code> class implements the admin topic behaviour,
 * basically processing administration requests.
 */
public class AdminTopicImpl extends TopicImpl
{
  /** Reference of the current AdminTopicImpl instance. */
  public static AdminTopicImpl ref;

  /** Identifier of the server this topic is deployed on. */
  private int serverId;

  /** Vector holding the local server's queues' identifiers. */
  private Vector queues;
  /** Vector holding the local server's dead message queues' identifiers. */
  private Vector deadMQueues;
  /** Vector holding the local server's topics' identifiers. */
  private Vector topics;

  /**
   * Table holding the TCP users identifications.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Object:</b> user password
   */
  private Hashtable usersTable;
  /**
   * Table holding the TCP users proxies identifiers.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Object:</b> proxy's identifier
   */
  private Hashtable proxiesTable;
  /**
   * Table holding the SOAP users identifications.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Object:</b> user password
   */
  private Hashtable soapTable;

  /** Table keeping the replyTo identifiers per request. */
  private Hashtable requestsTable;
  /** Counter of messages produced by this AdminTopic. */
  private long msgCounter = 0;

  /** Identifier of the admin proxy, temporarily kept. */
  private AgentId adminProxId;



  /**
   * Constructs an <code>AdminTopicImpl</code> instance.
   *
   * @param topicId  Identifier of the agent hosting the AdminTopicImpl.
   */
  public AdminTopicImpl(AgentId topicId)
  {
    super(topicId, topicId);
    serverId = (new Short(AgentServer.getServerId())).intValue();
    queues = new Vector();
    deadMQueues = new Vector();
    topics = new Vector();
    usersTable = new Hashtable();
    soapTable = new Hashtable();
    proxiesTable = new Hashtable();
    requestsTable = new Hashtable();
  }

  /** Returns a string view of this AdminTopicImpl instance. */
  public String toString()
  {
    return "AdminTopicImpl:" + destId.toString();
  }

  /** 
   * (Re) initialiazes the AdminTopicImpl reference, and at the first
   * initialization, registers this topic to the NameService on server 0.
   */
  public void initialize(boolean firstTime)
  {
    ref = this;

    if (! firstTime)
      return;

    AgentId nsId = NameService.getDefault((new Integer(0)).shortValue());
    Channel.sendTo(nsId, new RegisterCommand(destId,
                                             "AdminTopic#initial",
                                             destId,
                                             false));
  }

  /**
   * Method used by <code>fr.dyade.aaa.mom.proxies.tcp.ConnectionFactory</code>
   * and <code>fr.dyade.aaa.mom.proxies.soap.SoapProxy</code> proxies to check
   * their clients identification.
   *
   * @exception Exception  If the user does not exist, is wrongly identified,
   *              or does not have any proxy deployed.
   */
  public AgentId getProxyId(String name, String pass) throws Exception
  {
    String userPass = null;
    AgentId userProxId = null;

    // Checking among the TCP users:
    userPass = (String) usersTable.get(name);
    if (userPass != null) {
      if (! userPass.equals(pass))
        throw new Exception("Invalid password for user [" + name + "]");
      userProxId = (AgentId) proxiesTable.get(name);
      if (userProxId == null)
        throw new Exception("No proxy deployed for user [" + name + "]");

      return userProxId;
    }

    // Checking among the SOAP users:
    userPass = (String) soapTable.get(name);
    if (userPass != null) {
      if (userPass.equals(pass))
        throw new Exception("Invalid password for user [" + name + "]");
      return SoapProxy.id;
    }
    else
      throw new Exception("User [" + name + "] does not exist");
  }

  /** Method returning the id of the admin topic. */ 
  public AgentId getId()
  {
    return destId;
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  If a received notification is
   *              unexpected by the AdminTopic.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + not.getClass().getName()
                                    + " from: " + from.toString());

    if (not instanceof AdminNotification)
      doReact(from, (AdminNotification) not);
    else if (not instanceof SimpleReport)
      doReact(from, (SimpleReport) not);
    else if (not instanceof fr.dyade.aaa.mom.comm.AdminReply)
      doReact(from, (fr.dyade.aaa.mom.comm.AdminReply) not);
    else
      super.react(from, not);
  }

  /**
   * Method implementing the reaction to a
   * <code>fr.dyade.aaa.mom.proxies.AdminNotification</code> notification 
   * notifying of the creation of an admin proxy.
   */
  protected void doReact(AgentId from, AdminNotification adminNot)
  {
    String name = adminNot.getName();
    String pass = adminNot.getPass();

    usersTable.put(name, pass);
    proxiesTable.put(name, adminNot.getProxyId());

    readers.add(adminNot.getProxyId());
    writers.add(adminNot.getProxyId());
   
    adminProxId = adminNot.getProxyId();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, name + " successfully"
                                    + " set as admin client.");
  }

  /**
   * Method implementing the reaction to a <code>SimpleReport</code>
   * notification coming from the NameService.
   * <p>
   * If the report is a successful <code>LookupReport</code>, it carries the
   * identifier of the initial AdminTopic. It is then used for clustering the
   * AdminTopics together.
   */ 
  protected void doReact(AgentId from, SimpleReport report)
  {
    // Request to the NameService failed:
    if (report.getStatus() != 4) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR, "Error occured while"
                                      + " linking admin topics, platform is"
                                      + " not administerable");
    }
    // LookupReport: it carries the identifier of the initial AdminTopic. 
    else if (report instanceof LookupReport) {
      AgentId adminTopic0Id = ((LookupReport) report).getAgent();
      Channel.sendTo(adminTopic0Id, new ClusterNot(destId));

      friends = new Vector();
      friends.add(adminTopic0Id);
    }
  }

  /**
   * Method implementing the reaction to a
   * <code>fr.dyade.aaa.mom.comm.AdminReply</code> notification replying to
   * an administration request.
   * <p>
   * A reply is sent back if needed.
   */ 
  protected void doReact(AgentId from, fr.dyade.aaa.mom.comm.AdminReply not)
  {
    String requestId = not.getRequestId();
    if (requestId == null)
      return;

    AgentId replyTo = (AgentId) requestsTable.remove(requestId);
    if (replyTo == null)
      return;

    AdminReply reply;

    if (not instanceof Monit_GetUsersRep)
      reply = doProcess((Monit_GetUsersRep) not);
    else if (not instanceof Monit_FreeAccessRep)
      reply = doProcess((Monit_FreeAccessRep) not);
    else if (not instanceof Monit_GetDMQSettingsRep)
      reply = doProcess((Monit_GetDMQSettingsRep) not);
    else if (not instanceof Monit_GetFatherRep)
      reply = doProcess((Monit_GetFatherRep) not);
    else if (not instanceof Monit_GetClusterRep)
      reply = doProcess((Monit_GetClusterRep) not);
    else if (not instanceof Monit_GetNumberRep)
      reply = doProcess((Monit_GetNumberRep) not);
    else
      reply = new AdminReply(not.getSuccess(), not.getInfo());
    
    distributeReply(replyTo, requestId, reply);
  }

  /**
   * Processes a <code>Monit_GetUsersRep</code> notification holding a
   * destination's readers' or writers' identifiers.
   */
  private AdminReply doProcess(Monit_GetUsersRep not)
  {
    Vector users = not.getUsers();

    String name;
    AgentId proxyId;
    Monitor_GetUsersRep reply = new Monitor_GetUsersRep();

    for (Enumeration names = proxiesTable.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      proxyId = (AgentId) proxiesTable.get(name);

      if (users.contains(proxyId))
        reply.addUser(name, proxyId.toString());
    }
    return reply;
  }

  /**
   * Processes a <code>Monit_FreeAccesRep</code> notification holding the
   * free access status of a destination.
   */
  private AdminReply doProcess(Monit_FreeAccessRep not)
  {
    return new Monitor_GetFreeAccessRep(not.getFreeReading(),
                                        not.getFreeWriting());
  }

  /**
   * Processes a <code>Monit_GetDMQSettingsRep</code> notification holding the
   * DMQ settings of a destination or proxy dead message queue.
   */
  private AdminReply doProcess(Monit_GetDMQSettingsRep not)
  {
    return new Monitor_GetDMQSettingsRep(not.getDMQId(), not.getThreshold());
  }

  /**
   * Processes a <code>Monit_GetFatherRep</code> notification holding the
   * identifier of a topic's hierarchical father.
   */
  private AdminReply doProcess(Monit_GetFatherRep not)
  {
    return new Monitor_GetFatherRep(not.getFatherId());
  }

  /**
   * Processes a <code>Monit_GetClusterRep</code> notification holding the
   * identifiers of a cluster's topics.
   */
  private AdminReply doProcess(Monit_GetClusterRep not)
  {
    return new Monitor_GetClusterRep(not.getTopics());
  }

  /**
   * Processes a <code>Monit_GetNumberRep</code> notification holding an
   * integer value sent by a destination.
   */
  private AdminReply doProcess(Monit_GetNumberRep not)
  {
    return new Monitor_GetNumberRep(not.getNumber());
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetRightRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, SetRightRequest request)
                 throws AccessException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetDMQRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, SetDMQRequest request)
                 throws AccessException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method;
   * <code>ClientMessages</code> notifications hold requests sent by an
   * administrator.
   *
   * @exception AccessException  If the requester is not a WRITER on the
   *              AdminTopic.
   */
  protected void doReact(AgentId from, ClientMessages not)
                 throws AccessException
  {
    if (! isWriter(from))
      throw new AccessException("WRITE right not granted");

    // Forwarding messages to the cluster.
    forwardMessages(not.getMessages());
    // ... and processing the wrapped requests locally:
    processAdminRequests(not.getMessages());
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; deletion requests are
   * not accepted by AdminTopics.
   */
  protected void doReact(AgentId from, DeleteNot not)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + not);
  }


  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>ClusterRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, ClusterRequest request)
                 throws AccessException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept to join clusters other than their admin topics cluster.
   */ 
  protected void doReact(AgentId from, ClusterTest request)
  {
    Channel.sendTo(from, new ClusterAck(request, false,
                                        "Topic [" + destId
                                        + "] is an admin topic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>ClusterAck</code>
   * is not expected by an AdminTopic.
   */ 
  protected void doReact(AgentId from, ClusterAck ack)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; if this AdminTopic is on
   * server0, new cluster fellow is notified to other fellows and other
   * fellows are notified to it.
   */
  protected void doReact(AgentId from, ClusterNot not)
  {
    if (friends == null)
      friends = new Vector();

    if (serverId == 0) {
      AgentId friendId;
      for (int i = 0; i < friends.size(); i++) {
        friendId = (AgentId) friends.get(i);
        Channel.sendTo(friendId, not);
        Channel.sendTo(not.topicId, new ClusterNot(friendId));
      }
    }
    friends.add(not.topicId);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Topic "
                                    + not.topicId.toString()
                                    + " set as a fellow.");
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnclusterRequest</code> notifications.
   *
   * @exception AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, UnclusterRequest request)
                 throws MomException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>SetFatherRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, SetFatherRequest request)
                 throws MomException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept to join a hierarchy.
   */ 
  protected void doReact(AgentId from, FatherTest not)
  {
    Channel.sendTo(from, new FatherAck(not, false,
                                       "Topic [" + destId
                                       + "] can't accept topic [" + from
                                       + "] as a son as it is an AdminTopic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>FatherAck</code>
   * acknowledges the process of creating a hierarchy of topics.
   */ 
  protected void doReact(AgentId from, FatherAck ack)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnsetFatherRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, UnsetFatherRequest request)
                 throws MomException
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  
  /**
   * Overrides this <code>TopicImpl</code> method; the forwarded messages
   * contain admin requests and will be processed.
   */
  protected void doReact(AgentId from, TopicForwardNot not)
  {
    processAdminRequests(not.messages);
  }

  /**
   * Specializes this <code>TopicImpl</code> reaction.
   */
  protected void doProcess(UnknownAgent uA)
  {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    // For admin requests, notifying the administrator.
    if (not instanceof fr.dyade.aaa.mom.comm.AdminRequest) {
      String reqId = ((fr.dyade.aaa.mom.comm.AdminRequest) not).getId();

      if (reqId != null) {
        AgentId replyTo = (AgentId) requestsTable.remove(reqId);

        String info = "Request ["
                      + not.getClass().getName()
                      + "], sent to AdminTopic on server ["
                      + serverId
                      + "], successful [false]: unknown agent ["
                      + agId + "]";

        distributeReply(replyTo, reqId, new AdminReply(false, info));
      }
    }
    super.doProcess(uA);
  }


  /**
   * Method getting the administration requests from a vector of messages,
   * and distributing them to the appropriate reactions.
   */ 
  private void processAdminRequests(Vector messages)
  {
    String msgId = null;
    AgentId replyTo = null;
    AdminRequest request = null;

    try {
      Message msg;
      for (int i = 0; i < messages.size(); i++) {
        msg = (Message) messages.get(i);
        msgId = msg.getIdentifier();
        replyTo = AgentId.fromString(msg.getReplyToId());

        try {
          try {
            request = (AdminRequest) msg.getObject();

            if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                        + ": got " + msg.getObject().getClass().getName());
          }
          catch (ClassCastException exc) {
            throw new RequestException(msg.getObject().getClass().getName());
          }
        }
        catch (Exception exc) {}

        if (request instanceof StopServerRequest)
          doProcess((StopServerRequest) request, replyTo, msgId);
        else if (request instanceof CreateQueueRequest)
          doProcess((CreateQueueRequest) request, replyTo, msgId);
        else if (request instanceof CreateTopicRequest)
          doProcess((CreateTopicRequest) request, replyTo, msgId);
        else if (request instanceof DeleteDestination)
          doProcess((DeleteDestination) request, replyTo, msgId);
        else if (request instanceof SetCluster)
          doProcess((SetCluster) request, replyTo, msgId);
        else if (request instanceof UnsetCluster)
          doProcess((UnsetCluster) request, replyTo, msgId);
        else if (request instanceof SetFather)
          doProcess((SetFather) request, replyTo, msgId);
        else if (request instanceof UnsetFather)
          doProcess((UnsetFather) request, replyTo, msgId);
        else if (request instanceof CreateUserRequest)
          doProcess((CreateUserRequest) request, replyTo, msgId);
        else if (request instanceof CreateSoapUserRequest)
          doProcess((CreateSoapUserRequest) request, replyTo, msgId);
        else if (request instanceof UpdateUser)
          doProcess((UpdateUser) request, replyTo, msgId);
        else if (request instanceof DeleteUser)
          doProcess((DeleteUser) request, replyTo, msgId);
        else if (request instanceof SetRight)
          doProcess((SetRight) request, replyTo, msgId);
        else if (request instanceof SetDefaultDMQ)
          doProcess((SetDefaultDMQ) request, replyTo, msgId);
        else if (request instanceof SetDestinationDMQ)
          doProcess((SetDestinationDMQ) request, replyTo, msgId);
        else if (request instanceof SetUserDMQ)
          doProcess((SetUserDMQ) request, replyTo, msgId);
        else if (request instanceof SetDefaultThreshold)
          doProcess((SetDefaultThreshold) request, replyTo, msgId);
        else if (request instanceof SetQueueThreshold)
          doProcess((SetQueueThreshold) request, replyTo, msgId);
        else if (request instanceof SetUserThreshold)
          doProcess((SetUserThreshold) request, replyTo, msgId);
        else if (request instanceof UnsetDefaultDMQ)
          doProcess((UnsetDefaultDMQ) request, replyTo, msgId);
        else if (request instanceof UnsetDestinationDMQ)
          doProcess((UnsetDestinationDMQ) request, replyTo, msgId);
        else if (request instanceof UnsetUserDMQ)
          doProcess((UnsetUserDMQ) request, replyTo, msgId);
        else if (request instanceof UnsetDefaultThreshold)
          doProcess((UnsetDefaultThreshold) request, replyTo, msgId);
        else if (request instanceof UnsetQueueThreshold)
          doProcess((UnsetQueueThreshold) request, replyTo, msgId);
        else if (request instanceof UnsetUserThreshold)
          doProcess((UnsetUserThreshold) request, replyTo, msgId);
        else if (request instanceof OldAddAdminId)
          doProcess((OldAddAdminId) request, replyTo, msgId);
        else if (request instanceof OldDelAdminId)
          doProcess((OldDelAdminId) request, replyTo, msgId);
        else if (request instanceof Monitor_GetDestinations)
          doProcess((Monitor_GetDestinations) request, replyTo, msgId);
        else if (request instanceof Monitor_GetUsers)
          doProcess((Monitor_GetUsers) request, replyTo, msgId);
        else if (request instanceof Monitor_GetReaders)
          doProcess((Monitor_GetReaders) request, replyTo, msgId);
        else if (request instanceof Monitor_GetWriters)
          doProcess((Monitor_GetWriters) request, replyTo, msgId);
        else if (request instanceof Monitor_GetFreeAccess)
          doProcess((Monitor_GetFreeAccess) request, replyTo, msgId);
        else if (request instanceof Monitor_GetDMQSettings)
          doProcess((Monitor_GetDMQSettings) request, replyTo, msgId);
        else if (request instanceof Monitor_GetFather)
          doProcess((Monitor_GetFather) request, replyTo, msgId);
        else if (request instanceof Monitor_GetCluster)
          doProcess((Monitor_GetCluster) request, replyTo, msgId);
        else if (request instanceof Monitor_GetPendingMessages)
          doProcess((Monitor_GetPendingMessages) request, replyTo, msgId);
        else if (request instanceof Monitor_GetPendingRequests)
          doProcess((Monitor_GetPendingRequests) request, replyTo, msgId);
        else if (request instanceof Monitor_GetSubscriptions)
          doProcess((Monitor_GetSubscriptions) request, replyTo, msgId);
      }
    }
    catch (MomException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, exc);

      String info = null;
      if (request == null)
        info = "Unexpected request sent to AdminTopic on server ["
               + serverId + "]: " + exc.getMessage();
      else
        info = "Request [" + request.getClass().getName()
               + "], sent to AdminTopic on server [" + serverId
               + "], successful [false]: " + exc.getMessage();

      distributeReply(replyTo, msgId, new AdminReply(false, info));
    }
    // Caught when a destination identifier is invalid: processed on server0.
    catch (IllegalArgumentException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR, exc);

      if (serverId != 0)
        return;

      String info = "Request [" + request.getClass().getName()
                    + "], successful [false]: the target destination is not"
                    + " a JORAM destination";

      distributeReply(replyTo, msgId, new AdminReply(false, info));
    }
  }

  /**
   * Processes a <code>StopServerRequest</code> instance requesting to stop
   * a given server.
   */
  private void doProcess(StopServerRequest request,
                         AgentId replyTo,
                         String msgId)
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;

    distributeReply(replyTo, msgId, new AdminReply(true, "Server stopped"));

    new Thread() {
      public void run()
      {
        AgentServer.stop();
      }
    }.start();
  }

  /**
   * Processes a <code>CreateQueueRequest</code> instance requesting the
   * creation of a <code>Queue</code> or a <code>DeadMQueue</code>
   * destination.
   *
   * @exception RequestException  If the queue deployement fails.
   */
  private void doProcess(CreateQueueRequest request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;
      
    Queue queue = null;
    String dest;
    if (request instanceof CreateDMQRequest) {
      queue = new DeadMQueue(destId);
      dest = "DMQ";
    }
    else {
      queue = new Queue(destId);
      dest = "queue";
    }

    try {
      queue.deploy();
      AgentId qId = queue.getId();

      if (dest.equals("queue"))
        queues.add(qId);
      else
        deadMQueues.add(qId);

      String info = "Request [" + request.getClass().getName()
                    + "], processed by AdminTopic on server [" + serverId
                    + "], successful [true]: " + dest + " ["
                    + qId.toString() + "] has been created and deployed";

      distributeReply(replyTo,
                      msgId,
                      new CreateDestinationReply(qId.toString(), info));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    }
    catch (Exception exc) {
      throw new RequestException("Queue not deployed: " + exc);
    }
  }
  
  /**
   * Processes a <code>CreateTopicRequest</code> instance requesting the
   * creation of a <code>Topic</code> destination.
   *
   * @exception RequestException  If the topic deployement failed.
   */
  private void doProcess(CreateTopicRequest request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;

    Topic topic = new Topic(destId);

    try {
      topic.deploy();
      AgentId tId = topic.getId();

      topics.add(tId);
  
      String info = "Request [" + request.getClass().getName()
                    + "], processed by AdminTopic on server [" + serverId
                    + "], successful [true]: topic ["
                    + tId.toString() + "] has been created and deployed";

      distributeReply(replyTo,
                      msgId,
                      new CreateDestinationReply(tId.toString(), info));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    }
    catch (Exception exc) {
      throw new RequestException("Topic not deployed: " + exc);
    }
  }

  /**
   * Processes a <code>DeleteDestination</code> instance requesting the
   * deletion of a destination.
   */
  private void doProcess(DeleteDestination request, AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getId());

    // If the destination is not local, doing nothing:
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new DeleteNot());

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: destination [" + destId
                  + "], successfuly notified for deletion";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes a <code>SetCluster<code> instance requesting to link two topics
   * in a cluster relationship.
   */
  private void doProcess(SetCluster request, AgentId replyTo, String msgId)
  {
    AgentId initId = AgentId.fromString(request.getInitId());
    AgentId topId = AgentId.fromString(request.getTopId());

    // If the initiator is not local, doing nothing:
    if (initId.getTo() != serverId)
      return;

    Channel.sendTo(initId, new ClusterRequest(msgId, topId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetCluster<code> instance requesting a topic to
   * leave the cluster it is part of.
   */
  private void doProcess(UnsetCluster request, AgentId replyTo, String msgId)
  {
    AgentId topId = AgentId.fromString(request.getTopId());

    // If the topic is not local, doing nothing:
    if (topId.getTo() != serverId)
      return;

    Channel.sendTo(topId, new UnclusterRequest(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>SetFather<code> instance requesting to link two topics
   * in a hierarchical relationship.
   */
  private void doProcess(SetFather request, AgentId replyTo, String msgId)
  {
     AgentId fatherId = AgentId.fromString(request.getFather());
     AgentId sonId = AgentId.fromString(request.getSon());

    // If the son is not local, doing nothing:
    if (sonId.getTo() != serverId)
      return;


    Channel.sendTo(sonId, new SetFatherRequest(msgId, fatherId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetFather<code> instance requesting a topic to
   * unset its hierarchical father.
   */
  private void doProcess(UnsetFather request, AgentId replyTo, String msgId)
  {
    AgentId topId = AgentId.fromString(request.getTopId());

    // If the topic is not local, doing nothing:
    if (topId.getTo() != serverId)
      return;
    
    Channel.sendTo(topId, new UnsetFatherRequest(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>CreateUserRequest</code> instance requesting the
   * creation of a <code>JmsProxy</code> agent for a given user.
   *
   * @exception RequestException  If the user already exists and is a SOAP
   *              user, or if it already exists as a TCP user but with a
   *              different password, or if the proxy deployment failed.
   */
  private void doProcess(CreateUserRequest request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;

    String name = request.getUserName();
    String pass = request.getUserPass();

    if (soapTable.containsKey(name)) {
      throw new RequestException("User ["
                                 + name
                                 + "] already exists and is a SOAP user");
    }

    AgentId proxId = (AgentId) proxiesTable.get(name);
    String info;

    // The user has already been set. 
    if (proxId != null) {
      if (! pass.equals((String) usersTable.get(name))) {
        throw new RequestException("User [" + name + "] already exists"
                                   + " but with a different password.");
      }
      info = "Request [" + request.getClass().getName()
             + "], processed by AdminTopic on server [" + serverId
             + "], successful [true]: proxy [" + proxId.toString()
             + "] of user [" + name + "] has been retrieved";
    }
    else {
      JmsProxy proxy = new JmsProxy();
      proxId = proxy.getId();

      try {
        proxy.deploy();
        usersTable.put(name, request.getUserPass());
        proxiesTable.put(name, proxy.getId());
  
        info = "Request [" + request.getClass().getName()
               + "], processed by AdminTopic on server [" + serverId
               + "], successful [true]: proxy ["
               + proxId.toString() + "] for user [" + name 
               + "] has been created and deployed";
      }
      catch (Exception exc) {
        throw new RequestException("User proxy not deployed: " + exc);
      }
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);

    distributeReply(replyTo,
                    msgId,
                    new CreateUserReply(proxId.toString(), info));
  }

  /**
   * Processes a <code>CreateSoapUserRequest</code> instance requesting the
   * setting of a new SOAP user on the local SOAP proxy.
   *
   * @exception RequestException  If the user already exists and is a TCP
   *              user, or if it already exists as a SOAP user but with a
   *              different password, or if the SOAP proxy service has not
   *              been started on the target server.
   */
  private void doProcess(CreateSoapUserRequest request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;

    String name = request.getUserName();
    String pass = request.getUserPass();

    if (usersTable.containsKey(name)) {
      throw new RequestException("User ["
                                 + name
                                 + "] already exists and is a TCP user");
    }

    // The user has already been set. 
    String storedPass = (String) soapTable.get(name);
    if (storedPass != null) {
      if (! pass.equals(storedPass)) {
        throw new RequestException("User [" + name + "] already exists"
                                   + " but with a different password.");
      }
    }
    else
      soapTable.put(name, pass);

    AgentId proxId = SoapProxy.id;
    if (proxId == null) {
      soapTable.remove(name);
      throw new RequestException("SOAP proxy service does not run on server " 
                                 + serverId);
    }

    String info = "Request [" + request.getClass().getName()
                  + "], processed by AdminTopic on server [" + serverId
                  + "], successful [true]: proxy [" + proxId.toString()
                  + "] of user [" + name + "] has been retrieved";

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);

    distributeReply(replyTo, msgId,
                    new CreateUserReply(proxId.toString(), info));
  }

  /**
   * Processes an <code>UpdateUser</code> instance requesting to modify the
   * identification of a user.
   *
   * @exception RequestException  If the user does not exist, or if it's new
   *              name is already used.
   */
  private void doProcess(UpdateUser request, AgentId replyTo, String msgId) 
               throws RequestException
  {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    // If the user does not belong to this server, doing nothing:
    if (proxId.getTo() != serverId)
      return;

    // If the user does not exist: throwing an exception:
    if (! usersTable.containsKey(name) && ! soapTable.containsKey(name))
      throw new RequestException("User [" + name + "] does not exist");

    String newName = request.getNewName();
    // If the new name is already taken by an other user than the modified
    // one:
    if (! newName.equals(name)
        && (usersTable.containsKey(newName)
            || soapTable.containsKey(newName)))
      throw new RequestException("Name [" + newName + "] already used");

    String newPass = request.getNewPass();

    // The user is a TCP user:
    if (usersTable.containsKey(name)) {
      usersTable.remove(name);
      proxiesTable.remove(name);
      usersTable.put(newName, request.getNewPass());
      proxiesTable.put(newName, proxId);
    }
    // Else, it is a SOAP user:
    else {
      soapTable.remove(name);
      soapTable.put(newName, request.getNewPass());
    }

    String info = "Request [" + request.getClass().getName()
                  + "], processed by AdminTopic on server [" + serverId
                  + "], successful [true]: user [" + name + "] has been"
                  + " updated to [" + newName + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes a <code>DeleteUser</code> instance requesting the deletion
   * of a user.
   */
  private void doProcess(DeleteUser request, AgentId replyTo, String msgId)
  {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    // If the user does not belong to this server, or has been deleted, doing
    // nothing.
    if (proxId.getTo() != serverId
        || (! usersTable.containsKey(name) && ! soapTable.containsKey(name)))
      return;

    String info;

    // The user to delete is a TCP user:
    if (usersTable.containsKey(name)) {
      Channel.sendTo(proxId, new DeleteNot());
      usersTable.remove(name);
      proxiesTable.remove(name);

      info = "Request [" + request.getClass().getName()
             + "], sent to AdminTopic on server [" + serverId
             + "], successful [true]: proxy [" + proxId
             + "], of user [" + name + "] has been notified of deletion";
    }
    // Else, it is a SOAP user:
    else {
      soapTable.remove(name);

      info = "Request [" + request.getClass().getName()
             + "], sent to AdminTopic on server [" + serverId
             + "], successful [true]: user [" + name + "] has been deleted";
    }

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes a <code>SetRight</code> instance requesting to grant a user
   * a given right on a given destination.
   */
  private void doProcess(SetRight request, AgentId replyTo, String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    // If the destination does not belong to this server, doing nothing:
    if (destId.getTo() != serverId)
      return;

    AgentId userId = null;
    if (request.getUserProxId() != null)
      userId = AgentId.fromString(request.getUserProxId());

    int right = 0;
    if (request instanceof SetReader)
      right = READ;
    else if (request instanceof SetWriter)
      right = WRITE;
    else if (request instanceof UnsetReader)
      right = - READ;
    else if (request instanceof UnsetWriter)
      right = - WRITE;

    Channel.sendTo(destId, new SetRightRequest(msgId, userId, right));
    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>SetDefaultDMQ</code> request requesting a given
   * dead message queue to be set as the default one.
   */
  private void doProcess(SetDefaultDMQ request, AgentId replyTo, String msgId)
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId)
      return;

    AgentId dmqId = null;
    if (request.getDmqId() != null)
      dmqId = AgentId.fromString(request.getDmqId());

    DeadMQueueImpl.id = dmqId;

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: dmq [" + dmqId.toString()
                  + "], has been successfuly set as the default one";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes a <code>SetDestinationDMQ</code> request requesting a given
   * dead message queue to be set as the DMQ of a given destination.
   */
  private void doProcess(SetDestinationDMQ request, AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    AgentId dmqId = AgentId.fromString(request.getDmqId());

    Channel.sendTo(destId, new SetDMQRequest(msgId, dmqId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>SetUserDMQ</code> request requesting a given
   * dead message queue to be set as the DMQ of a given user.
   */
  private void doProcess(SetUserDMQ request, AgentId replyTo, String msgId)
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    // The user is not local, doing nothing.
    if (userId.getTo() != serverId)
      return;

    AgentId dmqId = AgentId.fromString(request.getDmqId());

    Channel.sendTo(userId, new SetDMQRequest(msgId, dmqId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  } 

  /**
   * Processes a <code>SetDefaultThreshold</code> request requesting a given
   * threshold value to be set as the default one.
   */
  private void doProcess(SetDefaultThreshold request, AgentId replyTo,
                         String msgId)
  {
    // If this server is not the target server, doing nothing.
    if (request.getServerId() != serverId)
      return;

    DeadMQueueImpl.threshold = new Integer(request.getThreshold());

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: default threshold ["
                  + request.getThreshold() + "] has been set";

    distributeReply(replyTo, msgId, new AdminReply(true, info)); 

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes a <code>SetQueueThreshold</code> request requesting
   * a given threshold value to be set as the threshold of a given
   * queue.
   */
  private void doProcess(SetQueueThreshold request, AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getQueueId());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    int thresh = request.getThreshold();

    Channel.sendTo(destId, new SetThreshRequest(msgId, new Integer(thresh)));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>SetUserThreshold</code> request requesting
   * a given threshold value to be set as the threshold of a given
   * user.
   */
  private void doProcess(SetUserThreshold request, AgentId replyTo,
                         String msgId)
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    // The user is not local, doing nothing.
    if (userId.getTo() != serverId)
      return;

    int thresh = request.getThreshold();

    Channel.sendTo(userId, new SetThreshRequest(msgId, new Integer(thresh)));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetDefaultDMQ</code> request requesting to unset
   * the default DMQ of a given server.
   */
  private void doProcess(UnsetDefaultDMQ request, AgentId replyTo,
                         String msgId)
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId) 
      return;

    DeadMQueueImpl.id = null;

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: default dmq has been unset";

    distributeReply(replyTo, msgId, new AdminReply(true, info)); 

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Processes an <code>UnsetDestinationDMQ</code> request requesting to unset
   * the DMQ of a given destination.
   */
  private void doProcess(UnsetDestinationDMQ request, AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new SetDMQRequest(msgId, null));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetUserDMQ</code> request requesting to unset
   * the DMQ of a given user.
   */
  private void doProcess(UnsetUserDMQ request, AgentId replyTo, String msgId)
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    // The user is not local, doing nothing.
    if (userId.getTo() != serverId)
      return;

    Channel.sendTo(userId, new SetDMQRequest(msgId, null));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetDefaultThreshold</code> request requesting
   * to unset the default threshold value.
   */
  private void doProcess(UnsetDefaultThreshold request, AgentId replyTo,
                         String msgId)
  {
    // If this server is not the target server, doing nothing:
    if (request.getServerId() != serverId) 
      return;

    DeadMQueueImpl.threshold = null;

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: default threshold has been unset";

    distributeReply(replyTo, msgId, new AdminReply(true, info)); 

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Default threshold"
                                    + " unset.");
  }

  /**
   * Processes an <code>UnsetQueueThreshold</code> request requesting
   * to unset the threshold of a given queue.
   */
  private void doProcess(UnsetQueueThreshold request, AgentId replyTo,
                         String msgId)
  {
     AgentId destId = AgentId.fromString(request.getQueueId());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new SetThreshRequest(msgId, null));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes an <code>UnsetUserThreshold</code> request requesting to unset
   * the threshold of a given user.
   */
  private void doProcess(UnsetUserThreshold request, AgentId replyTo,
                         String msgId)
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    // The user is not local, doing nothing.
    if (userId.getTo() != serverId)
      return;

    Channel.sendTo(userId, new SetThreshRequest(msgId, null));
  
    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /** Temporary method kept for maintaining the old administration. */
  private void doProcess(OldAddAdminId request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    if (request.getServerId() != serverId)
      return;

    String name = request.getName();

    if (usersTable.containsKey(name))
      throw new RequestException("Name [" + name + "] already taken");

    usersTable.put(name, request.getPass());
    proxiesTable.put(name, adminProxId);

    distributeReply(replyTo, msgId, new AdminReply(true, null));
  }

  /** Temporary method kept for maintaining the old administration. */
  private void doProcess(OldDelAdminId request, AgentId replyTo,
                         String msgId) throws RequestException
  {
    if (request.getServerId() != serverId)
      return;

    String name = request.getName();

    if (! usersTable.containsKey(name))
      throw new RequestException("Name [" + name + "] is not known");

    usersTable.remove(name);
    proxiesTable.remove(name);

    distributeReply(replyTo, msgId, new AdminReply(true, null));
  }

  /**
   * Processes a <code>Monitor_GetDestinations</code> request by sending 
   * registered destinations.
   */
  private void doProcess(Monitor_GetDestinations request,
                         AgentId replyTo,
                         String msgId)
  {
    if (request.getServerId() != serverId)
      return;

    Monitor_GetDestinationsRep reply = new Monitor_GetDestinationsRep();

    int i;
    for (i = 0; i < queues.size(); i ++)
      reply.addQueue(((AgentId) queues.get(i)).toString());
    for (i = 0; i < deadMQueues.size(); i ++)
      reply.addDeadMQueue(((AgentId) deadMQueues.get(i)).toString());
    for (i = 0; i < topics.size(); i ++)
      reply.addTopic(((AgentId) topics.get(i)).toString());

    distributeReply(replyTo, msgId, reply);
  }

  /**
   * Processes a <code>Monitor_GetUsers</code> request by sending the
   * users table.
   */
  private void doProcess(Monitor_GetUsers request,
                         AgentId replyTo,
                         String msgId)
  {
    if (request.getServerId() != serverId)
      return;

    Monitor_GetUsersRep reply = new Monitor_GetUsersRep();
  
    String name; 
    for (Enumeration names = proxiesTable.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      reply.addUser(name, ((AgentId) proxiesTable.get(name)).toString());
    }

    distributeReply(replyTo, msgId, reply);
  }

  /**
   * Processes a <code>Monitor_GetReaders</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetReaders request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_GetReaders(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetWriters</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetWriters request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_GetWriters(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetFreeAccess</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetFreeAccess request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_FreeAccess(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetDMQSettings</code> request either by
   * processing it and sending back the default DMQ settings, or by
   * forwarding it to its target destination or proxy.
   */
  private void doProcess(Monitor_GetDMQSettings request,
                         AgentId replyTo,
                         String msgId)
  {
    
    if (request.getServerId() != -1 && request.getServerId() == serverId) {
      Monitor_GetDMQSettingsRep reply;
      String id = null;
      if (DeadMQueueImpl.id != null)
        id = DeadMQueueImpl.id.toString();
      reply = new Monitor_GetDMQSettingsRep(id, DeadMQueueImpl.threshold);
      distributeReply(replyTo, msgId, reply);
    }
    else {
      AgentId target = AgentId.fromString(request.getTarget());

      if (target.getTo() == serverId) {
        Channel.sendTo(target, new Monit_GetDMQSettings(msgId));

        if (replyTo != null)
          requestsTable.put(msgId, replyTo);
      }
    }
  }

  /**
   * Processes a <code>Monitor_GetFather</code> request by forwarding it to
   * its target topic, if local.
   */
  private void doProcess(Monitor_GetFather request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId topicId = AgentId.fromString(request.getTopic());

    // The destination is not local, doing nothing.
    if (topicId.getTo() != serverId)
      return;

    Channel.sendTo(topicId, new Monit_GetFather(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetCluster</code> request by forwarding it to
   * its target topic, if local.
   */
  private void doProcess(Monitor_GetCluster request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId topicId = AgentId.fromString(request.getTopic());

    // The destination is not local, doing nothing.
    if (topicId.getTo() != serverId)
      return;

    Channel.sendTo(topicId, new Monit_GetCluster(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetPendingMessages</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetPendingMessages request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_GetPendingMessages(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetPendingRequests</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetPendingRequests request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_GetPendingRequests(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }

  /**
   * Processes a <code>Monitor_GetSubscriptions</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetSubscriptions request,
                         AgentId replyTo,
                         String msgId)
  {
    AgentId destId = AgentId.fromString(request.getDest());

    // The destination is not local, doing nothing.
    if (destId.getTo() != serverId)
      return;

    Channel.sendTo(destId, new Monit_GetSubscriptions(msgId));

    if (replyTo != null)
      requestsTable.put(msgId, replyTo);
  }
    
 
  /** 
   * Actually sends an <code>AdminReply</code> object to an identified
   * destination.
   *
   * @param to  Identifier of a destination to send the reply to.
   * @param msgId  Identifier of the original request.
   * @param reply  The <code>AdminReply</code> instance to send.
   */
  private void distributeReply(AgentId to, String msgId, AdminReply reply)
  {
    if (to == null)
      return;

    Message message = new Message();

    if (msgCounter == Long.MAX_VALUE)
      msgCounter = 0;
    msgCounter++;

    message.setIdentifier("ID:" + destId.toString() + ":" + msgCounter);
    message.setCorrelationId(msgId);
    message.setTimestamp(System.currentTimeMillis());
    message.setDestination(destId.toString(), false);

    try {
      message.setObject(reply);

      Vector messages = new Vector();
      messages.add(message);

      ClientMessages clientMessages = new ClientMessages(null, messages);
      Channel.sendTo(to, clientMessages);
    }
    catch (Exception exc) {}
  }
}
