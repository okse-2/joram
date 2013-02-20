/*
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

public class ReliableTcpConnection {

  public static final int INIT = 0;
  public static final int CONNECT = 1;
  public static final int CONNECTING = 2;

  public static final String[] statusNames = {"INIT", "CONNECT", "CONNECTING"};

  public static Logger logger = Debug.getLogger(ReliableTcpConnection.class.getName());

  public static String WINDOW_SIZE_PROP_NAME = "fr.dyade.aaa.util.ReliableTcpConnection.windowSize";

  public static int DEFAULT_WINDOW_SIZE = 100;
  
  public static int DEFAULT_BUFFER_SIZE = 8192;
  
  public static final int DEFAULT_TCP_SENDBUFFER_SIZE = 32768;

  public static final int DEFAULT_TCP_RECEIVEBUFFER_SIZE = 32768;

  private int windowSize;

  private Socket sock;

  private BufferedOutputStream bos;

  private BufferedInputStream bis;

  private LinkedBlockingQueue<byte[]> receiveQueue;

  private int status;
  
  private java.util.Timer timer;
  
  private Reader reader;

  public ReliableTcpConnection(java.util.Timer timer) {    
    windowSize = Integer.getInteger(
      WINDOW_SIZE_PROP_NAME,
      DEFAULT_WINDOW_SIZE).intValue();
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, 
                 "ReliableTcpConnection.windowSize=" + windowSize);
    this.timer = timer;
    this.receiveQueue = new LinkedBlockingQueue<byte[]>();
    setStatus(INIT);
  }
  
  //JORAM_PERF_BRANCH
  public int size() {
    return receiveQueue.size();
  }

  private synchronized void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ReliableTcpConnection.setStatus(" + statusNames[status] + ')');
    this.status = status;
  }

  private final synchronized int getStatus() {
    return status;
  }

  public void init(Socket sock) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ReliableTcpConnection.init()");
    synchronized (this) {
      if (getStatus() != INIT) 
        throw new IOException("Already connected");
      setStatus(CONNECTING);
    }

    try {
      this.sock = sock;
      sock.setSendBufferSize(DEFAULT_TCP_SENDBUFFER_SIZE);
      sock.setReceiveBufferSize(DEFAULT_TCP_RECEIVEBUFFER_SIZE);
      sock.setTcpNoDelay(true);
      
      bos = new BufferedOutputStream(sock.getOutputStream(), DEFAULT_BUFFER_SIZE);
      bis = new BufferedInputStream(sock.getInputStream(), DEFAULT_BUFFER_SIZE);

      setStatus(CONNECT);

      FlushTask flushTask = new FlushTask();
      timer.schedule(flushTask, 50, 50);
      
      reader = new Reader();
      reader.start();
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }

  public void send(AbstractJmsMessage request) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.send(" + request + ')');

    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");
    try {      
      doSend(request);
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ReliableTcpConnection.send()", exc);
      close();
      throw exc;
    }
  }
  
  private void doSend(AbstractJmsMessage msg) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.doSend(" + msg + ')');
    // JORAM_PERF_BRANCH:
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    AbstractJmsMessage.write(msg, baos);
    byte[] bytes = baos.toByteArray();
    synchronized (bos) {
      StreamUtil.writeTo(bytes.length, bos);
      bos.write(bytes);
    }
  }
  
  public AbstractJmsReply receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.receive()");
    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");
    byte[] bytes = receiveQueue.take();
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      return (AbstractJmsReply) AbstractJmsMessage.read(bais);
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }

  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ReliableTcpConnection.close()");
    if (getStatus() == INIT) 
      return;
// Remove for SSL (bis.close() ==> lock)
//      try { 
//        if (bis != null) bis.close();
//      } catch (IOException exc) {}
    try { 
      sock.getOutputStream().close();
    } catch (IOException exc) {}
    try { 
      sock.close();
    } catch (IOException exc) {}
    reader.stop();
    setStatus(INIT);
  }

  static class TcpMessage {
    long id;
    AbstractJmsMessage object;

    TcpMessage(long id, AbstractJmsMessage object) {
      this.id = id;
      this.object = object;
    }

    public String toString() {
      return '(' + super.toString() + 
        ",id=" + id + 
        ",object=" + object + ')';
    }
  }
  
  //JORAM_PERF_BRANCH:
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
      }
    }
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
          byte[] bytes = StreamUtil.readFully(len, bis);
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
