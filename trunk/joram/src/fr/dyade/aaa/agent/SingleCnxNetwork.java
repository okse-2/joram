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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  <code>SingleCnxNetwork</code> is a simple implementation of
 * <code>StreamNetwork</code> class with a single connection at
 * a time.
 */
class SingleCnxNetwork extends StreamNetwork {
  /** RCS version number of this file: $Revision: 1.7 $ */
  public static final String RCS_VERSION="@(#)$Id: SingleCnxNetwork.java,v 1.7 2002-03-26 16:08:39 joram Exp $";

  Vector sendList;

  final static boolean TempNetwallFix = false;

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
	    // AF: Configuration coherency verification.
	    if (TempNetwallFix) {
	      oos.writeObject(mclock.getNetU1(msgto));
	      oos.writeObject(mclock.getNetU2(msgto));
	    }
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

    WatchDog watchDog = null;

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
		  // AF: Configuration coherency verification.
		  if (TempNetwallFix) {
		    oos.writeObject(mclock.getNetU1(msgto));
		    oos.writeObject(mclock.getNetU2(msgto));
		  }
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
