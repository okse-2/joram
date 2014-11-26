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
 * A <code>ConsumerDenyRequest</code> instance is used by a
 * <code>MessageConsumer</code> for denying a received message.
 */
public final class ConsumerDenyRequest extends AbstractRequest {
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

  protected int getClassId() {
    return CONSUMER_DENY_REQUEST;
  }

  /** Message identifier. */
  private String id;

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /** <code>true</code> if the request must not be acked by the server. */
  private boolean doNotAck = false;

  /** Sets the server ack policy. */
  public void setDoNotAck(boolean doNotAck) {
    this.doNotAck = doNotAck;
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
    os.writeString(id);
    os.writeBoolean(queueMode);
    os.writeBoolean(doNotAck);
  }
}
