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
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.MomTracing;

import java.io.*;
import java.net.*;

import fr.dyade.aaa.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The activity responsible for reading the requests
 * from the socket and invoke the user's proxy.
 */
public class TcpReader extends Daemon {

  /**
   * The TCP connection that started
   * this reader.
   */
  private TcpConnection tcpConnection;

  /**
   * The connection with the user's proxy.
   */
  private UserConnection userConnection;

  private ReliableTcpConnection reliableTcp;
    
  /**
   * The input stream to read
   */
  private ObjectInputStream in;

  /**
   * Creates a new reader.
   *
   * @param sock the socket to read
   * @param userConnection the connection 
   * with the user's proxy
   * @param tcpConnection the TCP connection
   */
  public TcpReader(UserConnection userConnection,
                   TcpConnection tcpConnection) 
    throws IOException {
    super("tcpReader");
    this.userConnection = userConnection;
    this.tcpConnection = tcpConnection;
    reliableTcp = 
      (ReliableTcpConnection)userConnection.
      getContext();
  }

  public void run() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpReader.run()");
    try {
      while (running) {
        Object obj = reliableTcp.receive();
        if (obj instanceof AbstractJmsRequest) {
          receive((AbstractJmsRequest)obj);
        } else {
          throw new StreamCorruptedException(
            "Invalid object read on stream.");
        }
      }
    } catch (Throwable error) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(
          BasicLevel.DEBUG, "", error);
    } finally {
      if (userConnection.getTimeout() == 0) {
        userConnection.close();
      }
      new Thread(new Runnable() {
          public void run() {            
            tcpConnection.close();
          }
        }).start();
    }
  }

  private void receive(AbstractJmsRequest request) throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpReader.receive(" + request + ')');
    userConnection.send(request);
  }

  protected void shutdown() {
    close();
  }
    
  protected void close() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpReader.close()");
    if (reliableTcp != null)
      reliableTcp.close();
    reliableTcp = null;
  }
}
