/*
 * Copyright (C) 2003 - 2012 ScalAgent Distributed Technologies
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Daemon;

/**
 * <tt>HttpNetwork</tt> is a simple implementation of <tt>StreamNetwork</tt>
 * based on HTTP 1.1 protocol.
 */
public class HttpNetwork extends StreamNetwork implements HttpNetworkMBean {
  /**
   * Network address of proxy server.
   * 
   * @see #proxyhost
   * @see #proxyport
   */
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
   * Gets the proxyhost value.
   *
   * @return the proxyhost value
   */
  public String getProxyhost() {
    return proxyhost;
  }

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
   * Gets the proxyport value.
   *
   * @return the proxyport value
   */
  public long getProxyport() {
    return proxyport;
  }

  /**
   * Period of time between two activation of NetServerOut, it matches to the
   * time between two requests from the client to the server when there is no
   * message to transmit from client to server. This value can be adjusted for
   * all HttpNetwork components by setting <code>ActivationPeriod</code>
   * global property or for a particular network by setting
   * <code>\<DomainName\>.ActivationPeriod</code> specific property.
   * <p>
   * Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file. By default,
   * its value is 10000 (10s).
   */
  protected long activationPeriod = 10000L;

  /**
   * Gets the activationPeriod value.
   *
   * @return the activationPeriod value
   */
  public long getActivationPeriod() {
    return activationPeriod;
  }

  /**
   * Sets the activationPeriod value.
   *
   * @param activationPeriod	the activationPeriod value
   */
  public void setActivationPeriod(long activationPeriod) {
    this.activationPeriod = activationPeriod;
  }

  /**
   *  Number of listening daemon, this value is only valid for the server
   * part of the HttpNetwork.
   *  This value can be adjusted for all HttpNetwork components by setting
   * <code>NbDaemon</code> global property or for a particular network by
   * setting <code>\<DomainName\>.NbDaemon</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int NbDaemon = 1;

  /**
   * Gets the NbDaemon value.
   *
   * @return the NbDaemon value
   */
  public long getNbDaemon() {
    return NbDaemon;
  }

  /**
   * Creates a new network component.
   */
  public HttpNetwork() {
    super();
  }

  /**
   * Descriptor of the listen server, it is used only on the client side
   * (NetServerOut component).
   */
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
    super.init(name, port, servers);

    activationPeriod = AgentServer.getLong("ActivationPeriod", activationPeriod).longValue();
    activationPeriod = AgentServer.getLong(name + ".ActivationPeriod", activationPeriod).longValue();
    
    NbDaemon = AgentServer.getInteger("NbDaemon", NbDaemon).intValue();
    NbDaemon = AgentServer.getInteger(name + ".NbDaemon", NbDaemon).intValue();

    proxyhost = AgentServer.getProperty("proxyhost");
    proxyhost = AgentServer.getProperty(name + ".proxyhost", proxyhost);
    if (proxyhost != null) {
      proxyport = AgentServer.getInteger("proxyport", 8080).intValue();
      proxyport = AgentServer.getInteger(name + ".proxyport", proxyport).intValue();
      proxy = InetAddress.getByName(proxyhost);
    }
  }

  /** Daemon component */
  Daemon dmon[] = null;

  /**
   * Causes this network component to begin execution.
   */
  public void start() throws Exception {
    logmon.log(BasicLevel.DEBUG, getName() + ", starting");
    try {
      if (isRunning()) return;

      if (port != 0) {
        dmon = new Daemon[NbDaemon];
        ServerSocket listen = createServerSocket();

        for (int i=0; i<NbDaemon; i++) {
          dmon[i] = new NetServerIn(getName() + '.' + i, listen, logmon);
        }
      } else {
        // AF: May be, we have to verify that there is only one 'listen' network.
        for (int i=0; i<servers.length; i++) {
          server = AgentServer.getServerDesc(servers[i]);
          if ((server.getServerId() != AgentServer.getServerId()) && (server.getPort() > 0)) {
            logmon.log(BasicLevel.DEBUG, getName() + ", server=" + server);
            break;
          }
          server = null;
        }
        dmon = new Daemon[1];
        dmon[0] = new NetServerOut(getName(), logmon);
      }

      for (int i=0; i<dmon.length; i++) {
        dmon[i].start();
      }
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
    if (dmon != null) {
      for (int i=0; i<dmon.length; i++) {
        dmon[i].stop();
      }
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", stopped");
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if (dmon != null) {
      for (int i=0; i<dmon.length; i++) {
        if (dmon[i].isRunning()) return true;
      }
    }
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
    if (dmon != null) {
      for (int i=0; i<dmon.length; i++) {
        strbuf.append(dmon[i].toString()).append("\n\t");
      }
    }
    return strbuf.toString();
  }
  
  String readLine(InputStream is, byte[] buf) throws IOException {
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

  protected void sendRequest(Message msg,
                             OutputStream os,
                             NetworkOutputStream nos,
                             int ack,
                             long currentTimeMillis) throws Exception {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("PUT ");
    if (proxy != null) {
      strbuf.append("http://").append(server.getHostname()).append(':').append(server.getPort());
    }
    strbuf.append("/msg?from=").append(AgentServer.getServerId());
    strbuf.append("&stamp=");
    if (msg != null) {
      strbuf.append(msg.getStamp());
    } else {
      strbuf.append("-1");
    }
    strbuf.append(" HTTP/1.1");
    
    nos.reset();
    nos.writeMessage(msg, ack, currentTimeMillis);

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
    os.flush();
  }

  protected final short getRequest(InputStream is,
                             NetworkInputStream nis,
                             byte[] buf) throws Exception {
    String line = null;

    line = readLine(is, buf);
    if ((line == null) ||
        (! (line.startsWith("GET ") || line.startsWith("PUT ")))) {
      throw new Exception("Bad request: " + line);
    }

    int idx1 = line.indexOf("?from=");
    if (idx1 == -1) throw new Exception("Bad request: " + line);
    int idx2 = line.indexOf("&", idx1);
    if (idx2 == -1) throw new Exception("Bad request: " + line);
    short from = Short.parseShort(line.substring(idx1+6, idx2));

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is, buf);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
        length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (nis.readFrom(is, length) != length)
      logmon.log(BasicLevel.WARN, name + "Bad request length: " + length);

    return from;
  }

  protected final void sendReply(Message msg,
                                 OutputStream os,
                                 NetworkOutputStream nos,
                                 int ack,
                                 long currentTimeMillis) throws Exception {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("HTTP/1.1 200 OK\r\n");

    nos.reset();
    nos.writeMessage(msg, ack, currentTimeMillis);

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
    os.flush();
  }

  protected void getReply(InputStream is,
                          NetworkInputStream nis,
                          byte[] buf) throws Exception {
    String line = null;

    line = readLine(is, buf);
    if ((line == null) ||
        ((! line.equals("HTTP/1.1 200 OK")) &&
         (! line.equals("HTTP/1.1 204 No Content")))) {
      throw new Exception("Bad reply: " + line);
    }

    // Skip all header lines, get length
    int length = 0;
    while (line != null) {
      line = readLine(is, buf);
      if ((line != null) && line.startsWith("Content-Length: ")) {
        // get content length
        length = Integer.parseInt(line.substring(16));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, name + ", length:" + length);
      }
    }

    if (nis.readFrom(is, length) != length)
      logmon.log(BasicLevel.WARN, name + "Bad reply length: " + length);
  }

  protected int handle(Message msgout,
                       NetworkInputStream nis) throws Exception {
    int ack = nis.getAckStamp();

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 this.getName() + ", handle: " + msgout + ", ack=" + ack);

    if ((msgout != null) && (msgout.stamp == ack)) {
      
      //AgentServer.getTransaction().begin();
      
      //  Suppress the processed notification from message queue,
      // and deletes it.
      qout.removeMessage(msgout);
      msgout.delete();
      msgout.free();
      AgentServer.getTransaction().commit(true);
    }

    Message msg = nis.getMessage();
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 this.getName() + ", get: " + msg);

    if (msg != null) {
      ack = msg.stamp;
      testBootTS(msg.getSource(), nis.getBootTS());
      deliver(msg);
      return ack;
    }

    return -1;
  }

  final class NetServerOut extends Daemon {
    Socket socket = null;

    InputStream is = null;
    OutputStream os = null;

    NetworkInputStream nis = null;
    NetworkOutputStream nos = null;

    NetServerOut(String name, Logger logmon) throws IOException {
      super(name + ".NetServerOut", logmon);
      // Overload logmon definition in Daemon
      this.logmon = logmon;

      nis = new NetworkInputStream();
      nos = new NetworkOutputStream();
    }
    
    int nbCnxTry = 0;

    protected void open(long currentTimeMillis) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, this.getName() + ", open: " + nbCnxTry);

      // If there are errors wait to open the connection.
      if (nbCnxTry != 0) {
        try {
          if (nbCnxTry < WDNbRetryLevel1) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, this.getName() + ", wait for " + WDRetryPeriod1);
            Thread.sleep(WDRetryPeriod1);
          } else if (nbCnxTry < WDNbRetryLevel2) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, this.getName() + ", wait for " + WDRetryPeriod2);
            Thread.sleep(WDRetryPeriod2);
          } else {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, this.getName() + ", wait for " + WDRetryPeriod3);
            Thread.sleep(WDRetryPeriod3);
          }
        } catch (InterruptedException exc) {}
        
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, this.getName() + ", end of watchdog period");
      }
      
      // Open the connection.
      socket = null;

      nbCnxTry += 1;
      if (proxy == null) {
        try {
          socket = createSocket(server);
        } catch (IOException exc) {
          logmon.log(BasicLevel.WARN, this.getName() + ", connection refused", exc);
          throw exc;
        }
      } else {
        try {
          socket = createSocket(proxy, proxyport);
        } catch (IOException exc) {
          logmon.log(BasicLevel.WARN, this.getName() + ", connection refused, reset addr");
          proxy = InetAddress.getByName(proxyhost);
          throw exc;
        }
      }
      nbCnxTry = 0;
      setSocketOption(socket);

      os = socket.getOutputStream();
      is = socket.getInputStream();
    }

    protected void close() {
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

    protected void shutdown() {
      thread.interrupt();
      close();
    }

    public void run() {
      Message msgout = null;
      int ack = -1;
      long currentTimeMillis = -1;
      byte[] buf = new byte[120];

      try {
        while (running) {
          canStop = true;

          try {
            currentTimeMillis = System.currentTimeMillis();
            do {
              // Removes all expired messages in qout.
              // AF: This task should be run regularly.
              msgout = qout.removeExpired(currentTimeMillis);
              if (msgout != null) {
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", Handles expired message: " + msgout);

                if (msgout.not.deadNotificationAgentId != null) {
                  ExpiredNot expiredNot = new ExpiredNot(msgout.not,
                                                         msgout.from,
                                                         msgout.to);
                  
                  // JORAM_PERf_BRANCH
                  //AgentServer.getTransaction().begin();
                  Channel.postAndValidate(Message.alloc(AgentId.localId, msgout.not.deadNotificationAgentId,
                                             expiredNot));
                  //Channel.validate();
                  
                  AgentServer.getTransaction().commit(true);
                }
                // Suppress the processed notification from message queue and deletes it.
                // It can be done outside of a transaction and committed later (on next handle).
                msgout.delete();
                msgout.free();
              }
            } while (msgout != null);

            try {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", waiting message: " + activationPeriod);

              msgout = qout.get(activationPeriod);
            } catch (InterruptedException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", interrupted");
            }
            open(currentTimeMillis);

            do {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendRequest: " + msgout + ", ack=" + ack);

              currentTimeMillis = System.currentTimeMillis();
              do {
                if ((msgout != null) &&
                    (msgout.not.expiration > 0) &&
                    (msgout.not.expiration < currentTimeMillis)) {
                  if (msgout.not.deadNotificationAgentId != null) {
                    if (logmon.isLoggable(BasicLevel.DEBUG)) {
                      logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification "
                                 + msgout.from + ", " + msgout.not + " to " + msgout.not.deadNotificationAgentId);
                    }
                    ExpiredNot expiredNot = new ExpiredNot(msgout.not, msgout.from, msgout.to);
                    
                    // JORAM_PERF_BRANCH
                    // AgentServer.getTransaction().begin();
                    Channel.postAndValidate(Message.alloc(AgentId.localId, msgout.not.deadNotificationAgentId,
                                               expiredNot));
                    //Channel.validate();
                    
                    AgentServer.getTransaction().commit(true);
                  } else {
                    if (logmon.isLoggable(BasicLevel.DEBUG)) {
                      logmon.log(BasicLevel.DEBUG, getName() + ": removes expired notification "
                                 + msgout.from + ", " + msgout.not);
                    }
                  }
                  // Suppress the processed notification from message queue,
                  // and deletes it. It can be done outside of a transaction
                  // and committed later (on next handle).
                  qout.removeMessage(msgout);
                  msgout.delete();
                  msgout.free();

                  msgout = qout.get(0L);
                  continue;
                }
                break;
              } while (true);

              sendRequest(msgout, os, nos, ack, currentTimeMillis);
              getReply(is, nis, buf);

              canStop = false;
              ack = handle(msgout, nis);
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
            close();
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

    NetworkInputStream nis = null;
    NetworkOutputStream nos = null;

    NetServerIn(String name, ServerSocket listen, Logger logmon) throws IOException {
      super(name + ".NetServerIn", logmon);
      this.listen = listen;
      // Overload logmon definition in Daemon
      this.logmon = logmon;

      nis = new NetworkInputStream();
      nos = new NetworkOutputStream();
    }

    protected void open(Socket socket) throws IOException {
      setSocketOption(socket);

      os = socket.getOutputStream();
      is = socket.getInputStream();

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");
    }

    protected void close() {
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

    protected void shutdown() {
      close();
      try {
        listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    public void run() {
      Message msgout= null;
      int ack = -1;

      byte[] buf = new byte[120];

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

            msgout = null;
            short from = getRequest(is, nis, buf);
            long currentTimeMillis = System.currentTimeMillis();
            do {
              canStop = false;
              ack = handle(msgout, nis);
              canStop = true;

              do {
                msgout = qout.getMessageTo(from);

                if ((msgout != null) && (msgout.not.expiration > 0L)
                    && (msgout.not.expiration < currentTimeMillis)) {

                  if (msgout.not.deadNotificationAgentId != null) {
                    if (logmon.isLoggable(BasicLevel.DEBUG)) {
                      logmon.log(BasicLevel.DEBUG, getName() + ": forward expired notification "
                                 + msgout.from + ", " + msgout.not + " to " + msgout.not.deadNotificationAgentId);
                    }
                    ExpiredNot expiredNot = new ExpiredNot(msgout.not, msgout.from, msgout.to);
                    
                    // JORAM_PERF_BRANCH
                    //AgentServer.getTransaction().begin();
                    Channel.postAndValidate(Message.alloc(AgentId.localId,
                                               msgout.not.deadNotificationAgentId,
                                               expiredNot));
                    //Channel.validate();
                    
                    AgentServer.getTransaction().commit(true);
                  } else {
                    if (logmon.isLoggable(BasicLevel.DEBUG)) {
                      logmon.log(BasicLevel.DEBUG,
                                 getName() + ": removes expired notification " +
                                 msgout.from + ", " + msgout.not);
                    }
                  }
                  // Suppress the processed notification from message queue,
                  // and deletes it. It can be done outside of a transaction
                  // and committed later (on next handle).
                  qout.removeMessage(msgout);
                  msgout.delete();
                  msgout.free();

                  continue;
                }
                break;
              } while (true);

              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           this.getName() + ", sendReply: " + msgout);

              sendReply(msgout, os, nos, ack, currentTimeMillis);
              getRequest(is, nis, buf);
            } while (running);
          } catch (Exception exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, ", connection closed", exc);
          } finally {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, ", connection ends");
            close();
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
  final class NetworkInputStream extends BufferedMessageInputStream {
    NetworkInputStream() {
      super();
    }

    // The boot timestamp of the incoming message.
    int boot;
    // The stamp of last acked outgoing message.
    int ack;

    /**
     * Reads the protocol header from this output stream.
     */
    protected void readHeader() throws IOException {
      readFully(8);
      // Reads boot timestamp of source server
      boot = readInt();
      ack = readInt();
    }

    Message msg = null;

    int readFrom(InputStream is, int length) throws Exception {
      this.in = is;
      if (length == 8) {
        readHeader();
        msg = null;
      } else {
        msg = readMessage();
      }

      clean();

      return length;
    }

    Message getMessage() {
      return msg;
    }

    int getBootTS() {
      return boot;
    }

    int getAckStamp() {
      return ack;
    }
  }

  /**
   * Class used to send messages through a stream.
   */
  final class NetworkOutputStream extends ByteArrayMessageOutputStream {
    /** Stamp of last acked message. */
    private int ack;

    NetworkOutputStream() throws IOException {
      super();
    }

    /**
     * Writes the protocol header to this output stream.
     */
    protected void writeHeader() {
      // Writes boot timestamp of source server
      writeInt(getBootTS());
      // Writes stamp of last received message
      writeInt(ack);
    }

    void writeMessage(Message msg, int ack,
                      long currentTimeMillis) throws IOException {
      this.ack = ack;
      super.writeMessage(msg, currentTimeMillis);
    }
  }
}
