/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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

import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * A <code>FactoryParameters</code> instance holds a
 * <code>&lt;XA&gt;ConnectionFactory</code> configuration parameters.
 */
public class FactoryParameters implements java.io.Serializable {
  /** Name of host hosting the server to create connections with. */
  private String host;
  /** Port to be used for accessing the server. */
  private int port;

  /**
   * url to connect to joram ha
   */
  private String url;

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
   * Determines whether the produced messages are asynchronously
   * sent or not (without or with acknowledgement)
   * Default is false (with ack).
   */
  public boolean asyncSend = false;

  /**
   * The maximum number of messages that can be
   * read at once from a queue.
   * Default is 1.
   */
  public int queueMessageReadMax = 1;

  /**
   * The maximum number of acknowledgements
   * that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode when listening to a topic.
   * Default is 0.
   */
  public int topicAckBufferMax = 0;

  /**
   * Determines whether client threads
   * which are using the same connection
   * are synchronized
   * in order to group together the requests they
   * send.
   */
  public boolean multiThreadSync = false;

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * Either they wake up (wait time out) or they are notified (by the
   * first woken up thread).
   * Default value is 1ms.
   */
  public int multiThreadSyncDelay = 1;

  /**
   * The maximum numbers of threads that hang if 'multiThreadSync' is true.
   * Default value is 10 waiting threads.
   */
  public int multiThreadSyncThreshold = 10;

  /**
   * This threshold is the maximum messages number over which the
   * subscription is passivated.
   * Default is Integer.MAX_VALUE.
   */
  public int topicPassivationThreshold = Integer.MAX_VALUE;

  /**
   * This threshold is the minimum messages number below which the
   * subscription is activated.
   * Default is 0.
   */
  public int topicActivationThreshold = 0;

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
  public FactoryParameters() {
  }

  /**
   * Returns the name of host hosting the server to create connections with.
   */
  public String getHost() {
    return host;
  }

  /** Returns the port to be used for accessing the server. */
  public int getPort() {
    return port;
  }

  /**
   * Returns the url.
   */
  public String getUrl() {
    return url;
  }

//   public void toReference(Reference ref) {
//     toReference(ref, "cf");
//   }

  public void toReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    ref.add(new StringRefAddr(prefix + ".host", getHost()));
    ref.add(new StringRefAddr(prefix + ".port",
                              new Integer(getPort()).toString()));

    ref.add(new StringRefAddr(prefix + ".url", getUrl()));
    ref.add(new StringRefAddr(prefix + ".cnxT",
                              new Integer(connectingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".txT",
                              new Integer(txPendingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".cnxPT", 
                              new Integer(cnxPendingTimer).toString()));
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
  }

//   public void fromReference(Reference ref) {
//     fromReference(ref, "cf");
//   }

  public void fromReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    host = (String) ref.get(prefix + ".host").getContent();
    port = new Integer((String) ref.get(prefix + ".port").getContent()).intValue();

    connectingTimer = new Integer((String) ref.get(prefix + ".cnxT").getContent()).intValue();
    txPendingTimer = new Integer((String) ref.get(prefix + ".txT").getContent()).intValue();
    cnxPendingTimer = new Integer((String) ref.get(prefix + ".cnxPT").getContent()).intValue();
    asyncSend = new Boolean((String) ref.get(prefix + ".asyncSend").getContent()).booleanValue();
    queueMessageReadMax = new Integer((String) ref.get(prefix + ".queueMessageReadMax").getContent()).intValue();
    topicAckBufferMax = new Integer((String) ref.get(prefix + ".topicAckBufferMax").getContent()).intValue();
    multiThreadSync = new Boolean((String) ref.get(prefix + ".multiThreadSync").getContent()).booleanValue();
    multiThreadSyncDelay = new Integer((String) ref.get(prefix + ".multiThreadSyncDelay").getContent()).intValue();
    multiThreadSyncThreshold = new Integer((String) ref.get(prefix + ".multiThreadSyncThreshold").getContent()).intValue();
    topicPassivationThreshold = new Integer((String) ref.get(prefix + ".topicPassivationThreshold").getContent()).intValue();
    topicActivationThreshold = new Integer((String) ref.get(prefix + ".topicActivationThreshold").getContent()).intValue();
  }

  public Hashtable toHashtable() {
    Hashtable h = new Hashtable();

    h.put("host", getHost());
    h.put("port", new Integer(getPort()));
    h.put("connectingTimer", new Integer(connectingTimer));
    h.put("txPendingTimer", new Integer(txPendingTimer));
    h.put("cnxPendingTimer", new Integer(cnxPendingTimer));
    h.put("asyncSend", new Boolean(asyncSend));
    h.put("queueMessageReadMax", new Integer(queueMessageReadMax));
    h.put("topicAckBufferMax", new Integer(topicAckBufferMax));
    h.put("multiThreadSync", new Boolean(multiThreadSync));
    h.put("multiThreadSyncDelay", new Integer(multiThreadSyncDelay));
    h.put("multiThreadSyncThreshold", new Integer(multiThreadSyncThreshold));
    h.put("topicPassivationThreshold", new Integer(topicPassivationThreshold));
    h.put("topicActivationThreshold", new Integer(topicActivationThreshold));

    return h;
  }

  public static FactoryParameters fromHashtable(Hashtable h) {
    FactoryParameters params = new FactoryParameters((String) h.get("host"),
        ((Integer) h.get("port")).intValue());
    params.connectingTimer = ((Integer) h.get("connectingTimer")).intValue();
    params.txPendingTimer = ((Integer) h.get("txPendingTimer")).intValue();
    params.cnxPendingTimer = ((Integer) h.get("cnxPendingTimer")).intValue();
    params.asyncSend = ((Boolean) h.get("asyncSend")).booleanValue();
    params.queueMessageReadMax = ((Integer) h.get("queueMessageReadMax")).intValue();
    params.topicAckBufferMax = ((Integer) h.get("topicAckBufferMax")).intValue();
    params.multiThreadSync = ((Boolean) h.get("multiThreadSync")).booleanValue();
    params.multiThreadSyncDelay = ((Integer) h.get("multiThreadSyncDelay")).intValue();
    params.multiThreadSyncThreshold = ((Integer) h.get("multiThreadSyncThreshold")).intValue();
    params.topicPassivationThreshold = ((Integer) h.get("topicPassivationThreshold")).intValue();
    params.topicActivationThreshold = ((Integer) h.get("topicActivationThreshold")).intValue();

    return params;
  }

  public String toString() {
    return '(' + super.toString() +
      ",host=" + host +
      ",port=" + port +
      ",connectingTimer=" + connectingTimer +
      ",txPendingTimer=" + txPendingTimer +
      ",cnxPendingTimer=" + cnxPendingTimer +
      ",asyncSend=" + asyncSend +
      ",topicAckBufferMax=" + topicAckBufferMax +
      ",multiThreadSync=" + multiThreadSync +
      ",multiThreadSyncDelay=" + multiThreadSyncDelay +
      ",multiThreadSyncThreshold=" + multiThreadSyncThreshold +
      ",topicPassivationThreshold=" + topicPassivationThreshold +
      ",topicActivationThreshold=" + topicActivationThreshold + ')';
  }
}
