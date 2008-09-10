/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
import fr.dyade.aaa.agent.Notification;
import org.objectweb.joram.mom.notifications.SetFatherRequest;

/**
 * A <code>FatherTest</code> instance is a notification sent by a topic 
 * for checking if an other topic may be its hierarchical father.
 */
class FatherTest extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** The original client request. */
  SetFatherRequest request;
  /** The original requester. */
  AgentId requester;

  /**
   * Constructs a <code>FatherNot</code> instance.
   *
   * @param request  The original client request.
   * @param requester  The original requester.
   */
  FatherTest(SetFatherRequest request, AgentId requester) {
    this.request = request;
    this.requester = requester;
  }
}
