/*
 * Copyright (C) 2002 SCALAGENT
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.net.*;

import fr.dyade.aaa.agent.*;

public class SCAdmin {
  private static A3CMLHandler a3configHdl = null;

  /**
   * Stops cleanly an agent server from its id.
   *
   * @param sid		id of agent server to stop
   */
  public static void stopAgentServer(short sid) throws Exception {
    Socket socket = null;
    try {
      A3CMLServer server = (A3CMLServer) a3configHdl.servers.get(new Short(sid));
      String host = server.hostname;
      int port = Integer.parseInt(
        a3configHdl.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
      socket = new Socket(host, port);
      socket.getOutputStream().write((AdminProxy.STOP_SERVER + "\n").getBytes());
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      throw new Exception("Can't stop server#" + sid +
                          ": " + exc.getMessage());
    } finally {
      close(socket);
      socket = null;
    }
  }

  /**
   * Stops violently an agent server from its id. Be careful an AgentServer
   * must be initialized (for configuration).
   *
   * @param sid		id of agent server to stop
   */
  public static void crashAgentServer(short sid) throws Exception {
    Socket socket = null;
    try {
      A3CMLServer server = (A3CMLServer) a3configHdl.servers.get(new Short(sid));
      String host = server.hostname;
      int port = Integer.parseInt(
        a3configHdl.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
      socket = new Socket(host, port);
      socket.getOutputStream().write((AdminProxy.CRASH_SERVER + "\n").getBytes());
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
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

  public static void main(String args[]) {
    try {
      a3configHdl = A3CMLHandler.getConfig((short) 0);

      if (args[0].equalsIgnoreCase("start")) {
        System.err.println("not yet implemented");
      } else if (args[0].equalsIgnoreCase("stop")) {
        short sid = Short.parseShort(args[1]);
        stopAgentServer(sid);
      } else if (args[0].equalsIgnoreCase("crash")) {
        short sid = Short.parseShort(args[1]);
        crashAgentServer(sid);
      } else {
        System.err.println("command not found");
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    }

  }
}
