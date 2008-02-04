/*
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
import java.util.Enumeration;

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

  private JGroups jgroups = null;

  public void setJGroups(JGroups jgroups) {
    this.jgroups = jgroups;
  }
  
  void ackMsg(JGroupsAckMsg ack) {
    try {
      AgentServer.getTransaction().begin();
      //  Deletes the processed notification
      qout.remove(ack.getStamp());
      ack.delete();
      AgentServer.getTransaction().commit();
      AgentServer.getTransaction().release();
      if (this.logmon.isLoggable(BasicLevel.DEBUG))
        this.logmon.log(BasicLevel.DEBUG,
                        this.getName() + ", ackMsg(...) done.");
    } catch (Exception exc) {
      this.logmon.log(BasicLevel.FATAL,
                      this.getName() + ", ackMsg unrecoverable exception",
                      exc);
    }
  }

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
    MessageOutputStream nos = null;

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
          nos = new MessageOutputStream();
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL,
                     getName() + ", cannot start.");
          return;
        }

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

          long currentTimeMillis = System.currentTimeMillis();
          // Try to send waiting messages
          watchdog(currentTimeMillis);

          if (msg != null) {
            msgto = msg.getDest();
            
            Socket socket = null;
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG,
                                this.getName() + ", try to send message -> " +
                                msg + "/" + msgto);

              if ((msg.not.expiration > 0) &&
                  (msg.not.expiration < currentTimeMillis)) {
                throw new ExpirationExceededException();
              }
              
              // Can throw an UnknownServerException...
              server = AgentServer.getServerDesc(msgto);
              try {
                if ((! server.active) ||
                    (server.last > currentTimeMillis)) {
                  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG,
                                    this.getName() + ", AgentServer#" + msgto + " is down");
                  throw new ConnectException("AgentServer#" + msgto + " is down");
                }
                
                // Open the connection.
                try {
                  if (this.logmon.isLoggable(BasicLevel.DEBUG))
                    this.logmon.log(BasicLevel.DEBUG, this.getName() + ", try to connect");

                  for (Enumeration e = server.getSockAddrs(); e.hasMoreElements();) {
                    fr.dyade.aaa.util.SocketAddress sa = 
                      (fr.dyade.aaa.util.SocketAddress) e.nextElement();
                    try {
                      server.moveToFirst(sa);
                      socket = createSocket(server);
                    } catch (IOException ioexc) {
                      this.logmon.log(BasicLevel.DEBUG,
                                      this.getName() + ", connection refused with addr=" + server.getAddr()+
                                      " port=" +  server.getPort() +", try next element");
                      continue;
                    }
                    if (this.logmon.isLoggable(BasicLevel.DEBUG))
                      this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");
                    break;
                  }
                  
                  if (socket == null)
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
                send(socket, msg, currentTimeMillis);
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
            } catch (ExpirationExceededException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() + ": removes expired notification " +
                           msg.from + ", " + msg.not);
            }

            AgentServer.getTransaction().begin();
            //  Suppress the processed notification from message queue,
            // and deletes it.
            qout.pop();
            // send ack in JGroups to delete msg
            if (jgroups != null)
              jgroups.send(new JGroupsAckMsg(msg));
            msg.delete();
            msg.free();
            AgentServer.getTransaction().commit();
            AgentServer.getTransaction().release();
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

//     /** The date of the last watchdog execution. */
//     private long last = 0L;

    /*
     *
     * @exception IOException unrecoverable exception during transaction.
     */
    void watchdog(long currentTimeMillis) throws IOException {
//       this.logmon.log(BasicLevel.DEBUG,
//                       this.getName() + " watchdog().");

//       if (currentTimeMillis < (last + WDActivationPeriod))
//         return;
//       last = currentTimeMillis;

      ServerDesc server = null;

      for (int i=0; i<sendList.size(); i++) {
        Message msg = (Message) sendList.getMessageAt(i);
        short msgto = msg.getDest();

        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() +
                          ", check msg#" + msg.getStamp() +
                          " from " + msg.from +
                          " to " + msg.to);

        if ((msg.not.expiration > 0) &&
            (msg.not.expiration < currentTimeMillis)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       getName() + ": removes expired notification " +
                       msg.from + ", " + msg.not);

          // Remove the message.
          AgentServer.getTransaction().begin();
          // Deletes the processed notification
          sendList.removeMessageAt(i); i--;
// AF: A reprendre.
//           // send ack in JGroups to delete msg
//           if (jgroups != null)
//             jgroups.send(new JGroupsAckMsg(msg));
          msg.delete();
          msg.free();
          AgentServer.getTransaction().commit();
          AgentServer.getTransaction().release();
        }

        try {
          server = AgentServer.getServerDesc(msgto);
        } catch (UnknownServerException exc) {
          this.logmon.log(BasicLevel.ERROR,
                          this.getName() + ", can't send message: " + msg,
                          exc);
          // Remove the message, may be we have to post an error
          // notification to sender.
          AgentServer.getTransaction().begin();
          // Deletes the processed notification
          sendList.removeMessageAt(i); i--;
// AF: A reprendre.
//          // send ack in JGroups to delete msg
//           if (jgroups != null)
//             jgroups.send(new JGroupsAckMsg(msg));
          msg.delete();
          msg.free();
          AgentServer.getTransaction().commit();
          AgentServer.getTransaction().release();

          continue;
        }

        if (server.last > currentTimeMillis) {
          // The server has already been tested during this round
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

            // Open the connection.
            Socket socket = createSocket(server);
            // The connection is ok, reset active and retry flags.
            server.active = true;
            server.retry = 0;
            // Reset last in order to allow sending of following messages
            // to the same server.
            server.last = currentTimeMillis;

            setSocketOption(socket);

            send(socket, msg, currentTimeMillis);
          } catch (SocketException exc) {
            if (this.logmon.isLoggable(BasicLevel.WARN))
              this.logmon.log(BasicLevel.WARN,
                              this.getName() + ", let msg in watchdog list",
                              exc);
            server.active = false;
            server.retry += 1;
            // Set last in order to avoid the sending of following messages to
            // same server.
            server.last = currentTimeMillis +1;
            //  There is a connection problem, let the message in the
            // waiting list.
            continue;
          } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR,
                            this.getName() + ", error", exc);
          }

          AgentServer.getTransaction().begin();
          //  Deletes the processed notification
          sendList.removeMessageAt(i); i--;
// AF: A reprendre.
//           // send ack in JGroups to delete msg
//           if (jgroups != null)
//             jgroups.send(new JGroupsAckMsg(msg));
          msg.delete();
          msg.free();
          AgentServer.getTransaction().commit();
          AgentServer.getTransaction().release();
        } else {
          // Set last in order to avoid the sending of following messages to
          // same server.
          server.last = currentTimeMillis +1;
        }
      }
    }

    public void send(Socket socket,
                     Message msg,
                     long currentTimeMillis) throws IOException {
      int ret;
      InputStream is = null;

      try {
        // Send the message,
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", write message");
        nos.writeMessage(socket, msg, currentTimeMillis);
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
        } catch (Exception exc) {}
        try {
          is.close();
        } catch (Exception exc) {}
        try {
          socket.close();
        } catch (Exception exc) {}
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
      byte[] iobuf = new byte[29];

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

            Message msg = Message.alloc();
            int n = 0;
            do {
              int count = is.read(iobuf, n, Message.LENGTH +4 - n);
              if (count < 0) throw new EOFException();
              n += count;
            } while (n < (Message.LENGTH +4));

            // Reads boot timestamp of source server
            int boot = ((iobuf[0] & 0xFF) << 24) +
              ((iobuf[1] & 0xFF) << 16) +
              ((iobuf[2] & 0xFF) <<  8) +
              ((iobuf[3] & 0xFF) <<  0);
            
            int idx = msg.readFromBuf(iobuf, 4);

            // Reads notification attributes
            boolean persistent = ((iobuf[idx] & Message.PERSISTENT) == 0)?false:true;
            boolean detachable = ((iobuf[idx] & Message.DETACHABLE) == 0)?false:true;

            // Reads notification object
            ois = new ObjectInputStream(is);
            msg.not = (Notification) ois.readObject();
            if (msg.not.expiration > 0)
              msg.not.expiration += System.currentTimeMillis();
            msg.not.persistent = persistent;
            msg.not.detachable = detachable;
            msg.not.detached = false;

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() + ", msg received");

            testBootTS(msg.getSource(), boot);
            deliver(msg);

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", send ack");

	    // then send the acknowledge.
	    os.write(0);
            os.flush();
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

  /**
   * Class used to send messages through a TCP stream.
   */
  final class MessageOutputStream extends ByteArrayOutputStream {
    private ObjectOutputStream oos = null;
    private OutputStream os = null;

    MessageOutputStream() throws IOException {
      super(256);
      oos = new ObjectOutputStream(this);
      count = 0;
      buf[Message.LENGTH +4] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
      buf[Message.LENGTH +5] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
      buf[Message.LENGTH +6] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
      buf[Message.LENGTH +7] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);
    }

    void writeMessage(Socket sock,
                      Message msg,
                      long currentTimeMillis) throws IOException {
      os = sock.getOutputStream();

      // Writes boot timestamp of source server
      buf[0] = (byte) (getBootTS() >>>  24);
      buf[1] = (byte) (getBootTS() >>>  16);
      buf[2] = (byte) (getBootTS() >>>  8);
      buf[3] = (byte) (getBootTS() >>>  0);

      int idx = msg.writeToBuf(buf, 4);
      // Writes notification attributes
      buf[idx++] = (byte) ((msg.not.persistent?Message.PERSISTENT:0) |
                           (msg.not.detachable?Message.DETACHABLE:0));

      // Be careful, the stream header is hard-written in buf
      count = Message.LENGTH +8;

      try {
        if (msg.not.expiration > 0)
          msg.not.expiration -= currentTimeMillis;
        oos.writeObject(msg.not);
        oos.reset();
        oos.flush();
        os.write(buf, 0, count);;
        os.flush();
      } finally {
        if (msg.not.expiration > 0)
          msg.not.expiration += currentTimeMillis;
        count = 0;
      }
    }
  }
}
