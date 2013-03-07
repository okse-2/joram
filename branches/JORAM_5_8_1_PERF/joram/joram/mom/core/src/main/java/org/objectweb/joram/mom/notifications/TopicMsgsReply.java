/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
import java.util.List;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.CallbackNotification;

/**
 * A <code>TopicMsgsReply</code> instance is used by a topic for sending
 * messages to an agent client which subscribed to it.
 */
// JORAM_PERF_BRANCH
public class TopicMsgsReply extends AbstractReplyNot implements CallbackNotification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** Vector of messages. */
  private List messages;

  /**
   * Constructs a <code>TopicMsgsReply</code>.
   *
   * @param messages  List of delivered messages.
   */
  public TopicMsgsReply(List messages) {
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
  public List getMessages() {
    return messages;
  }

  /**
   * Sets the identifier of the producer's dead message queue. Basically, it
   * simply calls {@link #setDeadNotificationAgentId(AgentId)}
   */
  public void setDMQId(AgentId dmqId) {
    setDeadNotificationAgentId(dmqId);
  }
  
  //JORAM_PERF_BRANCH
  private Runnable callback;

  public Runnable getCallback() {
    return callback;
  }

  public void setCallback(Runnable callback) {
    this.callback = callback;
  }
  
} 
