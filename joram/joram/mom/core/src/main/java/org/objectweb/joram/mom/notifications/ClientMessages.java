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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.AgentId;

/**
 * A <code>ClientMessages</code> instance is used by a client agent for
 * sending one or many messages to a destination.
 */
public class ClientMessages extends AbstractRequestNot {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Message sent by the client. */
  private Message message = null;
  /** Messages sent by the client. */
  private List messages = null;
  
  private boolean asyncSend;
  
  private AgentId proxyId;

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
  public ClientMessages(int clientContext, int requestId, List messages) {
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

  public AgentId getProxyId() {
    return proxyId;
  }

  public void setProxyId(AgentId proxyId) {
    this.proxyId = proxyId;
  }

  /** Adds a message to deliver. */
  public void addMessage(Message msg) {
    if (message == null && messages == null) {
      this.message = msg;
      this.setExpiration(message.expiration);
      this.setPriority(message.priority);
    } else {
      if (messages == null) {
        messages = new ArrayList();
        messages.add(message);
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
  public List getMessages() {
    if (messages == null) {
      ArrayList<Message> msgs = new ArrayList<Message>();
      if (message != null)
        msgs.add(message);
      return msgs;
    }
    return messages;
  }
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }

  public int getMessageCount() {
    if (messages == null) {
      if (message == null) return 0;
      return 1;
    }
    return messages.size();
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
