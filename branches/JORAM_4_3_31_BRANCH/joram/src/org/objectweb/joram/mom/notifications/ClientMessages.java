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
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.AgentId;
import org.objectweb.joram.shared.messages.Message;

import java.util.Vector;

/**
 * A <code>ClientMessages</code> instance is used by a client agent for
 * sending one or many messages to a destination.
 */
public class ClientMessages extends AbstractRequest
{
  /** Message sent by the client. */
  private Message message = null;
  /** Messages sent by the client. */
  private Vector messages = null;
  /** Identifier of the producer's dead message queue, if any. */
  private AgentId producerDMQId = null;
  
  private boolean asyncSend;


  /**
   * Constructs a <code>ClientMessages</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   */
  public ClientMessages(int clientContext, int requestId)
  {
    super(clientContext, requestId);
  }

  /**
   * Constructs a <code>ClientMessages</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param messages  Vector of messages.
   */
  public ClientMessages(int clientContext, int requestId, Vector messages)
  {
    super(clientContext, requestId);
    if (messages.size() == 1)
      message = (Message) messages.get(0);
    else
      this.messages = messages;
  }

  /**
   * Constructs a <code>ClientMessages</code> instance.
   */
  public ClientMessages()
  {} 


  /** Adds a message to deliver. */
  public void addMessage(Message msg)
  {
    if (message == null && messages == null)
      message = msg;
    else {
      if (messages == null) {
        messages = new Vector();
        messages.add(message);
        message = null;
      }
      messages.add(msg);
    }
  }

  /** Sets the identifier of the producer's dead message queue, if any. */
  public void setDMQId(AgentId dmqId)
  {
    producerDMQId = dmqId;
  }

  
  /** Returns the messages. */
  public Vector getMessages()
  {
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

  /** Returns the identifier of the producer's dead message queue, if any. */
  public AgentId getDMQId()
  {
    return producerDMQId;
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
    output.append(",producerDMQId=").append(producerDMQId);
    output.append(",asyncSend=").append(asyncSend);
    output.append(')');

    return output;
  }
} 
