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

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.mom.selectors.*;

import java.util.*;

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
public class TopicImpl extends DestinationImpl
{
  /**
   * Table of subscriptions.
   * <p>
   * <b>Key:</b> subscriber identifier<br>
   * <b>Object:</b> vector of <code>SubscribeRequest</code> instances
   */
  private Hashtable subsTable;

  /** Identifier of this topic's father, if any. */
  protected AgentId fatherId = null;
  /** Vector of cluster fellows, if any. */
  protected Vector friends = null;


  /**
   * Constructs a <code>TopicImpl</code> instance.
   *
   * @param topicId  See superclass.
   * @param adminId  See superclass.
   */
  public TopicImpl(AgentId topicId, AgentId adminId)
  {
    super(topicId, adminId);

    subsTable = new Hashtable();
  }

  /** Returns a string view of this TopicImpl instance. */
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
    String reqId = null;
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
    // MOM Exceptions are sent to the requester.
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

    int number = 0;
    if (subsTable != null) {
      Vector subs;
      for (Enumeration keys = subsTable.keys(); keys.hasMoreElements();) {
        subs = (Vector) subsTable.get(keys.nextElement());
        number = number + subs.size();
      }
    }
    Channel.sendTo(from, new Monit_GetNumberRep(not, number));
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
   * instance, requesting to set a new subscription.
   *
   * @exception AccessException  If the sender is not a READER.
   */
  protected void doReact(AgentId from, SubscribeRequest not)
                 throws AccessException
  {
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    Vector clientSubs;
    SubscribeRequest currSub;
    boolean added = false;

    // If the sender already has subscriptions, adding the new one, or
    // replacing the one that has the same name:
    if (subsTable.containsKey(from)) {
      clientSubs = (Vector) subsTable.get(from);
      for (int i = 0; i < clientSubs.size(); i++) {
        currSub = (SubscribeRequest) clientSubs.get(i);
        if ((currSub.getName()).equals(not.getName())) {
          clientSubs.setElementAt(not, i);
          added = true;
          break;
        }
      }	
      if (! added)
        clientSubs.add(not);
    }
    // Else, creating its entry and adding the new subscription:
    else {
      clientSubs = new Vector();
      clientSubs.add(not);
      subsTable.put(from, clientSubs);
    }
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Subscription "
                                    + not.getName() + " of client " + from
                                    + " stored.");
  }

  /**
   * Method implementing the reaction to an <code>UnsubscribeRequest</code>
   * instance, requesting to remove one or many client subscriptions.
   *
   * @exception RequestException  If the subscription to remove does not exist.
   */
  protected void doReact(AgentId from, UnsubscribeRequest not)
                 throws RequestException
  {
    // Getting the name of the subscription to remove:
    String subName = not.getName();
    // Getting the subscriptions of the requester:
    Vector clientSubs = (Vector) subsTable.get(from);

    // If the requester has subscriptions: 
    if (clientSubs != null) {
      // If it requests to remove all its subscriptions, removing them:
      if (subName == null) {
        subsTable.remove(from);
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "All subscriptions" 
                                        + " of client " + from + " removed.");
        return;
      }
      // Else, removing the identified subscription:
      else {
        int i = 0;
        SubscribeRequest sub;
        while (i < clientSubs.size()) {
          sub = (SubscribeRequest) clientSubs.get(i);
          if (subName.equals(sub.getName())) {
            clientSubs.remove(i);

            if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Subscription " 
                                            + subName + " removed.");

            // If no more subs are available for this requester, removing
            // its entry:
            if (clientSubs.isEmpty())
              subsTable.remove(from);

            return;
          }
          else
            i++;
        }
      }
    }
    throw new RequestException("Subscription [" + subName
                               + "] does not exist");
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
   * When a reader is removed, deleting this reader's subscriptions if any,
   * and sending <code>ExceptionReply</code> notifications to the client.
   */
  protected void doProcess(SetRightRequest not)
  {
    // If the request does not unset a reader, doing nothing.
    if (not.getRight() != -READ)
      return;

    AgentId user = not.getClient();

    if (! subsTable.containsKey(user))
      return;

    Vector subs = (Vector) subsTable.remove(user);

    SubscribeRequest sub;
    ExceptionReply reply;
    while (! subs.isEmpty()) {
      sub = (SubscribeRequest) subs.remove(0);
      reply = new ExceptionReply(sub.getConnectionKey(), sub.getName(),
                                 new AccessException("READ right removed"));
      Channel.sendTo(user, reply);
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
    forwardMessages(not.getMessages());
    
    // Processing the messages:
    processMessages(not.getMessages());
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
      subsTable.remove(agId);
      // Removing the father identifier, if needed.
      if (fatherId != null && agId.equals(fatherId))
        fatherId = null;
    }
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * <code>UnknownAgent</code> notifications are sent for each
   * subscription, and <code>UnclusterNot</code> notifications to the cluster
   * fellows.
   */
  protected void doProcess(DeleteNot not)
  {
    AgentId clientId;
    Vector subs;
    SubscribeRequest sub;

    // For each subscriber...
    Enumeration keys = subsTable.keys();
    while (keys.hasMoreElements()) {
      clientId = (AgentId) keys.nextElement();
      subs = (Vector) subsTable.remove(clientId);

      while (! subs.isEmpty()) {
        sub = (SubscribeRequest) subs.remove(0);
        Channel.sendTo(clientId, new UnknownAgent(destId, sub));
      }
    }
    subsTable = null;

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
  protected void forwardMessages(Vector messages)
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
   * valid subscriptions by sending <code>TopicMsgsReply</code> to the
   * valid subscribers.
   */
  protected void processMessages(Vector messages)
  {
    AgentId client;
    Vector clientSubs;
    TopicMsgsReply rep;
    Message msg;
    Vector subNames;
    SubscribeRequest sub;

    // For each client of the topic:
    Enumeration clients = subsTable.keys();
    while (clients.hasMoreElements()) {
      client = (AgentId) clients.nextElement();
      clientSubs = (Vector) subsTable.get(client);
      rep = new TopicMsgsReply();

      // For each message:
      for (int i = 0; i < messages.size(); i++) {
        try {
          msg = (Message) messages.get(i);
          subNames = new Vector();
          
          // For each client subscription: checking the message
          for (int j = 0; j < clientSubs.size(); j++) { 
            sub = (SubscribeRequest) clientSubs.get(j);

            // If selection works, adding the current subscription to the
            // vector of replied subscriptions:
            if (Selector.matches(msg, sub.getSelector()))
              subNames.add(sub.getName());
          }
          // If the current message replies to subscriptions, adding it in
          // the reply: 
          if (! subNames.isEmpty()) {
            rep.addMessage(msg, subNames);

           if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
             MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                           + msg.getIdentifier() + " added"
                                           + " for delivery to "
                                           + client.toString());
          }
        }
        // Invalid message class: going on.
        catch (ClassCastException cE) {}
      }
      // If the reply is not empty, sending it:
      if (! rep.isEmpty())
        Channel.sendTo(client, rep);
    }
  }
}
