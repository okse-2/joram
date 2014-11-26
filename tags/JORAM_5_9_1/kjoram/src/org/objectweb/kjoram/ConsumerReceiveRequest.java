/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * A <code>ConsumerReceiveRequest</code> is sent by a
 * <code>MessageConsumer</code> when requesting a message.
 */
public final class ConsumerReceiveRequest extends AbstractRequest {
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

  /** The selector for filtering messages on a queue. */
  private String selector;

  /** The time to live value of the request (negative for infinite). */
  private long timeToLive;

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  private boolean receiveAck;

  protected int getClassId() {
    return CONSUMER_RECEIVE_REQUEST;
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
  public void writeTo(OutputXStream os) throws IOException {
    super.writeTo(os);
    os.writeString(selector);
    os.writeLong(timeToLive);
    os.writeBoolean(queueMode);
    os.writeBoolean(receiveAck);
  }
}
