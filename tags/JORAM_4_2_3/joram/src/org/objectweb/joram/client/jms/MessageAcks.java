/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import java.util.Vector;

/**
 * A <code>MessageAcks</code> instance holds the identifiers of messages to
 * acknowledge on a queue or on a proxy subscription.
 */
class MessageAcks
{
  /** The vector of messages identifiers. */
  private Vector ids;
  /** <code>true</code> if the messages to acknowledge are on a queue. */
  private boolean queueMode;

  /**
   * Constructs a <code>MessageAcks</code> instance.
   *
   * @param queueMode  <code>true</code> for queue messages.
   */
  MessageAcks(boolean queueMode)
  {
    this.queueMode = queueMode;
    ids = new Vector();
  }

  /** Adds a message identifier. */
  void addId(String id)
  {
    ids.add(id);
  }

  /** Adds a vector of message identifiers. */
  void addIds(Vector ids)
  {
    this.ids.addAll(ids);
  }

  /** Returns the vector of message identifiers. */
  Vector getIds()
  {
    return ids;
  }

  /**
   * Returns <code>true</code> if the messages to acknowledge are on a queue.
   */
  boolean getQueueMode()
  {
    return queueMode;
  }

  public String toString() {
    return '(' + super.toString() +
      ",ids=" + ids +
      ",queueMode=" + queueMode + ')';
  }
} 
