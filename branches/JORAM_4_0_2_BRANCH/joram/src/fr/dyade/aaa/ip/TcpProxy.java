/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;

/**
  * Class providing a TCP "connection".
  * This class is used for a client or a server as well. A client proxy
  * is identified by a -1 <code>localPort</code> value.
  */
public abstract class TcpProxy extends ProxyAgent {
  protected int localPort = -1;		/** in server: required listening port, may be 0 */
  protected String remoteHost = null;	/** in client: server host name */
  protected int remotePort = -1;	/** in client: server port */

  protected transient int listenPort = -1;	/** actual listening port */
  protected transient ServerSocket server = null;

  /**
    * Creates a TCP server with unknown port.
    */
  public TcpProxy() {
    this(AgentServer.getServerId(), 0);
  }

  /**
    * Creates an agent to be configured.
    *
    * @param to		target agent server
    * @param name	symbolic name of this agent
    */
  public TcpProxy(short to, String name) {
    super(to, name);
  }

  /**
    * Creates a local TCP server.
    *
    * @param localPort	port number > 0, or 0 for any port
    */
  public TcpProxy(int localPort) {
    this(AgentServer.getServerId(), localPort);
  }

  /**
    * Creates a TCP server.
    *
    * @param to		agent server id where agent is to be deployed
    * @param localPort	port number > 0, or 0 for any port
    */
  public TcpProxy(short to, int localPort) {
    super(to, null);
    this.localPort = localPort;
  }

  /**
    * Creates a local TCP client.
    */
  public TcpProxy(String remoteHost, int remotePort) {
    this(AgentServer.getServerId(), remoteHost, remotePort);
  }

  /**
    * Creates a TCP client.
    *
    * @param to		    agent server id where agent is to be deployed
    * @param remoteHost	    server host name
    * @param remotePort	    server listening port
    */
  public TcpProxy(short to, String remoteHost, int remotePort) {
    super(to, null);
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
  }


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +
      ",localPort=" + localPort +
      ",remoteHost=" + remoteHost +
      ",remotePort=" + remotePort +
      ",listenPort=" + listenPort + ")";
  }

  /**
    * Initializes the transient members of this agent.
    * This function is first called by the factory agent,
    * then by the system each time the agent server is restarted.
    *
    * @param firstTime		true when first called by the factory
    */
  protected void agentInitialize(boolean firstTime) throws Exception {
    if (localPort >= 0) {
      // this is a server, initialize the listen socket
      server = new ServerSocket(localPort);
      listenPort = server.getLocalPort();
    }
    super.agentInitialize(firstTime);
  }

  /**
    * Reinitializes the agent, that is reconnects its input and output.
    * This function may be called only when all drivers are null.
    */
  protected void reinitialize() throws IOException {
    if (localPort >= 0 && listenPort == -1) {
      // this should not happen, get ready however
      server = new ServerSocket(localPort);
      listenPort = server.getLocalPort();
    }
    super.reinitialize();
  }

  /**
    * Initializes the connection with the outside, up to creating
    * the input and output streams <code>ois</code> and <code>oos</code>.
    *
    * The chain of filters, transforming the input stream of bytes into
    * a <code>Notification</code> are set by <code>setInputFilters</code>
    * defined  in derived classes. <code>setOutputFilters</code> is similar
    * for the output stream.
    *
    * If this agent is a server proxy the listening port has already been set
    * in <code>initialize</code>.
    */
  public void connect() throws Exception {
    Socket sock;
    if (localPort >= 0) {
      // this is a server
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "before accept");
      sock = server.accept();
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "after accept");
    } else {
      // this is a client
      infinite:
      while (true) {
	try {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "before connect");
	  sock = new Socket(remoteHost, remotePort);
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "after connect");
	  break infinite;
	} catch (ConnectException exc) {
          logmon.log(BasicLevel.ERROR,
                     "exception during connect " +
                     remoteHost + ':' + remotePort, exc);
	  // assume server is down
	  // wait for 1 mn before retry
	  Thread.sleep(60000);
	}
      }
    }
    oos = setOutputFilters(sock.getOutputStream());
    ois = setInputFilters(sock.getInputStream());
    logmon.log(BasicLevel.DEBUG, "driver done");
  }

  /**
    * Closes the connection with the outside.
    */
  public void disconnect() throws IOException {
    if (ois != null) {
      ois.close();
      ois = null;
    }
    if (oos != null) {
      oos.close();
      oos = null;
    }
    if (server != null) {
      server.close();
      server = null;
    }
    listenPort = -1;
  }

  /**
   * Reacts to end of in driver execution.
   */
  protected void driverDone(DriverDone not) throws IOException {
    super.driverDone(not);
    if (ois != null || oos != null) {
      // wait for both drivers to terminate execution
      stop();
      return;
    }
    // try to reconnect
    reinitialize();
  }

  /**
    * Creates a (chain of) filter(s) for transforming the specified
    * <code>InputStream</code> into a <code>NotificationInputStream</code>.
    */
  protected abstract NotificationInputStream setInputFilters(InputStream in) throws StreamCorruptedException, IOException;

  /**
    * Creates a (chain of) filter(s) for transforming the specified
    * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
    */
  protected abstract NotificationOutputStream setOutputFilters(OutputStream out) throws IOException;
}
