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

package fr.dyade.aaa.ns;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification requesting a name-agent lookup.
 * The <code>NameService</code> agent replies with a <code>LookupReport</code>
 * notification sent to the agent referenced in <code>report</code>.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		LookupReport
 */
public class LookupCommand extends SimpleCommand {

public static final String RCS_VERSION="@(#)$Id: LookupCommand.java,v 1.10 2003-06-23 13:44:58 fmaistre Exp $";

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   */
  public LookupCommand(AgentId report, String name) {
    super(report, name);
  }
}
