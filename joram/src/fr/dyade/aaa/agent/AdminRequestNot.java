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

import fr.dyade.aaa.agent.conf.*;
import fr.dyade.aaa.admin.script.*;

/**
 * AdminRequestNot is a notification to
 * configure A3CMLConfig.
 *
 * @see Script
 * @see AgentAdmin
 * @see AdminReplyNot
 */
public class AdminRequestNot extends Notification {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: AdminRequestNot.java,v 1.5 2004-03-16 10:03:45 fmaistre Exp $"; 

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

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("AdminRequestNot(");
    buf.append("autoStart=");
    buf.append(autoStart);
    buf.append(",silence=");
    buf.append(silence);
    buf.append(",script=");
    buf.append(script);
    buf.append(")");
    return buf.toString();
  }
}
