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
package com.scalagent.scheduler.monitor;

import java.io.*;

import fr.dyade.aaa.agent.*;

/**
  * Notification describing a service task.
  * The task is to be performed by a <code>MonitorAgent</code> agent.
  * Task end is reported via an <code>IndexedReport</code> notification.
  * <p>
  * This class is a variation of <code>Command</code>, which it may eventually
  * replace.
  *
  * @see	IndexedReport
  * @see	Monitor
  * @see	Command
  */
public class IndexedCommand extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** identifier local to agent issuing command */
  private int id;

  /**
   * Creates a notification to be sent.
   */
  public IndexedCommand() {
    this.id = 0;
  }

  /**
   * Accesses property.
   * Allows differed setting of <code>id</code> variable.
   * Does not allow changing the variable.
   *
   * @param id		command identifier
   */
  public void setId(int id) throws Exception {
    if (this.id != 0) {
      throw new IllegalArgumentException("cannot change id: " + this);
    }
    this.id = id;
  }

  /**
   * Accesses property.
   *
   * @return		command identifier
   */
  public int getId() { return id; }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",id=").append(id);
    output.append(')');

    return output;
  }
}
