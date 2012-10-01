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
 * Contributor(s): 
 */
package org.objectweb.joram.mom.proxies.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.GetConnectionNot;
import org.objectweb.joram.mom.proxies.OpenConnectionNot;
import org.objectweb.joram.mom.proxies.ReliableConnectionContext;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.stream.MetaData;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.Configuration;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;



/**
 * Listens to the TCP connections from the JMS clients.
 * Creates a <code>TcpConnection</code> for each
 * accepted TCP connection.
 * Opens the <code>UserConnection</code> with the
 * right user's proxy.
 */
public class TcpConnectionListener extends Daemon {
  /** logger */
  public static Logger logger = Debug.getLogger(TcpConnectionListener.class.getName());

  /**
   * The TCP proxy service 
   */
  private TcpProxyService proxyService;

  private int timeout;

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

  /**
   * Number of times this connection listener has encountered an erroneous
   * authentication.
   */
  private int failedLoginCount;

  /**
   * Number of connections started with this connection listener.
   */
  private int connectionCount;

  /**
   * Number of times this connection listener has encountered an erroneous magic
   * number or protocol version.
   */
  private int protocolErrorCount;

  /**
   * Creates a new connection listener
   *
   * @param proxyService  the TCP proxy service associated with this connection listener
   * @param timeout       the timeout
   */
  public TcpConnectionListener(TcpProxyService proxyService, int timeout) {
    super("TcpConnectionListener");
    this.proxyService = proxyService;
    this.timeout = timeout;
    this.clockSynchroThreshold = Configuration.getLong(CLOCK_SYNCHRO_THRESHOLD, clockSynchroThreshold).longValue();
  }

  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpConnectionListener.run()");

    // Wait for the administration topic deployment.
    // TODO (AF): a synchronization would be much better.
    try {
      Thread.sleep(500);
    } catch (InterruptedException exc) {
      // continue
    }

    while (running) {
      canStop = true;
      try {
        acceptConnection();
      } catch (Exception exc) {}
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

    public void send() throws IOException {
      try {
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

  /**
   * Accepts a TCP connection. Opens the <code>UserConnection</code> with the
   * right user's proxy, creates and starts the <code>TcpConnection</code>.
   */
  private void acceptConnection() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpConnectionListener.acceptConnection()");

    Socket sock = proxyService.getServerSocket().accept();
    String inaddr = sock.getInetAddress().getHostAddress();

    connectionCount++;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> accept connection from " + inaddr);

    try {
      sock.setTcpNoDelay(true);

      // Fix bug when the client doesn't use the right protocol (e.g. Telnet)
      // and blocks this listener.
      sock.setSoTimeout(timeout);

      InputStream is = sock.getInputStream();
      NetOutputStream nos = new NetOutputStream(sock);

      byte[] magic = StreamUtil.readByteArrayFrom(is, 8);
      for (int i = 0; i < 5; i++) {
        if (magic.length == i || magic[i] != MetaData.joramMagic[i] && magic[i] > 0) {
          String errorMsg = "Bad magic number. Client is not compatible with JORAM.";
          logger.log(BasicLevel.ERROR, errorMsg);
          protocolErrorCount++;
          throw new IllegalAccessException(errorMsg);
        }
      }
      if (magic[7] != MetaData.joramMagic[7]) {
        if (magic[7] > 0 && MetaData.joramMagic[7] > 0) {
          String errorMsg = "Bad protocol version number " + magic[7] + " != " + MetaData.joramMagic[7];
          logger.log(BasicLevel.ERROR, errorMsg);
          protocolErrorCount++;
          throw new IllegalAccessException(errorMsg);
        }
        
        logger.log(BasicLevel.WARN,
                   "Wildcard protocol version number: from stream = " + magic[7] + ", from MetaData = " + MetaData.joramMagic[7]);
      }

      long dt = Math.abs(StreamUtil.readLongFrom(is) - System.currentTimeMillis());
      if (dt > clockSynchroThreshold)
        logger.log(BasicLevel.WARN, " -> bad clock synchronization between client and server: " + dt);
      StreamUtil.writeTo(dt, nos);

      Identity identity = Identity.read(is);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> read identity = " + identity);

      int key = StreamUtil.readIntFrom(is);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> read key = " + key);

      int heartBeat = 0;
      if (key == -1) {
        heartBeat = StreamUtil.readIntFrom(is);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> read heartBeat = " + heartBeat);
      }

      GetProxyIdNot gpin = new GetProxyIdNot(identity, inaddr);
      AgentId proxyId;
      try {
        gpin.invoke(AdminTopic.getDefault());
        proxyId = gpin.getProxyId();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        failedLoginCount++;
        StreamUtil.writeTo(1, nos);
        StreamUtil.writeTo(exc.getMessage(), nos);
        nos.send();
        return;
      }

      IOControl ioctrl;
      ReliableConnectionContext ctx;
      if (key == -1) {
        OpenConnectionNot ocn = new OpenConnectionNot(true, heartBeat);
        ocn.invoke(proxyId);
        StreamUtil.writeTo(0, nos);
        ctx = (ReliableConnectionContext) ocn.getConnectionContext();
        key = ctx.getKey();
        StreamUtil.writeTo(key, nos);
        nos.send();
        ioctrl = new IOControl(sock);
      } else {
        GetConnectionNot gcn = new GetConnectionNot(key);
        try {
          gcn.invoke(proxyId);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "", exc);
          StreamUtil.writeTo(1, nos);
          StreamUtil.writeTo(exc.getMessage(), nos);
          nos.send();
          return;
        }
        ctx = (ReliableConnectionContext) gcn.getConnectionContext();
        StreamUtil.writeTo(0, nos);
        nos.send();
        ioctrl = new IOControl(sock, ctx.getInputCounter());

        TcpConnection tcpConnection = proxyService.getConnection(proxyId, key);
        if (tcpConnection != null) {
          tcpConnection.close();
        }
      }

      // Reset the timeout in order to enable the server to indefinitely
      // wait for requests.
      sock.setSoTimeout(0);

      TcpConnection tcpConnection = new TcpConnection(ioctrl, ctx, proxyId, proxyService, identity);
      tcpConnection.start();
    } catch (IllegalAccessException exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
      sock.close();
      throw exc;
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      sock.close();
      throw exc;
    }
  }

  protected void shutdown() {
    close();
  }

  protected void close() {
    proxyService.resetServerSocket();
  }

  public int getFailedLoginCount() {
    return failedLoginCount;
  }

  public int getInitiatedConnectionCount() {
    return connectionCount;
  }

  public int getProtocolErrorCount() {
    return protocolErrorCount;
  }
}
