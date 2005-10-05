/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent DT
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.*;

public class RegisterTmpDestNot 
    extends fr.dyade.aaa.agent.Notification {

  private AgentId tmpDestId;

  private boolean topic;

  private boolean add;

  public RegisterTmpDestNot(AgentId tmpDestId,
                            boolean topic,
                            boolean add) {
    this.tmpDestId = tmpDestId;
    this.topic = topic;
    this.add = add;
  }

  public final AgentId getTmpDestId() {
    return tmpDestId;
  }

  public final boolean isTopic() {
    return topic;
  }

  public final boolean toAdd() {
    return add;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",tmpDestId=" + tmpDestId + 
      ",topic=" + topic + 
      ",add=" + add + ')';
  }
}
