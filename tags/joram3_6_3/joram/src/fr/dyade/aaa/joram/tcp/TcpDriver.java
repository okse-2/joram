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
 * Contributor(s):
 */
package fr.dyade.aaa.joram.tcp;

import fr.dyade.aaa.mom.jms.AbstractJmsReply;

import java.io.*;


/**
 * A <code>TcpDriver</code> gets server deliveries coming through a TCP socket.
 */
class TcpDriver extends fr.dyade.aaa.joram.Driver
{
  /** The input stream to listen on. */
  private ObjectInputStream ois;

  /**
   * Constructs a <code>TcpDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   * @param ois  The connection's input stream.
   */
  TcpDriver(fr.dyade.aaa.joram.Connection cnx, ObjectInputStream ois)
  {
    super(cnx);
    this.ois = ois;
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
    return (AbstractJmsReply) ois.readObject();
  } 

  /** Shuts down the driver. */
  public void shutdown()
  {
    try {
      ois.close();
    }
    catch (Exception e) {}
    close();
  }
}
