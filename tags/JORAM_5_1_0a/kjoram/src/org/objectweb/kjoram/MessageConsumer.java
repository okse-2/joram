/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;


import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class MessageConsumer {
  public static Logger logger = Debug.getLogger(MessageConsumer.class.getName());
  
  /** <code>true</code> if the producer is closed. */
  public boolean closed;

  /** The session the producer belongs to. */
  public Session session;

  /** The destination the producer sends messages to. */
  public Destination dest;

  /** <code>true</code> for a durable subscriber. */
  public boolean durableSubscriber;

  /** The selector for filtering messages. */
  public String selector;

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  public boolean noLocal;

  /** 
   * The consumer server side target is either a queue or a subscription on
   * its proxy.
   */
  public String targetName;

  /** <code>true</code> if the consumer is a queue consumer. */
  public boolean queueMode;

  /**
   * Message listener context (null if no message listener).
   */
  public MessageConsumerListener mcl;


  /**
   * Constructs a MessageConsumer for the specified destination.
   *
   * @param session  The session the producer belongs to.
   * @param dest     The destination the producer sends messages to.
   *
   * @exception IllegalStateException
   *  If the connection is broken.
   * @exception JoramException
   *  If the creation fails for any other reason.
   */
  public MessageConsumer(Session session, Destination dest, String selector) 
  throws JoramException {
    this(session, dest, selector, null, false);
  }

  /**
   * Constructs a MessageConsumer for the specified destination using a
   * message selector.
   *
   * @param session  The session the producer belongs to.
   * @param dest     The destination the producer sends messages to.
   *
   * @exception IllegalStateException
   *  If the connection is broken.
   * @exception JoramException
   *  If the creation fails for any other reason.
   */
  public MessageConsumer(Session session, 
                         Destination dest,
                         String selector,
                         String subName, 
                         boolean noLocal) 
  throws JoramException {
    if (dest == null) throw new InvalidDestinationException();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumer()"); 

    // If the destination is a topic, the consumer is a subscriber:
    if (dest.isQueue()) {
      targetName = dest.getUID();
      queueMode = true;
    } else {
      if (subName == null) {
        subName = session.getConnection().nextSubName();
        durableSubscriber = false;
      } else {
        durableSubscriber = true;
      }
      AbstractReply reply = session.syncRequest(
          new ConsumerSubRequest(dest.getUID(), subName,
          selector,
          noLocal,
          durableSubscriber));
      if (reply != null) {
        reply = null;
      }
      targetName = subName;
      this.noLocal = noLocal;
      queueMode = false;
    }

    this.session = session;
    this.dest = dest;
    this.selector = null;
    this.mcl = null;

    this.closed = false;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumer session = " + session + ", dest = " + dest); 
  }

  public String getTargetName() {
    return targetName;
  }

  public boolean getQueueMode() {
    return queueMode;
  }

  /**
   * Sets the message consumer's MessageListener.
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
  public void setMessageListener(MessageListener listener) throws JoramException {
    if (closed) throw new IllegalStateException();

    throw new NotYetImplementedException();
  }

  /**
   * Gets the message consumer's MessageListener.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public MessageListener getMessageListener() throws JoramException {
    if (closed) throw new IllegalStateException();

    if (mcl == null)
      return null;
    return mcl.getMessageListener();
  }

  /**
   * Gets this message consumer's message selector expression.
   * API method.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  public String getMessageSelector() throws JoramException {
    if (closed) throw new IllegalStateException();
    return selector;
  }

  /** 
   * Receives the next message produced for this message consumer.
   * 
   * @exception IllegalStateException
   *  If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *  If the requester is not a READER on the destination.
   * @exception JoramException
   *  If the request fails for any other reason.
   */
  public Message receive() throws JoramException {
    return receive(0);
  }

  /** 
   * Receives the next message that arrives before the specified timeout.
   *
   * @exception IllegalStateException
   *  If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *  If the requester is not a READER on the destination.
   * @exception JoramException
   *  If the request fails for any other reason.
   */
  public Message receive(long timeOut) throws JoramException {
    if (closed) throw new IllegalStateException();

    return session.receive(timeOut, timeOut, this, 
                            targetName, selector, queueMode);
  }

  /** 
   * Receives the next message if one is immediately available.
   * 
   * @exception IllegalStateException
   *  If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *  If the requester is not a READER on the destination.
   * @exception JoramException
   *  If the request fails for any other reason.
   */
  public Message receiveNoWait() throws JoramException {
    if (closed) throw new IllegalStateException();

    if (session.getConnection().isStopped()) {
      return null;
    } else {
      return session.receive(-1, 0, this, targetName, selector, queueMode);
    }
  }

  /**
   * Closes the message consumer.
   * API method.
   * @throws JoramException 
   */
  public void close() throws JoramException {
    // Ignoring call if producer is already closed:
    if (closed) return;
    closed = true;

    if (!queueMode) {
      // For a topic, remove the subscription.
      if (durableSubscriber) {
        try {
          AbstractReply reply = session.syncRequest(new ConsumerCloseSubRequest(targetName));
          if (reply != null) {
            reply = null;
          }
        } catch (JoramException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "EXCEPTION:: MessageConsumer close()", exc); 
        }
      } else {
        try {
          AbstractReply reply = session.syncRequest(new ConsumerUnsubRequest(targetName));
          if (reply != null) {
            reply = null;
          }
        } catch (JoramException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "EXCEPTION:: MessageConsumer close()", exc); 
        }
      }
    }
    session.closeConsumer(this);

    if (mcl != null) {
      // This may block if a message listener
      // is currently receiving a message (onMessage is called)
      // so we have to be out of the synchronized block.
      mcl.close();
      
      // Stop the listener.
      session.removeMessageListener(mcl);
    }
  }
}

