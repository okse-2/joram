/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  <code>PoolCnxNetwork</code> is an implementation of
 * <code>StreamNetwork</code> class for stream sockets that manages
 * multiple connection.
 */
public class PoolCnxNetwork extends CausalNetwork {
  /** */
  WakeOnConnection wakeOnConnection = null; 
  /** */
  NetSession sessions[] = null;
  /** */
  Dispatcher dispatcher = null;
  /** */
  WatchDog watchDog = null;

  static int nbMaxCnx;
  int nbActiveCnx = 0;
  NetSession activeSessions[];
  long current = 0L;

  /**
   * Creates a new network component.
   */
  public PoolCnxNetwork() throws Exception {
    super();
  }

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
    super.init(name, port, servers);

    // Creates a session for all domain's server.
    sessions = new NetSession[servers.length];
    for (int i=0; i<sessions.length; i++) {
      if (servers[i] != AgentServer.getServerId())
        sessions[i] = new NetSession(getName(), servers[i]);
    }
    wakeOnConnection = new WakeOnConnection(getName(), logmon);
    dispatcher = new Dispatcher(getName(), logmon);
    watchDog = new WatchDog(getName(), logmon);
  }

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {
      nbMaxCnx = AgentServer.getInteger(getName() + ".nbMaxCnx").intValue();
    } catch (Exception exc) {
      try {
	nbMaxCnx = AgentServer.getInteger("fr.dyade.aaa.agent.PoolCnxNetwork.nbMaxCnx").intValue();
      } catch (Exception exc2) {
	nbMaxCnx = 5;
      }
    }

    try {
      if (isRunning())
	throw new IOException("Consumer already running.");

      for (int i=0; i<sessions.length; i++) {
        if (sessions[i] != null) sessions[i].init();
      }

      activeSessions = new NetSession[nbMaxCnx];

      wakeOnConnection.start();
      dispatcher.start();
      watchDog.start();
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

  /**
   * Wakes up the watch-dog thread.
   */
  public void wakeup() {
    if (watchDog != null) watchDog.wakeup();
  }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (wakeOnConnection != null) wakeOnConnection.stop();
    if (dispatcher != null) dispatcher.stop();
    if (watchDog != null) watchDog.stop();
    for (int i=0; i<sessions.length; i++) {
      // May be we can take in account only "active" sessions.
      if (sessions[i]!= null) sessions[i].stop();
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
    if ((wakeOnConnection != null) && wakeOnConnection.isRunning() &&
	(dispatcher != null) && dispatcher.isRunning() &&
	(watchDog != null) && watchDog.isRunning())
      return true;

    return false;
  }

  final NetSession getSession(short sid) {
    return sessions[clock.index(sid)];
  }

  /**
   * Returns a string representation of this consumer, including the
   * daemon's name and status.
   *
   * @return	A string representation of this consumer. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(super.toString()).append("\n\t");
    if (wakeOnConnection != null)
      strbuf.append(wakeOnConnection.toString()).append("\n\t");
    if (dispatcher != null)
      strbuf.append(dispatcher.toString()).append("\n\t");
    if (watchDog != null)
      strbuf.append(watchDog.toString()).append("\n\t");
    for (int i=0; i<sessions.length; i++) {
      // May be we can take in account only "active" sessions.
      if (sessions[i]!= null)
	strbuf.append(sessions[i].toString()).append("\n\t");
    }

    return strbuf.toString();
  }

  final class MessageVector extends Vector {
    public synchronized Message removeMessage(int stamp) {
      Message msg = null;

      modCount++;
      for (int index=0 ; index<elementCount ; index++) {
        try {
          msg = (Message) elementData[index];
        } catch (ClassCastException exc) {
          continue;
        }
        if (msg.getStamp() == stamp) {
          int j = elementCount - index - 1;
          if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
          }
          elementCount--;
          elementData[elementCount] = null; /* to let gc do its work */
        
          return msg;
        }
      }
      throw new NoSuchElementException();
    }
  }

  final class NetSession implements Runnable {
    /** Destination server id */
    private short sid;
    /**
     * Boolean variable used to stop the daemon properly. The dameon tests
     * this variable between each request, and stops if it is false.
     * @see start
     * @see stop
     */
    private volatile boolean running = false;
    /**
     * True if the sessions can be stopped, false otherwise. A session can
     * be stopped if it is waiting.
     */
    private boolean canStop = false;
    /** The thread. */
    private Thread thread = null;
    /** The session's name. */
    private String name = null;

    /**
     *  True if a "local" connection is in progress, a local connection
     * is initiated from this server to the remote one (defined by the
     * {@link #server server} descriptor.
     *  This attribute is used to synchronize local and remote attempts to
     * make connections.
     */
    private boolean local = false;

    /**
     * The description of the remote server handled by this network session.
     */
    private ServerDesc server;
    /** The communication socket. */
    private Socket sock = null;
    /** */
    private ObjectInputStream ois = null;
    /** */
    private ObjectOutputStream oos = null;
    /** */
    private MessageVector sendList;

    private long last = 0L;

    NetSession(String name, short sid) {
      this.sid = sid;
      this.name = name + ".netSession#" + sid;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", created");
      
      running = false;
      canStop = false;
      thread = null;

      sendList = new MessageVector();
    }

    void init() throws UnknownServerException {
      server = AgentServer.getServerDesc(sid);
    }

    /**
     * Returns this session's name.
     *
     * @return this session's name.
     */
    public final String getName() {
      return name;
    }

    void start() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", started");

      long currentTimeMillis = System.currentTimeMillis();

      if (((server.retry < WDNbRetryLevel1) && 
	   ((server.last + WDRetryPeriod1) < currentTimeMillis)) ||
	  ((server.retry < WDNbRetryLevel2) &&
	   ((server.last + WDRetryPeriod2) < currentTimeMillis)) ||
	  ((server.last + WDRetryPeriod3) < currentTimeMillis)) {
	if (localStart()) {
	  startEnd();
	} else {
	  server.last = currentTimeMillis;
	  server.retry += 1;
	}
      }
    }

    void start(Socket sock,
	       ObjectInputStream ois,
	       ObjectOutputStream oos) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", remotely started");

      if (remoteStart(sock, ois, oos)) startEnd();
    }

    /**
     *  Its method is called by <a href="#start()">start</a> in order to
     * initiate a connection from the local server. The corresponding code
     * on remote server is the method <a href="#remoteStart()">remoteStart</a>.
     * Its method creates the socket, initiates the network connection, and
     * negociates with remote server.<p><hr>
     *  Its method can be overidden in order to change the connection protocol
     * (introduces authentification by example, or uses SSL), but must respect
     * somes conditions:<ul>
     * <li>send a Boot object after the initialization of object streams (it
     * is waiting by the wakeOnConnection thread),
     * <li>wait for an acknowledge,
     * <li>set the sock, ois and oos attributes at the end if the connection
     * is correct.
     * </ul><p>
     *  In order to overide the protocol, we have to implements its method,
     * with the remoteStart and the transmit methods.
     *
     * @return	true if the connection is established, false otherwise.
     */
    boolean localStart() {
      synchronized (this) {
	if ((this.sock != null) || this.local) {
	  //  The connection is already established, or a "local" connection
	  // is in progress (remoteStart method is synchronized).
	  //  In all cases refuses the connection request.
          if (logmon.isLoggable(BasicLevel.WARN))
            logmon.log(BasicLevel.WARN, getName() + ", connection refused");
	  return false;
	}

	// Set the local attribute in order to block all others local attempts.
	this.local = true;
      }

      Socket sock = null;

      ObjectInputStream ois = null;
      ObjectOutputStream oos = null;

      try {
	sock = createSocket(server);
	setSocketOption(sock);
	// Be careful: The OOS should be initialized first in order to
	// send the header waited by OIS at the other end.
	oos = new ObjectOutputStream(sock.getOutputStream());
	ois = new ObjectInputStream(sock.getInputStream());

	oos.writeObject(new Boot());
	oos.flush();
	oos.reset();

	StatusMessage statusMsg = (StatusMessage) ois.readObject();

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", receive: " + statusMsg);

	// AF: Normally the remote server never reply with a Nack, it closes
	// the connection directly, so we catch a ConnectException.
	if (statusMsg.status == StatusMessage.NAckStatus)
	  throw new ConnectException("Nack status received");
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", connection refused.", exc);
	// TODO: Try it later, may be a a connection is in progress...
	try {
	  oos.close();
	} catch (Exception exc2) {}
	try {
	  ois.close();
	} catch (Exception exc2) {}
	try {
	  sock.close();
	} catch (Exception exc2) {}

	// Reset the local attribute to allow future attempts.
        this.local = false;

	return false;
      }

      // Normally, only one thread can reach this code (*1), so we don't have
      // to synchronized theses statements. First sets sock attribute, then
      // releases the local lock.
      // (*1) all local attempts are blocked and the remote side has already
      // setup the connection (ACK reply).
      this.sock = sock;
      this.local = false;

      this.ois = ois;
      this.oos = oos;

      return true;
    }

    /**
     *  Its method is called by <a href="start(java.net.Socket,
     * java.io.ObjectInputStream, java.io.ObjectOutputStream">start</a>
     * in order to reply to a connection request from a remote server.
     * The corresponding code on remote server is the method
     * <a href="#localStart()">localStart</a>.
     *
     * @param sock	the connected socket
     * @param ois	the input stream
     * @param oos	the output stream
     *
     * @return	true if the connection is established, false otherwise.
     */
    synchronized boolean remoteStart(Socket sock,
				     ObjectInputStream ois,
				     ObjectOutputStream oos) {
      try {
	if ((this.sock != null) ||
	    (this.local && server.sid > AgentServer.getServerId()))
	  //  The connection is already established, or
	  // a "local" connection is in progress from this server with a
	  // greater priority.
	  //  In all cases, stops this "remote" attempt.
	  //  If the "local" attempt has a lower priority, it will fail
	  // due to a remote reject.
	  throw new ConnectException("Already connected");

	// Accept this connection.
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                         getName() + ", send AckStatus");

	oos.writeObject(new StatusMessage(StatusMessage.AckStatus));
	oos.flush();
	oos.reset();

	// Fixing sock attribute will prevent any future attempt 
	this.sock = sock;
	this.ois = ois;
	this.oos = oos;

	return true;
      } catch (Exception exc) {
	// May be a a connection is in progress, try it later...
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                         getName() + ", connection refused", exc);

	// Close the connection (# NACK).
	try {
	  oos.close();
	} catch (Exception exc2) {}
	try {
	  ois.close();
	} catch (Exception exc2) {}
	try {
	  sock.close();
	} catch (Exception exc2) {}
      }
      return false;
    }

    /**
     *  The session is well initialized, we can start the server thread that
     * "listen" the connected socket. If the maximum number of connections
     * is reached, one connection from the pool is closed.
     */
    private void startEnd() {
      server.active = true;
      server.retry = 0;
    
      synchronized(activeSessions) {
	if (nbActiveCnx < nbMaxCnx) {
	  // Insert the current session in the active pool.
	  activeSessions[nbActiveCnx++] = this;
	} else {
	  // Search the last recently used session in the pool.
	  long min = Long.MAX_VALUE;
	  int idx = -1;
	  for (int i=0; i<nbMaxCnx; i++) {
	    if (activeSessions[i].last < min) {
	      idx = i;
	      min = activeSessions[i].last;
	    }
	  }
	  // Kill choosed session and insert new one
	  activeSessions[idx].stop();
	  activeSessions[idx] = this;
	}
	last = current++;
      }
      thread = new Thread(this, getName());
      thread.setDaemon(false);

      running = true;
      canStop = true;
      thread.start();

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                         getName() + ", connection started");

      //  Try to send all waiting messages. As this.sock is no longer null
      // so we must do a copy a waiting messages. New messages will be send
      // directly in send method.
      //  Be careful, in a very limit case a message can be sent 2 times:
      // added in sendList after sock setting and before array copy, il will
      // be transmit in send method and below. However there is no problem,
      // the copy will be discarded on remote node and 2 ack messages will
      // be received on local node.
      Object[] waiting = sendList.toArray();
      logmon.log(BasicLevel.DEBUG,
		 getName() + ", send " + waiting.length + " waiting messages");
      for (int i=0; i<waiting.length; i++) {
	transmit((Message) waiting[i]);
      }
    }

    /**
     *
     */
    void stop() {
      running = false;
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", stopped.");

      while ((thread != null) && thread.isAlive()) {
        if (canStop) {
          if (thread.isAlive()) thread.interrupt();
          shutdown();
        }
        try {
          thread.join(1000L);
        } catch (InterruptedException exc) {
          continue;
        }
        thread = null;
      }
    }

    public void shutdown() {
      close();
    }

    synchronized void close() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", closed.");

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
     * Removes all messages in sendList previous to the ack'ed one.
     * Be careful, messages in sendList are not always in stamp order.
     * Its method should not be synchronized, it scans the list from
     * begin to end, and it removes always the first element. Other
     * methods using sendList just adds element at the end.
     */
    final private void doAck(int ack) throws IOException {
      Message msg = null;
      try {
        //  Suppress the acknowledged notification from waiting list,
        // and deletes it.
        msg = sendList.removeMessage(ack);
        AgentServer.transaction.begin();
        msg.delete();
        AgentServer.transaction.commit();
        AgentServer.transaction.release();

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", remove msg#" + msg.getStamp());
      } catch (NoSuchElementException exc) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", can't ack, unknown msg#" + ack);
      }
    }

    /**
     * Be careful, its method should not be synchronized (in that case, the
     * overall synchronization of the connection -method start- can dead-lock).
     */
    final void send(Message msg) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        if (msg.not != null) {
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", send msg#" + msg.getStamp());
        } else {
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", send ack#" + msg.getStamp());
        }
      }

      if (msg.not != null) {
        sendList.addElement(msg);
      }

      if (sock == null) {
	// If there is no connection between local and destination server,
	// try to make one!
	start();
      } else {
	transmit(msg);
      }
    }
    
//  final private synchronized void ack(int stamp) throws IOException {
    final private void ack(int stamp) throws Exception {
      if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", set ack msg#" + stamp);

      Message ack = Message.alloc(AgentId.localId,
                                  AgentId.localId(server.sid),
                                  null);
      ack.setUpdate(Update.alloc(AgentServer.getServerId(),
                                 AgentServer.getServerDesc(server.sid).gateway,
                                 stamp));
      qout.push(ack);
    }

    final private synchronized void transmit(Message msg) {
      last = current++;
      try {
        oos.writeObject(msg);
        oos.flush();
        oos.reset();
      } catch (IOException exc) {
        logmon.log(BasicLevel.ERROR,
                   getName() + ", exception in sending message", exc);
	close();
      } catch (NullPointerException exc) {
        // The stream is closed, exits !
      }
    }

    public void run() {
      Object obj;

      try {
	while (running) {
	  canStop = true;

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", waiting message");

	  try {
	    obj = ois.readObject();
	  } catch (ClassNotFoundException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    logmon.log(BasicLevel.ERROR,
                       getName() + ", error during waiting message", exc);
	    continue;
	  } catch (InvalidClassException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message..
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
	  } catch (NullPointerException exc) {
            // The stream is closed, exits !
            break;
          }

	  canStop = false;

          if (obj instanceof Message) {
            Message msg = (Message) obj;
            //  Keep message stamp in order to acknowledge it (be careful,
            // the message get a new stamp to be delivered).
            int stamp = msg.getStamp();
            if (msg.not != null) {
              deliver(msg);
              ack(stamp);
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() +
                           ", ack received #" + stamp);
              doAck(stamp);
            }
	  } else {
            if (logmon.isLoggable(BasicLevel.WARN))
              logmon.log(BasicLevel.WARN, getName() + ", receives " + obj);
	  }
	}
      } catch (EOFException exc) {
        if (running)
          logmon.log(BasicLevel.WARN,
                     this.getName() + ", connection closed", exc);
      } catch (SocketException exc) {
        if (running)
          logmon.log(BasicLevel.WARN,
                     this.getName() + ", connection closed", exc);
      } catch (Exception exc) {
	logmon.log(BasicLevel.ERROR, getName() + ", exited", exc);
      } finally {
	logmon.log(BasicLevel.DEBUG, getName() + ", ends");
	running = false;
	close();
      }
    }
  }

  final class WakeOnConnection extends Daemon {
    ServerSocket listen = null;

    WakeOnConnection(String name, Logger logmon) throws IOException {
      super(name + ".wakeOnConnection");
      // creates a server socket listening on configured port
      listen = createServerSocket();
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
      /** Connected socket. */
      Socket sock = null;
      /** Input stream from transient agent server. */
      ObjectInputStream ois = null;
      /** Output stream to transient agent server. */
      ObjectOutputStream oos = null;

      Object msg = null;

      try {
	while (running) {
	  try {
	    canStop = true;

	    // Get the connection
	    try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", waiting connection");

	      sock = listen.accept();
	    } catch (IOException exc) {
              if (running)
                this.logmon.log(BasicLevel.ERROR,
                                this.getName() +
                                ", error during waiting connection", exc);
	      continue;
	    }
	    canStop = false;

	    setSocketOption(sock);
	    // Be careful: The OOS should be initialized first in order to
	    // send the header waited by OIS at the other end
	    oos = new ObjectOutputStream(sock.getOutputStream());
	    ois = new ObjectInputStream(sock.getInputStream());

	    msg = ois.readObject();

	    if (msg instanceof Boot) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", connection setup from #" +
                           ((Boot)msg).sid);
	      getSession(((Boot)msg).sid).start(sock, ois, oos);
	    } else {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", bad connection setup");
	      try {
		oos.close();
	      } catch (Exception exc2) {}
	      try {
		ois.close();
	      } catch (Exception exc2) {}
	      try {
		sock.close();
	      } catch (Exception exc2) {}
	    }
	  } catch (Exception exc) {
	    this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", bad connection setup", exc);
	  }
	}
      } finally {
        finish();
      }
    }
  }

  final class Dispatcher extends Daemon {
    Dispatcher(String name, Logger logmon) {
      super(name + ".dispatcher");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {}

    protected void shutdown() {}

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
            continue;
          }
          canStop = false;
          if (! running) break;

          // Send the message
          getSession(msg.getToId()).send(msg);
          qout.pop();
        }
      } finally {
        finish();
      }
    }
  }

  final class WatchDog extends Daemon {
   /** Use to synchronize thread */
    private Object lock;

    WatchDog(String name, Logger logmon) {
      super(name + ".watchdog");
      lock = new Object();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {}

    protected void shutdown() {
      wakeup();
    }

    void wakeup() {
      synchronized (lock) {
	lock.notify();
      }
    }

    public void run() {
      int ret;
      Message msg = null;
      long currentTimeMillis;
      
      try {
        synchronized (lock) {
          while (running) {
            try {
              lock.wait(WDActivationPeriod);
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", activated");
            } catch (InterruptedException exc) {
              continue;
            }

            if (! running) break;

            for (int sid=0; sid<sessions.length; sid++) {
              if ((sessions[sid] != null) &&
                  (sessions[sid].sendList.size() > 0) &&
                  (! sessions[sid].running)) {
                sessions[sid].start();
              }
            }
          }
        }
      } finally {
        finish();
      }
    }
  }
}
