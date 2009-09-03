/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
package org.objectweb.joram.mom.proxies.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.AckedQueue;
import org.objectweb.joram.mom.proxies.GetConnectionNot;
import org.objectweb.joram.mom.proxies.OpenConnectionNot;
import org.objectweb.joram.mom.proxies.ReliableConnectionContext;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Daemon;

/**
 * Listens to the TCP connections from the JMS clients.
 * Creates a <code>TcpConnection</code> for each
 * accepted TCP connection.
 * Opens the <code>UserConnection</code> with the
 * right user's proxy.
 */
public class TcpConnectionListener extends Daemon {
  /**
   * The server socket listening to connections from the JMS clients.
   */
  private ServerSocket serverSocket;

  private DataInputStream dis;

  private DataOutputStream dos;
  
  /**
   * The TCP proxy service 
   */
  private TcpProxyService proxyService;

  private int timeout;

  /**
   * Creates a new connection listener
   *
   * @param serverSocket the server socket to listen to
   * @param proxyService the TCP proxy service of this
   * connection listener
   */
  public TcpConnectionListener(ServerSocket serverSocket,
                               TcpProxyService proxyService,
                               int timeout) {
    super("TcpConnectionListener");
    this.serverSocket = serverSocket;
    this.proxyService = proxyService;
    this.timeout = timeout;
  }

  public void run() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpConnectionListener.run()");

    // Wait for the admin topic deployment.
    // (a synchronization would be much better)
    try {
      Thread.sleep(2000);
    } catch (InterruptedException exc) {
      // continue
    }

    loop:
    while (running) {
      canStop = true;
      if (serverSocket != null) {
        try {
          acceptConnection();
        } catch (Exception exc) {
          if (running) {
            continue loop;
          } else {
            break loop;
          }
        }
      }
    }
  }

  /**
   * Accepts a TCP connection. Opens the <code>UserConnection</code> with the
   * right user's proxy, creates and starts the <code>TcpConnection</code>.
   */
  private void acceptConnection() throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
          "TcpConnectionListener.acceptConnection()");

    Socket sock = serverSocket.accept();
    String inaddr = sock.getInetAddress().getHostAddress();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, " -> accept connection");

    try {
      sock.setTcpNoDelay(true);

      // Fix bug when the client doesn't
      // use the right protocol (e.g. Telnet)
      // and blocks this listener.
      sock.setSoTimeout(timeout);

      dis = new DataInputStream(sock.getInputStream());
      dos = new DataOutputStream(sock.getOutputStream());

      String userName = dis.readUTF();
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, " -> read userName = "
            + userName);
      String userPassword = dis.readUTF();
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, " -> read userPassword = "
            + userPassword);
      int key = dis.readInt();
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, " -> read key = " + key);
      int heartBeat = 0;
      if (key == -1) {
        heartBeat = dis.readInt();
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG, " -> read heartBeat = "
              + heartBeat);
      }

      GetProxyIdNot gpin = new GetProxyIdNot(userName, userPassword, inaddr);
      AgentId proxyId;
      try {
        gpin.invoke(new AgentId(AgentServer.getServerId(),
                                AgentServer.getServerId(),
                                AgentId.JoramAdminStamp));
        proxyId = gpin.getProxyId();
      } catch (Exception exc) {
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
        dos.writeInt(1);
        dos.writeUTF(exc.toString());
        return;
      }

      IOControl ioctrl;
      AckedQueue replyQueue;
      if (key == -1) {
        OpenConnectionNot ocn = new OpenConnectionNot(true, heartBeat);
        ocn.invoke(proxyId);
        dos.writeInt(0);
        ReliableConnectionContext ctx =
          (ReliableConnectionContext)ocn.getConnectionContext();
        key = ctx.getKey();
        dos.writeInt(ctx.getKey());
        dos.flush();
        replyQueue = (AckedQueue) ctx.getQueue();
        ioctrl = new IOControl(sock);
      } else {
        GetConnectionNot gcn = new GetConnectionNot(key);
        try {
          gcn.invoke(proxyId);
        } catch (Exception exc) {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
          dos.writeInt(1);
          dos.writeUTF(exc.getMessage());
          dos.flush();
          return;
        }
        ReliableConnectionContext ctx =
          (ReliableConnectionContext)gcn.getConnectionContext();
        replyQueue = ctx.getQueue();
        heartBeat = ctx.getHeartBeat();
        dos.writeInt(0);
        dos.flush();
        ioctrl = new IOControl(sock, ctx.getInputCounter());

        TcpConnection tcpConnection = proxyService.getConnection(proxyId, key);
        if (tcpConnection != null) {
          tcpConnection.close();
        }
      }

      // Reset the timeout in order to enable the server to indefinitely
      // wait for requests.
      sock.setSoTimeout(0);

      TcpConnection tcpConnection = new TcpConnection(ioctrl, proxyId,
          replyQueue, key, proxyService, heartBeat == 0);
      tcpConnection.start();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      sock.close();
      throw exc;
    }
  }
  
  protected void shutdown() {
    close();
  }
    
  protected void close() {
    try {
      if (serverSocket != null)
        serverSocket.close();
    } catch (IOException exc) {}
    serverSocket = null;
  }
}
