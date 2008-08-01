/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import fr.dyade.aaa.util.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

import javax.jms.*;
import javax.jms.IllegalStateException;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

public class ReliableTcpClient {

  public static final int INIT = 0;
  public static final int CONNECT = 1;
  public static final int CLOSE = 2;

  public static final String[] statusNames =
  {"INIT", "CONNECT", "CLOSE"};

  private FactoryParameters params;

  private String name;
  
  private String password;

  private int key;

  private ReliableTcpConnection connection;

  private volatile int status;

  private Vector addresses;
  /**
   *  True if the client must try to reconnect in case of connection
   * failure. It depends of cnxPendingTimer on a "normal" TCP connection,
   * always true on HA.
   */
  private boolean reconnect;
  /**
   *  Time in ms during the client try to reconnect to the server. It depends
   * of connectingTimer and cnxPendingTimer from the connection parameters.
   */
  private int reconnectTimeout = 0;
  
  private Timer timer;

  public ReliableTcpClient() {}
  
  public void setTimer(Timer timer2) {
    timer = timer2;
  }

  public void init(FactoryParameters params, 
                   String name,
                   String password,
                   boolean reconnect) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient.init(" + 
        params + ',' + name + ',' + password + ',' + reconnect + ')');
    this.params = params;
    this.name = name;
    this.password = password;
    this.reconnect = reconnect;
    if (params.cnxPendingTimer > 0)
      this.reconnectTimeout =
        Math.max(2*params.cnxPendingTimer,
                 (params.connectingTimer*1000)+params.cnxPendingTimer);
    addresses = new Vector();
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
    connect(false);
  }

  public synchronized void connect(boolean reconnect) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + 
        "].connect(" + reconnect + ')');
    
    if (status != INIT) 
      throw new IllegalStateException("Connect: state error");

    long startTime = System.currentTimeMillis();
    long endTime = startTime;
    if (addresses.size() > 1) {
      // infinite retry in case of HA.
      endTime = Long.MAX_VALUE;
    } else {
      if (reconnect) {
        endTime += reconnectTimeout;
      } else {
        endTime += params.connectingTimer * 1000L;
      }
    }

    int attemptsC = 0;
    long nextSleep = 100;
    while (true) {
      if (status == CLOSE) 
        throw new IllegalStateException("Closed connection");
      attemptsC++;
      for (int i = 0; i < addresses.size(); i++) {
        ServerAddress sa = (ServerAddress)addresses.elementAt(i);
        try {
          doConnect(sa.hostName, sa.port);
          setStatus(CONNECT);
          return;
        } catch (JMSSecurityException exc) {
          throw exc;
        } catch (UnknownHostException uhe) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", uhe); 
          IllegalStateException jmsExc =
            new IllegalStateException(
              "Server's host is unknown: " + 
              sa.hostName);
          jmsExc.setLinkedException(uhe);
          throw jmsExc;
        } catch (IOException ioe) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", ioe); 
          // continue
        } catch (JMSException jmse) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", jmse); 
          // continue
        } catch (Exception e) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", e); 
          // continue
        }
      }
      long currentTime = System.currentTimeMillis();

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
          wait(nextSleep);
        } catch (InterruptedException intExc) {
          IllegalStateException jmsExc =
            new IllegalStateException("Could not open the connection"
                                      + " with "
                                      + addresses + ": interrupted");
        }          

        // Trying again!
        nextSleep = nextSleep * 2;
      } else {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(
              BasicLevel.DEBUG, " -> close connection " + name + ',' + key);

          // If timer is over, throwing an IllegalStateException:
          long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
          IllegalStateException jmsExc =
            new IllegalStateException("Could not connect to JMS server"
                                      + " with "
                                      + addresses
                                      + " after " + attemptsC
                                      + " attempts during "
                                      + attemptsT + " secs: server is"
                                      + " not listening" );
          throw jmsExc;
      }
    }
  }

  protected Socket createSocket(String hostName, int port) 
    throws Exception {
    return new Socket(hostName, port);
  }

  private void doConnect(String hostName, int port) 
    throws Exception, JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "ReliableTcpClient[" + 
        name + ',' + key + "].doConnect(" + 
	hostName + ',' + port + ')');

    Socket socket = createSocket(hostName, port);    
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(0);
    socket.setSoLinger(true, 1000);
    
    DataOutputStream dos = 
      new DataOutputStream(socket.getOutputStream());
    DataInputStream dis = 
      new DataInputStream(socket.getInputStream());
    
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
	JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, 
          " -> write name = " + name);
    dos.writeUTF(name);
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
	JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, 
          " -> write password = " + password);
    dos.writeUTF(password);
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
	JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, 
          " -> write key = " + key);
    dos.writeInt(key);
    
    if (key == -1) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, " -> open new connection");      
      dos.writeInt(reconnectTimeout);
      dos.flush();

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
          new ReliableTcpConnection(timer);
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
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
	  BasicLevel.DEBUG, " -> read res = " + res);
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
    throw jmsExc;
  }

  public void send(Object request) 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + 
        "].send(" + request + ')');
    if (status == CLOSE) throw new IOException("Closed connection");
    if (status != CONNECT) {
      if (reconnect) waitForReconnection();
      else throw new IOException("Closed connection");
    }
    while (true) {
      try {
        connection.send(request);
        return;
      } catch (IOException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "ReliableTcpClient[" + 
            name + ',' + key + "]", exc);
        if (reconnect) {
          waitForReconnection();
        } else {
          close();
          throw exc;
        }
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
        if (reconnect) {
          reconnect();
        } else {
          close();
          throw exc;
        }
      }
    }
  }

  private synchronized void waitForReconnection() throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "ReliableTcpClient[" + name
          + ',' + key + "].waitForReconnection()");
    while (status == INIT) {
      try {
        wait();
      } catch (InterruptedException exc) {
        //continue
      }
    }
    switch (status) {
    case CONNECT:
      break;
    case CLOSE:
      throw new Exception("Connection closed");
    }
  }

  private synchronized void reconnect() throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "ReliableTcpClient[" + name
          + ',' + key + "].reconnect()");
    switch (status) {
    case CONNECT:
      setStatus(INIT);
    case INIT:
      try {
        connect(true);
      } catch (JMSException exc) {
        close();
        throw exc;
      } finally {
        notifyAll();
      }
      break;
    case CLOSE:
      throw new Exception("Connection closed");
    default:
      throw new Error("State error");
    }
  }

  public synchronized void close() {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableTcpClient[" + name + ',' + key + "].close()");
    if (status != CLOSE) {
      setStatus(CLOSE);
      connection.close();
    }
  }

  public void addServerAddress(String host, int port) {
    addresses.addElement(new ServerAddress(host, port));
  }

  public String toString() {
    return '(' + super.toString() + 
      ",params=" + params + 
      ",name=" + name + 
      ",password=" + password + 
      ",key=" + key + 
      ",connection=" + connection + 
      ",status=" + statusNames[status] + 
      ",addresses=" + addresses + ')';
  }

  static class ServerAddress {
    String hostName;
    int port;

    public ServerAddress(String hostName, int port) {
      this.hostName = hostName;
      this.port = port;
    }

    public String toString() {
      return "(hostName=" + hostName + 
        ",port=" + port + ')';
    }
  }
}
