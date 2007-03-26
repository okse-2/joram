/*
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class ReliableTcpConnection {

  public static final int INIT = 0;
  public static final int CONNECT = 1;
  public static final int CONNECTING = 2;

  public static final String[] statusNames =
  {"INIT", "CONNECT", "CONNECTING"};

  public static Logger logger = 
      Debug.getLogger("fr.dyade.aaa.util.ReliableTcpConnection");

  public static String WINDOW_SIZE_PROP_NAME = 
      "fr.dyade.aaa.util.ReliableTcpConnection.windowSize";

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

  public ReliableTcpConnection(java.util.Timer timer2) {    
    windowSize = Integer.getInteger(
      WINDOW_SIZE_PROP_NAME,
      DEFAULT_WINDOW_SIZE).intValue();
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(
        BasicLevel.INFO, 
        "ReliableTcpConnection.windowSize=" + 
        windowSize);
    timer = timer2;
    inputCounter = -1;
    outputCounter = 0;
    unackCounter = 0;
    pendingMessages = new Vector();
    inputLock = new Object();
    outputLock = new Object();
    
    setStatus(INIT);
  }

  private synchronized void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, 
        "ReliableTcpConnection.setStatus(" + 
        statusNames[status] + ')');
    this.status = status;
  }

  private final synchronized int getStatus() {
    return status;
  }

  public void init(Socket sock) 
    throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection.init()");
    synchronized (this) {
      if (getStatus() != INIT) 
        throw new IOException("Already connected");
      setStatus(CONNECTING);
    }

    try {
      this.sock = sock;

      synchronized (outputLock) {
        nos = new NetOutputStream(sock);
        
        synchronized (pendingMessages) {
          for (int i = 0; i < pendingMessages.size(); i++) {
            TcpMessage pendingMsg = 
              (TcpMessage)pendingMessages.elementAt(i);
            doSend(pendingMsg.id, inputCounter, pendingMsg.object);
          }
        }
      }

      synchronized (inputLock) {
        bis = new BufferedInputStream(sock.getInputStream());
      }

      setStatus(CONNECT);
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(
          BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }

  public void send(Object request) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection.send(" + 
        request + ')');

    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");
    try {      
      synchronized (outputLock) {        
        doSend(outputCounter, inputCounter, request);
        addPendingMessage(new TcpMessage(
          outputCounter, request));
        outputCounter++;
      }
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }
  
  private void doSend(long id, long ackId, Object obj) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpConnection.doSend(" + id + 
                 ',' + ackId + ',' + obj + ')');
    synchronized (outputLock) {
      nos.send(id, ackId, obj);
      unackCounter = 0;
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

  private void addPendingMessage(TcpMessage msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection." + 
        "addPendingMessage(" + 
        msg + ')');
    synchronized (pendingMessages) {
      pendingMessages.addElement(msg);
    }
  }

  private void ackPendingMessages(long ackId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection." + 
        "ackPendingMessages(" + 
        ackId + ')');
    synchronized (pendingMessages) {
      while (pendingMessages.size() > 0) {
        TcpMessage pendingMsg = 
          (TcpMessage)pendingMessages.elementAt(0);
        if (ackId < pendingMsg.id) {
          // It's an old acknowledge
          break;
        } else {
          pendingMessages.removeElementAt(0);
        }
      }
    }
  }

  public Object receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection." + 
        "receive()");
    if (getStatus() != CONNECT) 
      throw new IOException("Connection closed");
    loop:
    while (true) {
      try {
        long messageId;
        long ackId;
        Object obj;

        synchronized (inputLock) {
          ObjectInputStream ois = new ObjectInputStream(bis);
          messageId = ois.readLong();
          ackId = ois.readLong();
          obj = ois.readObject();
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(
            BasicLevel.DEBUG, 
            " -> id = " + messageId);
        ackPendingMessages(ackId);
        if (obj != null) {
          if (unackCounter < windowSize) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(
                BasicLevel.DEBUG, " -> unackCounter++");
            unackCounter++;
          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(
                BasicLevel.DEBUG, " -> schedule");
            AckTimerTask ackTimertask = new AckTimerTask();
            timer.schedule(ackTimertask, 0);
          }
          if (messageId > inputCounter) {
            inputCounter = messageId;
            return obj;
          } else if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(
              BasicLevel.DEBUG, " -> already received message: " + 
              messageId + " " + obj);
        }        
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
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection." + 
        "close()");
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
    Object object;

    TcpMessage(long id, Object object) {
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
        logger.log(
          BasicLevel.DEBUG, "AckTimerTask.run()");
      try {
        doSend(-1, inputCounter, null);
        cancel();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
  }
}
