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
 *  <code>SimpleNetwork</code> is a simple implementation of
 * <code>StreamNetwork</code> class with a single connection at
 * a time.
 */
public class SimpleNetwork extends StreamNetwork {
  /** FIFO list of all messages to be sent by the watch-dog thead. */
  MessageVector sendList;

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

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws IOException {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {    
      if (sendList == null)
        sendList = new MessageVector(getName(),
                                     AgentServer.getTransaction().isPersistent());

      if (netServerIn == null)
        netServerIn = new NetServerIn(getName(), logmon);
      if (netServerOut == null)
        netServerOut = new NetServerOut(getName(), logmon);

      if (! netServerIn.isRunning()) netServerIn.start();
      if (! netServerOut.isRunning()) netServerOut.start();
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't start", exc);
      throw exc;
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

//   /**
//    * Wakes up the watch-dog thread.
//    */
//   public void wakeup() {
//     if (netServerOut != null) netServerOut.wakeup();
//   }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (netServerIn != null) netServerIn.stop();
    if (netServerOut != null) netServerOut.stop();
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
	(netServerOut != null) && netServerOut.isRunning())
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

    return strbuf.toString();
  }
  
//   /**
//    * Use to clean the qout of all messages to the dead node.
//    *
//    * @param	dead - the unique id. of dead server.
//    */
//   void clean(short dead) {
//     Message msg = null;
    
//     // TODO: Be careful, to the route algorithm!

//     synchronized (lock) {
//       for (int i=0; i<qout.size(); i++) {
//         msg = (Message) qout.getMessageAt(i);
//         if (msg.to.to == dead) {
//           qout.removeMessageAt(i);
//         }
//       }
//     }
//   }
  
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
            msg = qout.get(WDActivationPeriod);
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", interrupted");
            continue;
          }
          canStop = false;
          if (! running) break;

          if (msg != null) {
            msgto = msg.getDest();

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
                //  There is a connection problem, put the message in a
                // waiting list.
                sendList.addMessage(msg);
                qout.pop();
                continue;
              }

              try {
                send(socket, msg);
              } catch (IOException exc) {
                this.logmon.log(BasicLevel.WARN,
                                this.getName() + ", move msg in watchdog list", exc);
                //  There is a problem during network transaction, put the
                // message in waiting list in order to retry later.
                sendList.addMessage(msg);
                qout.pop();
                continue;
              } 
            } catch (UnknownServerException exc) {
              this.logmon.log(BasicLevel.ERROR,
                              this.getName() + ", can't send message: " + msg,
                              exc);
              // Remove the message (see below), may be we have to post an
              // error notification to sender.
            }

            AgentServer.transaction.begin();
            //  Suppress the processed notification from message queue,
            // and deletes it.
            qout.pop();
            msg.delete();
            AgentServer.transaction.commit();
            AgentServer.transaction.release();
          } else {
            watchdog();
          }
        }
      } catch (Exception exc) {
        this.logmon.log(BasicLevel.FATAL,
                        this.getName() + ", unrecoverable exception", exc);
        //  There is an unrecoverable exception during the transaction
        // we must exit from server.
        AgentServer.stop(false);
      } finally {
        finish();
      }
    }

    /*
     *
     * @exception IOException unrecoverable exception during transaction.
     */
    void watchdog() throws IOException {
      ServerDesc server = null;
      long currentTimeMillis = System.currentTimeMillis();

      for (int i=0; i<sendList.size(); i++) {
        Message msg = (Message) sendList.getMessageAt(i);
        short msgto = msg.getDest();

        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() +
                          ", check msg#" + msg.getStamp() +
                          " from " + msg.from +
                          " to " + msg.to);

        try {
          server = AgentServer.getServerDesc(msgto);
        } catch (UnknownServerException exc) {
          this.logmon.log(BasicLevel.ERROR,
                          this.getName() + ", can't send message: " + msg,
                          exc);
          // Remove the message, may be we have to post an error
          // notification to sender.
          AgentServer.transaction.begin();
          // Deletes the processed notification
          sendList.removeMessageAt(i); i--;
          msg.delete();
          AgentServer.transaction.commit();
          AgentServer.transaction.release();

          continue;
        }

        if ((server.active) ||
            ((server.retry < WDNbRetryLevel1) && 
             ((server.last + WDRetryPeriod1) < currentTimeMillis)) ||
            ((server.retry < WDNbRetryLevel2) &&
             ((server.last + WDRetryPeriod2) < currentTimeMillis)) ||
            ((server.last + WDRetryPeriod3) < currentTimeMillis)) {
          try {
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

            send(socket, msg);
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
            continue;
          } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error", exc);
          }

          AgentServer.transaction.begin();
          //  Deletes the processed notification
          sendList.removeMessageAt(i); i--;
          msg.delete();
          AgentServer.transaction.commit();
          AgentServer.transaction.release();
        }
      }
    }

    public void send(Socket socket, Message msg) throws IOException {
      int ret;
      InputStream is = null;

      try {
        // Send the message,
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", write message");
        nos.writeMessage(socket, msg);
        socket.shutdownOutput();
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
            InputStream is = socket.getInputStream();

    byte[] iobuf = new byte[28];
    Message msg = Message.alloc();
    int n = 0;
    do {
      int count = is.read(iobuf, n, 28 - n);
      if (count < 0) throw new EOFException();
      n += count;
     } while (n < 28);

    // Gets sender's AgentId
    msg.from = new AgentId(
      (short) (((iobuf[0] & 0xFF) <<  8) + (iobuf[1] & 0xFF)),
      (short) (((iobuf[2] & 0xFF) <<  8) + (iobuf[3] & 0xFF)),
      ((iobuf[4] & 0xFF) << 24) + ((iobuf[5] & 0xFF) << 16) +
      ((iobuf[6] & 0xFF) <<  8) + ((iobuf[7] & 0xFF) <<  0));
    // Gets adressee's AgentId
    msg.to = new AgentId(
      (short) (((iobuf[8] & 0xFF) <<  8) + (iobuf[9] & 0xFF)),
      (short) (((iobuf[10] & 0xFF) <<  8) + (iobuf[11] & 0xFF)),
      ((iobuf[12] & 0xFF) << 24) + ((iobuf[13] & 0xFF) << 16) +
      ((iobuf[14] & 0xFF) <<  8) + ((iobuf[15] & 0xFF) <<  0));
    // Gets source server id of message
    msg.source = (short) (((iobuf[16] & 0xFF) <<  8) +
                          ((iobuf[17] & 0xFF) <<  0));
    // Gets destination server id of message
    msg.dest = (short) (((iobuf[18] & 0xFF) <<  8) +
                        ((iobuf[19] & 0xFF) <<  0));
    // Gets stamp of message
    msg.stamp = ((iobuf[20] & 0xFF) << 24) + ((iobuf[21] & 0xFF) << 16) +
      ((iobuf[22] & 0xFF) <<  8) + ((iobuf[23] & 0xFF) <<  0);
    // Gets boot timestamp of source server
    msg.boot = ((iobuf[24] & 0xFF) << 24) + ((iobuf[25] & 0xFF) << 16) +
      ((iobuf[26] & 0xFF) <<  8) + ((iobuf[27] & 0xFF) <<  0);
    // Reads notification object
    ois = new ObjectInputStream(is);
    msg.not = (Notification) ois.readObject();

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", msg received");

// 	    if (obj instanceof Message) {
	      deliver((Message) msg);
// 	    } else {
//               this.logmon.log(BasicLevel.ERROR,
//                               this.getName() + ", not a message");
// 	      throw new IOException("Not a message");
// 	    }

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", send ack");

	    // then send the acknowledge.
	    os.write(0);
            socket.shutdownOutput();
	  } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR, 
                            this.getName() + ", closed", exc);
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
      

}
