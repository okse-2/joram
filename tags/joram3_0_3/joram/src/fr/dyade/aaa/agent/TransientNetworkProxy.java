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
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.Arrays;

/**
 * A <code>TransientNetworkProxy</code> component is responsible for handling
 * communications with all transient agent servers associated with the local
 * persistent agent server.<p>
 * It uses a set of threaded objects to perform its task:<ul>
 * <li> a <code>Listener</code> object for waiting for transient agent
 * servers connections,
 * <li>a <code>Manager</code> object for handling outgoing communications
 * with all transient agent servers,
 * <li>a set of separate <code>Monitor</code> objects to handle incoming
 * messages from each transient server.
 * </ul>
 * A <code>TransientNetworkProxy</code> component is created when the
 * configuration file read by the local agent server indicates one or more
 * transient server handled by this one.
 *
 * @see		TransientNetworkServer
 * @see		AgentServer
 */
final class TransientNetworkProxy extends Network {
  /** RCS version number of this file: $Revision: 1.4 $ */
  public static final String RCS_VERSION="@(#)$Id: TransientNetworkProxy.java,v 1.4 2002-03-26 16:08:39 joram Exp $";

  /** The stamp. Be careful, the stamp is transient. */
  int stamp = 0;

  /**
   * Creates a new <code>TransientNetworkProxy</code> component. This simple
   * constructor is required in order to use <code>Class.newInstance()</code>
   * method during configuration.
   *
   * The configuration of component is then done by <code>init</code> method.
   */
  TransientNetworkProxy() {}

  /**
   * Initializes a new <code>TransientNetworkProxy</code> component. This
   * method is used in order to easily creates and configure a component
   * from a class name. So we can use the <code>Class.newInstance()</code>
   * method for create (whitout any parameter) the component, then we can
   * initialize it with this method.
   *
   * @param port	the listen port.
   * @param servers	the domain's servers.
   */
  public void init(String name, int port, short[] servers) {
    this.name = "AgentServer#" + AgentServer.getServerId() + '.' + name;
    this.port = port;
    this.servers = servers;
    // Sorts the array of server ids into ascending numerical order.
    Arrays.sort(servers);

    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized");
  }

  /**
   * Insert a message in the <code>MessageQueue</code>. This method is unused
   * as this component is not persistent.
   *
   * @param msg		the message
   */
  public final void insert(Message msg) {
    // Update stamp to maximum known.
    if (msg.update.stamp > stamp)
      stamp = msg.update.stamp;
    qout.insert(msg);
  }

  /**
   * Saves logical clock information to persistent storage. This class is
   * used to handle transient server, so there is no need of persistancy.
   * In fact, stamp is only used to ordered messages locally and it is
   * computed at initialization (see <code><a href="#insert()">insert()</a>
   * </code>.
   */
  public final void save() throws IOException {}

  /**
   * Restores logical clock information from persistent storage. This class is
   * used to handle transient server, so there is no need of persistancy.
   */
  public final void restore() throws IOException {}

  /**
   *  Adds a message in "ready to deliver" list.  There is no need of stamp
   * allocation: the network link is FIFO and there is no persistancy.
   * 
   * @param msg		the message
   */
  public final void post(Message msg) throws IOException {
    msg.update = new Update(AgentServer.getServerId(),
			    msg.to.to,
			    ++stamp);
    msg.save();
    qout.push(msg);
  }

  /**
   * Wakes up the watch-dog thread.
   */
  public void wakeup() {}

  /**
   * The <code>Listener</code> object that waits for transient agent
   * servers connection.
   */
  Listener listener = null;
  /**
   * The <code>Manager</code> object that handles outgoing communications
   * with all transient agent servers.
   */
  Manager manager = null;
  /**
   * the <code>Monitor</code> objects that handle incoming messages
   * from each transient server.
   */
  Monitor[] monitors = null;

  /**
   * Causes this component to begin execution.
   */
  public final void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");

    try {
      if (isRunning())
	throw new IOException("Consumer already running.");

      // Creates a manager.
      manager = new Manager(getName(), logmon);
      // Creates a listener.
      listener = new Listener(getName(), logmon);
      // For all transient servers, creates a monitor driver.
      monitors = new Monitor[servers.length];
      for (int i=0; i<monitors.length; i++) {
	// Get characteristics of server, then initialize the monitor.
	monitors[i] = new Monitor(getName(),
				  AgentServer.getServerDesc(servers[i]));
      }
      listener.start();
      manager.start();
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
  public final void stop() {
    if (listener != null) listener.stop();
    if (manager != null) manager.stop();
    for (int i=0; i<monitors.length; i++) {
      if (monitors[i]!= null) monitors[i].stop();
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((listener != null) && listener.isRunning() &&
	(manager != null) && manager.isRunning())
      return true;
    else
      return false;
  }

  final Monitor getMonitor(short sid) {
    return monitors[index(sid)];
  }

  /**
   * A <code>Manager</code> object is responsible for handling outgoing
   * communications with all transient agent servers associated with the
   * local persistent agent server.
   */
  final class Manager extends Daemon {
    Manager(String name, org.objectweb.util.monolog.api.Logger logmon) {
      super(name + ".manager");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      setDaemon(true);
    }

    protected void close() {}

    protected void shutdown() {}

    public void run() {
      Monitor monitor = null;
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

	  // Send the message
	  try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", try to send message -> " + msg);

	    monitor = getMonitor(msg.to.to);
	    monitor.send(msg);
	  } catch (ArrayIndexOutOfBoundsException exc) {
	    // The server is unknown.
	    // TODO: May be we have to post an error notification to
	    // sender.
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", server #" + msg.to.to + "unknown");
	  } catch (IOException exc) {
	    // The server is unreachable.
	    this.logmon.log(BasicLevel.WARN,
                            this.getName() + ", server #" + msg.to.to + "unreachable", exc);
	    monitor.stop();
	  }

	  try {
	    AgentServer.transaction.begin();
	    //  Suppress the processed notification from message queue,
	    // and deletes it.
	    qout.pop();
	    msg.delete();
	    AgentServer.transaction.commit();
	    AgentServer.transaction.release();
	  } catch (IOException exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", unrecoverable exception", exc);
	    //  There is an unrecoverable exception during the transaction
	    // we must exit from server.
	    AgentServer.stop();
	  }
	}
      } finally {
        finish();
      }
    }
  }

  /**
   * Class used by <code>TransientManager</code> for waiting for transient
   * agent servers connections.<p>
   * It creates a server socket listening on that port so that depending
   * transient agent server may connect to it. The transient server first
   * sends a <code>Boot</code> object onto the created connection, identifying
   * itself for this server.
   */
  final class Listener extends Daemon {
    ServerSocket listen = null;

    Listener(String name, org.objectweb.util.monolog.api.Logger logmon) {
      super(name + ".listener");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
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

    /**
     * 
     */
    public void run() {
      // creates a server socket listening on configured port
      try {
        for (int i=0; ; i++) {
          try {
            listen = new ServerSocket(port);
            break;
          } catch (BindException exc) {
            if (i > 20) throw exc;
            try {
              Thread.currentThread().sleep(i * 250);
            } catch (InterruptedException e) {}
          }
        }
      } catch (IOException exc) {
	this.logmon.log(BasicLevel.ERROR,
                             this.getName() + ", error in initialization", exc);
	AgentServer.stop();
      }

      try {
	/** Connected socket. */
	Socket sock = null;
	/** Input stream from transient agent server. */
	ObjectInputStream ois = null;
	/** Output stream to transient agent server. */
	ObjectOutputStream oos = null;

	while (running) {
	  canStop = true;
	  // waits for a transient server to connect
	  try {
	    sock = listen.accept();
	  } catch (IOException exc) {
	     this.logmon.log(BasicLevel.WARN,
                             this.getName() + ", error during connection", exc);
	    continue;
	  }
	  canStop = false;

	  try {
	    Monitor monitor = null;

	    if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");

	    // sets the input and output flows
	    oos = new ObjectOutputStream(sock.getOutputStream());
	    ois = new ObjectInputStream(sock.getInputStream());

	    // gets the server configuration
	    Boot msg = (Boot) ois.readObject();

	    // finds the server monitor from its Id
	    try {
	      monitor = getMonitor(msg.sid);
	    } catch (ArrayIndexOutOfBoundsException exc) {
	      this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", server #" + msg.sid + "unknown");
	      // Throws an exception in order to close the connection
	      throw new Exception("unknown server #" + msg.sid);
	    }

	    // sets the input and output streams with the server
	    monitor.start(sock, ois, oos);

	    if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", handle server #" + msg.sid);
	  } catch (Exception exc) {
	    this.logmon.log(BasicLevel.WARN,
                            this.getName() + ": error in connecting", exc);
	    // close the streams and socket
	    try {
	      oos.close();
	    } catch (Exception e) {}
	    try {
	      ois.close();
	    } catch (Exception e) {}
	    try {
	      sock.close();
	    } catch (Exception e) {}
	  } finally {
	    oos = null;
	    ois = null;
	    sock = null;
	  }
	}
      } finally {
        finish();
      }
    }
  }

  /**
   *  A <code>Monitor</code> object collects data for managing a separate
   * transient agent server. It is part of a <code>TransientNetworkProxy</code>
   * agent which is responsible for handling communications with all transient
   * agent servers associated with the local persistent agent server.<p>
   *  Each <code>Monitor</code> object declares a thread for handling incoming
   * notifications from the transient agent server. Outgoing notifications to
   * all transient agent servers are handled by a separate thread.
   */
  class Monitor implements Runnable {
    /**
     * Boolean variable used to stop the daemon properly. The dameon tests
     * this variable between each request, and stops if it is false.
     * @see start
     * @see stop
     */
    protected volatile boolean running;
    /**
     * Boolean variable used to stop the daemon properly. If this
     * variable is true then the daemon is waiting and it can interupted,
     * else it handles a request and it will exit after (it tests the
     * <code>{@link #running running}</code> variable between
     * each reaction)
     */
    protected volatile boolean canStop;
    /** The active component of this daemon. */ 
    Thread thread = null;
    /** The <code>daemon</code>'s name. */ 
    private String name;

    /** List of all messages waiting for connection. */
    Vector sendList;

    /** communication socket */
    Socket sock = null;
    /** input stream from transient agent server */
    ObjectInputStream ois = null;
    /** output stream to transient agent server */
    ObjectOutputStream oos = null;
    /** Description  of associated transient server */
    ServerDesc server = null;

    Monitor(String name, ServerDesc server) {
      this.server = server;
      this.name = name + ".monitor#" + server.sid;

      running = false;
      canStop = false;
      thread = null;

      sendList = new Vector();
    }

    /**
     * Returns this <code>daemon</code>'s name.
     *
     * @return this <code>daemon</code>'s name.
     */
    public final String getName() {
      return name;
    }

    /**
     * Starts the monitor.
     *
     * @param sock	the connected socket.
     * @param ois	the object input stream.
     * @param oos	the object output stream.
     */
    synchronized void start(Socket sock,
	       ObjectInputStream ois,
	       ObjectOutputStream oos) {
      if ((thread != null) && thread.isAlive()) {
        logmon.log(BasicLevel.WARN, getName() + ", already started.");
        throw new IllegalThreadStateException("already started");
      }

      this.sock = sock;
      this.ois = ois;
      this.oos = oos;

      // Send messages in sendList out.
      for (int i=0; i<sendList.size(); i++) {
	Message msg = (Message) sendList.elementAt(i);
	try {
	  send(msg);
	} catch (IOException exc) {
	  // The server is unreachable.
	  logmon.log(BasicLevel.WARN,
                     getName() +
                     ", server #" + msg.to.to + "unreachable", exc);
	  close();
	  return;
	}

	try {
	  AgentServer.transaction.begin();
	  //  Deletes the processed notification
	  sendList.removeElementAt(i); i--;
	  msg.delete();
	  AgentServer.transaction.commit();
	  AgentServer.transaction.release();
	} catch (IOException exc) {
          logmon.log(BasicLevel.ERROR,
                     getName() + ", unrecoverable exception", exc);
	  //  There is an unrecoverable exception during the transaction
	  // we must exit from server.
	  AgentServer.stop();
          return;
	}
      }

      thread = new Thread(this, this.getName());
      thread.setDaemon(true);
      canStop = true;
      running = true;
      thread.start();

      logmon.log(BasicLevel.DEBUG, getName() + ", started");
    }

    /**
     * Stops a transient agent server by closing its connection.
     */
    synchronized void stop() {
      running = false;

      if (thread != null) {
        if (canStop) {
          if (thread.isAlive()) thread.interrupt();
          shutdown();
        }
        while (true) {
          try {
            thread.join();
            break;
          } catch (InterruptedException exc) {
            continue;
          }
        }
        thread = null;
      }

      logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
    }

    public void shutdown() {
      close();
    }

    /**
     * Close the connection.
     */
    synchronized void close() {
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

    /**
     * Sends a message to the transient server.
     *
     * @param msg	the message.
     */
    synchronized void send(Serializable msg) throws IOException {
      if (oos == null) {
	sendList.addElement(msg);
	return;
      }

      oos.writeObject(msg);
      oos.flush();
      oos.reset();
    }

    public void run() {
      Message msg;

      try {
	while (running) {
	  canStop = true;
	  try {
	    msg = (Message) ois.readObject();
	  } catch (ClassNotFoundException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    logmon.log(BasicLevel.ERROR,
                       getName() + ", error during waiting message", exc);
	    continue;
	  } catch (InvalidClassException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    logmon.log(BasicLevel.ERROR,
                       getName() + ", error during waiting message", exc);
	    continue;
	  } catch (StreamCorruptedException exc) {
	    logmon.log(BasicLevel.ERROR,
                       getName() + ", error during waiting message", exc);
	    break;
	  } catch (OptionalDataException exc) {
	    logmon.log(BasicLevel.ERROR,
                       getName() + ", error during waiting message", exc);
	    break;
	  } catch (EOFException exc) {
	    logmon.log(BasicLevel.WARN,
                       this.getName() + ", connection closed", exc);
	    break;
	  } catch (SocketException exc) {
	    logmon.log(BasicLevel.WARN,
                       this.getName() + ", connection closed", exc);
	    break;
	  } catch (IOException exc) {
            if (running)
              logmon.log(BasicLevel.ERROR,
                         getName() + ", error during waiting message", exc);
	    break;
	  }
	  canStop = false;

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", receives message " + msg);

	  // delivers it to the rigth consumer
	  AgentServer.transaction.begin();
	  // Allocate a local time to the message to order it in
	  // local queue, and save it.
	  Channel.post( msg);
	  Channel.save();
	  AgentServer.transaction.commit();
	  // then commit and validate the message.
	  Channel.validate();
	  AgentServer.transaction.release();
	}
      } catch (Exception exc) {
	// TODO:
	logmon.log(BasicLevel.ERROR, getName() + ", exited", exc);
      } finally {
	logmon.log(BasicLevel.DEBUG, getName() + ", ends");
      }
    }
  }
}
