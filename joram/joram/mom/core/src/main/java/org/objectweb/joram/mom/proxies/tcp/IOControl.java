/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.joram.mom.proxies.ProxyMessage;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

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
  
  private LinkedBlockingQueue<byte[]> receiveQueue;
  
  private Reader reader;
  
  boolean noAckedQueue = false;

  public IOControl(Socket sock) throws IOException {
    this(sock, -1);
  }
    
  public IOControl(Socket sock,
		   long inputCounter)  throws IOException {    
    windowSize = AgentServer.getInteger("fr.dyade.aaa.util.ReliableTcpConnection.windowSize", 100).intValue();
    unackCounter = 0;
    this.inputCounter = inputCounter;
    this.sock = sock;

    nos = new NetOutputStream(sock);
    bis = new BufferedInputStream(sock.getInputStream());
    
    this.receiveQueue = new LinkedBlockingQueue<byte[]>();
    reader = new Reader();
    reader.start();
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
  
  public void setNoAckedQueue(boolean noAckedQueue) {
    this.noAckedQueue = noAckedQueue;
  }
  
  public ProxyMessage receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.receive()");
    
    try {
      while (true) {
        byte[] bytes = receiveQueue.take();
        if (bytes.length == 0) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "IOControl: The reader offer a closing marker, so closed.");
          throw new IOException("Connection closed.");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        long messageId = StreamUtil.readLongFrom(bais);
        long ackId = StreamUtil.readLongFrom(bais);
        AbstractJmsRequest obj = (AbstractJmsRequest) AbstractJmsMessage.read(bais);
        receivedCount++;

        if (messageId > inputCounter) {
          inputCounter = messageId;
          if (!noAckedQueue) { 
            synchronized (this) {
              if (unackCounter < windowSize) {
                unackCounter++;
              } else {
                send(new ProxyMessage(-1, messageId, null));
              }
            }
          }
          return new ProxyMessage(messageId, ackId, obj);
        }
        logger.log(BasicLevel.DEBUG, "IOControl.receive: already received message: " + messageId + " -> " + obj);
      }
    } catch (InterruptedException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, exc);
      close();
      throw new IllegalStateException("Interrupted receive: Connection closed.");
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
    if (reader != null) {
      reader.stop();
    }
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
  
  class Reader extends fr.dyade.aaa.common.Daemon {
    Reader() {
      super("IOcontrol.Reader", logger);
    }

    public void run() {
      try {
        while (running) {
          canStop = true;
          int len = StreamUtil.readIntFrom(bis);
          byte[] bytes = new byte[len];
          int count = 0;
          int nb = -1;
          do {
            nb = bis.read(bytes, count, len-count);
            if (nb < 0) throw new EOFException();
            count += nb;
          } while (count != len);
          receiveQueue.offer(bytes);
        }
      } catch (Exception exc) { 
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, exc);
      } finally {
        // offer a closing marker (empty bytes).
        receiveQueue.offer(new byte[0]);
      }
    }
    
    /**
     * Enables the daemon to stop itself.
     */
    public void stop() {
      if (isCurrentThread()) {
        finish();
      } else {
        super.stop();
      }
    }

    protected void shutdown() {}

    protected void close() {}
    
  }
  
  public int getreceiveQueueSize() {
    return receiveQueue.size();
  }
  
}
