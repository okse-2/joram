/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class SCAdminHelper {
  /** Hashtable that contain all <code>Process</code> of running AgentServer */
  protected Hashtable<Short, Process> ASP = null;

  protected Logger logmon = null;

  public SCAdminHelper() {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger("fr.dyade.aaa.agent.SCAdmin");

    ASP = new Hashtable<Short, Process>();
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   */
  public String startAgentServer(short sid) throws Exception {
    return startAgentServer(sid, null, null, null, null);
  }

  /**
   * Starts an agent server from its id using specific jvmargs.
   *
   * @param sid		id of agent server to start
   * @param jvmargs	arguments to pass to the created java program
   */
  public String startAgentServer(short sid,
                                 String[] jvmargs) throws Exception {
    return startAgentServer(sid, null, jvmargs, null, null);
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
    return startAgentServer(sid, dir, jvmargs, null, null);
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
                                 String[] args) throws Exception {
    return startAgentServer(sid, dir, jvmargs, null, args);
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
    logmon.log(BasicLevel.DEBUG,
               "SCAdmin: start AgentServer#" + sid);

    Process p = (Process) ASP.get(new Short(sid));
    if (p != null) {
      try {
        logmon.log(BasicLevel.DEBUG,
                   "SCAdmin: AgentServer#" + sid + " -> " + p.exitValue());
      } catch (IllegalThreadStateException exc) {
        // there is already another AS#sid running
        logmon.log(BasicLevel.WARN,
                   "SCAdmin: AgentServer#" + sid + " already running.");
        throw new IllegalStateException("AgentServer#" + sid +
        " already running.");
      }
    }

    p = execAgentServer(sid, dir, jvmargs, className, args);
    ASP.put(new Short(sid), p);
    String ret = waitServerStarting(p);
    closeServerStream(p);

    return ret;
  }

  /**
   * Runs an agent server from its id and specific parameters.
   *
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
    logmon.log(BasicLevel.DEBUG,
               "SCAdmin: run AgentServer#" + sid);

    String javapath = 
      new File(new File(System.getProperty("java.home"), "bin"),
      "java").getPath();
    String classpath = System.getProperty("java.class.path");

    Vector<String> argv = new Vector<String>();
    argv.addElement(javapath);
    argv.addElement("-classpath");
    argv.addElement(classpath);
    if (jvmargs != null) {
      for (int i=0; i<jvmargs.length; i++)
        argv.addElement(jvmargs[i]);
    }
    argv.addElement("-Dcom.sun.management.jmxremote");
    argv.addElement("-DMXServer=com.scalagent.jmx.JMXServer");

    if (className == null)
      className = "fr.dyade.aaa.agent.AgentServer";
    argv.addElement(className);
    argv.addElement(Short.toString(sid));
    argv.addElement("s" + sid);
    if (args != null) {
      for (int i=0; i<args.length; i++)
        argv.addElement(args[i]);
    }

    String[] command = new String[argv.size()];
    argv.copyInto(command);

    logmon.log(BasicLevel.DEBUG,
               "SCAdmin" + ": launches AgentServer#" + sid);

    Process p = null;
    if (dir == null) {
      p = Runtime.getRuntime().exec(command);
    } else {
      p = Runtime.getRuntime().exec(command, null, dir);
    }

    return p;
  }

  /**
   *  Waits for the starting of an AgentServer pointed out by its process.
   *
   * @param p	the AgentServer process.
   */
  public String waitServerStarting(Process p) throws Exception {
    BufferedReader br =
      new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = br.readLine();
    if (line != null) {
      if (line.endsWith(AgentServer.ERRORSTRING)) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(line);
        while (((line = br.readLine()) != null) &&
            (! line.equals(AgentServer.ENDSTRING))) {          
          strBuf.append('\n');
          strBuf.append(line);
        }
        line = strBuf.toString();
      }
    }
    return line;
  }

  /**
   *  Closes all subsequent streams of the process to avoid deadlock due to
   * limited buffer size.
   *
   * @param p	the AgentServer process.
   */
  public void closeServerStream(Process p) throws Exception {
    try {
      p.getInputStream().close();
    } catch (Exception exc) {}
    try {
      p.getOutputStream().close();
    } catch (Exception exc) {}
    try {
      p.getErrorStream().close();
    } catch (Exception exc) {}
  }

  /**
   * Kills this agent server process.
   *
   * @param sid		id of agent server to stop
   */
  public void killAgentServer(short sid) throws Exception {
    Process p = (Process) ASP.get(new Short(sid));

    logmon.log(BasicLevel.DEBUG,
               "SCAdmin: kill AgentServer#" + sid + " [" + p + ']');

    if (p != null) p.destroy();
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
    Process p = (Process) ASP.get(new Short(sid));

    logmon.log(BasicLevel.DEBUG,
               "SCAdmin: join AgentServer#" + sid + " [" + p + ']');

    // TODO: put it in previous method and set a Timer.
    if (p != null) return p.waitFor();

    throw new UnknownServerException();
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
  public int exitValue(short sid) throws IllegalThreadStateException, UnknownServerException {
    Process p = (Process)ASP.get(new Short(sid));
    if (p != null) {
      int res = p.exitValue();
      return res;
    }

    throw new UnknownServerException();
  }

  /**
   * Kill an agent server and remove it from the ASP table.
   *
   * @param sid		id of agent server to stop
   */
  public void destroyAgentServer(short sid) throws Exception {
    Short key = new Short(sid);
    Process p = (Process)ASP.get(key);
    if (p != null) {
      ASP.remove(key);
      p.destroy();
    }
  }

  /**
   * Stops cleanly an agent server from its id.
   *
   * @param sid		id of agent server to stop
   * @param port	port of the corresponding AdminProxy.
   */
  public void stopAgentServer(short sid, int port) throws Exception {
    stopAgentServer(sid, "localhost", port);
  }

  /**
   * Stops cleanly an agent server from its id.
   *
   * @param sid		id of agent server to stop
   * @param host	hostname of the agent server.
   * @param port	port of the corresponding AdminProxy.
   */
  public void stopAgentServer(short sid, String host, int port) throws Exception {
    Socket socket = null;

    logmon.log(BasicLevel.DEBUG, "SCAdmin: stop AgentServer#" + sid);

    try {
      socket = new Socket(host, port);
      socket.getOutputStream().write(AdminProxy.STOP_SERVER.getBytes());
      socket.getOutputStream().write('\n');
      socket.getOutputStream().flush();
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "SCAdmin: Can't stop server#" + sid, exc);
      throw new Exception("Can't stop server#" + sid +
                          ": " + exc.getMessage());
    } finally {
      close(socket);
      socket = null;
    }
  }

  /**
   * Stops violently an agent server from its id.
   *
   * @param sid		id of agent server to stop
   * @param port	port of the corresponding AdminProxy.
   */
  public void crashAgentServer(short sid, int port) throws Exception {
    crashAgentServer(sid, "localhost", port);
  }

  /**
   * Stops violently an agent server from its id.
   *
   * @param sid		id of agent server to stop
   * @param host	hostname of the agent server.
   * @param port	port of the corresponding AdminProxy.
   */
  public void crashAgentServer(short sid, String host, int port) throws Exception {
    Socket socket = null;

    logmon.log(BasicLevel.DEBUG, "SCAdmin: crash AgentServer#" + sid);

    try {
      socket = new Socket(host, port);
      socket.getOutputStream().write(AdminProxy.CRASH_SERVER.getBytes());
      socket.getOutputStream().write('\n');
      socket.getOutputStream().flush();
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "SCAdmin: Can't crash server#" + sid, exc);
      throw new Exception("Can't crash server#" + sid + ": " + exc.getMessage());
    } finally {
      close(socket);
      socket = null;
    }
  }

  static void close(Socket socket) {
    try {
      socket.getInputStream().close();
    } catch (Exception exc) {}
    try {
      socket.getOutputStream().close();
    } catch (Exception exc) {}
    try {
      socket.close();
    } catch (Exception exc) {}
  }
}
