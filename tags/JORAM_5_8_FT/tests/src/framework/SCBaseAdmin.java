/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

public abstract class SCBaseAdmin implements SCAdminItf {

  protected abstract byte[] getHaltCommand();
  
  protected static final Logger logmon = Debug.getLogger(SCBaseAdmin.class.getName());

  /** Map containing all <code>Process</code> of running AgentServers */
  protected Map<Short, Server> launchedServers = new HashMap<Short, Server>();

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
    startAgentServer(sid, null);
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
        daemon = new TelnetReaderDaemon(socket.getInputStream(), logmon);
        daemon.start();

        socket.getOutputStream().write(getHaltCommand());
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

  public void stopAgentServerExt(int telnetPort) throws Exception {
    Socket socket = null;
    TelnetReaderDaemon daemon = null;

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminClassic: stop on port " + telnetPort);
    }
    try {
      socket = new Socket("localhost", telnetPort);
      daemon = new TelnetReaderDaemon(socket.getInputStream(), logmon);
      daemon.start();

      socket.getOutputStream().write(getHaltCommand());
      socket.getOutputStream().flush();
    } catch (Throwable exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "SCAdminClassic: Can't stop server on port " + telnetPort, exc);
      if (daemon != null) {
        daemon.stop();
      }
      throw new Exception("Can't stop server on port " + telnetPort + ": " + exc.getMessage());
    }

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdminClassic: wait closing telnet connection on " + telnetPort);
    }
    while (daemon.isRunning()) {
      Thread.sleep(500);
    }
    daemon.close();
  }

  protected static class TelnetReaderDaemon extends Daemon {

    private InputStreamReader reader;

    protected TelnetReaderDaemon(InputStream stream, Logger logger) {
      super("TelnetReaderDaemon", logger);
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

  protected static class Server {
    public int telnetPort;

    public Process process;

    public Server(int telnetPort, Process process) {
      super();
      this.telnetPort = telnetPort;
      this.process = process;
    }
  }
  /* *********************************************************** */

  protected static final int MIN_PORT_NUMBER = 1025;
  protected static final int MAX_PORT_NUMBER = 32760;
  protected static final int PORT_CHECK_RANGE = 1000;
  protected static final int PORT_CHECK_START = 20000;

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
