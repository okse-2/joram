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

import javax.jms.IllegalStateException;
import javax.jms.MessageFormatException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.MessageProducer</code> interface.
 */
public class MessageProducer implements javax.jms.MessageProducer
{
  /** <code>true</code> if the producer's destination is identified. */
  private boolean identified = true;

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
   * @exception JMSSecurityException  If the client is not a WRITER on the
   *              queue.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  MessageProducer(Session sess, Destination dest) throws JMSException
  {
    this.sess = sess;
    this.dest = dest;

    // Checking user's access permission:
    if (dest != null)
      sess.cnx.isWriter(dest.getName());
    else
      identified = false;

    sess.producers.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
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
    if (closed) {
      throw new IllegalStateException("Forbidden call on a closed producer.");
      }

    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT
        && deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT) {
        throw new JMSException("Can't set invalid delivery mode.");
    }
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
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring call if producer is already closed:
    if (closed)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": closing...");

    sess.producers.remove(this);
    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed.");

  }

  /**
   * Produces a message with the default delivery parameters.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  protected void produce(javax.jms.Message message) throws JMSException
  {
    if (! identified)
      throw new UnsupportedOperationException("Can't produce message to"
                                              + " an unidentified"
                                              + " destination.");
    // Actually producing it:
    doProduce(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * Produces a message with given delivery parameters.
   *
   * @exception UnsupportedOperationException  If the dest is unidentified.
   * @exception IllegalStateException  If the producer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  protected void produce(javax.jms.Message message, int deliveryMode,
                         int priority, long timeToLive) throws JMSException
  {
    if (! identified)
      throw new UnsupportedOperationException("Can't produce message to"
                                              + " an unidentified"
                                              + " destination.");
    // Actually producing it:
    doProduce(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * Produces a message with default delivery parameters for an unidentified 
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
  protected void produce(Destination dest,
                         javax.jms.Message message) throws JMSException
  {
    if (identified)
      throw new UnsupportedOperationException("An unidentified message"
                                              + " producer can't use this"
                                              + " identified message"
                                              + " producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't produce message to"
                                              + " an unidentified"
                                              + " destination.");
    // Checking user's access permission:
    sess.cnx.isWriter(dest.getName());

    doProduce(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * Produces a message with given delivery parameters for an unidentified
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
  protected void produce(Destination dest, javax.jms.Message message,
                         int deliveryMode, int priority,
                         long timeToLive) throws JMSException
  {
    if (identified)
      throw new UnsupportedOperationException("An unidentified message"
                                              + " producer can't use this"
                                              + " identified message"
                                              + " producer.");
    if (dest == null)
      throw new UnsupportedOperationException("Can't produce message to"
                                              + " an unidentified"
                                              + " destination.");
    // Checking user's access permission:
    sess.cnx.isWriter(dest.getName());

    doProduce(dest, message, deliveryMode, priority, timeToLive);
  }

  /**
   * Actually sends a message to a given destination.
   *
   * @exception MessageFormatException  If the message to produce is invalid.
   * @exception InvalidDestinationException  If the specified destination is
   *              invalid.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  private void doProduce(Destination dest, javax.jms.Message message,
                         int deliveryMode, int priority,
                         long timeToLive) throws JMSException
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": producing...");

    if (closed)
      throw new IllegalStateException("Forbidden call on a closed producer.");

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

    fr.dyade.aaa.mom.messages.Message momMsg = null;
    // If the message to produce is a proprietary one, getting the MOM message
    // it wraps:
    if (message instanceof fr.dyade.aaa.joram.Message)
      momMsg = ((Message) message).getMomMessage();

    // If the message to produce is a non proprietary JMS message, building
    // a proprietary message and then getting the MOM message it wraps:
    else if (message instanceof javax.jms.Message) {
      try {
        Message joramMessage = Message.convertJMSMessage(sess, message);
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
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Buffering the message.");

      sess.prepareSend(dest.getName(),
                       (fr.dyade.aaa.mom.messages.Message) momMsg.clone());
    }
    // If not, building a new request and sending it:
    else {
      ProducerMessages pM = new ProducerMessages(dest.getName());
      pM.addMessage(momMsg);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Sending "+ momMsg);
      
      sess.cnx.syncRequest(pM);
    }
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": produced msg: " 
                                 + msgID);
  }
}
