/*
 * Copyright (C) 2003 SCALAGENT
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
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  <code>SingleCnxNetwork</code> is a simple implementation of
 * <code>StreamNetwork</code> class with a single connection at
 * a time.
 */
class SimpleNetwork extends Network {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: SimpleNetwork.java,v 1.1 2003-03-19 15:16:06 fmaistre Exp $";

  /**
   * FIFO list of all messages to be sent by the watch-dog thead.
   */
  Vector sendList;

  /**
   * Creates a new network component.
   */
  SimpleNetwork() {
    super();
    waiting = new Vector();
  }

  // -----+----- -----+----- -----+----- -----+----- -----+----- -----+----- 
  // start of code from StreamNetwork class.

  /**
   * Numbers of attempt to bind the server's socket before aborting.
   */
  final static int CnxRetry = 10;

  /**
   *  This method creates and returns a socket connected to a ServerSocket at
   * the specified network address and port. It may be overloaded in subclass,
   * in order to create particular subclasses of sockets.
   * <p>
   *  Due to polymorphism of both factories and sockets, different kinds of
   * sockets can be used by the same application code. The sockets returned
   * to the application can be subclasses of <a href="java.net.Socket">
   * Socket</a>, so that they can directly expose new APIs for features such
   * as compression, security, or firewall tunneling.
   *
   * @param host	the server host.
   * @param port	the server port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createSocket(InetAddress host, int port) throws IOException {
    if (host == null)
      throw new UnknownHostException();
    return new Socket(host, port);
  }

  /**
   *  This method creates and returns a server socket which uses all network
   * interfaces on the host, and is bound to the specified port. It may be
   * overloaded in subclass, in order to create particular subclasses of
   * server sockets.
   *
   * @param port	the port to listen to.
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket() throws IOException {
    for (int i=0; ; i++) {
      try {
        return new ServerSocket(port);
      } catch (BindException exc) {
        if (i > CnxRetry) throw exc;
        try {
          Thread.currentThread().sleep(i * 250);
        } catch (InterruptedException e) {}
      }
    }
  }

  /**
   *  Configures this socket using the socket options established for this
   * factory. It may be overloaded in subclass, in order to handle particular
   * subclasses of sockets
   *
   * @param Socket	the socket.
   *
   * @exception IOException	for networking errors
   */ 
  static void setSocketOption(Socket sock) throws SocketException {
    // Don't use TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(true);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(0);
    // Set Linger-on-Close timeout.
    sock.setSoLinger(true, 60);
  }

  /**
   *  Returns an <code>ObjectInputStream</code> for this socket. This
   * method may be overloaded in subclass, transforming the data along
   * the way or providing additional functionality (ie cyphering).
   *
   * @param sock	the socket.
   * @return		an input stream for reading object from this socket.
   *
   * @exception	IOException	if an I/O error occurs when creating the
   *				input stream.
   */
  static ObjectInputStream
  getInputStream(Socket sock) throws IOException {
    return new ObjectInputStream(sock.getInputStream());
  }

  /**
   *  Returns an <code>ObjectOutputStream</code> for this socket. This
   * method may be overloaded in subclass, transforming the data along
   * the way or providing additional functionality (ie cyphering).
   *
   * @param sock	the socket.
   * @return		an output stream for writing object to this socket.
   *
   * @exception	IOException	if an I/O error occurs when creating the
   *				output stream.
   */
  static ObjectOutputStream
  getOutputStream(Socket sock) throws IOException {
    return new ObjectOutputStream(sock.getOutputStream());
  }

  // End of code from StreamNetwork class.
  // -----+----- -----+----- -----+----- -----+----- -----+----- -----+----- 

  // Adaptation of caode from CausalNetwork in order to ensure FIFO
  // ordering between nodes

  /** Logical timestamp information for messages in domain, stamp[0] for
   * messages sent, and stamp[1] for messages received.
   */
  protected int[][] stamp;
  /** True if the timestamp is modified since last save. */
  protected boolean modified = false;
  /**
   * The waiting list: it contains all messages that waiting to be delivered.
   */
  Vector waiting;

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  public final void insert(Message msg) {
    if (msg.update.getFromId() == AgentServer.getServerId()) {
      // The update has been locally generated, the message is ready to
      // deliver, we have to insert it in the queue.
      qout.insert(msg);
    } else {
      // The update has been generated on a remote server. If the message
      // don't have a local update, It is waiting to be delivered. So we
      // have to insert it in waiting list.
      addRecvMessage(msg);
    }
  }

  /**
   * Adds a message in waiting list. This method is used to retain messages
   * that cannot be delivered now. Each message in this list is evaluated
   * (see <code>deliver()</code>) each time a new message succeed.
   * <p><hr>
   * This method is also used to fill the waiting list during initialisation.
   *
   * @param msg		the message
   */
  final void addRecvMessage(Message msg) {
    waiting.addElement(msg);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    if (modified) {
      AgentServer.transaction.save(stamp, getName());
      modified = false;
    }
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    stamp = (int [][]) AgentServer.transaction.load(getName());
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
    this.name = "AgentServer#" + AgentServer.getServerId() + '.' + name;
    this.port = port;

    // Get the logging monitor from current server MonologLoggerFactory
    // Be careful, logmon is initialized from name and not this.name !!
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized");

    // Sorts the array of server ids into ascending numerical order.
    Arrays.sort(servers);
    // then get the logical clock.
    restore();
    if (stamp ==  null) {
      stamp = new int[2][servers.length];
      this.servers = servers;
      // Save the servers configuration and the logical time stamp.
      AgentServer.transaction.save(this.servers, name + "Servers");
      save();
    } else {
      // Join with the new domain configuration:
      this.servers = (short[]) AgentServer.transaction.load(name + "Servers");
      if (!Arrays.equals(this.servers, servers)) {
        logmon.log(BasicLevel.WARN,
                   "MatrixClock." + name + ", updates configuration");
	// TODO: Insert or suppress corresponding elements in matrix...
	throw new IOException("Bad configuration");
      }
    }
  }

  /**
   * Try to deliver the received message to the right consumer.
   *
   * @param msg		the message.
   */
  public void deliver(Message msg) throws Exception {
    // Get real from serverId.
    short from = msg.update.getFromId();
    int fromIdx = index(from);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", recv msg#" + msg.update.stamp +
                 " from " + msg.from +
                 " to " + msg.to +
                 " by " + from);

    AgentServer.getServerDesc(from).active = true;
    AgentServer.getServerDesc(from).retry = 0;

    if (msg.update.stamp == (stamp[1][fromIdx] +1)) {
      stamp[1][fromIdx] += 1;
      modified = true;
      // Deliver the message then try to deliver alls waiting message.
      AgentServer.transaction.begin();
      // Allocate a local time to the message to order it in
      // local queue, and save it.
      Channel.post(msg);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", deliver msg#" + msg.update.stamp);
      scanlist:
      while (true) {
	for (int i=0; i<waiting.size(); i++) {
	  Message tmpMsg = (Message) waiting.elementAt(i);
	  if ((tmpMsg.update.getFromId() == from) &&
              (tmpMsg.update.stamp == (stamp[1][fromIdx] +1))) {
            stamp[1][fromIdx] += 1;
	    // Be Careful, changing the stamp imply the filename
	    // change !! So we have to delete the old file.
	    tmpMsg.delete();
	    //  Deliver the message, then delete it from list.
	    Channel.post(tmpMsg);
	    waiting.removeElementAt(i);

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ",	 deliver msg#" + tmpMsg.update.stamp);

	    // logical time has changed we have to rescan the list.
	    continue scanlist;
	  }
	}
	//  We have scan the entire list without deliver any message
	// so we leave the loop.
	break scanlist;
      }
      Channel.save();
      AgentServer.transaction.commit();
      // then commit and validate the message.
      Channel.validate();
      AgentServer.transaction.release();
    } else if (msg.update.stamp > (stamp[1][fromIdx] +1)) {
      AgentServer.transaction.begin();
      // Insert in a waiting list.
      msg.save();
      waiting.addElement(msg);
      AgentServer.transaction.commit();
      AgentServer.transaction.release();
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", block msg#" + msg.update.stamp);
    } else {
//    it's an already delivered message, we have just to re-send an
//    aknowledge (see below).
    }
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws IOException {
    short to = AgentServer.servers[msg.to.to].gateway;
    int toIdx = index(to);

    modified = true;
    // Allocates a new timestamp. Be careful, if the message needs to be
    // routed we have to use the next destination in timestamp generation.
    msg.update = new Update(AgentServer.getServerId(),
			    AgentServer.servers[msg.to.to].gateway,
			    ++stamp[0][toIdx]);
    // Saves the message.
    msg.save();
    // Push it in "ready to deliver" queue.
    qout.push(msg);
  }

  /** Input component */
  NetServerIn netServerIn = null;
  /** Output component */
  NetServerOut netServerOut = null;
  /** Watch-dog component */
  WatchDog watchDog = null;

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws IOException {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {
      if (isRunning())
	throw new IOException("Consumer already running");

      sendList = new Vector();
    
      netServerIn = new NetServerIn(getName(), logmon);
      netServerOut = new NetServerOut(getName(), logmon);
      watchDog = new WatchDog(getName(), logmon);

      netServerIn.start();
      netServerOut.start();
      watchDog.start();
    } catch (IOException exc) {
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
    if (netServerIn != null) netServerIn.stop();
    if (netServerOut != null) netServerOut.stop();
    if (watchDog != null) watchDog.stop();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning() &&
	(netServerOut != null) && netServerOut.isRunning() &&
	(watchDog != null) && watchDog.isRunning())
      return true;
    else
      return false;
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
    if (netServerIn != null)
      strbuf.append(netServerIn.toString()).append("\n\t");
    if (netServerOut != null)
      strbuf.append(netServerOut.toString()).append("\n\t");
    if (watchDog != null)
      strbuf.append(watchDog.toString()).append("\n");

    return strbuf.toString();
  }

  final class NetServerOut extends Daemon {
    NetServerOut(String name, Logger logmon) {
      super(name + ".NetServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }
    protected void close() {}

    protected void shutdown() {}

    public void run() {
      int ret;
      Message msg = null;
      short msgto;
      ServerDesc server = null;

      try {
	while (running) {
	  try {
	    canStop = true;

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");

	    try {
	      msg = qout.get();
	    } catch (InterruptedException exc) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", interrupted");
	      continue;
	    }
	    canStop = false;

	    msgto = msg.update.getToId();

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", try to send message -> " +
                         msg + "/" + msgto);
            // Can throw an UnknownServerException...
            server = AgentServer.getServerDesc(msgto);

	    if (! server.active) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", AgentServer#" + msgto + " is down");
	      throw new ConnectException("AgentServer#" + msgto + " is down");
	    }
	  
	    // Open the connection.
	    Socket socket = null;
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", try to connect");
              socket = createSocket(server.getAddr(), server.port);
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.WARN,
                              this.getName() + ", connection refused", exc);
              server.active = false;
              server.last = System.currentTimeMillis();
              server.retry += 1;
              throw exc;
            }
	    setSocketOption(socket);

	    if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", write message");
	    // Send the message,
	    ObjectOutputStream oos = getOutputStream(socket);
	    oos.writeObject(msg);

	    if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", wait ack");
	    // and wait the acknowledge.
	    InputStream is = socket.getInputStream();
	    if ((ret = is.read()) == -1)
	      throw new ConnectException("Connection broken");

	    if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", receive ack");
	
	    try {
	      oos.close();
	    } catch (IOException exc) {}
	    try {
	      is.close();
	    } catch (IOException exc) {}
	    try {
	      socket.close();
	    } catch (IOException exc) {}
	  } catch (IOException exc) {
            this.logmon.log(BasicLevel.WARN,
                       this.getName() + ", move msg in watchdog list", exc);
	    //  There is a connection problem, put the message in a
	    // waiting list.
	    sendList.addElement(msg);
	    qout.pop();
	    continue;
	  } catch (UnknownServerException exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", can't send message: " + msg,
                            exc);
            // Remove the message (see below), may be we have to post an
            // error notification to sender.
          }

	  try {
	    AgentServer.transaction.begin();
	    //  Suppress the processed notification from message queue,
	    // and deletes it.
	    qout.pop();
	    msg.delete();
	    AgentServer.transaction.commit();
	    AgentServer.transaction.release();
	  } catch (Exception exc) {
	    this.logmon.log(BasicLevel.FATAL,
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

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;

// AF: To be deleted
//     WatchDog watchDog = null;

    NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn");
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

    public void run() {
      Socket socket = null;
      OutputStream os = null;
      ObjectInputStream ois = null;

      try {
	while (running) {
	  try {
	    canStop = true;

	    // Get the connection
	    try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", waiting connection");
	      socket = listen.accept();
	    } catch (IOException exc) {
	      continue;
	    }
	    canStop = false;

	    setSocketOption(socket);
// 	    socket.setSoLinger(true, 1000);

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");

	    // Read the message,
	    os = socket.getOutputStream();
	    ois = getInputStream(socket);

	    Object obj = ois.readObject();

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", msg received");

	    if (obj instanceof Message) {
	      deliver((Message) obj);
	    } else if (obj instanceof Boot) {
	      Boot boot = (Boot) obj;
	      // It's a valid boot message, set the server on.
	      AgentServer.getServerDesc(boot.sid).active = true;
	      AgentServer.getServerDesc(boot.sid).retry = 0;

	      if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", connection setup from #" +
                           boot.sid);

	      // resend all waiting messages.
	      wakeup();
	    } else {
	      // TODO: ?
	    }

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", send ack");

	    // then send the acknowledge.
	    os.write((byte) 0);
	    os.flush();	// nop !
	  } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR, ", exited", exc);
	  } finally {
	    try {
	      os.close();
	    } catch (Exception exc) {}
	    os = null;
	    try {
	      ois.close();
	    } catch (Exception exc) {}
	    ois = null;
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
  }


  final class WatchDog extends Daemon {
    /** Use to synchronize thread */
    private Object lock;

    WatchDog(String name, Logger logmon) {
      super(name + ".WatchDog");
      lock = new Object();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {}

    protected void shutdown() {
      wakeup();
    }

    /**
     *  Use to wake up the watch-dog thread after a message from a
     * stopped node (see NetServerIn).
     */
    void wakeup() {
      synchronized (lock) {
	lock.notify();
      }
    }
  
    /**
     * Use to clean the sendList of all messages to the dead node.
     * @param	dead - the unique id. of dead server.
     */
    void clean(short dead) {
      Message msg = null;

      // TODO: Be careful, to the route algorithm!

      synchronized (lock) {
	for (int i=0; i<sendList.size(); i++) {
	  msg = (Message) sendList.elementAt(i);
	  if (msg.to.to == dead) {
	    sendList.removeElementAt(i);
	  }
	}
      }
    }

    public void run() {
      int ret;
      Message msg = null;
      short msgto;
      ServerDesc server = null;
      
      try {
        synchronized (lock) {
	  while (running) {
	    try {
	      if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", waiting...");
	      lock.wait(WDActivationPeriod);
	    } catch (InterruptedException exc) {
	      continue;
	    }
	    
	    if (! running) break;
	    long currentTimeMillis = System.currentTimeMillis();

	    for (int i=0; i<sendList.size(); i++) {
	      msg = (Message) sendList.elementAt(i);
	      msgto = msg.update.getToId();

	      if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() +
                           ", check msg#" + msg.update.stamp +
			    " from " + msg.from +
			    " to " + msg.to);

              try {
                server = AgentServer.getServerDesc(msgto);

                if ((server.active) ||
                    ((server.retry < WDNbRetryLevel1) && 
                     ((server.last + WDRetryPeriod1) < currentTimeMillis)) ||
                    ((server.retry < WDNbRetryLevel2) &&
                     ((server.last + WDRetryPeriod2) < currentTimeMillis)) ||
                    ((server.last + WDRetryPeriod3) < currentTimeMillis)) {
                  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG,
                                    this.getName() +
                                    ", send msg#" + msg.update.stamp);

                  server.last = currentTimeMillis;

		  // Open the connection.
		  Socket socket = null;
                  try {
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", try to connect");
                    socket = createSocket(server.getAddr(), server.port);
                    server.active = true;
                    server.retry = 0;
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", connected");
                  } catch (IOException exc) {
                    this.logmon.log(BasicLevel.WARN,
                                    this.getName() + ", connection refused",
                                    exc);
                    throw exc;
		  }
		  setSocketOption(socket);

		  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG,
                                    this.getName() + ", write message");

		  // Send the message,
		  ObjectOutputStream oos = getOutputStream(socket);
		  oos.writeObject(msg);

		  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG,
                                    this.getName() + ", wait ack");

		  // and wait the acknowledge.
		  InputStream is = socket.getInputStream();
		  if ((ret = is.read()) == -1)
		    throw new ConnectException("Connection broken");

		  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG,
                               this.getName() + ", receive ack");
	    
		  try {
		    oos.close();
		  } catch (IOException exc) {}
		  try {
		    is.close();
		  } catch (IOException exc) {}
		  try {
		    socket.close();
		  } catch (IOException exc) {}

                  try {
                    AgentServer.transaction.begin();
                    //  Deletes the processed notification
                    sendList.removeElementAt(i); i--;
                    msg.delete();
                    AgentServer.transaction.commit();
                    AgentServer.transaction.release();
                  } catch (Exception exc) {
                    this.logmon.log(BasicLevel.FATAL,
                                    this.getName() + ", unrecoverable exception",
                                    exc);
                    //  There is an unrecoverable exception during the
                    // transaction we must exit from server.
                    AgentServer.stop();
                  }
                }
              } catch (SocketException exc) {
                if (this.logmon.isLoggable(BasicLevel.WARN))
                  this.logmon.log(BasicLevel.WARN,
                                  this.getName() + ", let msg in watchdog list",
                                  exc);
                server.active = false;
                server.last = System.currentTimeMillis();
                server.retry += 1;
                //  There is a connection problem, let the message in the
                // waiting list.
              } catch (UnknownServerException exc) {
                this.logmon.log(BasicLevel.ERROR,
                                this.getName() + ", can't send message: " + msg,
                                exc);
                // Remove the message, may be we have to post an error
                // notification to sender.
                try {
                  AgentServer.transaction.begin();
                  // Deletes the processed notification
                  sendList.removeElementAt(i); i--;
                  msg.delete();
                  AgentServer.transaction.commit();
                  AgentServer.transaction.release();
                } catch (Exception exc2) {
                  this.logmon.log(BasicLevel.FATAL,
                                  this.getName() + ", unrecoverable exception",
                                  exc2);
                  //  There is an unrecoverable exception during the
                  // transaction we must exit from server.
                  AgentServer.stop();
                }
              } catch (Exception exc) {
                this.logmon.log(BasicLevel.ERROR,
                                this.getName() + ", error", exc);
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
