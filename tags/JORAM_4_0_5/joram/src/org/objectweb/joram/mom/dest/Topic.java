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
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;


/**
 * A <code>Topic</code> agent is an agent hosting a MOM topic, and which
 * behaviour is provided by a <code>TopicImpl</code> instance.
 *
 * @see TopicImpl
 */
public class Topic extends Agent implements AdminDestinationItf
{
  /**
   * The reference of the <code>TopicImpl</code> instance providing this
   * agent with its topic behaviour.
   */
  protected TopicImpl topicImpl;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public Topic() {}

  /**
   * Constructs a <code>Topic</code> agent. 
   * 
   * @param adminId  Identifier of the agent which will be the administrator
   *          of the topic.
   */ 
  public Topic(AgentId adminId)
  {
    init(adminId);
  }

  /**
   * Constructor with parameters for fixing the topic.
   */
  protected Topic(boolean fixed)
  {
    super(fixed);
  }

  /**
   * Constructor with parameters for fixing the topic and specifying its
   * identifier.
   */
  protected Topic(String name, boolean fixed, int stamp)
  {
    super(name, fixed, stamp);
  }

  /**
   * Initializes the topic.
   *
   * @param adminId  Identifier of the topic administrator.
   */
  public void init(AgentId adminId) {
    topicImpl = new TopicImpl(getId(), adminId);
  }

  /**
   * Sets properties for the topic.
   * <p>
   * Empty method as no properties may be set for the topic.
   */
  public void setProperties(Properties prop) {}

  
  /**
   * Reactions to notifications are implemented in the
   * <code>TopicImpl</code> class.
   */
  public void react(AgentId from, Notification not) throws Exception
  {
    try {
      topicImpl.react(from, not);

      // A DeleteNot notification is finally processed at the
      // Agent level when its processing went successful in
      // the DestinationImpl instance.
      if (not instanceof DeleteNot && topicImpl.canBeDeleted()) 
        super.react(from, not);
    }
    catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }
}
