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
import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Strings;
import fr.dyade.aaa.agent.*;

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
  /** the unique <code>ProcessManager</code> in the agent server */
  public static ProcessManager manager;

  static Logger xlogmon = null;

  /**
   * Initializes the <code>ProcessManager</code> object.
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void init(String args, boolean firstTime) throws Exception {
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
    return (ProcessManager) AgentServer.getTransaction().load("processManager");
  }

  /**
   * Saves object in persistent storage.
   */
  void save() throws IOException {
    AgentServer.getTransaction().save(manager, "processManager");
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

