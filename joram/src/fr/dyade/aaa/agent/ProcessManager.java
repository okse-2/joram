/*
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

import java.util.*;
import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * Object which monitors processes execution.
 * There is only one <code>ProcessManager</code> object per agent server.
 * It eventually signals the process end to the registered agent by
 * an external <code>ProcessEnd</code> notification.
 *
 * The <code>ProcessManager</code> object is initialized in <code>init</code>,
 * called from <code>AgentServer.init</code>.
 *
 * This classes reuses the persistency service provided by
 * <code>Transaction</code>.
 *
 * @see		ProcessEnd
 * @see		ProcessMonitor
 */
public class ProcessManager implements Serializable {
  /** RCS version number of this file: $Revision: 1.12 $ */
  public static final String RCS_VERSION="@(#)$Id: ProcessManager.java,v 1.12 2003-03-19 15:16:06 fmaistre Exp $"; 

  /** the unique <code>ProcessManager</code> in the agent server */
  public static ProcessManager manager;

  static Logger xlogmon = null;

  /**
   * Initializes the <code>ProcessManager</code> object.
   *
   * @exception Exception
   *	unspecialized exception
   */
  static void init() throws Exception {
    // Get the logging monitor from current server MonologLoggerFactory
    xlogmon = Debug.getLogger(Debug.A3Debug + ".ProcessManager");

    manager = ProcessManager.load();
    if (manager == null) {
      manager = new ProcessManager();
      manager.save();
    } else if (manager.registry.size() > 0) {
      // declare previously registered processes as having failed
      // assume -1 is interpreted as a failure return code ...
      for (int i = manager.registry.size(); i-- > 0;) {
	ProcessMonitor monitor =
	  (ProcessMonitor) manager.registry.elementAt(i);
	manager.registry.removeElementAt(i);
	Channel.sendTo(
	  monitor.agent,
	  new ProcessEnd(-1, "unknown process end due to server failure"));
      }
      manager.save();
    }
  }

  /**
   * Builds object from persistent image.
   *
   * @return	loaded object or null if no persistent image exists
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the stored image class may not be found
   */
  static ProcessManager load() throws IOException, ClassNotFoundException {
    return (ProcessManager) AgentServer.transaction.load("processManager");
  }

  /**
   * Saves object in persistent storage.
   */
  void save() throws IOException {
    AgentServer.transaction.save(manager, "processManager");
  }


  /** repository holding <code>ProcessMonitor</code>s */
  Vector registry;

  /**
   * Default constructor.
   */
  ProcessManager() {
    registry = new Vector();
  }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",registry=" + Strings.toString(registry) + ")";
  }

  /**
   * Registers the <code>Process</code> object with the monitoring agent.
   * This <code>ProcessManager</code> delegates the process management to
   * a <code>ProcessMonitor</code> running in a separate thread as a
   * <code>Driver</code>.
   * When the process terminates the <code>ProcessMonitor</code> unregisters
   * from this <code>ProcessManager</code>.
   *
   * @param process	process to monitor
   * @param agent	agent to warn when process terminates
   *
   * @exception Exception
   *	unspecialized exception
   */
  public synchronized void register(Process process, AgentId agent) throws Exception {
    ProcessMonitor monitor = new ProcessMonitor(process, agent);
    monitor.start();
    registry.addElement(monitor);
    save();
  }

  /**
   * Unregisters terminated process.
   * <p>
   * This function is executed by the <code>ProcessMonitor</code> from
   * a separate thread.
   *
   * @param monitor	ProcessMonitor object managing the terminated process
   *
   * @exception Exception
   *	unspecialized exception
   */
  synchronized void unregister(ProcessMonitor monitor) throws Exception {
    registry.removeElement(monitor);
    save();
  }

  /**
   * Kills a process identified by the agent associated with it.
   *
   * @param agent	agent associated with the process to kill
   *
   * @exception Exception
   *	unspecialized exception
   */
  public synchronized void destroy(AgentId agent) throws Exception {
    ProcessMonitor monitor;
    // find possibly deleted monitor
    for (int i = registry.size(); i-- > 0;) {
      monitor = (ProcessMonitor) registry.elementAt(i);
      if (agent.equals(monitor.agent)) {
	monitor.process.destroy();
	return;
      }
    }
  }
}
