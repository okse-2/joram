/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package org.objectweb.joram.mom.dest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.Message;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * The <code>DestinationImpl</code> class implements the common behaviour of
 * MOM destinations.
 */
public abstract class DestinationImpl implements java.io.Serializable
{
  /**
   * <code>true</code> if the destination successfully processed a deletion
   * request.
   */
  private boolean deletable = false;

  /** Identifier of the destination's administrator. */
  protected AgentId adminId;
  /** Identifier of the agent hosting the destination. */
  protected AgentId destId;

  /** <code>true</code> if the READ access is granted to everybody. */
  protected boolean freeReading = false;
  /** <code>true</code> if the WRITE access is granted to everybody. */
  protected boolean freeWriting = false;
  /** Table of the destination readers and writers. */
  protected Hashtable clients;
  /**
   * Identifier of the dead message queue this destination must send its
   * dead messages to, if any.
   */
  protected AgentId dmqId = null;

  /** READ access value. */
  public static int READ = 1;
  /** WRITE access value. */
  public static int WRITE = 2;
  /** READ and WRITE access value. */
  public static int READWRITE = 3;


  /**
   * Constructs a <code>DestinationImpl</code>.
   *
   * @param destId  Identifier of the agent hosting the destination.
   * @param adminId  Identifier of the administrator of the destination.
   */ 
  public DestinationImpl(AgentId destId, AgentId adminId)
  {
    this.destId = destId;
    this.adminId = adminId;
    clients = new Hashtable();

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
      else if (not instanceof Monit_GetReaders)
        doReact(from, (Monit_GetReaders) not);
      else if (not instanceof Monit_GetWriters)
        doReact(from, (Monit_GetWriters) not);
      else if (not instanceof Monit_FreeAccess)
        doReact(from, (Monit_FreeAccess) not);
      else if (not instanceof Monit_GetDMQSettings)
        doReact(from, (Monit_GetDMQSettings) not);
      else if (not instanceof SpecialAdminRequest)
        doReact(from, (SpecialAdminRequest) not);
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
   */
  protected void doReact(AgentId from, SetRightRequest not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    AgentId user = not.getClient();
    int right = not.getRight();
    String info;

    try {
      processSetRight(user,right);
      specialProcess(not);
      info = "Request ["
             + not.getClass().getName()
             + "], sent to Destination ["
             + destId
             + "], successful [true]: user ["
             + user
             + "] set with right [" + right +"]";
      Channel.sendTo(from, new AdminReply(not, true, info)); 
    }
    catch (RequestException exc) {
      info = "Request ["
             + not.getClass().getName()
             + "], sent to Destination ["
             + destId
             + "], successful [false]: "
             + exc.getMessage();
      Channel.sendTo(from, new AdminReply(not, false, info));
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /** set user right. */
  protected void processSetRight(AgentId user, int right) 
    throws RequestException {
    // Setting "all" users rights:
    if (user == null) {
      if (right == READ)
        freeReading = true;
      else if (right == WRITE)
        freeWriting = true;
      else if (right == -READ)
        freeReading = false;
      else if (right == -WRITE)
        freeWriting = false;
      else
        throw new RequestException("Invalid right value: " + right);
    }
    // Setting a specific user right:
    else {
      Integer currentRight = (Integer) clients.get(user);
      if (right == READ) {
        if (currentRight != null && currentRight.intValue() == WRITE)
          clients.put(user, new Integer(READWRITE));
        else
          clients.put(user, new Integer(READ));
      }
      else if (right == WRITE) {
        if (currentRight != null && currentRight.intValue() == READ)
          clients.put(user, new Integer(READWRITE));
        else
          clients.put(user, new Integer(WRITE));
      }
      else if (right == -READ) {
        if (currentRight != null && currentRight.intValue() == READWRITE)
          clients.put(user, new Integer(WRITE));
        else if (currentRight != null && currentRight.intValue() == READ)
          clients.remove(user);
      }
      else if (right == -WRITE) {
        if (currentRight != null && currentRight.intValue() == READWRITE)
          clients.put(user, new Integer(READ));
        else if (currentRight != null && currentRight.intValue() == WRITE)
          clients.remove(user);
      }
      else
        throw new RequestException("Invalid right value: " + right);
    }
  }
    
  /**
   * Method implementing the reaction to a <code>SetDMQRequest</code>
   * notification setting the dead message queue identifier for this
   * destination.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, SetDMQRequest not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    dmqId = not.getDmqId();
    
    String info = "Request ["
                  + not.getClass().getName()
                  + "], sent to Destination ["
                  + destId
                  + "], successful [true]: dmq ["
                  + dmqId
                  + "] set" ;
    Channel.sendTo(from, new AdminReply(not, true, info));
    
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetReaders</code>
   * notification requesting the identifiers of the destination's readers.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetReaders not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    AgentId key;
    int right;
    Vector readers = new Vector();
    for (Enumeration keys = clients.keys(); keys.hasMoreElements();) {
      key = (AgentId) keys.nextElement();
      right = ((Integer) clients.get(key)).intValue();

      if (right == READ || right == READWRITE)
        readers.add(key);
    }
    Channel.sendTo(from, new Monit_GetUsersRep(not, readers));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetWriters</code>
   * notification requesting the identifiers of the destination's writers.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetWriters not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    AgentId key;
    int right;
    Vector writers = new Vector();
    for (Enumeration keys = clients.keys(); keys.hasMoreElements();) {
      key = (AgentId) keys.nextElement();
      right = ((Integer) clients.get(key)).intValue();

      if (right == WRITE || right == READWRITE)
        writers.add(key);
    }
    Channel.sendTo(from, new Monit_GetUsersRep(not, writers));
  }

  /**
   * Method implementing the reaction to a <code>Monit_FreeAccess</code>
   * notification requesting the free access status of this destination.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_FreeAccess not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Channel.sendTo(from,
                   new Monit_FreeAccessRep(not, freeReading, freeWriting));
  }

  /**
   * Method implementing the reaction to a <code>Monit_GetDMQSettings</code>
   * notification requesting the destination's DMQ settings.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetDMQSettings not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String id = null;
    if (dmqId != null)
      id = dmqId.toString();

    Channel.sendTo(from, new Monit_GetDMQSettingsRep(not, id, null));
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
    // If sender is not a writer, sending the messages to the DMQ, and
    // throwing an exception:
    if (! isWriter(from)) {
      ClientMessages deadM;
      deadM = new ClientMessages(not.getClientContext(), not.getRequestId());

      Message msg;
      for (Enumeration msgs = not.getMessages().elements();
           msgs.hasMoreElements();) {
        msg = (Message) msgs.nextElement();
        msg.notWriteable = true;
        deadM.addMessage(msg);
      }
      sendToDMQ(deadM, not.getDMQId());
      throw new AccessException("WRITE right not granted");
    }
    specialProcess(not);
  }

  /**
   * Method implementing the reaction to an <code>UnknownAgent</code>
   * notification.
   * <p>
   * If the unknown agent is the DMQ, its identifier is set to null. If it
   * is a client of the destination, it is removed. Specific processing is
   * also done in subclasses.
   */
  protected void doReact(AgentId from, UnknownAgent not)
  {
    if (not.agent.equals(adminId)) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                          "Admin of dest "
                                          + destId
                                          + " does not exist anymore.");
    }
    else if (not.agent.equals(dmqId))
      dmqId = null;
    else {
      clients.remove(from);
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
   * Method implementing the reaction to a <code>SpecialAdminRequest</code>
   * notification requesting the special administration of the destination.
   * <p>
   */
  protected void doReact(AgentId from, SpecialAdminRequest not) {
    String info;
    Object obj = null;
    try {
      if (!isAdministrator(from)) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, 
                                        "SpecialAdminRequest request" +
                                        " received from non administrator" +
                                        " client " + from);
        throw new RequestException("ADMIN right not granted");
      }
      obj = specialAdminProcess(not);
      info = "Request ["
        + not.getClass().getName()
        + "], sent to Destination ["
        + destId
        + "], successful [true] ";
      Channel.sendTo(from, 
                     new AdminReply(not, true, info, obj)); 
    } catch (RequestException exc) {
      info = "Request ["
        + not.getClass().getName()
        + "], sent to Destination ["
        + destId
        + "], successful [false]: "
        + exc.getMessage();
      Channel.sendTo(from, 
                     new AdminReply(not, false, info, obj));
    }
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }
  
  protected Object specialAdminProcess(SpecialAdminRequest not) 
    throws RequestException {
    return null;
  }

  /**
   * Checks the reading permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a reading permission.
   */
  protected boolean isReader(AgentId client)
  {
    if (isAdministrator(client) || freeReading)
      return true;

    Integer clientRight = (Integer) clients.get(client);
    if (clientRight == null)
      return false;
    else
      return clientRight.intValue() == READ
             || clientRight.intValue() == READWRITE;
  }

  /**
   * Checks the writing permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a writing permission.
   */
  protected boolean isWriter(AgentId client)
  {
    if (isAdministrator(client) || freeWriting)
      return true;

    Integer clientRight = (Integer) clients.get(client);
    if (clientRight == null)
      return false;
    else
      return clientRight.intValue() == WRITE
             || clientRight.intValue() == READWRITE;
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
   * @param deadMessages  The dead messages.
   * @param dmqId  Identifier of the dead message queue to use,
   *          <code>null</code> if not provided.
   */
  protected void sendToDMQ(ClientMessages deadMessages, AgentId dmqId)
  {
    // Sending the dead messages to the provided DMQ:
    if (dmqId != null)
      Channel.sendTo(dmqId, deadMessages);
    // Sending the dead messages to the destination's DMQ:
    else if (this.dmqId != null)
      Channel.sendTo(this.dmqId, deadMessages);
    // Sending the dead messages to the server's default DMQ:
    else if (DeadMQueueImpl.id != null) {
      Channel.sendTo(DeadMQueueImpl.id, deadMessages);
    }
  }

  /**
   * Abstract method to be implemented by subclasses for specifically
   * processing notifications.
   */
  protected abstract void specialProcess(Notification not);
}
