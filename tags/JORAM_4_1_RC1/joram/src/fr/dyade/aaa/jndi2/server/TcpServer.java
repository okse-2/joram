/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;

public class TcpServer {

  private volatile ServerSocket listen;

  private Monitor monitors[];  

  private AgentId serverId;

  public TcpServer(ServerSocket listen,
                   int nbm,
                   AgentId serverId) {
    this.listen = listen;
    this.monitors = new Monitor[nbm];
    this.serverId = serverId;
    for (int i = 0; i < monitors.length; i++) {
      monitors[i] = new Monitor("JndiServer.Monitor#" + i, this);
      monitors[i].setDaemon(true);
      monitors[i].setThreadGroup(AgentServer.getThreadGroup());
    }
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

    private TcpServer tcpServer;

    protected Monitor(String name,
                      TcpServer tcpServer) {
      super(name);
      this.tcpServer = tcpServer;
    }
    
    public final void run() {
      Socket socket;
      try {
        loop:
	while (running) {
	  canStop = true;
	  try {
            ServerSocket listen = tcpServer.getListen();
            if (listen != null) {
              socket = listen.accept();
              canStop = false;
            } else {
              break loop;
            }
	  } catch (IOException exc) {
	    if (running) {
              Trace.logger.log(
                BasicLevel.DEBUG,
                this.getName() + 
                ", error during accept", exc);
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {}
              continue loop;
            } else {
              break loop;
            }
          }

	  if (! running) break loop;
          
          if (Trace.logger.isLoggable(BasicLevel.DEBUG)) {
            Trace.logger.log(
              BasicLevel.DEBUG,
              this.getName() + ", connection from " +
              socket.getInetAddress() + ':' +
              socket.getPort());
          }

          try {
            Channel.sendTo(
              tcpServer.getServerId(), 
              new TcpRequestNot(
                new TcpRequestContext(socket)));
          } catch (Exception exc) {
            Trace.logger.log(
              BasicLevel.ERROR,
              this.getName() + 
              ", error during send", exc);
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
