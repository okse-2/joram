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

import fr.dyade.aaa.admin.cmd.ExceptionCmd;
import fr.dyade.aaa.admin.script.*;

/**
 * AdminReplyNot is a notification. It's
 * a reply of AdminRequestNot.
 *
 * @see StartScript
 * @see StopScript
 * @see AgentAdmin
 * @see AdminRequestNot
 */
public class AdminReplyNot extends Notification {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: AdminReplyNot.java,v 1.5 2004-03-16 10:03:45 fmaistre Exp $"; 

  /** exception catch in configuration */
  public ExceptionCmd exc = null;
  /** start script */
  public StartScript startScript = null;
  /** stop script */
  public StopScript stopScript = null;
  /** operation status */
  public int status = 0;

  public AdminReplyNot() {}

  public AdminReplyNot(ExceptionCmd exc) {
    this.exc = exc;
  }

  public String toString() {
    if (exc != null)
      return exc.toString();
    return "";
  }
}
