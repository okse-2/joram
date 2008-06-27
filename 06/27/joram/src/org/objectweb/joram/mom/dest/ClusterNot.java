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
package org.objectweb.joram.mom.dest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>ClusterNot</code> instance is a notification sent by a topic 
 * to another topic for notifying it of a topic joining the cluster they
 * are part of.
 */
class ClusterNot extends Notification
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The identifier of the topic to add to the cluster. */
  AgentId topicId;

  /**
   * Constructs a <code>ClusterNot</code> instance.
   *
   * @param topicId  The identifier of the topic to add to the cluster.
   */
  ClusterNot(AgentId topicId)
  {
    this.topicId = topicId;
  }
}
