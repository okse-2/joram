/*
 * Copyright (C) 2001 - 2002 SCALAGENT
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
  * Object which monitors in a separate thread the execution of a process,
  * on account for a monitoring <code>Agent</code>.
  * Warns the monitoring agent at process end, with
  * exitValue as returned by <code>Process.exitValue</code>, and
  * errorMessage as returned by <code>Process.getErrorStream</code>.
  *
  * @see	ProcessManager
  */
class ProcessMonitor extends Driver implements Serializable {
  /** RCS version number of this file: $Revision: 1.14 $ */
  public static final String RCS_VERSION="@(#)$Id: ProcessMonitor.java,v 1.14 2003-06-23 13:37:51 fmaistre Exp $";

  transient Process process;	/** monitored process */
  AgentId agent;		/** registering agent */

  /**
    * Default constructor
    */
  ProcessMonitor(Process process, AgentId agent) {
    this.process = process;
    this.agent = agent;
  }

  /**
    * Starts the monitoring thread, sends a <code>ProcessEnd</code>
    * notification to monitoring agent at process end.
    * When the process terminates sends a <code>ProcessEnd</code>
    * notification to monitoring agent then <code>unregister</code>s from
    * <code>ProcessManager</code> object.
    */
  public void run() {
    int exitValue = 0;
    String errorMessage = null;
    try {
      while (isRunning) {
	  canStop = true;
	  try {
            if (ProcessManager.xlogmon.isLoggable(BasicLevel.DEBUG))
	      ProcessManager.xlogmon.log(BasicLevel.DEBUG,
                                         "AgentServer#" + AgentServer.getServerId() +
                                         ".ProcessMonitor, waiting");
	      exitValue = process.waitFor();
              if (ProcessManager.xlogmon.isLoggable(BasicLevel.DEBUG))
                  ProcessManager.xlogmon.log(BasicLevel.DEBUG,
                                             "AgentServer#" + AgentServer.getServerId() +
                                             ".ProcessMonitor, exit " + exitValue);
	  } catch (InterruptedException exc) {
	      continue;
	  }
	  canStop = false;
	  break;
      }
      if (exitValue != 0) {
	// get the error stream as error message
	InputStreamReader error = new InputStreamReader(process.getErrorStream());
	int c;
	errorMessage = "";
	while ((c = error.read()) != -1) {
	  errorMessage += (char) c;
	}
      }
      sendTo(agent, new ProcessEnd(exitValue, errorMessage));
      ProcessManager.manager.unregister(this);
    } catch (Exception exc) {
      ProcessManager.xlogmon.log(BasicLevel.ERROR,
                                 "AgentServer#" + AgentServer.getServerId() +
                                 ".ProcessMonitor, failure in run", exc);      
    }
  }

  public void close() {}
}
