/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.agent.ConfigController;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CML;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.shared.admin.*;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.mom.proxies.AdminNotification;
import org.objectweb.joram.mom.proxies.UserAgent;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>AdminTopicImpl</code> class implements the admin topic behaviour,
 * basically processing administration requests.
 */
public class AdminTopicImpl extends TopicImpl {
  /** Reference of the server's local AdminTopicImpl instance. */
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
   * Table holding the local server's destinations names.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> destination agent identifier
   */
  private Hashtable destinationsTable;

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
   * Table keeping the administrator's requests.
   * <p>
   * <b>Key:</b> request's message identifier<br>
   * <b>Value:</b> request's message ReplyTo field
   */
  private Hashtable requestsTable;
  /** Counter of messages produced by this AdminTopic. */
  private long msgCounter = 0;

  /**
   * Identifier of the server's default dead message queue, kept here for
   * persisting it.
   */
  private AgentId defaultDMQId;
  /** Server's default threshold value, kept here for persisting it. */
  private Integer defaultThreshold;

  /**
   * Constructs an <code>AdminTopicImpl</code> instance.
   *
   * @param topicId  Identifier of the agent hosting the AdminTopicImpl.
   */
  public AdminTopicImpl(AgentId topicId) {
    super(topicId, topicId);
    serverId = (new Short(AgentServer.getServerId())).intValue();
    queues = new Vector();
    deadMQueues = new Vector();
    topics = new Vector();
    destinationsTable = new Hashtable();
    usersTable = new Hashtable();
    proxiesTable = new Hashtable();
    requestsTable = new Hashtable();
  }

  public String toString() {
    return "AdminTopicImpl:" + destId.toString();
  }


  /**
   * Method used by <code>ConnectionManager</code> proxies to check their
   * clients identification.
   *
   * @exception Exception  If the user does not exist, is wrongly identified,
   *              or does not have any proxy deployed.
   * @see org.objectweb.joram.mom.proxies.ConnectionManager
   */
  public AgentId getProxyId(String name, String pass) throws Exception {
    String userPass = null;
    AgentId userProxId = null;

    userPass = (String) usersTable.get(name);
    if (userPass != null) {
      if (! userPass.equals(pass))
        throw new Exception("Invalid password for user [" + name + "]");
      userProxId = (AgentId) proxiesTable.get(name);
      if (userProxId == null)
        throw new Exception("No proxy deployed for user [" + name + "]");

      return userProxId;
    }
    else
      throw new Exception("User [" + name + "] does not exist");
  }

  /** Method used by proxies for retrieving their name. */
  public String getName(AgentId proxyId) {
    String name;
    for (Enumeration enum = proxiesTable.keys(); enum.hasMoreElements();) {
      name = (String) enum.nextElement();
      if (proxyId.equals(proxiesTable.get(name)))
        return name;
    }
    return null;
  }

  /** Method used by proxies for retrieving their password. */
  public String getPassword(AgentId proxyId) {
    String name;
    for (Enumeration enum = proxiesTable.keys(); enum.hasMoreElements();) {
      name = (String) enum.nextElement();
      if (proxyId.equals(proxiesTable.get(name)))
        return (String) usersTable.get(name);;
    }
    return null;
  }

  /** Method used by proxies for checking if a given name is already used. */
  public boolean isTaken(String name) {
    return usersTable.containsKey(name);
  }
  
  /** Method returning the id of the admin topic. */ 
  public AgentId getId() {
    return destId;
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  If a received notification is
   *              unexpected by the AdminTopic.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + not.getClass().getName()
                                    + " from: " + from.toString());

    if (not instanceof AdminNotification)
      doReact(from, (AdminNotification) not);
    else if (not instanceof AdminRequestNot)
      doReact(from, (AdminRequestNot) not);
    else if (not instanceof org.objectweb.joram.mom.notifications.AdminReply)
      doReact(from, (org.objectweb.joram.mom.notifications.AdminReply) not);
    else if (not instanceof GetProxyIdNot)
      doReact((GetProxyIdNot)not);
    else if (not instanceof GetProxyIdListNot)
      doReact((GetProxyIdListNot)not);
    else
      super.react(from, not);
  }

  /**
   * Method implementing the reaction to a
   * <code>org.objectweb.joram.mom.proxies.AdminNotification</code>
   * notification notifying of the creation of an admin proxy.
   */
  protected void doReact(AgentId from, AdminNotification adminNot) {
    String name = adminNot.getName();
    String pass = adminNot.getPass();

    usersTable.put(name, pass);
    proxiesTable.put(name, adminNot.getProxyId());

    clients.put(adminNot.getProxyId(), new Integer(READWRITE));
   
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, name + " successfully"
                                    + " set as admin client.");
  }

  /**
   * Method implementing the reaction to a <code>AdminRequest</code>
   * notification notifying of the creation of an admin proxy.
   */
  protected void doReact(AgentId from, AdminRequestNot adminNot) {
    // AF: verify that from is an AdminTopic
    processAdminRequests(adminNot.replyTo, adminNot.msgId, adminNot.request);
  }

  /**
   * Method implementing the reaction to a
   * <code>org.objectweb.joram.mom.notifications.AdminReply</code>
   * notification replying to an administration request.
   * <p>
   * A reply is sent back to the connected administrator if needed.
   */  
  protected void doReact(AgentId from,
                         org.objectweb.joram.mom.notifications.AdminReply not) {
    String requestId = not.getRequestId();
    if (requestId == null) return;

    AgentId replyTo = (AgentId) requestsTable.remove(requestId);
    if (replyTo == null) return;

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
      reply = new AdminReply(not.getSuccess(), 
                             not.getInfo(),
                             not.getReplyObject());
    
    distributeReply(replyTo, requestId, reply);
  }

  protected void doReact(GetProxyIdNot not) {
    try {
      AgentId proxyId = getProxyId(not.getUserName(), 
				   not.getPassword());
      not.Return(proxyId);
    } catch (Exception exc) {
      not.Throw(exc);
    }
  }

  protected void doReact(GetProxyIdListNot not) {
    Vector idList = new Vector();
    Enumeration ids = proxiesTable.elements();
    while (ids.hasMoreElements()) {
      AgentId aid = (AgentId)ids.nextElement();
      idList.addElement(aid);
    }
    AgentId[] res = new AgentId[idList.size()];
    idList.copyInto(res);
    not.Return(res);
  }

  /**
   * Processes a <code>Monit_GetUsersRep</code> notification holding a
   * destination's readers' or writers' identifiers.
   */
  private AdminReply doProcess(Monit_GetUsersRep not) {
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
  private AdminReply doProcess(Monit_FreeAccessRep not) {
    return new Monitor_GetFreeAccessRep(not.getFreeReading(),
                                        not.getFreeWriting());
  }

  /**
   * Processes a <code>Monit_GetDMQSettingsRep</code> notification holding the
   * DMQ settings of a destination or proxy dead message queue.
   */
  private AdminReply doProcess(Monit_GetDMQSettingsRep not) {
    return new Monitor_GetDMQSettingsRep(not.getDMQId(), not.getThreshold());
  }

  /**
   * Processes a <code>Monit_GetFatherRep</code> notification holding the
   * identifier of a topic's hierarchical father.
   */
  private AdminReply doProcess(Monit_GetFatherRep not) {
    return new Monitor_GetFatherRep(not.getFatherId());
  }

  /**
   * Processes a <code>Monit_GetClusterRep</code> notification holding the
   * identifiers of a cluster's topics.
   */
  private AdminReply doProcess(Monit_GetClusterRep not) {
    return new Monitor_GetClusterRep(not.getTopics());
  }

  /**
   * Processes a <code>Monit_GetNumberRep</code> notification holding an
   * integer value sent by a destination.
   */
  private AdminReply doProcess(Monit_GetNumberRep not) {
    return new Monitor_GetNumberRep(not.getNumber());
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetRightRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, SetRightRequest request)
                 throws AccessException {
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
                 throws AccessException {
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
                 throws AccessException {
    if (! isWriter(from))
      throw new AccessException("WRITE right not granted");

    // ... and processing the wrapped requests locally:
    processAdminRequests(not);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; deletion requests are
   * not accepted by AdminTopics.
   */
  protected void doReact(AgentId from, DeleteNot not) {
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
  protected void doReact(AgentId from, ClusterTest request) {
    Channel.sendTo(from, new ClusterAck(request, false,
                                        "Topic [" + destId
                                        + "] is an admin topic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>ClusterAck</code>
   * is not expected by an AdminTopic.
   */ 
  protected void doReact(AgentId from, ClusterAck ack) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; if this AdminTopic is on
   * server0, new cluster fellow is notified to other fellows and other
   * fellows are notified to it.
   */
  protected void doReact(AgentId from, ClusterNot not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected notification: " + not);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnclusterRequest</code> notifications.
   *
   * @exception AccessException  Not thrown.
   */ 
  protected void doReact(AgentId from, UnclusterRequest request)
                 throws MomException {
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
                 throws MomException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept to join a hierarchy.
   */ 
  protected void doReact(AgentId from, FatherTest not) {
    Channel.sendTo(from, new FatherAck(not, false,
                                       "Topic [" + destId
                                       + "] can't accept topic [" + from
                                       + "] as a son as it is an AdminTopic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>FatherAck</code>
   * acknowledges the process of creating a hierarchy of topics.
   */ 
  protected void doReact(AgentId from, FatherAck ack) {
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
                 throws MomException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + request);
  }

  
  /**
   * Overrides this <code>TopicImpl</code> method; the forwarded messages
   * contain admin requests and will be processed.
   */
  protected void doReact(AgentId from, TopicForwardNot not) {
    processAdminRequests(not.messages);
  }

  /**
   * Specializes this <code>TopicImpl</code> reaction.
   */
  protected void doProcess(UnknownAgent uA) {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    // For admin requests, notifying the administrator.
    if (not instanceof org.objectweb.joram.mom.notifications.AdminRequest) {
      String reqId =
        ((org.objectweb.joram.mom.notifications.AdminRequest) not).getId();

      if (reqId != null) {
        AgentId replyTo = (AgentId) requestsTable.remove(reqId);

        String info = strbuf.append("Request [")
          .append(not.getClass().getName())
          .append("], sent to AdminTopic on server [")
          .append(serverId)
          .append("], successful [false]: unknown agent [")
          .append(agId).append("]").toString();
        strbuf.setLength(0);

        distributeReply(replyTo, reqId, new AdminReply(false, info));
      }
    } else {
      super.doProcess(uA);
    }
  }

  /**
   * Method getting the administration requests from messages, and
   * distributing them to the appropriate reactions.
   */ 
  private void processAdminRequests(ClientMessages not) {
    Message msg;
    String msgId = null;
    AgentId replyTo = null;
    String info = null;
    AdminRequest request = null;

    Enumeration messages = not.getMessages().elements();

    while (messages.hasMoreElements()) {
      msg = (Message) messages.nextElement();
      msgId = msg.getIdentifier();
      replyTo = AgentId.fromString(msg.getReplyToId());
      request = null;

      try {
        request = (AdminRequest) msg.getObject();

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "--- " + this + ": got " 
                                        + msg.getObject().getClass().getName());
      } catch (ClassCastException exc) {
        MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                      "--- " + this + ": got bad object");
        if (request == null) {
          info = strbuf.append("Unexpected request to AdminTopic on server [")
            .append(serverId).append("]: ").append(exc.getMessage()).toString();
          strbuf.setLength(0);
        } else {
          info = strbuf.append("Request [").append(request.getClass().getName())
            .append("], sent to AdminTopic on server [").append(serverId)
            .append("], successful [false]: ")
            .append(exc.getMessage()).toString();
          strbuf.setLength(0);
        }

        distributeReply(replyTo, msgId, new AdminReply(false, info));
      } catch (Exception exc) {}

      processAdminRequests(replyTo, msgId, request);
    }
  }

  /**
   * Method getting the administration requests from messages, and
   * distributing them to the appropriate reactions.
   */ 
  private void processAdminRequests(AgentId replyTo,
                                    String msgId,
                                    AdminRequest request) {
    String info = null;

    try {
      if (request instanceof StopServerRequest)
        doProcess((StopServerRequest) request, replyTo, msgId);
      else if (request instanceof CreateDestinationRequest)
        doProcess((CreateDestinationRequest) request, replyTo, msgId);
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
      else if (request instanceof Monitor_GetServersIds)
        doProcess((Monitor_GetServersIds) request, replyTo, msgId);
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
      else if (request instanceof SpecialAdmin)
        doProcess((SpecialAdmin) request, replyTo, msgId);
      else if (request instanceof AddServerRequest)
        doProcess((AddServerRequest) request, replyTo, msgId);
      else if (request instanceof AddDomainRequest)
        doProcess((AddDomainRequest) request, replyTo, msgId);
      else if (request instanceof RemoveServerRequest)
        doProcess((RemoveServerRequest) request, replyTo, msgId);
      else if (request instanceof RemoveDomainRequest)
        doProcess((RemoveDomainRequest) request, replyTo, msgId);
      else if (request instanceof GetConfigRequest)
        doProcess((GetConfigRequest) request, replyTo, msgId);
    } catch (UnknownServerException exc) {
      // Caught when a target server is invalid.
      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], successful [false]: ")
        .append(exc.getMessage()).toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(false, info));
    } catch (MomException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, exc);

      if (request == null) {
        info = strbuf.append("Unexpected request to AdminTopic on server [")
          .append(serverId).append("]: ").append(exc.getMessage()).toString();
        strbuf.setLength(0);
      } else {
        info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], sent to AdminTopic on server [").append(serverId)
          .append("], successful [false]: ")
          .append(exc.getMessage()).toString();
        strbuf.setLength(0);
      }

      distributeReply(replyTo, msgId, new AdminReply(false, info));
    }
  }

  /**
   * Processes a <code>StopServerRequest</code> instance requesting to stop
   * a given server.
   */
  private void doProcess(StopServerRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
     if (checkServerId(request.getServerId())) {
      // It's the local server, process the request.
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Server stopped"));
      AgentServer.stop(false, 1000);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>CreateDestinationRequest</code> instance
   * requesting the creation of a destination.
   *
   * @exception UnknownServerException  If the target server does not exist.
   * @exception RequestException  If the destination deployement fails.
   */
  private void doProcess(CreateDestinationRequest request,
                         AgentId replyTo,
                         String msgId)
    throws UnknownServerException, RequestException {

    if (checkServerId(request.getServerId())) {
      // The destination is  local, process the request.
      String destName = request.getDestinationName();
      boolean destNameActivated = (destName != null && ! destName.equals(""));

      AgentId destId;
      
      Agent dest = null;
      String info;
      String clazz = request.getClassName();
      Properties properties = request.getProperties();
      
      // Retrieving an existing destination:
      if (destNameActivated && destinationsTable.containsKey(destName)) {
        destId = (AgentId) destinationsTable.get(destName);
        info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], processed by AdminTopic on server [").append(serverId)
          .append("], successful [true]: destination [")
          .append(destName).append("] has been retrieved").toString();
        strbuf.setLength(0);
      }
      // Instanciating the destination class.
      else {
        try {
          if ((clazz != null) && (clazz.length() > 0)) {
            dest = (Agent) Class.forName(clazz).newInstance();
            if (properties != null)
            ((AdminDestinationItf) dest).setProperties(properties);
            ((AdminDestinationItf) dest).init(this.destId);
          } else throw new Exception("Invalid empty class name.");
        } catch (Exception exc) {
          if (exc instanceof ClassCastException)
            throw new RequestException("Class [" + clazz + "] is not a Destination class.");
          else
            throw new RequestException("Could not instanciate Destination class [" + clazz + "]: " + exc);
        }

        try {
          dest.deploy();
          destId = dest.getId();
          
          if (dest instanceof DeadMQueue)
            deadMQueues.add(destId);
          else if (dest instanceof Topic)
            topics.add(destId);
          else if (dest instanceof Queue)
            queues.add(destId);
          
          if (destNameActivated)
            destinationsTable.put(destName, destId);
          
          info = strbuf.append("Request [").append(request.getClass().getName())
            .append("], processed by AdminTopic on server [").append(serverId)
            .append("], successful [true]: ").append(clazz).append(" [")
            .append(destId.toString()).append("] has been created and deployed")
            .toString();
          strbuf.setLength(0);
        }
        catch (Exception exc) {
          throw new RequestException("Error while deploying Destination [" + clazz + "]: " + exc);
        }
      }

      distributeReply(replyTo,
                      msgId,
                      new CreateDestinationReply(destId.toString(), info));
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>DeleteDestination</code> instance requesting the
   * deletion of a destination.
   */
  private void doProcess(DeleteDestination request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getId());

    // If the destination is not local, doing nothing:
    if (checkServerId(destId.getTo())) {
      // The destination is  local, process the request.
      String info;

      queues.remove(destId);
      topics.remove(destId);
      deadMQueues.remove(destId);

      String name;
      AgentId id;
      for (Enumeration names = destinationsTable.keys();
           names.hasMoreElements();) {
        name = (String) names.nextElement();
        id = (AgentId) destinationsTable.get(name);
        if (id.equals(destId))
          destinationsTable.remove(name);
      }

      Channel.sendTo(destId, new DeleteNot());

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], sent to AdminTopic on server [").append(serverId)
        .append("], successful [true]: destination [").append(destId)
        .append("], successfuly notified for deletion").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetCluster<code> instance requesting to link two topics
   * in a cluster relationship.
   */
  private void doProcess(SetCluster request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId initId = AgentId.fromString(request.getInitId());
    AgentId topId = AgentId.fromString(request.getTopId());

    if (checkServerId(initId.getTo())) {
      // The initiator is  local, process the request.
      Channel.sendTo(initId, new ClusterRequest(msgId, topId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(initId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetCluster<code> instance requesting a topic to
   * leave the cluster it is part of.
   */
  private void doProcess(UnsetCluster request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId topId = AgentId.fromString(request.getTopId());

    if (checkServerId(topId.getTo())) {
      // The destination is  local, process the request.
      Channel.sendTo(topId, new UnclusterRequest(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(topId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetFather<code> instance requesting to link two topics
   * in a hierarchical relationship.
   */
  private void doProcess(SetFather request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
     AgentId fatherId = AgentId.fromString(request.getFather());
     AgentId sonId = AgentId.fromString(request.getSon());

     if (checkServerId(sonId.getTo())) {
       // If the son is local, process the request.
       Channel.sendTo(sonId, new SetFatherRequest(msgId, fatherId));
       if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(sonId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetFather<code> instance requesting a topic to
   * unset its hierarchical father.
   */
  private void doProcess(UnsetFather request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId topId = AgentId.fromString(request.getTopId());

    if (checkServerId(topId.getTo())) {
      // If the topic is local, process the request.
      Channel.sendTo(topId, new UnsetFatherRequest(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(topId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>CreateUserRequest</code> instance requesting the
   * creation of a <code>UserAgent</code> for a given user.
   *
   * @exception UnknownServerException  If the target server does not exist.
   * @exception RequestException  If the user already exists but with a
   *              different password, or if the proxy deployment failed.
   */
  private void doProcess(CreateUserRequest request,
                         AgentId replyTo,
                         String msgId)
               throws UnknownServerException, RequestException
  {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      String name = request.getUserName();
      String pass = request.getUserPass();

      AgentId proxId = (AgentId) proxiesTable.get(name);
      String info;

      // The user has already been set. 
      if (proxId != null) {
        if (! pass.equals((String) usersTable.get(name))) {
          throw new RequestException("User [" + name + "] already exists"
                                     + " but with a different password.");
        }
        info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], processed by AdminTopic on server [").append(serverId)
          .append("], successful [true]: proxy [").append(proxId.toString())
          .append("] of user [").append(name)
          .append("] has been retrieved").toString();
        strbuf.setLength(0);
      } else {
        UserAgent proxy = new UserAgent();
        proxId = proxy.getId();

        try {
          proxy.deploy();
          usersTable.put(name, request.getUserPass());
          proxiesTable.put(name, proxy.getId());
  
          info = strbuf.append("Request [").append(request.getClass().getName())
            .append("], processed by AdminTopic on server [").append(serverId)
            .append("], successful [true]: proxy [")
            .append(proxId.toString()).append("] for user [").append(name)
            .append("] has been created and deployed").toString();
          strbuf.setLength(0);
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
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UpdateUser</code> instance requesting to modify the
   * identification of a user.
   *
   * @exception RequestException  If the user does not exist, or if it's new
   *              name is already used.
   */
  private void doProcess(UpdateUser request,
                         AgentId replyTo,
                         String msgId)
               throws RequestException, UnknownServerException
  {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    if (checkServerId(proxId.getTo())) {
      // If the user belong to this server, process the request.
      String info;

      // If the user does not exist: throwing an exception:
      if (! usersTable.containsKey(name))
        throw new RequestException("User [" + name + "] does not exist");

      String newName = request.getNewName();
      // If the new name is already taken by an other user than the modified
      // one:
      if (! newName.equals(name)
          && (usersTable.containsKey(newName)))
        throw new RequestException("Name [" + newName + "] already used");

      String newPass = request.getNewPass();

      if (usersTable.containsKey(name)) {
        usersTable.remove(name);
        proxiesTable.remove(name);
        usersTable.put(newName, request.getNewPass());
        proxiesTable.put(newName, proxId);
      }

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], processed by AdminTopic on server [").append(serverId)
        .append("], successful [true]: user [").append(name)
        .append("] has been updated to [").append(newName).
        append("]").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(proxId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>DeleteUser</code> instance requesting the deletion
   * of a user.
   */
  private void doProcess(DeleteUser request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    if (checkServerId(proxId.getTo())) {
      // If the user belong to this server, process the request.
      String info;

      if (usersTable.containsKey(name)) {
        Channel.sendTo(proxId, new DeleteNot());
        usersTable.remove(name);
        proxiesTable.remove(name);
    
        info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], sent to AdminTopic on server [").append(serverId)
          .append("], successful [true]: proxy [").append(proxId)
          .append("], of user [").append(name)
          .append("] has been notified of deletion").toString();
        strbuf.setLength(0);
      } else {
        info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], sent to AdminTopic on server [").append(serverId)
          .append("], successful [false]: user [").append(name)
          .append(" does not exist").toString();
        strbuf.setLength(0);
      }
      distributeReply(replyTo, msgId, new AdminReply(true, info));
    
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(proxId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetRight</code> instance requesting to grant a user
   * a given right on a given destination.
   */
  private void doProcess(SetRight request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (checkServerId(destId.getTo())) {
      // If the destination belong to this server, process request
      
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
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetDefaultDMQ</code> request requesting a given
   * dead message queue to be set as the default one.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(SetDefaultDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      String info;

      AgentId dmqId = null;
      if (request.getDmqId() != null)
        dmqId = AgentId.fromString(request.getDmqId());

      DeadMQueueImpl.id = dmqId;

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], sent to AdminTopic on server [").append(serverId)
        .append("], successful [true]: dmq [").append(dmqId.toString())
        .append("], has been successfuly set as the default one").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetDestinationDMQ</code> request requesting a given
   * dead message queue to be set as the DMQ of a given destination.
   */
  private void doProcess(SetDestinationDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      AgentId dmqId = AgentId.fromString(request.getDmqId());
      Channel.sendTo(destId, new SetDMQRequest(msgId, dmqId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetUserDMQ</code> request requesting a given
   * dead message queue to be set as the DMQ of a given user.
   */
  private void doProcess(SetUserDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    if (checkServerId(userId.getTo())) {
      // The user is local, process the request.
      AgentId dmqId = AgentId.fromString(request.getDmqId());
      Channel.sendTo(userId, new SetDMQRequest(msgId, dmqId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(userId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  } 

  /**
   * Processes a <code>SetDefaultThreshold</code> request requesting a given
   * threshold value to be set as the default one.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(SetDefaultThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      String info;

      DeadMQueueImpl.threshold = new Integer(request.getThreshold());

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], sent to AdminTopic on server [").append(serverId)
        .append("], successful [true]: default threshold [")
        .append(request.getThreshold()).append("] has been set").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info)); 

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetQueueThreshold</code> request requesting
   * a given threshold value to be set as the threshold of a given
   * queue.
   */
  private void doProcess(SetQueueThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getQueueId());

    if (checkServerId(destId.getTo())) {
      // The destination is not local, doing nothing.
      int thresh = request.getThreshold();
      Channel.sendTo(destId, new SetThreshRequest(msgId, new Integer(thresh)));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetUserThreshold</code> request requesting
   * a given threshold value to be set as the threshold of a given
   * user.
   */
  private void doProcess(SetUserThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    if (checkServerId(userId.getTo())) {
      // The user is local, process the request.
      int thresh = request.getThreshold();
      Channel.sendTo(userId, new SetThreshRequest(msgId, new Integer(thresh)));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(userId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetDefaultDMQ</code> request requesting to unset
   * the default DMQ of a given server.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(UnsetDefaultDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      String info;

      DeadMQueueImpl.id = null;

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], sent to AdminTopic on server [").append(serverId)
        .append("], successful [true]: default dmq has been unset").toString();
      strbuf.setLength(0);
      
      distributeReply(replyTo, msgId, new AdminReply(true, info)); 
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetDestinationDMQ</code> request requesting to unset
   * the DMQ of a given destination.
   */
  private void doProcess(UnsetDestinationDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new SetDMQRequest(msgId, null));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetUserDMQ</code> request requesting to unset
   * the DMQ of a given user.
   */
  private void doProcess(UnsetUserDMQ request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    if (checkServerId(userId.getTo())) {
      // The user is local, process the request.
      Channel.sendTo(userId, new SetDMQRequest(msgId, null));
      if (replyTo != null)requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(userId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetDefaultThreshold</code> request requesting
   * to unset the default threshold value.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(UnsetDefaultThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      String info;

      DeadMQueueImpl.threshold = null;

      info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], sent to AdminTopic on server [").append(serverId)
        .append("], successful [true]: default threshold has been unset")
        .toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info)); 

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                      "Default threshold unset.");
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetQueueThreshold</code> request requesting
   * to unset the threshold of a given queue.
   */
  private void doProcess(UnsetQueueThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
     AgentId destId = AgentId.fromString(request.getQueueId());

     if (checkServerId(destId.getTo())) {
       // The destination is local, process the request.
       Channel.sendTo(destId, new SetThreshRequest(msgId, null));
       if (replyTo != null) requestsTable.put(msgId, replyTo);
     } else {
       // Forward the request to the right AdminTopic agent.
       Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>UnsetUserThreshold</code> request requesting to unset
   * the threshold of a given user.
   */
  private void doProcess(UnsetUserThreshold request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId userId = AgentId.fromString(request.getUserProxId());

    if (checkServerId(userId.getTo())) {
      // The user is local, process the request.
      Channel.sendTo(userId, new SetThreshRequest(msgId, null));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(userId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetServersIds</code> request by sending 
   * the list of the platform servers' ids.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(Monitor_GetServersIds request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      Enumeration enum = AgentServer.getServersIds();
      Vector ids = new Vector();
      while (enum.hasMoreElements())
        ids.add(enum.nextElement());

      Monitor_GetServersIdsRep reply = new Monitor_GetServersIdsRep(ids);
      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetDestinations</code> request by sending 
   * registered destinations.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(Monitor_GetDestinations request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      Monitor_GetDestinationsRep reply = new Monitor_GetDestinationsRep();

      int i;
      for (i = 0; i < queues.size(); i ++)
        reply.addQueue(((AgentId) queues.get(i)).toString());
      for (i = 0; i < deadMQueues.size(); i ++)
        reply.addDeadMQueue(((AgentId) deadMQueues.get(i)).toString());
      for (i = 0; i < topics.size(); i ++)
        reply.addTopic(((AgentId) topics.get(i)).toString());
      reply.setNames(destinationsTable);

      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetUsers</code> request by sending the
   * users table.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(Monitor_GetUsers request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (checkServerId(request.getServerId())) {
      Monitor_GetUsersRep reply = new Monitor_GetUsersRep();
  
      String name; 
      for (Enumeration names = proxiesTable.keys(); names.hasMoreElements();) {
        name = (String) names.nextElement();
        reply.addUser(name, ((AgentId) proxiesTable.get(name)).toString());
      }
      
      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetReaders</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetReaders request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new Monit_GetReaders(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetWriters</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetWriters request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new Monit_GetWriters(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetFreeAccess</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(Monitor_GetFreeAccess request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
    Channel.sendTo(destId, new Monit_FreeAccess(msgId));
    if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetDMQSettings</code> request either by
   * processing it and sending back the default DMQ settings, or by
   * forwarding it to its target destination or proxy.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(Monitor_GetDMQSettings request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    if (request.getServerId() != -1) {
      if (checkServerId(request.getServerId())) {
        Monitor_GetDMQSettingsRep reply;
        String id = null;
        if (DeadMQueueImpl.id != null)
          id = DeadMQueueImpl.id.toString();
        reply = new Monitor_GetDMQSettingsRep(id, DeadMQueueImpl.threshold);
        distributeReply(replyTo, msgId, reply);
      } else {
        // Forward the request to the right AdminTopic agent.
        Channel.sendTo(AdminTopic.getDefault((short) request.getServerId()),
               new AdminRequestNot(replyTo, msgId, request));
      }
    } else {
      if (request.getTarget() != null) {
        AgentId targetId = AgentId.fromString(request.getTarget());
        
        if (checkServerId(targetId.getTo())) {
          Channel.sendTo(targetId, new Monit_GetDMQSettings(msgId));

          if (replyTo != null)
            requestsTable.put(msgId, replyTo);
        } else {
          // Forward the request to the right AdminTopic agent.
          Channel.sendTo(AdminTopic.getDefault(targetId.getTo()),
                 new AdminRequestNot(replyTo, msgId, request));
        }
      } else {
        // AF: Return an error/empty message to unlock client ?
      }
    }
  }

  /**
   * Processes a <code>Monitor_GetFather</code> request by forwarding it to
   * its target topic, if local.
   */
  private void doProcess(Monitor_GetFather request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId topicId = AgentId.fromString(request.getTopic());

    if (checkServerId(topicId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(topicId, new Monit_GetFather(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(topicId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetCluster</code> request by forwarding it to
   * its target topic, if local.
   */
  private void doProcess(Monitor_GetCluster request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId topicId = AgentId.fromString(request.getTopic());

    if (checkServerId(topicId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(topicId, new Monit_GetCluster(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(topicId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetPendingMessages</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetPendingMessages request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new Monit_GetPendingMessages(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetPendingRequests</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetPendingRequests request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new Monit_GetPendingRequests(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>Monitor_GetSubscriptions</code> request by
   * forwarding it to its target queue, if local.
   */
  private void doProcess(Monitor_GetSubscriptions request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException
  {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      Channel.sendTo(destId, new Monit_GetSubscriptions(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }
  
  private void doProcess(SpecialAdmin request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDestId());
    
    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "AdminTopicImpl.doProcess " +
                                      "SpecialAdminRequest destId=" + destId);

      if (getId().equals(destId)) {
        // If this destination is the target destination, doing nothing:
        distributeReply(replyTo, msgId,
                        new AdminReply(false, "destId mustn't be TopicAdmin."));
        return;
      }

      Channel.sendTo(destId, new SpecialAdminRequest(msgId,request));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      Channel.sendTo(AdminTopic.getDefault(destId.getTo()),
             new AdminRequestNot(replyTo, msgId, request));
    }
  }

  private void doProcess(AddDomainRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      ConfigController ctrl = AgentServer.getConfigController();
      ctrl.beginConfig();
      ctrl.addDomain(request.getDomainName(),
                     "fr.dyade.aaa.agent.SimpleNetwork");
      ctrl.addNetwork(request.getServerName(),
                      request.getDomainName(),
                      request.getPort());
      ctrl.commitConfig();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Domain added"));
      if (replyTo != null) {
        broadcastRequest(request);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(RemoveDomainRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      ConfigController ctrl = AgentServer.getConfigController();
      ctrl.beginConfig();
      ctrl.removeDomain(request.getDomainName());
      ctrl.commitConfig();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Domain removed"));
      if (replyTo != null) {
        broadcastRequest(request);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }
  
  private void doProcess(AddServerRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      ConfigController ctrl = AgentServer.getConfigController();
      ctrl.beginConfig();
      ctrl.addServer(request.getServerName(),
                     request.getHostName(),
                     request.getServerId());
      ctrl.addNetwork(request.getServerName(),
                      request.getDomainName(),
                      request.getPort());
      ctrl.addService(request.getServerName(),
                      "org.objectweb.joram.mom.proxies.ConnectionManager",
                      "root root");
      ctrl.commitConfig();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Server added"));
      if (replyTo != null) {
        broadcastRequest(request);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(RemoveServerRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      ConfigController ctrl = AgentServer.getConfigController();
      ctrl.beginConfig();
      ctrl.removeServer(request.getServerName());
      ctrl.commitConfig();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Server removed"));
      if (replyTo != null) {
        broadcastRequest(request);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(GetConfigRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      A3CMLConfig a3cmlConfig = AgentServer.getConfig();
      ByteArrayOutputStream baos = 
        new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(baos);
      A3CML.toXML(a3cmlConfig, out);
      baos.flush();
      baos.close();
      String config = baos.toString();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, config));
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void broadcastRequest(AdminRequest req) {
    AdminRequestNot not = new AdminRequestNot(null, null, req);
    Enumeration ids = AgentServer.getServersIds();
    while (ids.hasMoreElements()) {
      short id = ((Short) ids.nextElement()).shortValue();
      if (id != AgentServer.getServerId()) {
        Channel.sendTo(AdminTopic.getDefault(id), not);
      }
    }
  }

  /** 
   * Returns <code>true</code> if a given server identification corresponds
   * to the local server's.
   *
   * @param serverId  Server identifier.
   *
   * @exception UnknownServerException  If the server does not exist.
   */
  private boolean checkServerId(int serverId) throws UnknownServerException
  {
    if (serverId == this.serverId)
      return true;

    Enumeration ids = AgentServer.getServersIds();
    while (ids.hasMoreElements()) {
      if (((Short) ids.nextElement()).intValue() == serverId)
        return false;
    }

    throw new UnknownServerException("server#" + serverId + " is unknow.");
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

    if (msgCounter == Long.MAX_VALUE) msgCounter = 0;
    msgCounter++;

    message.setIdentifier("ID:" + destId.toString() + ":" + msgCounter);
    message.setCorrelationId(msgId);
    message.setTimestamp(System.currentTimeMillis());
    message.setDestination(destId.toString(), false);

    try {
      message.setObject(reply);

      Vector messages = new Vector();
      messages.add(message);

      ClientMessages clientMessages = new ClientMessages(-1, -1, messages);
      Channel.sendTo(to, clientMessages);
    }
    catch (Exception exc) {}
  }


  /** Serializes an <code>AdminTopicImpl</code> instance. */
  private void writeObject(java.io.ObjectOutputStream out)
               throws java.io.IOException
  {
    defaultDMQId = DeadMQueueImpl.id;
    defaultThreshold = DeadMQueueImpl.threshold;
    out.defaultWriteObject();
  }

  /** Deserializes an <code>AdminTopicImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    ref = this;
    DeadMQueueImpl.id = defaultDMQId;
    DeadMQueueImpl.threshold = defaultThreshold;
  }

  static class AdminRequestNot extends Notification {
    String msgId = null;
    AgentId replyTo = null;
    AdminRequest request = null;

    AdminRequestNot(AgentId replyTo,
                    String msgId,
                    AdminRequest request) {
      this.msgId = msgId;
      this.replyTo = replyTo;
      this.request = request;
    }
  }
}
