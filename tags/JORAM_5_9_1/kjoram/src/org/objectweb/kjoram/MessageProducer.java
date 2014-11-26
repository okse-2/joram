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

public class MessageProducer {
  /** Default delivery mode: PERSISTENT. */
  public int deliveryMode;

  /** Default priority: 4. */
  public int priority;

  /** Default time to live: 0, messages never expire. */
  public long timeToLive;

  /** <code>true</code> if the producer is closed. */
  public boolean closed;

  /** The session the producer belongs to. */
  public Session session;

  /** The destination the producer sends messages to. */
  public Destination dest;
  
  /**
   * Constructs a message producer.
   *
   * @param session  The session the producer belongs to.
   * @param dest     The destination the producer sends messages to.
   *
   * @exception IllegalStateException
   *  If the connection is broken.
   * @exception JoramException
   *  If the creation fails for any other reason.
   */
  public MessageProducer(Session session, Destination dest) 
  throws IllegalStateException {
    this.deliveryMode = DeliveryMode.PERSISTENT;
    this.priority = 4;
    this.timeToLive = 0;
    this.closed = false;
    this.session = session;
    this.dest = dest;
  }

  /**
   * Sets the producer's default delivery mode.
   *
   * @exception IllegalStateException
   *    If the producer is closed.
   * @exception IllegalArgumentException
   *  When setting an invalid delivery mode.
   */
  public void setDeliveryMode(int deliveryMode) 
  throws IllegalStateException, IllegalArgumentException {
    if (closed) throw new IllegalStateException();

    if ((deliveryMode != DeliveryMode.PERSISTENT) &&
        (deliveryMode != DeliveryMode.NON_PERSISTENT))
      throw new IllegalArgumentException();

    this.deliveryMode = deliveryMode;
  }

  /**
   * Sets the producer's default priority.
   *
   * @exception IllegalStateException  
   *  If the producer is closed.
   * @exception IllegalArgumentException
   *  When setting an invalid priority.
   */
  public void setPriority(int priority) 
  throws IllegalStateException, IllegalArgumentException {
    if (closed) throw new IllegalStateException();
    if (priority < 0 || priority > 9) throw new IllegalArgumentException();

    this.priority = priority;
  }

  /**
   * Sets the default duration of time in milliseconds that a produced
   * message should be retained by the provider.
   *
   * @exception IllegalStateException  
   *  If the producer is closed.
   */
  public void setTimeToLive(long timeToLive) throws IllegalStateException {
    if (closed) throw new IllegalStateException();

    this.timeToLive = timeToLive;
  }

  /**
   * Gets the destination associated with this MessageProducer.
   *
   * @exception IllegalStateException
   *  If the producer is closed.
   */
  public Destination getDestination() throws IllegalStateException {
    if (closed) throw new IllegalStateException();

    return dest;
  }

  /**
   * Gets the producer's default delivery mode.
   *
   * @exception IllegalStateException  
   *  If the producer is closed.
   */
  public int getDeliveryMode() throws IllegalStateException {
    if (closed) throw new IllegalStateException();

    return deliveryMode;
  }

  /**
   * Gets the producer's default priority.
   *
   * @exception IllegalStateException  
   *  If the producer is closed.
   */
  public int getPriority() throws IllegalStateException {
    if (closed) throw new IllegalStateException();

    return priority;
  }

  /**
   * Gets the default duration in milliseconds that a produced message
   * should be retained by the provider.
   *
   * @exception IllegalStateException  
   *  If the producer is closed.
   */
  public long getTimeToLive() throws IllegalStateException {
    if (closed) throw new IllegalStateException();

    return timeToLive;
  }

  /**
   * Sends a message with the default delivery parameters.
   *
   * @exception InvalidDestinationException
   *  If the destinationInvalidDestinationException is unidentified.
   * @exception IllegalStateException
   *  If the producer is closed, or if the connection is broken.
   * @exception JoramException
   *  If the request fails for any other reason.
   */
  public void send(Message msg) throws JoramException {
    send(msg, dest);
  }

  public void send(Message msg, Destination dest) throws JoramException {
    send(msg, dest, deliveryMode, priority, timeToLive);
  }

  /**
   * Sends a message with given delivery parameters.
   *
   * @exception InvalidDestinationException
   *  If the destinationInvalidDestinationException is unidentified.
   * @exception IllegalStateException
   *  If the producer is closed, or if the connection is broken.
   * @exception SecurityException
   *  If the user if not a WRITER for the specified destination.
   * @exception JoramException
   *  If the request fails for any other reason.
   */
  public void send(Message msg, Destination dest,
                             int deliveryMode,
                             int priority,
                             long timeToLive) throws JoramException {
    if (dest == null)
      throw new InvalidDestinationException();

    if (msg  == null)
      throw new MessageFormatException();

    if (closed) throw new IllegalStateException();

    session.send(msg, dest, deliveryMode, priority, timeToLive);
  }

  /**
   * Closes the message producer.
   * API method.
   */
  public void close() {
    // Ignoring call if producer is already closed:
    if (closed) return;

    session.closeProducer(this);
    closed = true;
  }

}

