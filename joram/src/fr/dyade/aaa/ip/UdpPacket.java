/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
public static final String RCS_VERSION="@(#)$Id: UdpPacket.java,v 1.6 2002-01-16 12:46:47 joram Exp $";

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
