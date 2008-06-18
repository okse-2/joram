/*
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 *  <code>PoolNetwork</code> is an implementation of <code>StreamNetwork</code>
 * class that manages multiple connection.
 */
public class PoolNetwork extends StreamNetwork implements PoolNetworkMBean {
  /** */
  WakeOnConnection wakeOnConnection = null; 
  /** */
  NetSession sessions[] = null;
  /** */
  Dispatcher dispatcher = null;
  /** */
  WatchDog watchDog = null;

  int nbMaxCnx;
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
      if (servers[i] != AgentServer.getServerId()) {
        sessions[i] = new NetSession(getName(), servers[i]);
      }
    }
    wakeOnConnection = new WakeOnConnection(getName(), logmon);
    dispatcher = new Dispatcher(getName(), logmon);
    watchDog = new WatchDog(getName(), logmon);
  }

  /**
   * Adds the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void addServer(short id) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      for (int i=0; i<servers.length; i++) {
        strbuf.append("\n\t").append(sessions[i]);
      }
      logmon.log(BasicLevel.DEBUG,
                 getName() + " before addServer:" + strbuf.toString());
    }

    try {
      super.addServer(id);

      if (sessions.length == servers.length) return;

      NetSession[] newSessions = new NetSession[servers.length];


      // Copy the old array in the new one
      for (int i=0; i<sessions.length; i++) {
        if ((sessions[i] != null) && 
            (sessions[i].sid != AgentServer.getServerId())) {
          newSessions[index(sessions[i].sid)] = sessions[i];
          sessions[i] = null;
        }
      }
      sessions = newSessions;
      // Allocate the NetSession for the new server
      int idx = index(id);
      sessions[idx] = new NetSession(getName(), id);
      sessions[idx].init();

      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        StringBuffer strbuf = new StringBuffer();
        for (int i=0; i<servers.length; i++) {
          strbuf.append("\t").append(sessions[i]).append("\n");
        }
        logmon.log(BasicLevel.DEBUG,
                   getName() + " after addServer:" + strbuf.toString());
      }

    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL, getName() + " addServer failed", exc);
    }
    logmon.log(BasicLevel.FATAL, getName() + " addServer ok", new Exception());
  }

  /**
   * Removes the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void delServer(short id) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      for (int i=0; i<servers.length; i++) {
        strbuf.append("\t").append(sessions[i]).append("\n");
      }
      logmon.log(BasicLevel.DEBUG, getName() + strbuf.toString());
    }

    try {
      super.delServer(id);

      NetSession[] newSessions = new NetSession[servers.length];
      int j = 0;
      for (int i=0; i<servers.length; i++) {
        if (sessions[i] == null) {
          j += 1;
        } else if (sessions[i].sid != id) {
          newSessions[j++] = sessions[i];
          sessions[i] = null;
        }
      }
      sessions = newSessions;

      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        StringBuffer strbuf = new StringBuffer();
        for (int i=0; i<servers.length; i++) {
          strbuf.append("\t").append(sessions[i]).append("\n");
        }
        logmon.log(BasicLevel.DEBUG, getName() + strbuf.toString());
      }

    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL, getName() + " delServer failed", exc);
    }
    logmon.log(BasicLevel.FATAL, getName() + " delServer ok", new Exception());
  }

  private String getMBeanName(short sid) {
    return new StringBuffer()
      .append("server=").append(AgentServer.getName())
      .append(",cons=").append(name)
      .append(",session=netSession#").append(sid)
      .toString();
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
        if (sessions[i] != null) {
          sessions[i].init();

          try {
            MXWrapper.registerMBean(new NetSessionWrapper(this, servers[i]),
                                    "AgentServer", getMBeanName(servers[i]));
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
          }
        }
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

    // Stop all active sessions
    for (int i=0; i<activeSessions.length; i++) {
      if (activeSessions[i] != null)
        activeSessions[i].stop();
      activeSessions[i] = null;
    }
    nbActiveCnx = 0;

    // Unregister all sesion's MBean
    for (int i=0; i<sessions.length; i++) {
      if (sessions[i] != null) {
        try {
          MXWrapper.unregisterMBean("AgentServer",
                                    getMBeanName(sessions[i].sid));
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
        }
      }
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

  /**
   * Returns the NetSession component handling the communication with the
   * remote server that id is passed in parameter.
   *
   * @param sid the server id of remote server.
   * @return	the NetSession component handling the communication with the
   *            remote server.
   */
  final NetSession getSession(short sid) {
    return sessions[index(sid)];
  }

  /**
   * Returns the maximum number of concurrent connected sessions.
   *
   * @return	the number of concurrent connected sessions.
   */
  public int getNbMaxActiveSession() {
    return nbMaxCnx;
  }

  /**
   * Returns the number of currently connected sessions.
   *
   * @return	the number of currently connected sessions.
   */
  public int getNbActiveSession() {
    return nbActiveCnx;
  }

  /**
   * Tests if the session is connected.
   *
   * @param sid the server id of remote server.
   * @return	true if this session is connected; false otherwise.
   */
  final boolean isSessionRunning(short sid) {
    return sessions[index(sid)].isRunning();
  }

  /**
   * Gets the number of waiting messages to send for this session.
   *
   * @param sid the server id of remote server.
   * @return	the number of waiting messages.
   */
  final int getSessionNbWaitingMessages(short sid) {
    return sessions[index(sid)].getNbWaitingMessages();
  }

  /**
   * Returns the number of messages sent since last reboot.
   * 
   * @param sid the server id of remote server.
   * @return  the number of messages sent since last reboot.
   */
  final int getNbMessageSent(short sid) {
    return sessions[index(sid)].getNbMessageSent();
  }

  /**
   * Returns the number of messages received since last reboot.
   * 
   * @param sid the server id of remote server.
   * @return  the number of messages received since last reboot.
   */
  final int getNbMessageReceived(short sid) {
    return sessions[index(sid)].getNbMessageReceived();
  }

  /**
   * Returns the number of acknowledge sent since last reboot.
   * 
   * @param sid the server id of remote server.
   * @return  the number of acknowledge sent since last reboot.
   */
  final int getNbAckSent(short sid) {
    return sessions[index(sid)].getNbAckSent();
  }
  
  /**
   * Returns the time in milliseconds of last message received.
   * 
   * @param sid the server id of remote server.
   * @return the time in milliseconds of last message received.
   */
  final long getLastReceived(short sid) {
    return sessions[index(sid)].getLastReceived();
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
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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

  final class WakeOnConnection extends Daemon {
    ServerSocket listen = null;

    WakeOnConnection(String name, Logger logmon) throws IOException {
      super(name + ".wakeOnConnection");
      // Create the listen socket in order to verify the port availability.
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
        // After a stop we needs to create anew the listen socket.
        if (listen == null) {
          // creates a server socket listening on configured port
          listen = createServerSocket();
        }

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
      } catch (IOException exc) {
        this.logmon.log(BasicLevel.ERROR,
                        this.getName() + ", bad socket initialisation", exc);
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

          Thread.yield();
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

            long currentTimeMillis = System.currentTimeMillis();

            for (int sid=0; sid<sessions.length; sid++) {
              if (sessions[sid] != null) {
                if ((sessions[sid].sendList.size() > 0) && (! sessions[sid].running)) {
                  // Try to start the session in order to send waiting messages.
                  sessions[sid].start(currentTimeMillis);
                } else if (currentTimeMillis > (sessions[sid].getLastReceived() + 300000L) && sessions[sid].running) {
                  // The session  is inactive since a long time, may be the connection is down,
                  // try to stop the session.
                  sessions[sid].stop();
                }
              }
            }
          }
        }
      } finally {
        finish();
      }
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

    NetworkInputStream nis = null;
    NetworkOutputStream nos = null;

    /** */
    private MessageVector sendList;

    private long last = 0L;

    public String toString() {
      return toString(new StringBuffer()).toString();
    }

    public StringBuffer toString(StringBuffer strbuf) {
      strbuf.append("[sid=").append(sid);
      strbuf.append(",running=").append(running);
      strbuf.append(",name=").append(name).append("]");
      return strbuf;
    }

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

    /**
     * Returns the server identification of remote server.
     * 
     * @return  the server identification of remote server.
     */
    public short getRemoteSID() {
      return sid;
    }

    /**
     * Tests if the session is connected.
     *
     * @return	true if this session is connected; false otherwise.
     */
    public boolean isRunning() {
      return running;
    }

    /**
     * Gets the number of waiting messages to send for this session.
     *
     * @return	the number of waiting messages.
     */
    int getNbWaitingMessages() {
      return sendList.size();
    }

    /**
     * The number of messages sent since last reboot.
     */
    int nbMessageSent = 0;

    /**
     * Returns the number of messages sent since last reboot.
     * 
     * @return  the number of messages sent since last reboot.
     */
    int getNbMessageSent() {
      return nbMessageSent;
    }

    /**
     * The number of messages received since last reboot.
     */
    int nbMessageReceived = 0;

    /**
     * Returns the number of messages received since last reboot.
     * 
     * @return  the number of messages received since last reboot.
     */
    int getNbMessageReceived() {
      return nbMessageReceived;
    }

    /**
     * The number of acknowledge sent since last reboot.
     */
    int nbAckSent = 0;

    /**
     * Returns the number of acknowledge sent since last reboot.
     * 
     * @return  the number of acknowledge sent since last reboot.
     */
    int getNbAckSent() {
      return nbAckSent;
    }

    /**
     * Time in milliseconds of last message received.
     */
    long lastReceived = 0L;
    
    /**
     * Returns the time in milliseconds of last message received.
     * 
     * @return the time in milliseconds of last message received.
     */
    long getLastReceived() {
      return lastReceived;
    }

    /**
     * Starts the session opening the connection with the remote server.
     * <p>
     * The protocol is synchronized in order to avoid a 'double' connection
     * between the two servers.
     * 
     * @param currentTimeMillis The current time in milliseconds
     * 
     * @see localStart
     */
    void start(long currentTimeMillis) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", started");

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
        AgentServer.getTransaction().commit(true);

        nis = new NetworkInputStream(sock.getInputStream());
        nos = new NetworkOutputStream(sock.getOutputStream());
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
        AgentServer.getTransaction().commit(true);

        nis = new NetworkInputStream(sock.getInputStream());
        nos = new NetworkOutputStream(sock.getOutputStream());

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
        if ((msg.not != null) && (msg.not.expiration > 0) && (msg.not.expiration < currentTimeMillis)) {
          try {
            ExpiredNot expiredNot = null;
            if (msg.not.deadNotificationAgentId != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification " + msg.from + ", "
                           + msg.not + " to " + msg.not.deadNotificationAgentId);
              }
              expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification " + msg.from + ", "
                           + msg.not);
              }
            }
            doAck(msg.getStamp(), expiredNot);
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, getName() + ": cannot remove expired notification " + msg.from
                       + ", " + msg.not, exc);
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
        logmon.log(BasicLevel.DEBUG, getName() + ", stops.");

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
     * Removes the acknowledged notification from waiting list.
     * Be careful, messages in sendList are not always in stamp order.
     * Its method should not be synchronized, it scans the list from
     * begin to end, and it removes always the first element. Other
     * methods using sendList just adds element at the end.
     */
    final private void doAck(int ack, ExpiredNot expiredNot) throws Exception {
      Message msg = null;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", ack received #" + ack);

      try {
        //  Suppress the acknowledged notification from waiting list,
        // and deletes it.
        msg = sendList.removeMessage(ack);
        AgentServer.getTransaction().begin();
        if (expiredNot != null) {
          Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot));
          Channel.validate();
        }
        msg.delete();
        msg.free();
        AgentServer.getTransaction().commit(true);

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
        nbMessageSent += 1;
        sendList.addElement(msg);

        if ((msg.not.expiration > 0) &&
            (msg.not.expiration < currentTimeMillis)) {
          try {
            ExpiredNot expiredNot = null;
            if (msg.not.deadNotificationAgentId != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification " + msg.from + ", "
                           + msg.not + " to " + msg.not.deadNotificationAgentId);
              }
              expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification " + msg.from + ", "
                           + msg.not);
              }
            }
            doAck(msg.getStamp(), expiredNot);
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, getName() + ": cannot removes expired notification " + msg.from
                       + ", " + msg.not, exc);
          }
        }
      } else {
        nbAckSent += 1;
      }

      if (sock == null) {
        // If there is no connection between local and destination server,
        // try to make one!
        start(currentTimeMillis);
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
      qout.validate();
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
    
    /**
     * 
     * @see java.lang.Runnable#run()
     */
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

          // Receives a valid message, set timestamp.
          lastReceived = System.currentTimeMillis();
          
          //  Keep message stamp in order to acknowledge it (be careful,
          // the message get a new stamp when it is delivered).
          int stamp = msg.getStamp();
          if (msg.not != null) {
            nbMessageReceived += 1;
            deliver(msg);
            ack(stamp);
          } else {
            doAck(stamp, null);
          }

          Thread.yield();
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
    final class NetworkInputStream extends BufferedMessageInputStream {
      NetworkInputStream(InputStream is) {
        super();
        this.is = is;
      }

      /**
       * Reads the protocol header from this output stream.
       */
      protected void readHeader() throws IOException {}
    }

    /**
     * Class used to send messages through a stream.
     */
    final class NetworkOutputStream extends BufferedMessageOutputStream {
      NetworkOutputStream(OutputStream os) throws IOException {
        super();
        this.out = os;
      }

      protected void writeHeader() {}
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

final class NetSessionWrapper implements NetSessionWrapperMBean {
  PoolNetwork network = null;
  short sid;

  /**
   *
   */
  NetSessionWrapper(PoolNetwork network, short sid) {
    this.network = network;
    this.sid = sid;
  }

  /**
   * Gets the server identification of remote host.
   *
   * @return	the server identification of remote host.
   */
  public short getRemoteSID() {
    return sid;
  }

  /**
   * Tests if the session is connected.
   *
   * @return	true if this session is connected; false otherwise.
   */
  public boolean isRunning() {
    return network.isSessionRunning(sid);
  }

  /**
   * Gets the number of waiting messages to send for this session.
   *
   * @return	the number of waiting messages.
   */
  public int getNbWaitingMessages() {
    return network.getSessionNbWaitingMessages(sid);
  }

  /**
   * Returns the number of messages sent since last reboot.
   * 
   * @return  the number of messages sent since last reboot.
   */
  public int getNbMessageSent() {
    return network.getNbMessageSent(sid);
  }

  /**
   * Returns the number of messages received since last reboot.
   * 
   * @return  the number of messages received since last reboot.
   */
  public int getNbMessageReceived() {
    return network.getNbMessageReceived(sid);
  }

  /**
   * Returns the number of acknowledge sent since last reboot.
   * 
   * @return  the number of acknowledge sent since last reboot.
   */
  public int getNbAckSent() {
    return network.getNbAckSent(sid);
  }
  
  /**
   * Returns the time in milliseconds of last message received.
   * 
   * @return the time in milliseconds of last message received.
   */
  public long getLastReceived() {
    return network.getLastReceived(sid);
  }
}
