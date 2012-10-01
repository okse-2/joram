/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp.tests.classic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.tests.SCAdmin;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.UnknownServiceException;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

public class SCAdminClassic implements SCAdmin {

  private static final Logger logmon = Debug.getLogger(SCAdminClassic.class.getName());

  /** Map containing all <code>Process</code> of running AgentServers */
  private Map launchedServers = new HashMap();

  public void killAgentServer(short sid) {
    Server server = (Server) launchedServers.get(new Short(sid));
    logmon.log(BasicLevel.DEBUG, "SCAdminClassic: kill AgentServer#" + sid);
    if (server != null) {
      server.process.destroy();
      try {
        server.process.waitFor();
      } catch (InterruptedException exc) {
        if (logmon.isLoggable(BasicLevel.ERROR)) {
          logmon.log(BasicLevel.ERROR, "SCAdminClassic: AgentServer#" + sid
              + " error waiting for process kill.", exc);
        }
      }
    } else {
      logmon.log(BasicLevel.WARN, "Server process to kill not found: " + sid);
    }
  }

  public void startAgentServer(short sid) throws Exception {

    logmon.log(BasicLevel.DEBUG, "SCAdminClassic: run AgentServer#" + sid);

    Server server = (Server) launchedServers.get(new Short(sid));
    if (server != null) {
      try {
        int exitValue = server.process.exitValue();
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "SCAdminClassic: AgentServer#" + sid + " -> " + exitValue);
        }
      } catch (IllegalThreadStateException exc) {
        if (logmon.isLoggable(BasicLevel.WARN)) {
          logmon.log(BasicLevel.WARN, "SCAdminClassic: AgentServer#" + sid + " already running.");
        }
        throw new IllegalStateException("AgentServer#" + sid + " already running.");
      }
    }
    
    A3CMLConfig a3config;
    String configPath = SCAdmin.A3SERVERS_LOCATION;
    try {
      a3config = A3CML.getXMLConfig(configPath);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, "SCAdminClassic: problem during configuration parsing", exc);
      throw new Exception("Problem during configuration parsing");
    }

    String javapath = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();
    String classpath = System.getProperty("java.class.path");

    List argv = new ArrayList();
    argv.add(javapath);
    argv.add("-classpath");
    argv.add(classpath);
    argv.add("-DNTNoLockFile=true");
    argv.add("-D" + Debug.DEBUG_FILE_PROPERTY + "=\"" + SCAdmin.A3DEBUG_LOCATION + "\"");
    argv.add("-D" + AgentServer.CFG_FILE_PROPERTY + "=\"" + SCAdmin.A3SERVERS_LOCATION + "\"");

    // Add JMX monitoring options
    argv.add("-Dcom.sun.management.jmxremote");

    // Retrieve port from a3 configuration file (a3servers.xml)
    int port = -1;
    try {
      port = Integer.parseInt(a3config.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
    } catch (UnknownServiceException exc) {
      if (logmon.isLoggable(BasicLevel.WARN)) {
        logmon.log(BasicLevel.WARN, "AdminProxy service not found, server will not be stoppable "
            + "using SCAdmin. Only the killAgentServer() method can be used. ");
      }
    }
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminClassic: AgentServer#" + sid + " telnet port: " + port);
    }

    // Main class
    argv.add("fr.dyade.aaa.agent.AgentServer");
    argv.add(Short.toString(sid));
    argv.add("s" + sid);

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin" + ": launches AgentServer#" + sid + " with: " + argv);
    }

    File runDir = new File(SCAdmin.RUNNING_DIR);
    if (!runDir.exists()) {
      runDir.mkdirs();
    }

    Process p = Runtime.getRuntime().exec((String[]) argv.toArray(new String[argv.size()]), null, runDir);

    p.getInputStream().close();
    p.getOutputStream().close();
    p.getErrorStream().close();

    launchedServers.put(new Short(sid), new Server(port, p));

    Thread.sleep(1000);

  }

  public void stopAgentServer(short sid) throws Exception {
    Socket socket = null;
    Server server = (Server) launchedServers.get(new Short(sid));
    TelnetReaderDaemon daemon = null;

    if (server != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "SCAdminClassic: stop AgentServer#" + sid);
      }
      try {
        socket = new Socket("localhost", server.telnetPort);
        daemon = new TelnetReaderDaemon(socket.getInputStream());
        daemon.start();

        socket.getOutputStream().write("halt\n".getBytes());
        socket.getOutputStream().flush();


      } catch (Throwable exc) {
        if (logmon.isLoggable(BasicLevel.ERROR))
          logmon.log(BasicLevel.ERROR, "SCAdminClassic: Can't stop server#" + sid + ", kill it.", exc);
        killAgentServer(sid);
        throw new Exception("SCAdminClassic: Can't stop server#" + sid, exc);
      }

      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "SCAdminClassic: join AgentServer#" + sid + " [" + server.process + ']');
      }
      server.process.waitFor();
      daemon.stop();
    } else {
      throw new UnknownServerException("Server " + sid + " unknown: not started using SCAdmin.");
    }
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

  public void cleanRunDir() throws Exception {
    deleteDirectory(new File(SCAdmin.RUNNING_DIR));
  }

  static public boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }

}
