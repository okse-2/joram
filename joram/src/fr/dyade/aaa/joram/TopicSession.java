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

import fr.dyade.aaa.mom.*;
import fr.dyade.aaa.mom.jms.*;

import java.util.*;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.TopicSession</code> interface.
 */
public class TopicSession extends Session implements javax.jms.TopicSession
{
  /** Counter of non durable subscriptions. */
  private int subsCounter = 0;

  /**
   * Constructs a topic session.
   *
   * @param ident  The identifier of the session.
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  TopicSession(String ident, TopicConnection cnx, boolean transacted,
               int acknowledgeMode) throws JMSException
  {
    super(ident, cnx, transacted, acknowledgeMode);
  }


  /** Returns a String image of this session. */
  public String toString()
  {
    return "TopicSess:" + ident;
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a WRITER on the
   *              topic.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicPublisher
       createPublisher(javax.jms.Topic topic) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicPublisher(this, (Topic) topic);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
       createSubscriber(javax.jms.Topic topic, String selector,
                        boolean noLocal) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, nextSubName(),
                               selector, noLocal, false);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
       createSubscriber(javax.jms.Topic topic) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, nextSubName(),
                               null, false, false);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
       createDurableSubscriber(javax.jms.Topic topic, String name,
                               String selector,
                               boolean noLocal) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, name, selector,
                               noLocal, true);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
       createDurableSubscriber(javax.jms.Topic topic, String name)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, name, null, false, true);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void unsubscribe(String name) throws JMSException
  {
    TopicSubscriber sub;
    for (int i = 0; i < consumers.size(); i++) {
      sub = (TopicSubscriber) consumers.get(i);
      if (sub.name.equals(name))
        throw new JMSException("Can't delete durable subscription " + name
                               + " as long as an active subscriber exists.");
    }
    cnx.syncRequest(new TSessUnsubRequest(name));
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception InvalidDestinationException  If the topic does not exist.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Topic createTopic(String topicName) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    SessCreateDestRequest req = new SessCreateDestRequest(topicName);
    SessCreateDestReply rep = (SessCreateDestReply) cnx.syncRequest(req);
    return new Topic(topicName);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    SessCreateTDReply reply =
      (SessCreateTDReply) cnx.syncRequest(new SessCreateTTRequest());
    String tempDest = reply.getAgentId();
    return new TemporaryTopic(tempDest, cnx);
  }


  /**
   * Specializes this method called by an application server to the PubSub
   * mode.
   */
  public synchronized void run()
  {
    int load = repliesIn.size();
    fr.dyade.aaa.mom.messages.Message momMsg;
    String msgId;
    String subName = ((TopicConnectionConsumer) connectionConsumer).subName;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "-- " + this
                                 + ": loaded with " + load
                                 + " message(s) and started.");
    try { 
      // Processing the current number of messages in the queue:
      for (int i = 0; i < load; i++) {
        momMsg = (fr.dyade.aaa.mom.messages.Message) repliesIn.pop();
        msgId = momMsg.getIdentifier();

        // If no message listener has been set for the session, denying the
        // processed message:
        if (messageListener == null) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(BasicLevel.ERROR, this + ": an"
                                       + " asynchronous delivery arrived for"
                                       + " a non existing session listener:"
                                       + " denying the message.");
     
          cnx.asyncRequest(new TSubDenyRequest(subName, msgId));
        }
        // Else:
        else {
          // Preparing ack for manual sessions:
          if (! autoAck)
            prepareAck(subName, msgId);
  
          // Passing the current message:
          try {
            messageListener.onMessage(Message.wrapMomMessage(this, momMsg));
  
            // Auto ack: acknowledging the message:
            if (autoAck)
              cnx.asyncRequest(new TSubAckRequest(subName, msgId));
          }
          // Catching a JMSException means that the building of the Joram
          // message went wrong: denying the message:
          catch (JMSException jE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": error while processing the"
                                         + " received message: " + jE);
            cnx.asyncRequest(new TSubDenyRequest(subName, msgId));
          }
          // Catching a RuntimeException means that the client onMessage() code
          // is incorrect; denying the message in auto ack mode:
          catch (RuntimeException rE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": RuntimeException thrown"
                                         + " by the listener: " + rE);
            if (autoAck)
              cnx.asyncRequest(new TSubDenyRequest(subName, msgId));
          }
        }
      }
    }
    catch (IllegalStateException isE) {}
  }

  
  /**
   * Specializes this method actually acknowledging the received messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  void acknowledge() throws IllegalStateException
  { 
    String sub;
    Vector ids;

    Enumeration subs = deliveries.keys();
    while (subs.hasMoreElements()) {
      sub = (String) subs.nextElement();
      ids = (Vector) deliveries.remove(sub);
      cnx.asyncRequest(new TSessAckRequest(sub, ids));
    }
  }

  /** Actually denies the received messages. */
  void deny()
  { 
    try {
      String sub;
      Vector ids;

      Enumeration subs = deliveries.keys();
      while (subs.hasMoreElements()) {
        sub = (String) subs.nextElement();
        ids = (Vector) deliveries.remove(sub);
        cnx.asyncRequest(new TSessDenyRequest(sub, ids));
      }
    }
    catch (IllegalStateException isE) {}
  }

  /**
   * Specializes this method called by the session daemon for passing an
   * asynchronous messages delivery to the appropriate subscriber.
   */
  void distribute(AbstractJmsReply asyncReply)
  {
    // Getting the delivery:
    SubMessages reply = (SubMessages) asyncReply;
    Vector messages = reply.getMessages();
    fr.dyade.aaa.mom.messages.Message momMsg;

    // Getting the subscriber:
    TopicSubscriber sub =
      (TopicSubscriber) cnx.requestsTable.get(asyncReply.getCorrelationId());

    // The target subscriber of the received message may be null if it has
    // been closed without having stopped the connection: denying has been
    // done on the server side when receiving the "close" request.
    if (sub != null) {
      // Passing the messages one by one:
      for (int i = 0; i < messages.size(); i++) {
        momMsg = (fr.dyade.aaa.mom.messages.Message) messages.get(i);
        sub.onMessage(momMsg);
      }
    }
  }

  /** Returns the next temporary subscription name. */
  private String nextSubName()
  {
    if (subsCounter == Integer.MAX_VALUE)
      subsCounter = 0;
    subsCounter++;

    return ident + ":" + subsCounter;
  }
}
