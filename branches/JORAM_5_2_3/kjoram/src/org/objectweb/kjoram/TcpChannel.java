/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class TcpChannel extends Channel {
  
  public static Logger logger = Debug.getLogger(TcpChannel.class.getName());
  
  class TcpMessage {
    long id;
    AbstractRequest msg;
  
    TcpMessage(long id, AbstractRequest msg) {
      this.id = id;
      this.msg = msg;
    }
  }

  private String user;
  private String pass;
  private String host;
  private int port;

  //Socket socket;
  InputStream sockin;
  OutputStream sockout;
  OutputXStream out;
  InputXStream in;

  // definition from ReliableTcpConnection
  int windowSize;
  long inputCounter;
  long outputCounter;
  int unackCounter;
  Vector pendingMessages;

  /**
   * @param user
   * @param pass
   * @param host
   * @param port
   */
  public TcpChannel(String user, String pass, String host, int port) {
    this.user = user;
    this.pass = pass;
    this.host = host;
    this.port = port;

    windowSize = 10;
    inputCounter = -1;
    outputCounter = 0;
    unackCounter = 0;
    pendingMessages = new Vector();

    key = -1;
    status = INIT;

    sockout = null;
    sockin = null;
    
    out = null;
    in = null;
  }

  public final static byte[] magic = {'J', 'O', 'R', 'A', 'M', 5, 2, 52};

  /* (non-Javadoc)
   * @see org.objectweb.kjoram.Channel#connect()
   */
  public void connect() throws IOException {
    if (status != INIT) throw new IOException("Bad status");
    status = CONNECTING;

    try {
      TcpSocket tcpSocket = (TcpSocket) Class.forName("org.objectweb.kjoram.MidpSocket").newInstance();
      tcpSocket.connect(host, port);
      sockout = tcpSocket.getOutputStream();
      sockin =  tcpSocket.getInputStream();
    } catch (InstantiationException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"EXCEPTION:: connect()", e);
      throw new IOException("InstantiationException:: TcpChannel connect()" + e.getMessage());
    } catch (IllegalAccessException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"EXCEPTION:: connect()", e);
      throw new IOException("IllegalAccessException:: TcpChannel connect()" + e.getMessage());
    } catch (ClassNotFoundException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"EXCEPTION:: connect()", e);
      throw new IOException("ClassNotFoundException:: TcpChannel connect()" + e.getMessage());
    }
  
    out = new OutputXStream();
    in = new InputXStream();

    // Writes the Joram magic number
    out.writeBuffer(magic);
    
    // Writes the user identity
    out.writeString("");
    out.writeString(user);
    out.writeString(pass);
    out.writeInt(key);

    if (key == -1) {
      // Open new connection
      out.writeInt(/* reconnectTimeout */ 0);

      out.writeTo(sockout);
      in.readFrom(sockin);

      int res = in.readInt();
      if (res > 0) {
        String info = in.readString();
        throw new IOException(info);
      }
      key = in.readInt();
    } else {
      // Reopen the connection
      out.writeTo(sockout);
      in.readFrom(sockin);

      int res = in.readInt();
      if (res > 0) {
        String info = in.readString();
        throw new IOException(info);
      }
    }

    TcpMessage pendingMsg = null;
    for (int i = 0; i < pendingMessages.size(); i++) {
      pendingMsg = (TcpMessage) pendingMessages.elementAt(i);
      doSend(pendingMsg.id, inputCounter, pendingMsg.msg);
    }

    status = CONNECTED;
  }

  /**
   * Sending a request through the TCP connection.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  public void send(AbstractRequest request) throws IOException {
    if (status != CONNECTED) throw new IOException("Connection closed");

    try {      
      doSend(outputCounter, inputCounter, request);
      pendingMessages.addElement(new TcpMessage(outputCounter, request));
      outputCounter++;
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"EXCEPTION:: send("+request+')', exc);
      close();
      throw exc;
    }
  }

  /**
   * @param id
   * @param counter
   * @param request
   * @throws IOException
   */
  void doSend(long id, long counter, AbstractRequest request) throws IOException {
    if (status != CONNECTED) throw new IOException("Bad status");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpChannel doSend(" + id + ',' + counter + ',' + request + ')');
    
    out.writeLong(id);
    out.writeLong(counter);
    AbstractMessage.write(request, out);
    out.writeTo(sockout);
  }

  /* (non-Javadoc)
   * @see org.objectweb.kjoram.Channel#receive()
   */
  public AbstractReply receive() throws Exception {
    if (status != CONNECTED) throw new IOException("Bad status");

    while (true) {
      in.readFrom(sockin);

      long msgid, ackid;

      msgid = in.readLong();
      ackid = in.readLong();
      AbstractReply reply = (AbstractReply) AbstractMessage.read(in);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "TcpChannel receive() reply = " + reply);

      while (pendingMessages.size() > 0) {
        TcpMessage pendingMsg = (TcpMessage) pendingMessages.elementAt(0);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "TcpChannel receive() pendingMsg = " + pendingMsg);
        if (ackid < pendingMsg.id) {
          // It's an old acknowledge
          break;
        } else {
          pendingMessages.removeElementAt(0);
        }
      }

      if (reply != null) {
        if (unackCounter < windowSize) {
          unackCounter++;
        } else {
          // TODO
/*           AckTimerTask ackTimertask = new AckTimerTask(); */
/*           timer.schedule(ackTimertask, 0); */
        }
        if (msgid > inputCounter) {
          inputCounter = msgid;
          return reply;
        }
      }
    }
  }

  /** Closes the TCP connection. */
  public void close() throws IOException {
    sockin.close();
    sockout.close();
  }
}
