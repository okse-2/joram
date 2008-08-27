/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>ConsumerMessages</code> is used by a JMS proxy for sending messages
 * to a consumer.
 */
public final class ConsumerMessages extends AbstractJmsReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Wrapped messages. */
  private Vector messages = null;

  /** Returns the messages to deliver. */
  public Vector getMessages() {
    if (messages == null)
      messages = new Vector();
    return messages;
  }

  public void addMessage(Message msg) {
    if (messages == null)
      messages = new Vector();
    messages.addElement(msg);
  }

  public int getMessageCount() {
    if (messages == null)
      return 0;
    else
      return messages.size();
  }

  /** Name of the subscription or the queue the messages come from. */
  private String comingFrom = null;

  /**
   * Returns the name of the queue or the subscription the messages come
   * from.
   */
  public String comesFrom() {
    return comingFrom;
  }

  public void setComesFrom(String comingFrom) {
    this.comingFrom = comingFrom;
  }

  /** <code>true</code> if the messages come from a queue. */
  private boolean queueMode;

  /** Returns <code>true</code> if the messages come from a queue. */
  public boolean getQueueMode() {
    return queueMode;
  }

  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  } 

  protected int getClassId() {
    return CONSUMER_MESSAGES;
  }

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
                          String comingFrom, boolean queueMode) {
    super(correlationId);
    if (message != null) {
      messages = new Vector();
      messages.addElement(message);
    }
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
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
                          String comingFrom, boolean queueMode) {
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
                          boolean queueMode) {
    super(correlationId);
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
  }

  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   */
  public ConsumerMessages() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",messages=").append(messages);
    strbuf.append(",comingFrom=").append(comingFrom);
    strbuf.append(",queueMode=").append(queueMode);
    strbuf.append(')');
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    Message.writeVectorTo(messages, os);
    StreamUtil.writeTo(comingFrom, os);
    StreamUtil.writeTo(queueMode, os);
  }
  
  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    messages = Message.readVectorFrom(is);
    comingFrom = StreamUtil.readStringFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
  }
}
