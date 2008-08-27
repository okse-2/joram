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

import org.objectweb.joram.shared.stream.StreamUtil;

import fr.dyade.aaa.util.Strings;

/**
 * A <code>ConsumerSetListRequest</code> is sent by a
 * <code>MessageConsumer</code> on which a message listener is set.
 */
public final class ConsumerSetListRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Selector for filtering messages on a queue. */
  private String selector;

  /** Sets the selector. */
  public void setSelector(String selector) {
    this.selector = selector;
  }

  /** Returns the selector for filtering messages. */
  public String getSelector() {
    return selector;
  }

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;
  
  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode() {
    return queueMode;
  }

  private String[] msgIdsToAck;

  public final String[] getMessageIdsToAck() {
    return msgIdsToAck;
  }

  private int msgCount;

  public final int getMessageCount() {
    return msgCount;
  }

  protected int getClassId() {
    return CONSUMER_SET_LIST_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerSetListRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  Selector for filtering messages.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerSetListRequest(String targetName, 
                                String selector, 
                                boolean queueMode,
                                String[] msgIdsToAck,
                                int msgCount) {
    super(targetName);
    this.selector = selector;
    this.queueMode = queueMode;
    this.msgIdsToAck = msgIdsToAck;
    this.msgCount = msgCount;
  }

  /**
   * Constructs a <code>ConsumerSetListRequest</code>.
   */
  public ConsumerSetListRequest() {
  }

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",selector=").append(selector);
    strbuf.append(",queueMode=").append(queueMode);
    strbuf.append(",msgIdsToAck=");
    Strings.toString(strbuf, msgIdsToAck);
    strbuf.append(",msgCount=").append(msgCount);
    strbuf.append(')');
  }

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(selector, os);
    StreamUtil.writeTo(queueMode, os);
    StreamUtil.writeArrayOfStringTo(msgIdsToAck, os);
    StreamUtil.writeTo(msgCount, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    selector = StreamUtil.readStringFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
    msgIdsToAck = StreamUtil.readArrayOfStringFrom(is);
    msgCount = StreamUtil.readIntFrom(is);
  }
}
