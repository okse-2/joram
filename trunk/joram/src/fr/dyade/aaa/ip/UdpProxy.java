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

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.agent.*;

/**
 * This class provides access to UDP communications. It receives packets from
 * a UDP socket and changes them into <code>UdpPacket</code> notifications to
 * be handled by the <code>doReact</code> abstract function. It forwards
 * <code>UdpPacket</code> notifications received from other agents as packets
 * onto the UDP socket.
 */
public abstract class UdpProxy extends ProxyAgent {
  /** RCS version number of this file: $Revision: 1.7 $ */
  public static final String RCS_VERSION="@(#)$Id: UdpProxy.java,v 1.7 2002-03-06 16:55:06 joram Exp $"; 

  /** default max buffer size for reading datagrams */
  protected static final int BUF_SIZE = 8192;

  /** listening port, may be 0 */
  public int localPort = -1;
  /** maximum size of datagrams to be received */
  public int maxSize;

  /** actual listening port */
  protected transient int listenPort = -1;
  /** UDP socket used for receiving and sending packets */
  protected transient DatagramSocket server = null;

  /**
   * Constructor.
   * <p>
   * The listening port number may be fixed or not. In the latter case
   * the actual listening port number may be known in an overloaded
   * <code>initialize</code> function, after a call to the base class
   * function, in the updated <code>listenPort</code> variable.
   *
   * @param localPort		listening port number, or 0 for any port
   * @param maxSize		maximum size of datagrams to be received
   */
  public UdpProxy(int localPort, int maxSize) {
    blockingCnx = false;
    this.localPort = localPort;
    this.maxSize = maxSize;
  }

  /**
   * Creates a UDP server with default buffer size to <code>BUF_SIZE</code>.
   *
   * @param localPort		port number, or 0 for any port
   * @param maxSize		maximum size of datagrams to be received
   */
  public UdpProxy(int localPort) {
    this(localPort, BUF_SIZE);
  }


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +
      ",localPort=" + localPort +
      ",listenPort=" + listenPort +
      ",maxSize=" + maxSize + ")";
  }

  /**
   * Initializes the connection with the outside, up to creating
   * the input and output streams <code>ois</code> and <code>oos</code>.
   * <p>
   * The function provides a default input filter as a <code>UdpInput</code>
   * object, which may be refined in the <code>setInputFilters</code>
   * function which may be overloaded in derived classes.
   * <p>
   * No output filter is defined so that no out driver is run. Issuing
   * UDP packets is a non blocking operation, which may be performed in
   * the main thread.
   */
  public void connect() throws Exception {
    if (localPort >= 0) {
      // this is a server
      if (localPort == 0) {
	server = new DatagramSocket();
      } else {
	server = new DatagramSocket(localPort);
      }
      listenPort = server.getLocalPort();
      ois = setInputFilters(new UdpInput(server, maxSize));
    } else {
      // this is a client
      // not yet implemented
      throw new IllegalStateException("UDP client client not implemented");
    }
  }

  public void disconnect() throws IOException {
  }

  /**
   * Creates a (chain of) filter(s) for specializing the specified
   * <code>UdpInput</code>.
   */
  protected NotificationInputStream setInputFilters(UdpInput in)
    throws StreamCorruptedException, IOException {
    return in;
  }

  /**
   * Reacts to notifications.
   * Assumes notifications from nullId come from drvIn; let derive classes
   * handle them. Forwards notifications coming from an identified agent
   * onto the outgoing connection.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof UdpPacket) {
      try {
	if (from.equals(getId())) {
	  // incoming datagram
	  doReact((UdpPacket) not);
	} else {
	  // sends packet
	  UdpPacket packet = (UdpPacket) not;
	  server.send(new DatagramPacket(
	    packet.data, packet.data.length, packet.address, packet.port));
	}
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   getName() + ", exception in " +
                   this + ".react(" + from + "," + not + ")",
                   exc);
	stop();
      }
    } else {
      super.react(from, not);
    }
  }

  /**
   * Reacts to <code>UdpPacket</code> notifications coming from UDP socket.
   * <p>
   * This function must be defined in derived classes.
   *
   * @param packet	notification to react to
   */
  public abstract void doReact(UdpPacket packet) throws Exception;
}
