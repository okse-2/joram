/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.proxies.tcp;

import org.objectweb.joram.mom.proxies.*;

import org.objectweb.joram.shared.JoramTracing;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;

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

  private IOControl ioctrl;

  private AgentId proxyId;

  private int key;

  private AckedQueue replyQueue;

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

  private boolean closeConnection;

  /**
   * Creates a new TCP connection.
   *
   * @param sock the TCP connection socket
   * @param proxyService the TCP proxy service
   */
  public TcpConnection(
    IOControl ioctrl,
    AgentId proxyId,
    AckedQueue replyQueue,
    int key,
    TcpProxyService proxyService,
    boolean closeConnection) {    
    this.ioctrl = ioctrl;
    this.proxyId = proxyId;
    this.replyQueue = replyQueue;
    this.key = key;
    this.proxyService = proxyService;
    this.closeConnection = closeConnection;
  }

  public final AgentId getProxyId() {
    return proxyId;
  }

  public final int getKey() {
    return key;
  }

  /**
   * Starts the connection reader and writer threads.
   */
  void start() throws Exception {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpConnection.start()");
    try {
      tcpWriter = new TcpWriter(
	ioctrl,
        replyQueue,
        this);
      tcpReader = new TcpReader(
        ioctrl,
	proxyId,
        this,
        closeConnection);
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
    if (ioctrl != null)
      ioctrl.close();
    ioctrl = null;
    proxyService.unregisterConnection(
      TcpConnection.this);
  }

}
