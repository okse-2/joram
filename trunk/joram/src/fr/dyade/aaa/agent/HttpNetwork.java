/*
 * Copyright (C) 2003 - 2005 ScalAgent Distributed Technologies
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
 * <tt>HttpNetwork</tt> is a simple implementation of <tt>StreamNetwork</tt>
 * based on HTTP 1.1 protocol.
 */
public class HttpNetwork extends StreamNetwork {
  private InetAddress proxy = null;
  /**
   *  Hostname (or IP dotted address) of proxy host, if not defined there
   * is a direct connection between the client and the server.
   *  This value can be adjusted for all HttpNetwork components by setting
   * <code>proxyhost</code> global property or for a particular
   * network by setting <code>\<DomainName\>.proxyhost</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  String proxyhost = null;
  /**
   *  Port number of proxy if any.
   *  This value can be adjusted for all HttpNetwork components by setting
   * <code>proxyport</code> global property or for a particular
   * network by setting <code>\<DomainName\>.proxyport</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int proxyport = 0;

  /**
   *  Period of time between two activation of NetServerOut, it matchs to the
   * time between two requests from the client to the server when there is no
   * message to transmit from client to server.
   *  This value can be adjusted for all HttpNetwork components by setting
   * <code>ActivationPeriod</code> global property or for a particular
   * network by setting <code>\<DomainName\>.ActivationPeriod</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  protected long activationPeriod = 10000L;

  /**
   * Creates a new network component.
   */
  public HttpNetwork() {
    super();
  }

  short remote = -1;
  ServerDesc server = null;

  MessageInputStream nis = null;
  MessageOutputStream nos = null;

  /**
   * Initializes a new network component. This method is used in order to
   * easily creates and configure a Network component from a class name.
   * So we can use the <code>Class.newInstance()</code> method for create
   * (whitout any parameter) the component, then we can initialize it with
   * this method.<br>
   * This method initializes the logical clock for the domain.
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   */
  public void init(String name, int port, short[] servers) throws Exception {
    if (servers.length != 2)
      throw new Exception("Configuration needs exactly 2 servers, " +
                          "HttpDomain: " + name + ", servers=" + 
                          Strings.toString(servers));

    if (servers[0] == AgentServer.getServerId())
      remote = servers[1];
    else if (servers[1] == AgentServer.getServerId())
      remote = servers[0];
    else
      throw new Exception("Configuration needs to include current server, " +
                          "HttpDomain: " + name);

    super.init(name, port, servers);

    nis = new MessageInputStream();
    nos = new MessageOutputStream();

    activationPeriod = Long.getLong("ActivationPeriod",
                                    activationPeriod).longValue();
    activationPeriod = Long.getLong(name + ".ActivationPeriod",
                                    activationPeriod).longValue();
    
    proxyhost = System.getProperty("proxyhost");
    proxyhost = System.getProperty(name + ".proxyhost", proxyhost);
    if (proxyhost != null) {
      proxyport = Integer.getInteger("proxyport", 8080).intValue();
      proxyport = Integer.getInteger(name + ".proxyport", proxyport).intValue();
      proxy = InetAddress.getByName(proxyhost);
    }
  }

  /** Daemon component */
  Daemon dmon = null;

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {
      if (isRunning()) return;

      server = AgentServer.getServerDesc(remote);

      if (port != 0) {
        dmon = new NetServerIn(getName(), logmon);
      } else {
        dmon = new NetServerOut(getName(), logmon);
      }
      dmon.start();
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
//     if (dmon != null) dmon.wakeup();
  }

  /**
   * Forces the network component to stop executing.
   */
  public void stop() {
    if (dmon != null) dmon.stop();
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((dmon != null) && dmon.isRunning())
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
    if (dmon != null)
      strbuf.append(dmon.toString()).append("\n\t");

    return strbuf.toString();
  }

  byte[] buf = new byte[120];

  protected String readLine(InputStream is) throws IOException {
    int i = 0;
    while ((buf[i++] = (byte) is.read()) != -1) {
      if ((buf[i-1] == '\n') && (buf[i-2] == '\r')) {
	i -= 2;
	break;
      }
    }

    if (i > 0) return new String(buf, 0, i);

    return null;
  }
  
  protected void sendRequest(Message msg, OutputStream os, int ack) throws Exception {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("PUT ");
    if (proxy != null) {
      strbuf.append("http://").append(server.getHostname()).append(':').append(server.getPort());
    }
    if (msg != null) {
      strbuf.append("/msg#").append(msg.getStamp()).append(" HTTP/1.1");
    } else {
        strbuf.append("/msg HTTP/1.1");
    }
    nos.writeMessage(msg, ack);

    if (proxy != null)
      strbuf.append("\r\nHost: ").append(server.getHostname());
    strbuf.append("\r\n" +
                  "User-Agent: ScalAgent 1.0\r\n" +
                  "Accept: image/jpeg;q=0.2\r\n" +
                  "Accept-Language: fr, en-us;q=0.50\r\n" +
                  "Accept-Encoding: gzip;q=0.9\r\n" +
                  "Accept-Charset: ISO-8859-1, utf-8;q=0.66\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Keep-Alive: 300\r\n" +
                  "Connection: keep-alive\r\n" +
                  "Proxy-Connection: keep-alive\r\n" +
                  "Pragma: no-cache\r\n");
    strbuf.append("Content-Length: ").append(nos.size());
    strbuf.append("\r\n" +
                  "Content-Type: image/jpeg\r\n");
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, name + ", writes:" + nos.size());
    nos.writeTo(os);
    nos.reset();

    os.flush();
  }

  protected void getRequest(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if ((line == null) ||
        (! (line.startsWith("GET ") || line.startsWith("PUT ")))) {
      throw new Exception("Bad request: " + line);
    }

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
	length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (nis.readFrom(is) != length)
      logmon.log(BasicLevel.WARN, name + "Bad request length: " + length);
  }

  protected void sendReply(Message msg, OutputStream os, int ack) throws Exception {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("HTTP/1.1 200 OK\r\n");

    nos.writeMessage(msg, ack);

    strbuf.append("Date: ").append("Fri, 21 Feb 2003 14:30:51 GMT");
    strbuf.append("\r\n" +
                  "Server: ScalAgent 1.0\r\n" +
                  "Last-Modified: ").append("Wed, 19 Apr 2000 08:16:28 GMT");
    strbuf.append("\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Accept-Ranges: bytes\r\n" +
                  "Keep-Alive: timeout=15, max=100\r\n" +
                  "Connection: Keep-Alive\r\n" +
                  "Proxy-Connection: Keep-Alive\r\n" +
                  "Pragma: no-cache\r\n");
    strbuf.append("Content-Length: ").append(nos.size());
    strbuf.append("\r\n" +
                  "Content-Type: image/gif\r\n");
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, name + ", writes:" + nos.size());
    nos.writeTo(os);
    nos.reset();

    os.flush();
  }

  protected void getReply(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if ((line == null) ||
        ((! line.equals("HTTP/1.1 200 OK")) &&
         (! line.equals("HTTP/1.1 204 No Content")))) {
      throw new Exception("Bad reply: " + line);
    }

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
	length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (nis.readFrom(is) != length)
      logmon.log(BasicLevel.WARN, name + "Bad reply length: " + length);
  }

  protected int handle(Message msgout) throws Exception {
    int ack = nis.getAckStamp();
    if ((msgout != null) && (msgout.stamp == ack)) {
      AgentServer.getTransaction().begin();
      //  Suppress the processed notification from message queue,
      // and deletes it.
      qout.pop();
      msgout.delete();
      msgout.free();
      AgentServer.getTransaction().commit();
      AgentServer.getTransaction().release();
    }

    Message msg = nis.getMessage();
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 this.getName() + ", get: " + msg);

    if (msg != null) {
      testBootTS(msg.getSource(), nis.getBootTS());
      deliver(msg);
      return msg.stamp;
    }

    return -1;
  }

  /**
   * This method creates a tunnelling socket if a proxy is used.
   *
   * @param host	the server host.
   * @param port	the server port.
   * @param proxy	the proxy host.
   * @param proxyport	the proxy port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createTunnelSocket(InetAddress host, int port,
                            InetAddress proxy, int proxyport) throws IOException {
    return createSocket(proxy, proxyport);
  }

  final class NetServerOut extends Daemon {
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;

    NetServerOut(String name, Logger logmon) {
      super(name + ".NetServerOut");
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void open() throws IOException {
      // Open the connection.
      socket = null;
      boolean phase1 = true;
      while (true) {
        if (proxy == null) {
          try {
            socket = createSocket(server);
            break;
          } catch (IOException exc) {
            logmon.log(BasicLevel.WARN,
                       this.getName() + ", connection refused", exc);
            if (! phase1) throw exc;
            phase1 = false;
            server.resetAddr();
          }
        } else {
          try {
            socket = createTunnelSocket(server.getAddr(), server.getPort(),
                                        proxy, proxyport);
            break;
          } catch (IOException exc) {
            logmon.log(BasicLevel.WARN,
                       this.getName() + ", connection refused", exc);
            if (! phase1) throw exc;
            phase1 = false;
            proxy = InetAddress.getByName(proxyhost);
          }
        }
      }
      setSocketOption(socket);

      os = socket.getOutputStream();
      is = socket.getInputStream();
    }

    protected void close() {
      if (socket != null) {
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

    protected void shutdown() {
      thread.interrupt();
      close();
    }

    public void run() {
      Message msgout = null;
      int ack = -1;

      try {
	while (running) {
          canStop = true;
	  try {
	    try {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", waiting message");
	      msgout = qout.get(activationPeriod);
	    } catch (InterruptedException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", interrupted");
	    }
            open();

            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendRequest: " + msgout);

              sendRequest(msgout, os, ack);
              getReply(is);

              canStop = false;
              ack = handle(msgout);
              canStop = true;
              // Get next message to send if any
              msgout = qout.get(0);
            } while (running && ((msgout != null) || (ack != -1)));
          } catch (Exception exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connection closed", exc);
          } finally {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connection ends");
            try {
              os.close();
            } catch (Exception exc) {}
            os = null;
            try {
              is.close();
            } catch (Exception exc) {}
            is = null;
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
        logmon.log(BasicLevel.WARN, ", exited");
	finish();
      }
    }
  }

  final class NetServerIn extends Daemon {
    ServerSocket listen = null;
    
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;

    NetServerIn(String name, Logger logmon) throws IOException {
      super(name + ".NetServerIn");
      listen = createServerSocket();
      // Overload logmon definition in Daemon
      this.logmon = logmon;
    }

    protected void open(Socket socket) throws IOException {
      setSocketOption(socket);

      os = socket.getOutputStream();
      is = socket.getInputStream();

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");
    }

    protected void close() {
      if (socket != null) {
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
      try {
	listen.close();
      } catch (Exception exc) {}
    }

    protected void shutdown() {
      close();
    }

    public void run() {
      Message msgout= null;
      int ack = -1;

      try {
	while (running) {
          canStop = true;

          // Get the connection
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", waiting connection");
            socket = listen.accept();
            open(socket);
            
            getRequest(is);
            do {
              canStop = false;
              ack = handle(msgout);
              canStop = true;

              msgout = qout.get(0);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendReply: " + msgout);

              sendReply(msgout, os, ack);
              getRequest(is);
            } while (running);
          } catch (Exception exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, ", connection closed", exc);
          } finally {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, ", connection ends");
            try {
              os.close();
            } catch (Exception exc) {}
            os = null;
            try {
              is.close();
            } catch (Exception exc) {}
            is = null;
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
        logmon.log(BasicLevel.WARN, ", exited");
        finish();
      }
    }
  }

  /**
   * Class used to read messages through a stream.
   */
  final class MessageInputStream extends ByteArrayInputStream {
    MessageInputStream() {
      super(new byte[256]);
    }

    private void readFully(InputStream is, int length) throws IOException {
      count = 0;
      if (length > buf.length) buf = new byte[length];
      
      int nb = -1;
      do {
        nb = is.read(buf, count, length-count);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", reads:" + nb);
        if (nb < 0) throw new EOFException();
        count += nb;
      } while (count != length);
      pos  = 0;
    }

    int msgLen;
    int msgBoot;
    int msgAck;

    Message msg = null;

    int readFrom(InputStream is) throws Exception {
      readFully(is, 12);
      // Reads message length
      msgLen = ((buf[0] & 0xFF) << 24) + ((buf[1] & 0xFF) << 16) +
        ((buf[2] & 0xFF) <<  8) + ((buf[3] & 0xFF) <<  0);
      // Reads boot timestamp of source server
      msgBoot = ((buf[4] & 0xFF) << 24) + ((buf[5] & 0xFF) << 16) +
        ((buf[6] & 0xFF) <<  8) + ((buf[7] & 0xFF) <<  0);
      msgAck = ((buf[8] & 0xFF) << 24) + ((buf[9] & 0xFF) << 16) +
        ((buf[10] & 0xFF) <<  8) + ((buf[11] & 0xFF) <<  0);

      if (msgLen > 36) {
        msg = Message.alloc();
        readFully(is, 25);

        // Reads sender's AgentId
        msg.from = new AgentId(
          (short) (((buf[0] & 0xFF) <<  8) + (buf[1] & 0xFF)),
          (short) (((buf[2] & 0xFF) <<  8) + (buf[3] & 0xFF)),
          ((buf[4] & 0xFF) << 24) + ((buf[5] & 0xFF) << 16) +
          ((buf[6] & 0xFF) <<  8) + ((buf[7] & 0xFF) <<  0));
        // Reads adressee's AgentId
        msg.to = new AgentId(
          (short) (((buf[8] & 0xFF) <<  8) + (buf[9] & 0xFF)),
          (short) (((buf[10] & 0xFF) <<  8) + (buf[11] & 0xFF)),
          ((buf[12] & 0xFF) << 24) + ((buf[13] & 0xFF) << 16) +
          ((buf[14] & 0xFF) <<  8) + ((buf[15] & 0xFF) <<  0));
        // Reads source server id of message
        msg.source = (short) (((buf[16] & 0xFF) <<  8) +
                              ((buf[17] & 0xFF) <<  0));
        // Reads destination server id of message
        msg.dest = (short) (((buf[18] & 0xFF) <<  8) +
                            ((buf[19] & 0xFF) <<  0));
        // Reads stamp of message
        msg.stamp = ((buf[20] & 0xFF) << 24) +
          ((buf[21] & 0xFF) << 16) +
          ((buf[22] & 0xFF) <<  8) +
          ((buf[23] & 0xFF) <<  0);
        // Reads notification attributes
        boolean persistent = ((buf[24] & 0x01) == 0x01) ? true : false;
        boolean detachable = ((buf[24] & 0x10) == 0x10) ? true : false;

        readFully(is, msgLen-37);
        // Reads notification object
        ObjectInputStream ois = new ObjectInputStream(this);
        msg.not = (Notification) ois.readObject();
        msg.not.persistent = persistent;
        msg.not.detachable = detachable;
        msg.not.detached = false;

        return msgLen;
      }
      msg = null;
      return 12;
    }

    int getLength() {
      return msgLen;
    }

    int getBootTS() {
      return msgBoot;
    }

    int getAckStamp() {
      return msgAck;
    }

    Message getMessage() {
      return msg;
    }
  }

  /**
   * Class used to send messages through a stream.
   */
  final class MessageOutputStream extends ByteArrayOutputStream {
    private ObjectOutputStream oos = null;

    MessageOutputStream() throws IOException {
      super(256);
      oos = new ObjectOutputStream(this);
      count = 0;
      buf[37] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
      buf[38] = (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
      buf[39] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
      buf[40] = (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);
    }

    void writeMessage(Message msg, int ack) throws IOException {
      // Writes boot timestamp of source server
      buf[4] = (byte) (getBootTS() >>>  24);
      buf[5] = (byte) (getBootTS() >>>  16);
      buf[6] = (byte) (getBootTS() >>>  8);
      buf[7] = (byte) (getBootTS() >>>  0);

      // Writes stamp of last received message
      buf[8] = (byte) (ack >>>  24);
      buf[9] = (byte) (ack >>>  16);
      buf[10] = (byte) (ack >>>  8);
      buf[11] = (byte) (ack >>>  0);

      count = 12;
      if (msg != null) {
        // Writes sender's AgentId
        buf[12] = (byte) (msg.from.from >>>  8);
        buf[13] = (byte) (msg.from.from >>>  0);
        buf[14] = (byte) (msg.from.to >>>  8);
        buf[15] = (byte) (msg.from.to >>>  0);
        buf[16] = (byte) (msg.from.stamp >>>  24);
        buf[17] = (byte) (msg.from.stamp >>>  16);
        buf[18] = (byte) (msg.from.stamp >>>  8);
        buf[19] = (byte) (msg.from.stamp >>>  0);
        // Writes adressee's AgentId
        buf[20]  = (byte) (msg.to.from >>>  8);
        buf[21]  = (byte) (msg.to.from >>>  0);
        buf[22] = (byte) (msg.to.to >>>  8);
        buf[23] = (byte) (msg.to.to >>>  0);
        buf[24] = (byte) (msg.to.stamp >>>  24);
        buf[25] = (byte) (msg.to.stamp >>>  16);
        buf[26] = (byte) (msg.to.stamp >>>  8);
        buf[27] = (byte) (msg.to.stamp >>>  0);
        // Writes source server id of message
        buf[28]  = (byte) (msg.source >>>  8);
        buf[29]  = (byte) (msg.source >>>  0);
        // Writes destination server id of message
        buf[30] = (byte) (msg.dest >>>  8);
        buf[31] = (byte) (msg.dest >>>  0);
        // Writes stamp of message
        buf[32] = (byte) (msg.stamp >>>  24);
        buf[33] = (byte) (msg.stamp >>>  16);
        buf[34] = (byte) (msg.stamp >>>  8);
        buf[35] = (byte) (msg.stamp >>>  0);
        // Writes notification attributes
        buf[36] = (byte) ((msg.not.persistent?0x01:0) |
                          (msg.not.detachable?0x10:0));
        // Be careful, the stream header is hard-written in buf[29..32]
        count = 41;

        oos.writeObject(msg.not);
        oos.reset();
        oos.flush();
      }
      // Writes boot timestamp of source server
      buf[0] = (byte) (count >>>  24);
      buf[1] = (byte) (count >>>  16);
      buf[2] = (byte) (count >>>  8);
      buf[3] = (byte) (count >>>  0);
    }
  }
}
