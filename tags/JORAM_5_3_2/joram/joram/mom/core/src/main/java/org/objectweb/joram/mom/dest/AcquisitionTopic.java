/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import org.objectweb.joram.shared.excepts.RequestException;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * An {@link AcquisitionTopic} agent is an agent hosting an
 * {@link AcquisitionTopicImpl}.
 */
public class AcquisitionTopic extends Topic {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public AcquisitionTopic() {
    fixed = true;
  }

  /**
   * Creates the {@link AcquisitionTopicImpl}.
   *
   * @param adminId  Identifier of the topic administrator.
   * @param prop     The initial set of properties.
   * @throws RequestException 
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) throws RequestException {
    return new AcquisitionTopicImpl(adminId, prop);
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof AcquisitionNot) {
      ((AcquisitionTopicImpl) destImpl).acquisitionNot((AcquisitionNot) not);
    } else {
      super.react(from, not);
    }
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    ((AcquisitionTopicImpl) destImpl).close();
  }

}
