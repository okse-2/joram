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

import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.TopicSubscriber</code> interface.
 */
public class TopicSubscriber extends MessageConsumer
                             implements javax.jms.TopicSubscriber
{
  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * published by the same connection.
   */
  private boolean noLocal;
  /** <code>true</code> if the subscription is durable. */
  private boolean durable;

  /** The subscription's name. */
  String name;
  
  /**
   * Constructs a subscriber.
   *
   * @param sess  The session the subscriber belongs to.
   * @param topic  The topic the subscriber subscribes to.
   * @param name  The subscription name.
   * @param selector  The selector for filtering messages.
   * @param noLocal <code>true</code> if the subscriber does not wish to
   *          consume messages published through the same connection.
   * @param durable  <code>true</code> if the subscription is durable.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  TopicSubscriber(TopicSession sess, Topic topic, String name, String selector,
                  boolean noLocal, boolean durable) throws JMSException
  {
    super(sess, topic, selector);
    this.noLocal = noLocal;
    this.durable = durable;
    this.name = name;

    sess.cnx.syncRequest(new TSessSubRequest(destName, name, selector,
                                             noLocal, durable));
  }

   /** Returns a string view of this receiver. */
  public String toString()
  {
    return "TopicSub:" + name;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public boolean getNoLocal() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " subscriber.");
    return noLocal;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public javax.jms.Topic getTopic() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " subscriber.");
    return (Topic) dest;
  }

  /**
   * Specializes this API method to the PubSub mode.
   * <p>
   * This method must not be called if the connection the subscriber belongs to
   * is started, because the session would then be accessed by the thread
   * calling this method and by the thread controlling asynchronous deliveries.
   * This situation is clearly forbidden by the single threaded nature of
   * sessions. Moreover, unsetting a message listener without stopping the 
   * connection may lead to the situation where asynchronous deliveries would
   * arrive on the connection, the session or the subscriber without being
   * able to reach their target listener!
   *
   * @exception IllegalStateException  If the subscriber is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
            throws JMSException
  {
    // Getting the current listener:
    javax.jms.MessageListener previousML = super.messageListener;
    // Setting the new one:
    super.setMessageListener(messageListener);

    // If setting a new listener, sending a request to the proxy:
    if (messageListener != null && previousML == null) {
      pendingReq = new TSubSetListRequest(name);
      pendingReq.setIdentifier(sess.cnx.nextRequestId());
      sess.cnx.requestsTable.put(pendingReq.getRequestId(), this);
      sess.cnx.asyncRequest(pendingReq);
    }
    // If unsetting the listener, sending a request to the proxy:
    else if (messageListener == null && previousML != null) {
      sess.cnx.requestsTable.remove(pendingReq.getRequestId());
      pendingReq = null;
      TSubUnsetListRequest unsetLR = new TSubUnsetListRequest(name);
      sess.cnx.syncRequest(unsetLR);
    }
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": MessageListener"
                                 + " set.");
  }

  /**
   * Specializes this API method to the Pub/Sub mode.
   *
   * @exception IllegalStateException  If the receiver is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Message receive(long timeOut) throws JMSException
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": requests to receive a message.");
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed receiver.");
    
    if (listenerSet) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": invalid"
                                   + " call as a listener exists for"
                                   + " this subscriber.");
    }
    else if (sess.msgListeners > 0) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgClient.log(BasicLevel.WARN, "Improper call as"
                                   + " asynchronous consumers have already"
                                   + " been set on the session.");
    }

    // Sending a synchronous "receive" request and synchronizing with
    // a possible "close":
    synchronized(this) {
      pendingReq = new TSubReceiveRequest(name, timeOut);
      receiving = true;
    }
    // Expecting an answer:
    SubMessages reply = (SubMessages) sess.cnx.syncRequest(pendingReq);
    pendingReq = null;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": received a"
                                 + " reply.");
    
    // Processing the received reply and synchronizing with a possible
    // "close":
    synchronized(this) {
      receiving = false;
      if (reply.getMessages() != null) {
        String msgId = reply.getMessage().getIdentifier();
        // Auto ack: acknowledging the message:
        if (sess.autoAck)
          sess.cnx.asyncRequest(new TSubAckRequest(name, msgId));
        // Session ack: passing the id for later ack or deny:
        else
          sess.prepareAck(name, msgId);

        return Message.wrapMomMessage(sess, reply.getMessage());
      }
      else
        return null;
    }
  }

  /**
   * Specializes this API method to the Pub/Sub mode.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring call if the subscriber is already closed:
    if (closed)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closing...");

    // Synchronizing with a pending "receive" or "onMessage":
    super.syncro();

    // Unsetting the listener, if any:
    if (listenerSet) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Unsetting listener.");

      sess.cnx.requestsTable.remove(pendingReq.getRequestId());
      pendingReq = null;
      sess.cnx.syncRequest(new TSubUnsetListRequest(name));
    }
    // De-activating the subscription if durable:
    if (durable)
      sess.cnx.syncRequest(new TSubCloseRequest(name));
    // Unsubscribing if non durable:
    else
      sess.cnx.syncRequest(new TSessUnsubRequest(name));

    // In the case of a pending "receive" request, replying by a null to it:
    if (receiving) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Replying to the"
                                   + " pending receive "
                                   + pendingReq.getRequestId()
                                   + " with a null message.");

      sess.cnx.repliesTable.put(pendingReq.getRequestId(), new SubMessages());
      Object lock = sess.cnx.requestsTable.remove(pendingReq.getRequestId());
      synchronized(lock) {
        lock.notify();
      }
    }
    // Synchronizing again:
    super.syncro();

    super.close();
  }

  
  /**
   * Specializes this method called by the session daemon for passing an
   * asynchronous message delivery to the subcriber's listener.
   */
  synchronized void onMessage(fr.dyade.aaa.mom.messages.Message message)
  {
    String msgId = message.getIdentifier();

    try {
      // The target listener of the received message may be null if it has
      // been unset without having stopped the connection: denying the msg:
      if (messageListener == null) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": an"
                                     + " asynchronous delivery arrived"
                                     + " for an improperly unset listener:"
                                     + " denying the message.");
        sess.cnx.asyncRequest(new TSubDenyRequest(name, msgId));
      }
      else {
        // In session ack mode, preparing later ack or deny:
        if (! sess.autoAck)
          sess.prepareAck(name, msgId);

        try {
          messageListener.onMessage(Message.wrapMomMessage(sess, message));
          // Auto ack: acknowledging the message:
          if (sess.autoAck)
            sess.cnx.asyncRequest(new TSubAckRequest(name, msgId));
        }
        // Catching a JMSException means that the building of the Joram
        // message went wrong: denying the message:
        catch (JMSException jE) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                       + ": error while processing the"
                                       + " received message: " + jE);
          sess.cnx.asyncRequest(new TSubDenyRequest(name, msgId));
        } 
        // Catching a RuntimeException means that the client onMessage() code
        // is incorrect; denying as expected by the JMS spec:
        catch (RuntimeException rE) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                       + ": RuntimeException thrown"
                                       + " by the listener: " + rE);
          if (sess.autoAck)
            sess.cnx.asyncRequest(new TSubDenyRequest(name, msgId));
        }
      }
    }
    // Catching an IllegalStateException means that the acknowledgement or
    // denying went wrong because the connection has been lost. Nothing more
    // can be done here.
    catch (JMSException jE) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, this + ": " + jE);
    }
  }
}
