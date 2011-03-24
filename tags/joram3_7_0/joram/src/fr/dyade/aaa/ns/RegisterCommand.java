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
 * Notification requesting a name-agent registration.
 * The <code>NameService</code> agent replies with a <code>SimpleReport</code>
 * notification sent to the agent referenced in <code>report</code>.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 */
public class RegisterCommand extends SimpleCommand {

public static final String RCS_VERSION="@(#)$Id: RegisterCommand.java,v 1.11 2003-09-11 09:54:12 fmaistre Exp $";

  /** agent associated with name */
  private AgentId agent;

  /** true if the register command must overwrite the evnetual existing entry */
  public boolean rebind = true;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   * @param agent		agent associated with name
   */
  public RegisterCommand(AgentId report, String name, AgentId agent) {
    this (report, name, agent, true);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   * @param agent		agent associated with name
   * @param rebind
   */
  public RegisterCommand(AgentId report, String name, AgentId agent, boolean rebind) {
    super(report, name);
    this.agent = agent;
    this.rebind = rebind;
  }

  /**
   * Accesses read only property.
   *
   * @return		agent associated with name
   */
  public AgentId getAgent() { return agent; }


  /**
   * Accesses read only property.
   *
   * @return		rebind value
   */
  public boolean getRebind() { return rebind; }

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