/*
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): Bull
 * Contributor(s): INRIA
 * Contributor(s): ScalAgent Distributed Technologies
 */

package fr.dyade.aaa.agent;

/**
 * Notify by agents when there is no reaction allowed. 
 *
 * @see Agent
 */
public class UnknownNotification extends Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The target agent id. */
  public AgentId agent;
  /** The failing notification. */
  public Notification not;

  /**
   * Allocates a new <code>UnknownNotification</code> notification.
   *
   * @param agent	The target agent id.
   * @param not		The failing notification.
   */
  public UnknownNotification(AgentId agent, Notification not) {
    this.agent = agent;
    this.not = not;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",agent=").append(agent);
    output.append(",not=").append(not);
    output.append(')');

    return output;
  }
}
