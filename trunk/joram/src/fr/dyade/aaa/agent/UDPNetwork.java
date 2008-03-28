/*
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.joram.mom.notifications.ExpiredNot;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;

public class UDPNetwork extends Network {

  final static int DATAGRAM_MAX_SIZE = 8000; // bytes

  /** Input component */
  NetServerIn netServerIn = null;

  /** Output component */
  NetServerOut netServerOut = null;

  Hashtable serversInfo = new Hashtable();
  Hashtable messageLastPacket = new Hashtable();

  private DatagramSocket socket;

  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning() && (netServerOut != null)
        && netServerOut.isRunning()) {
      return true;
    } else {
      return false;
    }
  }

  public void init(String name, int port, short[] servers) throws Exception {
    super.init(name, port, servers);
  }

  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    if (netServerIn == null) {
      netServerIn = new NetServerIn(getName(), logmon);
    }
    if (netServerOut == null) {
      netServerOut = new NetServerOut(getName(), logmon);
    }
    if (!netServerIn.isRunning()) {
      netServerIn.start();
    }
    if (!netServerOut.isRunning()) {
      netServerOut.start();
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

  public void stop() {
    if (netServerIn != null) {
      netServerIn.stop();
    }
    if (netServerOut != null) {
      netServerOut.stop();
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  final class ServerInfo {
    int nbPacketsSent = 1;
    int nbPacketsReceived;
    int nbPacketsAck;
    MessageInputStream messageInputStream;
    List messagesToAck = Collections.synchronizedList(new LinkedList());
  }

  final class MessageAndIndex {
    Message msg;
    int index;
    int size;

    public MessageAndIndex(Message msg, int index, int size) {
      this.msg = msg;
      this.index = index;
      this.size = size;
    }
  }

  final class NetServerIn extends Daemon {

    final byte[] buf = new byte[DATAGRAM_MAX_SIZE];
    final DatagramPacket packet = new DatagramPacket(buf, buf.length);

    protected NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn", logmon);
      socket = new DatagramSocket(port);
    }

    protected void close() {
      socket.close();
    }

    protected void shutdown() {
      Enumeration enumSrvInfo = serversInfo.elements();
      while (enumSrvInfo.hasMoreElements()) {
        ServerInfo srvInfo = (ServerInfo) enumSrvInfo.nextElement();
        if (srvInfo.messageInputStream != null) {
          srvInfo.messageInputStream.shutdown();
        }
      }
      close();
    }

    public void run() {
      try {
        while (running) {
          try {
            canStop = true;

            try {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting messages");
              }
              socket.receive(packet);
            } catch (SocketException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting messages interruption ");
              }
              continue;
            }
            canStop = false;
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, this.getName() + ", received message from: " + " "
                  + packet.getAddress() + ":" + packet.getPort());
            }

            ServerInfo srvInfo = ((ServerInfo) serversInfo.get(packet.getSocketAddress()));
            if (srvInfo == null) {
              srvInfo = new ServerInfo();
              serversInfo.put(packet.getSocketAddress(), srvInfo);
            }

            // Reads ack number
            int ackUpTo = ((buf[0] & 0xFF) << 24) + ((buf[1] & 0xFF) << 16) + ((buf[2] & 0xFF) << 8)
                + ((buf[3] & 0xFF) << 0);

            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "MessageInputStream, ack up to " + ackUpTo);
            }

            boolean isNack = false;
            if (ackUpTo < 0) {
              ackUpTo = -ackUpTo - 1;
              isNack = true;
            }

            AgentServer.getTransaction().begin();
            while (!srvInfo.messagesToAck.isEmpty()
                && ((MessageAndIndex) srvInfo.messagesToAck.get(0)).index
                    + ((MessageAndIndex) srvInfo.messagesToAck.get(0)).size - 1 <= ackUpTo) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, "MessageInputStream, clean message "
                    + ((MessageAndIndex) srvInfo.messagesToAck.get(0)).msg);
              }
              MessageAndIndex msgi = (MessageAndIndex) srvInfo.messagesToAck.remove(0);

              //  Suppress the processed notification from message queue,
              // and deletes it.
              msgi.msg.delete();
              msgi.msg.free();
            }
            AgentServer.getTransaction().commit(true);

            if (isNack) {
              synchronized (qout) {
                qout.notify();
              }
            }

            // Ack was not alone in the packet
            if (packet.getLength() > Message.LENGTH) {
              // Reads packet number
              int packetNumber = ((buf[4] & 0xFF) << 24) + ((buf[5] & 0xFF) << 16) + ((buf[6] & 0xFF) << 8)
                  + ((buf[7] & 0xFF) << 0);

              if (packetNumber != srvInfo.nbPacketsReceived + 1) {
                if (packetNumber <= srvInfo.nbPacketsReceived) {
                  if (logmon.isLoggable(BasicLevel.INFO)) {
                    logmon.log(BasicLevel.DEBUG, "MessageInputStream, Already received packet "
                        + packetNumber + "-> Ignored");
                  }
                } else {
                  if (logmon.isLoggable(BasicLevel.INFO)) {
                    logmon.log(BasicLevel.DEBUG, "MessageInputStream, Missing packet -> Send nack "
                        + (srvInfo.nbPacketsReceived + 1));
                  }
                  srvInfo.nbPacketsAck = -(srvInfo.nbPacketsReceived + 1);
                  netServerOut.messageOutputStream.writeAck(srvInfo.nbPacketsAck, packet.getSocketAddress());
                }

              } else {

                srvInfo.nbPacketsReceived++;

                if (logmon.isLoggable(BasicLevel.DEBUG)) {
                  logmon.log(BasicLevel.DEBUG, "MessageInputStream, packet received " + packetNumber);
                }

                if (srvInfo.messageInputStream == null || !srvInfo.messageInputStream.isRunning()) {
                  srvInfo.messageInputStream = new MessageInputStream(srvInfo, logmon);
                  srvInfo.messageInputStream.start();
                }
                srvInfo.messageInputStream.feed(packet);
              }
            }

          } catch (Exception ioe) {
            logmon.log(BasicLevel.ERROR, this.getName(), ioe);
          }
        }
      } finally {
        finish();
      }
    }
  }

  final class NetServerOut extends Daemon {

    MessageOutputStream messageOutputStream = null;

    protected NetServerOut(String name, Logger logmon) throws Exception {
      super(name + ".NetServerOut", logmon);
    }

    protected void close() {
    }

    protected void shutdown() {
      close();
    }

    public void run() {

      Message msg = null;
      short msgto;
      ServerDesc server = null;

      try {

        try {
          messageOutputStream = new MessageOutputStream();
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL, getName() + ", cannot start.");
          return;
        }

        while (running) {

          canStop = true;

          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");
            }
            msg = qout.get(WDActivationPeriod);
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
            }
            continue;
          }

          canStop = false;
          watchdog(msg == null);

          if (msg != null) {

            ExpiredNot expiredNot = null;

            try {
              msgto = msg.getDest();
              server = AgentServer.getServerDesc(msgto);

              if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", try to send message -> " + msg + "/"
                    + msgto);
              }

              if ((msg.not.expiration > 0L) && (msg.not.expiration < System.currentTimeMillis())) {
                if (msg.not.deadNotificationAgentId != null) {
                  if (logmon.isLoggable(BasicLevel.DEBUG)) {
                    logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification1 " + msg.from
                        + ", " + msg.not + " to " + msg.not.deadNotificationAgentId);
                  }
                  expiredNot = new ExpiredNot(msg.not);
                } else {
                  if (logmon.isLoggable(BasicLevel.DEBUG)) {
                    logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification " + msg.from
                        + ", " + msg.not);
                  }
                }
              } else {
                messageOutputStream.writeMessage(new InetSocketAddress(server.getAddr(), server.getPort()),
                    msg, System.currentTimeMillis());
              }
            } catch (UnknownServerException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", can't send message: " + msg, exc);
              // Remove the message (see below), may be we have to post an
              // error notification to sender.
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", can't send message: " + msg, exc);
              // Remove the message (see below), may be we have to post an
              // error notification to sender.
            }

            AgentServer.getTransaction().begin();
            if (expiredNot != null) {
              Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot));
              Channel.validate();
            }
            qout.pop();
            AgentServer.getTransaction().commit(true);

          }
        }
      } catch (Exception exc) {
        this.logmon.log(BasicLevel.FATAL, this.getName() + ", unrecoverable exception", exc);
        //  There is an unrecoverable exception during the transaction
        // we must exit from server.
        AgentServer.stop(false);
      } finally {
        finish();
      }
    }

    private long last = 0L;

    public void watchdog(boolean force) {
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis < (last + WDActivationPeriod) && !force)
        return;
      last = currentTimeMillis;
      Enumeration enuAddr = serversInfo.keys();
      long time = System.currentTimeMillis();
      while (enuAddr.hasMoreElements()) {
        SocketAddress addr = (SocketAddress) enuAddr.nextElement();
        ServerInfo servInfo = (ServerInfo) serversInfo.get(addr);
        synchronized (servInfo.messagesToAck) {
          if (servInfo.messagesToAck.isEmpty()) {
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog send ack.");
              netServerOut.messageOutputStream.writeAck(servInfo.nbPacketsAck, addr);
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", watchdog ack ", exc);
            }
            continue;
          }
          Iterator iterMessages = servInfo.messagesToAck.iterator();
          while (iterMessages.hasNext()) {
            try {
              MessageAndIndex msgi = (MessageAndIndex) iterMessages.next();
              netServerOut.messageOutputStream.rewriteMessage(addr, msgi.msg, time, msgi.index);
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", re-send message " + msgi.msg);
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", re send error ", exc);
            }
          }
        }
      }
    }
  }

  /**
   * Class used to send messages with UDP packets.
   */
  final class MessageOutputStream extends OutputStream {

    private ObjectOutputStream oos = null;
    private byte[] buf = new byte[DATAGRAM_MAX_SIZE];
    private byte[] ackBuf = new byte[8];
    private int count = 0;
    private DatagramPacket packet;
    private int datagramStamp;
    private int size;

    private SocketAddress serverAddr;
    private ServerInfo serverInfo;

    MessageOutputStream() throws IOException {
      oos = new ObjectOutputStream(this);
    }

    void writeMessage(SocketAddress addr, Message msg, long currentTimeMillis) throws IOException {

      serverAddr = addr;
      if (serversInfo.get(serverAddr) == null) {
        serverInfo = new ServerInfo();
        serversInfo.put(serverAddr, serverInfo);
      } else {
        serverInfo = (ServerInfo) serversInfo.get(serverAddr);
      }
      datagramStamp = serverInfo.nbPacketsSent;
      size = 0;

      int idx = msg.writeToBuf(buf, 8);
      // Writes notification attributes
      buf[idx++] = (byte) ((msg.not.persistent ? Message.PERSISTENT : 0) | (msg.not.detachable ? Message.DETACHABLE
          : 0));

      buf[Message.LENGTH + 8] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
      buf[Message.LENGTH + 9] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
      buf[Message.LENGTH + 10] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
      buf[Message.LENGTH + 11] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);

      // Be careful, the stream header is hard-written in buf
      count = Message.LENGTH + 12;

      try {
        if (msg.not.expiration > 0L) {
          msg.not.expiration -= currentTimeMillis;
        }
        oos.writeObject(msg.not);
        oos.flush();
        oos.reset();

        sendPacket();
      } finally {
        if (msg.not.expiration > 0L) {
          msg.not.expiration += currentTimeMillis;
        }
        count = 0;
      }
      serverInfo.messagesToAck.add(new MessageAndIndex(msg, serverInfo.nbPacketsSent, size));
      serverInfo.nbPacketsSent = datagramStamp;
    }

    void writeAck(int ackNumber, SocketAddress addr) throws IOException {
      // Writes the ack for last packet received
      ackBuf[0] = (byte) (ackNumber >>> 24);
      ackBuf[1] = (byte) (ackNumber >>> 16);
      ackBuf[2] = (byte) (ackNumber >>> 8);
      ackBuf[3] = (byte) (ackNumber >>> 0);
      packet = new DatagramPacket(ackBuf, 8, addr);
      socket.send(packet);
    }

    public void write(int b) throws IOException {
      buf[count] = (byte) b;
      count++;
      if (count == DATAGRAM_MAX_SIZE) {
        sendPacket();
        count = 8;
      }
    }

    private void sendPacket() throws IOException {
      // Writes the ack for last packet received
      buf[0] = (byte) (serverInfo.nbPacketsAck >>> 24);
      buf[1] = (byte) (serverInfo.nbPacketsAck >>> 16);
      buf[2] = (byte) (serverInfo.nbPacketsAck >>> 8);
      buf[3] = (byte) (serverInfo.nbPacketsAck >>> 0);

      // Writes the number of the datagram
      buf[4] = (byte) (datagramStamp >>> 24);
      buf[5] = (byte) (datagramStamp >>> 16);
      buf[6] = (byte) (datagramStamp >>> 8);
      buf[7] = (byte) (datagramStamp >>> 0);

      datagramStamp++;
      size++;

      packet = new DatagramPacket(buf, count, serverAddr);

      socket.send(packet);
    }

    void rewriteMessage(SocketAddress addr, Message msg, long currentTimeMillis, int index)
        throws IOException {
      serverAddr = addr;
      datagramStamp = index;

      int idx = msg.writeToBuf(buf, 8);
      // Writes notification attributes
      buf[idx++] = (byte) ((msg.not.persistent ? Message.PERSISTENT : 0) | (msg.not.detachable ? Message.DETACHABLE
          : 0));

      buf[Message.LENGTH + 8] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
      buf[Message.LENGTH + 9] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
      buf[Message.LENGTH + 10] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
      buf[Message.LENGTH + 11] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);

      // Be careful, the stream header is hard-written in buf
      count = Message.LENGTH + 12;

      try {
        if (msg.not.expiration > 0L) {
          msg.not.expiration -= currentTimeMillis;
        }
        oos.writeObject(msg.not);
        oos.reset();
        oos.flush();

        sendPacket();

      } finally {
        if (msg.not.expiration > 0L) {
          msg.not.expiration += currentTimeMillis;
        }
        count = 0;
      }
    }

  }

  final class MessageInputStream extends Daemon {

    private ServerInfo servInfo;
    private PipedInputStream pipeIn;
    private PipedOutputStream pipeOut;

    public MessageInputStream(ServerInfo info, Logger logmon) throws IOException {
      super(name + ".MessageInputStream", logmon);
      this.servInfo = info;
      pipeIn = new PipedInputStream();
      pipeOut = new PipedOutputStream(pipeIn);
    }

    public void feed(DatagramPacket packet) throws IOException {
      pipeOut.write(packet.getData(), packet.getOffset() + 8, packet.getLength() - 8);
      pipeOut.flush();
    }

    public void run() {

      try {
        while (running) {
          try {
            Message message = Message.alloc();

            canStop = true;
            try {
              message.readFromStream(pipeIn);
            } catch (IOException ioe) {
              break;
            }
            canStop = false;

            // Reads notification attributes
            byte octet = (byte) pipeIn.read();
            boolean persistent = ((octet & Message.PERSISTENT) == 0) ? false : true;
            boolean detachable = ((octet & Message.DETACHABLE) == 0) ? false : true;

            // Reads notification object
            ObjectInputStream ois;
            try {
              ois = new ObjectInputStream(pipeIn);
            } catch (StreamCorruptedException exc) {
              logmon.log(BasicLevel.ERROR, "MessageInputStream ", exc);
              continue;
            }

            message.not = (Notification) ois.readObject();
            if (message.not.expiration > 0L) {
              message.not.expiration += System.currentTimeMillis();
            }
            message.not.persistent = persistent;
            message.not.detachable = detachable;
            message.not.detached = false;

            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "MessageInputStream, msg received " + message);
            }

            deliver(message);

            pipeIn.read();
            servInfo.nbPacketsAck = servInfo.nbPacketsReceived;
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, "MessageInputStream ", exc);
            break;
          }
        }
      } finally {
        finish();
      }

    }

    protected void close() {
      try {
        pipeIn.close();
        pipeOut.close();
      } catch (IOException exc) {
        logmon.log(BasicLevel.ERROR, "MessageInputStream ", exc);
      }
    }

    protected void shutdown() {
      close();
    }
  }

}
