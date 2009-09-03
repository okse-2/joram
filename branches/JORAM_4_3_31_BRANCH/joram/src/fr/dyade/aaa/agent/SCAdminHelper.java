/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.*;

public class SCAdminHelper {
  /** Hashtable that contain all <code>Process</code> of running AgentServer */
  protected Hashtable ASP = null;

  protected Logger logmon = null;

  public SCAdminHelper() {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger("fr.dyade.aaa.agent.SCAdmin");

    ASP = new Hashtable();
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   */
  public String startAgentServer(short sid) throws Exception {
    return startAgentServer(sid, null, null);
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   * @param jvmarg	arguments to pass to the created java program
   */
  public String startAgentServer(short sid,
                                 String[] jvmarg) throws Exception {
    return startAgentServer(sid, null, jvmarg, null);
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	current working directory if <code>null</code>
   * @param jvmarg	arguments to pass to the created java program
   */
  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmarg) throws Exception {
    return startAgentServer(sid, dir, jvmarg, 
                            "fr.dyade.aaa.agent.AgentServer", null);
  }

  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmarg,
				 String[] servarg) throws Exception {
    return startAgentServer(sid, dir, jvmarg, 
                            "fr.dyade.aaa.agent.AgentServer", servarg);
  }

  /**
   * Starts an agent server from its id.
   *
   * @param sid		id of agent server to start
   * @param dir		new working directory for the created agent server,
   *	current working directory if <code>null</code>
   * @param jvmarg	arguments to pass to the created java program
   * @param className   the name of the main class
   * @param servarg	additional arguments to pass to the created java program
   */
  public String startAgentServer(short sid,
                                 File dir,
                                 String[] jvmarg,
                                 String className,
				 String[] servarg) throws Exception {
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

    String javapath = 
      new File(new File(System.getProperty("java.home"), "bin"),
               "java").getPath();
    String classpath = System.getProperty("java.class.path");

    Vector argv = new Vector();
    argv.addElement(javapath);
    argv.addElement("-classpath");
    argv.addElement(classpath);
    if (jvmarg != null) {
      for (int i=0; i<jvmarg.length; i++)
        argv.addElement(jvmarg[i]);
    }
    argv.addElement(className);
    argv.addElement(Short.toString(sid));
    argv.addElement("s" + sid);
    if (servarg != null) {
      for (int i=0; i<servarg.length; i++)
        argv.addElement(servarg[i]);
    }

    String[] command = new String[argv.size()];
    argv.copyInto(command);

    logmon.log(BasicLevel.DEBUG,
               "SCAdmin" + ": starts AgentServer#" + sid);
    if (dir == null) {
      p = Runtime.getRuntime().exec(command);
    } else {
      p = Runtime.getRuntime().exec(command, null, dir);
    }
    ASP.put(new Short(sid), p);
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
    // Close all streams of subprocess in order to avoid deadlock due
    // to limited buffer size.
    try {
      p.getInputStream().close();
    } catch (Exception exc) {}
    try {
      p.getOutputStream().close();
    } catch (Exception exc) {}
    try {
      p.getErrorStream().close();
    } catch (Exception exc) {}
    return line;
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
    if (p != null) {
      return p.waitFor();
    } else {
      throw new UnknownServerException();
    }
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
    Process p = (Process)ASP.get(new Short(sid));
    if (p != null) {
      int res = p.exitValue();
      return res;
    } else {
      throw new UnknownServerException();
    }
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
      throw new Exception("Can't crash server#" + sid +
                          ": " + exc.getMessage());
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
