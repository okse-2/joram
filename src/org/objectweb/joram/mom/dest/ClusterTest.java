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

import java.util.Set;

import org.objectweb.joram.mom.notifications.ClusterRequest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>ClusterTest</code> instance is a notification sent by a topic 
 * to another topic for checking if it might be part of a cluster.
 */
class ClusterTest extends Notification
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The original client request. */
  ClusterRequest request;
  /** The original requester. */
  AgentId requester;
  
  /** Set containing AgentId of topics already in the cluster */
  Set friends;

  /**
   * Constructs a <code>ClusterTest</code> instance.
   *
   * @param request  The original client request.
   * @param requester  The original requester.
   */
  ClusterTest(ClusterRequest request, AgentId requester, Set friends) {
    this.request = request;
    this.requester = requester;
    this.friends = friends;
  }
}
