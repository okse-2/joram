/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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

import java.util.Properties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.management.MXWrapper;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;


/**
 * A <code>Queue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>QueueImpl</code> instance.
 *
 * @see QueueImpl
 */
public class Queue extends Agent implements AdminDestinationItf
{
  /**
   * Reference to the <code>QueueImpl</code> instance providing this
   * agent with its queue behaviour.
   */
  protected QueueImpl queueImpl;


  /**
   * Constructs a <code>Queue</code> agent. 
   *
   * @param adminId  Identifier of the agent which will be the administrator
   *          of the queue.
   */ 
  public Queue(AgentId adminId)
  {
    init(adminId);
  }

  /**
   * Constructor with parameter for fixing the queue or not.
   */ 
  protected Queue(boolean fixed)
  {
    super(fixed);
  }

  /**
   * Empty constructor for subclass. 
   */ 
  protected Queue()
  {}

  /**
   * Initializes the queue.
   *
   * @param adminId  Identifier of the queue administrator.
   */
  public void init(AgentId adminId) {
    queueImpl = new QueueImpl(getId(), adminId);
  }

  /**
   * Sets properties for the queue.
   * <p>
   * Empty method as no properties may be set for the queue.
   */
  public void setProperties(Properties prop) {}

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception
  {
    super.agentInitialize(firstTime);
    MXWrapper.registerMBean(queueImpl,
                            "JORAM destinations",
                            getId().toString(),
                            "Queue",
                            null);
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime)
  {
    try {
      MXWrapper.unregisterMBean("JORAM destinations",
                                getId().toString(),
                                "Queue",
                                null);
    }
    catch (Exception exc) {}
  }

  /**
   * Reactions to notifications are implemented in the
   * <code>QueueImpl</code> class.
   */ 
  public void react(AgentId from, Notification not) throws Exception
  {
    try {
      queueImpl.react(from, not);

      // A DeleteNot notification is finally processed at the Agent level
      // when its processing went successful in the DestinationImpl instance.
      if (not instanceof DeleteNot && queueImpl.canBeDeleted())
        super.react(from, not);
    }
    catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }
}
