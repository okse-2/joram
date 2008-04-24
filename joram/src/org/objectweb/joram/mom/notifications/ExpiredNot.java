/*
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.Notification;

/**
 * The ExpiredNot holds an expired notification (timeout) which is sent to the
 * previously specified deadNotificationAgent.
 * 
 * @see #setDeadNotificationAgentId(fr.dyade.aaa.agent.AgentId)
 */
public class ExpiredNot extends Notification {

  private static final long serialVersionUID = 1L;

  private Notification expiredNot;

  public ExpiredNot(Notification expiredNot) {
    this.expiredNot = expiredNot;
  }

  public Notification getExpiredNot() {
    return expiredNot;
  }

}
