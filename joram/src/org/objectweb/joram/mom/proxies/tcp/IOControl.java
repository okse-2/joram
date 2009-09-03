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

  private NetOutputStream nos;

  private BufferedInputStream bis;

  private int windowSize;

  private int unackCounter;

  public IOControl(Socket sock) throws IOException {
    this(sock, -1);
  }
    
  public IOControl(Socket sock,
		   long inputCounter)  throws IOException {    
    windowSize = Integer.getInteger(
      fr.dyade.aaa.util.ReliableTcpConnection.WINDOW_SIZE_PROP_NAME,
      fr.dyade.aaa.util.ReliableTcpConnection.DEFAULT_WINDOW_SIZE).intValue();
    unackCounter = 0;
    this.inputCounter = inputCounter;
    this.sock = sock;

    nos = new NetOutputStream(sock);
    bis = new BufferedInputStream(sock.getInputStream());
  }

  public synchronized void send(ProxyMessage msg) throws IOException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "IOControl.send(" + 
        msg + ')');
    try {
      nos.send(msg.getId(), msg.getAckId(), msg.getObject());
      unackCounter = 0;
    } catch (IOException exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }

  static class NetOutputStream {
    private ByteArrayOutputStream baos = null;
    private ObjectOutputStream oos = null;
    private OutputStream os = null;

    static private final byte[] streamHeader = {
      (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
    };

    NetOutputStream(Socket sock) throws IOException {
      baos = new ByteArrayOutputStream(1024);
      oos = new ObjectOutputStream(baos);
      baos.reset();
      os = sock.getOutputStream();
    }

    void send(long id, long ackId, Object msg) throws IOException {
      try {
        baos.write(streamHeader, 0, 4);
        oos.writeLong(id);
        oos.writeLong(ackId);
        oos.writeObject(msg);
        oos.flush();

        baos.writeTo(os);
        os.flush();
      } finally {
        oos.reset();
        baos.reset();
      }
    }
  }
  
  public ProxyMessage receive() throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "IOControl.receive()");
    try {
      while (true) {
        ObjectInputStream ois = new ObjectInputStream(bis);
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
      if (bis != null) bis.close();
      bis = null;
    } catch (IOException exc) {}
    try { 
      if (sock != null) sock.getOutputStream().close();
    } catch (IOException exc) {}
    try { 
      if (sock != null) sock.close();
      sock = null;
    } catch (IOException exc) {}
  }
}
