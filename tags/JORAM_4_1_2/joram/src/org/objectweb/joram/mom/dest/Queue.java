/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

/**
 * A <code>Queue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>QueueImpl</code> instance.
 *
 * @see QueueImpl
 */
public class Queue extends Destination {
  /**
   * Empty constructor for newInstance(). 
   */ 
  protected Queue() {}

  /**
   * Constructs a <code>Queue</code> agent. 
   *
   * @param adminId  Identifier of the agent which will be the administrator
   *          of the queue.
   */ 
  public Queue(AgentId adminId) {
    super(adminId);
  }

  /**
   * Constructor with parameter for fixing the queue or not.
   */ 
  protected Queue(boolean fixed) {
    super(fixed);
  }

  /**
   * Creates the <tt>QueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   */
  public DestinationImpl createsImpl(AgentId adminId) {
    return new QueueImpl(getId(), adminId);
  }
}
