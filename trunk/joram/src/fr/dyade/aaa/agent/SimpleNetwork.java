/*
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;

/**
 *  <code>SimpleNetwork</code> is a simple implementation of
 * <code>StreamNetwork</code> class with a single connection at
 * a time.
 */
public class SimpleNetwork extends StreamNetwork {
  /** FIFO list of all messages to be sent by the watch-dog thread. */
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
      AgentServer.getTransaction().commit(true);
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
    NetworkOutputStream nos = null;

    NetServerOut(String name, Logger logmon) {
      super(name + ".NetServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.setThreadGroup(AgentServer.getThreadGroup());
    }

    protected void close() {}

    protected void shutdown() {}

    public void run() {
      Message msg = null;
      Set servers = new HashSet();
      try {
        try {
          nos = new NetworkOutputStream();
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL, getName() + ", cannot start.");
          return;
        }

        while (running) {
          canStop = true;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");
            msg = qout.get(WDActivationPeriod);
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
            continue;
          }
          canStop = false;
          if (! running) break;

          if (msg != null) {
            sendList.addMessage(msg);
            qout.pop();
          }
          long currentTimeMillis = System.currentTimeMillis();
          ServerDesc server = null;

          for (int i = 0; i < sendList.size(); i++) {
            msg = sendList.getMessageAt(i);
            short msgto = msg.getDest();

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG,
                              this.getName() +
                              ", check msg#" + msg.getStamp() +
                              " from " + msg.from +
                              " to " + msg.to);

            if (msg.not.expiration > 0L && msg.not.expiration < currentTimeMillis) {
              
              // Remove the message.
              AgentServer.getTransaction().begin();

              if (msg.not.deadNotificationAgentId != null) {
                if (logmon.isLoggable(BasicLevel.DEBUG)) {
                  logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification " + msg.from
                      + ", " + msg.not + " to " + msg.not.deadNotificationAgentId);
                }
                ExpiredNot expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
                Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot));
                Channel.validate();
              } else {
                if (logmon.isLoggable(BasicLevel.DEBUG)) {
                  logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification " + msg.from + ", "
                      + msg.not);
                }
              }

              // Deletes the processed notification
              sendList.removeMessageAt(i); i--;
    // AF: A reprendre.
//               // send ack in JGroups to delete msg
//               if (jgroups != null)
//                 jgroups.send(new JGroupsAckMsg(msg));
              msg.delete();
              msg.free();
              AgentServer.getTransaction().commit(true);
              continue;
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
//              // send ack in JGroups to delete msg
//               if (jgroups != null)
//                 jgroups.send(new JGroupsAckMsg(msg));
              msg.delete();
              msg.free();
              AgentServer.getTransaction().commit(true);
              continue;
            }

            if (servers.contains(server)) {
              // The server has already been tested during this round
              continue;
            }

            this.logmon.log(BasicLevel.DEBUG,
                            this.getName() + server.active + ',' +
                            server.retry + ',' +
                            server.last + ',' +
                            currentTimeMillis);

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
                server.last = currentTimeMillis;

                setSocketOption(socket);

                send(socket, msg, currentTimeMillis);
              } catch (SocketException exc) {
                if (this.logmon.isLoggable(BasicLevel.WARN))
                  this.logmon.log(BasicLevel.WARN,
                                  this.getName() + ", let msg in watchdog list",
                                  exc);
                server.active = false;
                server.retry++;
                server.last = currentTimeMillis;
                
                // Do not send following messages to this server
                servers.add(server);
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
//               // send ack in JGroups to delete msg
//               if (jgroups != null)
//                 jgroups.send(new JGroupsAckMsg(msg));
              msg.delete();
              msg.free();
              AgentServer.getTransaction().commit(true);
            } else {
              // Do not send following messages to this server
              servers.add(server);
            }
          }
          servers.clear();
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

    void send(Socket socket, Message msg, long currentTimeMillis) throws IOException {
      InputStream is = null;
      OutputStream os = null;

      try {
        os = socket.getOutputStream();
        // Send the message,
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", write message");
        nos.writeMessage(os, msg, currentTimeMillis);
        // and wait the acknowledge.
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", wait ack");
        is = socket.getInputStream();
        if (is.read() == -1)
          throw new ConnectException("Connection broken");

        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG,
                          this.getName() + ", receive ack");
      } finally {
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
  }

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;

    NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn");
      // Create the listen socket in order to verify the port availability.
      listen = createServerSocket();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
      this.setThreadGroup(AgentServer.getThreadGroup());
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
      InputStream is = null;
      OutputStream os = null;

      try {
        // After a stop we needs to create anew the listen socket.
        if (listen == null) {
          // creates a server socket listening on configured port
          listen = createServerSocket();
        }

        NetworkInputStream nis = new NetworkInputStream();
        while (running) {
          try {
            canStop = true;

            // Get the connection
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting connection");
              socket = listen.accept();
            } catch (IOException exc) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
              continue;
            }
            canStop = false;

            setSocketOption(socket);

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");

            // Read the message,
            os = socket.getOutputStream();
            is = socket.getInputStream();

            Message msg = nis.readMessage(is);

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", msg received");

            deliver(msg);

            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", send ack");

            // then send the acknowledge.
            os.write(0);
            os.flush();
          } catch (Exception exc) {
            this.logmon.log(BasicLevel.ERROR, this.getName() + ", closed", exc);
          } finally {
            nis.clean();
            try {
              if (os != null) os.close();
            } catch (Exception exc) {}
            os = null;
            try {
              if (is != null) is.close();
            } catch (Exception exc) {}
            is = null;
            try {
              if (socket != null) socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } catch (IOException exc) {
        this.logmon.log(BasicLevel.ERROR,
                        this.getName() + ", bad socket initialization", exc);
      } finally {
        finish();
      }
    }
  }

  final class NetworkOutputStream extends BufferedMessageOutputStream {
    NetworkOutputStream() throws IOException {
      super();
    }

    /**
     * Writes the protocol header to this output stream.
     */
    protected void writeHeader() {
      writeInt(getBootTS());
    }

    /**
     * Writes a message to the output stream of the socket.
     * Be careful, the buffer must be large enough to contain the header.
     *
     * @param sock	      The output socket.
     * @param msg 	      The message to write out.
     * @param currentTimeMillis The current time in milliseconds, this
     *        parameter is used to the handling of notification expiration.
     */
    void writeMessage(OutputStream os,
                      Message msg,
                      long currentTimeMillis) throws IOException {
      out = os;
      writeMessage(msg, currentTimeMillis);
    }
  }

  final class NetworkInputStream extends BufferedMessageInputStream {
    NetworkInputStream() {
      super();
    }

    // The boot timestamp of the incoming message.
    int boot;

    /**
     * Reads the protocol header from this output stream.
     */
    protected void readHeader() throws IOException {
      readFully(Message.LENGTH +4);
      // Reads boot timestamp of source server
      boot = readInt();
    }

    /**
     * Reads the message from the input stream.
     *
     * @param is the input stream.
     * @return the incoming message.
     */
    Message readMessage(InputStream is) throws Exception {
      this.in = is;
      Message msg = readMessage();
      testBootTS(msg.getSource(), boot);
      return msg;
    }
  }
}
