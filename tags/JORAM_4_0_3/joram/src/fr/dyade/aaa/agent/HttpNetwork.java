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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * <code>HttpNetwork</code> is a simple implementation of <code>Network</code>
 * based on HTTP 1.1 protocol.
 */
public class HttpNetwork extends FIFONetwork {
  InetAddress proxy = null;
  String proxyhost = null;
  int proxyport = 0;

  /**
   * Time between two activation of NetServerOut, it matchs to the time between
   * two requests from the client to the server when there is no message to
   * transmit from client to server.
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
                          "HttpDomain: " + name);

    if (servers[0] == AgentServer.getServerId())
      remote = servers[1];
    else if (servers[1] == AgentServer.getServerId())
      remote = servers[0];
    else
      throw new Exception("Configuration needs to include current server, " +
                          "HttpDomain: " + name);

    super.init(name, port, servers);

    activationPeriod = Long.getLong("ActivationPeriod",
                                    WDActivationPeriod).longValue();
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
      if (isRunning())
	throw new IOException("Consumer already running");

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

  /**
   * Numbers of attempt to bind the server's socket before aborting.
   */
  final static int CnxRetry = 3;

  /**
   *  This method creates and returns a socket connected to a ServerSocket at
   * the specified network address and port. It may be overloaded in subclass,
   * in order to create particular subclasses of sockets.
   * <p>
   *  Due to polymorphism of both factories and sockets, different kinds of
   * sockets can be used by the same application code. The sockets returned
   * to the application can be subclasses of <a href="java.net.Socket">
   * Socket</a>, so that they can directly expose new APIs for features such
   * as compression, security, or firewall tunneling.
   *
   * @param host	the server host.
   * @param port	the server port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createSocket(InetAddress host, int port) throws IOException {
    if (host == null)
      throw new UnknownHostException();
    return new Socket(host, port);
  }

  /**
   *  This method creates and returns a server socket which uses all network
   * interfaces on the host, and is bound to the specified port. It may be
   * overloaded in subclass, in order to create particular subclasses of
   * server sockets.
   *
   * @param port	the port to listen to.
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket() throws IOException {
    for (int i=0; ; i++) {
      try {
        return new ServerSocket(port);
      } catch (BindException exc) {
        if (i > CnxRetry) throw exc;
        try {
          Thread.sleep(i * 200);
        } catch (InterruptedException e) {}
      }
    }
  }

  /**
   *  Configures this socket using the socket options established for this
   * factory. It may be overloaded in subclass, in order to handle particular
   * subclasses of sockets
   *
   * @param Socket	the socket.
   *
   * @exception IOException	for networking errors
   */ 
  static void setSocketOption(Socket sock) throws SocketException {
    // Don't use TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(true);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(0);
    // Set Linger-on-Close timeout.
    sock.setSoLinger(true, 60);
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

    if (i > 0) {
//       logmon.log(BasicLevel.DEBUG,
//                  this.getName() + ": " + i + ", " + new String(buf, 0, i));
      return new String(buf, 0, i);
    }
    return null;
  }
  
  protected void sendRequest(Message msg, OutputStream os) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    byte[] buf = null;

    if (msg != null) {
      strbuf.append("PUT ");
      if (proxy != null) {
        strbuf.append("http://").append(server.getHostname()).append(':').append(server.port);
      }
      strbuf.append("/msg#").append(msg.getStamp()).append(" HTTP/1.1");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(msg);
      oos.flush();
      buf = bos.toByteArray();
    } else {
      strbuf.append("GET ");
      if (proxy != null) {
        strbuf.append("http://").append(server.getHostname()).append(':').append(server.port);
        strbuf.append("/msg HTTP/1.1");
      }
    }

    if (proxy != null) {
      strbuf.append("\r\nHost: ").append(server.getHostname());
    }
    strbuf.append("\r\n" +
                  "User-Agent: ScalAgent 1.0\r\n" +
                  "Accept: image/jpeg;q=0.2\r\n" +
                  "Accept-Language: fr, en-us;q=0.50\r\n" +
                  "Accept-Encoding: gzip;q=0.9\r\n" +
                  "Accept-Charset: ISO-8859-1, utf-8;q=0.66\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Keep-Alive: 300\r\n" +
                  "Connection: keep-alive\r\n");
    if (buf != null) {
      strbuf.append("Content-Length: ").append(buf.length);
      strbuf.append("\r\n" +
                    "Content-Type: image/jpeg\r\n");
    }
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (buf != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, name + ", writes:" + buf.length);
      os.write(buf);
    }
    os.flush();
  }

  protected Message getRequest(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if (line.startsWith("GET ")) {
    } else if (line.startsWith("PUT ")) {
    } else {
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

    if (length != 0) {
      int nb = -1;
      int offset = 0;
      byte[] buf = new byte[length];
      do {
        nb = is.read(buf, offset, length-offset);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", reads:" + nb);
        if (nb > 0) offset += nb;
      } while ((nb != -1) && (offset != length));
      
      if (offset != buf.length) {
	throw new Exception("Bad request length: " + offset);
      } else {
	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	Message msg = (Message) ois.readObject();
	return msg;
      }
    }
    
    return null;
  }

  protected void sendReply(Message msg, OutputStream os) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    byte[] buf = null;

    if (msg != null) {
      strbuf.append("HTTP/1.1 200 OK\r\n");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(msg);
      oos.flush();
      buf = bos.toByteArray();
    } else {
      strbuf.append("HTTP/1.1 204 No Content\r\n");
    }

    strbuf.append("Date: ").append("Fri, 21 Feb 2003 14:30:51 GMT");
    strbuf.append("\r\n" +
                  "Server: ScalAgent 1.0\r\n" +
                  "Last-Modified: ").append("Wed, 19 Apr 2000 08:16:28 GMT");
    strbuf.append("\r\n" +
                  "Cache-Control: no-cache\r\n" +
                  "Cache-Control: no-store\r\n" +
                  "Accept-Ranges: bytes\r\n" +
                  "Keep-Alive: timeout=15, max=100\r\n" +
                  "Connection: Keep-Alive\r\n");
    if (buf != null) {
      strbuf.append("Content-Length: ").append(buf.length);
      strbuf.append("\r\n" +
                    "Content-Type: image/gif\r\n");
    } else {
      strbuf.append("Content-Length: 0\r\n");
    }
    strbuf.append("\r\n");

    os.write(strbuf.toString().getBytes());
    
    if (buf != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, name + ", writes:" + buf.length);
      os.write(buf);
    }
    os.flush();
  }

  protected Message getReply(InputStream is) throws Exception {
    String line = null;

    line = readLine(is);
    if (line.equals("HTTP/1.1 200 OK")) {
    } else if (line.equals("HTTP/1.1 204 No Content")) {
    } else {
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

    if (length != 0) {
      int nb = -1;
      int offset = 0;
      byte[] buf = new byte[length];
      do {
        nb = is.read(buf, offset, length-offset);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", reads:" + nb);
        if (nb > 0) offset += nb;
      } while ((nb != -1) && (offset != length));

      if (offset != buf.length) {
	throw new Exception("Bad reply length: " + offset);
      } else {
	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	Message msg = (Message) ois.readObject();
	return msg;
      }
    }

    return null;
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
      Message msgin = null;
      Message msgout = null;

      try {
	while (running) {
	  try {
	    canStop = true;

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");

	    try {
	      msgout = qout.get(activationPeriod);
	    } catch (InterruptedException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", interrupted");
	    }

            // Open the connection.
            socket = null;
            while (true) {
              boolean phase1 = true;
              if (proxy == null) {
                try {
                  socket = createSocket(server.getAddr(), server.port);
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
                  socket = createSocket(proxy, proxyport);
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

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");

            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendRequest: " + msgout);

              sendRequest(msgout, os);

              msgin = getReply(is);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getReply: " + msgin);

              if (msgout != null) {
                AgentServer.transaction.begin();
                //  Suppress the processed notification from message queue,
                // and deletes it.
                qout.pop();
                msgout.delete();
                AgentServer.transaction.commit();
                AgentServer.transaction.release();
              }

              if (msgin != null) {
                canStop = false;
                deliver(msgin);
                canStop = true;
              }

              // Get next message to send if any
              msgout = qout.get(0);
            } while (running && ((msgout != null) || (msgin != null)));
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", close connection");
          } catch (Exception exc) {
            logmon.log(BasicLevel.WARN,
                       this.getName() + ", ", exc);
          } finally {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connection closed");
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
      Message msgin = null;
      Message msgout= null;

      try {
	while (running) {
          canStop = true;

          // Get the connection
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", waiting connection");
            socket = listen.accept();


            setSocketOption(socket);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         this.getName() + ", connected");
            
            msgin = getRequest(is);
            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getRequest: " + msgin);
              
              if (msgin != null) {
                canStop = false;
                deliver(msgin);
                canStop = true;
              }
                
              msgout = qout.get(0);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendReply: " + msgout);

              sendReply(msgout, os);
              
              msgin = getRequest(is);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", getRequest: " + msgin);

              if (msgout != null) {
                // Be careful, actually msg should be null !!

                canStop = false;

                AgentServer.transaction.begin();
                //  Deletes the processed notification
                qout.pop();
                msgout.delete();
                AgentServer.transaction.commit();
                AgentServer.transaction.release();
              }

            } while (running);
          } catch (Exception exc) {
            logmon.log(BasicLevel.WARN, ", connection closed", exc);
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
}
