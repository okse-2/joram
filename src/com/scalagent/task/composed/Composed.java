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
package com.scalagent.task.composed;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.Condition;
import com.scalagent.task.*;

/**
 * <code>Task</code> whose goal is to perform sub-tasks.
 * This is an abstract class. Derived classes define sub-tasks order of
 * execution and rules for parameter passing.
 * <p>
 * Sub-task agents, be they given as a constructor parameter or by the
 * function <code>setChild</code>, must have been created with this agent
 * as parent.
 *
 * @see		Sequential
 */
public abstract class Composed extends Task {
  /** parameter for this task */
  Object parameter = null;
  /** sub-tasks */
  protected TaskHandle child[];

  /**
   * Default constructor
   */
  public Composed() {
    super();
  }

 /**
   * Invoked by the Configurator
   */
  public Composed(short to, String name) {
    super(to, name);
  }  

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param child	children tasks ids, maybe null and set by
   *			<code>setChild</code>
   */
  public Composed(short to, AgentId parent, AgentId child[]) {
    super(to, parent);
    this.child = null;
    if (child != null)
      setChild(child);
  }

  /**
   * Sets this task's sub-tasks.
   * <p>
   * Must be called before this agent is deployed.
   *
   * @param child	children tasks ids
   */
  public void setChild(AgentId child[]) {
    this.child = new TaskHandle[child.length];
    for (int i = 0; i < child.length; i ++) {
      this.child[i] = new TaskHandle(child[i]);
      sendTo(child[i], new SetParent(getId()));
    }
  }

  /**
   * Provides a string image for this object.
   *
   * @return	string image for this object
   */
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("(");
    result.append(super.toString());
    result.append(",parameter=");
    result.append(parameter);
    result.append(",child=");
    if (child == null) {
      result.append("null");
    } else {
      result.append("(");
      result.append(child.length);
      for (int i = 0; i < child.length; i ++) {
	result.append(",");
	result.append(child[i]);
      }
      result.append(")");
    }
    result.append(")");
    return result.toString();
  }

  /**
   * Reacts to <code>Composed</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>StatusNotification</code> from sub-tasks.
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
   * Reacts to <code>StatusNotification</code> notifications from sub-tasks.
   * Calls <code>childStatus</code>.
   *
   * @param not		notification to react to
   */
  public void doReact(StatusNotification not) throws Exception {
    int i = child.length;
    while (i-- > 0) {
      if (child[i].id.equals(not.getTask()))
	break;
    }
    if (i < 0)
      throw new IllegalArgumentException("StatusNotification from no child");
    childStatus(i, not);
  }

  /**
   * Resets a <code>Task</code> agent ready for execution.
   * Checks for a <code>Task</code> agent with no condition to start.
   */
  public void reset() throws Exception {

    // resets all sub-tasks handles
    for (int i = child.length; i-- > 0;) {
      sendTo(child[i].id, new ResetNotification());
      child[i].status = Status.WAIT;
    }

    super.reset();
  }

  /**
   * Sets the parameter value for this task.
   * <p>
   * Keeps the parameter so that it may be used by the derived class
   * specific <code>startSubtasks</code> function.
   *
   * @param parameter	parameter for this task
   */
  protected void setParameter(Object parameter) throws Exception {
    this.parameter = parameter;
  }

  /**
   * Starts program execution, overloads <code>start</code> from base class.
   * Calls <code>startSubtasks</code>.
   * <p>
   * This function must start calling <code>setStatus(Status.INIT&RUN)</code>,
   * and ensure that <code>setStatus(Status.DONE/FAIL)</code> is eventually
   * called.
   * This function is also called when the execution conditions of a restarted
   * task come true.
   */
  protected void start() throws Exception {
    if (child.length == 0)
      throw new IllegalStateException("Composed task with no sub-task");

    setStatus(Status.INIT);

    // starts subtasks ready for execution
    startSubtasks();
  }

  /**
   * Stops task execution.
   * This function must ensure that <code>setStatus(Status.DONE/FAIL/STOP)</code>
   * is eventually called.
   */
  protected void taskStop() throws Exception {
    // sends notification to all started sub-events
    for (int i = child.length; i-- > 0;) {
      if (child[i].status == Status.INIT ||
	  child[i].status == Status.RUN) {
	sendTo(child[i].id, new EventKill());
      }
    }
  }

  /**
    * Starts sub-tasks that can be started (all their dependent tasks
    * have completed). Abstract function to be defined in derived classes.
    */
  protected abstract void startSubtasks() throws Exception;

  /**
   * Analyzes child status.
   *
   * @param i		index of child
   * @param not		notification from child
   */
  protected void childStatus(int i, StatusNotification not) throws Exception {
    if (not.getStatus() == Status.WAIT &&
	child[i].status == Status.INIT) {
      // ignores outdated WAIT status
      return;
    }
    child[i].status = not.getStatus();

    switch (child[i].status) {
    default:
      // ignore
      return;
    case Status.RUN:
      if (getStatus() == Status.INIT)
	setStatus(Status.RUN);
      return;
    case Status.DONE:
      child[i].result = not.getResult();
      break;
    case Status.FAIL:
    case Status.STOP:
      String message = getErrorMessage();
      if (message == null)
	message = "";
      message += "<" + i + "> " + not.getMessage();
      setErrorMessage(message);
      break;
    }

    // a sub-task has terminated, check for this task termination
    // start sub-tasks which depend on the completed one
    if (!checkForEnd())
      startSubtasks();
  }

  /**
   * Checks for this task termination. This task waits for all sub-tasks
   * to terminate, either correctly or in error, before changing to a
   * terminal status. It changes to DONE status only when all sub-tasks
   * have completed with a DONE status. The associated result value is
   * build in derived class overloaded <code>buildResult</code> function.
   * <p>
   * This function signals by a <code>true</code> return value a failed subtask
   * (or any other reason) which prevents this <code>Composed</code> task
   * from continuing.
   *
   * @return		true if execution should not continue
   */
  protected boolean checkForEnd() throws Exception {
    boolean childWait = false;
    boolean childRun = false;
    boolean childDone = false;
    boolean childFail = false;
    boolean childStop = false;

    for (int i = child.length; i-- > 0;) {
      switch (child[i].status) {
      case Status.WAIT:
	childWait = true;
	break;
      case Status.INIT:
      case Status.RUN:
      case Status.KILL:
	childRun = true;
	break;
      case Status.DONE:
	childDone = true;
	break;
      case Status.FAIL:
	childFail = true;
	break;
      case Status.STOP:
	childStop = true;
	break;
      }
    }

    if (childRun) {
      // one sub-task is still running
      // wait for sub-task to terminate before changing status
      if (getStatus() == Status.KILL) {
	// stops this task execution
	return true;
      }
      return false;
    }

    // no sub-task is running, sub-tasks in WAIT status may remain
    if (childFail) {
      // a sub-task has failed, which makes this task fail
      setStatus(Status.FAIL);
      return true;
    }

    if (childStop) {
      // a sub-task has stopped, the others may be in WAIT or DONE status
      setStatus(Status.STOP);
      return true;
    }

    // all sub-tasks are in WAIT or DONE status
    if (childWait) {
      if (getStatus() == Status.KILL) {
	setStatus(Status.STOP);
	return true;
      }
      // continues this task execution
      return false;
    }

    // all sub-tasks are in DONE status
    setStatus(Status.DONE);
    return true;
  }

  /**
   * Deletes this agent.
   *
   * Deletes all children agents.
   */
  protected void delete(AgentId agent) {
    super.delete(agent);
    for (int i = 0; i < child.length; i ++)
      sendTo(child[i].id, new DeleteNot());
  }

  /**
   * Restarts this agent execution.
   */
  protected void restart() throws Exception {
    // resets all failed or stopped sub-tasks
    for (int i = 0; i < child.length; i ++) {
      if (child[i].status == Status.FAIL ||
	  child[i].status == Status.STOP) {
	sendTo(child[i].id, new Condition(false));
	sendTo(child[i].id, new RestartNotification());
	child[i].status = Status.WAIT;
      }
    }

    super.restart();
  }
}
