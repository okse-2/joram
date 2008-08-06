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

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;

/**
 * <code>Task</code> whose goal is to monitor a service task. A service task
 * is performed by a server agent reacting to a <code>Command</code>
 * notification.
 * This <code>ServiceTask</code> agent waits for a <code>Report</code> then
 * sends the appropriate <code>StatusNotification</code> notification.
 *
 * @see		Command
 * @see		Report
 */
public class ServiceTask extends Task {
  /** task server */
  protected AgentId server;
  /** service task command notification */
  protected Command command;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param server	task server
   * @param command	service task command notification
   */
  public ServiceTask(short to, AgentId parent, AgentId server, Command command) throws Exception {
    super(to, parent);
    this.server = server;
    this.command = command;
    command.setReport(getId());
  }
  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",server=" + server +
      ",command=" + command + ")";
  }

  /**
   * Reacts to <code>ServiceTask</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>Report</code> from task server.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof Report) {
	doReact((Report) not);
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
   * Reacts to <code>Report</code> notifications.
   * Calls <code>taskReport</code>.
   *
   * @param not		notification to react to
   */
  public void doReact(Report not) throws Exception {
    taskReport(not);
  }

  /**
   * Starts program execution, overloads <code>start</code> from base class.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
    sendTo(server, command);
  }

  /**
   * Analyzes task report.
   *
   * @param not		report to analyze
   */
  protected void taskReport(Report not) throws Exception {
    switch (not.getStatus()) {
    case Status.FAIL:
      setErrorMessage(not.getMessage());
      break;
    case Status.DONE:
      break;
    default:
      return;
    }
    // task is terminated
    setStatus(not.getStatus());
  }

  /**
   * Deletes this agent.
   * <p>
   * Should kill the task on target.
   */
  protected void delete(AgentId agent) {
    super.delete(agent);
  }

  /**
   * Stops task execution, must be defined in derived classes.
   * This function must ensure that <code>setStatus(Status.DONE/FAIL/STOP)</code>
   * is eventually called.
   */
  protected void taskStop() throws Exception {
    // TO DO, stop task in server !
  }
}
