/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import javax.jms.CompletionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.MessageFormatException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Implements the <code>javax.jms.MessageProducer</code> interface.
 * <p>
 * A client uses a MessageProducer object to send messages to a destination.
 * A MessageProducer object is created by calling the createProducer method on
 * the session object. A message producer is normally dedicated to a unique
 * destination.
 * <br>
 * A client also has the option of creating a message producer without
 * supplying a unique destination. In this case, a destination must be
 * provided with every send operation. 
 * <br>
 * A client can specify a default delivery mode, priority, and time to live
 * for messages sent by a message producer. It can also specify the delivery
 * mode, priority, and time to live for each individual message.
 */
public class MessageProducer implements javax.jms.MessageProducer {
  /** Default delivery mode. */
  private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

  /** Default priority. */
  private int priority = Message.DEFAULT_PRIORITY;

  /** Default time to live. */
  private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;

  /**
   * <code>true</code> if the client requests not to use the message
   * identifiers; however it is not taken into account, as our MOM needs
   * message identifiers for managing acknowledgements.
   */
  private boolean messageIDDisabled = false;

  /** <code>true</code> if the time stamp is disabled. */
  private boolean timestampDisabled = false;

  /** <code>true</code> if the producer's destination is identified. */
  private boolean identified = true;

  /** <code>true</code> if the producer is closed. */
  protected boolean closed = false;

  /** The session the producer belongs to. */
  protected Session sess;

  /** The destination the producer sends messages to. */
  protected Destination dest = null;

  private static Logger logger = Debug.getLogger(MessageProducer.class.getName());

  /**
   * Constructs a producer.
   *
   * @param sess  The session the producer belongs to.
   * @param dest  The destination the producer sends messages to.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageProducer(Session sess, Destination dest) throws JMSException {
    this.sess = sess;
    this.dest = dest;
    if (dest == null)
      identified = false;
    else
      dest.check();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * API method, not taken into account.
   * Message IDs are always enabled.
   * 
   * @param value indicates if message IDs are disabled, not taken in account.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setDisableMessageID(boolean value) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");
  }

  /**
   * API method.
   * Sets the producer's default delivery mode.
   * <p>
   * Delivery mode is set to PERSISTENT by default.
   * 
   * @param deliveryMode  the message delivery mode for this message producer; legal values are
   *                      DeliveryMode.NON_PERSISTENT and DeliveryMode.PERSISTENT.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid delivery mode.
   */
  public synchronized void setDeliveryMode(int deliveryMode) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT
        && deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Can't set invalid delivery mode.");

    this.deliveryMode = deliveryMode;
  }

  /**
   * API method.
   * Sets the producer's default priority.
   * <p>
   * The JMS API defines ten levels of priority value, with 0 as the lowest priority
   * and 9 as the highest. Clients should consider priorities 0-4 as gradations of normal
   * priority and priorities 5-9 as gradations of expedited priority.
   * <p>
   * Priority is set to 4 by default (Message.DEFAULT_PRIORITY).
   * 
   * @param priority the message priority for this message producer; must be a value
   *                 between 0 and 9.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid priority.
   */
  public synchronized void setPriority(int priority) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    if (priority < 0 || priority > 9)
      throw new JMSException("Can't set invalid priority value.");

    this.priority = priority;
  }

  /**
   * API method.
   * Sets the default duration of time in milliseconds that a produced message should
   * be retained by the provider.
   * <p>
   * Time to live is set to zero by default (Message.DEFAULT_TIME_TO_LIVE).
   * 
   * @param timeToLive the message time to live in milliseconds; zero is unlimited.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setTimeToLive(long timeToLive) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    this.timeToLive = timeToLive;
  }

  /**
   * API method.
   * Sets whether message timestamps are disabled.
   * <p>
   * Since timestamps take some effort to create and increase a message's size, Joram
   * optimizes message overhead if it is given a hint that the timestamp is not used by
   * an application. By calling the setDisableMessageTimestamp method on this message
   * producer, a JMS client enables this potential optimization for all messages sent
   * by this message producer (the produced messages have the timestamp set to zero).
   * <p>
   * Message timestamps are enabled by default.
   * 
   * @param value indicates if message timestamps are disabled.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setDisableMessageTimestamp(boolean value) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    this.timestampDisabled = value;
  }

  /**
   * API method.
   * Gets the destination associated with this MessageProducer.
   * 
   * @return the destination associated with this MessageProducer.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized javax.jms.Destination getDestination() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return dest;
  }

  /**
   * API method.
   * Gets an indication of whether message IDs are disabled, always false.
   * 
   * @return false.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized boolean getDisableMessageID() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return messageIDDisabled;
  }

  /**
   * API method.
   * Gets the producer's default delivery mode, by default Message.DEFAULT_DELIVERY_MODE.
   * 
   * @return the message delivery mode for this message producer. 
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized int getDeliveryMode() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return deliveryMode;
  }

  /**
   * API method.
   * Gets the producer's default priority, by default Message.DEFAULT_PRIORITY.
   * 
   * @return the message priority for this message producer.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized int getPriority() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return priority;
  }

  /**
   * API method.
   * Gets the default duration in milliseconds that a produced message should be
   * retained by the provider, by default Message.DEFAULT_TIME_TO_LIVE.
   * 
   * @return the message time to live in milliseconds; zero is unlimited.
   * 
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized long getTimeToLive() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return timeToLive;
  }

  /**
   * API method.
   * Gets an indication of whether message timestamps are disabled.
   * 
   * @return an indication of whether message timestamps are disabled.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized boolean getDisableMessageTimestamp() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return timestampDisabled;
  }


  /**
   * API method.
   * Sends a message with the MessageProducer's default delivery parameters.
   * 
   * @param message the message to send.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Message message) throws JMSException {
    if (! identified)
      throw new UnsupportedOperationException("Can't send message to an unidentified destination.");
    // Actually producing it:
    doSend(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * API method.
   * Sends a message to the destination with given delivery parameters.
   * 
   * @param message       the message to send.
   * @param deliveryMode  the delivery mode to use.
   * @param priority      the priority for this message.
   * @param timeToLive    the message's lifetime in milliseconds.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Message message, 
                                int deliveryMode,
                                int priority, 
                                long timeToLive) throws JMSException {
    if (! identified)
      throw new UnsupportedOperationException("Can't send message to an unidentified destination.");
    // Actually producing it:
    doSend(dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * API method.
   * Sends a message to a destination for an unidentified message producer using default
   * delivery parameters.
   * <p>
   * Typically, a message producer is assigned a destination at creation time; however the
   * JMS API also supports unidentified message producers, which require that the destination
   * be supplied every time a message is sent.
   * 
   * @param dest    the destination to send this message to.
   * @param message the message to send.
   *
   * @exception UnsupportedOperationException  When the producer did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified destination.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Destination dest,
                                javax.jms.Message message) throws JMSException {
    if (identified)
      throw new UnsupportedOperationException("An unidentified message producer can't use this identified message producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't send message to an unidentified destination.");

    doSend((Destination) dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * API method.
   * Sends a message to a destination for an unidentified message producer with
   * given delivery parameters.
   * <p>
   * Typically, a message producer is assigned a destination at creation time; however the
   * JMS API also supports unidentified message producers, which require that the destination
   * be supplied every time a message is sent.
   * 
   * @param dest          the destination to send this message to.
   * @param message       the message to send.
   * @param deliveryMode  the delivery mode to use.
   * @param priority      the priority for this message.
   * @param timeToLive    the message's lifetime in milliseconds.
   *
   * @exception UnsupportedOperationException  When the producer did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified destination.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Destination dest, 
                                javax.jms.Message message,
                                int deliveryMode, 
                                int priority,
                                long timeToLive) throws JMSException
                                {
    if (identified)
      throw new UnsupportedOperationException("An unidentified message producer can't use this identified message producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't send message to an unidentified destination.");

    doSend((Destination) dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * API method.
   * Closes the message producer.
   * <p>
   * In order to free significant resources allocated on behalf of a MessageProducer,
   * clients should close them when they are not needed.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void close() throws JMSException {
    // Ignoring call if producer is already closed:
    if (closed)
      return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": closing...");

    sess.closeProducer(this);
    closed = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": closed.");

  }

  /**
   * Actually sends a message to a given destination.
   *
   * @exception MessageFormatException  If the message to send is invalid.
   * @exception InvalidDestinationException  If the specified destination is
   *              invalid.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  private void doSend(Destination dest, 
                      javax.jms.Message message,
                      int deliveryMode, 
                      int priority,
                      long timeToLive) throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    sess.send(dest, message, deliveryMode, priority, timeToLive, timestampDisabled);
  }

  public void setDeliveryDelay(long deliveryDelay) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public long getDeliveryDelay() throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public void send(javax.jms.Message message,
		  CompletionListener completionListener) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public void send(javax.jms.Message message, int deliveryMode, int priority,
		  long timeToLive, CompletionListener completionListener)
				  throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public void send(javax.jms.Destination destination, javax.jms.Message message,
		  CompletionListener completionListener) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public void send(javax.jms.Destination destination, javax.jms.Message message,
		  int deliveryMode, int priority, long timeToLive,
		  CompletionListener completionListener) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }
}
