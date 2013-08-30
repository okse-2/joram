/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2013 ScalAgent Distributed Technologies
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
 *                 Abdenbi Benammour
 */
package org.objectweb.joram.client.jms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.TransactionRolledBackException;

import org.objectweb.joram.client.jms.connection.CompletionListener;
import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.client.jms.connection.Requestor;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CommitRequest;
import org.objectweb.joram.shared.client.ConsumerAckRequest;
import org.objectweb.joram.shared.client.ConsumerDenyRequest;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.client.ConsumerReceiveRequest;
import org.objectweb.joram.shared.client.ConsumerUnsubRequest;
import org.objectweb.joram.shared.client.GetAdminTopicReply;
import org.objectweb.joram.shared.client.GetAdminTopicRequest;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.client.SessAckRequest;
import org.objectweb.joram.shared.client.SessCreateDestReply;
import org.objectweb.joram.shared.client.SessCreateDestRequest;
import org.objectweb.joram.shared.client.SessDenyRequest;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Implements the <code>javax.jms.Session</code> interface.
 * <p>
 * A Session object is a single-threaded context for producing and consuming
 * messages. A session serves several purposes:
 * <ul>
 * <li>It is a factory for message producers and consumers.</li>
 * <li>It is a factory for Joram specific message.</li>
 * <li>It defines a serial order for the messages it consumes and the messages
 * it produces.</li>
 * <li>It retains messages it consumes until they have been acknowledged.</li>
 * <li>It serializes execution of message listeners registered with its message
 * consumers.</li>
 * <li>It is a factory for TemporaryTopics and TemporaryQueues.</li>
 * <li>It supports a single series of transactions that combine work spanning
 * its producers and consumers into atomic units.</li>
 * </ul>
 *  A session can create and service multiple message producers and consumers.
 *  The Session class defines the different acknowledge modes:
 * <ul>
 * <li>AUTO_ACKNOWLEDGE – With this acknowledgment mode, the session automatically
 * acknowledges a client's receipt of a message either when the session has successfully
 * returned from a call to receive or when the message listener the session has called
 * to process the message successfully returns.</li>
 * <li>CLIENT_ACKNOWLEDGE – With this acknowledgment mode, the client acknowledges a consumed
 * message by calling the message's acknowledge method.</li>
 * <li>DUPS_OK_ACKNOWLEDGE – This acknowledgment mode instructs the session to lazily acknowledge
 * the delivery of messages.</li>
 * <li>SESSION_TRANSACTED – This value is returned from the method getAcknowledgeMode if the
 * session is transacted.</li>
 * </ul>
 */
public class Session implements javax.jms.Session, SessionMBean {

  public static Logger logger = Debug.getLogger(Session.class.getName());
  public static Logger trace = Debug.getLogger(Session.class.getName() + ".Message");

  /**
   *  With this acknowledgment mode, the client acknowledges a consumed message by calling
   * the message's acknowledge method. Contrary to CLIENT_ACKNOWLEDGE mode this mode allows
   * to acknowledge only the specified message.
   */
  public static int INDIVIDUAL_ACKNOWLEDGE = 4;
  
  /**
   * Status of the session
   */
  private static class Status {
    /**
     * Status of the session when the connection is stopped.
     * This is the initial status.
     */
    public static final int STOP = 0;

    /**
     * Status of the session when the connection is started.
     */
    public static final int START = 1;

    /**
     * Status of the connection when it is closed.
     */
    public static final int CLOSE = 2;

    private static final String[] names = { "STOP", "START", "CLOSE" };

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The way the session is used.
   */
  private static class SessionMode {
    /**
     * The session is still not used.
     * This is the initial mode.
     */
    public static final int NONE = 0;

    /**
     * The session is used to synchronously receive messages.
     */
    public static final int RECEIVE = 1;

    /**
     * The session is used to asynchronously listen to messages.
     */
    public static final int LISTENER = 2;

    /**
     * The session is used by an application server.
     */
    public static final int APP_SERVER = 3;

    private static final String[] names = { "NONE", "RECEIVE", "LISTENER", "APP_SERVER" };

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The status of the current request.
   * Only valid in when the session is used to synchronously receive messages
   * (RECEIVE mode).
   */  
  private static class RequestStatus {
    /** No request. This is the initial status. */
    public static final int NONE = 0;
    /**  A request is running (pending). */
    public static final int RUN = 1;
    /**  The request is done. */
    public static final int DONE = 2;

    private static final String[] names = { "NONE", "RUN", "DONE" };

    public static String toString(int status) {
      return names[status];
    }
  }

  /** Task for closing the session if it becomes pending. */
  private SessionCloseTask closingTask;

  /** <code>true</code> if the session's transaction is scheduled. */
  private boolean scheduled;

  /** The message listener of the session, if any. */
  protected javax.jms.MessageListener messageListener;

  /** The identifier of the session. */
  private String ident;

  /** The connection the session belongs to. */
  private Connection cnx;

  /** <code>true</code> if the session is transacted. */
  boolean transacted;

  /** The acknowledgement mode of the session. */
  private int acknowledgeMode;

  /** <code>true</code> if the session's acknowledgements are automatic. */
  private boolean autoAck;

  /** Vector of message consumers. */
  private Vector consumers;

  /** Vector of message producers. */
  private Vector producers;

  /** Vector of queue browsers. */
  private Vector browsers;

  /** FIFO queue holding the asynchronous server deliveries. */
  private fr.dyade.aaa.common.Queue repliesIn;

  /** Daemon distributing asynchronous server deliveries. */
  private SessionDaemon daemon;

  boolean checkThread() {
    return (daemon != null && daemon.isCurrentThread());
  }

  /** Counter of message listeners. */
  private int listenerCount;

  /** 
   * Table holding the <code>ProducerMessages</code> holding producers'
   * messages and destinated to be sent at commit.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> <code>ProducerMessages</code>
   */
  Hashtable sendings;

  /** 
   * Table holding the identifiers of the messages delivered per
   * destination or subscription, and not acknowledged.
   * <p>
   * <b>Key:</b> destination or subscription name<br>
   * <b>Object:</b> <code>MessageAcks</code> instance
   */
  Hashtable deliveries;

  /**
   * The request multiplexer used to communicate with the user proxy.
   */
  private RequestMultiplexer mtpx;

  /**
   * The requestor used by the session to communicate with the user proxy.
   */
  private Requestor requestor;

  /**
   * The requestor used by the session to make 'receive' with the user
   * proxy. This second requestor is necessary because it must be closed
   * during the session close (see method close).
   */
  private Requestor receiveRequestor;

  /**
   * Indicates that the session has been recovered by a message listener.
   * Doesn't need to be volatile because it is only used by the SessionDaemon
   * thread.
   */
  private boolean recover;
  
  /**
   * Indicates that the session has been close by a message listener.
   * Doesn't need to be volatile because it is only used by the SessionDaemon
   * thread.
   */
  private boolean toClose;

  /**
   * Status of the session: STOP, START, CLOSE
   */
  private int status;

  /**
   * Mode of the session: NONE, RECEIVE, LISTENER, APP_SERVER
   */
  private int sessionMode;

  /**
   * Status of the request: NONE, RUN, DONE.
   */
  private int requestStatus;

  /**
   * The message consumer currently making a request (null if none).
   */
  private MessageConsumer pendingMessageConsumer;

  /**
   * The current active control thread.
   */
  private Thread singleThreadOfControl;
  
  /**
   * Used to synchronize the method close()
   */
  private Closer closer;

  /**
   *  Indicates whether the messages consumed are implicitly acknowledged
   * or not. When true messages are immediately removed from queue when
   * delivered.
   *  Contrary to Session's AUTO_ACKNOWLEDGE mode there is none acknowledge
   * message from client to server.
   * 
   * @see FactoryParameters#implicitAck
   */
  private boolean implicitAck;
  
  /** 
   *  Indicates whether the messages consumed are implicitly acknowledged
   * or not. If true messages are immediately removed from queue when
   * delivered.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   *
   * @return true if messages produced are implicitly acknowledged.
   */
  public boolean isImplicitAck() {
    return implicitAck;
  }

  /**
   *  Sets implicit acknowledge for this session.
   * <p>
   *  Determines whether the messages produced are implicitly acknowledged
   * or not. If set to true the messages are immediately removed from queue
   * when delivered.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   * 
   * @param implicitAck if true sets implicit acknowledge for this session.
   */
  public void setImplicitAck(boolean implicitAck) {
    this.implicitAck = implicitAck;
  }

  /**
   *  Indicates whether the messages produced are asynchronously sent
   * or not (without or with acknowledgment).
   * 
   * @see FactoryParameters#asyncSend
   */
  private boolean asyncSend;

  /** 
   *  Indicates whether the messages produced are asynchronously sent
   * or not (without or with acknowledgment).
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   *
   * @return true if messages produced are asynchronously sent.
   */
  public boolean isAsyncSend() {
    return asyncSend;
  }

  /**
   *  Sets asynchronously sending for this session.
   * <p>
   *  Determines whether the messages produced are asynchronously sent
   * or not (without or with acknowledgement).
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   * 
   * @param asyncSend	if true sets asynchronous sending for this session.
   * 
   * @see FactoryParameters.asyncSend
   */
  public void setAsyncSend(boolean asyncSend) {
    this.asyncSend = asyncSend;
  }

  /**
   *  Maximum number of messages that can be read at once from a queue.
   * <p>
   *  This attribute is inherited from Connection at initialization.
   *
   * @see FactoryParameters.queueMessageReadMax
   */
  private int queueMessageReadMax;

  /**
   *  Get the maximum number of messages that can be read at once from a queue
   * for this Session.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 1.
   * 
   * @return    The maximum number of messages that can be read at once from
   *            a queue.
   *
   * @see FactoryParameters.queueMessageReadMax
   */
  public final int getQueueMessageReadMax() {
    return queueMessageReadMax;
  }

  /**
   *  Set the maximum number of messages that can be read at once from a queue
   * for this Session.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 1.
   * 
   * @param queueMessageReadMax	The maximum number of messages that can be
   *				read at once from a queue.
   *
   * @see FactoryParameters.queueMessageReadMax
   */
  public void setQueueMessageReadMax(int queueMessageReadMax) {
    this.queueMessageReadMax = queueMessageReadMax;
  }

  /**
   *  Maximum number of acknowledgements that can be buffered when using
   * Session.DUPS_OK_ACKNOWLEDGE mode.
   * <p>
   *  This attribute is inherited from Connection at initialization.
   * 
   * @see FactoryParameters.topicAckBufferMax
   */
  private int topicAckBufferMax;

  /**
   *  Get the maximum number of acknowledgements that can be buffered when
   * using Session.DUPS_OK_ACKNOWLEDGE mode for this session.
   * <p>
   *  This attribute is inherited from Connection at initialization.
   *
   * @return The Maximum number of acknowledgements that can be buffered when
   *         using Session.DUPS_OK_ACKNOWLEDGE mode.
   *
   * @see FactoryParameters.topicAckBufferMax
   */
  public final int getTopicAckBufferMax() {
    return topicAckBufferMax;
  }

  /**
   * Set the maximum number of acknowledgements that can be buffered when
   * using Session.DUPS_OK_ACKNOWLEDGE mode for this session.
   * <p>
   *  This attribute is inherited from Connection at initialization.
   *
   * @param topicAckBufferMax The Maximum number of acknowledgements that
   *			      can be buffered in Session.DUPS_OK_ACKNOWLEDGE
   *			      mode.
   *
   * @see FactoryParameters.topicAckBufferMax
   */
  public void setTopicAckBufferMax(int topicAckBufferMax) {
    this.topicAckBufferMax = topicAckBufferMax;
  }

  /**
   *  This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is Integer.MAX_VALUE.
   *
   * @see FactoryParameters.topicPassivationThreshold
   */
  private int topicPassivationThreshold;

  /**
   * Get the threshold of passivation for this session.
   * <p>
   * This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is Integer.MAX_VALUE.
   *
   * @return The maximum messages number over which the subscription
   *         is passivated.
   *
   * @see FactoryParameters.topicPassivationThreshold
   */
  public final int getTopicPassivationThreshold() {
    return topicPassivationThreshold;
  }

  /**
   * Set the threshold of passivation for this session.
   * <p>
   * This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is Integer.MAX_VALUE.
   *
   * @param topicPassivationThreshold The maximum messages number over which
   *				      the subscription is passivated.
   *
   * @see FactoryParameters.topicPassivationThreshold
   */
  public void setTopicPassivationThreshold(int topicPassivationThreshold) {
    this.topicPassivationThreshold = topicPassivationThreshold;
  }

  /**
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 0.
   *
   * @see FactoryParameters.topicActivationThreshold
   */
  private int topicActivationThreshold;

  /**
   * Get the threshold of activation for this session.
   * <p>
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 0.
   *
   * @return The minimum messages number below which the subscription
   *         is activated.
   *
   * @see FactoryParameters.topicActivationThreshold
   */
  public final int getTopicActivationThreshold() {
    return topicActivationThreshold;
  }

  /**
   * Set the threshold of activation for this session.
   * <p>
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 0.
   *
   * @param topicActivationThreshold The minimum messages number below which
   *			   	     the subscription is activated.
   *
   * @see FactoryParameters.topicActivationThreshold
   */
  public void setTopicActivationThreshold(int topicActivationThreshold) {
    this.topicActivationThreshold = topicActivationThreshold;
  }
  
  /**
   * If a message body is upper than the <code>compressedMinSize</code>,
   * this message body is compressed.
   * <p>
   * This attribute is inherited from Connection at initialization
   * default value is 0 no compression
   *
   * @see FactoryParameters.compressedMinSize
   */
  private int compressedMinSize;
  
  /**
   * Get the compressedMinSize for this session.
   * <p>
   * The minimum message body size before a message body compression.
   * <p>
   * This attribute is inherited from Connection at initialization,
   * default value is 0 no compression
   *
   * @return The minimum size before a message body compression
   *
   * @see FactoryParameters.compressedMinSize
   */
  public final int getCompressedMinSize() {
    return compressedMinSize;
  }

  /**
   * Sets the minimum size beyond which the message body is compressed in this session.
   * This attribute is inherited from Connection at initialization, the default value is
   * 0 (no compression).
   *
   * @param compressedMinSize The minimum size before a message body compression.
   *
   * @see FactoryParameters.compressedMinSize
   */
  public final void setCompressedMinSize(int compressedMinSize) {
    this.compressedMinSize = compressedMinSize;
  }
  
  /**
   * the compression level (0-9)
   * <p>
   * This attribute is inherited from Connection at initialization
   * default value is Deflater.BEST_SPEED (1)
   * 
   * @see FactoryParameters.compressionLevel
   */
  private int compressionLevel;
  
  /**
   * Get the compression level for this session.
   * <p>
   *  This attribute is inherited from FactoryParameters, 
   *  default value is Deflater.BEST_SPEED (1).
   *
   * @return The compression level
   *
   * @see FactoryParameters.compressionLevel
   */
  public final int getCompressionLevel() {
    return compressionLevel;
  }

  /**
   * Set the compression level for this session.
   * <p>
   *  This attribute is inherited from FactoryParameters, 
   *  default value is Deflater.BEST_SPEED (1).
   *  This method can overload this attribute.
   *
   * @param The compression level
   *
   * @see FactoryParameters.compressionLevel
   */
  public final void setCompressionLevel(int compressionLevel) {
    this.compressionLevel = compressionLevel;
  }

  /**
   *  Indicates whether the subscription requests are asynchronously handled
   * or not.
   * <p>
   *  Default value is false, the subscription is handled synchronously so the
   * topic must be accessible.
   * 
   * @since JORAM 5.0.7
   */
  private boolean asyncSub = false;

  /** 
   *  Indicates whether the subscription request is asynchronously handled
   * or not.
   * <p>
   *  Default value is false, the subscription is handled synchronously so the
   * topic must be accessible.
   *
   * @return true if the subscription requests are asynchronously handled.
   * 
   * @since JORAM 5.0.7
   */
  public boolean isAsyncSub() {
    return asyncSub;
  }

  /** 
   * Sets asynchronous subscription for this session. 
   * <p>
   *  Determines whether the subscription request is asynchronously handled
   * or not.
   * <p>
   *  Default value is false, the subscription is handled synchronously so the
   * topic must be accessible.
   *
   * @param asyncSub if true sets  asynchronous subscription for this session.
   * 
   * @since JORAM 5.0.7
   */
  public void setAsyncSub(boolean asyncSub) {
    this.asyncSub = asyncSub;
  }

  private MessageConsumerListener messageConsumerListener;

  private List inInterceptors;
  private List outInterceptors;

//
//   * Sets the list of IN message interceptors.
//   * @param pInInterceptors
//
//  public void setInMessageInterceptors(List pInInterceptors) {
//    inInterceptors = pInInterceptors;
//  }
//
//
//   * Sets the OUT message interceptor.
//   * @param pOutInterceptor
//
//  public void setOutMessageInterceptors(List pOutInterceptors) {
//    outInterceptors = pOutInterceptors;
//  }

  /**
   * Returns the MBean name.
   * @return the MBean name.
   */
  public String getJMXBeanName() {
    StringBuffer buf = new StringBuffer();
    buf.append(cnx.getJMXBeanName());
    buf.append(",location=Session");
    buf.append(",session=").append(getClass().getSimpleName()).append("_").append(ident);
    return buf.toString();
  }

  public String registerMBean() {
    String JMXBeanName = getJMXBeanName();
    try {
      MXWrapper.registerMBean(this, JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Session.registerMBean: " + JMXBeanName, e);
    }

    return JMXBeanName;
  }

  public void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean(getJMXBeanName());
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Session.unregisterMBean: " + getJMXBeanName(), e);
    }
  }
  
  /**
   * Opens a session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client), 3 (dups ok), 4 (individual).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  Session(Connection cnx, boolean transacted, int acknowledgeMode, RequestMultiplexer mtpx)
      throws JMSException {
    if (!transacted
        && acknowledgeMode != javax.jms.Session.AUTO_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.DUPS_OK_ACKNOWLEDGE
        && acknowledgeMode != INDIVIDUAL_ACKNOWLEDGE
        && !(cnx instanceof XAQueueConnection)
        && !(cnx instanceof XATopicConnection)
        && !(cnx instanceof XAConnection))
      throw new JMSException("Can't create a non transacted session with an invalid acknowledge mode.");

    this.ident = cnx.nextSessionId();
    this.cnx = cnx;
    this.transacted = transacted;
    this.acknowledgeMode = acknowledgeMode;
    this.mtpx = mtpx;
    requestor = new Requestor(mtpx);
    receiveRequestor = new Requestor(mtpx);

    autoAck = !transacted && !(acknowledgeMode == javax.jms.Session.CLIENT_ACKNOWLEDGE || acknowledgeMode == INDIVIDUAL_ACKNOWLEDGE);
    
    consumers = new Vector();
    producers = new Vector();
    browsers = new Vector();
    repliesIn = new fr.dyade.aaa.common.Queue();
    sendings = new Hashtable();
    deliveries = new Hashtable();
    
    closer = new Closer();

    // If the session is transacted and the transactions limited by a timer,
    // a closing task might be useful.
    if (transacted && cnx.getTxPendingTimer() > 0) {
      closingTask = new SessionCloseTask(cnx.getTxPendingTimer() * 1000);
    }
    
    // Retrieves default parameters from connection. The user can configure
    // the session through get/set methods.
    implicitAck = cnx.getImplicitAck();
    asyncSend = cnx.getAsyncSend();
    queueMessageReadMax = cnx.getQueueMessageReadMax();
    topicAckBufferMax = cnx.getTopicAckBufferMax();
    topicActivationThreshold = cnx.getTopicActivationThreshold();
    topicPassivationThreshold = cnx.getTopicPassivationThreshold();
    compressedMinSize = cnx.getCompressedMinSize();
    compressionLevel = cnx.getCompressionLevel();

    setStatus(Status.STOP);
    setSessionMode(SessionMode.NONE);
    setRequestStatus(RequestStatus.NONE);

    //add interceptors...
    inInterceptors = cnx.getInInterceptors();
    outInterceptors = cnx.getOutInterceptors();
    
    registerMBean();
  }

  /**
   * Sets the status of the session.
   */
  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }

  public boolean isStarted() {
    return (status == Status.START);
  }

  public String getStatus() {
    return Status.toString(status);
  }
  
  /**
   * Sets the session mode.
   */
  private void setSessionMode(int sessionMode) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.setSessionMode(" + SessionMode.toString(sessionMode) + ')');
    this.sessionMode = sessionMode;
  }

  public String getSessionMode() {
    return SessionMode.toString(sessionMode);
  }

  /**
   * Sets the request status.
   */
  private void setRequestStatus(int requestStatus) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.setRequestStatus(" + RequestStatus.toString(requestStatus) + ')');
    this.requestStatus = requestStatus;
  }
  
  public String getRequestStatus() {
    return RequestStatus.toString(requestStatus);
  }
  
  /**
   * Checks if the session is closed. 
   * If true, an IllegalStateException is raised.
   */
  protected synchronized void checkClosed() throws IllegalStateException {
    if (status == Status.CLOSE)
      throw new IllegalStateException("Forbidden call on a closed session.");
  }

  /**
   * Checks if the calling thread is the thread of control. If not, 
   * an IllegalStateException is raised.
   */
  private synchronized void checkThreadOfControl() throws IllegalStateException {
    if (singleThreadOfControl != null && Thread.currentThread() != singleThreadOfControl)
      throw new IllegalStateException("Illegal control thread");
  }

  /**
   * Checks the session mode. If it is not the expected session mode,
   * raises an IllegalStateException.
   *
   * @param expectedSessionMode the expected session mode.
   */
  private void checkSessionMode(int expectedSessionMode) throws IllegalStateException {
    if (sessionMode == SessionMode.NONE) {
      setSessionMode(sessionMode);
    } else if (sessionMode != expectedSessionMode) {
      throw new IllegalStateException("Bad session mode");
    }
  }

  /** Returns a String image of this session. */
  public String toString() {
    return "Sess:" + ident;
  }

  /**
   * API method.
   * Returns the acknowledgement mode of the session. The acknowledgement mode is
   * set at the time that the session is created. If the session is transacted, the
   * acknowledgement mode is ignored.
   * 
   * @return If the session is not transacted, returns the current acknowledgement mode
   * for the session. If the session is transacted, returns Session.SESSION_TRANSACTED.
   * 
   * @exception JMSException  Actually never thrown.
   */
  public final int getAcknowledgeMode() throws JMSException {
    checkClosed();
    if (transacted)
      return Session.SESSION_TRANSACTED;
    return acknowledgeMode;
  }

  /**
   * API method.
   * Indicates whether the session is in transacted mode.
   * 
   * @return true if  the session is in transacted mode.
   * 
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized final boolean getTransacted() throws JMSException {
    checkClosed();
    return transacted;
  }

  /**
   * set transacted.
   * see connector ManagedConnectionImpl (Connector).
   */
  public void setTransacted(boolean t) {
    if (status != Status.CLOSE) {
      transacted = t;
//      if (!t) {
//        autoAck = true;
//      }
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.setTransacted transacted = " + transacted + ", autoAck = "
          + autoAck);
    // else should throw an exception but not expected in
    // the connector.
  }

  /**
   * API method.
   * Sets the session's distinguished message listener, this is an expert facility not used
   * by regular JMS clients.
   * <p>
   * When the distinguished message listener is set, no other form of message receipt in the
   * session can be used; however, all forms of sending messages are still supported.
   * 
   * @param listener the message listener to associate with this session.

   * @exception JMSException  Actually never thrown.
   */
  public synchronized void setMessageListener(javax.jms.MessageListener listener) throws JMSException {
    checkSessionMode(SessionMode.APP_SERVER);
    this.messageListener = listener;
  }

  /**
   * API method.
   * Returns the session's distinguished message listener, this is an expert facility not
   * used by regular JMS clients.
   * 
   * @return the message listener associated with this session
   * 
   * @exception JMSException  Actually never thrown.
   */
  public synchronized javax.jms.MessageListener getMessageListener() throws JMSException {
    return messageListener;
  }

  /**
   * API method.
   * Creates a Message object.
   *
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see Message
   */
  public synchronized javax.jms.Message createMessage() throws JMSException {
    checkClosed();
    Message m = new Message();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates a TextMessage  object, a TextMessage object is used to send a message
   * containing a String object.
   * 
   * @return a newly created TextMessage object.
   *
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see TextMessage
   */
  public synchronized javax.jms.TextMessage createTextMessage() throws JMSException {
    checkClosed();
    TextMessage m = new TextMessage();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates a TextMessage  object, a TextMessage object is used to send a message
   * containing a String object.
   * 
   * @param text  the string to use to initialize this message.
   * @return a newly created TextMessage object.
   * 
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see TextMessage
   */
  public synchronized javax.jms.TextMessage createTextMessage(String text) throws JMSException {
    checkClosed();
    TextMessage message = new TextMessage();
    message.setCompressedMinSize(compressedMinSize);
    message.setCompressionLevel(compressionLevel);
    message.setText(text);
    return message;
  }
  
  /**
   * API method.
   * Creates a <code>BytesMessage</code> object, a BytesMessage object could be used to send
   * a message containing a stream of uninterpreted bytes.
   *
   * @return a newly created ByteMessage object.
   * 
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see BytesMessage
   */
  public synchronized javax.jms.BytesMessage createBytesMessage() throws JMSException {
    checkClosed();
    BytesMessage m = new BytesMessage();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates a <code>MapMessage</code> object,  a MapMessage object is used to send a set
   * of name-value pairs, where names are String objects and values are primitive values.
   * 
   * @return a newly created MapMessage object.
   * 
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see MapMessage
   */
  public synchronized javax.jms.MapMessage createMapMessage() throws JMSException {
    checkClosed();
    MapMessage m = new MapMessage();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates an ObjectMessage object, an ObjectMessage object is used to send a message that
   * contains a serializable Java object.
   * 
   * @return a newly created ObjectMessage object.
   * 
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see ObjectMessage
   */
  public synchronized javax.jms.ObjectMessage createObjectMessage() throws JMSException {
    checkClosed();
    ObjectMessage m = new ObjectMessage();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates an ObjectMessage object, an ObjectMessage object is used to send a message that
   * contains a serializable Java object.
   * 
   * @param object  the object to use to initialize this message.
   * @return a newly created ObjectMessage object.
   *
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see ObjectMessage
   */
  public synchronized javax.jms.ObjectMessage createObjectMessage(java.io.Serializable object)
      throws JMSException {
    checkClosed();
    ObjectMessage message = new ObjectMessage();
    message.setCompressedMinSize(compressedMinSize);
    message.setCompressionLevel(compressionLevel);
    message.setObject(object);
    return message;
  }

  /**
   * API method.
   * Creates a StreamMessage  object,  a StreamMessage object is used to send a self-defining
   * stream of primitive values.
   * 
   * @return a newly created StreamMessage object.
   *
   * @exception IllegalStateException  If the session is closed.
   * 
   * @see StreamMessage
   */
  public synchronized javax.jms.StreamMessage createStreamMessage() throws JMSException {
    checkClosed();
    StreamMessage m = new StreamMessage();
    m.setCompressedMinSize(compressedMinSize);
    m.setCompressionLevel(compressionLevel);
    return m;
  }

  /**
   * API method.
   * Creates a QueueBrowser object to peek at the messages on the specified queue using a message selector.
   * 
   * @param queue     the queue to browse
   * @param selector  the expression allowing to filter messages 
   * @return a newly created QueueBrowser object.
   * 
   * @exception IllegalStateException       if the session is closed.
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception InvalidSelectorException    if the message selector is invalid.
   */
  public synchronized javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue, String selector)
      throws JMSException {
    checkClosed();
    checkThreadOfControl();
    QueueBrowser qb = new QueueBrowser(this, (Queue) queue, selector);
    browsers.addElement(qb);
    return qb;
  }

  /**
   * API method.
   * Creates a QueueBrowser object to peek at the messages on the specified queue.
   * 
   *
   * @param queue     the queue to browse
   * @return a newly created QueueBrowser object.
   * 
   * @exception IllegalStateException       if the session is closed.
   * @exception InvalidDestinationException if an invalid destination is specified.
   */
  public synchronized javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue) throws JMSException {
    checkClosed();
    checkThreadOfControl();
    QueueBrowser qb = new QueueBrowser(this, (Queue) queue, null);
    browsers.addElement(qb);
    return qb;
  }

  /**
   * API method.
   * Creates a MessageProducer to send messages to the specified destination. A client uses a
   * MessageProducer object to send messages to a destination.
   *
   * @param dest  the Destination to send to, or null if this is a producer which does not have
   *              a specified destination.
   * @return 
   *              
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageProducer createProducer(javax.jms.Destination dest)
      throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageProducer mp = new MessageProducer(this, (Destination) dest);
    addProducer(mp);
    return mp;
  }
  
  /**
   * API method.
   * Creates a MessageConsumer for the specified destination using a message selector. A client
   * uses a MessageConsumer object to receive messages that have been sent to a destination.
   * <p>
   * In some cases, a connection may both publish and subscribe to a topic. The consumer NoLocal
   * attribute allows a consumer to inhibit the delivery of messages published by its own connection.
   * The default value for this attribute is False. The noLocal value is only supported by destinations
   * that are topics.
   * 
   * @param dest      the Destination to access.
   * @param selector  The selector allowing to filter messages.
   * @param noLocal   if true, and the destination is a topic, inhibits the delivery of messages
   *                  published by its own connection.
   * @return the created MessageConsumer object.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest, String selector,
      boolean noLocal) throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(this, (Destination) dest, selector, null, noLocal);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   * Creates a MessageConsumer for the specified destination using a message selector. A client
   * uses a MessageConsumer object to receive messages that have been sent to a destination.
   *
   * @param dest      the Destination to access.
   * @param selector  The selector allowing to filter messages.
   * @return the created MessageConsumer object.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest, String selector)
      throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(this, (Destination) dest, selector);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   * Creates a MessageConsumer for the specified destination. A client uses a MessageConsumer
   * object to receive messages that have been sent to a destination.
   * 
   * @param dest the Destination to access.
   * @return the created MessageConsumer object.
   * 
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
      throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(this, (Destination) dest, null);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   * Creates a durable subscriber to the specified topic, using a message selector and
   * specifying whether messages published by its own connection should be delivered to it.
   * <p>
   * If a client needs to receive all the messages published on a topic, including the ones
   * published while the subscriber is inactive, it needs to use a durable TopicSubscriber.
   * Joram retains a record of durable subscriptions and insures that all messages from the
   * topic's publishers are retained until they are acknowledged by this durable subscriber
   * or they have expired.
   * <p>
   * A client can change an existing durable subscription by creating a durable TopicSubscriber
   * with the same name and a new topic and/or message selector. Changing a durable subscriber
   * is equivalent to unsubscribing (deleting) the old one and creating a new one.
   * 
   * @param topic     the non-temporary Topic to subscribe to.
   * @param name      the name used to identify this subscription.
   * @param selector  The selector allowing to filter messages. A value of null or an empty string
   *                  indicates that there is no message selector for the message consumer.
   * @param noLocal   if true, inhibits the delivery of messages published by its own connection.
   * @return the created TopicSubscriber object.
   * 
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.TopicSubscriber createDurableSubscriber(javax.jms.Topic topic, String name,
      String selector, boolean noLocal) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.createDurableSubscriber(" + topic + ',' + name + ',' + selector
          + ',' + noLocal + ')');
    checkClosed();
    checkThreadOfControl();
    TopicSubscriber ts = new TopicSubscriber(this, (Topic) topic, name, selector, noLocal);
    addConsumer(ts);
    return ts;
  }

  /**
   * API method.
   * Creates a durable subscriber to the specified topic.
   * <p>
   * If a client needs to receive all the messages published on a topic, including the ones
   * published while the subscriber is inactive, it needs to use a durable TopicSubscriber.
   * Joram retains a record of durable subscriptions and insures that all messages from the
   * topic's publishers are retained until they are acknowledged by this durable subscriber
   * or they have expired.
   * <p>
   * A client can change an existing durable subscription by creating a durable TopicSubscriber
   * with the same name and a new topic and/or message selector. Changing a durable subscriber
   * is equivalent to unsubscribing (deleting) the old one and creating a new one.
   * 
   * @param topic     the non-temporary Topic to subscribe to.
   * @param name      the name used to identify this subscription.
   * @return the created TopicSubscriber object.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.TopicSubscriber createDurableSubscriber(javax.jms.Topic topic, String name)
      throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.createDurableSubscriber(" + topic + ',' + name + ')');
    checkClosed();
    checkThreadOfControl();
    TopicSubscriber ts = new TopicSubscriber(this, (Topic) topic, name, null, false);
    addConsumer(ts);
    return ts;
  }

  /**
   * This method allows to create or retrieve a Queue with the given name
   * on the local server. First a destination with the specified name is searched
   * on the server, if it does not exist it is created. In any case a queue
   * identity with its Joram specific address is returned.
   * <p>
   * If the given name is a provider-specific name ("#x.y.z" unique identifier)
   * a queue identity is returned with the specified identifier.
   * <p>
   * API method.
   * <p>
   * Clients that depend on this ability are not portable. Normally the physical
   * creation of destination is an administrative task and is not to be initiated
   * by the JMS API.
   *
   * @param name  the name of this queue.
   * @return a queue with the given name.
   * 
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the topic creation failed.
   * 
   * @see Queue
   */
  public synchronized javax.jms.Queue createQueue(String name) throws JMSException {
    checkClosed();
    checkThreadOfControl();
    
    try {
      Destination.checkId(name);
    } catch (InvalidDestinationException exc) {
      String id = createDestination(DestinationConstants.getQueueType(), name);
      Queue queue = new Queue(id);
      queue.adminName = name;
      return queue;
    }
    return new Queue(name);
  }

  /**
   * This method allows to create or retrieve a Topic with the given name
   * on the local server. First a destination with the specified name is searched
   * on the server, if it does not exist it is created. In any case a topic
   * identity with its provider-specific address is returned.
   * <p>
   * If the given name is a Joram specific name ("#x.y.z" unique identifier)
   * a topic identity is returned with the specified identifier.
   * <p>
   * API method.
   * <p>
   * Clients that depend on this ability are not portable. Normally the physical
   * creation of destination is an administrative task and is not to be initiated
   * by the JMS API.
   * 
   * @param name  the name of this topic.
   * @return a topic with the given name.
   * 
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the topic creation failed.
   * 
   * @see Topic
   */
  public synchronized javax.jms.Topic createTopic(String name) throws JMSException {
    checkClosed();
    checkThreadOfControl();

    // Checks if the topic to retrieve is the administration topic:
    if (name.equals("#AdminTopic")) {
      try {
        GetAdminTopicReply reply = (GetAdminTopicReply) requestor.request(new GetAdminTopicRequest());
        if (reply.getId() != null)
          return new Topic(reply.getId());
        
        throw new JMSException("AdminTopic could not be retrieved.");
      } catch (JMSException exc) {
        throw exc;
      } catch (Exception exc) {
        throw new JMSException("AdminTopic could not be retrieved: " + exc);
      }
    }

    try {
      Destination.checkId(name);
    } catch (InvalidDestinationException exc) {
      String id = createDestination(DestinationConstants.getTopicType(), name);
      Topic topic = new Topic(id);
      topic.adminName = name;
      return topic;
    }
    return new Topic(name);
  }

  /**
   * Create a destination with the given name and type.
   * If a destination of a corresponding name and type exists it is returned.
   * 
   * @param type  the type of the destination to create.
   * @param name  the name of the destination to create.
   * @return  the unique identifier of the created destination.
   * 
   * @throws JMSException
   */
  private String createDestination(byte type, String name) throws JMSException {
    SessCreateDestReply reply = (SessCreateDestReply) requestor.request(new SessCreateDestRequest(type, name));
    return reply.getAgentId();
  }
  
  /**
   * API method.
   * Creates a TemporaryQueue object. Its lifetime will be that of the Connection
   * unless it is deleted earlier.
   * 
   * @return a temporary queue identity.
   * 
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   * 
   * @see TemporaryQueue
   */
  public synchronized javax.jms.TemporaryQueue createTemporaryQueue() throws JMSException {
    checkClosed();
    checkThreadOfControl();

    SessCreateDestReply reply = (SessCreateDestReply) requestor.request(new SessCreateDestRequest(DestinationConstants.getTemporaryQueueType()));
    String tempDest = reply.getAgentId();
    return new TemporaryQueue(tempDest, cnx);
  }

  /**
   * API method.
   * Creates a TemporaryTopic object. Its lifetime will be that of the Connection
   * unless it is deleted earlier.
   * 
   * @return a temporary topic identity.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   * 
   * @see TemporaryTopic
   */
  public synchronized javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException {
    checkClosed();
    checkThreadOfControl();

    SessCreateDestReply reply = (SessCreateDestReply) requestor.request(new SessCreateDestRequest(DestinationConstants.getTemporaryTopicType()));
    String tempDest = reply.getAgentId();
    return new TemporaryTopic(tempDest, cnx);
  }

  /** API method. */
  public synchronized void run() {
    int load = repliesIn.size();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "-- " + this + ": loaded with " + load + " message(s) and started.");

    try {
      // Processing the current number of messages in the queue:
      for (int i = 0; i < load; i++) {
        org.objectweb.joram.shared.messages.Message momMsg = (org.objectweb.joram.shared.messages.Message) repliesIn.pop();
        onMessage(momMsg, messageConsumerListener);
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
    }
  }
  
  /**
   * Called by MultiSessionConsumer
   * ASF mode
   */
  void setMessageConsumerListener(MessageConsumerListener mcl) {
    messageConsumerListener = mcl;
  }
      
  /**
   * API method.
   * Commits all messages done in this transaction and releases any locks currently held.
   *
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted, or if the connection is broken.
   */
  public synchronized void commit() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.commit()");

    checkClosed();
    checkThreadOfControl();
    
    if (cnx.checkCLSession(this))
      throw new IllegalStateException("Cannot commit session");

    if (!transacted)
      throw new IllegalStateException("Can't commit a non transacted session.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": committing...");

    // If the transaction was scheduled: canceling.
    if (scheduled) {
      closingTask.cancel();
      scheduled = false;
    }

    // Sending client messages:
    try {
      CommitRequest commitReq = new CommitRequest();

      Enumeration producerMessages = sendings.elements();
      while (producerMessages.hasMoreElements()) {
        ProducerMessages pM = (ProducerMessages) producerMessages.nextElement();
        commitReq.addProducerMessages(pM);
      }
      sendings.clear();
      
      // Acknowledging the received messages:
      Enumeration targets = deliveries.keys();
      while (targets.hasMoreElements()) {
        String target = (String) targets.nextElement();
        MessageAcks acks = (MessageAcks) deliveries.get(target);
        commitReq.addAckRequest(new SessAckRequest(target, acks.getIds(), acks.getQueueMode()));
      }
      deliveries.clear();
      
      if (asyncSend) {
        // Asynchronous sending
        commitReq.setAsyncSend(true);
        mtpx.sendRequest(commitReq);
      } else {
        requestor.request(commitReq);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ": committed.");
    }
    // Catching an exception if the sendings or acknowledgement went wrong:
    catch (JMSException jE) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", jE);
      TransactionRolledBackException tE = new TransactionRolledBackException(
          "A JMSException was thrown during the commit.");
      tE.setLinkedException(jE);

      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Exception: " + tE);

      rollback();
      throw tE;
    }
  }

  /**
   * API method.
   * Rolls back any messages done in this transaction and releases any locks currently held.
   * 
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted.
   */
  public synchronized void rollback() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.rollback()");

    checkClosed();
    checkThreadOfControl();
    
    if (cnx.checkCLSession(this))
      throw new IllegalStateException("Cannot rollback session");

    if (!transacted)
      throw new IllegalStateException("Can't rollback a non transacted" + " session.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": rolling back...");

    // If the transaction was scheduled: canceling.
    if (scheduled) {
      closingTask.cancel();
      scheduled = false;
    }

    // Denying the received messages:
    deny();
    // Deleting the produced messages:
    sendings.clear();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": rolled back.");
  }

  /** 
   * API method.
   * Stops message delivery in this session, and restarts message delivery with the
   * oldest unacknowledged message.
   * 
   * @exception IllegalStateException  If the session is closed, or transacted.
   */
  public synchronized void recover() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.recover()");

    checkClosed();
    checkThreadOfControl();

    if (transacted)
      throw new IllegalStateException("Can't recover a transacted session.");
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " recovering...");

    if (daemon != null && daemon.isCurrentThread()) {
      recover = true;
    } else {
      doRecover();
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": recovered.");
  }
  
  private void doRecover() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.doRecover()");
    deny();
  }

  /**
   * API method.
   * Unsubscribes a durable subscription that has been created by a client, this method
   * deletes the state being maintained on behalf of the subscriber by the Joram server.
   * <p>
   * It is erroneous for a client to delete a durable subscription while there is an active
   * MessageConsumer for the subscription, or while a consumed message is part of a pending
   * transaction or has not been acknowledged in the session.
   * 
   * @param name the name used to identify this subscription.
   * 
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception InvalidDestinationException  If the subscription does not 
   *              exist.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void unsubscribe(String name) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.unsubscribe(" + name + ')');

    if (name == null)
      throw new JMSException("Bad subscription name: null");

    checkClosed();
    checkThreadOfControl();
    
    MessageConsumer cons;
    if (consumers != null) {
      for (int i = 0; i < consumers.size(); i++) {
        cons = (MessageConsumer) consumers.get(i);
        if (!cons.queueMode && cons.targetName.equals(name))
          throw new JMSException("Can't delete durable subscription " + name
              + " as long as an active subscriber exists.");
      }
    }
    syncRequest(new ConsumerUnsubRequest(name));
  }
  
  /**
   * API method. Closes the session.
   * <p>
   * In order to free significant resources allocated on behalf of a session, clients should close
   * sessions when they are not needed. Closing a session automatically close all related producers,
   * and consumers and causes all temporary destinations to be deleted. 
   * <p>
   * This call will block until a receive call or message listener in progress has completed. A blocked
   * message consumer receive call returns null when this session is closed. Closing a transacted session
   * must roll back the transaction in progress.
   * <p>
   * This method is the only Session method that can be called concurrently.
   * <p>
   * Invoking any other Session method on a closed session must throw a JMSException.IllegalStateException.
   * Closing a closed session must not throw an exception.
   *
   * @exception JMSException if the JMS provider fails to close the session due to some internal error.
   */
  public void close() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.close()");
    
    if (cnx.checkCLSession(this))
      throw new IllegalStateException("Cannot close session");

    if (daemon != null && daemon.isCurrentThread())
      toClose = true;
    else
      closer.close();
    unregisterMBean();
  }

  /**
   * This class synchronizes the close.
   * Close can't be synchronized with 'this' because the Session must be
   * accessed concurrently during its closure. So we need a second lock.
   */
  class Closer {
    synchronized void close() throws JMSException {
      doClose();
    }
  }

  void doClose() throws JMSException {
    synchronized (this) {
      if (status == Status.CLOSE)
        return;
    }
    
    // Don't synchronize the consumer closure because
    // it could deadlock with message listeners or
    // client threads still using the session.

    Vector consumersToClose = (Vector) consumers.clone();
    consumers.clear();
    for (int i = 0; i < consumersToClose.size(); i++) {
      MessageConsumer mc = (MessageConsumer) consumersToClose.elementAt(i);
      try {
        mc.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }

    Vector browsersToClose = (Vector) browsers.clone();
    browsers.clear();
    for (int i = 0; i < browsersToClose.size(); i++) {
      QueueBrowser qb = (QueueBrowser) browsersToClose.elementAt(i);
      try {
        qb.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }

    Vector producersToClose = (Vector) producers.clone();
    producers.clear();
    for (int i = 0; i < producersToClose.size(); i++) {
      MessageProducer mp = (MessageProducer) producersToClose.elementAt(i);
      try {
        mp.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
    
    // This is now in removeMessageListener
    // called by MessageConsumer.close()
    // (see above)
//     try {
//       repliesIn.stop();
//     } catch (InterruptedException iE) {}
      
    stop();

    // The requestor must be closed because
    // it could be used by a concurrent receive
    // as it is not synchronized (see receive()).
    receiveRequestor.close();
      
    // Denying the non acknowledged messages:
    if (transacted) {
      rollback();
    } else {
      deny();
    }

    cnx.closeSession(this);
      
    synchronized (this) {
      setStatus(Status.CLOSE);
    }
  }

  /**
   * Starts the asynchronous deliveries in the session.
   * <p>
   * This method is called by a started connection.
   */
  synchronized void start() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.start()");

    if (status == Status.CLOSE)
      return;
    if (status == Status.START)
      return;
    if (listenerCount > 0) {
      doStart();
    }

    setStatus(Status.START);
  }

  private void doStart() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.doStart()");
    repliesIn.start();
    daemon = new SessionDaemon();
    daemon.setDaemon(false);
    daemon.start();
    singleThreadOfControl = daemon.getThread();
  }

  /**
   * Stops the asynchronous deliveries processing in the session.
   * <p>
   * This method must be carefully used. When the session is stopped, the
   * connection might very well going on pushing deliveries in the
   * session's queue. If the session is never re-started, these deliveries
   * will never be popped out, and this may lead to a situation of consumed
   * but never acknowledged messages.
   * <p>
   * This fatal situation never occurs as the <code>stop()</code> method is
   * either called by he <code>Session.close()</code>
   * and <code>Connection.stop()</code> methods, which first empties the
   * session's deliveries and forbid any further push.
   */
  synchronized void stop() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.stop()");

    if (status == Status.STOP || status == Status.CLOSE)
      return;

    // DF: According to JMS 1.1 java doc
    // the method stop "blocks until receives in progress have completed." 
    // But the JMS 1.1 specification doesn't mention this point. 
    // So we don't implement it: a stop doesn't block until 
    // receives have completed.
    
    // TODO: Verify the JMS 2.0 specification.

//     while (requestStatus != RequestStatus.NONE) {
//       try {
//         wait();
//       } catch (InterruptedException exc) {}
//     }

    doStop();
    
    setStatus(Status.STOP);
  }

  private void doStop() {
    if (daemon != null) {
      daemon.stop();
      daemon = null;
      singleThreadOfControl = null;
    }
  }

  /** 
   * Method called by message producers when producing a message for
   * preparing the session to later commit it.
   *
   * @param dest  The destination the message is destinated to.
   * @param msg  The message.
   */
  private void prepareSend(Destination dest, org.objectweb.joram.shared.messages.Message msg)
      throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.prepareSend(" + dest + ',' + msg + ')');

    checkClosed();
    checkThreadOfControl();
    
    // If the transaction was scheduled, cancelling:
    if (scheduled)
      closingTask.cancel();

    ProducerMessages pM = (ProducerMessages) sendings.get(dest.getName());
    if (pM == null) {
      pM = new ProducerMessages(dest.getName());
      sendings.put(dest.getName(), pM);
    }
    pM.addMessage(msg);

    // If the transaction was scheduled, re-scheduling it:
    if (scheduled)
      closingTask.start();
  }

  /** 
   * Method called by message consumers when receiving a message for
   * preparing the session to later acknowledge or deny it.
   *
   * @param name  Name of the destination or of the proxy subscription 
   *          the message comes from.
   * @param id  Identifier of the consumed message.
   * @param queueMode  <code>true</code> if the message consumed comes from
   *          a queue.
   */
  private void prepareAck(String name, String id, boolean queueMode) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.prepareAck(" + name + ',' + id + ',' + queueMode + ')');

    // If the transaction was scheduled, cancelling:
    if (scheduled)
      closingTask.cancel();

    MessageAcks acks = (MessageAcks) deliveries.get(name);
    if (acks == null) {
      acks = new MessageAcks(queueMode);
      deliveries.put(name, acks);
    }
    acks.addId(id);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> acks = " + acks);

    // If the transaction must be scheduled, scheduling it:
    if (closingTask != null) {
      scheduled = true;
      closingTask.start();
    }
  }

  /**
   * Method acknowledging the received messages.
   * Called by Message.
   */
  synchronized void acknowledge() throws JMSException {
    checkClosed();
    if (transacted || acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE) {
      return;
    }
    doAcknowledge();
  }

  /**
   * Method acknowledging the received messages.
   */
  private void doAcknowledge() throws JMSException {
    Enumeration targets = deliveries.keys();
    while (targets.hasMoreElements()) {
      String target = (String) targets.nextElement();
      MessageAcks acks = (MessageAcks) deliveries.remove(target);
      mtpx.sendRequest(new SessAckRequest(target, acks.getIds(), acks.getQueueMode()));
    }
  }

  /**
   * Method acknowledging one received message.
   * Called by Message.
   */
  synchronized void acknowledge(Destination dest, String msgId) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.acknowledge(" + dest + ", " + msgId + ')');
    checkClosed();
    if (acknowledgeMode == INDIVIDUAL_ACKNOWLEDGE) {
      Enumeration targets = deliveries.keys();
      while (targets.hasMoreElements()) {
        String target = (String) targets.nextElement();
        if (target.equals(dest.getAdminName()) || target.equals(dest.getName())) {
          MessageAcks acks = (MessageAcks) deliveries.get(target);
          acks.remove(msgId);
          Vector ackToSend = new Vector();
          ackToSend.add(msgId);
          mtpx.sendRequest(new SessAckRequest(dest.getName(), ackToSend, dest.isQueue()));
          return;
        }
      }
    }
  }

  /** 
   * Method denying the received messages.
   *
   * Called from:
   * - rollback -> synchronized client thread
   * - recover -> synchronized client thread
   * - close -> synchronized client thread 
   * - onMessage -> not synchronized session daemon.
   * It is the only thread that can run into the session
   * (session mode = LISTENER) except for the method close that
   * can be called concurrently. But close() first stops the session
   * daemon and then calls deny().
   *
   * The hashtable deliveries is also accessed from:
   * - acknowledge -> synchronized client thread
   * - commit -> synchronized client thread
   * - receive -> synchronized client thread.
   * - onMessage -> not synchronized session daemon (see above).
   */
  private void deny() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.deny()");
    Enumeration targets = deliveries.keys();
    while (targets.hasMoreElements()) {
      String target = (String) targets.nextElement();
      MessageAcks acks = (MessageAcks) deliveries.remove(target);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> acks = " + acks + ')');
      SessDenyRequest deny = new SessDenyRequest(target, acks.getIds(), acks.getQueueMode());
      deny.setRedelivered(true);
      if (acks.getQueueMode()) {
        requestor.request(deny);
      } else {
        mtpx.sendRequest(deny);
      }
    }
  }

  /**
   * Called by MessageConsumer.
   * This method is not synchronized because it can be concurrently called
   * by close() and Connection.stop().
   */
  javax.jms.Message receive(long requestTimeToLive, long waitTimeOut, MessageConsumer mc, String targetName,
      String selector, boolean queueMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.receive(" + requestTimeToLive + ',' + waitTimeOut + ','
          + targetName + ',' + selector + ',' + queueMode + ')');
    preReceive(mc);
    try {
      ConsumerMessages reply = null;
      ConsumerReceiveRequest request = new ConsumerReceiveRequest(targetName, selector, requestTimeToLive, queueMode);
      if (implicitAck)
        request.setReceiveAck(true);
      reply = (ConsumerMessages) receiveRequestor.request(request, waitTimeOut, null);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> reply = " + reply);
        
      synchronized (this) {
        // The session may have been 
        // closed in between.
        if (status == Status.CLOSE) {
          if (reply != null) {
            mtpx.deny(reply);
          }
          return null;
        }
        
        if (reply != null) {
          Vector msgs = reply.getMessages();
          if (msgs != null && !msgs.isEmpty()) {
            Message msg = Message.wrapMomMessage(this, (org.objectweb.joram.shared.messages.Message) msgs.get(0));
            String msgId = msg.getJMSMessageID();
            
            // Auto ack: acknowledging the message:
            if (autoAck && !implicitAck) {
              ConsumerAckRequest req = new ConsumerAckRequest(targetName, queueMode);
              req.addId(msgId);
              mtpx.sendRequest(req);
            } else {
              prepareAck(targetName, msgId, queueMode);
            }
            msg.session = this;
            if (trace.isLoggable(BasicLevel.INFO))
              trace.log(BasicLevel.INFO,
                         this + " handling message=" + msg + ", from=" + mc.dest.getAdminName() + '/' + mc.targetName);
            // Executes IN interceptors
            if ((inInterceptors != null) && (!inInterceptors.isEmpty())) {
              for (Iterator it = inInterceptors.iterator(); it.hasNext();) {
                MessageInterceptor interceptor = (MessageInterceptor) it.next();
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG,
                             "Intercepting the message after receiving by " + interceptor.getClass().getName());

                try {
                  interceptor.handle(msg, this);
                } catch (Throwable t) {
                  if (logger.isLoggable(BasicLevel.WARN))
                    logger.log(BasicLevel.WARN, "Error during interception (continue anyway...)", t);
                }
              }
            }
            return msg;
          }
          
          return null;
        }
        
        return null;
      }
    } finally {
      postReceive();
    }
  }

  /**
   * First stage before calling the proxy and waiting
   * for the reply. It is synchronized because it
   * locks the session in order to prevent any other
   * thread to make another operation.
   */
  private synchronized void preReceive(MessageConsumer mc) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.preReceive(" + mc + ')');
    // The message consumer may have been closed
    // after the first check (in MessageConsumer.receive())
    // and before preReceive.
    mc.checkClosed();

    checkClosed();
    checkThreadOfControl();
    
    // Don't call checkSessionMode because
    // we also check that the session mode is not 
    // already set to RECEIVE.
    switch (sessionMode) {
    case SessionMode.NONE:
      setSessionMode(SessionMode.RECEIVE);
      break;
    default:
      throw new IllegalStateException("Illegal session mode");
    }

    if (requestStatus != RequestStatus.NONE) 
      throw new IllegalStateException("Illegal request status");

    singleThreadOfControl = Thread.currentThread();
    pendingMessageConsumer = mc;
    
    setRequestStatus(RequestStatus.RUN);
  }
  
  /**
   * Final stage after calling the reply has been returned
   * by the roxy. It releases the session and enables another
   * thread to call it.
   */
  private synchronized void postReceive() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.postReceive()");

    singleThreadOfControl = null;
    pendingMessageConsumer = null;
    setRequestStatus(RequestStatus.NONE);
    setSessionMode(SessionMode.NONE);
    notifyAll();
  }
  
  /**
   * Called here and by sub-classes.
   */
  protected synchronized void addConsumer(MessageConsumer mc) {
    consumers.addElement(mc);
  }

  /**
   * Called by MessageConsumer.
   */
  synchronized void closeConsumer(MessageConsumer mc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.closeConsumer(" + mc + ')');
    consumers.removeElement(mc);

    if (pendingMessageConsumer == mc) {
      if (requestStatus == RequestStatus.RUN) {
        // Close the requestor. A call to abortRequest() 
        // is not enough because the receiving thread 
        // may call request() just after this thread 
        // calls abort().
        receiveRequestor.close();

        // Wait for the end of the request
        try {
          while (requestStatus != RequestStatus.NONE) {
            wait();
          }
        } catch (InterruptedException exc) {
        }

        // Create a new requestor.
        receiveRequestor = new Requestor(mtpx);
      }
    }
  }

  /**
   * Called by Connection (i.e. temporary destinations deletion)
   */
  synchronized void checkConsumers(String agentId) throws JMSException {
    for (int j = 0; j < consumers.size(); j++) {
      MessageConsumer cons = (MessageConsumer) consumers.elementAt(j);
      if (agentId.equals(cons.dest.agentId)) {
        throw new JMSException("Consumers still exist for this temp queue.");
      }
    }
  }

  /**
   * Called here and by sub-classes.
   */
  protected void addProducer(MessageProducer mp) {
    producers.addElement(mp);
  }

  /**
   * Called by MessageProducer.
   */
  synchronized void closeProducer(MessageProducer mp) {
    producers.removeElement(mp);
  }

  /**
   * Called by Queue browser.
   */
  synchronized void closeBrowser(QueueBrowser qb) {
    browsers.removeElement(qb);
  }

  /**
   * Called by MessageConsumer
   */
  synchronized MessageConsumerListener addMessageListener(MessageConsumerListener mcl) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.addMessageListener(" + mcl + ')');
    checkClosed();
    checkThreadOfControl();

    checkSessionMode(SessionMode.LISTENER);

    mcl.start();

    if (status == Status.START && listenerCount == 0) {
      doStart();
    }

    listenerCount++;
    return mcl;
  }

  /**
   * Called by MessageConsumer. The thread of control and the status
   * must be checked if the call results from a setMessageListener
   * but not from a close.
   */
  void removeMessageListener(MessageConsumerListener mcl, boolean check) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.removeMessageListener(" + mcl + ',' + check + ')');

    if (check) {
      checkClosed();
      checkThreadOfControl();
    }
    
    // This may block if a message listener
    // is currently receiving a message (onMessage is called)
    // so we have to be out of the synchronized block.
    mcl.close();
    
    synchronized (this) {
      listenerCount--;
      if (status == Status.START && listenerCount == 0) {
        try {
          repliesIn.stop();
        } catch (InterruptedException iE) {
        }
        // All the message listeners have been closed
        // so we can call doStop() in a synchronized
        // block. No deadlock possible.
        doStop();
      }
    }
  }

  /**
   * Called by MessageConsumerListener (demultiplexer thread
   * from RequestMultiplexer) in order to distribute messages 
   * to a message consumer.
   * Not synchronized because a concurrent close
   * can be done.
   *
   * @exception 
   */
  void pushMessages(SingleSessionConsumer consumerListener, ConsumerMessages messages) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.pushMessages(" + consumerListener + ',' + messages + ')');
    repliesIn.push(new MessageListenerContext(consumerListener, messages));
  }

  /**
   * Called by ConnectionConsumer in order to distribute a message through the 
   * method run(). Session mode is APP_SERVER.
   */
  void onMessage(org.objectweb.joram.shared.messages.Message msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.onMessage(" + msg + ')');

    repliesIn.push(msg);
  }

  /**
   * Called by:
   * - method run (application server thread) synchronized
   * - method onMessage (SessionDaemon thread) not synchronized
   * but no concurrent call except a close which first stops
   * SessionDaemon.
   */
  private void denyMessage(String targetName, String msgId, boolean queueMode, boolean redelivered) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.denyMessage(" + targetName + ',' + msgId + ',' + queueMode + ',' + redelivered + ')');
    ConsumerDenyRequest cdr = new ConsumerDenyRequest(targetName, msgId, queueMode);
    cdr.setRedelivered(redelivered);
    if (queueMode) {
      requestor.request(cdr);
    } else {
      mtpx.sendRequest(cdr);
    }
  }
  
  /**
   * Called by SessionDaemon.
   * Not synchronized but no concurrent call except 
   * a close which first stops SessionDaemon.
   */
  private void onMessages(MessageListenerContext ctx) throws JMSException {
    Vector msgs = ctx.messages.getMessages();
    for (int i = 0; i < msgs.size(); i++) {
      onMessage((org.objectweb.joram.shared.messages.Message) msgs.elementAt(i), ctx.consumerListener);
    }
  }

  /**
   * Called by onMessages()
   */
  void onMessage(org.objectweb.joram.shared.messages.Message momMsg,
                 MessageConsumerListener mcl) throws JMSException {
    String msgId = momMsg.id;

    if (!autoAck)
      prepareAck(mcl.getTargetName(), msgId, mcl.getQueueMode());

    Message msg = null;
    try {
      msg = Message.wrapMomMessage(this, momMsg);
    } catch (JMSException jE) {
      // Catching a JMSException means that the building of the Joram
      // message went wrong: denying the message:
      if (autoAck)
        denyMessage(mcl.getTargetName(), msgId, mcl.getQueueMode(), true);
      return;
    }
    msg.session = this;
    if (trace.isLoggable(BasicLevel.INFO))
      trace.log(BasicLevel.INFO,
                 this + " handling message=" + msg + ", from=" + mcl.getDestName() + '/' + mcl.getTargetName());
    // Executes IN interceptors
    if ((inInterceptors != null) && (!inInterceptors.isEmpty())) {
      for (Iterator it = inInterceptors.iterator(); it.hasNext();) {
        MessageInterceptor interceptor = (MessageInterceptor) it.next();
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Intercepting the message after receiving by " + interceptor.getClass().getName());

        try {
          interceptor.handle(msg, this);
        } catch (Throwable t) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "Error during interception (continue anyway...)", t);
        }
      }
    }

    try {
      if (messageListener == null) {
        // Standard JMS (MessageConsumer)
        mcl.onMessage(msg, acknowledgeMode);
      } else {
        // ASF (ConnectionConsumer)
        mcl.onMessage(msg, messageListener, acknowledgeMode);
      }
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);

      if (autoAck || mcl.isClosed()) {
        denyMessage(mcl.getTargetName(), msgId, mcl.getQueueMode(), false);
      }
      return;
    }
    
    if (recover) {
      // The session has been recovered by the
      // listener thread.
      if (autoAck) {
        denyMessage(mcl.getTargetName(), msgId, mcl.getQueueMode(), true);
      } else {
        doRecover();
        recover = false;
      }
    } else {
      if (autoAck) {
        mcl.ack(msgId, acknowledgeMode);
      }
    }
    
    if (toClose) {
      doClose();
      toClose = false;
    }
  }

  /**
   * Called by MessageProducer.
   */
  synchronized void send(Destination dest, javax.jms.Message msg, int deliveryMode, int priority,
      long timeToLive, boolean timestampDisabled, long deliveryDelay, javax.jms.CompletionListener completionListener) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Session.send(" + dest + ',' + msg + ',' +
                 deliveryMode + ',' + priority + ',' + timeToLive + ',' + timestampDisabled + ',' + completionListener + ')');

    checkClosed();
    checkThreadOfControl();

    if (msg == null)
      throw new MessageFormatException("Cannot send null message");

    // Updating the message property fields:
    msg.setJMSMessageID(cnx.nextMessageId());
    msg.setJMSDeliveryMode(deliveryMode);
    msg.setJMSDestination(dest);
    
    if (timeToLive == 0) {
      msg.setJMSExpiration(0);
    } else {
      msg.setJMSExpiration(System.currentTimeMillis() + timeToLive);
    }
    msg.setJMSPriority(priority);
    if (!timestampDisabled) {
      msg.setJMSTimestamp(System.currentTimeMillis());
    }
    msg.setJMSRedelivered(false);

    // set the deliveryTime
    if (deliveryDelay > 0) {
      long deliveryTime = System.currentTimeMillis() + deliveryDelay;
      msg.setJMSDeliveryTime(deliveryTime);
    }
    
    CompletionListener listener = null;
    if (completionListener != null)
      listener = new CompletionListener(completionListener, msg, this);
    
    Message joramMsg = null;
    try {
      joramMsg = (Message) msg;
    } catch (ClassCastException exc) {
      try {
        // If the message to send is a non proprietary JMS message, try
        // to convert it.
        joramMsg = Message.convertJMSMessage(msg);
      } catch (JMSException jE) {
        MessageFormatException mE = new MessageFormatException("Message to send is invalid.");
        mE.setLinkedException(jE);
        throw mE;
      }
    }
    if (trace.isLoggable(BasicLevel.INFO))
      trace.log(BasicLevel.INFO,
                 this + " sending message=" + joramMsg + ", to=" + dest.getAdminName());
    //Add out interception...
    if ((outInterceptors != null) && (!outInterceptors.isEmpty())) {
      for (Iterator it = outInterceptors.iterator(); it.hasNext();) {
        MessageInterceptor interceptor = (MessageInterceptor) it.next();
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Intercepting the message before sending by "
              + interceptor.getClass().getName());

        try {
          interceptor.handle(joramMsg, this);
        } catch (Throwable t) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "Warning while interception (continue anyway...)", t);
        }
      }
    }
    joramMsg.prepare();

    if (transacted) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Buffering the message.");
      // If the session is transacted, keeping the request for later delivery:
      prepareSend(dest, (org.objectweb.joram.shared.messages.Message) joramMsg.momMsg.clone());
    } else {
      ProducerMessages pM = new ProducerMessages(dest.getName(),
          (org.objectweb.joram.shared.messages.Message) joramMsg.momMsg.clone());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Sending " + joramMsg);

      if (asyncSend || (!joramMsg.momMsg.persistent)) {
        // Asynchronous sending
        pM.setAsyncSend(true);
        mtpx.sendRequest(pM, listener);
      } else {
        requestor.request(pM, listener);
      }
    }
  }

  /**
   * Called by MessageConsumer. The requestor raises an
   * exception if it is called during another request.
   * This cannot happen as a session is monothreaded.
   * A concurrent close first aborts the current request
   * so it releases the requestor for a subsequent use.
   */
  synchronized AbstractJmsReply syncRequest(AbstractJmsRequest request) throws JMSException {
    return requestor.request(request);
  }

  final Connection getConnection() {
    return cnx;
  }

  final String getId() {
    return ident;
  }

  final RequestMultiplexer getRequestMultiplexer() {
    return mtpx;
  }

  public final boolean isAutoAck() {
    return autoAck;
  }
  
  /**
   * The <code>SessionCloseTask</code> class is used by non-XA transacted
   * sessions for taking care of closing them if they tend to be pending,
   * and if a transaction timer has been set.
   */
  private class SessionCloseTask extends TimerTask {
    private long txPendingTimer;

    SessionCloseTask(long txPendingTimer) {
      this.txPendingTimer = txPendingTimer;
    }

    /** Method called when the timer expires, actually closing the session. */
    public void run() {
      try {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Session closed because of pending transaction");
        close();
      } catch (Exception e) {
      }
    }

    public void start() {
      try {
        mtpx.schedule(this, txPendingTimer);
      } catch (Exception e) {
      }
    }
  }

  /**
   * This thread controls the session in mode LISTENER.
   */
  private class SessionDaemon extends fr.dyade.aaa.common.Daemon {
    SessionDaemon() {
      super("Connection#" + cnx + " - Session#" + ident, logger);
    }

    public void run() {
      while (running) {
        canStop = true;
        MessageListenerContext ctx;
        try {
          ctx = (MessageListenerContext) repliesIn.get();
          repliesIn.pop();
        } catch (InterruptedException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "", exc);
          return;
        }

        canStop = false;
        try {
          onMessages(ctx);
        } catch (JMSException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "", exc);
        }
      }
    }

    Thread getThread() {
      return thread;
    }

    protected void shutdown() {}

    protected void close() {}
  }

  /**
   * Context used to associate a message consumer with 
   * a set of messages to consume.
   */
  private static class MessageListenerContext {
    SingleSessionConsumer consumerListener;
    ConsumerMessages messages;

    MessageListenerContext(SingleSessionConsumer consumerListener, ConsumerMessages messages) {
      this.consumerListener = consumerListener;
      this.messages = messages;
    }
  }

  /**
   * API 2.0 method.
   */
  public javax.jms.MessageConsumer createSharedConsumer(javax.jms.Topic topic,
                                                        String name) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.createSharedConsumer(" + topic + ',' + name + ')');

    checkClosed();
    checkThreadOfControl();
    
    // TODO:
    if (topic == null)
      throw new InvalidDestinationException("Invalid null destination.");
    ((Topic) topic).check();

    throw new JMSException("not yet implemented.");
  }

  /**
   * API 2.0 method.
   */
  public javax.jms.MessageConsumer createSharedConsumer(javax.jms.Topic topic,
                                                        String name,
                                                        String selector) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Session.createSharedConsumer(" + topic + ',' + name + ',' + selector + ')');

    checkClosed();
    checkThreadOfControl();
    
	  // TODO:
    if (topic == null)
      throw new InvalidDestinationException("Invalid null destination.");
    ((Topic) topic).check();

	  throw new JMSException("not yet implemented.");
  }

  /**
   * API 2.0 method.
   * Creates a durable consumer to the specified topic.
   * <p>
   * If a client needs to receive all the messages published on a topic, including the ones
   * published while the subscriber is inactive, it needs to use a durable TopicSubscriber.
   * Joram retains a record of durable subscribers and insures that all messages from the
   * topic's publishers are retained until they are acknowledged by this durable consumer
   * or they have expired.
   * <p>
   * A client can change an existing durable consumer by creating a durable MessageConsumer
   * with the same name and a new topic and/or message selector. Changing a durable consumer
   * is equivalent to unsubscribing (deleting) the old one and creating a new one.
   * 
   * @param topic     the non-temporary Topic to subscribe to.
   * @param name      the name used to identify this subscription.
   * @return the created MessageConsumer object.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageConsumer createDurableConsumer(javax.jms.Topic topic,
                                                         String name) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Session.createDurableConsumer(" + topic + ',' + name + ')');
    
    checkClosed();
    checkThreadOfControl();
    
    MessageConsumer mc = new MessageConsumer(this, (Topic) topic, null, name, false);
    addConsumer(mc);
    return mc;
  }
  
  /**
   * API 2.0 method.
   * Creates a durable consumer to the specified topic.
   * <p>
   * If a client needs to receive all the messages published on a topic, including the ones
   * published while the subscriber is inactive, it needs to use a durable TopicSubscriber.
   * Joram retains a record of durable subscribers and insures that all messages from the
   * topic's publishers are retained until they are acknowledged by this durable consumer
   * or they have expired.
   * <p>
   * A client can change an existing durable consumer by creating a durable MessageConsumer
   * with the same name and a new topic and/or message selector. Changing a durable consumer
   * is equivalent to unsubscribing (deleting) the old one and creating a new one.
   * 
   * @param topic     the non-temporary Topic to subscribe to.
   * @param name      the name used to identify this subscription.
   * @param selector  the selector used to filter incoming messages.
   * @param noLocal   if true, inhibits the delivery of messages published by its own connection.
   * @return the created MessageConsumer object.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageConsumer createDurableConsumer(javax.jms.Topic topic,
                                                         String name,
                                                         String selector,
                                                         boolean noLocal) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Session.createDurableConsumer(" + topic + ',' + name  + ',' + selector  + ',' + noLocal + ')');
    
    checkClosed();
    checkThreadOfControl();
    
    MessageConsumer mc = new MessageConsumer(this, (Topic) topic, selector, name, noLocal);
    addConsumer(mc);
    return mc;
  }

  /**
   * API 2.0 method.
   */
  public javax.jms.MessageConsumer createSharedDurableConsumer(javax.jms.Topic topic,
                                                               String name) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Session.createSharedDurableConsumer(" + topic + ',' + name + ')');

    checkClosed();
    checkThreadOfControl();
    
    // TODO:
    if (topic == null)
      throw new InvalidDestinationException("Invalid null destination.");
    ((Topic) topic).check();

	  throw new JMSException("not yet implemented.");
  }

  /**
   * API 2.0 method.
   */
  public javax.jms.MessageConsumer createSharedDurableConsumer(javax.jms.Topic topic,
                                                               String name,
                                                               String selector) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Session.createSharedDurableConsumer(" + topic + ',' + name + ',' + selector + ')');

    checkClosed();
    checkThreadOfControl();
    
    // TODO:
    if (topic == null)
      throw new InvalidDestinationException("Invalid null destination.");
    ((Topic) topic).check();

	  throw new JMSException("not yet implemented.");
  }
}
