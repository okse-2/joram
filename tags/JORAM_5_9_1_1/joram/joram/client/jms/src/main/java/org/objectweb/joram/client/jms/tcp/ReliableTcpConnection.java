/*
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
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

  private int windowSize;

  private volatile long inputCounter;

  private long outputCounter;

  private volatile int unackCounter;

  private Vector pendingMessages;

  private Socket sock;

  private NetOutputStream nos;

  private BufferedInputStream bis;

  private Object inputLock;

  private Object outputLock;

  private int status;
  
  private java.util.Timer timer;
  
  private boolean noAckedQueue = false;
  
  private LinkedBlockingQueue<byte[]> receiveQueue;
  
  private Reader reader;

  public ReliableTcpConnection(java.util.Timer timer2, boolean noAckedQueue) {    
    windowSize = Integer.getInteger(
      WINDOW_SIZE_PROP_NAME,
      DEFAULT_WINDOW_SIZE).intValue();
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "ReliableTcpConnection.windowSize=" + windowSize + ", noAckedQueue=" + noAckedQueue);
    timer = timer2;
    inputCounter = -1;
    outputCounter = 0;
    unackCounter = 0;
    pendingMessages = new Vector();
    inputLock = new Object();
    outputLock = new Object();
    this.noAckedQueue = noAckedQueue;
    this.receiveQueue = new LinkedBlockingQueue<byte[]>();
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
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "ReliableTcpConnection.init()");
    synchronized (this) {
      if (getStatus() != INIT) 
        throw new IOException("Already connected");
      setStatus(CONNECTING);
    }

    try {
      this.sock = sock;

      synchronized (outputLock) {
        nos = new NetOutputStream(sock);
        
        if (!noAckedQueue) {
          synchronized (pendingMessages) {
            for (int i = 0; i < pendingMessages.size(); i++) {
              TcpMessage pendingMsg = (TcpMessage) pendingMessages.elementAt(i);
              doSend(pendingMsg.id, inputCounter, pendingMsg.object);
            }
          }
        }
      }

      synchronized (inputLock) {
        bis = new BufferedInputStream(sock.getInputStream());
      }
      
      reader = new Reader();
      reader.start();

      setStatus(CONNECT);
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, exc);
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
      synchronized (outputLock) {        
        doSend(outputCounter, inputCounter, request);
        if (!noAckedQueue) {
          addPendingMessage(new TcpMessage(
              outputCounter, request));
        }
        outputCounter++;
      }
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ReliableTcpConnection.send()", exc);
      close();
      throw exc;
    }
  }
  
  private void doSend(long id, long ackId, AbstractJmsMessage msg) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.doSend(" + id + ',' + ackId + ',' + msg + ')');
    synchronized (outputLock) {
      nos.send(id, ackId, msg);
      unackCounter = 0;
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

  private void addPendingMessage(TcpMessage msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.addPendingMessage(" + msg + ')');
    if (!noAckedQueue) {
      synchronized (pendingMessages) {
        pendingMessages.addElement(msg);
      }
    }
  }

  private void ackPendingMessages(long ackId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.ackPendingMessages(" + ackId + ')');
    
    if (!noAckedQueue) {
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
  }

  public AbstractJmsReply receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.receive()");
    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");

    while (true) {
      try {
        byte[] bytes = receiveQueue.take();
        if (bytes.length == 0) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "The reader offer a closing marker, so closed.");
          throw new IOException("Connection closed.");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        long messageId;
        long ackId;
        AbstractJmsReply obj;

        messageId = StreamUtil.readLongFrom(bais);
        ackId = StreamUtil.readLongFrom(bais);
        obj =  (AbstractJmsReply) AbstractJmsMessage.read(bais);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> id = " + messageId);
        if (!noAckedQueue) {
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
        } else {
          if (obj != null) {
            return obj;
          }
        }
      } catch (InterruptedException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, exc);
        close();
        throw new IllegalStateException("Interrupted receive: Connection closed.");
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, exc);
        close();
        throw exc;
      }
    }
  }

  public boolean isReaderRun() {
    return reader.isRunning();
  }
  
  public void close() {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "ReliableTcpConnection.close()");
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
    if (reader != null) {
      reader.stop();
    }
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
  
  
  class Reader extends fr.dyade.aaa.common.Daemon {
    
    Reader() {
      super("ReliableTcpConnection.Reader", logger);
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
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Reader stop()");
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
