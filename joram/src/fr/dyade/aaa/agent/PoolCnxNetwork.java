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

final class StatusMessage implements Serializable {
  byte status;
  int stamp;

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

class PoolCnxNetwork extends StreamNetwork {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: PoolCnxNetwork.java,v 1.1 2000-10-05 15:15:22 tachkeni Exp $";
 
  String name = null;

  WakeOnConnection wakeOnConnection = null; 
  NetSession sessions[] = null;
  NetServerOut2 netServerOut = null;
  WatchDog2 watchDog = null;
  Vector recvList;

  static int nbMaxCnx = 5;
  int nbActiveCnx = 0;
  NetSession activeSessions[];
  long current = 0L;

  PoolCnxNetwork(MatrixClock mclock) throws Exception {
    super(mclock);
    name = "PoolCnxNetwork#" + Server.getServerId();
    recvList = new Vector();
    try {
      nbMaxCnx = Server.getInteger("fr.dyade.aaa.agent.PoolCnxNetwork.nbMaxCnx").intValue();
    } catch (Exception exc) {}
    activeSessions = new NetSession[nbMaxCnx];
  }

  /**
   * Returns this session's name.
   *
   * @return this session's name.
   */
  public final String getName() {
    return name;
  }

  void addRecvMessage(Message msg) {
    recvList.addElement(msg);
  }

  void start(MessageQueue qin,
	     MessageQueue qout) throws IOException {
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + ": start", false);

    if ((Server.networkServers != null) &&
	(Server.networkServers.length > 1)) {
      sessions = new NetSession[Server.networkServers.length];
      for (int i=0; i<Server.networkServers.length; i++) {
	if ((i != Server.getServerId()) &&
	    (Server.networkServers[i] != null)) {
	  sessions[i] = new NetSession(this,
				       Server.networkServers[i],
				       qin, recvList, mclock);
	}
      }
    }
    wakeOnConnection = new WakeOnConnection(this, sessions);
    netServerOut = new NetServerOut2(this, qout, sessions);
    watchDog = new WatchDog2(this, sessions);
  }

  void wakeup() {
    if (watchDog != null) watchDog.wakeup();
  }

  void stop() {
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + ": stop", false);

    if (wakeOnConnection != null) wakeOnConnection.stop();
    if (netServerOut != null) netServerOut.stop();
    if (watchDog != null) watchDog.wakeup();
    for (int i=0; i<Server.networkServers.length; i++) {
      if ((i != Server.getServerId()) && (sessions[i]!= null)) {
	sessions[i].stop();
      }
    }
  }
}

class NetSession implements Runnable {
  PoolCnxNetwork network;
  ServerDesc server;

  String name = null;

  boolean canStop = false;

  Socket sock = null;
  Thread thread = null;

  ObjectInputStream ois = null;
  ObjectOutputStream oos = null;

  MessageQueue qin;
  Vector recvList;
  Vector sendList;
  MatrixClock mclock;

  long last = 0L;

  NetSession(PoolCnxNetwork network,
	     ServerDesc server,
	     MessageQueue qin,
	     Vector recvList,
	     MatrixClock mclock) {
    this.network = network;
    this.server = server;
    this.qin = qin;
    this.recvList = recvList;
    this.mclock = mclock;

    name = "NetSession#" + Server.getServerId() + "." + server.sid;

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
      Debug.trace(getName() + ": start", false);

    long currentTimeMillis = System.currentTimeMillis();

    if (((server.retry < Network.WDNbRetryLevel1) && 
	 ((server.last + Network.WDRetryPeriod1) < currentTimeMillis)) ||
	((server.retry < Network.WDNbRetryLevel2) &&
	 ((server.last + Network.WDRetryPeriod2) < currentTimeMillis)) ||
	((server.last + Network.WDRetryPeriod3) < currentTimeMillis)) {
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
      Debug.trace(getName() + ": remote start", false);

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
  synchronized boolean localStart() {
    if (this.sock != null)
      // The connection is already established
      return false;

    Socket sock = null;

    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;

    try {
      sock = network.createSocket(server.getAddr(), server.port);

      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": connection setup", false);

      network.setSocketOption(sock);
      // Be careful: The OOS should be initialized first in order to
      // send the header waited by OIS at the other end
      oos = network.getOutputStream(sock);
      ois = network.getInputStream(sock);

      oos.writeObject(new Boot());
      oos.flush();
      oos.reset();

      StatusMessage statusMsg = (StatusMessage) ois.readObject();
      if (statusMsg.status == StatusMessage.NAckStatus) {
	throw new ConnectException("Nack status received");
      }
    } catch (Exception exc) {
      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": bad connection setup", exc);
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

    this.sock = sock;
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
   * @param ois		the input stream
   * @param oos		the output stream
   * @return	true if the connection is established, false otherwise.
   */
  synchronized boolean remoteStart(Socket sock,
				   ObjectInputStream ois,
				   ObjectOutputStream oos) {
    try {
      if ((this.sock != null) &&
	  (server.sid < Server.getServerId())) {
	// A connection initialized from a localStart is already started.
	throw new ConnectException("Already connected");
      }
      
      oos.writeObject(StatusMessage.Ack);
      oos.flush();
      oos.reset();
    } catch (Exception exc) {
      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": bad connection setup", exc);
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

    this.sock = sock;
    this.ois = ois;
    this.oos = oos;

    return true;
  }

  /**
   *  The session is well initialized then start the server thread that
   * "listen" the connected socket. If the maximum number of connections
   * is reached, one connection from the pool is closed.
   */
  private void startEnd() {
    server.active = true;
    server.retry = 0;
    
    synchronized(network.activeSessions) {
      if (network.nbActiveCnx < network.nbMaxCnx) {
	// Insert the current session in the active pool.
	network.activeSessions[network.nbActiveCnx++] = this;
      } else {
	// Search the last recently used session in the pool.
	long min = Long.MAX_VALUE;
	int idx = -1;
	for (int i=0; i<network.nbMaxCnx; i++) {
	  if (network.activeSessions[i].last < min) {
	    idx = i;
	    min = network.activeSessions[i].last;
	  }
	}
	// Kill choosed session and insert new one
	network.activeSessions[idx].stop();
	network.activeSessions[idx] = this;
      }
      last = network.current++;
    }
    thread = new Thread(this, getName());
    thread.setDaemon(false);
    thread.start();

    // Try to send all waiting messages.
    for (int i=0; i<sendList.size(); i++) {
      transmit((Serializable) sendList.elementAt(i));
    }
  }

  /**
   *
   */
  synchronized void stop() {
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + ": stop", false);

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
    if (Debug.debug && Debug.sendMessage)
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
    if (Debug.debug && Debug.sendMessage)
      Debug.trace(getName() + ": ack message #" + stamp, false);

    StatusMessage.Ack.stamp = stamp;
    transmit(StatusMessage.Ack);
  }

  synchronized void transmit(Serializable msg) {
    last = network.current++;
    try {
      if (oos != null) {
	oos.writeObject(msg);
	oos.flush();
	oos.reset();
      }
    } catch (IOException exc) {
      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": Exception in sending message", exc);
      close();
    }
  }

  public void run() {
    Object obj;

    try {
      while (Server.isRunning) {
	canStop = true;

	if (Debug.debug && Debug.recvMessage)
	  Debug.trace(getName() + ": wait message", false);

	try {
	  obj = ois.readObject();
	} catch (ClassNotFoundException exc) {
	  // TODO: In order to process it we have to return an error,
	  // but in that case me must identify the bad message...
	  continue;
	} catch (InvalidClassException exc) {
	  // TODO: In order to process it we have to return an error,
	  // but in that case me must identify the bad message...
	  continue;
	} catch (StreamCorruptedException exc) {
	  break;
	} catch (OptionalDataException exc) {
	  break;
	}

	canStop = false;

	if (obj instanceof StatusMessage) {
	  StatusMessage ack = (StatusMessage) obj;

	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": ack received #" + ack.stamp, false);

	  for (int i=0; i<sendList.size(); i++) {
	    Message tmpMsg = (Message) sendList.elementAt(i);
	    if (tmpMsg.update.stamp == ack.stamp) {
	      //  Suppress the acknowledged notification from waiting list,
	      // and deletes it.
	      sendList.removeElementAt(i);
	      Server.transaction.begin();
	      tmpMsg.delete();
	      Server.transaction.commit();
	      Server.transaction.release();

	      if (Debug.debug && Debug.recvMessage)
		Debug.trace(getName() + ": ack ok #" + ack.stamp, false);

	      break;
	    }
	  }
	} else if (obj instanceof Message) {
	  Message msg = (Message) obj;
	  //  Keep message stamp in order to acknowledge it (be careful,
	  // the message get a new stamp to be delivered).
	  int stamp = msg.update.stamp;

	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": message received #" + msg.update.stamp,
			false);

	  // Test if the message can be delivered then deliver it
	  // else put it in the waiting list
	  int todo = mclock.testRecvUpdate(msg.update);
	  if (todo == MatrixClock.DELIVER) {
	    // Deliver the message then try to deliver alls waiting message.
	    Server.transaction.begin();
	    // Allocate a local time to the message to order it in
	    // local queue, then save it.
	    msg.update = mclock.getSendUpdate(Server.getServerId());
	    msg.save();
	    Server.qin.push(msg);

	    if (Debug.debug && Debug.recvMessage)
	      Debug.trace(getName() +
			  ": deliver msg#" + msg.update.stamp +
			  " from " + msg.from +
			  " to " + msg.to, false);
	  scanlist:
	    while (true) {
	      for (int i=0; i<recvList.size(); i++) {
		Message tmpMsg = (Message) recvList.elementAt(i);
		if (mclock.testRecvUpdate(tmpMsg.update) == MatrixClock.DELIVER) {
		  // Allocate a local time to the message, then save it.
		  tmpMsg.update = mclock.getSendUpdate(Server.getServerId());
		  tmpMsg.save();
		  //  Deliver the message, then delete it from list.
		  Server.qin.push(tmpMsg);
		  recvList.removeElementAt(i);
		  // local time has changed we have to rescan the list.

		  if (Debug.debug && Debug.recvMessage)
		    Debug.trace(getName() +
				": deliver msg#" + tmpMsg.update.stamp +
				" from " + tmpMsg.from +
				" to " + tmpMsg.to, false);

		  continue scanlist;
		}
	      }
	      //  We have scan the entire list without deliver any message
	      // so we leave the loop.
	      break scanlist;
	    }
	    mclock.save();
	    Server.transaction.commit();
	    // then commit and validate the message.
	    Server.qin.validate();
	    Server.transaction.release();
	  } else if (todo == MatrixClock.WAIT_TO_DELIVER) {
	    Server.transaction.begin();
	    // Insert in a waiting list.
	    msg.save();
	    recvList.addElement(msg);
	    Server.transaction.commit();
	    Server.transaction.release();

	    if (Debug.debug && Debug.recvMessage)
	      Debug.trace("block msg#" + msg.update.stamp +
			  " from " + msg.from +
			  " to " + msg.to, false);
	  }
	  ack(stamp);
	} else {
	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": receives" + obj, false);
	}
      }
    } catch (Exception exc) {
      // TODO:
      if (Debug.debug && Debug.recvMessage)
	Debug.trace(getName() + ": problem during connection", exc);
    }

    close();
    thread = null;
  }
}

final class WakeOnConnection extends Thread {
  PoolCnxNetwork network;
  volatile boolean canStop = false;
  ServerSocket listenSocket = null;
  NetSession sessions[]  = null;

  WakeOnConnection(PoolCnxNetwork network,
		   NetSession sessions[]) throws IOException {
    super("WakeOnConnection#" + Server.getServerId());
    this.network = network;
    this.sessions = sessions;
    // In order to permit the Agent server ending.
    setDaemon(false);
    listenSocket = network.createServerSocket(Server.networkServers[Server.getServerId()].port);
    start();
  }

  public void run() {
    Socket sock = null;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    Object msg = null;

    while (Server.isRunning) {
      try {
	canStop = true;

	// Get the connection
	try {
	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": wait connection", false);

	  sock = listenSocket.accept();
	} catch (IOException exc) {
	  exc.printStackTrace();
	  continue;
	}
	canStop = false;

	network.setSocketOption(sock);
	// Be careful: The OOS should be initialized first in order to
	// send the header waited by OIS at the other end
	oos = network.getOutputStream(sock);
	ois = network.getInputStream(sock);

	msg = ois.readObject();

	if (msg instanceof Boot) {
	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": connection setup from #" +
			((Boot)msg).sid, false);

	  sessions[((Boot)msg).sid].start(sock, ois, oos);
	} else {
	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace(getName() + ": bad connection setup", false);

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
	  Debug.trace(getName() + ": bad connection setup", exc);
      }
    }
  }
}

final class NetServerOut2 extends Thread {
  PoolCnxNetwork network;
  volatile boolean canStop;
  MessageQueue qout;
  NetSession sessions[] = null;

  NetServerOut2(PoolCnxNetwork network,
		  MessageQueue qout,
		  NetSession sessions[]) {
    super("NetServerOut#" + Server.getServerId());
    this.network = network;
    this.qout = qout;
    this.sessions = sessions;
    // In order to permit the Agent server ending.
    setDaemon(false);
    canStop = true;
    start();
  }

  public void run() {
    int ret;
    Message msg = null;
      
    while (Server.isRunning) {
      canStop = true;

      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": waiting message", false);

      msg = (Message) Server.qout.get();
      canStop = false;

      if (Debug.debug && Debug.sendMessage)
	Debug.trace(getName() + ": try to send message -> " + msg.to.to, false);
      // Send the message
      sessions[msg.to.to].send(msg);
      Server.qout.pop();
    }
  }
}

class WatchDog2 extends Thread {
  /** Use to synchronize thread */
  private Object lock;

  PoolCnxNetwork network;
  NetSession sessions[] = null;

  WatchDog2(PoolCnxNetwork network,
	      NetSession sessions[]) {
    super("WatchDog2#" + Server.getServerId());
    this.network = network;
    this.sessions = sessions;
    // In order to permit the Agent server ending.
    setDaemon(false);
    lock = new Object();
    start();
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
      while (Server.isRunning) {
	try {
	  lock.wait(Network.WDActivationPeriod);
	} catch (InterruptedException exc) {
	  continue;
	}

	if (! Server.isRunning) break;

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
