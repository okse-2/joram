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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.Strings;


/**
 * Notification describing a basic name service command.
 * Only used by <code>NameService</code> agents.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		NameService
 */
public class SimpleCommand extends Notification {

public static final String RCS_VERSION="@(#)$Id: SimpleCommand.java,v 1.10 2003-06-23 13:44:58 fmaistre Exp $";

  /** agent to report completion status to */
  private AgentId report;

  /** name of target entry of the command */
  private String name;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   */
  public SimpleCommand(AgentId report, String name) {
    this.report = report;
    this.name = name;
  }

  /**
   * Accesses property.
   * Allows differed setting of report variable.
   * Does not allow changing the variable.
   *
   * @param report	agent to report status to
   */
  public void setReport(AgentId report) throws Exception {
    if (this.report != null) {
      throw new IllegalArgumentException("cannot change report: " + this);
    }
    this.report = report;
  }

  /**
   * Accesses property.
   *
   * @return		agent to report status to
   */
  public final AgentId getReport() {
    if (report == null)
      return AgentId.nullId;
    else
      return report;
  }

  /**
   * Accesses read only property.
   *
   * @return		name of target entry of the command
   */
  public String getName() { return name; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",report=" + report + 
      ",name=" + Strings.toString(name) + ")";
  }
}
