/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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

  private long inputCounter;

  private long outputCounter;

  private int unackCounter;

  private Vector pendingMessages;

  private Socket sock;

  private ObjectOutputStream oos;

  private ObjectInputStream ois;

  private Object inputLock;

  private Object outputLock;

  private int status;

  public ReliableTcpConnection() {    
    windowSize = Integer.getInteger(
      WINDOW_SIZE_PROP_NAME,
      DEFAULT_WINDOW_SIZE).intValue();
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(
        BasicLevel.INFO, 
        "ReliableTcpConnection.windowSize=" + 
        windowSize);
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
        oos = new ObjectOutputStream(
          sock.getOutputStream());
        oos.flush();
        
        synchronized (pendingMessages) {
          for (int i = 0; i < pendingMessages.size(); i++) {
            TcpMessage pendingMsg = 
              (TcpMessage)pendingMessages.elementAt(i);
            doSend(pendingMsg.id, pendingMsg.object);
          }
        }
      }

      synchronized (inputLock) {
        ois = new ObjectInputStream(
          new BufferedInputStream(
            sock.getInputStream()));
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
        TcpMessage msg = new TcpMessage(
          outputCounter, request);
        doSend(outputCounter, request);
        addPendingMessage(msg);
        outputCounter++;
      }
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      close();
      throw exc;
    }
  }
  
  private void doSend(long id, Object obj) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "ReliableTcpConnection.doSend(" + 
        id + ',' + obj + ')');
    synchronized (outputLock) {
      oos.writeLong(id);
      oos.writeObject(obj);
      oos.reset();
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
        Object obj;
        synchronized (inputLock) {
          messageId = ois.readLong();
          obj = ois.readObject();
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(
            BasicLevel.DEBUG, 
            " -> id = " + messageId);
        if (obj == null) {
          ackPendingMessages(messageId);
          continue loop;
        } else {
          if (unackCounter < windowSize) {
            unackCounter++;
          } else {
            doSend(messageId, null);
            unackCounter = 0;
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
    try { 
      ois.close();
    } catch (IOException exc) {}
    try { 
      oos.close();
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
}
