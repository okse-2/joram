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
 * <code>HttpNetwork</code> is a simple implementation of <code>Network</code>
 * based on HTTP 1.1 protocol.
 */
class HttpNetwork extends Network {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: HttpNetwork.java,v 1.1 2003-03-19 15:16:06 fmaistre Exp $";

  InetAddress proxy = null;
  String proxyhost = null;
  int proxyport = 0;

  /**
   * Time between two activation of NetServerOut, it matchs to the time between
   * two requests from the client to the server when there is no message to
   * transmit from client to server.
   */
  protected long activationPeriod = 10000L;
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
   * Creates a new network component.
   */
  HttpNetwork() {
    super();
    waiting = new Vector();
  }

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

    activationPeriod = Long.getLong("ActivationPeriod",
                                    WDActivationPeriod).longValue();
    activationPeriod = Long.getLong(name + ".ActivationPeriod",
                                    activationPeriod).longValue();
    
    proxyhost = System.getProperty("proxyhost");
    proxyhost = System.getProperty(name + ".proxyhost", proxyhost);
    if (proxyhost != null) {
      proxyport = Integer.getInteger("proxyport", 8080).intValue();
      proxyport = Integer.getInteger(name + ".proxyport", proxyport).intValue();
      proxy = InetAddress.getByName(proxyhost);
    }

    // Get the logging monitor from current server MonologLoggerFactory
    // Be careful, logmon is initialized from name and not this.name !!
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized:" + port);

    // Sorts the array of server ids into ascending numerical order.
    if (servers.length == 2) {
      Arrays.sort(servers);
    } else {
      throw new Exception("Bad configuration: " + 
                          "needs exactly 2 servers in Http domain.");
    }
    
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

  /** Daemon component */
  Daemon dmon = null;

  ServerDesc server = null;

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {
      if (isRunning())
	throw new IOException("Consumer already running");

      if (servers[0] == AgentServer.getServerId())
        server = AgentServer.getServerDesc(servers[1]);
      else 
        server = AgentServer.getServerDesc(servers[0]);

      if (port != 0) {
        dmon = new NetServerIn(getName(), logmon);
      } else {
        dmon = new NetServerOut(getName(), logmon);
      }
      dmon.start();
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
//     if (dmon != null) dmon.wakeup();
  }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (dmon != null) dmon.stop();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((dmon != null) && dmon.isRunning())
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
    if (dmon != null)
      strbuf.append(dmon.toString()).append("\n\t");

    return strbuf.toString();
  }

  /**
   * Numbers of attempt to bind the server's socket before aborting.
   */
  final static int CnxRetry = 3;

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
          Thread.currentThread().sleep(i * 200);
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

  byte[] buf = new byte[120];

  protected String readLine(InputStream is) throws IOException {
    int i = 0;
    while ((buf[i++] = (byte) is.read()) != -1) {
      if ((buf[i-1] == '\n') && (buf[i-2] == '\r')) {
	i -= 2;
	break;
      }
    }

    if (i > 0) {
//       logmon.log(BasicLevel.DEBUG,
//                  this.getName() + ": " + i + ", " + new String(buf, 0, i));
      return new String(buf, 0, i);
    }
    return null;
  }
  
  protected void sendRequest(Message msg, OutputStream os) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    byte[] buf = null;

    if (msg != null) {
      strbuf.append("PUT ");
      if (proxy != null) {
        strbuf.append("http://").append(server.getHostname()).append(':').append(server.port);
      }
      strbuf.append("/msg#").append(msg.update.stamp).append(" HTTP/1.1");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(msg);
      oos.flush();
      buf = bos.toByteArray();
    } else {
      strbuf.append("GET ");
      if (proxy != null) {
        strbuf.append("http://").append(server.getHostname()).append(':').append(server.port);
        strbuf.append("/msg HTTP/1.1");
      }
    }

    if (proxy != null) {
      strbuf.append("\r\nHost: ").append(server.getHostname());
    }
    strbuf.append("\r\n" +
                  "User-Agent: ScalAgent 1.0\r\n" +
                  "Accept: image/jpeg;q=0.2\r\n" +
                  "Accept-Language: fr, en-us;q=0.50\r\n" +
                  "Accept-Encoding: gzip;q=0.9\r\n" +
                  "Accept-Charset: ISO-8859-1, utf-8;q=0.66\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Keep-Alive: 300\r\n" +
                  "Connection: keep-alive\r\n");
    if (buf != null) {
      strbuf.append("Content-Length: ").append(buf.length);
      strbuf.append("\r\n" +
                    "Content-Type: image/jpeg\r\n");
    }
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (buf != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, name + ", writes:" + buf.length);
      os.write(buf);
    }
    os.flush();
  }

  protected Message getRequest(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if (line.startsWith("GET ")) {
    } else if (line.startsWith("PUT ")) {
    } else {
      throw new Exception();
    }

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
	length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (length != 0) {
      int nb = -1;
      int offset = 0;
      byte[] buf = new byte[length];
      do {
        nb = is.read(buf, offset, length-offset);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", reads:" + nb);
        if (nb > 0) offset += nb;
      } while ((nb != -1) && (offset != length));
      
      if (offset != buf.length) {
//       if (is.read(buf) != buf.length) {
	throw new Exception();
      } else {
	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	Message msg = (Message) ois.readObject();
	return msg;
      }
    }
    
    return null;
  }

  protected void sendReply(Message msg, OutputStream os) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    byte[] buf = null;

    if (msg != null) {
      strbuf.append("HTTP/1.1 200 OK\r\n");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(msg);
      oos.flush();
      buf = bos.toByteArray();
    } else {
      strbuf.append("HTTP/1.1 204 No Content\r\n");
    }

    strbuf.append("Date: ").append("Fri, 21 Feb 2003 14:30:51 GMT");
    strbuf.append("\r\n" +
                  "Server: ScalAgent 1.0\r\n" +
                  "Last-Modified: ").append("Wed, 19 Apr 2000 08:16:28 GMT");
    strbuf.append("\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Accept-Ranges: bytes\r\n" +
                  "Keep-Alive: timeout=15, max=100\r\n" +
                  "Connection: Keep-Alive\r\n");
    if (buf != null) {
      strbuf.append("Content-Length: ").append(buf.length);
      strbuf.append("\r\n" +
                    "Content-Type: image/gif\r\n");
    }
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (buf != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, name + ", writes:" + buf.length);
      os.write(buf);
    }
    os.flush();
  }

  protected Message getReply(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if (line.equals("HTTP/1.1 200 OK")) {
    } else if (line.equals("HTTP/1.1 204 No Content")) {
    } else {
      throw new Exception();
    }

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
	length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (length != 0) {
      int nb = -1;
      int offset = 0;
      byte[] buf = new byte[length];
      do {
        nb = is.read(buf, offset, length-offset);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", reads:" + nb);
        if (nb > 0) offset += nb;
      } while ((nb != -1) && (offset != length));

      if (offset != buf.length) {
//       if (is.read(buf) != buf.length) {
	throw new Exception();
      } else {
	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	Message msg = (Message) ois.readObject();
	return msg;
      }
    }

    return null;
  }

  final class NetServerOut extends Daemon {
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;

    NetServerOut(String name, Logger logmon) {
      super(name + ".NetServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {
      if (socket != null) {
        try {
          os.close();
        } catch (Exception exc) {}
        try {
          is.close();
        } catch (Exception exc) {}
        try {
          socket.close();
        } catch (Exception exc) {}
      }
    }

    protected void shutdown() {
      thread.interrupt();
      close();
    }

    public void run() {
      Message msgin = null;
      Message msgout = null;

      try {
	while (running) {
	  try {
	    canStop = true;

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");

	    try {
	      msgout = qout.get(activationPeriod);
	    } catch (InterruptedException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", interrupted");
	    }

            // Open the connection.
            socket = null;
            try {
              if (proxy == null) {
                socket = createSocket(server.getAddr(), server.port);
              } else {
                socket = createSocket(proxy, proxyport);
              }
            } catch (IOException exc) {
              logmon.log(BasicLevel.WARN,
                         this.getName() + ", connection refused", exc);
              throw exc;
            }
            setSocketOption(socket);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");

            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendRequest: " + msgout);

              sendRequest(msgout, os);

              msgin = getReply(is);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getReply: " + msgin);

              if (msgout != null) {
                AgentServer.transaction.begin();
                //  Suppress the processed notification from message queue,
                // and deletes it.
                qout.pop();
                msgout.delete();
                AgentServer.transaction.commit();
                AgentServer.transaction.release();
              }

              if (msgin != null) {
                canStop = false;
                deliver(msgin);
                canStop = true;
              }

              // Get next message to send if any
              msgout = qout.get(0);
            } while (running && ((msgout != null) || (msgin != null)));
            logmon.log(BasicLevel.DEBUG, ", close connection");
          } catch (Exception exc) {
            logmon.log(BasicLevel.WARN,
                       this.getName() + ", ", exc);
          } finally {
            try {
              os.close();
            } catch (Exception exc) {}
            os = null;
            try {
              is.close();
            } catch (Exception exc) {}
            is = null;
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
        logmon.log(BasicLevel.WARN, ", exited");
	finish();
      }
    }
  }

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;
    
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;

    NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn");
      listen = createServerSocket();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {
      if (socket != null) {
        try {
          os.close();
        } catch (Exception exc) {}
        try {
          is.close();
        } catch (Exception exc) {}
        try {
          socket.close();
        } catch (Exception exc) {}
      }
      try {
	listen.close();
      } catch (Exception exc) {}
    }

    protected void shutdown() {
      close();
    }

    public void run() {
      Message msgin = null;
      Message msgout= null;

      try {
	while (running) {
          canStop = true;

          // Get the connection
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", waiting connection");
            socket = listen.accept();


            setSocketOption(socket);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");
            
            msgin = getRequest(is);
            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getRequest: " + msgin);
              
              if (msgin != null) {
                canStop = false;
                deliver(msgin);
                canStop = true;
              }
                
              msgout = qout.get(0);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendReply: " + msgout);

              sendReply(msgout, os);
              
              msgin = getRequest(is);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getRequest: " + msgin);

              if (msgout != null) {
                // Be careful, actually msg should be null !!

                canStop = false;

                AgentServer.transaction.begin();
                //  Deletes the processed notification
                qout.pop();
                msgout.delete();
                AgentServer.transaction.commit();
                AgentServer.transaction.release();
              }

            } while (running);
          } catch (Exception exc) {
            logmon.log(BasicLevel.WARN, ", connection closed", exc);
          } finally {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, ", connection ends");
            try {
              os.close();
            } catch (Exception exc) {}
            os = null;
            try {
              is.close();
            } catch (Exception exc) {}
            is = null;
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
        logmon.log(BasicLevel.WARN, ", exited");
        finish();
      }
    }
  }
}
