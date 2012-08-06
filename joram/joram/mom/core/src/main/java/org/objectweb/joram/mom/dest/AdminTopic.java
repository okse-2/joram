/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.AdminReplyNot;
import org.objectweb.joram.mom.notifications.AdminRequestNot;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ClusterJoinAck;
import org.objectweb.joram.mom.notifications.ClusterJoinNot;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.GetProxyIdListNot;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.notifications.GetRightsReplyNot;
import org.objectweb.joram.mom.notifications.GetRightsRequestNot;
import org.objectweb.joram.mom.notifications.RequestGroupNot;
import org.objectweb.joram.mom.notifications.TopicForwardNot;
import org.objectweb.joram.mom.proxies.AdminNotification;
import org.objectweb.joram.mom.proxies.SendReplyNot;
import org.objectweb.joram.mom.proxies.UserAgent;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminCommandRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.DestinationAdminRequest;
import org.objectweb.joram.shared.admin.GetConfigRequest;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetDestinationsReply;
import org.objectweb.joram.shared.admin.GetDestinationsRequest;
import org.objectweb.joram.shared.admin.GetDomainNames;
import org.objectweb.joram.shared.admin.GetDomainNamesRep;
import org.objectweb.joram.shared.admin.GetLocalServer;
import org.objectweb.joram.shared.admin.GetLocalServerRep;
import org.objectweb.joram.shared.admin.GetRightsReply;
import org.objectweb.joram.shared.admin.GetRightsRequest;
import org.objectweb.joram.shared.admin.GetServersIdsReply;
import org.objectweb.joram.shared.admin.GetServersIdsRequest;
import org.objectweb.joram.shared.admin.GetStatsReply;
import org.objectweb.joram.shared.admin.GetStatsRequest;
import org.objectweb.joram.shared.admin.GetUsersReply;
import org.objectweb.joram.shared.admin.GetUsersRequest;
import org.objectweb.joram.shared.admin.RemoveDomainRequest;
import org.objectweb.joram.shared.admin.RemoveServerRequest;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetRight;
import org.objectweb.joram.shared.admin.SetThresholdRequest;
import org.objectweb.joram.shared.admin.StopServerRequest;
import org.objectweb.joram.shared.admin.UpdateUser;
import org.objectweb.joram.shared.admin.UserAdminRequest;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.messages.MessageHelper;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
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
 * The <code>AdminTopic</code> class implements the administration topic
 * behavior, basically processing administration requests.
 * <p>
 * It receives administration requests from the client encapsulated in JMS
 * messages:
 * <ul>
 * <li>If the request concerns an user or a destination it forwards the request
 * to the target. This target directly replies (*) to the client sending a JMS
 * message to the replyTo destination specified in the original JMS message.</li>
 * <li>If the request can be processed by the administration topic, either the
 * request concerns the local server and it is processed, or it is forwarded to
 * the administration topic of the target server.</li>
 * </ul>
 * (*) Currently the getRights reply is handled differently as it needs the
 * transformation of the user list. This behavior should disappear with the role
 * based mechanism.
 */
public final class AdminTopic extends Topic implements AdminTopicMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Reference of the server's local AdminTopic instance. */
  private static AdminTopic ref;

  /** Identifier of the server this topic is deployed on. */
  private int serverId;

  /**
   * Table holding the local server's destinations names.
   * This table is used by more than one thread so it needs to be synchronized.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> destination agent identifier
   */
  private Map destinationsTable = new Hashtable();

  /**
   * Table holding the TCP users identifications.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Object:</b> user password
   */
  private Map usersTable = new Hashtable();

  /**
   * Table holding the TCP users proxies identifiers.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Object:</b> proxy's identifier
   */
  private Map proxiesTable = new Hashtable();

  /**
   * Table keeping the administrator's requests.
   * <p>
   * <b>Key:</b> request's message identifier<br>
   * <b>Value:</b> request's message ReplyTo field
   */
  private Map requestsTable = new Hashtable();

  /** Counter of messages produced by this AdminTopic. */
  private long msgCounter = 0;

  /**
   * Constructs an <code>AdminTopic</code> agent.
   */
  public AdminTopic() throws RequestException {
    super("JoramAdminTopic", true, AgentId.JoramAdminStamp);
    setAdminId(getId());
    serverId = AgentServer.getServerId();
  }

  public String toString() {
    return "AdminTopic";
  }

  /**
   * Gets the identifier of the default administration topic on a given server.
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.JoramAdminStamp);
  }

  static AgentId adminId = null;

  /**
   * Gets the identifier of the default administration topic on the
   * current server.
   */
  public final static AgentId getDefault() {
    if (adminId == null)
      adminId = new AgentId(AgentServer.getServerId(), AgentServer.getServerId(), AgentId.JoramAdminStamp);
    return adminId;
  }

  /**
   * Returns true if the given AgentId is the unique identifier of an AdminTopic
   * agent.
   * 
   * @param id the AgentId to verify.
   * @return true if the given AgentId is the unique identifier of an AdminTopic
   *         agent.
   */
  public final static boolean isAdminTopicId(AgentId id) {
    if (id == null)
      return false;
    return id.getStamp() == AgentId.JoramAdminStamp;
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": got " + not + " from: " + from.toString());

    // state change, so save.
    setSave();

    if (not instanceof AdminNotification) {
      // This notification is used at boot by an administrator proxy to declare
      // its identification.
      handleAdminNotification((AdminNotification) not);
    } else if (not instanceof FwdAdminRequestNot) {
      // This notification contains forwarded administration request from remote
      // AdminTopic. This code overloads the normal behavior in Destination.

      // AF (TODO): May be we should verify that from is an AdminTopic but in the
      // AgentServer sandbox only an AdminTopic can generate such a notification.
      processAdminRequests(((FwdAdminRequestNot) not).getReplyTo(),
          ((FwdAdminRequestNot) not).getRequestMsgId(), ((FwdAdminRequestNot) not).getRequest(), from);
    } else if (not instanceof AdminReplyNot) {
      // Now most of the replies are sent directly to the waiting destination. Only
      // the GetRightsReplyNot requires a processing, in future it will be removed.
      handleAdminReply((AdminReplyNot) not);
    } else if (not instanceof GetProxyIdNot) {
      // This notification (SyncNotification) is used during connection to get the
      // AgentId of the user's proxy.
      handleGetProxyIdNot((GetProxyIdNot) not);
    } else if (not instanceof GetProxyIdListNot) {
      // This notification is needed by the HA mode.
      // AF (TODO): remove it.
      handleGetProxyIdListNot((GetProxyIdListNot) not);
    } else {
      super.react(from, not);
    }
  }


  /**
   * Method used by <code>ConnectionManager</code> proxies to check their
   * clients identification.
   *
   * @param identity
   * @param inaddr
   * @return The agent unique identifier of the authentified user.
   * 
   * @exception Exception  If the user does not exist, is wrongly identified,
   *              or does not have any proxy deployed.
   * @see org.objectweb.joram.mom.proxies.ConnectionManager
   */
  private AgentId getProxyId(Identity identity,
                            String inaddr) throws Exception {

    AgentId userProxId = null;
    Identity userIdentity = (Identity) usersTable.get(identity.getUserName());
    if (userIdentity == null) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "User [" + identity.getUserName() + "] does not exist");
      throw new Exception("User [" + identity.getUserName() + "] does not exist");
    }

    if (!userIdentity.check(identity)) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "identity check failed.");
      throw new Exception("identity check failed.");
    }

    userProxId = (AgentId) proxiesTable.get(identity.getUserName());
    if (userProxId == null) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "No proxy deployed for user [" + identity.getUserName() + "]");
      throw new Exception("No proxy deployed for user [" + identity.getUserName() + "]");
    }

    return userProxId;
  }

  /**
   * Method used to send a response when a message is denied because of a lack
   * of rights.
   */
  protected void handleDeniedMessage(String msgId, AgentId replyTo) {
    distributeReply(replyTo, msgId, new AdminReply(AdminReply.PERMISSION_DENIED, "Permission denied."));
  }

  /**
   * Method implementing the reaction to a <code>AdminNotification</code>
   * notification notifying of the creation of an administrator proxy.
   * 
   * @param adminNot  the <code>AdminNotification</code> notification.
   */
  private void handleAdminNotification(AdminNotification adminNot) {
    Identity identity = adminNot.getIdentity();

    try {
      //identity.validate();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "AdminTopic store identity = " + identity);
      
      usersTable.put(identity.getUserName(), identity);
      proxiesTable.put(identity.getUserName(), adminNot.getProxyId());

      clients.put(adminNot.getProxyId(), new Integer(READWRITE));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   identity.getUserName() + " successfully set as admin client.");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Exception:: ", e);
    }
  }

  /**
   * Method implementing the reaction to a
   * <code>org.objectweb.joram.mom.notifications.AdminReply</code>
   * notification replying to an administration request.
   * <p>
   * A reply is sent back to the connected administrator if needed.
   */  
  private void handleAdminReply(AdminReplyNot not) {
    String requestId = not.getRequestId();
    if (requestId == null) return;

    AgentId replyTo = (AgentId) requestsTable.remove(requestId);
    if (replyTo == null) return;

    AdminReply reply;

    if (not instanceof GetRightsReplyNot)
      reply = doProcess((GetRightsReplyNot) not);
    else
      reply = new AdminReply(not.getSuccess(), not.getInfo());

    distributeReply(replyTo, requestId, reply);
  }

  private void handleGetProxyIdNot(GetProxyIdNot not) {
    try {
      AgentId proxyId = getProxyId(not.getIdentity(), not.getInAddr());
      not.Return(proxyId);
    } catch (Exception exc) {
      not.Throw(exc);
    }
  }

  private void handleGetProxyIdListNot(GetProxyIdListNot not) {
    List idList = new ArrayList();
    Iterator ids = proxiesTable.values().iterator();
    while (ids.hasNext()) {
      AgentId aid = (AgentId) ids.next();
      idList.add(aid);
    }
    AgentId[] res = (AgentId[]) idList.toArray(new AgentId[idList.size()]);
    not.Return(res);
  }

  /**
   * Processes a <code>Monit_GetUsersRep</code> notification holding a
   * destination's readers' or writers' identifiers.
   */
  private AdminReply doProcess(GetRightsReplyNot not) {
    Vector readers = not.getReaders();
    Vector writers = not.getWriters();

    String name;
    AgentId proxyId;
    GetRightsReply reply = new GetRightsReply(not.getSuccess(), not.getInfo(), not.isFreeReading(), not.isFreeWriting());

    for (Iterator names = proxiesTable.keySet().iterator(); names.hasNext();) {
      name = (String) names.next();
      proxyId = (AgentId) proxiesTable.get(name);

      if (readers.contains(proxyId))
        reply.addReader(name, proxyId.toString());
      if (writers.contains(proxyId))
        reply.addWriter(name, proxyId.toString());
    }
    return reply;
  }

  protected void requestGroupNot(AgentId from, RequestGroupNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminTopic.requestGroupNot(" + not + ')');
    Enumeration en = not.getClientMessages();
    while (en.hasMoreElements()) {
      ClientMessages cm = (ClientMessages) en.nextElement();
      try {
        clientMessages(from, cm);
      } catch (Exception exc) {
      }
    }
  }

  /**
   * Overrides this {@link Destination} method;
   * 
   * @param msgs <code>ClientMessages</code> notifications hold requests sent by
   *          an administrator.
   */
  protected ClientMessages preProcess(AgentId from, ClientMessages msgs) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminTopic.clientMessages(" + msgs + ')');
    if (!msgs.isPersistent() && !msgs.getAsyncSend()) {
      // Means that this notification has been sent by a local
      // proxy (optimization). Must acknowledge it.
      forward(from, new SendReplyNot(msgs.getClientContext(), msgs.getRequestId()));
    }

    // ... and processing the wrapped requests locally:
    processAdminRequests(msgs);

    return null;
  }

  /* ***** ***** ***** ***** *****
   * Overrides 'cluster' and 'tree' default behaviors from Topic:
   * - AdminTopics are part of an implicit cluster.
   * - AdminTopics do not accept to join a hierarchy.
   * ***** ***** ***** ***** ***** */

  /**
   * Overrides this <code>Topic</code> method; a <code>ClusterAck</code> is not
   * expected by an AdminTopic.
   */ 
  protected void clusterJoinAck(ClusterJoinAck ack) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected notification: " + ack);
  }

  /**
   * Overrides this <code>Topic</code> method; if this AdminTopic is on
   * server0, new cluster fellow is notified to other fellows and other
   * fellows are notified to it.
   */
  protected void clusterJoin(ClusterJoinNot not) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected notification: " + not);
  }

  /**
   * Overrides this <code>Topic</code> method; the forwarded messages
   * contain admin requests and will be processed.
   */
  protected void topicForwardNot(AgentId from, TopicForwardNot not) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected notification: " + not);
  }

  /* ***** ***** ***** ***** *****
   * ***** ***** ***** ***** ***** */

  /**
   * Overrides this {@link Destination} method.
   * Deletion requests are not accepted by AdminTopics.
   */
  public void deleteNot(AgentId from, DeleteNot not) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected request: " + not);
  }

  /**
   * Specializes this <code>Topic</code> reaction.
   */
  protected void doUnknownAgent(UnknownAgent uA) {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    // For admin requests, notifying the administrator.
    if (not instanceof AdminRequestNot) {
      String reqId = ((AdminRequestNot) not).getId();

      if (reqId != null) {
        AgentId replyTo = (AgentId) requestsTable.remove(reqId);

        strbuf.append("Request [").append(not.getClass().getName());
        strbuf.append("], sent to AdminTopic on server [").append(serverId);
        strbuf.append("], successful [false]: unknown agent [").append(agId).append(']');

        distributeReply(replyTo, reqId, new AdminReply(false, strbuf.toString()));
        strbuf.setLength(0);
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
    if (not == null) return;

    Iterator messages = not.getMessages().iterator();

    while (messages.hasNext()) {
      Message msg = (Message) messages.next();
      nbMsgsReceiveSinceCreation += 1;

      String msgId = msg.id;
      AgentId replyTo = AgentId.fromString(msg.replyToId);
      AdminRequest request = (AdminRequest) msg.getAdminMessage();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this + ": got " + request);

      if (request == null) {
        logger.log(BasicLevel.ERROR, "--- " + this + ": got bad AdminRequest.");
        distributeReply(replyTo, msgId, new AdminReply(false, "Unexpected request to AdminTopic"));
      }

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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ".processAdminRequests(" + msgId + ',' + request + ')');

    String info = null;
    if (request == null) {
      info = strbuf.append("Unexpected null request to AdminTopic on server [") .append(serverId).append("]").toString();
      strbuf.setLength(0);
      distributeReply(replyTo, msgId, new AdminReply(false, info));
      return;
    }

    // state change, so save.
    setSave();

    try {
      if (request instanceof StopServerRequest)
        doProcess((StopServerRequest) request, replyTo, msgId);
      else if (request instanceof CreateDestinationRequest)
        doProcess((CreateDestinationRequest) request, replyTo, msgId);
      else if (request instanceof DeleteDestination)
        doProcess((DeleteDestination) request, replyTo, msgId);
      else if (request instanceof CreateUserRequest)
        doProcess((CreateUserRequest) request, replyTo, msgId);
      else if (request instanceof UpdateUser)
        doProcess((UpdateUser) request, replyTo, msgId);
      else if (request instanceof DeleteUser)
        doProcess((DeleteUser) request, replyTo, msgId);
      else if (request instanceof SetDMQRequest)
        doProcess((SetDMQRequest) request, replyTo, msgId);
      else if (request instanceof SetThresholdRequest)
        doProcess((SetThresholdRequest) request, replyTo, msgId);
      else if (request instanceof GetServersIdsRequest)
        doProcess((GetServersIdsRequest) request, replyTo, msgId);
      else if (request instanceof GetDomainNames)
        doProcess((GetDomainNames) request, replyTo, msgId);
      else if (request instanceof GetLocalServer)
        doProcess(replyTo, msgId);
      else if (request instanceof GetDestinationsRequest)
        doProcess((GetDestinationsRequest) request, replyTo, msgId);
      else if (request instanceof GetUsersRequest)
        doProcess((GetUsersRequest) request, replyTo, msgId);
      else if (request instanceof GetRightsRequest)
        doProcess((GetRightsRequest) request, replyTo, msgId);
      else if (request instanceof GetDMQSettingsRequest)
        doProcess((GetDMQSettingsRequest) request, replyTo, msgId);
      else if (request instanceof GetStatsRequest)
        doProcess((GetStatsRequest) request, replyTo, msgId);
      else if (request instanceof DestinationAdminRequest)
        doProcess((DestinationAdminRequest) request, replyTo, msgId);
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
      else if (request instanceof AdminCommandRequest)
        doProcess((AdminCommandRequest) request, replyTo, msgId);
    } catch (UnknownServerException exc) {
      // Caught when a target server is invalid.
      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], successful [false]: ")
      .append(exc.getMessage()).toString();
      strbuf.setLength(0);

      distributeReply(replyTo, msgId, new AdminReply(AdminReply.UNKNOWN_SERVER, info));
    } catch (MomException exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      info = strbuf.append("Request [").append(request.getClass().getName())
          .append("], sent to AdminTopic on server [").append(serverId)
          .append("], successful [false]: ")
          .append(exc.getMessage()).toString();
      strbuf.setLength(0);

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
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>CreateDestinationRequest</code> instance
   * requesting the creation of a destination.
   *
   * @exception UnknownServerException  If the target server does not exist.
   * @exception RequestException  If the destination deployment fails.
   */
  private void doProcess(CreateDestinationRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException, RequestException {
    if (checkServerId(request.getServerId())) {
      // The destination is local, process the request.

      DestinationDesc destDesc = createDestination(request.getDestinationName(),
                                                   // Not really needed, the administration topic already has administration rights.
                                                   getId(),
                                                   request.getProperties(),
                                                   request.getExpectedType(),
                                                   request.getClassName(),
                                                   request.getClass().getName(),
                                                   strbuf);

      distributeReply(replyTo,
                      msgId,
                      new CreateDestinationReply(destDesc.getId().toString(), 
                                                 destDesc.getName(),
                                                 strbuf.toString()));
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, strbuf.toString());
      strbuf.setLength(0);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
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
  private DestinationDesc createDestination(String destName,
                                           AgentId adminId,
                                           Properties properties,
                                           byte type,
                                           String className,
                                           String requestClassName,
                                           StringBuffer strbuf) throws UnknownServerException, RequestException {
    DestinationDesc destDesc;

    // Retrieving an existing destination:
    if (destName != null && ! destName.equals("") && destinationsTable.containsKey(destName)) {
      destDesc = (DestinationDesc) destinationsTable.get(destName);
      if (! DestinationConstants.compatible(destDesc.getType(), type)) {
        throw new RequestException("Destination type not compliant");
      }
      strbuf.append("Request [").append(requestClassName)
      .append("], processed by AdminTopic on server [").append(serverId)
      .append("], successful [true]: destination [")
      .append(destName).append("] has been retrieved");
    } else {
      // Instantiating the destination class.
      Destination dest = null;
      try {
        dest = (Destination) Class.forName(className).newInstance();
        dest.setName(destName);
        dest.setAdminId(adminId);
        dest.setProperties(properties, true);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR,
                   "Could not instantiate Destination class [" + className + "]: ", exc);
        if (exc instanceof ClassCastException)
          throw new RequestException("Class [" + className + "] is not a Destination class.");

        throw new RequestException("Could not instantiate Destination class [" + className + "]: " + exc);
      }
      
      byte destType = dest.getType();
      if (! DestinationConstants.compatible(destType, type)) {
        throw new RequestException("Requested destination type is not compliant with destination classname");
      }

      if (destName == null || destName.equals(""))
        destName = dest.getAgentId();

      destDesc = new DestinationDesc(dest.getId(), destName, destType);
      try {
        dest.deploy();
        destinationsTable.put(destName, destDesc);

        strbuf.append("Request [").append(requestClassName)
        .append("], processed by AdminTopic on server [").append(serverId)
        .append("], successful [true]: ").append(className).append(" [")
        .append(dest.getAgentId()).append("] has been created and deployed");

      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "xxx", exc);
        throw new RequestException("Error while deploying Destination [" + className + "]: " + exc);
      }
    }
    return destDesc;
  }

  /**
   * Processes a <code>DeleteDestination</code> instance requesting the
   * deletion of a destination.
   */
  private void doProcess(DeleteDestination request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getId());

    // If the destination is not local, doing nothing:
    if (checkServerId(destId.getTo())) {
      // The destination is  local, process the request.
      String info;

      Iterator destinations = destinationsTable.values().iterator();
      while (destinations.hasNext()) {
        DestinationDesc destDesc = (DestinationDesc) destinations.next();
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
      forward(getDefault(destId.getTo()),
              new FwdAdminRequestNot(request, replyTo, msgId));
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
                        String msgId) throws UnknownServerException, RequestException {
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
          if (! userIdentity.check(identity)) {
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
        proxy.setName(name);
        proxId = proxy.getId();
        
      	// set interceptors.
      	proxy.setInterceptors(request.getProperties());
        
        try {
        	// deploy UserAgent
          proxy.deploy();
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "doProcess CreateUserRequest:: store (in usersTable) this identity = " + identity);
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
        	if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "EXCEPTION:: createUser [" + name + "]", exc);
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
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
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
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "doProcess UpdateUser:: store (in usersTable) this identity = " + newIdentity);
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
      forward(getDefault(proxId.getTo()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>DeleteUser</code> instance requesting the deletion
   * of a user.
   */
  private void doProcess(DeleteUser request, AgentId replyTo, String msgId) throws UnknownServerException {
    String name = request.getUserName();
    AgentId proxId = AgentId.fromString(request.getProxId());

    if (checkServerId(proxId.getTo())) {
      // If the user belong to this server, process the request.
      if (usersTable.containsKey(name)) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Forward delete request to proxy " + proxId);
        forward(proxId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
      } else {
        String info = strbuf.append("Request [").append(request.getClass().getName())
            .append("], sent to AdminTopic on server [").append(serverId)
            .append("], successful [false]: user [").append(name).append("] does not exist").toString();
        strbuf.setLength(0);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, info);
        distributeReply(replyTo, msgId, new AdminReply(AdminReply.NAME_UNKNOWN, info));
      }

    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault(proxId.getTo()), new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>SetDMQ</code> request requesting a given queue to be set as
   * the DMQ of a given destination or user.
   * If the AgentId of the destination is NullId set the default DMQ.
   * If the AgentId of the DMQ is NullId unset the DMQ.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(SetDMQRequest request, AgentId replyTo, String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (destId.isNullId()) {
      // This request ask to set the default DMQ
      if (checkServerId(destId.getTo())) {
        // Set the default DMQ for local server
        Queue.defaultDMQId = null;
        if (request.getDmqId() != null)
          Queue.defaultDMQId = AgentId.fromString(request.getDmqId());

        distributeReply(replyTo, msgId, new AdminReply(true, null));
      } else {
        // Forward the request to the right AdminTopic agent.
        forward(getDefault(destId.getTo()),
                new FwdAdminRequestNot(request, replyTo, msgId));
      }
    } else {
      // Send the request to the destination or User.
      forward(destId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
    }

  }

  /**
   * Processes a <code>SetThreshold</code> request requesting a given threshold value to be set
   * as the threshold of a given  destination or user.
   * If the AgentId of the destination is NullId set the default threshold.
   * If the threshold value is less than 0 reset the threshold.
   * 
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(SetThresholdRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDestId());

    if (destId.isNullId()) {
      // Set the default Threshold
      if (checkServerId(destId.getTo())) {
        // Set the default Threshold for local server
        Queue.defaultThreshold = 0;
        if (request.getThreshold() > 0)
          Queue.defaultThreshold = request.getThreshold();

        distributeReply(replyTo, msgId, new AdminReply(true, null));
      } else {
        // Forward the request to the right AdminTopic agent.
        forward(getDefault(destId.getTo()),
                new FwdAdminRequestNot(request, replyTo, msgId));
      }
    } else {
      // Forward the request to the target.
      forward(destId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
    }
  }

  /**
   * Processes a <code>Monitor_GetServersIds</code> request by sending 
   * the list of the platform servers' ids.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(GetServersIdsRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    if (checkServerId(request.getServerId())) {
      try {
        int[] ids;
        String[] names;
        String[] hostNames;
        String domainName = request.getDomainName();
        Iterator servers;
        A3CMLConfig config = AgentServer.getConfig();
        int serversCount;
        if (domainName != null) {
          A3CMLDomain domain = config.getDomain(domainName);
          servers = domain.servers.iterator();
          serversCount = domain.servers.size();
        } else {
          servers = config.servers.values().iterator();
          serversCount = config.servers.size();
        }
        ids = new int[serversCount];
        names = new String[serversCount];
        hostNames = new String[serversCount];
        int i = 0;
        while (servers.hasNext()) {
          A3CMLServer server = (A3CMLServer) servers.next();
          ids[i] = server.sid;
          names[i] = server.name;
          hostNames[i] = server.hostname;
          i++;
        }
        GetServersIdsReply reply = new GetServersIdsReply(ids, names, hostNames);
        distributeReply(replyTo, msgId, reply);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        distributeReply(replyTo, msgId,
                        new AdminReply(false, exc.toString()));
      }
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  private void doProcess(AgentId replyTo, String msgId) {
    try {
      A3CMLConfig config = AgentServer.getConfig();
      A3CMLServer a3cmlServer = config.getServer(AgentServer.getServerId(), AgentServer.getClusterId());
      distributeReply(replyTo, msgId,
                      new GetLocalServerRep(a3cmlServer.sid,
                                            a3cmlServer.name,
                                            a3cmlServer.hostname));
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, this + ".doProcess()", exc);
      distributeReply(replyTo, msgId, new AdminReply(false, exc.toString()));
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
  private void doProcess(GetDestinationsRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    if (checkServerId(request.getServerId())) {
      Iterator destinations = destinationsTable.values().iterator();
      String[] ids = new String[destinationsTable.size()];
      String[] names = new String[destinationsTable.size()];
      byte[] types = new byte[destinationsTable.size()];
      int i = 0;
      while (destinations.hasNext()) {
        DestinationDesc destDesc = (DestinationDesc) destinations.next();
        ids[i] = destDesc.getId().toString();
        names[i] = destDesc.getName();
        types[i] = destDesc.getType();
        i++;
      }
      GetDestinationsReply reply = new GetDestinationsReply(ids, names, types);
      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>Monitor_GetUsers</code> request by sending the
   * users table.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(GetUsersRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    if (checkServerId(request.getServerId())) {
      Hashtable users = new Hashtable(proxiesTable.size()*4/3);
      
      String name; 
      for (Iterator names = proxiesTable.keySet().iterator(); names.hasNext();) {
        name = (String) names.next();
        /** Adds the user to the table. */
        users.put(name, ((AgentId) proxiesTable.get(name)).toString());
      }

      GetUsersReply reply = new GetUsersReply(users);
      distributeReply(replyTo, msgId, reply);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault((short) request.getServerId()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>GetRightsRequest</code> request by forwarding it
   * to its target destination, if local.
   */
  private void doProcess(GetRightsRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId destId = AgentId.fromString(request.getDest());

    // Be careful, this request must be sent to the AdminTopic of the server where
    // the destination is, because the information about users is handled locally.
    // It the reply is handled by this AdminTopic it is bad !!
    // This issue should disappear with roles based security.
   if (checkServerId(destId.getTo())) {
      // The destination is local, process the request.
      forward(destId, new GetRightsRequestNot(msgId));
      if (replyTo != null) requestsTable.put(msgId, replyTo);
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault(destId.getTo()),
              new FwdAdminRequestNot(request, replyTo, msgId));
    }
  }

  /**
   * Processes a <code>Monitor_GetDMQSettings</code> request either by
   * processing it and sending back the default DMQ settings, or by
   * forwarding it to its target destination or proxy.
   *
   * @exception UnknownServerException  If the target server does not exist.
   */
  private void doProcess(GetDMQSettingsRequest request,
                         AgentId replyTo,
                         String msgId) throws UnknownServerException {
    AgentId targetId = AgentId.fromString(request.getDestId());
    
    if (targetId.isNullId()) {
      // Get the default DMQ setting
      if (checkServerId(targetId.getTo())) {
        // Get the default DMQ setting for local server
        String dmqId = null;
        if (Queue.defaultDMQId != null)
          dmqId = Queue.defaultDMQId.toString();
        GetDMQSettingsReply reply = new GetDMQSettingsReply(dmqId, Queue.defaultThreshold);
        distributeReply(replyTo, msgId, reply);
      } else {
        // Forward the request to the right AdminTopic agent.
        forward(getDefault(targetId.getTo()),
                new FwdAdminRequestNot(request, replyTo, msgId));
      }
    } else {
      // Forward the request to the target
      forward(targetId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
    }
  }

  /**
   * Processes a <code>Monitor_GetStat</code> request by
   * forwarding it to its target destination, if local.
   */
  private void doProcess(GetStatsRequest request, AgentId replyTo, String msgId) {
    AgentId destId = AgentId.fromString(request.getDest());

    if (destId.isNullId()) {
      // Return the statistics of the server
      Hashtable stats = new Hashtable();
      stats.put("AverageLoad1", new Float(AgentServer.getEngineAverageLoad1()));
      stats.put("AverageLoad5", new Float(AgentServer.getEngineAverageLoad5()));
      stats.put("AverageLoad15", new Float(AgentServer.getEngineAverageLoad15()));
      GetStatsReply reply = new GetStatsReply(stats);
      distributeReply(replyTo, msgId, reply);
    } else {
      forward(destId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
    }
  }

  /**
   * Processes a <code>DestinationRequest</code> request by
   * forwarding it to its target destination, if local.
   */
  private void doProcess(DestinationAdminRequest request, AgentId replyTo, String msgId) {
    AgentId destId = AgentId.fromString(request.getDestId());
    forward(destId, new FwdAdminRequestNot(request, replyTo, msgId, createMessageId()));
  }

  /* ***** ***** ***** ***** *****
   * These methods allows the configuration handling.
   * ***** ***** ***** ***** ***** */

  /**
   * Adds a new domain to the configuration.
   * 
   * @param request The request describing the domain to create.
   * @param replyTo The destination to reply.
   * @param msgId   The JMS message id needed to reply.
   * @param from    The <code>AgentId</code> of sender.
   */
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
                      new AdminReply(AdminReply.NAME_ALREADY_USED, exc.getMessage()));
    } catch (ServerConfigHelper.StartFailureException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(AdminReply.START_FAILURE, exc.getMessage()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId, new AdminReply(false, exc.toString()));
    }
  }

  /**
   * Removes a domain in the configuration.
   * 
   * @param request The request describing the domain to remove.
   * @param replyTo The destination to reply.
   * @param msgId   The JMS message id needed to reply.
   * @param from    The <code>AgentId</code> of sender.
   */
  private void doProcess(RemoveDomainRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(true);
      if (helper.removeDomain(request.getDomainName())) {
        distributeReply(replyTo, msgId, new AdminReply(true, "Domain removed"));
      }
      if (from == null) {
        broadcastRequest(request, -1, replyTo, msgId);
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  /**
   * Adds a server to the configuration.
   * 
   * @param request The request describing the server to create.
   * @param replyTo The destination to reply.
   * @param msgId   The JMS message id needed to reply.
   * @param from    The <code>AgentId</code> of sender.
   */
  private void doProcess(AddServerRequest request,
                         AgentId replyTo,
                         String msgId,
                         AgentId from) {
    try {
      ServerConfigHelper helper = new ServerConfigHelper(false);
      helper.addServer(request.getServerId(),
                       request.getHostName(),
                       request.getDomainName(),
                       request.getPort(),
                       request.getServerName());
      helper.addService(request.getServerId(),
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
      distributeReply(replyTo, msgId, new AdminReply(true, "Server added"));
      if (from == null) {
        broadcastRequest(request, request.getServerId(), replyTo, msgId);
      }
    } catch (ServerConfigHelper.ServerIdAlreadyUsedException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(AdminReply.SERVER_ID_ALREADY_USED, exc.getMessage()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  /**
   * Removes a server in the configuration.
   * 
   * @param request The request describing the server to remove.
   * @param replyTo The destination to reply.
   * @param msgId   The JMS message id needed to reply.
   * @param from    The <code>AgentId</code> of sender.
   */
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
        broadcastRequest(request, request.getServerId(), replyTo, msgId);
      }
    } catch (UnknownServerException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(AdminReply.UNKNOWN_SERVER, exc.getMessage()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId,
                      new AdminReply(false, exc.toString()));
    }
  }

  /**
   * Sends the administration request to AdminTopic of all servers except the given one.
   * This method is used to broadcast server/domain creation and deletion.
   * 
   * @param request       The administration request.
   * @param avoidServerId The id. of the server to avoid.
   * @param replyTo       The destination to reply.
   * @param msgId         The JMS message id needed to reply.
   */
  private void broadcastRequest(AdminRequest request,
                                int avoidServerId,
                                AgentId replyTo,
                                String msgId) {
    FwdAdminRequestNot not = new FwdAdminRequestNot(request, replyTo, msgId);
    Enumeration ids = AgentServer.getServersIds();
    while (ids.hasMoreElements()) {
      short id = ((Short) ids.nextElement()).shortValue();
      if (id != AgentServer.getServerId() && id != avoidServerId) {
        forward(getDefault(id), not);
      }
    }
  }

  /**
   * Gets the current configuration.
   * 
   * @param request
   * @param replyTo
   * @param msgId
   */
  private void doProcess(GetConfigRequest request,
                         AgentId replyTo,
                         String msgId) {
    try {
      A3CMLConfig a3cmlConfig = AgentServer.getConfig();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(baos);
      A3CML.toXML(a3cmlConfig, out);
      out.flush();
      baos.flush();
      baos.close();
      distributeReply(replyTo, msgId, new AdminReply(true, baos.toString()));
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      distributeReply(replyTo, msgId, new AdminReply(false, exc.toString()));
    }
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
      logger.log(BasicLevel.DEBUG, "AdminTopic.distributeReply(" + to + ',' + msgId + ',' + reply + ')');

    if (to == null) return;

    Message message = MessageHelper.createMessage(createMessageId(), msgId, getAgentId(), getType());
    try {
      message.setAdminMessage(reply);
      ClientMessages clientMessages = new ClientMessages(-1, -1, message);
      forward(to, clientMessages);
      nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation + 1;
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "", exc);
    }
  }

  private String createMessageId() {
    msgCounter++;
    return "ID:" + getId().toString() + '_' + msgCounter;
  }

  /**
   * Handle administration request about user.
   * 
   * @param request
   * @param replyTo
   * @param requestMsgId
   * @throws UnknownServerException
   */
  private void doProcess(UserAdminRequest request,
                         AgentId replyTo,
                         String requestMsgId) throws UnknownServerException {
    AgentId userId = AgentId.fromString(request.getUserId());
    if (checkServerId(userId.getTo())) {
      // Delegate to the proxy
      forward(userId, new FwdAdminRequestNot(request, replyTo, requestMsgId, createMessageId()));
    } else {
      // Forward the request to the right AdminTopic agent.
      forward(getDefault(userId.getTo()),
              new FwdAdminRequestNot(request, replyTo, requestMsgId, null));
    }
  }
  
  /**
   * Process an admin command.
   * 
   * @param request The administration request.
   * @param replyTo The destination to reply.
   * @param requestMsgId The JMS message id needed to reply.
   * @throws UnknownServerException
   */
  private void doProcess(AdminCommandRequest request, AgentId replyTo, String requestMsgId)
      throws UnknownServerException {
    AgentId targetId = null;
    try {
      targetId = AgentId.fromString(request.getTargetId());
    } catch (Exception e) {
      throw new UnknownServerException(e.getMessage());
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminTopic.doProcess(" + request + ',' + replyTo + ',' + requestMsgId
          + ")   targetId = " + targetId);

    if (targetId == null) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "Request (AdminCommandRequest) to an undefined targetId (null).");
      distributeReply(replyTo, requestMsgId, new AdminReply(AdminReply.UNKNOWN_SERVER,
          "Request (AdminCommandRequest) to an undefined targetId (null)."));
      return;
    }
    if (targetId.isNullId()) {
      if (checkServerId(targetId.getTo())) {
        Properties replyProp = null;
        try {
          switch (request.getCommand()) {
          case AdminCommandConstant.CMD_NO:
            break;
          case AdminCommandConstant.CMD_INVOKE_STATIC:
            Object result = invokeStaticMethod(request.getProp());
            if (result != null) {
              replyProp = new Properties();
              if (result instanceof Object[]) {
                replyProp.setProperty(AdminCommandConstant.INVOKE_METHOD_RESULT,
                    Arrays.toString((Object[]) result));
              } else {
                replyProp.setProperty(AdminCommandConstant.INVOKE_METHOD_RESULT, result.toString());
              }
            }
            break;

          default:
            throw new Exception("Bad command : \"" + AdminCommandConstant.commandNames[request.getCommand()] + "\"");
          }
          distributeReply(replyTo, requestMsgId, new AdminCommandReply(true,
              AdminCommandConstant.commandNames[request.getCommand()] + " done.", replyProp));
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "", exc);
          distributeReply(replyTo, requestMsgId, new AdminReply(false, exc.toString()));
        }
      } else {
        // Forward the request to the right AdminTopic agent.
        forward(getDefault(targetId.getTo()), new FwdAdminRequestNot(request, replyTo, requestMsgId));
      }
    } else {
      // Forward the request to the target.
      forward(targetId, new FwdAdminRequestNot(request, replyTo, requestMsgId, createMessageId()));
    }
  }

  /**
   * Invokes a static method on the server using the specified properties.
   * 
   * @param prop
   */
  private static Object invokeStaticMethod(Properties prop) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminTopic.invokeStaticMethod(" + prop + ")");

    String className = prop.getProperty(AdminCommandConstant.INVOKE_CLASS_NAME);
    if (className == null) {
      throw new IllegalArgumentException("Class name property must be specified to invoke method.");
    }
    String methodName = prop.getProperty(AdminCommandConstant.INVOKE_METHOD_NAME);
    if (methodName == null) {
      throw new IllegalArgumentException("Method name property must be specified to invoke method.");
    }
    
    int i = 0;
    String paramType;
    List paramClasses = new ArrayList();
    List paramValues = new ArrayList();
    while ((paramType = prop.getProperty(AdminCommandConstant.INVOKE_METHOD_ARG + i)) != null) {
      String paramValue = prop.getProperty(AdminCommandConstant.INVOKE_METHOD_ARG_VALUE + i);
      if (paramType.equals(Integer.TYPE.getName())) {
        paramClasses.add(Integer.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Integer(paramValue));
      } else if (paramType.equals(Integer.class.getName())) {
        paramClasses.add(Integer.class);
        paramValues.add(paramValue == null ? null : new Integer(paramValue));
      } else if (paramType.equals(String.class.getName())) {
        paramClasses.add(String.class);
        paramValues.add(paramValue == null ? null : paramValue);
      } else if (paramType.equals(Byte.TYPE.getName())) {
        paramClasses.add(Byte.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Byte(paramValue));
      } else if (paramType.equals(Byte.class.getName())) {
        paramClasses.add(Byte.class);
        paramValues.add(paramValue == null ? null : new Byte(paramValue));
      } else if (paramType.equals(Short.TYPE.getName())) {
        paramClasses.add(Short.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Short(paramValue));
      } else if (paramType.equals(Short.class.getName())) {
        paramClasses.add(Short.class);
        paramValues.add(paramValue == null ? null : new Short(paramValue));
      } else if (paramType.equals(Long.TYPE.getName())) {
        paramClasses.add(Long.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Long(paramValue));
      } else if (paramType.equals(Long.class.getName())) {
        paramClasses.add(Long.class);
        paramValues.add(paramValue == null ? null : new Long(paramValue));
      } else if (paramType.equals(Float.TYPE.getName())) {
        paramClasses.add(Float.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Float(paramValue));
      } else if (paramType.equals(Float.class.getName())) {
        paramClasses.add(Float.class);
        paramValues.add(paramValue == null ? null : new Float(paramValue));
      } else if (paramType.equals(Double.TYPE.getName())) {
        paramClasses.add(Double.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Double(paramValue));
      } else if (paramType.equals(Double.class.getName())) {
        paramClasses.add(Double.class);
        paramValues.add(paramValue == null ? null : new Double(paramValue));
      } else if (paramType.equals(Boolean.TYPE.getName())) {
        paramClasses.add(Boolean.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        paramValues.add(new Boolean(paramValue));
      } else if (paramType.equals(Boolean.class.getName())) {
        paramClasses.add(Boolean.class);
        paramValues.add(paramValue == null ? null : new Boolean(paramValue));
      } else if (paramType.equals(Character.TYPE.getName())) {
        paramClasses.add(Character.TYPE);
        if (paramValue == null) {
          throw new IllegalArgumentException("Primitive type can't be null to invoke method.");
        }
        if (paramValue.length() == 0) {
          throw new IllegalArgumentException("Empty value for char argument.");
        }
        paramValues.add(new Character(paramValue.charAt(0)));
      } else if (paramType.equals(Character.class.getName())) {
        paramClasses.add(Character.class);
        if (paramValue != null && paramValue.length() == 0) {
          throw new IllegalArgumentException("Empty value for java.lang.Character argument.");
        }
        paramValues.add(paramValue == null ? null : new Character(paramValue.charAt(0)));
      } else {
        throw new IllegalArgumentException("Class not allowed for static invocation: " + paramType);
      }
      i++;
    }

    Class<?> clazz = Class.forName(className);
    Method method = clazz.getMethod(methodName,
        (Class[]) paramClasses.toArray(new Class[paramClasses.size()]));
    if (! Modifier.isStatic(method.getModifiers()))
      throw new IllegalArgumentException("Specified method must be static: " + method);

    return method.invoke(null, paramValues.toArray());
  }

  /** 
   * Returns <code>true</code> if a given server identification corresponds
   * to the local server's.
   *
   * @param serverId  Server identifier.
   *
   * @exception UnknownServerException  If the server does not exist.
   */
  private boolean checkServerId(int serverId) throws UnknownServerException {
    if (serverId == this.serverId)
      return true;

    Enumeration ids = AgentServer.getServersIds();
    while (ids.hasMoreElements()) {
      if (((Short) ids.nextElement()).intValue() == serverId)
        return false;
    }

    throw new UnknownServerException("server#" + serverId + " is unknow.");
  }

  public static class DestinationDesc implements Serializable {
    /** define serialVersionUID for interoperability */
    private static final long serialVersionUID = 1L;
    
    private AgentId id;
    private String name;
    private byte type;

    public DestinationDesc(AgentId id,
                           String name,
                           byte type) {
      this.id = id;
      this.name = name;
      this.type = type;
    }

    public final AgentId getId() {
      return id;
    }

    public final String getName() {
      return name;
    }

    public final byte getType() {
      return type;
    }

    public String toString() {
      StringBuffer strbuf = new StringBuffer();
      
      strbuf.append('(').append(super.toString());
      strbuf.append(",id=").append(id);
      strbuf.append(",name=").append(name);
      strbuf.append(",type=").append(type).append(')');
      
      return strbuf.toString();
    }
  }

  /* ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** */

  /** Serializes an <code>AdminTopic</code> instance. */
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    // Saves DMQ defaults.
    out.writeObject(Queue.defaultDMQId);
    out.writeInt(Queue.defaultThreshold);

    out.defaultWriteObject();
  }

  /** Deserializes an <code>AdminTopic</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
  throws java.io.IOException, ClassNotFoundException {
    Queue.defaultDMQId = (AgentId) in.readObject();
    Queue.defaultThreshold = in.readInt();
    in.defaultReadObject();
    ref = this;
  }

  /* ***** ***** ***** ***** *****
   * These methods are only needed by the MBean interface
   * ***** ***** ***** ***** ***** */

  /**
   * {@inheritDoc}
   */
  public void createUser(String user, String passwd) throws Exception {
    createUser(user, passwd, serverId);
  }

  /**
   * {@inheritDoc}
   */
  public void createUser(String user, String passwd, int serverId) throws Exception {
    createUser(user, passwd, serverId, SimpleIdentity.class.getName());
  }

  /**
   * {@inheritDoc}
   */
  public void createUser(String user, String passwd, int serverId, String identityClassName) throws Exception {
    Identity identity = null;
    try {
      identity = (Identity) Class.forName(identityClassName).newInstance();
      if (passwd != null)
        identity.setIdentity(user, passwd);
      else
        identity.setUserName(user);
    } catch (Exception e) {
      throw new RequestException(e.getMessage());
    }
    CreateUserRequest request = new CreateUserRequest(identity, serverId, null);
    FwdAdminRequestNot createNot = new FwdAdminRequestNot(request, null, null);
    Channel.sendTo(getId(), createNot);
  }

  /**
   * {@inheritDoc}
   */
  public void createQueue(String name) {
    createQueue(name, serverId);
  }

  /**
   * {@inheritDoc}
   */
  public void createQueue(String name, int serverId) {
    createQueue(name, Queue.class.getName(), serverId);
  }

  /**
   * {@inheritDoc}
   */
  public void createQueue(String name, String queueClassName, int serverId) {
    CreateDestinationRequest request = new CreateDestinationRequest(serverId, name, queueClassName, null, DestinationConstants.QUEUE_TYPE);
    FwdAdminRequestNot createNot = new FwdAdminRequestNot(request, null, null);
    Channel.sendTo(getId(), createNot);
  }
  
  /**
   * {@inheritDoc}
   */
  public void createTopic(String name) {
    createTopic(name, serverId);
  }

  /**
   * {@inheritDoc}
   */
  public void createTopic(String name, int serverId) {
    createTopic(name, Topic.class.getName(), serverId);
  }

  /**
   * {@inheritDoc}
   */
  public void createTopic(String name, String topicClassName, int serverId) {
    CreateDestinationRequest request = new CreateDestinationRequest(serverId, name, topicClassName, null,
        DestinationConstants.TOPIC_TYPE);
    FwdAdminRequestNot createNot = new FwdAdminRequestNot(request, null, null);
    Channel.sendTo(getId(), createNot);
  }
  
  public static void deleteUser(String userName) {
    ref.usersTable.remove(userName);
    ref.proxiesTable.remove(userName);
  }

  // ================================================================================
  // This code is used by the UserAgent to handle destination creation through the
  // JMS Session.
  // ================================================================================

  /**
   * Retrieves an existing destination.
   * 
   * @param name  The name of the destination.
   * @param type  The type of the destination.
   * @return the descriptor of the destination, null if it does not exist.
   * 
   * @throws RequestException If the existing destination have a different type.
   */
  public static DestinationDesc lookupDest(String name, byte type) throws RequestException {
    DestinationDesc desc = null;
    
    if (name != null && name.length() > 0) {
      desc = (DestinationDesc) ref.destinationsTable.get(name);
      if ((desc != null) && (! DestinationConstants.compatible(desc.getType(), type))) {
        throw new RequestException("Destination type not compliant");
      }
    }
    return desc;
  }
  
  /**
   * Retrieves an existing user
   * 
   * @param name  The name of the user
   */
  public static AgentId lookupUser(String name) {
    AgentId proxId = null;
    if (name != null && name.length() > 0) {
      proxId = (AgentId) ref.proxiesTable.get(name);
    }
    return proxId;
  }
  
  /**
   * Registers a newly created destination.
   * 
   * @param id    The unique identifier of the created destination.
   * @param name  The name of the created destination.
   * @param type  The type of the created destination.
   * 
   * @throws IOException If an error occurs during saving.
   */
  public static void registerDest(AgentId id, String name, byte type) {
    DestinationDesc desc = new DestinationDesc(id, name, type);
    ref.destinationsTable.put(name, desc);
    // Store the AdminTopic in order to save changes
    try {
      AgentServer.getTransaction().save(ref, ref.getId().toString());
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR, "Cannot unregister destination", exc);
    }
  }
  
  /**
   * Removes a registered destination.
   * 
   * @param name    The name of the destination to remove.
   */
  public static void unregisterDest(String name) {
    ref.destinationsTable.remove(name);
    // Store the AdminTopic in order to save changes
    try {
      AgentServer.getTransaction().save(ref, ref.getId().toString());
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR, "Cannot unregister destination", exc);
    }
  }

  // ================================================================================
  // This code below is needed by external applications. The interface between these
  // applications and Joram needs to be defined.
  // TODO (AF): This code should be moved in an helper class.
  // ================================================================================

  /**
   * Processes a <code>CreateUserRequest</code> instance requesting the
   * creation of a <code>UserAgent</code> for a given user and save Agent
   * AdminTopic. (used by ScalAgent mediation)
   *
   * @exception UnknownServerException  If the target server does not exist.
   * @exception RequestException  If the user already exists but with a
   *              different password, or if the proxy deployment failed.
   * @throws IOException transaction exception
   * 
   * @deprecated
   */
  public static void CreateUserAndSave(CreateUserRequest request,
                                       AgentId replyTo,
                                       String msgId) throws UnknownServerException, RequestException, IOException {
    ref.doProcess(request, replyTo, msgId);
    //  save Agent AdminTopic
    AgentServer.getTransaction().save(ref, ref.getId().toString()); 
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
   * 
   * @deprecated
   */
  public static DestinationDesc createDestinationAndSave(String destName,
                                                         AgentId adminId,
                                                         Properties properties,
                                                         byte type,
                                                         String className,
                                                         String requestClassName,
                                                         StringBuffer strbuf) throws UnknownServerException, RequestException, IOException {
    // create destination.
    DestinationDesc destDesc = ref.createDestination(destName,
                                                     adminId, properties,
                                                     type, className,
                                                     requestClassName,
                                                     strbuf);
    // save Agent AdminTopic
    AgentServer.getTransaction().save(ref, ref.getId().toString()); 
    return destDesc;
  }

  /**
   * Processes a <code>SetRight</code> instance requesting to grant a user
   * a given right on a given destination. And save Agent TopicAdmin.
   * (used by ScalAgent mediation)
   *
   * @param request
   * @param replyTo
   * @param msgId
   * 
   * @throws UnknownServerException
   * @throws IOException
   * 
   * @deprecated
   */
  public static void setRightAndSave(SetRight request,
                                     AgentId replyTo,
                                     String msgId) throws UnknownServerException, IOException {
    ref.doProcess(request, replyTo, msgId);
    // save Agent AdminTopic
    AgentServer.getTransaction().save(ref, ref.getId().toString()); 
  }

  /**
   * is destinationTable contain destName ?
   * (used by ScalAgent mediation)
   * 
   * @param destName destination name.
   * @return true if contain.
   * 
   * @deprecated
   */
  public static boolean isDestinationTableContain(String destName) {
    return ref.destinationsTable.containsKey(destName);
  }
}
