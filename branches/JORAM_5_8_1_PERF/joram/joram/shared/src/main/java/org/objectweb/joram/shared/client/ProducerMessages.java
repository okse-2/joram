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
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>ProducerMessages</code> instance is sent by a
 * <code>MessageProducer</code> when sending messages.
 */
public final class ProducerMessages extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The wrapped messages. */
  private Vector messages = null;

  /**
   * Indicates whether the produced messages
   * are asynchronously send or not
   * (without or with an acknowledgement).
   */
  private boolean asyncSend = false;
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }
  
  // JORAM_PERF_BRANCH
  private boolean isQueue;

  //JORAM_PERF_BRANCH
  public boolean isQueue() {
    return isQueue;
  }

  //JORAM_PERF_BRANCH
  public void setQueue(boolean isQueue) {
    this.isQueue = isQueue;
  }

  /** Returns the produced messages. */
  public Vector getMessages() {
    if (messages == null)
      messages = new Vector();
    return messages;
  }

  /** Adds a message to deliver. */
  public void addMessage(Message msg) {
    if (messages == null)
      messages = new Vector();
    messages.addElement(msg);
  }

  /** Adds messages to deliver. */
  public void addMessages(Vector msgs) {
    if (messages == null)
      messages = new Vector();
    for (Enumeration e = msgs.elements(); e.hasMoreElements(); )
      messages.addElement(e.nextElement());
  }

  /**
   * Replace old messages vector by new newMsgs vector
   * 
   * @param newMsgs new messages vector.
   */
  public void setMessages(Vector newMsgs) {
  	messages = newMsgs;
  }
  
  protected int getClassId() {
    return PRODUCER_MESSAGES;
  }

  /**
   * Constructs a <code>ProducerMessages</code> instance.
   *
   * @param dest  Name of the destination the messages are sent to.
   */
  public ProducerMessages(String dest) {
    super(dest);
  }

  /**
   * Constructs a <code>ProducerMessages</code> instance carrying a single
   * message.
   *
   * @param dest  Name of the destination the messages are sent to.
   * @param msg  Message to carry.
   */
  public ProducerMessages(String dest, Message msg) {
    super(dest);
    messages = new Vector();
    messages.addElement(msg);
  }

  /**
   * Constructs a <code>ProducerMessages</code> instance.
   */
  public ProducerMessages() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",messages=").append(messages);
    strbuf.append(",asyncSend=").append(asyncSend);
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
    StreamUtil.writeTo(asyncSend, os);
    // JORAM_PERF_BRANCH
    StreamUtil.writeTo(isQueue, os);
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
    asyncSend = StreamUtil.readBooleanFrom(is);
    // JORAM_PERF_BRANCH
    isQueue = StreamUtil.readBooleanFrom(is);
  }
}
