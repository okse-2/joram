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
package org.objectweb.joram.client.jms.tcp;

import org.objectweb.joram.shared.client.AbstractJmsReply;

import java.io.*;


/**
 * A <code>TcpDriver</code> gets server deliveries coming through a TCP socket.
 */
class TcpDriver extends org.objectweb.joram.client.jms.Driver
{
  private ReliableTcpClient tcpClient;

  /**
   * Constructs a <code>TcpDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   * @param ois  The connection's input stream.
   */
  TcpDriver(org.objectweb.joram.client.jms.Connection cnx,
            ReliableTcpClient tcpClient)
  {
    super(cnx);
    this.tcpClient = tcpClient;
  }

 
  /**
   * Returns an <code>AbstractJmsReply</code> delivered by the connected
   * server.
   *
   * @exception IOException  If the connection failed.
   * @exception ClassNotFoundException  If the reply is invalid.
   */
  protected AbstractJmsReply getDelivery() throws Exception
  {
    return (AbstractJmsReply) tcpClient.receive();
  } 

  /** Shuts down the driver. */
  public void shutdown()
  {
    if (tcpClient != null)
      tcpClient.close();
    tcpClient = null;
    close();
  }
}
