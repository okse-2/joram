/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Dyade Public License,
 * as defined by the file JORAM_LICENSE_ADDENDUM.html
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Dyade web site (www.dyade.fr).
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific terms governing rights and
 * limitations under the License.
 *
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released April 20, 2000.
 *
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.*;
import fr.dyade.aaa.util.*;

class PoolCnxNetwork extends StreamNetwork {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: PoolCnxNetwork.java,v 1.3 2001-05-14 16:26:41 tachkeni Exp $";

  static final class StatusMessage implements Serializable {
    transient byte status;
    transient int stamp;

    StatusMessage(byte status) {
      super();
      this.status = status;
    }

    static byte AckStatus = 0;
    static byte NAckStatus = -1;

    static StatusMessage Ack = new StatusMessage(AckStatus);
    static StatusMessage NAck = new StatusMessage(NAckStatus);

    public final String toString() {
      return "StatusMessage(" + status + ", " + stamp + ")";
    }

    private void writeObject(java.io.ObjectOutputStream out)
      throws IOException {
      out.writeByte(status);
      out.writeInt(stamp);
    }

    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
      status = in.readByte();
      stamp = in.readInt();
    }
  }

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
  PoolCnxNetwork() throws Exception {
    super();
  }

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws IOException {
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " starting.", false);

    try {
      nbMaxCnx = AgentServer.getInteger(name + ".nbMaxCnx").intValue();
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

      activeSessions = new NetSession[nbMaxCnx];
    
      // Creates a session for all domain's server.
      sessions = new NetSession[servers.length];
      for (int i=0; i<servers.length; i++) {
	if (servers[i] != AgentServer.getServerId())
	  sessions[i] = new NetSession(getName(),
				       AgentServer.getServerDesc(servers[i]));
      }
      wakeOnConnection = new WakeOnConnection(getName());
      dispatcher = new Dispatcher(getName());
      watchDog = new WatchDog(getName());

      wakeOnConnection.start();
      dispatcher.start();
      watchDog.start();
    } catch (IOException exc) {
      Debug.trace(getName(), exc);
      throw exc;
    }

    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " started.", false);
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
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " stopped.", false);

    if (wakeOnConnection != null) wakeOnConnection.stop();
    if (dispatcher != null) dispatcher.stop();
    if (watchDog != null) watchDog.wakeup();
    for (int i=0; i<sessions.length; i++) {
      // May be we can take in account only "active" sessions.
      if (sessions[i]!= null) sessions[i].stop();
    }
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((wakeOnConnection == null) || wakeOnConnection.isRunning ||
	(dispatcher == null) || dispatcher.isRunning ||
	(watchDog == null) || watchDog.isRunning)
      return false;

    return true;
  }

  final NetSession getSession(short sid) {
    return sessions[index(sid)];
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

  final class NetSession implements Runnable {
    /**
     * Boolean variable used to stop the daemon properly. The dameon tests
     * this variable between each request, and stops if it is false.
     * @see start
     * @see stop
     */
    private volatile boolean isRunning;
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
    private Vector sendList;

    private long last = 0L;

    NetSession(String name, ServerDesc server) {
      this.server = server;
      this.name = name + ".netSession#" + server.sid;

      if (Debug.debug && Debug.A3Server)
	Debug.trace(getName() + " created.", false);
      
      isRunning = false;
      canStop = false;
      thread = null;

      sendList = new Vector();
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
      if (Debug.debug && Debug.network)
	Debug.trace(getName() + " started.", false);

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
      if (Debug.debug && Debug.network)
	Debug.trace(getName() + " remotely started.", false);

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
	  if (Debug.debug && Debug.network)
	    Debug.trace(getName() + " connection refused.", false);

	  return false;
	}

	//  Set the local attribute in order to block all others local
	// attempts.
	this.local = true;
      }

      Socket sock = null;

      ObjectInputStream ois = null;
      ObjectOutputStream oos = null;

      try {
	sock = createSocket(server.getAddr(), server.port);

	setSocketOption(sock);
	// Be careful: The OOS should be initialized first in order to
	// send the header waited by OIS at the other end.
	oos = getOutputStream(sock);
	ois = getInputStream(sock);

	oos.writeObject(new Boot());
	oos.flush();
	oos.reset();

	StatusMessage statusMsg = (StatusMessage) ois.readObject();

	if (Debug.debug && Debug.message)
	  Debug.trace(getName() + " receive:" + statusMsg, false);

	// AF: Normally the remote server never reply with a Nack, it closes
	// the connection directly, so we catch a ConnectException.
	if (statusMsg.status == StatusMessage.NAckStatus)
	  throw new ConnectException("Nack status received");
      } catch (Exception exc) {
	if (Debug.debug && Debug.network)
	  Debug.trace(getName() + ": connection refused.", exc);
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
	if (Debug.debug && Debug.message)
	  Debug.trace(getName() + " send:" + StatusMessage.Ack, false);

	oos.writeObject(StatusMessage.Ack);
	oos.flush();
	oos.reset();

	// Fixing sock attribute will prevent any future attempt 
	this.sock = sock;
	this.ois = ois;
	this.oos = oos;

	return true;
      } catch (Exception exc) {
	// May be a a connection is in progress, try it later...
	if (Debug.debug && Debug.network)
	  Debug.trace(getName() + ": connection refused", exc);

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

      isRunning = true;
      canStop = true;
      thread.start();

      if (Debug.debug && Debug.network)
	Debug.trace(getName() + ": connection started", false);

      // Try to send all waiting messages.
      for (int i=0; i<sendList.size(); i++) {
	transmit((Serializable) sendList.elementAt(i));
      }
    }

    /**
     *
     */
    synchronized void stop() {
      isRunning = false;
      
      if (Debug.debug && Debug.network)
	Debug.trace(getName() + " stopped.", false);

      if (thread == null)
	// The session is idle.
	return;

      if (canStop && (sock != null)) close();
    }

    synchronized void close() {
      if (Debug.debug && Debug.network)
	Debug.trace(getName() + ": close", false);

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
     * Be careful, its method should not be synchronized (in that case, the
     * overall synchronization of the connection -method start- can dead-lock).
     */
    final void send(Message msg) {
      if (Debug.debug && Debug.message)
	Debug.trace(getName() + ": send message #" + msg.update.stamp, false);

      sendList.addElement(msg);
      if (sock == null) {
	// If there is no connection between local and destination server,
	// try to make one!
	start();
      } else {
	transmit(msg);
      }
    }

    // Shoul be synchronized !!
    final private void ack(int stamp) throws IOException {
      if (Debug.debug && Debug.message)
	Debug.trace(getName() + ": ack message #" + stamp, false);

      StatusMessage.Ack.stamp = stamp;
      transmit(StatusMessage.Ack);
    }

    synchronized void transmit(Serializable msg) {
      last = current++;
      try {
	if (oos != null) {
	  oos.writeObject(msg);
	  oos.flush();
	  oos.reset();
	}
      } catch (IOException exc) {
	if (Debug.debug && Debug.message)
	  Debug.trace(getName() + ": Exception in sending message", exc);
	close();
      }
    }

    public void run() {
      Object obj;

      try {
	while (isRunning) {
	  canStop = true;

	  if (Debug.debug && Debug.message)
	    Debug.trace(getName() + ": waiting message", false);

	  try {
	    obj = ois.readObject();
	  } catch (ClassNotFoundException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    Debug.trace(getName(), exc);
	    continue;
	  } catch (InvalidClassException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    Debug.trace(getName(), exc);
	    continue;
	  } catch (StreamCorruptedException exc) {
	    Debug.trace(getName(), exc);
	    break;
	  } catch (OptionalDataException exc) {
	    Debug.trace(getName(), exc);
	    break;
	  }

	  canStop = false;

	  if (obj instanceof StatusMessage) {
	    StatusMessage ack = (StatusMessage) obj;

	    if (Debug.debug && Debug.message)
	      Debug.trace(getName() + ": ack received #" + ack.stamp, false);

	    for (int i=0; i<sendList.size(); i++) {
	      Message tmpMsg = (Message) sendList.elementAt(i);
	      if (tmpMsg.update.stamp == ack.stamp) {
		//  Suppress the acknowledged notification from waiting list,
		// and deletes it.
		sendList.removeElementAt(i);
		AgentServer.transaction.begin();
		tmpMsg.delete();
		AgentServer.transaction.commit();
		AgentServer.transaction.release();

		if (Debug.debug && Debug.message)
		  Debug.trace(getName() + ": ack ok #" + ack.stamp, false);

		break;
	      }
	    }
	  } else if (obj instanceof Message) {
	    //  Keep message stamp in order to acknowledge it (be careful,
	    // the message get a new stamp to be delivered).
	    int stamp = ((Message) obj).update.stamp;
	    deliver((Message) obj);
	    ack(stamp);
	  } else {
	    if (Debug.debug && Debug.message)
	      Debug.trace(getName() + ": receives " + obj, false);
	  }
	}
      } catch (Exception exc) {
	Debug.trace(getName() + ": exited", exc);
      } finally {
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(getName() + ": stopped", false);

	isRunning = false;
	close();
	thread = null;
      }
    }
  }

  final class WakeOnConnection extends Daemon {
    ServerSocket listen = null;

    WakeOnConnection(String name) {
      super(name + ".wakeOnConnection");
    }

    void shutdown() {
      try {
	listen.close();
      } catch (Exception exc) {}
      listen = null;
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

      // creates a server socket listening on configured port
      try {
	listen = createServerSocket();

	while (isRunning) {
	  try {
	    canStop = true;

	    // Get the connection
	    try {
	      if (Debug.debug && Debug.network)
		Debug.trace(this.getName() + ": wait connection", false);

	      sock = listen.accept();
	    } catch (IOException exc) {
	      if (Debug.debug && Debug.network)
		Debug.trace(this.getName(), exc);
	      continue;
	    }
	    canStop = false;

	    setSocketOption(sock);
	    // Be careful: The OOS should be initialized first in order to
	    // send the header waited by OIS at the other end
	    oos = getOutputStream(sock);
	    ois = getInputStream(sock);

	    msg = ois.readObject();

	    if (msg instanceof Boot) {
	      if (Debug.debug && Debug.network)
		Debug.trace(this.getName() + ": connection setup from #" +
			    ((Boot)msg).sid, false);

	      getSession(((Boot)msg).sid).start(sock, ois, oos);
	    } else {
	      if (Debug.debug && Debug.network)
		Debug.trace(this.getName() + ": bad connection setup", false);

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
	    if (Debug.debug)
	      Debug.trace(this.getName() + ": bad connection setup", exc);
	  }
	}
      } catch (IOException exc) {
	Debug.trace(this.getName() + " exited.", exc);
      } finally {
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(this.getName() + " stopped", false);
	isRunning = false;
	thread = null;
      }
    }
  }

  final class Dispatcher extends Daemon {
    Dispatcher(String name) {
      super(name + ".dispatcher");
    }

    void shutdown() {}

    public void run() {
      Message msg = null;
      
      while (isRunning) {
	canStop = true;

	if (Debug.debug && Debug.message)
	  Debug.trace(thread.getName() + ": waiting message", false);

	try {
	  msg = qout.get();
	} catch (InterruptedException exc) {
	  continue;
	}
	canStop = false;

	// Send the message
	getSession(msg.update.getToId()).send(msg);
	qout.pop();
      }
    }
  }

  final class WatchDog extends Daemon {
   /** Use to synchronize thread */
    private Object lock;

    WatchDog(String name) {
      super(name + ".watchdog");
      lock = new Object();
    }
    void shutdown() {
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
      
      synchronized (lock) {
	while (isRunning) {
	  try {
	    lock.wait(WDActivationPeriod);
	  } catch (InterruptedException exc) {
	    continue;
	  }

	  if (! isRunning) break;

	  for (int sid=0; sid<sessions.length; sid++) {
	    if ((sessions[sid] != null) &&
		(sessions[sid].sendList.size() > 0) &&
		(! sessions[sid].server.active)) {
	      sessions[sid].start();
	    }
	  }
	}
      }
    }
  }
}
