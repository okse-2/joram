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
 * A <code>BridgeTopic</code> agent is an agent hosting a bridge topic,
 * and which behaviour is provided by a <code>BridgeTopicImpl</code> instance.
 *
 * @see BridgeTopicImpl
 */
public class BridgeTopic extends Topic
{
  /** The bridge topic's properties. */
  private transient Properties prop;


  /**
   * Constructs a <code>BridgeTopic</code> agent. 
   */ 
  public BridgeTopic()
  {
    super(true);
  }

  /**
   * Initializes the bridge topic.
   *
   * @param adminId  Identifier of the bridge topic administrator.
   *
   * @exception IllegalArgumentException  If the JMS properties are invalid.
   */
  public void init(AgentId adminId) {
    topicImpl = new BridgeTopicImpl(getId(), adminId);
    ((BridgeTopicImpl) topicImpl).init(prop);
  }

  /**
   * Sets the bridge properties.
   */
  public void setProperties(Properties prop) {
    this.prop = prop;
  }
}
