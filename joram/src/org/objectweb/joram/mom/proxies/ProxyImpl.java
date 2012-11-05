/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
 * Copyright (C) 2003 - 2004 Bull SA
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
package org.objectweb.joram.mom.proxies;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AbortReceiveRequest;
import org.objectweb.joram.mom.notifications.AbstractReply;
import org.objectweb.joram.mom.notifications.AbstractRequest;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.AdminReply;
import org.objectweb.joram.mom.notifications.BrowseReply;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettings;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettingsRep;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsg;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsgRep;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.RegisterTmpDestNot;
import org.objectweb.joram.mom.notifications.SetDMQRequest;
import org.objectweb.joram.mom.notifications.SetNbMaxMsgRequest;
import org.objectweb.joram.mom.notifications.SetRightRequest;
import org.objectweb.joram.mom.notifications.SetThreshRequest;
import org.objectweb.joram.mom.notifications.SubscribeReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.mom.notifications.UserAdminRequestNot;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.GetSubscription;
import org.objectweb.joram.shared.admin.GetSubscriptionMessage;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIds;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageRep;
import org.objectweb.joram.shared.admin.GetSubscriptionRep;
import org.objectweb.joram.shared.admin.GetSubscriptions;
import org.objectweb.joram.shared.admin.GetSubscriptionsRep;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.ActivateConsumerRequest;
import org.objectweb.joram.shared.client.CnxCloseReply;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.CnxConnectReply;
import org.objectweb.joram.shared.client.CnxConnectRequest;
import org.objectweb.joram.shared.client.CnxStartRequest;
import org.objectweb.joram.shared.client.CnxStopRequest;
import org.objectweb.joram.shared.client.CommitRequest;
import org.objectweb.joram.shared.client.ConsumerAckRequest;
import org.objectweb.joram.shared.client.ConsumerCloseSubRequest;
import org.objectweb.joram.shared.client.ConsumerDenyRequest;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.client.ConsumerReceiveRequest;
import org.objectweb.joram.shared.client.ConsumerSetListRequest;
import org.objectweb.joram.shared.client.ConsumerSubRequest;
import org.objectweb.joram.shared.client.ConsumerUnsetListRequest;
import org.objectweb.joram.shared.client.ConsumerUnsubRequest;
import org.objectweb.joram.shared.client.GetAdminTopicReply;
import org.objectweb.joram.shared.client.GetAdminTopicRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.client.QBrowseReply;
import org.objectweb.joram.shared.client.QBrowseRequest;
import org.objectweb.joram.shared.client.ServerReply;
import org.objectweb.joram.shared.client.SessAckRequest;
import org.objectweb.joram.shared.client.SessCreateTDReply;
import org.objectweb.joram.shared.client.SessCreateTQRequest;
import org.objectweb.joram.shared.client.SessCreateTTRequest;
import org.objectweb.joram.shared.client.SessDenyRequest;
import org.objectweb.joram.shared.client.TempDestDeleteRequest;
import org.objectweb.joram.shared.client.XACnxCommit;
import org.objectweb.joram.shared.client.XACnxPrepare;
import org.objectweb.joram.shared.client.XACnxRecoverReply;
import org.objectweb.joram.shared.client.XACnxRecoverRequest;
import org.objectweb.joram.shared.client.XACnxRollback;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.DestinationException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.excepts.StateException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>ProxyImpl</code> class implements the MOM proxy behaviour,
 * basically forwarding client requests to MOM destinations and MOM
 * destinations replies to clients.
 */ 
public class ProxyImpl implements java.io.Serializable, ProxyImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(ProxyImpl.class.getName());

  /** period to run the cleaning task, by default 60s. */
  protected long period = 60000L;
  
  /** the number of erroneous messages forwarded to the DMQ */
  protected long nbMsgsSentToDMQSinceCreation = 0;

  /**
   * Returns  the period value of this queue, -1 if not set.
   *
   * @return the period value of this queue; -1 if not set.
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets or unsets the period for this queue.
   *
   * @param period The period value to be set or -1 for unsetting previous
   *               value.
   */
  public void setPeriod(long period) {
    if ((this.period == -1L) && (period != -1L)) {
      // Schedule the CleaningTask.
      Channel.sendTo(proxyAgent.getId(), new WakeUpNot());
    }
    this.period = period;
  }

  /**
   * Identifier of this proxy dead message queue, <code>null</code> for DMQ
   * not set.
   */
  private AgentId dmqId = null;
  /**
   * Threshold value, 0 or negative for no threshold, <code>null</code> for
   * value not set.
   */
  private Integer threshold = null; 

  /**
   * Table of the proxy's <code>ClientContext</code> instances.
   * <p>
   * <b>Key:</b> context identifier<br>
   * <b>Value:</b> context
   */
  private Hashtable contexts;
  /**
   * Table holding the <code>ClientSubscription</code> instances.
   * <p>
   * <b>Key:</b> subscription name<br>
   * <b>Value:</b> client subscription
   */
  private Hashtable subsTable;
  /**
   * Table holding the recovered transactions branches.
   * <p>
   * <b>Key:</b> transaction identifier<br>
   * <b>Value:</b> <code>XACnxPrepare</code> instance
   */
  private Hashtable recoveredTransactions;

  /** Counter of message arrivals from topics. */ 
  private long arrivalsCounter = 0; 

  /** The reference of the agent hosting the proxy. */
  private ProxyAgentItf proxyAgent;
  /** 
   * Table holding the <code>TopicSubscription</code> instances.
   * <p>
   * <b>Key:</b> topic identifier<br>
   * <b>Value:</b> topic subscription
   */
  private transient Hashtable topicsTable;
  /**
   * Table holding the subscriptions' messages.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private transient Hashtable messagesTable;

  /** 
   * Identifier of the active context. 
   * Value -1 means that there's no active
   * context.
   */
  private transient int activeCtxId;
  /** Reference to the active <code>ClientContext</code> instance. */
  private transient ClientContext activeCtx;  

  /**
   * Constructs a <code>ProxyImpl</code> instance.
   */
  public ProxyImpl(ProxyAgentItf proxyAgent) {
    contexts = new Hashtable();
    subsTable = new Hashtable();
    this.proxyAgent = proxyAgent;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Returns a string representation of this user's proxy.
   */
  public String toString() {
    if (proxyAgent == null)
      return "ProxyImpl:";
    else
      return "ProxyImpl:" + proxyAgent.getId();
  }


  /**
   * (Re)initializes the proxy.
   *
   * @exception Exception  If the proxy state could not be fully retrieved,
   *              leading to an inconsistent state.
   */
  public void initialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " (re)initializing...");
 
    topicsTable = new Hashtable();
    messagesTable = new Hashtable();

    setActiveCtxId(-1);
    
    // Re-initializing after a crash or a server stop.

    // Browsing the pre-crash contexts:
    ClientContext activeCtx;
    AgentId destId;
    for (Enumeration ctxIds = contexts.keys(); ctxIds.hasMoreElements();) {
      activeCtx = (ClientContext) contexts.remove(ctxIds.nextElement());

      // Denying the non acknowledged messages:
      for (Enumeration queueIds = activeCtx.getDeliveringQueues();
           queueIds.hasMoreElements();) {
        destId = (AgentId) queueIds.nextElement();
        proxyAgent.sendNot(destId, new DenyRequest(activeCtx.getId()));

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Denies messages on queue " + destId.toString());
      }

      // Saving the prepared transactions.
      Enumeration xids = activeCtx.getTxIds();
      Xid xid;
      XACnxPrepare recoveredPrepare;
      XACnxPrepare prepare;
      while (xids.hasMoreElements()) {
        if (recoveredTransactions == null)
          recoveredTransactions = new Hashtable();

        xid = (Xid) xids.nextElement();

        recoveredPrepare = (XACnxPrepare) recoveredTransactions.get(xid);
        prepare = activeCtx.getTxPrepare(xid);

        if (recoveredPrepare == null)
          recoveredTransactions.put(xid, prepare);
        else {
          recoveredPrepare.getSendings().addAll(prepare.getSendings());
          recoveredPrepare.getAcks().addAll(prepare.getAcks());
        }
      }

      // Deleting the temporary destinations:
      for (Enumeration tempDests = activeCtx.getTempDestinations();
           tempDests.hasMoreElements();) {
        destId = (AgentId) tempDests.nextElement();
        deleteTemporaryDestination(destId);
  
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Deletes temporary destination " + destId.toString());
      }
    }

    // Retrieving the subscriptions' messages.
    Vector messages = Message.loadAll(getMsgTxname());

    if (subsTable.isEmpty()) {
      // it is possible because we always save MessageSoftRef
      // so we must delete all message.
      Message.deleteAll(getMsgTxname());
    }
    
    // Browsing the pre-crash subscriptions:
    String subName;
    ClientSubscription cSub;
    Vector topics = new Vector();
    TopicSubscription tSub;
    for (Enumeration subNames = subsTable.keys();
         subNames.hasMoreElements();) {
      subName = (String) subNames.nextElement();
      cSub = (ClientSubscription) subsTable.get(subName);
      destId = cSub.getTopicId();
      if (! topics.contains(destId))
        topics.add(destId);
      // Deleting the non durable subscriptions.
      if (!cSub.getDurable()) {
        subsTable.remove(subName);
        try {
          MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName(subName));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "  - Problem when unregistering ClientSubscriptionMbean", e1);
        }
      }
      // Reinitializing the durable ones.
      else {
        cSub.setProxyAgent(proxyAgent);
        cSub.reinitialize(messagesTable, 
                          messages,
                          true);
        try {
          MXWrapper.registerMBean(cSub, "Joram#"+AgentServer.getServerId(), getMBeanName(subName));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "  - Could not register ClientSubscriptionMbean", e1);
        }
        tSub = (TopicSubscription) topicsTable.get(destId);
        if (tSub == null) {
          tSub = new TopicSubscription();
          topicsTable.put(destId, tSub);
        }
        tSub.putSubscription(subName, cSub.getSelector());
      }
    }
    // Browsing the topics and updating their subscriptions.
    for (Enumeration topicIds = topics.elements();
         topicIds.hasMoreElements();)
      updateSubscriptionToTopic((AgentId) topicIds.nextElement(), -1, -1);
  }

  private void setActiveCtxId(int activeCtxId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ProxyImpl.setActiveCtxId(" + activeCtxId + ')');
    this.activeCtxId = activeCtxId;
  }

  /**
   * Method processing clients requests.
   * <p>
   * Some of the client requests are directly forwarded, some others are
   * sent to the proxy so that their processing occurs in a transaction.
   * <p>
   * A <code>MomExceptionReply</code> wrapping a <tt>DestinationException</tt>
   * might be sent back if a target destination can't be identified.
   */
  public void reactToClientRequest(int key, AbstractJmsRequest request)
  {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "--- " + this + " got " + request.getClass().getName() +
                   " with id: " + request.getRequestId() +
                   " through activeCtx: " + key);

      if (request instanceof ProducerMessages)
        reactToClientRequest(key, (ProducerMessages) request);
      else if (request instanceof ConsumerReceiveRequest)
        reactToClientRequest(key, (ConsumerReceiveRequest) request);
      else if (request instanceof ConsumerSetListRequest)
        reactToClientRequest(key, (ConsumerSetListRequest) request);
      else if (request instanceof QBrowseRequest)
        reactToClientRequest(key, (QBrowseRequest) request);
      else if (request instanceof JmsRequestGroup)
        reactToClientRequest(key, (JmsRequestGroup) request);
      else {
        doReact(key, request);   
      }
    }
    // Catching an exception due to an invalid agent identifier to
    // forward the request to:
    catch (IllegalArgumentException iE) {
      DestinationException dE =
        new DestinationException("Proxy could not forward the request to"
                                 + " incorrectly identified destination: "
                                 + iE);

      doReply(key, new MomExceptionReply(request.getRequestId(), dE));
    }
  }

  /**
   * Forwards the messages sent by the client in a
   * <code>ProducerMessages</code> request as a <code>ClientMessages</code>
   * MOM request directly to a destination, and acknowledges them by sending
   * a <code>ServerReply</code> back.
   */
  private void reactToClientRequest(int key, ProducerMessages req) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.reactToClientRequest(" +  key + ',' + req + ')');

    AgentId destId = AgentId.fromString(req.getTarget());
    ClientMessages not = new ClientMessages(
      key,
      req.getRequestId(),
      req.getMessages());

    setDmq(not);
    
    
    if (destId.getTo() == proxyAgent.getId().getTo()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> local sending");
      not.setPersistent(false);
      if (req.getAsyncSend()) {
        not.setAsyncSend(true);
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> remote sending");
      if (!req.getAsyncSend()) {
        proxyAgent.sendNot(proxyAgent.getId(),
                           new SendReplyNot(key, req.getRequestId()));
      }
    }
    
    proxyAgent.sendNot(destId, not);
  }
  
  private void setDmq(ClientMessages not) {
    //  Setting the producer's DMQ identifier field: 
    if (dmqId != null) {
      not.setDMQId(dmqId);
    } else {
      not.setDMQId(QueueImpl.getDefaultDMQId());
    }
  }

  /**
   * Either forwards the <code>ConsumerReceiveRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   */
  private void reactToClientRequest(int key, ConsumerReceiveRequest req)
  {
    if (req.getQueueMode()) {
      ReceiveRequest not = new ReceiveRequest(
        key,
        req.getRequestId(),
        req.getSelector(),
        req.getTimeToLive(),
        req.getReceiveAck(),
        null,
        1);
      AgentId to = AgentId.fromString(req.getTarget());
      if (to.getTo() == proxyAgent.getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local receiving");
        not.setPersistent(false);
        proxyAgent.sendNot(to, not);
      } else {
        proxyAgent.sendNot(to, not);
      }
    } else {
      doReact(key, req);   
    }
  }

  /**
   * Either forwards the <code>ConsumerSetListRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   */
  private void reactToClientRequest(int key, ConsumerSetListRequest req) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "ProxyImp.reactToClientRequest(" + key + ',' + req + ')');
    if (req.getQueueMode()) {
      ReceiveRequest not = new ReceiveRequest(key,
                                              req.getRequestId(),
                                              req.getSelector(),
                                              0,
                                              false,
                                              req.getMessageIdsToAck(),
                                              req.getMessageCount());    
      AgentId to = AgentId.fromString(req.getTarget());
      if (to.getTo() == proxyAgent.getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local sending");
        not.setPersistent(false);
        proxyAgent.sendNot(to, not);
      } else {
        proxyAgent.sendNot(to, not);
      }
    }
    else {
      doReact(key, req);   
    }
  }

  /**
   * Forwards the client's <code>QBrowseRequest</code> request as
   * a <code>BrowseRequest</code> MOM request directly to a destination.
   */
  private void reactToClientRequest(int key, QBrowseRequest req)
  {
    proxyAgent.sendNot(AgentId.fromString(req.getTarget()),
                       new BrowseRequest(key,
                                         req.getRequestId(),
                                         req.getSelector()));
  }
  
  private void reactToClientRequest(int key, JmsRequestGroup request) {
    AbstractJmsRequest[] requests = request.getRequests();
    RequestBuffer rm = new RequestBuffer(proxyAgent);
    for (int i = 0; i < requests.length; i++) {
      if (requests[i] instanceof ProducerMessages) {
        ProducerMessages pm =(ProducerMessages) requests[i];
        rm.put(key, pm);
      } else {
        reactToClientRequest(key, requests[i]);
      }
    }
    
    rm.flush();
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * <p>
   * A JMS proxy reacts to:
   * <ul>
   * <li><code>SyncReply</code> proxy synchronizing notification,</li>
   * <li><code>SetDMQRequest</code> admin notification,</li>
   * <li><code>SetThreshRequest</code> admin notification,</li>
   * <li><code>SetNbMaxMsgRequest</code> admin notification,</li>
   * <li><code>Monit_GetNbMaxMsg</code> admin notification,</li>
   * <li><code>Monit_GetDMQSettings</code> monitoring notification,</li>
   * <li><code>AbstractReply</code> destination replies,</li>
   * <li><code>AdminReply</code> administration replies,</li>
   * <li><code>fr.dyade.aaa.agent.UnknownAgent</code>.</li>
   * </ul>
   * 
   * @exception UnknownNotificationException
   *              If the notification is not expected.
   */ 
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    // Administration and monitoring requests:
    if (not instanceof SetDMQRequest)
      doReact(from, (SetDMQRequest) not);
    else if (not instanceof SetThreshRequest)
      doReact(from, (SetThreshRequest) not);
    else if (not instanceof SetNbMaxMsgRequest)
      doReact(from, (SetNbMaxMsgRequest) not);
    else if (not instanceof Monit_GetNbMaxMsg)
      doReact(from, (Monit_GetNbMaxMsg) not);
    else if (not instanceof Monit_GetDMQSettings)
      doReact(from, (Monit_GetDMQSettings) not);
    // Synchronization notification:
    else if (not instanceof SyncReply)
      doReact((SyncReply) not);
    // Notifications sent by a destination:
    else if (not instanceof AbstractReply) 
      doFwd(from, (AbstractReply) not);
    else if (not instanceof AdminReply)
      doReact((AdminReply) not);
    // Platform notifications:
    else if (not instanceof UnknownAgent)
      doReact((UnknownAgent) not);
    else if (not instanceof UserAdminRequestNot)
      doReact((UserAdminRequestNot) not);
    else
      throw new UnknownNotificationException("Unexpected notification: " 
                                             + not.getClass().getName());
  }

  
  /**
   * Distributes the client requests to the appropriate reactions.
   * <p>
   * The proxy accepts the following requests:
   * <ul>
   * <li><code>GetAdminTopicRequest</code></li>
   * <li><code>CnxConnectRequest</code></li>
   * <li><code>CnxStartRequest</code></li>
   * <li><code>CnxStopRequest</code></li>
   * <li><code>SessCreateTQRequest</code></li>
   * <li><code>SessCreateTTRequest</code></li>
   * <li><code>ConsumerSubRequest</code></li>
   * <li><code>ConsumerUnsubRequest</code></li>
   * <li><code>ConsumerCloseSubRequest</code></li>
   * <li><code>ConsumerSetListRequest</code></li>
   * <li><code>ConsumerUnsetListRequest</code></li>
   * <li><code>ConsumerReceiveRequest</code></li>
   * <li><code>ConsumerAckRequest</code></li>
   * <li><code>ConsumerDenyRequest</code></li>
   * <li><code>SessAckRequest</code></li>
   * <li><code>SessDenyRequest</code></li>
   * <li><code>TempDestDeleteRequest</code></li>
   * <li><code>XACnxPrepare</code></li>
   * <li><code>XACnxCommit</code></li>
   * <li><code>XACnxRollback</code></li>
   * <li><code>XACnxRecoverRequest</code></li>
   * </ul>
   * <p>
   * A <code>JmsExceptReply</code> is sent back to the client when an
   * exception is thrown by the reaction.
   */ 
  private void doReact(int key, AbstractJmsRequest request)
  {
    try {
      // Updating the active context if the request is not a new context
      // request!
      if (! (request instanceof CnxConnectRequest))
        setCtx(key);

      if (request instanceof GetAdminTopicRequest)
        doReact(key, (GetAdminTopicRequest) request);
      else if (request instanceof CnxConnectRequest)
        doReact(key, (CnxConnectRequest) request);
      else if (request instanceof CnxStartRequest)
        doReact((CnxStartRequest) request);
      else if (request instanceof CnxStopRequest)
        doReact((CnxStopRequest) request);
      else if (request instanceof SessCreateTQRequest)
        doReact((SessCreateTQRequest) request);
      else if (request instanceof SessCreateTTRequest)
        doReact((SessCreateTTRequest) request);
      else if (request instanceof ConsumerSubRequest)
        doReact((ConsumerSubRequest) request);
      else if (request instanceof ConsumerUnsubRequest)
        doReact((ConsumerUnsubRequest) request);
      else if (request instanceof ConsumerCloseSubRequest)
        doReact((ConsumerCloseSubRequest) request);
      else if (request instanceof ConsumerSetListRequest)
        doReact((ConsumerSetListRequest) request);
      else if (request instanceof ConsumerUnsetListRequest)
        doReact((ConsumerUnsetListRequest) request);
      else if (request instanceof ConsumerReceiveRequest)
        doReact((ConsumerReceiveRequest) request);
      else if (request instanceof ConsumerAckRequest)
        doReact((ConsumerAckRequest) request);
      else if (request instanceof ConsumerDenyRequest)
        doReact((ConsumerDenyRequest) request);
      else if (request instanceof SessAckRequest)
        doReact((SessAckRequest) request);
      else if (request instanceof SessDenyRequest)
        doReact((SessDenyRequest) request);
      else if (request instanceof TempDestDeleteRequest)
        doReact((TempDestDeleteRequest) request);
      else if (request instanceof XACnxPrepare)
        doReact((XACnxPrepare) request);
      else if (request instanceof XACnxCommit)
        doReact((XACnxCommit) request);
      else if (request instanceof XACnxRollback)
        doReact((XACnxRollback) request);
      else if (request instanceof XACnxRecoverRequest)
        doReact((XACnxRecoverRequest) request);
      else if (request instanceof CnxCloseRequest)
        doReact(key, (CnxCloseRequest) request);
      else if (request instanceof ActivateConsumerRequest)
        doReact(key, (ActivateConsumerRequest) request);
      else if (request instanceof CommitRequest)
        doReact(key, (CommitRequest)request);
    }
    catch (MomException mE) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, mE);

      // Sending the exception to the client:
      doReply(new MomExceptionReply(request.getRequestId(), mE));
    }
  }

  /**
   * Method implementing the reaction to a <code>GetAdminTopicRequest</code>
   * requesting the identifier of the local admin topic.
   * <p>
   * It simply sends back a <code>GetAdminTopicReply</code> holding the 
   * admin topic identifier.
   * 
   * @exception AccessException  If the requester is not an administrator.
   */
  private void doReact(int key, GetAdminTopicRequest req)
               throws AccessException
  {
//     if (! admin)
//       throw new AccessException("Request forbidden to a non administrator.");

    doReply(
      key,
      new GetAdminTopicReply(req, AdminTopic.getDefault().toString()));
  }

  /**
   * Method implementing the reaction to a <code>CnxConnectRequest</code>
   * requesting the key of the active context.
   * <p>
   * It simply sends back a <code>ConnectReply</code> holding the active
   * context's key.
   *
   * @exception DestinationException  In case of a first administrator 
   *              context, if the local administration topic reference
   *              is not available.
   */
  private void doReact(int key, CnxConnectRequest req)
    throws DestinationException {
    // state change, so save.
    proxyAgent.setSave();

    setActiveCtxId(key);
    activeCtx = new ClientContext(proxyAgent.getId(), key);
    activeCtx.setProxyAgent(proxyAgent);
    contexts.put(new Integer(key), activeCtx);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Connection " + key + " opened.");

    doReply(new CnxConnectReply(req, key, proxyAgent.getId().toString()));
  }

  /**
   * Method implementing the proxy reaction to a <code>CnxStartRequest</code>
   * requesting to start a context.
   * <p>
   * This method sends the pending <code>ConsumerMessages</code> replies,
   * if any.
   */
  private void doReact(CnxStartRequest req) {
    activeCtx.setActivated(true);

    // Delivering the pending deliveries, if any:
    for (Enumeration deliveries = activeCtx.getPendingDeliveries();
         deliveries.hasMoreElements();)
      doReply((AbstractJmsReply) deliveries.nextElement());

    // Clearing the pending deliveries.
    activeCtx.clearPendingDeliveries();
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>CnxStopRequest</code> requesting to stop a context.
   * <p>
   * This method sends a <code>ServerReply</code> back.
   */
  private void doReact(CnxStopRequest req) {
    activeCtx.setActivated(false);
    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SessCreateTQRequest</code> requesting the creation of a temporary
   * queue.
   * <p>
   * Creates the queue, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   *
   * @exception RequestException  If the queue could not be deployed.
   */
  private void doReact(SessCreateTQRequest req) throws RequestException {
    try {
      Queue queue = new Queue();
      queue.init(proxyAgent.getId(), null);
      AgentId qId = queue.getId();

      queue.deploy();

      // Setting free WRITE right on the queue:
      proxyAgent.sendNot(qId, new SetRightRequest(null, null, 2));

      activeCtx.addTemporaryDestination(qId);

      SessCreateTDReply reply = new SessCreateTDReply(req, qId.toString());
      proxyAgent.sendNot(proxyAgent.getId(),
                         new SyncReply(activeCtxId, reply));

      proxyAgent.sendNot(AdminTopic.getDefault(),
                         new RegisterTmpDestNot(qId, false, true));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Temporary queue " + qId + " created.");
    }
    catch (java.io.IOException iE) {
      throw new RequestException("Could not create temporary queue: " + iE);
    } 
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SessCreateTTRequest</code> requesting the creation of a temporary
   * topic.
   * <p>
   * Creates the topic, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   *
   * @exception RequestException  If the topic could not be deployed.
   */
  private void doReact(SessCreateTTRequest req) throws RequestException {
    Topic topic = new Topic();
    topic.init(proxyAgent.getId(), null);
    AgentId tId = topic.getId();

    try {
      topic.deploy();

      // Setting free WRITE right on the topic:
      proxyAgent.sendNot(tId, new SetRightRequest(null, null, 2));

      activeCtx.addTemporaryDestination(tId);

      SessCreateTDReply reply = new SessCreateTDReply(req, tId.toString());
      proxyAgent.sendNot(proxyAgent.getId(),
                         new SyncReply(activeCtxId, reply));

      proxyAgent.sendNot(AdminTopic.getDefault(),
                         new RegisterTmpDestNot(tId, true, true));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Temporary topic" + tId + " created.");
    } catch (java.io.IOException iE) {
      topic = null;
      throw new RequestException("Could not deploy temporary topic "
                                 + tId + ": " + iE);
    } 
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerSubRequest</code> requesting to subscribe to a topic.
   *
   * @exception StateException  If activating an already active durable
   *              subscription.
   */
  private void doReact(ConsumerSubRequest req) throws StateException {
    AgentId topicId = AgentId.fromString(req.getTarget());
    String subName = req.getSubName();
    
    boolean newTopic = ! topicsTable.containsKey(topicId);
    boolean newSub = ! subsTable.containsKey(subName);

    TopicSubscription tSub;
    ClientSubscription cSub;

    // true if a SubscribeRequest has been sent to the topic. 
    boolean sent = false;

    if (newTopic) { // New topic...
      tSub = new TopicSubscription();
      topicsTable.put(topicId, tSub);
    } else { // Known topic...
      tSub = (TopicSubscription) topicsTable.get(topicId);
    }

    if (newSub) { // New subscription...
      // state change, so save.
      proxyAgent.setSave();
      cSub = new ClientSubscription(proxyAgent.getId(),
                                    activeCtxId,
                                    req.getRequestId(),
                                    req.getDurable(),
                                    topicId,
                                    req.getSubName(),
                                    req.getSelector(),
                                    req.getNoLocal(),
                                    dmqId,
                                    threshold,
                                    messagesTable);
      cSub.setProxyAgent(proxyAgent);
     
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + subName + " created.");

      subsTable.put(subName, cSub);
      try {
        MXWrapper.registerMBean(cSub, "Joram#"+AgentServer.getServerId(), getMBeanName(subName));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "  - Could not register ClientSubscriptionMbean", e);
      }
      tSub.putSubscription(subName, req.getSelector());
      sent =
        updateSubscriptionToTopic(topicId, activeCtxId, req.getRequestId(), req.isAsyncSubscription());
    }  else { // Existing durable subscription...
      cSub = (ClientSubscription) subsTable.get(subName);

      if (cSub.getActive())
        throw new StateException("The durable subscription " + subName +
                                 " has already been activated.");

      // Updated topic: updating the subscription to the previous topic.
      boolean updatedTopic = ! topicId.equals(cSub.getTopicId());
      if (updatedTopic) {
        TopicSubscription oldTSub =
          (TopicSubscription) topicsTable.get(cSub.getTopicId());
        oldTSub.removeSubscription(subName);
        updateSubscriptionToTopic(cSub.getTopicId(), -1, -1, req.isAsyncSubscription());
      }

      // Updated selector?
      boolean updatedSelector;
      if (req.getSelector() == null && cSub.getSelector() != null)
        updatedSelector = true;
      else if (req.getSelector() != null && cSub.getSelector() == null)
        updatedSelector = true;
      else if (req.getSelector() == null && cSub.getSelector() == null)
        updatedSelector = false;
      else
        updatedSelector = ! req.getSelector().equals(cSub.getSelector());

      // Reactivating the subscription.
      cSub.reactivate(activeCtxId,
                      req.getRequestId(),
                      topicId,
                      req.getSelector(),
                      req.getNoLocal());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Subscription " + subName + " reactivated.");

      // Updated subscription: updating subscription to topic.  
      if (updatedTopic || updatedSelector) {
        tSub.putSubscription(subName, req.getSelector());
        sent = updateSubscriptionToTopic(topicId,
                                         activeCtxId,
                                         req.getRequestId(),
                                         req.isAsyncSubscription());
      }
    }
    // Activating the subscription.
    activeCtx.addSubName(subName);

    // Acknowledging the request, if needed.
    if (! sent)
      proxyAgent.sendNot(proxyAgent.getId(),
                         new SyncReply(activeCtxId, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerSetListRequest</code> notifying the creation of a client
   * listener.
   * <p>
   * Sets the listener for the subscription, launches a delivery sequence.
   *
   * @exception DestinationException  If the subscription does not exist.
   */
  private void doReact(ConsumerSetListRequest req) throws DestinationException
  {
    // Getting the subscription:
    String subName = req.getTarget();
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new DestinationException("Can't set a listener on the non existing subscription: " + subName);

    sub.setListener(req.getRequestId());

    ConsumerMessages consM = sub.deliver();
    if (consM != null) {
      if (activeCtx.getActivated())
        doReply(consM);
      else
        activeCtx.addPendingDelivery(consM);
    }
  }
   
  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerUnsetListRequest</code> notifying that a consumer listener
   * is unset.
   *
   * @exception DestinationException  If the subscription does not exist.
   */
  private void doReact(ConsumerUnsetListRequest req) 
    throws DestinationException {
    // If the listener was listening to a queue, cancelling any pending reply:
    if (req.getQueueMode()) {
      activeCtx.cancelReceive(req.getCancelledRequestId());
      AgentId to = AgentId.fromString(req.getTarget());
      proxyAgent.sendNot(
        to,
        new AbortReceiveRequest(activeCtx.getId(), 
                                req.getRequestId(),
                                req.getCancelledRequestId()));
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerCloseSubRequest</code> requesting to deactivate a durable
   * subscription.
   *
   * @exception DestinationException  If the subscription does not exist. 
   */
  private void doReact(ConsumerCloseSubRequest req) throws DestinationException
  {
    // Getting the subscription:
    String subName = req.getTarget();
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new DestinationException("Can't desactivate non existing subscription: " + subName);

    // De-activating the subscription:
    activeCtx.removeSubName(subName);
    sub.deactivate();

    // Acknowledging the request:
    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerUnsubRequest</code> requesting to remove a subscription.
   *
   * @exception DestinationException  If the subscription does not exist.
   */
  private void doReact(ConsumerUnsubRequest req) throws DestinationException {
    // state change, so save.
    proxyAgent.setSave();

    // Getting the subscription.
    String subName = req.getTarget();
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);
    if (sub == null)
      throw new DestinationException("Can't unsubscribe non existing subscription: " + subName);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Deleting subscription " + subName);

    // Updating the proxy's subscription to the topic.
    AgentId topicId = sub.getTopicId();
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);
    tSub.removeSubscription(subName);
    updateSubscriptionToTopic(topicId, -1, -1);

    // Deleting the subscription.
    sub.delete();
    activeCtx.removeSubName(subName);
    subsTable.remove(subName);
    try {
      MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName(subName));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "  - Problem when unregistering ClientSubscriptionMbean", e);
    }

    // Acknowledging the request:
    proxyAgent.sendNot(proxyAgent.getId(),
                       new SyncReply(activeCtxId, new ServerReply(req)));
  }

  /**
   * Method implementing the proxy reaction to a
   * <code>ConsumerReceiveRequest</code> instance, requesting a message from a
   * subscription.
   * <p>
   * This method registers the request and launches a delivery sequence. 
   *
   * @exception DestinationException  If the subscription does not exist. 
   */
  private void doReact(ConsumerReceiveRequest req) 
    throws DestinationException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyImpl.doReact(" + req + ')');
    
    String subName = req.getTarget();
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new DestinationException("Can't request a message from the unknown subscription: " + subName);

    // Getting a message from the subscription.
    sub.setReceiver(req.getRequestId(), req.getTimeToLive());
    ConsumerMessages consM = sub.deliver();

    if (consM != null && req.getReceiveAck()) {
      Vector messageList = consM.getMessages();
      for (int i = 0; i < messageList.size(); i++) {
        Message msg = (Message)messageList.elementAt(i);
        sub.acknowledge(msg.getIdentifier());
      }
    }

    // Nothing to deliver but immediate delivery request: building an empty
    // reply.
    if (consM == null && req.getTimeToLive() == -1) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> immediate delivery");
      sub.unsetReceiver();
      consM = new ConsumerMessages(req.getRequestId(), subName, false);
    }
    
    // Delivering.
    if (consM != null && activeCtx.getActivated()) {
      doReply(consM);
    } else if (consM != null) {
      activeCtx.addPendingDelivery(consM);
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>SessAckRequest</code> acknowledging messages either on a queue
   * or on a subscription.
   */
  private void doReact(SessAckRequest req) 
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      Vector ids = req.getIds();
      
      AcknowledgeRequest not =
        new AcknowledgeRequest(activeCtxId,
                               req.getRequestId(),
                               ids);
      if (qId.getTo() == proxyAgent.getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local acking");
        not.setPersistent(false);
      }
      
      proxyAgent.sendNot(qId, not);
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null)
        sub.acknowledge(req.getIds().elements());
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>SessDenyRequest</code> denying messages either on a queue or on
   * a subscription.
   */
  private void doReact(SessDenyRequest req) {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      Vector ids = req.getIds();
      proxyAgent.sendNot(qId,
                         new DenyRequest(activeCtxId, req.getRequestId(), ids));

      // Acknowledging the request unless forbidden:
      if (! req.getDoNotAck())
        proxyAgent.sendNot(proxyAgent.getId(),
                           new SyncReply(activeCtxId, new ServerReply(req)));
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null) 
        return;

      sub.deny(req.getIds().elements());

      // Launching a delivery sequence:
      ConsumerMessages consM = sub.deliver();
      // Delivering.
      if (consM != null && activeCtx.getActivated())
        doReply(consM);
      else if (consM != null)
        activeCtx.addPendingDelivery(consM);
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerAckRequest</code> acknowledging a message either on a queue
   * or on a subscription.
   */
  private void doReact(ConsumerAckRequest req)
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      AcknowledgeRequest not = new AcknowledgeRequest(activeCtxId,
                                                      req.getRequestId(),
                                                      req.getIds());
      if (qId.getTo() == proxyAgent.getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local acking");
        not.setPersistent(false);
        proxyAgent.sendNot(qId, not);
      } else {
        proxyAgent.sendNot(qId, not);
      }
    } else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        sub.acknowledge(req.getIds().elements());
      }
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerDenyRequest</code> denying a message either on a queue
   * or on a subscription.
   * <p>
   * This request is acknowledged when destinated to a queue.
   */
  private void doReact(ConsumerDenyRequest req) {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      String id = req.getId();
      proxyAgent.sendNot(qId,
                         new DenyRequest(activeCtxId, req.getRequestId(), id));

      // Acknowledging the request, unless forbidden:
      if (! req.getDoNotAck())
        proxyAgent.sendNot(proxyAgent.getId(),
                           new SyncReply(activeCtxId, new ServerReply(req)));
    } else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null) 
        return;

      Vector ids = new Vector();
      ids.add(req.getId());
      sub.deny(ids.elements());

      // Launching a delivery sequence:
      ConsumerMessages consM = sub.deliver();
      // Delivering.
      if (consM != null && activeCtx.getActivated())
        doReply(consM);
      else if (consM != null)
        activeCtx.addPendingDelivery(consM);
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a 
   * <code>TempDestDeleteRequest</code> request for deleting a temporary
   * destination.
   * <p>
   * This method sends a <code>fr.dyade.aaa.agent.DeleteNot</code> to the
   * destination and acknowledges the request.
   */
  private void doReact(TempDestDeleteRequest req) {
    // Removing the destination from the context's list:
    AgentId tempId = AgentId.fromString(req.getTarget());
    activeCtx.removeTemporaryDestination(tempId);

    // Sending the request to the destination:
    deleteTemporaryDestination(tempId);

    // Acknowledging the request:
    proxyAgent.sendNot(proxyAgent.getId(),
                       new SyncReply(activeCtxId, new ServerReply(req)));
  }

  private void deleteTemporaryDestination(AgentId destId) {
    proxyAgent.sendNot(destId, new DeleteNot());
    proxyAgent.sendNot(AdminTopic.getDefault(),
                       new RegisterTmpDestNot(destId, false, false));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XACnxPrepare</code> request holding messages and acknowledgements
   * produced in an XA transaction.
   *
   * @exception StateException  If the proxy has already received a prepare
   *                              order for the same transaction.
   */
  private void doReact(XACnxPrepare req) throws StateException {
    try {
      Xid xid = new Xid(req.getBQ(), req.getFI(), req.getGTI());
      activeCtx.registerTxPrepare(xid, req);
      doReply(new ServerReply(req));
    } catch (Exception exc) {
      throw new StateException(exc.getMessage());
    }
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XACnxCommit</code> request committing the operations performed
   * in a given transaction.
   * <p>
   * This method actually processes the objects sent at the prepare phase,
   * and acknowledges the request.
   * 
   * @exception StateException  If committing an unknown transaction.
   */
  private void doReact(XACnxCommit req) throws StateException {
    Xid xid = new Xid(req.getBQ(), req.getFI(), req.getGTI());

    XACnxPrepare prepare = activeCtx.getTxPrepare(xid);

    if (prepare == null)
      throw new StateException("Unknown transaction identifier.");

    Vector sendings = prepare.getSendings();
    Vector acks = prepare.getAcks();

    ProducerMessages pM;
    ClientMessages not;
    while (! sendings.isEmpty()) {
      pM = (ProducerMessages) sendings.remove(0);
      not = new ClientMessages(activeCtxId,
                               pM.getRequestId(),
                               pM.getMessages());
      proxyAgent.sendNot(AgentId.fromString(pM.getTarget()), not);
    }

    while (! acks.isEmpty())
      doReact((SessAckRequest) acks.remove(0));

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XACnxRollback</code> request rolling back the operations performed
   * in a given transaction.
   */
  private void doReact(XACnxRollback req) {
    Xid xid = new Xid(req.getBQ(), req.getFI(), req.getGTI());

    String queueName;
    AgentId qId;
    Vector ids;
    for (Enumeration queues = req.getQueues(); queues.hasMoreElements();) {
      queueName = (String) queues.nextElement();
      qId = AgentId.fromString(queueName);
      ids = req.getQueueIds(queueName);
      proxyAgent.sendNot(qId,
                         new DenyRequest(activeCtxId, req.getRequestId(), ids));
    }

    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    for (Enumeration subs = req.getSubs(); subs.hasMoreElements();) {
      subName = (String) subs.nextElement();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        sub.deny(req.getSubIds(subName).elements());

        consM = sub.deliver();
        if (consM != null && activeCtx.getActivated())
          doReply(consM);
        else if (consM != null)
          activeCtx.addPendingDelivery(consM);
      }
    }

   XACnxPrepare prepare = activeCtx.getTxPrepare(xid);

    if (prepare != null) {
      Vector acks = prepare.getAcks();

      SessAckRequest ack;
      while (! acks.isEmpty()) {
        ack = (SessAckRequest) acks.remove(0);
        doReact(new SessDenyRequest(ack.getTarget(),
                                    ack.getIds(),
                                    ack.getQueueMode(),
                                    true));
      }
    }

    proxyAgent.sendNot(proxyAgent.getId(),
                       new SyncReply(activeCtxId, new ServerReply(req)));
  }

  /**
   * Reacts to a <code>XACnxRecoverRequest</code> request requesting the 
   * identifiers of the prepared transactions.
   * <p>
   * Returns the identifiers of the recovered transactions, puts the prepared
   * data into the active context for future commit or rollback.
   *
   * @exception StateException  If a recovered transaction branch is already
   *                              present in the context.
   */
  private void doReact(XACnxRecoverRequest req)
    throws StateException {
    // state change, so save.
    proxyAgent.setSave();

    Vector bqs = new Vector();
    Vector fis = new Vector();
    Vector gtis = new Vector();
    if (recoveredTransactions != null) {
      Enumeration keys = recoveredTransactions.keys();
      Xid xid;
      while (keys.hasMoreElements()) {
        xid = (Xid) keys.nextElement();
        bqs.add(xid.bq);
        fis.add(new Integer(xid.fi));
        gtis.add(xid.gti);
        try {
          activeCtx.registerTxPrepare(xid,
                                      (XACnxPrepare) recoveredTransactions.remove(xid));
        }
        catch (Exception exc) {
          throw new StateException("Recovered transaction branch has already been prepared by the RM.");
        }
      }
    }
    recoveredTransactions = null;
    doReply(new XACnxRecoverReply(req, bqs, fis, gtis));
  }

  /**
   * Method implementing the reaction to a <code>SetDMQRequest</code>
   * instance setting the dead message queue identifier for this proxy
   * and its subscriptions.
   */
  private void doReact(AgentId from, SetDMQRequest not) {
    // state change, so save.
    proxyAgent.setSave();
    
    dmqId = not.getDmqId();

    for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();) 
      ((ClientSubscription) subsTable.get(keys.nextElement())).setDMQId(dmqId);

    proxyAgent.sendNot(from, new AdminReply(not, true, "DMQ set: " + dmqId));
  }

  /**
   * Method implementing the reaction to a <code>SetThreshRequest</code>
   * instance setting the threshold value for this proxy and its
   * subscriptions.
   */
  private void doReact(AgentId from, SetThreshRequest not) {
    // state change, so save.
    proxyAgent.setSave();
    
    threshold = not.getThreshold();

    for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();) 
      ((ClientSubscription)
         subsTable.get(keys.nextElement())).setThreshold(not.getThreshold());

    proxyAgent.sendNot(from,
                       new AdminReply(not,
                                      true,
                                      "Threshold set: " + threshold));
  }

  /**
   * Method implementing the reaction to a <code>SetNbMaxMsgRequest</code>
   * instance setting the NbMaxMsg value for the subscription.
   */
  protected void doReact(AgentId from, SetNbMaxMsgRequest not) {
    int nbMaxMsg = not.getNbMaxMsg();
    String subName = not.getSubName();

    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    if (sub != null) {
      sub.setNbMaxMsg(nbMaxMsg);
      proxyAgent.sendNot(from,
                         new AdminReply(not,
                                        true,
                                        "NbMaxMsg set: " + nbMaxMsg + " on " + subName));
    } else {
      proxyAgent.sendNot(from,
                         new AdminReply(not,
                                        false,
                                        "NbMaxMsg not set: " + nbMaxMsg + " on " + subName));
    }
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetNbMaxMsg</code> notification requesting the
   * number max of messages in the subscription.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetNbMaxMsg not) {
    int nbMaxMsg = -1;
    String subName = not.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    if (sub != null)
      nbMaxMsg = sub.getNbMaxMsg();

    Channel.sendTo(from, new Monit_GetNbMaxMsgRep(not,nbMaxMsg));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetDMQSettings</code>
   * instance requesting the DMQ settings of this proxy.
   */
  private void doReact(AgentId from, Monit_GetDMQSettings not)
  {
    String id = null;
    if (dmqId != null)
      id = dmqId.toString();
    proxyAgent.sendNot(from, new Monit_GetDMQSettingsRep(not, id, threshold));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SyncReply</code> notification sent by itself, wrapping a reply
   * to be sent to a client.
   */
  private void doReact(SyncReply not)
  {
    doReply(not.key, not.reply);
  }

  /**
   * The method closes a given context by denying the non acknowledged messages
   * delivered to this context, and deleting its temporary subscriptions and
   * destinations.
   */
  private void doReact(int key, CnxCloseRequest req) {
    // state change, so save.
    proxyAgent.setSave();

    //setCtx(cKey);

    // Denying the non acknowledged messages:
    AgentId id;
    boolean prepared = false;
    for (Enumeration ids = activeCtx.getDeliveringQueues(); ids.hasMoreElements();) {
      id = (AgentId) ids.nextElement();

      for (Enumeration xids = activeCtx.getTxIds(); xids.hasMoreElements();) {
        Xid xid = (Xid) xids.nextElement(); 
        if (activeCtx.isPrepared(xid)) {
          prepared = true;
          break;
        }
      }
      if (!prepared)
        proxyAgent.sendNot(id, new DenyRequest(key));
      prepared = false;
    }

    // Removing or deactivating the subscriptions:
    String subName = null;
    ClientSubscription sub;
    Vector topics = new Vector();
    for (Enumeration subs = activeCtx.getActiveSubs(); subs.hasMoreElements();) {
      subName = (String) subs.nextElement();
      sub = (ClientSubscription) subsTable.get(subName);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Deactivate subscription "
            + subName + ", topic id = " + sub.getTopicId());

      if (sub.getDurable()) {
        sub.deactivate();

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Durable subscription"
              + subName + " de-activated.");
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> topicsTable = "
              + topicsTable);

        sub.delete();
        subsTable.remove(subName);
        try {
          MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName(subName));
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "  - Problem when unregistering ClientSubscriptionMbean", e);
        }
        TopicSubscription tSub = (TopicSubscription) topicsTable.get(sub
            .getTopicId());
        tSub.removeSubscription(subName);

        if (!topics.contains(sub.getTopicId()))
          topics.add(sub.getTopicId());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Temporary subscription"
              + subName + " deleted.");
      }
    }
    // Browsing the topics which at least have one subscription removed.
    for (Enumeration topicIds = topics.elements(); topicIds.hasMoreElements();)
      updateSubscriptionToTopic((AgentId) topicIds.nextElement(), -1, -1);

    // Deleting the temporary destinations:
    AgentId destId;
    for (Enumeration dests = activeCtx.getTempDestinations(); dests
        .hasMoreElements();) {
      destId = (AgentId) dests.nextElement();
      activeCtx.removeTemporaryDestination(destId);
      deleteTemporaryDestination(destId);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Deletes temporary"
            + " destination " + destId.toString());
    }

    // Saving the prepared transactions.
    Enumeration xids = activeCtx.getTxIds();
    Xid xid;
    XACnxPrepare recoveredPrepare;
    XACnxPrepare prepare;
    while (xids.hasMoreElements()) {
      if (recoveredTransactions == null)
        recoveredTransactions = new Hashtable();

      xid = (Xid) xids.nextElement();

      recoveredPrepare = (XACnxPrepare) recoveredTransactions.get(xid);
      prepare = activeCtx.getTxPrepare(xid);

      if (recoveredPrepare == null)
        recoveredTransactions.put(xid, prepare);
      else {
        recoveredPrepare.getSendings().addAll(prepare.getSendings());
        recoveredPrepare.getAcks().addAll(prepare.getAcks());
      }
    }

    // Finally, deleting the context:
    contexts.remove(new Integer(key));
    activeCtx = null;
    setActiveCtxId(-1);

    CnxCloseReply reply = new CnxCloseReply();
    reply.setCorrelationId(req.getRequestId());
    proxyAgent.sendToClient(key, reply);
  }

  private void doReact(int key, ActivateConsumerRequest req) {
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    sub.setActive(req.getActivate());
  }
  
  private void doReact(int key, CommitRequest req) {
    // The commit may involve some local agents
    int asyncReplyCount = 0;
    
    Enumeration pms = req.getProducerMessages();
    if (pms != null) {
      while (pms.hasMoreElements()) {
        ProducerMessages pm = (ProducerMessages) pms.nextElement();
        AgentId destId = AgentId.fromString(pm.getTarget());
        ClientMessages not = new ClientMessages(key, 
            req.getRequestId(), pm.getMessages());
        setDmq(not);    
        if (destId.getTo() == proxyAgent.getId().getTo()) {
          // local sending
          not.setPersistent(false);
          if (req.getAsyncSend()) {
            not.setAsyncSend(true);
          } else {
            asyncReplyCount++;
          }
        }
        proxyAgent.sendNot(destId, not);
      }
    }
    
    Enumeration acks = req.getAckRequests();
    if (acks != null) {
      while (acks.hasMoreElements()) {
        SessAckRequest sar = (SessAckRequest) acks.nextElement();
        if (sar.getQueueMode()) {
          AgentId qId = AgentId.fromString(sar.getTarget());
          Vector ids = sar.getIds();
          AcknowledgeRequest not = new AcknowledgeRequest(activeCtxId, req
              .getRequestId(), ids);
          if (qId.getTo() == proxyAgent.getId().getTo()) {
            // local sending
            not.setPersistent(false);
            // No reply to wait for
          }

          proxyAgent.sendNot(qId, not);
        } else {
          String subName = sar.getTarget();
          ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
          if (sub != null) {
            sub.acknowledge(sar.getIds().elements());
            proxyAgent.setSave();
          }
        }
      }
    }
   
    if (!req.getAsyncSend()) {
      if (asyncReplyCount == 0) {
        proxyAgent.sendNot(proxyAgent.getId(), new SendReplyNot(key, req
            .getRequestId()));
      } else {
        // we need to wait for the replies
        // from the local agents
        // before replying to the client.
        activeCtx.addMultiReplyContext(req.getRequestId(), asyncReplyCount);
      }
    }
    // else the client doesn't expect any ack
  }

  /**
   * Distributes the JMS replies to the appropriate reactions.
   * <p>
   * JMS proxies react the following replies:
   * <ul>
   * <li><code>QueueMsgReply</code></li>
   * <li><code>BrowseReply</code></li>
   * <li><code>SubscribeReply</code></li>
   * <li><code>TopicMsgsReply</code></li>
   * <li><code>ExceptionReply</code></li>
   * </ul>
   */
  private void doFwd(AgentId from, AbstractReply rep) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " got " + rep.getClass().getName() +
                 " with id: " + rep.getCorrelationId() + " from: " + from);

    if (rep instanceof QueueMsgReply)
      doFwd(from, (QueueMsgReply) rep);
    else if (rep instanceof BrowseReply)
      doFwd((BrowseReply) rep);
    else if (rep instanceof SubscribeReply)
      doFwd((SubscribeReply) rep);
    else if (rep instanceof TopicMsgsReply)
      doFwd(from, (TopicMsgsReply) rep);
    else if (rep instanceof ExceptionReply)
      doReact(from, (ExceptionReply) rep);
    else {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Unexpected reply: " + rep);
    }
  }


  /**
   * Actually forwards a <code>QueueMsgReply</code> coming from a destination
   * as a <code>ConsumerMessages</code> destinated to the requesting client.
   * <p>
   * If the corresponding context is stopped, stores the
   * <code>ConsumerMessages</code> for later delivery.
   */
  private void doFwd(AgentId from, QueueMsgReply rep) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.doFwd(" + from + ',' + rep + ')');
    
    try {
      // Updating the active context:
      setCtx(rep.getClientContext());

      // If the receive request being replied has been cancelled, denying
      // the message.
      if (rep.getCorrelationId() == activeCtx.getCancelledReceive()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
            " -> cancelled receive: id=" + activeCtx.getCancelledReceive());

        if (rep.getSize() > 0) {
          Vector msgList = rep.getMessages();
          for (int i = 0; i < msgList.size(); i++) {
            Message msg = new Message((org.objectweb.joram.shared.messages.Message) msgList.elementAt(i));
            String msgId = msg.getIdentifier();
            
            if (logger.isLoggable(BasicLevel.INFO))
              logger.log(BasicLevel.INFO, " -> denying message: " + msgId);
            
            proxyAgent.sendNot(from,
                               new DenyRequest(0, rep.getCorrelationId(), msgId));
          }
        }
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> reply");

        ConsumerMessages jRep;

        // Building the reply and storing the wrapped message id for later
        // denying in the case of a failure:
        if (rep.getSize() > 0) {
          jRep = new ConsumerMessages(
            rep.getCorrelationId(),
            rep.getMessages(),
            from.toString(),
            true);
          activeCtx.addDeliveringQueue(from);
        } else {
          jRep = new ConsumerMessages(
              rep.getCorrelationId(),
              (Vector)null,
              from.toString(),
              true);
        }

        // If the context is started, delivering the message, or buffering it:
        if (activeCtx.getActivated()) {
          doReply(jRep);
        } else {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, " -> buffer the reply");
          activeCtx.addPendingDelivery(jRep);
        }
      }
    } catch (StateException pE) {
      // The context is lost: denying the message:
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", pE);
      if (rep.getMessages().size() > 0) {
        Vector msgList = rep.getMessages();
        for (int i = 0; i < msgList.size(); i++) {
          Message msg = new Message((org.objectweb.joram.shared.messages.Message) msgList.elementAt(i));
          String msgId = msg.getIdentifier();
          
          if (logger.isLoggable(BasicLevel.INFO))
            logger.log(BasicLevel.INFO,  "Denying message: " + msgId);
          
          proxyAgent.sendNot(from,
                             new DenyRequest(0, rep.getCorrelationId(), msgId));
        }
      }
    }
  }


  /**
   * Actually forwards a <code>BrowseReply</code> coming from a
   * destination as a <code>QBrowseReply</code> destinated to the
   * requesting client.
   */
  private void doFwd(BrowseReply rep) {
    try {
      // Updating the active context:
      setCtx(rep.getClientContext());
      doReply(new QBrowseReply(rep.getCorrelationId(), 
                               rep.getMessages()));
    } catch (StateException pE) {
      // The context is lost; nothing to do.
    }
  }

  /**
   * Forwards the topic's <code>SubscribeReply</code> as a
   * <code>ServerReply</code>.
   */
  private void doFwd(SubscribeReply rep) {
    try {
      setCtx(rep.getClientContext());
      doReply(new ServerReply(rep.getCorrelationId()));
    } catch (StateException pE) {
      // The context is lost; nothing to do.
    }
  }

  transient String msgTxname = null;

  protected final String getMsgTxname() {
    if (msgTxname == null)
      msgTxname = 'M' + proxyAgent.getId().toString() + '_';
    return msgTxname;
  }

  protected final void setMsgTxName(Message msg) {
    if (msg.getTxName() == null)
      msg.setTxName(getMsgTxname() + msg.order);
  }

  /**
   * Method implementing the proxy reaction to a <code>TopicMsgsReply</code>
   * holding messages published by a topic.
   */
  private void doFwd(AgentId from, TopicMsgsReply rep) {
    // Browsing the target subscriptions:
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(from);
    if (tSub == null || tSub.isEmpty()) return;

    String subName;
    ClientSubscription sub;

    // AF: TODO we should parse each message for each subscription
    // see ClientSubscription.browseNewMessages
    Vector messages = new Vector();
    for (Enumeration msgs = rep.getMessages().elements();
         msgs.hasMoreElements();) {
      Message message = new Message((org.objectweb.joram.shared.messages.Message) msgs.nextElement());
      // Setting the arrival order of the messages
      message.order = arrivalsCounter++;
      messages.add(message);
    }

    for (Enumeration names = tSub.getNames(); names.hasMoreElements();) {
      subName = (String) names.nextElement();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // Browsing the delivered messages.
      sub.browseNewMessages(messages);
    }

    // Save message if it is delivered to a durable subscription.
    for (Enumeration msgs = messages.elements(); msgs.hasMoreElements();) { 
      Message message = (Message) msgs.nextElement();
      
      if (message.durableAcksCounter > 0) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> save message " + message);
        proxyAgent.setSave();
        // Persisting the message.
        setMsgTxName(message);
        message.save();
        message.releaseFullMessage();
      }
    } 

    for (Enumeration names = tSub.getNames(); names.hasMoreElements();) {
      subName = (String) names.nextElement();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // If the subscription is active, launching a delivery sequence.
      if (sub.getActive()) {
        ConsumerMessages consM = sub.deliver();
        
        if (consM != null) {
          try {
            setCtx(sub.getContextId());
            if (activeCtx.getActivated())
              doReply(consM);
            else
              activeCtx.addPendingDelivery(consM);
          } catch (StateException pE) {
            // The context is lost: nothing to do.
          }
        }
      }
    }
  }

  /**
   * Actually forwards an <code>ExceptionReply</code> coming from a destination
   * as a <code>MomExceptionReply</code> destinated to the requesting client.
   * <p>
   * If the wrapped exception is an <code>AccessException</code> thrown by
   * a <code>Topic</code> as a reply to a <code>SubscribeRequest</code>,
   * removing the corresponding subscriptions.
   */
  private void doReact(AgentId from, ExceptionReply rep) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.doReact(" + from + ',' + rep + ')');
    MomException exc = rep.getException();

    // The exception comes from a topic refusing the access: deleting the subs.
    if (exc instanceof AccessException) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + from + ')');
      TopicSubscription tSub = (TopicSubscription) topicsTable.remove(from);
      if (tSub != null) {
        String name;
        ClientSubscription sub;
        for (Enumeration e = tSub.getNames(); e.hasMoreElements();) { 
          name = (String) e.nextElement();
          sub = (ClientSubscription) subsTable.remove(name);
          try {
            MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName(name));
          } catch (Exception e1) {
            if (logger.isLoggable(BasicLevel.WARN))
              logger.log(BasicLevel.WARN, "  - Problem when unregistering ClientSubscriptionMbean", e1);
          }
          sub.delete();

          try {
            setCtx(sub.getContextId());
            activeCtx.removeSubName(name);
            doReply(new MomExceptionReply(rep.getCorrelationId(), exc));
          } catch (StateException pExc) {}
        }
        return;
      }
    }
    // Forwarding the exception to the client.
    try {
      setCtx(rep.getClientContext());
      doReply(new MomExceptionReply(rep.getCorrelationId(), exc));
    } catch (StateException pExc) {}
  }
  
  private String getMBeanName(String name) {
    return new StringBuffer().append("type=User").append(",name=").append(
        (proxyAgent.getName() == Agent.nullName) ? proxyAgent.getId().toString() : proxyAgent.getName())
        .append(",sub=").append(name).toString();
  }

  /** 
   * An <code>AdminReply</code> acknowledges the setting of a temporary
   * destination; nothing needs to be done.
   */
  private void doReact(AdminReply reply)
  {}

  /**
   * Method implementing the JMS proxy reaction to an <code>UnknownAgent</code>
   * notification notifying that a destination does not exist or is deleted.
   * <p>
   * If it notifies of a deleted topic, the method removes the 
   * corresponding subscriptions. If the wrapped request is messages sending,
   * the messages are sent to the DMQ.
   * <p>
   * A <code>JmsExceptReply</code> is sent to the concerned requester.
   * <p>
   * This case might also happen when sending a <code>ClientMessages</code>
   * to a dead message queue. In that case, the invalid DMQ identifier is set
   * to null.
   */
  private void doReact(UnknownAgent uA) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyImpl.doReact(" + uA + ')');
    Notification not = uA.not;
    AgentId agId = uA.agent;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO,
                 "--- " + this + " notified of invalid destination: " + agId.toString());
    
    // The deleted destination is a topic: deleting its subscriptions.
    if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   " -> topicsTable.remove(" + agId + ')');
    TopicSubscription tSub = (TopicSubscription) topicsTable.remove(agId);
    if (tSub != null) {
      String name;
      ClientSubscription sub;
      DestinationException exc;
      exc = new DestinationException("Destination " + agId +
                                     " does not exist.");
      for (Enumeration e = tSub.getNames(); e.hasMoreElements();) { 
        name = (String) e.nextElement();
        sub = (ClientSubscription) subsTable.remove(name);
        try {
          MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName(name));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "  - Problem when unregistering ClientSubscriptionMbean", e1);
        }
        sub.delete();

        try {
          setCtx(sub.getContextId());
          activeCtx.removeSubName(name);
          doReply(new MomExceptionReply(sub.getSubRequestId(), exc));
        } catch (StateException pExc) {}
      }
      return;
    }

    if (not instanceof AbstractRequest) {
      AbstractRequest req = (AbstractRequest) not;

      // If the wrapped request is messages sending,forwarding them to the DMQ:
      if (req instanceof ClientMessages) {
        // If the queue actually was a dead message queue, updating its
        // identifier:
        if (dmqId != null && agId.equals(dmqId)) {
          // state change, so save.
          proxyAgent.setSave();
          dmqId = null;
          for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();)
            ((ClientSubscription) subsTable.get(keys.nextElement())).setDMQId(null);
        }
        // Sending the messages again if not coming from the default DMQ:
        if (QueueImpl.getDefaultDMQId() != null && !agId.equals(QueueImpl.getDefaultDMQId())) {
          DMQManager dmqManager = new DMQManager(dmqId, null);
          Enumeration msgs = ((ClientMessages) req).getMessages().elements();
          while (msgs.hasMoreElements()) {
            org.objectweb.joram.shared.messages.Message msg = (org.objectweb.joram.shared.messages.Message) msgs.nextElement();
            nbMsgsSentToDMQSinceCreation++;
            dmqManager.addDeadMessage(msg, MessageErrorConstants.DELETED_DEST);
          }
          dmqManager.sendToDMQ();
        }

        DestinationException exc;
        exc = new DestinationException("Destination " + agId + " does not exist.");
        MomExceptionReply mer = new MomExceptionReply(req.getRequestId(), exc);
        try {
          setCtx(req.getClientContext());
          // Contrary to a receive, send the error even if the
          // connection is not started.
          doReply(mer);
        } catch (StateException se) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "", se);          
          // Do nothing (the context doesn't exist any more).
        }
      } else if (req instanceof ReceiveRequest) {
        DestinationException exc = new DestinationException(
          "Destination " + agId + " does not exist.");
        MomExceptionReply mer = new MomExceptionReply(
          req.getRequestId(), exc);
        try {
          setCtx(req.getClientContext());
          if (activeCtx.getActivated()) {
            doReply(mer);
          } else {
            activeCtx.addPendingDelivery(mer);
          }
        } catch (StateException se) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "", se);          
          // Do nothing (the contexte doesn't exist any more).
        }
      }
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "Connection " + req.getClientContext() +
                   " notified of the deletion of destination " + agId);
    }
  }

  private void doReact(UserAdminRequestNot not) {
    org.objectweb.joram.shared.admin.AdminRequest adminRequest = 
      not.getRequest();
    if (adminRequest instanceof GetSubscriptions) {
      doReact((GetSubscriptions)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscriptionMessageIds) {
      doReact((GetSubscriptionMessageIds)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscriptionMessage) {
      doReact((GetSubscriptionMessage)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof DeleteSubscriptionMessage) {
      doReact((DeleteSubscriptionMessage)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscription) {
      doReact((GetSubscription)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof ClearSubscription) {
      doReact((ClearSubscription)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    }
  }

  private void doReact(GetSubscriptions request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    Enumeration keys = subsTable.keys();
    Enumeration values = subsTable.elements();
    String[] subNames = new String[subsTable.size()];
    String[] topicIds = new String[subsTable.size()];
    int[] messageCounts = new int[subsTable.size()];
    boolean[] durable = new boolean[subsTable.size()];
    int i = 0;
    while (keys.hasMoreElements()) {
      subNames[i] = (String)keys.nextElement();
      ClientSubscription cs = 
        (ClientSubscription)values.nextElement();
      topicIds[i] = cs.getTopicId().toString();
      messageCounts[i] = cs.getPendingMessageCount();
      durable[i] = cs.getDurable();
      i++;
    }
    GetSubscriptionsRep reply = new GetSubscriptionsRep(
      subNames, topicIds, messageCounts, durable);
    replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
  }

  /**
   * Returns the list of subscriptions for this user. Each subscription is
   * identified by its unique 'symbolic' name.
   *
   * @return The list of subscriptions for this user.
   */
  public String[] getSubscriptionNames() {
    Enumeration keys = subsTable.keys();
    String[] res = new String[subsTable.size()];
    int i = 0;
    while (keys.hasMoreElements()) {
      res[i] = (String)keys.nextElement();
      i++;
    }
    return res;
  }

  private void doReact(GetSubscriptionMessageIds request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription)subsTable.get(subName);
    }
    if (cs != null) {
      GetSubscriptionMessageIdsRep reply = 
        new GetSubscriptionMessageIdsRep(
          cs.getMessageIds());
      replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Subscription not found: " + 
          request.getSubscriptionName()), 
        replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(GetSubscription request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription)subsTable.get(subName);
    }
    if (cs != null) {
      GetSubscriptionRep reply = 
        new GetSubscriptionRep(
          cs.getTopicId().toString(),
          cs.getPendingMessageCount(),
          cs.getDurable());
      replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Subscription not found: " + 
          request.getSubscriptionName()), 
        replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(GetSubscriptionMessage request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    ClientSubscription cs = null;
    String subName = request.getSubscriptionName();
    if (subName != null) {
      cs = (ClientSubscription)subsTable.get(subName);
    }
    if (cs != null) {
      String msgId = request.getMessageId();
      
      Message message = null;
      if (msgId != null)
        message = cs.getSubscriptionMessage(msgId);

      if (message != null) {
        GetSubscriptionMessageRep reply = null;
        if (request.getFullMessage()) {
          reply = new GetSubscriptionMessageRep(message.getFullMessage());
        } else {
          reply = new GetSubscriptionMessageRep(message.getHeaderMessage());
        }
        replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
      } else {
        replyToTopic(
          new org.objectweb.joram.shared.admin.AdminReply(
            false, "Message not found: " + 
            request.getMessageId()), 
          replyTo, requestMsgId, replyMsgId);
      }
    } else {
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Subscription not found: " + 
          subName),
        replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(DeleteSubscriptionMessage request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription)subsTable.get(subName);
    }
    if (cs != null) {
      cs.deleteMessage(request.getMessageId());
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          true, null),
        replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Subscription not found: " + 
          request.getSubscriptionName()),
        replyTo, requestMsgId, replyMsgId);
    }
  }

  /**
   * Deletes a particular pending message in a subscription.
   * The subscription is identified  by its unique name, the message is pointed
   * out through its unique identifier.
   *
   * @param subName  The subscription unique name.
   * @param msgId    The unique message's identifier.
   */
  public void deleteSubscriptionMessage(String subName,
                                        String msgId) {
    ClientSubscription cs = (ClientSubscription)subsTable.get(subName);
    if (cs != null) {
      cs.deleteMessage(msgId);
    }
  }
  
  private void doReact(ClearSubscription request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription) subsTable.get(subName);
    }
    if (cs != null) {
      cs.clear();
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          true, null),
        replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Subscription not found: " + 
          request.getSubscriptionName()),
        replyTo, requestMsgId, replyMsgId);
    }
  }

  private void replyToTopic(
    org.objectweb.joram.shared.admin.AdminReply reply,
    AgentId replyTo,
    String requestMsgId,
    String replyMsgId) {
    org.objectweb.joram.shared.messages.Message message = new org.objectweb.joram.shared.messages.Message();
    message.correlationId = requestMsgId;
    message.timestamp = System.currentTimeMillis();
    message.setDestination(replyTo.toString(), Topic.TOPIC_TYPE);
    message.id = replyMsgId;
    try {
      message.setAdminMessage(reply);
      ClientMessages clientMessages = new ClientMessages(-1, -1, message);
      Channel.sendTo(replyTo, clientMessages);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
      throw new Error(exc.getMessage());
    }
  }

  /**
   * Updates the reference to the active context.
   *
   * @param key  Key of the activated context.
   *
   * @exception StateException  If the context has actually been closed or
   *              lost.
   */
  private void setCtx(int key) throws StateException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyImpl.setCtx(" + key + ')');

    if (key < 0) throw new StateException("Invalid context: " + key);

    // If the required context is the last used, no need to update the
    // references:
    if (key == activeCtxId) return;

    // Else, updating the activeCtx reference:
    setActiveCtxId(key);
    activeCtx = (ClientContext) contexts.get(new Integer(key));

    // If context not found, throwing an exception:
    if (activeCtx == null) {
      setActiveCtxId(-1);
      activeCtx = null;
      throw new StateException("Context " + key + " is closed or broken.");
    }
  }
 
  /**
   * Method used for sending an <code>AbstractJmsReply</code> back to an
   * external client within the active context.
   *
   * @param rep  The reply to send.
   */
  private void doReply(AbstractJmsReply reply) {
    doReply(activeCtxId, reply);
  }

  ClientContext getClientContext(int ctxId) {
    return (ClientContext)contexts.get(
      new Integer(ctxId));
  }

  /**
   * Method used for sending an <code>AbstractJmsReply</code> back to an
   * external client through a given context.
   *
   * @param key  The context through witch replying.
   * @param rep  The reply to send.
   */
  private void doReply(int key, AbstractJmsReply reply) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.doReply(" + key + ',' + reply + ')');
    proxyAgent.sendToClient(key, reply);
  }

  void cleanPendingMessages(long currentTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.cleanPendingMessages(" + messagesTable.size() + ')');

    
    String id = null;
    Message message = null;
    DMQManager dmqManager = null;

    for (Enumeration ids = messagesTable.keys(); ids.hasMoreElements(); ) {
      id = (String) ids.nextElement();
      message = (Message) messagesTable.get(id);
      if ((message == null) || message.isValid(currentTime))
        continue;

      messagesTable.remove(id);
      if (message.durableAcksCounter > 0)
        message.delete();

      if (dmqManager == null)
        dmqManager = new DMQManager(dmqId, null);
      nbMsgsSentToDMQSinceCreation++;
      dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "ProxyImpl expired message " + message.getIdentifier());
    }
    
    Enumeration en = subsTable.elements();
    while (en.hasMoreElements()) {
      ((ClientSubscription) en.nextElement()).cleanMessageIds();
    }
    
    // If needed, sending the dead messages to the DMQ:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl.cleanPendingMessages -> " + messagesTable.size());
  }

  /**
   * This method deletes the proxy by notifying its connected clients,
   * denying the non acknowledged messages, deleting the temporary
   * destinations, removing the subscriptions.
   *
   * @exception Exception  If the requester is not an administrator.
   */
  public void deleteProxy(AgentId from) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " notified to be deleted.");

    if (! from.equals(AdminTopic.getDefault()))
      throw new Exception("Unauthorized deletion of proxy");

    // Notifying the connected clients:
    Enumeration keys = contexts.keys();
    int key;
    while (keys.hasMoreElements()) {
      key = ((Integer) keys.nextElement()).intValue();
      try {
        setCtx(key);

        doReply(new MomExceptionReply(new StateException("Client proxy is deleted.")));

        // Denying the non acknowledged messages:
        AgentId id;
        for (Enumeration ids = activeCtx.getDeliveringQueues();
             ids.hasMoreElements();) {
          id = (AgentId) ids.nextElement();
          proxyAgent.sendNot(id, new DenyRequest(activeCtxId));
        }

        // Deleting the temporary destinations:
        AgentId destId;
        for (Enumeration dests = activeCtx.getTempDestinations();
             dests.hasMoreElements();) {
          destId = (AgentId) dests.nextElement();
          deleteTemporaryDestination(destId);
  
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,
                       "Sending DeleteNot to temporary destination " +
                       destId.toString());
        }
      } catch (StateException pE) {}
    }

    // Removing all proxy's subscriptions:
    AgentId destId;
    for (Enumeration topics = topicsTable.keys(); topics.hasMoreElements();) {
      destId = (AgentId) topics.nextElement();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + destId + ')');
      topicsTable.remove(destId);
      updateSubscriptionToTopic(destId, -1, -1);
    }
  }

  /**
   * Updates the proxy's subscription to a topic.
   *
   * @param topicId  Identifier of the topic to subscribe to.
   * @param contextId  Identifier of the subscription context.
   * @param requestId  Identifier of the subscription request.
   *
   * @return  <code>true</code> if a <code>SubscribeRequest</code> has been
   *          sent to the topic.
   */
  private boolean updateSubscriptionToTopic(AgentId topicId,
      int contextId,
      int requestId) {
    return updateSubscriptionToTopic(topicId, contextId, requestId, false);
  }
  
  /**
   * Updates the proxy's subscription to a topic.
   *
   * @param topicId  Identifier of the topic to subscribe to.
   * @param contextId  Identifier of the subscription context.
   * @param requestId  Identifier of the subscription request.
   * @param asyncSub   asynchronous subscription request.
   *
   * @return  <code>true</code> if a <code>SubscribeRequest</code> has been
   *          sent to the topic.
   */
  private boolean updateSubscriptionToTopic(AgentId topicId,
                                            int contextId,
                                            int requestId,
                                            boolean asyncSub)
  {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ProxyImpl.updateSubscriptionToTopic(" +
                 topicId + ',' + contextId + ',' + requestId + ',' + asyncSub +')');
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);

    // No more subs to this topic: unsubscribing.
    if (tSub == null || tSub.isEmpty()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   " -> topicsTable.remove(" + topicId + ')');
      topicsTable.remove(topicId);
      proxyAgent.sendNot(topicId,
                         new UnsubscribeRequest(contextId, requestId));
      return false;
    }

    // Otherwise, updating the subscription if the selector evolved.
    String builtSelector = tSub.buildSelector();
    if (tSub.getLastSelector() != null
        && builtSelector.equals(tSub.getLastSelector()))
      return false;

    tSub.setLastSelector(builtSelector);
    SubscribeRequest req = new SubscribeRequest(contextId, requestId, builtSelector, asyncSub);
    proxyAgent.sendNot(topicId, req);
    
    // send reply if asynchronous subscription request.
    if (asyncSub) {
      doFwd(new SubscribeReply(req));
    }
    
    return true;
  }


  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl[" + proxyAgent.getId() + "].readbag()");

    activeCtxId = in.readInt();
    /* // Orders elements is unknown, not use read bag in the same order
       Enumeration elements = contexts.elements();
       while (elements.hasMoreElements()) {
          ClientContext cc = (ClientContext)elements.nextElement();
          cc.setProxyAgent(proxyAgent);
          cc.readBag(in);
       }
       elements = subsTable.elements();
       while (elements.hasMoreElements()) {
          ClientSubscription cs = (ClientSubscription)elements.nextElement();
          cs.setProxyAgent(proxyAgent);
          cs.readBag(in);
       }*/
    /*** part modified */
    int size = in.readInt();
    Object obj=null;
    for(int j=0;j<size;j++){
	obj=in.readObject();
	ClientContext cc = (ClientContext) contexts.get(obj);
	cc.setProxyAgent(proxyAgent);
	cc.readBag(in);
    }
    size = in.readInt();
    for(int j=0;j<size;j++){
	obj=in.readObject();
	ClientSubscription cs = (ClientSubscription) subsTable.get(obj);
	cs.setProxyAgent(proxyAgent);
	cs.readBag(in);
    }
    /*** end part modified */  
    
    activeCtx = (ClientContext)contexts.get(
      new Integer(activeCtxId));

    Vector messages = (Vector)in.readObject();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> messages = " + messages);
    
    topicsTable = new Hashtable();
    messagesTable = new Hashtable();

    Vector topics = new Vector();
    TopicSubscription tSub;


    for (Enumeration subNames = subsTable.keys();
         subNames.hasMoreElements();) {
      String subName = (String) subNames.nextElement();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> subName = " + subName);
      
      ClientSubscription cSub = (ClientSubscription) subsTable.get(subName);
      AgentId destId = cSub.getTopicId();
      if (! topics.contains(destId))
        topics.add(destId);
      cSub.reinitialize(messagesTable, 
                        messages,
                        false);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> destId = " + destId + ')');
      
      tSub = (TopicSubscription) topicsTable.get(destId);
      if (tSub == null) {
        tSub = new TopicSubscription();
        topicsTable.put(destId, tSub);
      }
      tSub.putSubscription(subName, cSub.getSelector());
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable = " + topicsTable);

    // DF: seems not useful here
    // for (Enumeration topicIds = topics.elements();
//          topicIds.hasMoreElements();) {
//       updateSubscriptionToTopic((AgentId) topicIds.nextElement(), -1, -1);
//     }
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ProxyImpl[" + proxyAgent.getId() + "].writeBag()");

    out.writeInt(activeCtxId);
    
    /*  Enumeration elements = contexts.elements();
	while (elements.hasMoreElements()) {
	   ((ClientContext)elements.nextElement()).writeBag(out);
	}
	elements = subsTable.elements();
	while (elements.hasMoreElements()) {
	   ((ClientSubscription)elements.nextElement()).writeBag(out);
	}*/
    /*** part modified */
    // the number of keys in contexts hashtable
    out.writeInt(contexts.size());
    Enumeration elements = contexts.keys();
    Object obj=null;
    while (elements.hasMoreElements()) {
	obj=elements.nextElement();
	out.writeObject(obj);
	((ClientContext) contexts.get(obj)).writeBag(out);
    }
    // the number of keys in subsTable hashtable
    out.writeInt(subsTable.size());
       elements = subsTable.keys();
    while (elements.hasMoreElements()) {
	obj=elements.nextElement();
	out.writeObject(obj);
	((ClientSubscription) subsTable.get(obj)).writeBag(out);
    }
    /*** end part modified */

    Vector messages = new Vector();
    elements = messagesTable.elements();
    while (elements.hasMoreElements()) {
      messages.addElement(elements.nextElement());
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> messages = " + messages + ')');

    out.writeObject(messages);
  
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }
}

/**
 * The <code>Xid</code> internal class is a utility class representing
 * a global transaction identifier.
 */
class Xid implements java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  byte[] bq;
  int fi;
  byte[] gti;


  Xid(byte[] bq, int fi, byte[] gti)
  {
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }
  
  public boolean equals(Object o)
  {
    if (! (o instanceof Xid))
      return false;

    Xid other = (Xid) o;

    return java.util.Arrays.equals(bq, other.bq)
           && fi == other.fi
           && java.util.Arrays.equals(gti, other.gti);
  }

  public int hashCode()
  {
    return (new String(bq) + "-" + new String(gti)).hashCode();
  }  
}
