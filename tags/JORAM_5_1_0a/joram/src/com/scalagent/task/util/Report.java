/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.task.util;

import java.io.*;
import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task.Status;

/**
  * Notification reporting a service task completion status.
  * Reuse status definition from <code>Task.Status</code>.
  *
  * @see	Command
  * @see	Status
  */
public class Report extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** executed command */
  private Command command;
  /** completion status */
  private int status;
  /** optional message */
  private String message;

  /**
   * Default constructor.
   */
  public Report() {
    this(null, Status.NONE, null);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command
   * @param status		completion status
   * @param message		optional message
   */
  public Report(Command command, int status, String message) {
    this.command = command;
    this.status = status;
    this.message = message;
  }

  /**
   * Accesses read only property.
   *
   * @return		executed command
   */
  public Command getCommand() { return command; }

  /**
   * Accesses read only property.
   *
   * @return		completion status
   */
  public int getStatus() { return status; }

  /**
   * Accesses read only property.
   *
   * @return		optional message
   */
  public String getMessage() { return message; }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",command=").append(command);
    output.append(",status=").append(Status.toString(status));
    output.append(",message=").append(message);
    output.append(')');

    return output;
  }
}
