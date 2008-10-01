/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
import com.scalagent.task.*;

/**
 * <code>Task</code> whose goal is to move or duplicate a file, allowing for
 * different source and target file systems.
 * <p>
 * This agent uses a <code>MoveDriver</code> object which performs the task
 * in a separate thread, as the copy may be long when the target file system
 * differs from the source one.
 *
 * @see		MoveDriver
 */
public class MoveFile extends ThreadTask {
  /** input file description */
  public File input = null;

  /** output file description */
  public File output = null;

  /** if true duplicates the file */
  public boolean duplicate = false;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param input	the source file
   * @param output	the target file
   */
  public MoveFile(short to, AgentId parent, File input, File output) {
    super(to, parent);
    this.input = input;
    this.output = output;
    this.duplicate = false;
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param input	the source file
   * @param output	the target file
   * @param duplicate	if true makes a copy
   */
  public MoveFile(short to, AgentId parent,
                  File input, File output,
                  boolean duplicate) {
    super(to, parent);
    this.input = input;
    this.output = output;
    this.duplicate = duplicate;
  }
  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append('(');
    buf.append(super.toString());
    buf.append(",input=");
    buf.append(input);
    buf.append(",output=");
    buf.append(output);
    buf.append(')');
    return buf.toString();
  }

  /**
   * Creates a derived class specific driver.
   *
   * @return	a driver, of a class derived from <code>ThreadTaskDriver</code>
   */
  protected ThreadTaskDriver createDriver() throws Exception {
    return new MoveDriver(getId(), input, output, duplicate);
  }
}
