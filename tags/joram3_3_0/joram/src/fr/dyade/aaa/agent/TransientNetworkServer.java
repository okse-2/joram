/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;

/**
 * A <code>TransientNetworkServer</code> component resides in a transient
 * agent server and ensures the notification delivery from and to the other
 * agent servers.
 * <p>
 * Each transient agent server is subordinated to a single persistent agent
 * server.
 * The persistent agent server, declared in the configuration file, hosts
 * a <code>TransientNetworkProxy</code> component connected which the
 * <code>TransientNetworkServer</code> component connects to when it
 * initializes.
 *
 * @see		TransientNetworkProxy
 * @see		AgentServer
 */
final class TransientNetworkServer extends Network {
  /** RCS version number of this file: $Revision: 1.7 $ */
  public static final String RCS_VERSION="@(#)$Id: TransientNetworkServer.java,v 1.7 2002-12-11 11:22:12 maistrfr Exp $";

  /**  */
  NetServerIn netServerIn = null;
  /**  */
  NetServerOut netServerOut = null;

  /**
   * Creates and initializes a new <code>TransientNetworkProxy</code>
   * component.
   */
  TransientNetworkServer() {}

  /**
   * Initializes a new network component. This method is used in order to
   * easily creates and configure a Network component from a class name.
   * So we can use the <code>Class.newInstance()</code> method for create
   * (whitout any parameter) the component, then we can initialize it with
   * this method.<br>
   * This method initializes the logical clock for the domain.
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   */
  public void init(String name, int port, short[] servers) throws Exception {
    name = "AgentServer#" + AgentServer.getServerId() + '.' + name;
    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized");

    netServerIn = new NetServerIn(getName(), logmon);
    netServerOut = new NetServerOut(getName(), logmon);
  }

  /**
   * Insert a message in the <code>MessageQueue</code>. This method is unused
   * as this component is not persistent.
   *
   * @param msg		the message
   */
  public void insert(Message msg) {
    qout.insert(msg);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {}

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws IOException {}

  /**
   *  Adds a message in "ready to deliver" list. There is no need of stamp
   * allocation: the network link is FIFO and there is no persistancy.
   * 
   * @param msg		the message
   */
  public void post(Message msg) {
    qout.push(msg);
  }

  /**
   * Wakes up the watch-dog thread.
   */
  public void wakeup() {}

  /** communication socket */
  Socket sock = null;
  /** input stream from transient agent server */
  ObjectInputStream ois = null;
  /** output stream to transient agent server */
  ObjectOutputStream oos = null;

  /**
   * Causes this component to begin execution.
   */
  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");

    try {
      if (isRunning())
	throw new IOException("Consumer already running.");

      // connects to the proxy server
      ServerDesc proxy = AgentServer.getServerDesc(AgentServer.getServerDesc(AgentServer.getServerId()).gateway);
      for (int i=0; ; i++) {
	try {
	  sock = new Socket(proxy.getAddr(), proxy.port);
	  break;
	} catch (IOException exc) {
	  if (i > 20) throw exc;
	  try {
	    Thread.currentThread().sleep(i * 250);
	  } catch (InterruptedException e) {}
	}
      }
      sock.setSoTimeout(0);
      sock.setTcpNoDelay(true);
      
      // sets the input and output flows
      oos = new ObjectOutputStream(sock.getOutputStream());
      ois = new ObjectInputStream(sock.getInputStream());
      // sends local configuration
      oos.writeObject(new Boot());
      oos.flush();
      oos.reset();

      netServerIn.start();
      netServerOut.start();
    } catch (UnknownServerException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

  /**
   * Forces the component to stop executing.
   */
  public void stop() {
    if (netServerIn != null) netServerIn.stop();
    if (netServerOut != null) netServerOut.stop();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  private boolean stopping = false;

  /**
   * Asynchronously stop the AgentServer. Creates a thread to execute
   * AgentServer.stop in order to allow TransientNetworkConsumer stopping
   * and avoid deadlock.
   */
  private synchronized void stopAgentServer() {
    if (stopping) return;

    Thread t = new Thread() {
        public void run() {
          AgentServer.stop();
        }
      };
    t.setDaemon(true);
    t.start();

    stopping = true;
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning() &&
	(netServerOut != null) && netServerOut.isRunning())
      return true;
    else
      return false;
  }

  /**
   * Close the connection.
   */
  public void close() {
    try {
      ois.close();
    } catch (Exception exc) {}
    ois = null;
    try {
      oos.close();
    } catch (Exception exc) {}
    oos = null;
    try {
      sock.close();
    } catch (Exception exc) {}
    sock = null;
  }

  final class NetServerIn extends Daemon {
    NetServerIn(String name, Logger logmon) {
      super(name + ".netServerIn");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {
      ((TransientNetworkServer) network).close();
    }

    protected void shutdown() {
      close();
    }

    public void run() {
      Message msg = null;

      try {
	while (running) {
	  canStop = true;

	  try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", waiting message");
	    msg = (Message) ois.readObject();
	    // reset the error's count.
	  } catch (ClassNotFoundException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during waiting message", exc);
	    continue;
	  } catch (InvalidClassException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during waiting message", exc);
	    continue;
	  } catch (StreamCorruptedException exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during waiting message", exc);
	    break;
	  } catch (OptionalDataException exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during waiting message", exc);
	    break;
	  } catch (EOFException exc) {
	    this.logmon.log(BasicLevel.WARN,
                            this.getName() + ", connection closed", exc);
	    break;
	  } catch (SocketException exc) {
	    this.logmon.log(BasicLevel.WARN,
                            this.getName() + ", connection closed", exc);
	    break;
	  } catch (IOException exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error during waiting message", exc);
	    break;
	  }
	  canStop = false;

          if (this.logmon.isLoggable(BasicLevel.DEBUG))
            this.logmon.log(BasicLevel.DEBUG,
                            this.getName() + ", receives message " + msg);

	  Channel.post( msg);
	  Channel.validate();
	}
      } catch (Exception exc) {
	// TODO:
	this.logmon.log(BasicLevel.ERROR, this.getName() + ", exited", exc);
      } finally {
        finish();
	stopAgentServer();
      }
    }
  }

  final class NetServerOut extends Daemon {
    NetServerOut(String name, Logger logmon) {
      super(name + ".netServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {
      ((TransientNetworkServer) network).close();
    }

    protected void shutdown() {
      close();
    }

    public void run() {
      Message msg = null;
      
      try {
	while (running) {
	  canStop = true;

          if (this.logmon.isLoggable(BasicLevel.DEBUG))
            this.logmon.log(BasicLevel.DEBUG,
                            this.getName() + ", waiting message");

	  try {
	    msg = qout.get();
	  } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", interrupted");
	    continue;
	  }
	  canStop = false;

          if (this.logmon.isLoggable(BasicLevel.DEBUG))
            this.logmon.log(BasicLevel.DEBUG,
                            this.getName() + ", try to send message -> " + msg);

	  // Send the message
	  try {
	    oos.writeObject(msg);
	    oos.flush();
	    oos.reset();
	  } catch (IOException exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", server unreachable", exc);
	    break;
	  }
	  qout.pop();
	}
      } finally {
        finish();
	stopAgentServer();
      }
    }
  }
}
