/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest.amqp;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A {@link LiveServerConnection} keeps alive a connection to an AMQP server.
 * When the connection fails, a reconnection routine starts.
 */
public class LiveServerConnection implements LiveServerConnectionMBean, ShutdownListener, Serializable {

  private static final long serialVersionUID = 1L;

  private static final Logger logger = Debug.getLogger(LiveServerConnection.class.getName());

  private transient ConnectionFactory cnxFactory;

  private transient ReconnectionDaemon cnxDaemon;

  private transient volatile Connection conn = null;

  private String name;

  private String host;

  private int port;

  private String user;

  private String password;

  /**
   * Starts a connection with a default AMQP server.
   */
  public LiveServerConnection() {
  }

  /**
   * Starts a connection with a server accessible via the factory provided.
   * 
   * @param factory the factory used to access the server.
   */
  public LiveServerConnection(String name, String host, int port, String user, String password) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.user = user;
    this.password = password;
  }

  public void startLiveConnection() {
    this.cnxFactory = new ConnectionFactory();
    cnxFactory.setHost(host);
    cnxFactory.setPort(port);
    if (user != null) {
      cnxFactory.setUsername(user);
    }
    if (password != null) {
      cnxFactory.setPassword(password);
    }
    cnxDaemon = new ReconnectionDaemon();

    try {
      conn = cnxFactory.newConnection();
      conn.addShutdownListener(this);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "connection failed, start daemon.", exc);
      }
      cnxDaemon.start();
    }

    try {
      MXWrapper.registerMBean(this, getMBeanName());
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
  }

  public boolean isConnectionOpen() {
    return conn != null && conn.isOpen();
  }

  public Connection getConnection() {
    return conn;
  }
  
  public ConnectionFactory getConnectionFactory() {
    return cnxFactory;
  }

  private String getMBeanName() {
    StringBuilder strbuf = new StringBuilder();

    strbuf.append("AMQP#").append(AgentServer.getServerId());
    strbuf.append(':');
    strbuf.append("type=Connections,name=").append(name);
    strbuf.append('[').append(cnxFactory.getHost()).append(']');

    return strbuf.toString();
  }

  /**
   * Stops maintaining the connection alive with the server.
   */
  public void stopLiveConnection() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Close connection.");
    }
    if (conn != null) {
      conn.removeShutdownListener(this);
      try {
        conn.close();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Connection closing error.", exc);
        }
      }
    }
    if (cnxDaemon.isRunning()) {
      cnxDaemon.stop();
    }
    try {
      MXWrapper.unregisterMBean(getMBeanName());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "unregisterMBean", exc);
      }
    }
  }

  public void shutdownCompleted(ShutdownSignalException cause) {
    if (logger.isLoggable(BasicLevel.WARN)) {
      logger.log(BasicLevel.WARN, "Connection with AMQP server lost, start reconnecting.", cause);
    }
    cnxDaemon.start();
  }

  /**
   * The <code>ReconnectionDaemon</code> thread is responsible for reconnecting
   * the module with the foreign AMQP server in case of disconnection.
   */
  private class ReconnectionDaemon extends Daemon {

    /** Number of reconnection trials of the first step. */
    private int attempts1 = 30;

    /** Retry interval (in milliseconds) of the first step. */
    private long interval1 = 1000L;

    /** Number of reconnection trials of the second step. */
    private int attempts2 = 55;

    /** Retry interval (in milliseconds) of the second step. */
    private long interval2 = 5000L;

    /** Retry interval (in milliseconds) of the third step. */
    private long interval3 = 60000L;

    /** Constructs a <code>ReconnectionDaemon</code> thread. */
    protected ReconnectionDaemon() {
      super("ReconnectionDaemon#" + name, logger);
      setDaemon(false);
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon<init>");
      }
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "run()");
      }

      int attempts = 0;
      long interval;

      try {
        while (running) {

          attempts++;

          if (attempts == 1) {
            interval = 0;
          } else if (attempts <= attempts1) {
            interval = interval1;
          } else if (attempts <= attempts2) {
            interval = interval2;
          } else {
            interval = interval3;
          }

          canStop = true;
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "attempt " + attempts + ", wait=" + interval);
            }
            Thread.sleep(interval);

            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "connect...");
            }
            canStop = false;

            conn = cnxFactory.newConnection();
            conn.addShutdownListener(LiveServerConnection.this);

          } catch (Exception exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "connection failed, continue... " + exc.getMessage());
            }
            continue;
          }

          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Connected on " + cnxFactory.getHost() + ':' + cnxFactory.getPort());
          }
          break;
        }
      } finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown() {
      interrupt();
    }

    /** Releases the daemon's resources. */
    public void close() {
    }
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getName() {
    return name;
  }

  public String getUserName() {
    return user;
  }

  public String getState() {
    if (isConnectionOpen()) {
      return "OK";
    }
    return "FAILING";
  }

}
