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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>ConsumerDenyRequest</code> instance is used by a
 * <code>MessageConsumer</code> for denying a received message.
 */
public final class ConsumerDenyRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Message identifier. */
  private String id;

  /** Sets the denied message identifier. */
  public void setId(String id) {
    this.id = id;
  }

  /** Returns the denied message identifier. */
  public String getId() {
    return id;
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

  /** <code>true</code> if the request must not be acked by the server. */
  private boolean doNotAck = false;

  /** Sets the server ack policy. */
  public void setDoNotAck(boolean doNotAck) {
    this.doNotAck = doNotAck;
  }

  /**
   * Returns <code>true</code> if the request must not be acked by the 
   * server.
   */
  public boolean getDoNotAck() {
    return doNotAck;
  }

  protected int getClassId() {
    return CONSUMER_DENY_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode) {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode,
                             boolean doNotAck) {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
    this.doNotAck = doNotAck;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   */
  public ConsumerDenyRequest() {}

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
    StreamUtil.writeTo(id, os);
    StreamUtil.writeTo(queueMode, os);
    StreamUtil.writeTo(doNotAck, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    id = StreamUtil.readStringFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
    doNotAck = StreamUtil.readBooleanFrom(is);
  }
}
