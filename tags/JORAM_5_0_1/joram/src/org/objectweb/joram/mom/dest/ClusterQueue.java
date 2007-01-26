/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
package org.objectweb.joram.mom.dest;

import java.util.*;

import org.objectweb.joram.mom.dest.Queue;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * A <code>ClusterQueue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>ClusterQueueImpl</code> instance.
 *
 * @see ClusterQueueImpl
 */
public class ClusterQueue extends Queue {
  /**
   * Empty constructor for newInstance(). 
   */ 
  protected ClusterQueue() {}

  /**
   * Creates the <tt>QueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    ClusterQueueImpl queueImpl = new ClusterQueueImpl(getId(), adminId, prop);
    return queueImpl;
  }
}
