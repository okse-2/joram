/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot (ScalAgent DT)
 * Contributor(s): 
 */
package org.objectweb.joram.mom.proxies.tcp;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.*;

import java.net.*;
import java.io.*;
import java.util.*;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.proxies.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Listens to the TCP connections from the JMS clients.
 * Creates a <code>TcpConnection</code> for each
 * accepted TCP connection.
 * Opens the <code>UserConnection</code> with the
 * right user's proxy.
 */
public class TcpConnectionListener 
    extends Daemon {

  /**
   * The server socket listening to connections
   * from the JMS clients.
   */
  private ServerSocket serverSocket;

  private DataInputStream dis;

  private DataOutputStream dos;
  
  /**
   * The TCP proxy service 
   */
  private TcpProxyService proxyService;

  /**
   * Creates a new connection listener
   *
   * @param serverSocket the server socket to listen to
   * @param proxyService the TCP proxy service of this
   * connection listener
   */
  public TcpConnectionListener(
    ServerSocket serverSocket,
    TcpProxyService proxyService) {
    super("TcpConnectionListener");
    this.serverSocket = serverSocket;
    this.proxyService = proxyService;
  }

  public void run() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpConnectionListener.run()");
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
   * Accepts a TCP connection. Opens the 
   * <code>UserConnection</code> with the
   * right user's proxy, creates and starts 
   * the <code>TcpConnection</code>.
   */
  private void acceptConnection() 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpConnectionListener.acceptConnection()");

    Socket sock = serverSocket.accept();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        " -> accept connection");

    try {
      sock.setTcpNoDelay(true);
      
      // Fix bug when the client doesn't
      // use the right protocol (e.g. Telnet)
      // and blocks this listener.
      sock.setSoTimeout(10000);
      
      dis = new DataInputStream(
        sock.getInputStream());
      dos = new DataOutputStream(
        sock.getOutputStream());
      
      String userName = dis.readUTF();
      int key = dis.readInt();
      
      UserConnection userConnection;
      if (key == -1) {
        String userPassword = dis.readUTF();
        int timeout = dis.readInt();
        try {
          ReliableTcpConnection reliableTcp =
            new ReliableTcpConnection();        
          userConnection = 
            ConnectionManager.openConnection(
              userName, 
              userPassword, 
              timeout,
              reliableTcp);
          dos.writeInt(0);
          dos.writeInt(userConnection.getKey());
        } catch (Exception exc) {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
          dos.writeInt(1);
          dos.writeUTF(exc.getMessage());
          return;
        }
      } else {
        userConnection = ConnectionManager.getConnection(
          userName, key);      
        if (userConnection == null) {
          dos.writeInt(1);
          dos.writeUTF("Connection closed");
          return;
        } else {
          dos.writeInt(0);
        }
      }
      
      ReliableTcpConnection reliableTcp =
        (ReliableTcpConnection)userConnection.getContext();
      
      // Reset the timeout in order
      // to enable the server to indefinitely
      // wait for requests.
      sock.setSoTimeout(0);
      
      reliableTcp.init(sock);
      
      TcpConnection tcpConnection = 
        new TcpConnection(
          userConnection,
          proxyService);
      tcpConnection.start();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(
          BasicLevel.DEBUG, "", exc);
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


