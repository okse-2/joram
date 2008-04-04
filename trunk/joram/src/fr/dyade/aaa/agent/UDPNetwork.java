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

  /** The maximum number of bytes of one datagram */
  final static int DATAGRAM_MAX_SIZE = 8000; // bytes

  /** Input component */
  private NetServerIn netServerIn = null;

  /** Output component */
  private NetServerOut netServerOut = null;

  /** An hashtable linking a socket address to some information about datagrams sent/received/acked */
  private Hashtable serversInfo = new Hashtable();

  /** A socket used to send and receive datagrams */
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

  /**
   * Structure storing details about a particular remote network.
   */
  final class ServerInfo {
    
    /** Identifier for the next packet to be send. Number 1 is for handshaking. */
    int nextPacketNumber = 2;
    
    /** Identifier for the last packet received. */
    int lastPacketReceived = 1;
    
    /** Identifier for the last packet received. */
    int lastPacketAck = 0;
    
    /** Object used to deserialize messages sent to this server. */
    MessageInputStream messageInputStream;
    
    /** A FIFO list to store sent messages waiting to be acked. */
    List messagesToAck = Collections.synchronizedList(new LinkedList());
    
    /** Tells if the server responded to the handshake message. */
    boolean handshaken = false; 
  }

  
  /**
   * A particular structure used to remember:
   * <ul>
   * <li> the sent message.</li>
   * <li> the index of the first packet containing the message.</li>
   * <li> the number of packets used to send the message.</li>
   * </ul>
   */
  final class MessageAndIndex {
    
    /** The sent message. */
    Message msg;
    
    /** The index of the first packet containing the message. */
    int index;
    
    /** The number of packets used to send the message. */
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

            // Reads packet number
            int packetNumber = ((buf[4] & 0xFF) << 24) + ((buf[5] & 0xFF) << 16) + ((buf[6] & 0xFF) << 8)
                + ((buf[7] & 0xFF) << 0);

            // Handshake response received
            if (ackUpTo == 1) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, "Handshake response received " + ackUpTo);
              }
              cleanServerInfo(srvInfo, packetNumber);
              continue;
            }

            // Handshake received
            if (packetNumber == 1) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, "Handshake received, send handshake response ");
              }
              cleanServerInfo(srvInfo, ackUpTo);
              // Send handshake response
              netServerOut.messageOutputStream.writeAck(1, packet.getSocketAddress());
              continue;
            }
                      
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, getName() + ", packet received " + packetNumber
                  + ", ack up to " + ackUpTo);
            }

            boolean isNack = false;
            if (ackUpTo < 0) {
              ackUpTo = -ackUpTo - 1;
              isNack = true;
            }

            // Suppress the acked notifications from waiting list and deletes it.
            AgentServer.getTransaction().begin();
            while (!srvInfo.messagesToAck.isEmpty()
                && ((MessageAndIndex) srvInfo.messagesToAck.get(0)).index
                    + ((MessageAndIndex) srvInfo.messagesToAck.get(0)).size - 1 <= ackUpTo) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ", clean message "
                    + ((MessageAndIndex) srvInfo.messagesToAck.get(0)).msg);
              }
              MessageAndIndex msgi = (MessageAndIndex) srvInfo.messagesToAck.remove(0);

              msgi.msg.delete();
              msgi.msg.free();
            }
            AgentServer.getTransaction().commit(true);

            // If nack, unblock netServerOut to launch watchdog
            if (isNack) {
              synchronized (qout) {
                qout.notify();
              }
            }

            // Ack was not alone in the packet
            if (packet.getLength() > Message.LENGTH) {

              if (packetNumber != srvInfo.lastPacketReceived + 1) {
                if (packetNumber <= srvInfo.lastPacketReceived) {
                  if (logmon.isLoggable(BasicLevel.INFO)) {
                    logmon.log(BasicLevel.DEBUG, getName() + ", Already received packet "
                        + packetNumber + "-> Ignored");
                  }
                } else {
                  if (logmon.isLoggable(BasicLevel.INFO)) {
                    logmon.log(BasicLevel.DEBUG, getName() + ", Missing packet -> Send nack "
                        + (srvInfo.lastPacketReceived + 1));
                  }
                  srvInfo.lastPacketAck = -(srvInfo.lastPacketReceived + 1);
                  netServerOut.messageOutputStream.writeAck(srvInfo.lastPacketAck, packet.getSocketAddress());
                }

              } else {

                srvInfo.lastPacketReceived++;

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

    /**
     * Cleans a server information about packets. Restarts datagram packets numbering from
     * beginning. Cleans expired messages.
     * 
     * @param srvInfo
     *            the server information.
     * @param bootstamp
     *            the boot timestamp for the given server.
     * @throws IOException
     * @throws Exception
     */
    private void cleanServerInfo(ServerInfo srvInfo, int bootstamp) throws IOException, Exception {
      if (srvInfo.messageInputStream != null) {
        srvInfo.messageInputStream.shutdown();
      }
      srvInfo.messageInputStream = new MessageInputStream(srvInfo, logmon);
      srvInfo.handshaken = true;
      srvInfo.lastPacketReceived = 1;
      srvInfo.nextPacketNumber = 2;
      srvInfo.lastPacketAck = 0;
      
      short remotesid = (short) (((buf[8] & 0xF) << 8) + ((buf[9] & 0xF) << 0));
      
      AgentServer.getTransaction().begin();
      synchronized (srvInfo.messagesToAck) {
        int diff = 0;
        if (srvInfo.messagesToAck.size() > 0) {
          MessageAndIndex msgi = (MessageAndIndex) srvInfo.messagesToAck.get(0);
          diff = msgi.index - 2;
        }
        Iterator iterMessages = srvInfo.messagesToAck.iterator();
        MessageAndIndex msgi = null;
        
        while (iterMessages.hasNext()) {
          msgi = (MessageAndIndex) iterMessages.next();
          
          ExpiredNot expiredNot = null;
          if ((msgi.msg.not.expiration > 0L)
              && (msgi.msg.not.expiration < System.currentTimeMillis())) {
            if (msgi.msg.not.deadNotificationAgentId != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification1 "
                    + msgi.msg.from + ", " + msgi.msg.not + " to "
                    + msgi.msg.not.deadNotificationAgentId);
              }
              expiredNot = new ExpiredNot(msgi.msg.not);
              Channel.post(Message.alloc(AgentId.localId, msgi.msg.not.deadNotificationAgentId,
                  expiredNot));
              Channel.validate();
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification "
                    + msgi.msg.from + ", " + msgi.msg.not);
              }
            }
            iterMessages.remove();
            diff += msgi.size;
          }
          
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Changed index " + msgi.index + "->" + (msgi.index - diff) + " "
                + msgi.msg);
          }
          msgi.index = msgi.index - diff;

        }
        if (msgi != null) {
          srvInfo.nextPacketNumber = msgi.index + msgi.size;
        }
      }
      
      testBootTS(remotesid, bootstamp);
      AgentServer.getTransaction().commit(true);
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
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", try to send message:" + msg + "/"
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
              // Remove the message (see below), may be we have to post an error notification to sender.
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", can't send message: " + msg, exc);
              // Remove the message (see below), may be we have to post an error notification to sender.
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
      if (currentTimeMillis < (last + WDActivationPeriod) && !force) {
        return;
      }
      last = currentTimeMillis;
      
      Enumeration enuAddr = serversInfo.keys();
      
      while (enuAddr.hasMoreElements()) {
        SocketAddress addr = (SocketAddress) enuAddr.nextElement();
        ServerInfo servInfo = (ServerInfo) serversInfo.get(addr);
        synchronized (servInfo.messagesToAck) {
          // If connection wasn't established, send handshake
          if (servInfo.handshaken == false) {
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog send handshake.");
              }
              netServerOut.messageOutputStream.handShake(addr);
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", watchdog ack ", exc);
            }
            continue;
          }
          
          // If no more message need to be send, only send an acknowledgment
          if (servInfo.messagesToAck.isEmpty()) {
            try {
              if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog send ack.");
              }
              netServerOut.messageOutputStream.writeAck(servInfo.lastPacketAck, addr);
            } catch (IOException exc) {
              this.logmon.log(BasicLevel.ERROR, this.getName() + ", watchdog ack ", exc);
            }
            continue;
          }
          
          // Re-send the messages
          Iterator iterMessages = servInfo.messagesToAck.iterator();
          while (iterMessages.hasNext()) {
            try {
              MessageAndIndex msgi = (MessageAndIndex) iterMessages.next();
              netServerOut.messageOutputStream.rewriteMessage(addr, msgi, currentTimeMillis);
              if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", re-send message " + msgi.msg);
              }
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
    private byte[] ackBuf = new byte[10];
    private byte[] handshakeBuf = new byte[10];
    
    private int count = 0;
    private DatagramPacket packet;
    private int datagramStamp;
    private int size;

    private SocketAddress serverAddr;
    private ServerInfo serverInfo;

    MessageOutputStream() throws IOException {
      oos = new ObjectOutputStream(this);
      // Stamp 1 for handshaking
      handshakeBuf[4] = 0;
      handshakeBuf[5] = 0;
      handshakeBuf[6] = 0;
      handshakeBuf[7] = 1;
      
      handshakeBuf[8] = (byte) (sid >>> 8);
      handshakeBuf[9] = (byte) (sid >>> 0);
    }

    void writeMessage(SocketAddress addr, Message msg, long currentTimeMillis) throws IOException {
      serverAddr = addr;
      if (serversInfo.get(serverAddr) == null) {
        serverInfo = new ServerInfo();
        serversInfo.put(serverAddr, serverInfo);
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, getName() + ", starting handshake.");
        }
        handShake(serverAddr);
      } else {
        serverInfo = (ServerInfo) serversInfo.get(serverAddr);
      }
      
      datagramStamp = serverInfo.nextPacketNumber;
      size = 0;

      sendMessage(msg, currentTimeMillis);
      serverInfo.messagesToAck.add(new MessageAndIndex(msg, serverInfo.nextPacketNumber, size));
      serverInfo.nextPacketNumber = datagramStamp;
    }

    void writeAck(int ackNumber, SocketAddress addr) throws IOException {
      // Writes the ack for last packet received
      ackBuf[0] = (byte) (ackNumber >>> 24);
      ackBuf[1] = (byte) (ackNumber >>> 16);
      ackBuf[2] = (byte) (ackNumber >>> 8);
      ackBuf[3] = (byte) (ackNumber >>> 0);
      
      // if ack=1, it's handshake response, use packet stamp for server boot timestamp
      if (ackNumber == 1) {
        int boot = getBootTS();
        ackBuf[4] = (byte) (boot >>> 24);
        ackBuf[5] = (byte) (boot >>> 16);
        ackBuf[6] = (byte) (boot >>> 8);
        ackBuf[7] = (byte) (boot >>> 0);
        ackBuf[8] = (byte) (sid >>> 8);
        ackBuf[9] = (byte) (sid >>> 0);
      } else {
        // Stamp 0 for acknowledge
        ackBuf[4] = 0;
        ackBuf[5] = 0;
        ackBuf[6] = 0;
        ackBuf[7] = 0;
        ackBuf[8] = 0;
        ackBuf[9] = 0;
      }
      packet = new DatagramPacket(ackBuf, 10, addr);
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
      buf[0] = (byte) (serverInfo.lastPacketAck >>> 24);
      buf[1] = (byte) (serverInfo.lastPacketAck >>> 16);
      buf[2] = (byte) (serverInfo.lastPacketAck >>> 8);
      buf[3] = (byte) (serverInfo.lastPacketAck >>> 0);

      // Writes the number of the datagram
      buf[4] = (byte) (datagramStamp >>> 24);
      buf[5] = (byte) (datagramStamp >>> 16);
      buf[6] = (byte) (datagramStamp >>> 8);
      buf[7] = (byte) (datagramStamp >>> 0);

      datagramStamp++;
      size++;

      if (serverInfo.handshaken) {
        packet = new DatagramPacket(buf, count, serverAddr);
        socket.send(packet);
      }
    }

    void rewriteMessage(SocketAddress addr, MessageAndIndex msgi, long currentTimeMillis)
        throws IOException {
      serverAddr = addr;
      datagramStamp = msgi.index;
      sendMessage(msgi.msg, currentTimeMillis);
    }

    private void sendMessage(Message msg, long currentTimeMillis) throws IOException {
      int idx = msg.writeToBuf(buf, 8);
      // Writes notification attributes
      buf[idx++] = (byte) ((msg.not.persistent ? Message.PERSISTENT : 0) 
          | (msg.not.detachable ? Message.DETACHABLE : 0));

      buf[Message.LENGTH + 8] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
      buf[Message.LENGTH + 9] = (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
      buf[Message.LENGTH + 10] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
      buf[Message.LENGTH + 11] = (byte) ((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);

      // Be careful, the stream header is hard-written in buf
      count = Message.LENGTH + 12;

      boolean hasExpiration = false;
      try {
        if (msg.not.expiration > 0L) {
          msg.not.expiration -= currentTimeMillis;
          hasExpiration = true;
        }
        oos.writeObject(msg.not);
        oos.reset();
        oos.flush();

        sendPacket();

      } finally {
        if (hasExpiration) {
          msg.not.expiration += currentTimeMillis;
        }
        count = 0;
      }
    }
    
    void handShake(SocketAddress addr) throws IOException {
      // Use acknumber for  for handshaking
      int boot = getBootTS();
      handshakeBuf[0] = (byte) (boot >>> 24);
      handshakeBuf[1] = (byte) (boot >>> 16);
      handshakeBuf[2] = (byte) (boot >>> 8);
      handshakeBuf[3] = (byte) (boot >>> 0);
      
      packet = new DatagramPacket(handshakeBuf, 10, addr);
      socket.send(packet);
    }
  }

  /**
   * Class used to transform UDP packets into a stream, to build the messages.
   */
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
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, getName() + ", interrupted " + message);
              }
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
              logmon.log(BasicLevel.ERROR, getName(), exc);
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
              logmon.log(BasicLevel.DEBUG, getName() + ", msg received " + message);
            }

            deliver(message);

            pipeIn.read();
            servInfo.lastPacketAck = servInfo.lastPacketReceived;
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR, getName(), exc);
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
        logmon.log(BasicLevel.ERROR, getName(), exc);
      }
    }

    protected void shutdown() {
      close();
    }
  }

}
