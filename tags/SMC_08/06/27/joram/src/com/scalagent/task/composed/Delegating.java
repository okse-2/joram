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
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.Condition;
import com.scalagent.task.*;

/**
 * <code>Task</code> whose task is partialy realized by dynamically created
 * sub-tasks.
 * <p>
 * Sub-tasks are associated with keys, enabling derived classes to recognize
 * a sub-task from the key. The key need not be unique, it identifies a category
 * of sub-tasks from the point of vue of the parent task.
 * <p>
 * Class <code>Delegating</code> is designed to be derived, the derived class
 * using the <code>addChild</code> function to create sub-tasks, and defining
 * the <code>delegatingStart</code> and <code>childDone</code> functions to
 * realize the derived class objective.
 * <p>
 * The <code>Delegating</code> class could be the base class of
 * <code>Composed</code>.
 */
public abstract class Delegating extends Task {
  /** sub-tasks as KTaskHandle objects */
  protected Vector children;

  /**
   * Empty constructor.
   */
  public Delegating() {
    super();
    children = new Vector();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   */
  public Delegating(short to, AgentId parent) {
    super(to, parent);
    children = new Vector();
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
    result.append(",children=");
    if (children == null) {
      result.append("null");
    } else {
      result.append("(");
      result.append(children.size());
      for (int i = 0; i < children.size(); i ++) {
	result.append(",");
	result.append((KTaskHandle) children.elementAt(i));
      }
      result.append(")");
    }
    result.append(")");
    return result.toString();
  }

  /**
   * Creates and starts a child task.
   *
   * Creates child only when this task is running.
   *
   * @param child	child task
   * @param key		key associated with child
   */
  public void startChild(Task child, int key) throws Exception {
    child.setParent(getId());
    child.deploy();
    sendTo(child.getId(), new ResetNotification());

    KTaskHandle handle = new KTaskHandle(child.getId(), key);
    children.addElement(handle);

    switch (getStatus()) {
    case Status.INIT:
    case Status.RUN:
      sendTo(child.getId(), new Condition());
      handle.status = Status.RUN;
      break;
    }
  }

  /**
   * Reacts to <code>Delegating/code> specific notifications.
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
	doReact(from, (StatusNotification) not);
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
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, StatusNotification not) throws Exception {
    KTaskHandle handle = null;
    int i = children.size();
    while (i-- > 0) {
      handle = (KTaskHandle) children.elementAt(i);
      if (handle.id.equals(not.getTask()))
	break;
    }
    if (i < 0)
      throw new IllegalArgumentException("StatusNotification from no child");

    childStatus(handle, not);
  }

  /**
   * Resets a <code>Task</code> agent ready for execution.
   */
  public void reset() throws Exception {
    // deletes all existing sub-tasks
    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      sendTo(handle.id, new DeleteNot());
      children.removeElementAt(i);
    }

    super.reset();
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
   * <p>
   * This function detects a restart case from the existency of sub-tasks.
   * In that case it restarts the sub-tasks. Otherwise it calls function
   * <code>delegatingStart</code>, which should be provided in derived classes.
   * A derived class should not overload <code>start</code>.
   */
  protected void start() throws Exception {

    setStatus(Status.INIT);

    if (children.size() == 0) {
      // actual start
      delegatingStart();
      return;
    }

    // restart case
    // restarts all registered subtasks
    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      switch (handle.status) {
      case Status.DONE:
	// deletes sub-task
	sendTo(handle.id, new DeleteNot());
	children.removeElement(handle);

	// continues this task
	// calls derived class specific code
	childDone(handle);
	break;
      case Status.WAIT:
	sendTo(handle.id, new Condition());
	handle.status = Status.RUN;
	break;
      default:
        logmon.log(BasicLevel.ERROR,
                   getName() + ", bad sub-task #" + i +
                 " [status=" + Status.toString(handle.status) + ']');
	break;
      }
    }
  }

  /**
   * Actually starts execution.
   * Abstract function to be defined in derived classes.
   */
  protected abstract void delegatingStart() throws Exception;

  /**
   * Stops task execution.
   * This function must ensure that
   * <code>setStatus(Status.DONE/FAIL/STOP)</code> is eventually called.
   */
  protected void taskStop() throws Exception {
    // sends notification to all started sub-tasks
    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      if (handle.status == Status.INIT ||
	  handle.status == Status.RUN) {
	sendTo(handle.id, new EventKill());
      }
    }
  }

  /**
   * Analyzes child status.
   * <p>
   * When a child fails, this task waits for the others to complete before
   * reporting failure. If any dependency exists between sub-tasks, this
   * function should be overloaded to call stop.
   *
   * @param child	child handle
   * @param not		notification from child
   */
  protected void childStatus(KTaskHandle child, StatusNotification not) throws Exception {
    if (not.getStatus() == Status.WAIT &&
	child.status == Status.INIT) {
      // ignores outdated WAIT status
      return;
    }
    child.status = not.getStatus();

    switch (child.status) {
    default:
      // ignore
      return;
    case Status.RUN:
      if (getStatus() == Status.INIT)
	setStatus(Status.RUN);
      return;
    case Status.DONE:
      child.result = not.getResult();
      // if this task has been required to be killed, keeps the sub-task handle
      // in DONE status so that childDone may be called on restart.
      if (getStatus() == Status.RUN) {
	// deletes sub-task
	sendTo(child.id, new DeleteNot());
	children.removeElement(child);

	// continues this task
	// calls derived class specific code
	childDone(child);
      }
      break;
    case Status.FAIL:
    case Status.STOP:
      String message = getErrorMessage();
      if (message == null)
	message = "";
      message += "<" + child.key + "> " + not.getMessage();
      setErrorMessage(message);
      break;
    }

    // a sub-task has terminated, checks for this task termination
    checkForEnd();
  }

  /**
   * Checks for this task termination. This task waits for all sub-tasks
   * to terminate, either correctly or in error, before changing to a
   * terminal status. It changes to DONE status only when no sub-tasks
   * remain. A sub-task with DONE status means that this task has been stopped.
   */
  protected void checkForEnd() throws Exception {
    if (children.size() == 0) {
      setStatus(Status.DONE);
      return;
    }

    boolean childWait = false;
    boolean childRun = false;
    boolean childDone = false;
    boolean childFail = false;
    boolean childStop = false;

    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      switch (handle.status) {
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
      return;
    }

    // no sub-task is running, sub-tasks in WAIT status may remain
    if (childFail) {
      // a sub-task has failed, which makes this task fail
      setStatus(Status.FAIL);
      return;
    }

    if (childStop || childDone) {
      // a sub-task has stopped, or this task has been stopped
      setStatus(Status.STOP);
      return;
    }

    // a waiting sub-task remains
    if (getStatus() == Status.KILL) {
      setStatus(Status.STOP);
    }
  }

  /**
   * Continues this task execution when a sub-task completes.
   * <p>
   * The possible sub-task result object may be found in the handle.
   *
   * @param handle	child handle
   */
  protected abstract void childDone(KTaskHandle child) throws Exception;

  /**
   * Deletes this agent.
   * <p>
   * Deletes all children agents.
   */
  protected void delete(AgentId agent) {
    super.delete(agent);

    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      sendTo(handle.id, new DeleteNot());
      children.removeElementAt(i);
    }
  }

  /**
   * Restarts this agent execution.
   */
  protected void restart() throws Exception {
    // resets all failed or stopped sub-tasks
    for (int i = children.size(); i-- > 0;) {
      KTaskHandle handle = (KTaskHandle) children.elementAt(i);
      if (handle.status == Status.FAIL ||
	  handle.status == Status.STOP) {
	sendTo(handle.id, new Condition(false));
	sendTo(handle.id, new RestartNotification());
	handle.status = Status.WAIT;
      }
    }

    super.restart();
  }
}
