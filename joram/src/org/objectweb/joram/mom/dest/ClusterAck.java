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
import org.objectweb.joram.mom.notifications.ClusterRequest;

/**
 * A <code>ClusterAck</code> instance is a notification sent by a topic
 * requested to join a cluster.
 */
class ClusterAck extends Notification
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The originial client request. */
  ClusterRequest request;
  /** The original requester. */
  AgentId requester;
  /** <code>true</code> if the topic can join the cluster. */
  boolean ok;
  /** Info. */
  String info;

  /**
   * Constructs a <code>ClusterAck</code> instance.
   *
   * @param clusterTest  The <code>ClusterTest</code> this notification
   *          replies to.
   * @param ok  <code>true</code> if the topic can join the cluster.
   * @param info  Related info.
   */
  ClusterAck(ClusterTest clusterTest, boolean ok, String info)
  {
    this.request = clusterTest.request;
    this.requester = clusterTest.requester;
    this.ok = ok;
    this.info = info;
  }
}
