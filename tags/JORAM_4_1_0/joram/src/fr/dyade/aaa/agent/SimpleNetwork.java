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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  <code>SimpleCnxNetwork</code> is a simple implementation of
 * <code>FifoNetwork</code> class with a single connection at
 * a time.
 */
public class SimpleNetwork extends FIFONetwork {
  /**
   * FIFO list of all messages to be sent by the watch-dog thead.
   */
  Vector sendList;

  /**
   * Creates a new network component.
   */
  public SimpleNetwork() {
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
      if (sendList == null)
        sendList = new Vector();
    
      if (netServerIn == null)
        netServerIn = new NetServerIn(getName(), logmon);
      if (netServerOut == null)
        netServerOut = new NetServerOut(getName(), logmon);
      if (watchDog == null)
        watchDog = new WatchDog(getName(), logmon);

      if (! netServerIn.isRunning()) netServerIn.start();
      if (! netServerOut.isRunning()) netServerOut.start();
      if (! watchDog.isRunning()) watchDog.start();
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
    NetOutputStream nos = null;

    NetServerOut(String name, Logger logmon) {
      super(name + ".NetServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.setThreadGroup(AgentServer.getThreadGroup());
    }

    protected void close() {}

    protected void shutdown() {}

    public void run() {
      int ret;
      Message msg = null;
      short msgto;
      ServerDesc server = null;
      InputStream is = null;

      try {
        try {
          nos = new NetOutputStream();
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL,
                     getName() + ", cannot start.");
          return;
        }

        loop:
	while (running) {
          canStop = true;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", waiting message");
            msg = qout.get();
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", interrupted");
            continue;
          }
          canStop = false;
          if (! running) break;

          msgto = msg.getToId();

          Socket socket = null;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", try to send message -> " +
                              msg + "/" + msgto);
            // Can throw an UnknownServerException...
            server = AgentServer.getServerDesc(msgto);

            try {
              if (! server.active) {
                if (this.logmon.isLoggable(BasicLevel.DEBUG))
                  this.logmon.log(BasicLevel.DEBUG,
                                  this.getName() + ", AgentServer#" + msgto + " is down");
                throw new ConnectException("AgentServer#" + msgto + " is down");
              }
	  
              // Open the connection.
              try {
                socket = createSocket(server);
              } catch (IOException exc) {
                this.logmon.log(BasicLevel.WARN,
                                this.getName() + ", connection refused", exc);
                server.active = false;
                server.last = System.currentTimeMillis();
                server.retry += 1;
                throw exc;
              }
              setSocketOption(socket);
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.WARN,
                              this.getName() + ", move msg in watchdog list", exc);
              if (msg.isPersistent()) {
                //  There is a connection problem, put the message in a
                // waiting list.
                // Be careful, if the message is not persistent, a new sending
                // may cause a duplication
                sendList.addElement(msg);
              }
              qout.pop();
              continue;
            }

            try {
              // Send the message,
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", write message");
              nos.writeObject(socket, msg);

              // and wait the acknowledge.
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", wait ack");
              is = socket.getInputStream();
              if ((ret = is.read()) == -1)
                throw new ConnectException("Connection broken");
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", receive ack");
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.WARN,
                              this.getName() + ", move msg in watchdog list", exc);
              if (msg.isPersistent()) {
                //  There is a problem during network transaction, put the
                // message in waiting list in order to retry later.
                // Be careful, if the message is not persistent, a new sending
                // may cause a duplication
                sendList.addElement(msg);
              }
              qout.pop();
              continue;
            } finally {
              try {
                socket.getOutputStream().close();
              } catch (IOException exc) {}
              try {
                is.close();
              } catch (IOException exc) {}
              try {
                socket.close();
              } catch (IOException exc) {}
            }
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
            AgentServer.stop(false);
            break loop;
	  }
	}
      } finally {
        finish();
      }
    }
  }

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;

    NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn");
      listen = createServerSocket();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.setThreadGroup(AgentServer.getThreadGroup());
    }

    protected void close() {
      try {
	listen.close();
      } catch (Exception exc) {}
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

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");

	    // Read the message,
	    os = socket.getOutputStream();
	    ois = new ObjectInputStream(socket.getInputStream());

	    Object obj = ois.readObject(); 

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", msg received");

	    if (obj instanceof Message) {
	      deliver((Message) obj);
	    } else {
              this.logmon.log(BasicLevel.ERROR,
                              this.getName() + ", not a message");
	      throw new IOException("Not a message");
	    }

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", send ack");

	    // then send the acknowledge.
	    os.write(0);
            socket.shutdownOutput();
	  } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR, ", closed", exc);
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
    NetOutputStream nos = null;

    WatchDog(String name, Logger logmon) {
      super(name + ".WatchDog");
      lock = new Object();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.setThreadGroup(AgentServer.getThreadGroup());
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
      InputStream is = null;
      
      try {
        try {
          nos = new NetOutputStream();
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL,
                     getName() + ", cannot start.");
          return;
        }

        loop:
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
	      msgto = msg.getToId();

	      if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                           this.getName() +
                           ", check msg#" + msg.getStamp() +
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
                                    ", send msg#" + msg.getStamp());

                  server.last = currentTimeMillis;

		  // Open the connection.
		  Socket socket = null;
                  try {
                    socket = createSocket(server);
                    // The connection is ok, reset active and retry flags.
                    server.active = true;
                    server.retry = 0;
                  } catch (IOException exc) {
                    this.logmon.log(BasicLevel.WARN,
                                    this.getName() + ", connection refused",
                                    exc);
                    throw exc;
		  }
		  setSocketOption(socket);

                  try {
                    // Send the message,
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", write message");
                    nos.writeObject(socket, msg);
                  
                    // and wait the acknowledge.
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", wait ack");
                    is = socket.getInputStream();
                    if ((ret = is.read()) == -1)
                      throw new ConnectException("Connection broken");
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", receive ack");
                  } finally {
                    try {
                      socket.getOutputStream().close();
                    } catch (IOException exc) {}
                    try {
                      is.close();
                    } catch (IOException exc) {}
                    try {
                      socket.close();
                    } catch (IOException exc) {}
                  }

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
                    AgentServer.stop(false);
                    break loop;
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
                  AgentServer.stop(false);
                  break loop;
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
