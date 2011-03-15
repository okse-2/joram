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
 * A <code>ConsumerSubRequest</code> is sent by a constructing
 * <code>MessageConsumer</code> destinated to consume messages on a topic.
 */
public final class ConsumerSubRequest extends AbstractRequest {
  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   */
  public ConsumerSubRequest(String topic, String subName, String selector,
                            boolean noLocal, boolean durable) {
    super(topic);
    this.subName = subName;
    this.selector = selector;
    this.noLocal = noLocal;
    this.durable = durable;
    this.asyncSub = false;
  }

  protected int getClassId() {
    return CONSUMER_SUB_REQUEST;
  }

  /** The subscription's name. */
  private String subName;

  /** The selector for filtering messages. */
  private String selector;

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  private boolean noLocal;

  /** <code>true</code> if the subscription is durable. */
  private boolean durable;
  
  /** asynchronous subscription. */
  private boolean asyncSub;

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
    os.writeString(subName);
    os.writeString(selector);
    os.writeBoolean(noLocal);
    os.writeBoolean(durable);
    os.writeBoolean(asyncSub);
  }
  
  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    subName = is.readString();
    selector = is.readString();
    noLocal = is.readBoolean();
    durable = is.readBoolean();
    asyncSub = is.readBoolean();
  }
}
