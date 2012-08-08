/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2012 ScalAgent Distributed Technologies
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
package framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.agent.osgi.Activator;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

public class SCAdminOSGi implements SCAdminItf {

  private static final Logger logmon = Debug.getLogger(SCAdminOSGi.class.getName());

  /** Map containing all <code>Process</code> of running AgentServers */
  private Map launchedServers = new HashMap();

  public void killAgentServer(short sid) {
    Server server = (Server) launchedServers.get(new Short(sid));
    logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: kill AgentServer#" + sid);
    if (server != null) {
      server.process.destroy();
      try {
        server.process.waitFor();
      } catch (InterruptedException exc) {
        if (logmon.isLoggable(BasicLevel.ERROR)) {
          logmon.log(BasicLevel.ERROR,
              "SCAdminOSGi: AgentServer#" + sid + " error waiting for process kill.", exc);
        }
      }
    } else {
      logmon.log(BasicLevel.WARN, "Server process to kill not found: " + sid);
    }
  }

  public void startAgentServer(short sid) throws Exception {
    startAgentServer(sid, null);
  }

  public void startAgentServer(short sid, String[] jvmargs) throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: run AgentServer#" + sid);

    Server server = (Server) launchedServers.get(new Short(sid));

    if (server != null) {
      try {
        int exitValue = server.process.exitValue();
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: AgentServer#" + sid + " -> " + exitValue);
        }
      } catch (IllegalThreadStateException exc) {
        if (logmon.isLoggable(BasicLevel.WARN)) {
          logmon.log(BasicLevel.WARN, "SCAdminOSGi: AgentServer#" + sid + " already running.");
        }
        throw new IllegalStateException("AgentServer#" + sid + " already running.");
      }
    }

    String javapath = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();

    // Find felix jar and put it on the classpath
    File felixbin = new File(System.getProperty("felix.dir") + "/felix.jar");
    if (!felixbin.exists()) {
      throw new Exception("Felix framework not found.");
    }
    List argv = new ArrayList();
    argv.add(javapath);

    argv.add("-classpath");
    argv.add("." + File.pathSeparatorChar + felixbin.getAbsolutePath());
    
    if (jvmargs != null) {
      for (int i = 0; i < jvmargs.length; i++)
        argv.add(jvmargs[i]);
    }

    // Add JMX monitoring options
    argv.add("-Dcom.sun.management.jmxremote");

    // Choose a random telnet port if unspecified
    Integer port = Integer.getInteger("osgi.shell.telnet.port");
    if (port==null) {
      port = new Integer(getFreePort());
    }
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: AgentServer#" + sid + " telnet port: " + port);
    }

    // Get felix configuration file.
    URI configFile = new URI(System.getProperty("felix.config.properties", "file:config.properties"));
    argv.add("-Dfelix.config.properties=" + configFile);

    // Assign AgentServer properties for server id, storage directory and cluster id.
    argv.add("-Dorg.osgi.framework.storage=" + 's' + sid + "/felix-cache");
    argv.add("-Dosgi.shell.telnet.port=" + port);
    argv.add("-D" + Activator.AGENT_SERVER_ID_PROPERTY + '=' + sid);
    argv.add("-D" + Activator.AGENT_SERVER_STORAGE_PROPERTY + "=s" + sid);
    argv.add("-XX:+UnlockDiagnosticVMOptions");
    argv.add("-XX:+UnsyncloadClass");
    argv.add("-Dgosh.args=--nointeractive");// need with gogo

    // Main class
    argv.add("org.apache.felix.main.Main");

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin" + ": launches AgentServer#" + sid + " with: " + argv);
    }

    Process p = Runtime.getRuntime().exec((String[]) argv.toArray(new String[argv.size()]));
    
    p.getInputStream().close();
    p.getOutputStream().close();
    p.getErrorStream().close();

    launchedServers.put(new Short(sid), new Server(port.intValue(), p));
  }

  public void stopAgentServer(short sid) throws Exception {
    Socket socket = null;
    Server server = (Server) launchedServers.get(new Short(sid));
    TelnetReaderDaemon daemon = null;

    if (server != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: stop AgentServer#" + sid);
      }
      try {
        socket = new Socket("localhost", server.telnetPort);
        daemon = new TelnetReaderDaemon(socket.getInputStream());
        daemon.start();

        // use stop 0 to shutdown ! (available in felix and gogo)
        socket.getOutputStream().write("stop 0\n".getBytes());
        socket.getOutputStream().flush();


      } catch (Throwable exc) {
        if (logmon.isLoggable(BasicLevel.ERROR))
          logmon.log(BasicLevel.ERROR, "SCAdminOSGi: Can't stop server#" + sid + ", kill it.", exc);
        killAgentServer(sid);
        throw new Exception("SCAdminOSGi: Can't stop server#" + sid, exc);
      }

      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: join AgentServer#" + sid + " [" + server.process + ']');
      }
      server.process.waitFor();
      daemon.stop();
    } else {
      throw new UnknownServerException("Server " + sid + " unknown: not started using SCAdmin.");
    }
  }

  public void stopAgentServerExt(int telnetPort) throws Exception {
    Socket socket = null;
    TelnetReaderDaemon daemon = null;

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: stop on port " + telnetPort);
    }
    try {
      socket = new Socket("localhost", telnetPort);
      daemon = new TelnetReaderDaemon(socket.getInputStream());
      daemon.start();

      // use stop 0 to shutdown ! (available in felix and gogo)
      socket.getOutputStream().write("stop 0\n".getBytes());
      socket.getOutputStream().flush();

    } catch (Throwable exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: Can't stop server on port " + telnetPort, exc);
      if (daemon != null) {
        daemon.stop();
      }
      throw new Exception("Can't stop server on port " + telnetPort + ": " + exc.getMessage());
    }

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminOSGi: wait closing telnet connection on " + telnetPort);
    }
    while (daemon.isRunning()) {
      Thread.sleep(500);
    }
    daemon.close();
  }

  private static class TelnetReaderDaemon extends Daemon {

    private InputStreamReader reader;

    protected TelnetReaderDaemon(InputStream stream) {
      super("TelnetReaderDaemon");
      this.reader = new InputStreamReader(stream);
    }

    protected void close() {
    }

    protected void shutdown() {
      try {
        reader.close();
      } catch (IOException exc) {
      }
    }

    public void run() {
      int character;
      StringBuffer sb = new StringBuffer();
      try {
        while (running) {
          canStop = true;
          try {
            character = reader.read();
          } catch (IOException exc) {
            logmon.log(BasicLevel.ERROR, "Error in telnet daemon.", exc);
            return;
          }
          canStop = false;
          if (character == -1) break;
          
          if (character == '\n') {
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "TelnetReaderDaemon read: " + sb);
            }
            sb.setLength(0);
          } else {
            sb.append((char) character);
          }
        }
      } finally {
        finish();
      }
    }
  }

  private static class Server {
    public int telnetPort;

    public Process process;

    public Server(int telnetPort, Process process) {
      super();
      this.telnetPort = telnetPort;
      this.process = process;
    }
  }

  /* *********************************************************** */

  private static final int MIN_PORT_NUMBER = 1025;
  private static final int MAX_PORT_NUMBER = 32760;
  private static final int PORT_CHECK_RANGE = 1000;
  private static final int PORT_CHECK_START = 20000;

  public static int getFreePort() {
    Random random = new Random();
    int port = random.nextInt(PORT_CHECK_RANGE) + PORT_CHECK_START;
    while (!available(port)) {
      port = random.nextInt(PORT_CHECK_RANGE) + PORT_CHECK_START;
    }
    return port;
  }

  /**
   * Checks to see if a specific port is available.
   * 
   * @param port
   *          the port to check for availability
   */
  public static boolean available(int port) {
    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
      throw new IllegalArgumentException("Invalid start port: " + port);
    }
    ServerSocket ss = null;
    try {
      ss = new ServerSocket(port);
      return true;
    } catch (IOException e) {
    } finally {
      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          /* should not be thrown */
        }
      }
    }
    return false;
  }

}
