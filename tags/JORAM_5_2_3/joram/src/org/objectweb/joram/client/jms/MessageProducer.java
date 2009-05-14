/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
import javax.jms.MessageFormatException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Implements the <code>javax.jms.MessageProducer</code> interface.
 */
public class MessageProducer implements javax.jms.MessageProducer {
  /** Default delivery mode. */
  private int deliveryMode = javax.jms.DeliveryMode.PERSISTENT;

  /** Default priority. */
  private int priority = 4;

  /** Default time to live. */
  private long timeToLive = 0;

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
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageProducer(Session sess, 
                  Destination dest) 
                  throws JMSException {
    this.sess = sess;
    this.dest = dest;
    if (dest == null)
      identified = false;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * API method; not taken into account.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setDisableMessageID(boolean value) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");
  }

  /**
   * Sets the producer's default delivery mode.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid delivery mode.
   */
  public synchronized void setDeliveryMode(int deliveryMode) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT
        && deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Can't set invalid delivery mode.");

    this.deliveryMode = deliveryMode;
  }

  /**
   * Sets the producer's default priority.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid priority.
   */
  public synchronized void setPriority(int priority) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    if (priority < 0 || priority > 9)
      throw new JMSException("Can't set invalid priority value.");

    this.priority = priority;
  }

  /**
   * Sets the default duration of time in milliseconds that a produced
   * message should be retained by the provider.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setTimeToLive(long timeToLive) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    this.timeToLive = timeToLive;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized void setDisableMessageTimestamp(boolean value) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    this.timestampDisabled = value;
  }

  /**
   * Gets the destination associated with this MessageProducer.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized javax.jms.Destination getDestination() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return dest;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized boolean getDisableMessageID() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return messageIDDisabled;
  }

  /**
   * Gets the producer's default delivery mode.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized int getDeliveryMode() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return deliveryMode;
  }

  /**
   * Gets the producer's default priority.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized int getPriority() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return priority;
  }

  /**
   * Gets the default duration in milliseconds that a produced message
   * should be retained by the provider.
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized long getTimeToLive() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return timeToLive;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public synchronized boolean getDisableMessageTimestamp() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    return timestampDisabled;
  }


  /**
   * Sends a message with the default delivery parameters.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Message message) throws JMSException
  {
    if (! identified)
      throw new UnsupportedOperationException("Can't send message to"
                                              + " an unidentified"
                                              + " destination.");
    // Actually producing it:
    doSend(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * Sends a message with given delivery parameters.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public synchronized void send(javax.jms.Message message, 
                                int deliveryMode,
                                int priority, 
                                long timeToLive) throws JMSException
                                {
    if (! identified)
      throw new UnsupportedOperationException("Can't send message to"
                                              + " an unidentified"
                                              + " destination.");
    // Actually producing it:
    doSend(dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * Sends a message with default delivery parameters for an unidentified 
   * message producer.
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
                                javax.jms.Message message) throws JMSException
                                {
    if (identified)
      throw new UnsupportedOperationException("An unidentified message"
                                              + " producer can't use this"
                                              + " identified message"
                                              + " producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't send message to"
                                              + " an unidentified"
                                              + " destination.");

    doSend((Destination) dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * Sends a message with given delivery parameters for an unidentified
   * message producer.
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
      throw new UnsupportedOperationException("An unidentified message"
                                              + " producer can't use this"
                                              + " identified message"
                                              + " producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't send message to"
                                              + " an unidentified"
                                              + " destination.");

    doSend((Destination) dest, message, deliveryMode, priority, timeToLive);
                                }

  /**
   * Closes the message producer.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void close() throws JMSException
  {
    // Ignoring call if producer is already closed:
    if (closed)
      return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + ": closing...");

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
                      long timeToLive) 
  throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

    sess.send(dest, message, deliveryMode, priority, 
              timeToLive, timestampDisabled);
  }
}
