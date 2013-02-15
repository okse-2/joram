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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

public class IOControl {
  public static Logger logger = Debug.getLogger(IOControl.class.getName());
  //JORAM_PERF_BRANCH
  //private long inputCounter;

  private Socket sock;

  private NetOutputStream nos;

  private BufferedInputStream bis;

  // JORAM_PERF_BRANCH:
  //private int windowSize;

  //private int unackCounter;
  
  //private long receivedCount;

  //private long sentCount;
  //JORAM_PERF_BRANCH.

  public IOControl(Socket sock) throws IOException {   
    // JORAM_PERF_BRANCH:
    //windowSize = AgentServer.getInteger("fr.dyade.aaa.util.ReliableTcpConnection.windowSize", 100).intValue();
    //unackCounter = 0;
    //JORAM_PERF_BRANCH.
    //this.inputCounter = inputCounter;
    this.sock = sock;

    nos = new NetOutputStream(sock);
    bis = new BufferedInputStream(sock.getInputStream());
    
    // JORAM_PERF_BRANCH
    FlushTask flushTask = new FlushTask();
    AgentServer.getTimer().schedule(flushTask, 50, 50);
  }

  public synchronized void send(ProxyMessage msg) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.send:" + msg);

    try {
      // JORAM_PERF_BRANCH
      nos.send(msg.getObject());
      /* JORAM_PERF_BRANCH
      sentCount++;
      unackCounter = 0;
      */
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "IOControl.send", exc);
      close();
      throw exc;
    }
  }

  static class NetOutputStream extends ByteArrayOutputStream {
    // JORAM_PERF_BRANCH
    private static final int MAX_BUFFER_SIZE = 8192;
    private OutputStream os = null;

    NetOutputStream(Socket sock) throws IOException {
      super(1024);
      reset();
      os = sock.getOutputStream();
    }

    public void reset() {
      // JORAM_PERF_BRANCH
      count = 0;
    }

    void send(AbstractJmsMessage msg) throws IOException {
        // JORAM_PERF_BRANCH
        //StreamUtil.writeTo(id, this);
        //StreamUtil.writeTo(ackId, this);
      AbstractJmsMessage.write(msg, this);
      if (size() > MAX_BUFFER_SIZE) {
        flushSocket();
      }
/* JORAM_PERF_BRANCH
        buf[0] = (byte) ((count -4) >>>  24);
        buf[1] = (byte) ((count -4) >>>  16);
        buf[2] = (byte) ((count -4) >>>  8);
        buf[3] = (byte) ((count -4) >>>  0);
*/
    }
    
    void flushSocket() throws IOException {
      if (size() > 0) {
        synchronized (this) {
          writeTo(os);
          reset();
        }
        os.flush();
      }
    }
  }
  
  //JORAM_PERF_BRANCH
  class FlushTask extends java.util.TimerTask {
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "FlushTask.run()");
      try {
        nos.flushSocket();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", exc);
      }
    }
  }
  
  public ProxyMessage receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IOControl.receive()");

    try {
      while (true) {
        // JORAM_PERF_BRANCH
        //int len = StreamUtil.readIntFrom(bis);
        //long messageId = StreamUtil.readLongFrom(bis);
        //long ackId = StreamUtil.readLongFrom(bis);
        AbstractJmsRequest obj = (AbstractJmsRequest) AbstractJmsMessage.read(bis);
        return new ProxyMessage(obj);
        
        /* JORAM_PERF_BRANCH
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
        }
        logger.log(BasicLevel.DEBUG, "IOControl.receive: already received message: " + messageId + " -> " + obj);
        JORAM_PERF_BRANCH */
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
  
  /* JORAM_PERF_BRANCH
  public long getSentCount() {
    return sentCount;
  }

  public long getReceivedCount() {
    return receivedCount;
  }
  */
}
