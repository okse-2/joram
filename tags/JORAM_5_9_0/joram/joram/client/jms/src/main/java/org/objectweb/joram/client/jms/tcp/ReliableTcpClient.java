/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.stream.MetaData;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Configuration;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.net.SocketFactory;
import fr.dyade.aaa.common.stream.StreamUtil;

public class ReliableTcpClient {
  public static Logger logger = Debug.getLogger(ReliableTcpClient.class.getName());

  public static final int INIT = 0;
  public static final int CONNECT = 1;
  public static final int CLOSE = 2;

  public static final String[] statusNames = {"INIT", "CONNECT", "CLOSE"};

  protected FactoryParameters params;

  protected Identity identity;

  protected int key;

  private ReliableTcpConnection connection;

  private volatile int status;

  private Vector addresses;
  /**
   *  True if the client must try to reconnect in case of connection
   * failure. It depends of cnxPendingTimer on a "normal" TCP connection.
   */
  private boolean reconnect;
  /**
   *  Time in ms during the client try to reconnect to the server. It depends
   * of connectingTimer and cnxPendingTimer from the connection parameters.
   */
  private int reconnectTimeout = 0;

  private Timer timer;

  /**
   *  Name of the property allowing to change the threshold of warning for the
   * verification of the synchronization between the client and server clock.
   *  A warning is generated if there is more than this value in milliseconds
   * between the two clocks.
   * <p>
   *  By default the value is 1000 milliseconds. 
   */
  public static final String CLOCK_SYNCHRO_THRESHOLD = "org.objectweb.joram.TcpConnection.ClockSynchro.Threshold";

  /**
   *  Value of the threshold of warning for the verification of the synchronization
   * between the client and server clock.
   *  A warning is generated if there is more than this value in milliseconds between
   * the two clocks.
   * <p>
   *  By default the value is 1000 milliseconds. 
   */
  private long clockSynchroThreshold = 1000L;

  public ReliableTcpClient() {
    this.clockSynchroThreshold = Configuration.getLong(CLOCK_SYNCHRO_THRESHOLD, clockSynchroThreshold).longValue();
  }

  public void setTimer(Timer timer2) {
    timer = timer2;
  }

  public void init(FactoryParameters params, 
                   Identity identity,
                   boolean reconnect) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient.init(" + params + ',' + identity + ',' + reconnect + ')');

    this.params = params;
    this.reconnect = reconnect;
    if (params.cnxPendingTimer > 0)
      this.reconnectTimeout = Math.max(2*params.cnxPendingTimer,
                                       (params.connectingTimer*1000)+params.cnxPendingTimer);
    addresses = new Vector();
    key = -1;
    this.identity = identity;

    setStatus(INIT);
  }

  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ReliableTcpClient[" + identity + ',' + key + "].setStatus(" + statusNames[status] + ')');
    this.status = status;
  }

  public void connect() throws JMSException {
    connect(false);
  }

  public synchronized void connect(boolean reconnect) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ReliableTcpClient[" + identity + ',' + key + "].connect(" + reconnect + ')');

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
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "ReliableTcpClient.connect", uhe); 
          IllegalStateException jmsExc = new IllegalStateException("Server's host is unknown: " + sa.hostName);
          jmsExc.setLinkedException(uhe);
          throw jmsExc;
        } catch (IOException ioe) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "ReliableTcpClient.connect", ioe); 
          // continue
        } catch (JMSException jmse) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "ReliableTcpClient.connect", jmse); 
          // continue
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "ReliableTcpClient.connect", e); 
          // continue
        }
      }
      long currentTime = System.currentTimeMillis();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   " -> currentTime = " + currentTime + ",endTime = " + endTime);

      // Keep on trying as long as timer is ok:
      if (currentTime < endTime) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     " -> retry connection " + identity + ',' + key);

        if (currentTime + nextSleep > endTime) {
          nextSleep = endTime - currentTime;    
        }      

        // Sleeping for a while:
        try {
          wait(nextSleep);
        } catch (InterruptedException intExc) {
          throw new IllegalStateException("Could not open the connection with " + addresses + ": interrupted");
        }          

        // Trying again!
        nextSleep = nextSleep * 2;
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     " -> close connection " + identity + ',' + key);

        // If timer is over, throwing an IllegalStateException:
        long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
        IllegalStateException jmsExc = new IllegalStateException("Could not connect to JMS server with "
            + addresses + " after " + attemptsC + " attempts during " + attemptsT
            + " secs: server is not listening or server protocol version is not compatible with client.");
        throw jmsExc;
      }
    }
  }

  protected Socket createSocket(String hostname, int port) throws Exception {
    InetAddress addr = InetAddress.getByName(hostname);

    InetAddress outLocalAddr = null;
    String outLocalAddrStr = params.outLocalAddress;
    if (outLocalAddrStr != null)
      outLocalAddr = InetAddress.getByName(outLocalAddrStr);

    int outLocalPort = params.outLocalPort;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient[" + identity + ',' + key + "].createSocket(" +
                 hostname + "," + port + ") on interface " + outLocalAddrStr + ":" + outLocalPort);

    SocketFactory factory = SocketFactory.getFactory(params.socketFactory);
    Socket socket = factory.createSocket(addr, port,
                                         outLocalAddr, outLocalPort,
                                         params.connectingTimer *1000);

    return socket;
  }

  private void doConnect(String hostname, int port) throws Exception, JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient[" + identity + ',' + key + "].doConnect(" + hostname + "," + port + ")");
    Socket socket = createSocket(hostname, port);

    socket.setTcpNoDelay(params.TcpNoDelay);
    socket.setSoTimeout(params.SoTimeout);
    if (params.SoLinger >= 0)
      socket.setSoLinger(true, params.SoLinger);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    OutputStream os = socket.getOutputStream();
    InputStream is = socket.getInputStream();

    // Writes the Joram magic number
    baos.write(MetaData.joramMagic);
    
    // Writes the ack mode (noAckedQueue)
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> write noAckedQueue = " + params.noAckedQueue);
    StreamUtil.writeTo(params.noAckedQueue, baos);
    
    // Writes the current date
    StreamUtil.writeTo(System.currentTimeMillis(), baos);

    // Writes the user identity
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> write identity = " + identity);
    Identity.write(identity, baos);

    // Writes the key
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> write key = " + key);
    StreamUtil.writeTo(key, baos);

    if (key == -1) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> connection opened, initializes new connection");
      StreamUtil.writeTo(reconnectTimeout, baos);
      baos.writeTo(os);
      os.flush();

      int len = StreamUtil.readIntFrom(is);
      long dt = StreamUtil.readLongFrom(is);
      if (dt > clockSynchroThreshold)
        logger.log(BasicLevel.WARN, " -> bad clock synchronization between client and server: " + dt);

      int res = StreamUtil.readIntFrom(is);
      if (res > 0) {
        String info = StreamUtil.readStringFrom(is);
        throwSecurityError(info);
      }

      key = StreamUtil.readIntFrom(is);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> key = " + identity.getUserName() + ',' + key);
      connection = new ReliableTcpConnection(timer, params.noAckedQueue);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> init reliable connection");
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> reinitializes connection " + identity + ',' + key);
      baos.writeTo(os);
      os.flush();

      int len = StreamUtil.readIntFrom(is);
      int res = StreamUtil.readIntFrom(is);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> read res = " + res);
      if (res > 0) {
        String info = StreamUtil.readStringFrom(is);
        throwSecurityError(info);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> reset reliable connection");
    }

    connection.init(socket);
  }

  private void throwSecurityError(String info) 
  throws JMSSecurityException {
    JMSSecurityException jmsExc = 
      new JMSSecurityException("Can't open the connection with the server " +
                               params.getHost() + " on port " +
                               params.getPort() + ": " + info);
    throw jmsExc;
  }

  public void send(AbstractJmsMessage request) 
  throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log( BasicLevel.DEBUG, 
                  "ReliableTcpClient[" + identity + ',' + key + "].send(" + request + ')');
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
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "ReliableTcpClient[" + identity + ',' + key + "]", exc);
        if (reconnect) {
          waitForReconnection();
        } else {
          close();
          throw exc;
        }
      }
    }
  }

  public Object receive() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient[" + identity + ',' + key + "].receive()");
    while (true) {
      try {        
        return connection.receive();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "ReliableTcpClient[" + identity + ',' + key + "]", exc);
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient[" + identity + ',' + key + "].waitForReconnection()");
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableTcpClient[" + identity + ',' + key + "].reconnect()");
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ReliableTcpClient[" + identity + ',' + key + "].close()");
    if (status != CLOSE) {
      setStatus(CLOSE);
      connection.close();
      this.identity = null;
    }
  }

  public void addServerAddress(String host, int port) {
    addresses.addElement(new ServerAddress(host, port));
  }

  public String toString() {
    return '(' + super.toString() + ",params=" + params + ",name=" + identity + 
    ",key=" + key + ",connection=" + connection + 
    ",status=" + statusNames[status] + ",addresses=" + addresses + ')';
  }

  static class ServerAddress {
    String hostName;
    int port;

    public ServerAddress(String hostName, int port) {
      this.hostName = hostName;
      this.port = port;
    }

    public String toString() {
      return "(hostName=" + hostName + ",port=" + port + ')';
    }
  }

  public void stopReconnections() {
    reconnect = false;
  }
}
