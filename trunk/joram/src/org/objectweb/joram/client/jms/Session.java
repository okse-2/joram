/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.client.jms.connection.Requestor;
import org.objectweb.joram.shared.client.*;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.TransactionRolledBackException;
import javax.jms.IllegalStateException;
import javax.jms.MessageFormatException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Session</code> interface.
 */
public class Session implements javax.jms.Session {

  public static final String RECEIVE_ACK =
      "org.objectweb.joram.client.jms.receiveAck";

  public static boolean receiveAck =
      Boolean.getBoolean(RECEIVE_ACK);

 public static final String PENDING_MSG_MAX =
     "org.objectweb.joram.client.jms.pendingMsgMax";

  public static final String PENDING_MSG_MIN =
      "org.objectweb.joram.client.jms.pendingMsgMin";

  private static int pendingMsgMax = 
      Integer.getInteger(PENDING_MSG_MAX, Integer.MAX_VALUE).intValue();

  private static int pendingMsgMin = 
      Integer.getInteger(PENDING_MSG_MIN, 0).intValue();

  /**
   * Status of the session
   */
  private static class Status {
    /**
     * Status of the session
     * when the connection is stopped.
     * This is the initial status.
     */
    public static final int STOP = 0;

    /**
     * Status of the session when the connection
     * is started.
     */
    public static final int START = 1;

    /**
     * Status of the connection when it is closed.
     */
    public static final int CLOSE = 2;

    private static final String[] names = {
      "STOP", "START", "CLOSE"};

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
     * The session is used to
     * synchronously receive messages.
     */
    public static final int RECEIVE = 1;

    /**
     * The session is used to asynchronously listen
     * to messages.
     */
    public static final int LISTENER = 2;

    /**
     * The session is used by an application server.
     */
    public static final int APP_SERVER = 3;

    private static final String[] names = {
      "NONE", "RECEIVE", "LISTENER", "APP_SERVER"};

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The status of the current request.
   * Only valid in the mode RECEIVE.
   */  
  private static class RequestStatus {
    /**
     * No request. This is the initial status.
     */
    public static final int NONE = 0;

    /**
     * A request is running (pending).
     */
    public static final int RUN = 1;

    /**
     * The request is done.
     */
    public static final int DONE = 2;

    private static final String[] names = {
      "NONE", "RUN", "DONE"};

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
  private fr.dyade.aaa.util.Queue repliesIn;

  /** Daemon distributing asynchronous server deliveries. */
  private SessionDaemon daemon;

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

  /** The connection consumer delivering messages to the session, if any. */
  private ConnectionConsumer connectionConsumer;

  /**
   * The request multiplexer used to communicate
   * with the user proxy.
   */
  private RequestMultiplexer mtpx;

  /**
   * The requestor used by the session 
   * to communicate
   * with the user proxy.
   */
  private Requestor requestor;

  /**
   * The requestor used by the session 
   * to make 'receive'
   * with the user proxy. This second requestor 
   * is necessary because it must be closed
   * during the session close (see method close).
   */
  private Requestor receiveRequestor;

  /**
   * Indicates that the session has been 
   * recovered by a message listener.
   * Doesn't need to be volatile because
   * it is only used by the SessionDaemon thread.
   */
  private boolean recover;

  /**
   * Status of the session:
   * STOP, START, CLOSE
   */
  private int status;

  /**
   * Mode of the session:
   * NONE, RECEIVE, LISTENER, APP_SERVER
   */
  private int sessionMode;

  /**
   * Status of the request:
   * NONE, RUN, DONE.
   */
  private int requestStatus;

  /**
   * The message consumer currently
   * making a request (null if none).
   */
  private MessageConsumer pendingMessageConsumer;

  /**
   * The current active control thread.
   */
  private Thread singleThreadOfControl;

  /**
   * Status boolean indicating whether
   * the message input is activated or not
   * for the message listeners.
   */
  private boolean passiveMsgInput;
  
  /**
   * Used to synchronize the
   * method close()
   */
  private Closer closer;
  
  /**
   * Indicates whether the messages produced are asynchronously
   * sent or not (without or with acknowledgement)
   */
  private boolean asyncSend;

  /**
   * Maximum number of messages that can be
   * read at once from a queue.
   */
  private int queueMessageReadMax;
  
  /**
   * Maximum number of acknowledgements
   * that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode.
   * Default is 0.
   */
  private int topicAckBufferMax;
  
  /**
   * This threshold is the maximum messages 
   * number over
   * which the subscription is passivated.
   * 
   */
  private int topicPassivationThreshold;
  
  /**
   * This threshold is the minimum 
   * messages number below which
   * the subscription is activated.
   * 
   */
  private int topicActivationThreshold;
  
  /**
   * Opens a session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  Session(Connection cnx, 
          boolean transacted,
          int acknowledgeMode,
          RequestMultiplexer mtpx)
    throws JMSException {
    if (! transacted 
        && acknowledgeMode != javax.jms.Session.AUTO_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.DUPS_OK_ACKNOWLEDGE
        && !(cnx instanceof XAQueueConnection)
        && !(cnx instanceof XATopicConnection)
        && !(cnx instanceof XAConnection))
      throw new JMSException("Can't create a non transacted session with an"
                             + " invalid acknowledge mode.");

    this.ident = cnx.nextSessionId();
    this.cnx = cnx;
    this.transacted = transacted;
    this.acknowledgeMode = acknowledgeMode;
    this.mtpx = mtpx;
    requestor = new Requestor(mtpx);
    receiveRequestor = new Requestor(mtpx);

    autoAck = ! transacted
      && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE;

    consumers = new Vector();
    producers = new Vector();
    browsers = new Vector();
    repliesIn = new fr.dyade.aaa.util.Queue();
    sendings = new Hashtable();
    deliveries = new Hashtable();
    
    closer = new Closer();

    // If the session is transacted and the transactions limited by a timer,
    // a closing task might be useful.
    if (transacted && cnx.getTxPendingTimer() > 0) {
      closingTask = new SessionCloseTask(
        cnx.getTxPendingTimer() * 1000);
    }
    
    asyncSend = cnx.getAsyncSend();
    queueMessageReadMax = cnx.getQueueMessageReadMax();
    topicAckBufferMax = cnx.getTopicAckBufferMax();
    topicActivationThreshold = cnx.getTopicActivationThreshold();
    topicPassivationThreshold = cnx.getTopicPassivationThreshold();

    setStatus(Status.STOP);
    setSessionMode(SessionMode.NONE);
    setRequestStatus(RequestStatus.NONE);
  }

  /**
   * Sets the status of the session.
   */
  private void setStatus(int status) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.setStatus(" + 
        Status.toString(status) + ')');
    this.status = status;
  }

  boolean isStarted() {
    return (status == Status.START);
  }

  /**
   * Sets the session mode.
   */
  private void setSessionMode(int sessionMode) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.setSessionMode(" + 
        SessionMode.toString(sessionMode) + ')');
    this.sessionMode = sessionMode;
  }

  /**
   * Sets the request status.
   */
  private void setRequestStatus(int requestStatus) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.setRequestStatus(" + 
        RequestStatus.toString(requestStatus) + ')');
    this.requestStatus = requestStatus;
  }
  
  /**
   * Checks if the session is closed. 
   * If true, an IllegalStateException
   * is raised.
   */  
  protected synchronized void checkClosed() 
    throws IllegalStateException {
    if (status == Status.CLOSE)
      throw new IllegalStateException(
        "Forbidden call on a closed session.");
  }

  /**
   * Checks if the calling thread is 
   * the thread of control. If not, 
   * an IllegalStateException is raised.
   */
  private synchronized void checkThreadOfControl() 
    throws IllegalStateException {
    if (singleThreadOfControl != null &&
        Thread.currentThread() != singleThreadOfControl)
      throw new IllegalStateException("Illegal control thread");
  }

  /**
   * Checks the session mode. If it is not 
   * the expected session mode, raises an illegal state
   * exception.
   *
   * @param expectedSessionMode the expected session mode.
   */
  private void checkSessionMode(
    int expectedSessionMode) 
    throws IllegalStateException {
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
   *
   * @exception JMSException  Actually never thrown.
   */
  public final int getAcknowledgeMode() throws JMSException {
    checkClosed();
    return getAckMode();
  }
  
  int getAckMode() {
    if (transacted)
      return Session.SESSION_TRANSACTED;
    return acknowledgeMode;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized final boolean getTransacted() 
    throws JMSException {
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
    }
    // else should throw an exception but not expected in
    // the connector.
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void setMessageListener(
    javax.jms.MessageListener messageListener)
    throws JMSException {
    checkSessionMode(SessionMode.APP_SERVER);
    this.messageListener = messageListener;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized javax.jms.MessageListener 
      getMessageListener() 
    throws JMSException {
    return messageListener;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.Message createMessage() 
    throws JMSException {
    checkClosed();
    return new Message();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.TextMessage createTextMessage() 
    throws JMSException {
    checkClosed();
    return new TextMessage();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.TextMessage createTextMessage(String text)
    throws JMSException {
    checkClosed();
    TextMessage message =  new TextMessage();
    message.setText(text);
    return message;
  }
  
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.BytesMessage createBytesMessage()
    throws JMSException {
    checkClosed();
    return new BytesMessage();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.MapMessage createMapMessage()
    throws JMSException {
    checkClosed();
    return new MapMessage();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.ObjectMessage createObjectMessage()
    throws JMSException {
    checkClosed();
    return new ObjectMessage();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.ObjectMessage createObjectMessage(
    java.io.Serializable obj)
    throws JMSException {
    checkClosed();
    ObjectMessage message = new ObjectMessage(); 
    message.setObject(obj);
    return message;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.StreamMessage createStreamMessage()
    throws JMSException {
    checkClosed();
    return new StreamMessage();
  }

  /**
   * API method
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.QueueBrowser
      createBrowser(javax.jms.Queue queue, 
                    String selector)
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    QueueBrowser qb = new QueueBrowser(this, (Queue) queue, selector);
    browsers.addElement(qb);
    return qb;
  }

  /**
   * API method
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.QueueBrowser 
      createBrowser(javax.jms.Queue queue)
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    QueueBrowser qb =  new QueueBrowser(this, (Queue) queue, null);
    browsers.addElement(qb);
    return qb;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageProducer createProducer(
    javax.jms.Destination dest)
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageProducer mp = new MessageProducer(
      this, 
      (Destination) dest);
    addProducer(mp);
    return mp;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer
      createConsumer(javax.jms.Destination dest, 
                     String selector,
                     boolean noLocal) 
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(
      this, (Destination) dest, 
      selector, null,
      noLocal);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer
      createConsumer(javax.jms.Destination dest, 
                     String selector)
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(
      this, (Destination) dest, selector);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.MessageConsumer 
      createConsumer(javax.jms.Destination dest)
    throws JMSException {
    checkClosed();
    checkThreadOfControl();
    MessageConsumer mc = new MessageConsumer(
      this, (Destination) dest, null);
    addConsumer(mc);
    return mc;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.TopicSubscriber
      createDurableSubscriber(javax.jms.Topic topic, 
                              String name,
                              String selector,
                              boolean noLocal) 
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.createDurableSubscriber(" + 
        topic + ',' + name + ',' + 
        selector + ',' + noLocal + ')');
    checkClosed();
    checkThreadOfControl();
    TopicSubscriber ts = new TopicSubscriber(
      this, (Topic) topic, name, selector, noLocal);
    addConsumer(ts);
    return ts;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.TopicSubscriber
      createDurableSubscriber(javax.jms.Topic topic, 
                              String name)
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.createDurableSubscriber(" + 
        topic + ',' + name + ')');
    checkClosed();
    checkThreadOfControl();
    TopicSubscriber ts = new TopicSubscriber(
      this, (Topic) topic, name, null, false);
    addConsumer(ts);
    return ts;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public synchronized javax.jms.Queue createQueue(
    String queueName) 
    throws JMSException {
    checkClosed();
    return new Queue(queueName);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the topic creation failed.
   */
  public synchronized javax.jms.Topic createTopic(
    String topicName) 
    throws JMSException {
    checkClosed();
    checkThreadOfControl();

    // Checks if the topic to retrieve is the administration topic:
    if (topicName.equals("#AdminTopic")) {
      try {
        GetAdminTopicReply reply =  
          (GetAdminTopicReply) requestor.request(new GetAdminTopicRequest());
        if (reply.getId() != null)
          return new Topic(reply.getId());
        else
          throw new JMSException("AdminTopic could not be retrieved.");
      }
      catch (JMSException exc) {
        throw exc;
      }
      catch (Exception exc) {
        throw new JMSException("AdminTopic could not be retrieved: " + exc);
      }
    }
    return new Topic(topicName);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized javax.jms.TemporaryQueue createTemporaryQueue() 
    throws JMSException {
    checkClosed();
    checkThreadOfControl();

    SessCreateTDReply reply =
      (SessCreateTDReply) requestor.request(new SessCreateTQRequest());
    String tempDest = reply.getAgentId();
    return new TemporaryQueue(tempDest, cnx);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized javax.jms.TemporaryTopic createTemporaryTopic() 
    throws JMSException {
    checkClosed();
    checkThreadOfControl();

    SessCreateTDReply reply =
      (SessCreateTDReply) requestor.request(new SessCreateTTRequest());
    String tempDest = reply.getAgentId();
    return new TemporaryTopic(tempDest, cnx);
  }

  /** API method. */
  public synchronized void run() {
    int load = repliesIn.size();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "-- " + this
                                 + ": loaded with " + load
                                 + " message(s) and started.");
    try {
      // Processing the current number of messages in the queue:
      for (int i = 0; i < load; i++) {
        org.objectweb.joram.shared.messages.Message momMsg = 
          (org.objectweb.joram.shared.messages.Message) repliesIn.pop();
        String msgId = momMsg.getIdentifier();
        
        if (messageListener == null) {
          // If no message listener has been set for the session, denying the
          // processed message:
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this + ": an"
                                       + " asynchronous delivery arrived for"
                                       + " a non existing session listener:"
                                       + " denying the message.");
          denyMessage(connectionConsumer.getTargetName(), 
                      msgId, 
                      connectionConsumer.getQueueMode());
        } else {
          Message msg = prepareMessage(
            momMsg, 
            connectionConsumer.getTargetName(),
            connectionConsumer.getQueueMode());
          
          if (msg == null) return;
          
          try {
            messageListener.onMessage(msg);
          } catch (RuntimeException exc) {
            if (autoAck) {
              denyMessage(connectionConsumer.getTargetName(), 
                          momMsg.getIdentifier(), 
                          connectionConsumer.getQueueMode());
            }
            return;
          }
          
          if (autoAck) {
            ackMessage(connectionConsumer.getTargetName(), 
                       momMsg.getIdentifier(), 
                       connectionConsumer.getQueueMode());
          }
        }
      }
    } catch (JMSException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
    }
  }

  /**
   * Called by ConnectionConsumer on a Session
   * from a server session pool.
   */
  synchronized void setConnectionConsumer(
    ConnectionConsumer connectionConsumer) {
    this.connectionConsumer = connectionConsumer;
  }
      
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted, or if the connection is broken.
   */
  public synchronized void commit() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.commit()");

    checkClosed();
    checkThreadOfControl();

    if (! transacted)
      throw new IllegalStateException("Can't commit a non transacted"
                                      + " session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": committing...");

    // If the transaction was scheduled: cancelling.
    if (scheduled) {
      closingTask.cancel();
      scheduled = false;
    }

    // Sending client messages:
    try {
      CommitRequest commitReq= new CommitRequest();
      
      Enumeration producerMessages = sendings.elements();
      while (producerMessages.hasMoreElements()) {
        ProducerMessages pM = 
          (ProducerMessages) producerMessages.nextElement();
        commitReq.addProducerMessages(pM);
      }
      sendings.clear();
      
      // Acknowledging the received messages:
      Enumeration targets = deliveries.keys();
      while (targets.hasMoreElements()) {
        String target = (String) targets.nextElement();
        MessageAcks acks = (MessageAcks) deliveries.get(target);
        commitReq.addAckRequest(
          new SessAckRequest(
            target, 
            acks.getIds(),
            acks.getQueueMode()));
      }
      deliveries.clear();
      
      if (asyncSend) {
        // Asynchronous sending
        commitReq.setAsyncSend(true);
        mtpx.sendRequest(commitReq);
      } else {
        requestor.request(commitReq);
      }

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": committed.");
    }
    // Catching an exception if the sendings or acknowledgement went wrong:
    catch (JMSException jE) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, "", jE);
      TransactionRolledBackException tE = 
        new TransactionRolledBackException("A JMSException was thrown during"
                                           + " the commit.");
      tE.setLinkedException(jE);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, "Exception: " + tE);

      rollback();
      throw tE;
    }
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted.
   */
  public synchronized void rollback() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.rollback()");

    checkClosed();
    checkThreadOfControl();

    if (! transacted)
      throw new IllegalStateException("Can't rollback a non transacted"
                                      + " session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": rolling back...");

    // If the transaction was scheduled: cancelling.
    if (scheduled) {
      closingTask.cancel();
      scheduled = false;
    }

    // Denying the received messages:
    deny();
    // Deleting the produced messages:
    sendings.clear();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": rolled back.");
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or transacted.
   */
  public synchronized void recover() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.recover()");

    checkClosed();
    checkThreadOfControl();

    if (transacted)
      throw new IllegalStateException("Can't recover a transacted session.");
    
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + " recovering...");

    if (daemon != null &&  
        daemon.isCurrentThread()) {
      recover = true;
    } else {
      doRecover();
    }

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": recovered.");
  }
  
  private void doRecover() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Session.doRecover()");
    deny();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception InvalidDestinationException  If the subscription does not 
   *              exist.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void unsubscribe(String name)
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.unsubscribe(" + name + ')');

    checkClosed();
    checkThreadOfControl();
    
    MessageConsumer cons;
    if (consumers != null) {
      for (int i = 0; i < consumers.size(); i++) {
        cons = (MessageConsumer) consumers.get(i);
        if (! cons.queueMode && cons.targetName.equals(name))
          throw new JMSException("Can't delete durable subscription " + name
                                 + " as long as an active subscriber exists.");
      }
    }
    syncRequest(new ConsumerUnsubRequest(name));
  }

  /**
   * API method.
   *
   * @exception JMSException
   */
  public void close() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.close()");
    closer.close();
  }

  /**
   * This class synchronizes the close.
   * Close can't be synchronized with 'this' 
   * because the Session must be accessed
   * concurrently during its closure. So
   * we need a second lock.
   */
  class Closer {
    synchronized void close() 
      throws JMSException {
      doClose();
    }
  }

  void doClose() throws JMSException {
    synchronized (this) {
      if (status == Status.CLOSE) return;
    }
    
    // Don't synchronize the consumer closure because
    // it could deadlock with message listeners or
    // client threads still using the session.

    Vector consumersToClose = (Vector)consumers.clone();
    consumers.clear();
    for (int i = 0; i < consumersToClose.size(); i++) {
      MessageConsumer mc = 
        (MessageConsumer)consumersToClose.elementAt(i);
      try {
        mc.close();
      } catch (JMSException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "", exc);
      }
    }
    
    Vector browsersToClose = (Vector)browsers.clone();
    browsers.clear();
    for (int i = 0; i < browsersToClose.size(); i++) {
      QueueBrowser qb = 
        (QueueBrowser)browsersToClose.elementAt(i);
      try {
      qb.close();
      } catch (JMSException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "", exc);
      }
    }
    
    Vector producersToClose = (Vector)producers.clone();
    producers.clear();
    for (int i = 0; i < producersToClose.size(); i++) {
      MessageProducer mp = 
        (MessageProducer)producersToClose.elementAt(i);
      try {
      mp.close();
      } catch (JMSException exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(
            BasicLevel.DEBUG, "", exc);
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
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.start()");

    if (status == Status.CLOSE) return;
    if (status == Status.START) return;
    if (listenerCount > 0) {
      doStart();
    }

    setStatus(Status.START);
  }

  private void doStart() {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.doStart()");
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
   * will never be poped out, and this may lead to a situation of consumed
   * but never acknowledged messages.
   * <p>
   * This fatal situation never occurs as the <code>stop()</code> method is
   * either called by he <code>Session.close()</code>
   * and <code>Connection.stop()</code> methods, which first empties the
   * session's deliveries and forbid any further push.
   */
  synchronized void stop() {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.stop()");
    if (status == Status.STOP ||
        status == Status.CLOSE) return;

    // DF: According to JMS 1.1 java doc
    // the method stop "blocks until receives in progress have completed." 
    // But the JMS 1.1 specification doesn't mention this point. 
    // So we don't implement it: a stop doesn't block until 
    // receives have completed.

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
  private void prepareSend(
    Destination dest, 
    org.objectweb.joram.shared.messages.Message msg) 
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.prepareSend(" + dest + ',' + msg + ')');
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
  private void prepareAck(String name, 
                          String id, 
                          boolean queueMode) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.prepareAck(" + 
        name + ',' + id + ',' + queueMode + ')');

    // If the transaction was scheduled, cancelling:
    if (scheduled)
      closingTask.cancel();

    MessageAcks acks = (MessageAcks) deliveries.get(name);
    if (acks == null) {
      acks = new MessageAcks(queueMode);
      deliveries.put(name, acks);
    }
    acks.addId(id);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, " -> acks = " + acks);

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
    if (transacted ||
        acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE) {
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
      mtpx.sendRequest(
        new SessAckRequest(
          target, 
          acks.getIds(),
          acks.getQueueMode()));
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
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.deny()");
    Enumeration targets = deliveries.keys();
    while (targets.hasMoreElements()) {
      String target = (String) targets.nextElement();
      MessageAcks acks = (MessageAcks) deliveries.remove(target);
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, 
          " -> acks = " + acks + ')');
      SessDenyRequest deny = new SessDenyRequest(
        target, 
        acks.getIds(), 
        acks.getQueueMode());
      if (acks.getQueueMode()) {
        requestor.request(deny);
      } else {
        mtpx.sendRequest(deny);
      }
    }
  }

  /**
   * Called by MessageConsumer
   * Not synchronized because ot it can be
   * concurrently called by close()
   * and Connection.stop().
   */
  javax.jms.Message receive(
    long requestTimeToLive,
    long waitTimeOut,
    MessageConsumer mc,
    String targetName,
    String selector,
    boolean queueMode) 
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.receive(" + 
        requestTimeToLive + ',' + 
        waitTimeOut + ',' + 
        targetName + ',' + 
        selector + ',' + 
        queueMode + ')');
    preReceive(mc);
    try {
      ConsumerMessages reply = null;
      ConsumerReceiveRequest request =
        new ConsumerReceiveRequest(
          targetName, 
          selector, 
          requestTimeToLive,
          queueMode);
      if (receiveAck) request.setReceiveAck(true);
      reply =
        (ConsumerMessages)receiveRequestor.request(
          request,
          waitTimeOut);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, 
          " -> reply = " + reply);
        
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
          if (msgs != null && ! msgs.isEmpty()) {
            org.objectweb.joram.shared.messages.Message msg =
              (org.objectweb.joram.shared.messages.Message) msgs.get(0);
            String msgId = msg.getIdentifier();
            
            // Auto ack: acknowledging the message:
            if (autoAck && ! receiveAck) {
              ConsumerAckRequest req = 
                new ConsumerAckRequest(
                  targetName,
                  queueMode);
              req.addId(msgId);
              mtpx.sendRequest(req);
            } else {
              prepareAck(targetName,
                         msgId,
                         queueMode);
            }
            return Message.wrapMomMessage(this, msg);
          } else {
            return null;
          }
        } else {
            return null;
        }
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
  private synchronized void preReceive(
    MessageConsumer mc) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.preReceive(" + mc + ')');
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
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.postReceive()");

    singleThreadOfControl = null;
    pendingMessageConsumer = null;
    setRequestStatus(RequestStatus.NONE);
    setSessionMode(SessionMode.NONE);
    notifyAll();
  }
  
  /**
   * Called here and by sub-classes.
   */
  protected synchronized void addConsumer(
    MessageConsumer mc) {
    consumers.addElement(mc);
  }

  /**
   * Called by MessageConsumer.
   */
  synchronized void closeConsumer(MessageConsumer mc) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.closeConsumer(" + mc + ')');
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
        } catch (InterruptedException exc) {}

        // Create a new requestor.
        receiveRequestor = new Requestor(mtpx);
      }
    }
  }
  
  /**
   * Called by Connection (i.e. temporary destinations deletion)
   */
  synchronized void checkConsumers(String agentId) 
    throws JMSException {
    for (int j = 0; j < consumers.size(); j++) {
      MessageConsumer cons = 
        (MessageConsumer) consumers.elementAt(j);
      if (agentId.equals(cons.dest.agentId)) {
        throw new JMSException(
          "Consumers still exist for this temp queue.");
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
  synchronized MessageConsumerListener addMessageListener(
    MessageConsumerListener mcl) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.addMessageListener(" + mcl + ')');
    checkClosed();
    checkThreadOfControl();

    checkSessionMode(SessionMode.LISTENER);

    mcl.start();
    
    if (status == Status.START &&
        listenerCount == 0) {
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
  void removeMessageListener(
    MessageConsumerListener mcl,
    boolean check) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.removeMessageListener(" + 
        mcl + ',' + check + ')');

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
  void pushMessages(MessageConsumerListener consumerListener, 
                   ConsumerMessages messages) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.pushMessages(" + 
        consumerListener + ',' + messages + ')');
    repliesIn.push(
      new MessageListenerContext(
        consumerListener, messages));
  }

  /**
   * Called by ConnectionConsumer in order
   * to distribute a message through the 
   * method run().
   * (session mode is APP_SERVER)
   */
  void onMessage(org.objectweb.joram.shared.messages.Message momMsg) {
    repliesIn.push(momMsg);
  }

  /**
   * Called by:
   * - method run (application server thread) synchronized
   */
  private void ackMessage(String targetName, 
                          String msgId,
                          boolean queueMode) 
    throws JMSException {
    ConsumerAckRequest ack = new ConsumerAckRequest(
      targetName, queueMode);
    ack.addId(msgId);
    mtpx.sendRequest(ack);
  }

  /**
   * Called by:
   * - method run (application server thread) synchronized
   * - method onMessage (SessionDaemon thread) not synchronized
   * but no concurrent call except a close which first stops
   * SessionDaemon.
   */
  private void denyMessage(String targetName, 
                           String msgId,
                           boolean queueMode) 
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "Session.denyMessage(" + 
        targetName + ',' + 
        msgId + ',' + 
        queueMode + ')');
    ConsumerDenyRequest cdr = new ConsumerDenyRequest(
      targetName, msgId, queueMode);
    if (queueMode) {
      requestor.request(cdr);
    } else {
      mtpx.sendRequest(cdr, null);
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
      onMessage(
        (org.objectweb.joram.shared.messages.Message)msgs.elementAt(i),
        ctx.consumerListener);
    }
  }

  /**
   * Called by onMessage()
   */
  private Message prepareMessage(
    org.objectweb.joram.shared.messages.Message momMsg,
    String targetName,
    boolean queueMode) throws JMSException {
    if (! autoAck) {
      prepareAck(targetName, 
                 momMsg.getIdentifier(), 
                 queueMode);
    }
    
    Message msg;
    try {
      return Message.wrapMomMessage(this, momMsg);      
    } catch (JMSException jE) {
      // Catching a JMSException means that the building of the Joram
      // message went wrong: denying the message:
      if (autoAck) {
        denyMessage(targetName, 
                    momMsg.getIdentifier(), 
                    queueMode);
      }
      return null;
    }
  }
  
  /**
   * Called by onMessages()
   */
  void onMessage(
    org.objectweb.joram.shared.messages.Message momMsg,
    MessageConsumerListener consumerListener) throws JMSException {
    MessageConsumer consumer = consumerListener.getMessageConsumer();
    
    Message msg = prepareMessage(
      momMsg, 
      consumer.targetName,
      consumer.queueMode);
    
    if (msg == null) return;
    
    try {
      consumerListener.onMessage(msg);
    } catch (JMSException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      if (autoAck || consumerListener.isClosed()) {
        denyMessage(consumer.targetName, 
                    momMsg.getIdentifier(), 
                    consumer.queueMode);
      }
      return;
    }
    
    if (recover) {
      // The session has been recovered by the
      // listener thread.
      if (autoAck) {
        denyMessage(consumer.targetName, 
                    momMsg.getIdentifier(), 
                    consumer.queueMode);
      } else {
        doRecover();
        recover = false;
      }
    } else {
      if (autoAck) {
        consumerListener.ack(consumer.targetName, momMsg.getIdentifier(),
            consumer.queueMode);
      }
    }
  }

  /**
   * Called by MessageProducer.
   */
  synchronized void send(Destination dest, 
                         javax.jms.Message message,
                         int deliveryMode, 
                         int priority,
                         long timeToLive,
                         boolean timestampDisabled) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Session.send(" + 
        dest + ',' +
        message + ',' + 
        deliveryMode + ',' + 
        priority + ',' + 
        timeToLive + ',' + 
        timestampDisabled + ')');
    
    checkClosed();
    checkThreadOfControl();

    // Updating the message property fields:
    String msgID = cnx.nextMessageId();
    message.setJMSMessageID(msgID);
    message.setJMSDeliveryMode(deliveryMode);
    message.setJMSDestination(dest);
    if (timeToLive == 0) {
      message.setJMSExpiration(0);
    } else {
      message.setJMSExpiration(System.currentTimeMillis() + timeToLive);
    } 
    message.setJMSPriority(priority);
    if (! timestampDisabled) {
      message.setJMSTimestamp(System.currentTimeMillis());
    }
    
    org.objectweb.joram.shared.messages.Message momMsg = null;
    if (message instanceof org.objectweb.joram.client.jms.Message) {
      // If the message to send is a proprietary one, getting the MOM message
      // it wraps:
      momMsg = ((Message) message).getMomMessage();
    } else if (message instanceof javax.jms.Message) {
      // If the message to send is a non proprietary JMS message, building
      // a proprietary message and then getting the MOM message it wraps:
      try {
        Message joramMessage = Message.convertJMSMessage(message);
        momMsg = joramMessage.getMomMessage();
      } catch (JMSException jE) {
        MessageFormatException mE = new MessageFormatException("Message to"
                                                               + " send is"
                                                               + " invalid.");
        mE.setLinkedException(jE);
        throw mE;
      }
    } else {
      // If not, building a new request and sending it:
      MessageFormatException mE = new MessageFormatException("Message to"
                                                             + " send is"
                                                             + " invalid.");
      throw mE;
    }

    if (transacted) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Buffering the message.");
      // If the session is transacted, keeping the request for later delivery:
      prepareSend(
        dest,
        (org.objectweb.joram.shared.messages.Message) momMsg.clone());
    } else {
      ProducerMessages pM = 
        new ProducerMessages(dest.getName(),
                             (org.objectweb.joram.shared.messages.Message) momMsg.clone());
      
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Sending " + momMsg);
      
      if (asyncSend || (! momMsg.getPersistent())) {
        // Asynchronous sending
        pM.setAsyncSend(true);     
        mtpx.sendRequest(pM);
      } else {
        requestor.request(pM);
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
  synchronized AbstractJmsReply syncRequest(
    AbstractJmsRequest request) 
    throws JMSException {
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

  private void activateMessageInput() throws JMSException {
    for (int i = 0; i < consumers.size(); i++) {
      MessageConsumer cons = 
        (MessageConsumer) consumers.elementAt(i);
      cons.activateMessageInput();
    }
    passiveMsgInput = false;
  }

  private void passivateMessageInput() throws JMSException {
    for (int i = 0; i < consumers.size(); i++) {
      MessageConsumer cons = 
        (MessageConsumer) consumers.elementAt(i);
      cons.passivateMessageInput();
    }
    passiveMsgInput = true;
  }

  /**
   * Set asyncSend for this Session.
   * 
   * @param b
   */
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  /**
   * Set queueMessageReadMax for this Session.
   * 
   * @param i
   */
  public void setQueueMessageReadMax(int i) {
    queueMessageReadMax = i;
  }
  
  public final int getQueueMessageReadMax() {
    return queueMessageReadMax;
  }
  
  public final int getTopicAckBufferMax() {
    return topicAckBufferMax;
  }
  
  public void setTopicAckBufferMax(int i) {
    topicAckBufferMax = i;
  }
  
  public final int getTopicActivationThreshold() {
    return topicActivationThreshold;
  }
  
  public void setTopicActivationThreshold(int i) {
    topicActivationThreshold = i;
  }
  
  public final int getTopicPassivationThreshold() {
    return topicPassivationThreshold;
  }
  
  public void setTopicPassivationThreshold(int i) {
    topicPassivationThreshold = i;
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
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, "Session closed "
                                     + "because of pending transaction");
        close();
      } catch (Exception e) {}
    }

    public void start() {
      try {
        mtpx.schedule(this, txPendingTimer);
      } catch (Exception e) {}
    }
  }

  /**
   * This thread controls the session in mode LISTENER.
   */
  private class SessionDaemon extends fr.dyade.aaa.util.Daemon {
    SessionDaemon() {
      super("Connection#" + cnx + " - Session#" + ident);
    }

    public void run() {
      while (running) {
        canStop = true;
        MessageListenerContext ctx;
        try {          
          ctx = (MessageListenerContext)repliesIn.get();
          repliesIn.pop();
        } catch (InterruptedException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
          return;
        }

        canStop = false;
        try {
          onMessages(ctx);
        } catch (JMSException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
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
    MessageConsumerListener consumerListener;
    ConsumerMessages messages;

    MessageListenerContext(
      MessageConsumerListener consumerListener, 
      ConsumerMessages messages) {
      this.consumerListener = consumerListener;
      this.messages = messages;
    }
  }
}
