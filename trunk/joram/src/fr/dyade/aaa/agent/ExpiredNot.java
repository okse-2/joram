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
package fr.dyade.aaa.agent;

/**
 * The ExpiredNot holds an expired notification (timeout) which is sent to the
 * previously specified deadNotificationAgent.
 * 
 * @see #setDeadNotificationAgentId(fr.dyade.aaa.agent.AgentId)
 */
public class ExpiredNot extends Notification {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /** The expiration which had expired. **/
  private Notification expiredNot;
  
  /** The agent which sent the expired notification. */
  private AgentId from;

  /** The destination agent of the expired notification. */
  private AgentId to;

  /**
   * @return The agent which sent the expired notification.
   */
  public AgentId getFromAgentId() {
    return from;
  }

  /**
   * @return The destination agent of the expired notification.
   */
  public AgentId getToAgentId() {
    return to;
  }

  /**
   * Builds a notification carrying an expired notification.
   * 
   * @param expiredNot
   *          The expiration which had expired.
   * @param from
   *          The agent which sent the expired notification.
   * @param to
   *          The destination agent of the expired notification.
   */
  public ExpiredNot(Notification expiredNot, AgentId from, AgentId to) {
    this.expiredNot = expiredNot;
    this.from = from;
    this.to = to;
  }

  /**
   * @return The notification which had expired.
   */
  public Notification getExpiredNot() {
    return expiredNot;
  }
}
