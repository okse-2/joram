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
 * A <code>ClientMessages</code> instance is used by a client agent for
 * sending one or many messages to a destination.
 */
public class ClientMessages extends AbstractRequest {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Message sent by the client. */
  private Message message = null;
  /** Messages sent by the client. */
  private Vector messages = null;
  
  private boolean asyncSend;

  /**
   * Constructs a <code>ClientMessages</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   */
  public ClientMessages(int clientContext, int requestId) {
    super(clientContext, requestId);
  }

  /**
   * Constructs a <code>ClientMessages</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param message  The message.
   */
  public ClientMessages(int clientContext, int requestId, Message message) {
    super(clientContext, requestId);
    this.message = message;
    this.setExpiration(message.expiration);
    this.setPriority(message.priority);
  }

  /**
   * Constructs a <code>ClientMessages</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param messages  Vector of messages.
   */
  public ClientMessages(int clientContext, int requestId, Vector messages) {
    super(clientContext, requestId);
    if (messages.size() == 1) {
      this.message = (Message) messages.get(0);
      this.setExpiration(message.expiration);
      this.setPriority(message.priority);
    } else {
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
  }

  /**
   * Constructs a <code>ClientMessages</code> instance.
   */
  public ClientMessages() {} 


  /** Adds a message to deliver. */
  public void addMessage(Message msg) {
    if (message == null && messages == null) {
      this.message = msg;
      this.setExpiration(message.expiration);
      this.setPriority(message.priority);
    } else {
      if (messages == null) {
        messages = new Vector();
        messages.add(this.message);
        message = null;
      }
      if (this.getExpiration() != 0L && (msg.expiration > this.getExpiration() || msg.expiration == 0L)) {
        this.setExpiration(msg.expiration);
      }
      if (msg.priority > this.getPriority()) {
        this.setPriority(msg.priority);
      }
      messages.add(msg);
    }
  }

  /**
   * Sets the identifier of the producer's dead message queue. Basically, it
   * simply calls {@link #setDeadNotificationAgentId(AgentId)}
   */
  public void setDMQId(AgentId dmqId) {
    setDeadNotificationAgentId(dmqId);
  }

  
  /** Returns the messages. */
  public Vector getMessages() {
    if (messages == null) {
      messages = new Vector();
      if (message != null)
        messages.add(message);
    }
    return messages;
  }
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }

  /**
   * Returns the identifier of the producer's dead message queue, if any.
   * Basically, it simply calls {@link #getDeadNotificationAgentId()}
   */
  public AgentId getDMQId() {
    return getDeadNotificationAgentId();
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",message=").append(message);
    output.append(",messages=").append(messages);
    output.append(",asyncSend=").append(asyncSend);
    output.append(')');

    return output;
  }
} 
