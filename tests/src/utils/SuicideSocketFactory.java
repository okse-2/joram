/*
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.net.SocketFactory;

/**
 * This class implements a SocketFactory. Sockets created by this factory are
 * closed after a random amount of time.
 */
public class SuicideSocketFactory extends SocketFactory {

  private static Logger logger = Debug.getLogger(SuicideSocketFactory.class.getName());
  
  private static int MIN_SURVIVAL_TIME = AgentServer.getInteger("SocketMinSurvivalTime", 10000).intValue();
  
  private static int MAX_EXTRA_TIME = AgentServer.getInteger("SocketMaxExtraTime", 10000).intValue();

  private static Random RAND = new Random(System.currentTimeMillis());
  
  /**
   * The SocketFactory singleton for this class.
   */
  static SocketFactory factory;

  /**
   * Returns the SocketFactory singleton for this class.
   *
   * @return The SocketFactory singleton for this class.
   */
  public static SocketFactory getFactory() {
    if (factory == null)
      factory = new SuicideSocketFactory();
    return factory;
  }

  /**
   *  Creates a stream socket and connects it to the specified port number at
   * the specified IP address. Try to establish the connection to the server
   * with a specified timeout value. A timeout of zero is interpreted as an
   * infinite timeout. The connection will then block until established or an
   * error occurs.
   *
   * @param addr    the IP address.
   * @param port    the port number.
   * @param timeout the timeout value to be used in milliseconds.
   */
  public Socket createSocket(InetAddress addr, int port,
                             int timeout) throws IOException {
    final Socket socket = new Socket();
    int extraTime = RAND.nextInt(MAX_EXTRA_TIME);

    socket.connect(new InetSocketAddress(addr, port), timeout);
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Socket creation on port: " + socket.getLocalPort());
    }
    
    try {
      Timer timer = new Timer(true);
      timer.schedule(new SuicideSocketTask(socket, timer), MIN_SURVIVAL_TIME + extraTime);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Socket will be destroyed in " + (MIN_SURVIVAL_TIME + extraTime) + " ms.");
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "Error on timer creation.", exc);
    }

    return socket;
  }

  /**
   *  Creates a socket and connects it to the specified remote host on the
   * specified remote port. The Socket will also bind() to the local address
   * and port supplied. Try to establish the connection to the server
   * with a specified timeout value. A timeout of zero is interpreted as an
   * infinite timeout. The connection will then block until established or an
   * error occurs.
   *
   * @param addr    the IP address of the remote host
   * @param port    the remote port
   * @param localAddr   the local address the socket is bound to
   * @param localPort   the local port the socket is bound to 
   * @param timeout the timeout value to be used in milliseconds.
   */
  public Socket createSocket(InetAddress addr, int port,
                             InetAddress localAddr, int localPort,
                             int timeout) throws IOException {
    final Socket socket = new Socket();
    
    socket.bind(new InetSocketAddress(localAddr, localPort));
    socket.connect(new InetSocketAddress(addr, port), timeout);
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Socket creation on port: " + socket.getLocalPort());
    }
    
    try {
      int extraTime = RAND.nextInt(MAX_EXTRA_TIME);
      Timer timer = new Timer();
      timer.schedule(new SuicideSocketTask(socket, timer), MIN_SURVIVAL_TIME + extraTime);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Socket will be destroyed in " + (MIN_SURVIVAL_TIME + extraTime) + " ms.");
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
        logger.log(BasicLevel.DEBUG, "Time elapsed: socket suicide ! " + socket.getLocalPort());
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
