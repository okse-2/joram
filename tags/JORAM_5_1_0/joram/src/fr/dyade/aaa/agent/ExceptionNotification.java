/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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
 * Notify by the engine to the sender of a notification when the corresponding
 * reaction throws an exception.
 *
 * @see Engine
 */
public class ExceptionNotification extends Notification {
  /**
   * 
   */
  static final long serialVersionUID = 1L;

  /** The target agent id. */
  public AgentId agent;
  /** The failing notification. */
  public Notification not;
  /** The exception thrown. */
  public Exception exc;

  /**
   * Allocates a new <code>ExceptionNotification</code> notification.
   *
   * @param agent	The target agent id.
   * @param not		The failing notification.
   * @param exc		The exception thrown.
   */
  public ExceptionNotification(AgentId agent,
			       Notification not,
			       Exception exc) {
    this.agent = agent;
    this.not = not;
    this.exc = exc;
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
    output.append(",exc=").append(exc);
    output.append(')');

    return output;
  }
}
