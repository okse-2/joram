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

/**
 *  <code>SingleCnxNetwork</code> is an implementation of <code>StreamNetwork</code>
 * class for stream sockets.
 * 
 */
class SingleCnxNetwork extends StreamNetwork {
  /** RCS version number of this file: $Revision: 1.2 $ */
  public static final String RCS_VERSION="@(#)$Id: SingleCnxNetwork.java,v 1.2 2001-05-04 14:54:53 tachkeni Exp $";

  Vector sendList;

  final static boolean TempNetwallFix = false;


  /**
   * Numbers of attempt to connect to a server's socket before aborting.
   */
  final static int CnxRetry = 3;

  /**
   * Creates a new network component.
   */
  SingleCnxNetwork() {
    super();
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
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " starting.", false);

    try {
      if (isRunning())
	throw new IOException("Consumer already running.");

      sendList = new Vector();
    
      netServerIn = new NetServerIn(getName());
      netServerOut = new NetServerOut(getName());
      watchDog = new WatchDog(getName());

      netServerIn.start();
      netServerOut.start();
      watchDog.start();
    } catch (IOException exc) {
      Debug.trace(getName(), exc);
      throw exc;
    }

    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " started", false);
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
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning &&
	(netServerOut != null) && netServerOut.isRunning &&
	(watchDog != null) && watchDog.isRunning)
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
    NetServerOut(String name) {
      super(name + ".NetServerOut");
    }

    void shutdown() {}

    public void run() {
      int ret;
      Message msg = null;
      short msgto;
      ServerDesc server = null;

      try {
	while (isRunning) {
	  try {
	    canStop = true;

	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + ": waiting message", false);

	    try {
	      msg = qout.get();
	    } catch (InterruptedException exc) {
	      continue;
	    }
	    canStop = false;

	    msgto = msg.update.getToId();
	    server = AgentServer.getServerDesc(msgto);

	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + ": try to send message -> " +
			  msg + "/" + msgto, false);

	    if (! server.active) {
	      if (Debug.debug && Debug.message)
		Debug.trace("Server#" + msgto + "inactive", false);
	      throw new ConnectException("Host is down");
	    }
	  
	    // Open the connection.
	    Socket socket = null;
	    for (int i=0;;) {
	      try {
		if (Debug.debug && Debug.message)
		  Debug.trace("Try to connect", false);
	      
		socket = createSocket(server.getAddr(), server.port);
		if (Debug.debug && Debug.message)
		  Debug.trace("Connected", false);
		break;
	      } catch (IOException exc) {
		if (Debug.debug && Debug.message)
		  Debug.trace("Connection aborted", exc);
		if ((server.getAddr() == null) || 
		    ((i += 1) > CnxRetry)) {
		  server.active = false;
		  server.last = System.currentTimeMillis();
		  server.retry += 1;
		  throw exc;
		}
	      }
	    }
	    setSocketOption(socket);

	    if (Debug.debug && Debug.message)
	      Debug.trace("Write message", false);
	    // Send the message,
	    ObjectOutputStream oos = getOutputStream(socket);
	    // AF: Configuration coherency verification.
	    if (TempNetwallFix) {
	      oos.writeObject(mclock.getNetU1(msgto));
	      oos.writeObject(mclock.getNetU2(msgto));
	    }
	    oos.writeObject(msg);

	    if (Debug.debug && Debug.message)
	      Debug.trace("Wait ack", false);
	    // and wait the acknowledge.
	    InputStream is = socket.getInputStream();
	    if ((ret = is.read()) == -1)
	      throw new ConnectException("Connection broken");

	    if (Debug.debug && Debug.message)
	      Debug.trace("Receive ack", false);
	
	    oos.close();
	    is.close();
	    socket.close();
	  } catch (IOException exc) {
	    if (Debug.debug && Debug.message)
	      Debug.trace("Move msg in watchdog list", exc);
	    //  There is a connection problem, put the message in a
	    // waiting list.
	    sendList.addElement(msg);
	    qout.pop();
	    continue;
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
	    Debug.trace("Unrecoverable exception", exc);
	    //  There is an unrecoverable exception during the transaction
	    // we must exit from server.
	    AgentServer.stop();
	  }
	}
      } finally {
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(this.getName() + ": stopped", false);

	isRunning = false;
	thread = null;
     }
    }
  }

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;

    WatchDog watchDog = null;

    NetServerIn(String name) throws IOException {
      super(name + ".NetServerIn");
      listen = createServerSocket();
    }

    void shutdown() {
      try {
	listen.close();
      } catch (IOException exc) {}
      listen = null;
    }

    public void run() {
      Socket socket = null;
      OutputStream os = null;
      ObjectInputStream ois = null;

      try {
	while (isRunning) {
	  try {
	    canStop = true;

	    // Get the connection
	    try {
	      if (Debug.debug && Debug.message)
		Debug.trace(this.getName() + "Wait connection", false);

	      socket = listen.accept();
	    } catch (IOException exc) {
	      continue;
	    }
	    canStop = false;

	    setSocketOption(socket);
// 	    socket.setSoLinger(true, 1000);

	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + "Read message", false);

	    // Read the message,
	    os = socket.getOutputStream();
	    ois = getInputStream(socket);

// AF: Configuration coherency verification - Netwall temporary fix.
// Si le serveur emetteur a "crashe", le serveur destinataire est cense
// avoir une connaissance plus "complete" du passe que celui-ci, donc:
//   He[e, r] < Hr[e, r] => e a crashe
//   ou He[e, r] = nb msg emis /e vers r (connaissance de e)
//   et Hr[e, r] = nb msg recu /r depuis e (connaissance de r)
//
// De meme, si le serveur destinataire a "crashe", alors le serveur
// emetteur aura une connaissance plus "complete" du passe que celui-ci,
// donc:
//   He[r, e] > Hr[r, e] => r a crashe
//   ou He[r, e] = nb msg recu /e depuis r (connaissance de e)
//   et Hr[r, e] = nb msg emis /r vers e(connaissance de r)
//
// Dans tous les cas, la connection doit etre refusee.

	    if (TempNetwallFix) {
	      Update u1 = (Update) ois.readObject();
	      Update u2 = (Update) ois.readObject();
	      if (mclock.testNU(u1, u2)) {
		throw new ConnectException("Bad serial version id.");
	      }
	    }
	    Object obj = ois.readObject();

	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + "Message read", false);

	    if (obj instanceof Message) {
	      deliver((Message) obj);
	    } else if (obj instanceof Boot) {
	      Boot boot = (Boot) obj;
	      // It's a valid boot message, set the server on.
	      AgentServer.getServerDesc(boot.sid).active = true;
	      AgentServer.getServerDesc(boot.sid).retry = 0;

	      if (Debug.debug && Debug.network)
		Debug.trace(this.getName() + "Get connection with server#" +
			    boot.sid, false);

	      // resend all waiting messages.
	      wakeup();
	    } else {
	      // TODO: ?
	    }

	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + "Send ack", false);

	    // then send the acknowledge.
	    os.write((byte) 0);
	    os.flush();	// nop !
	  } catch (Exception exc) {
	    Debug.trace(this.getName(), exc);
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
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(this.getName() + ": stopped", false);

	isRunning = false;
	thread = null;
      }
    }
  }


  final class WatchDog extends Daemon {
    /** Use to synchronize thread */
    private Object lock;

    WatchDog(String name) {
      super(name + ".WatchDog");
      lock = new Object();
    }

    void shutdown() {
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
      
      synchronized (lock) {
	try {
	  while (isRunning) {
	    try {
	      if (Debug.debug && Debug.message)
		Debug.trace(this.getName() + " waiting...", false);
	      lock.wait(WDActivationPeriod);
	    } catch (InterruptedException exc) {
	      continue;
	    }
	    
	    if (! isRunning) break;
	    long currentTimeMillis = System.currentTimeMillis();

	    for (int i=0; i<sendList.size(); i++) {
	      msg = (Message) sendList.elementAt(i);

	      msgto = msg.update.getToId();
	      server = AgentServer.getServerDesc(msgto);

	      if (Debug.debug && Debug.message)
		Debug.trace("Check msg#" + msg.update.stamp +
			    " from " + msg.from +
			    " to " + msg.to, false);

	      if ((server.active) ||
		  ((server.retry < WDNbRetryLevel1) && 
		   ((server.last + WDRetryPeriod1) < currentTimeMillis)) ||
		  ((server.retry < WDNbRetryLevel2) &&
		   ((server.last + WDRetryPeriod2) < currentTimeMillis)) ||
		  ((server.last + WDRetryPeriod3) < currentTimeMillis)) {
		if (Debug.debug && Debug.message)
		  Debug.trace("send msg#" + msg.update.stamp, false);

		server.last = currentTimeMillis;

		try {
		  // Open the connection.
		  InetAddress addr = null;
		  Socket socket = null;
		  for (int j=0;;) {
		    try {
		      if (Debug.debug && Debug.message)
			Debug.trace("Try to connect", false);
		      addr = server.getAddr();
		      socket = createSocket(addr, server.port);
		      server.active = true;
		      server.retry = 0;
		      if (Debug.debug && Debug.message)
			Debug.trace("Connected", false);
		      break;
		    } catch (IOException exc) {
		      if (Debug.debug && Debug.message)
			Debug.trace("Connection aborted", exc);
		      if ((addr == null) || ((j += 1) > CnxRetry))
			throw exc;
		    }
		  }
		  setSocketOption(socket);

		  if (Debug.debug && Debug.message)
		    Debug.trace("Write message", false);

		  // Send the message,
		  ObjectOutputStream oos = getOutputStream(socket);
		  // AF: Configuration coherency verification.
		  if (TempNetwallFix) {
		    oos.writeObject(mclock.getNetU1(msgto));
		    oos.writeObject(mclock.getNetU2(msgto));
		  }
		  oos.writeObject(msg);

		  if (Debug.debug && Debug.message)
		    Debug.trace("Wait ack", false);

		  // and wait the acknowledge.
		  InputStream is = socket.getInputStream();
		  if ((ret = is.read()) == -1)
		    throw new ConnectException("Connection broken");

		  if (Debug.debug && Debug.message)
		    Debug.trace("Receive ack", false);
	    
		  oos.close();
		  is.close();
		  socket.close();

		  AgentServer.transaction.begin();
		  //  Deletes the processed notification
		  sendList.removeElementAt(i); i--;
		  msg.delete();
		  AgentServer.transaction.commit();
		  AgentServer.transaction.release();
		} catch (SocketException exc) {
		  if (Debug.debug && Debug.message)
		    Debug.trace("Let msg in watchdog list", exc);

		  server.active = false;
		  server.last = System.currentTimeMillis();
		  server.retry += 1;
		  //  There is a connection problem, let the message in the
		  // waiting list.
		} catch (Exception exc) {
		  Debug.trace("WatchDog", exc);
		}
	      }
	    }
	  }
	} finally {
	  if (Debug.debug && Debug.A3Server)
	    Debug.trace(this.getName() + ": stopped", false);

	  isRunning = false;
	  thread = null;
	}
      }
    }
  }
}
