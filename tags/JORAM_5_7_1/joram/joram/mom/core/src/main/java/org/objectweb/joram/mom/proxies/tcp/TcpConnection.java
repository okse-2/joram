/*
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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

import java.util.Date;

import org.objectweb.joram.mom.proxies.ReliableConnectionContext;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Handles the TCP connection. Starts the reader and writer threads responsible for
 * reading the requests and writing the replies.
 * Calls the <code>UserConnection</code> in order to invoke the user's proxy and get
 * its replies.
 *
 * @see TcpProxyService
 * @see TcpConnectionListener
 */
public class TcpConnection implements TcpConnectionMBean {
  /** logger */
  public static Logger logger = Debug.getLogger(TcpConnection.class.getName());

  private IOControl ioctrl;

  private AgentId proxyId;

  private ReliableConnectionContext ctx;
  /**
   * The reader thread responsible for reading the requests (input).
   */
  private TcpReader tcpReader;

  /**
   * The writer thread responsible for writing the replies (output).
   */
  private TcpWriter tcpWriter;

  /**
   * The TCP proxy service used to register and unregister this connection.
   */
  private TcpProxyService proxyService;

  private boolean closeConnection;
  
  private Identity identity;
  
  private Date creationDate;

  /**
   * Creates a new TCP connection.
   *
   * @param ioctrl
   * @param ctx
   * @param proxyId
   * @param proxyService the TCP proxy service
   * @param identity 
   */
  public TcpConnection(IOControl ioctrl,
                       ReliableConnectionContext ctx,
                       AgentId proxyId,
                       TcpProxyService proxyService,
                       Identity identity) {
    this.creationDate = new Date();
    this.ioctrl = ioctrl;
    this.proxyId = proxyId;
    this.ctx = ctx;
    this.proxyService = proxyService;
    this.closeConnection = ctx.getHeartBeat() == 0;
    this.identity = identity;
    try {
      MXWrapper.registerMBean(this, "Joram#" + AgentServer.getServerId(), getMBeanName());
    } catch (Exception e) {
      logger.log(BasicLevel.DEBUG, "registerMBean", e);
    }
  }

  private String getMBeanName() {
    return proxyService.getMBeanName() + ",id=" + identity.getUserName() + "[" + ctx.getKey() + "]";
  }

  public final AgentId getProxyId() {
    return proxyId;
  }

  public final int getKey() {
    return ctx.getKey();
  }

  /**
   * Starts the connection reader and writer threads.
   */
  void start() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpConnection.start()");
    try {
      tcpWriter = new TcpWriter(ioctrl, ctx.getQueue(), this);
      tcpReader = new TcpReader(ioctrl, proxyId, this, closeConnection);
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
  public void close() {
    if (tcpWriter != null)
      tcpWriter.stop();
    if (tcpReader != null)
      tcpReader.stop();
    if (ioctrl != null)
      ioctrl.close();
    try {
      MXWrapper.unregisterMBean("Joram#" + AgentServer.getServerId(), getMBeanName());
    } catch (Exception e) {
      logger.log(BasicLevel.DEBUG, "unregisterMBean", e);
    }
    ioctrl = null;
    proxyService.unregisterConnection(this);
  }

  public String getUserName() {
    return identity.getUserName();
  }

  public String getAddress() {
    return ioctrl.getSocket().toString();
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public long getReceivedCount() {
    return ioctrl.getReceivedCount();
  }

  public long getSentCount() {
    return ioctrl.getSentCount();
  }

}
