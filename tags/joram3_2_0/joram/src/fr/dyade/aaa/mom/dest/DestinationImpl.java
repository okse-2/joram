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

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>DestinationImpl</code> class provides the common behaviour of
 * MOM destinations, mainly for controlling access and administering.
 */
public abstract class DestinationImpl implements java.io.Serializable
{
  /** Vector of the destination administrators ids. */
  private Vector admins;
  /** Vector of the destination readers ids. */
  private Vector readers;
  /** Vector of the destination writers ids. */
  private Vector writers;
  /** <code>true</code> if the READ access is granted to everybody. */
  private  boolean freeReading = false;
  /** <code>true</code> if the WRITE access is granted to everybody. */
  private  boolean freeWriting = false;
  /** <code>true</code> if the ADMIN access is granted to everybody. */
  private  boolean freeAdmin = false;

  /** Counter of received messages. */
  private long messagesCounter = 0;

  /**
   * <code>AgentId</code> identifier of the agent representing the
   * destination.
   */
  protected AgentId destId;
  /**
   * Identifier of the dead message queue attributed to this destination, if
   * any.
   */
  protected AgentId dmqId = null;
  /** <code>true</code> if the destination processed a deletion request. */
  protected boolean deleted = false;

  /** READ access value. */
  public static int READ = 1;
  /** WRITE access value. */
  public static int WRITE = 2;
  /** ADMIN access value. */
  public static int ADMIN = 3;


  /**
   * Constructs a <code>DestinationImpl</code>.
   *
   * @param destId  Identifier of the agent representing the MOM destination.
   * @param adminId  Identifier of the agent creating the destination, and
   *          which is its default administrator.
   */ 
  public DestinationImpl(AgentId destId, AgentId adminId) 
  {
    this.destId = destId;

    admins = new Vector();
    readers = new Vector();
    writers = new Vector();

    admins.add(adminId);
    readers.add(adminId);
    writers.add(adminId);
  }

  /**
   * Distributes the requests to the appropriate destination methods.
   * <p>
   * The accepted request is:
   * <ul>
   * <li><code>SetRightRequest</code> notifications.</li>
   * <li><code>SetRightRequest</code> notifications,</li>
   * <li><code>SetDMQRequest</code> notifications,</li>
   * </ul>
   * <p>
   * An <code>ExceptionReply</code> notification is sent back in the case of
   * an error when processing a request.
   */
  public void doReact(AgentId from, AbstractRequest req)
  {
    try {
      if (req instanceof SetRightRequest)
        doReact(from, (SetRightRequest) req);
      else if (req instanceof SetDMQRequest)
        doReact(from, (SetDMQRequest) req);
      else
        throw new RequestException("Unexpected request!");
    }
    catch (MomException mE) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, mE);

      ExceptionReply eR = new ExceptionReply(req, mE);
      Channel.sendTo(from, eR);
    }
  }

  /**
   * Method implementing the destination reaction to a
   * <code>SetRightRequest</code> instance requesting rights to be set for
   * a user.
   *
   * @exception AccessException  If the requester is not an administrator.
   * @exception RequestException  If the right value is invalid.
   */
  private void doReact(AgentId from, SetRightRequest not) throws MomException
  {
    if (! isAdministrator(from))
      throw new AccessException("The needed ADMIN right is not granted"
                                + " on dest " + destId);

    AgentId client = not.getClient();
    setUserRight(client, not.getRight());

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "User "
                                    + client + " right " + not.getRight()
                                    + " has been set.");
  }

  /**
   * Method implementing the destination reaction to a
   * <code>SetDMQRequest</code> instance setting the dead message queue
   * identifier for this destination.
   *
   * @exception AccessException  If the requester is not an administrator.
   */
  private void doReact(AgentId from, SetDMQRequest not) throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("The needed ADMIN right is not granted"
                                + " on dest " + destId);

    dmqId = not.getDmqId();
  }

  /**
   * Sets a given user's right.
   *
   * @param user  User which right is to be set, <code>null</code> for "all".
   * @param right  Right to set.
   * @exception RequestException  In case of an incorrect right value.
   */
  protected void setUserRight(AgentId user, int right) throws RequestException
  {
    // Setting "all" users rights:
    if (user == null) {
      if (right == READ)
        freeReading = true;
      else if (right == WRITE)
        freeWriting = true;
      else if (right == ADMIN) {
        freeReading = true;
        freeWriting = true;
        freeAdmin = true;
      }
      else if (right == -READ)
        freeReading = false;
      else if (right == -WRITE)
        freeWriting = false;
      else if (right == 0 || right == -ADMIN) {
        freeReading = false;
        freeWriting = false;
        freeAdmin = false;
      }
      else
        throw new RequestException("Incorrect right value: " + right);
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
      else if (right == ADMIN) {
        if (! admins.contains(user)) {
          admins.add(user);
          if (! readers.contains(user))
            readers.add(user);
          if (! writers.contains(user))
            writers.add(user);
        }
      }
      else if (right == -READ) 
        readers.remove(user);
      else if (right == -WRITE)
        writers.remove(user);
      else if (right == 0 || right == -ADMIN) {
        readers.remove(user);
        writers.remove(user);
        admins.remove(user);
      }
      else
        throw new RequestException("Incorrect right value: " + right);
    }
  }

  /**
   * Checks the reading permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a reading permission.
   */
  protected boolean isReader(AgentId client)
  {
    if (freeReading || readers.contains(client))
      return true;
    return false;
  }

  /**
   * Checks the writing permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a writing permission.
   */
  protected boolean isWriter(AgentId client)
  {
    if (freeWriting || writers.contains(client))
      return true;
    return false;
  }

  /**
   * Checks the administering permission of a given client agent.
   *
   * @param client  AgentId of the client requesting an admin permission.
   */
  protected boolean isAdministrator(AgentId client)
  {
    if (freeAdmin || admins.contains(client))
      return true;
    return false;
  }

  /** Returns the next message count value. */
  protected long nextMessageCount()
  {
    if (messagesCounter == Long.MAX_VALUE)
      messagesCounter = 0;
 
    return messagesCounter++;
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
    if (dmqId != null)
      Channel.sendTo(dmqId, new ClientMessages(null, deadMessages));
    else if (this.dmqId != null)
      Channel.sendTo(this.dmqId, new ClientMessages(null, deadMessages));
    else if (DeadMQueueImpl.id != null) {
      Channel.sendTo(DeadMQueueImpl.id,
                     new ClientMessages(null, deadMessages));
    }
  }

  /**
   * Returns <code>true</code> if the destination processed a deletion 
   * request. 
   */
  public boolean canBeDeleted()
  {
    return deleted;
  }
}
