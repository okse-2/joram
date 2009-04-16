/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.ha;

import java.io.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.jndi2.server.*;
import fr.dyade.aaa.jndi2.msg.*;

public class HATcpServer {

  private volatile ServerSocket listen;

  private Monitor monitors[];

  private AgentId serverId;

  public HATcpServer(ServerSocket listen,
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

    private HATcpServer tcpServer;

    protected Monitor(String name,
                      HATcpServer tcpServer) {
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
                BasicLevel.ERROR,
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
            IOControl ioCtrl = new IOControl(socket);
            int rid = ioCtrl.readInt();
            if (Trace.logger.isLoggable(BasicLevel.DEBUG))
              Trace.logger.log(BasicLevel.DEBUG, " -> request id = " + rid); 
            switch (rid) {
            case HARequestManager.IDEMPOTENT:
              Channel.sendTo(
                tcpServer.getServerId(), 
                new TcpRequestNot(new HARequestContext(
                  ioCtrl, HARequestManager.IDEMPOTENT)));
              break;
            case HARequestManager.NOT_IDEMPOTENT:
              GetRequestIdNot gri =
                new GetRequestIdNot();
              gri.invoke(tcpServer.getServerId());
              int newRid = gri.getId();
              ioCtrl.writeInt(newRid);
              Channel.sendTo(
                tcpServer.getServerId(), 
                new TcpRequestNot(new HARequestContext(
                  ioCtrl, newRid)));
              break;
            default:
              Channel.sendTo(
                tcpServer.getServerId(), 
                new TcpRequestNot(new HARequestContext(
                  ioCtrl, rid)));
            }
          } catch (Exception exc) {
            Trace.logger.log(
              BasicLevel.ERROR,
              this.getName() + 
              "", exc);
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
