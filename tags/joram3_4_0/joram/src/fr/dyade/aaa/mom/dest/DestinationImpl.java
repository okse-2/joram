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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>DestinationImpl</code> class implements the common behaviour of
 * MOM destinations.
 */
public abstract class DestinationImpl implements java.io.Serializable
{
  /** Identifier of the destination's administrator. */
  private AgentId adminId;

  /**
   * <code>true</code> if the destination successfully processed a deletion
   * request.
   */
  private boolean deletable = false;

  /** Identifier of the agent hosting this DestinationImpl. */
  protected AgentId destId;

  /** <code>true</code> if the READ access is granted to everybody. */
  protected boolean freeReading = false;
  /** <code>true</code> if the WRITE access is granted to everybody. */
  protected boolean freeWriting = false;
  /** Vector of the destination readers ids. */
  protected Vector readers;
  /** Vector of the destination writers ids. */
  protected Vector writers;

  /**
   * Identifier of the dead message queue this destination must send its
   * dead messages to, if any.
   */
  protected AgentId dmqId = null;

  /** READ access value. */
  public static int READ = 1;
  /** WRITE access value. */
  public static int WRITE = 2;


  /**
   * Constructs a <code>DestinationImpl</code>.
   *
   * @param destId  Identifier of the agent hosting the DestinationImpl.
   * @param adminId  Identifier of the administrator of this destination.
   */ 
  public DestinationImpl(AgentId destId, AgentId adminId) 
  {
    this.destId = destId;
    this.adminId = adminId;

    readers = new Vector();
    writers = new Vector();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, this + ": created.");
  }

  
  /** Returns <code>true</code> if the destination might be deleted. */
  public boolean canBeDeleted()
  {
    return deletable;
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  If a received notification is
   *              unexpected by the destination.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    try {
      if (not instanceof SetRightRequest)
        doReact(from, (SetRightRequest) not);
      else if (not instanceof SetDMQRequest)
        doReact(from, (SetDMQRequest) not);
      else if (not instanceof ClientMessages)
        doReact(from, (ClientMessages) not);
      else if (not instanceof UnknownAgent)
        doReact(from, (UnknownAgent) not);
      else if (not instanceof DeleteNot)
        doReact(from, (DeleteNot) not);
      else
        throw new UnknownNotificationException(not.getClass().getName());
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
   * Method implementing the reaction to a <code>SetRightRequest</code>
   * notification requesting rights to be set for a user.
   *
   * @exception AccessException  If the requester is not the administrator.
   * @exception RequestException If the right requested is invalid.
   */
  protected void doReact(AgentId from, SetRightRequest not) throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    AgentId user = not.getClient();
    int right = not.getRight();

    // Setting "all" users rights:
    if (user == null) {
      if (right == READ)
        freeReading = true;
      else if (right == WRITE)
        freeWriting = true;
      else if (right == -READ) {
        freeReading = false;
        specialProcess(not);
      }
      else if (right == -WRITE)
        freeWriting = false;
      else
        throw new RequestException("Invalid right value: " + right);
    }
    // Setting a specific user right:
    else {
      if (right == READ) {
        if (! readers.contains(user))
          readers.add(user);
      }
      else if (right == WRITE) {
        if (! writers.contains(user))
          writers.add(user);
      }
      else if (right == -READ) {
        readers.remove(user);
        specialProcess(not);
      }
      else if (right == -WRITE)
        writers.remove(user);
      else
        throw new RequestException("Invalid right value: " + right);
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "User " + user 
                                    + " right set to " + right);
  }

  /**
   * Method implementing the reaction to a <code>SetDMQRequest</code>
   * notification setting the dead message queue identifier for this
   * destination.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, SetDMQRequest not)
                 throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    dmqId = not.getDmqId();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "DMQ id set to "
                                    + dmqId);
  }

  /**
   * Method implementing the reaction to a <code>ClientMessages</code>
   * notification holding messages sent by a client.
   * <p>
   * If the sender is not a writer on the destination the messages are
   * sent to the DMQ and an exception is thrown. Otherwise, the processing of
   * the received messages is performed in subclasses.
   *
   * @exception AccessException  If the sender is not a WRITER on the
   *              destination.
   */
  protected void doReact(AgentId from, ClientMessages not)
                 throws AccessException
  {
    Vector messages = not.getMessages();

    // If sender is not a writer, sending the messages to the DMQ, and
    // throwing an exception:
    if (! isWriter(from)) {
      for (int i = 0; i < messages.size(); i++) {
        try {
          ((Message) messages.get(i)).notWritable = true;
        }
        // Invalid message class: removing it.
        catch (ClassCastException cE) {
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR, "Invalid message"
                                          + " class: " + cE);
          messages.remove(i);
          i--;
        }
      }
      sendToDMQ(messages, not.getDMQId());
      throw new AccessException("WRITE right not granted");
    }
    specialProcess(not);
  }

  /**
   * Method implementing the reaction to an <code>UnknownAgent</code>
   * notification.
   * <p>
   * If the unknown agent is a DMQ, its identifier is set to null. If it
   * is a client of the destination, it is removed. Specific processing is
   * also done in subclasses.
   */
  protected void doReact(AgentId from, UnknownAgent not)
  {
    if (not.agent.equals(adminId)) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR, "Admin of dest "
                                          + destId
                                          + " does not exist anymore.");
    }
    else if (not.agent.equals(dmqId))
      dmqId = null;
    else if (not.agent.equals(DeadMQueueImpl.id))
      DeadMQueueImpl.id = null;
    else {
      readers.remove(not.agent);
      writers.remove(not.agent);
      specialProcess(not);
    }
  }

  /**
   * Method implementing the reaction to a <code>DeleteNot</code>
   * notification requesting the deletion of the destination.
   * <p>
   * The processing is done in subclasses if the sender is an administrator.
   */
  protected void doReact(AgentId from, DeleteNot not)
  {
    if (! isAdministrator(from)) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, "Deletion request"
                                      + " received from non administrator"
                                      + " client " + from);
    }
    else {
      specialProcess(not);
      deletable = true;
    }
  }

  
  /**
   * Checks the reading permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a reading permission.
   */
  protected boolean isReader(AgentId client)
  {
    return isAdministrator(client) || freeReading || readers.contains(client);
  }

  /**
   * Checks the writing permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a writing permission.
   */
  protected boolean isWriter(AgentId client)
  {
    return isAdministrator(client) || freeWriting || writers.contains(client);
  }

  /**
   * Checks the administering permission of a given client agent.
   *
   * @param client  AgentId of the client requesting an admin permission.
   */
  protected boolean isAdministrator(AgentId client)
  {
    return client.equals(adminId);
  }

  /**
   * Sends dead messages to the appropriate dead message queue.
   *
   * @param deadMessages  The vector of dead messages.
   * @param dmqId  Identifier of the dead message queue to use,
   *          <code>null</code> if not provided.
   */
  protected void sendToDMQ(Vector deadMessages, AgentId dmqId)
  {
    // Sending the dead messages to the provided DMQ:
    if (dmqId != null)
      Channel.sendTo(dmqId, new ClientMessages(null, deadMessages));
    // Sending the dead messages to the destination's DMQ:
    else if (this.dmqId != null)
      Channel.sendTo(this.dmqId, new ClientMessages(null, deadMessages));
    // Sending the dead messages to the server's default DMQ:
    else if (DeadMQueueImpl.id != null) {
      Channel.sendTo(DeadMQueueImpl.id,
                     new ClientMessages(null, deadMessages));
    }
  }

  /**
   * Abstract method to be implemented by subclasses for specifically
   * processing notifications.
   */
  protected abstract void specialProcess(Notification not);
}
