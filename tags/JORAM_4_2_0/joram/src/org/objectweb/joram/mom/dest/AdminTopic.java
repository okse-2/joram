/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import fr.dyade.aaa.agent.AgentId;

/**
 * An <code>AdminTopic</code> agent is a MOM administration service, which
 * behaviour is provided by an <code>AdminTopicImpl</code> instance.
 *
 * @see AdminTopicImpl
 */
public class AdminTopic extends Topic {
  /**
   * Constructs an <code>AdminTopic</code> agent. 
   */ 
  public AdminTopic() {
    super("JoramAdminTopic", true, AgentId.JoramAdminStamp);
    init(null);
  }

  /**
   * Creates the <tt>TopicImpl</tt>.
   *
   * @param adminId  Identifier of the topic administrator.
   */
  public DestinationImpl createsImpl(AgentId adminId) {
    return new AdminTopicImpl(getId());
  }

  /**
   * Gets the identifier of the default administration topic on a given server.
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.JoramAdminStamp);
  }
}
