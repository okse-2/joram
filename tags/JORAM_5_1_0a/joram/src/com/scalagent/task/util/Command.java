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

/**
 * Notification describing a service task.
 * The task is to be performed by an agent, as for a <code>Task</code> agent,
 * except that the performing agent may react to multiple commands, and
 * the particular task is identified by this notification and not by an agent.
 * Task end is reported via a <code>Report</code> notification.
 *
 * @see		Report
 */
public class Command extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** agent to report completion status to */
  private AgentId report;

  /**
   * Default constructor.
   */
  public Command() {
    this(null);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param report	agent to report status to
   */
  public Command(AgentId report) {
    this.report = report;
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
  public AgentId getReport() { return report; }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",report=").append(report);
    output.append(')');

    return output;
  }
}
