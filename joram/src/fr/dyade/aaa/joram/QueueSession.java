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

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.QueueSession</code> interface.
 */
public class QueueSession extends Session implements javax.jms.QueueSession
{
  /** The vector of queue browsers of this session. */
  Vector browsers;

  /**
   * Constructs a queue session.
   *
   * @param ident  The identifier of the session.
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  QueueSession(String ident, Connection cnx, boolean transacted,
               int acknowledgeMode) throws JMSException
  {
    super(ident, cnx, transacted, acknowledgeMode);
    browsers = new Vector();
  }

  /** Returns a String image of this session. */
  public String toString()
  {
    return "QueueSess:" + ident;
  }


  /**
   * API method
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.QueueBrowser
       createBrowser(javax.jms.Queue queue, String selector)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueBrowser(this, (Queue) queue, selector);
  }

  /**
   * API method
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueBrowser(this, (Queue) queue, null);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a WRITER on the
   *              queue.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.QueueSender createSender(javax.jms.Queue queue)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueSender(this, (Queue) queue);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              queue.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.QueueReceiver
       createReceiver(javax.jms.Queue queue, String selector)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueReceiver(this, (Queue) queue, selector);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              queue.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueReceiver(this, (Queue) queue, null);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception InvalidDestinationException  If the queue does not exist.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.Queue createQueue(String queueName) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    SessCreateDestRequest req = new SessCreateDestRequest(queueName);
    SessCreateDestReply rep = (SessCreateDestReply) cnx.syncRequest(req);
    return new Queue(queueName);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public javax.jms.TemporaryQueue createTemporaryQueue() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    SessCreateTDReply reply =
      (SessCreateTDReply) cnx.syncRequest(new SessCreateTQRequest());
    String tempDest = reply.getAgentId();
    return new TemporaryQueue(tempDest, cnx);
  }


  /**
   * Specializes this method called by an application server to the PTP
   * mode.
   */
  public synchronized void run()
  {
    int load = repliesIn.size();
    fr.dyade.aaa.mom.messages.Message momMsg;
    String msgId;
    String queueName = connectionConsumer.destName;

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
     
          cnx.syncRequest(new QRecDenyRequest(queueName, msgId));
        }
        // Else:
        else {
          // Preparing ack for manual sessions:
          if (! autoAck)
            prepareAck(queueName, msgId);
  
          // Passing the current message:
          try {
            messageListener.onMessage(Message.wrapMomMessage(this, momMsg));
  
            // Auto ack: acknowledging the message:
            if (autoAck)
              cnx.asyncRequest(new QRecAckRequest(queueName, msgId));
          }
          // Catching a JMSException means that the building of the Joram
          // message went wrong: denying the message:
          catch (JMSException jE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": error while processing the"
                                         + " received message: " + jE);
            cnx.syncRequest(new QRecDenyRequest(queueName, msgId));
          }
          // Catching a RuntimeException means that the client onMessage() code
          // is incorrect; denying the message in auto ack mode:
          catch (RuntimeException rE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": RuntimeException thrown"
                                         + " by the listener: " + rE);
            if (autoAck)
              cnx.syncRequest(new QRecDenyRequest(queueName, msgId));
          }
        }
      }
    }
    catch (JMSException e) {}
  }


  /** Specializes this API method to the PTP mode for closing browsers. */
  public synchronized void close() throws JMSException
  {
    while (! browsers.isEmpty())
      ((QueueBrowser) browsers.get(0)).close();

    super.close();
  }


  /**
   * Specializes this method actually acknowledging the received messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  void acknowledge() throws IllegalStateException
  { 
    String dest;
    Vector ids;

    Enumeration dests = deliveries.keys();
    while (dests.hasMoreElements()) {
      dest = (String) dests.nextElement();
      ids = (Vector) deliveries.remove(dest);
      cnx.asyncRequest(new QSessAckRequest(dest, ids));
    }
  }

  /** Actually denies the received messages. */
  void deny()
  {
    try {
      String dest;
      Vector ids;

      Enumeration dests = deliveries.keys();
      while (dests.hasMoreElements()) {
        dest = (String) dests.nextElement();
        ids = (Vector) deliveries.remove(dest);
        cnx.syncRequest(new QSessDenyRequest(dest, ids));
      }
    }
    catch (JMSException jE) {}
  }

  /**
   * Specializes this method called by the session daemon for passing an
   * asynchronous message delivery to the appropriate receiver.
   */
  void distribute(AbstractJmsReply asyncReply)
  {
    // Getting the message:
    QueueMessage reply = (QueueMessage) asyncReply;
    fr.dyade.aaa.mom.messages.Message momMsg = reply.getMessage();

    // Getting the consumer:
    QueueReceiver rec =
      (QueueReceiver) cnx.requestsTable.remove(reply.getCorrelationId());

    // The target receiver of the received message may be null if it has
    // been closed without having stopped the connection: denying the msg:
    if (rec == null) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": an asynchronous"
                                   + " delivery arrived for an improperly"
                                   + " closed receiver: denying the"
                                   + " message.");
     
      try { 
        cnx.syncRequest(new QRecDenyRequest(momMsg.getDestination().getName(),
                                            momMsg.getIdentifier()));
      }
      catch (JMSException jE) {}
    }
    // Passing the message:
    else
      rec.onMessage(momMsg);
  }
}
