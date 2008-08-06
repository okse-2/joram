/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import fr.dyade.aaa.agent.AgentId;

/**
 * A <code>DeadMQueue</code> agent is an agent hosting a MOM dead message
 * queue, and which behavior is provided by a <code>DeadMQueueImpl</code>
 * instance.
 *
 * @see DeadMQueueImpl
 */
public class DeadMQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static String getDestinationType() {
    return "queue.dmq";
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public DeadMQueue() {}

  /**
   * Creates the dead message queue.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    DestinationImpl dest = new QueueImpl(adminId, prop);
    dest.setFreeWriting(true);
    return dest;
  }
}
