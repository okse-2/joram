/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */

package fr.dyade.aaa.agent;

/**
 * Notify by the engine to the sender of a notification when the target agent
 * does not exist.
 *
 * @see Engine
 */
public class UnknownAgent extends Notification {
  public static final String RCS_VERSION="@(#)$Id: UnknownAgent.java,v 1.17 2004-03-16 10:03:45 fmaistre Exp $";

  static final long serialVersionUID = 3125179672784868254L;

  /** The non-existent target agent id. */
  public AgentId agent;
  /** The failing notification that can not be delivered. */
  public Notification not;

  /**
   * Allocates a new <code>UnknownAgent</code> notification.
   *
   * @param agent	The non-existent target agent id.
   * @param not		The failing notification that can not be delivered.
   */
  public UnknownAgent(AgentId agent, Notification not) {
    this.agent = agent;
    this.not = not;
  }

  /**
   * Returns a string representation of this notification.
   *
   * @return	A string representation of this notification. 
   */
  public String toString() {
    return "(" + super.toString() +
      ",agent=" + agent +
      ",not=" + not + ")";
  }
}

