/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ClusterRequest;
import org.objectweb.joram.mom.notifications.DestinationAdminRequestNot;
import org.objectweb.joram.mom.notifications.GetProxyIdListNot;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.notifications.Monit_FreeAccess;
import org.objectweb.joram.mom.notifications.Monit_FreeAccessRep;
import org.objectweb.joram.mom.notifications.Monit_GetCluster;
import org.objectweb.joram.mom.notifications.Monit_GetClusterRep;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettings;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettingsRep;
import org.objectweb.joram.mom.notifications.Monit_GetFather;
import org.objectweb.joram.mom.notifications.Monit_GetFatherRep;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsg;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsgRep;
import org.objectweb.joram.mom.notifications.Monit_GetNumberRep;
import org.objectweb.joram.mom.notifications.Monit_GetPendingMessages;
import org.objectweb.joram.mom.notifications.Monit_GetPendingRequests;
import org.objectweb.joram.mom.notifications.Monit_GetReaders;
import org.objectweb.joram.mom.notifications.Monit_GetStat;
import org.objectweb.joram.mom.notifications.Monit_GetStatRep;
import org.objectweb.joram.mom.notifications.Monit_GetSubscriptions;
import org.objectweb.joram.mom.notifications.Monit_GetUsersRep;
import org.objectweb.joram.mom.notifications.Monit_GetWriters;
import org.objectweb.joram.mom.notifications.RegisterDestNot;
import org.objectweb.joram.mom.notifications.RegisterTmpDestNot;
import org.objectweb.joram.mom.notifications.RegisteredDestNot;
import org.objectweb.joram.mom.notifications.RequestGroupNot;
import org.objectweb.joram.mom.notifications.SetDMQRequest;
import org.objectweb.joram.mom.notifications.SetFatherRequest;
import org.objectweb.joram.mom.notifications.SetNbMaxMsgRequest;
import org.objectweb.joram.mom.notifications.SetRightRequest;
import org.objectweb.joram.mom.notifications.SetThreshRequest;
import org.objectweb.joram.mom.notifications.SpecialAdminRequest;
import org.objectweb.joram.mom.notifications.UnclusterRequest;
import org.objectweb.joram.mom.notifications.UnsetFatherRequest;
import org.objectweb.joram.mom.notifications.UserAdminRequestNot;
import org.objectweb.joram.mom.proxies.AdminNotification;
import org.objectweb.joram.mom.proxies.SendReplyNot;
import org.objectweb.joram.mom.proxies.UserAgent;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.GetConfigRequest;
import org.objectweb.joram.shared.admin.GetDomainNames;
import org.objectweb.joram.shared.admin.GetDomainNamesRep;
import org.objectweb.joram.shared.admin.GetLocalServer;
import org.objectweb.joram.shared.admin.GetLocalServerRep;
import org.objectweb.joram.shared.admin.GetSubscriberIds;
import org.objectweb.joram.shared.admin.Monitor_GetCluster;
import org.objectweb.joram.shared.admin.Monitor_GetClusterRep;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettings;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettingsRep;
import org.objectweb.joram.shared.admin.Monitor_GetDestinations;
import org.objectweb.joram.shared.admin.Monitor_GetDestinationsRep;
import org.objectweb.joram.shared.admin.Monitor_GetFather;
import org.objectweb.joram.shared.admin.Monitor_GetFatherRep;
import org.objectweb.joram.shared.admin.Monitor_GetFreeAccess;
import org.objectweb.joram.shared.admin.Monitor_GetFreeAccessRep;
import org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsg;
import org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsgRep;
import org.objectweb.joram.shared.admin.Monitor_GetNumberRep;
import org.objectweb.joram.shared.admin.Monitor_GetPendingMessages;
import org.objectweb.joram.shared.admin.Monitor_GetPendingRequests;
import org.objectweb.joram.shared.admin.Monitor_GetReaders;
import org.objectweb.joram.shared.admin.Monitor_GetServersIds;
import org.objectweb.joram.shared.admin.Monitor_GetServersIdsRep;
import org.objectweb.joram.shared.admin.Monitor_GetStat;
import org.objectweb.joram.shared.admin.Monitor_GetStatRep;
import org.objectweb.joram.shared.admin.Monitor_GetSubscriptions;
import org.objectweb.joram.shared.admin.Monitor_GetUsers;
import org.objectweb.joram.shared.admin.Monitor_GetUsersRep;
import org.objectweb.joram.shared.admin.Monitor_GetWriters;
import org.objectweb.joram.shared.admin.QueueAdminRequest;
import org.objectweb.joram.shared.admin.RemoveDomainRequest;
import org.objectweb.joram.shared.admin.RemoveServerRequest;
import org.objectweb.joram.shared.admin.SetCluster;
import org.objectweb.joram.shared.admin.SetDefaultDMQ;
import org.objectweb.joram.shared.admin.SetDefaultThreshold;
import org.objectweb.joram.shared.admin.SetDestinationDMQ;
import org.objectweb.joram.shared.admin.SetFather;
import org.objectweb.joram.shared.admin.SetNbMaxMsg;
import org.objectweb.joram.shared.admin.SetQueueThreshold;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetRight;
import org.objectweb.joram.shared.admin.SetUserDMQ;
import org.objectweb.joram.shared.admin.SetUserThreshold;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.admin.SpecialAdmin;
import org.objectweb.joram.shared.admin.StopServerRequest;
import org.objectweb.joram.shared.admin.UnsetCluster;
import org.objectweb.joram.shared.admin.UnsetDefaultDMQ;
import org.objectweb.joram.shared.admin.UnsetDefaultThreshold;
import org.objectweb.joram.shared.admin.UnsetDestinationDMQ;
import org.objectweb.joram.shared.admin.UnsetFather;
import org.objectweb.joram.shared.admin.UnsetQueueThreshold;
import org.objectweb.joram.shared.admin.UnsetReader;
import org.objectweb.joram.shared.admin.UnsetUserDMQ;
import org.objectweb.joram.shared.admin.UnsetUserThreshold;
import org.objectweb.joram.shared.admin.UnsetWriter;
import org.objectweb.joram.shared.admin.UpdateUser;
import org.objectweb.joram.shared.admin.UserAdminRequest;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.ServerConfigHelper;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CMLDomain;
import fr.dyade.aaa.agent.conf.A3CMLNetwork;
import fr.dyade.aaa.agent.conf.A3CMLServer;

/**
 * The <code>AdminTopicImpl</code> class implements the admin topic behavior,
 * basically processing administration requests.
 */
public final class AdminTopicImpl extends TopicImpl implements AdminTopicImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Reference of the server's local AdminTopicImpl instance. */
  private static AdminTopicImpl ref;

  /** Identifier of the server this topic is deployed on. */
  private int serverId;

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
   * Constructs an <code>AdminTopicImpl</code> instance.
   *
   * @param topicId  Identifier of the agent hosting the AdminTopicImpl.
   */
  public AdminTopicImpl(AgentId topicId) {
    super(topicId, null);
    serverId = AgentServer.getServerId();
    destinationsTable = new Hashtable();
    usersTable = new Hashtable();
    proxiesTable = new Hashtable();
    requestsTable = new Hashtable();
  }

  public String toString() {
    return "AdminTopicImpl";
  }


  /**
   * Method used by <code>ConnectionManager</code> proxies to check their
   * clients identification.
   *
   * @param name
   * @param pass
   * @param inaddr
   * @exception Exception  If the user does not exist, is wrongly identified,
   *              or does not have any proxy deployed.
   * @see org.objectweb.joram.mom.proxies.ConnectionManager
   */
  public AgentId getProxyId(Identity identity,
                            String inaddr) throws Exception {

    AgentId userProxId = null;
    Identity userIdentity = (Identity) usersTable.get(identity.getUserName());
    if (userIdentity == null) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "User [" + identity.getUserName() + "] does not exist");
      throw new Exception("User [" + identity.getUserName() + "] does not exist");
    }

    if (identity instanceof Identity) {
      if (! identity.check(userIdentity)) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "identity check failed.");
        throw new Exception("identity check failed.");
      } else {        
        userProxId = (AgentId) proxiesTable.get(identity.getUserName());
        if (userProxId == null) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "No proxy deployed for user [" + identity.getUserName() + "]");
          throw new Exception("No proxy deployed for user [" + identity.getUserName() + "]");
        }

        return userProxId;
      }
    } else {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Bad Auth must be instanceof Identity :: identity = " + identity);
      throw new Exception("Bad Auth must be instanceof Identity :: identity = " + identity);
    }
  }

  /** Method used by proxies for retrieving their name. */
  public String getName(AgentId proxyId) {
    String name;
    for (Enumeration e = proxiesTable.keys(); e.hasMoreElements();) {
      name = (String) e.nextElement();
      if (proxyId.equals(proxiesTable.get(name)))
        return name;
    }
    return null;
  }

  /** Method used by proxies for retrieving their password. */
  public Object getPassword(AgentId proxyId) {
    String name;
    for (Enumeration e = proxiesTable.keys(); e.hasMoreElements();) {
      name = (String) e.nextElement();
      if (proxyId.equals(proxiesTable.get(name)))
        return usersTable.get(name);
    }
    return null;
  }

  /** Method used by proxies for checking if a given name is already used. */
  public boolean isTaken(String name) {
    return usersTable.containsKey(name);
  }

  /**
   * Method implementing the reaction to a
   * <code>org.objectweb.joram.mom.proxies.AdminNotification</code>
   * notification notifying of the creation of an admin proxy.
   */
  public void AdminNotification(AgentId from, AdminNotification adminNot) {
    Identity identity = adminNot.getIdentity();

    try {
      //identity.validate();

      usersTable.put(identity.getUserName(), identity);
      proxiesTable.put(identity.getUserName(), adminNot.getProxyId());

      clients.put(adminNot.getProxyId(), new Integer(READWRITE));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, identity.getUserName() + " successfully"
                   + " set as admin client.");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Exception:: ", e);
    }
  }

  /**
   * Method implementing the reaction to a <code>AdminRequest</code>
   * notification notifying of the creation of an admin proxy.
   */
  public void AdminRequestNot(AgentId from, AdminRequestNot adminNot) {
    // AF: verify that from is an AdminTopic
    processAdminRequests(adminNot.replyTo, adminNot.msgId, adminNot.request, from);
  }

  /**
   * Method implementing the reaction to a
   * <code>org.objectweb.joram.mom.notifications.AdminReply</code>
   * notification replying to an administration request.
   * <p>
   * A reply is sent back to the connected administrator if needed.
   */  
  public void AdminReply(AgentId from,
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
    else if (not instanceof Monit_GetStatRep)
      reply = doProcess((Monit_GetStatRep) not);
    else if (not instanceof Monit_GetNbMaxMsgRep)
      reply = doProcess((Monit_GetNbMaxMsgRep) not);
    else
      reply = new AdminReply(not.getSuccess(), 
                             not.getInfo(),
                             not.getReplyObject());

    distributeReply(replyTo, requestId, reply);
  }

  public void GetProxyIdNot(GetProxyIdNot not) {
    try {
      AgentId proxyId = getProxyId(not.getIdentity(), 
                                   not.getInAddr());
      not.Return(proxyId);
    } catch (Exception exc) {
      not.Throw(exc);
    }
  }

  public void GetProxyIdListNot(GetProxyIdListNot not) {
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

  public void RegisterTmpDestNot(RegisterTmpDestNot not) {
    String destName = not.getTmpDestId().toString();
    if (not.toAdd()) {
      String type;
      String className;
      if (not.isTopic()) {
        type = "topic.tmp";
        className = Topic.class.getName();
      } else {
        type = "queue.tmp";
        className = Queue.class.getName();
      }
      DestinationDesc destDesc = 
        new DestinationDesc(
                            not.getTmpDestId(),
                            destName,
                            className,
                            type);
      destinationsTable.put(destName, destDesc);
    } else {
      destinationsTable.remove(destName);
    }
  }

  public void RegisterDestNot(RegisterDestNot not) {
    String name = not.getName();
    if (name == null || destinationsTable.contains(name))
      return;

    DestinationDesc destDesc = 
      new DestinationDesc(
                          not.getId(),
                          not.getName(),
                          not.getClassName(),
                          not.getType());
    destinationsTable.put(name, destDesc);
  }

  public void RegisteredDestNot(AgentId from, RegisteredDestNot not) {
    DestinationDesc destDesc = 
      (DestinationDesc) destinationsTable.get(not.getName());
    if (destDesc != null)
      not.setDestination(destDesc.getId());
    forward(not.getReply(), not);
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
   * Processes a <code>Monit_GetStatRep</code> notification holding a
   * statistic sent by a destination.
   */
  private AdminReply doProcess(Monit_GetStatRep not) {
    return new Monitor_GetStatRep(not.getStats());
  }

  /**
   * Processes a <code>Monit_GetNbMaxMsgRep</code> notification holding a
   * nbMaxMsg sent by a destination.
   */
  private AdminReply doProcess(Monit_GetNbMaxMsgRep not) {
    return new Monitor_GetNbMaxMsgRep(not.getNbMaxMsg());
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetRightRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  public void setRightRequest(AgentId from, SetRightRequest request) throws AccessException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; AdminTopics do not
   * accept <code>SetDMQRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  public void setDMQRequest(AgentId from, SetDMQRequest request) throws AccessException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }

  public void requestGroupNot(AgentId from, RequestGroupNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "AdminTopicImpl.requestGroupNot(" + not + ')');
    Enumeration en = not.getClientMessages();
    while (en.hasMoreElements()) {
      ClientMessages cm = (ClientMessages) en.nextElement();
      try {
        clientMessages(from, cm);
      } catch (Exception exc) {
      }
    }
  }

  public SetRightRequest preProcess(SetRightRequest req) {
    // nothing to do
    return req;
  }
  public void postProcess(SetRightRequest req) {
    // nothing to do
  }

  /**
   * Overrides this <code>DestinationImpl</code> method;
   * <code>ClientMessages</code> notifications hold requests sent by an
   * administrator.
   */
  public ClientMessages preProcess(AgentId from, ClientMessages msgs) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "AdminTopicImpl.clientMessages(" + msgs + ')');
    if (! msgs.getPersistent() && !msgs.getAsyncSend()) {
      // Means that this notification has been sent by a local
      // proxy (optimization). Must acknowledge it.
      forward(from, new SendReplyNot(msgs.getClientContext(), msgs.getRequestId()));
    }

    // ... and processing the wrapped requests locally:
    processAdminRequests(msgs);

    return null;
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; deletion requests are
   * not accepted by AdminTopics.
   */
  public void deleteNot(AgentId from, DeleteNot not) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + not);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>ClusterRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  public void clusterRequest(AgentId from, ClusterRequest request) throws AccessException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept to join clusters other than their admin topics cluster.
   */ 
  public void clusterTest(AgentId from, ClusterTest request) {
    forward(from, new ClusterAck(request, false,
                                 "Topic [" + getId() + "] is an admin topic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>ClusterAck</code>
   * is not expected by an AdminTopic.
   */ 
  public void clusterAck(AgentId from, ClusterAck ack) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; if this AdminTopic is on
   * server0, new cluster fellow is notified to other fellows and other
   * fellows are notified to it.
   */
  public void clusterNot(AgentId from, ClusterNot not) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected notification: " + not);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnclusterRequest</code> notifications.
   *
   * @exception AccessException  Not thrown.
   */ 
  public void unclusterRequest(AgentId from, UnclusterRequest request) throws MomException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>SetFatherRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  public void setFatherRequest(AgentId from, SetFatherRequest request)
  throws MomException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept to join a hierarchy.
   */ 
  public void fatherTest(AgentId from, FatherTest not) {
    forward(from, new FatherAck(not, false,
                                "Topic [" + getId()
                                + "] can't accept topic [" + from
                                + "] as a son as it is an AdminTopic"));
  }

  /**
   * Overrides this <code>TopicImpl</code> method; a <code>FatherAck</code>
   * acknowledges the process of creating a hierarchy of topics.
   */ 
  public void fatherAck(AgentId from, FatherAck ack) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>TopicImpl</code> method; AdminTopics do not
   * accept <code>UnsetFatherRequest</code> notifications.
   *
   * @exception  AccessException  Not thrown.
   */ 
  public void unsetFatherRequest(AgentId from, UnsetFatherRequest request) throws MomException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "Unexpected request: " + request);
  }


  /**
   * Overrides this <code>TopicImpl</code> method; the forwarded messages
   * contain admin requests and will be processed.
   */
  public void topicForwardNot(AgentId from, TopicForwardNot not) {
    processAdminRequests(not.messages);
  }

  /**
   * Specializes this <code>TopicImpl</code> reaction.
   */
  protected void doUnknownAgent(UnknownAgent uA) {
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
      super.doUnknownAgent(uA);
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

    if (not == null) return;

    Enumeration messages = not.getMessages().elements();

    while (messages.hasMoreElements()) {
      nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation + 1;
      msg = (Message) messages.nextElement();
      msgId = msg.id;
      replyTo = AgentId.fromString(msg.getReplyToId());
      request = null;

      try {
        request = (AdminRequest) msg.getAdminMessage();

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "--- " + this + ": got " + msg.getAdminMessage());
      } catch (ClassCastException exc) {
        logger.log(BasicLevel.ERROR,
                   "--- " + this + ": got bad AdminRequest");
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

      processAdminRequests(replyTo, msgId, request, null);
    }
  }

  /**
   * Method getting the administration requests from messages, and
   * distributing them to the appropriate reactions.
   */ 
  private void processAdminRequests(AgentId replyTo,
                                    String msgId,
                                    AdminRequest request,
                                    AgentId from) {
    String info = null;

    // state change, so save.
    setSave();

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
      else if (request instanceof SetNbMaxMsg)
        doProcess((SetNbMaxMsg) request, replyTo, msgId);
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
      else if (request instanceof GetDomainNames)
        doProcess((GetDomainNames) request, replyTo, msgId);
      else if (request instanceof GetLocalServer)
        doProcess((GetLocalServer) request, replyTo, msgId);
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
      else if (request instanceof Monitor_GetStat)
        doProcess((Monitor_GetStat) request, replyTo, msgId);
      else if (request instanceof Monitor_GetNbMaxMsg)
        doProcess((Monitor_GetNbMaxMsg) request, replyTo, msgId);
      else if (request instanceof Monitor_GetSubscriptions)
        doProcess((Monitor_GetSubscriptions) request, replyTo, msgId);
      else if (request instanceof SpecialAdmin)
        doProcess((SpecialAdmin) request, replyTo, msgId);
      else if (request instanceof AddServerRequest)
        doProcess((AddServerRequest) request, replyTo, msgId, from);
      else if (request instanceof AddDomainRequest)
        doProcess((AddDomainRequest) request, replyTo, msgId, from);
      else if (request instanceof RemoveServerRequest)
        doProcess((RemoveServerRequest) request, replyTo, msgId, from);
      else if (request instanceof RemoveDomainRequest)
        doProcess((RemoveDomainRequest) request, replyTo, msgId, from);
      else if (request instanceof GetConfigRequest)
        doProcess((GetConfigRequest) request, replyTo, msgId);
      else if (request instanceof UserAdminRequest)
        doProcess((UserAdminRequest) request, replyTo, msgId);
      else if (request instanceof GetSubscriberIds)
        doProcess((GetSubscriberIds) request, replyTo, msgId);
      else if (request instanceof QueueAdminRequest)
        doProcess((QueueAdminRequest) request, replyTo, msgId);
    } catch (UnknownServerException exc) {
      // Caught when a target server is invalid.
      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], successful [false]: ")
      .append(exc.getMessage()).toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(false, info));
    } catch (MomException exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

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
      AgentServer.stop(false, 500L, true);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      // The destination is local, process the request.

      DestinationDesc destDesc = createDestination(
                                                   request.getDestinationName(),
                                                   null,
                                                   request.getProperties(),
                                                   request.getExpectedType(),
                                                   request.getClassName(),
                                                   request.getClass().getName(),
                                                   strbuf);

      distributeReply(
                      replyTo,
                      msgId,
                      new CreateDestinationReply(
                                                 destDesc.getId().toString(), 
                                                 destDesc.getName(),
                                                 destDesc.getType(),
                                                 strbuf.toString()));
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, strbuf.toString());
      strbuf.setLength(0);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Instantiating the destination class or retrieving the destination.
   * 
   * @param destName           destination Name
   * @param adminId            other admin (null for TopicAdmin)
   * @param properties         destination properties
   * @param type               destination type ("queue" or "topic")
   * @param className          creates an instance of the class
   * @param requestClassName  
   * @param strbuf             information
   * @return DestinationDesc   contain destination description
   * @throws UnknownServerException
   * @throws RequestException
   */
  public DestinationDesc createDestination(
                                           String destName,
                                           AgentId adminId,
                                           Properties properties,
                                           String type,
                                           String className,
                                           String requestClassName,
                                           StringBuffer strbuf)
  throws UnknownServerException, RequestException {

    boolean destNameActivated = (destName != null && ! destName.equals(""));
    DestinationDesc destDesc;
    Agent dest = null;

    // Retrieving an existing destination:
    if (destNameActivated && destinationsTable.containsKey(destName)) {
      destDesc = (DestinationDesc) destinationsTable.get(destName);
      if (! destDesc.isAssignableTo(type)) {
        throw new RequestException("Destination type not compliant");
      }
      strbuf.append("Request [").append(requestClassName)
      .append("], processed by AdminTopic on server [").append(serverId)
      .append("], successful [true]: destination [")
      .append(destName).append("] has been retrieved");
    } else {
      // Instantiating the destination class.
      Class clazz;
      String destType;
      try {
        clazz = Class.forName(className);
        dest = (Agent) clazz.newInstance();
        if (destName != null) {
          dest.name = destName;
        }

        if (adminId == null)
          adminId = getId();
        ((AdminDestinationItf) dest).init(adminId, properties);

        Method getTypeM = clazz.getMethod("getDestinationType", new Class[0]);
        destType = (String)getTypeM.invoke(null, new Object[0]);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR,
                   "Could not instantiate Destination class [" + className + "]: ", exc);
        if (exc instanceof ClassCastException) {
          throw new RequestException("Class [" + className + "] is not a Destination class.");
        } else {
          throw new RequestException("Could not instantiate Destination class [" + className + "]: " + exc);
        }
      }

      AgentId createdDestId = dest.getId();

      if (! destNameActivated)
        destName = createdDestId.toString();

      destDesc = new DestinationDesc(createdDestId, destName, 
                                     className, destType);
      if (! destDesc.isAssignableTo(type)) {
        throw new RequestException("Destination type not compliant");
      }

      try {
        dest.deploy();
        destinationsTable.put(destName, destDesc);

        strbuf.append("Request [").append(requestClassName)
        .append("], processed by AdminTopic on server [").append(serverId)
        .append("], successful [true]: ").append(className).append(" [")
        .append(createdDestId.toString()).append("] has been created and deployed");

      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "xxx", exc);
        throw new RequestException("Error while deploying Destination [" + 
                                   clazz + "]: " + exc);
      }
    }
    return destDesc;
  }

  /**
   * Instantiating the destination class or retrieving the destination
   * and save Agent AdminTopic. (used by ScalAgent mediation)
   * 
   * @param destName           destination Name
   * @param adminId            other admin (null for TopicAdmin)
   * @param properties         destination properties
   * @param type               destination type ("queue" or "topic")
   * @param className          creates an instance of the class
   * @param requestClassName  
   * @param strbuf             information
   * @return DestinationDesc   contain destination description
   * @throws UnknownServerException
   * @throws RequestException
   * @throws IOException  transaction exception.
   */
  public static DestinationDesc createDestinationAndSave(
                                                         String destName,
                                                         AgentId adminId,
                                                         Properties properties,
                                                         String type,
                                                         String className,
                                                         String requestClassName,
                                                         StringBuffer strbuf)
  throws UnknownServerException, RequestException, IOException {
    // create destination.
    DestinationDesc destDesc = ref.createDestination(
                                                     destName,
                                                     adminId,
                                                     properties,
                                                     type,
                                                     className,
                                                     requestClassName,
                                                     strbuf);
    // save Agent AdminTopic
    AgentServer.getTransaction().save(ref.agent, ref.getId().toString()); 
    return destDesc;
  }

  /**
   * is destinationTable contain destName ?
   * (used by ScalAgent mediation)
   * 
   * @param destName destination name.
   * @return true if contain.
   */
  public static boolean isDestinationTableContain(String destName) {
    return ref.destinationsTable.containsKey(destName);
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

      Enumeration destinations = destinationsTable.elements();
      while (destinations.hasMoreElements()) {
        DestinationDesc destDesc = 
          (DestinationDesc)destinations.nextElement();
        if (destDesc.getId().equals(destId)) {
          destinationsTable.remove(destDesc.getName());
          break;
        }
      }

      forward(destId, new DeleteNot());

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to AdminTopic on server [").append(serverId)
      .append("], successful [true]: destination [").append(destId)
      .append("], successfuly notified for deletion").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(initId, new ClusterRequest(msgId, topId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(initId.getTo()),
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
      forward(topId, new UnclusterRequest(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(topId.getTo()),
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
      forward(sonId, new SetFatherRequest(msgId, fatherId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(sonId.getTo()),
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
      forward(topId, new UnsetFatherRequest(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(topId.getTo()),
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
  public void doProcess(CreateUserRequest request,
                        AgentId replyTo,
                        String msgId)
  throws UnknownServerException, RequestException {
    if (checkServerId(request.getServerId())) {
      // If this server is the target server, process the request.
      Identity identity = request.getIdentity();
      String name = identity.getUserName();

      AgentId proxId = (AgentId) proxiesTable.get(name);
      String info;

      // The user has already been set. 
      if (proxId != null) {
        Identity userIdentity = (Identity) usersTable.get(name);
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, "User [" + name + "] already exists : " + userIdentity);
        try {
          if (! identity.check(userIdentity)) {
            throw new RequestException("User [" + name + "] already exists"
                                       + " but with a different password.");
          } 
        } catch (Exception e) {
          throw new RequestException("User [" + name + "] already exists :: Exception :" + e.getMessage());
        }
        info = strbuf.append("Request [").append(request.getClass().getName())
        .append("], processed by AdminTopic on server [").append(serverId)
        .append("], successful [true]: proxy [").append(proxId.toString())
        .append("] of user [").append(name)
        .append("] has been retrieved").toString();
        strbuf.setLength(0);
      } else {
        //        try {
        //          if (! identity.validate()) {
        //            throw new RequestException("User [" + name + "] security validate failed.");
        //          }
        //        } catch (Exception e) {
        //          throw new RequestException(e.getMessage());
        //        }

        UserAgent proxy = new UserAgent();
        if (name != null) {
          proxy.name = name;
        }
        proxId = proxy.getId();

        try {
          proxy.deploy();
          usersTable.put(name, identity);
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

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);

      distributeReply(replyTo,
                      msgId,
                      new CreateUserReply(proxId.toString(), info));
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }


  /**
   * Processes a <code>CreateUserRequest</code> instance requesting the
   * creation of a <code>UserAgent</code> for a given user and save Agent
   * AdminTopic. (used by ScalAgent mediation)
   *
   * @exception UnknownServerException  If the target server does not exist.
   * @exception RequestException  If the user already exists but with a
   *              different password, or if the proxy deployment failed.
   * @throws IOException transaction exception
   */
  public static void CreateUserAndSave(
                                       CreateUserRequest request,
                                       AgentId replyTo,
                                       String msgId)
  throws UnknownServerException, RequestException, IOException {
    ref.doProcess(request, replyTo, msgId);
    //  save Agent AdminTopic
    AgentServer.getTransaction().save(ref.agent, ref.getId().toString()); 
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
  throws RequestException, UnknownServerException {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    if (checkServerId(proxId.getTo())) {
      // If the user belong to this server, process the request.
      String info;

      // If the user does not exist: throwing an exception:
      if (! usersTable.containsKey(name))
        throw new RequestException("User [" + name + "] does not exist");

      Identity newIdentity = request.getNewIdentity();
      // If the new name is already taken by an other user than the modified
      // one:
      if (! newIdentity.getUserName().equals(name)
          && (usersTable.containsKey(newIdentity.getUserName())))
        throw new RequestException("Name [" + newIdentity.getUserName() + "] already used");

      if (usersTable.containsKey(name)) {
        usersTable.remove(name);
        proxiesTable.remove(name);
        usersTable.put(newIdentity.getUserName(), newIdentity);
        proxiesTable.put(newIdentity.getUserName(), proxId);
      }

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], processed by AdminTopic on server [").append(serverId)
      .append("], successful [true]: user [").append(name)
      .append("] has been updated to [").append(newIdentity.getUserName()).
      append("]").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(proxId.getTo()),
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
        forward(proxId, new DeleteNot());
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

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(proxId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
                         }

  /**
   * Processes a <code>SetRight</code> instance requesting to grant a user
   * a given right on a given destination.
   */
  public void doProcess(SetRight request,
                        AgentId replyTo,
                        String msgId) throws UnknownServerException {
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

      forward(destId, new SetRightRequest(msgId, userId, right));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes a <code>SetRight</code> instance requesting to grant a user
   * a given right on a given destination. And save Agent TopicAdmin.
   * (used by ScalAgent mediation)
   *
   * @param request
   * @param replyTo
   * @param msgId
   * @throws UnknownServerException
   * @throws IOException
   */
  public static void setRightAndSave(
                                     SetRight request,
                                     AgentId replyTo,
                                     String msgId) throws UnknownServerException, IOException {
    ref.doProcess(request, replyTo, msgId);
    // save Agent AdminTopic
    AgentServer.getTransaction().save(ref.agent, ref.getId().toString()); 
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

      QueueImpl.defaultDMQId = dmqId;

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to AdminTopic on server [").append(serverId)
      .append("], successful [true]: dmq [").append(dmqId.toString())
      .append("], has been successfuly set as the default one").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      forward(destId, new SetDMQRequest(msgId, dmqId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(userId, new SetDMQRequest(msgId, dmqId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(userId.getTo()),
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

      QueueImpl.defaultThreshold = new Integer(request.getThreshold());

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to AdminTopic on server [").append(serverId)
      .append("], successful [true]: default threshold [")
      .append(request.getThreshold()).append("] has been set").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info)); 

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
              new AdminRequestNot(replyTo, msgId, request));
    }
                         }

  /**
   * Processes a <code>SetNbMaxMsg</code> request requesting
   * a given nbMaxMsg value to be set in queue or subscription.
   */
  private void doProcess(SetNbMaxMsg request,
                         AgentId replyTo,
                         String msgId) 
  throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getId());

    if (checkServerId(destId.getTo())) {
      // The destination is not local, doing nothing.
      int nbMaxMsg = request.getNbMaxMsg();
      String subName = request.getSubName();
      forward(destId, new SetNbMaxMsgRequest(msgId, nbMaxMsg, subName));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(destId, new SetThreshRequest(msgId, new Integer(thresh)));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(userId, new SetThreshRequest(msgId, new Integer(thresh)));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(userId.getTo()),
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

      QueueImpl.defaultDMQId = null;

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to AdminTopic on server [").append(serverId)
      .append("], successful [true]: default dmq has been unset").toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info)); 

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, info);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      forward(destId, new SetDMQRequest(msgId, null));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(userId, new SetDMQRequest(msgId, null));
      if (replyTo != null)requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(userId.getTo()),
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

      QueueImpl.defaultThreshold = null;

      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to AdminTopic on server [").append(serverId)
      .append("], successful [true]: default threshold has been unset")
      .toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(true, info)); 

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
        "Default threshold unset.");
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      forward(destId, new SetThreshRequest(msgId, null));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(userId, new SetThreshRequest(msgId, null));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(userId.getTo()),
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
                         String msgId) throws UnknownServerException {
    if (checkServerId(request.getServerId())) {
      try {
        int[] ids;
        String[] names;
        String[] hostNames;
        String domainName = request.getDomainName();
        Enumeration servers;
        A3CMLConfig config = AgentServer.getConfig();
        int serversCount;
        if (domainName != null) {
          A3CMLDomain domain = config.getDomain(domainName);
          servers = domain.servers.elements();
          serversCount = domain.servers.size();
        } else {
          servers = config.servers.elements();
          serversCount = config.servers.size();
        }
        ids = new int[serversCount];
        names = new String[serversCount];
        hostNames = new String[serversCount];
        int i = 0;
        while (servers.hasMoreElements()) {
          A3CMLServer server = (A3CMLServer)servers.nextElement();
          ids[i] = server.sid;
          names[i] = server.name;
          hostNames[i] = server.hostname;
          i++;
        }
        Monitor_GetServersIdsRep reply = new Monitor_GetServersIdsRep(
                                                                      ids, names, hostNames);
        distributeReply(replyTo, msgId, reply);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        distributeReply(replyTo, msgId,
                        new AdminReply(false, exc.toString()));
      }
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }

  private void doProcess(GetLocalServer request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    try {
      A3CMLConfig config = AgentServer.getConfig();
      A3CMLServer a3cmlServer = config.getServer(AgentServer.getServerId());
      distributeReply(replyTo, msgId,
                      new GetLocalServerRep(a3cmlServer.sid,
                                            a3cmlServer.name,
                                            a3cmlServer.hostname));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(GetDomainNames request,
                         AgentId replyTo,
                         String msgId) {
    try {
      A3CMLConfig config = AgentServer.getConfig();
      A3CMLServer server = config.getServer((short)request.getServerId());
      String[] domainNames = new String[server.networks.size()];
      for (int i = 0; i < server.networks.size(); i++) {
        A3CMLNetwork nw = (A3CMLNetwork)server.networks.elementAt(i);
        domainNames[i] = nw.domain;
      }
      GetDomainNamesRep reply = new GetDomainNamesRep(domainNames);
      distributeReply(replyTo, msgId, reply);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
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
      Enumeration destinations = destinationsTable.elements();
      String[] ids = new String[destinationsTable.size()];
      String[] names = new String[destinationsTable.size()];
      String[] types = new String[destinationsTable.size()];
      int i = 0;
      while (destinations.hasMoreElements()) {
        DestinationDesc destDesc = 
          (DestinationDesc)destinations.nextElement();
        ids[i] = destDesc.getId().toString();
        names[i] = destDesc.getName();
        types[i] = destDesc.getType();
        i++;
      }
      Monitor_GetDestinationsRep reply = 
        new Monitor_GetDestinationsRep(ids, names, types);
      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      forward(AdminTopic.getDefault((short) request.getServerId()),
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
      forward(destId, new Monit_GetReaders(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(destId, new Monit_GetWriters(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(destId, new Monit_FreeAccess(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
        if (QueueImpl.defaultDMQId != null)
          id = QueueImpl.defaultDMQId.toString();
        reply = new Monitor_GetDMQSettingsRep(id, QueueImpl.defaultThreshold);
        distributeReply(replyTo, msgId, reply);
      } else {
        // Forward the request to the right AdminTopic agent.
        forward(AdminTopic.getDefault((short) request.getServerId()),
                new AdminRequestNot(replyTo, msgId, request));
      }
    } else {
      if (request.getTarget() != null) {
        AgentId targetId = AgentId.fromString(request.getTarget());

        if (checkServerId(targetId.getTo())) {
          forward(targetId, new Monit_GetDMQSettings(msgId));

          if (replyTo != null)
            requestsTable.put(msgId, replyTo);
        } else {
          // Forward the request to the right AdminTopic agent.
          forward(AdminTopic.getDefault(targetId.getTo()),
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
      forward(topicId, new Monit_GetFather(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(topicId.getTo()),
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
      forward(topicId, new Monit_GetCluster(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(topicId.getTo()),
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
      forward(destId, new Monit_GetPendingMessages(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(destId, new Monit_GetPendingRequests(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
                         }

  /**
   * Processes a <code>Monitor_GetStat</code> request by
   * forwarding it to its target destination, if local.
   */
  private void doProcess(Monitor_GetStat request,
                         AgentId replyTo,
                         String msgId) 
  throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDest());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      forward(destId, new Monit_GetStat(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }

  /**
   * Processes an <code>Monitor_GetNbMaxMsg</code> request requesting
   * to get the nb max msg.
   */
  private void doProcess(Monitor_GetNbMaxMsg request,
                         AgentId replyTo,
                         String msgId)
  throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getId());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      String subName = request.getSubName();
      forward(destId, new Monit_GetNbMaxMsg(msgId, subName));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
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
      forward(destId, new Monit_GetSubscriptions(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
                         }

  private void doProcess(SpecialAdmin request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "AdminTopicImpl.doProcess " +
                   "SpecialAdminRequest destId=" + destId);

      if (getId().equals(destId)) {
        // If this destination is the target destination, doing nothing:
        distributeReply(replyTo, msgId,
                        new AdminReply(false, "destId mustn't be TopicAdmin."));
        return;
      }

      forward(destId, new SpecialAdminRequest(msgId,request));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(destId.getTo()),
              new AdminRequestNot(replyTo, msgId, request));
    }
  }

  private void doProcess(AddDomainRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(true);
      if (helper.addDomain(request.getDomainName(), request.getNetwork(), request.getServerId(), request.getPort())) {
        distributeReply(replyTo, msgId, new AdminReply(true, "Domain added"));
      }
      if (from == null)
        broadcastRequest(request, -1, replyTo, msgId);
    } catch (ServerConfigHelper.NameAlreadyUsedException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, AdminReply.NAME_ALREADY_USED, exc.getMessage(), null));
    } catch (ServerConfigHelper.StartFailureException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, AdminReply.START_FAILURE, exc.getMessage(), null));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId, new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(RemoveDomainRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(true);
      if (helper.removeDomain(request.getDomainName())) {
        distributeReply(replyTo, msgId,
                        new AdminReply(true, "Domain removed"));
      }
      if (from == null) {
        broadcastRequest(request, 
                         -1, 
                         replyTo, msgId);
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(AddServerRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(false);
      helper.addServer(
                       request.getServerId(),
                       request.getHostName(),
                       request.getDomainName(),
                       request.getPort(),
                       request.getServerName());
      helper.addService(
                        request.getServerId(),
                        "org.objectweb.joram.mom.proxies.ConnectionManager",
      "root root");
      String[] serviceNames = request.getServiceNames();
      String[] serviceArgs = request.getServiceArgs();
      for (int i = 0; i < serviceNames.length; i++) {
        helper.addService(request.getServerId(), 
                          serviceNames[i], 
                          serviceArgs[i]);
      }
      helper.commit();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Server added"));
      if (from == null) {
        broadcastRequest(request, 
                         request.getServerId(), 
                         replyTo, msgId);
      }
    } catch (ServerConfigHelper.ServerIdAlreadyUsedException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(
                                     false, 
                                     AdminReply.SERVER_ID_ALREADY_USED, 
                                     exc.getMessage(), null));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(RemoveServerRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(true);
      helper.removeServer(request.getServerId());
      distributeReply(replyTo, msgId,
                      new AdminReply(true, "Server removed"));
      if (from == null) {
        broadcastRequest(request, 
                         request.getServerId(),
                         replyTo, msgId);
      }
    } catch (UnknownServerException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(
                                     false, 
                                     AdminReply.UNKNOWN_SERVER, 
                                     exc.getMessage(), null));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void broadcastRequest(AdminRequest req,
                                int avoidServerId,
                                AgentId replyTo,
                                String msgId) {
    AdminRequestNot not = new AdminRequestNot(replyTo, msgId, req);
    Enumeration ids = AgentServer.getServersIds();
    while (ids.hasMoreElements()) {
      short id = ((Short) ids.nextElement()).shortValue();
      if (id != AgentServer.getServerId() &&
          id != avoidServerId) {
        forward(AdminTopic.getDefault(id), not);
      }
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
      out.flush();
      baos.flush();
      baos.close();
      String config = baos.toString();
      distributeReply(replyTo, msgId,
                      new AdminReply(true, config));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(UserAdminRequest request,
                         AgentId replyTo,
                         String requestMsgId) 
  throws UnknownServerException {
    AgentId userId = AgentId.fromString(request.getUserId());
    if (checkServerId(userId.getTo())) {
      // Delegate to the proxy
      forward(userId, new UserAdminRequestNot(
                                              request, replyTo, requestMsgId, createMessageId()));
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(AdminTopic.getDefault(userId.getTo()),
              new AdminRequestNot(
                                  replyTo, requestMsgId, request));
    }
  }

  private void doProcess(GetSubscriberIds request,
                         AgentId replyTo,
                         String requestMsgId) 
  throws UnknownServerException {
    try {
      AgentId topicId = AgentId.fromString(request.getTopicId());
      forward(topicId, new DestinationAdminRequestNot(
                                                      request, replyTo, requestMsgId, createMessageId()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, requestMsgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  private void doProcess(QueueAdminRequest request,
                         AgentId replyTo,
                         String requestMsgId) 
  throws UnknownServerException {
    try {
      AgentId queueId = AgentId.fromString(request.getQueueId());
      forward(queueId, new DestinationAdminRequestNot(
                                                      request, replyTo, requestMsgId, createMessageId()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, requestMsgId,
                      new AdminReply(false, exc.toString()));
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

  private String createMessageId() {
    msgCounter++;
    return "ID:" + getId().toString() + '_' + msgCounter;
  }

  /** 
   * Actually sends an <code>AdminReply</code> object to an identified
   * destination.
   *
   * @param to  Identifier of a destination to send the reply to.
   * @param msgId  Identifier of the original request.
   * @param reply  The <code>AdminReply</code> instance to send.
   */
  private void distributeReply(AgentId to, String msgId, AdminReply reply) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
                 BasicLevel.DEBUG,
                 "AdminTopicImpl.distributeReply(" + 
                 to + ',' + msgId + ',' + reply + ')');

    if (to == null)
      return;

    Message message = new Message();
    message.id = createMessageId();
    message.correlationId = msgId;
    message.timestamp = System.currentTimeMillis();
    message.setDestination(getId().toString(), Topic.TOPIC_TYPE);
    try {
      message.setAdminMessage(reply);
      ClientMessages clientMessages = new ClientMessages(-1, -1, message);
      forward(to, clientMessages);
      nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation + 1;
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "", exc);
    }
  }

  /** Serializes an <code>AdminTopicImpl</code> instance. */
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    // Saves DMQ defaults.
    out.writeObject(QueueImpl.defaultDMQId);
    out.writeObject(QueueImpl.defaultThreshold);

    out.defaultWriteObject();
  }

  /** Deserializes an <code>AdminTopicImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
  throws java.io.IOException, ClassNotFoundException {
    QueueImpl.defaultDMQId = (AgentId) in.readObject();
    QueueImpl.defaultThreshold = (Integer) in.readObject();
    in.defaultReadObject();
    ref = this;
  }

  static class AdminRequestNot extends Notification {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
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

  public static class DestinationDesc implements java.io.Serializable {
    /** define serialVersionUID for interoperability */
    private static final long serialVersionUID = 1L;
    private AgentId id;
    private String name;
    private String className;
    private String type;

    public DestinationDesc(AgentId id,
                           String name,
                           String className,
                           String type) {
      this.id = id;
      this.name = name;
      this.className = className.intern();
      this.type = type.intern();
    }

    public final AgentId getId() {
      return id;
    }

    public final String getName() {
      return name;
    }

    public final String getClassName() {
      return className;
    }

    public final String getType() {
      return type;
    }

    public boolean isAssignableTo(String assignedType) {
      return type.startsWith(assignedType);
    }

    public String toString() {
      return '(' + super.toString() +
      ",id=" + id +
      ",name=" + name + 
      ",className=" + className + 
      ",type=" + type + ')';
    }
  }
}
