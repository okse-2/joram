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

import java.net.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification which provides access to a UDP datagram packet.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		UdpInput
 */
public class UdpPacket extends Notification {
public static final String RCS_VERSION="@(#)$Id: UdpPacket.java,v 1.9 2004-02-13 10:23:58 fmaistre Exp $";

  /** source or target address of packet */
  public InetAddress address;
  /** source or target port of packet */
  public int port = -1;
  /** packet data */
  public byte[] data;

  /**
   * Constructor.
   *
   * @param packet	encapsulated packet
   */
  public UdpPacket(InetAddress address, int port, byte[] data) {
    this.address = address;
    this.port = port;
    this.data = data;
  }

  /**
   * Builds an object from a datagram packet. Duplicates data.
   *
   * @param packet	encapsulated packet
   */
  public UdpPacket(DatagramPacket packet) {
    this.address = packet.getAddress();
    this.port = packet.getPort();
    this.data = new byte[packet.getLength()];
    System.arraycopy(packet.getData(), 0, data, 0, data.length);
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",address=");
    output.append(address);
    output.append(",port=");
    output.append(port);
    output.append(",length=");
    output.append(data.length);
    output.append(")");
    return output.toString();
  }
}
