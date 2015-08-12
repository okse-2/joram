/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Daemon;

public class TcpServer implements TcpServerMBean {

  private volatile ServerSocket listen;

  private Monitor monitors[];  

  private AgentId serverId;

  public TcpServer(ServerSocket listen, int poolSize, int timeout, AgentId serverId) {
    this.listen = listen;
    this.monitors = new Monitor[poolSize];
    this.serverId = serverId;
    for (int i = 0; i < monitors.length; i++) {
      monitors[i] = new Monitor("JndiServer.Monitor#" + i, timeout, this);
      monitors[i].setDaemon(true);
      monitors[i].setThreadGroup(AgentServer.getThreadGroup());
    }
  }
  
  /**
   * Gets the number of threads of the pool.
   * 
   * @return the number of threads of the pool.
   */
  public int getPoolSize() {
    return monitors.length;
  }
  
  /**
   * Gets the listen port of the server.
   * 
   * @return the listen port of the server.
   */
  public int getListenPort() {
    return listen.getLocalPort();
  }
  
  public final void start() {
    for (int i = 0; i < monitors.length; i++) {
      monitors[i].start();
    }
  }

  public final void stop() {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, "TcpServer.stop()");
    for (int i = 0; i < monitors.length; i++) {
      monitors[i].stopping = true;
    }
    try {
      listen.close();
      listen = null;
    } catch (Exception exc) {}
    for (int i = 0; i < monitors.length; i++) {
      monitors[i].stop();
    }
  }

  public final ServerSocket getListen() {
    return listen;
  }

  public final AgentId getServerId() {
    return serverId;
  }

  public static class Monitor extends Daemon {

    private int timeout;

    private TcpServer tcpServer;

    boolean stopping = false;

    protected Monitor(String name, int timeout, TcpServer tcpServer) {
      super(name, Trace.logger);
      this.timeout = timeout;
      this.tcpServer = tcpServer;
    }

    public final void run() {
      Socket socket;
      try {
        loop: while (running) {
          canStop = true;
          try {
            ServerSocket listen = tcpServer.getListen();
            if (listen != null) {
              socket = listen.accept();
              socket.setTcpNoDelay(true);
              socket.setSoTimeout(timeout);
              socket.setSoLinger(true, 1000);
              canStop = false;
            } else {
              break loop;
            }
          } catch (IOException exc) {
            canStop = false;
            Thread.interrupted();
            if (running && !stopping) {
              Trace.logger.log(BasicLevel.DEBUG, this.getName() + ", error during accept", exc);
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {
              }
              continue loop;
            }
            
            break loop;
          } finally {
            canStop = false;
          }

          if (!running)
            break loop;

          if (Trace.logger.isLoggable(BasicLevel.DEBUG)) {
            Trace.logger.log(BasicLevel.DEBUG,
                this.getName() + ", connection from " + socket.getInetAddress() + ':' + socket.getPort());
          }

          try {
            TcpRequestContext ctx = new TcpRequestContext(socket);
            Channel.sendTo(tcpServer.getServerId(), new TcpRequestNot(ctx));
          } catch (Exception exc) {
            Trace.logger.log(BasicLevel.ERROR, this.getName() + ", error during send", exc);
            if (socket != null) {
              try {
                socket.close();
              } catch (IOException exc2) {
              }
            }
          }
        }
      } finally {
        finish();
      }
    }

    protected void close() {
      
    }
    
    protected void shutdown() {
      close();
    }
  }
}
