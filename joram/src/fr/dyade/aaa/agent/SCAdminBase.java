/*
 * Copyright (C) 2002 - 2008 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.conf.*;

public class SCAdminBase {
  protected A3CMLConfig a3config = null;

  protected Logger logmon = null;

  protected SCAdminHelper scadmin = null;

  public SCAdminBase() {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger("fr.dyade.aaa.agent.SCAdmin");

    scadmin = new SCAdminHelper();
  }

  public SCAdminBase(String path) throws Exception {
    this();
    update(path);
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   */
  public String startAgentServer(short sid) throws Exception {
    return startAgentServer(sid, null);
  }

  /**
   * Starts an agent server from its id using specific storage directory.
   * JVM arguments are initialized from A3 configuration.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	                current working directory if <code>null</code>
   */
  public String startAgentServer(short sid,
                                 File dir) throws Exception {
    StringTokenizer st = new StringTokenizer(a3config.getJvmArgs(sid));
    int nb = st.countTokens();
    String[] jvmargs = new String[nb];
    for (int i=0; i<nb; i++){
      jvmargs[i] = st.nextToken();
    }

    return scadmin.startAgentServer(sid, dir, jvmargs);
  }

  /**
   * Starts an agent server from its id using specific jvmargs and storage
   * directory.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	current working directory if <code>null</code>
   * @param jvmargs	arguments to pass to the created java program
   */
  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmargs) throws Exception {
    return scadmin.startAgentServer(sid, dir, jvmargs);
  }

  /**
   * Starts an agent server from its id using specific jvmargs and storage
   * directory.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	                current working directory if <code>null</code>
   * @param jvmargs	arguments to pass to the created java program
   * @param args	additional arguments to pass to the created java
   *                    program
   */
  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmargs,
				 String[] servargs) throws Exception {
    return scadmin.startAgentServer(sid, dir, jvmargs, servargs);
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	current working directory if <code>null</code>
   * @param jvmargs	arguments to pass to the created java program
   * @param className   the name of the main class
   * @param args	additional arguments to pass to the created java
   *                    program
   */
  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmargs,
                                 String className,
				 String[] args) throws Exception {
    return scadmin.startAgentServer(sid, dir, jvmargs, className, args);
  }

  /**
   * Runs an agent server from its id and specific parameters.
   *
   * @param name	id of agent server to start
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	current working directory if <code>null</code>
   * @param jvmargs	arguments to pass to the created java program
   * @param className   the name of the main class
   * @param args	additional arguments to pass to the created java
   *                    program
   */
  public Process execAgentServer(short sid,
                                 File dir,
                                 String[] jvmargs,
                                 String className,
                                 String[] args) throws Exception {
    return scadmin.execAgentServer(sid, dir, jvmargs, className, args);
  }

  /**
   *  Waits for the starting of an AgentServer pointed out by its process.
   * Closes all subsequent streams of the process to avoid deadlock due to
   * limited buffer size.
   *
   * @param p	the AgentServer process.
   */
  public String waitServerStarting(Process p) throws Exception {
    return scadmin.waitServerStarting(p);
  }

  /**
   *  Closes all subsequent streams of the process to avoid deadlock due to
   * limited buffer size.
   *
   * @param p	the AgentServer process.
   */
  public void closeServerStream(Process p) throws Exception {
    scadmin.closeServerStream(p);
  }

  /**
   * Kills this agent server process.
   *
   * @param sid		id of agent server to stop
   */
  public void killAgentServer(short sid) throws Exception {
    scadmin.killAgentServer(sid);
  }

  /**
   * Causes the current thread to wait, if necessary, until the process
   * running this agent server has terminated.
   *
   * @param sid		id of agent server to stop
   * @return 		the exit value of the agent server.
   * @exception UnknownServerException if the agent server is unknown.
   */
  public int joinAgentServer(short sid) throws Exception {
    return scadmin.joinAgentServer(sid);
  }

  /**
   * Ask for the exit value of an agent server.
   *
   * @param sid		id of agent server to stop
   * @return 		the exit value of the agent server.
   * @exception IllegalThreadStateException
   * 			if the agent server is still running.
   * @exception UnknownServerException
   *			if the agent server is unknown.
   */
  public int exitValue(short sid) 
    throws IllegalThreadStateException, UnknownServerException {
    return scadmin.exitValue(sid);
  }

  /**
   * Stops cleanly an agent server from its id.
   *
   * @param sid		id of agent server to stop
   */
  public void stopAgentServer(short sid) throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdmin: stop AgentServer#" + sid);

    A3CMLServer server = a3config.getServer(sid);
    String host = server.hostname;
    int port = Integer.parseInt(
      a3config.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
    scadmin.stopAgentServer(sid, host, port);
  }

  /**
   * Stops violently an agent server from its id.
   *
   * @param sid		id of agent server to stop
   */
  public void crashAgentServer(short sid) throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdmin: stop AgentServer#" + sid);

    A3CMLServer server = a3config.getServer(sid);
    String host = server.hostname;
    int port = Integer.parseInt(
      a3config.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
    scadmin.crashAgentServer(sid, host, port);
  }

  /**
   * Updates the configuration.
   */
  public void update() throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdmin: update()");

    try {
      a3config = A3CML.getXMLConfig();
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                  "SCAdmin: problem during configuration parsing", exc);
      throw new Exception("Problem during configuration parsing");
    }

  }

  public void update(String path) throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdmin: update(" + path + ")");

    try {
      a3config = A3CML.getXMLConfig(path);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                  "SCAdmin: problem during configuration parsing", exc);
      throw new Exception("Problem during configuration parsing");
    }
  }
}
