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
import fr.dyade.aaa.util.TimerTask;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.TransactionRolledBackException;
import javax.jms.IllegalStateException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Session</code> interface.
 */
public class Session implements javax.jms.Session
{
  /** Task actually terminating pending transactions. */
  private TimerTask transactask = null;

  /** The message listener of the session, if any. */
  protected javax.jms.MessageListener messageListener = null;

  /** The identifier of the session. */
  String ident;
  /** The connection the session belongs to. */
  Connection cnx;
  /** <code>true</code> if the session is transacted. */
  boolean transacted;
  /** The acknowledgement mode of the session. */
  int acknowledgeMode;
  /** <code>true</code> if the session is closed. */
  boolean closed = false;
  /** <code>true</code> if the session is started. */
  boolean started = false;

  /** <code>true</code> if the session's acknowledgements are automatic. */
  boolean autoAck;

  /** Vector of message consumers. */
  Vector consumers;
  /** Vector of message producers. */
  Vector producers;
  /** Vector of queue browsers. */
  Vector browsers;
  /** FIFO queue holding the asynchronous server deliveries. */
  fr.dyade.aaa.util.Queue repliesIn;
  /** Daemon distributing asynchronous server deliveries. */
  SessionDaemon daemon = null;
  /** Counter of message listeners. */
  int msgListeners = 0;
  /** 
   * Table holding the <code>ProducerMessages</code> holding producers'
   * messages and destinated to be sent at commit.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> <code>ProducerMessages</code>
   */
  Hashtable sendings;
  /** 
   * Table holding the identifiers of the messages delivered per
   * destination or subscription, and not acknowledged.
   * <p>
   * <b>Key:</b> destination or subscription name<br>
   * <b>Object:</b> <code>MessageAcks</code> instance
   */
  Hashtable deliveries;

  /** The connection consumer delivering messages to the session, if any. */
  ConnectionConsumer connectionConsumer = null;


  /**
   * Opens a session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  Session(Connection cnx, boolean transacted,
          int acknowledgeMode) throws JMSException
  {
    if (! transacted 
        && acknowledgeMode != javax.jms.Session.AUTO_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.DUPS_OK_ACKNOWLEDGE)
      throw new JMSException("Can't create a non transacted session with an"
                             + " invalid acknowledge mode.");

    this.ident = cnx.nextSessionId();
    this.cnx = cnx;
    this.transacted = transacted;
    this.acknowledgeMode = acknowledgeMode;

    autoAck = ! transacted
              && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE;

    consumers = new Vector();
    producers = new Vector();
    browsers = new Vector();
    repliesIn = new fr.dyade.aaa.util.Queue();
    sendings = new Hashtable();
    deliveries = new Hashtable();

    cnx.sessions.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a String image of this session. */
  public String toString()
  {
    return "Sess:" + ident;
  }


  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getAcknowledgeMode() throws JMSException
  {
    return acknowledgeMode;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public boolean getTransacted() throws JMSException
  {
    return transacted;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
              throws JMSException
  {
    this.messageListener = messageListener;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public javax.jms.MessageListener getMessageListener() throws JMSException
  {
    return messageListener;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.Message createMessage() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new Message(this);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.TextMessage createTextMessage() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new TextMessage(this);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.TextMessage createTextMessage(String text)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new TextMessage(this, text);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.BytesMessage createBytesMessage()
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new BytesMessage(this);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.MapMessage createMapMessage()
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new MapMessage(this);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.ObjectMessage createObjectMessage()
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new ObjectMessage(this);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.ObjectMessage createObjectMessage(java.io.Serializable obj)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new ObjectMessage(this, obj);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.StreamMessage createStreamMessage()
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    
    return new StreamMessage(this);
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
   *              destination.
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageProducer createProducer(javax.jms.Destination dest)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new MessageProducer(this, (Destination) dest);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              destination.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector,
                        boolean noLocal) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new MessageConsumer(this, (Destination) dest, selector, null,
                               noLocal);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              destination.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new MessageConsumer(this, (Destination) dest, selector);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the client is not a READER on the
   *              destination.
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new MessageConsumer(this, (Destination) dest, null);
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

    return new TopicSubscriber(this, (Topic) topic, name, selector, noLocal);
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

    return new TopicSubscriber(this, (Topic) topic, name, null, false);
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

  /** API method. */
  public synchronized void run()
  {
    int load = repliesIn.size();
    fr.dyade.aaa.mom.messages.Message momMsg;
    String msgId;
    String targetName = connectionConsumer.targetName;
    boolean queueMode = connectionConsumer.queueMode;

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
   
          if (queueMode) 
            cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId, true));
          else
            cnx.asyncRequest(new ConsumerDenyRequest(targetName, msgId,false));
        }
        // Else:
        else {
          // Preparing ack for manual sessions:
          if (! autoAck)
            prepareAck(targetName, msgId, queueMode);
  
          // Passing the current message:
          try {
            messageListener.onMessage(Message.wrapMomMessage(this, momMsg));
  
            // Auto ack: acknowledging the message:
            if (autoAck)
              cnx.asyncRequest(new ConsumerAckRequest(targetName, msgId,
                                                      queueMode));
          }
          // Catching a JMSException means that the building of the Joram
          // message went wrong: denying the message:
          catch (JMSException jE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": error while processing the"
                                         + " received message: " + jE);
            if (queueMode)
              cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                      queueMode));
            else
              cnx.asyncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                       queueMode));
          }
          // Catching a RuntimeException means that the client onMessage() code
          // is incorrect; denying the message if needed:
          catch (RuntimeException rE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
              JoramTracing.dbgClient.log(BasicLevel.ERROR, this
                                         + ": RuntimeException thrown"
                                         + " by the listener: " + rE);
            if (autoAck && queueMode)
              cnx.syncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                      queueMode));
            else if (autoAck && ! queueMode)
              cnx.asyncRequest(new ConsumerDenyRequest(targetName, msgId,
                                                       queueMode));
          }
        }
      }
    }
    catch (JMSException e) {}
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted, or if the connection is broken.
   */
  public void commit() throws JMSException
  {
    if (! transacted)
      throw new IllegalStateException("Can't commit a non transacted"
                                      + " session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": committing...");

    if (transactask != null) {
      transactask.cancel();
      transactask = null;
    }

    // Sending client messages:
    try {
      Enumeration dests = sendings.keys();
      String dest;
      ProducerMessages pM;
      while (dests.hasMoreElements()) {
        dest = (String) dests.nextElement();
        pM = (ProducerMessages) sendings.remove(dest);
        cnx.syncRequest(pM);
      }
      // Acknowledging the received messages:
      acknowledge();

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": committed.");
    }
    // Catching an exception if the sendings or acknowledgement went wrong:
    catch (JMSException jE) {
      TransactionRolledBackException tE = 
        new TransactionRolledBackException("A JMSException was thrown during"
                                           + " the commit.");
      tE.setLinkedException(jE);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, "Exception: " + tE);

      rollback();
      throw tE;
    }
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or not
   *              transacted.
   */
  public void rollback() throws JMSException
  {
    if (! transacted)
      throw new IllegalStateException("Can't rollback a non transacted"
                                      + " session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": rolling back...");

    if (transactask != null) {
      transactask.cancel();
      transactask = null;
    }

    // Denying the received messages:
    deny();
    // Deleting the produced messages:
    sendings.clear();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": rolled back.");
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the session is closed, or transacted.
   */
  public void recover() throws JMSException
  {
    if (transacted)
      throw new IllegalStateException("Can't recover a transacted session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + " recovering...");

    // Stopping the session, denying the received messages:
    stop();
    deny();
    // Re-starting the session:
    start();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": recovered.");
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
    MessageConsumer cons;
    for (int i = 0; i < consumers.size(); i++) {
      cons = (MessageConsumer) consumers.get(i);
      if (! cons.queueMode && cons.targetName.equals(name))
        throw new JMSException("Can't delete durable subscription " + name
                               + " as long as an active subscriber exists.");
    }
    cnx.syncRequest(new ConsumerUnsubRequest(name));
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void close() throws JMSException
  {
    // Ignoring the call if the session is already closed:
    if (closed)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": closing..."); 

    while (! browsers.isEmpty())
      ((QueueBrowser) browsers.get(0)).close();

    // Emptying the current pending deliveries:
    try {
      repliesIn.stop();
    }
    catch (InterruptedException iE) {}

    // Stopping the session:
    stop();

    // Denying the non acknowledged messages:
    if (transacted)
      rollback();
    else
      deny();
      
    // Closing the session's resources:
    while (! consumers.isEmpty())
      ((MessageConsumer) consumers.get(0)).close();
    while (! producers.isEmpty())
      ((MessageProducer) producers.get(0)).close();

    cnx.sessions.remove(this);
    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed."); 
  }

  
  /**
   * Starts the asynchronous deliveries in the session.
   * <p>
   * This method is called either by a consumer when setting the first
   * message listener of the session, if the connection is started, or
   * by the starting connection if at least one listener has previously
   * been set by a consumer.
   * <p>
   * It creates and starts a daemon dedicated to distributing the
   * asynchronous deliveries arriving on the connection to their consumers.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  void start() throws IllegalStateException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": starting...");

    repliesIn.start();

    // Starting the daemon if needed:
    if (! started && msgListeners > 0) {
      daemon = new SessionDaemon(this);
      daemon.setDaemon(true);
      daemon.start();
    }
    started = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": started.");
  }

  /**
   * Stops the asynchronous deliveries processing in the session.
   * <p>
   * This method must be carefully used. When the session is stopped, the
   * connection might very well going on pushing deliveries in the
   * session's queue. If the session is never re-started, these deliveries
   * will never be poped out, and this may lead to a situation of consumed
   * but never acknowledged messages.
   * <p>
   * This fatal situation never occurs as the <code>stop()</code> method is
   * either called by the <code>recover()</code> method, which then calls
   * the <code>start()</code> method, or by the <code>Session.close()</code>
   * and <code>Connection.stop()</code> methods, which first empty the
   * session's deliveries and forbid any further push.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  void stop() throws IllegalStateException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    // Ignoring the call if the session is already stopped:
    if (! started)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": stopping...");

    // Stopping the daemon if needed:
    if (daemon != null) {
      daemon.stop();
      daemon = null;
    }
    // Synchronizing the stop() with the consumers:
    if (consumers != null) {
      MessageConsumer consumer; 
      for (int i = 0; i < consumers.size(); i++) {
        consumer = (MessageConsumer) consumers.get(i);
        consumer.syncro();
      }
    }

    started = false;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": stopped.");
  }

  /** 
   * Method called by message producers when producing a message for
   * preparing the session to later commit it.
   *
   * @param name  Name of the destination the message is destinated to.
   * @param msg  The message.
   */
  void prepareSend(String name, fr.dyade.aaa.mom.messages.Message msg)
  {
    boolean rearmTimer = false;
    if (transactask != null) {
      transactask.cancel();
      rearmTimer = true;
    }

    ProducerMessages pM = (ProducerMessages) sendings.get(name);
    if (pM == null) {
      pM = new ProducerMessages(name);
      sendings.put(name, pM);
    }
    pM.addMessage(msg);

    try {
      if (rearmTimer) {
        transactask = new TxTimerTask(this);
        cnx.transactimer.schedule(transactask,
                                  cnx.factoryConfiguration.txTimer * 1000);
      }
    }
    catch (Exception e) {}
  }

  /** 
   * Method called by message consumers when receiving a message for
   * preparing the session to later acknowledge or deny it.
   *
   * @param name  Name of the destination or of the proxy subscription 
   *          the message comes from.
   * @param id  Identifier of the consumed message.
   * @param queueMode  <code>true</code> if the message consumed comes from
   *          a queue.
   */
  void prepareAck(String name, String id, boolean queueMode)
  {
    if (transactask != null) 
      transactask.cancel();

    MessageAcks acks = (MessageAcks) deliveries.get(name);
    if (acks == null) {
      acks = new MessageAcks(queueMode);
      deliveries.put(name, acks);
    }
    acks.addId(id);

    try {
      if (cnx.transactimer != null) {
        transactask = new TxTimerTask(this);
        cnx.transactimer.schedule(transactask,
                                  cnx.factoryConfiguration.txTimer * 1000);
      }
    }
    catch (Exception e) {}
  }

  /**
   * Method acknowledging the received messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  void acknowledge() throws IllegalStateException
  { 
    String target;
    MessageAcks acks;

    Enumeration targets = deliveries.keys();
    while (targets.hasMoreElements()) {
      target = (String) targets.nextElement();
      acks = (MessageAcks) deliveries.remove(target);
      cnx.asyncRequest(new SessAckRequest(target, acks.getIds(),
                                          acks.getQueueMode()));
    }
  }

  /** Method denying the received messages. */
  void deny()
  {
    try {
      String target;
      MessageAcks acks;
      SessDenyRequest deny;

      Enumeration targets = deliveries.keys();
      while (targets.hasMoreElements()) {
        target = (String) targets.nextElement();
        acks = (MessageAcks) deliveries.remove(target);
        deny = new SessDenyRequest(target, acks.getIds(), acks.getQueueMode());
        if (acks.getQueueMode())
          cnx.syncRequest(deny);
        else
          cnx.asyncRequest(deny);
      }
    }
    catch (JMSException jE) {}
  }

  /**
   * Method called by the session daemon for passing an
   * asynchronous message delivery to the appropriate consumer.
   */
  void distribute(AbstractJmsReply asyncReply)
  {
    // Getting the message:
    ConsumerMessages reply = (ConsumerMessages) asyncReply;
    Vector messages;

    messages = reply.getMessages();

    // Getting the consumer:
    MessageConsumer cons = null;
    if (reply.getQueueMode()) {
      cons =
        (MessageConsumer) cnx.requestsTable.remove(reply.getCorrelationId());
    }
    else
      cons = (MessageConsumer) cnx.requestsTable.get(reply.getCorrelationId());

    // Passing the message(s) to the consumer:
    if (cons != null) {
      fr.dyade.aaa.mom.messages.Message momMsg;
      for (int i = 0; i < messages.size(); i++) {
        momMsg = (fr.dyade.aaa.mom.messages.Message) messages.get(i);
        cons.onMessage(momMsg);
      }
    }
    // The target consumer of the received message may be null if it has
    // been closed without having stopped the connection: denying the
    // deliveries.
    else {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": an asynchronous"
                                   + " delivery arrived for an improperly"
                                   + " closed consumer: denying the"
                                   + " messages.");

      Vector msgs = reply.getMessages();
      
      if (msgs.isEmpty())
        return;

      Vector ids = new Vector();
      fr.dyade.aaa.mom.messages.Message msg;
      while (! msgs.isEmpty()) {
        msg = (fr.dyade.aaa.mom.messages.Message) msgs.remove(0);
        ids.addElement(msg.getIdentifier());
      }
  
      try { 
        cnx.asyncRequest(new SessDenyRequest(reply.comesFrom(), ids,
                                             reply.getQueueMode(), true));
      }
      catch (JMSException jE) {}
    }
  }
}
