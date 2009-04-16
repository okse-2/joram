/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.objectweb.joram.mom.proxies.ProxyMessage;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Debug;

public class IOControl {
  public static Logger logger = Debug.getLogger(IOControl.class.getName());

  private long inputCounter;

  private Socket sock;

  private NetOutputStream nos;

  private BufferedInputStream bis;

  private int windowSize;

  private int unackCounter;
  
  private long receivedCount;

  private long sentCount;

  public IOControl(Socket sock) throws IOException {
    this(sock, -1);
  }
    
  public IOControl(Socket sock,
		   long inputCounter)  throws IOException {    
    windowSize = AgentServer.getInteger(
      fr.dyade.aaa.util.ReliableTcpConnection.WINDOW_SIZE_PROP_NAME,
      fr.dyade.aaa.util.ReliableTcpConnection.DEFAULT_WINDOW_SIZE).intValue();
    unackCounter = 0;
    this.inputCounter = inputCounter;
    this.sock = sock;

    nos = new NetOutputStream(sock);
    bis = new BufferedInputStream(sock.getInputStream());
  }

  public synchronized void send(ProxyMessage msg) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.send:" + msg);

    try {
      nos.send(msg.getId(), msg.getAckId(), msg.getObject());
      sentCount++;
      unackCounter = 0;
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "IOControl.send", exc);
      close();
      throw exc;
    }
  }

  static class NetOutputStream extends ByteArrayOutputStream {
    private OutputStream os = null;

    NetOutputStream(Socket sock) throws IOException {
      super(1024);
      reset();
      os = sock.getOutputStream();
    }

    public void reset() {
      count = 4;
    }

    void send(long id, long ackId, AbstractJmsMessage msg) throws IOException {
      try {
        StreamUtil.writeTo(id, this);
        StreamUtil.writeTo(ackId, this);
        AbstractJmsMessage.write(msg, this);

        buf[0] = (byte) ((count -4) >>>  24);
        buf[1] = (byte) ((count -4) >>>  16);
        buf[2] = (byte) ((count -4) >>>  8);
        buf[3] = (byte) ((count -4) >>>  0);

        writeTo(os);
        os.flush();
      } finally {
        reset();
      }
    }
  }
  
  public ProxyMessage receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.receive()");

    try {
      while (true) {
        int len = StreamUtil.readIntFrom(bis);
        long messageId = StreamUtil.readLongFrom(bis);
        long ackId = StreamUtil.readLongFrom(bis);
        AbstractJmsRequest obj = (AbstractJmsRequest) AbstractJmsMessage.read(bis);
        receivedCount++;

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
          logger.log(BasicLevel.DEBUG, "IOControl.receive: already received message: " + messageId + " -> " + obj);
        }
      }
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "IOControl.receive", exc);
      close();
      throw exc;
    }
  }

  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.close()");

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
  
  Socket getSocket() {
    return sock;
  }
  
  public long getSentCount() {
    return sentCount;
  }

  public long getReceivedCount() {
    return receivedCount;
  }
  
}
