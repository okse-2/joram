/*
 * Copyright (C) 2003 - 2005 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.*;

import java.nio.*;
import java.nio.channels.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Daemon;

/**
 * <code>NGNetwork</code> is a new implementation of <code>Network</code>
 * class using nio package.
 */
public class NGNetwork extends StreamNetwork {
  final static int Kb = 1024;
  final static int Mb = 1024 * Kb;

  final static int SO_BUFSIZE = 64 * Kb;

  Selector selector = null;

  Dispatcher dispatcher = null;
  NetServer dmon[] = null;

  // Be careful, NbNetServer>1 cause malfunctioning (slowness, etc.)
  final static int NbNetServer = 1;

  CnxHandler[] handlers = null;

  /**
   * Creates a new network component.
   */
  public NGNetwork() {
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

    // Creates a connection handler for each domain's server.
    handlers = new CnxHandler[servers.length];
    for (int i=0; i<servers.length; i++) {
      if (servers[i] != AgentServer.getServerId())
        handlers[i] = new CnxHandler(getName(), servers[i]);
    }
  }

  ServerSocketChannel listen = null;

  void open() throws IOException {
    // Create a blocking server socket channel on port
    listen = ServerSocketChannel.open();
    listen.configureBlocking(false);
    listen.socket().bind(new InetSocketAddress(port));

    // Register channels with selector
    listen.register(selector, SelectionKey.OP_ACCEPT);
  }

  void close() {
    try {
      listen.close();
    } catch (Exception exc) {}
    listen = null;
  }

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    try {
      logmon.log(BasicLevel.DEBUG, getName() + ", starting");

      // Create a selector
      selector = Selector.open();
      // Creates a connection handler for each domain's server.
      for (int i=0; i<handlers.length; i++) {
        if (handlers[i] != null) handlers[i].init();
      }
      open();

      if (dispatcher == null)
        dispatcher = new Dispatcher(getName(), logmon);

      if (dmon == null) {
        dmon = new NetServer[NbNetServer];
        for (int i=0; i<NbNetServer; i++) {
          dmon[i] = new NetServer(getName(), logmon);
        }
      }

      if (! dispatcher.isRunning()) dispatcher.start();
      for (int i=0; i<NbNetServer; i++) {
        if (! dmon[i].isRunning()) dmon[i].start();
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

  final CnxHandler getHandler(short sid) {
    return handlers[index(sid)];
  }

  //   /**
  //    *  Adds a message in "ready to deliver" list. This method allocates a
  //    * new time stamp to the message ; be Careful, changing the stamp imply
  //    * the filename change too.
  //    */
  //   public void post(Message msg) throws Exception {
  //     short to = AgentServer.getServerDesc(msg.to.to).gateway;
  //     // Allocates a new timestamp. Be careful, if the message needs to be
  //     // routed we have to use the next destination in timestamp generation.
  //     msg.source = AgentServer.getServerId();
  //     msg.dest = to;
  //     msg.stamp = getSendUpdate(to);
  //     // Saves the message.
  //     msg.save();
  //     // Push it in "ready to deliver" queue.
  //     getHandler(to).send(msg);
  //   }

  /**
   * Wakes up the watch-dog thread.
   */
  public void wakeup() {
    if (selector != null) selector.wakeup();
    logmon.log(BasicLevel.DEBUG, getName() + ", wakeup");
  }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (dispatcher != null) dispatcher.stop();
    if (dmon != null) {
      for (int i=0; i<NbNetServer; i++) {
        if (dmon[i] != null) dmon[i].stop();
      }
    }
    close();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((dispatcher == null) || ! dispatcher.isRunning())
      return false;

    if (dmon == null)
      return false;

    for (int i=0; i<NbNetServer; i++) {
      if ((dmon[i] == null) || ! dmon[i].isRunning())
        return false;
    }

    return true;
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
    if (dispatcher != null) 
      strbuf.append(dispatcher.toString()).append("\n");
    for (int i=0; i<NbNetServer; i++) {
      if ((dmon != null) && (dmon[i] != null))
        strbuf.append(dmon[i].toString()).append("\n");
    }

    return strbuf.toString();
  }

  void cnxStart(SocketChannel channel) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", remotely started");

    channel.socket().setSendBufferSize(SO_BUFSIZE);
    channel.socket().setReceiveBufferSize(SO_BUFSIZE);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " bufsize: " + 
                 channel.socket().getReceiveBufferSize() + ", " +
                 channel.socket().getSendBufferSize());

    ByteBuffer buf = ByteBuffer.allocate(6);
    channel.read(buf);
    buf.flip();
    short sid = buf.getShort();
    int boot = buf.getInt();

    CnxHandler cnx = getHandler(sid);
    if (cnx.remoteStart(channel, boot)) cnx.startEnd();
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

          try {
            // Send the message
            getHandler(msg.getDest()).send(msg);
          } catch (IOException exc) {
            if (this.logmon.isLoggable(BasicLevel.ERROR))
              this.logmon.log(BasicLevel.ERROR, this.getName(), exc);
          }
          qout.pop();
        }
      } finally {
        finish();
      }
    }
  }

  final class NetServer extends Daemon {
    NetServer(String name, Logger logmon) throws IOException {
      super(name + ".NetServer");
      // Overload logmon definition in Daemon
      this.logmon = logmon;

      //       // Create a blocking server socket channel on port
      //       listen = ServerSocketChannel.open();
      //       listen.configureBlocking(false);
      //       listen.socket().bind(new InetSocketAddress(port));

      //       // Register channels with selector
      //       listen.register(selector, SelectionKey.OP_ACCEPT);
    }

    protected void close() {
      //       try {
      // 	listen.close();
      //       } catch (Exception exc) {}
      //       listen = null;
    }

    protected void shutdown() {
      close();
    }

    public void run() {
      int nbop = 0;
      CnxHandler cnx = null;

      try {

        while (running) {

          // This call blocks until there is activity on one of the  
          // registered channels. This is the key method in non-blocking I/O.
          canStop = true;
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + ", on select");
            nbop = selector.select(WDActivationPeriod);
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + ", on select:" + nbop);
          } catch (IOException exc) {
          }

          for (int idx=0; idx<handlers.length; idx++) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + ", " + handlers[idx]);

            if ((handlers[idx] != null) &&
                (handlers[idx].sendlist.size() > 0) &&
                (handlers[idx].channel == null)) {
              try {
                handlers[idx].start();
              } catch (IOException exc) {
                this.logmon.log(BasicLevel.WARN,
                                this.getName() + ", can't start cnx#" + idx,
                                exc);
              }
            }
          }

          if (nbop == 0) continue;
          canStop = false;

          // Get list of selection keys with pending events, then process
          // each key
          Set keys = selector.selectedKeys();
          for(Iterator it = keys.iterator(); it.hasNext(); ) {
            if (! running) break;

            // Get the selection key
            SelectionKey key = (SelectionKey) it.next();
            // Remove it from the list to indicate that it is being processed
            it.remove();

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + "(1): " + key + " -> " + key.interestOps());

            logmon.log(BasicLevel.DEBUG,
                       getName() + ":" +
                       key.isValid() +
                       key.isAcceptable() +
                       key.isReadable() +
                       key.isWritable());
            try {
              // Check if it's a connection request
              if (key.isAcceptable()) {
                if (logmon.isLoggable(BasicLevel.DEBUG))
                  logmon.log(BasicLevel.DEBUG, getName() + " acceptable");

                // Get channel with connection request (useless ?)
                ServerSocketChannel server = (ServerSocketChannel) key.channel();

                // Accept the connection request.
                SocketChannel channel = server.accept();

                // Register the channel with selector, listening for all events
                cnxStart(channel);
              } else {
                cnx = (CnxHandler) key.attachment();

                if (logmon.isLoggable(BasicLevel.DEBUG))
                  logmon.log(BasicLevel.DEBUG,
                             getName() + ": " + key + " -> " + cnx);
                // Since the ready operations are cumulative,
                // need to check readiness for each operation
                if (key.isValid() && key.isReadable()) {
                  if (logmon.isLoggable(BasicLevel.DEBUG))
                    logmon.log(BasicLevel.DEBUG, getName() + " readable");
                  cnx.read();
                }
                if (key.isValid() && key.isWritable()) {
                  if (logmon.isLoggable(BasicLevel.DEBUG))
                    logmon.log(BasicLevel.DEBUG, getName() + " writable");
                  cnx.write();
                } else  if (cnx.sendlist.size() > 0) {
                  logmon.log(BasicLevel.FATAL, getName() + " force");
                  key.interestOps(key.channel().validOps());
                }
              }
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG, getName() + "(2): " +
                           key + " -> " + key.interestOps());

            } catch (Exception exc) {
              logmon.log(BasicLevel.ERROR, getName(), exc);
              // Handle error with channel and unregister
              try {
                cnx.close();
              } catch (IOException exc2) {
                logmon.log(BasicLevel.ERROR, getName(), exc2);
              }
            }
          }
        }
      } catch (Throwable exc) {
        logmon.log(BasicLevel.FATAL, getName(), exc);
      }
    }
  }

  class CnxHandler {
    /** Destination server id */
    private short sid;
    /** The handler's name. */
    private String name = null;
    /**
     *  True if a "local" connection is in progress, a local connection
     * is initiated from this server to the remote one (defined by the
     * {@link #server server} descriptor.
     *  This attribute is used to synchronize local and remote attempts to
     * make connections.
     */
    private boolean local = false;
    /** The description of the remote server handled by this network session */
    private ServerDesc server;
    /** The communication socket channel */
    SocketChannel channel = null;

    /** Date of last connection attempt */
    long lasttry = 0L;

    /** Informations for output */
    int nbwrite = 0;
    MessageOutputStream mos = null;
    ByteBuffer bufout = null;

    /** FIFO list of all messages to be sent */
    MessageVector sendlist = null;

    /** Informations for input */
    ByteBuffer bufin = null;
    MessageInputStream mis = null;

    CnxHandler(String name, short sid) throws IOException {
      this.sid = sid;
      this.name = name + ".cnxHandler#" + sid;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", created");

      mos = new MessageOutputStream();
      bufin = ByteBuffer.allocateDirect(SO_BUFSIZE);
      mis = new MessageInputStream();

      sendlist = new MessageVector();
    }

    void init() throws IOException, UnknownServerException {
      server = AgentServer.getServerDesc(sid);
      if (sendlist.size() > 0) start();
    }

    /**
     * Returns this session's name.
     *
     * @return this session's name.
     */
    public final String getName() {
      return name;
    }

    void start() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", try to start");

      long currentTimeMillis = System.currentTimeMillis();

      if (server == null)
        // probalby the Consumer is initialized but not always started
        return;

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
        if ((this.channel != null) || this.local) {
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

      SocketChannel channel = null;
      try {
        SocketAddress addr = new InetSocketAddress(server.getAddr(),
                                                   server.getPort());
        channel = SocketChannel.open(addr);

        channel.socket().setSendBufferSize(SO_BUFSIZE);
        channel.socket().setReceiveBufferSize(SO_BUFSIZE);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " bufsize: " + 
                     channel.socket().getReceiveBufferSize() + ", " +
                     channel.socket().getSendBufferSize());

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", writeBoot: " + getBootTS());

        ByteBuffer buf = ByteBuffer.allocate(6);
        buf.putShort(AgentServer.getServerId());
        buf.putInt(getBootTS());
        buf.flip();
        channel.write(buf);

        //         ByteBuffer buf = ByteBuffer.allocate(6);
        buf.flip();
        if (channel.read(buf) > 0) {
          // Reads the message length
          buf.flip();
          int boot = buf.getInt();

          AgentServer.getTransaction().begin();
          testBootTS(sid, boot);
          AgentServer.getTransaction().commit(true);
        } else {
          throw new ConnectException("Can't get status");
        }
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", connection refused.", exc);
        // TODO: Try it later, may be a a connection is in progress...
        try {
          channel.close();
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
      this.channel = channel;
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
    synchronized boolean remoteStart(SocketChannel channel, int boot) {
      try {
        if ((this.channel != null) ||
            (this.local && server.sid > AgentServer.getServerId())) {
          //  The connection is already established, or
          // a "local" connection is in progress from this server with a
          // greater priority.
          //  In all cases, stops this "remote" attempt.
          //  If the "local" attempt has a lower priority, it will fail
          // due to a remote reject.
          throw new ConnectException("Already connected");
        }

        // Accept this connection.
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", writeBoot: " + getBootTS());

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(getBootTS());
        buf.flip();
        channel.write(buf);

        AgentServer.getTransaction().begin();
        testBootTS(sid, boot);
        AgentServer.getTransaction().commit(true);

        // Fixing sock attribute will prevent any future attempt 
        this.channel = channel;
        return true;
      } catch (Exception exc) {
        // May be a a connection is in progress, try it later...
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN,
                     getName() + ", connection refused", exc);

        // Close the connection (# NACK).
        try {
          channel.close();
        } catch (Exception exc2) {}
      }
      return false;
    }

    /**
     *  The session is well initialized, we can start the server thread that
     * "listen" the connected socket. If the maximum number of connections
     * is reached, one connection from the pool is closed.
     */
    private void startEnd() throws IOException {
      server.active = true;
      server.retry = 0;

      //       mos = new MessageOutputStream();
      nbwrite = 0;
      //       bufin = ByteBuffer.allocateDirect(SO_BUFSIZE);
      bufin.clear();
      //       mis = new MessageInputStream();

      // The returned channel is in blocking mode.
      channel.configureBlocking(false);
      // Register channels with selector
      channel.register(selector, channel.validOps(), this);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", connection started");

      sendlist.reset();
    }

    synchronized void send(Message msg) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", send message: " + msg);

      // Adds it to the list of message to be sent, it will be removed
      // after its ack receipt.
      sendlist.addMessage(msg);

      if ((channel != null) && (bufout == null)) {
        // As no message are actually sending the channel is only subscribe
        // for reading, subscribe this channel for write operation will permit
        // to transmit the new added message.

        // Be careful, as this change is only take in account for the next
        // select operation, we have to use wakeup on selector

        SelectionKey key = channel.keyFor(selector);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", send message, key=" + key);
        if (key != null)
          key.interestOps(channel.validOps());
      }

      // In all case a selector.wakeup() will solve the problem !!
      if (selector == null) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", network not started.");
      } else {
        selector.wakeup();
      }
    }

    /**
     * Class used to send messages through a stream.
     */
    final class MessageOutputStream extends ByteArrayOutputStream {
      private ObjectOutputStream oos = null;

      MessageOutputStream() throws IOException {
        super(256);

        oos = new ObjectOutputStream(this);

        count = 0;
        buf[29] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
        buf[30] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
        buf[31] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
        buf[32] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);
      }

      void writeMessage(Message msg) throws IOException {
        logmon.log(BasicLevel.DEBUG, getName() + ", writes " + msg);

        // Writes sender's AgentId
        buf[4] = (byte) (msg.from.from >>>  8);
        buf[5] = (byte) (msg.from.from >>>  0);
        buf[6] = (byte) (msg.from.to >>>  8);
        buf[7] = (byte) (msg.from.to >>>  0);
        buf[8] = (byte) (msg.from.stamp >>>  24);
        buf[9] = (byte) (msg.from.stamp >>>  16);
        buf[10] = (byte) (msg.from.stamp >>>  8);
        buf[11] = (byte) (msg.from.stamp >>>  0);
        // Writes adressee's AgentId
        buf[12]  = (byte) (msg.to.from >>>  8);
        buf[13]  = (byte) (msg.to.from >>>  0);
        buf[14] = (byte) (msg.to.to >>>  8);
        buf[15] = (byte) (msg.to.to >>>  0);
        buf[16] = (byte) (msg.to.stamp >>>  24);
        buf[17] = (byte) (msg.to.stamp >>>  16);
        buf[18] = (byte) (msg.to.stamp >>>  8);
        buf[19] = (byte) (msg.to.stamp >>>  0);
        // Writes source server id of message
        buf[20]  = (byte) (msg.source >>>  8);
        buf[21]  = (byte) (msg.source >>>  0);
        // Writes destination server id of message
        buf[22] = (byte) (msg.dest >>>  8);
        buf[23] = (byte) (msg.dest >>>  0);
        // Writes stamp of message
        buf[24] = (byte) (msg.stamp >>>  24);
        buf[25] = (byte) (msg.stamp >>>  16);
        buf[26] = (byte) (msg.stamp >>>  8);
        buf[27] = (byte) (msg.stamp >>>  0);
        count = 28;

        if (msg.not != null) {
          // Writes notification attributes
          buf[28] = msg.optToByte();
          // Be careful, the stream header is hard-written in buf[29..32]
          count = 33;

          oos.writeObject(msg.not);
          oos.reset();
          oos.flush();
        }

        // Writes length at beginning
        buf[0] = (byte) (count >>>  24);
        buf[1] = (byte) (count >>>  16);
        buf[2] = (byte) (count >>>  8);
        buf[3] = (byte) (count >>>  0);

        logmon.log(BasicLevel.DEBUG, getName() + ", writes " + count);

        nbwrite = count;
        bufout = ByteBuffer.wrap(buf, 0, count);
        nbwrite -= channel.write(bufout);
      }
    }

    //     void write(Message msg) throws IOException {
    //       mos.writeMessage(msg);
    //     }

    /**
     * Method called each time the channel is Writable
     */
    private synchronized void write() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + " write-1");

      // test if there is still bytes to write
      if ((bufout != null) && (nbwrite > 0)) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " write-2");
        nbwrite -= channel.write(bufout);
      } else {
        if (nbwrite == 0) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + " write-3");
          //           if (bufout != null) {
          //             // end of message sending, if it is an acknowledge remove it
          //             // from sendlist else wait for ack.
          //             if (sendlist.currentMessage().not == null) {
          //               logmon.log(BasicLevel.DEBUG, getName() + " remove ack sent");
          //               sendlist.removeCurrent();
          //             }
          //           }
          Message msg = sendlist.nextMessage();
          if (msg == null) {
            bufout = null;
            // There is no more message to send, unsubscribe this channel
            // for write operation.
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + " write-4x:" + msg);
            channel.register(selector, SelectionKey.OP_READ, this);
          } else {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + " write-4:" + msg);
            mos.writeMessage(msg);
            if (msg.not == null) {
              logmon.log(BasicLevel.DEBUG, getName() + " remove ack sent");
              sendlist.removeCurrent();
            }
          }
        }
      }
    }

    /**
     * Method called each time the channel is Readable
     */
    private synchronized void read() throws Exception {
      int bytes = channel.read(bufin);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + " reads: " + bytes);

      if (bytes == 0) return;

      if (bytes < 0) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " cnx remotely closed");
        close();
        return;
      }

      bufin.flip();
      while (bytes > 0) {
        //         logmon.log(BasicLevel.FATAL,
        //                    "mis.getBuffer()=" + mis.getBuffer().length);
        //         logmon.log(BasicLevel.FATAL,
        //                    "mis.getCount()=" + mis.getCount());
        //         logmon.log(BasicLevel.FATAL,
        //                    "mis.length=" + mis.length);
        //         logmon.log(BasicLevel.FATAL,
        //                    "bytes=" + bytes);

        if (mis.length ==  -1) {
          //	if (mis.msg ==  null) {
          // Reads the message header.
          if ((mis.getCount() + bytes) < 28) {
            bufin.get(mis.getBuffer(), mis.getCount(), bytes);
            mis.setCount(mis.getCount() + bytes);
            bytes = 0;
          } else {
            bufin.get(mis.getBuffer(), mis.getCount(), 28-mis.getCount());
            bytes -= 28-mis.getCount();
            mis.setCount(28);

            Message msg = mis.readHeader();

            if (mis.length == 28) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() + ", ack received #" + msg.stamp);
              //            logmon.log(BasicLevel.FATAL, msg.toString());
              doAck(msg.stamp);
              msg.free();

              // Reset data structures for next messages
              mis.length = -1;
              mis.msg = null;
              mis.setCount(0);
            }

            //             // Reads the message length
            // 	  try {
            // 	    for (; nbread <28; nbread++) {
            //               bufin.get(mis.buf, nbread, 28-nbread);
            // 	      bytes -= 1;
            // 	    }
            //             if (logmon.isLoggable(BasicLevel.DEBUG))
            //               logmon.log(BasicLevel.DEBUG, getName() + " get length: " + length);
            // 	    // Allocates byte array for message storage
            // 	    array = new byte[length];
            // 	    nbread = 0;
            // 	  } catch (BufferUnderflowException exc) {
            // 	    break;
          }
        } else {
          // The message header is read, reads the notification if any.
          if ((mis.getCount() + bytes) < (mis.length-28)) {
            bufin.get(mis.getBuffer(), mis.getCount(), bytes);
            mis.setCount(mis.getCount() + bytes);
            bytes = 0;
          } else {
            bufin.get(mis.getBuffer(), mis.getCount(), mis.length-28-mis.getCount());
            bytes -= mis.length-28-mis.getCount();
            mis.setCount(mis.length-28);

            Message msg = mis.readMessage();

            //  Keep message stamp in order to acknowledge it (be careful,
            // the message get a new stamp to be delivered).
            int stamp = msg.getStamp();
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ", message received #" + stamp);
            //          logmon.log(BasicLevel.FATAL, msg.toString());
            deliver(msg);
            ack(stamp);

            // Reset data structures for next messages
            mis.length = -1;
            mis.msg = null;
            mis.setCount(0);
          }
        }
      }
      bufin.clear();
    }

    /**
     * Class used to read messages through a stream.
     */
    final class MessageInputStream extends ByteArrayInputStream {
      int length = -1;
      Message msg = null;

      MessageInputStream() {
        super(new byte[512]);
        count = 0;
      }

      public void reset() {
        super.reset();
        length = -1;
        msg = null;
      }

      byte[] getBuffer() {
        return buf;
      }

      int getCount() {
        return count;
      }

      void setCount(int count) {
        this.count = count;
      }

      Message readHeader() throws Exception {
        // Reads boot timestamp of source server
        length = ((buf[0] & 0xFF) << 24) + ((buf[1] & 0xFF) << 16) +
        ((buf[2] & 0xFF) <<  8) + ((buf[3] & 0xFF) <<  0);

        msg = Message.alloc();
        // Reads sender's AgentId
        msg.from = new AgentId(
                               (short) (((buf[4] & 0xFF) <<  8) + (buf[5] & 0xFF)),
                               (short) (((buf[6] & 0xFF) <<  8) + (buf[7] & 0xFF)),
                               ((buf[8] & 0xFF) << 24) + ((buf[9] & 0xFF) << 16) +
                               ((buf[10] & 0xFF) <<  8) + ((buf[11] & 0xFF) <<  0));
        // Reads adressee's AgentId
        msg.to = new AgentId(
                             (short) (((buf[12] & 0xFF) <<  8) + (buf[13] & 0xFF)),
                             (short) (((buf[14] & 0xFF) <<  8) + (buf[15] & 0xFF)),
                             ((buf[16] & 0xFF) << 24) + ((buf[17] & 0xFF) << 16) +
                             ((buf[18] & 0xFF) <<  8) + ((buf[19] & 0xFF) <<  0));
        // Reads source server id of message
        msg.source = (short) (((buf[20] & 0xFF) <<  8) +
            ((buf[21] & 0xFF) <<  0));
        // Reads destination server id of message
        msg.dest = (short) (((buf[22] & 0xFF) <<  8) +
            ((buf[23] & 0xFF) <<  0));
        // Reads stamp of message
        msg.stamp = ((buf[24] & 0xFF) << 24) +
        ((buf[25] & 0xFF) << 16) +
        ((buf[26] & 0xFF) <<  8) +
        ((buf[27] & 0xFF) <<  0);

        if ((length -28) > buf.length)
          buf = new byte[length -28];

        count = 0;

        return msg;
      }

      Message readMessage() throws Exception {
        // AF: Be careful I think that there is an error here. The buffer has
        // been refilled and may be reallocated !! 
        if (length > 28) {
          // Reads notification attributes
          boolean persistent = ((buf[28] & 0x01) == 0x01) ? true : false;
          boolean detachable = ((buf[28] & 0x10) == 0x10) ? true : false;
          pos = 1;
          // Reads notification object
          ObjectInputStream ois = new ObjectInputStream(this);
          msg.not = (Notification) ois.readObject();
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
        msg = sendlist.removeMessage(ack);
        AgentServer.getTransaction().begin();
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

      send(ack);
    }

    void close() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", close");
      try {
        channel.keyFor(selector).cancel();
      } catch (Exception exc) {}
      try {
        channel.close();
      } catch (Exception exc) {
        //
      } finally {
        channel = null;
      }
      nbwrite = 0;
      bufout = null;
    }    

    public String toString() {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append('(').append(super.toString());
      strbuf.append(',').append(name);
      strbuf.append(',').append(channel);
      strbuf.append(',').append(nbwrite);
      strbuf.append(',').append(sendlist).append(')');
      return strbuf.toString();
    }
  }

  final class MessageVector {
    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer, 
     * and is at least large enough to contain all the vector's elements.<p>
     *
     * Any array elements following the last element in the Vector are null.
     */
    private Message elementData[] = null;

    /**
     * The number of valid components in this <tt>MessageVector</tt> object. 
     */
    private int elementCount = 0;

    /**
     * The actual item in this <tt>MessageVector</tt> object.
     */
    private int current = -1;

    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment. 
     */
    public MessageVector() {
      this.elementData = new Message[20];
    }

    public synchronized Message nextMessage() {
      logmon.log(BasicLevel.FATAL, getName() + ", nextMessage:" + toString());

      if ((current +1) < elementCount)
        return elementData[++current];
      return null;
    }

    /**
     * Returns the number of message in this vector.
     *
     * @return  the number of message in this vector.
     */
    public synchronized int size() {
      return elementCount;
    }

    public synchronized void reset() {
      current = -1;
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * @param   msg   the component to be added.
     */
    public synchronized void addMessage(Message msg) {
      logmon.log(BasicLevel.FATAL, getName() + ", addMessage:" + toString());

      if ((elementCount + 1) > elementData.length) {
        Message oldData[] = elementData;
        elementData = new Message[elementData.length * 2];
        System.arraycopy(oldData, 0, elementData, 0, elementCount);
      }
      elementData[elementCount++] = msg;
    }

    public synchronized void removeCurrent() {
      logmon.log(BasicLevel.FATAL, getName() + ", removeCurrent:" + toString());

      if (elementCount > (current +1)) {
        System.arraycopy(elementData, current +1,
                         elementData, current, elementCount - current -1);

      }
      elementData[elementCount-1] = null; /* to let gc do its work */
      elementCount--;
      current--;
    }

    /**
     * Removes a message specified by its stamp. Only remove real message,
     * this method don't touch to acknowledge message.
     *
     * @param   stamp   the stamp of the message to remove.
     */
    public synchronized Message removeMessage(int stamp) {
      Message msg = null;

      logmon.log(BasicLevel.FATAL, getName() + ", removeMessage:" + toString());

      for (int index=0 ; index<elementCount ; index++) {
        msg = elementData[index];

        if ((msg.not != null) && (msg.getStamp() == stamp)) {
          if (elementCount > (index +1)) {
            System.arraycopy(elementData, index +1,
                             elementData, index, elementCount - index - 1);
          }
          elementData[elementCount-1] = null; /* to let gc do its work */
          elementCount--;
          // AF: To verify !!
          if (index <=current) current--;

          return msg;
        }
      }
      throw new NoSuchElementException();
    }

    public String toString() {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append(super.toString());
      strbuf.append(',').append(current);
      strbuf.append(',').append(elementCount);
      for (int i=0; i<elementCount; i++) {
        strbuf.append(",(").append(elementData[i]).append(')');
      }
      return strbuf.toString();
    }
  }
}
