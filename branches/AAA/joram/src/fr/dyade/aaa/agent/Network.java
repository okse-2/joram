/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
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

// History:
// 	v1.0   	10/28/97	Initial version.
// 	v1.1	12/28/97	Add big message handling.
//	v1.2	02/02/98	Use TCP protocol.

final class Boot implements Serializable {
  short sid;

  Boot() {
    this.sid = Server.serverId;
  }
}

final class Network {

  /** RCS version number of this file: $Revision: 1.1.1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Network.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";

  Vector sendList;
  Vector recvList;

  MatrixClock mclock;

  final static boolean TempNetwallFix = false;

  /**
   * Numbers of attempt to connect to a server's socket before aborting.
   */
  final static int CnxRetry = 3;

  Network(MatrixClock mclock) throws Exception {
    this.mclock = mclock;
    if ((Server.networkServers != null) &&
	(Server.networkServers.length > 1)) {
      sendList = new Vector();
      recvList = new Vector();
    }
  }

  NetServerIn netServerIn = null;
  NetServerOut netServerOut = null;
  WatchDog watchDog = null;

  void stop() {
    if ((netServerIn != null) && (netServerIn.canStop)) {
      if (netServerIn.listenSocket != null)
	try {
	  netServerIn.listenSocket.close();
	} catch (IOException exc) {
	  if (Debug.debug)
	    Debug.trace("exception during NetServerIn stop", exc);
	}
    }
    if ((netServerOut != null) && (netServerOut.canStop)) netServerOut.stop();
    if (watchDog != null) watchDog.wakeup();
  }

  void start(MessageQueue qin,
	     MessageQueue qout) throws IOException {
    if ((Server.networkServers == null) ||
	(Server.networkServers.length == 1) ||
	Server.isTransient(Server.getServerId()))
      // Don't need to run network component.
      return;

    netServerIn = new NetServerIn(qin, recvList, mclock);

//     Boot boot = new Boot();
//     for (short i=0; i<Server.networkServers.length; i++) {
//       if ((i != Server.serverId) && (Server.networkServers[i] != null)) {
// 	   send(i, boot);
//       }
//     }

    netServerOut = new NetServerOut(qout, sendList, mclock);
    watchDog = new WatchDog(sendList, mclock);
  }

//   final void send(short id, Object msg) throws IOException {
//     Socket socket = null;
//     ObjectOutputStream oos = null;
//     InputStream is = null;

//     try {
//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Try to connect server#" + id +
// 		    ":" + Server.networkServers[id].hostname +
// 		    "/" + Server.networkServers[id].port, false);
      
//       // Open the connection.
//       socket = new Socket(Server.networkServers[id].getAddr(),
// 			  Server.networkServers[id].port);
//       Server.networkServers[id].active = true;
//       Server.networkServers[id].retry = 0;

//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("connection ok", false);
//       socket.setTcpNoDelay(true);
//       socket.setSoTimeout(0);

//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Send boot message", false);

//       // Send the message,
//       oos = new ObjectOutputStream(socket.getOutputStream());
// // AF: Configuration coherency verification - Netwall temporary fix.
//       if (Network.TempNetwallFix) {
// 	oos.writeObject(mclock.getNetU1(id));
// 	oos.writeObject(mclock.getNetU2(id));
//       }
//       oos.writeObject(msg);

//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Wait ack", false);

//       // and wait the acknowledge.
//       is = socket.getInputStream();
//       if (is.read() == -1)
// 	throw new ConnectException("Connection broken");

//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Recv ack", false);

//       oos.close();
//       is.close();
//       socket.close();
//     } catch (UnknownHostException exc) {
//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Can't resolve \"" + hostname + "\" Inet address", exc);

//       Server.networkServers[id].active = false;
//       Server.networkServers[id].last = System.currentTimeMillis();
//       Server.networkServers[id].retry += 1;
//     } catch (SocketException exc) {
//       if (Debug.debug && Debug.bootNetwork)
// 	Debug.trace("Cannot connect server#" + id, exc);

//       Server.networkServers[id].active = false;
//       Server.networkServers[id].last = System.currentTimeMillis();
//       Server.networkServers[id].retry += 1;
//     }
//   }
}

final class NetServerOut extends Thread {
  volatile boolean canStop;
  MessageQueue qout;
  Vector sendList;
  MatrixClock mclock;

  NetServerOut(MessageQueue qout,
	       Vector sendList,
	       MatrixClock mclock) {
    super("NetServerOut#" + Server.serverId);
    this.qout = qout;
    this.sendList = sendList;
    this.mclock = mclock;
    // In order to permit the Agent server ending.
    setDaemon(false);
    canStop = true;
    start();
  }

  public void run() {
    int ret;
    Message msg = null;
      
    try {
      while (Server.isRunning) {
	try {
	  canStop = true;
	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Waiting message - qout.get()", false);
	  msg = (Message) Server.qout.get();
	  canStop = false;

	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Send msg#" + msg.update.stamp +
			" from " + msg.from +
			" to " + msg.to, false);

	  if (! Server.networkServers[msg.to.to].active) {
	    if (Debug.debug && Debug.sendMessage)
	      Debug.trace("Server#" + msg.to.to + "inactive", false);
	    throw new ConnectException("Host is down");
	  }
	  
	  // Open the connection.
	  Socket socket = null;
	  for (int i=0;;) {
	    try {
	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Try to connect", false);
	      socket = new Socket(Server.networkServers[msg.to.to].getAddr(),
				  Server.networkServers[msg.to.to].port);
	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Connected", false);
	      break;
	    } catch (IOException exc) {
	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Connection aborted", exc);
	      if ((Server.networkServers[msg.to.to].addr == null) || 
		  ((i += 1) > Network.CnxRetry)) {
		Server.networkServers[msg.to.to].active = false;
		Server.networkServers[msg.to.to].last = System.currentTimeMillis();
		Server.networkServers[msg.to.to].retry += 1;
		throw exc;
	      }
	    }
	  }
	  socket.setSoTimeout(0);
	  socket.setTcpNoDelay(true);

	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Write message", false);
	  // Send the message,
	  ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
// AF: Configuration coherency verification.
	  if (Network.TempNetwallFix) {
	    oos.writeObject(mclock.getNetU1(msg.to.to));
	    oos.writeObject(mclock.getNetU2(msg.to.to));
	  }
	  oos.writeObject(msg);

	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Wait ack", false);
	  // and wait the acknowledge.
	  InputStream is = socket.getInputStream();
	  if ((ret = is.read()) == -1)
	    throw new ConnectException("Connection broken");

	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Receive ack", false);
	
	  oos.close();
	  is.close();
	  socket.close();
	} catch (SocketException exc) {
	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Move msg in watchdog list", exc);
	  //  There is a connection problem, put the message in a
	  // waiting list.
	  Server.transaction.begin();
	  // TODO: Is there any need of a transaction here?
	  sendList.addElement(msg);
	  Server.qout.pop();
	  Server.transaction.commit();
	  Server.transaction.release();
	  continue;
	} 

	if (Debug.debug && Debug.sendMessage)
	  Debug.trace("Remove message in qout", false);
	Server.transaction.begin();
	//  Suppress the processed notification from message queue,
	// and deletes it.
	Server.qout.pop();
	msg.delete();
	Server.transaction.commit();
	Server.transaction.release();
      }
    } catch (Exception exc) {
      Debug.trace("Unrecoverable exception", exc);
      //  There is an unrecoverable exception during the transaction
      // we must exit from server.
      canStop = false;
      Server.stop();
    }
  }
}

final class NetServerIn extends Thread {
  volatile boolean canStop = false;
  ServerSocket listenSocket = null;

  MessageQueue qin;
  Vector recvList;
  MatrixClock mclock;

  NetServerIn(MessageQueue qin,
	      Vector recvList,
	      MatrixClock mclock) throws IOException {
    super("NetServerIn#" + Server.serverId);
    this.qin = qin;
    this.recvList = recvList;
    this.mclock = mclock;
    // In order to permit the Agent server ending.
    setDaemon(false);
    listenSocket = new ServerSocket(Server.networkServers[Server.serverId].port);
    start();
  }

  public void run() {
    Socket socket = null;
    OutputStream os = null;
    ObjectInputStream ois = null;

    while (Server.isRunning) {
      try {
	canStop = true;

	// Get the connection
	try {
	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace("Wait connection", false);

	  socket = listenSocket.accept();
	} catch (IOException exc) {
	  continue;
	}
	canStop = false;

	socket.setTcpNoDelay(true);
	socket.setSoTimeout(0);
	socket.setSoLinger(true, 1000);

	if (Debug.debug && Debug.recvMessage)
	  Debug.trace("Read message", false);

	// Read the message,
	os = socket.getOutputStream();
	ois = new ObjectInputStream(socket.getInputStream());

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

	if (Network.TempNetwallFix) {
	  Update u1 = (Update) ois.readObject();
	  Update u2 = (Update) ois.readObject();
	  if (mclock.testNU(u1, u2)) {
	    throw new ConnectException("Bad serial version id.");
	  }
	}
	Object obj = ois.readObject();

	if (Debug.debug && Debug.recvMessage)
	  Debug.trace("Message read", false);

	if (obj instanceof Message) {
	  Message msg = (Message) obj;

	  if (Debug.debug && Debug.recvMessage)
	    Debug.trace("recv msg#" + msg.update.stamp +
			" from " + msg.from +
			" to " + msg.to, false);
	  // Get real from serverId.
	  short from = msg.from.to;
	  if (Server.isTransient(from))
	    from = Server.transientProxyId(from).to;

	  Server.networkServers[from].active = true;
	  Server.networkServers[from].retry = 0;

	  // Test if the message can be delivered then deliver it
	  // else put it in the waiting list
	  int todo = mclock.testRecvUpdate(from, msg.update);
	  if (todo == MatrixClock.DELIVER) {
	    // Deliver the message then try to deliver alls waiting message.
	    Server.transaction.begin();
	    // Allocate a local time to the message, then save it.
	    msg.update = mclock.getSendUpdate(Server.serverId);
	    msg.save();
	    Server.qin.push(msg);

	    if (Debug.debug && Debug.recvMessage)
	      Debug.trace("deliver msg#" + msg.update.stamp +
			  " from " + msg.from +
			  " to " + msg.to, false);
	  scanlist:
	    while (true) {
	      for (int i=0; i<recvList.size(); i++) {
		Message tmpMsg = (Message) recvList.elementAt(i);
		if (mclock.testRecvUpdate(tmpMsg.from.to, tmpMsg.update) == MatrixClock.DELIVER) {
		  // Allocate a local time to the message, then save it.
		  tmpMsg.update = mclock.getSendUpdate(Server.serverId);
		  tmpMsg.save();
		  //  Deliver the message, then delete it from list.
		  Server.qin.push(tmpMsg);
		  recvList.removeElementAt(i);
		  // local time has changed we have to rescan the list.

		  if (Debug.debug && Debug.recvMessage)
		    Debug.trace("deliver msg#" + tmpMsg.update.stamp +
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
	    // Insert in a waiting list.
	    msg.save();
	    recvList.addElement(msg);

	    if (Debug.debug && Debug.recvMessage)
	      Debug.trace("block msg#" + msg.update.stamp +
			  " from " + msg.from +
			  " to " + msg.to, false);
	  }
//        else it's an already delivered message.
//        TODO: May be we have to print a trace?
	} else if (obj instanceof Boot) {
	  Boot boot = (Boot) obj;
	  // It's a valid boot message, set the server on.
	  Server.networkServers[boot.sid].active = true;
	  Server.networkServers[boot.sid].retry = 0;

	  if (Debug.debug && Debug.bootNetwork)
	    Debug.trace("Get connection with server#" + boot.sid, false);

	  // resend all waiting messages.
	  if (Server.network.watchDog != null)
	    Server.network.watchDog.wakeup();
	} else {
	  // TODO: ?
	}

	if (Debug.debug && Debug.recvMessage)
	  Debug.trace("Send ack", false);

	// then send the acknowledge.
	os.write((byte) 0);
	os.flush();	// nop !
      } catch (Exception exc) {
	Debug.trace("NetServerIn", exc);
      } finally {
	try {
	  if (os != null) {
	    os.close();
	    os = null;
	  }
	  if (ois != null) {
	    ois.close();
	    ois = null;
	  }
 	  if (socket != null) {
	    socket.close();
	    socket = null;
	  }
	} catch (IOException exc) {
	  Debug.trace("NetServerIn", exc);
	}
      }
    }
  }
}

final class WatchDog extends Thread {
  /** Use to synchronize thread */
  private Object lock;

  Vector sendList;
  MatrixClock mclock;

  final static long WDActivationPeriod = 10000L; // 10 seconds
  final static int WDNbRetryLevel1 = 6;
  final static long WDRetryPeriod1 = 15000L;	 // 15 seconds
  final static int WDNbRetryLevel2 = 12;
  final static long WDRetryPeriod2 = 120000L;	 // 2 minutes
  final static long WDRetryPeriod3 = 1800000L;	 // 30 minutes

  WatchDog(Vector sendList, MatrixClock mclock) {
    super("WatchDog#" + Server.serverId);
    this.sendList = sendList;
    this.mclock = mclock;
    // In order to permit the Agent server ending.
    setDaemon(false);
    lock = new Object();
    start();
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
      
    synchronized (lock) {
      while (Server.isRunning) {
	try {
	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Waiting", false);
	  lock.wait(WDActivationPeriod);
	} catch (InterruptedException exc) {
	  continue;
	}

	if (! Server.isRunning) break;
	long currentTimeMillis = System.currentTimeMillis();

	for (int i=0; i<sendList.size(); i++) {
	  msg = (Message) sendList.elementAt(i);

	  if (Debug.debug && Debug.sendMessage)
	    Debug.trace("Check msg#" + msg.update.stamp +
			" from " + msg.from +
			" to " + msg.to, false);

	  if ((Server.networkServers[msg.to.to].active) ||
	      ((Server.networkServers[msg.to.to].retry < WDNbRetryLevel1) && 
	       ((Server.networkServers[msg.to.to].last + WDRetryPeriod1) < currentTimeMillis)) ||
	      ((Server.networkServers[msg.to.to].retry < WDNbRetryLevel2) &&
	       ((Server.networkServers[msg.to.to].last + WDRetryPeriod2) < currentTimeMillis)) ||
	      ((Server.networkServers[msg.to.to].last + WDRetryPeriod3) < currentTimeMillis)) {
	    if (Debug.debug && Debug.sendMessage)
	      Debug.trace("send msg#" + msg.update.stamp +
			  " from " + msg.from +
			  " to " + msg.to, false);

	    Server.networkServers[msg.to.to].last = currentTimeMillis;

	    try {
	      // Open the connection.
	      Socket socket = null;
	      for (int j=0;;) {
		try {
		  if (Debug.debug && Debug.sendMessage)
		    Debug.trace("Try to connect", false);
                  socket = new Socket(Server.networkServers[msg.to.to].getAddr(),
				      Server.networkServers[msg.to.to].port);
		  Server.networkServers[msg.to.to].active = true;
		  Server.networkServers[msg.to.to].retry = 0;
		  if (Debug.debug && Debug.sendMessage)
		    Debug.trace("Connected", false);
		  break;
		} catch (IOException exc) {
		  if (Debug.debug && Debug.sendMessage)
		    Debug.trace("Connection aborted", exc);
		  if ((Server.networkServers[msg.to.to].addr == null) ||
		      ((j += 1) > Network.CnxRetry))
		    throw exc;
		}
	      }
	      socket.setSoTimeout(0);
	      socket.setTcpNoDelay(true);

	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Write message", false);

	      // Send the message,
	      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
// AF: Configuration coherency verification.
	      if (Network.TempNetwallFix) {
		oos.writeObject(mclock.getNetU1(msg.to.to));
		oos.writeObject(mclock.getNetU2(msg.to.to));
	      }
	      oos.writeObject(msg);

	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Wait ack", false);

	      // and wait the acknowledge.
	      InputStream is = socket.getInputStream();
	      if ((ret = is.read()) == -1)
		throw new ConnectException("Connection broken");

	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Receive ack", false);
	    
	      oos.close();
	      is.close();
	      socket.close();

	      Server.transaction.begin();
	      //  Deletes the processed notification
	      sendList.removeElementAt(i); i--;
	      msg.delete();
	      Server.transaction.commit();
	      Server.transaction.release();
	    } catch (SocketException exc) {
	      if (Debug.debug && Debug.sendMessage)
		Debug.trace("Let msg in watchdog list", exc);

	      Server.networkServers[msg.to.to].active = false;
	      Server.networkServers[msg.to.to].last = System.currentTimeMillis();
	      Server.networkServers[msg.to.to].retry += 1;
	      //  There is a connection problem, let the message in the
	      // waiting list.
	    } catch (Exception exc) {
	      Debug.trace("WatchDog", exc);
	    }
	  }
	}
      }
    }
  }
}