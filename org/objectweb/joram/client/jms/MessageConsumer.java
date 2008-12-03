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

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.shared.client.ConsumerCloseSubRequest;
import org.objectweb.joram.shared.client.ConsumerSubRequest;
import org.objectweb.joram.shared.client.ConsumerUnsubRequest;

import org.objectweb.joram.shared.selectors.ClientSelector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * Implements the <code>javax.jms.MessageConsumer</code> interface.
 */
public class MessageConsumer implements javax.jms.MessageConsumer {
  /**
   * Status of the message consumer.
   */
  private static class Status {
    /**
     * Status of the message consumer
     * when it is open. It is the initial state.
     */
    public static final int OPEN = 0;
    
    /**
     * Status of the message consumer when it is
     * closed.
     */
    public static final int CLOSE = 1;
    
    private static final String[] names = {
      "OPEN", "CLOSE"};
    
    public static String toString(int status) {
      return names[status];
    }
  }

  /** The selector for filtering messages. */
  String selector;

  /** <code>true</code> for a durable subscriber. */
  private boolean durableSubscriber;

  /** The destination the consumer gets its messages from. */
  protected Destination dest;

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  protected boolean noLocal;

  /** The session the consumer belongs to. */
  protected Session sess;

  /** 
   * The consumer server side target is either a queue or a subscription on
   * its proxy.
   */
  String targetName;

  /** <code>true</code> if the consumer is a queue consumer. */
  boolean queueMode;

  /**
   * Message listener context (null if no message listener).
   */
  private MessageConsumerListener mcl;

  /**
   * Status of the message consumer
   * OPEN, CLOSE
   */
  private int status;
  
  /**
   * Used to synchonize the 
   * method close()
   */
  private Closer closer;

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
   * @exception IllegalStateException  If the connection is broken, or if the
   *                                   subscription is durable and already
   *                                   activated.
   * @exception JMSException           Generic exception.
   */
  MessageConsumer(Session sess, 
                  Destination dest, 
                  String selector,
                  String subName, 
                  boolean noLocal) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.<init>(" + 
        sess + ',' + dest + ',' + selector + ',' + 
        subName + ',' + noLocal + ')');
    
    if (dest == null)
      throw new InvalidDestinationException("Invalid null destination.");

    if (dest instanceof TemporaryQueue) {
      Connection tempQCnx = ((TemporaryQueue) dest).getCnx();

      if (tempQCnx == null || ! tempQCnx.equals(sess.getConnection()))
        throw new JMSSecurityException("Forbidden consumer on this "
                                       + "temporary destination.");
    }
    else if (dest instanceof TemporaryTopic) {
      Connection tempTCnx = ((TemporaryTopic) dest).getCnx();
    
      if (tempTCnx == null || ! tempTCnx.equals(sess.getConnection()))
        throw new JMSSecurityException("Forbidden consumer on this "
                                       + "temporary destination.");
    }

    try {
      ClientSelector.checks(selector);
    } catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    // If the destination is a topic, the consumer is a subscriber:
    if (dest instanceof javax.jms.Topic) {
      if (subName == null) {
        subName = sess.getConnection().nextSubName();
        durableSubscriber = false;
      } else {
        durableSubscriber = true;
      }
      sess.syncRequest(
        new ConsumerSubRequest(dest.getName(),
                               subName,
                               selector,
                               noLocal,
                               durableSubscriber,
                               sess.isAsyncSub()));
      targetName = subName;
      this.noLocal = noLocal;
      queueMode = false;
    } else {
      targetName = dest.getName();
      queueMode = true;
    }

    this.sess = sess;
    this.dest = dest;
    this.selector = selector;
    
    closer = new Closer();

    setStatus(Status.OPEN);
  }

  /**
   * Constructs a consumer.
   *
   * @param sess  The session the consumer belongs to.
   * @param dest  The destination the consumer gets messages from.
   * @param selector  Selector for filtering messages.
   *
   * @exception InvalidSelectorException  If the selector syntax is invalid.
   * @exception IllegalStateException  If the connection is broken, or if the
   *                                   subscription is durable and already
   *                                   activated.
   * @exception JMSException           Generic exception.
   */
  MessageConsumer(Session sess, 
                  Destination dest,
                  String selector) throws JMSException {
    this(sess, dest, selector, null, false);
  }

  private synchronized void setStatus(int status) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }

  public final String getTargetName() {
    return targetName;
  }

  public final boolean getQueueMode() {
    return queueMode;
  }

  protected synchronized void checkClosed() 
    throws IllegalStateException {
    if (status == Status.CLOSE)
      throw new IllegalStateException("Forbidden call on a closed consumer.");
  }

  /** Returns a string view of this consumer. */
  public String toString() {
    return "Consumer:" + sess.getId();
  }
  
  /**
   * Sets the message consumer's MessageListener.
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
   */
  public synchronized void setMessageListener(
    javax.jms.MessageListener messageListener)
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.setMessageListener(" + 
        messageListener + ')');
    checkClosed();
    if (mcl != null) {
      if (messageListener == null) {
        sess.removeMessageListener(mcl, true);
        mcl = null;
      } else throw new IllegalStateException(
        "Message listener not null");
    } else {
      if (messageListener != null) {
        mcl = sess.addMessageListener(
          new SingleSessionConsumer(
            queueMode,
            durableSubscriber,
            selector,
            targetName,
            sess,
            messageListener,
            sess.getQueueMessageReadMax(),
            sess.getTopicActivationThreshold(),
            sess.getTopicPassivationThreshold(),
            sess.getTopicAckBufferMax(),
            sess.getRequestMultiplexer()));
      }
      // else idempotent
    }
  }

  /**
   * Gets the message consumer's MessageListener.
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public synchronized javax.jms.MessageListener getMessageListener() 
    throws JMSException {
    checkClosed();
    if (mcl == null) return null;
    return mcl.getMessageListener();
  }

  /**
   * Gets this message consumer's message selector expression.
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public final String getMessageSelector() 
    throws JMSException {
    checkClosed();
    return selector;
  }

  /** 
   * Receives the next message that arrives before the specified timeout.
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSSecurityException  If the requester is not a READER on the
   *              destination.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive(long timeOut) 
    throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.receive(" + timeOut + ')');
    checkClosed();
    return sess.receive(timeOut, timeOut, this, 
                        targetName, selector, queueMode);
  }

  /** 
   * Receives the next message produced for this message consumer.
   * API method.
   * 
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSSecurityException  If the requester is not a READER on the
   *              destination.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive() 
    throws JMSException {
    return receive(0);
  }

  /** 
   * Receives the next message if one is immediately available.
   * API method.
   * 
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSSecurityException  If the requester is not a READER on the
   *              destination.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receiveNoWait() 
    throws JMSException {
    checkClosed();
    if (sess.getConnection().isStopped()) {
      return null;
    } else {
      return sess.receive(-1, 0, this, 
                          targetName, selector, queueMode);
    }
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
        "MessageConsumer.close()");
    closer.close();
  }

  /**
   * This class synchronizes the close.
   * Close can't be synchronized with 'this' 
   * because the MessageConsumer must be accessed
   * concurrently during its closure. So
   * we need a second lock.
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
      // The status must be changed before
      // the call to Session.closeConsumer
      // in order to enable Session.preReceive
      // to check if the consumer has been closed.
      setStatus(Status.CLOSE);
    }
    
    if (!queueMode) {
      // For a topic, remove the subscription.
      if (durableSubscriber) {
        try {
          sess.syncRequest(new ConsumerCloseSubRequest(targetName));
        } catch (JMSException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
        }
      } else {
        try {
          sess.syncRequest(new ConsumerUnsubRequest(targetName));
        } catch (JMSException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
        }
      }
    }
    
    sess.closeConsumer(this);
    
    if (mcl != null) {
      // Stop the listener.
      sess.removeMessageListener(mcl, false);
    }
  }

  void activateMessageInput() throws JMSException {
    if (mcl != null) 
      mcl.activateMessageInput();
  }

  void passivateMessageInput() throws JMSException {
    if (mcl != null) 
      mcl.passivateMessageInput();
  }
}
