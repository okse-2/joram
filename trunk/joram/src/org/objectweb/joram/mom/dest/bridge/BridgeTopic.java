/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest.bridge;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Topic;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>BridgeTopic</code> agent is an agent hosting a bridge topic,
 * and which behaviour is provided by a <code>BridgeTopicImpl</code> instance.
 *
 * @see BridgeTopicImpl
 */
public class BridgeTopic extends Topic {

  /**
   * Constructs a <code>BridgeTopic</code> agent. 
   */ 
  public BridgeTopic() {
    super();
    fixed = true;
  }

  /**
   * Creates the bridge topic.
   *
   * @param adminId  Identifier of the bridge topic administrator.
   * @param prop     The initial set of properties.
   *
   * @exception IllegalArgumentException  If the JMS properties are invalid.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    BridgeTopicImpl topicImpl = new BridgeTopicImpl(getId(), adminId, prop);
    return topicImpl;
  }
  
  /**
   * Specializes this <code>TopicImpl</code> method for processing the
   * specific bridge notifications.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not)
              throws Exception {
    if (not instanceof BridgeDeliveryNot)
      ((BridgeTopicImpl) destImpl).bridgeDeliveryNot(from, (BridgeDeliveryNot) not);
    else if (not instanceof BridgeAckNot)
      ((BridgeTopicImpl) destImpl).bridgeAckNot((BridgeAckNot) not);
    else
      super.react(from, not);
  }
}
