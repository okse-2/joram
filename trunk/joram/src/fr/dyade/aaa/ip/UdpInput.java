/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */


package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;
import fr.dyade.aaa.agent.*;


/**
 * Class building <code>UdpPacket</code> notifications from
 * <code>DatagramPacket</code> objects received at a UDP socket.
 *
 * @author	Lacourte Serge
 * @version	v1.1
 *
 * @see	UdpPacket
 * @see	UdpProxy
 */
public class UdpInput implements NotificationInputStream {

public static final String RCS_VERSION="@(#)$Id: UdpInput.java,v 1.9 2004-02-13 10:23:58 fmaistre Exp $"; 

  protected DatagramSocket server = null;
  protected byte[] buffer = null;

  public UdpInput(DatagramSocket server, int maxSize) {
    this.server = server;
    buffer = new byte[maxSize];
  }

  /**
   * Gets a <code>Notification</code> from the stream.
   */
  public Notification readNotification()
    throws ClassNotFoundException, IOException {
    // reads next datagram
    DatagramPacket client = new DatagramPacket(buffer, buffer.length);
    server.receive(client);
    return new UdpPacket(client);
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    server.close();
  }
}
