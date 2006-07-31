/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
public class FactoryParameters implements java.io.Serializable
{
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
   *
   */
  public int multiThreadSyncDelay = 1;

  /**
   * This threshold is the maximum messages
   * number over
   * which the subscription is passivated.
   * Default is Integer.MAX_VALUE.
   */
  public int topicPassivationThreshold = Integer.MAX_VALUE;

  /**
   * This threshold is the minimum
   * messages number below which
   * the subscription is activated.
   * Default is 0.
   */
  public int topicActivationThreshold = 0;

  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param host  Name of host hosting the server to create connections with.
   * @param port  Port to be used for accessing the server.
   */
  public FactoryParameters(String host, int port)
  {
    this.host = host;
    this.port = port;
  }

  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param url  joram ha url
   */
  public FactoryParameters(String url)
  {
    this.url = url;
    host = "";
    port = -1;
  }

  /**
   * Returns the name of host hosting the server to create connections with.
   */
  public String getHost()
  {
    return host;
  }

  /** Returns the port to be used for accessing the server. */
  public int getPort()
  {
    return port;
  }

  /**
   * Returns the url.
   */
  public String getUrl()
  {
    return url;
  }

  public void toReference(Reference ref) {
    ref.add(new StringRefAddr("cFactory.host", getHost()));
    ref.add(new StringRefAddr("cFactory.port", new Integer(getPort())
        .toString()));
    ref.add(new StringRefAddr("cFactory.url", getUrl()));
    ref.add(new StringRefAddr("cFactory.cnxT", new Integer(
        connectingTimer).toString()));
    ref.add(new StringRefAddr("cFactory.txT",
        new Integer(txPendingTimer).toString()));
    ref.add(new StringRefAddr("cFactory.cnxPT", new Integer(
       cnxPendingTimer).toString()));
    ref.add(new StringRefAddr("cFactory.asyncSend", new Boolean(
        asyncSend).toString()));
    ref.add(new StringRefAddr("cFactory.queueMessageReadMax", new Integer(
        queueMessageReadMax).toString()));
  }

  public void fromReference(Reference ref) {
    String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
    String txTimer = (String) ref.get("cFactory.txT").getContent();
    String cnxPTimer = (String) ref.get("cFactory.cnxPT").getContent();
    String asyncSendS= (String) ref.get("cFactory.asyncSend").getContent();
    String queueMessageReadMaxS = (String) ref.get("cFactory.queueMessageReadMax").getContent();
    connectingTimer = (new Integer(cnxTimer)).intValue();
    txPendingTimer = (new Integer(txTimer)).intValue();
    cnxPendingTimer = (new Integer(cnxPTimer)).intValue();
    asyncSend = (new Boolean(asyncSendS)).booleanValue();
    queueMessageReadMax = (new Integer(queueMessageReadMaxS)).intValue();
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
      ",queueMessageReadMax=" + queueMessageReadMax + ')';
  }
}
