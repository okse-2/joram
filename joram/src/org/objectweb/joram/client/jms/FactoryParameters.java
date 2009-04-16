/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms;

import java.util.Hashtable;

import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import fr.dyade.aaa.util.SocketFactory;

/**
 * A <code>FactoryParameters</code> instance holds a
 * <code>&lt;XA&gt;ConnectionFactory</code> configuration parameters.
 */
public class FactoryParameters implements java.io.Serializable, Cloneable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Name of host hosting the server to create connections with. */
  private String host;

  /**
   * Returns the name of host hosting the server to create connections with.
   *
   * @return The name of host hosting the server.
   */
  public String getHost() {
    return host;
  }

  /** Port to be used for accessing the server. */
  private int port;

  /**
   * Returns the port to be used for accessing the server.
   *
   * @return The port to be used for accessing the server.
   */
  public int getPort() {
    return port;
  }

  /**
   * url needed to connect to joram HA
   */
  private String url;

  /**
   * Returns the url to be used for accessing the server.
   *
   * @return The url to be used for accessing the server.
   */
  public String getUrl() {
    return url;
  }

  /**
   *  Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm),
   * default value is true.
   */
  public boolean TcpNoDelay = true;
  /**
   *  Enable SO_LINGER with the specified linger time in seconds, if the
   * value is less than 0 then it disables SO_LINGER. Default value is -1.
   */
  public int SoLinger = -1;
  /**
   *  Enable/disable SO_TIMEOUT with the specified timeout in milliseconds.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 0.
   */
  public int SoTimeout = 0;
  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 0;
  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   * <p>
   * The default value is 0 (no timer).
   */
  public int txPendingTimer = 0;
  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  public int cnxPendingTimer = 0;

  /**
   * Allows to define a specific factory for socket in order to by-pass
   * compatibility problem between JDK version.
   * Currently there is two factories, The default factory one for JDK
   * since 1.4, and "fr.dyade.aaa.util.SocketFactory13" for JDK prior to 1.4.
   */
  public String socketFactory = SocketFactory.DefaultFactory;
  
  /**
   *  Determines whether the messages consumed are implicitly acknowledged
   * or not. When true messages are immediately removed from queue when
   * delivered.
   */
  public boolean implicitAck;

  /**
   *  Determines whether the produced messages are asynchronously
   * sent or not (without or with acknowledgement)
   * <p>
   *  Default is false (with ack).
   */
  public boolean asyncSend = false;

  /**
   *  The maximum number of messages that can be read at once from a queue.
   * <p>
   *  Default is 1.
   */
  public int queueMessageReadMax = 1;

  /**
   *  The maximum number of acknowledgements that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode when listening to a topic.
   * <p>
   *  Default is 0.
   */
  public int topicAckBufferMax = 0;

  /**
   * Determines whether client threads which are using the same connection
   * are synchronized in order to group together the requests they send.
   */
  public boolean multiThreadSync = false;

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * <p>
   *  Either they wake up (wait time out) or they are notified (by the first
   * waken up thread).
   * <p>
   *  Default value is 1ms.
   */
  public int multiThreadSyncDelay = 1;

  /**
   * The maximum numbers of threads that hang if 'multiThreadSync' is true.
   * <p>
   * Default value is 10 waiting threads.
   */
  public int multiThreadSyncThreshold = 10;

  /**
   *  This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  Default is Integer.MAX_VALUE.
   */
  public int topicPassivationThreshold = Integer.MAX_VALUE;

  /**
   *  This threshold is the minimum messages number below which the
   * subscription is activated.
   * <p>
   *  Default value is 0.
   */
  public int topicActivationThreshold = 0;
  
  /**
   *  This is the local IP address on which the TCP connection is activated. 
   * <p>
   *  The value can either be a machine name, such as "java.sun.com", or a
   * textual representation of its IP address.
   */
  public String outLocalAddress = null;

  /**
   * This is the local IP address port on which the TCP connection is activated
   */
  public int outLocalPort = 0;

  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param host  Name of host hosting the server to create connections with.
   * @param port  Port to be used for accessing the server.
   */
  public FactoryParameters(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param url  joram ha url
   */
  public FactoryParameters(String url) {
    this.url = url;
    host = "";
    port = -1;
  }

  /**
   * Constructs an empty <code>FactoryParameters</code>.
   */
  public FactoryParameters() {}

//   public void toReference(Reference ref) {
//     toReference(ref, "cf");
//   }

  public void toReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    ref.add(new StringRefAddr(prefix + ".host", getHost()));
    ref.add(new StringRefAddr(prefix + ".port",
                              new Integer(getPort()).toString()));

    ref.add(new StringRefAddr(prefix + ".url", getUrl()));

    ref.add(new StringRefAddr(prefix + ".TcpNoDelay",
                              new Boolean(TcpNoDelay).toString()));
    ref.add(new StringRefAddr(prefix + ".SoLinger",
                              new Integer(SoLinger).toString()));
    ref.add(new StringRefAddr(prefix + ".SoTimeout",
                              new Integer(SoTimeout).toString()));

    ref.add(new StringRefAddr(prefix + ".cnxT",
                              new Integer(connectingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".txT",
                              new Integer(txPendingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".cnxPT", 
                              new Integer(cnxPendingTimer).toString()));

    ref.add(new StringRefAddr(prefix + ".socketFactory", socketFactory));

    ref.add(new StringRefAddr(prefix + ".implicitAck", 
                              new Boolean(implicitAck).toString()));
    ref.add(new StringRefAddr(prefix + ".asyncSend", 
                              new Boolean(asyncSend).toString()));
    ref.add(new StringRefAddr(prefix + ".queueMessageReadMax", 
                              new Integer(queueMessageReadMax).toString()));
    ref.add(new StringRefAddr(prefix + ".topicAckBufferMax", 
                              new Integer(topicAckBufferMax).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSync", 
                              new Boolean(multiThreadSync).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSyncDelay", 
                              new Integer(multiThreadSyncDelay).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSyncThreshold", 
                              new Integer(multiThreadSyncThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".topicPassivationThreshold", 
                              new Integer(topicPassivationThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".topicActivationThreshold", 
                              new Integer(topicActivationThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".outLocalPort", new Integer(outLocalPort).toString()));
    if (outLocalAddress != null)
      ref.add(new StringRefAddr(prefix + ".outLocalAddress", outLocalAddress));
  }

//   public void fromReference(Reference ref) {
//     fromReference(ref, "cf");
//   }

  public void fromReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    host = (String) ref.get(prefix + ".host").getContent();
    port = new Integer((String) ref.get(prefix + ".port").getContent()).intValue();

    url = (String) ref.get(prefix + ".url").getContent();

    TcpNoDelay = new Boolean((String) ref.get(prefix + ".TcpNoDelay").getContent()).booleanValue();
    SoLinger = new Integer((String) ref.get(prefix + ".SoLinger").getContent()).intValue();
    SoTimeout = new Integer((String) ref.get(prefix + ".SoTimeout").getContent()).intValue();

    connectingTimer = new Integer((String) ref.get(prefix + ".cnxT").getContent()).intValue();
    txPendingTimer = new Integer((String) ref.get(prefix + ".txT").getContent()).intValue();
    cnxPendingTimer = new Integer((String) ref.get(prefix + ".cnxPT").getContent()).intValue();

    socketFactory = (String) ref.get(prefix + ".socketFactory").getContent();
    
    implicitAck = new Boolean((String) ref.get(prefix + ".implicitAck").getContent()).booleanValue();
    asyncSend = new Boolean((String) ref.get(prefix + ".asyncSend").getContent()).booleanValue();
    queueMessageReadMax = new Integer((String) ref.get(prefix + ".queueMessageReadMax").getContent()).intValue();
    topicAckBufferMax = new Integer((String) ref.get(prefix + ".topicAckBufferMax").getContent()).intValue();
    multiThreadSync = new Boolean((String) ref.get(prefix + ".multiThreadSync").getContent()).booleanValue();
    multiThreadSyncDelay = new Integer((String) ref.get(prefix + ".multiThreadSyncDelay").getContent()).intValue();
    multiThreadSyncThreshold = new Integer((String) ref.get(prefix + ".multiThreadSyncThreshold").getContent()).intValue();
    topicPassivationThreshold = new Integer((String) ref.get(prefix + ".topicPassivationThreshold").getContent()).intValue();
    topicActivationThreshold = new Integer((String) ref.get(prefix + ".topicActivationThreshold").getContent()).intValue();
    outLocalPort = new Integer((String) ref.get(prefix + ".outLocalPort").getContent()).intValue();
    RefAddr outLocalAddressRef = ref.get(prefix + ".outLocalAddress");
    if (outLocalAddressRef != null)
      outLocalAddress = (String) outLocalAddressRef.getContent();
  }

  public Hashtable code(Hashtable h, String prefix) {
    if (getHost() != null)
      h.put(prefix + ".host", getHost());
    h.put(prefix + ".port", new Integer(getPort()));

    if (getUrl() != null)
      h.put(prefix + ".url", getUrl());

    h.put(prefix + ".TcpNoDelay", new Boolean(TcpNoDelay));
    h.put(prefix + ".SoLinger", new Integer(SoLinger));
    h.put(prefix + ".SoTimeout", new Integer(SoTimeout));

    h.put(prefix + ".connectingTimer", new Integer(connectingTimer));
    h.put(prefix + ".txPendingTimer", new Integer(txPendingTimer));
    h.put(prefix + ".cnxPendingTimer", new Integer(cnxPendingTimer));

    h.put(prefix + ".socketFactory", socketFactory);
    
    h.put(prefix + ".implicitAck", new Boolean(implicitAck));
    h.put(prefix + ".asyncSend", new Boolean(asyncSend));
    h.put(prefix + ".queueMessageReadMax", new Integer(queueMessageReadMax));
    h.put(prefix + ".topicAckBufferMax", new Integer(topicAckBufferMax));
    h.put(prefix + ".multiThreadSync", new Boolean(multiThreadSync));
    h.put(prefix + ".multiThreadSyncDelay", new Integer(multiThreadSyncDelay));
    h.put(prefix + ".multiThreadSyncThreshold", new Integer(multiThreadSyncThreshold));
    h.put(prefix + ".topicPassivationThreshold", new Integer(topicPassivationThreshold));
    h.put(prefix + ".topicActivationThreshold", new Integer(topicActivationThreshold));
    h.put(prefix + ".outLocalPort", new Integer(outLocalPort));
    if (outLocalAddress != null)
      h.put(prefix + ".outLocalAddress", outLocalAddress);

    return h;
  }

  public void decode(Hashtable h, String prefix) {
    host = (String) h.get(prefix + ".host");
    port = ((Integer) h.get(prefix + ".port")).intValue();

    url = (String) h.get(prefix + ".url");

    TcpNoDelay = ((Boolean) h.get(prefix + ".TcpNoDelay")).booleanValue();
    SoLinger = ((Integer) h.get(prefix + ".SoLinger")).intValue();
    SoTimeout = ((Integer) h.get(prefix + ".SoTimeout")).intValue();

    connectingTimer = ((Integer) h.get(prefix + ".connectingTimer")).intValue();
    txPendingTimer = ((Integer) h.get(prefix + ".txPendingTimer")).intValue();
    cnxPendingTimer = ((Integer) h.get(prefix + ".cnxPendingTimer")).intValue();

    socketFactory = (String) h.get(prefix + ".socketFactory");
    
    implicitAck = ((Boolean) h.get(prefix + ".implicitAck")).booleanValue();
    asyncSend = ((Boolean) h.get(prefix + ".asyncSend")).booleanValue();
    queueMessageReadMax = ((Integer) h.get(prefix + ".queueMessageReadMax")).intValue();
    topicAckBufferMax = ((Integer) h.get(prefix + ".topicAckBufferMax")).intValue();
    multiThreadSync = ((Boolean) h.get(prefix + ".multiThreadSync")).booleanValue();
    multiThreadSyncDelay = ((Integer) h.get(prefix + ".multiThreadSyncDelay")).intValue();
    multiThreadSyncThreshold = ((Integer) h.get(prefix + ".multiThreadSyncThreshold")).intValue();
    topicPassivationThreshold = ((Integer) h.get(prefix + ".topicPassivationThreshold")).intValue();
    topicActivationThreshold = ((Integer) h.get(prefix + ".topicActivationThreshold")).intValue();
    outLocalPort = ((Integer) h.get(prefix + ".outLocalPort")).intValue();
    outLocalAddress = (String) h.get(prefix + ".outLocalAddress");
  }

  public Object clone() {
    Object clone = null;
    try {
      clone = super.clone();
    } catch (CloneNotSupportedException exc) {
      // Unreachable
    }
    return clone;
  }
  
  public String toString() {
    return '(' + super.toString() +
      ",host=" + host +
      ",port=" + port +
      ",url" + url +
      ",connectingTimer=" + connectingTimer +
      ",txPendingTimer=" + txPendingTimer +
      ",cnxPendingTimer=" + cnxPendingTimer +
      ",implicitAck=" + implicitAck +
      ",asyncSend=" + asyncSend +
      ",topicAckBufferMax=" + topicAckBufferMax +
      ",multiThreadSync=" + multiThreadSync +
      ",multiThreadSyncDelay=" + multiThreadSyncDelay +
      ",multiThreadSyncThreshold=" + multiThreadSyncThreshold +
      ",topicPassivationThreshold=" + topicPassivationThreshold +
      ",topicActivationThreshold=" + topicActivationThreshold +
      ",outLocalAddress=" + outLocalAddress +
      ",outLocalPort=" + outLocalPort + ')';
  }
}
