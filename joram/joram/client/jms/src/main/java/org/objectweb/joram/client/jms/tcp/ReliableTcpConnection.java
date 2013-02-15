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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

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

  private int windowSize;

  // JORAM_PERF_BRANCH
  //private volatile long inputCounter;

  //private long outputCounter;

  //private volatile int unackCounter;

  //private Vector pendingMessages;

  private Socket sock;

  private NetOutputStream nos;

  private BufferedInputStream bis;

  //private Object inputLock;

  //private Object outputLock;

  private int status;
  
  private java.util.Timer timer;

  public ReliableTcpConnection(java.util.Timer timer) {    
    windowSize = Integer.getInteger(
      WINDOW_SIZE_PROP_NAME,
      DEFAULT_WINDOW_SIZE).intValue();
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, 
                 "ReliableTcpConnection.windowSize=" + windowSize);
    this.timer = timer;
    // JORAM_PERF_BRANCH:
    //inputCounter = -1;
    //outputCounter = 0;
    //unackCounter = 0;
    //pendingMessages = new Vector();
    //inputLock = new Object();
    //outputLock = new Object();
    setStatus(INIT);
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
      nos = new NetOutputStream(sock);
      
      // JORAM_PERF_BRANCH
      /*
        synchronized (pendingMessages) {
          for (int i = 0; i < pendingMessages.size(); i++) {
            TcpMessage pendingMsg = (TcpMessage) pendingMessages.elementAt(i);
            // JORAM_PERF_BRANCH:
            doSend(pendingMsg.object);
          }
        }
      */

      //synchronized (inputLock) {
      bis = new BufferedInputStream(sock.getInputStream());
      //}

      setStatus(CONNECT);

      FlushTask flushTask = new FlushTask();
      timer.schedule(flushTask, 50, 50);
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
        // JORAM_PERF_BRANCH:
        doSend(request);
        /* JORAM_PERF_BRANCH:
        addPendingMessage(new TcpMessage(
          outputCounter, request));
        outputCounter++;
          JORAM_PERF_BRANCH.*/
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
    nos.send(msg);
    //unackCounter = 0;
  }

  static class NetOutputStream extends ByteArrayOutputStream {
    private static final int MAX_BUFFER_SIZE = 8192;
    
    private OutputStream os = null;

    NetOutputStream(Socket sock) throws IOException {
      super(1024);
      reset();
      os = sock.getOutputStream();
    }

    public void reset() {
      count = 0;
    }

    synchronized void send(AbstractJmsMessage msg) throws IOException {
        // JORAM_PERF_BRANCH:
        //StreamUtil.writeTo(id, this);
        //StreamUtil.writeTo(ackId, this);
      AbstractJmsMessage.write(msg, this);
      if (size() > MAX_BUFFER_SIZE) {
        flushSocket();
      }
/* JORAM_PERF_BRANCH:
        buf[0] = (byte) ((count -4) >>>  24);
        buf[1] = (byte) ((count -4) >>>  16);
        buf[2] = (byte) ((count -4) >>>  8);
        buf[3] = (byte) ((count -4) >>>  0);
*/
    }
    
    // JORAM_PERF_BRANCH:
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
  
  /* JORAM_PERF_BRANCH:
  private void addPendingMessage(TcpMessage msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.addPendingMessage(" + msg + ')');
    synchronized (pendingMessages) {
      pendingMessages.addElement(msg);
    }
  }
  JORAM_PERF_BRANCH.*/
  /* JORAM_PERF_BRANCH:
  private void ackPendingMessages(long ackId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.ackPendingMessages(" + ackId + ')');
    
    synchronized (pendingMessages) {
      while (pendingMessages.size() > 0) {
        TcpMessage pendingMsg = (TcpMessage)pendingMessages.elementAt(0);
        if (ackId < pendingMsg.id) {
          // It's an old acknowledge
          break;
        }
        
        pendingMessages.removeElementAt(0);
      }
    }
  }
*/
  public AbstractJmsReply receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.receive()");
    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");

    while (true) {
      try {
        //long messageId;
        //long ackId;
        AbstractJmsReply obj;

        // JORAM_PERF_BRANCH:
        //synchronized (inputLock) {
          
          //int len = StreamUtil.readIntFrom(bis);
          //messageId = StreamUtil.readLongFrom(bis);
          //ackId = StreamUtil.readLongFrom(bis);
        obj =  (AbstractJmsReply) AbstractJmsMessage.read(bis);
          // JORAM_PERF_BRANCH:
        return obj;
          //JORAM_PERF_BRANCH.

        /* JORAM_PERF_BRANCH:
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> id = " + messageId);
        ackPendingMessages(ackId);
        if (obj != null) {
          if (unackCounter < windowSize) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> unackCounter++");
            unackCounter++;
          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> schedule");
            AckTimerTask ackTimertask = new AckTimerTask();
            timer.schedule(ackTimertask, 0);
          }
          if (messageId > inputCounter) {
            inputCounter = messageId;
            return obj;
          } else if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,
                       " -> already received message: " + messageId + " " + obj);
        }
        JORAM_PERF_BRANCH.*/       
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        close();
        throw exc;
      }
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
  
  /* JORAM_PERF_BRANCH:
  class AckTimerTask extends java.util.TimerTask {
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "AckTimerTask.run()");
      try {
        doSend(-1, inputCounter, null);
        cancel();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
  }
  JORAM_PERF_BRANCH.*/
  
  //JORAM_PERF_BRANCH:
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
  
}
