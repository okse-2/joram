/*
 * Copyright (C) 2001 - 2002 SCALAGENT
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
  /** RCS version number of this file: $Revision: 1.11 $ */
  public static final String RCS_VERSION="@(#)$Id: ProcessMonitor.java,v 1.11 2002-10-21 08:41:13 maistrfr Exp $";

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
