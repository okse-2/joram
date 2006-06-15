/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import fr.dyade.aaa.agent.AgentId;

import java.util.Properties;

/**
 * A <code>BridgeQueue</code> agent is an agent hosting a bridge queue,
 * and which behaviour is provided by a <code>BridgeQueueImpl</code> instance.
 *
 * @see BridgeQueueImpl
 */
public class BridgeQueue extends Queue {
  /**
   * Constructs a <code>BridgeQueue</code> agent. 
   */ 
  public BridgeQueue() {
    super();
    fixed = true;
  }

  /**
   * Creates the bridge queue.
   *
   * @param adminId  Identifier of the bridge queue administrator.
   * @param prop     The initial set of properties.
   *
   * @exception IllegalArgumentException  If the JMS properties are invalid.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    BridgeQueueImpl queueImpl = new BridgeQueueImpl(getId(), adminId, prop);
    return queueImpl;
  }
}
