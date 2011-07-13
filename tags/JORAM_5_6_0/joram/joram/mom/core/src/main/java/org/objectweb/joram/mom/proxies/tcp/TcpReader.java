/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.proxies.CloseConnectionNot;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.ProxyMessage;
import org.objectweb.joram.shared.client.AbstractJmsRequest;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

/**
 * The activity responsible for reading the requests from the socket and invoke
 * the user's proxy.
 */
public class TcpReader extends Daemon {
  /** logger */
  public static Logger logger = Debug.getLogger(TcpReader.class.getName());

  /**
   * The TCP connection that started this reader.
   */
  private TcpConnection tcpConnection;

  private IOControl ioctrl;

  private AgentId proxyId;

  private boolean closeConnection;

  /**
   * Creates a new reader.
   */
  public TcpReader(IOControl ioctrl,
                   AgentId proxyId,
                   TcpConnection tcpConnection,
                   boolean closeConnection) {
    super("tcpReader." + tcpConnection.getKey());
    this.ioctrl = ioctrl;
    this.proxyId = proxyId;
    this.tcpConnection = tcpConnection;
    this.closeConnection = closeConnection;
  }

  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpReader.run()");
    try {
      while (running) {
        ProxyMessage msg = ioctrl.receive();
        canStop = false;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "TcpReader reads msg: " + msg);
        ConnectionManager.sendToProxy(proxyId,
                                      tcpConnection.getKey(),
                                      (AbstractJmsRequest)msg.getObject(), 
                                      msg);
        canStop = true;
      }
    } catch (Throwable error) {
      canStop = false;
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", error);
    } finally {
      canStop = false;
      if (closeConnection) {
        Channel.sendTo(proxyId, new CloseConnectionNot(tcpConnection.getKey()));
      }
      new Thread(new Runnable() {
        public void run() {
          tcpConnection.close();
        }
      }).start();

    }
  }

  protected void shutdown() {
    close();
  }

  protected void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpReader.close()");
    if (ioctrl != null)
      ioctrl.close();
    ioctrl = null;
  }
}
