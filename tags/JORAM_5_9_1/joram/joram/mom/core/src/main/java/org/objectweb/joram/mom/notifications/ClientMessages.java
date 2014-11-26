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

import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.Encoder;

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
  private List<Message> messages = null;
  
  private boolean asyncSend;
  
  private AgentId proxyId;
  
  @Override
  public int getEncodableClassId() {
    return JoramHelper.CLIENT_MESSAGES_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int res = super.getEncodedSize() ;
    
    res += BOOLEAN_ENCODED_SIZE;
    if (message != null) {
      res += message.getEncodedSize();
    }
    
    res += BOOLEAN_ENCODED_SIZE;
    if (messages != null) {
      res += INT_ENCODED_SIZE;
      for (Message msg : messages) {
        res += msg.getEncodedSize();
      }
    }
    
    res += BOOLEAN_ENCODED_SIZE;
    
    res += BOOLEAN_ENCODED_SIZE;
    if (proxyId != null) {
      res += proxyId.getEncodedSize();
    }
    return res;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    
    if (message != null) {
      encoder.encodeBoolean(true);
      message.encode(encoder);
    } else {
      encoder.encodeBoolean(false);
    }
    
    if (messages != null) {
      encoder.encodeBoolean(true);
      encoder.encodeUnsignedInt(messages.size());
      for (Message msg : messages) {
        msg.encode(encoder);
      }
    } else {
      encoder.encodeBoolean(false);
    }
    
    encoder.encodeBoolean(asyncSend);
    
    if (proxyId == null) {
      encoder.encodeBoolean(false);
    } else {
      encoder.encodeBoolean(true);
      proxyId.encode(encoder);
    }
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
   
    boolean flag = decoder.decodeBoolean();
    if (flag) {
      message = new Message();
      message.decode(decoder);
    }
    
    flag = decoder.decodeBoolean();
    if (flag) {
      int size = decoder.decodeUnsignedInt();
      messages = new ArrayList<Message>(size);
      for (int i = 0; i < size; i++) {
        Message msg = new Message();
        msg.decode(decoder);
        messages.add(msg);
      }
    }
    
    asyncSend = decoder.decodeBoolean();
    
    flag = decoder.decodeBoolean();
    if (flag) {
      proxyId = new AgentId((short) 0, (short) 0, 0);
      proxyId.decode(decoder);
    }
  }

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
    output.append(",proxyId=").append(proxyId);
    output.append(')');

    return output;
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new ClientMessages();
    }

  }
  
} 
