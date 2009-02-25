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
package com.scalagent.task;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;

/**
 * Abstract base class for agents whose goal is to perform a single task.
 * <p>
 * A <code>Task</code> agent holds a <code>status</code> variable reporting
 * the execution status of the agent task. The agent starts in <code>NONE</code>
 * status when deployed. When it receives a <code>ResetNotification</code>
 * notification it gets ready for execution and switches to <code>WAIT</code>
 * status. It then waits for its activation condition to come true, usually by
 * waiting for <code>Condition</code> notifications, and switches to
 * <code>RUN</code> status. An agent with no condition to wait for switches to
 * <code>RUN</code> status immediately after <code>WAIT</code>.
 * <p>
 * When the agent task execution completes, the agent switches to
 * <code>DONE</code> status. If it fails it switches to <code>FAIL</code>
 * status. If the agent task execution is asked stop, via a
 * <code>EventKill</code> notification, the agent status switches to
 * <code>KILL</code>, acknowledging the stop request, then to <code>STOP</code>,
 * when execution has actually stopped.
 * <p>
 * An agent in <code>FAIL</code> or <code>STOP</code> status may be restarted
 * by it being sent a <code>RestartNotification</code> notification. The task
 * execution restarts at the point it was stopped, assuming the origin of the
 * execution stopping has been corrected. An agent in <code>FAIL</code>,
 * <code>STOP</code>, or <code>DONE</code> status may also be resetted to
 * restart the task execution from the beginning, by it being sent a
 * <code>ResetNotification</code> notification.
 * <p>
 * Basic support is provided for allowing input and output parameters for tasks.
 * An untyped input parameter may be given to an agent via a
 * <code>ParamNotification</code> notification. The optional untyped result
 * is given as a member of the final <code>StatusNotification</code>
 * notification with status <code>DONE</code>.
 * <p>
 * A <code>Task</code> agent usually has a parent agent which
 * it reports status changes to, and which is responsible for its deletion.
 * A child task has a default start condition from its parent, null named,
 * which may be built by the parent using the <code>Condition</code> default
 * constructor.
 * <p>
 * The <code>Task</code> class is designed to be inherited. A derived class
 * should provide a constructor and overload the functions <code>start</code>
 * and <code>taskStop</code>. It may also overload the functions
 * <code>setParameter</code> and <code>buildResult</code>. It is also strongly
 * advised that the function <code>toString</code> be overloaded.
 * <p>
 * An example use of a <code>Task</code> derived agent follows :
 * <br> ConditionHandle[] conditions = new ConditionHandle[1];
 * <br> conditions[1] = new ConditionHandle("mystart", null);
 * <br> Task task = new MyTask(AgentServer.getServerId(), null, conditions);
 * <br> task.deploy();
 * <br> sendTo(task.getId(), new ResetNotification());
 * <br> sendTo(task.getId(), new Condition("mystart"));
 *
 * @see		StatusNotification
 */
public abstract class Task extends Agent {
  /**
   * Returns default log topic for agents. Its method should be overridden
   * in subclass in order to permit fine configuration of logging system.
   * By default it returns <code>fr.dyade.aaa.SCBean</code>.
   */
  protected String getLogTopic() {
    return "fr.dyade.aaa.task";
  }

  public static class Status {
    /** initializing */
    public static final int NONE = 0;
    /** ready */
    public static final int WAIT = 1;
    /** acknowledge start order */
    public static final int INIT = 2;
    /** running */
    public static final int RUN = 3;
    /** completed */
    public static final int DONE = 4;
    /** failed */
    public static final int FAIL = 5;
    /** acknowledge kill order */
    public static final int KILL = 6;
    /** stopped */
    public static final int STOP = 7;

    /**
     * String description of statuses.
     * Entry index must match status int value.
     */
    private static final String[] messages = {
      "NONE", "WAIT", "INIT", "RUN", "DONE", "FAIL", "KILL", "STOP"};
    
    /**
     * Provides a string image for parameter. Uses <code>messages</code>.
     *
     * @return		string image for parameter
     */
    public static String toString(int status) {
      return messages[status];
    }
  }

  /**
   * if <code>true</code>, traces errors in debug trace file;
   * may be set using property <code>Debug.var.fr.dyade.aaa.task.Task.error</code>.
   * Default value is <code>true</code>.
   */
  public static boolean error = true;

  /**
   * if <code>true</code>, traces task execution in debug trace file;
   * may be set using property <code>Debug.var.fr.dyade.aaa.task.Task.exec</code>.
   * Default value is <code>false</code>.
   */
  public static boolean exec = false;

  /** execution status (Status.*) */
  private int status;
  /** agent to report status to */
  private AgentId parent;
  /** notifications to wait for */
  private ConditionHandle[] conditions;
  /** optional, when status is FAIL */
  private String errorMessage;

  /** listeners of status notifications */
  public final ListenerSet statusListeners = new ListenerSet();

  /**
   * Default constructor
   */
  public Task() {
    super();
    status = Status.NONE;
    parent = null;
    conditions = null;
    errorMessage = null;
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	symbolic name
   * @param parent	agent to report status to
   * @param conditions	notifications to wait for
   */
  public Task(short to, String name, AgentId parent, ConditionHandle[] conditions) {
    super(to, name);
    status = Status.NONE;
    this.parent = parent;
    errorMessage = null;
    this.conditions = null;
    setConditions(conditions);
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param conditions	notifications to wait for
   */
  public Task(short to, AgentId parent, ConditionHandle[] conditions) {
    this(to, null, parent, conditions);
  }

  /**
   * Creates an agent to be deployed remotely with null conditions.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   */
  public Task(short to, AgentId parent) {
    this(to, null, parent, null);
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	symbolic name
   */
  public Task(short to, String name) {
    this(to, name, null, null);
  }

  /**
   * Property accessor.
   * Signals a <code>StatusNotification</code> notification with the new status
   * to the parent agent and all status listeners.
   * <p>
   * When new status is <code>DONE</code> the <code>buildResult</code>
   * function is called to build the derived class specific result object.
   *
   * @param status	new status
   */
  protected void setStatus(int status) throws Exception {
    if (status == this.status)
      return;

    // sets new status
    this.status = status;

    // signals new status to parent
    StatusNotification newStatus = new StatusNotification(
      getId(), status, errorMessage);
    if (status == Status.DONE)
      newStatus.setResult(buildResult());
    sendTo(parent, newStatus);
    // and to interested agents
    newStatus.setResult(null);
    statusListeners.send(newStatus);
//     // sends a monitoring event
//     notifyStatusListeners("status", Status.toString(status));
  }

  /**
   * Property accessor.
   *
   * @return		execution status
   */
  protected int getStatus() { return status; }

  /**
   * Property accessor.
   *
   * @return		agent to report status to
   */
  protected AgentId getParent() { return parent; }

  /**
   * Property accessor.
   * <p>
   * Allows the parent field to be set if it is null.
   * Throws an exception when parent change is required.
   *
   * @param parent	agent to report status to
   */
  public void setParent(AgentId parent) throws Exception {
    if (parent == null)
      throw new IllegalArgumentException("null parent");

    if (this.parent != null) {
      if (parent.equals(this.parent)) return;
      throw new IllegalArgumentException("may not change task parent");
    }

    this.parent = parent;
    // adds parent condition
    setConditions(conditions);
  }

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
  protected String getErrorMessage() { return errorMessage; }

  /**
   * Sets conditions. Should be called before the agent is deployed.
   *
   * @param conditions	notifications to wait for
   */
  public void setConditions(ConditionHandle[] conditions) {
    int conditionsNum = 0;
    if (parent != null)
      conditionsNum ++;
    if (conditions != null)
      conditionsNum += conditions.length;
    if (conditionsNum == 0) {
      this.conditions = null;
    } else {
      this.conditions = new ConditionHandle[conditionsNum];
    }
    int i = 0;
    if (parent != null)
      this.conditions[i++] = new ConditionHandle(null, parent);
    if (conditions != null) {
      for (int j = conditions.length; j-- > 0;)
	this.conditions[i+j] = conditions[j];
      i += conditions.length;
    }
  }

  /**
   * Sets conditions as a blank separated list of names.
   * The associated triggering agents are undefined (null).
   * This function is to be called by the GCT, and is provided only because
   * the GCT is not yet able to handle array types.
   *
   * @param conditions	notifications to wait for
   */
  public void setListenerCondition(String listenerCondition) {
    StringTokenizer parserCond = new StringTokenizer(listenerCondition);
    ConditionHandle[] cond;
    cond = new ConditionHandle[parserCond.countTokens()];
    String condListen;
    for (int i = 0; i < cond.length; i ++) {
      condListen = parserCond.nextToken();
      cond[i] = new ConditionHandle(condListen, null);
    }
    this.setConditions(cond);
  }

  /**
   * Adds an agent to the list of listeners to status change notifications.
   *
   * @param listener	id of agent listening to status change notifications
   */
  public void addStatusListener(AgentId listener) throws Exception {
    statusListeners.addListener(listener, true);
  }

  /**
   * Checks if an agent is in the list of listeners to status change
   * notifications.
   *
   * @param listener	id of agent to check
   * @return		true when listen belongs to the list of listeners,
   *			false otherwise
   */
  public boolean isStatusListener(AgentId listener) {
    if (statusListeners == null)
      return false;
    int res = statusListeners.indexFromId(listener);
    return (res != -1);
  }

  /**
   * Removes an agent to the list of listeners to status change notifications.
   *
   * @param listener	id of agent to remove
   */
  public void removeStatusListener(AgentId listener) {
    statusListeners.removeListener(listener);
  }

  
  /**
   * Provides a string image for this object.
   * <p>
   * This function may be overloaded. A derived function is advised to follow
   * the template :
   * <br> public String toString() {
   * <br>   StringBuffer output = new StringBuffer();
   * <br>   output.append("(");
   * <br>   output.append(super.toString());
   * <br>   output.append(",field=");
   * <br>   output.append(field);
   * <br>   output.append(")");
   * <br>   return output.toString();
   * <br> }
   *
   * @return	string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",status=");
    output.append(Status.toString(status));
    output.append(",parent=");
    output.append(parent);
    output.append(",conditions=");
    if (conditions == null) {
      output.append("null");
    } else {
      output.append("(");
      output.append(conditions.length);
      for (int i = 0; i < conditions.length; i ++) {
	output.append(",");
	output.append(conditions[i]);
      }
      output.append(")");
    }
    output.append(",errorMessage=");
    if (errorMessage == null) {
      output.append("null");
    } else {
      output.append("\"");
      output.append(errorMessage);
      output.append("\"");
    }
    output.append(",statusListeners=");
    output.append(statusListeners);
    output.append(")");
    return output.toString();
  }

  /**
   * Reacts to <code>Task</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>ResetNotification</code>,
   *	<code>RestartNotification</code>,
   *	<code>EventKill</code>,
   *	<code>DeleteNot</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
    public void react(AgentId from, Notification not) throws Exception {
     /* AF */ logmon.log(BasicLevel.DEBUG, getName() + ", " +
			 this+ ".react(" + from + ", " +not + ")");

    try {
      if (not instanceof SetParent) {
	doReact(from, (SetParent) not);
        return;
      } else if (not instanceof ResetNotification) {
	doReact(from, (ResetNotification) not);
        return;
      } else if (not instanceof ParamNotification) {
	doReact(from, (ParamNotification) not);
        return;
      } else if (not instanceof Condition) {
	doReact(from, (Condition) not);
        return;
      } else if (not instanceof StartNotification) {
	doReact(from, (StartNotification) not);
        return;
      } else if (not instanceof RestartNotification) {
	doReact(from, (RestartNotification) not);
        return;
      } else if (not instanceof EventKill) {
	doReact(from, (EventKill) not);
        return;
      } else if (not instanceof DeleteNot) {
	doReact(from, (DeleteNot) not);
        return;
      } else if (not instanceof AddStatusListenerNot) {
	doReact(from, (AddStatusListenerNot) not);
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
   * Reacts to <code>SetParent</code> notifications.
   * Calls <code>setParent</code>.
   * <p>
   * This notification is currently allowed just after deployment,
   * while this task is still in <code>NONE</code> status.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, SetParent not) throws Exception {
    switch (status) {
    case Status.NONE:
      setParent(not.parent);
      break;
    default:
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", can't set parent [status=" +
                 Status.toString(status) + ']');
      return;
    }
  }

  /**
   * Reacts to <code>ResetNotification</code> notifications.
   * Calls <code>reset</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, ResetNotification not) throws Exception {
    switch (status) {
    case Status.NONE:
    case Status.DONE:
    case Status.FAIL:
    case Status.STOP:
      reset();
      break;
    default:
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", can't reset [status=" +
                 Status.toString(status) + ']');
      return;
    }
  }

  /**
   * Reacts to <code>ParamNotification</code> notifications.
   * Calls <code>setParameter</code>.
   * <p>
   * There is no status check when receiving this notification. However
   * this agent not being able to handle the actual parameter may result
   * in it switching to <code>FAIL</code> status.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, ParamNotification not) throws Exception {
    setParameter(not.getParameter());
  }

  /**
   * Reacts to <code>Condition</code> notifications.
   * Calls <code>setCondition</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, Condition not) throws Exception {
    switch (status) {
    case Status.INIT:
    case Status.RUN:
    case Status.KILL:
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", ignored condition [" + not + "] from " + from +
                 "[status=" + Status.toString(status) + ']');
      return;
    default:
      setCondition(not.name, from, not.status);
      break;
    }
  }

  /**
   * Reacts to <code>StartNotification</code> notifications.
   * Calls <code>start</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, StartNotification not) throws Exception {
    if (status != Status.WAIT) {
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", can't start unready task [status=" +
                 Status.toString(status) + ']');
      return;
    }

    start();
  }

  /**
   * Reacts to <code>RestartNotification</code> notifications.
   * Calls <code>restart</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, RestartNotification not) throws Exception {
    switch (status) {
    case Status.FAIL:
    case Status.STOP:
      restart();
      break;
    default:
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", can't restart unstopped task [status=" +
                 Status.toString(status) + ']' + from);
      return;
    }
  }

  /**
   * Reacts to <code>EventKill</code> notifications.
   * Calls <code>stop</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, EventKill not) throws Exception {
    stop();
  }

  /**
   * Reacts to <code>AgentDeleteRequest</code> notifications.
   * Calls <code>delete</code>.
   *
   * This agent checks that the requesting agent actually is its parent.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  protected void doReact(AgentId from, DeleteNot not) throws Exception {
    if (parent != null && !parent.equals(from)) {
      if (not.reply != null)
        sendTo(not.reply, new DeleteAck(getId(), DeleteAck.DENIED));
      else
        sendTo(from, new DeleteAck(getId(), DeleteAck.DENIED));

      logmon.log(BasicLevel.ERROR,
                 getName() + ", can't delete task [" + parent + ',' + from +']');
      return;
    }

    if (not.reply != null)
      delete(not.reply);
    else
      delete(from);
  }


  /**
   * Reacts to <code>AddStatusListenerNot</code> notifications.
   * Calls <code></code>.
   * <p>
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, AddStatusListenerNot not) throws Exception {
    boolean currentListener = isStatusListener(from);
    if (!currentListener) {	 
      addStatusListener(from);
    }
  }


  /**
   * Resets a <code>Task</code> agent ready for execution.
   */
  public void reset() throws Exception {
    // resets object state
    errorMessage = null;

    if (conditions != null) {
      for (int i = conditions.length; i-- > 0;)
	conditions[i].status = false;
    }

    // sets execution status to WAIT
    setStatus(Status.WAIT);

    // starts tasks with no condition
    checkConditions();
  }

  /**
   * Sets the parameter value for this task.
   * <p>
   * The base class implementation ignores the parameter. The derived classes
   * are assumed to keep the parameter as an object of a specialized class,
   * thus nullifying the benefit of handling it in this class. Moreover
   * derived classes should check the actual class of the parameter against
   * the expected class, and switch to FAIL status whenever the class does not
   * conform.
   *
   * @param parameter	parameter for this task
   */
  protected void setParameter(Object parameter) throws Exception {}

  /**
   * Sets the value of a start condition of this task.
   * <p>
   * Calls <code>checkConditions</code>.
   *
   * @param condition	condition name, may be null
   * @param id		agent sending condition
   * @param status	condition status
   */
  protected void setCondition(
    String cname,
    AgentId cid,
    boolean cstatus) throws Exception {

    ConditionHandle condition = null;

    if (conditions != null) {
      for (int i = conditions.length; i-- > 0;) {
	if ((conditions[i].name == null ||
	     (cname != null && cname.equals(conditions[i].name))) &&
	    (conditions[i].id == null ||
	     cid.equals(conditions[i].id))) {
	  condition = conditions[i];
	  break;
	}
      }
    }

    if (condition == null)
      throw new IllegalArgumentException("unexpected condition");

    condition.status = cstatus;

    checkConditions();
  }


  /**
   * Checks task conditions for starting execution.
   * <p>
   * When all conditions hold true the agent sends itself a
   * <code>StartNotification</code> notification.
   */
  protected void checkConditions() throws Exception {
    if (conditions != null) {
      for (int i = conditions.length; i-- > 0;)
        if (! conditions[i].status)
          return;
    }
    // all conditions hold true
    sendTo(getId(), new StartNotification());
  }
  

  /**
   * Restarts this agent execution.
   * <p>
   * Actually prepares the agent for execution restart. Actual restart
   * may be subject to activation conditions which may have been revoked.
   * When activation conditions come true again, the <code>start</code>
   * function is called.
   * <p>
   * This function may be overloaded in derived classes. However the
   * derived class implementation should call this base class implementation
   * after having prepared the restart.
   */
  protected void restart() throws Exception {
    // resets execution status to WAIT without changing agent execution state
    setErrorMessage(null);
    setStatus(Status.WAIT);

    // restarts tasks with no condition or with true condition
    checkConditions();
  }

  /**
   * Starts task execution, must be defined in derived classes.
   * <p>
   * This function must start calling <code>setStatus(Status.INIT&RUN)</code>,
   * and ensure that <code>setStatus(Status.DONE/FAIL)</code> is eventually
   * called.
   * This function is also called when the execution conditions of a restarted
   * task come true.
   */
  protected abstract void start() throws Exception;

  /**
   * Builds the result object when this task completes.
   * The base class implementation returns a null value to comply with the
   * previous interface. Derived classes using the result object should
   * overload this function.
   *
   * @return	the result object of the task
   */
  protected Object buildResult() throws Exception {
    return null;
  }

  /**
   * Stops task execution, common code for all <code>Task</code> agents.
   * <p>
   * This function is likely not to be overloaded.
   */
  protected void stop() throws Exception {
    switch (status) {
    case Status.WAIT:
      setStatus(Status.STOP);
      break;
    case Status.INIT:
    case Status.RUN:
      setStatus(Status.KILL);
      taskStop();
      break;
    default:
      // ignore notification
      logmon.log(BasicLevel.WARN,
                 getName() + ", can't stop [status=" +
                 Status.toString(status) + ']');
      return;
    }
  }

  /**
   * Stops task execution, must be defined in derived classes.
   * <p>
   * This function must ensure that
   * <code>setStatus(Status.DONE/FAIL/STOP)</code> is eventually called.
   */
  protected abstract void taskStop() throws Exception;
}
