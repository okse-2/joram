/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
public abstract class ServletAgent extends Agent {
  /** The TCP listen port */
  protected int port = -1;
  /** The number of monitors.*/
  protected int nbm = 1;

  /**
   * Creates a ServletAgent proxy.
   */
  public ServletAgent() {
    this(AgentServer.getServerId(), null);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public ServletAgent(String name) {
    this(AgentServer.getServerId(), name);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public ServletAgent(short to, String name) {
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

  private transient Monitor monitors[] = null;
  private transient ServerSocket listen = null;

  protected transient String host = null;

  /**
   * Initializes the ServletAgent.<p>
   * Creates the listen socket, and starts the monitors.
   *
   * @param firstTime	<code>true</code> when service starts anew
   */
  public final void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);

    host = InetAddress.getLocalHost().getHostName();
    listen = new ServerSocket(port);

    monitors = new Monitor[nbm];
    for (int i=0; i<monitors.length; i++) {
      monitors[i] = new Monitor(name + ".Monitor#" + i, i, logmon);
    }

    start();
  }

  /**
   * Finalizes this proxy agent execution. Calls <code>stop</code> to stop
   * the drivers.
   *
   * @param lastTime	true when last called by the factory on agent deletion.
   */
  public final void agentFinalize(boolean lastTime) {
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
      ".ServletAgent." + getClass().getName();
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

  protected abstract Request createRequest();

  protected abstract Response createResponse();

  /**
   * Parse the incoming request and set the corresponding request
   * properties. The current implementation is empty, it should be
   * overloaded in sub-classes depending of used protocol.
   *
   * @param request The current request
   */
  protected void parseRequest(Request request) throws Exception { }

  /**
   * Notify request handler. The current implementation just send a
   * RequestNot notification to proxy agent.
   *
   * @param request The current request
   */
  protected void sendRequest(Request request) throws Exception {
    sendTo(getId(), new RequestNot(request));
  }

  /**
   * Reply to client. The current implementation just write the
   * response content to the output stream.
   *
   * @param request 	The current request
   * @param response	The current response
   */
  protected void finishResponse(Request request,
                                Response response) throws Exception {
    // TODO:
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
      if (not instanceof RequestNot) {
        Request request = monitors[((RequestNot) not).id].request;
        Response response = monitors[((RequestNot) not).id].response;
        service(request, response);
        request.validResponse(true);
      } else {
        super.react(from, not);
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR,
                   getName() + ", error in " + this +
                   ".react(" + from + ", " + not + ")", exc);
    }
  }

  public abstract void service(Request request,
                               Response response) throws Exception;

  class Monitor extends Daemon {
    transient Socket socket = null;
    transient Request request = null;
    transient Response response = null;

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
        // Allocates request and response contexts
        request = createRequest();
        request.setId(id);
        response = createResponse();
        response.setId(id);

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
            // Get the streams
            request.input = socket.getInputStream();
            response.output = socket.getOutputStream();
            // Record the connection parameters related to this request.
            request.setInet(socket.getInetAddress());
            request.setPort(socket.getPort());
            // Parse request from input stream
            parseRequest(request);
            // Send a request notification to proxy
            sendRequest(request);
            // Wait for a response
            canStop = true;

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", wait response.");

            if (request.waitResponse()) {
              canStop = false;

              // Writes response to output stream
              finishResponse(request, response);
            } else {
              // TODO:
            }
          } catch (Throwable exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during connection", exc);
          } finally {
            // Closes the connection
            try {
              request.input.close();
            } catch (Exception exc) {}
            request.recycle();
            try {
              response.output.flush();
              response.output.close();
            } catch (Exception exc) {}
            response.recycle();
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
      request.validResponse(false);
      try {
	listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    protected void shutdown() {
      close();
    }
  }
}
