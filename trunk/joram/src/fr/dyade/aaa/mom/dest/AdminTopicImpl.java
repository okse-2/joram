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
    usersTable = new Hashtable();
    soapTable = new Hashtable();
    proxiesTable = new Hashtable();
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
    else if (not instanceof ExceptionReply)
      doReact(from, (ExceptionReply) not);
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
    if (report instanceof LookupReport) {
      AgentId adminTopic0Id = ((LookupReport) report).getAgent();
      Channel.sendTo(adminTopic0Id, new ClusterNot(destId));

      friends = new Vector();
      friends.add(adminTopic0Id);
    }
  }

  /**
   * Method implementing the reaction to an <code>ExceptionReply</code>
   * notification sent by a destination if an admin request went wrong.
   * <p>
   * The information is forwarded to the topics fellows and published to
   * the subscribers.
   */  
  protected void doReact(AgentId from, ExceptionReply not)
  {
    distributeReply(null, not.getCorrelationId(),
                    new AdminReply(false, not.getException().getMessage()));
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetRightRequest</code> notifications.
   *
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, SetRightRequest request)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetDMQRequest</code> notifications.
   *
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, SetDMQRequest not)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
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
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
      MomTracing.dbgDestination.log(BasicLevel.ERROR, "--- " + this
                                    + " notified to be deleted.");
  }


  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>ClusterRequest</code> notifications.
   *
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, ClusterRequest request)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
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
   * acknowledges the process of creating a cluster of topics.
   */ 
  protected void doReact(AgentId from, ClusterAck ack)
  {
    // Extracting the replyTo and id identifiers from the identifier of the
    // original request.
    String reqId = ack.request.getRequestId();
    int index = reqId.indexOf("-");
    AgentId replyTo;
    String id;

    if (index == -1) {
      replyTo = null;
      id = reqId;
    }
    else {
      replyTo = AgentId.fromString(reqId.substring(0, index));
      id = reqId.substring(index);
    }
    distributeReply(replyTo, id, new AdminReply(ack.ok, ack.info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, ack.info);
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
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, UnclusterRequest request)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>SetFatherRequest</code> notifications.
   *
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, SetFatherRequest request)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
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
    // Extracting the replyTo and id identifiers from the identifier of the
    // original request.
    String reqId = ack.request.getRequestId();
    int index = reqId.indexOf("-");
    AgentId replyTo;
    String id;

    if (index == -1) {
      replyTo = null;
      id = reqId;
    }
    else {
      replyTo = AgentId.fromString(reqId.substring(0, index));
      id = reqId.substring(index);
    }
    distributeReply(replyTo, id, new AdminReply(ack.ok, ack.info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, ack.info);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnsetFatherRequest</code> notifications.
   *
   * @exception  RequestException  Systematically thrown.
   */ 
  protected void doReact(AgentId from, UnsetFatherRequest request)
                 throws MomException
  {
    throw new RequestException("Unexpected request");
  }

  
  /**
   * Overrides this <code>TopicImpl</code> method; the forwarded messages
   * containing an admin request will be processed, whereas the forwarded
   * messages containing an admin reply will be distributed.
   */
  protected void doReact(AgentId from, TopicForwardNot not)
  {
    Vector messages = not.messages;

    Message message;
    Object obj;
    Vector requests = new Vector();
    Vector replies = new Vector();
    while (! messages.isEmpty()) {
      message = (Message) messages.remove(0);
      try {
        obj = message.getObject();
      
        if (obj instanceof AdminRequest)
          requests.add(message);
        else if (obj instanceof AdminReply)
          replies.add(message);
      }
      catch (Exception exc) {}
    }

    if (! requests.isEmpty())
      processAdminRequests(requests);

    if (! replies.isEmpty())
      processMessages(replies);
  }

  /**
   * Specializes this <code>TopicImpl</code> reaction.
   */
  protected void doProcess(UnknownAgent uA)
  {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    // For admin requests, notifying the administrator.
    if (not instanceof AbstractRequest) {
      String reqId = ((AbstractRequest) not).getRequestId();
      int index = reqId.indexOf("-");
      AgentId replyTo;
      String id;

      if (index == -1) {
        replyTo = null;
        id = reqId;
      }
      else {
        replyTo = AgentId.fromString(reqId.substring(0, index));
        id = reqId.substring(index);
      }

      String info = "Request [" + not.getClass().getName()
                    + "], sent to AdminTopic on server [" + serverId
                    + "], successful [false]: unknown agent [" + agId + "]";

      distributeReply(replyTo, id, new AdminReply(false, info));
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

        if (request instanceof CreateQueueRequest)
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

      String info = "Request [" + request.getClass().getName()
                    + "], processed by AdminTopic on server [" + serverId
                    + "], successful [true]: " + dest + " ["
                    + qId.toString() + "] has been created and deployed";

      distributeReply(replyTo, msgId,
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
  
      String info = "Request [" + request.getClass().getName()
                    + "], processed by AdminTopic on server [" + serverId
                    + "], successful [true]: topic ["
                    + tId.toString() + "] has been created and deployed";

      distributeReply(replyTo, msgId,
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

    ClusterRequest not;
    if (replyTo == null)
      not = new ClusterRequest(msgId, topId);
    else   
      not = new ClusterRequest(replyTo + "-" + msgId, topId);

    Channel.sendTo(initId, not);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: topic [" + topId
                  + "], successfuly notified to leave cluster.";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    SetFatherRequest not;
    if (replyTo == null)
      not = new SetFatherRequest(msgId, fatherId);
    else   
      not = new SetFatherRequest(replyTo + "-" + msgId, fatherId);

    Channel.sendTo(sonId, not);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: topic [" + topId
                  + "], successfuly notified to unset its father.";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    distributeReply(replyTo, msgId,
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
  
    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: user [" + userId
                  + "] has been set with right [" + right
                  + "] in destination [" + destId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: dmq [" + dmqId.toString()
                  + "], has been notified to destination [" + destId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: dmq [" + dmqId.toString()
                  + "], has been notified to user [" + userId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: threshold [" + thresh
                  + "], has been notified to queue [" + destId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: threshold [" + thresh
                  + "], has been notified to user [" + userId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: dmq has been unset on"
                  + " destination [" + destId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: dmq has been unset on"
                  + " user [" + userId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info); 
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

    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: threshold "
                  + "has been unset on queue [" + destId + "]";

    distributeReply(replyTo, msgId, new AdminReply(true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
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
  
    String info = "Request [" + request.getClass().getName()
                  + "], sent to AdminTopic on server [" + serverId
                  + "], successful [true]: user [" + userId
                  + "] threshold has been unset";

    distributeReply(replyTo, msgId, new AdminReply(true, info));
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
   * Actually sends an <code>AdminReply</code> object to an identified
   * destination, if any, or to the AdminTopic subscribers and fellows.
   *
   * @param to  Identifier of a destination to send the reply to, if any.
   * @param msgId  Identifier of the original request.
   * @param reply  The <code>AdminReply</code> instance to send.
   */
  private void distributeReply(AgentId to, String msgId, AdminReply reply)
  {
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

      if (to != null) {
        ClientMessages clientMessages = new ClientMessages(null, messages);
        Channel.sendTo(to, clientMessages);
      }
      else {
        forwardMessages(messages);
        processMessages(messages);
      }
    }
    catch (Exception exc) {}
  }
}
