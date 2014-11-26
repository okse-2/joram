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
import java.util.Vector;

/**
 * A <code>ConsumerMessages</code> is used by a proxy for sending messages
 * to a consumer.
 */
public final class ConsumerMessages extends AbstractReply {
  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   */
  public ConsumerMessages() {}

  protected int getClassId() {
    return CONSUMER_MESSAGES;
  }

  /** Wrapped messages. */
  private Vector messages = null;

  /** Returns the messages to deliver. */
  public Vector getMessages() {
    if (messages == null)
      messages = new Vector();
    return messages;
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

  /** <code>true</code> if the messages come from a queue. */
  private boolean queueMode;

  /** Returns <code>true</code> if the messages come from a queue. */
  public boolean getQueueMode() {
    return queueMode;
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */
  
  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    messages = Message.readVectorFrom(is);
    comingFrom = is.readString();
    queueMode = is.readBoolean();
  }
}
