/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */
package fr.dyade.aaa.task;

import java.io.*;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.task.Task.Status;

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
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: ThreadTaskDriver.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

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
    this.logmon = Debug.getMonitor("fr.dyade.aaa.task");
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
