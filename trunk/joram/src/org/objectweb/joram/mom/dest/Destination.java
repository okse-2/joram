/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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

import java.util.Properties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

/**
 * A <code>Destination</code> agent is an agent hosting a MOM destination,
 * for example a <tt>Queue</tt> or a <tt>Topic</tt>.
 * Its behaviour is provided by a <code>DestinationImpl</code> instance.
 *
 * @see DestinationImpl
 */
public abstract class Destination extends Agent implements AdminDestinationItf
{
  /**
   * The reference of the <code>DestinationImpl</code> instance providing this
   * this agent with its <tt>Destination</tt> behaviour.
   */
  protected DestinationImpl destImpl;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public Destination() {}

  /**
   * Constructs a <code>Destination</code> agent. 
   * 
   * @param adminId  Identifier of the agent which will be the administrator
   *          of the topic.
   */ 
  public Destination(AgentId adminId) {
    init(adminId);
  }

  /**
   * Constructor with parameters for fixing the destination.
   */
  protected Destination(boolean fixed) {
    super(fixed);
  }

  /**
   * Constructor with parameters for fixing the destination and specifying its
   * identifier.
   */
  protected Destination(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /**
   * Initializes the destination by creating the <tt>DestinationImpl</tt>
   * object.
   *
   * @param adminId  Identifier of the destination administrator.
   */
  public final void init(AgentId adminId) {
    destImpl = createsImpl(adminId);
  }

  /**
   * Creates the specific implementation.
   *
   * @param adminId  Identifier of the topic administrator.
   */
  public abstract DestinationImpl createsImpl(AgentId adminId);
  
  /**
   * Sets properties for the destination.
   * <p>
   * Empty method as no properties may be set for the generic destination.
   */
  public void setProperties(Properties prop) {}

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
  }

  /**
   * Reactions to notifications are implemented by the
   * <tt>DestinationImpl</tt> class.
   */
  public void react(AgentId from, Notification not) throws Exception
  {
    try {
      destImpl.react(from, not);

      // A DeleteNot notification is finally processed at the
      // Agent level when its processing went successful in
      // the DestinationImpl instance.
      if (not instanceof DeleteNot && destImpl.canBeDeleted()) 
        super.react(from, not);
    } catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }
}
