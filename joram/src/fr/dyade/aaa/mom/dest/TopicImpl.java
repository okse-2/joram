/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.mom.selectors.*;

import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>TopicImpl</code> class provides the MOM topic behaviour,
 * basically distributing the received messages to subscribers.
 */
public class TopicImpl extends DestinationImpl
{
  /**
   * Table of subscriptions.
   * <p>
   * <b>Key:</b> Identifier of subscribing agents<br>
   * <b>Object:</b> Vector of <code>SubscribeRequest</code> instances
   */
  private Hashtable subsTable;
  /** Vector of cluster fellows. */
  private Vector friends;

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
    friends = new Vector();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this queue implementation. */
  public String toString()
  {
    return "TopicImpl:" + destId.toString();
  }


  /**
   * Distributes the requests to the appropriate topic reactions.
   * <p>
   * Accepted requests are:
   * <ul>
   * <li><code>ClientMessages</code> notifications,</li>
   * <li><code>SubscribeRequest</code> notifications,</li>
   * <li><code>UnsubscribeRequest</code> notifications,</li>
   * <li><code>ClusterRequest</code> notifications,</li>
   * <li><code>UnclusterRequest</code> notifications,</li>
   * <li><code>ClusterMessages</code> notifications.</li>
   * </ul>
   * <p>
   * An <code>ExceptionReply</code> notification is sent back in the case of an
   * error while processing a request.
   */
  public void doReact(AgentId from, AbstractRequest request)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + request.getClass().getName()
                                    + " with id: " + request.getRequestId()
                                    + " from: " + from.toString());
    try {
      if (request instanceof ClientMessages)
        doReact(from, (ClientMessages) request);
      else if (request instanceof SubscribeRequest)
        doReact(from, (SubscribeRequest) request);
      else if (request instanceof UnsubscribeRequest)
        doReact(from, (UnsubscribeRequest) request);
      else if (request instanceof ClusterRequest)
        doReact(from, (ClusterRequest) request);
      else if (request instanceof UnclusterRequest)
        doReact(from, (UnclusterRequest) request);
      else if (request instanceof ClusterMessages)
        doReact(from, (ClusterMessages) request);
      else
        super.doReact(from, (AbstractRequest) request);
    }
    catch (MomException mE) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, mE);

      ExceptionReply eR = new ExceptionReply(request, mE);
      Channel.sendTo(from, eR);
    }
  }
  
  /**
   * Method implementing the topic reaction to a <code>ClientMessages</code>
   * instance holding messages sent by a client agent.
   * <p>
   * The method may send <code>ClusterMessages</code> instances to the cluster's
   * topics, if any, and <code>TopicMsgsReply</code> instances to the valid
   * subscribers.
   *
   * @exception AccessException  If the sender is not a writer.
   */
  private void doReact(AgentId from, ClientMessages not) throws AccessException
  {
    // If sender is not a writer, throwing an exception:
    if (! super.isWriter(from))
      throw new AccessException("The needed WRITE right is not granted"
                                + " on topic " + destId);

    Vector messages = not.getMessages();
    if (messages.isEmpty())
      return;

    // Forwarding the messages to the cluster fellows, if any:
    AgentId topicId;
    for (int i = 0; i < friends.size(); i++) {
      topicId = (AgentId) friends.get(i);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Messages forwarded"
                                      + " to fellow topic " + topicId);

      Channel.sendTo(topicId, new ClusterMessages(from, not));
    }

    // Processing the messages:
    processMessages(from, messages);
  }

  /**
   * Method implementing the topic reaction to a <code>ClusterMessages</code>
   * instance holding messages sent by a topic part of the same cluster.
   * <p>
   * The method may send <code>TopicMsgsReply</code> notifications to the
   * valid subscribers.
   *
   * @exception AccessException  If the sender is not a WRITER.
   */
  private void doReact(AgentId from,
                       ClusterMessages not) throws AccessException
  {
    // If sender is not a writer, throwing an exception:
    if (! super.isWriter(from))
      throw new AccessException("The needed WRITE right is not granted"
                                + " on topic " + destId);

    processMessages(not.getFrom(), not.getMessages());
  }

  /**
   * Actually processes the distribution of the received messages to the
   * valid subscriptions by sending <code>TopicMsgsReply</code> to the
   * valid subscribers.
   */
  private void processMessages(AgentId from, Vector messages)
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

      // For each valid message:
      for (int i = 0; i < messages.size(); i++) {
        try {
          msg = (Message) messages.get(i);

          if (msg.isValid()) {
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
            if (! subNames.isEmpty())
              rep.addMessage(msg, subNames);
          }
        }
        catch (ClassCastException cE) {
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
            MomTracing.dbgDestination.log(BasicLevel.WARN, "Invalid message: "
                                          + cE);
        }
      }
      // If the reply is not empty, sending it:
      if (! rep.isEmpty()) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Vector of messages"
                                        + " sent to subscriber " + client);
        Channel.sendTo(client, rep);
      }
    }
  }

  /**
   * Method implementing the topic reaction to a <code>SubscribeRequest</code>
   * instance, requesting to set a new subscription.
   *
   * @exception AccessException  If the sender is not a READER.
   */
  private void doReact(AgentId from, SubscribeRequest not)
             throws AccessException
  {
    if (! super.isReader(from))
      throw new AccessException("The needed READ right is not granted"
                                + " on topic " + destId);

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
   * Method implementing the topic reaction to an
   * <code>UnsubscribeRequest</code> instance, requesting to remove one
   * or many client subscriptions.
   *
   * @exception RequestException  If the subscription to remove does not exist.
   */
  private void doReact(AgentId from,
                       UnsubscribeRequest not) throws RequestException
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
    throw new RequestException("Can't remove non existing subscription "
                               + subName);
  } 

  /**
   * Method implementing the topic reaction to a <code>ClusterRequest</code>
   * instance, requesting the topic to be part of a cluster.
   *
   * @exception AccessException  If the requester is not an ADMIN.
   * @exception RequestException  If the topic is not part of the new cluster,
   *              or if the topic is already part of a cluster.
   */  
  private void doReact(AgentId from, ClusterRequest not) throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("The needed ADMIN right is not granted"
                                + " on topic " + destId);
    if (! friends.isEmpty())
      throw new RequestException("Invalid cluster request as topic " + destId
                                 + " is already part of a cluster.");

    Vector topics = not.getTopics();
    int index = topics.indexOf(super.destId);

    if (index == -1)
      throw new RequestException("Invalid cluster request as this topic "
                                 + " is not part of the cluster to form.");

    AgentId topicId;

    // Adding each new friend to the list of fellows and granting them
    // the WRITE permission:
    for (int i = 0; i < topics.size(); i++) {
      topicId = (AgentId) topics.get(i);

      if (i != index) {
        friends.add(topicId);
        setUserRight(topicId, 2);
      }
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Cluster " + topics
                                    + " registered.");
  }

  /**
   * Method implementing the topic reaction to an
   * <code>UnclusterRequest</code> instance, cancelling the cluster the
   * topic is part of.
   *
   * @exception AccessException  If the requester is not an ADMIN.
   * @exception RequestException  If the topic is not part of a cluster.
   */  
  private void doReact(AgentId from, UnclusterRequest not) throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("The needed ADMIN right is not granted"
                                + " on topic " + destId);
    if (friends.isEmpty())
      throw new RequestException("Can't unclusterize this topic as it is "
                                 + "not part of a cluster.");

    AgentId topicId;

    // Removing the cluster fellows:
    while (! friends.isEmpty()) {
      topicId = (AgentId) friends.remove(0);
      setUserRight(topicId, -2);
    }
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Cluster quitted.");
  }

  /**
   * Method implementing the topic reaction to an <code>ExceptionReply</code>
   * sent by a fellow topic which actually failed to join the cluster, or 
   * which is deleted.
   * <p>
   * This method simply removes this topic from the vector of cluster's
   * fellows.
   */
  public void removeTopic(AgentId from)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified of a corrupted cluster"
                                    + " topic: " + from);
    AgentId tId;

    for (int i = 0; i < friends.size(); i++) {
      tId = (AgentId) friends.get(i);
      if (tId.equals(from)) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, "Topic removed"
                                        + " from the cluster list.");
        try {
          setUserRight(tId, -2);
        }
        catch (RequestException rE) {}
        friends.remove(i);
        break;
      }
    }
  }

  /**
   * Method implementing the topic reaction to a
   * <code>fr.dyade.aaa.agent.UnknownAgent</code> notification received
   * when trying to send messages to a deleted client.
   * <p>
   * This method simply removes the subscriptions of the deleted client.
   */
  public void removeDeadClient(UnknownAgent uA)
  {
    AgentId agId = uA.agent;
    Notification not = uA.not;

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified of a dead client: "
                                    + agId.toString());

    // Removing the deleted client's subscriptions.
    if (not instanceof TopicMsgsReply)
      subsTable.remove(agId);
  }

  /**
   * Method implementing the topic reaction to a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> notification requesting it
   * to be deleted.
   * <p>
   * The notification is ignored if the sender is not an admin of the topic.
   * Otherwise, <code>ExceptionReply</code> replies are sent to the
   * subscribers and the cluster fellows.
   */
  public void delete(AgentId from)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified to be deleted.");

    // If the requester is not an admin, ignoring the notification:
    if (! super.isAdministrator(from)) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, "Deletion request"
                                      + " sent by invalid agent: " + from);
      return;
    }

    // Building the exception to send to the subscribers: 
    DestinationException exc = new DestinationException("Topic " + destId
                                                        + " is deleted.");
    AgentId clientId;
    Vector subs;
    ExceptionReply excRep;

    // For each subscriber...
    Enumeration keys = subsTable.keys();
    while (keys.hasMoreElements()) {
      clientId = (AgentId) keys.nextElement();
      excRep = new ExceptionReply(exc);
      Channel.sendTo(clientId, excRep);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Client "
                                      + clientId.toString() + " notified"
                                      + " of the topic deletion.");
    }
    subsTable = null;

    AgentId topicId;
    // For each cluster fellow...
    while (! friends.isEmpty()) {
      topicId = (AgentId) friends.remove(0);
      excRep = new ExceptionReply(exc);
      Channel.sendTo(topicId, excRep);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Topic " + topicId
                                      + " notified of the topic deletion.");
    }
    deleted = true;
  }
}
