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
 * Notification reporting the agent associated with a name, as result
 * from a <code>LookupCommand</code> request.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	LookupCommand
 */
public class LookupReport extends SimpleReport {

public static final String RCS_VERSION="@(#)$Id: LookupReport.java,v 1.10 2003-06-23 13:44:58 fmaistre Exp $";

  /** agent associated with name */
  private AgentId agent;

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command
   * @param status		completion status
   * @param message		optional message
   * @param agent		agent associated with name
   */
  public LookupReport(SimpleCommand command, int status, String message,
		      AgentId agent)
  {
    super(command, status, message);
    this.agent = agent;
  }

  /**
   * Accesses read only property.
   *
   * @return		agent associated with name
   */
  public AgentId getAgent() { return agent; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",agent=" + agent + ")";
  }
}
