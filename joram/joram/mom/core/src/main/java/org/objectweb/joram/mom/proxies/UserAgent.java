/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Vector;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.dest.AdminTopic.DestinationDesc;
import org.objectweb.joram.mom.dest.Destination;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.messages.MemoryController;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AbortReceiveRequest;
import org.objectweb.joram.mom.notifications.AbstractReplyNot;
import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.AdminReplyNot;
import org.objectweb.joram.mom.notifications.BrowseReply;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.SubscribeReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.InterceptorsHelper;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.mom.util.MessageInterceptor;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminCommandRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetSubscription;
import org.objectweb.joram.shared.admin.GetSubscriptionMessage;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIds;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageRep;
import org.objectweb.joram.shared.admin.GetSubscriptionRep;
import org.objectweb.joram.shared.admin.GetSubscriptions;
import org.objectweb.joram.shared.admin.GetSubscriptionsRep;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.SetThresholdRequest;
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
import org.objectweb.joram.shared.client.SessCreateDestReply;
import org.objectweb.joram.shared.client.SessCreateDestRequest;
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
import org.objectweb.joram.shared.messages.MessageHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.WakeUpTask;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodedString;
import fr.dyade.aaa.common.encoding.Encoder;
import fr.dyade.aaa.util.Transaction;
import fr.dyade.aaa.util.TransactionObject;
import fr.dyade.aaa.util.TransactionObjectFactory;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>UserAgent</code> class implements the MOM proxy behaviour,
 * basically forwarding client requests to MOM destinations and MOM
 * destinations replies to clients.
 */ 
public final class UserAgent extends Agent implements UserAgentMBean, ProxyAgentItf {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(UserAgent.class.getName());
  
  // JORAM_PERF_BRANCH
  public static final boolean DIRECT_QUEUE_DELIVER = true;

  /** the in and out interceptors list. */
  private String interceptors_in = null;
  private String interceptors_out = null;
  private transient List interceptorsOUT = null;
  private transient List interceptorsIN = null;
    
  /** period to run the cleaning task, by default 60s. */
  private long period = 60000L;
  
  /** the number of erroneous messages forwarded to the DMQ */
  private long nbMsgsSentToDMQSinceCreation = 0;
  
  // JORAM_PERF_BRANCH
  private transient List<ClientContext> modifiedClientContexts;
  private transient List<ClientSubscription> modifiedClientSubscriptions;

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
    if (this.period != period) {
      // Schedule the task.
      WakeUpNot not = new WakeUpNot();
      not.update = true;
      Channel.sendTo(getId(), not);
      this.period = period;
    }
  }

  /**
   * Identifier of this proxy dead message queue, <code>null</code> for DMQ
   * not set.
   */
  private AgentId dmqId = null;
  
  /**
   * Returns the default DMQ for subscription of this user.
   * @return  the default DMQ for subscription of this user.
   */
  public String getDMQId() {
    if (dmqId != null) return dmqId.toString();
    return null;
  }
  
  /**
   *  Threshold above which messages are considered as undeliverable because
   * constantly denied.
   *  This value is used as default value at subscription creation.
   *  0 stands for no threshold, -1 for value not set (use server' default value).
   */
  private int threshold = -1; 

  /**
   * Returns the default threshold for the subscription of this user.
   * 0 stands for no threshold, -1 for value not set.
   *
   * @return the maximum number of message if set; -1 otherwise.
   */
  public int getThreshold() {
    return threshold;
  }

  /**
   * Sets the default threshold for the subscription of this user.
   * 0 stands for no threshold, -1 for value not set.
   *  
   * @param threshold the threshold to set.
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  /**
   *  Maximum number of Message store in subscriptions (-1 set no limit).
   *  This value is used as default value at subscription creation.
   */
  private int nbMaxMsg = -1;

  /**
   * Returns the default maximum number of message for the subscription of this user.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message if set; -1 otherwise.
   */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /**
   * Sets the maximum number of message for the subscription of this user.
   *
   * @param nbMaxMsg the maximum number of message (-1 set no limit).
   */
  public void setNbMaxMsg(int nbMaxMsg) {
    this.nbMaxMsg = nbMaxMsg;
  }

  /**
   * Table of the proxy's <code>ClientContext</code> instances.
   * <p>
   * <b>Key:</b> context identifier<br>
   * <b>Value:</b> context
   */
  private Map<Integer, ClientContext> contexts;

  /**
   * Table holding the <code>ClientSubscription</code> instances.
   * <p>
   * <b>Key:</b> subscription name<br>
   * <b>Value:</b> client subscription
   */

  private Map<EncodedString, ClientSubscription> subsTable;

  /**
   * Table holding the recovered transactions branches.
   * <p>
   * <b>Key:</b> transaction identifier<br>
   * <b>Value:</b> <code>XACnxPrepare</code> instance
   */
  private Map<Xid, XACnxPrepare> recoveredTransactions;

  /** Counter of message arrivals from topics. */ 
  private long arrivalsCounter = 0; 

  /** 
   * Table holding the <code>TopicSubscription</code> instances.
   * <p>
   * <b>Key:</b> topic identifier<br>
   * <b>Value:</b> topic subscription
   */
  private transient Map topicsTable;

  /**
   * Table holding the subscriptions' messages.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private transient Map messagesTable;

  /** 
   * Identifier of the active context. 
   * Value -1 means that there's no active
   * context.
   */
  private transient int activeCtxId;

  /** Reference to the active <code>ClientContext</code> instance. */
  private transient ClientContext activeCtx;

  /**
   * Table that contains the user connections:
   * - key = <code>Integer</code> (connection key)
   * - value = <code></code>
   */
  private transient Hashtable connections;

  private transient Hashtable heartBeatTasks;

  /**
   * Counter of the connection keys
   */
  private int keyCounter = 0;

  private transient WakeUpTask cleaningTask;
  
  // JORAM_PERF_BRANCH
  public UserAgent(UserAgent nullAgent) {
    super(nullAgent);
  }

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.agentInitialize(" + firstTime + ')');
    modifiedClientContexts = new ArrayList<ClientContext>();
    modifiedClientSubscriptions = new ArrayList<ClientSubscription>();
    
    super.agentInitialize(firstTime);
    initialize(firstTime);
    if (getPeriod() > 0)
      cleaningTask = new WakeUpTask(getId(), WakeUpNot.class, getPeriod());
    try {
      MXWrapper.registerMBean(this, getMBeanName().toString());
    } catch (Exception exc) {
      logger.log(BasicLevel.DEBUG, this + " jmx failed", exc);
    }
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
    if (cleaningTask != null)
      cleaningTask.cancel();
    try {
      MXWrapper.unregisterMBean(getMBeanName().toString());
    } catch (Exception exc) {
      logger.log(BasicLevel.DEBUG, this + " jmx failed", exc);
    }
    super.agentFinalize(lastTime);
  }

  private StringBuffer getMBeanName() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("Joram#").append(AgentServer.getServerId());
    strbuf.append(':');
    strbuf.append("type=User,name=").append(getName());
    return strbuf;
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the JMS client proxy with its specific behaviour.
   * <p>
   * A JMS proxy specifically reacts to the following notifications:
   * <ul>
   * <li><code>OpenConnectionNot</code></li>
   * </ul>
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.react(" + from + ',' + not + ')');

    // set agent no save: the default behavior is transient
    setNoSave();

    // Administration and monitoring requests:
//  if (not instanceof SetDMQRequest)
//    doReact(from, (SetDMQRequest) not);
//  else 
//  if (not instanceof SetThresholdRequestNot)
//    doReact(from, (SetThresholdRequestNot) not);
//  else
//    if (not instanceof SetNbMaxMsgRequest)
//    doReact(from, (SetNbMaxMsgRequest) not);
//  else if (not instanceof GetNbMaxMsgRequestNot)
//    doReact(from, (GetNbMaxMsgRequestNot) not);
//  else if (not instanceof GetDMQSettingsRequestNot)
//    doReact(from, (GetDMQSettingsRequestNot) not);
//  else

    if (not instanceof OpenConnectionNot) {
      doReact((OpenConnectionNot) not);
    } else if (not instanceof GetConnectionNot) {
      doReact((GetConnectionNot) not);
    } else if (not instanceof CloseConnectionNot) {
      doReact((CloseConnectionNot) not);
    } else if (not instanceof CloseConnectionNot2) {
      doReact((CloseConnectionNot2) not);
    } else if (not instanceof ResetCollocatedConnectionsNot) {
      doReact((ResetCollocatedConnectionsNot) not);
    } else if (not instanceof SendReplyNot) {
      doReact((SendReplyNot) not);
    } else if (not instanceof RequestNot) {
      doReact((RequestNot) not);
    } else if (not instanceof ReturnConnectionNot) {
      doReact((ReturnConnectionNot) not);
    } else if (not instanceof SendRepliesNot) {
      doReact((SendRepliesNot) not);
    } else if (not instanceof ProxyRequestGroupNot) {
      doReact((ProxyRequestGroupNot) not);
    } else if (not instanceof WakeUpNot) {
      if (cleaningTask == null || ((WakeUpNot) not).update) {
        doSetPeriod(getPeriod());
      }
      if (getPeriod() > 0) {
        cleanPendingMessages(System.currentTimeMillis());
      }
    } else if (not instanceof SyncReply) {
      doReact((SyncReply) not);
    } else if (not instanceof AbstractReplyNot) {
      doFwd(from, (AbstractReplyNot) not);
    } else if (not instanceof AdminReplyNot) {
      doReact((AdminReplyNot) not);
    } else if (not instanceof UnknownAgent) {
      doReact((UnknownAgent) not);
    } else if (not instanceof FwdAdminRequestNot) {
      doReact((FwdAdminRequestNot) not);
    } else {
      super.react(from, not);
    }
    saveModifiedClientContexts();
    saveModifiedClientSubscriptions();
  }
  
  private void saveModifiedClientContexts() {
    // JORAM_PERF_BRANCH
    if (modifiedClientContexts.size() > 0) {
      for (ClientContext modifiedCC : modifiedClientContexts) {
        if (modifiedCC.isModified) {
          modifiedCC.save();
          modifiedCC.isModified = false;
        }
      }
      modifiedClientContexts.clear();
    }
  }
  
  private void saveModifiedClientSubscriptions() {
    // JORAM_PERF_BRANCH
    if (modifiedClientSubscriptions.size() > 0) {
      for (ClientSubscription modifiedCS : modifiedClientSubscriptions) {
        if (modifiedCS.isModified) {
          modifiedCS.save();
          modifiedCS.isModified = false;
        }
      }
      modifiedClientSubscriptions.clear();
    }
  }

  private void doSetPeriod(long period) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": setPeriod(" + period + ")." + " -> task " + cleaningTask);
    if (cleaningTask == null) {
      cleaningTask = new WakeUpTask(getId(), WakeUpNot.class, period);
    } else {
      // cancel task
      cleaningTask.cancel();
      // Schedules the wake up task period.
      if (period > 0)
        cleaningTask = new WakeUpTask(getId(), WakeUpNot.class, period);
    }
  }

  /**
   * Registers and starts the <code>UserConnection</code>.
   */
  private void doReact(OpenConnectionNot not) {
    // state change, so save.
    setSave();

    if (connections == null) {
      connections = new Hashtable();
      heartBeatTasks = new Hashtable();
    }

    Integer objKey = new Integer(keyCounter);
    ConnectionContext ctx;
    if (not.getReliable()) {
      ctx = new ReliableConnectionContext(keyCounter, not.getHeartBeat());
      connections.put(objKey, ctx);
    } else {
      ctx = new StandardConnectionContext(keyCounter);
      connections.put(objKey, ctx);
    }

    if (not.getHeartBeat() > 0) {
      HeartBeatTask heartBeatTask = new HeartBeatTask(not.getHeartBeat(), objKey, getId());
      heartBeatTasks.put(objKey, heartBeatTask);
      try {
        heartBeatTask.start();
      } catch (IOException exc) {
        // Cannot schedule task, removes it from the hashtable
        heartBeatTasks.remove(objKey);
      }
    }

    // Differs the reply because the connection key counter
    // must be saved before the OpenConnectionNot returns.
    sendTo(getId(), new ReturnConnectionNot(not, ctx));
    keyCounter++;
  }
  
  // JORAM_PERF_BRANCH:
  public String[] getConnectionInfos() {
    String[] infos = new String[connections.size()];
    Enumeration enumer = connections.elements();
    int i = 0;
    while (enumer.hasMoreElements()) {
      infos[i] = enumer.nextElement().toString();
      i++;
    }
    return infos;
  }

  /**
   * Differs the reply because the connection key counter
   * must be saved before the OpenConnectionNot returns.
   */
  private void doReact(ReturnConnectionNot not) {
    not.Return();
  }

  private void doReact(GetConnectionNot not) {
    int key = not.getKey();
    if (connections == null) {
      not.Throw(new Exception("Connection " + key + " not found"));
    } else {
      Integer objKey = new Integer(key);
      ReliableConnectionContext ctx = (ReliableConnectionContext) connections.get(objKey);
      if (ctx == null) {
        not.Throw(new Exception("Connection " + key + " not found"));
      } else {
        not.Return(ctx);
      }
    }
  }

  private void doReact(RequestNot not) {
    Integer key = new Integer(not.getConnectionKey());
    if (connections != null) {
      ConnectionContext ctx = (ConnectionContext) connections.get(key);
      if (ctx != null) {
        HeartBeatTask heartBeatTask = (HeartBeatTask) heartBeatTasks.get(key);
        if (heartBeatTask != null) {
          heartBeatTask.touch();
        }

        AbstractJmsRequest request = ctx.getRequest(not.getMessage());
        reactToClientRequest(key.intValue(), request);

        if (ctx.isClosed()) {
          //CnxCloseRequest request = (CnxCloseRequest) not.getMessage();
          connections.remove(key);
          HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
          if (hbt != null) hbt.cancel();
        }
      }
    }
    // else should not happen because:
    // - RequestNot is transient
    // - RequestNot always follows an OpenConnection or
    // a GetConnection
  }

  private void doReact(ProxyRequestGroupNot not) {
    RequestNot[] requests = not.getRequests();
    RequestBuffer rm = new RequestBuffer(this);
    for (int i = 0; i < requests.length; i++) {
      RequestNot req = requests[i];
      Integer key = new Integer(req.getConnectionKey());
      HeartBeatTask heartBeatTask = (HeartBeatTask) heartBeatTasks.get(key);
      if (heartBeatTask != null) {
        heartBeatTask.touch();
      }
      ConnectionContext ctx = (ConnectionContext) connections.get(key);
      if (ctx != null) {
        AbstractJmsRequest request = ctx.getRequest(req.getMessage());
        if (request instanceof ProducerMessages) {
          ProducerMessages pm = (ProducerMessages) request;
          rm.put(req.getConnectionKey(), pm);
        } else if (request instanceof JmsRequestGroup) {
          JmsRequestGroup jrg = (JmsRequestGroup) request;
          AbstractJmsRequest[] groupedRequests = jrg.getRequests();
          for (int j = 0; j < groupedRequests.length; j++) {
            if (groupedRequests[i] instanceof ProducerMessages) {
              ProducerMessages pm = (ProducerMessages) groupedRequests[i];
              rm.put(req.getConnectionKey(), pm);
            } else {
              reactToClientRequest(key.intValue(), groupedRequests[i]);
            }
          }
        } else {
          reactToClientRequest(key.intValue(), request);
        }
      }
    }
    rm.flush();
  }

  
  private void doReact(CloseConnectionNot2 not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CloseConnectionNot2: key=" + not.getKey());
    
     if (connections != null) {
      Integer key = new Integer(not.getKey());
      ConnectionContext ctx = (ConnectionContext) connections.remove(key);
      connections.remove(key);
      HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
      // Normally the task is already cancelled by the task itself.
      if (hbt != null) hbt.cancel();

      reactToClientRequest(not.getKey(), new CnxCloseRequest());

      if (ctx != null) {
        MomException exc = new MomException(MomExceptionReply.HBCloseConnection, "Connection " + getId()
                                            + ':' + key + " closed");
        ctx.pushError(exc);
      }
    }
  }
  
  private void doReact(CloseConnectionNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CloseConnectionNot2: key=" + not.getKey());

    if (connections != null) {
      Integer key = new Integer(not.getKey());
      // The connection may have already been explicitly closed by a CnxCloseRequest.
      if (connections.remove(key) != null) {
        reactToClientRequest(not.getKey(), new CnxCloseRequest());
        connections.remove(key);
        HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
        if (hbt != null) hbt.cancel();
      }
    }
    // else should not happen:
    // 1- CloseConnectionNot is transient
    // 2- CloseConnectionNot follows an OpenConnectionNot
    // or a GetConnectionNot
  }

  private void doReact(ResetCollocatedConnectionsNot not) {
    if (connections != null) {
      Collection values = connections.values();
      Iterator iterator = values.iterator();
      while (iterator.hasNext()) {
        Object obj = iterator.next();
        // Standard connections must be dropped.
        // Only reliable connections can be recovered.
        if (obj instanceof StandardConnectionContext) {
          ConnectionContext cc = (ConnectionContext) obj;
          reactToClientRequest(cc.getKey(), new CnxCloseRequest());
          HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(cc.getKey());
          if (hbt != null) hbt.cancel();
          iterator.remove();
        }
      }
    }
  }

  private void doReact(SendRepliesNot not) {
    Enumeration en = not.getReplies();
    while (en.hasMoreElements()) {
      SendReplyNot sr = (SendReplyNot) en.nextElement();
      doReact(sr);
    }
  }

  /**
   * Notification sent by local agents (destinations)
   * indicating that the proxy can reply to a client.
   * 
   * @param not
   */
  private void doReact(SendReplyNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + not + ')');
    ClientContext cc = getClientContext(not.getKey());
    if (cc != null) {
      if (cc.setReply(not.getRequestId()) == 0) {
        sendToClient(not.getKey(), new ServerReply(not.getRequestId()), false);
        
        // JORAM_PERF_BRANCH
        modifiedClient(cc);
        
      }
    } else if (logger.isLoggable(BasicLevel.DEBUG)) {
      // Can happen if the connection is closed before the SendReplyNot
      // arrives.
      logger.log(BasicLevel.DEBUG, "UserAgent: unknown client context for " + not);
    }
  }

  /**
   * Sends a notification to the specified agent.
   * 
   * @param to the identifier of the recipient agent
   * @param not the notification to send
   */
  public void sendNot(AgentId to, Notification not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.sendNot(" + to + ',' + not + ')');
    sendTo(to, not);
  }

  /**
   * Sends a reply to the client connected through
   * the specified connection.
   * 
   * @param key the key of the connection the client
   *          is connected through.
   * @param reply the reply to send to the client.
   */
  public void sendToClient(int key, AbstractJmsReply reply, boolean asyncSend) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.sendToClient(" + key + ',' + reply + ')');
    Integer objKey = new Integer(key);
    if (connections != null) {
      ConnectionContext ctx = (ConnectionContext) connections.get(objKey);
      if (ctx != null) {
      	// interceptors process...
      	if (interceptorsOUT != null && !interceptorsOUT.isEmpty()) {
      		if (reply instanceof ConsumerMessages) {
      			org.objectweb.joram.shared.messages.Message m = null;
      			Vector msgs = ((ConsumerMessages) reply).getMessages();
      			Vector newMsgs = new Vector();
      			Vector acks = new Vector();
      			for (int i = 0; i < msgs.size(); i++) {
      				m = (org.objectweb.joram.shared.messages.Message) msgs.elementAt(i);
      				// interceptors iterator
      				Iterator it = interceptorsOUT.iterator();
      				while (it.hasNext()) {
      					MessageInterceptor interceptor = (MessageInterceptor) it.next();
      					// interceptor handle
      					if (!interceptor.handle(m)) {
      						m = null;
      						break;
      					}
      				}
      				if (m != null) {
    						newMsgs.add(m);
      				} else {
      					acks.add(((org.objectweb.joram.shared.messages.Message) msgs.elementAt(i)).id);
      				// Send the original messages to the user DMQ.
        				sendToDMQ((org.objectweb.joram.shared.messages.Message) msgs.elementAt(i), MessageErrorConstants.INTERCEPTORS);
      				}
      			}
      			if (newMsgs.size() == 0 && !msgs.isEmpty()) {  
      				// ack messages
      				org.objectweb.joram.shared.messages.Message msg = (org.objectweb.joram.shared.messages.Message) msgs.firstElement(); 
      				sendNot(AgentId.fromString(msg.toId), new AcknowledgeRequest(activeCtxId, reply.getCorrelationId(), acks));
      			}
      			//update consumer message.
      			((ConsumerMessages) reply).setMessages(newMsgs);
      		}
      	}
      	// push the reply
      	ctx.pushReply(reply, asyncSend);
      }
    }
    // else may happen. Drop the reply.
  }

  /**
   * Timer task responsible for closing the connection if it has
   * not sent any requests for the duration 'timeout'.
   */
  class HeartBeatTask extends TimerTask implements Externalizable {
    /**
     * Maximum time between two requests on the connection (This value is
     * normally the double of the hear-beat period).
     */
    private transient int timeout;

    private transient Integer key;

    private transient long lastRequestDate;

    private transient AgentId userId = null;
    
    HeartBeatTask(int timeout, Integer key, AgentId userId) {
      this.timeout = timeout;
      this.key = key;
      this.userId = userId;
    }

    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "HeartBeatTask: run - key=" + key);

      long date = System.currentTimeMillis();
      if ((date - lastRequestDate) > timeout) {
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, "HeartBeatTask: close connection");
        
        Channel.sendTo(userId, (Notification) new CloseConnectionNot(key.intValue()));
        this.cancel();

//        ConnectionContext ctx = (ConnectionContext) connections.remove(key);
//        heartBeatTasks.remove(key);
//        reactToClientRequest(key.intValue(), new CnxCloseRequest());
//
//        if (ctx != null) {
//          MomException exc = new MomException(MomExceptionReply.HBCloseConnection, "Connection " + getId()
//              + ':' + key + " closed");
//          ctx.pushError(exc);
//        }
      }
    }

    public void start() throws IOException {
      lastRequestDate = System.currentTimeMillis();
      try {
        AgentServer.getTimer().schedule(this, timeout/2, timeout/2);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "HeartBeatTask: cannot schedule task " + key, exc);
        throw new IOException(exc.getMessage());
      }
    }

    public void touch() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "HeartBeatTask: touch", new Exception());
      lastRequestDate = System.currentTimeMillis();
    }

    // This code below is only needed by HA mechanism, do not use.

    public HeartBeatTask() {
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      timeout = in.readInt();
      key = new Integer(in.readInt());
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(timeout);
      out.writeInt(key.intValue());
    }
  }

  public void setNoSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setNoSave()");
    super.setNoSave();
  }

  public void setSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.setSave()");
    super.setSave();
  }

  /**
   * Constructs a <code>UserAgent</code> instance.
   */
  public UserAgent() {
    super(true);
    contexts = new Hashtable();
    subsTable = new Hashtable();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Creates a new user proxy.
   * 
   * @see ConnectionManager
   */
  public UserAgent(String name, int stamp) {
    super(name, true, stamp);
    contexts = new Hashtable();
    subsTable = new Hashtable();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Returns a string representation of this user's proxy.
   */
  public String toString() {
    return "UserAgent:" + getId();
  }

  /**
   * Only call in UserAgent creation.
   * 
   * @param prop properties
   */
  public void setInterceptors(Properties prop) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " initInterceptors(" + prop + ')');
  	
  	if (prop == null) return;

  	interceptors_out = (String) prop.get(AdminCommandConstant.INTERCEPTORS_OUT);
  	interceptors_in = (String) prop.get(AdminCommandConstant.INTERCEPTORS_IN);
  }
  
  /**
   * (Re)initializes the proxy.
   * 
   * @param firstTime 
   *
   * @exception Exception  If the proxy state could not be fully retrieved,
   *              leading to an inconsistent state.
   */
  private void initialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " (re)initializing...");
 
    topicsTable = new Hashtable();
    messagesTable = new Hashtable();
    
    // JORAM_PERF_BRANCH
    contexts = new Hashtable<Integer, ClientContext>();
    subsTable = new Hashtable<EncodedString, ClientSubscription>();
    
    // JORAM_PERF_BRANCH
    Transaction tx = AgentServer.getTransaction();
    String[] persistedClientNames = tx.getList(ClientContext.getTransactionPrefix(getId()));
    for (int i = 0; i < persistedClientNames.length; i++) {
      try {
        ClientContext cc = (ClientContext) tx.load(persistedClientNames[i]);
        cc.txname = persistedClientNames[i];
        contexts.put(cc.getId(), cc);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "ClientContext named [" + persistedClientNames[i]
            + "] could not be loaded", exc);
      }
    }
    String[] persistedSubscriptionNames = tx.getList(ClientSubscription.getTransactionPrefix(getId()));
    for (int i = 0; i < persistedSubscriptionNames.length; i++) {
      try {
        ClientSubscription cs = (ClientSubscription) tx.load(persistedSubscriptionNames[i]);
        cs.txname = persistedSubscriptionNames[i];
        subsTable.put(cs.getEncodedName(), cs);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "ClientSubscription named [" + persistedSubscriptionNames[i]
            + "] could not be loaded", exc);
      }
    }

    setActiveCtxId(-1);
    
    // Re-initializing after a crash or a server stop.

    // interceptors
    if (interceptors_out != null) {
    	interceptorsOUT = new ArrayList();
    	InterceptorsHelper.addInterceptors(interceptors_out, interceptorsOUT);
    }
    if (interceptors_in != null) {
    	interceptorsIN = new ArrayList();
    	InterceptorsHelper.addInterceptors(interceptors_in, interceptorsIN);
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "contexts=" + contexts);
    
    // Browsing the pre-crash contexts:
    ClientContext activeCtx;
    AgentId destId;
    for (Iterator ctxs = contexts.values().iterator(); ctxs.hasNext();) {
      activeCtx = (ClientContext) ctxs.next();
      activeCtx.setProxyAgent(this);
      ctxs.remove();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "activeCtx.getDeliveringQueues()=" + activeCtx);

      // Denying the non acknowledged messages:
      for (Iterator queueIds = activeCtx.getDeliveringQueues(); queueIds.hasNext();) {
        destId = (AgentId) queueIds.next();
        sendNot(destId, new DenyRequest(activeCtx.getId()));

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Denies messages on queue " + destId.toString());
        
        
        // JORAM_PERF_BRANCH
        sendNot(destId, new AbortReceiveRequest(activeCtx.getId(), -1, -1));
      }

      // Saving the prepared transactions.
      Iterator xids = activeCtx.getTxIds();
      Xid xid;
      XACnxPrepare recoveredPrepare;
      XACnxPrepare prepare;
      while (xids.hasNext()) {
        if (recoveredTransactions == null)
          recoveredTransactions = new Hashtable();

        xid = (Xid) xids.next();

        recoveredPrepare = (XACnxPrepare) recoveredTransactions.get(xid);
        prepare = activeCtx.getTxPrepare(xid);
        
        // JORAM_PERF_BRANCH
        modifiedClient(activeCtx);

        if (recoveredPrepare == null)
          recoveredTransactions.put(xid, prepare);
        else {
          recoveredPrepare.getSendings().addAll(prepare.getSendings());
          recoveredPrepare.getAcks().addAll(prepare.getAcks());
        }
      }

      // Deleting the temporary destinations:
      for (Iterator tempDests = activeCtx.getTempDestinations(); tempDests.hasNext();) {
        destId = (AgentId) tempDests.next();
        deleteTemporaryDestination(destId);
  
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Deletes temporary destination " + destId.toString());
      }
    }

    // Retrieving the subscriptions' messages.
    List messages = Message.loadAll(getMsgTxname());

    if (subsTable.isEmpty()) {
      // it is possible because we always save MessageSoftRef
      // so we must delete all message.
      Message.deleteAll(getMsgTxname());
    }
    
    // Browsing the pre-crash subscriptions:
    Map.Entry subEntry;
    ClientSubscription cSub;
    List topics = new ArrayList();
    TopicSubscription tSub;
    for (Iterator subs = subsTable.entrySet().iterator(); subs.hasNext();) {
      subEntry = (Map.Entry) subs.next();
      cSub = (ClientSubscription) subEntry.getValue();
      destId = cSub.getTopicId();
      if (! topics.contains(destId))
        topics.add(destId);
      // Deleting the non durable subscriptions.
      if (!cSub.getDurable()) {
        subs.remove();
        try {
          MXWrapper.unregisterMBean(getSubMBeanName((String) subEntry.getKey()));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e1);
        }
      }
      // Reinitializing the durable ones.
      else {
        cSub.setProxyAgent(this);
        cSub.reinitialize(messagesTable, messages, true);
        try {
          MXWrapper.registerMBean(cSub, getSubMBeanName((String) subEntry.getKey()));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "  - Could not register ClientSubscriptionMbean", e1);
        }
        tSub = (TopicSubscription) topicsTable.get(destId);
        if (tSub == null) {
          tSub = new TopicSubscription();
          topicsTable.put(destId, tSub);
        }
        
        // JORAM_PERF_BRANCH
        EncodedString encodedString = (EncodedString) subEntry.getKey();
        
        tSub.putSubscription(encodedString.getString(), cSub.getSelector());
        
        // JORAM_PERF_BRANCH
        tSub.putDurable(encodedString.getString(), Boolean.TRUE);
      }
    }
    // Browsing the topics and updating their subscriptions.
    for (Iterator topicIds = topics.iterator(); topicIds.hasNext();) {
      
      // JORAM_PERF_BRANCH
      AgentId topicId = (AgentId) topicIds.next();
      TopicSubscription topicSubscription = (TopicSubscription) topicsTable.get(topicId);
      
      updateSubscriptionToTopic(topicId, -1, -1, topicSubscription.isDurable());
    }
    
    saveModifiedClientContexts();
    saveModifiedClientSubscriptions();
  }

  private void setActiveCtxId(int activeCtxId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.setActiveCtxId(" + activeCtxId + ')');
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
  protected void reactToClientRequest(int key, AbstractJmsRequest request) {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this + " got " + request.getClass().getName() + " with id: "
            + request.getRequestId() + " through activeCtx: " + key);

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
    } catch (IllegalArgumentException iE) {
      // Catching an exception due to an invalid agent identifier to
      // forward the request to:
      DestinationException dE = new DestinationException("Incorrect destination identifier: " + iE);
      sendToClient(key, new MomExceptionReply(request.getRequestId(), dE), true);
    } catch (RequestException exc) {
      sendToClient(key, new MomExceptionReply(request.getRequestId(), exc), true);
    }
  }

  /**
   * Forwards the messages sent by the client in a
   * <code>ProducerMessages</code> request as a <code>ClientMessages</code>
   * MOM request directly to a destination, and acknowledges them by sending
   * a <code>ServerReply</code> back.
   * 
   * @throws RequestException The destination id is undefined
   */
  private void reactToClientRequest(int key, ProducerMessages req) throws RequestException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.reactToClientRequest(" + key + ',' + req + ')');

    AgentId destId = AgentId.fromString(req.getTarget());
    if (destId == null)
      throw new RequestException("Request to an undefined destination (null).");

    ProducerMessages pm = req; 
    if (interceptorsIN != null && !interceptorsIN.isEmpty()) {
    	org.objectweb.joram.shared.messages.Message m = null;
    	Vector msgs = ((ProducerMessages) req).getMessages();
    	Vector newMsgs = new Vector();
    	for (int i = 0; i < msgs.size(); i++) {
    		m = (org.objectweb.joram.shared.messages.Message) msgs.elementAt(i);
    		Iterator it = interceptorsIN.iterator();
    		while (it.hasNext()) {
    			MessageInterceptor interceptor = (MessageInterceptor) it.next();
    			if (!interceptor.handle(m)) {
    				m = null;
    				break;
    			}
    		}
    		if (m != null) {
    			newMsgs.add(m);
    		} else {
    			// send the originals messages to the user DMQ.
    			sendToDMQ((org.objectweb.joram.shared.messages.Message) msgs.elementAt(i), MessageErrorConstants.INTERCEPTORS);
    		}
    	}
    	// no message to send. Send reply to the producer.
    	if (newMsgs.size() == 0 && !msgs.isEmpty()) { 
    		if (logger.isLoggable(BasicLevel.DEBUG))
    			logger.log(BasicLevel.DEBUG, "UserAgent.reactToClientRequest : no message to send.");
    		if (destId.getTo() == getId().getTo() && !pm.getAsyncSend()) {
    			// send producer reply
    			sendNot(getId(), new SendReplyNot(key, pm.getRequestId()));
    		}
    		return;
    	}
    	//update producer message.
    	((ProducerMessages) pm).setMessages(newMsgs);
    	pm = req;
    }

    ClientMessages not = new ClientMessages(key, pm.getRequestId(), pm.getMessages());
    setDmq(not);
    
    // JORAM_PERF_BRANCH
    not.setProxyId(getId());
    
    if (destId.getTo() == getId().getTo()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> local sending");
      not.setPersistent(false);
      not.setExpiration(0L);
      if (pm.getAsyncSend()) {
        not.setAsyncSend(true);
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> remote sending");
      if (!pm.getAsyncSend()) {
        sendNot(getId(), new SendReplyNot(key, pm.getRequestId()));
      }
    }

    sendNot(destId, not);
  }

  private void sendToDMQ(org.objectweb.joram.shared.messages.Message msg, short messageError) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "sendToDMQ(" + msg + ',' + messageError + ')');
  	DMQManager dmqManager = new DMQManager(dmqId, null);
  	nbMsgsSentToDMQSinceCreation++;
  	dmqManager.addDeadMessage(msg, messageError);
  	dmqManager.sendToDMQ();
  }
  
  private void setDmq(ClientMessages not) {
    //  Setting the producer's DMQ identifier field: 
    if (dmqId != null) {
      not.setDMQId(dmqId);
    } else {
      not.setDMQId(Queue.getDefaultDMQId());
    }
  }

  /**
   * Either forwards the <code>ConsumerReceiveRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   * 
   * @throws RequestException Undefined (null) destination
   */
  private void reactToClientRequest(int key, ConsumerReceiveRequest req) throws RequestException {    
    if (req.getQueueMode()) {
      ReceiveRequest not = new ReceiveRequest(key, req.getRequestId(), req.getSelector(),
          req.getTimeToLive(), req.getReceiveAck(), null, 1);
      AgentId destId = AgentId.fromString(req.getTarget());
      if (destId == null)
        throw new RequestException("Request to an undefined destination (null).");

      if (destId.getTo() == getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local receiving");
        not.setPersistent(false);
        sendNot(destId, not);
      } else {
        sendNot(destId, not);
      }
    } else {
      doReact(key, req);   
    }
  }

  /**
   * Either forwards the <code>ConsumerSetListRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   * 
   * @throws RequestException Undefined (null) destination
   */
  private void reactToClientRequest(int key, ConsumerSetListRequest req) throws RequestException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyImp.reactToClientRequest(" + key + ',' + req + ')');
    if (req.getQueueMode()) {
      ReceiveRequest not = new ReceiveRequest(key,
                                              req.getRequestId(),
                                              req.getSelector(),
                                              0,
                                              false,
                                              req.getMessageIdsToAck(),
                                              req.getMessageCount());

      AgentId destId = AgentId.fromString(req.getTarget());
      if (destId == null)
        throw new RequestException("Request to an undefined destination (null).");

      // JORAM_PERF_BRANCH:
      if (DIRECT_QUEUE_DELIVER) {
        not.setImplicitReceive(true);
        try {
          setCtx(key);
        } catch (StateException pE) {
          throw new RequestException(pE.toString());
        }
        activeCtx.addDeliveringQueue(destId);
        
        // JORAM_PERF_BRANCH
        modifiedClient(activeCtx);
        
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "activeCtx.getDeliveringQueues() = " + activeCtx);
      }
      // JORAM_PERF_BRANCH.
      
      if (destId.getTo() == getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local sending");
        not.setPersistent(false);
        sendNot(destId, not);
      } else {
        sendNot(destId, not);
      }
    }
    else {
      doReact(key, req);   
    }
  }

  /**
   * Forwards the client's <code>QBrowseRequest</code> request as
   * a <code>BrowseRequest</code> MOM request directly to a destination.
   * 
   * @throws RequestException Undefined (null) destination
   */
  private void reactToClientRequest(int key, QBrowseRequest req) throws RequestException {
    AgentId destId = AgentId.fromString(req.getTarget());
    if (destId == null)
      throw new RequestException("Request to an undefined destination (null).");
    
    sendNot(destId, new BrowseRequest(key, req.getRequestId(), req.getSelector()));
  }
  
  private void reactToClientRequest(int key, JmsRequestGroup request) {
    AbstractJmsRequest[] requests = request.getRequests();
    RequestBuffer rm = new RequestBuffer(this);
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
  private void doReact(int key, AbstractJmsRequest request) {
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
      else if (request instanceof SessCreateDestRequest)
        doReact((SessCreateDestRequest) request);
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
      else
        logger.log(BasicLevel.WARN, this + " - unhandling request: " + request);
    } catch (MomException mE) {
      logger.log(BasicLevel.ERROR, this + " - error during request: " + request, mE);

      // Sending the exception to the client:
      doReply(new MomExceptionReply(request.getRequestId(), mE));
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, this + " - unexpected error during request: " + request, exc);

      // Sending the exception to the client:
      doReply(new MomExceptionReply(request.getRequestId(), new MomException(exc.getMessage())));
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
  private void doReact(int key, GetAdminTopicRequest req) throws AccessException {
//     if (! admin)
//       throw new AccessException("Request forbidden to a non administrator.");
    sendToClient(key, new GetAdminTopicReply(req, AdminTopic.getDefault().toString()), false);
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
  private void doReact(int key, CnxConnectRequest req) throws DestinationException {
    // state change, so save.
    // JORAM_PERF_BRANCH: now the CLientContext is separately stored
    // setSave();

    setActiveCtxId(key);
    activeCtx = new ClientContext(getId(), key);
    activeCtx.setProxyAgent(this);
    contexts.put(new Integer(key), activeCtx);
    
    // JORAM_PERF_BRANCH
    modifiedClient(activeCtx);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Connection " + key + " opened.");

    doReply(new CnxConnectReply(req, key, getId().toString()));
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
    for (Iterator deliveries = activeCtx.getPendingDeliveries(); deliveries.hasNext();)
      doReply((AbstractJmsReply) deliveries.next());

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
   * Method implementing the JMS proxy reaction to a <code>SessCreateDestRequest</code>
   * requesting the creation of a destination.
   * <p>
   * Creates the queue, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   * <p>
   * Creates the topic, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   *
   * @exception RequestException  If the destination could not be deployed.
   */
  private void doReact(SessCreateDestRequest req) throws RequestException {
    AgentId destId = null;

    // Verify if the destination exists
    DestinationDesc desc = AdminTopic.lookupDest(req.getName(), req.getType());
    if (desc == null) {
      Destination dest = null;
      if (DestinationConstants.isQueue(req.getType())) {
        // Create a local queue.
        dest = new Queue();
      } else if (DestinationConstants.isTopic(req.getType())) {
        // Create a local topic.
        dest = new Topic();
      } else {
        throw new RequestException("Could not create destination, unknown type:" + req.getType());
      }
      dest.setName(req.getName());
      dest.setAdminId(getId());
      dest.setFreeWriting(true); // Setting free WRITE right on the destination
      if (! DestinationConstants.isTemporary(req.getType()))
        dest.setFreeReading(true); // Setting free READ right on the destination
      destId = dest.getId();
      try {
        dest.deploy();
      } catch (IOException exc) {
        throw new RequestException("Could not create destination:" + exc.getMessage());
      }
      // Registers the newly created destination
      AdminTopic.registerDest(destId, (req.getName() == null) ? destId.toString() : req.getName(),
          req.getType());

      if (DestinationConstants.isTemporary(req.getType())) {
        // Registers the temporary destination in order to clean it at the end of the connection
        activeCtx.addTemporaryDestination(destId);
        
        // JORAM_PERF_BRANCH
        modifiedClient(activeCtx);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "UserAgent, new destination created: " + destId);
    } else {
      destId = desc.getId();
    }

    SessCreateDestReply reply = new SessCreateDestReply(req, destId.toString());
    sendNot(getId(), new SyncReply(activeCtxId, reply));
  }
  
  // JORAM_PERF_BRANCH
  private void modifiedClient(ClientContext cc) {
    if (! modifiedClientContexts.contains(cc)) {
      modifiedClientContexts.add(cc);
    }
  }
  
  //JORAM_PERF_BRANCH
  private void modifiedSubscription(ClientSubscription cs) {
    if (! modifiedClientSubscriptions.contains(cs)) {
      modifiedClientSubscriptions.add(cs);
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a <code>ConsumerSubRequest</code>
   * requesting to subscribe to a topic.
   *
   * @exception StateException    If activating an already active durable subscription.
   * @exception RequestException  If the subscription parameters are not correct.
   */
  private void doReact(ConsumerSubRequest req) throws StateException, RequestException {
    AgentId topicId = AgentId.fromString(req.getTarget());
    String subName = req.getSubName();
    
    if (topicId == null)
      throw new RequestException("Cannot subscribe to an undefined topic (null).");
    
    if (subName == null)
      throw new RequestException("Unauthorized null subscription name.");
    
    // JORAM_PERF_BRANCH
    EncodedString encodedSubName = new EncodedString(subName);
    
    boolean newTopic = ! topicsTable.containsKey(topicId);
    
    // JORAM_PERF_BRANCH
    boolean newSub = ! subsTable.containsKey(encodedSubName);

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
    
    // JORAM_PERF_BRANCH
    if (req.getDurable()) {
      tSub.putDurable(subName, Boolean.TRUE);
    }
    
    
    // JORAM_PERF_BRANCH
    EncodedString encodedSelector;
    if (req.getSelector() == null) {
      encodedSelector = null;
    } else {
      encodedSelector = new EncodedString(req.getSelector());
    }

    if (newSub) { // New subscription...
      // state change, so save.
      setSave();
      cSub = new ClientSubscription(getId(),
                                    activeCtxId,
                                    req.getRequestId(),
                                    req.getDurable(),
                                    topicId,
                                    encodedSubName,
                                    encodedSelector,
                                    req.getNoLocal(),
                                    dmqId,
                                    threshold,
                                    nbMaxMsg,
                                    messagesTable);
      cSub.setProxyAgent(this);
     
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + subName + " created.");
      
      // JORAM_PERF_BRANCH
      subsTable.put(encodedSubName, cSub);
      cSub.save();
      modifiedSubscription(cSub);
      
      try {
        MXWrapper.registerMBean(cSub, getSubMBeanName(subName));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "  - Could not register ClientSubscriptionMbean", e);
      }
      tSub.putSubscription(subName, req.getSelector());
      sent = updateSubscriptionToTopic(topicId, activeCtxId, req.getRequestId(), req.isAsyncSubscription(), req.getDurable());
    } else { // Existing durable subscription...
      
      // JORAM_PERF_BRANCH
      cSub = (ClientSubscription) subsTable.get(encodedSubName);

      if (cSub.getActive() > 0)
        throw new StateException("The durable subscription " + subName + " has already been activated.");

      // Updated topic: updating the subscription to the previous topic.
      boolean updatedTopic = ! topicId.equals(cSub.getTopicId());
      if (updatedTopic) {
        TopicSubscription oldTSub =
          (TopicSubscription) topicsTable.get(cSub.getTopicId());
        oldTSub.removeSubscription(subName);
        updateSubscriptionToTopic(cSub.getTopicId(), -1, -1, req.isAsyncSubscription(), req.getDurable());
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
      // JORAM_PERF_BRANCH
      cSub.reactivate(activeCtxId, req.getRequestId(), topicId, encodedSelector, req.getNoLocal());
      
      // JORAM_PERF_BRANCH
      modifiedSubscription(cSub);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + subName + " reactivated.");

      // Updated subscription: updating subscription to topic.  
      if (updatedTopic || updatedSelector) {
        tSub.putSubscription(subName, req.getSelector());
        sent = updateSubscriptionToTopic(topicId, activeCtxId, req.getRequestId(), req.isAsyncSubscription(), req.getDurable());
      }
      
      // JORAM_PERF_BRANCH
      // cSub.save();
    }
    // Activating the subscription.
    // JORAM_PERF_BRANCH
    activeCtx.addSubName(encodedSubName);

    // Acknowledging the request, if needed.
    if (!sent)
      sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
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
  private void doReact(ConsumerSetListRequest req) throws DestinationException {
    // Getting the subscription:
    String subName = req.getTarget();
    ClientSubscription sub = null;
    
    // JORAM_PERF_BRANCH
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(new EncodedString(subName));

    if (sub == null)
      throw new DestinationException("Can't set a listener on the non existing subscription: " + subName);

    sub.setListener(req.getRequestId());

    ConsumerMessages consM = sub.deliver();
    
    //JORAM_PERF_BRANCH
    modifiedSubscription(sub);
    
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
  private void doReact(ConsumerUnsetListRequest req) throws DestinationException {
    // If the listener was listening to a queue, cancelling any pending reply:
    if (req.getQueueMode()) {
      activeCtx.cancelReceive(req.getCancelledRequestId());
      AgentId to = AgentId.fromString(req.getTarget());
      sendNot(to,
          new AbortReceiveRequest(activeCtx.getId(), req.getRequestId(), req.getCancelledRequestId()));
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerCloseSubRequest</code> requesting to deactivate a durable
   * subscription.
   *
   * @exception DestinationException  If the subscription does not exist. 
   */
  private void doReact(ConsumerCloseSubRequest req) throws DestinationException {
    // Getting the subscription:
    String subName = req.getTarget();
    
     // JORAM_PERF_BRANCH
    EncodedString encodedSubName = new EncodedString(subName);
    
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(encodedSubName);

    if (sub == null)
      throw new DestinationException("Can't desactivate non existing subscription: " + subName);

    // De-activating the subscription:
    activeCtx.removeSubName(encodedSubName);
    //sub.deactivate(false);
    
    // JORAM_PERF_BRANCH
    modifiedSubscription(sub);

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
    setSave();

    // Getting the subscription.
    String subName = req.getTarget();
    
    // JORAM_PERF_BRANCH
    EncodedString encodedSubName = new EncodedString(subName);
    
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(encodedSubName);
    if (sub == null)
      throw new DestinationException("Can't unsubscribe non existing subscription: " + subName);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Deleting subscription " + subName);

    // Updating the proxy's subscription to the topic.
    AgentId topicId = sub.getTopicId();
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);
    tSub.removeSubscription(subName);
    
    // JORAM_PERF_BRANCH
    tSub.removeDurable(subName);
    
    updateSubscriptionToTopic(topicId, -1, -1, tSub.isDurable());

    // Deleting the subscription.
    sub.deleteMessages();
    
    // JORAM_PERF_BRANCH
    sub.delete();
    
    activeCtx.removeSubName(encodedSubName);
    subsTable.remove(encodedSubName);

    try {
      MXWrapper.unregisterMBean(getSubMBeanName(subName));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e);
    }

    // Acknowledging the request:
    sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
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
  private void doReact(ConsumerReceiveRequest req) throws DestinationException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + req + ')');

    String subName = req.getTarget();
    ClientSubscription sub = null;
    
    // JORAM_PERF_BRANCH
    if (subName != null) {
      EncodedString encodedSubName = new EncodedString(subName);
      sub = (ClientSubscription) subsTable.get(encodedSubName);
    }

    if (sub == null)
      throw new DestinationException("Can't request a message from the unknown subscription: " + subName);

    // Getting a message from the subscription.
    sub.setReceiver(req.getRequestId(), req.getTimeToLive());
    ConsumerMessages consM = sub.deliver();
    
    //JORAM_PERF_BRANCH
    modifiedSubscription(sub);

    if (consM != null && req.getReceiveAck()) {
      // Immediate acknowledge
      Vector messageList = consM.getMessages();
      for (int i = 0; i < messageList.size(); i++) {
        Message msg = (Message) messageList.elementAt(i);
        sub.acknowledge(msg.getId());
        
        //JORAM_PERF_BRANCH
        modifiedSubscription(sub);
      }
    }

    if (consM == null && req.getTimeToLive() == -1) {
      // Nothing to deliver but immediate delivery request: building an empty reply.
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
  private void doReact(SessAckRequest req) {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      Vector ids = req.getIds();

      AcknowledgeRequest not = new AcknowledgeRequest(activeCtxId, req.getRequestId(), ids);
      if (qId.getTo() == getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local acking");
        not.setPersistent(false);
      }

      sendNot(qId, not);
    } else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
      if (sub != null) {
        sub.acknowledge(req.getIds().iterator());

        // JORAM_PERF_BRANCH
        modifiedSubscription(sub);
      }
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
      DenyRequest dr = new DenyRequest(activeCtxId, req.getRequestId(), ids);
      if (req.isRedelivered())
        dr.setRedelivered(true);
      sendNot(qId, dr);

      // Acknowledging the request unless forbidden:
      if (!req.getDoNotAck())
        sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
    } else {
      String subName = req.getTarget();
      
      // JORAM_PERF_BRANCH
      ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));

      if (sub == null)
        return;

      sub.deny(req.getIds().iterator(), req.isRedelivered());

      // Launching a delivery sequence:
      ConsumerMessages consM = sub.deliver();
      
      //JORAM_PERF_BRANCH
      modifiedSubscription(sub);
      
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
  private void doReact(ConsumerAckRequest req) {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      AcknowledgeRequest not = new AcknowledgeRequest(activeCtxId, req.getRequestId(), req.getIds());
      if (qId.getTo() == getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local acking");
        not.setPersistent(false);
        sendNot(qId, not);
      } else {
        sendNot(qId, not);
      }
    } else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
      if (sub != null) {
        sub.acknowledge(req.getIds().iterator());
        
        //JORAM_PERF_BRANCH
        modifiedSubscription(sub);
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
      DenyRequest denyRequest = new DenyRequest(activeCtxId, req.getRequestId(), id);
      denyRequest.setRedelivered(req.isRedelivered());
      sendNot(qId, denyRequest);

      // Acknowledging the request, unless forbidden:
      if (!req.getDoNotAck())
        sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
    } else {
      String subName = req.getTarget();
      
      // JORAM_PERF_BRANCH
      ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));

      if (sub == null)
        return;

      Vector<String> ids = new Vector<String>();
      ids.add(req.getId());
      sub.deny(ids.iterator(), req.isRedelivered());

      // Launching a delivery sequence:
      ConsumerMessages consM = sub.deliver();
      
      //JORAM_PERF_BRANCH
      modifiedSubscription(sub);
      
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
    
    // JORAM_PERF_BRANCH
    modifiedClient(activeCtx);

    // Sending the request to the destination:
    deleteTemporaryDestination(tempId);

    // Acknowledging the request:
    sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
  }

  private void deleteTemporaryDestination(AgentId destId) {
    sendNot(destId, new DeleteNot());
    AdminTopic.unregisterDest(destId.toString());
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
      
      // JORAM_PERF_BRANCH
      modifiedClient(activeCtx);
      
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
    
    // JORAM_PERF_BRANCH
    modifiedClient(activeCtx);

    if (prepare == null)
      throw new StateException("Unknown transaction identifier.");

    Vector sendings = prepare.getSendings();
    Vector acks = prepare.getAcks();

    ProducerMessages pM;
    ClientMessages not;
    while (!sendings.isEmpty()) {
      pM = (ProducerMessages) sendings.remove(0);
      not = new ClientMessages(activeCtxId, pM.getRequestId(), pM.getMessages());
      sendNot(AgentId.fromString(pM.getTarget()), not);
    }

    while (!acks.isEmpty())
      doReact((SessAckRequest) acks.remove(0));

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XACnxRollback</code> request rolling back the operations performed
   * in a given transaction.
   */
  private void doReact(XACnxRollback req) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doReact(" + req + ')');
    
    Xid xid = new Xid(req.getBQ(), req.getFI(), req.getGTI());

    String queueName;
    AgentId qId;
    Vector ids;
    for (Enumeration queues = req.getQueues(); queues.hasMoreElements();) {
      queueName = (String) queues.nextElement();
      qId = AgentId.fromString(queueName);
      ids = req.getQueueIds(queueName);
      DenyRequest deny =  new DenyRequest(activeCtxId, req.getRequestId(), ids);
      deny.setRedelivered(true);
      sendNot(qId, deny);
    }

    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    for (Enumeration<String> subs = req.getSubs(); subs.hasMoreElements();) {
      subName = subs.nextElement();
      
      // JORAM_PERF_BRANCH
      sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
      
      if (sub != null) {
        sub.deny(req.getSubIds(subName).iterator(), true);

        consM = sub.deliver();
        
        //JORAM_PERF_BRANCH
        modifiedSubscription(sub);
        
        if (consM != null && activeCtx.getActivated())
          doReply(consM);
        else if (consM != null)
          activeCtx.addPendingDelivery(consM);
      }
    }

   XACnxPrepare prepare = activeCtx.getTxPrepare(xid);
   
   // JORAM_PERF_BRANCH
   modifiedClient(activeCtx);

    if (prepare != null) {
      Vector acks = prepare.getAcks();

      SessAckRequest ack;
      while (! acks.isEmpty()) {
        ack = (SessAckRequest) acks.remove(0);
        SessDenyRequest deny = new SessDenyRequest(ack.getTarget(),
            ack.getIds(),
            ack.getQueueMode(),
            true);
        deny.setRedelivered(true);
        doReact(deny);
      }
    }

    sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
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
    setSave();

    Vector bqs = new Vector();
    Vector fis = new Vector();
    Vector gtis = new Vector();
    if (recoveredTransactions != null) {
      Iterator txs = recoveredTransactions.entrySet().iterator();
      Xid xid;
      while (txs.hasNext()) {
        Map.Entry txEntry = (Map.Entry) txs.next();
        xid = (Xid) txEntry.getKey();
        bqs.add(xid.bq);
        fis.add(new Integer(xid.fi));
        gtis.add(xid.gti);
        try {
          txs.remove();
          activeCtx.registerTxPrepare(xid, (XACnxPrepare) txEntry.getValue());
          
          // JORAM_PERF_BRANCH
          modifiedClient(activeCtx);
          
        }
        catch (Exception exc) {
          throw new StateException("Recovered transaction branch has already been prepared by the RM.");
        }
      }
    }
    recoveredTransactions = null;
    doReply(new XACnxRecoverReply(req, bqs, fis, gtis));
  }

//  /**
//   * Method implementing the reaction to a <code>SetDMQRequest</code>
//   * instance setting the dead message queue identifier for this proxy
//   * and its subscriptions.
//   */
//  private void doReact(AgentId from, SetDMQRequest not) {
//    // state change, so save.
//    setSave();
//    
//    dmqId = not.getDmqId();
//
//    for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();) 
//      ((ClientSubscription) subsTable.get(keys.nextElement())).setDMQId(dmqId);
//
//    sendNot(from, new AdminReplyNot(not, true, "DMQ set: " + dmqId));
//  }

//  /**
//   * Method implementing the reaction to a <code>SetThreshRequest</code>
//   * instance setting the threshold value for this proxy and its
//   * subscriptions.
//   */
//  private void doReact(AgentId from, SetThresholdRequestNot not) {
//    // state change, so save.
//    setSave();
//    
//    threshold = not.getThreshold();
//
//    for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();) 
//      ((ClientSubscription)
//         subsTable.get(keys.nextElement())).setThreshold(not.getThreshold());
//
//    sendNot(from,
//                       new AdminReplyNot(not,
//                                      true,
//                                      "Threshold set: " + threshold));
//  }

//  /**
//   * Method implementing the reaction to a <code>SetNbMaxMsgRequest</code>
//   * instance setting the NbMaxMsg value for the subscription.
//   */
//  protected void doReact(AgentId from, SetNbMaxMsgRequest not) { XXX
//    int nbMaxMsg = not.getNbMaxMsg();
//    String subName = not.getSubName();
//
//    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
//    if (sub != null) {
//      sub.setNbMaxMsg(nbMaxMsg);
//      sendNot(from,
//                         new AdminReplyNot(not,
//                                        true,
//                                        "NbMaxMsg set: " + nbMaxMsg + " on " + subName));
//    } else {
//      sendNot(from,
//                         new AdminReplyNot(not,
//                                        false,
//                                        "NbMaxMsg not set: " + nbMaxMsg + " on " + subName));
//    }
//  }

//  /**
//   * Method implementing the reaction to a <code>Monit_GetDMQSettings</code>
//   * instance requesting the DMQ settings of this proxy.
//   */
//  private void doReact(AgentId from, GetDMQSettingsRequestNot not)
//  {
//    String id = null;
//    if (dmqId != null)
//      id = dmqId.toString();
//    sendNot(from, new GetDMQSettingsReplyNot(not, id, threshold));
//  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SyncReply</code> notification sent by itself, wrapping a reply
   * to be sent to a client.
   */
  private void doReact(SyncReply not) {
    sendToClient(not.key, not.reply, false);
  }

  /**
   * The method closes a given context by denying the non acknowledged messages
   * delivered to this context, and deleting its temporary subscriptions and
   * destinations.
   */
  private void doReact(int key, CnxCloseRequest req) {
    // state change, so save.
    setSave();

    //setCtx(cKey);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "contexts=" + contexts);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "activeCtx.getDeliveringQueues() = " + activeCtx);
    
    // Denying the non acknowledged messages:
    AgentId id;
    boolean prepared = false;
    for (Iterator ids = activeCtx.getDeliveringQueues(); ids.hasNext();) {
      id = (AgentId) ids.next();
      
      // JORAM_PERF_BRANCH
      sendNot(id, new AbortReceiveRequest(activeCtx.getId(), -1, -1));

      for (Iterator xids = activeCtx.getTxIds(); xids.hasNext();) {
        Xid xid = (Xid) xids.next();
        if (activeCtx.isPrepared(xid)) {
          prepared = true;
          break;
        }
      }
      if (!prepared)
        sendNot(id, new DenyRequest(key));
      prepared = false;
    }

    // Removing or deactivating the subscriptions:
    EncodedString subName = null;
    ClientSubscription sub;
    List topics = new Vector();
    for (Iterator<EncodedString> subs = activeCtx.getActiveSubs(); subs.hasNext();) {
      subName = subs.next();
      sub = subsTable.get(subName);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
            "Deactivate subscription " + subName + ",  subsTable = " + subsTable);

      if (sub.getDurable()) {
        sub.deactivate(true);
        
        //JORAM_PERF_BRANCH
        modifiedSubscription(sub);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Durable subscription" + subName + " de-activated.");
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> topicsTable = " + topicsTable);

        sub.deleteMessages();
        subsTable.remove(subName);
        
        // JORAM_PERF_BRANCH
        sub.delete();
        
        try {
          MXWrapper.unregisterMBean(getSubMBeanName(subName.getString()));
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e);
        }
        TopicSubscription tSub = (TopicSubscription) topicsTable.get(sub
            .getTopicId());
        tSub.removeSubscription(subName.getString());

        if (!topics.contains(sub.getTopicId()))
          topics.add(sub.getTopicId());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Temporary subscription" + subName + " deleted.");
      }
    }
    // Browsing the topics which at least have one subscription removed.
    for (Iterator topicIds = topics.iterator(); topicIds.hasNext();) {
      AgentId topicId = (AgentId) topicIds.next();

      // JORAM_PERF_BRANCH
      TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);
      
      updateSubscriptionToTopic(topicId, -1, -1, tSub.isDurable());
    }

    // Deleting the temporary destinations:
    AgentId destId;
    for (Iterator dests = activeCtx.getTempDestinations(); dests.hasNext();) {
      destId = (AgentId) dests.next();
      activeCtx.removeTemporaryDestination(destId);
      
      // JORAM_PERF_BRANCH
      modifiedClient(activeCtx);
      
      deleteTemporaryDestination(destId);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Deletes temporary" + " destination " + destId.toString());
    }

    // Saving the prepared transactions.
    Iterator xids = activeCtx.getTxIds();
    Xid xid;
    XACnxPrepare recoveredPrepare;
    XACnxPrepare prepare;
    while (xids.hasNext()) {
      if (recoveredTransactions == null)
        recoveredTransactions = new Hashtable();

      xid = (Xid) xids.next();

      recoveredPrepare = (XACnxPrepare) recoveredTransactions.get(xid);
      prepare = activeCtx.getTxPrepare(xid);
      
      // JORAM_PERF_BRANCH
      modifiedClient(activeCtx);

      if (recoveredPrepare == null)
        recoveredTransactions.put(xid, prepare);
      else {
        recoveredPrepare.getSendings().addAll(prepare.getSendings());
        recoveredPrepare.getAcks().addAll(prepare.getAcks());
      }
    }

    // Finally, deleting the context:
    ClientContext cc = contexts.remove(new Integer(key));
    
    // JORAM_PERF_BRANCH
    cc.delete();
    
    activeCtx = null;
    setActiveCtxId(-1);

    CnxCloseReply reply = new CnxCloseReply();
    reply.setCorrelationId(req.getRequestId());
    sendToClient(key, reply, false);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "contexts=" + contexts);
  }

  private void doReact(int key, ActivateConsumerRequest req) {
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
    sub.setActive(req.getActivate());

    if (sub.getActive() > 0 ) {
      ConsumerMessages consM = sub.deliver();
      
      //JORAM_PERF_BRANCH
      modifiedSubscription(sub);
      
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
  
  private void doReact(int key, CommitRequest req) {
    // The commit may involve some local agents
    int asyncReplyCount = 0;
    
    // JORAM_PERF_BRANCH
    /*
    Enumeration pms = req.getProducerMessages();
    if (pms != null) {
      while (pms.hasMoreElements()) {
        ProducerMessages pm = (ProducerMessages) pms.nextElement();
        AgentId destId = AgentId.fromString(pm.getTarget());
        ClientMessages not = new ClientMessages(key, 
            req.getRequestId(), pm.getMessages());
        setDmq(not);    
        if (destId.getTo() == getId().getTo()) {
          // local sending
          not.setPersistent(false);
          if (req.getAsyncSend()) {
            not.setAsyncSend(true);
          } else {
            asyncReplyCount++;
          }
        }
        sendNot(destId, not);
      }
    }
    */
    
    Enumeration acks = req.getAckRequests();
    if (acks != null) {
      while (acks.hasMoreElements()) {
        SessAckRequest sar = (SessAckRequest) acks.nextElement();
        
        if (sar.getQueueMode()) {
          // JORAM_PERF_BRANCH
          /*
          AgentId qId = AgentId.fromString(sar.getTarget());
          Vector ids = sar.getIds();
          AcknowledgeRequest not = new AcknowledgeRequest(activeCtxId, req
              .getRequestId(), ids);
          if (qId.getTo() == getId().getTo()) {
            // local sending
            not.setPersistent(false);
            // No reply to wait for
          }

          sendNot(qId, not);
          */
        } else {
          String subName = sar.getTarget();
          
          // JORAM_PERF_BRANCH
          ClientSubscription sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
          
          if (sub != null) {
            sub.acknowledge(sar.getIds().iterator());
            
            //JORAM_PERF_BRANCH
            modifiedSubscription(sub);
            
            // TODO (AF): is it needed to save the proxy ?
            // if (sub.getDurable())
            
            // JORAM_PERF_BRANCH
            // already done in ClientSubscription
            // setSave();
          }
        }
      }
    }
   
    // JORAM_PERF_BRANCH
    /*
    if (!req.getAsyncSend()) {
      if (asyncReplyCount == 0) {
        sendNot(getId(), new SendReplyNot(key, req
            .getRequestId()));
      } else {
        // we need to wait for the replies
        // from the local agents
        // before replying to the client.
        activeCtx.addMultiReplyContext(req.getRequestId(), asyncReplyCount);
      }
    }
    */
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
  private void doFwd(AgentId from, AbstractReplyNot rep) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "--- " + this + " got " + rep.getClass().getName() + " with id: " + rep.getCorrelationId()
              + " from: " + from);

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
      logger.log(BasicLevel.DEBUG, "UserAgent.doFwd(" + from + ',' + rep + ')');

    try {
      // Updating the active context:
      setCtx(rep.getClientContext());

      // If the receive request being replied has been cancelled, denying
      // the message.
      if (rep.getCorrelationId() == activeCtx.getCancelledReceive()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> cancelled receive: id=" + activeCtx.getCancelledReceive());

        if (rep.getSize() > 0) {
          Vector msgList = rep.getMessages();
          for (int i = 0; i < msgList.size(); i++) {
            Message msg = new Message((org.objectweb.joram.shared.messages.Message) msgList.elementAt(i));
            String msgId = msg.getId();

            if (logger.isLoggable(BasicLevel.INFO))
              logger.log(BasicLevel.INFO, " -> denying message: " + msgId);

            sendNot(from, new DenyRequest(0, rep.getCorrelationId(), msgId));
          }
        }
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> reply");

        ConsumerMessages jRep;

        // Building the reply and storing the wrapped message id for later
        // denying in the case of a failure:
        if (rep.getSize() > 0) {
          jRep = new ConsumerMessages(rep.getCorrelationId(), rep.getMessages(), from.toString(), true);
          activeCtx.addDeliveringQueue(from);
          
          // JORAM_PERF_BRANCH
          modifiedClient(activeCtx);
          
        } else {
          jRep = new ConsumerMessages(rep.getCorrelationId(), (Vector) null, from.toString(), true);
        }

        // If the context is started, delivering the message, or buffering it:
        if (activeCtx.getActivated()) {
          doReply(jRep, true);
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
          String msgId = msg.getId();

          if (logger.isLoggable(BasicLevel.INFO))
            logger.log(BasicLevel.INFO, "Denying message: " + msgId);

          sendNot(from, new DenyRequest(0, rep.getCorrelationId(), msgId));
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
      msgTxname = 'M' + getId().toString() + '_';
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
    List messages = new ArrayList();
    for (Iterator msgs = rep.getMessages().iterator(); msgs.hasNext();) {
      Message message = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
      // Setting the arrival order of the messages
      message.order = arrivalsCounter++;
      messages.add(message);
    }
    
    // JORAM_PERF_BRANCH
    MemoryController.getMemoryController().checkMemory(rep);

    for (Iterator names = tSub.getNames(); names.hasNext();) {
      subName = (String) names.next();
      
      // JORAM_PERF_BRANCH
      sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
      
      if (sub == null) continue;

      // Browsing the delivered messages.
      sub.browseNewMessages(messages);
      
      // JORAM_PERF_BRANCH
      modifiedSubscription(sub);
    }

    // Save message if it is delivered to a durable subscription.
    for (Iterator msgs = messages.iterator(); msgs.hasNext();) {
      Message message = (Message) msgs.next();

      if (message.durableAcksCounter > 0) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> save message " + message);
        // TODO (AF): The message saving does it need the proxy saving ?
        
        // JORAM_PERF_BRANCH
        if (message.isPersistent()) {
          setSave();
        }
        
        // Persisting the message.
        setMsgTxName(message);
        message.save();
        message.releaseFullMessage();
      }
    } 

    for (Iterator names = tSub.getNames(); names.hasNext();) {
      subName = (String) names.next();
      
      // JORAM_PERF_BRANCH
      sub = (ClientSubscription) subsTable.get(new EncodedString(subName));
      
      if (sub == null) continue;

      // If the subscription is active, launching a delivery sequence.
      if (sub.getActive() > 0 ) {
        ConsumerMessages consM = sub.deliver();
        
        //JORAM_PERF_BRANCH
        modifiedSubscription(sub);
        
        if (consM != null) {
          try {
            setCtx(sub.getContextId());
            if (activeCtx.getActivated())
              doReply(consM, true);
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
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + from + ',' + rep + ')');
    MomException exc = rep.getException();

    // The exception comes from a topic refusing the access: deleting the subs.
    if (exc instanceof AccessException) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + from + ')');
      TopicSubscription tSub = (TopicSubscription) topicsTable.remove(from);
      if (tSub != null) {
        String name;
        ClientSubscription sub;
        for (Iterator e = tSub.getNames(); e.hasNext();) {
          name = (String) e.next();
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Remove subsTable " + subsTable + " " + name);
          sub = (ClientSubscription) subsTable.remove(name);
          try {
            MXWrapper.unregisterMBean(getSubMBeanName(name));
          } catch (Exception e1) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e1);
          }
          sub.deleteMessages();

          try {
            setCtx(sub.getContextId());
            
            // JORAM_PERF_BRANCH
            activeCtx.removeSubName(new EncodedString(name));
            
            doReply(new MomExceptionReply(rep.getCorrelationId(), exc));
          } catch (StateException pExc) {
            // The context is lost: nothing to do.
          }
        }
        return;
      }
    }
    // Forwarding the exception to the client.
    try {
      setCtx(rep.getClientContext());
      doReply(new MomExceptionReply(rep.getCorrelationId(), exc));
    } catch (StateException pExc) {
      // The context is lost: nothing to do.
    }
  }
  
  private String getSubMBeanName(String name) {
    return getMBeanName().append(",sub=").append(name).toString();
  }

  /** 
   * An <code>AdminReply</code> acknowledges the setting of a temporary
   * destination; nothing needs to be done.
   */
  private void doReact(AdminReplyNot reply) {}

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
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + uA + ')');
    Notification not = uA.not;
    AgentId agId = uA.agent;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "--- " + this + " notified of invalid destination: " + agId.toString());

    // The deleted destination is a topic: deleting its subscriptions.
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + agId + ')');
    TopicSubscription tSub = (TopicSubscription) topicsTable.remove(agId);
    if (tSub != null) {
      String name;
      ClientSubscription sub;
      DestinationException exc;
      exc = new DestinationException("Destination " + agId + " does not exist.");
      for (Iterator e = tSub.getNames(); e.hasNext();) {
        name = (String) e.next();
        sub = (ClientSubscription) subsTable.remove(name);
        try {
          MXWrapper.unregisterMBean(getSubMBeanName(name));
        } catch (Exception e1) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e1);
        }
        sub.deleteMessages();
        
        // JORAM_PERF_BRANCH
        sub.delete();

        try {
          setCtx(sub.getContextId());
          
          // JORAM_PERF_BRANCH
          activeCtx.removeSubName(new EncodedString(name));
          
          doReply(new MomExceptionReply(sub.getSubRequestId(), exc));
        } catch (StateException pExc) {
          // The context is lost: nothing to do.
        }
      }
      return;
    }

    if (not instanceof AbstractRequestNot) {
      AbstractRequestNot req = (AbstractRequestNot) not;

      // If the wrapped request is messages sending,forwarding them to the DMQ:
      if (req instanceof ClientMessages) {
        // If the queue actually was a dead message queue, updating its
        // identifier:
        if (dmqId != null && agId.equals(dmqId)) {
          // state change, so save.
          setSave();
          dmqId = null;
          for (Iterator subs = subsTable.values().iterator(); subs.hasNext();)
            ((ClientSubscription) subs.next()).setDMQId(null);
        }
        // Sending the messages again if not coming from the default DMQ:
        if (Queue.getDefaultDMQId() != null && !agId.equals(Queue.getDefaultDMQId())) {
          DMQManager dmqManager = new DMQManager(dmqId, null);
          Iterator msgs = ((ClientMessages) req).getMessages().iterator();
          while (msgs.hasNext()) {
            org.objectweb.joram.shared.messages.Message msg = (org.objectweb.joram.shared.messages.Message) msgs.next();
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
        DestinationException exc = new DestinationException("Destination " + agId + " does not exist.");
        MomExceptionReply mer = new MomExceptionReply(req.getRequestId(), exc);
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
          // Do nothing (the context doesn't exist any more).
        }
      }
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "Connection " + req.getClientContext()
            + " notified of the deletion of destination " + agId);
    }
  }

  private void doReact(FwdAdminRequestNot not) {
    AdminRequest adminRequest = not.getRequest();
    
    if (adminRequest instanceof GetSubscriptions) {
      doReact((GetSubscriptions) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscriptionMessageIds) {
      doReact((GetSubscriptionMessageIds) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscriptionMessage) {
      doReact((GetSubscriptionMessage) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof DeleteSubscriptionMessage) {
      doReact((DeleteSubscriptionMessage) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscription) {
      doReact((GetSubscription) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof ClearSubscription) {
      doReact((ClearSubscription) adminRequest, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetNbMaxMsgRequest) {
      GetNbMaxMsgRequest request = (GetNbMaxMsgRequest) adminRequest;
      
      int nbMaxMsg = -1;
      if (request.getSubName() == null) {
        nbMaxMsg = this.nbMaxMsg;
      } else {
        ClientSubscription sub = (ClientSubscription) subsTable.get(request.getSubName());
        if (sub != null)
          nbMaxMsg = sub.getNbMaxMsg();
      }
      replyToTopic(new GetNumberReply(nbMaxMsg), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetDMQSettingsRequest) {
      String subName = ((GetDMQSettingsRequest) adminRequest).getSubName();

      String dmq = (dmqId != null) ? dmqId.toString() : null;
      int threshold = -1;
      if (subName == null) {
        threshold = this.threshold;
      } else {
        ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
        if (sub != null) {
          threshold = sub.getThreshold();
        }
      }
      replyToTopic(new GetDMQSettingsReply(dmq, threshold),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof SetDMQRequest) {
      setSave();
      
      if (((SetDMQRequest)adminRequest).getDmqId() != null)
        dmqId = AgentId.fromString(((SetDMQRequest)adminRequest).getDmqId());
      else
        dmqId = null;

      for (Iterator subs = subsTable.values().iterator(); subs.hasNext();) {
        ClientSubscription cs =  ((ClientSubscription) subs.next());
        cs.setDMQId(dmqId);
        
        // JORAM_PERF_BRANCH
        modifiedSubscription(cs);
      }

      replyToTopic(new AdminReply(true, null), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof SetThresholdRequest) {
      setSave(); // state change, so save.
      int threshold = ((SetThresholdRequest) adminRequest).getThreshold();
      
      AdminReply reply = null;
      String subName = ((SetThresholdRequest) adminRequest).getSubName();
      if (subName == null) {
        // Set the default value for new subscriptions of this user
        this.threshold = threshold;
        reply = new AdminReply(true, null);
      } else {
        // Set the given subscription
        ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
        if (sub != null) {
          sub.setThreshold(threshold);
          
          // JORAM_PERF_BRANCH
          modifiedSubscription(sub);
          
          reply = new AdminReply(true, null);
        } else {
          reply = new AdminReply(AdminReply.NAME_UNKNOWN, "Subscription unknow: " + subName);
        }
      }

      replyToTopic(reply, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof SetNbMaxMsgRequest) {
      setSave(); // state change, so save.
      int nbMaxMsg = ((SetNbMaxMsgRequest) adminRequest).getNbMaxMsg();
      
      AdminReply reply = null;
      String subName = ((SetNbMaxMsgRequest) adminRequest).getSubName();
      if (subName == null) {
        // Set the default value for new subscriptions of this user
        this.nbMaxMsg = nbMaxMsg;
        reply = new AdminReply(true, null);
      } else {
        // Set the given subscription
        ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
        if (sub != null) {
          sub.setNbMaxMsg(nbMaxMsg);
          reply = new AdminReply(true, null);
        } else {
          reply = new AdminReply(AdminReply.NAME_UNKNOWN, "Subscription unknow: " + subName);
        }
      }

      replyToTopic(reply, not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof DeleteUser) {
      deleteProxy(not);
    } else if (adminRequest instanceof AdminCommandRequest) {
    	doReact((AdminCommandRequest) adminRequest, not.getReplyTo(), not.getRequestMsgId());
    } else {
      logger.log(BasicLevel.ERROR, "Unknown administration request for proxy " + getId());
      replyToTopic(new AdminReply(AdminReply.UNKNOWN_REQUEST, null), not.getReplyTo(), not.getRequestMsgId(),
          not.getReplyMsgId());
      
    }
  }

  private void doReact(AdminCommandRequest request, AgentId replyTo, String requestMsgId) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doReact(" + request + ", " + replyTo + ", " + requestMsgId + ')');
  	Properties prop = null;
  	Properties replyProp = null;
  	try {
			switch (request.getCommand()) {
			case AdminCommandConstant.CMD_ADD_INTERCEPTORS:
				prop = request.getProp();
				if (interceptorsOUT == null)
					interceptorsOUT = new ArrayList();
				InterceptorsHelper.addInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS_OUT), interceptorsOUT);
				interceptors_out = InterceptorsHelper.getListInterceptors(interceptorsOUT);
				if (interceptorsIN == null)
					interceptorsIN = new ArrayList();
				InterceptorsHelper.addInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS_IN), interceptorsIN);
				interceptors_in = InterceptorsHelper.getListInterceptors(interceptorsIN);
				// state change
				setSave();
				break;
			case AdminCommandConstant.CMD_REMOVE_INTERCEPTORS:
				prop = request.getProp();
				InterceptorsHelper.removeInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS_OUT), interceptorsOUT);
				interceptors_out = InterceptorsHelper.getListInterceptors(interceptorsOUT);
				InterceptorsHelper.removeInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS_IN), interceptorsIN);
				interceptors_in = InterceptorsHelper.getListInterceptors(interceptorsIN);
				if (interceptorsIN != null && interceptorsIN.isEmpty())
					interceptorsIN = null;
				if (interceptorsOUT != null && interceptorsOUT.isEmpty())
					interceptorsOUT = null;
				// state change
				setSave();
				break;
			case AdminCommandConstant.CMD_GET_INTERCEPTORS:
				replyProp = new Properties();
				if (interceptors_in == null) {
	                replyProp.put(AdminCommandConstant.INTERCEPTORS_IN, "");
				} else {
	                replyProp.put(AdminCommandConstant.INTERCEPTORS_IN, InterceptorsHelper.getListInterceptors(interceptorsIN));
				}
				if (interceptors_out == null) {
	                replyProp.put(AdminCommandConstant.INTERCEPTORS_OUT, "");
				} else {
	                replyProp.put(AdminCommandConstant.INTERCEPTORS_OUT, InterceptorsHelper.getListInterceptors(interceptorsOUT));
				}
				break;
			case AdminCommandConstant.CMD_REPLACE_INTERCEPTORS:
				prop = request.getProp();
				if (interceptorsIN == null && prop.containsKey(AdminCommandConstant.INTERCEPTORS_IN_NEW))
					throw new Exception("interceptorsIN == null.");
				if (interceptorsOUT == null && prop.containsKey(AdminCommandConstant.INTERCEPTORS_OUT_NEW))
					throw new Exception("interceptorsOUT == null.");
				// replace IN interceptor
				InterceptorsHelper.replaceInterceptor(
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_IN_NEW)), 
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_IN_OLD)), 
						interceptorsIN);
				interceptors_in = InterceptorsHelper.getListInterceptors(interceptorsIN);
				// replace OUT interceptor
				InterceptorsHelper.replaceInterceptor(
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_OUT_NEW)), 
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_OUT_OLD)), 
						interceptorsOUT);
				interceptors_out = InterceptorsHelper.getListInterceptors(interceptorsOUT);
				// state change
				setSave();
				break;

			default:
				throw new Exception("Bad command : \"" + request.getCommand() + "\"");
			}
			// reply
			replyToTopic(new AdminCommandReply(true, AdminCommandConstant.commandNames[request.getCommand()] + " done.", replyProp), replyTo, requestMsgId, requestMsgId);
		} catch (Exception exc) {
			if (logger.isLoggable(BasicLevel.WARN))
				logger.log(BasicLevel.WARN, "", exc);
			replyToTopic(new AdminReply(-1, exc.getMessage()), replyTo, requestMsgId, requestMsgId);
		}
  }
  
  private void doReact(GetSubscriptions request, AgentId replyTo, String requestMsgId, String replyMsgId) {
    Iterator subsIterator = subsTable.entrySet().iterator();
    String[] subNames = new String[subsTable.size()];
    String[] topicIds = new String[subsTable.size()];
    int[] messageCounts = new int[subsTable.size()];
    int[] ackCounts = new int[subsTable.size()];
    boolean[] durable = new boolean[subsTable.size()];
    int i = 0;
    while (subsIterator.hasNext()) {
      Map.Entry subEntry = (Map.Entry) subsIterator.next();
      subNames[i] = (String) subEntry.getKey();
      ClientSubscription cs = (ClientSubscription) subEntry.getValue();
      topicIds[i] = cs.getTopicId().toString();
      messageCounts[i] = cs.getPendingMessageCount();
      ackCounts[i] = cs.getDeliveredMessageCount();
      durable[i] = cs.getDurable();
      i++;
    }
    GetSubscriptionsRep reply = new GetSubscriptionsRep(
      subNames, topicIds, messageCounts, ackCounts, durable);
    replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
  }

  /**
   * Returns the list of subscriptions for this user. Each subscription is
   * identified by its unique 'symbolic' name.
   *
   * @return The list of subscriptions for this user.
   */
  public String[] getSubscriptionNames() {
    return (String[]) subsTable.keySet().toArray(new String[subsTable.size()]);
  }

  private void doReact(GetSubscriptionMessageIds request, AgentId replyTo, String requestMsgId,
      String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription) subsTable.get(subName);
    }
    if (cs != null) {
      GetSubscriptionMessageIdsRep reply = new GetSubscriptionMessageIdsRep(cs.getMessageIds());
      replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(new AdminReply(false, "Subscription not found: " + request.getSubscriptionName()),
          replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(GetSubscription request, AgentId replyTo, String requestMsgId, String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription) subsTable.get(subName);
    }
    if (cs != null) {
      GetSubscriptionRep reply = new GetSubscriptionRep(cs.getTopicId().toString(),
          cs.getPendingMessageCount(), cs.getDeliveredMessageCount(), cs.getDurable());
      replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(new org.objectweb.joram.shared.admin.AdminReply(false, "Subscription not found: "
          + request.getSubscriptionName()), replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(GetSubscriptionMessage request, AgentId replyTo, String requestMsgId, String replyMsgId) {
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
        replyToTopic(new AdminReply(false, "Message not found: " + request.getMessageId()), 
                     replyTo, requestMsgId, replyMsgId);
      }
    } else {
      replyToTopic(new AdminReply(false, "Subscription not found: " + subName),
                   replyTo, requestMsgId, replyMsgId);
    }
  }

  private void doReact(DeleteSubscriptionMessage request, AgentId replyTo, String requestMsgId,
      String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription) subsTable.get(subName);
    }
    if (cs != null) {
      cs.deleteMessage(request.getMessageId());
      
      //JORAM_PERF_BRANCH
      modifiedSubscription(cs);
      
      replyToTopic(new AdminReply(true, null), replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(new AdminReply(false, "Subscription not found: " + request.getSubscriptionName()),
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
  public void deleteSubscriptionMessage(String subName, String msgId) {
    ClientSubscription cs = (ClientSubscription) subsTable.get(subName);
    if (cs != null) {
      cs.deleteMessage(msgId);
      
      //JORAM_PERF_BRANCH
      modifiedSubscription(cs);
    }
  }
  
  private void doReact(ClearSubscription request, AgentId replyTo, String requestMsgId, String replyMsgId) {
    String subName = request.getSubscriptionName();
    ClientSubscription cs = null;
    if (subName != null) {
      cs = (ClientSubscription) subsTable.get(subName);
    }
    if (cs != null) {
      cs.clear();
      
      // JORAM_PERF_BRANCH
      modifiedSubscription(cs);
      
      replyToTopic(new AdminReply(true, null), replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(new AdminReply(false, "Subscription not found: " + request.getSubscriptionName()),
          replyTo, requestMsgId, replyMsgId);
    }
  }

  private void replyToTopic(AdminReply reply, AgentId replyTo, String requestMsgId, String replyMsgId) {
    if (replyTo == null) // In some cases the request needs no response
      return;
    
    org.objectweb.joram.shared.messages.Message message = MessageHelper.createMessage(replyMsgId,
        requestMsgId, replyTo.toString(), DestinationConstants.TOPIC_TYPE);
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
      logger.log(BasicLevel.DEBUG, "UserAgent.setCtx(" + key + ')');

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
    if (reply instanceof MomExceptionReply) {
      logger.log(BasicLevel.ERROR, "" + reply, new Exception());
    }
    doReply(reply, false);
  }
  
  private void doReply(AbstractJmsReply reply, boolean asyncSend) {
    sendToClient(activeCtxId, reply, asyncSend);
  }

  protected ClientContext getClientContext(int ctxId) {
    return (ClientContext)contexts.get(new Integer(ctxId));
  }

  protected void cleanPendingMessages(long currentTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.cleanPendingMessages(" + messagesTable.size() + ')');
    
    Message message = null;
    DMQManager dmqManager = null;

    for (Iterator values = messagesTable.values().iterator(); values.hasNext();) {
      message = (Message) values.next();
      if ((message == null) || message.isValid(currentTime))
        continue;

      values.remove();
      if (message.durableAcksCounter > 0)
        message.delete();

      if (dmqManager == null)
        dmqManager = new DMQManager(dmqId, null);
      nbMsgsSentToDMQSinceCreation++;
      dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "UserAgent expired message " + message.getId());
    }
    
    Iterator subs = subsTable.values().iterator();
    while (subs.hasNext()) {
      ((ClientSubscription) subs.next()).cleanMessageIds();
    }
    
    // If needed, sending the dead messages to the DMQ:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.cleanPendingMessages -> " + messagesTable.size());
  }

  public void delete() {
    DeleteUser request = new DeleteUser(getName(), getId().toString());
    FwdAdminRequestNot deleteNot = new FwdAdminRequestNot(request, null, null);
    Channel.sendTo(AdminTopic.getDefault(), deleteNot);
  }

  /**
   * This method deletes the proxy by notifying its connected clients,
   * denying the non acknowledged messages, deleting the temporary
   * destinations, removing the subscriptions.
   *
   * @exception Exception  If the requester is not an administrator.
   */
  private void deleteProxy(FwdAdminRequestNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " notified to be deleted.");

    String userName = ((DeleteUser) not.getRequest()).getUserName();
    if (contexts.size() > 0) {
      String info = "Delete proxy request successful [false]: proxy [" + getId() + "] of user ["
          + userName + "] is currently in use.";

      if (not.getReplyTo() != null) {
        replyToTopic(new AdminReply(AdminReply.PERMISSION_DENIED, info), not.getReplyTo(),
            not.getRequestMsgId(), not.getReplyMsgId());
      }
      return;
    }

    AdminTopic.deleteUser(userName);

    String info = "Delete proxy request successful [true]: proxy [" + getId() + "] of user ["
        + userName + "] has been notified of deletion";

    if (not.getReplyTo() != null) {
      replyToTopic(new AdminReply(true, info), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    }

    // Removing all proxy's subscriptions:
    AgentId destId;
    for (Iterator topics = topicsTable.keySet().iterator(); topics.hasNext();) {
      destId = (AgentId) topics.next();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + destId + ')');
      topics.remove();
      updateSubscriptionToTopic(destId, -1, -1, false);
    }
    
    // Delete all subscriptions
    for (Iterator subs = subsTable.entrySet().iterator(); subs.hasNext();) {
      Map.Entry subEntry = (Entry) subs.next();
      String subName = (String) subEntry.getKey();
      ClientSubscription sub = (ClientSubscription) subEntry.getValue();

      // Deleting the subscription.
      sub.deleteMessages();
      try {
        MXWrapper.unregisterMBean(getSubMBeanName(subName));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e);
      }
    }

    Channel.sendTo(getId(), new DeleteNot());

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
      int requestId,
      boolean durable) {
    return updateSubscriptionToTopic(topicId, contextId, requestId, false, durable);
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
  // JORAM_PERF_BRANCH: + durable
  private boolean updateSubscriptionToTopic(AgentId topicId, int contextId, int requestId, boolean asyncSub, boolean durable) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.updateSubscriptionToTopic(" + topicId + ',' + contextId + ','
          + requestId + ',' + asyncSub + ')');

    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);

    // No more subs to this topic: unsubscribing.
    if (tSub == null || tSub.isEmpty()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + topicId + ')');
      topicsTable.remove(topicId);
      sendNot(topicId,
                         new UnsubscribeRequest(contextId, requestId));
      return false;
    }

    // Otherwise, updating the subscription if the selector evolved.
    String builtSelector = tSub.buildSelector();
    if (tSub.getLastSelector() != null
        && builtSelector.equals(tSub.getLastSelector()))
      return false;

    tSub.setLastSelector(builtSelector);
    SubscribeRequest req = new SubscribeRequest(contextId, requestId, builtSelector, asyncSub, durable);
    sendNot(topicId, req);
    
    // send reply if asynchronous subscription request.
    if (asyncSub) {
      doFwd(new SubscribeReply(req));
    }
    
    return true;
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }
  
  // JORAM_PERF_BRANCH
  public int getClassId() {
    return JoramHelper.USERAGENT_CLASS_ID;
  }

  // JORAM_PERF_BRANCH
  public void encodeTransactionObject(DataOutputStream os) throws IOException {
    super.encodeTransactionObject(os);
    os.writeLong(arrivalsCounter);
    
    /*
    os.writeInt(contexts.size());
    Iterator<Entry<Integer, ClientContext>> contextIterator = contexts.entrySet().iterator();
    while (contextIterator.hasNext()) {
      Entry<Integer, ClientContext> context = contextIterator.next();
      os.writeInt(context.getKey());
      context.getValue().encodeTransactionObject(os);
    }
    */
    
    if (dmqId == null) {
      os.writeBoolean(true);
    } else {
      os.writeBoolean(false);
      dmqId.encodeTransactionObject(os);
    }
    // TODO: interceptors_in
    // TODO: interceptors_out
    os.writeInt(keyCounter);
    os.writeInt(nbMaxMsg);
    os.writeLong(nbMsgsSentToDMQSinceCreation);
    os.writeLong(period);
    // TODO: recoveredTransactions
    
    /*
    os.writeInt(subsTable.size());
    Iterator<Entry<EncodedString, ClientSubscription>> subsTableIterator = subsTable.entrySet().iterator();
    while (subsTableIterator.hasNext()) {
      Entry<EncodedString, ClientSubscription> context = subsTableIterator.next();
      //os.writeUTF(context.getKey());
      context.getKey().writeTo(os);
      context.getValue().encodeTransactionObject(os);
    }
    */
    
    os.writeInt(threshold); 
  }
  
  // JORAM_PERF_BRANCH
  public void decodeTransactionObject(DataInputStream is) throws IOException {
    super.decodeTransactionObject(is);
    arrivalsCounter = is.readLong();
    
    /*
    int contextsSize = is.readInt();
    contexts = new Hashtable<Integer, ClientContext>(contextsSize);
    for (int i = 0; i < contextsSize; i++) {
      Integer key = is.readInt();
      ClientContext value = new ClientContext();
      value.decodeTransactionObject(is);
      contexts.put(key, value);
    }
    */
    
    boolean isNull = is.readBoolean();
    if (isNull) {
      dmqId = null;
    } else {
      dmqId = new AgentId((short) 0, (short) 0, 0);
      dmqId.decodeTransactionObject(is);
    }
    // TODO: interceptors_in
    interceptors_in = null;
    // TODO: interceptors_out
    interceptors_out = null;
    keyCounter = is.readInt();
    nbMaxMsg = is.readInt();
    nbMsgsSentToDMQSinceCreation = is.readLong();
    period = is.readLong();
    // TODO: recoveredTransactions
    recoveredTransactions = null;
    
    /*
    int subsTableSize = is.readInt();
    subsTable = new Hashtable<EncodedString, ClientSubscription>(subsTableSize);
    for (int i = 0; i < subsTableSize; i++) {
      //String key = is.readUTF();
      EncodedString key = new EncodedString();
      key.readFrom(is);
      
      ClientSubscription value = new ClientSubscription();
      value.decodeTransactionObject(is);
      subsTable.put(key, value);
    }
    */
    
    threshold = is.readInt(); 
  }
  
  //JORAM_PERF_BRANCH
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encodeUnsignedLong(arrivalsCounter);
    
    if (dmqId == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      dmqId.encode(encoder);
    }
    // TODO: interceptors_in
    // TODO: interceptors_out
    encoder.encodeUnsignedInt(keyCounter);
    encoder.encodeUnsignedInt(nbMaxMsg);
    encoder.encodeUnsignedLong(nbMsgsSentToDMQSinceCreation);
    encoder.encodeUnsignedLong(period);
    // TODO: recoveredTransactions
    
    encoder.encodeUnsignedInt(threshold); 
  }

  //JORAM_PERF_BRANCH
  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    arrivalsCounter = decoder.decodeUnsignedLong();
    
    boolean isNull = decoder.decodeBoolean();
    if (isNull) {
      dmqId = null;
    } else {
      dmqId = new AgentId((short) 0, (short) 0, 0);
      dmqId.decode(decoder);
    }
    // TODO: interceptors_in
    interceptors_in = null;
    // TODO: interceptors_out
    interceptors_out = null;
    keyCounter = decoder.decodeUnsignedInt();
    nbMaxMsg = decoder.decodeUnsignedInt();
    nbMsgsSentToDMQSinceCreation = decoder.decodeUnsignedLong();
    period = decoder.decodeUnsignedLong();
    // TODO: recoveredTransactions
    recoveredTransactions = null;
    
    threshold = decoder.decodeUnsignedInt();
  } 
  
  //JORAM_PERF_BRANCH
  public int getEncodedSize() throws Exception {
    int encodedSize = super.getEncodedSize();
    encodedSize += 8;
    encodedSize += 1;
    if (dmqId != null) {
      encodedSize += dmqId.getEncodedSize();
    }
    encodedSize += 4;
    encodedSize += 4;
    encodedSize += 8;
    encodedSize += 8;
    encodedSize += 4;
    return encodedSize;
  }
  
  //JORAM_PERF_BRANCH
  public long getUsedMemorySize() {
    return MemoryController.getMemoryController().getUsedMemorySize();
  }
  
  //JORAM_PERF_BRANCH
  public int getMemoryCallbackCount() {
    return MemoryController.getMemoryController().getMemoryCallbackCount();
  }
  
  //JORAM_PERF_BRANCH
  public static class UserAgentFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new UserAgent(null);
    }

  }
  
}
