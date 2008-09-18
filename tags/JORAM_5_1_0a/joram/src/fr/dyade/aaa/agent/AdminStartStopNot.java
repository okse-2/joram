/*
 * Copyright (C) 1996 - 2000 SCALAGENT
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

import fr.dyade.aaa.admin.script.*;

/**
 * AdminStartStopNot is a notification to
 * start and stop networks, servers persitents,
 * servers transients and services.
 *
 * @see StartScript
 * @see StopScript
 * @see AgentAdmin
 * @see AdminAckStartStopNot
 */
public class AdminStartStopNot extends Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** start script vector */
  public StartScript startScript = null;
  /** stop script vector */
  public StopScript stopScript = null;

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
    output.append(",startScript=").append(startScript);
    output.append(",stopScript=").append(stopScript);
    output.append(')');

    return output;
  }
}
