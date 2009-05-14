/*
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CSSI
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
 * class that manages multiple connection in a pool paradigm way.
 */
public class PoolNetwork extends StreamNetwork implements PoolNetworkMBean {
  /** Daemon listening for connection from other servers. */
  WakeOnConnection wakeOnConnection = null; 
  /**
   * Components handling communication with other servers.
   * There is a NetSession component for each server in the domain, some
   * are 'active' (i.e connected).
   */
  NetSession sessions[] = null;
  /** Daemon sending message to others servers. */
  Dispatcher dispatcher = null;
  
  /** sender daemon use by NetSession to send message. */
  PoolSender poolSender = null;
  
  /** Daemon handling the messages for inaccessible servers. */
  WatchDog watchDog = null;

  /** Synchronized vector of active (i.e. connected) sessions. */
  Vector activeSessions;

  /**
   * Defines if the streams between servers are compressed or not.
   * <p>
   * Default value is false, be careful in a domain all servers must use
   * the same definition.
   * <p>
   *  This value can be adjusted for all network components by setting
   * <code>PoolNetwork.compressedFlows</code> global property or for a
   * particular network by setting <code>\<DomainName\>.compressedFlows</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  boolean compressedFlows = false;
  
  /**
   * Returns if the stream between servers are compressed or not.
   *
   * @return  true if the streams between servers are compressed, false
   *               otherwise.
   */
  public boolean getCompressedFlows() {
    return compressedFlows;
  }

  /**
   * Defines the maximum number of concurrent connected sessions.
   * <p>
   * By default this property is set to -1 to dynamically adjust to the number
   * of servers of the domain (excepting the current server). Setting this value
   * needs precautions to avoid unexpected connection loss.
   * <p>
   * This value can be adjusted for all network components by setting
   * <code>PoolNetwork.nbMaxCnx</code> global property or for a particular
   * network by setting <code>\<DomainName\>.nbMaxCnx</code> specific property.
   * <p>
   * Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int nbMaxCnx;

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
    return activeSessions.size();
  }
    
  /**
   *  Defines in milliseconds the maximum idle period permitted before reseting
   * the connection.
   * <p>
   *  The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 60000 (1 minute), value less than 1000 are
   * unauthorized.
   * <p>
   *  This value can be adjusted for all network components by setting
   * <code>PoolNetwork.IdleTimeout</code> global property or for a particular
   * network by setting <code>\<DomainName\>.IdleTimeout</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  long IdleTimeout = 60000L;

  /**
   * Returns the maximum idle period permitted before reseting the connection.
   *
   * @return the maximum idle period permitted before reseting the connection.
   */
  public long getIdleTimeout() {
    return IdleTimeout;
  }

  /**
   * Sets the maximum idle period permitted before reseting the connection.
   *
   * @param the maximum idle period permitted before reseting the connection.
   */
  public void setIdleTimeout(long idleTimeout) {
    if (idleTimeout > 1000L)
      this.IdleTimeout = idleTimeout;
  }
  
  /**
   * Defines the default value for maximum number of message sent and non
   * acknowledged on a connection.
   * <p>
   * By default this value is set to -1 and there is no flow control.
   * <p>
   *  This value can be adjusted for all network components by setting the
   * <code>PoolNetwork.maxMessageInFlow</code> global property or for a particular
   * network by setting \<DomainName\>.maxMessageInFlow</code> specific property.
   * <p>
   *  For a particular network the value can be defined finely for a the connection
   * with a particular remote server by setting <code>PoolNetwork.maxMessageInFlow_N</code>
   * or \<DomainName\>.maxMessageInFlow_N</code>.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int defaultMaxMessageInFlow = -1;

  /**
   * Defines the maximum number of free senders in the pool.
   * <p>
   * By default this value is set to <code>2</code>,
   * values less than 1 are unauthorized.
   * <p>
   *  This value can be adjusted for all network components by setting the
   * <code>PoolNetwork.nbMaxFreeSender</code> global property or for a particular
   * network by setting \<DomainName\>.nbMaxFreeSender</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int nbMaxFreeSender;

  /**
   * Returns the maximum number of free senders in the pool.
   *
   * @return  the number of free senders in the pool.
   */
  public int getNbMaxFreeSender() {
    return nbMaxFreeSender;
  }

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
   * (whithout any parameter) the component, then we can initialize it with
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
    poolSender = new PoolSender(getName(), nbMaxFreeSender);
  }

  /**
   * Set the properties of the network.
   * Inherited from Network class, can be extended by subclasses.
   */
  public void setProperties() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, domain + ", PoolNetwork.setProperties()");
    super.setProperties();

    try {
      nbMaxCnx = AgentServer.getInteger(domain + ".nbMaxCnx").intValue();
    } catch (Exception exc) {
      try {
        nbMaxCnx = AgentServer.getInteger("PoolNetwork.nbMaxCnx").intValue();
      } catch (Exception exc2) {
        nbMaxCnx = -1;
      }
    }

    nbMaxFreeSender = AgentServer.getInteger("PoolNetwork.nbMaxFreeSender", 2).intValue();
    nbMaxFreeSender = AgentServer.getInteger(domain + ".nbMaxFreeSender", nbMaxFreeSender).intValue();
    if (nbMaxFreeSender < 1)
      nbMaxFreeSender = 2;
    
    IdleTimeout = AgentServer.getLong("PoolNetwork.IdleTimeout", IdleTimeout).longValue();
    IdleTimeout = AgentServer.getLong(domain + ".IdleTimeout", IdleTimeout).longValue();
    if (IdleTimeout < 1000L) IdleTimeout = 5000L;
    
    defaultMaxMessageInFlow = AgentServer.getInteger("PoolNetwork.maxMessageInFlow", defaultMaxMessageInFlow).intValue();
    defaultMaxMessageInFlow = AgentServer.getInteger(domain + ".maxMessageInFlow", defaultMaxMessageInFlow).intValue();
  
    String value = AgentServer.getProperty(domain + ".compressedFlows");
    if (value == null)
      value = AgentServer.getProperty("PoolNetwork.compressedFlows");
    compressedFlows = Boolean.valueOf(value).booleanValue();
    
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append(" setProperties(");
      strbuf.append("nbMaxCnx=").append(nbMaxCnx);
      strbuf.append(", nbMaxFreeSender=").append(nbMaxFreeSender);
      strbuf.append(", IdleTimeout=").append(IdleTimeout);
      strbuf.append(", defaultMaxMessageInFlow=").append(defaultMaxMessageInFlow);
      strbuf.append(", compressedFlows=").append(compressedFlows);
      strbuf.append(')');
      
      logmon.log(BasicLevel.DEBUG, getName() + strbuf.toString());
    }
  }
  
  /**
   * Adds the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void addServer(short id) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append(getName()).append(" before addServer(").append(id).append("):");
      for (int i=0; i<servers.length; i++) {
        strbuf.append("\n\tserver#" + servers[i] + " -> " + sessions[i]);
      }
      logmon.log(BasicLevel.DEBUG, strbuf.toString());
    }

    // First we have to verify that the server is not already defined.
    // Be careful, this test is already done in superclass.
    if (index(id) >= 0) return;

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
        strbuf.append(getName()).append(" after addServer:");
        for (int i=0; i<servers.length; i++) {
          strbuf.append("\n\tserver#" + servers[i] + " -> " + sessions[i]);
        }
        logmon.log(BasicLevel.DEBUG, strbuf.toString());
      }

    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL, getName() + " addServer failed", exc);
    }
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " addServer ok");
  }

  /**
   * Removes the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void delServer(short id) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append(getName()).append(" before delServer(").append(id).append("):");
      for (int i=0; i<servers.length; i++) {
        strbuf.append("\n\tserver#" + servers[i] + " -> " + sessions[i]);
      }
      logmon.log(BasicLevel.DEBUG, strbuf.toString());
    }

    // First we have to verify that the server is defined.
    // Be careful, this test is already done in superclass.
    if (index(id) < 0) return;

    try {
      super.delServer(id);

      NetSession[] newSessions = new NetSession[servers.length];
      int j = 0;
      for (int i=0; i<sessions.length; i++) {
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
        strbuf.append(getName()).append(" after delServer:");
        for (int i=0; i<servers.length; i++) {
          strbuf.append("\n\tserver#" + servers[i] + " -> " + sessions[i]);
        }
        logmon.log(BasicLevel.DEBUG, strbuf.toString());
      }

    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL, getName() + " delServer failed", exc);
    }
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " delServer ok");
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
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", starting");

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

      if (nbMaxCnx != -1) {
        activeSessions = new Vector(nbMaxCnx);
      } else {
        activeSessions = new Vector(servers.length - 1);
      }

      wakeOnConnection.start();
      dispatcher.start();
      watchDog.start();
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    }
    if (logmon.isLoggable(BasicLevel.DEBUG))
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

    if (poolSender != null) poolSender.stop();
    
    // Unregister all session's MBean
    for (int i=0; i<sessions.length; i++) {
      if (sessions[i] != null) {
        try {
          MXWrapper.unregisterMBean("AgentServer",
                                    getMBeanName(sessions[i].sid));
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
        }
        if (sessions[i].isRunning()) {
          sessions[i].stop();
        }
      }
    }
    activeSessions.clear();
    if (logmon.isLoggable(BasicLevel.DEBUG))
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
  final NetSession getSession(short sid) throws UnknownServerException {
    try {
      return sessions[index(sid)];
    } catch (ArrayIndexOutOfBoundsException exc) {
      throw new UnknownServerException("Server#" + sid + " is undefined");
    }
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
   * Gets the maximum number of message sent and non acknowledged.
   * 
   * @param sid the server id of remote server.
   * @return  the maximum number of message sent and non acknowledged.
   */
  final int getMaxMessageInFlow(short sid) {
    return sessions[index(sid)].getMaxMessageInFlow();
  }
  
  /**
   * Sets the maximum number of message sent and non acknowledged.
   * 
   * @param sid the server id of remote server.
   * @param maxMessageInFlow  the maximum number of message sent and non acknowledged.
   */
  void setMaxMessageInFlow(short sid, int maxMessageInFlow) {
    sessions[index(sid)].setMaxMessageInFlow(maxMessageInFlow);
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
   * Returns the number of buffering messages to sent since last reboot.
   * 
   * @param sid the server id of remote server.
   * @return  the number of buffering messages to sent since last reboot.
   */
  final int getNbBufferingMessageToSent(short sid) {
    return sessions[index(sid)].getNbBufferingMessageToSent();
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
                                this.getName() + ", error during waiting connection", exc);
              continue;
            }
            canStop = false;

            setSocketOption(sock);

            Boot boot = readBoot(sock.getInputStream());
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", connection setup from #" + boot.sid);
            getSession(boot.sid).start(sock, boot.boot);
          } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", bad connection setup", exc);
          }
        }
      } catch (IOException exc) {
        this.logmon.log(BasicLevel.ERROR,
                        this.getName() + ", bad socket initialization", exc);
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

          do {
            // Removes all expired messages in qout.
            // AF: This task should be run regularly.
            msg = qout.removeExpired(System.currentTimeMillis());
            if (msg != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                  this.getName() + ", Handles expired message: " + msg);

              if (msg.not.deadNotificationAgentId != null) {
                ExpiredNot expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
                try {
                  AgentServer.getTransaction().begin();
                  Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId,
                      expiredNot));
                  Channel.validate();
                  AgentServer.getTransaction().commit(true);
                } catch (Exception e) {
                  logmon.log(BasicLevel.ERROR,
                      this.getName() + ", cannot post ExpireNotification", e);
                  continue;
                }

              }
              // Suppress the processed notification from message queue and deletes it.
              // It can be done outside of a transaction and committed later (on next handle).
              try {
                msg.delete();
              } catch (IOException exc) {
                logmon.log(BasicLevel.ERROR,
                    this.getName() + ", cannot delete message", exc);
              }
              msg.free();
            }
          } while (msg != null);

          try {
            msg = qout.get();
          } catch (InterruptedException exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                  this.getName() + ", interrupted");
            continue;
          }

          canStop = false;
          if (! running) break;

          // Send the message: Get a sender in the pool and gives the message to transmit.
          // It must be done in an unique operation to avoid the release of the sender between
          // the getSender and the Sender.send methods.
          try {
            poolSender.send(msg);
          } catch (UnknownServerException exc) {
            logmon.log(BasicLevel.ERROR,
                       this.getName() + ", Cannot send message to unknown server", exc);
          }
          
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
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", activated");
            } catch (InterruptedException exc) {
              continue;
            }

            if (! running) break;

            long currentTimeMillis = System.currentTimeMillis();

            try {
              for (int sid=0; sid<sessions.length; sid++) {
                if (sessions[sid] != null) {
                  if ((sessions[sid].sendList.size() > 0) && (! sessions[sid].running)) {
                    // Try to start the session in order to send waiting messages.
                    sessions[sid].start(currentTimeMillis);
                  } else if ((IdleTimeout > 0) && sessions[sid].running &&
                      (currentTimeMillis > (sessions[sid].last + IdleTimeout))) {
                    // The session  is inactive since a long time, may be the connection is down,
                    // try to stop the session.
                    sessions[sid].stop();
                  }
                }
              }
            } catch (Exception exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName(), exc);
            }
          }
        }
      } finally {
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.WARN, this.getName() + ", ended");
        finish();
      }
    }
  }

  class MessageList extends Vector {
    /**
     * Insert the message in the Vector depending of its stamp.
     * This method is used after a connection restart in order to insert messages
     * already sent but not acknowledged in the list of messages to send.
     * 
     * @param waiting An array containing the messages to insert in the list.
     */
    public synchronized void insertMessage(Object waiting[]) {
      modCount++;

      Message msg = null;
      for (int i=0; i<waiting.length; i++) {
        msg = (Message) waiting[i];

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", insert msg (" + msg + ')');

        int idx = 0;
        while (idx < elementCount) {
          Message current = (Message) elementData[idx];
          if ((current.not != null) && (current.stamp > msg.stamp)) break;
          idx++;
        }
        insertElementAt(msg, idx);
      }
    }
    
    /**
     *  Inserts the specified acknowledge at the right place at the beginning of
     * the list.
     * 
     * @param ack The received message to acknowledge.
     */
    public synchronized void insertAck(Message ack) {
      modCount++;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", insert ack (" + ack + ')');

      int idx = 0;
      while (idx < elementCount) {
        Message current = (Message) elementData[idx];
        if ((current.not == null) && (current.stamp == ack.stamp)) {
          // The acknowledge is already in the list
          return;
        }
        if ((current.not != null) || (current.stamp > ack.stamp)) {
          // There is no more acknowledge in the list or the current acknowledge
          // has a bigger stamp
          break;
        }
        idx++;
      }
      insertElementAt(ack, idx);
    }
    
//    /**
//     *  Verify that there is not already an acknowledge corresponding to the
//     * specified message.
//     * 
//     * @param msg The received message to acknowledge.
//     * @return    true if the ack corresponding to the specified message is
//     *            already in the queue, false otherwise.
//     */
//    public synchronized boolean containsAck(Message msg) {
//      if (msg.not != null) return false;
//      Iterator it = this.iterator();
//      while (it.hasNext()) {
//        Message m = (Message) it.next();
//        if (msg.from.equals(m.from) &&
//            msg.to.equals(m.to) &&
//            msg.stamp == m.stamp) {
//          return true;
//        }
//      }
//      return false;
//    }
  }
  
  final class Sender extends Daemon {
    MessageList msgToSend;
    PoolSender poolSender;
    
    NetSession session = null;
    
    /**
     *  The session of this sender is in a connection phase (between PoolSender.reset
     * in NetSession.localStart or NetSession.remoteStart, and PoolSender.restart in
     * NetSession.startEnd).
     *  At the beginning of this phase the waiting messages in NetSession.sendList are
     * inserted in the Sender.msgToSend list, the sender should no longer try to send
     * messages.
     */
    public boolean restart = false;
    /**
     *  This attribute indicates that the daemon is in a waiting phase. When restart is true
     * the daemon will no longer send messages, so the PoolSender.restart method can copy
     * messages from NetSession.sendList to Sender.msgToSend list.
     */
    public boolean waiting = false;
    
    Sender(String name, Logger logmon, PoolSender poolSender) {
      super(name + ".sender");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.poolSender = poolSender;
      msgToSend = new MessageList();
    }

    protected void close() {}

    protected void shutdown() {
      if (this.logmon.isLoggable(BasicLevel.DEBUG))
        this.logmon.log(BasicLevel.DEBUG, this.getName() + ", shutdown.");      
      synchronized (this) {
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG, this.getName() + ", shutdown - 1.");
        // Wake up the daemon so it can die.
        notify();
        try {
          wait(100);
        } catch (InterruptedException e) { }
      }
    }

    synchronized void send(Message msg) {
      if (msg != null) {
        if (msg.not == null) {
          msgToSend.insertAck(msg);
        } else {
          msgToSend.addElement(msg);
        }
      }
     
      if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", send [" + session.sid + "] notify run, msgToSend=" + msgToSend);
      
      // Wake up the daemon so it will send the message.
      notify();
    }
    
    public void run() {
      try {
        while (running) {
          canStop = true;

          synchronized (this) {
            try {
              if (msgToSend.isEmpty() || restart) {
                if (this.logmon.isLoggable(BasicLevel.DEBUG))
                  this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waits message.");
                waiting = true;
                wait();
                if (this.logmon.isLoggable(BasicLevel.DEBUG))
                  this.logmon.log(BasicLevel.DEBUG, this.getName() + ", trace1.");
                if (restart) continue;
                if (this.logmon.isLoggable(BasicLevel.DEBUG))
                  this.logmon.log(BasicLevel.DEBUG, this.getName() + ", trace2.");
              }
            } catch (InterruptedException e) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", trace3.");
              continue;
            } finally {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", trace4.");
              waiting = false;
            }
          }
          
          canStop = false;
          if (! running) break;

          if (session == null) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", Sender[null] notified, msgToSend=" + msgToSend);
            continue;
          }
          
          if (session.sock == null) {
            // If there is no connection between local and destination server,
            // try to make one!
            if (session.server.retry == 0) {
              // Avoid deadlock with a concurrent connection setting
              waiting = true;
              // If server.retry is higher than 0, the watchdog daemon will regularly try
              // to connect, don't block the Dispatcher.
              session.start(System.currentTimeMillis());
            }
//            // Be careful, don't send the current message it will be sent in order with all
//            // other messages from sendList.
//            continue;
          }

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", send(" + msgToSend + ')');
          
          // Send a message 
          if (! msgToSend.isEmpty()) session.send();

          // Release this sender if needed
          waiting = true;
          if (! restart)
            poolSender.release(this);
        }
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR, getName(), exc);
      } finally {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", ends");
        finish();
      }
    }
  }

  final class PoolSender {
    String name;
    
    /** Number of free senders */
    int nbFree = 0;
    /** Array containing free senders */
    Sender free[] = null;
    
    Vector senders = null;
    
    /** Number of allocated senders since staring */
    int count = 0;
    
    PoolSender(String name, int nbMaxFreeSender) {
      this.name = name;
      free = new Sender[nbMaxFreeSender];
      senders = new Vector();
    }
    
    /**
     * Returns a sender associated to the specified remote server.
     * This method is private and should be use only by synchronized methods
     * restart and send.
     * 
     * @param sid the id of remote server.
     * @return    the sender associated to the specified remote server.
     */
    private Sender getSender(short sid) throws UnknownServerException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", getSender(" + sid + ')');
      
      NetSession session = getSession(sid);
      if (session.sender == null) {
        if (nbFree == 0) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", creates a Sender (" + sid + ')');

          Sender sender = new Sender(name + '_' + count++, logmon, this);
          senders.add(sender);
          sender.session = session;
          session.sender = sender;
          sender.start();
        } else {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", alloc a Sender (" + sid + ')');

          nbFree -= 1;
          Sender sender = free[nbFree];
          free[nbFree] = null; /* to let gc do its work */

          sender.session = session;
          session.sender = sender;
        }
      }
      
      if (! session.sender.isRunning()) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, getName() + ", start anew sender (" + sid + ')');
        session.sender.start();
      }
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", getSender returns " + session.sender.getName());

      return session.sender;
    }
    
    synchronized void reset(short sid) throws UnknownServerException {
      if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, getName() + ", reset(" + sid + ')');
      
      Sender sender = getSender(sid);
      // 
      sender.restart = true;
      
      if (! sender.isCurrentThread()) {
        synchronized (sender) {
          while (!sender.waiting) {
            try {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG, getName() + ", waits sender.");
              sender.wait(10);
            } catch (InterruptedException e) {
              continue;
            }
          }
        }
      }
      
      // Inserts all non acknowledged messages in msgTosent list so they can be
      // sent by the sender daemon.
      // TODO: We have also to keep and transmit the stamp of last received
      // message in order to avoid useless transmission from the remote server.
      if (sender.session.sendList.size() > 0) {
        Object[] waiting = sender.session.sendList.toArray();
        sender.session.sendList.clear();
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
              getName() + ", reset - " + waiting.length + " waiting messages");
        sender.msgToSend.insertMessage(waiting);
      }
      if (logmon.isLoggable(BasicLevel.WARN))
        logmon.log(BasicLevel.WARN, getName() + ", reset end (" + sid + ')');
    }
    
    synchronized void restart(short sid) throws UnknownServerException {
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, getName() + ", restart(" + sid + ')');

      Sender sender = getSender(sid);
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
            getName() + ", restart sender=" + sender + ", msgToSend=" + sender.msgToSend);
      sender.restart = false;
      // Wake up the daemon if needed
      if (!sender.msgToSend.isEmpty())
        sender.send(null);
    }
    
    synchronized void send(Message msg) throws UnknownServerException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", send(" + msg + ')');
      
      Sender sender = getSender(msg.getDest());
      sender.send(msg);
    }
    
    synchronized void release(Sender sender) {
      if (sender.msgToSend.isEmpty() && !sender.restart) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", release sender (" + sender.session.sid + ')');

        sender.session.sender = null;
        // If there is enough free element, let the gc get this element. 
        if (nbFree == free.length) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", free sender (" + sender.session.sid + ')');
          sender.session = null;
          // Be careful, this method is called by the sender itself so calling the
          // stop method will cause a deadlock. 
          sender.stop();
          senders.remove(sender);
          return;
        }

        sender.session = null;
        free[nbFree++] = sender;
      }
    }
    
    public void stop() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + "poolSender.stop()");
      
      // Stops all senders associated to a network session.
      for (int i=0; i<sessions.length; i++) {
        if ((sessions[i] != null) && (sessions[i].sender != null)) {
          Sender sender = sessions[i].sender;
          sessions[i].sender = null;
          sender.session = null;
          sender.stop(); 
          
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + "senders["+i+"].stop() done.");
        }
      }
      // Stops free sender kept in the pool.
      while (nbFree > 0) {
        nbFree -= 1;
        free[nbFree].stop(); free[nbFree] = null;
        
      }
    }
  }

  /**
   * The class NetSession controls the connection with a particular server.
   * <p>
   * Each NetSession object is dedicated to the handling of the connection with a
   * server, at a time a session can be either active (with a TCP connection and a
   * receiver thread) or passive. Currently the number of active NetSession is limited,
   * this number can be adjust through the nbMaxCnx property.
   * <p>
   * The message sending is handled by dynamically Sender components.
   */
  final class NetSession implements Runnable {
    // TODO: Use MessageVector component for sendList and msgToSent in order to allow
    // the swapping of waiting messages.
    
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

    /**
     * Sender component needed to send messages.
     * It is kept here so the PoolSender can retrieve allocated sender, this
     * attribute should not be used directly (instead use PoolSender.getSender
     * method).
     */
    Sender sender = null;
    
    /**
     * Maximum number of message sent and non acknowledged.
     */
    int maxMessageInFlow = -1;
    
    /**
     * Gets the maximum number of message sent and non acknowledged.
     * 
     * @return  the maximum number of message sent and non acknowledged.
     */
    int getMaxMessageInFlow() {
      return maxMessageInFlow;
    }
    
    /**
     * Sets the maximum number of message sent and non acknowledged.
     * 
     * @param maxMessageInFlow  the maximum number of message sent and non acknowledged.
     */
    void setMaxMessageInFlow(int maxMessageInFlow) {
      this.maxMessageInFlow = maxMessageInFlow;
    }
    
    NetworkInputStream nis = null;
    NetworkOutputStream nos = null;

    /**
     *  List of all sent messages waiting for an acknowledge from the remote server.
     * These messages are sent anew after a reconnection.
     */
    private MessageVector sendList;

    /**
     *  Time in milliseconds of last use of this session, this attribute is set during
     * connection, then updated at each sending or receiving. It is used to choose unused
     * sessions.
     */
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
      
      maxMessageInFlow = AgentServer.getInteger("PoolNetwork.maxMessageInFlow_" + sid, defaultMaxMessageInFlow).intValue();
      maxMessageInFlow = AgentServer.getInteger(domain + ".maxMessageInFlow_" + sid, maxMessageInFlow).intValue();

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
     * Returns the number of buffering messages to sent since last reboot.
     *
     * @return  the number of buffering messages to sent since last reboot.
     */
    int getNbBufferingMessageToSent() {
//      Sender sender = poolSender.getSender(sid);
      if (sender == null || sender.msgToSend == null)
        return 0;
      return sender.msgToSend.size();
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
        logmon.log(BasicLevel.DEBUG, getName() + ", starting");

      if (((server.retry < WDNbRetryLevel1) && 
           ((server.last + WDRetryPeriod1) < currentTimeMillis)) ||
          ((server.retry < WDNbRetryLevel2) &&
           ((server.last + WDRetryPeriod2) < currentTimeMillis)) ||
          ((server.last + WDRetryPeriod3) < currentTimeMillis)) {
        if (localStart()) {
          // The physical connection is established, starts the corresponding session
          // then sends all waiting messages.
          startEnd();
        } else {
          // TODO: May be we should not set these values if a start is in progress..
          server.last = currentTimeMillis;
          server.retry += 1;
        }

        //  Try to send all waiting messages. As this.sock is no longer null
        // so we must do a copy a waiting messages. New messages will be send
        // directly in send method.
        //  Be careful, in a very limit case a message can be sent 2 times:
        // added in sendList after sock setting and before array copy, il will
        // be transmit in send method and below. However there is no problem,
        // the copy will be discarded on remote node and 2 ack messages will
        // be received on local node.
        try {
          poolSender.restart(sid);
        } catch (UnknownServerException exc) {
          // Should never happen!
        }
      }
    }

    void start(Socket sock, int boot) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", remotely started");

      if (remoteStart(sock, boot)) startEnd();

      //  Try to send all waiting messages. As this.sock is no longer null
      // so we must do a copy a waiting messages. New messages will be send
      // directly in send method.
      //  Be careful, in a very limit case a message can be sent 2 times:
      // added in sendList after sock setting and before array copy, il will
      // be transmit in send method and below. However there is no problem,
      // the copy will be discarded on remote node and 2 ack messages will
      // be received on local node.
      try {
        poolSender.restart(sid);
      } catch (UnknownServerException exc) {
        // Should never happen!
      }
    }

    /**
     *  Its method is called by <a href="#start()">start</a> in order to
     * initiate a connection from the local server. The corresponding code
     * on remote server is the method <a href="#remoteStart()">remoteStart</a>.
     * Its method creates the socket, initiates the network connection, and
     * negotiates with remote server.<p><hr>
     *  Its method can be overridden in order to change the connection protocol
     * (introduces authentication by example, or uses SSL), but must respect
     * some conditions:<ul>
     * <li>send a Boot object after the initialization of object streams (it
     * is waiting by the wakeOnConnection thread),
     * <li>wait for an acknowledge,
     * <li>set the sock, ois and oos attributes at the end if the connection
     * is correct.
     * </ul><p>
     *  In order to override the protocol, we have to implements its method,
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

        try {
          poolSender.reset(sid);
        } catch (UnknownServerException exc) {
          // Should never happen!
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

        try {
          NetworkOutputStream nos = new NetworkOutputStream(sock.getOutputStream());
          NetworkInputStream nis = new NetworkInputStream(sock.getInputStream());
          this.nis = nis;
          this.nos = nos;

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", connection done");
        } catch (Exception exc) {
          throw exc;
        }
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.DEBUG, getName() + ", connection aborted", exc);
        
        // Try it later, may be a a connection is in progress...
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

        poolSender.reset(sid);

        AgentServer.getTransaction().begin();
        testBootTS(sid, boot);
        AgentServer.getTransaction().commit(true);

        try {
          NetworkOutputStream nos = new NetworkOutputStream(sock.getOutputStream());
          NetworkInputStream nis = new NetworkInputStream(sock.getInputStream());
          this.nis = nis;
          this.nos = nos;
        } catch (Exception exc) {
          throw exc;
        }
        
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

      for (int i = activeSessions.size() - 1; i >= 0; i--) {
        NetSession session = (NetSession) activeSessions.get(i);
        if (!session.running) {
          activeSessions.remove(i);
        }
      }
      
      if (nbMaxCnx == -1 || activeSessions.size() < nbMaxCnx) {
        // Insert the current session in the active pool.
        activeSessions.add(this);
      } else {
        // Search the last recently used session in the pool.
        long min = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < activeSessions.size(); i++) {
          NetSession session = (NetSession) activeSessions.get(i);
          if (session.last < min) {
            idx = i;
            min = session.last;
          }
        }
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", Kill session " + activeSessions.get(idx)
              + ",  and insert new one.");
        // Kill chosen session and insert new one
        ((NetSession) activeSessions.get(idx)).stop();
        activeSessions.set(idx, this);
      }
      
      last = System.currentTimeMillis();
      thread = new Thread(this, getName());
      thread.setDaemon(false);

      running = true;
      canStop = true;
      thread.start();

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", connection started");
    }

    /**
     *
     */
    void stop() {
      running = false;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", stops.");

      // Be careful, if this method is called by the thread itself the code below
      // cause a deadlock. Simply set running to false then return so the daemon should
      // terminate.
      if (Thread.currentThread() == thread) return;

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
      // The sock attribute is set to null in the finally clause of NetSession.run(),
      // so it avoids start of session before the thread ending.
      nis = null;
      nos = null;
    }

    /**
     * Removes the acknowledged notification from waiting list.
     * Be careful, messages in sendList are not always in stamp order.
     * This method should not be synchronized, it scans the list from
     * begin to end, and it removes always the first element. Other
     * methods using sendList just adds element at the end.
     * 
     * @param ack         The stamp of acknowledged message to remove.
     * @throws Exception  Error during the message deletion.
     */
    final private void doAck(int ack) throws Exception {
      Message msg = null;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", ack received #" + ack);

      try {
        //  Suppress the acknowledged notification from waiting list,
        // then deletes it.
        msg = sendList.removeMessage(ack);
        
        AgentServer.getTransaction().begin();
        msg.delete();
        msg.free();
        AgentServer.getTransaction().commit(true);
      } catch (NoSuchElementException exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, getName() + ", can't ack, unknown msg#" + ack);
      } catch (Exception e) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, getName() + ", exception during doAck : msg#" + ack);
        throw e;
      }
    }
    
    /**
     * Removes the expired message, if needed an ExpiredNot is sent to the
     * deadNotificationAgentId specified.
     * 
     * @param msg         The message to remove.
     * @throws Exception  
     */
    final private void removeExpired(Message msg) {
      int stamp = msg.getStamp();
      
      try {
        //  Suppress the acknowledged notification from waiting list,
        // and deletes it.
        sendList.removeMessage(stamp);
      } catch (NoSuchElementException exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                   getName() + ", can't removes unknown expired msg#" + stamp);
      }
      
      try {
        AgentServer.getTransaction().begin();

        ExpiredNot expiredNot = null;
        if (msg.not.deadNotificationAgentId != null) {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG,
                       getName() + ": forward expired notification " + msg.from + ", " + msg.not + " to " + msg.not.deadNotificationAgentId);
          }
          expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
        } else {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG,
                       getName() + ": removes expired notification " + msg.from + ", " + msg.not);
          }
        }
        
        if (expiredNot != null) {
          Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot));
          Channel.validate();
        }
        
        msg.delete();
        msg.free();
        
        AgentServer.getTransaction().commit(true);
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", exception in removeExpired msg#" + stamp, exc);
      }
    }

    /**
     * Try to send the first message of the list to the corresponding remote server.
     * Be careful, its method should not be synchronized (in that case, the overall
     * synchronization of the connection -method start- can dead-lock).
     * 
     * @param msg The message to send.
     */
    final void send() {
      if (sock != null) {
        Message msg = null;
        long currentTimeMillis = System.currentTimeMillis();

        if (maxMessageInFlow > 0) {
          // The flow control is activated, verify the number of messages waiting
          // for an acknowledge.
          msg = (Message) sender.msgToSend.firstElement();
          while ((sock != null) && ((msg.not != null)) && (sendList.size() > maxMessageInFlow)) {
            // Waits for acknowledges from remote server.
            try {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG, getName() + ", transmit waits -> sendList.size=" + sendList.size());
              Thread.sleep(100L);
              
              // If the connection is broken don't wait anymore.
              if (sock == null) return;
            } catch (InterruptedException exc) {}
            // Get anew the first message of the list, it may be an outgoing acknowledge. 
            msg = (Message) sender.msgToSend.firstElement();
          }
        }

        if (sock == null) return;
        
        msg = (Message) sender.msgToSend.remove(0);

        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          if (msg.not != null) {
            logmon.log(BasicLevel.DEBUG, getName() + ", send msg#" + msg.getStamp());
          } else {
            logmon.log(BasicLevel.DEBUG,  getName() + ", send ack#" + msg.getStamp());
          }
        }

        if (msg.not != null) {
          nbMessageSent += 1;
          sendList.addElement(msg);

          if ((msg.not.expiration > 0) && (msg.not.expiration < currentTimeMillis)) {
            removeExpired(msg);
            return;
          }
        } else {
          nbAckSent += 1;
        }

        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          if (msg.not != null)
            logmon.log(BasicLevel.DEBUG, getName() + ", transmit(msg#" + msg.stamp + ", " + currentTimeMillis + ')');
          else
            logmon.log(BasicLevel.DEBUG, getName() + ", transmit(ack#" + msg.stamp + ", " + currentTimeMillis + ')');
        }

        // Writes the message on the corresponding connection.
        last = currentTimeMillis;
        try {
          nos.writeMessage(msg, currentTimeMillis);
        } catch (IOException exc) {
          if (logmon.isLoggable(BasicLevel.WARN))
            logmon.log(BasicLevel.WARN,
                     getName() + ", exception in sending message " + msg, exc);
          close();
        } catch (NullPointerException exc) {
          // The stream is closed
          if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", exception in sending message " + msg);
        }
      } else {
        Message msg = (Message) sender.msgToSend.remove(0);

        long currentTimeMillis = System.currentTimeMillis();

        // The connection is down just insert the message in sendList
        if (msg.not != null) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", move msg#" + msg.getStamp() + " to sendList");
          
          sendList.addElement(msg);

          if ((msg.not.expiration > 0) && (msg.not.expiration < currentTimeMillis)) {
            removeExpired(msg);
            return;
          }
        }
      }
    }

    final private void ack(int stamp) throws Exception {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", set ack msg#" + stamp);

      Message ack = Message.alloc(AgentId.localId,
                                  AgentId.localId(server.sid),
                                  null);
      ack.source = AgentServer.getServerId();
      ack.dest = AgentServer.getServerDesc(server.sid).gateway;
      ack.stamp = stamp;

      qout.push(ack);
      qout.validate();
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
          last = lastReceived;
          
          //  Keep message stamp in order to acknowledge it (be careful,
          // the message get a new stamp when it is delivered).
          int stamp = msg.getStamp();
          if (msg.not != null) {
            nbMessageReceived += 1;
            // Deliver the message
            deliver(msg);
            // then send an acknowledge
            ack(stamp);
          } else {
            // removes the acknowledged message.
            doAck(stamp);
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
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", ends");

        running = false;
        close();
        // Avoid a session starting before the end of this thread.
        sock = null;
      }
    }
    
    /**
     * Class used to read messages through a stream.
     */
    final class NetworkInputStream extends BufferedMessageInputStream {
      
      NetworkInputStream(InputStream is) throws IOException {
        super();
        this.in = is;
        this.compressedFlows = getCompressedFlows();
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
      OutputStream os = null;
      
      NetworkOutputStream(OutputStream os) throws IOException {
        super();
        this.out = os;
        this.compressedFlows = getCompressedFlows();
      }

      protected void writeHeader() throws IOException {}
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
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", readBoot from #" + boot.sid + " -> " + boot.boot);

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
   * Gets the maximum number of message sent and non acknowledged.
   * 
   * @return  the maximum number of message sent and non acknowledged.
   */
  public int getMaxMessageInFlow() {
    return network.getMaxMessageInFlow(sid);
  }
  
  /**
   * Sets the maximum number of message sent and non acknowledged.
   * 
   * @param maxMessageInFlow  the maximum number of message sent and non acknowledged.
   */
  public void setMaxMessageInFlow(int maxMessageInFlow) {
    network.setMaxMessageInFlow(sid, maxMessageInFlow);
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
  
  
  /**
   * Returns the number of buffering messages to sent since last reboot.
   *
   * @return  the number of buffering messages to sent since last reboot.
   */
  public int getNbBufferingMessageToSent() {
    return network.getNbBufferingMessageToSent(sid);
  }
}