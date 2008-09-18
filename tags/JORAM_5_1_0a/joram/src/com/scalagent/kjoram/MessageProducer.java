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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.jms.*;
import com.scalagent.kjoram.excepts.IllegalStateException;
import com.scalagent.kjoram.excepts.*;

public class MessageProducer
{
  /** Default delivery mode. */
  private int deliveryMode = DeliveryMode.PERSISTENT;
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
  

  /**
   * Constructs a producer.
   *
   * @param sess  The session the producer belongs to.
   * @param dest  The destination the producer sends messages to.
   *
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageProducer(Session sess, Destination dest) throws JMSException
    {
      this.sess = sess;
      this.dest = dest;

      if (dest == null)
        identified = false;

      sess.producers.addElement(this);

      if (JoramTracing.dbgClient)
        JoramTracing.log(JoramTracing.DEBUG, this + ": created.");
    }

  /**
   * API method; not taken into account.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public void setDisableMessageID(boolean value) throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid delivery mode.
   */
  public void setDeliveryMode(int deliveryMode) throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      if (deliveryMode != DeliveryMode.PERSISTENT
          && deliveryMode != DeliveryMode.NON_PERSISTENT)
        throw new JMSException("Can't set invalid delivery mode.");

      this.deliveryMode = deliveryMode;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   * @exception JMSException  When setting an invalid priority.
   */
  public void setPriority(int priority) throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      if (priority < 0 || priority > 9)
        throw new JMSException("Can't set invalid priority value.");

      this.priority = priority;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public void setTimeToLive(long timeToLive) throws JMSException
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
  public void setDisableMessageTimestamp(boolean value) throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      this.timestampDisabled = value;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public Destination getDestination() throws JMSException
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
  public boolean getDisableMessageID() throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      return messageIDDisabled;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public int getDeliveryMode() throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      return deliveryMode;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public int getPriority() throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      return priority;
    }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the producer is closed.
   */
  public long getTimeToLive() throws JMSException
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
  public boolean getDisableMessageTimestamp() throws JMSException
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
  public void send(Message message) throws JMSException
    {
      if (! identified)
        throw new RuntimeException("Can't send message to"
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
  public void send(Message message, int deliveryMode,
                   int priority, long timeToLive) throws JMSException
    {
      if (! identified)
        throw new RuntimeException("Can't send message to"
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
  public void send(Destination dest,
                   Message message) throws JMSException
    {
      if (identified)
        throw new RuntimeException("An unidentified message"
                                   + " producer can't use this"
                                   + " identified message"
                                   + " producer.");
      if (dest == null)
        throw new RuntimeException("Can't send message to"
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
  public void send(Destination dest, Message message,
                   int deliveryMode, int priority,
                   long timeToLive) throws JMSException
    {
      if (identified)
        throw new RuntimeException("An unidentified message"
                                   + " producer can't use this"
                                   + " identified message"
                                   + " producer.");
      if (dest == null)
        throw new RuntimeException("Can't send message to"
                                   + " an unidentified"
                                   + " destination.");

      doSend((Destination) dest, message, deliveryMode, priority, timeToLive);
    }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
    {
      // Ignoring call if producer is already closed:
      if (closed)
        return;

      if (JoramTracing.dbgClient)
        JoramTracing.log(JoramTracing.DEBUG, "--- " + this
                         + ": closing...");

      sess.producers.removeElement(this);
      closed = true;

      if (JoramTracing.dbgClient)
        JoramTracing.log(JoramTracing.DEBUG, this + ": closed.");

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
  private void doSend(Destination dest, Message message,
                      int deliveryMode, int priority,
                      long timeToLive) throws JMSException
    {
      if (closed)
        throw new IllegalStateException("Forbidden call on a closed producer.");

      if (JoramTracing.dbgClient)
        JoramTracing.log(JoramTracing.DEBUG, "--- " + this
                         + ": producing...");

      // Updating the message property fields:
      String msgID = sess.cnx.nextMessageId();
      message.setJMSMessageID(msgID);
      message.setJMSDeliveryMode(deliveryMode);
      message.setJMSDestination(dest);
      if (timeToLive == 0)
        message.setJMSExpiration(0);
      else
        message.setJMSExpiration(System.currentTimeMillis() + timeToLive);
      message.setJMSPriority(priority);
      if (! timestampDisabled)
        message.setJMSTimestamp(System.currentTimeMillis());

      com.scalagent.kjoram.messages.Message momMsg = null;
      // If the message to send is a proprietary one, getting the MOM message
      // it wraps:
      if (message instanceof Message)
        momMsg = ((Message) message).getMomMessage();

      // If the message to send is a non proprietary JMS message, building
      // a proprietary message and then getting the MOM message it wraps:
      else if (message instanceof Message) {
        try {
          Message joramMessage = Message.convertJMSMessage(message);
          momMsg = joramMessage.getMomMessage();
        }
        catch (JMSException jE) {
          MessageFormatException mE = new MessageFormatException("Message to"
                                                                 + " send is"
                                                                 + " invalid.");
          mE.setLinkedException(jE);
          throw mE;
        }
      }
      else {
        MessageFormatException mE = new MessageFormatException("Message to"
                                                               + " send is"
                                                               + " invalid.");
        throw mE;
      }

      // If the session is transacted, keeping the request for later delivery:
      if (sess.transacted) {
        if (JoramTracing.dbgClient)
          JoramTracing.log(JoramTracing.DEBUG, "Buffering the message.");

        sess.prepareSend(dest,
                         (com.scalagent.kjoram.messages.Message) momMsg.clone());
      }
      // If not, building a new request and sending it:
      else {
      ProducerMessages pM = new ProducerMessages(dest.getName(), momMsg);

      if (JoramTracing.dbgClient)
        JoramTracing.log(JoramTracing.DEBUG, "Sending " + momMsg);
      
        sess.cnx.syncRequest(pM);
      }
    }
}
