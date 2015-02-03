/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2015 ScalAgent Distributed Technologies
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.Vector;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.dest.AdminTopic.DestinationDesc;
import org.objectweb.joram.mom.dest.Destination;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.Topic;
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
import org.objectweb.joram.mom.notifications.GetClientSubscriptions;
import org.objectweb.joram.mom.notifications.ClientSubscriptionNot;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.ReconnectSubscribersNot;
import org.objectweb.joram.mom.notifications.SubscribeReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.TopicDeliveryTimeNot;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.InterceptorsHelper;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.mom.util.MessageInterceptor;
import org.objectweb.joram.mom.util.MessageTable;
import org.objectweb.joram.mom.util.MessageTableFactory;
import org.objectweb.joram.mom.util.TopicDeliveryTimeTask;
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
import org.objectweb.joram.shared.client.AddClientIDReply;
import org.objectweb.joram.shared.client.AddClientIDRequest;
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

import com.scalagent.scheduler.ScheduleEvent;
import com.scalagent.scheduler.Scheduler;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.CallbackNotification;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.CountDownCallback;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.WakeUpTask;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;
import fr.dyade.aaa.util.Transaction;
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
  
  public static final String ARRIVAL_STATE_PREFIX = "AS_";
  public static final String MESSAGE_TABLE_PREFIX = "MT_";

  /** the in and out interceptors list. */
  private transient List<MessageInterceptor> interceptorsOUT = null;
  private transient List<MessageInterceptor> interceptorsIN = null;
  private List<Properties> interceptorsPropIN = null;
  private List<Properties> interceptorsPropOUT = null;
  
  /** Map contains the clientID */
  private transient Map<Integer, String> clientIDs = new HashMap<Integer, String>();

  /** period to run the cleaning task, by default 60s. */
  private long period = 60000L;

  /** the number of erroneous messages forwarded to the DMQ */
  private long nbMsgsSentToDMQSinceCreation = 0;

  /**
   * The ClientContexts to be saved after a react.
   */
  private transient List<ClientContext> modifiedClientContexts;

  /**
   * The ClientSubscriptions to be saved after a react.
   */
  private transient List<ClientSubscription> modifiedClientSubscriptions;

  /**
   * Returns the period value of this queue, -1 if not set.
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
   * 
   * @return the default DMQ for subscription of this user.
   */
  public String getDMQId() {
    if (dmqId != null) return dmqId.toString();
    return null;
  }

  /**
   * Threshold above which messages are considered as undeliverable because
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
  private Map<String, ClientSubscription> subsTable;
  
  /**
   * Table holding the <code>SharedCtx</code> instances.
   * <p>
   * <b>Key:</b> subscription name<br>
   * <b>Value:</b> the shared context
   */
  private transient Map<String, SharedCtx> sharedSubs;
  
  /**
   * This kind of SharedCts (LinkedHashMap) is well-suited to building LRU caches.
   */
  class SharedCtx extends LinkedHashMap<Integer, Integer> {
    SharedCtx(int ctxId, int requestId) {
      super(100, 1.1f, true);
      put(ctxId, requestId);
    }
  }
  
  /**
   * <b>Key:</b> subscription name<br>
   * <b>Value:</b> clientID
   */
  private Properties subsClientIDs;
  
  /**
   * Table holding the recovered transactions branches.
   * <p>
   * <b>Key:</b> transaction identifier<br>
   * <b>Value:</b> <code>XACnxPrepare</code> instance
   */
  private Map<Xid, XACnxPrepare> recoveredTransactions;

  /** Counter of message arrivals from topics. */
  private UserAgentArrivalState arrivalState;

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
  private transient MessageTable messagesTable;

  /**
   * Identifier of the active context. 
   * Value -1 means that there's no active context.
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

  protected transient Scheduler deliveryScheduler = null;

  /**
   * Used by the Encodable framework
   */
  protected UserAgent(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.agentInitialize(" + firstTime + ')');

    modifiedClientContexts = new ArrayList<ClientContext>();
    modifiedClientSubscriptions = new ArrayList<ClientSubscription>();
    clientIDs = new HashMap<Integer, String>();
    sharedSubs = new HashMap<String, SharedCtx>();

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
  
  public int getMessageTableConsumedMemory() {
    return messagesTable.getConsumedMemory();
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
    // if (not instanceof SetDMQRequest)
    // doReact(from, (SetDMQRequest) not);
    // else
    // if (not instanceof SetThresholdRequestNot)
    // doReact(from, (SetThresholdRequestNot) not);
    // else
    // if (not instanceof SetNbMaxMsgRequest)
    // doReact(from, (SetNbMaxMsgRequest) not);
    // else if (not instanceof GetNbMaxMsgRequestNot)
    // doReact(from, (GetNbMaxMsgRequestNot) not);
    // else if (not instanceof GetDMQSettingsRequestNot)
    // doReact(from, (GetDMQSettingsRequestNot) not);
    // else

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
    } else if (not instanceof TopicDeliveryTimeNot) {
      doReact((TopicDeliveryTimeNot) not);
    } else if (not instanceof GetClientSubscriptions) {
      doReact(from, (GetClientSubscriptions) not);
    } else if (not instanceof ReconnectSubscribersNot) {
      doReact(from, (ReconnectSubscribersNot) not);
    } else {
      super.react(from, not);
    }
  }
  
  protected void agentSave() throws IOException {
    super.agentSave();
    arrivalState.save();
    saveModifiedClientContexts();
    saveModifiedClientSubscriptions();
  }
  
  /**
   * Used to get number of local subscribers to 'from'.
   * This number is sent as an Admin reply.
   * 
   * @param from should be a Topic agent ID.
   * @param not contains the original Admin not sent to 'from'.
   */
  private void doReact(AgentId from, GetClientSubscriptions not) {
	FwdAdminRequestNot aNot = (FwdAdminRequestNot) not.getAdminNot();
	int ls = ((TopicSubscription) topicsTable.get(from)).size();
	
    replyToTopic(new GetNumberReply(ls),
      aNot.getReplyTo(), aNot.getRequestMsgId(), aNot.getReplyMsgId());
  }
  
  /**
   * Sends reconnection messages to one or more subscribers.
   * 
   * @param from
   * @param not
   */
  private void doReact(AgentId from, ReconnectSubscribersNot not) {
    ClientSubscription sub;
    ConsumerMessages consM;

    String subName = not.getSubName();
    ArrayList<org.objectweb.joram.shared.messages.Message> msgs = not.getMsgs();

    List<Message> messages = new ArrayList<Message>();
    messages.add(new Message(msgs.get(0)));

    if (subName != null) {
      // Redirect a specific subscriber.
      sub = subsTable.get(subName);
      sub.browseNewMessages(messages);
      consM = sub.deliver();
      logger.log(BasicLevel.ERROR, "Reconnection message sent.");
      try {
        setCtx(sub.getContextId());
        if (activeCtx.getActivated()) {
          doReply(consM);
        }
      } catch (StateException e) {
        logger.log(BasicLevel.ERROR, "Error while sending reconnection message..");
      }
    } else {
      // Redirect many subscribers..
      ArrayList<Integer> subs = not.getSubs();
      TopicSubscription tSub = (TopicSubscription) topicsTable.get(from);
      int i = 0;
      int s = subs.get(i);
      // message has already been initialized
      for (Iterator names = tSub.getNames(); names.hasNext();) {
        subName = (String) names.next();
        sub = (ClientSubscription) subsTable.get(subName);
        if (sub != null && sub.getActive() > 0) {
          sub.browseNewMessages(messages);
          consM = sub.deliver();
          logger.log(BasicLevel.ERROR, "");
          try {
            setCtx(sub.getContextId());
            if (activeCtx.getActivated()) {
              doReply(consM);
            }
          } catch (StateException e) {
            logger.log(BasicLevel.ERROR, "Error while sending reconnection message..\n");
          }
        }

        if (--s == 0) {
          if (++i < subs.size()) {
            s = subs.get(i);
            messages.set(0, new Message(msgs.get(i)));
          } else {
            break;
          }
        }
      }
    }

    // If there is an Admin request to reply to..
    FwdAdminRequestNot adr = not.getNot();
    if (adr != null) {
      replyToTopic(new AdminReply(true, null),
                   adr.getReplyTo(), adr.getRequestMsgId(), adr.getReplyMsgId());
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
    try {
      ctx = (ConnectionContext) Class.forName(not.getType().getClassName()).newInstance();
    } catch (Exception e) {
      if(logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"Error at context instanciation: ", e);
      return;
    }
    ctx.initialize(keyCounter, not);
    connections.put(objKey, ctx);
   
    
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
        reactToClientRequest(key.intValue(), request, not);

        if (ctx.isClosed()) {
          if (!(request instanceof CnxCloseRequest))
            logger.log(BasicLevel.WARN, "RequestNot on closed context: " + key);

          connections.remove(key);
          HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
          if (hbt != null) hbt.cancel();
        }
      } else if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "ConnectionContext not found");
    } else if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "No connections");
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
          // process interceptors
          ProducerMessages pm = processInterceptors(key, (ProducerMessages) request);
          if (pm != null)
            rm.put(req.getConnectionKey(), pm);
        } else if (request instanceof JmsRequestGroup) {
          JmsRequestGroup jrg = (JmsRequestGroup) request;
          AbstractJmsRequest[] groupedRequests = jrg.getRequests();
          for (int j = 0; j < groupedRequests.length; j++) {
            if (groupedRequests[j] instanceof ProducerMessages) {
              ProducerMessages pm = (ProducerMessages) groupedRequests[j];
              rm.put(req.getConnectionKey(), pm);
            } else {
              reactToClientRequest(key.intValue(), groupedRequests[j], null);
            }
          }
        } else {
          reactToClientRequest(key.intValue(), request, null);
        }
      }
    }
    rm.flush();
  }

  private void doReact(CloseConnectionNot2 not) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "CloseConnectionNot2: key=" + not.getKey());

    if (connections != null) {
      Integer key = new Integer(not.getKey());
      // The connection may have already been explicitly closed by a CnxCloseRequest.
      if (connections.containsKey(key)) {
        reactToClientRequest(not.getKey(), new CnxCloseRequest(), null);
        ConnectionContext ctx = (ConnectionContext) connections.remove(key);

        HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
        if (hbt != null) hbt.cancel();

        if (ctx != null) {
          MomException exc = new MomException(MomExceptionReply.HBCloseConnection,
                                              "Connection " + getId() + ':' + key + " closed");
          ctx.pushError(exc);
        }
      }
      // Remove the clientID (normally done in handling of CnxCloseRequest - see above)
      clientIDs.remove(key);
    }
  }

  private void doReact(CloseConnectionNot not) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "CloseConnectionNot: key=" + not.getKey());

    if (connections != null) {
      Integer key = new Integer(not.getKey());
      // The connection may have already been explicitly closed by a CnxCloseRequest.
      if (connections.containsKey(key)) {
        reactToClientRequest(not.getKey(), new CnxCloseRequest(), null);
        connections.remove(key);
        
        HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
        if (hbt != null) hbt.cancel();
      }
      // Remove the clientID (normally done in handling of CnxCloseRequest - see above)
      clientIDs.remove(key);
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
          reactToClientRequest(cc.getKey(), new CnxCloseRequest(), null);
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
        sendToClient(not.getKey(), new ServerReply(not.getRequestId()));
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
  public void sendToClient(int key, AbstractJmsReply reply) {
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
              Iterator<MessageInterceptor> it = interceptorsOUT.iterator();
              while (it.hasNext()) {
                MessageInterceptor interceptor = (MessageInterceptor) it.next();
                // interceptor handle
                if (!interceptor.handle(m, key)) {
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
            // update consumer message.
            ((ConsumerMessages) reply).setMessages(newMsgs);
          }
        }
        // push the reply
        ctx.pushReply(reply);
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
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "HeartBeatTask: closeclose connection - key=" + key);

        Channel.sendTo(userId, (Notification) new CloseConnectionNot2(key.intValue()));
        this.cancel();
      }
    }

    public void start() throws IOException {
      lastRequestDate = System.currentTimeMillis();
      try {
        AgentServer.getTimer().schedule(this, timeout / 2, timeout / 2);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "HeartBeatTask: cannot schedule task " + key, exc);
        throw new IOException(exc.getMessage());
      }
    }

    public void touch() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "HeartBeatTask: touch");
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
   * @throws Exception
   */
  public void setInterceptors(Properties prop) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " setInterceptors(" + prop + ')');

  	if (prop == null) return;

    if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_IN)) {
      if (interceptorsPropIN == null)
        interceptorsPropIN = new ArrayList<Properties>();
      if (interceptorsIN == null)
        interceptorsIN = new ArrayList<MessageInterceptor>();
      // TODO: clean prop
  	  addInterceptor(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_IN, interceptorsIN, prop, interceptorsPropIN);
    }

    if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_OUT)) {
      if (interceptorsPropOUT == null)
        interceptorsPropOUT = new ArrayList<Properties>();
      if (interceptorsOUT == null)
        interceptorsOUT = new ArrayList<MessageInterceptor>();
      // TODO: clean prop
      addInterceptor(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_OUT, interceptorsOUT, prop, interceptorsPropOUT);
    }
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
    
    if (firstTime) {
      arrivalState = new UserAgentArrivalState(ARRIVAL_STATE_PREFIX + getId().toString());
    } else {
      arrivalState = UserAgentArrivalState.load(ARRIVAL_STATE_PREFIX + getId().toString());
    }
    
    MessageTableFactory messageTableFactory = MessageTableFactory.newFactory();
    messagesTable = messageTableFactory.createMessageTable(MESSAGE_TABLE_PREFIX + getId().toString());

    if (contexts == null) contexts = new Hashtable<Integer, ClientContext>();
    if (subsTable == null) subsTable = new Hashtable();
    if (subsClientIDs == null) subsClientIDs = new Properties();

    Transaction tx = AgentServer.getTransaction();
    String[] persistedClientNames = tx.getList(ClientContext.getTransactionPrefix(getId()));
    for (int i = 0; i < persistedClientNames.length; i++) {
      try {
        ClientContext cc = (ClientContext) tx.load(persistedClientNames[i]);
        cc.txName = persistedClientNames[i];
        cc.setProxyId(getId());
        cc.setProxyAgent(this);
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
        cs.txName = persistedSubscriptionNames[i];
        cs.setProxyId(getId());
        cs.setProxyAgent(this);
        
        cs.initMessageIds();
        cs.loadMessageIds();
        
        subsTable.put(cs.getName(), cs);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "ClientSubscription named [" + persistedSubscriptionNames[i]
            + "] could not be loaded", exc);
      }
    }

    setActiveCtxId(-1);

    // Re-initializing after a crash or a server stop.

    // interceptors
    if (interceptorsPropOUT != null) {
      interceptorsOUT = new ArrayList<MessageInterceptor>();
    	InterceptorsHelper.addInterceptors(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_OUT, interceptorsPropOUT, interceptorsOUT);
    }
    if (interceptorsPropIN != null) {
      interceptorsIN = new ArrayList<MessageInterceptor>();
    	InterceptorsHelper.addInterceptors(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_IN, interceptorsPropIN, interceptorsIN);
    }

    // Browsing the pre-crash contexts:
    ClientContext activeCtx;
    AgentId destId;
    for (Iterator ctxs = contexts.values().iterator(); ctxs.hasNext();) {
      activeCtx = (ClientContext) ctxs.next();
      activeCtx.setProxyAgent(this);
      ctxs.remove();

      // Denying the non acknowledged messages:
      for (Iterator queueIds = activeCtx.getDeliveringQueues(); queueIds.hasNext();) {
        destId = (AgentId) queueIds.next();
        sendNot(destId, new DenyRequest(activeCtx.getId()));

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Denies messages on queue " + destId.toString());
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
      if (!topics.contains(destId))
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
        tSub.putSubscription((String) subEntry.getKey(), cSub.getSelector());
      }
    }
    // Browsing the topics and updating their subscriptions.
    for (Iterator topicIds = topics.iterator(); topicIds.hasNext();)
      updateSubscriptionToTopic((AgentId) topicIds.next(), -1, -1);
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
  protected void reactToClientRequest(int key, AbstractJmsRequest request, CallbackNotification callbackNotification) {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this + " got " + request.getClass().getName() + " with id: "
            + request.getRequestId() + " through activeCtx: " + key);

      if (request instanceof ProducerMessages)
        reactToClientRequest(key, (ProducerMessages) request, callbackNotification);
      else if (request instanceof ConsumerReceiveRequest)
        reactToClientRequest(key, (ConsumerReceiveRequest) request);
      else if (request instanceof ConsumerSetListRequest)
        reactToClientRequest(key, (ConsumerSetListRequest) request);
      else if (request instanceof QBrowseRequest)
        reactToClientRequest(key, (QBrowseRequest) request);
      else if (request instanceof JmsRequestGroup)
        reactToClientRequest(key, (JmsRequestGroup) request);
      else {
        doReact(key, request, callbackNotification);
      }
    } catch (IllegalArgumentException iE) {
      // Catching an exception due to an invalid agent identifier to
      // forward the request to:
      DestinationException dE = new DestinationException("Incorrect destination identifier: " + iE);
      sendToClient(key, new MomExceptionReply(request.getRequestId(), dE));
    } catch (RequestException exc) {
      sendToClient(key, new MomExceptionReply(request.getRequestId(), exc));
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
  private void reactToClientRequest(int key, ProducerMessages req, CallbackNotification callbackNotification) throws RequestException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.reactToClientRequest(" + key + ',' + req + ')');

    AgentId destId = AgentId.fromString(req.getTarget());
    if (destId == null)
      throw new RequestException("Request to an undefined destination (null).");

    // process interceptors
    ProducerMessages pm = processInterceptors(key, req);
    if (pm == null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "UserAgent.reactToClientRequest : no message to send.");
      
      if (destId.getTo() == getId().getTo() && !req.getAsyncSend() &&  !callbackNotification.hasCallback()) {
        // send producer reply
        sendNot(getId(), new SendReplyNot(key, req.getRequestId()));
      }
      return;
    }

    ClientMessages not = new ClientMessages(key, pm.getRequestId(), pm.getMessages());
    setDmq(not);

    if (destId.getTo() == getId().getTo()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> local sending");
      not.setPersistent(false);
      not.setExpiration(0L);
      if (pm.getAsyncSend()) {
        not.setAsyncSend(true);
      } else {
        callbackNotification.passCallback(not);
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> remote sending");
      if (!pm.getAsyncSend() && !callbackNotification.hasCallback()) {
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
    // Setting the producer's DMQ identifier field:
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
      doReact(key, req, null);
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

      if (destId.getTo() == getId().getTo()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> local sending");
        not.setPersistent(false);
        sendNot(destId, not);
      } else {
        sendNot(destId, not);
      }
    } else {
      doReact(key, req, null);
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
        // process interceptors
        ProducerMessages pm = processInterceptors(key, (ProducerMessages) requests[i]);
        if (pm != null)
          rm.put(key, pm);
      } else {
        reactToClientRequest(key, requests[i], null);
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
  private void doReact(int key, AbstractJmsRequest request, CallbackNotification callbackNotification) {
    try {
      // Updating the active context if the request is not a new context
      // request!
      if (!(request instanceof CnxConnectRequest))
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
        doReact(key, (CommitRequest) request, callbackNotification);
      else if (request instanceof AddClientIDRequest)
        doReact(key, (AddClientIDRequest) request);
      else if (request instanceof org.objectweb.joram.shared.client.PingRequest)
        // No need to do something, the job is done in RequestNot handling (HBT.touch)
        logger.log(BasicLevel.DEBUG, this + " - ping request");
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
    // if (! admin)
    // throw new AccessException("Request forbidden to a non administrator.");
    sendToClient(key, new GetAdminTopicReply(req, AdminTopic.getDefault().toString()));
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
    setSave();

    setActiveCtxId(key);
    activeCtx = new ClientContext(getId(), key);
    activeCtx.setProxyAgent(this);
    modifiedClient(activeCtx);
    contexts.put(new Integer(key), activeCtx);

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
   * Method implementing the JMS proxy reaction to a <code>CnxStopRequest</code>
   * requesting to stop a context.
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
      if (!DestinationConstants.isTemporary(req.getType()))
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
        // Registers the temporary destination in order to clean it at the end
        // of the connection
        activeCtx.addTemporaryDestination(destId);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "UserAgent, new destination created: " + destId);
    } else {
      destId = desc.getId();
    }

    SessCreateDestReply reply = new SessCreateDestReply(req, destId.toString());
    sendNot(getId(), new SyncReply(activeCtxId, reply));
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
    String clientId = req.getClientID();
    boolean shared = req.isShared();

    if (topicId == null)
      throw new RequestException("Cannot subscribe to an undefined topic (null).");

    if (subName == null)
      throw new RequestException("Unauthorized null subscription name.");

    boolean newTopic = !topicsTable.containsKey(topicId);
    boolean newSub = !subsTable.containsKey(subName);
    
    if (!newSub && !shared && !req.getClientID().equals(subsClientIDs.get(subName))) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "throw Exception : unshared durable subscription \"" + subName + "\" must use \"" 
            + subsClientIDs.get(subName) + "\" client identifier instead of " + clientId);
      throw new RequestException("unshared durable subscription \"" + subName + "\" must use \"" 
          + subsClientIDs.get(subName) + "\" client identifier instead of " + clientId);
    }
    
    if (clientId != null)
      subsClientIDs.put(subName, clientId);
    
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
      setSave();

      cSub = new ClientSubscription(getId(),
          activeCtxId,
          req.getRequestId(),
          req.getDurable(),
          topicId,
          req.getSubName(),
          req.getSelector(),
          req.getNoLocal(),
          dmqId,
          threshold,
          nbMaxMsg,
          messagesTable,
          clientId);

      try {
        cSub.initMessageIds();
      } catch (Exception e) {
        throw new RequestException(e.toString());
      }
      
      cSub.setProxyAgent(this);
      modifiedSubscription(cSub);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + subName + " created.");

      if (shared) {
        sharedSubs.put(subName, new SharedCtx(activeCtxId, req.getRequestId()));
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Subscription sharedSubs = " + sharedSubs);
      }
      
      subsTable.put(subName, cSub);
      
      try {
        MXWrapper.registerMBean(cSub, getSubMBeanName(subName));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "  - Could not register ClientSubscriptionMbean", e);
      }
      tSub.putSubscription(subName, req.getSelector());
      sent = updateSubscriptionToTopic(topicId, activeCtxId, req.getRequestId(), req.isAsyncSubscription());
    } else { // Existing durable subscription...
      cSub = (ClientSubscription) subsTable.get(subName);
      boolean newShared = false;
      if (shared) {
        SharedCtx sharedCtx = sharedSubs.get(subName);
        if (sharedCtx == null) {
          // the server restart, and the sharedSubs Tab is transient.
          // So set the SharedCtx.
          sharedCtx = new SharedCtx(activeCtxId, req.getRequestId());
          sharedSubs.put(subName, sharedCtx);
        }
        
        if (!sharedCtx.containsKey(activeCtxId)) {
          sharedCtx.put(activeCtxId, req.getRequestId());
          newShared = true;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Existing durable subscription add new SharedCtx : " + sharedCtx);
        }
      }
      
      if (cSub.getActive() > 0 && !newShared)
        throw new StateException("The durable subscription " + subName + " has already been activated.");

      // Updated topic: updating the subscription to the previous topic.
      boolean updatedTopic = !topicId.equals(cSub.getTopicId());
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
        updatedSelector = !req.getSelector().equals(cSub.getSelector());

      // Reactivating the subscription.
      cSub.reactivate(activeCtxId, req.getRequestId(), topicId, req.getSelector(), req.getNoLocal());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + subName + " reactivated.");

      // Updated subscription: updating subscription to topic.
      if (updatedTopic || updatedSelector) {
        tSub.putSubscription(subName, req.getSelector());
        sent = updateSubscriptionToTopic(topicId, activeCtxId, req.getRequestId(), req.isAsyncSubscription());
      }
    }
    // Activating the subscription.
    activeCtx.addSubName(subName);

    // Acknowledging the request, if needed.
    if (!sent)
      sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
    
    // Forward client subscription (should be ignored if topicId isn't an ElasticTopic)
    Channel.sendTo(topicId,new ClientSubscriptionNot(subName));
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
  private void doReact(ConsumerUnsetListRequest req) throws DestinationException {
    // If the listener was listening to a queue, cancelling any pending reply:
    if (req.getQueueMode()) {
      activeCtx.cancelReceive(req.getCancelledRequestId());
      AgentId to = AgentId.fromString(req.getTarget());
      sendNot(to, new AbortReceiveRequest(activeCtx.getId(), req.getRequestId(), req.getCancelledRequestId()));
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
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new DestinationException("Can't desactivate non existing subscription: " + subName);

    if (!sharedSubs.containsKey(subName)) {
      // De-activating the subscription:
      activeCtx.removeSubName(subName);
      sub.deactivate(false);
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ConsumerCloseSubRequest: SharedCtx remove ctxId = " + activeCtx.getId());
      SharedCtx sharedCtx = sharedSubs.get(subName);
      sharedCtx.remove(activeCtx.getId());
      activeCtx.removeSubName(subName);
      if (sharedCtx.isEmpty()) {
        sub.deactivate(false);
        sharedSubs.remove(subName);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "ConsumerCloseSubRequest: activCtx remove " + subName);
      }
    }

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
    ClientSubscription sub = null;
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);
    if (sub == null)
      throw new DestinationException("Can't unsubscribe non existing subscription: " + subName);

    if (sharedSubs.containsKey(subName)) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ConsumerUnsubRequest: SharedCtx remove ctxId = " + activeCtx.getId());
      SharedCtx sharedCtx = sharedSubs.get(subName);
      sharedCtx.remove(activeCtx.getId());
      activeCtx.removeSubName(subName);
      if (!sharedCtx.isEmpty()) {
        // Acknowledging the request:
        sendNot(getId(), new SyncReply(activeCtxId, new ServerReply(req)));
        return;
      }
      sharedSubs.remove(subName);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ConsumerUnsubRequest: sharedSubs remove " + subName);
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Deleting subscription " + subName);

    // Updating the proxy's subscription to the topic.
    AgentId topicId = sub.getTopicId();
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);
    tSub.removeSubscription(subName);
    updateSubscriptionToTopic(topicId, -1, -1);

    // Deleting the subscription.
    sub.deleteMessages();

    sub.delete();

    activeCtx.removeSubName(subName);
    subsTable.remove(subName);
    subsClientIDs.remove(subName);
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
    if (subName != null)
      sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new DestinationException("Can't request a message from the unknown subscription: " + subName);

    // Getting a message from the subscription.
    sub.setReceiver(req.getRequestId(), req.getTimeToLive());
    ConsumerMessages consM = sub.deliver();
    
    if (consM != null && req.getReceiveAck()) {
      // Immediate acknowledge
      Vector messageList = consM.getMessages();
      for (int i = 0; i < messageList.size(); i++) {
        Message msg = (Message) messageList.elementAt(i);
        sub.acknowledge(msg.getId());
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
   * Method implementing the JMS proxy reaction to a <code>SessAckRequest</code>
   * acknowledging messages either on a queue or on a subscription.
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
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null)
        sub.acknowledge(req.getIds().iterator());
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a <code>SessDenyRequest</code> denying
   * messages either on a queue or on a subscription.
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
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null)
        return;

      sub.deny(req.getIds().iterator(), req.isRedelivered());

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
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        sub.acknowledge(req.getIds().iterator());
      }
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a <code>ConsumerDenyRequest</code> denying
   * a message either on a queue or on a subscription.
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
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null)
        return;

      Vector<String> ids = new Vector<String>();
      ids.add(req.getId());
      sub.deny(ids.iterator(), req.isRedelivered());

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
      DenyRequest deny = new DenyRequest(activeCtxId, req.getRequestId(), ids);
      deny.setRedelivered(true);
      sendNot(qId, deny);
    }

    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    for (Enumeration<String> subs = req.getSubs(); subs.hasMoreElements();) {
      subName = subs.nextElement();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        sub.deny(req.getSubIds(subName).iterator(), true);

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
      while (!acks.isEmpty()) {
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
        } catch (Exception exc) {
          throw new StateException(
              "Recovered transaction branch has already been prepared by the RM.");
        }
      }
    }
    recoveredTransactions = null;
    doReply(new XACnxRecoverReply(req, bqs, fis, gtis));
  }

  // /**
  // * Method implementing the reaction to a <code>SetDMQRequest</code>
  // * instance setting the dead message queue identifier for this proxy
  // * and its subscriptions.
  // */
  // private void doReact(AgentId from, SetDMQRequest not) {
  // // state change, so save.
  // setSave();
  //
  // dmqId = not.getDmqId();
  //
  // for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();)
  // ((ClientSubscription) subsTable.get(keys.nextElement())).setDMQId(dmqId);
  //
  // sendNot(from, new AdminReplyNot(not, true, "DMQ set: " + dmqId));
  // }

  // /**
  // * Method implementing the reaction to a <code>SetThreshRequest</code>
  // * instance setting the threshold value for this proxy and its
  // * subscriptions.
  // */
  // private void doReact(AgentId from, SetThresholdRequestNot not) {
  // // state change, so save.
  // setSave();
  //
  // threshold = not.getThreshold();
  //
  // for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();)
  // ((ClientSubscription)
  // subsTable.get(keys.nextElement())).setThreshold(not.getThreshold());
  //
  // sendNot(from,
  // new AdminReplyNot(not,
  // true,
  // "Threshold set: " + threshold));
  // }

  // /**
  // * Method implementing the reaction to a <code>SetNbMaxMsgRequest</code>
  // * instance setting the NbMaxMsg value for the subscription.
  // */
  // protected void doReact(AgentId from, SetNbMaxMsgRequest not) { XXX
  // int nbMaxMsg = not.getNbMaxMsg();
  // String subName = not.getSubName();
  //
  // ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
  // if (sub != null) {
  // sub.setNbMaxMsg(nbMaxMsg);
  // sendNot(from,
  // new AdminReplyNot(not,
  // true,
  // "NbMaxMsg set: " + nbMaxMsg + " on " + subName));
  // } else {
  // sendNot(from,
  // new AdminReplyNot(not,
  // false,
  // "NbMaxMsg not set: " + nbMaxMsg + " on " + subName));
  // }
  // }

  // /**
  // * Method implementing the reaction to a <code>Monit_GetDMQSettings</code>
  // * instance requesting the DMQ settings of this proxy.
  // */
  // private void doReact(AgentId from, GetDMQSettingsRequestNot not)
  // {
  // String id = null;
  // if (dmqId != null)
  // id = dmqId.toString();
  // sendNot(from, new GetDMQSettingsReplyNot(not, id, threshold));
  // }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SyncReply</code> notification sent by itself, wrapping a reply
   * to be sent to a client.
   */
  private void doReact(SyncReply not) {
    sendToClient(not.key, not.reply);
  }

  /**
   * The method closes a given context by denying the non acknowledged messages
   * delivered to this context, and deleting its temporary subscriptions and
   * destinations.
   */
  private void doReact(int key, CnxCloseRequest req) {
    // state change, so save.
    setSave();

    // remove the clientID
    clientIDs.remove(key);
    
    // setCtx(cKey);

    // Denying the non acknowledged messages:
    AgentId id;
    boolean prepared = false;
    for (Iterator ids = activeCtx.getDeliveringQueues(); ids.hasNext();) {
      id = (AgentId) ids.next();

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
    String subName = null;
    ClientSubscription sub;
    List topics = new Vector();
    for (Iterator subs = activeCtx.getActiveSubs(); subs.hasNext();) {
      subName = (String) subs.next();
      sub = (ClientSubscription) subsTable.get(subName);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
            "Deactivate subscription " + subName + ", topic id = " + sub.getTopicId());

      if (sub.getDurable()) {
        sub.deactivate(true);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Durable subscription" + subName + " de-activated.");
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> topicsTable = " + topicsTable);

        sub.deleteMessages();

        sub.delete();

        subsTable.remove(subName);

        try {
          MXWrapper.unregisterMBean(getSubMBeanName(subName));
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e);
        }
        TopicSubscription tSub = (TopicSubscription) topicsTable.get(sub.getTopicId());
        tSub.removeSubscription(subName);

        if (!topics.contains(sub.getTopicId()))
          topics.add(sub.getTopicId());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Temporary subscription" + subName + " deleted.");
      }
    }
    // Browsing the topics which at least have one subscription removed.
    for (Iterator topicIds = topics.iterator(); topicIds.hasNext();)
      updateSubscriptionToTopic((AgentId) topicIds.next(), -1, -1);

    // Deleting the temporary destinations:
    AgentId destId;
    for (Iterator dests = activeCtx.getTempDestinations(); dests.hasNext();) {
      destId = (AgentId) dests.next();
      activeCtx.removeTemporaryDestination(destId);
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

      if (recoveredPrepare == null)
        recoveredTransactions.put(xid, prepare);
      else {
        recoveredPrepare.getSendings().addAll(prepare.getSendings());
        recoveredPrepare.getAcks().addAll(prepare.getAcks());
      }
    }

    // Finally, deleting the context:
    ClientContext cc = contexts.remove(new Integer(key));
    cc.delete();

    activeCtx = null;
    setActiveCtxId(-1);

    CnxCloseReply reply = new CnxCloseReply();
    reply.setCorrelationId(req.getRequestId());
    sendToClient(key, reply);
  }

  private void doReact(int key, ActivateConsumerRequest req) {
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    sub.setActive(req.getActivate());

    if (sub.getActive() > 0) {
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

  private void doReact(int key, CommitRequest req, CallbackNotification callbackNotification) {
    // The commit may involve some local agents
    int asyncReplyCount = 0;

    Enumeration pms = req.getProducerMessages();
    if (pms != null) {
      while (pms.hasMoreElements()) {
        // process interceptors
        ProducerMessages pm = processInterceptors(key, (ProducerMessages) pms.nextElement());
        if (pm == null)
          continue;
        AgentId destId = AgentId.fromString(pm.getTarget());
        ClientMessages not = new ClientMessages(key, 
            req.getRequestId(), pm.getMessages());
        setDmq(not);
        if (destId.getTo() == getId().getTo()) {
          // local sending
          not.setPersistent(false);
          if (req.getAsyncSend()) {
            not.setAsyncSend(true);
          } else if (callbackNotification.hasCallback()) {
            callbackNotification.passCallback(not);
          } else {
            asyncReplyCount++;
          }
        }
        sendNot(destId, not);
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
          if (qId.getTo() == getId().getTo()) {
            // local sending
            not.setPersistent(false);
            // No reply to wait for
          }

          sendNot(qId, not);
        } else {
          String subName = sar.getTarget();
          ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
          if (sub != null) {
            sub.acknowledge(sar.getIds().iterator());
            // TODO (AF): is it needed to save the proxy ?
            // if (sub.getDurable())
            // Assumes that there is nothing to save in the UserAgent.
            //setSave();
          }
        }
      }
    }

    if (!req.getAsyncSend()) {
      if (! callbackNotification.hasCallback()) {
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
      // else the callback handles the ack
    }
    // else the client doesn't expect any ack
  }

  /**
   * Method implementing the reaction to a <code>AddClientIDRequest</code>
   * instance add the clientID value of a connection.
   * 
   */
  private void doReact(int key, AddClientIDRequest req) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AddClientIDRequest  key = " + key + ", clientID = " + req.clientID);
    if (clientIDs.containsValue(req.clientID))
      throw new Exception("clientID \""+ req.clientID + "\" already active.");
    clientIDs.put(new Integer(key), req.clientID);
    
    AddClientIDReply reply = new AddClientIDReply();
    reply.setCorrelationId(req.getRequestId());
    sendToClient(key, reply);
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
        } else {
          jRep = new ConsumerMessages(rep.getCorrelationId(), (Vector) null, from.toString(), true);
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
    List<String> delaySubNames = null;
    long currentTime = System.currentTimeMillis();
    // AF: TODO we should parse each message for each subscription
    // see ClientSubscription.browseNewMessages
    List<Message> messages = new ArrayList<Message>();
    for (Iterator msgs = rep.getMessages().iterator(); msgs.hasNext();) {
      org.objectweb.joram.shared.messages.Message sharedMsg = 
          (org.objectweb.joram.shared.messages.Message) msgs.next();
      Message message = new Message(sharedMsg);
      if (sharedMsg.deliveryTime > currentTime) {
        if (delaySubNames == null) {
          delaySubNames = new ArrayList<String>();
          for (Iterator subNames = tSub.getNames(); subNames.hasNext();) {
            delaySubNames.add((String) subNames.next());
          }
        }
        scheduleDeliveryTimeMessage(from, sharedMsg, delaySubNames);
      } else {
        // Setting the arrival order of the messages
        message.order = arrivalState.getAndIncrementArrivalCount();
        messages.add(message);
      }
    }

    for (Iterator names = tSub.getNames(); names.hasNext();) {
      subName = (String) names.next();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // Browsing the delivered messages.
      sub.browseNewMessages(messages);
    }

    // Save message if it is delivered to a durable subscription.
    for (Iterator msgs = messages.iterator(); msgs.hasNext();) {
      Message message = (Message) msgs.next();

      if (message.durableAcksCounter > 0) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> save message " + message);
        // TODO (AF): The message saving does it need the proxy saving ?
        if (message.isPersistent()) { 
          arrivalState.setModified();
          
          // Persisting the message.
          setMsgTxName(message);
          message.save();
          message.releaseFullMessage();
        }
      }
    }

    for (Iterator names = tSub.getNames(); names.hasNext();) {
      subName = (String) names.next();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // If the subscription is active, launching a delivery sequence.
      if (sub.getActive() > 0) {
        ConsumerMessages consM = sub.deliver();

        if (consM != null) {
          try {
            int ctxId = sub.getContextId();
            SharedCtx sharedCtx = sharedSubs.get(subName);
            if (sharedCtx != null) {
              if(logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "Subscription "+ subName + ", sharedCtx = " + sharedCtx);
              int i = 0;
              do {
                // if shared, used the next contextId 
                Iterator<Entry<Integer, Integer>> it = sharedCtx.entrySet().iterator();
                Entry<Integer, Integer> entry = it.next();
                ClientContext ctx = (ClientContext) contexts.get(new Integer(entry.getKey()));
                if (ctx.getActivated()) {
                  ctxId = ctx.getId();
                  if(logger.isLoggable(BasicLevel.DEBUG))
                    logger.log(BasicLevel.DEBUG, "Subscription "+ subName + ", ctxId = " + ctxId);
                  sharedCtx.get(ctxId);//update LRU
                  break;
                }
                i++;
              } while (sharedCtx.size() < i);
            }
            setCtx(ctxId);
            if (activeCtx.getActivated())
              doReply(consM);
            else
              activeCtx.addPendingDelivery(consM);
          } catch (StateException pE) {
            // The context is lost: nothing to do.
          }
        }
      } else if(logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Subscription " + sub + " is not active");
    }
    
    messagesTable.checkConsumedMemory();
  }

  void scheduleDeliveryTimeMessage(AgentId from, org.objectweb.joram.shared.messages.Message msg, List<String> subNames) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.scheduleDeliveryTimeMessage(" + msg + ", " + subNames + ')');

    if (deliveryScheduler == null) {
      try {
        deliveryScheduler = new Scheduler(AgentServer.getTimer());
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "UserAgent.scheduleDeliveryTimeMessage", exc);
      }
    }
    // schedule a task
    try {
      deliveryScheduler.scheduleEvent(
          new ScheduleEvent(msg.id, 
          new Date(msg.deliveryTime)), 
          new TopicDeliveryTimeTask(getId(),
              from,
              msg, 
          subNames));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "UserAgent.scheduleDeliveryTimeMessage(" + msg + ')', e);
    }
  }

  private void doReact(TopicDeliveryTimeNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + not + ')');

    // Browsing the target subscriptions:
    TopicSubscription tSub = (TopicSubscription) topicsTable.get(not.topic);
    if (tSub == null || tSub.isEmpty()) return;

    Message momMsg = new Message(not.msg);
    List<Message> messages = new ArrayList<Message>();
    messages.add(momMsg);

    String subName;
    ClientSubscription sub;
    for (Iterator names = not.subNames.iterator(); names.hasNext();) {
      subName = (String) names.next();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // Browsing the delivered messages.
      sub.browseNewMessages(messages);
    }

    // Save message if it is delivered to a durable subscription.
    Message message = momMsg;
    if (message.durableAcksCounter > 0) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> save message " + message);
      // TODO (AF): The message saving does it need the proxy saving ?
      if (message.isPersistent()) {
        setSave();
        // Persisting the message.
        setMsgTxName(message);
        message.save();
        message.releaseFullMessage();
      }
    }

    for (Iterator names = not.subNames.iterator(); names.hasNext();) {
      subName = (String) names.next();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub == null) continue;

      // If the subscription is active, launching a delivery sequence.
      if (sub.getActive() > 0) {
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
          sub = (ClientSubscription) subsTable.remove(name);
          try {
            MXWrapper.unregisterMBean(getSubMBeanName(name));
          } catch (Exception e1) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "  - Problem when unregistering ClientSubscriptionMbean", e1);
          }
          sub.deleteMessages();

          sub.delete();

          try {
            setCtx(sub.getContextId());
            activeCtx.removeSubName(name);
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

        try {
          setCtx(sub.getContextId());
          activeCtx.removeSubName(name);
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

      if (((SetDMQRequest) adminRequest).getDmqId() != null)
        dmqId = AgentId.fromString(((SetDMQRequest) adminRequest).getDmqId());
      else
        dmqId = null;

      for (Iterator subs = subsTable.values().iterator(); subs.hasNext();)
        ((ClientSubscription) subs.next()).setDMQId(dmqId);

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

        // add interceptors out
        if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_OUT)) {
          if (interceptorsPropOUT == null)
            interceptorsPropOUT = new ArrayList<Properties>();
          if (interceptorsOUT == null)
            interceptorsOUT = new ArrayList<MessageInterceptor>();
          addInterceptor(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_OUT, interceptorsOUT, prop, interceptorsPropOUT);
        }
        // add interceptors in
        if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_IN)) {
          if (interceptorsPropIN == null)
            interceptorsPropIN = new ArrayList<Properties>();
          if (interceptorsIN == null)
            interceptorsIN = new ArrayList<MessageInterceptor>();
          addInterceptor(getAgentId(), getName(), AdminCommandConstant.INTERCEPTORS_IN, interceptorsIN, prop, interceptorsPropIN);
        }
        break;

      case AdminCommandConstant.CMD_REMOVE_INTERCEPTORS:
        prop = request.getProp();
				removeInterceptor(AdminCommandConstant.INTERCEPTORS_OUT, interceptorsOUT, prop.getProperty(AdminCommandConstant.INTERCEPTORS_OUT), interceptorsPropOUT);
				removeInterceptor(AdminCommandConstant.INTERCEPTORS_IN, interceptorsIN, prop.getProperty(AdminCommandConstant.INTERCEPTORS_IN), interceptorsPropIN);
        break;

      case AdminCommandConstant.CMD_GET_INTERCEPTORS:
        replyProp = new Properties();
        if (interceptorsIN == null) {
          replyProp.put(AdminCommandConstant.INTERCEPTORS_IN, "");
        } else {
				  replyProp.put(AdminCommandConstant.INTERCEPTORS_IN, InterceptorsHelper.getListInterceptors(interceptorsIN));
        }
        if (interceptorsOUT == null) {
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

        if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_IN_OLD)) {
          // replace IN interceptor
				  replaceInterceptorIN(
				      getAgentId(),
				      getName(),
				      prop);
        }

        if (prop.containsKey(AdminCommandConstant.INTERCEPTORS_OUT_OLD)) {
          // replace OUT interceptor
				  replaceInterceptorOUT(
				      getAgentId(),
				      getName(),
				      prop);
        }
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

  private void doReact(GetSubscriptionMessageIds request, AgentId replyTo,
      String requestMsgId, String replyMsgId) {
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
      cs = (ClientSubscription) subsTable.get(subName);
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
    
    if(logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG,"Contexts:");
      for(Integer k : contexts.keySet()) {
        logger.log(BasicLevel.DEBUG,k+" : "+contexts.get(k));
      }
    }

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
    sendToClient(activeCtxId, reply);
  }

  protected ClientContext getClientContext(int ctxId) {
    return (ClientContext) contexts.get(new Integer(ctxId));
  }

  protected void cleanPendingMessages(long currentTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.cleanPendingMessages(" + messagesTable.size() + ')');

    Message message = null;
    DMQManager dmqManager = null;

    if (dmqManager == null)
      dmqManager = new DMQManager(dmqId, null);
    
    // The table cleaning is delegated to the table itself
    messagesTable.clean(currentTime, dmqManager);

    // Now each ClientSubscription is cleaned in a lazy way: the identifier
    // of an invalid message is removed when the message is required by the
    // ClientSubscription (see method 'deliver').
    /*
    Iterator subs = subsTable.values().iterator();
    while (subs.hasNext()) {
      ((ClientSubscription) subs.next()).cleanMessageIds();
    }*/

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
      updateSubscriptionToTopic(destId, -1, -1);
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
  private boolean updateSubscriptionToTopic(AgentId topicId, int contextId, int requestId, boolean asyncSub) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.updateSubscriptionToTopic(" + topicId + ',' + contextId + ','
          + requestId + ',' + asyncSub + ')');

    TopicSubscription tSub = (TopicSubscription) topicsTable.get(topicId);

    // No more subs to this topic: unsubscribing.
    if (tSub == null || tSub.isEmpty()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> topicsTable.remove(" + topicId + ')');
      topicsTable.remove(topicId);
      sendNot(topicId, new UnsubscribeRequest(contextId, requestId));
      return false;
    }

    // Otherwise, updating the subscription if the selector evolved.
    String builtSelector = tSub.buildSelector();
    if (tSub.getLastSelector() != null
        && builtSelector.equals(tSub.getLastSelector()))
      return false;

    tSub.setLastSelector(builtSelector);
    SubscribeRequest req = new SubscribeRequest(contextId, requestId, builtSelector, asyncSub);
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

  private ProducerMessages processInterceptors(int key, ProducerMessages pm) {
    if (interceptorsIN != null && !interceptorsIN.isEmpty()) {
      org.objectweb.joram.shared.messages.Message m = null;
      Vector msgs = ((ProducerMessages) pm).getMessages();
      Vector newMsgs = new Vector();
      for (int i = 0; i < msgs.size(); i++) {
        m = (org.objectweb.joram.shared.messages.Message) msgs.elementAt(i);
        Iterator<MessageInterceptor> it = interceptorsIN.iterator();
        while (it.hasNext()) {
          MessageInterceptor interceptor = it.next();
          if (!interceptor.handle(m, key)) {
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
          logger.log(BasicLevel.DEBUG, "UserAgent.processInterceptors : no message to send.");
        return null;
      }
      // update producer message.
      ((ProducerMessages) pm).setMessages(newMsgs);
    }
    return pm;
  }

  private void addInterceptor(
      String agentId, 
      String agentName,
      String interceptorsKey,
      List<MessageInterceptor> interceptors, 
      final Properties prop, 
      List<Properties> interceptorsProp) throws Exception {

    String error = null;
    String interceptorsClassName = prop.getProperty(interceptorsKey);
    if (interceptorsClassName == null) return;

    if (interceptorsClassName.contains(InterceptorsHelper.INTERCEPTOR_CLASS_NAME_SEPARATOR)) {
      StringTokenizer token = new StringTokenizer(interceptorsClassName, InterceptorsHelper.INTERCEPTOR_CLASS_NAME_SEPARATOR);
      while (token.hasMoreTokens()) {
        String interceptorClassName = token.nextToken();
        Properties iProp = new Properties();
        iProp.setProperty(interceptorsKey, interceptorClassName);
        interceptorsProp.add(iProp);
        try {
          InterceptorsHelper.addInterceptors(agentId, agentName, interceptorsKey, iProp, interceptors);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "addInterceptors", exc);
          StringWriter sw = new StringWriter();
          exc.printStackTrace(new PrintWriter(sw));
          if (error == null)
            error = "(" + interceptorClassName + " exc=" + sw.toString() + ')';
          else
            error += "(" + interceptorClassName + " exc=" + sw.toString() + ')';
        }
      }
    } else {
      interceptorsProp.add(prop);
      InterceptorsHelper.addInterceptors(agentId, agentName, interceptorsKey, prop, interceptors);
    }

    // state change
    setSave();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.addInterceptor interceptors = " + interceptors + ", interceptorsProp = " + interceptorsProp);

    if (error != null) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "UserAgent.addInterceptor error = " + error);
      throw new Exception(error);
    }
  }

  private void removeInterceptor(
      String interceptorsKey,
      List<MessageInterceptor> interceptors, 
      String classNames, 
      List<Properties> interceptorsProp) throws Exception {

    if (classNames == null || interceptors == null || interceptorsProp == null) return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.removeInterceptor classNames = " + classNames + ", interceptors = " + interceptors + ", interceptorsProp = " + interceptorsProp);

    StringTokenizer token = new StringTokenizer(classNames, InterceptorsHelper.INTERCEPTOR_CLASS_NAME_SEPARATOR);
    while (token.hasMoreTokens()) {
      String className = token.nextToken();
      // find and remove the interceptor
      Properties toRemove = null;
      Iterator<Properties> it = interceptorsProp.iterator();
      while (it.hasNext()) {
        Properties properties = (Properties) it.next();
        if (properties.getProperty(interceptorsKey).equals(className)) {
          toRemove = properties;
          break;
        }
      }
      if (toRemove != null) {
        interceptorsProp.remove(toRemove);

        InterceptorsHelper.removeInterceptors(classNames, interceptors);
        if (interceptors.isEmpty())
          interceptors = null;
        // state change
        setSave();
      }
    }
  }

  private void replaceInterceptorIN(
      String agentId, 
      String agentName,
      final Properties prop) throws Exception {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.replaceInterceptorIN(" + agentId + ", " + agentName + ", " + prop + ')');

    Boolean ret = InterceptorsHelper.replaceInterceptor(
        agentId, 
        agentName, 
        AdminCommandConstant.INTERCEPTORS_IN_NEW,
        AdminCommandConstant.INTERCEPTORS_IN_OLD, 
        interceptorsIN, 
        prop);

    if (ret) {
      Iterator<Properties> it = interceptorsPropIN.iterator();
      while (it.hasNext()) {
        Properties properties = (Properties) it.next();
        if (properties.getProperty(AdminCommandConstant.INTERCEPTORS_IN).equals(prop.getProperty(AdminCommandConstant.INTERCEPTORS_IN_OLD))) {
          int index = interceptorsPropIN.indexOf(properties);
          Properties newProp = new Properties();
          Set<Entry<Object, Object>> entrys = prop.entrySet();
          Iterator<Entry<Object, Object>> iterator = entrys.iterator();
          while (iterator.hasNext()) {
            Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();
            if (entry.getKey().equals(AdminCommandConstant.INTERCEPTORS_IN_NEW))
              newProp.put(AdminCommandConstant.INTERCEPTORS_IN, entry.getValue());
            else
              newProp.put(entry.getKey(), entry.getValue());
          }
          interceptorsPropIN.remove(index);
          interceptorsPropIN.add(index, newProp);
          // state change
          setSave();
          break;
        }
      }
    }
  }

  private void replaceInterceptorOUT(
      String agentId, 
      String agentName,
      final Properties prop) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.replaceInterceptorOUT(" + agentId + ", " + agentName + ", " + prop + ')');

    Boolean ret = InterceptorsHelper.replaceInterceptor(
        agentId, 
        agentName, 
        AdminCommandConstant.INTERCEPTORS_OUT_NEW,
        AdminCommandConstant.INTERCEPTORS_OUT_OLD, 
        interceptorsOUT, 
        prop);

    if (ret) {
      // update the interceptorsPropOUT list
      Iterator<Properties> it = interceptorsPropOUT.iterator();
      while (it.hasNext()) {
        Properties properties = (Properties) it.next();
        if (properties.getProperty(AdminCommandConstant.INTERCEPTORS_OUT).equals(prop.getProperty(AdminCommandConstant.INTERCEPTORS_OUT_OLD))) {
          int index = interceptorsPropOUT.indexOf(properties);
          Properties newProp = new Properties();
          Set<Entry<Object, Object>> entrys = prop.entrySet();
          Iterator<Entry<Object, Object>> iterator = entrys.iterator();
          while (iterator.hasNext()) {
            Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();
            if (entry.getKey().equals(AdminCommandConstant.INTERCEPTORS_OUT_NEW))
              newProp.put(AdminCommandConstant.INTERCEPTORS_OUT, entry.getValue());
            else
              newProp.put(entry.getKey(), entry.getValue());
          }
          interceptorsPropOUT.remove(index);
          interceptorsPropOUT.add(index, newProp);
          // state change
          setSave();
          break;
        }
      }
    }
  }

  public void modifiedClient(ClientContext cc) {
    if (!modifiedClientContexts.contains(cc)) {
      modifiedClientContexts.add(cc);
    }
  }

  public void modifiedSubscription(ClientSubscription cs) {
    if (!modifiedClientSubscriptions.contains(cs)) {
      modifiedClientSubscriptions.add(cs);
    }
  }

  private void saveModifiedClientContexts() {
    if (modifiedClientContexts.size() > 0) {
      for (ClientContext modifiedCC : modifiedClientContexts) {
        if (modifiedCC.modified) {
          modifiedCC.save();
          modifiedCC.modified = false;
        }
      }
      modifiedClientContexts.clear();
    }
  }

  private void saveModifiedClientSubscriptions() {
    if (modifiedClientSubscriptions.size() > 0) {
      for (ClientSubscription modifiedCS : modifiedClientSubscriptions) {
        if (modifiedCS.modified) {
          modifiedCS.save();
          modifiedCS.modified = false;
        }
      }
      modifiedClientSubscriptions.clear();
    }
  }

  @Override
  public int getEncodableClassId() {
    return JoramHelper.USER_AGENT_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int res = super.getEncodedSize();

    res += BOOLEAN_ENCODED_SIZE;
    if (dmqId != null) {
      res += dmqId.getEncodedSize();
    }

    res += BOOLEAN_ENCODED_SIZE;
    if (interceptorsPropIN != null) {
      res += INT_ENCODED_SIZE;
      for (Properties properties : interceptorsPropIN) {
        res += EncodableHelper.getEncodedSize(properties);
      }
    }

    res += BOOLEAN_ENCODED_SIZE;
    if (interceptorsPropOUT != null) {
      res += INT_ENCODED_SIZE;
      for (Properties properties : interceptorsPropOUT) {
        res += EncodableHelper.getEncodedSize(properties);
      }
    }

    res += 2 * INT_ENCODED_SIZE + 2 * LONG_ENCODED_SIZE;

    res += BOOLEAN_ENCODED_SIZE;
    if (recoveredTransactions != null) {
      res += INT_ENCODED_SIZE;
      Iterator<Entry<Xid, XACnxPrepare>> recoveredTransactionsIterator = recoveredTransactions
          .entrySet().iterator();
      while (recoveredTransactionsIterator.hasNext()) {
        Entry<Xid, XACnxPrepare> context = recoveredTransactionsIterator.next();
        // Not useful to encode the key as it is in the value
        // context.getKey().encode(encoder);
        res += context.getValue().getEncodedSize();
      }
    }

    res += INT_ENCODED_SIZE;
    res += EncodableHelper.getEncodedSize(subsClientIDs);
    return res;
  }

  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    
    if (dmqId == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      dmqId.encode(encoder);
    }

    if (interceptorsPropIN == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeUnsignedInt(interceptorsPropIN.size());
      for (Properties properties : interceptorsPropIN) {
        EncodableHelper.encodeProperties(properties, encoder);
      }
    }

    if (interceptorsPropOUT == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeUnsignedInt(interceptorsPropOUT.size());
      for (Properties properties : interceptorsPropOUT) {
        EncodableHelper.encodeProperties(properties, encoder);
      }
    }

    encoder.encodeUnsignedInt(keyCounter);
    encoder.encodeUnsignedInt(nbMaxMsg);
    encoder.encodeUnsignedLong(nbMsgsSentToDMQSinceCreation);
    encoder.encodeUnsignedLong(period);

    if (recoveredTransactions == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeUnsignedInt(recoveredTransactions.size());
      Iterator<Entry<Xid, XACnxPrepare>> recoveredTransactionsIterator = recoveredTransactions
          .entrySet().iterator();
      while (recoveredTransactionsIterator.hasNext()) {
        Entry<Xid, XACnxPrepare> context = recoveredTransactionsIterator.next();
        // Not useful to encode the key as it is in the value
        // context.getKey().encode(encoder);
        context.getValue().encode(encoder);
      }
    }

    encoder.encodeUnsignedInt(threshold);
    EncodableHelper.encodeProperties(subsClientIDs, encoder);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    
    boolean isNull = decoder.decodeBoolean();
    if (isNull) {
      dmqId = null;
    } else {
      dmqId = new AgentId((short) 0, (short) 0, 0);
      dmqId.decode(decoder);
    }

    isNull = decoder.decodeBoolean();
    if (isNull) {
      interceptorsPropIN = null;
    } else {
      int size = decoder.decodeUnsignedInt();
      interceptorsPropIN = new Vector<Properties>(size);
      for (int i = 0; i < size; i++) {
        Properties properties = EncodableHelper.decodeProperties(decoder);
        interceptorsPropIN.add(properties);
      }
    }

    isNull = decoder.decodeBoolean();
    if (isNull) {
      interceptorsPropOUT = null;
    } else {
      int size = decoder.decodeUnsignedInt();
      interceptorsPropOUT = new Vector<Properties>(size);
      for (int i = 0; i < size; i++) {
        Properties properties = EncodableHelper.decodeProperties(decoder);
        interceptorsPropOUT.add(properties);
      }
    }

    keyCounter = decoder.decodeUnsignedInt();
    nbMaxMsg = decoder.decodeUnsignedInt();
    nbMsgsSentToDMQSinceCreation = decoder.decodeUnsignedLong();
    period = decoder.decodeUnsignedLong();

    isNull = decoder.decodeBoolean();
    if (isNull) {
      recoveredTransactions = null;
    } else {
      int size = decoder.decodeUnsignedInt();
      recoveredTransactions = new Hashtable<Xid, XACnxPrepare>(size);
      for (int i = 0; i < size; i++) {
        XACnxPrepare ctx = new XACnxPrepare();
        ctx.decode(decoder);
        Xid xid = new Xid(ctx.getBQ(), ctx.getFI(), ctx.getGTI());
        recoveredTransactions.put(xid, ctx);
      }
    }

    threshold = decoder.decodeUnsignedInt();
    subsClientIDs= EncodableHelper.decodeProperties(decoder);
  }

  public static class UserAgentFactory implements EncodableFactory {

    public Encodable createEncodable() {
      // These are just initial values to be changed when decoding the agent
      return new UserAgent(null, false, AgentId.MinWKSIdStamp);
    }

  }

}

/**
 * The <code>Xid</code> internal class is a utility class representing
 * a global transaction identifier.
 */
class Xid implements Serializable, Encodable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  byte[] bq;
  int fi;
  byte[] gti;

  Xid() {}

  Xid(byte[] bq, int fi, byte[] gti) {
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Xid))
      return false;

    Xid other = (Xid) o;

    return java.util.Arrays.equals(bq, other.bq)
           && fi == other.fi
        && java.util.Arrays.equals(gti, other.gti);
  }

  public int hashCode() {
    return (new String(bq) + "-" + new String(gti)).hashCode();
  }

  public int getEncodableClassId() {
    // Not defined
    return -1;
  }

  public int getEncodedSize() throws Exception {
    return bq.length + INT_ENCODED_SIZE + gti.length;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encodeByteArray(bq);
    encoder.encodeUnsignedInt(fi);
    encoder.encodeByteArray(gti);
  }

  public void decode(Decoder decoder) throws Exception {
    bq = decoder.decodeByteArray();
    fi = decoder.decodeUnsignedInt();
    gti = decoder.decodeByteArray();
  }
}
