/*
 * Copyright (C) 2001 - SCALAGENT
 */
package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;

/**
 * A <code>ServletAgent</code> proxy provides a general agent framework for
 * services built using the request-response paradigm. Their primary use is
 * to provide web-based access to agent oriented application using HTML web
 * pages.<p>
 * The <code>ServletAgent</code> proxy needs at least an argument: the TCP
 * port number.<p>
 */
public abstract class ServerAgent extends Agent {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: ServerAgent.java,v 1.1 2002-12-11 11:22:24 maistrfr Exp $"; 

  /** The TCP listen port */
  protected int port = -1;
  /** The number of monitors.*/
  protected int nbm = 1;

  /**
   * Creates a ServletAgent proxy.
   */
  public ServerAgent() {
    this(AgentServer.getServerId(), null);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public ServerAgent(String name) {
    this(AgentServer.getServerId(), name);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public ServerAgent(short to, String name) {
    super(to, name, true);
  }

  /**
   * Set the TCP listen port.
   *
   * @param port  TCP listen port of this proxy.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Set the number of monitors.
   *
   * @param nbm  Number of monitors of this proxy.
   */
  public void setNbMonitor(int nbm) {
    this.nbm = nbm;
  }

  protected transient Monitor monitors[] = null;
  protected transient ServerSocket listen = null;

  protected transient String host = null;

  /**
   * Initializes the ServletAgent.<p>
   * Creates the listen socket, and starts the monitors.
   *
   * @param firstTime	<code>true</code> when service starts anew
   */
  public void initialize(boolean firstTime) throws Exception {
    super.initialize(firstTime);

    host = InetAddress.getLocalHost().getHostName();
    listen = getServerSocket();
    if (listen == null) {
      listen = new ServerSocket(port);
    }

    monitors = new Monitor[nbm];
    for (int i=0; i<monitors.length; i++) {
      monitors[i] = new Monitor(name + ".Monitor#" + i, i, logmon);
      monitors[i].setDaemon(true);
    }

    start();
  }

  /**
   * A service may redefine this method in order to
   * create the socket at initialization time.
   */
  protected ServerSocket getServerSocket() throws IOException {
    return null;
  }

  /**
   * Finalizes this proxy agent execution. Calls <code>stop</code> to stop
   * the drivers.
   *
   * @exception Throwable
   *	unspecialized exception
   */
  protected final void finalize() throws Throwable {
    stop();
  }

  /**
   * Returns log topic for <code>ServletAgent</code> proxies. Its method
   * overriddes the default one in Agent, the resulting logging topic
   * is <code>Debug.A3Proxy</code> dot <code>ServletAgent</code> dot the
   * real classname.
   */
  protected String getLogTopic() {
    return fr.dyade.aaa.agent.Debug.A3Proxy +
      ".ServerAgent." + getClass().getName();
  }

  public final void start() {
    for (int i=0; i<monitors.length; i++) {
      monitors[i].start();
    }
  }

  public final void stop() {
    for (int i=0; i<monitors.length; i++) {
      if (monitors[i] != null) monitors[i].stop();
    }
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(").append(super.toString());
    strBuf.append(",port=").append(port);
    strBuf.append(",monitors=[");
    for (int i=0; i<monitors.length; i++) {
      strBuf.append(monitors[i].toString()).append(",");
    }
    strBuf.append("]");
    strBuf.append(")");

    return strBuf.toString();
  }

  /**
   * Reacts to notifications.<br>
   * Assumes notifications from nullId come from monitors; let derive
   * classes handle them.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", " + this +
                   ".react(" + from + ", " + not + ")");
    try {
      super.react(from, not);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR,
                   getName() + ", error in " + this +
                   ".react(" + from + ", " + not + ")", exc);
    }
  }

  /**
   * Handles the connection.
   */
  protected abstract void doRequest(Monitor monitor) throws Exception;

  public class Monitor extends Daemon {
    transient Socket socket = null;

    transient int id = -1;

    /**
     * Constructor.
     */
    protected Monitor(String name, int id,
                      org.objectweb.util.monolog.api.Logger logmon) {
      super(name);
      this.id = id;
      this.logmon = logmon;
    }

    /**
     * Provides a string image for this object.
     *
     * @return	printable image of this object
     */
    public String toString() {
      return "(" + super.toString() +
        ",id=" + id +
	",socket=" + socket + ")";
    }

    public final void run() {
      try {
	while (running) {
	  canStop = true;
	  try {
            socket = listen.accept();
	    canStop = false;
	  } catch (IOException exc) {
	    if (running)
              this.logmon.log(BasicLevel.ERROR,
                              this.getName() + ", error during accept", exc);
	  }

	  if (! running) break;

          if (this.logmon.isLoggable(BasicLevel.DEBUG))
            this.logmon.log(BasicLevel.DEBUG,
                            this.getName() + ", connection from " +
                            socket.getInetAddress() + ':' +
                            socket.getPort());

          try {
            doRequest(this);
          } catch (Throwable exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during connection", exc);
          } finally {
            // Closes the connection
            try {
              socket.getInputStream().close();
            } catch (Exception exc) {}
            try {
              socket.getOutputStream().flush();
              socket.getOutputStream().close();
            } catch (Exception exc) {}
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
	finish();
      }
    }

    protected void close() {
      try {
	listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    protected void shutdown() {
      close();
    }

    public final Socket getSocket() {
      return socket;
    }

    public final int getId() {
      return id;
    }
  }
}
