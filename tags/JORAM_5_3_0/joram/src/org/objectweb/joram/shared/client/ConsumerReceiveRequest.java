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

/**
 * A <code>ConsumerReceiveRequest</code> is sent by a
 * <code>MessageConsumer</code> when requesting a message.
 */
public final class ConsumerReceiveRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The selector for filtering messages on a queue. */
  private String selector;

  /** Sets the selector. */
  public void setSelector(String selector) {
    this.selector = selector;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector() {
    return selector;
  }

  /** The time to live value of the request (negative for infinite). */
  private long timeToLive;

  /** Sets the time to live value. */
  public void setTimeToLive(long timeToLive) {
    this.timeToLive = timeToLive;
  }

  /** Returns the time to live value in milliseconds. */
  public long getTimeToLive() {
    return timeToLive;
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

  private boolean receiveAck;

  public void setReceiveAck(boolean receiveAck) {
    this.receiveAck = receiveAck;
  }

  public final boolean getReceiveAck() {
    return receiveAck;
  }

  protected int getClassId() {
    return CONSUMER_RECEIVE_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  The selector for filtering messages, if any.
   * @param timeToLive  Time to live value in milliseconds, negative for
   *          infinite.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerReceiveRequest(String targetName, String selector,
                                long timeToLive, boolean queueMode) {
    super(targetName);
    this.selector = selector;
    this.timeToLive = timeToLive;
    this.queueMode = queueMode;
    receiveAck = false;
  }

  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   */
  public ConsumerReceiveRequest() {}

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
    StreamUtil.writeTo(selector, os);
    StreamUtil.writeTo(timeToLive, os);
    StreamUtil.writeTo(queueMode, os);
    StreamUtil.writeTo(receiveAck, os);
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
    timeToLive = StreamUtil.readLongFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
    receiveAck = StreamUtil.readBooleanFrom(is);
  }
}
