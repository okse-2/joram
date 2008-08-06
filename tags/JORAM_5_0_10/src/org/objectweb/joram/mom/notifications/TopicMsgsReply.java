/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import java.util.Iterator;
import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.AgentId;

/**
 * A <code>TopicMsgsReply</code> instance is used by a topic for sending
 * messages to an agent client which subscribed to it.
 */
public class TopicMsgsReply extends AbstractReply {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Vector of messages. */
  private Vector messages;

  /**
   * Constructs a <code>TopicMsgsReply</code>.
   *
   * @param messages  Vector of delivered messages.
   */
  public TopicMsgsReply(Vector messages) {
    long newExpiration = -1L;
    int newPriority = 0;
    for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
      Message msg = (Message) iterator.next();
      if (newExpiration != 0L && (msg.expiration > newExpiration || msg.expiration == 0L)) {
        newExpiration = msg.expiration;
      }
      if (msg.priority > newPriority) {
        newPriority = msg.priority;
      }
    }
    this.messages = messages;
    this.setExpiration(newExpiration);
    this.setPriority(newPriority);
  }


  /** Returns the messages. */
  public Vector getMessages() {
    return messages;
  }

  /**
   * Sets the identifier of the producer's dead message queue. Basically, it
   * simply calls {@link #setDeadNotificationAgentId(AgentId)}
   */
  public void setDMQId(AgentId dmqId) {
    setDeadNotificationAgentId(dmqId);
  }
} 
