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
 * The activity responsible for getting the replies
 * from the user's proxy and writing them to the
 * socket. 
 */
public class TcpWriter extends Daemon {
  
  /**
   * The connection with the user's proxy.
   */
  private UserConnection userConnection;
  
  /**
   * The TCP connection that started
   * this writer.
   */
  private TcpConnection tcpConnection;

  private ReliableTcpConnection reliableTcp;

  /**
   * The output stream where to write
   */
  private ObjectOutputStream out;

  /**
   * Creates a new writer.
   *
   * @param sock the socket where to write
   * @param userConnection the connection 
   * with the user's proxy
   * @param tcpConnection the TCP connection
   */
  public TcpWriter(UserConnection userConnection,
                   TcpConnection tcpConnection) 
    throws IOException {
    super("tcpWriter");
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
        "TcpWriter.run()");
    try {
      while (running) {
        AbstractJmsReply reply =  
          userConnection.receive();
        reliableTcp.send((AbstractJmsReply) reply);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(
          BasicLevel.DEBUG, "", exc);
    }    
  }

  protected void shutdown() {
    close();
  }
    
  protected void close() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpWriter.close()");
    if (reliableTcp != null)
      reliableTcp.close();
    reliableTcp = null;
  }
}
