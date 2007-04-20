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

  /** Sessions counter. */
  private int sessionsC = 0;

  /** Messages counter. */
  private int messagesC = 0;

  /** Subscriptions counter. */
  private int subsC = 0;

  /** Client's agent proxy identifier. */
  String proxyId;

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
    this.factoryParameters = factoryParameters;
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
    
    stringImage = "Cnx:" + proxyId + ':' + key;
    hashCode = stringImage.hashCode();

    mtpx.setDemultiplexerDaemonName(toString());
  }

  private final String newTrace(String trace) {
    return "Connection[" + proxyId + ':' + key + ']' + trace;
  }

  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 newTrace(".setStatus(" + Status.toString(status) + ')'));
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
   * Specializes this Object method; returns <code>true</code> if the
   * parameter is a <code>Connection</code> instance sharing the same
   * proxy identifier and connection key.
   */
  public boolean equals(Object obj) {
    return (obj instanceof Connection) && (hashCode() == obj.hashCode()) && toString().equals(obj.toString());
  }

  final long getTxPendingTimer() {
    return factoryParameters.txPendingTimer;
  }
  
  final boolean getAsyncSend() {
    return factoryParameters.asyncSend;
  }
  
  final int getQueueMessageReadMax() {
    return factoryParameters.queueMessageReadMax;
  }
  
  final int getTopicAckBufferMax() {
    return factoryParameters.topicAckBufferMax;
  }
  
  final int getTopicActivationThreshold() {
    return factoryParameters.topicActivationThreshold;
  }

  final int getTopicPassivationThreshold() {
    return factoryParameters.topicPassivationThreshold;
  }
  
  /**
   * Checks if the connecion is closed. If true
   * raises an IllegalStateException.
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
                 newTrace(".createConnectionConsumer(" + dest +
                          ',' +selector + ',' + sessionPool +
                          ',' + maxMessages + ')'));
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
                 newTrace(".createDurableConnectionConsumer(" + 
                          topic + ',' + subName + ',' + selector + ',' + 
                          sessPool + ',' + maxMessages + ')'));
    checkClosed();
    if (subName == null) 
      throw new JMSException("Invalid subscription name: " + subName);
    return createConnectionConsumer((Destination) topic, subName, selector, sessPool, maxMessages);
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
          targetName, selector, false, durable));
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
                 newTrace(".createSession(" + transacted + ',' +  acknowledgeMode + ')'));
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
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".start()")); 
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
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".stop()"));
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
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".close()"));

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
      logger.log(
        BasicLevel.DEBUG, newTrace(".cleanup()"));
    
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
  synchronized String nextSessionId() {
    if (sessionsC == Integer.MAX_VALUE)
      sessionsC = 0;
    sessionsC++;
    return "c" + key + "s" + sessionsC;
  }
 
  /** Returns a new message identifier. */
  synchronized String nextMessageId() {
    if (messagesC == Integer.MAX_VALUE)
      messagesC = 0;
    messagesC++;
    return "ID:" + proxyId.substring(1) + "c" + key + "m" + messagesC;
  }

  /** Returns a new subscription name. */
  synchronized String nextSubName() {
    if (subsC == Integer.MAX_VALUE)
      subsC = 0;
    subsC++;
    return "c"  + key + "sub" + subsC;
  }

  /**
   * Called by Session.
   */
  synchronized void closeSession(Session session) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".closeSession(" + session + ')'));
    sessions.removeElement(session);
  }

  /**
   * Called by MultiSessionConsumer.
   * Synchronized with run().
   */
  synchronized void closeConnectionConsumer(MultiSessionConsumer cc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 newTrace(".closeConnectionConsumer(" + cc + ')'));
    cconsumers.removeElement(cc);
  }

  synchronized AbstractJmsReply syncRequest(
    AbstractJmsRequest request) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 newTrace(".syncRequest(" + request + ')'));
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
