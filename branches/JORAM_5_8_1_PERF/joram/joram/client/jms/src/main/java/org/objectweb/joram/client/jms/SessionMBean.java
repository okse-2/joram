/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;


public interface SessionMBean {
  
  int getRepliesInSize();

  /**
   * Status of the session
   */
  String getStatus();

  /**
   * The way the session is used.
   */
  String getSessionMode();

  /**
   * The status of the current request.
   * Only valid in when the session is used to synchronously receive messages
   * (RECEIVE mode).
   */  
  String getRequestStatus();

  
  /** The identifier of the session. */
  //String getIdentifier();

  /** <code>true</code> if the session's acknowledgements are automatic. */
  boolean isAutoAck();

  /** 
   *  Indicates whether the messages consumed are implicitly acknowledged
   * or not. If true messages are immediately removed from queue when
   * delivered.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   *
   * @return true if messages produced are implicitly acknowledged.
   * @see #implicitAck
   */
   boolean isImplicitAck();

  /** 
   *  Indicates whether the messages produced are asynchronously sent
   * or not (without or with acknowledgment).
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * by default false. 
   *
   * @return true if messages produced are asynchronously sent.
   * @see #asyncSend
   */
  boolean isAsyncSend();

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
   * @see #queueMessageReadMax
   */
   int getQueueMessageReadMax();

  /**
   *  Get the maximum number of acknowledgements that can be buffered when
   * using Session.DUPS_OK_ACKNOWLEDGE mode for this session.
   * <p>
   *  This attribute is inherited from Connection at initialization.
   *
   * @return The Maximum number of acknowledgements that can be buffered when
   *         using Session.DUPS_OK_ACKNOWLEDGE mode.
   *
   * @see FactoryParameters#topicAckBufferMax
   * @see #topicAckBufferMax
   */
  int getTopicAckBufferMax();

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
   * @see #topicPassivationThreshold
   */
  int getTopicPassivationThreshold();

  /**
   * Get the threshold of activation for this session.
   * <p>
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from Connection at initialization,
   * default value is 0.
   *
   * @see #topicActivationThreshold
   *
   * @return The minimum messages number below which the subscription
   *         is activated.
   */
  int getTopicActivationThreshold();

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
  boolean isAsyncSub();

  boolean isStarted();

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  int getAcknowledgeMode() throws JMSException;
  
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  boolean getTransacted() throws JMSException;


  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception InvalidDestinationException  If the subscription does not 
   *              exist.
   * @exception JMSException  If the request fails for any other reason.
   */
  void unsubscribe(String name) throws JMSException;

  /**
   * Closes the session.
   * API method.
   *
   * @exception JMSException
   */
  void close() throws JMSException;

 

  /**
   * Starts the asynchronous deliveries in the session.
   * <p>
   * This method is called by a started connection.
   */
  //void start();

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
  //void stop();
}
