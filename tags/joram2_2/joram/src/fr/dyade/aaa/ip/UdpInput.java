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

public static final String RCS_VERSION="@(#)$Id: UdpInput.java,v 1.6 2002-01-16 12:46:47 joram Exp $"; 

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
