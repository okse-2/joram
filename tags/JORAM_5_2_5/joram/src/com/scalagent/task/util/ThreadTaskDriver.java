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

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;
import com.scalagent.task.Task.Status;

/**
 * This class specializes the <code>Driver</code> class to be used by
 * an associated <code>ThreadTask</code> agent.
 * <p>
 * The <code>run</code> function is still declared abstract and should be
 * defined in subclasses. Successful completion of the function is detected
 * by the <code>errorMessage</code> variable unchanged, at <code>null</code>.
 * The <code>run</code> function updating the <code>errorMessage</code>
 * variable to a not null value signals a failed execution. When the driver
 * terminates, a <code>StatusNotification</code> notification is sent to the
 * associated task agent. Status is <code>DONE</code> or <code>FAIL</code>,
 * depending on the value of the <code>errorMessage</code> variable.
 *
 * @see		ThreadTask
 */
public abstract class ThreadTaskDriver extends Driver {
  /** id of associated <code>ThreadTask</code> agent */
  protected AgentId task;

  /** when <code> the driver has been asked to stop */
  public volatile boolean stop = false;

  /**
   * if this variable is not null when the driver ends,
   * a <code>FAIL</code> status is sent to the task agent
   */
  protected String errorMessage = null;

  /**
   * Constructor.
   *
   * @param task	id of associated <code>ThreadTask</code> agent
   */
  protected ThreadTaskDriver(AgentId task) {
    super();
    this.task = task;
    // Get the logging monitor from current server MonologMonitorFactory.
    this.logmon = Debug.getLogger("fr.dyade.aaa.task");
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",task=" + task +
      ",stop=" + stop +
      ",errorMessage=" + errorMessage + ")";
  }

  /**
   * Actually executes the driver code.
   * To be defined in derived classes.
   * <p>
   * Beware: this code is executed in a separate thread, outside from any
   * transaction. Notifications may be sent using function <code>sendTo</code>,
   * and they will actually be sent as soon as the function is called; there is
   * no atomic treatment as there is in an agent reaction.
   * <p>
   * May update <code>errorMessage</code>, a not null value meaning failure.
   * <p>
   * Should regularly check for variable <code>stop</code>, and exit the thread
   * when it is <code>true</code>.
   */
  public abstract void run() throws Exception;

  /**
   * Finalizes the driver.
   *
   * Reports driver end to the associated task agent,
   * with a <code>DriverDone</code> notification.
   */
  protected void end() {
    // report end to task
    try {
      if (stop) {
	sendTo(task, new StatusNotification(null, Status.STOP, errorMessage));
      } else if (errorMessage == null) {
	sendTo(task, new StatusNotification(null, Status.DONE, null));
      } else {
	sendTo(task, new StatusNotification(null, Status.FAIL, errorMessage));
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", error in reporting end", exc);
    }
  }
}
