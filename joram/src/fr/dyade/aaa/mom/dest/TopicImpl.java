/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.admin.*;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.comm.AdminReply;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.mom.selectors.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;


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
public class TopicImpl extends DestinationImpl implements TopicImplMBean
{
  /** Identifier of this topic's father, if any. */
  protected AgentId fatherId = null;
  /** Vector of cluster fellows, if any. */
  protected Vector friends = null;
  
  /** Vector of subscribers' identifiers. */
  protected Vector subscribers;
  /** Table of subscribers' selectors. */
  protected Hashtable selectors;


  /**
   * Constructs a <code>TopicImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the topic.
   * @param adminId  Identifier of the administrator of the topic.
   */
  public TopicImpl(AgentId destId, AgentId adminId)
  {
    super(destId, adminId);
    subscribers = new Vector();
    selectors = new Hashtable();
  }


  public String toString()
  {
    return "TopicImpl:" + destId.toString();
  }


  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  If a received notification is
   *              unexpected by the topic.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    int reqId = -1;
    if (not instanceof AbstractRequest)
      reqId = ((AbstractRequest) not).getRequestId();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + not.getClass().getName()
                                    + " with id: " + reqId
                                    + " from: " + from.toString());
    try {
      if (not instanceof ClusterRequest)
        doReact(from, (ClusterRequest) not);
      else if (not instanceof ClusterTest)
        doReact(from, (ClusterTest) not);
      else if (not instanceof ClusterAck)
        doReact(from, (ClusterAck) not);
      else if (not instanceof ClusterNot)
        doReact(from, (ClusterNot) not);
      else if (not instanceof UnclusterRequest)
        doReact(from, (UnclusterRequest) not);
      else if (not instanceof UnclusterNot)
        doReact(from, (UnclusterNot) not);
      else if (not instanceof SetFatherRequest)
        doReact(from, (SetFatherRequest) not);
      else if (not instanceof FatherTest)
        doReact(from, (FatherTest) not);
      else if (not instanceof FatherAck)
        doReact(from, (FatherAck) not);
      else if (not instanceof UnsetFatherRequest)
        doReact(from, (UnsetFatherRequest) not);
      else if (not instanceof Monit_GetSubscriptions)
        doReact(from, (Monit_GetSubscriptions) not);
      else if (not instanceof Monit_GetFather)
        doReact(from, (Monit_GetFather) not);
      else if (not instanceof Monit_GetCluster)
        doReact(from, (Monit_GetCluster) not);
      else if (not instanceof SubscribeRequest)
        doReact(from, (SubscribeRequest) not);
      else if (not instanceof UnsubscribeRequest)
        doReact(from, (UnsubscribeRequest) not);
      else if (not instanceof TopicForwardNot)
        doReact(from, (TopicForwardNot) not);
      else
        super.react(from, not);
    }
    // MOM exceptions are sent to the requester.
    catch (MomException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, exc);

      AbstractRequest req = (AbstractRequest) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    }
  }

  /**
   * Method implementing the reaction to a <code>ClusterRequest</code>
   * instance requesting to add a topic to the cluster, or to set a
   * cluster with a given topic.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  protected void doReact(AgentId from, ClusterRequest req)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if (fatherId != null) {
      String info = "Request [" + req.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: topic part of a hierarchy";
      Channel.sendTo(from, new AdminReply(req, false, info));
      return;
    }

    AgentId newFriendId = req.getTopicId();

    if (friends == null)
     friends = new Vector();

    if (friends.contains(newFriendId) || destId.equals(newFriendId)) {
      String info = "Request [" + req.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: joining topic already"
                    + " part of cluster";
      Channel.sendTo(from, new AdminReply(req, false, info));
      return;
    }

    ClusterTest not = new ClusterTest(req, from);
    Channel.sendTo(newFriendId, not);
  }

  /**
   * Method implementing the reaction to a <code>ClusterTest</code>
   * notification sent by a fellow topic for testing if this topic might be
   * part of a cluster.
   */
  protected void doReact(AgentId from, ClusterTest not)
  {
    // The topic is already part of a cluster: can't join an other cluster.
    if (friends != null && ! friends.isEmpty())
      Channel.sendTo(from, new ClusterAck(not, false,
                                          "Topic [" + destId
                                          + "] can't join cluster of topic"
                                          + " [" + from + "] as it is"
                                          + " already part of a cluster"));
    // The topic is already part of a hierarchy: can't join a cluster.
    else if (fatherId != null)
      Channel.sendTo(from, new ClusterAck(not, false,
                                          "Topic [" + destId
                                          + "] can't join cluster of topic"
                                          + " [" + from + "] as it is"
                                          + " already part of a hierarchy"));
    // The topic is free: joining the cluster.
    else {
      friends = new Vector();
      friends.add(from);
      Channel.sendTo(from, new ClusterAck(not, true,
                                          "Topic [" + destId
                                          + "] ok for joining cluster of"
                                          + " topic [" + from + "]"));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Topic "
                                      + destId.toString() + " joins cluster"
                                      + "cluster of topic " + from.toString());
    }
  }

  /**
   * Method implementing the reaction to a <code>ClusterAck</code>
   * notification sent by a topic requested to join the cluster.
   */ 
  protected void doReact(AgentId from, ClusterAck ack)
  { 
    // The topic does not accept to join the cluster: doing nothing.
    if (! ack.ok) {
      Channel.sendTo(ack.requester,
                     new AdminReply(ack.request, false, ack.info));
      return;
    }
  
    AgentId fellowId;
    ClusterNot fellowNot;
    ClusterNot newFriendNot = new ClusterNot(from);
    for (int i = 0; i < friends.size(); i++) {
      fellowId = (AgentId) friends.get(i);
      fellowNot = new ClusterNot(fellowId);
      // Notifying the joining topic of the current fellow.
      Channel.sendTo(from, fellowNot);
      // Notifying the current fellow of the joining topic.
      Channel.sendTo(fellowId, newFriendNot);
    }
    friends.add(from);

    String info = "Request [" + ack.request.getClass().getName()
                  + "], sent to Topic [" + destId
                  + "], successful [true]: topic ["
                  + from + "] joined cluster";
    Channel.sendTo(ack.requester, new AdminReply(ack.request, true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a <code>ClusterNot</code>
   * notification sent by a fellow topic for notifying this topic
   * of a new cluster fellow.
   */
  protected void doReact(AgentId from, ClusterNot not)
  {
    friends.add(not.topicId);
      
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Topic "
                                    + not.topicId.toString()
                                    + " set as a fellow.");
  }
 
  /**
   * Method implementing the reaction to an <code>UnclusterRequest</code>
   * instance requesting this topic to leave the cluster it is part of.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  protected void doReact(AgentId from, UnclusterRequest request)
                 throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if (friends == null || friends.isEmpty()) {
      String info = "Request [" + request.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: topic not part of a cluster";
      Channel.sendTo(from, new AdminReply(request, false, info));
      return;
    }

    UnclusterNot not = new UnclusterNot();
    AgentId fellowId;
    // Notifying each fellow of the leave.
    while (! friends.isEmpty()) {
      fellowId = (AgentId) friends.remove(0);
      Channel.sendTo(fellowId, not);
    }
    friends = null;

    String info = "Request [" + request.getClass().getName()
                  + "], sent to Topic [" + destId
                  + "], successful [true]: topic left the cluster";
    Channel.sendTo(from, new AdminReply(request, true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }
 
  /**
   * Method implementing the reaction to an <code>UnclusterNot</code>
   * notification sent by a topic leaving the cluster.
   */
  protected void doReact(AgentId from, UnclusterNot not)
  {
    friends.remove(from);

    if (friends.isEmpty())
      friends = null;

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Topic "
                                    + from.toString() + " removed from"
                                    + " cluster.");
  }

  /**
   * Method implementing the reaction to a <code>SetFatherRequest</code>
   * instance notifying this topic it is part of a hierarchy as a son.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  protected void doReact(AgentId from, SetFatherRequest request)
                 throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if (fatherId != null) {
      String info = "Request [" + request.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: topic already part"
                    + " of a hierarchy";
      Channel.sendTo(from, new AdminReply(request, false, info));
      return;
    }

    if (friends != null) {
      String info = "Request [" + request.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: topic already part"
                    + " of a cluster";
      Channel.sendTo(from, new AdminReply(request, false, info));
      return;
    }

    Channel.sendTo(request.getFatherId(), new FatherTest(request, from));
  }

  /**
   * Method reacting to a <code>FatherTest</code> notification checking if it
   * can be a father to a topic.
   */ 
  protected void doReact(AgentId from, FatherTest not)
  {
    if (friends != null && ! friends.isEmpty())
      Channel.sendTo(from, new FatherAck(not, false,
                                         "Topic [" + destId
                                         + "] can't accept topic [" + from
                                         + "] as a son as it is part of a"
                                         + " cluster"));
    else
      Channel.sendTo(from, new FatherAck(not, true,
                                         "Topic [" + destId
                                         + "] accepts topic [" + from
                                         + "] as a son"));
  }

  /**
   * Method reacting to a <code>FatherAck</code> notification coming from
   * the topic this topic requested as a father.
   */ 
  protected void doReact(AgentId from, FatherAck not)
  {
    // The topic does not accept to join the hierarchy: doing nothing.
    if (! not.ok) {
      Channel.sendTo(not.requester,
                     new AdminReply(not.request, false, not.info));
      return;
    }
  
    // The topic accepts to be a father: setting it.
    fatherId = from;
  
    String info = "Request [" + not.request.getClass().getName()
                  + "], sent to Topic [" + destId
                  + "], successful [true]: topic ["
                  + from + "] set as father";
    Channel.sendTo(not.requester, new AdminReply(not.request, true, info));
  
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to an <code>UnsetFatherRequest</code>
   * instance notifying this topic to leave its father.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  protected void doReact(AgentId from, UnsetFatherRequest request)
                 throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    if (fatherId == null) {
      String info = "Request [" + request.getClass().getName()
                    + "], sent to Topic [" + destId
                    + "], successful [false]: topic is not a son";
      Channel.sendTo(from, new AdminReply(request, false, info));
      return;
    }

    fatherId = null;

    String info = "Request [" + request.getClass().getName()
                  + "], sent to Topic [" + destId
                  + "], successful [true]: father unset";
    Channel.sendTo(from, new AdminReply(request, true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetSubscriptions</code> notification requesting the
   * number of subscriptions.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetSubscriptions not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Channel.sendTo(from, new Monit_GetNumberRep(not, subscribers.size()));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetFather</code>
   * notification requesting the identifier of the hierarchical father.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetFather not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String id = null;
    if (fatherId != null)
      id = fatherId.toString();

    Channel.sendTo(from, new Monit_GetFatherRep(not, id));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetCluster</code>
   * notification requesting the identifiers of the cluster's topics.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetCluster not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Vector cluster = null;
    if (friends != null) {
      cluster = new Vector();
      for (int i = 0; i < friends.size(); i++)
        cluster.add(friends.get(i).toString());
      cluster.add(destId.toString());
    }

    Channel.sendTo(from, new Monit_GetClusterRep(not, cluster));
  }

  /**
   * Method implementing the reaction to a <code>SubscribeRequest</code>
   * instance. 
   *
   * @exception AccessException  If the sender is not a READER.
   */
  protected void doReact(AgentId from, SubscribeRequest not)
                 throws AccessException
  {
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Adding new subscriber.
    if (! subscribers.contains(from))
      subscribers.add(from);

    // The requester might either be a new subscriber, or an existing one;
    // setting the selector, possibly by removing or modifying an already set
    // expression.
    if (not.getSelector() != null && ! not.getSelector().equals(""))
      selectors.put(from, not.getSelector());
    else
      selectors.remove(from);

    Channel.sendTo(from, new SubscribeReply(not));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "Client " + from
                                    + " set as a subscriber with selector "
                                    + not.getSelector());
  }

  /**
   * Method implementing the reaction to an <code>UnsubscribeRequest</code>
   * instance, requesting to remove a subscriber.
   */
  protected void doReact(AgentId from, UnsubscribeRequest not)
  {
    subscribers.remove(from);
    selectors.remove(from);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "Client " + from
                                    + " removed from the subscribers.");
  } 

  /**
   * Method implementing the reaction to a <code>TopicForwardNot</code>
   * instance, carrying messages forwarded by a cluster fellow or a
   * hierarchical son.
   */
  protected void doReact(AgentId from, TopicForwardNot not)
  {
    // If the forward comes from a son, forwarding it to the father, if any.
    if (not.toFather && fatherId != null)
      Channel.sendTo(fatherId, not);
    
    // Processing the received messages. 
    processMessages(not.messages);
  }


  /**
   * The <code>DestinationImpl</code> class calls this method for passing
   * notifications which have been partly processed, so that they are
   * specifically processed by the <code>TopicImpl</code> class.
   */
  protected void specialProcess(Notification not)
  {
    if (not instanceof SetRightRequest)
      doProcess((SetRightRequest) not);
    else if (not instanceof ClientMessages)
      doProcess((ClientMessages) not);
    else if (not instanceof UnknownAgent)
      doProcess((UnknownAgent) not);
    else if (not instanceof DeleteNot)
      doProcess((DeleteNot) not);
  }
  
  /**
   * Method specifically processing a <code>SetRightRequest</code> instance.
   * <p>
   * When a reader is removed, deleting this reader's subscription if any,
   * and sending an <code>ExceptionReply</code> notification to the client.
   */
  protected void doProcess(SetRightRequest not)
  {
    // If the request does not unset a reader, doing nothing.
    if (not.getRight() != -READ)
      return;

    AgentId user = not.getClient();
    AccessException exc = new AccessException("READ right removed.");

    // Identified user: removing it.
    if (user != null) {
      subscribers.remove(user);
      selectors.remove(user);
      Channel.sendTo(user, new ExceptionReply(exc));
    }
    // Free reading right removed: removing all non readers.
    else {
      for (Enumeration subs = subscribers.elements(); 
           subs.hasMoreElements();) {
        user = (AgentId) subs.nextElement();
        if (! isReader(user)) {
          subscribers.remove(user);
          selectors.remove(user);
          Channel.sendTo(user, new ExceptionReply(exc));
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
  protected void doProcess(ClientMessages not)
  {
    // Forwarding the messages to the father or the cluster fellows, if any:
    forwardMessages(not);
    // Processing the messages:
    processMessages(not);
  }

  /**
   * Method specifically processing an <code>UnknownAgent</code> instance.
   * <p>
   * This method notifies the administrator of the failing cluster or
   * hierarchy building request, if needed, or removes the subscriptions of
   * the deleted client, if any, or sets the father identifier to null if it
   * comes from a deleted father.
   */
  protected void doProcess(UnknownAgent uA)
  {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    // Deleted topic was requested to join the cluster: notifying the
    // requester:
    if (not instanceof ClusterTest) {
      ClusterTest cT = (ClusterTest) not;
      String info = "Topic [" + agId + "] can't join cluster "
                    + "as it does not exist";
      Channel.sendTo(cT.requester, new AdminReply(cT.request, false, info));
    }
    // Deleted topic was requested as a father: notifying the requester:
    else if (not instanceof FatherTest) {
      FatherTest fT = (FatherTest) not;
      String info = "Topic [" + agId + "] can't join hierarchy "
                    + "as it does not exist";
      Channel.sendTo(fT.requester, new AdminReply(fT.request, false, info));
    }
    else {
      // Removing the deleted client's subscriptions, if any.
      subscribers.remove(agId);
      selectors.remove(agId);

      // Removing the father identifier, if needed.
      if (fatherId != null && agId.equals(fatherId))
        fatherId = null;

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
  protected void doProcess(DeleteNot not)
  {
    AgentId clientId;
    Vector subs;
    SubscribeRequest sub;

    // For each subscriber...
    for (int i = 0; i < subscribers.size(); i++) {
      clientId = (AgentId) subscribers.get(i);
      Channel.sendTo(clientId, new UnknownAgent(destId, null));
    }

    // For each cluster fellow if any...
    if (friends != null) {
      AgentId topicId;
      while (! friends.isEmpty()) {
        topicId = (AgentId) friends.remove(0);
        Channel.sendTo(topicId, new UnclusterNot());
      }
    }
  }

  /**
   * Actually forwards a vector of messages to the father or the cluster
   * fellows, if any.
   */
  protected void forwardMessages(ClientMessages messages)
  {
    if (friends != null && ! friends.isEmpty()) {
      AgentId topicId;
      for (int i = 0; i < friends.size(); i++) {
        topicId = (AgentId) friends.get(i);
        Channel.sendTo(topicId, new TopicForwardNot(messages, false));

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Messages "
                                        + "forwarded to fellow "
                                        + topicId.toString());
      } 
    }
    else if (fatherId != null) {
      Channel.sendTo(fatherId, new TopicForwardNot(messages, true));

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Messages "
                                      + "forwarded to father "
                                      + fatherId.toString());
    }
  }

  /**
   * Actually processes the distribution of the received messages to the
   * valid subscriptions by sending a <code>TopicMsgsReply</code> notification
   * to the valid subscribers.
   */
  protected void processMessages(ClientMessages not)
  {
    Vector messages = not.getMessages();
    AgentId subscriber;
    boolean local;
    String selector;
    boolean alreadySentLocally = false;
    Vector deliverables;
    Message message;

    // Browsing the subscribers.
    for (Enumeration subs = subscribers.elements(); subs.hasMoreElements();) {
      subscriber = (AgentId) subs.nextElement();
      local = (subscriber.getTo() == AgentServer.getServerId());
      selector = (String) selectors.get(subscriber);

      // Current subscriber does not filter messages: all messages will be
      // sent.
      if (selector == null || selector.equals("")) {
        // Subscriber not local, or no other sending occured locally: directly
        // sending the messages.
        if (! local)
          deliverables = messages;
        else if (! alreadySentLocally) {
          deliverables = messages;
          alreadySentLocally = true;
        }
        // A local sending already occured: cloning the messages.
        else {
          deliverables = new Vector();
          for (Enumeration msgs = messages.elements(); msgs.hasMoreElements();)
            deliverables.add(((Message) msgs.nextElement()).clone());
        }
      }
      // Current subscriber filters messages; sending the matching messages.
      else {
        deliverables = new Vector();
        for (int i = 0; i < messages.size(); i++) {
          message = (Message) messages.get(i);
        
          if (Selector.matches(message, selector)) {

            // Subscriber not local, or no other sending occured locally:
            // directly sending the message.
            if (! local)
              deliverables.add(message);
            else if (! alreadySentLocally) {
              deliverables.add(message);
              alreadySentLocally = true;
            }
            // A local sending already occured: cloning the message.
            else
              deliverables.add(message.clone());
          }
        }  
      }
      // There are messages to send.
      if (! deliverables.isEmpty())
        Channel.sendTo(subscriber, new TopicMsgsReply(deliverables));
    }
  }


  /**
   * MBean method: returns <code>true</code> if the destination is freely
   * readable.
   */
  public boolean isFreelyReadable()
  {
    return freeReading;
  }

  /**
   * MBean method: returns <code>true</code> if the destination is freely
   * writeable.
   */
  public boolean isFreelyWriteable()
  {
    return freeWriting;
  }

  /**
   * MBean method: returns the identifiers of the readers on the destination.
   */
  public String getReaders()
  {
    Object key;
    int right;
    Vector readers = new Vector();
    for (Enumeration enum = clients.keys(); enum.hasMoreElements();) {
      key = enum.nextElement();
      right = ((Integer) clients.get(key)).intValue();
      if (right == READ || right == READWRITE)
        readers.add(key.toString());
    }
    return readers.toString();
  }

  /**
   * MBean method: returns the identifiers of the writers on the destination.
   */
  public String getWriters()
  {
    Object key;
    int right;
    Vector writers = new Vector();
    for (Enumeration enum = clients.keys(); enum.hasMoreElements();) {
      key = enum.nextElement();
      right = ((Integer) clients.get(key)).intValue();
      if (right == WRITE || right == READWRITE)
        writers.add(key.toString());
    }
    return writers.toString();
  }

  /** MBean interface implementation: deletes the destination. */
  public void delete()
  {
    DeleteDestination request = new DeleteDestination(destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: removes a given writer.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              writer is not a registered writer.
   */
  public void removeWriter(String writerId) throws Exception
  {
    try {
      if (writerId != null && ! isWriter(AgentId.fromString(writerId)))
        throw new Exception("Unknown writer identifier.");
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }

    UnsetWriter request = new UnsetWriter(writerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }
        

  /**
   * MBean interface implementation: removes a given reader.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              reader is not a registered reader.
   */
  public void removeReader(String readerId) throws Exception
  {
    try {
      if (readerId != null && ! isReader(AgentId.fromString(readerId)))
        throw new Exception("Unknown reader identifier.");
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }

    UnsetReader request = new UnsetReader(readerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: adds a given writer on the destination.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addWriter(String writerId) throws Exception
  {
    try {
      AgentId.fromString(writerId);
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }
    SetWriter request = new SetWriter(writerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: adds a given reader on the destination.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addReader(String readerId) throws Exception
  {
    try {
      AgentId.fromString(readerId);
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }
    SetReader request = new SetReader(readerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }
 
  /**
   * MBean interface implementation: removes free writing access on
   * the destination.
   */
  public void removeFreeWriting()
  {
    UnsetWriter request = new UnsetWriter(null, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }  

  /**
   * MBean interface implementation: removes free reading access on
   * the destination.
   */
  public void removeFreeReading()
  {
    UnsetReader request = new UnsetReader(null, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }  

  /**
   * MBean interface implementation: provides free writing access on
   * the destination.
   */
  public void provideFreeWriting()
  {
    try {
      addWriter(null);
    }
    catch (Exception exc) {}
  }

  /**
   * MBean interface implementation: provides free reading access on
   * the destination.
   */
  public void provideFreeReading()
  {
    try {
      addReader(null);
    }
    catch (Exception exc) {}
  }

  /**
   * MBean interface implementation; returns the number of subscribers.
   */
  public int getNumberOfSubscribers()
  {
    return subscribers.size();
  }
}
