/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Contributor(s):
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
public class BridgeQueue extends Queue
{
  /** The bridge queue's properties. */
  private transient Properties prop;


  /**
   * Constructs a <code>BridgeQueue</code> agent. 
   */ 
  public BridgeQueue()
  {
    super(true);
  }

  /**
   * Initializes the bridge queue.
   *
   * @param adminId  Identifier of the bridge queue administrator.
   *
   * @exception IllegalArgumentException  If the JMS properties are invalid.
   */
  public void init(AgentId adminId) {
    queueImpl = new BridgeQueueImpl(getId(), adminId);
    ((BridgeQueueImpl) queueImpl).init(prop);
  }

  /**
   * Sets the bridge properties.
   */
  public void setProperties(Properties prop) {
    this.prop = prop;
  }
}
