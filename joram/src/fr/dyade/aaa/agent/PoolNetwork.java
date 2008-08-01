/*
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  <code>PoolNetwork</code> is an implementation of <code>StreamNetwork</code>
 * class that manages multiple connection.
 */
public class PoolNetwork extends StreamNetwork {
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
  public PoolNetwork() throws Exception {
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
	nbMaxCnx = AgentServer.getInteger("PoolNetwork.nbMaxCnx").intValue();
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

    MessageInputStream nis = null;
    MessageOutputStream nos = null;

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

    void start(Socket sock, int boot) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", remotely started");

      if (remoteStart(sock, boot)) startEnd();
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
      try {
	sock = createSocket(server);
	setSocketOption(sock);

	writeBoot(sock.getOutputStream());
        int boot = readAck(sock.getInputStream());

        AgentServer.getTransaction().begin();
        testBootTS(sid, boot);
        AgentServer.getTransaction().commit();
        AgentServer.getTransaction().release();

        nis = new MessageInputStream(sock.getInputStream());
        nos = new MessageOutputStream(sock.getOutputStream());
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", connection refused.", exc);
	// TODO: Try it later, may be a a connection is in progress...
	try {
	  sock.getOutputStream().close();
	} catch (Exception exc2) {}
	try {
	  sock.getInputStream().close();
	} catch (Exception exc2) {}
	try {
	  sock.close();
	} catch (Exception exc2) {}

	// Reset the local attribute to allow future attempts.
        this.local = false;
        nis = null;
        nos = null;

	return false;
      }

      // Normally, only one thread can reach this code (*1), so we don't have
      // to synchronized theses statements. First sets sock attribute, then
      // releases the local lock.
      // (*1) all local attempts are blocked and the remote side has already
      // setup the connection (ACK reply).
      this.sock = sock;
      this.local = false;

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
    synchronized boolean remoteStart(Socket sock, int boot) {
      try {
	if ((this.sock != null) ||
	    (this.local && server.sid > AgentServer.getServerId()))
	  //  The connection is already established, or a "local" connection
	  // is in progress from this server with a greater priority.
	  //  In all cases, stops this "remote" attempt. If the "local"
	  // attempt has a lower priority, it will fail due to a remote
	  // reject.
	  throw new ConnectException("Already connected");

	// Accept this connection.
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", send AckStatus");

        writeAck(sock.getOutputStream());

        AgentServer.getTransaction().begin();
        testBootTS(sid, boot);
        AgentServer.getTransaction().commit();
        AgentServer.getTransaction().release();

        nis = new MessageInputStream(sock.getInputStream());
        nos = new MessageOutputStream(sock.getOutputStream());

	// Fixing sock attribute will prevent any future attempt 
	this.sock = sock;

	return true;
      } catch (Exception exc) {
	// May be a a connection is in progress, try it later...
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, getName() + ", connection refused", exc);

	// Close the connection (# NACK).
	try {
	  sock.getOutputStream().close();
	} catch (Exception exc2) {}
	try {
	  sock.getInputStream().close();
	} catch (Exception exc2) {}
	try {
	  sock.close();
	} catch (Exception exc2) {}
        nis = null;
        nos = null;
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
        logmon.log(BasicLevel.DEBUG, getName() + ", connection started");

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

      Message msg = null;
      long currentTimeMillis = System.currentTimeMillis();
      for (int i=0; i<waiting.length; i++) {
        msg = (Message) waiting[i];
        if ((msg.not != null) &&
            (msg.not.expiration > 0) &&
            (msg.not.expiration < currentTimeMillis)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       getName() + ": removes expired notification " +
                       msg.from + ", " + msg.not);
          try {
            doAck(msg.getStamp());
          } catch (IOException exc) {
            logmon.log(BasicLevel.ERROR,
                       getName() + ": cannot removes expired notification " +
                       msg.from + ", " + msg.not, exc);
          }
        } else {
          transmit(msg, currentTimeMillis);
        }
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
	sock.getInputStream().close();
      } catch (Exception exc) {}
      try {
	sock.getOutputStream().close();
      } catch (Exception exc) {}
      try {
	sock.close();
      } catch (Exception exc) {}
      sock = null;

      nis = null;
      nos = null;
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

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", ack received #" + ack);

      try {
        //  Suppress the acknowledged notification from waiting list,
        // and deletes it.
        msg = sendList.removeMessage(ack);
        AgentServer.getTransaction().begin();
        msg.delete();
        msg.free();
        AgentServer.getTransaction().commit();
        AgentServer.getTransaction().release();

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

      long currentTimeMillis = System.currentTimeMillis();

      if (msg.not != null) {
        sendList.addElement(msg);

        if ((msg.not.expiration > 0) &&
            (msg.not.expiration < currentTimeMillis)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       getName() + ": removes expired notification " +
                       msg.from + ", " + msg.not);
          try {
            doAck(msg.getStamp());
          } catch (IOException exc) {
            logmon.log(BasicLevel.ERROR,
                       getName() + ": cannot removes expired notification " +
                       msg.from + ", " + msg.not, exc);
          }
          return;
        }
      }

      if (sock == null) {
	// If there is no connection between local and destination server,
	// try to make one!
	start();
      } else {
	transmit(msg, currentTimeMillis);
      }
    }
    
    final private void ack(int stamp) throws Exception {
      if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", set ack msg#" + stamp);

      Message ack = Message.alloc(AgentId.localId,
                                  AgentId.localId(server.sid),
                                  null);
      ack.source = AgentServer.getServerId();
      ack.dest = AgentServer.getServerDesc(server.sid).gateway;
      ack.stamp = stamp;

      qout.push(ack);
    }

    final private synchronized void transmit(Message msg,
                                             long currentTimeMillis) {
      last = current++;
      try {
        nos.writeMessage(msg, currentTimeMillis);
      } catch (IOException exc) {
        logmon.log(BasicLevel.ERROR,
                   getName() + ", exception in sending message", exc);
	close();
      } catch (NullPointerException exc) {
        // The stream is closed, exits !
      }
    }

    public void run() {
      Message msg;

      try {
	while (running) {
	  canStop = true;

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", waiting message");

	  try {
	    msg = nis.readMessage();
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

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", receives: " + msg);

          //  Keep message stamp in order to acknowledge it (be careful,
          // the message get a new stamp to be delivered).
          int stamp = msg.getStamp();
          if (msg.not != null) {
            deliver(msg);
            ack(stamp);
          } else {
            doAck(stamp);
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

    /**
     * Class used to read messages through a stream.
     */
    final class MessageInputStream extends ByteArrayInputStream {
      private InputStream is = null;

      MessageInputStream(InputStream is) {
        super(new byte[256]);
        this.is = is;
      }

      private void readFully(int length) throws IOException {
        count = 0;
        if (length > buf.length) buf = new byte[length];

        int nb = -1;
        do {
          nb = is.read(buf, count, length-count);
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", reads:" + nb);
          if (nb < 0) throw new EOFException();
          count += nb;
        } while (count != length);
        pos  = 0;
      }

      Message readMessage() throws Exception {
        count = 0;
        readFully(Message.LENGTH +4 -1);
        // Reads boot timestamp of source server
        int length = ((buf[0] & 0xFF) << 24) + ((buf[1] & 0xFF) << 16) +
          ((buf[2] & 0xFF) <<  8) + ((buf[3] & 0xFF) <<  0);

        Message msg = Message.alloc();
        int idx = msg.readFromBuf(buf, 4);

        if (length > idx) {
          // Be careful, the buffer is resetted
          readFully(length - idx);

          // Reads notification attributes
          boolean persistent = ((buf[0] & Message.PERSISTENT) == 0)?false:true;
          boolean detachable = ((buf[0] & Message.DETACHABLE) == 0)?false:true;

          pos = 1;
          // Reads notification object
          ObjectInputStream ois = new ObjectInputStream(this);
          msg.not = (Notification) ois.readObject();
          if (msg.not.expiration > 0)
            msg.not.expiration += System.currentTimeMillis();
          msg.not.persistent = persistent;
          msg.not.detachable = detachable;
          msg.not.detached = false;
        } else {
          msg.not = null;
        }

        return msg;
      }
    }

    /**
     * Class used to send messages through a stream.
     */
    final class MessageOutputStream extends ByteArrayOutputStream {
      private OutputStream os = null;
      private ObjectOutputStream oos = null;

      MessageOutputStream(OutputStream os) throws IOException {
        super(256);

        this.os = os;
        oos = new ObjectOutputStream(this);
        count = 0;
        buf[Message.LENGTH +4] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
        buf[Message.LENGTH +5] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
        buf[Message.LENGTH +6] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
        buf[Message.LENGTH +7] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);
      }

      void writeMessage(Message msg,
                        long currentTimeMillis) throws IOException {
        logmon.log(BasicLevel.DEBUG, getName() + ", sends " + msg);

        int idx = msg.writeToBuf(buf, 4);
        // Be careful, notification attribute are not written if there
        // is no notification.
        count = Message.LENGTH +4 -1;
        
        try {
          if (msg.not != null) {
            // Writes notification attributes
            buf[idx++] = (byte) ((msg.not.persistent?Message.PERSISTENT:0) |
                                 (msg.not.detachable?Message.DETACHABLE:0));

            // Be careful, the stream header is hard-written in buf
            count = Message.LENGTH +8;

            if (msg.not.expiration > 0)
              msg.not.expiration -= currentTimeMillis;
            oos.writeObject(msg.not);
            
            oos.reset();
            oos.flush();
          }

          // Writes length at beginning
          buf[0] = (byte) (count >>>  24);
          buf[1] = (byte) (count >>>  16);
          buf[2] = (byte) (count >>>  8);
          buf[3] = (byte) (count >>>  0);

          os.write(buf, 0, count);;
          os.flush();
        } finally {
          if ((msg.not != null) && (msg.not.expiration > 0))
            msg.not.expiration += currentTimeMillis;
          count = 0;
        }
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

            Boot boot = readBoot(sock.getInputStream());
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", connection setup from #" +
                              boot.sid);
            getSession(boot.sid).start(sock, boot.boot);
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
          getSession(msg.getDest()).send(msg);
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


  final void writeBoot(OutputStream out) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", writeBoot: " + getBootTS());

    byte[] iobuf = new byte[6];
    iobuf[0] = (byte) (AgentServer.getServerId() >>>  8);
    iobuf[1] = (byte) (AgentServer.getServerId() >>>  0);
    iobuf[2] = (byte) (getBootTS() >>>  24);
    iobuf[3] = (byte) (getBootTS() >>>  16);
    iobuf[4] = (byte) (getBootTS() >>>  8);
    iobuf[5] = (byte) (getBootTS() >>>  0);
    out.write(iobuf);
    out.flush();
  }
  
  final class Boot {
    transient short sid;
    transient int boot;
  }

  final void readFully(InputStream is, byte[] iobuf) throws IOException {
    int n = 0;
    do {
      int count = is.read(iobuf, n, iobuf.length - n);
      if (count < 0) throw new EOFException();
      n += count;
    } while (n < iobuf.length);
  }

  final Boot readBoot(InputStream in) throws IOException {
    Boot boot = new Boot();

    byte[] iobuf = new byte[6];
    readFully(in, iobuf);
    boot.sid = (short) (((iobuf[0] & 0xFF) <<  8) + (iobuf[1] & 0xFF));
    boot.boot = ((iobuf[2] & 0xFF) << 24) + ((iobuf[3] & 0xFF) << 16) +
      ((iobuf[4] & 0xFF) <<  8) + ((iobuf[5] & 0xFF) <<  0);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", readBoot from #" + boot.sid +
                 " -> " + boot.boot);

    return boot;
  }
  
  final void writeAck(OutputStream out) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", writeAck: " + getBootTS());

    byte[] iobuf = new byte[4];
    iobuf[0] = (byte) (getBootTS() >>>  24);
    iobuf[1] = (byte) (getBootTS() >>>  16);
    iobuf[2] = (byte) (getBootTS() >>>  8);
    iobuf[3] = (byte) (getBootTS() >>>  0);
    out.write(iobuf);
    out.flush();
  }
  
  final int readAck(InputStream in)throws IOException {
    byte[] iobuf = new byte[4];
    readFully(in, iobuf);
    int boot = ((iobuf[0] & 0xFF) << 24) + ((iobuf[1] & 0xFF) << 16) +
      ((iobuf[2] & 0xFF) <<  8) + ((iobuf[3] & 0xFF) <<  0);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", readAck:" + boot);

    return boot;
  }
}
