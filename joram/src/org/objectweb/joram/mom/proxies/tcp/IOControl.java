/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
 */
package org.objectweb.joram.mom.proxies.tcp;

import java.io.*;
import java.util.*;
import java.net.*;

import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.mom.MomTracing;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class IOControl {

  private long inputCounter;

  private Socket sock;

  private ObjectOutputStream oos;

  private ObjectInputStream ois;

  private int windowSize;

  private int unackCounter;

  public IOControl(Socket sock) throws IOException {
    this(sock, -1);
  }
    
  public IOControl(Socket sock,
		   long inputCounter) 
    throws IOException {    
    windowSize = Integer.getInteger(
      fr.dyade.aaa.util.ReliableTcpConnection.WINDOW_SIZE_PROP_NAME,
      fr.dyade.aaa.util.ReliableTcpConnection.DEFAULT_WINDOW_SIZE).intValue();
    unackCounter = 0;
    this.inputCounter = inputCounter;
    this.sock = sock;

    oos = new ObjectOutputStream(
      sock.getOutputStream());
    oos.flush();

    ois = new ObjectInputStream(
      new BufferedInputStream(
        sock.getInputStream()));
  }

  public synchronized void send(ProxyMessage msg) throws IOException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "IOControl.send(" + 
        msg + ')');
    try {
      oos.writeLong(msg.getId());
      oos.writeLong(msg.getAckId());
      oos.writeObject(msg.getObject());
      oos.reset();
      unackCounter = 0;
    } catch (IOException exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }
  
  public ProxyMessage receive() throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "IOControl.receive()");
    try {
      while (true) {
        long messageId = ois.readLong();
        long ackId = ois.readLong();
        Object obj = ois.readObject();
	if (messageId > inputCounter) {
	  inputCounter = messageId;
          synchronized (this) {
            if (unackCounter < windowSize) {
              unackCounter++;
            } else {
              send(new ProxyMessage(-1, messageId, null));
            }
          }
	  return new ProxyMessage(messageId, ackId, obj);      
	} else {
	  MomTracing.dbgProxy.log(
	    BasicLevel.DEBUG, " -> already received message: " + 
	    messageId + " " + obj);
	}
      }
    } catch (IOException exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }

  public void close() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "IOControl.close()");
    try { 
      ois.close();
    } catch (IOException exc) {}
    try { 
      oos.close();
    } catch (IOException exc) {}
    try { 
      sock.close();
    } catch (IOException exc) {}
  }
}
