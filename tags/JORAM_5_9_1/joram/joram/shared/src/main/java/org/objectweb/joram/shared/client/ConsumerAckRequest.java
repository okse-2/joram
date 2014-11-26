/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>ConsumerAckRequest</code> instance is used by a
 * <code>MessageConsumer</code> for acknowledging a received message.
 */
public final class ConsumerAckRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Message identifier. */
  private Vector ids;

  /** Sets the acknowledged message identifier. */
  public void addId(String id) {
    ids.addElement(id);
  }

  /** Returns the acknowledged message identifier. */
  public Vector getIds() {
    return ids;
  }

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  }

  /** Returns <code>true</code> if the request is for a queue. */
  public boolean getQueueMode() {
    return queueMode;
  }

  protected int getClassId() {
    return CONSUMER_ACK_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param queueMode  <code>true</code> if this request is for a queue.
   */
  public ConsumerAckRequest(String targetName, boolean queueMode) {
    super(targetName);
    ids = new Vector();
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   */
  public ConsumerAckRequest() {}

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
    StreamUtil.writeListOfStringTo(ids, os);
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
    ids = StreamUtil.readVectorOfStringFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
  }
}
