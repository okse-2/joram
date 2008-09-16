/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package org.objectweb.joram.mom.dest;

import org.objectweb.joram.mom.notifications.ClientMessages;

/**
 * A <code>TopicForwardNot</code> is a notification sent by a topic to 
 * another topic part of the same cluster, or to its hierarchical father,
 * and holding a forwarded <code>ClientMessages</code> notification.
 */
public class TopicForwardNot extends fr.dyade.aaa.agent.Notification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * <code>true</code> if the notification is destinated to a hierarchical
   * father.
   */
  public boolean toFather;
  /** The forwarded messages. */
  public ClientMessages messages;

  /**
   * Constructs a <code>TopicForwardNot</code> instance.
   *
   * @param messages  Notification to forward.
   * @param toFather  <code>true</code> if the notification is destinated
   *          to a hierarchical father.
   */
  TopicForwardNot(ClientMessages messages, boolean toFather) {
    this.messages = messages;
    this.toFather = toFather;
  }
}
