/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ClusterJoinAck;
import org.objectweb.joram.mom.notifications.ClusterJoinNot;
import org.objectweb.joram.mom.notifications.ClusterRemoveNot;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.SubscribeReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.TopicForwardNot;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ClusterAdd;
import org.objectweb.joram.shared.admin.ClusterLeave;
import org.objectweb.joram.shared.admin.ClusterList;
import org.objectweb.joram.shared.admin.ClusterListReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetFatherReply;
import org.objectweb.joram.shared.admin.GetFatherRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetSubscriberIds;
import org.objectweb.joram.shared.admin.GetSubscriberIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionsRequest;
import org.objectweb.joram.shared.admin.SetFather;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.common.Debug;

/**
 * The <code>Topic</code> class implements the MOM topic behavior,
 * basically distributing the received messages to subscribers.
 * <p>
 * A Topic might be part of a hierarchy; if it is the case, and if the topic is
 * not on top of that hierarchy, it will have a father to forward messages to.
 * <p>
 * A topic might also be part of a cluster; if it is the case, it will have
 * friends to forward messages to.
 * <p>
 * A topic can be part of a hierarchy and of a cluster at the same time.
 */
public class Topic extends Destination implements TopicMBean {

  public static Logger logger = Debug.getLogger(Topic.class.getName());

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Identifier of this topic's father, if any. */
  protected AgentId fatherId = null;

  /** Set of cluster elements (including itself), if any. */
  protected Set friends = null;

  /** Vector of subscribers' identifiers. */
  protected List subscribers = new Vector();
  
  // JORAM_PERF_BRANCH
  private Map durableSubscriptions = new HashMap<AgentId, Boolean>();

  /** Table of subscribers' selectors. */
  protected Map selectors = new Hashtable();

  /** Internal boolean used for tagging local sendings. */
  protected transient boolean alreadySentLocally;

  public Topic() {
  }

  public Topic(String string, boolean b, int joramAdminStamp) {
    super(string, b, joramAdminStamp);
  }

  public final byte getType() {
    return DestinationConstants.TOPIC_TYPE;
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    setAlreadySentLocally(false);
    int reqId = -1;
    if (not instanceof AbstractRequestNot)
      reqId = ((AbstractRequestNot) not).getRequestId();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": got " + not.getClass().getName() + " with id: " + reqId
          + " from: " + from.toString());
    try {
      if (not instanceof ClusterJoinAck)
        clusterJoinAck((ClusterJoinAck) not);
      else if (not instanceof ClusterJoinNot)
        clusterJoin((ClusterJoinNot) not);
      else if (not instanceof ClusterRemoveNot)
        clusterRemove(from);
      else if (not instanceof SubscribeRequest)
        subscribeRequest(from, (SubscribeRequest) not);
      else if (not instanceof UnsubscribeRequest)
        unsubscribeRequest(from);
      else if (not instanceof TopicForwardNot)
        topicForwardNot(from, (TopicForwardNot) not);
      else
        super.react(from, not);
    } catch (MomException exc) {
      // MOM exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      AbstractRequestNot req = (AbstractRequestNot) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    }
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime		true when first called by the factory
   */
  public void initialize(boolean firstTime) {}

  /**
   * Finalizes the destination before it is garbaged.
   * 
   * @param lastime true if the destination is deleted
   */
  protected void finalize(boolean lastTime) {}

  /**
   * Returns a string representation of this destination.
   */
  public String toString() {
    return "Topic:" + getId().toString();
  }

  public void wakeUpNot(WakeUpNot not) {
    // nothing to do
  }

  /**
   * Reaction to the request of adding a new cluster element.
   */
  private void clusterAdd(FwdAdminRequestNot req, String joiningTopic) {
    AgentId newFriendId = AgentId.fromString(joiningTopic);

    // Adds the given topic to the cluster containing the current one.
    if (friends == null) {
      friends = new HashSet();
      friends.add(getId());
    }
    forward(newFriendId,
        new ClusterJoinNot(friends, req.getReplyTo(), req.getRequestMsgId(), req.getReplyMsgId()));
  }

  /**
   * Method implementing the reaction to a {@link ClusterJoinNot} notification,
   * sent by a fellow topic for notifying this topic to join the cluster, doing
   * a transitive closure of clusters, if any.
   */
  protected void clusterJoin(ClusterJoinNot not) {
    setSave(); // state change, so save.
    if (friends == null) {
      friends = new HashSet();
      friends.add(getId());
    }
    friends.addAll(not.getCluster());
    sendToCluster(new ClusterJoinAck(friends));
    replyToTopic(new AdminReply(true, null), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
  }

  /**
   * Method implementing the reaction to a {@link ClusterJoinAck} notification,
   * doing a transitive closure with the current cluster and the one of the new
   * cluster element.
   */
  protected void clusterJoinAck(ClusterJoinAck not) {
    friends.addAll(not.getCluster());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " Topic.clusterJoinAck(" + not + ")" + "\nfriends="
          + friends);
  }

  /**
   * Returns the cluster list.
   * 
   * @return the cluster list.
   */
  private List clusterList() {
    List cluster = new ArrayList();
    if (friends != null) {
      Iterator iterator = friends.iterator();
      while (iterator.hasNext()) {
        cluster.add(iterator.next().toString());
      }
    } else {
      cluster.add(getAgentId());
    }
    return cluster;
  }

  public String[] getClusterElements() {
    List list = clusterList();
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * Ask this topic to leave the cluster.
   */
  private void clusterLeave() {
    // Removes this topic of its cluster
    if (friends != null) {
      // Sends a notification to all members asking to remove the topic
      sendToCluster(new ClusterRemoveNot());
      friends = null;
      setSave(); // state change, so save.
    }
  }

  /**
   * Remove the specified topic from current cluster.
   * 
   * @param topic The topic which left the cluster
   */
  private void clusterRemove(AgentId topic) {
    setSave(); // state change, so save.
    friends.remove(topic);
    if (friends.size() == 1)
      friends = null;
  }

  /**
   * Sends a notification to all topics in cluster.
   * 
   * @param not The notification to send.
   */
  protected void sendToCluster(Notification not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " Topic.sendToCluster(" + not + ")");

    if (friends == null || friends.size() < 2)
      return;

    for (Iterator e = friends.iterator(); e.hasNext();) {
      AgentId id = (AgentId) e.next();
      if (!id.equals(getId()))
        forward(id, not);
    }
  }

  /**
   * This method is currently needed by the JMSBridge.
   * AF(TODO): Remove it.
   * @deprecated
   */
  public void preSubscribe() {}
  /**
   * This method is currently needed by the JMSBridge.
   * AF(TODO): Remove it.
   * @deprecated
   */
  public void postSubscribe() {}

  /**
   * Method implementing the reaction to a <code>SubscribeRequest</code>
   * instance. 
   *
   * @exception AccessException  If the sender is not a READER.
   */
  protected void subscribeRequest(AgentId from, SubscribeRequest not) throws AccessException {
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    preSubscribe();

    setSave(); // state change, so save.
    
    // Adding new subscriber.
    if (! subscribers.contains(from)) {
      subscribers.add(from);
    }
    
    // JORAM_PERF_BRANCH
    if (not.isDurable()) {
      durableSubscriptions.put(from, Boolean.TRUE);
    } else {
      durableSubscriptions.remove(from);
    }

    // The requester might either be a new subscriber, or an existing one;
    // setting the selector, possibly by removing or modifying an already set
    // expression.
    if (not.getSelector() != null && ! not.getSelector().equals(""))
      selectors.put(from, not.getSelector());
    else
      selectors.remove(from);

    if (!not.isAsyncSub())
      forward(from, new SubscribeReply(not));

    postSubscribe();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Client " + from + " set as a subscriber with selector " + not.getSelector());
  }

  /**
   * This method is currently needed by the JMSBridge.
   * AF(TODO): Remove it.
   * @deprecated
   */
  public void preUnsubscribe() {}
  /**
   * This method is currently needed by the JMSBridge.
   * AF(TODO): Remove it.
   * @deprecated
   */
  public void postUnsubscribe() {}

  /**
   * Method implementing the reaction to an <code>UnsubscribeRequest</code>
   * instance, requesting to remove a subscriber.
   */
  protected void unsubscribeRequest(AgentId from) {
    preUnsubscribe();

    setSave(); // state change, so save.
    subscribers.remove(from);
    selectors.remove(from);

    postUnsubscribe();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "Client " + from + " removed from the subscribers.");
  }

  /**
   * Method implementing the reaction to a <code>TopicForwardNot</code>
   * instance, carrying messages forwarded by a cluster fellow or a
   * hierarchical son.
   */
  protected void topicForwardNot(AgentId from, TopicForwardNot not) {
    doClientMessages(from, not.messages, not.fromCluster, false);
  }

  /**
   * 
   * @see org.objectweb.joram.mom.dest.Destination#handleAdminRequestNot(fr.dyade.aaa.agent.AgentId, org.objectweb.joram.mom.notifications.FwdAdminRequestNot)
   */
  public void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {
    AdminRequest adminRequest = not.getRequest();
    
    if (adminRequest instanceof GetSubscriberIds) {
      replyToTopic(new GetSubscriberIdsRep(getSubscriberIds()),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetSubscriptionsRequest) {
      replyToTopic(new GetNumberReply(getNumberOfSubscribers()),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetDMQSettingsRequest) {
      replyToTopic(new GetDMQSettingsReply((dmqId != null)?dmqId.toString():null, 0),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof SetFather) {
      setSave(); // state change, so save.
      this.fatherId = AgentId.fromString(((SetFather) adminRequest).getFather());
      replyToTopic(new AdminReply(true, null),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetFatherRequest) {
      replyToTopic(new GetFatherReply((fatherId != null)?fatherId.toString():null),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof ClusterList) {
      List clstr = clusterList();
      replyToTopic(new ClusterListReply(clstr), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof ClusterAdd) {
      clusterAdd(not, ((ClusterAdd) adminRequest).getAddedDest());
    } else if (adminRequest instanceof ClusterLeave) {
      clusterLeave();
      replyToTopic(new AdminReply(true, null), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else {
      super.handleAdminRequestNot(from, not);
    }
  }

  /**
   * Returns the number of subscribers.
   * Each user appears once even if there is multiples subscriptions, the different
   * subscriptions can be enumerate through the proxy MBean.
   * 
   * @return the number of subscribers.
   */
  public final int getNumberOfSubscribers() {
    return subscribers.size();
  }

  /**
   * Returns the list of unique identifiers of all subscribers. Each user
   * appears once even if there is multiples subscriptions, the different
   * subscriptions can be enumerate through the proxy MBean.
   *
   * @return the list of unique identifiers of all subscribers.
   */
  public final String[] getSubscriberIds() {
    String[] res = new String[subscribers.size()];
    for (int i = 0; i < res.length; i++) {
      AgentId aid = (AgentId) subscribers.get(i);
      res[i] = aid.toString();
    }
    return res;
  }

  /**
   * Method specifically processing a <code>SetRightRequest</code> instance.
   * <p>
   * When a reader is removed, deleting this reader's subscription if any,
   * and sending an <code>ExceptionReply</code> notification to the client.
   */
  protected void doRightRequest(AgentId user, int right) {
    // If the request does not unset a reader, doing nothing.
    if (right != -READ) return;

    if (user != null) {
      // Identified user: removing it.
      setSave(); // state change, so save.
      subscribers.remove(user);
      selectors.remove(user);
      forward(user, new ExceptionReply(new AccessException("READ right removed.")));
    } else {
      // Free reading right removed: removing all non readers.
      for (Iterator subs = subscribers.iterator(); subs.hasNext();) {
        user = (AgentId) subs.next();
        if (! isReader(user)) {
          setSave(); // state change, so save.
          subs.remove();
          selectors.remove(user);
          forward(user, new ExceptionReply(new AccessException("READ right removed.")));
        }
      }
    }
  }

  /**
   * Method specifically processing a <code>ClientMessages</code> instance.
   * <p>
   * This method may forward the messages to the topic father if any, or
   * to the cluster fellows if any.It may finally send
   * <code>TopicMsgsReply</code> instances to the valid subscribers.
   */
  protected void doClientMessages(AgentId from, ClientMessages not, boolean throwsExceptionOnFullDest) {
    doClientMessages(from, not, false, throwsExceptionOnFullDest);
  }

  private void doClientMessages(AgentId from, ClientMessages not, boolean fromCluster, boolean throwsExceptionOnFullDest) {
    ClientMessages clientMsgs = preProcess(from, not);
    if (clientMsgs != null) {
      // Forwarding the messages to the father or the cluster fellows, if any:
      forwardMessages(clientMsgs, fromCluster);

      // Processing the messages:
      processMessages(clientMsgs);

      postProcess(clientMsgs);
    }
  }

  /**
   * Method specifically processing an <code>UnknownAgent</code> instance.
   * <p>
   * This method notifies the administrator of the failing cluster or
   * hierarchy building request, if needed, or removes the subscriptions of
   * the deleted client, if any, or sets the father identifier to null if it
   * comes from a deleted father.
   */
  protected void doUnknownAgent(UnknownAgent uA) {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    if (not instanceof ClusterJoinNot) {
      ClusterJoinNot cT = (ClusterJoinNot) not;
      logger.log(BasicLevel.ERROR, "Cluster join failed: " + uA.agent + " unknown.");
      String info = "Cluster join failed: Unknown destination.";
      replyToTopic(new AdminReply(AdminReply.BAD_CLUSTER_REQUEST, info), cT.getReplyTo(),
          cT.getRequestMsgId(), cT.getReplyMsgId());
    } else if (not instanceof ClusterJoinAck || not instanceof ClusterRemoveNot) {
      logger.log(BasicLevel.ERROR, "Cluster error: " + uA.agent + " unknown. "
          + "The topic has probably been removed in the meantime.");
      clusterRemove(agId);
    } else {
      setSave(); // state change, so save.
      // Removing the deleted client's subscriptions, if any.
      subscribers.remove(agId);
      selectors.remove(agId);

      // Removing the father identifier, if needed.
      if (agId.equals(fatherId))
        fatherId = null;
    }
  }

  /**
   * Method specifically processing a <code>fr.dyade.aaa.agent.DeleteNot</code>
   * instance.
   * <p>
   * <code>UnknownAgent</code> notifications are sent to each subscriber and
   * <code>UnclusterNot</code> notifications to the cluster fellows.
   */
  protected void doDeleteNot(DeleteNot not) {
    AgentId clientId;

    // For each subscriber...
    for (int i = 0; i < subscribers.size(); i++) {
      clientId = (AgentId) subscribers.get(i);
      forward(clientId, new UnknownAgent(getId(), null));
    }

    clusterLeave();
    setSave(); // state change, so save.
  }

  /**
   * Actually forwards a list of messages to the father or the cluster
   * fellows, if any.
   */
  protected void forwardMessages(ClientMessages messages) {
    forwardMessages(messages, false);
  }

  private void forwardMessages(ClientMessages messages, boolean fromCluster) {
    if (!fromCluster) {
      sendToCluster(new TopicForwardNot(messages, true));
    }
    if (fatherId != null) {
      forward(fatherId, new TopicForwardNot(messages, false));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Messages forwarded to father " + fatherId.toString());
    }
  }

  /**
   * Actually processes the distribution of the received messages to the
   * valid subscriptions by sending a <code>TopicMsgsReply</code> notification
   * to the valid subscribers.
   */
  protected void processMessages(ClientMessages not) {
    List messages = not.getMessages();
    AgentId subscriber;
    boolean local;
    String selector;
    List deliverables;
    Message message;

    nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation + messages.size();
    
    // interceptors process
    if (interceptorsAvailable()) {
    	DMQManager dmqManager = null;
    	List newMessages = new ArrayList();
    	Iterator it = messages.iterator();
    	while (it.hasNext()) {
    		Message m = (Message) it.next();
    		// set the destination name to the message
    		m.setProperty("JoramDestinationName", getName());
    		message = processInterceptors(m);
    		if (message != null) {
    			newMessages.add(message);
    		} else {
    			//send message to the DMQ
    			if (dmqManager == null)
    				dmqManager = new DMQManager(dmqId, getId());
          nbMsgsSentToDMQSinceCreation++;
          dmqManager.addDeadMessage(m, MessageErrorConstants.INTERCEPTORS);
    		}
    	}
    	
    	if (dmqManager != null)
    		dmqManager.sendToDMQ();
    	
    	if (!newMessages.isEmpty()) {
    		messages = newMessages;
    	} else {
    		return;
    	}
    }
    
    setNoSave();
    boolean persistent = false;
    
    // JORAM_PERF_BRANCH
    ClientMessages.TopicReplyCallback callback = not.getTopicReplyCallback();
    int localDurableSubscriberCount = 0;
    
    // JORAM_PERF_BRANCH
    boolean persistentMessage = false;
    for (Iterator msgs = messages.iterator(); msgs.hasNext();) {
      Message msg = (Message) msgs.next();
      if (msg.persistent) {
        persistentMessage = true;
      }
    }

    for (Iterator subs = subscribers.iterator(); subs.hasNext();) {
      // Browsing the subscribers.
      subscriber = (AgentId) subs.next();
      local = (subscriber.getTo() == AgentServer.getServerId());
      selector = (String) selectors.get(subscriber);

      if (selector == null || selector.equals("")) {
        // Current subscriber does not filter messages: all messages
        // will be sent.
        if (! local) {
          // Subscriber not local, or no other sending occurred locally:
          // directly sending the messages.
          deliverables = messages;
          persistent = true;
        } else if (! alreadySentLocally) {
          deliverables = messages;
          alreadySentLocally = true;
        }
        // A local sending already occurred: cloning the messages.
        else {
          deliverables = new Vector();
          for (Iterator msgs = messages.iterator(); msgs.hasNext();)
            deliverables.add(((Message) msgs.next()).clone());
        }
      } else {
        // Current subscriber filters messages; sending the matching messages.
        deliverables = new Vector();
        for (int i = 0; i < messages.size(); i++) {
          message = (Message) messages.get(i);

          if (Selector.matches(message, selector)) {

            // Subscriber not local, or no other sending occurred locally:
            // directly sending the message.
            if (! local) {
              deliverables.add(message);
              persistent = true;
            } else if (! alreadySentLocally) {
              deliverables.add(message);
              alreadySentLocally = true;
            }
            // A local sending already occurred: cloning the message.
            else 
              deliverables.add(message.clone());
          }
        }  
      }
      
      // There are messages to send.
      if (! deliverables.isEmpty()) {
        
        TopicMsgsReply topicMsgsReply = new TopicMsgsReply(deliverables);
        topicMsgsReply.setPersistent(persistent);
        
        // JORAM_PERF_BRANCH
        if (persistentMessage && durableSubscriptions.get(subscriber) != null) {
          localDurableSubscriberCount++;
          topicMsgsReply.setCallback(callback);
        }
        
        setDmq(topicMsgsReply); 
        forward(subscriber, topicMsgsReply);
        nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation + deliverables.size();
      }
    }
    
    // JORAM_PERF_BRANCH
    if (callback != null) {
      if (localDurableSubscriberCount > 0) {
        callback.setSubscriberCount(localDurableSubscriberCount);
      } else {
        // The callback needs to be called at the end of this reaction
        callback.setSubscriberCount(1);
        not.setCallback(callback);
      }
    }
  }

  private void setDmq(TopicMsgsReply not) {
    // Setting the producer's DMQ identifier field:
    if (dmqId != null) {
      not.setDMQId(dmqId);
    } else {
      not.setDMQId(Queue.getDefaultDMQId());
    }
  }

  // AF (TODO): This method seems to be useless, verify and delete it.
  public void setAlreadySentLocally(boolean alreadySentLocally) {
    this.alreadySentLocally = alreadySentLocally;
  }

  public long getNbMsgsReceiveSinceCreation() {
    return nbMsgsReceiveSinceCreation;
  }

	public String getTxName(String msgId) {
	  // TODO Auto-generated method stub
	  return null;
  }
	
	// Flow Control related fields
  protected fr.dyade.aaa.common.stream.Properties getStats() {
    return null;
  }
}
