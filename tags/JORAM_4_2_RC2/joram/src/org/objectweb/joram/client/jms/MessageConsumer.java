/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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

import org.objectweb.joram.shared.client.*;
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

  /** The message listener, if any. */
  private javax.jms.MessageListener messageListener = null;

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
      org.objectweb.joram.shared.selectors.Selector.checks(selector);
    }
    catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
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
                               durableSubscriber));
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
    if (this.messageListener != null) {
      if (messageListener == null) {
        sess.removeMessageListener(mcl, true);
        this.messageListener = null;
        mcl = null;
      } else throw new IllegalStateException(
        "Message listener not null");
    } else {
      if (messageListener != null) {
        mcl = sess.addMessageListener(this);
        this.messageListener = messageListener;
      }
      // else idempotent
    }
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public synchronized javax.jms.MessageListener getMessageListener() 
    throws JMSException {
    checkClosed();
    return messageListener;
  }

  /**
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
   * API method implemented in subclasses.
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
    if (sess.getConnection().isStarted()) {
      return sess.receive(-1, 0, this, 
                          targetName, selector, queueMode);
    } else {
      return null;
    }
  }

  /**
   * API method.
   *
   * @exception JMSException
   */
  public synchronized void close() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.close()");

    // Ignoring the call if consumer is already closed:
    if (status == Status.CLOSE) return;

    sess.closeConsumer(this);

    if (queueMode) {
      if (messageListener != null) {
        sess.removeMessageListener(mcl, false);
      }
    } else {
      if (durableSubscriber) {
        sess.syncRequest(
          new ConsumerCloseSubRequest(targetName));
      } else {
        sess.syncRequest(
          new ConsumerUnsubRequest(targetName));
      }
    }

    setStatus(Status.CLOSE);
  }

  /**
   * Called by Session for passing an asynchronous message 
   * delivery to the listener.
   * Not synchronized because it could deadlock with close:
   * the closing thread waits for the listener the thread
   * to return from onMessage. The synchronization between
   * close and onMessage is done in MessageConsumerListener.
   */
  void onMessage(Message msg) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "MessageConsumer.onMessage(" + msg + ')');
    if (messageListener == null) 
      throw new IllegalStateException("Null message listener");
    try {
      messageListener.onMessage(msg);
    } catch (RuntimeException re) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", re);
      JMSException exc = new JMSException(re.toString());
      exc.setLinkedException(re);
      throw exc;
    }
  }
}
