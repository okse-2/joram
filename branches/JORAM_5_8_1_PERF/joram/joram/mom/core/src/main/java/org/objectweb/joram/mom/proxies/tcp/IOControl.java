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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.joram.mom.proxies.ProxyMessage;
import org.objectweb.joram.mom.proxies.ReliableConnectionContext;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

public class IOControl {
  
  public static Logger logger = Debug.getLogger(IOControl.class.getName());
  
  public static int DEFAULT_BUFFER_SIZE = 8192;
  
  public static final int DEFAULT_TCP_SENDBUFFER_SIZE = 32768;

  public static final int DEFAULT_TCP_RECEIVEBUFFER_SIZE = 32768;

  private Socket sock;

  private BufferedOutputStream bos;

  private BufferedInputStream bis;
  
  private LinkedBlockingQueue<byte[]> receiveQueue;
  
  private Reader reader;

  public IOControl(Socket sock) throws IOException {   
    // JORAM_PERF_BRANCH:
    //windowSize = AgentServer.getInteger("fr.dyade.aaa.util.ReliableTcpConnection.windowSize", 100).intValue();
    //unackCounter = 0;
    //JORAM_PERF_BRANCH.
    //this.inputCounter = inputCounter;
    this.sock = sock;
    sock.setSendBufferSize(DEFAULT_TCP_SENDBUFFER_SIZE);
    sock.setReceiveBufferSize(DEFAULT_TCP_RECEIVEBUFFER_SIZE);
    sock.setTcpNoDelay(true);
    
    bos = new BufferedOutputStream(sock.getOutputStream(), DEFAULT_BUFFER_SIZE);
    bis = new BufferedInputStream(sock.getInputStream(), DEFAULT_BUFFER_SIZE);
    this.receiveQueue = new LinkedBlockingQueue<byte[]>();
    // JORAM_PERF_BRANCH
    FlushTask flushTask = new FlushTask();
    AgentServer.getTimer().schedule(flushTask, 50, 50);
    
    reader = new Reader();
    reader.start();
  }
  
  public void send(ProxyMessage msg) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.send:" + msg);
    try {
      byte[] bytes;
      if (! ReliableConnectionContext.ENGINE_ENCODE) {
        try {
          AbstractJmsMessage ajm = (AbstractJmsMessage) msg.getObject();
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          AbstractJmsMessage.write(ajm, baos);
          bytes = baos.toByteArray();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        bytes = (byte[]) msg.getObject();
      }
      // JORAM_PERF_BRANCH
      synchronized (bos) {
        StreamUtil.writeTo(bytes.length, bos);
        bos.write(bytes);
        if (! msg.isAsyncSend()) {
          bos.flush();
        }
      }
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "IOControl.send", exc);
      close();
      throw exc;
    }
  }
  
  //JORAM_PERF_BRANCH
  class FlushTask extends java.util.TimerTask {
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "FlushTask.run()");
      try {
        synchronized (bos) {
          bos.flush();
        }
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", exc);
        cancel();
      }
    }
  }
  
  public ProxyMessage receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.receive()");
    
    byte[] bytes = receiveQueue.take();
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      AbstractJmsRequest obj = (AbstractJmsRequest) AbstractJmsMessage.read(bais);
      return new ProxyMessage(obj, false);
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
    reader.stop();
  }
  
  Socket getSocket() {
    return sock;
  }
  
  class Reader extends fr.dyade.aaa.common.Daemon {
    Reader() {
      super("ReliableTcpConnectionReader", logger);
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
          logger.log(BasicLevel.DEBUG, "", exc);
        close();
      } finally {
        finish();
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
}
