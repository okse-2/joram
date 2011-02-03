/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

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
} 