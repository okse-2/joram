/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.AdminReply;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ClusterRequest;
import org.objectweb.joram.mom.notifications.DestinationAdminRequestNot;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_GetCluster;
import org.objectweb.joram.mom.notifications.Monit_GetClusterRep;
import org.objectweb.joram.mom.notifications.Monit_GetFather;
import org.objectweb.joram.mom.notifications.Monit_GetFatherRep;
import org.objectweb.joram.mom.notifications.Monit_GetNumberRep;
import org.objectweb.joram.mom.notifications.Monit_GetSubscriptions;
import org.objectweb.joram.mom.notifications.SetFatherRequest;
import org.objectweb.joram.mom.notifications.SetRightRequest;
import org.objectweb.joram.mom.notifications.SubscribeReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.UnclusterRequest;
import org.objectweb.joram.mom.notifications.UnsetFatherRequest;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.shared.admin.GetSubscriberIds;
import org.objectweb.joram.shared.admin.GetSubscriberIdsRep;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The <code>TopicImpl</code> class implements the MOM topic behaviour,
 * basically distributing the received messages to subscribers.
 * <p>
 * A Topic might be part of a hierarchy; if it is the case, and if the topic
 * is not on top of that hierarchy, it will have a father to forward messages
 * to.
 * <p>
 * A topic might also be part of a cluster; if it is the case, it will have
 * friends to forward messages to.
 * <p>
 * A topic can't be part of a hierarchy and of a cluster at the same time.
 */
public class TopicImpl extends DestinationImpl implements TopicImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  /** Identifier of this topic's father, if any. */
  protected AgentId fatherId = null;
  /** Set of cluster fellows, if any. */
  protected Set friends = null;

  /** Vector of subscribers' identifiers. */
  protected Vector subscribers;
  /** Table of subscribers' selectors. */
  protected Hashtable selectors;

  /** Internal boolean used for tagging local sendings. */
  protected transient boolean alreadySentLocally;

  /**
   * Constructs a <code>TopicImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the topic.
   * @param adminId  Identifier of the administrator of the topic.
   * @param prop     The initial set of properties.
   */
  public TopicImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    subscribers = new Vector();
    selectors = new Hashtable();
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime		true when first called by the factory
   */
  public void initialize(boolean firstTime) {
  }

  /**
   * Returns a string representation of this destination.
   */
  public String toString() {
    return "TopicImpl:" + getId().toString();
  }

  /**
   * Method implementing the reaction to a <code>ClusterRequest</code>
   * instance requesting to add a topic to the cluster, or to set a
   * cluster with a given topic.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  public void clusterRequest(AgentId from, ClusterRequest req) throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String info = null;
    if (fatherId != null) {
      info = strbuf.append("Request [").append(req.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: topic part of a hierarchy").toString();
      strbuf.setLength(0);
      forward(from, new AdminReply(req, false, info));
      return;
    }

    AgentId newFriendId = req.getTopicId();

    if (friends == null) {
      // state change, so save.
      setSave();
      friends = new HashSet();
    }

    if (getId().equals(newFriendId)) {
      info = strbuf.append("Request [").append(req.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: joining topic already")
      .append(" part of cluster").toString();
      strbuf.setLength(0);
      forward(from, new AdminReply(req, false, info));
      return;
    }

    ClusterTest not = new ClusterTest(req, from, friends);
    forward(newFriendId, not);
  }

  /**
   * Method implementing the reaction to a <code>ClusterTest</code>
   * notification sent by a fellow topic for testing if this topic might be
   * part of a cluster.
   */
  public void clusterTest(AgentId from, ClusterTest not) {
    String info = null;
    // The topic is already part of a cluster: can't join an other cluster.
    if (friends != null && ! friends.isEmpty()) {
      if (friends.contains(from)) {
        info = strbuf.append("Topic [").append(getId())
        .append("] already joined cluster of topic [").append(from)
        .append(']').toString();
        strbuf.setLength(0);
        friends.add(from);
        friends.addAll(not.friends);
        // Remove self if present
        friends.remove(getId());
        forward(from, new ClusterAck(not, true, info));
      } else {
        info = strbuf.append("Topic [").append(getId())
        .append("] can't join cluster of topic [").append(from)
        .append("] as it is already part of a cluster").toString();
        strbuf.setLength(0);
        forward(from, new ClusterAck(not, false, info));
      }
      // The topic is already part of a hierarchy: can't join a cluster.
    } else if (fatherId != null) {
      info = strbuf.append("Topic [").append(getId())
      .append("] can't join cluster of topic [").append(from)
      .append("] as it is already part of a hierarchy").toString();
      strbuf.setLength(0);
      forward(from, new ClusterAck(not, false, info));
      // The topic is free: joining the cluster.
    } else {
      // state change, so save.
      setSave();
      friends = new HashSet();
      friends.add(from);
      friends.addAll(not.friends);
      // Remove self if present
      friends.remove(getId());
      info = strbuf.append("Topic [").append(getId())
      .append("] ok for joining cluster of topic [").append(from)
      .append(']').toString();
      strbuf.setLength(0);
      forward(from, new ClusterAck(not, true, info));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Topic " + getId().toString() + " joins cluster of topic " + from.toString());
    }
  }

  /**
   * Method implementing the reaction to a <code>ClusterAck</code>
   * notification sent by a topic requested to join the cluster.
   */ 
  public void clusterAck(AgentId from, ClusterAck ack){ 
    // The topic does not accept to join the cluster: doing nothing.
    if (! ack.ok) {
      forward(ack.requester, new AdminReply(ack.request, false, ack.info));
      return;
    }

    ClusterNot newFriendNot = new ClusterNot(from);
    Iterator iterator = friends.iterator();
    while (iterator.hasNext()) {
      // Notifying the current fellow of the joining topic.
      forward((AgentId) iterator.next(), newFriendNot);
    }
    // state change, so save.
    setSave();
    friends.add(from);

    String info = strbuf.append("Request [").append(ack.request.getClass().getName())
    .append("], sent to Topic [").append(getId())
    .append("], successful [true]: topic [")
    .append(from).append("] joined cluster").toString();
    strbuf.setLength(0);
    forward(ack.requester, new AdminReply(ack.request, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a <code>ClusterNot</code>
   * notification sent by a fellow topic for notifying this topic
   * of a new cluster fellow.
   */
  public void clusterNot(AgentId from, ClusterNot not) {
    if (! not.topicId.equals(getId())) {
      // state change, so save.
      setSave();
      friends.add(not.topicId);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Topic " + not.topicId.toString() + " set as a fellow.");
    }
  }

  /**
   * Method implementing the reaction to an <code>UnclusterRequest</code>
   * instance requesting this topic to leave the cluster it is part of.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  public void unclusterRequest(AgentId from, UnclusterRequest request) throws MomException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if (friends == null || friends.isEmpty()) {
      String info = strbuf.append("Request [")
      .append(request.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: topic not part of a cluster")
      .toString();
      strbuf.setLength(0);
      forward(from, new AdminReply(request, false, info));
      return;
    }

    UnclusterNot not = new UnclusterNot();
    // Notifying each fellow of the leave.
    Iterator iterator = friends.iterator();
    while (iterator.hasNext()) {
      forward((AgentId) iterator.next(), not);
    }
    friends = null;
    // state change, so save.
    setSave();

    String info = strbuf.append("Request [")
    .append(request.getClass().getName())
    .append("], sent to Topic [").append(getId())
    .append("], successful [true]: topic left the cluster").toString();
    strbuf.setLength(0);
    forward(from, new AdminReply(request, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to an <code>UnclusterNot</code>
   * notification sent by a topic leaving the cluster.
   */
  public void unclusterNot(AgentId from, UnclusterNot not) {
    // state change, so save.
    setSave();
    friends.remove(from);

    if (friends.isEmpty()) friends = null;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Topic "  + from.toString() + " removed from" + " cluster.");
  }

  /**
   * Method implementing the reaction to a <code>SetFatherRequest</code>
   * instance notifying this topic it is part of a hierarchy as a son.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  public void setFatherRequest(AgentId from, SetFatherRequest request) throws MomException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if ((fatherId != null) && ! fatherId.equals(request.getFatherId())) {
      strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: topic already part of a hierarchy");
      forward(from, new AdminReply(request, false, strbuf.toString()));
      strbuf.setLength(0);
      return;
    }

    if (friends != null) {
      strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: topic already part of a cluster");
      forward(from, new AdminReply(request, false, strbuf.toString()));
      strbuf.setLength(0);
      return;
    }

    forward(request.getFatherId(), new FatherTest(request, from));
  }

  /**
   * Method reacting to a <code>FatherTest</code> notification checking if it
   * can be a father to a topic.
   */ 
  public void fatherTest(AgentId from, FatherTest not) {
    if (friends != null && ! friends.isEmpty()) {
      strbuf.append("Topic [").append(getId())
      .append("] can't accept topic [").append(from)
      .append("] as a son as it is part of a cluster");
      forward(from, new FatherAck(not, false, strbuf.toString()));
      strbuf.setLength(0);
    } else {
      strbuf.append("Topic [").append(getId())
      .append("] accepts topic [").append(from).append("] as a son");
      forward(from, new FatherAck(not, true, strbuf.toString()));
      strbuf.setLength(0);
    }
  }

  /**
   * Method reacting to a <code>FatherAck</code> notification coming from
   * the topic this topic requested as a father.
   */ 
  public void fatherAck(AgentId from, FatherAck not) {
    // The topic does not accept to join the hierarchy: doing nothing.
    if (! not.ok) {
      forward(not.requester, new AdminReply(not.request, false, not.info));
      return;
    }

    // state change, so save.
    setSave();
    // The topic accepts to be a father: setting it.
    fatherId = from;

    String info = strbuf.append("Request [")
    .append(not.request.getClass().getName())
    .append("], sent to Topic [").append(getId())
    .append("], successful [true]: topic [")
    .append(from).append("] set as father").toString();
    strbuf.setLength(0);
    forward(not.requester, new AdminReply(not.request, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to an <code>UnsetFatherRequest</code>
   * instance notifying this topic to leave its father.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  public void unsetFatherRequest(AgentId from, UnsetFatherRequest request) throws MomException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String info = null;
    if (fatherId == null) {
      info = strbuf.append("Request [").append(request.getClass().getName())
      .append("], sent to Topic [").append(getId())
      .append("], successful [false]: topic is not a son").toString();
      strbuf.setLength(0);
      forward(from, new AdminReply(request, false, info));
      return;
    }

    // state change, so save.
    setSave();
    fatherId = null;

    info = strbuf.append("Request [").append(request.getClass().getName())
    .append("], sent to Topic [").append(getId())
    .append("], successful [true]: father unset").toString();
    strbuf.setLength(0);
    forward(from, new AdminReply(request, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetSubscriptions</code> notification requesting the
   * number of subscriptions.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetSubscriptions(AgentId from, Monit_GetSubscriptions not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    forward(from, new Monit_GetNumberRep(not, subscribers.size()));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetFather</code>
   * notification requesting the identifier of the hierarchical father.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetFather(AgentId from, Monit_GetFather not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String id = null;
    if (fatherId != null)
      id = fatherId.toString();

    forward(from, new Monit_GetFatherRep(not, id));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetCluster</code>
   * notification requesting the identifiers of the cluster's topics.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetCluster(AgentId from, Monit_GetCluster not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Vector cluster = null;
    if (friends != null) {
      cluster = new Vector();
      Iterator iterator = friends.iterator();
      while (iterator.hasNext()) {
        cluster.add(iterator.next().toString());
      }
      cluster.add(getId().toString());
    }

    forward(from, new Monit_GetClusterRep(not, cluster));
  }

  public void preSubscribe(SubscribeRequest not) {
    // do nothing
  }

  public void postSubscribe(SubscribeRequest not) {
    // do nothing
  }

  /**
   * Method implementing the reaction to a <code>SubscribeRequest</code>
   * instance. 
   *
   * @exception AccessException  If the sender is not a READER.
   */
  public void subscribeRequest(AgentId from, SubscribeRequest not) throws AccessException {
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    preSubscribe(not);

    // Adding new subscriber.
    if (! subscribers.contains(from)) {
      // state change, so save.
      setSave();
      subscribers.add(from);
    }

    // state change, so save.
    setSave();

    // The requester might either be a new subscriber, or an existing one;
    // setting the selector, possibly by removing or modifying an already set
    // expression.
    if (not.getSelector() != null && ! not.getSelector().equals(""))
      selectors.put(from, not.getSelector());
    else
      selectors.remove(from);

    if (!not.isAsyncSub())
      forward(from, new SubscribeReply(not));

    postSubscribe(not);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Client " + from + " set as a subscriber with selector " + not.getSelector());
  }

  public void preUnsubscribe(UnsubscribeRequest not) {
    // do nothing
  }

  public void postUnsubscribe(UnsubscribeRequest not) {
    // do nothing
  }

  /**
   * Method implementing the reaction to an <code>UnsubscribeRequest</code>
   * instance, requesting to remove a subscriber.
   */
  public void unsubscribeRequest(AgentId from, UnsubscribeRequest not) {

    preUnsubscribe(not);

    // state change, so save.
    setSave();
    subscribers.remove(from);
    selectors.remove(from);

    postUnsubscribe(not);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Client " + from + " removed from the subscribers.");
  } 

  /**
   * Method implementing the reaction to a <code>TopicForwardNot</code>
   * instance, carrying messages forwarded by a cluster fellow or a
   * hierarchical son.
   */
  public void topicForwardNot(AgentId from, TopicForwardNot not) { 
    // If the forward comes from a son, forwarding it to the father, if any.
    if (not.toFather && fatherId != null) {
      forward(fatherId, not);
      alreadySentLocally = fatherId.getTo() == AgentServer.getServerId();
    }

    // Processing the received messages. 
    processMessages(not.messages);
  }

  public void destinationAdminRequestNot(AgentId from, DestinationAdminRequestNot not) {
    org.objectweb.joram.shared.admin.AdminRequest adminRequest = 
      not.getRequest();
    if (adminRequest instanceof GetSubscriberIds) {
      getSubscriberIds((GetSubscriberIds)adminRequest,
                       not.getReplyTo(),
                       not.getRequestMsgId(),
                       not.getReplyMsgId());
    }
  }

  private void getSubscriberIds(GetSubscriberIds request,
                                AgentId replyTo,
                                String requestMsgId,
                                String replyMsgId) {
    GetSubscriberIdsRep reply = 
      new GetSubscriberIdsRep(getSubscriberIds());
    replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
  }

  /**
   * Returns the list of unique identifiers of all subscribers. Each user
   * appears once even if there is multiples subscriptions, the different
   * subscriptions can be enumerate through the proxy MBean.
   *
   * @return the list of unique identifiers of all subscribers.
   */
  public String[] getSubscriberIds() {
    String[] res = new String[subscribers.size()];
    for (int i = 0; i < res.length; i++) {
      AgentId aid = (AgentId)subscribers.elementAt(i);
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
  protected void doRightRequest(SetRightRequest not) {
    // If the request does not unset a reader, doing nothing.
    if (not.getRight() != -READ)
      return;

    SetRightRequest rightRequest = preProcess(not);

    if (rightRequest != null) {
      AgentId user = rightRequest.getClient();
      AccessException exc = new AccessException("READ right removed.");

      // Identified user: removing it.
      if (user != null) {
        // state change, so save.
        setSave();
        subscribers.remove(user);
        selectors.remove(user);
        forward(user, new ExceptionReply(exc));
      }
      // Free reading right removed: removing all non readers.
      else {
        for (Enumeration subs = subscribers.elements(); 
        subs.hasMoreElements();) {
          user = (AgentId) subs.nextElement();
          if (! isReader(user)) {
            // state change, so save.
            setSave();
            subscribers.remove(user);
            selectors.remove(user);
            forward(user, new ExceptionReply(exc));
          }
        }
      }

      postProcess(rightRequest);
    }
  }

  /**
   * Method specifically processing a <code>ClientMessages</code> instance.
   * <p>
   * This method may forward the messages to the topic father if any, or
   * to the cluster fellows if any.It may finally send
   * <code>TopicMsgsReply</code> instances to the valid subscribers.
   */
  protected void doClientMessages(AgentId from, ClientMessages not) {
    ClientMessages clientMsgs = preProcess(from, not);

    if (clientMsgs != null) {
      // Forwarding the messages to the father or the cluster fellows, if any:
      forwardMessages(clientMsgs);
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

    // Deleted topic was requested to join the cluster: notifying the
    // requester:
    String info = null;
    if (not instanceof ClusterTest) {
      ClusterTest cT = (ClusterTest) not;
      info = strbuf.append("Topic [").append(agId)
      .append("] can't join cluster as it does not exist").toString();
      strbuf.setLength(0);
      forward(cT.requester, new AdminReply(cT.request, false, info));
    } else if (not instanceof FatherTest) {
      // Deleted topic was requested as a father: notifying the requester:
      FatherTest fT = (FatherTest) not;
      info = strbuf.append("Topic [").append(agId)
      .append("] can't join hierarchy as it does not exist").toString();
      strbuf.setLength(0);
      forward(fT.requester, new AdminReply(fT.request, false, info));
    } else {
      // state change, so save.
      setSave();
      // Removing the deleted client's subscriptions, if any.
      subscribers.remove(agId);
      selectors.remove(agId);

      // Removing the father identifier, if needed.
      if (fatherId != null && agId.equals(fatherId)) {
        // state change, so save.
        setSave();
        fatherId = null;
      }
    }
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * <code>UnknownAgent</code> notifications are sent to each subscriber
   * and <code>UnclusterNot</code> notifications to the cluster
   * fellows.
   */
  protected void doDeleteNot(DeleteNot not) {
    AgentId clientId;

    // For each subscriber...
    for (int i = 0; i < subscribers.size(); i++) {
      clientId = (AgentId) subscribers.get(i);
      forward(clientId, new UnknownAgent(getId(), null));
    }

    // For each cluster fellow if any...
    if (friends != null) {
      Iterator iterator = friends.iterator();
      while (iterator.hasNext()) {
        // state change, so save.
        setSave();
        forward((AgentId) iterator.next(), new UnclusterNot());
      }
      friends = null;
    }
  }

  /**
   * Actually forwards a vector of messages to the father or the cluster
   * fellows, if any.
   */
  protected void forwardMessages(ClientMessages messages) {
    if (friends != null && ! friends.isEmpty()) {
      AgentId topicId;
      Iterator iterator = friends.iterator();
      while (iterator.hasNext()) {
        topicId = (AgentId) iterator.next();
        forward(topicId, new TopicForwardNot(messages, false));

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Messages " + "forwarded to fellow " + topicId.toString());
      } 
    } else if (fatherId != null) {
      forward(fatherId, new TopicForwardNot(messages, true));

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Messages " + "forwarded to father " + fatherId.toString());
    }
  }

  /**
   * Actually processes the distribution of the received messages to the
   * valid subscriptions by sending a <code>TopicMsgsReply</code> notification
   * to the valid subscribers.
   */
  protected void processMessages(ClientMessages not) {
    Vector messages = not.getMessages();
    AgentId subscriber;
    boolean local;
    String selector;
    Vector deliverables;
    Message message;

    nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation + messages.size();

    setNoSave();
    boolean persistent = false;

    for (Enumeration subs = subscribers.elements(); subs.hasMoreElements();) {
      // Browsing the subscribers.
      subscriber = (AgentId) subs.nextElement();
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
          for (Enumeration msgs = messages.elements(); msgs.hasMoreElements();)
            deliverables.add(((Message) msgs.nextElement()).clone());
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
        setDmq(topicMsgsReply); 
        forward(subscriber, topicMsgsReply);
        nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation + deliverables.size();
      }
    }
  }

  private void setDmq(TopicMsgsReply not) {
    // Setting the producer's DMQ identifier field:
    if (dmqId != null) {
      not.setDMQId(dmqId);
    } else {
      not.setDMQId(QueueImpl.getDefaultDMQId());
    }
  }

  public void setAlreadySentLocally(boolean alreadySentLocally) {
    this.alreadySentLocally = alreadySentLocally;
  }

  public long getNbMsgsReceiveSinceCreation() {
    return nbMsgsReceiveSinceCreation;
  }
}
