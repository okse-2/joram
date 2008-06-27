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

import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;

/**
 * <code>Task</code> whose goal is to execute a function in a separate thread.
 * <p>
 * Multithreading in the agent server is special, as the code executed
 * outside of the main engine thread is no longer part of a transaction.
 * The class <code>Driver</code> has been designed to help programming in
 * a separate thread. This is why the function executed by the
 * <code>ThreadTask</code> agent must be declared as the <code>run</code>
 * function of class <code>ThreadTaskDriver</code>, derived from
 * <code>Driver</code>. Notifications may be sent from this function by calling
 * the <code>sendTo</code> function of class <code>Driver</code>.
 * <p>
 *
 * @see		ThreadTaskDriver
 */
public class ThreadTask extends Task {
  /** associated driver */
  protected transient ThreadTaskDriver driver = null;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   */
  public ThreadTask(short to, AgentId parent) {
    super(to, parent);
    setFixed(true);
  }
  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append('(');
    buf.append(super.toString());
    buf.append(",command=");
    if (command == null) {
      buf.append(command);
    } else {
      buf.append('(');
      buf.append(command.length);
      for (int i = 0; i < command.length; i ++) {
	buf.append(',');
	buf.append(command[i]);
      }
      buf.append(')');
    }
    buf.append(')');
    return buf.toString();
  }
   */

  /**
   * Initializes the transient members of this agent.
   * This function is first called by the factory agent,
   * then by the system each time the agent server is restarted.
   *
   * This function is not declared final so that derived classes
   * may change their reload policy.
   *
   * @param firstTime		true when first called by the factory
   */
  public void scbInitialize(boolean firstTime) throws Exception {
    super.scbInitialize(firstTime);
    // checks for an agent server failure while agent was executing
    switch (getStatus()) {
    case Status.RUN:
      setErrorMessage("unknown thread end due to server failure");
      setStatus(Status.FAIL);
      break;
    }
  }

  /**
   * Reacts to <code>ThreadTask</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>StatusNotification</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof StatusNotification) {
	doReact((StatusNotification) not);
        return;
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", exception in " +
                 toString() + ".react(" + not + ")", exc);
      setErrorMessage(exc.toString());
      try {
	setStatus(Status.FAIL);
      } catch (Exception exc1) {
	logmon.log(BasicLevel.ERROR, getName() + ", cannot set status", exc1);
      }
      return;
    }
    super.react(from, not);
  }

  /**
   * Reacts to <code>StatusNotification</code> notifications.
   * Calls <code>driverStatus</code>.
   *
   * @param not		notification to react to
   */
  public void doReact(StatusNotification not) throws Exception {
    driverStatus(not);
  }

  /**
   * Starts driver execution, overloads <code>start</code> from base class.
   * <p>
   * If the <code>driver</code> variable has not been previously set,
   * calls first <code>createDriver</code>.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", start " + this);
    if (driver == null)
      driver = createDriver();
    if (driver == null) {
      // assumes there is nothing to execute
      setStatus(Status.DONE);
      return;
    }
    driver.start();
  }

  /**
   * Creates a derived class specific driver.
   * <p>
   * The base class implementation returns <code>null</code>.
   *
   * @return	a driver, of a class derived from <code>ThreadTaskDriver</code>
   */
  protected ThreadTaskDriver createDriver() throws Exception {
    return null;
  }

  /**
   * Analyzes driver's report.
   *
   * @param not		the driver's report
   */
  protected void driverStatus(StatusNotification not) throws Exception {
    if (not.getStatus() == Status.DONE) {
      setStatus(Status.DONE);
    } else {
      setErrorMessage(not.getMessage());
      if (getStatus() == Status.KILL)
	setStatus(Status.STOP);
      else
	setStatus(Status.FAIL);
    }
    driver = null;
  }

  /**
   * Stops task execution, must be defined in derived classes.
   * This function must ensure that
   * <code>setStatus(Status.DONE/FAIL/STOP)</code> is eventually called.
   */
  protected void taskStop() throws Exception {
    // called only in INIT or RUN status
    // driver is not null
    driver.stop = true;
  }
}
