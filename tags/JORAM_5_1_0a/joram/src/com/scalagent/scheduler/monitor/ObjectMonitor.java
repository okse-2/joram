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
package com.scalagent.scheduler.monitor;

import java.io.*;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task.Status;

/**
  * This is the base class for <code>Monitor</code> objects inside an agent.
  */
public abstract class ObjectMonitor implements Monitor {
  /** object to report status to */
  private MonitorParent parent;
  /** execution status (<code>Status.*</code>) */
  private int status;
  /** optional, when status is <code>DONE</code> */
  private transient Object returnValue;
  /** optional, when status is <code>FAIL</code> */
  private transient String errorMessage;

  /**
   * Constructor.
   * Initializes <code>status</code> to <code>NONE</code>.
   *
   * @param parent	object to report status to
   */
  public ObjectMonitor(MonitorParent parent) {
    this.parent = parent;
    status = Status.NONE;
    returnValue = null;
    errorMessage = null;
  }

  /**
   * Property accessor.
   * Changes this <code>Monitor</code> status and signals new status to parent.
   *
   * @param status	new status
   */
  protected void setStatus(int status) throws Exception {
    if (status == this.status)
      return;
    this.status = status;
    // signals new status
    parent.childReport(this, status);
  }

  /**
   * Property accessor.
   *
   * @return		execution status
   */
  public int getStatus() { return status; }

  /**
   * Read only property accessor.
   *
   * @return		object to report status to
   */
  protected MonitorParent getParent() { return parent; }

  /**
   * Property accessor.
   *
   * @errorMessage	error message
   */
  protected void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Property accessor.
   *
   * @return		optional error message
   */
  public String getErrorMessage() { return errorMessage; }

  /**
   * Property accessor.
   *
   * @returnValue	return value
   */
  protected void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }

  /**
   * Property accessor.
   *
   * @return		optional return value
   */
  public Object getReturnValue() { return returnValue; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",status=" + Status.toString(status) +
      ",returnValue=" + returnValue +
      ",errorMessage=" + errorMessage + ")";
  }

  /**
   * Starts monitor execution.
   */
  public abstract void start() throws Exception;

  /**
   * Allows a enclosed <code>CommandMonitor</code> object to send a
   * <code>Command</code>. Registers the object so that it can be forwarded
   * the <code>Report</code> to.
   * <p>
   * Actual implementation is provided by enclosing agent.
   * <p>
   * This function is not necessary in <code>Monitor</code> interface.
   * However this simple implementation is needed in all <code>Monitor</code>
   * objects which are <code>Monitor</code> parents.
   *
   * @param monitor	object sending the command
   * @param to		agent target of command
   * @param command	command to send
   */
  protected void sendTo(CommandMonitor monitor, AgentId to, IndexedCommand command) throws Exception {
    parent.sendTo(monitor, to, command);
  }
}
