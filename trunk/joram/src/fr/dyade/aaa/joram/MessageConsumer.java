/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.*;

import javax.jms.InvalidSelectorException;
import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.MessageConsumer</code> interface.
 */
public abstract class MessageConsumer implements javax.jms.MessageConsumer
{
  /** The session the consumer belongs to. */
  protected Session sess;
  /** The selector for filtering messages. */
  protected String selector;
  /** The message listener, if any. */
  protected javax.jms.MessageListener messageListener = null;
  /** <code>true</code> if the consumer is closed. */
  protected boolean closed = false;

  /** <code>true</code> if a listener is set for this consumer. */
  protected boolean listenerSet = false;
  /** Pending "receive" or listener request. */
  protected AbstractJmsRequest pendingReq = null;
  /**
   * <code>true</code> if the consumer has a pending synchronous "receive"
   * request.
   */
  protected boolean receiving = false;

  /** The destination the consumer gets its messages from. */
  Destination dest;
  /** The destination name. */
  String destName;


  /**
   * Constructs a consumer.
   *
   * @param sess  The session the consumer belongs to.
   * @param queue  The destination the consumer gets messages from.
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
    if (dest == null)
      throw new InvalidDestinationException("Invalid destination: " + dest);

    try {
      fr.dyade.aaa.mom.selectors.Selector.checks(selector);
    }
    catch (fr.dyade.aaa.mom.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    this.sess = sess;
    this.dest = dest;
    this.selector = selector;
    destName = dest.getName();

    // Checking the user's access permission:
    sess.cnx.isReader(destName);

    sess.consumers.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * API method specialized in subclasses.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
            throws JMSException
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": setting MessageListener to "
                                 + messageListener);

    if (closed)
      throw new IllegalStateException("Forbidden call on a closed consumer.");

    if (sess.cnx.started) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": improper call"
                                   + " on a started connection.");
    }

    // If unsetting the listener:
    if (this.messageListener != null && messageListener == null) {
      sess.msgListeners--;
      // Stopping the daemon if not needed anymore:
      if (sess.msgListeners == 0 && sess.started) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": stops the"
                                     + " session daemon.");
        sess.daemon.stop();
        sess.daemon = null;
      }
      listenerSet = false;
    }
    // Else, if setting a new listener:
    else if (this.messageListener == null && messageListener != null) {
      sess.msgListeners++;

      if (sess.msgListeners == 1 && sess.started) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": starts the"
                                     + " session daemon.");
        sess.daemon = new SessionDaemon(sess);
        sess.daemon.setDaemon(true);
        sess.daemon.start();
      }
      listenerSet = true;
    }
    this.messageListener = messageListener;
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

  /** API method implemented in subclasses. */
  public abstract javax.jms.Message receive(long timeOut) throws JMSException;

  /** 
   * API method.
   * 
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive() throws JMSException
  {
    return receive(-1);
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
    return receive(0);
  }

  /**
   * API method specialized in subclasses.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring the call if consumer is already closed:
    if (closed)
      return;

    sess.consumers.remove(this);
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
   * The purpose of this abstract method is to force the
   * <code>QueueReceiver</code> and <code>TopicSubscriber</code> classes to
   * specifically manage the distribution of asynchronous deliveries to their
   * message listeners.
   */
  abstract void onMessage(fr.dyade.aaa.mom.messages.Message message);
}
