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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.management.MXWrapper;

public class UDPNetwork extends Network implements UDPNetworkMBean {

  /** The maximum number of bytes of one datagram */
  final static int DATAGRAM_MAX_SIZE = 8000; // bytes

  /** Input component */
  private NetServerIn netServerIn = null;

  /** Output component */
  private NetServerOut netServerOut = null;

  /** An hashtable linking a socket address to some information about datagrams sent/received/acked */
  private Hashtable serversInfo = new Hashtable();
  
  WatchDog watchDog = null;

  /** A socket used to send and receive datagrams */
  private DatagramSocket socket;

  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning() && (netServerOut != null)
        && netServerOut.isRunning() && watchDog != null && watchDog.isRunning()) {
      return true;
    } else {
      return false;
    }
  }

  public void init(String name, int port, short[] servers) throws Exception {
    super.init(name, port, servers);
    watchDog = new WatchDog(getName(), logmon);
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
    watchDog.start();
    logmon.log(BasicLevel.DEBUG, getName() + ", started");
  }

  public void stop() {
    if (netServerIn != null) {
      netServerIn.stop();
    }
    if (netServerOut != null) {
      netServerOut.stop();
    }
    if (watchDog != null)
      watchDog.stop();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Structure storing details about a particular remote network.
   */
  final class ServerInfo implements ServerInfoMBean {
    
    /** Identifier for the next packet to be send. Number 1 is for handshaking. */
    int nextPacketNumber = 2;
    
    /** Identifier for the last packet received. */
    int lastPacketReceived = 1;
    
    /** Identifier for the last packet acked. */
    int lastPacketAck = 0;
    
    /** Object used to deserialize messages sent to this server. */
    MessageInputStream messageInputStream;
    
    /** A FIFO list to store sent messages waiting to be acked. */
    List messagesToAck = Collections.synchronizedList(new LinkedList());
    
    /** Tells if the server responded to the handshake message. */
    boolean handshaken = false; 
    
    /** The date of the last reception of a message. */
    long lastMsgReceivedDate;

    /** The date of the last sending of a message. */
    long lastMsgSentDate;

    /** Number of unsuccessful connection to this server. */
    int retry;
    
    /** Number of the last message sent */
    int lastMsgSentNumber;

    public int getNextPacketNumber() {
      return nextPacketNumber;
    }

    public int getLastPacketReceived() {
      return lastPacketReceived;
    }

    public int getLastPacketAck() {
      return lastPacketAck;
    }

    public int getNbWaitingAckMessages() {
      return messagesToAck.size();
    }

    public long getLastMsgReceivedDate() {
      return lastMsgReceivedDate;
    }

    public long getLastMsgSentDate() {
      return lastMsgSentDate;
    } 
  }
  
  public interface ServerInfoMBean {

    public int getNextPacketNumber();

    public int getLastPacketReceived();

    public int getLastPacketAck();

    public int getNbWaitingAckMessages();

    public long getLastMsgReceivedDate();

    public long getLastMsgSentDate();

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
      socket.setReceiveBufferSize(Integer.getInteger("UDPReceiveBufferSize", 1000000).intValue());
      socket.setSendBufferSize(Integer.getInteger("UDPSendBufferSize", 10000).intValue());
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, this.getName() + ", socket buffer sizes: Receive:"
            + socket.getReceiveBufferSize() + " Send:" + socket.getSendBufferSize());
      }
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

            SocketAddress socketAddress = packet.getSocketAddress();
            ServerInfo srvInfo = ((ServerInfo) serversInfo.get(socketAddress));
            if (srvInfo == null) {
              srvInfo = new ServerInfo();
              try {
                MXWrapper.registerMBean(srvInfo, "AgentServer", getMBeanName(socketAddress.toString()
                    .replace(':', '#')));
              } catch (Exception exc) {
                logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
              }
              serversInfo.put(socketAddress, srvInfo);
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
              watchDog.wakeup(true);
              continue;
            }

            // Handshake received
            if (packetNumber == 1) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, "Handshake received, send handshake response ");
              }
              cleanServerInfo(srvInfo, ackUpTo);
              // Send handshake response
              netServerOut.messageOutputStream.writeAck(1, socketAddress);
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

            // If nack, wake up watchdog thread
            if (isNack) {
              watchDog.wakeup(true);
            }

            // Ack was not alone in the packet
            if (packet.getLength() > Message.LENGTH) {

              srvInfo.lastMsgReceivedDate = System.currentTimeMillis();

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
                  netServerOut.messageOutputStream.writeAck(srvInfo.lastPacketAck, socketAddress);
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
      
      short remotesid = (short) (((buf[8] & 0xF) << 8) + ((buf[9] & 0xF) << 0));
      
      AgentServer.getTransaction().begin();
      synchronized (srvInfo.messagesToAck) {
        
        srvInfo.handshaken = true;
        srvInfo.lastPacketReceived = 1;
        srvInfo.nextPacketNumber = 2;
        srvInfo.lastPacketAck = 0;
        srvInfo.retry = 1;
        
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
    MessageOutputStream reSendMessageOutputStream = null;

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
          reSendMessageOutputStream = new MessageOutputStream();
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
            msg = qout.get();
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
            }
            continue;
          }

          canStop = false;

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

  }

  /**
   * Class used to send messages with UDP packets.
   */
  final class MessageOutputStream extends OutputStream {

    private ObjectOutputStream oos = null;
    
    private byte[] buf = new byte[DATAGRAM_MAX_SIZE];
    private int count = 0;
    private int datagramStamp;
    private int size;
    
    private SocketAddress serverAddr;
    private ServerInfo serverInfo;
    
    private byte[] ackBuf = new byte[10];
    private byte[] handshakeBuf = new byte[10];

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
      ServerInfo serverInfo;
      if (serversInfo.get(addr) == null) {
        serverInfo = new ServerInfo();
        try {
          MXWrapper.registerMBean(serverInfo, "AgentServer", getMBeanName(addr.toString().replace(':', '#')));
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
        }
        serversInfo.put(addr, serverInfo);
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, getName() + ", starting handshake.");
        }
        handShake(addr);
      } else {
        serverInfo = (ServerInfo) serversInfo.get(addr);
      }
      
      synchronized (serverInfo.messagesToAck) {
        size = 0;
        rewriteMessage(serverInfo, addr, serverInfo.nextPacketNumber, msg, currentTimeMillis);
        serverInfo.messagesToAck.add(new MessageAndIndex(msg, serverInfo.nextPacketNumber, size));
        serverInfo.nextPacketNumber = datagramStamp;
      }
      this.serverInfo = null;
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
      socket.send(new DatagramPacket(ackBuf, 10, addr));
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

      serverInfo.lastMsgSentDate = System.currentTimeMillis();
      if (serverInfo.handshaken) {
        socket.send(new DatagramPacket(buf, count, serverAddr));
      }
    }

    /**
     *  Write the Message internal state to the buffer.
     */
    int writeToBuf(Message msg, int idx) {
      // Writes sender's AgentId
      buf[idx++] = (byte) (msg.from.from >>>  8);
      buf[idx++] = (byte) (msg.from.from >>>  0);
      buf[idx++] = (byte) (msg.from.to >>>  8);
      buf[idx++] = (byte) (msg.from.to >>>  0);
      buf[idx++] = (byte) (msg.from.stamp >>>  24);
      buf[idx++] = (byte) (msg.from.stamp >>>  16);
      buf[idx++] = (byte) (msg.from.stamp >>>  8);
      buf[idx++] = (byte) (msg.from.stamp >>>  0);
      // Writes adressee's AgentId
      buf[idx++]  = (byte) (msg.to.from >>>  8);
      buf[idx++]  = (byte) (msg.to.from >>>  0);
      buf[idx++] = (byte) (msg.to.to >>>  8);
      buf[idx++] = (byte) (msg.to.to >>>  0);
      buf[idx++] = (byte) (msg.to.stamp >>>  24);
      buf[idx++] = (byte) (msg.to.stamp >>>  16);
      buf[idx++] = (byte) (msg.to.stamp >>>  8);
      buf[idx++] = (byte) (msg.to.stamp >>>  0);
      // Writes source server id of message
      buf[idx++]  = (byte) (msg.source >>>  8);
      buf[idx++]  = (byte) (msg.source >>>  0);
      // Writes destination server id of message
      buf[idx++] = (byte) (msg.dest >>>  8);
      buf[idx++] = (byte) (msg.dest >>>  0);
      // Writes stamp of message
      buf[idx++] = (byte) (msg.stamp >>>  24);
      buf[idx++] = (byte) (msg.stamp >>>  16);
      buf[idx++] = (byte) (msg.stamp >>>  8);
      buf[idx++] = (byte) (msg.stamp >>>  0);

      return idx;
    }

    void rewriteMessage(ServerInfo serverInfo,
                        SocketAddress addr,
                        int startIndex,
                        Message msg,
                        long currentTimeMillis) throws IOException {
      serverAddr = addr;
      datagramStamp = startIndex;
      this.serverInfo = serverInfo;
      
      int idx = writeToBuf(msg, 8);
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
      int boot = getBootTS();
      handshakeBuf[0] = (byte) (boot >>> 24);
      handshakeBuf[1] = (byte) (boot >>> 16);
      handshakeBuf[2] = (byte) (boot >>> 8);
      handshakeBuf[3] = (byte) (boot >>> 0);
      socket.send(new DatagramPacket(handshakeBuf, 10, addr));
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
 
    void readMessageHdrFromStream(Message msg) throws IOException {
      // Reads sender's AgentId
      msg.from = new AgentId(
        (short) (((pipeIn.read() & 0xFF) << 8) + (pipeIn.read() & 0xFF)),
        (short) (((pipeIn.read() & 0xFF) << 8) + (pipeIn.read() & 0xFF)),
        ((pipeIn.read() & 0xFF) << 24) + ((pipeIn.read() & 0xFF) << 16) +
        ((pipeIn.read() & 0xFF) << 8) + ((pipeIn.read() & 0xFF) << 0));
      // Reads adressee's AgentId
      msg.to = new AgentId(
        (short) (((pipeIn.read() & 0xFF) << 8) + (pipeIn.read() & 0xFF)),
        (short) (((pipeIn.read() & 0xFF) << 8) + (pipeIn.read() & 0xFF)),
        ((pipeIn.read() & 0xFF) << 24) + ((pipeIn.read() & 0xFF) << 16) +
        ((pipeIn.read() & 0xFF) << 8) + ((pipeIn.read() & 0xFF) << 0));
      // Reads source server id of message
      msg.source = (short) (((pipeIn.read() & 0xFF) << 8) +
                            ((pipeIn.read() & 0xFF) << 0));
      // Reads destination server id of message
      msg.dest = (short) (((pipeIn.read() & 0xFF) << 8) +
                          ((pipeIn.read() & 0xFF) << 0));
      // Reads stamp of message
      msg.stamp = ((pipeIn.read() & 0xFF) << 24) +
        ((pipeIn.read() & 0xFF) << 16) +
        ((pipeIn.read() & 0xFF) << 8) +
        ((pipeIn.read() & 0xFF) << 0);
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
              readMessageHdrFromStream(message);
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
  
  final class WatchDog extends Daemon {
    /** Use to synchronize thread */
    private Object lock;
    
    private boolean force = false;

    WatchDog(String name, Logger logmon) {
      super(name + ".watchdog");
      lock = new Object();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void close() {
    }

    protected void shutdown() {
      close();
    }

    void wakeup(boolean forced) {
      force = forced;
      synchronized (lock) {
        lock.notify();
      }
    }

    public void run() {
      try {
        synchronized (lock) {
          while (running) {
            try {
              lock.wait(WDActivationPeriod);
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, this.getName() + ", activated, force=" + force);
            } catch (InterruptedException exc) {
              continue;
            }
            boolean hasBeenForced = force;
            force = false;

            if (!running) {
              break;
            }

            Enumeration enuAddr = serversInfo.keys();
            long currentTimeMillis = System.currentTimeMillis();
            
            while (enuAddr.hasMoreElements()) {
              SocketAddress addr = (SocketAddress) enuAddr.nextElement();
              ServerInfo servInfo = (ServerInfo) serversInfo.get(addr);
              
              synchronized (servInfo.messagesToAck) {
                
                if (!hasBeenForced
                    && !((servInfo.retry < WDNbRetryLevel1)
                      || ((servInfo.retry < WDNbRetryLevel2) && ((servInfo.lastMsgSentDate + WDRetryPeriod2) < currentTimeMillis))
                      || ((servInfo.lastMsgSentDate + WDRetryPeriod3) < currentTimeMillis))) {
                  continue;
                }
                
                if (!servInfo.messagesToAck.isEmpty()) {
                  if (servInfo.lastMsgSentNumber == ((MessageAndIndex) servInfo.messagesToAck.get(0)).msg.stamp) {
                    servInfo.retry++;
                    if (servInfo.retry > 4) {
                      if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                        this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connection lost with the server.");
                      }
                      servInfo.handshaken = false;
                    }
                  } else {
                    servInfo.lastMsgSentNumber = ((MessageAndIndex) servInfo.messagesToAck.get(0)).msg.stamp;
                  }
                }
                
                // If connection wasn't established, send handshake
                if (servInfo.handshaken == false) {
                  try {
                    if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                      this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog send handshake.");
                    }
                    netServerOut.messageOutputStream.handShake(addr);
                    servInfo.lastMsgSentDate = currentTimeMillis;
                  } catch (IOException exc) {
                    this.logmon.log(BasicLevel.ERROR, this.getName() + ", watchdog ack ", exc);
                  }
                  continue;
                }
                
                if (!hasBeenForced && currentTimeMillis - servInfo.lastMsgSentDate < WDActivationPeriod / 2) {
                  if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                    this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog don't send ack: last message sent recently");
                  }
                  continue;
                }

                // If no more message need to be send, only send an acknowledgment
                if (servInfo.messagesToAck.isEmpty()) {
                  if (currentTimeMillis - servInfo.lastMsgReceivedDate > WDActivationPeriod * 2) {
                    continue;
                  }
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
                
                if (!hasBeenForced && currentTimeMillis - servInfo.lastMsgSentDate < WDActivationPeriod - 100) {
                  if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
                    this.logmon.log(BasicLevel.DEBUG, this.getName() + ", watchdog don't re-send: last message sent recently");
                  }
                  continue;
                }

                // Re-send the messages
                Iterator iterMessages = servInfo.messagesToAck.iterator();
                while (iterMessages.hasNext()) {
                  try {
                    MessageAndIndex msgi = (MessageAndIndex) iterMessages.next();
                    netServerOut.reSendMessageOutputStream.rewriteMessage(servInfo, addr, msgi.index,
                        msgi.msg, currentTimeMillis);
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
      } catch (RuntimeException rexc) {
        this.logmon.log(BasicLevel.DEBUG, this.getName() + ", re send error ", rexc);
      }
    }
  }

  private String getMBeanName(String socketAddress) {
    return new StringBuffer().append("server=").append(AgentServer.getName()).append(",cons=").append(name)
        .append(",serverDest=#").append(socketAddress).toString();
  }

  public int getSocketReceiveBufferSize() {
    try {
      return socket.getReceiveBufferSize();
    } catch (SocketException exc) {
      exc.printStackTrace();
    }
    return -1;
  }

  public int getSocketSendBufferSize() {
    try {
      return socket.getSendBufferSize();
    } catch (SocketException exc) {
      exc.printStackTrace();
    }
    return -1;
  }

  public void setSocketReceiveBufferSize(int size) {
    try {
      socket.setReceiveBufferSize(size);
    } catch (SocketException exc) {
      exc.printStackTrace();
    }
  }

  public void setSocketSendBufferSize(int size) {
    try {
      socket.setSendBufferSize(size);
    } catch (SocketException exc) {
      exc.printStackTrace();
    }
  }
}
