/*
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.util.Daemon;

/**
 * <code>NGNetwork</code> is a new implementation of <code>Network</code>
 * class using nio package.
 */
public class NGNetwork extends FIFONetwork {
  final class MessageVector {
    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer, 
     * and is at least large enough to contain all the vector's elements.<p>
     *
     * Any array elements following the last element in the Vector are null.
     */
    protected Message elementData[] = null;

    /**
     * The number of valid components in this <tt>MessageVector</tt> object. 
     */
    protected int elementCount = 0;

    /**
     * The actual item in this <tt>MessageVector</tt> object. 
     */
    protected int current = 0;


    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment. 
     */
    public MessageVector() {
	this.elementData = new Message[20];
    }

    public synchronized Message currentMessage() {
      logmon.log(BasicLevel.DEBUG, getName() + "currentMessage:" +
                 current + ", " + elementCount);
      if (current <= elementCount)
        return elementData[current -1];
      else
        return null;
    }

    public synchronized Message nextMessage() {
      logmon.log(BasicLevel.DEBUG, getName() + "nextMessage:" +
                 current + ", " + elementCount);
      if (current < elementCount)
        return elementData[current++];
      else
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
      current = 0;
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * @param   msg   the component to be added.
     */
    public synchronized void addMessage(Message msg) {
      logmon.log(BasicLevel.DEBUG, getName() + "addMessage:" +
                 current + ", " + elementCount);

        int minCapacity = elementCount + 1;
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    Message oldData[] = elementData;
	    int newCapacity = oldCapacity * 2;
    	    if (newCapacity < minCapacity) {
		newCapacity = minCapacity;
	    }
	    elementData = new Message[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
	elementData[elementCount++] = msg;
    }

    public synchronized void removeCurrent() {
      logmon.log(BasicLevel.DEBUG, getName() + "removeCurrent:" +
                 current + ", " + elementCount);

      Message msg = msg = elementData[current -1];

      if (elementCount > current) {
        System.arraycopy(elementData, current,
                         elementData, current -1, elementCount - current);
      }
      elementCount--;
      current--;
      elementData[elementCount] = null; /* to let gc do its work */
    }

    /**
     * Removes a message specified by its stamp. Only remove real message,
     * this method don't touch to acknowledge message.
     *
     * @param   stamp   the stamp of the message to remove.
     */
    public synchronized Message removeMessage(int stamp) {
      Message msg = null;

      logmon.log(BasicLevel.DEBUG, getName() + "removeMessage:" +
                 current + ", " + elementCount);

      for (int index=0 ; index<elementCount ; index++) {
        msg = elementData[index];

        if ((msg.not != null) && (msg.getStamp() == stamp)) {
          int j = elementCount - index - 1;
          if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
          }
          // AF: To verify !!
          if (index < current) current--;
          elementCount--;
          elementData[elementCount] = null; /* to let gc do its work */
        
          return msg;
        }
      }
      throw new NoSuchElementException();
    }
  }

  final static int Kb = 1024;
  final static int Mb = 1024 * Kb;

  final static int SO_BUFSIZE = 64 * Kb;

  Selector selector = null;

  CnxHandler[] handlers = null;

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
    ByteBuffer bufout1 = null;
    ByteBuffer bufout2 = null;

    /** FIFO list of all messages to be sent */
    MessageVector sendlist = null;

    /** Informations for input */
    int length = 0;
    int nbread = 0;
    ByteBuffer bufin = null;
    byte[] array = null;

    CnxHandler(String name, short sid) {
      this.sid = sid;
      this.name = name + ".cnxHandler#" + sid;
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", created");

      this.bufout1 = ByteBuffer.allocateDirect(4);
      this.bufin = ByteBuffer.allocateDirect(SO_BUFSIZE);

      this.sendlist = new MessageVector();
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

    final static short CNX_OK = 0;

    void start() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", started");

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
                                                   server.port);
        channel = SocketChannel.open(addr);

        channel.socket().setSendBufferSize(SO_BUFSIZE);
        channel.socket().setReceiveBufferSize(SO_BUFSIZE);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " bufsize: " + 
                     channel.socket().getReceiveBufferSize() + ", " +
                     channel.socket().getSendBufferSize());

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort(AgentServer.getServerId());
        buf.flip();
        channel.write(buf);

        buf.flip();
        if (channel.read(buf) > 0) {
          // Reads the message length
          buf.flip();
          short status = buf.getShort();
          if (status != CNX_OK) {
            throw new ConnectException("Nack status received: " + status);
          }
        } else {
          throw new ConnectException("Can't get status");
        }

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", connection done.");
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
    synchronized boolean remoteStart(SocketChannel channel) {
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
                         getName() + ", send AckStatus");

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort(CNX_OK);
        buf.flip();
        channel.write(buf);

        logmon.log(BasicLevel.DEBUG,
                         getName() + ", AckStatus sent");

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
 
      // The returned channel is in blocking mode.
      channel.configureBlocking(false);
      // Register channels with selector
      channel.register(selector, channel.validOps(), this);

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
//       Object[] waiting = sendList.toArray();
//       logmon.log(BasicLevel.DEBUG,
// 		 getName() + ", send " + waiting.length + " waiting messages");
//       for (int i=0; i<waiting.length; i++) {
// 	transmit((Message) waiting[i]);
//       }
      sendlist.reset();
    }

    void send(Message msg) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                         getName() + ", send message: " + msg);

      // Adds it to the list of message to be sent, it will be removed
      // after its ack receipt.
      sendlist.addMessage(msg);

// AF: Try to use daemon thread to establish connection
//       if (channel == null) start();

      if ((channel != null) && (bufout2 == null)) {
        // As no message are actually sending the channel is only subscribe
        // for reading, subscribe this channel for write operation will permit
        // to transmit the new added message.

	// Be careful, as this change is only take in account for the next
	// select operation, we have to use wakeup on selector

	channel.keyFor(selector).interestOps(channel.validOps());
      }

      // In all case a selector.wakeup() will solve the problem !!
      if (selector == null) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", network not started.");
      } else {
        selector.wakeup();
      }
    }

    void write(Message msg) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(msg);
      oos.flush();
      byte[] buf = bos.toByteArray();

      bufout1.putInt(buf.length);
      bufout1.flip();

      nbwrite = 4;
      do {
	nbwrite -= channel.write(bufout1);
      } while (nbwrite != 0);
      bufout1.clear();

      nbwrite = buf.length;
      bufout2 = ByteBuffer.wrap(buf);
      nbwrite -= channel.write(bufout2);
    }

    /**
     * Method calls each time the channel is Writable
     */
    void write() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + " write-1");

      // test if there is still bytes to write
      if ((bufout2 != null) && (nbwrite > 0)) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " write-2");
        nbwrite -= channel.write(bufout2);
      } else {
        if (nbwrite == 0) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + " write-3");
          if (bufout2 != null) {
            // end of message sending, if it is an acknowledge remove it
            // from sendlist else wait for ack.
            if (sendlist.currentMessage().not == null) {
              logmon.log(BasicLevel.DEBUG, getName() + " remove ack sent");
              sendlist.removeCurrent();
            }
          }
          Message msg = sendlist.nextMessage();
          if (msg == null) {
            bufout2 = null;
            // There is no more message to send, unsubscribe this channel
            // for write operation.
            channel.register(selector, SelectionKey.OP_READ, this);
          } else {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + " write-4:" + msg);
            write(msg);
          }
        }
      }
    }

    void read() throws Exception {
      int bytes = channel.read(bufin);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + " reads: " + bytes);

      if (bytes <= 0) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " cnx remotely closed");
	close();
	return;
      }

      bufin.flip();
      while (bytes > 0) {
	if (array ==  null) {
	  // Reads the message length
	  try {
	    for (; nbread <4; nbread++) {
	      length <<= 8;
	      length |= (bufin.get() & 0xFF);
	      bytes -= 1;
	    }
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, getName() + " get length: " + length);
	    // Allocates byte array for message storage
	    array = new byte[length];
	    nbread = 0;
	  } catch (BufferUnderflowException exc) {
	    break;
	  }
	} else {
	  if ((nbread + bytes) < length) {
	    bufin.get(array, nbread, bytes);
	    nbread += bytes;
	    bytes = 0;
	  } else {
	    bufin.get(array, nbread, length-nbread);
	    bytes -= length-nbread;
	    nbread = length;
            
            ByteArrayInputStream bis = new ByteArrayInputStream(array);
            ObjectInputStream ois = new ObjectInputStream(bis);	  
            Message msg = (Message) ois.readObject();

            //  Keep message stamp in order to acknowledge it (be careful,
            // the message get a new stamp to be delivered).
            int stamp = msg.getStamp();
            if (msg.not != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() + ", message received #" + stamp);
              deliver(msg);
              ack(stamp);
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() + ", ack received #" + stamp);
              doAck(stamp);
            }

	    // Reset data structures for next messages
	    array = null;
	    length = 0;
	    nbread = 0;
	  }
	}
      }
      bufin.clear();
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
      nbread = nbwrite = 0;
      bufout2 = null;
      array = null;
    }    
  }

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

  final CnxHandler getHandler(short sid) {
    return handlers[clock.index(sid)];
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    short to = AgentServer.getServerDesc(msg.to.to).gateway;
    // Allocates a new timestamp. Be careful, if the message needs to be
    // routed we have to use the next destination in timestamp generation.
    msg.setUpdate(clock.getSendUpdate(to));
    // Saves the message.
    msg.save();
    // Push it in "ready to deliver" queue.
    getHandler(to).send(msg);
  }

  /** Daemon */
  NetServer dmon = null;
//   /** */
//   WatchDog watchDog = null;

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    try {
      if (isRunning())
	throw new IOException("Consumer already running");

      logmon.log(BasicLevel.DEBUG, getName() + ", starting");

      // Create a selector
      selector = Selector.open();

      // Creates a connection handler for each domain's server.
      for (int i=0; i<handlers.length; i++) {
        if (handlers[i] != null) handlers[i].init();
      }
    
      dmon = new NetServer(getName(), logmon);
//       watchDog = new WatchDog(getName(), logmon);

      dmon.start();
//       watchDog.start();
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
    if (selector != null) selector.wakeup();
//     if (watchDog != null) watchDog.wakeup();
    logmon.log(BasicLevel.DEBUG, getName() + ", wakeup");
  }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (dmon != null) dmon.stop();
//     if (watchDog != null) watchDog.stop();
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
      strbuf.append(dmon.toString()).append("\n");

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
    
    ByteBuffer buf = ByteBuffer.allocate(2);
    channel.read(buf);
    buf.flip();
    short sid = buf.getShort();
      
    CnxHandler cnx = getHandler(sid);
    if (cnx.remoteStart(channel)) cnx.startEnd();
  }

  final class NetServer extends Daemon {
    ServerSocketChannel listen = null;

    NetServer(String name, Logger logmon) throws IOException {
      super(name + ".NetServer");
      // Overload logmon definition in Daemon
      this.logmon = logmon;

      // Create a blocking server socket channel on port
      listen = ServerSocketChannel.open();
      listen.configureBlocking(false);
      listen.socket().bind(new InetSocketAddress(port));

      // Register channels with selector
      listen.register(selector, SelectionKey.OP_ACCEPT);
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
      int nbop = 0;
      CnxHandler cnx = null;

      while (running) {

        // This call blocks until there is activity on one of the  
        // registered channels. This is the key method in non-blocking I/O.
        canStop = true;
        try {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ", on select");
          nbop = selector.select(WDActivationPeriod);
        } catch (IOException exc) {
        }

        for (int idx=0; idx<handlers.length; idx++) {
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
            logmon.log(BasicLevel.DEBUG, getName() + ": " +
                       key + " -> " + key.interestOps());
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
              }
            }
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, getName(), exc);
            // Handle error with channel and unregister
            try {
              cnx.close();
            } catch (IOException exc2) {
            }
          }
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, getName() + ": " +
                       key + " -> " + key.interestOps());
        }
      }
    }
  }
}
