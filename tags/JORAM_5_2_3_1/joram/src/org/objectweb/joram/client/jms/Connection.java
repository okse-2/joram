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
 */
package org.objectweb.joram.client.jms;

import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.client.jms.connection.Requestor;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.CnxConnectReply;
import org.objectweb.joram.shared.client.CnxConnectRequest;
import org.objectweb.joram.shared.client.CnxStartRequest;
import org.objectweb.joram.shared.client.CnxStopRequest;
import org.objectweb.joram.shared.client.ConsumerSubRequest;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Implements the <code>javax.jms.Connection</code> interface.
 */
public class Connection implements javax.jms.Connection {
  public static Logger logger = Debug.getLogger(Connection.class.getName());
  
  /**
   * Status of the connection.
   */
  private static class Status {
    /**
     * Status of the connection when it is stopped.
     * This is the initial status.
     */
    public static final int STOP = 0;

    /**
     * Status of the connection when it is started.
     */
    public static final int START = 1;

    /**
     * Status of the conenction when it is closed.
     */
    public static final int CLOSE = 2;

    private static final String[] names = {
      "STOP", "START", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The request multiplexer used to communicate
   * with the user proxy.
   */
  private RequestMultiplexer mtpx;

  /**
   * The requestor used to communicate
   * with the user proxy.
   */
  private Requestor requestor;

  /** Connection meta data. */
  private ConnectionMetaData metaData = null;

  class AtomicCounter {
    long value;
    StringBuffer strbuf;
    int initial;

    AtomicCounter(String prefix) {
      value = 0;
      strbuf = new StringBuffer(prefix.length() + 20);
      strbuf.append(prefix);
      initial = strbuf.length();
    }

    synchronized String nextValue() {
      strbuf.setLength(initial);
      strbuf.append(value++);
      return strbuf.toString();
    }
  }

  /** Sessions counter. */
  private AtomicCounter sessionsC;

  /** Messages counter. */
  private AtomicCounter messagesC;

  /** Subscriptions counter. */
  private AtomicCounter subsC;

  /** Client's agent proxy identifier. */
  private String proxyId;

  /** Connection key. */
  private int key;

  /** The factory's parameters. */
  private FactoryParameters factoryParameters;

  /**
   * Status of the connection.
   * STOP, START, CLOSE
   */
  private int status;

  /** Vector of the connection's sessions. */
  private Vector sessions;

  /** Vector of the connection's consumers. */
  private Vector cconsumers;

  /**
   * Used to synchronize the method close()
   */
  private Closer closer;

  private String stringImage = null;
  private int hashCode;

  /**
   * Creates a <code>Connection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param connectionImpl  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection(FactoryParameters factoryParameters,
                    RequestChannel requestChannel) 
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Connection.<init>(" + factoryParameters +
                 ',' + requestChannel + ')');
    // We need to clone the FactoryParameter Object to avoid side-effect with
    // external modifications.
    this.factoryParameters = (FactoryParameters) factoryParameters.clone();
    mtpx = new RequestMultiplexer(this,
                                  requestChannel,
                                  factoryParameters.cnxPendingTimer);
    if (factoryParameters.multiThreadSync) {
      mtpx.setMultiThreadSync(factoryParameters.multiThreadSyncDelay,
                              factoryParameters.multiThreadSyncThreshold);
    }
    
    requestor = new Requestor(mtpx);
    sessions = new Vector();
    cconsumers = new Vector();
    
    closer = new Closer();
    
    setStatus(Status.STOP);

    // Requesting the connection key and proxy identifier:
    CnxConnectRequest req = new CnxConnectRequest();
    CnxConnectReply rep = (CnxConnectReply) requestor.request(req);
    proxyId = rep.getProxyId();
    key = rep.getCnxKey();

    sessionsC = new AtomicCounter("c" + key + 's');
    messagesC = new AtomicCounter("ID:" + proxyId.substring(1) + 'c' + key + 'm');
    subsC = new AtomicCounter("c"  + key + "sub");

    stringImage = "Connection[" + proxyId + ':' + key + ']';
    hashCode = (proxyId.hashCode() & 0xFFFF0000) + key;

    mtpx.setDemultiplexerDaemonName(toString());
  }

  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }

  boolean isStopped() {
    return (status == Status.STOP);
  }

  /** String image of the connection. */
  public String toString() {
    return stringImage;
  }

  public int hashCode() {
    return hashCode;
  }
  
  /**
   * Returns <code>true</code> if the parameter is a <code>Connection</code> instance
   * sharing the same proxy identifier and connection key.
   */
  public boolean equals(Object obj) {
    if (obj == this) return true;

    if (obj instanceof Connection) {
      Connection cnx = (Connection) obj;
      return (proxyId.equals(cnx.proxyId) && (key == cnx.key));
    }
    return false;
  }

  /**
   *  Returns the duration in seconds during which a JMS transacted (non XA)
   * session might be pending; above that duration the session is rolled back
   * and closed; the 0 value means "no timer".
   *
   * @return the duration in seconds during which a JMS transacted (non XA)
   * session might be pending.
   *
   * @see FactoryParameters.txPendingTimer
   * @see Session.txPendingTimer
   */
  public final long getTxPendingTimer() {
    return factoryParameters.txPendingTimer;
  }
  
  /** 
   *  Indicates whether the messages consumed are implicitly acknowledged
   * or not. If true messages are immediately removed from queue when
   * delivered.
   * <p>
   *  This attribute is inherited from FactoryParameters, by default false.
   *
   * @return true if messages produced are implicitly acknowledged.
   *
   * @see FactoryParameters.implicitAck
   * @see Session.implicitAck
   */
  public final boolean getImplicitAck() {
    return factoryParameters.implicitAck;
  }

  /** 
   *  Indicates whether the messages produced are asynchronously sent
   * or not (without or with acknowledgement).
   * <p>
   *  This attribute is inherited from FactoryParameters, by default false. 
   *
   * @return true if messages produced are asynchronously sent.
   *
   * @see FactoryParameters.asyncSend
   * @see Session.asyncSend
   */
  public final boolean getAsyncSend() {
    return factoryParameters.asyncSend;
  }
  
  /**
   *  Get the maximum number of messages that can be read at once from a queue
   * for this Connection.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 1.
   * 
   * @return    The maximum number of messages that can be read at once from
   *            a queue.
   *
   * @see FactoryParameters.queueMessageReadMax
   * @see Session.queueMessageReadMax
   */
  public final int getQueueMessageReadMax() {
    return factoryParameters.queueMessageReadMax;
  }
  
  /**
   *  Get the maximum number of acknowledgements that can be buffered when
   * using Session.DUPS_OK_ACKNOWLEDGE mode for this Connection.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 0.
   *
   * @return The Maximum number of acknowledgements that can be buffered when
   *         using Session.DUPS_OK_ACKNOWLEDGE mode.
   *
   * @see FactoryParameters.topicAckBufferMax
   * @see Session.setTopicAckBufferMax
   */
  public final int getTopicAckBufferMax() {
    return factoryParameters.topicAckBufferMax;
  }
  
  /**
   * Get the threshold of passivation for this Connection.
   * <p>
   * This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is
   * Integer.MAX_VALUE.
   *
   * @return The maximum messages number over which the subscription
   *         is passivated.
   *
   * @see FactoryParameters.topicPassivationThreshold
   * @see Session.setTopicPassivationThreshold
   */
  public final int getTopicPassivationThreshold() {
    return factoryParameters.topicPassivationThreshold;
  }
  
  /**
   * Get the threshold of activation for this Connection.
   * <p>
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 0.
   *
   * @return The minimum messages number below which the subscription
   *         is activated.
   *
   * @see FactoryParameters.topicActivationThreshold
   * @see Session.setTopicActivationThreshold
   */
  public final int getTopicActivationThreshold() {
    return factoryParameters.topicActivationThreshold;
  }
  
  /**
   * Returns the local IP address on which the TCP connection is activated. 
   * <p>
   * This attribute is inherited from FactoryParameters.
   *  
   * @return the local IP address on which the TCP connection is activated.
   *
   * @see FactoryParameters.outLocalAddress
   */
  public final String getOutLocalAddress() {
    return factoryParameters.outLocalAddress;
  }

  /**
   * Returns the local IP address port on which the TCP connection is activated
   * <p>
   * This attribute is inherited from FactoryParameters.
   *  
   * @return the local IP address port on which the TCP connection is activated.
   *
   * @see FactoryParameters.outLocalPort
   */
  public final int getOutLocalPort() {
    return factoryParameters.outLocalPort;
  }
  
  /**
   * Checks if the connection is closed. If true raises an
   * IllegalStateException.
   */
  final protected synchronized void checkClosed() throws IllegalStateException {
    if (status == Status.CLOSE ||  mtpx.isClosed()) 
      throw new IllegalStateException("Forbidden call on a closed connection.");
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public synchronized javax.jms.ConnectionConsumer
      createConnectionConsumer(
        javax.jms.Destination dest, 
        String selector,
        javax.jms.ServerSessionPool sessionPool,
        int maxMessages) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 stringImage + ".createConnectionConsumer(" + dest + ',' + selector + ',' +
                 sessionPool + ',' + maxMessages + ')');
    checkClosed();
    return createConnectionConsumer(dest, null, selector, sessionPool, maxMessages);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
      createDurableConnectionConsumer(javax.jms.Topic topic, 
                                      String subName,
                                      String selector,
                                      javax.jms.ServerSessionPool sessPool,
                                      int maxMessages) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 stringImage + ".createDurableConnectionConsumer(" + topic + ',' + subName + ',' + selector + ',' +
                 sessPool + ',' + maxMessages + ')');
    checkClosed();
    if (subName == null) 
      throw new JMSException("Invalid subscription name: " + subName);
    return createConnectionConsumer(topic, subName, selector, sessPool, maxMessages);
  }
  
  private synchronized javax.jms.ConnectionConsumer
    createConnectionConsumer(
        javax.jms.Destination dest, 
        String subName,
        String selector,
        javax.jms.ServerSessionPool sessionPool,
        int maxMessages)  throws JMSException {
    checkClosed();
    
    try {
      org.objectweb.joram.shared.selectors.Selector.checks(selector);
    } catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    if (sessionPool == null)
      throw new JMSException("Invalid ServerSessionPool parameter: "
                             + sessionPool);
    if (maxMessages <= 0)
      throw new JMSException("Invalid maxMessages parameter: " + maxMessages);
    
    boolean queueMode;
    String targetName;
    boolean durable;
    
    if (dest instanceof javax.jms.Queue) {
      queueMode = true;
      targetName = ((Destination) dest).getName();
      durable = false;
    } else {
      queueMode = false;
      if (subName == null) {
        targetName = nextSubName();
        durable = false;
      } else {
        targetName = subName;
        durable = true;
      }
      requestor.request(new ConsumerSubRequest(((Destination) dest).getName(),
          targetName, selector, false, durable, false));
    }
    
    MultiSessionConsumer msc =
      new MultiSessionConsumer(
          queueMode,
          durable,
          selector,
          targetName,
          sessionPool,
          factoryParameters.queueMessageReadMax,
          factoryParameters.topicActivationThreshold,
          factoryParameters.topicPassivationThreshold,
          factoryParameters.topicAckBufferMax,
          mtpx,
          this,
          maxMessages);
    
    msc.start();
    
    cconsumers.addElement(msc);
    
    return msc;
  }  

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public synchronized javax.jms.Session
      createSession(boolean transacted, 
                    int acknowledgeMode)
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 stringImage + ".createSession(" + transacted + ',' +  acknowledgeMode + ')');
    checkClosed();
    Session session = new Session(
      this,
      transacted, 
      acknowledgeMode, 
      mtpx);
    addSession(session);
    return session;
  }

  /**
   * Called here and by sub-classes.
   */
  protected synchronized void addSession(Session session) {
    sessions.addElement(session);
    if (status == Status.START) {
      session.start();
    }
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public synchronized void
      setExceptionListener(javax.jms.ExceptionListener listener) throws JMSException {
    checkClosed();
    mtpx.setExceptionListener(listener);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.ExceptionListener getExceptionListener() throws JMSException {
    checkClosed();
    return mtpx.getExceptionListener();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void setClientID(String clientID) throws JMSException {
    throw new IllegalStateException("ClientID is already set by the"
                                    + " provider.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public String getClientID() throws JMSException {
    checkClosed();
    return proxyId;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.ConnectionMetaData getMetaData() throws JMSException {
    checkClosed();
    if (metaData == null)
      metaData = new ConnectionMetaData();
    return metaData;
  }

  /**
   * API method for starting the connection.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public synchronized void start() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".start()"); 
    checkClosed();
    
    // Ignoring the call if the connection is started:
    if (status == Status.START)
      return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this
                                 + ": starting..."); 

    // Starting the sessions:

    for (int i = 0; i < sessions.size(); i++) {
      Session session = (Session) sessions.elementAt(i);
      session.start();
    }

    // Sending a start request to the server:
    mtpx.sendRequest(new CnxStartRequest());

    setStatus(Status.START);
  }

  /**
   * API method for stopping the connection; even if the connection appears
   * to be broken, stops the sessions.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public void stop() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".stop()");
    checkClosed();

    synchronized (this) {
      if (status == Status.STOP)
        return;
    }
    
    // At this point, the server won't deliver messages anymore,
    // the connection just waits for the sessions to have finished their
    // processings.
    // Must go out of the synchronized block in order to enable
    // the message listeners to use the connection.
    // As a csq, the connection stop is reentrant. Several 
    // threads can enter this method during the stopping stage.
    for (int i = 0; i < sessions.size(); i++) {
      Session session = (Session) sessions.get(i);
      session.stop();
    }
    
    synchronized (this) {
      if (status == Status.STOP)
        return;

      // Sending a synchronous "stop" request to the server:
      requestor.request(new CnxStopRequest());

      // Set the status as STOP as the following operations
      // (Session.stop) can't fail.
      setStatus(Status.STOP);
    }
  }

  /**
   * API method for closing the connection; even if the connection appears
   * to be broken, closes the sessions.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".close()");

    closer.close();
  }

  /**
   * This class synchronizes the close.
   * Close can't be synchronized with 'this' 
   * because the connection must be accessed
   * concurrently during its closure. So
   * we need a second lock.
   */
  class Closer {
    synchronized void close() {
      doClose();
    }
  }

  void doClose() {
    synchronized (this) {
      if (status == Status.CLOSE) {
        return;
      }
    }
      
    Vector sessionsToClose = (Vector)sessions.clone();
    sessions.clear();
    
    for (int i = 0; i < sessionsToClose.size(); i++) {
      Session session = 
        (Session) sessionsToClose.elementAt(i);
      try {
        session.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(
            BasicLevel.DEBUG, "", exc);
      }
    }
    
    Vector consumersToClose = (Vector)cconsumers.clone();
    cconsumers.clear();
    
    for (int i = 0; i < consumersToClose.size(); i++) {
      MultiSessionConsumer consumer = 
        (MultiSessionConsumer) consumersToClose.elementAt(i);
      try {
        consumer.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(
            BasicLevel.DEBUG, "", exc);
      }
    }

    
    try {
      CnxCloseRequest closeReq = new CnxCloseRequest();
      requestor.request(closeReq);
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(
          BasicLevel.DEBUG, "", exc);
    }
    
    mtpx.close();
    
    synchronized (this) {
      setStatus(Status.CLOSE);
    }
  }


  /**
   * Used by OutboundConnection in the connector layer.
   * When a connection is put back in a pool, 
   * it must be cleaned up.
   */
  public void cleanup() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage +".cleanup()");
    
    // Closing the sessions:
    // Session session;
    Vector sessionsToClose = (Vector)sessions.clone();
    sessions.clear();

    for (int i = 0; i < sessionsToClose.size(); i++) {
      Session session = 
        (Session) sessionsToClose.elementAt(i);
      try {
        session.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(
            BasicLevel.DEBUG, "", exc);
      }
    }
    
    mtpx.cleanup();
  }

  /** Returns a new session identifier. */
  String nextSessionId() {
    return sessionsC.nextValue();
  }
 
  /** Returns a new message identifier. */
  String nextMessageId() {
    return messagesC.nextValue();
  }

  /** Returns a new subscription name. */
  String nextSubName() {
    return subsC.nextValue();
  }

  /**
   * Called by Session.
   */
  synchronized void closeSession(Session session) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".closeSession(" + session + ')');
    sessions.removeElement(session);
  }

  /**
   * Called by MultiSessionConsumer.
   * Synchronized with run().
   */
  synchronized void closeConnectionConsumer(MultiSessionConsumer cc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".closeConnectionConsumer(" + cc + ')');
    cconsumers.removeElement(cc);
  }

  synchronized AbstractJmsReply syncRequest(
    AbstractJmsRequest request) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".syncRequest(" + request + ')');
    return requestor.request(request);
  }

  /**
   * Called by temporary destinations deletion.
   */
  synchronized void checkConsumers(String agentId) throws JMSException {
    for (int i = 0; i < sessions.size(); i++) {
      Session sess = (Session) sessions.elementAt(i);
      sess.checkConsumers(agentId);
    }
  }

  protected final RequestMultiplexer getRequestMultiplexer() {
    return mtpx;
  }
}
