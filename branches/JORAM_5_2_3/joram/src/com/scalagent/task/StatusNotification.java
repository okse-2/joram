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
package com.scalagent.task;

import java.io.*;
import java.util.*;
import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task.Status;

/**
 * Notification reporting a status change in a <code>Task</code> agent.
 * <p>
 * This object identifies the <code>Task</code> agent changing status,
 * the new status, an optional message mainly used when the new status is
 * <code>FAIL</code>, and an optional result object when the new status is
 * <code>DONE</code>.
 * <p>
 * The result value is provided as an <code>Object</code> object, which
 * of course is specialized for specific <code>Task</code> agents. That
 * requires all agent servers receiving this notification to be able to
 * deserialize that specialized result class. It is then recommended to
 * send to registered status listeners of a <code>Task</code> agent a
 * degenerated notification omitting the result object.
 *
 * @see		Task
 */
public class StatusNotification extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** date of status change */
  private Date date;
  /** task which changed status */
  private AgentId task;
  /** new status */
  private int status;
  /** optional message */
  private String message;
  /** optional result value when status is DONE */
  private Object result;

  /**
   * Creates a notification to be sent.
   *
   * @param task		task which changed status
   * @param status		new status
   * @param message		optional message
   * @param result		optional result value
   */
  public StatusNotification(AgentId task, int status, String message, Object result) {
    this.date = new Date();
    this.task = task;
    this.status = status;
    this.message = message;
    this.result = result;
  }

  /**
   * Creates a notification to be sent with null result object.
   *
   * @param task		task which changed status
   * @param status		new status
   * @param message		optional message
   */
  public StatusNotification(AgentId task, int status, String message) {
    this(task, status, message, null);
  }

  /**
   * Accesses read only property.
   *
   * @return		date of status change
   */
  public Date getDate() { return date; }

  /**
   * Accesses read only property.
   *
   * @return		task which changed status
   */
  public AgentId getTask() { return task; }

  /**
   * Accesses read only property.
   *
   * @return		new status
   */
  public int getStatus() { return status; }

  /**
   * Accesses read only property.
   *
   * @return		optional message
   */
  public String getMessage() { return message; }

  /**
   * Accesses property.
   *
   * @return		optional result value
   */
  public Object getResult() { return result; }

  /**
   * Sets property.
   *
   * @param result	optional result value
   */
  public void setResult(Object result) {
    this.result = result;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",date=").append(date);
    output.append(",task=").append(task);
    output.append(",status=").append(Status.toString(status));
    output.append(",message=").append(message);
    output.append(",result=").append(result);
    output.append(')');

    return output;
  }
}
