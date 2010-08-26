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
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.AgentId;


/**
 * A <code>ClusterRequest</code> instance is used by a client agent
 * for notifying a topic of the identifier of an other topic to set a
 * cluster with.
 */
public class ClusterRequest extends AdminRequest
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The identifier of the topic the target topic must set a cluster with. */
  private AgentId topicId;


  /**
   * Constructs a <code>ClusterRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param topicId  The identifier of the topic the target topic must
   *          set a cluster with.
   */
  public ClusterRequest(String id, AgentId topicId)
  {
    super(id);
    this.topicId = topicId;
  }


  /**
   * Returns the identifier of the topic the target topic must set a
   * cluster with. 
   */
  public AgentId getTopicId()
  {
    return topicId;
  }
} 
