/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): David Feliot (ScalAgent DT)
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.tcp;

import fr.dyade.aaa.util.*;

import java.io.*;
import java.net.*;

import javax.jms.*;
import javax.jms.IllegalStateException;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

public class ReliableTcpClient {

  public static final int INIT = 0;
  public static final int CONNECTING = 1;
  public static final int CONNECT = 2;
  public static final int CLOSE = 3;
  public static final int ERROR = 4;

  public static final String[] statusNames =
  {"INIT", "CONNECTING", "CONNECT", "CLOSE", "ERROR"};

  private FactoryParameters params;

  private String name;
  
  private String password;

  private int key;

  private ReliableTcpConnection connection;

  private int status;

  private JMSException error;

  public ReliableTcpClient(
    FactoryParameters params, 
    String name,
    String password) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient.<init>()");
    this.params = params;
    this.name = name;
    this.password = password;
    key = -1;
    setStatus(INIT);
  }

  private void setStatus(int status) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + 
        "].setStatus(" +
        statusNames[status] + ')');
    this.status = status;
  }

  public void connect() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + "].connect()");
    
    synchronized (this) {
      while (status == CONNECTING) {
        try {
          wait();
        } catch (InterruptedException exc) {}
      }
      switch (status) {
      case CONNECT:
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> already connected");
        return;
      case ERROR:
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> error = " + error);
        throw error;
      case INIT:
      case CLOSE:
        setStatus(CONNECTING);
        break;
      }
    }

    // Setting the timer values:
    long startTime = System.currentTimeMillis();
    long endTime = startTime + params.connectingTimer * 1000;
    long currentTime;
    long nextSleep = 2000;
    int attemptsC = 0;

    while (true) {
      attemptsC++;
      try {
        doConnect();

        synchronized (this) {
          setStatus(CONNECT);
          notifyAll();
        }

        return;
      } catch (UnknownHostException uhe) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "ReliableTcpClient[" + name + ',' + key + "]", uhe);

        IllegalStateException jmsExc =
          new IllegalStateException(
            "Server's host is unknown: " + 
            params.getHost());
        jmsExc.setLinkedException(uhe);
        throw jmsExc;
      } catch (IOException ioe) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "ReliableTcpClient[" + name + ',' + key + "]", ioe);

        // IOExceptions notify that the connection could not be opened,
        // possibly because the server is not listening: trying again.
        currentTime = System.currentTimeMillis();

        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> currentTime = " + currentTime + 
            ",endTime = " + endTime);

        // Keep on trying as long as timer is ok:
        if (currentTime < endTime) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(
              BasicLevel.DEBUG, " -> retry connection " + name + ',' + key);

          if (currentTime + nextSleep > endTime) {
            nextSleep = endTime - currentTime;    
          }      

          // Sleeping for a while:
          try {
            Thread.sleep(nextSleep);
          }
          catch (InterruptedException intExc) {}          

          // Trying again!
          nextSleep = nextSleep * 2;
        } else {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(
              BasicLevel.DEBUG, " -> close connection " + name + ',' + key);

          // If timer is over, throwing an IllegalStateException:
          long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
          IllegalStateException jmsExc =
            new IllegalStateException("Could not open the connection"
                                      + " with server "
                                      + params.getHost()
                                      + " on port " + params.getPort()
                                      + " after " + attemptsC
                                      + " attempts during "
                                      + attemptsT + " secs: server is"
                                      + " not listening" );
          jmsExc.setLinkedException(ioe);
          
          synchronized (this) {
            setStatus(ERROR);
            error = jmsExc;
            notifyAll();
          }

          throw jmsExc;
        }
      }
    }
  }

  private void doConnect() 
    throws IOException, JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "ReliableTcpClient[" + 
        name + ',' + key + "].doConnect()");

    Socket socket = new Socket(
      params.getHost(),
      params.getPort());
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(0);
    socket.setSoLinger(true, 1000);
    
    DataOutputStream dos = 
      new DataOutputStream(socket.getOutputStream());
    DataInputStream dis = 
      new DataInputStream(socket.getInputStream());
    
    dos.writeUTF(name);
    dos.writeInt(key);
    
    if (key == -1) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, " -> open new connection");
      dos.writeUTF(password);
      dos.writeInt(params.cnxPendingTimer);
      int res = dis.readInt();
      if (res > 0) {
        String info = dis.readUTF();
        throwSecurityError(info);
      } else {
        key = dis.readInt();
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> key = " + name + ',' + key);
        connection = 
          new ReliableTcpConnection();
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> init reliable connection");
        connection.init(socket);
      }
    } else {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, " -> reopen connection " + name + ',' + key);
      int res = dis.readInt();
      if (res > 0) {
        String info = dis.readUTF();
        throwSecurityError(info);
      } else {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, " -> reset reliable connection");
        
        connection.init(socket);
      }
    }
  }

  private void throwSecurityError(String info) 
    throws JMSSecurityException {
    JMSSecurityException jmsExc = 
      new JMSSecurityException(
        "Can't open the connection with the"
        + " server " + params.getHost()
        + " on port " + params.getPort()
        + ": " + info);
    synchronized (this) {
      setStatus(ERROR);
      error = jmsExc;
      notifyAll();
    }
    throw jmsExc;
  }

  public void send(Object request) 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + 
        "].send(" + request + ')');
    while (true) {
      try {
        connection.send(request);
        return;
      } catch (IOException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "ReliableTcpClient[" + 
            name + ',' + key + "]", exc);
        reconnect();
      }
    }
  }

  public Object receive() 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + "].receive()");
    while (true) {
      try {        
        return connection.receive();
      } catch (IOException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "ReliableTcpClient[" + 
            name + ',' + key + "]", exc);
        reconnect();
      }
    }
  }

  private void reconnect() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + "].reconnect()");
    synchronized (this) {
      if (status == CONNECT)
        setStatus(CLOSE);
    }
    try {
      Thread.sleep(2000);
    } catch (InterruptedException intExc) {}
    connect();
  }

  public void close() {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + "].close()");
    setStatus(CLOSE);
    connection.close();
  }
}
