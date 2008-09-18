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
 * The main use of <code>Monitor</code> objects is to provide for handling
 * an asynchronous <code>IndexedCommand</code> <code>IndexedReport</code>
 * communication without creating additional and temporary
 * <code>ServiceTask</code> or <code>Composed</code> agents. Class
 * <code>CommandMonitor</code> provides a base implementation
 * for this use of <code>Monitor</code>s.
 * <p>
 * A <code>CommandMonitor</code> object holds an <code>IndexedCommand</code>
 * notification and a target agent id, and sends the notification to the agent
 * when <code>start</code> is called. The associated <code>IndexedReport</code>
 * notification is forwarded back to the <code>Monitor</code> object by its
 * enclosing agent calling <code>commandReport</code>.
 */
public class CommandMonitor extends ObjectMonitor {
  /** target agent for command */
  AgentId target;
  /** command to send to target */
  IndexedCommand command;

  /**
   * Constructor.
   *
   * @param parent	object to report status to
   * @param target	target agent for command
   * @param command	command to send to target
   */
  public CommandMonitor(MonitorParent parent,
			AgentId target,
			IndexedCommand command) {
    super(parent);
    this.target = target;
    this.command = command;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",target=" + target +
      ",command=" + command + ")";
  }

  /**
   * Starts monitor execution.
   */
  public void start() throws Exception {
    setStatus(Status.RUN);
    sendTo(this, target, command);
  }
  
  /**
   * Reacts to a the command report from target agent.
   *
   * @param report	report from agent
   */
  public void commandReport(IndexedReport report) throws Exception {
    switch (report.getStatus()) {
    case Status.DONE:
      setReturnValue(report);
      break;
    case Status.FAIL:
      setErrorMessage(report.getErrorMessage());
      break;
    }
    setStatus(report.getStatus());
  }
}
