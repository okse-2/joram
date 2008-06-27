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
 * <code>Notification</code> reporting the end of a process execution.
 *
 * @see		ProcessManager
 */
public class ProcessEnd extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** as returned by <code>Process.exitValue</code> */
  private int exitValue;
  /** optional, as returned by <code>Process.getErrorStream</code> */
  private String errorMessage;

  /**
   * Creates a notification to be sent.
   *
   * @param exitValue		as returned by <code>Process.exitValue</code>
   * @param errorMessage	optional, as returned by <code>Process.getErrorStream</code>
   */
  public ProcessEnd(int exitValue, String errorMessage) {
    this.exitValue = exitValue;
    this.errorMessage = errorMessage;
  }

  /**
   * Accesses read only property.
   *
   * @return		as returned by <code>Process.exitValue</code>
   */
  public int getExitValue() { return exitValue; }

  /**
   * Accesses read only property.
   *
   * @return		optional, as returned by <code>Process.getErrorStream</code>
   */
  public String getErrorMessage() { return errorMessage; }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",exitValue=").append(exitValue);
    output.append(",errorMessage=").append(errorMessage);
    output.append(')');

    return output;
  }
}

