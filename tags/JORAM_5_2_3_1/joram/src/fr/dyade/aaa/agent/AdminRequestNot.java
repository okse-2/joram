/*
 * Copyright (C) 1996 - 2008 ScalAgent Distributed Technologies

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
 * AdminRequestNot is a notification to configure A3CMLConfig.
 *
 * @see Script
 * @see AgentAdmin
 * @see AdminReplyNot
 */
public class AdminRequestNot extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  /** configuration script. */
  public Script script = null;
  /** used to start script in a same reaction.
   *  (set configuration and start this configuration) */
  public boolean autoStart = false;
  /** silence use for idempotence. */
  public boolean silence = false;

  public AdminRequestNot(Script script, 
                         boolean autoStart,
                         boolean silence) {
    this.script = script;
    this.autoStart = autoStart;
    this.silence = silence;
  }

  public AdminRequestNot(Script script, boolean autoStart) {
    this.script = script;
    this.autoStart = autoStart;
  }

  public AdminRequestNot(Script script) {
    this.script = script;
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
    output.append(",autoStart=").append(autoStart);
    output.append(",silence=").append(silence);
    output.append(",script=").append(script);
    output.append(')');

    return output;
  }
}
