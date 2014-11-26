/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

public class SuicideServerSocket extends ServerSocket {
  
  static Logger logger = Debug.getLogger(SuicideServerSocket.class.getName());

  private static int MIN_SURVIVAL_TIME = AgentServer.getInteger("ServerSocketMinSurvivalTime", 10000).intValue();

  private static int MAX_EXTRA_TIME = AgentServer.getInteger("ServerSocketMaxExtraTime", 10000).intValue();

  private static Random RAND = new Random(System.currentTimeMillis());

  public SuicideServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
    super(port, backlog, bindAddr);
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Server socket creation on port: " + port);
    }
  }

  public SuicideServerSocket(int port, int backlog) throws IOException {
    super(port, backlog);
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Server socket creation on port: " + port);
    }
  }
  
  public Socket accept() throws IOException {
    Socket socket = super.accept();
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Server socket, connection accepted on port: " + socket.getLocalPort());
    }

    try {
      int extraTime = RAND.nextInt(MAX_EXTRA_TIME);
      Timer timer = new Timer(true);
      timer.schedule(new SuicideSocketTask(socket, timer), MIN_SURVIVAL_TIME + extraTime);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Server socket will be destroyed in " + (MIN_SURVIVAL_TIME + extraTime) + " ms.");
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "Error on timer creation.", exc);
    }
    return socket;
  }
  
  private class SuicideSocketTask extends TimerTask {

    private Socket socket;
    private Timer timer;

    public SuicideSocketTask(Socket socket, Timer timer) {
      this.socket = socket;
      this.timer = timer;
    }

    public synchronized void run() {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Time elapsed: server socket suicide ! " + socket.getLocalPort());
      }
      try {
        if (!socket.isClosed())
          socket.close();
      } catch (IOException exc) {
        logger.log(BasicLevel.ERROR, "", exc);
      }
      timer.cancel();
    }
  }
}
