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
 * <code>Task</code> whose goal is to execute a program in a separate process.
 * <p>
 * The command line may be given as a list of arguments, or as a single
 * line which is parsed. The program completing with value <code>0</code>
 * is interpreted as a successful completion of the task. Another exit value
 * leads to a <code>FAIL</code> status, with an error message read from the
 * process error stream.
 * <p>
 * The created process is registered with a local <code>ProcessManager</code>,
 * which will eventually signal the process end to this agent by an external
 * <code>ProcessEnd</code> notification.
 *
 * @see		ProcessManager
 * @see		ProcessEnd
 */
public class Program extends Task {
  /** program arguments */
  protected String[] command;

  /**
   * Default constructor
   */
  public Program() {
    super();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param command	program command line
   */
  public Program(short to, AgentId parent, String[] command) {
    super(to, parent);
    this.command = command;
  }

  /**
   * Creates an agent to be deployed remotely. Invoked by the Configurator.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	name of agent
   */
  public Program(short to, String name) {
    super(to, name);
  }


  /**
   * A set command used in the GCT
   */
  public void setCommandLine(String commandLine) {
    command = parseCommand(commandLine);
  }


  /**
   * Creates an agent to be deployed remotely.
   * Parses command line with a <code>StringTokenizer</code> which does not
   * recognize blanks in strings. If blanks in program arguments are needed
   * use constructor with arguments table.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param commandLine	program command line
   */
  public Program(short to, AgentId parent, String commandLine) {
    super(to, parent);
    command = parseCommand(commandLine);
  }

  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
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

  /**
   * Reacts to <code>Program</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>ProcessEnd</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof ProcessEnd) {
	logmon.log(BasicLevel.DEBUG, getName() + ", ProcessEnd " + not);
	doReact((ProcessEnd) not);
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
   * Reacts to <code>ProcessEnd</code> notifications.
   * Calls <code>endProcess</code>.
   *
   * @param not		notification to react to
   */
  public void doReact(ProcessEnd not) throws Exception {
    endProcess(not);
  }

  /**
   * Starts program execution, overloads <code>start</code> from base class.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", start " + this);
    Process process = Runtime.getRuntime().exec(command);
    ProcessManager.manager.register(process, getId());
  }

  /**
   * Analyzes process report.
   *
   * @param not		the process report
   */
  protected void endProcess(ProcessEnd not) throws Exception {
    if (not.getExitValue() != 0)
      setErrorMessage(not.getErrorMessage());

    if (getStatus() == Status.KILL)
      setStatus(Status.STOP);
    else if (not.getExitValue() == 0)
      setStatus(Status.DONE);
    else
      setStatus(Status.FAIL);
  }

  /**
   * Parses command line arguments.
   * <p>
   * This function does not recognize blanks in strings
   *
   * @param commandLine		the line to parse
   */
  protected static String[] parseCommand(String commandLine) {
    StringTokenizer parser = new StringTokenizer(commandLine);
    String[] command = new String[parser.countTokens()];
    for (int i = 0; i < command.length; i ++)
      command[i] = parser.nextToken();
    return command;
  }

  /**
   * Deletes this agent.
   *
   * Should unregister from ProcessManager.
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
    // kill the process
    ProcessManager.manager.destroy(getId());
  }

}
