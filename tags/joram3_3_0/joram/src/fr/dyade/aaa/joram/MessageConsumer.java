/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.*;
import fr.dyade.aaa.util.TimerTask;

import java.util.Vector;

import javax.jms.InvalidSelectorException;
import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.MessageConsumer</code> interface.
 */
public class MessageConsumer implements javax.jms.MessageConsumer
{
  /** The selector for filtering messages. */
  private String selector;
  /** The message listener, if any. */
  private javax.jms.MessageListener messageListener = null;
  /** <code>true</code> for a durable subscriber. */
  private boolean durableSubscriber;
  /** Pending "receive" or listener request. */
  private AbstractJmsRequest pendingReq = null;
  /**
   * <code>true</code> if the consumer has a pending synchronous "receive"
   * request.
   */
  private boolean receiving = false;
  /** Task for replying to a pending synchronous "receive" with timer. */
  private TimerTask replyingTask = null;

  /** The destination the consumer gets its messages from. */
  protected Destination dest;
  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  protected boolean noLocal;
  /** <code>true</code> if the consumer is closed. */
  protected boolean closed = false;

  /** The session the consumer belongs to. */
  Session sess;
  /** 
   * The consumer server side target is either a queue or a subscription on
   * its proxy.
   */
  String targetName;
  /** <code>true</code> if the consumer is a queue consumer. */
  boolean queueMode;

  /**
   * Constructs a consumer.
   *
   * @param sess  The session the consumer belongs to.
   * @param dest  The destination the consumer gets messages from.
   * @param selector  Selector for filtering messages.
   * @param subName  The durableSubscriber subscription's name, if any.
   * @param noLocal  <code>true</code> for a subscriber not wishing to consume
   *          messages produced by its connection.
   *
   * @exception InvalidSelectorException  If the selector syntax is invalid.
   * @exception JMSSecurityException  If the user is not a READER on the 
   *              destination.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageConsumer(Session sess, Destination dest, String selector,
                  String subName, boolean noLocal) throws JMSException
  {
    if (dest == null)
      throw new InvalidDestinationException("Invalid null destination.");

    if (dest instanceof TemporaryQueue) {
      Connection tempQCnx = ((TemporaryQueue) dest).getCnx();

      if (tempQCnx == null || ! tempQCnx.equals(sess.cnx))
        throw new JMSSecurityException("Forbidden consumer on this "
                                       + "temporary destination.");
    }
    else if (dest instanceof TemporaryTopic) {
      Connection tempTCnx = ((TemporaryTopic) dest).getCnx();
    
      if (tempTCnx == null || ! tempTCnx.equals(sess.cnx))
        throw new JMSSecurityException("Forbidden consumer on this "
                                       + "temporary destination.");
    }

    try {
      fr.dyade.aaa.mom.selectors.Selector.checks(selector);
    }
    catch (fr.dyade.aaa.mom.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    // If the destination is a topic, the consumer is a subscriber:
    if (dest instanceof javax.jms.Topic) {
      if (subName == null) {
        subName = sess.cnx.nextSubName();
        durableSubscriber = false;
      }
      else
        durableSubscriber = true;

      sess.cnx.syncRequest(new ConsumerSubRequest(dest.getName(),
                                                  subName,
                                                  selector,
                                                  noLocal, durableSubscriber));
      targetName = subName;
      this.noLocal = noLocal;
      queueMode = false;
    }
    else {
      targetName = dest.getName();
      queueMode = true;
    }

    this.sess = sess;
    this.dest = dest;
    this.selector = selector;

    sess.consumers.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a consumer.
   *
   * @param sess  The session the consumer belongs to.
   * @param dest  The destination the consumer gets messages from.
   * @param selector  Selector for filtering messages.
   *
   * @exception InvalidSelectorException  If the selector syntax is invalid.
   * @exception JMSSecurityException  If the user is not a READER on the 
   *              destination.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageConsumer(Session sess, Destination dest,
                  String selector) throws JMSException
  {
    this(sess, dest, selector, null, false);
  }

  /** Returns a string view of this consumer. */
  public String toString()
  {
    return "Consumer:" + sess.ident;
  }

  /**
   * API method.
   * <p>
   * This method must not be called if the connection the consumer belongs to
   * is started, because the session would then be accessed by the thread
   * calling this method and by the thread controlling asynchronous deliveries.
   * This situation is clearly forbidden by the single threaded nature of
   * sessions. Moreover, unsetting a message listener without stopping the 
   * connection may lead to the situation where asynchronous deliveries would
   * arrive on the connection, the session or the consumer without being
   * able to reach their target listener!
   *
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   * @exception IllegalStateException  If the consumer is closed.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
              throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed consumer.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": setting MessageListener to "
                                 + messageListener);

    if (sess.cnx.started && JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
      JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": improper call"
                                 + " on a started connection.");

    // If unsetting the listener:
    if (this.messageListener != null && messageListener == null) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": unsets"
                                   + " listener request.");

      sess.cnx.requestsTable.remove(pendingReq.getRequestId());

      this.messageListener = messageListener;
      sess.msgListeners--;

      ConsumerUnsetListRequest unsetLR = null;
      if (queueMode) {
        unsetLR = new ConsumerUnsetListRequest(pendingReq.getRequestId(),
                                               true);
      }
      else
        unsetLR = new ConsumerUnsetListRequest(targetName, false);

      try {
        sess.cnx.syncRequest(unsetLR);
      } 
      // A JMSException might be caught if the connection is broken.
      catch (JMSException jE) {}
      pendingReq = null;

      // Stopping the daemon if not needed anymore:
      if (sess.msgListeners == 0 && sess.started) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": stops the"
                                     + " session daemon.");
        sess.daemon.stop();
        sess.daemon = null;
        sess.started = false;
      }
    }
    // Else, if setting a new listener:
    else if (this.messageListener == null && messageListener != null) {
      sess.msgListeners++;

      if (sess.msgListeners == 1
          && (sess.started || sess.cnx.started)) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": starts the"
                                     + " session daemon.");
        sess.daemon = new SessionDaemon(sess);
        sess.daemon.setDaemon(false);
        sess.daemon.start();
        sess.started = true;
      }

      this.messageListener = messageListener;
      pendingReq = new ConsumerSetListRequest(targetName, selector, queueMode);
      pendingReq.setIdentifier(sess.cnx.nextRequestId());
      sess.cnx.requestsTable.put(pendingReq.getRequestId(), this);
      sess.cnx.asyncRequest(pendingReq);
    }

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": MessageListener"
                                 + " set.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public javax.jms.MessageListener getMessageListener() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed consumer.");

    return messageListener;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public String getMessageSelector() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed consumer.");

    return selector;
  }

  /** 
   * API method implemented in subclasses. 
   *
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive(long timeOut) throws JMSException
  {
    // Synchronizing with a possible "close".
    synchronized(this) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                   + ": requests to receive a message.");

      if (closed)
        throw new IllegalStateException("Forbidden call on a closed consumer.");

      if (messageListener != null) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, "Improper call as a"
                                     + " listener exists for this consumer.");
      }
      else if (sess.msgListeners > 0) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, "Improper call as"
                                     + " asynchronous consumers have already"
                                     + " been set on the session.");
      }

      pendingReq = new ConsumerReceiveRequest(targetName, selector, timeOut,
                                              queueMode);
      pendingReq.setIdentifier(sess.cnx.nextRequestId());
      receiving = true;

      // In case of a timer, scheduling the receive:
      if (timeOut > 0) {
        replyingTask = new ConsumerReplyTask(pendingReq.getRequestId());
        sess.schedule(replyingTask, timeOut);
      }
    }

    // Expecting an answer:
    ConsumerMessages reply =
     (ConsumerMessages) sess.cnx.syncRequest(pendingReq);

    // Synchronizing again with a possible "close":
    synchronized(this) {
      receiving = false;
      pendingReq = null;

      if (replyingTask != null)
        replyingTask.cancel();

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": received a"
                                   + " reply.");
   
      if (reply.getMessage() != null) {
        String msgId = reply.getMessage().getIdentifier();
        // Auto ack: acknowledging the message:
        if (sess.autoAck)
          sess.cnx.asyncRequest(new ConsumerAckRequest(targetName, msgId,
                                                       queueMode));
        // Session ack: passing the id for later ack or deny:
        else
          sess.prepareAck(targetName, msgId, queueMode);

        return Message.wrapMomMessage(sess, reply.getMessage());
      }
      else
        return null;
    }
  }

  /** 
   * API method.
   * 
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive() throws JMSException
  {
    return receive(0);
  }

  /** 
   * API method.
   * 
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receiveNoWait() throws JMSException
  {
    return receive(-1);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring the call if consumer is already closed:
    if (closed)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": closing...");

    // Synchronizig with a possible receive() or onMessage() ongoing process.
    syncro();

    // Removing this resource's reference from everywhere:
    Object lock = null;
    if (pendingReq != null)
      lock = sess.cnx.requestsTable.remove(pendingReq.getRequestId());
    sess.consumers.remove(this);

    // Unsetting the listener, if any:
    try {
      if (messageListener != null) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Unsetting listener.");

        if (queueMode) {
          ConsumerUnsetListRequest unsetLR =
            new ConsumerUnsetListRequest(pendingReq.getRequestId(), true);
          sess.cnx.syncRequest(unsetLR);
        }
      }

      if (durableSubscriber)
        sess.cnx.syncRequest(new ConsumerCloseSubRequest(targetName));
      else if (! queueMode)
        sess.cnx.syncRequest(new ConsumerUnsubRequest(targetName));
    }
    // A JMSException might be caught if the connection is broken.
    catch (JMSException jE) {}

    // In the case of a pending "receive" request, replying by a null to it:
    if (lock != null && receiving) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Replying to the"
                                   + " pending receive "
                                   + pendingReq.getRequestId()
                                   + " with a null message.");

      sess.cnx.repliesTable.put(pendingReq.getRequestId(),
                                new ConsumerMessages(null, null, queueMode));

      synchronized(lock) {
        lock.notify();
      }
    }

    // Synchronizing again:
    syncro();

    closed = true;
    
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed.");
  }

  /**
   * Returns when the consumer isn't busy executing a "onMessage" or a
   * "receive" anymore; method called for synchronization purposes.
   */
  synchronized void syncro() {}
  
  /**
   * Method called by the session daemon for passing an asynchronous message 
   * delivery to the listener.
   */
  synchronized void onMessage(fr.dyade.aaa.mom.messages.Message message)
  {
    String msgId = message.getIdentifier();

    try {
      // If the listener has been unset without having stopped the
      // connection, this case might happen:
      if (messageListener == null) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": an"
                                     + " asynchronous delivery arrived"
                                     + " for an improperly unset listener:"
                                     + " denying the message.");
        sess.cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                     queueMode, true));
      }
      else {
        // In session ack mode, preparing later ack or deny:
        if (! sess.autoAck)
          sess.prepareAck(targetName, msgId, queueMode);

        try {
          messageListener.onMessage(Message.wrapMomMessage(sess, message));
          // Auto ack: acknowledging the message:
          if (sess.autoAck)
            sess.cnx.asyncRequest(new ConsumerAckRequest(targetName, msgId,
                                                         queueMode));
        }
        // Catching a JMSException means that the building of the Joram
        // message went wrong: denying as expected by the spec:
        catch (JMSException jE) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                       + ": error while processing the"
                                       + " received message: " + jE);

          if (queueMode)
            sess.cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                         queueMode));
          else
            sess.cnx.asyncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                          queueMode));
        }
        // Catching a RuntimeException means that the client onMessage() code
        // is incorrect; denying as expected by the JMS spec:
        catch (RuntimeException rE) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                       + ": RuntimeException thrown"
                                       + " by the listener: " + rE);

          if (sess.autoAck && queueMode)
            sess.cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                         queueMode));
          else if (sess.autoAck && ! queueMode)
            sess.cnx.asyncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                          queueMode));
        }
        // Sending a new request if queue mode:
        if (queueMode) {
          pendingReq = new ConsumerSetListRequest(targetName, selector, true);
          pendingReq.setIdentifier(sess.cnx.nextRequestId());
          sess.cnx.requestsTable.put(pendingReq.getRequestId(), this);
          sess.cnx.asyncRequest(pendingReq);
        }
      }
    }
    // Catching an IllegalStateException means that the acknowledgement or
    // denying went wrong because the connection has been lost. Nothing more
    // can be done here.
    catch (JMSException jE) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, this + ": " + jE);
    }
  }

  /**
   * The <code>ConsumerReplyTask</code> class is used by "receive" requests
   * with timer for taking care of answering them if the timer expires.
   */
  private class ConsumerReplyTask extends TimerTask
  {
    /** The identifier of the request to answer. */
    private String requestId;
    /** The reply to put in the connection's table. */
    private ConsumerMessages nullReply;

    /**
     * Constructs a <code>ConsumerReplyTask</code> instance.
     *
     * @param requestId  The identifier of the request to answer.
     */
    ConsumerReplyTask(String requestId)
    {
      this.requestId = requestId;
      this.nullReply = new ConsumerMessages(requestId, targetName, queueMode);
    }

    /**
     * Method called when the timer expires, actually putting a null answer
     * in the replies table and unlocking the requester.
     */
    public void run()
    {
      try {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, "Receive request" +
                                     " answered because timer expired");

        Lock lock = (Lock) sess.cnx.requestsTable.remove(requestId);

        if (lock == null)
          return;

        synchronized (lock) {
          sess.cnx.repliesTable.put(requestId, nullReply);
          lock.notify();
        }
      }
      catch (Exception e) {}
    }
  }
}
