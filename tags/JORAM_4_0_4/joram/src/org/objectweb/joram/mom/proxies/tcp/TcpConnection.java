/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): David Feliot (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies.tcp;

import org.objectweb.joram.mom.proxies.*;

import java.net.*;
import java.io.*;

import org.objectweb.joram.mom.MomTracing;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.ReliableTcpConnection;

/**
 * Handles the TCP connection. Starts the
 * reader and writer threads responsible for
 * reading the requests and writing the replies.
 * Calls the <code>UserConnection</code> in order
 * to invoke the user's proxy and get its replies.
 *
 * @see TcpProxyService
 * @see TcpConnectionListener
 */
public class TcpConnection {

  private ReliableTcpConnection reliableTcp;
  
  /**
   * The connection with the user's proxy
   */
  private UserConnection userConnection;

  /**
   * The reader thread responsible for
   * reading the requests (input).
   */
  private TcpReader tcpReader;

  /**
   * The writer thread responsible for
   * writing the replies (output).
   */
  private TcpWriter tcpWriter;

  /**
   * The TCP proxy service used to 
   * register and unregister this connection.
   */
  private TcpProxyService proxyService;

  /**
   * Creates a new TCP connection.
   *
   * @param sock the TCP connection socket
   * @param userConnection the connection with
   * the user's proxy
   * @param proxyService the TCP proxy service
   */
  public TcpConnection(
    UserConnection userConnection,
    TcpProxyService proxyService) {    
    this.userConnection = userConnection;
    this.proxyService = proxyService;    
    reliableTcp = 
      (ReliableTcpConnection)userConnection.
      getContext();
  }

  /**
   * Starts the connection reader and writer threads.
   */
  void start() throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpConnection.start()");
    try {
      tcpWriter = new TcpWriter(
        userConnection,
        this);
      tcpReader = new TcpReader(
        userConnection,
        this);
      proxyService.registerConnection(this);
      tcpWriter.start();
      tcpReader.start();
    } catch (Exception exc) {
      close();
      throw exc;
    }
  }

  /**
   * Stops the connection reader and 
   * writer threads.
   * Closes the socket.
   */
  void close() {
    if (tcpWriter != null)
      tcpWriter.stop();
    if (tcpReader != null)
      tcpReader.stop();
    if (reliableTcp != null)
      reliableTcp.close();
    reliableTcp = null;
    proxyService.unregisterConnection(
      TcpConnection.this);
  }

  public String toString() {
    return '(' + super.toString() + 
      ",userConnection=" + userConnection + ')';
  }
}
