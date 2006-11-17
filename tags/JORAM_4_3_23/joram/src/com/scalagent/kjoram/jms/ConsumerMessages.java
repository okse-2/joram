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
package com.scalagent.kjoram.jms;

import com.scalagent.kjoram.messages.Message;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A <code>ConsumerMessages</code> is used by a JMS proxy for sending messages
 * to a consumer.
 */
public class ConsumerMessages extends AbstractJmsReply
{
  /** Wrapped message. */
  private Message message = null;
  /** Wrapped messages. */
  private Vector messages = null;
  /** Name of the subscription or the queue the messages come from. */
  private String comingFrom = null;
  /** <code>true</code> if the messages come from a queue. */
  private boolean queueMode;


  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param message  Message to wrap.
   * @param comingFrom  Name of the queue or the subscription the message
   *          come from.
   * @param queueMode  <code>true</code> if the message come from a queue.
   */
  public ConsumerMessages(int correlationId, Message message,
                          String comingFrom, boolean queueMode)
  {
    super(correlationId);
    this.message = message;
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
    if (message != null) {
      messages = new Vector();
      messages.addElement(message);
    }
  }

  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param messages  Messages to wrap.
   * @param comingFrom  Name of the queue or the subscription the messages
   *          comes from.
   * @param queueMode  <code>true</code> if the messages come from a queue.
   */
  public ConsumerMessages(int correlationId, Vector messages,
                          String comingFrom, boolean queueMode)
  {
    super(correlationId);
    this.messages = messages;
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
  }

  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param comingFrom  Name of the queue or the subscription the reply
   *          comes from.
   * @param queueMode  <code>true</code> if it replies to a queue consumer.
   */
  public ConsumerMessages(int correlationId, String comingFrom,
                          boolean queueMode)
  {
    super(correlationId);
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
    messages = new Vector();
  }

  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   */
  public ConsumerMessages() {
    messages = new Vector();
  }

  /** Returns the messages to deliver. */
  public Vector getMessages()
  {
    if (messages == null) {
      messages = new Vector();
      if (message != null)
        messages.addElement(message);
    }
    return messages;
  }

  /**
   * Returns the name of the queue or the subscription the messages come
   * from.
   */
  public String comesFrom()
  {
    return comingFrom;
  }

  public void setComesFrom(String comingFrom) {
    this.comingFrom = comingFrom;
  }

  /** Returns <code>true</code> if the messages come from a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  } 

  /** Returns the first sent message. */
  public Message getMessage()
  {
    if (messages == null || messages.isEmpty())
      return null;

    return (Message) messages.elementAt(0);
  }

  public void addMessage(Message msg) {
    messages.addElement(msg);
  }

  public void setMessage(Message msg) {
    message = msg;
  }

  /**
   * Transforms this reply into a hashtable of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (comingFrom != null)
      h.put("comingFrom",comingFrom);
    h.put("queueMode",new Boolean(queueMode));
    // Coding and adding the messages into a array:
    int size = 0;
    if (messages != null)
      size = messages.size();
    if (size > 0) {
      Vector arrayMsg = new Vector();
      for (int i = 0; i<size; i++) {
        Message msg = (Message) messages.elementAt(0);
        messages.removeElementAt(0);
        arrayMsg.insertElementAt(msg.soapCode(),i);
      }
      if (arrayMsg != null)
        h.put("arrayMsg",arrayMsg);
    } else {
      if (message != null) {
        h.put("singleMsg",message.soapCode());
      }
    }
    return h;
  }

  /** 
   * Transforms a hashtable of primitive values into a
   * <code>ConsumerMessages</code> reply.
   */
  public static Object soapDecode(Hashtable h) {
    ConsumerMessages req = new ConsumerMessages();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    req.setComesFrom((String) h.get("comingFrom"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    Vector arrayMsg = (Vector) h.get("arrayMsg");
    if (arrayMsg != null) {
      for (int i = 0; i<arrayMsg.size(); i++)
        req.addMessage(Message.soapDecode((Hashtable) arrayMsg.elementAt(i)));
    } else
      req.setMessage(Message.soapDecode((Hashtable)h.get("singleMsg")));
    return req;
  }
}
