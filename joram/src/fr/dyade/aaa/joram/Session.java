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

import java.util.*;

import javax.jms.JMSException;
import javax.jms.TransactionRolledBackException;
import javax.jms.IllegalStateException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Session</code> interface.
 */
public abstract class Session implements javax.jms.Session
{
  /** The message listener of the session, if any. */
  protected javax.jms.MessageListener messageListener = null;
  /** The connection consumer delivering messages to the session, if any. */
  ConnectionConsumer connectionConsumer = null;

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
   * <b>Object:</b> vector of message identifiers
   */
  Hashtable deliveries;

  /** Timer for terminating pending transactions. */
  private Timer transactimer = null;


  /**
   * Opens a session.
   *
   * @param ident  The identifier of the session.
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  Session(String ident, Connection cnx, boolean transacted,
          int acknowledgeMode) throws JMSException
  {
    if (! transacted 
        && acknowledgeMode != javax.jms.Session.AUTO_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE
        && acknowledgeMode != javax.jms.Session.DUPS_OK_ACKNOWLEDGE)
      throw new JMSException("Can't create a non transacted session with an"
                             + " invalid acknowledge mode.");
    this.ident = ident;
    this.cnx = cnx;
    this.transacted = transacted;
    this.acknowledgeMode = acknowledgeMode;

    autoAck = ! transacted
              && acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE;

    consumers = new Vector();
    producers = new Vector();
    repliesIn = new fr.dyade.aaa.util.Queue();
    sendings = new Hashtable();
    deliveries = new Hashtable();

    cnx.sessions.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
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

    if (transactimer != null) {
      transactimer.cancel();
      transactimer = null;
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

    if (transactimer != null) {
      transactimer.cancel();
      transactimer = null;
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
   * This abstract API method purpose is to force the <code>QueueSession</code>
   * and <code>TopicSession</code> classes to specifically manage asynchronous
   * deliveries passed to the session by a connection consumer.
   */
  public abstract void run();


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
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": starting...");

    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

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
   * Stops the asynchronous deliveries in the session.
   * <p>
   * This method is called by the session's connection when it is actually
   * stopping. The connection calls it when the session finished to distribute
   * the asynchronous deliveries it received before the connection stopped. 
   * <p>
   * It allows the connection to synchronize its stop with any pending
   * synchronous receive or current listener message processing.
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
    if (transactimer != null) {
      transactimer.cancel();
      rearmTimer = true;
    }

    ProducerMessages pM = (ProducerMessages) sendings.get(name);
    if (pM == null) {
      pM = new ProducerMessages(name);
      sendings.put(name, pM);
    }
    pM.addMessage(msg);

    if (rearmTimer) {
      transactimer = new Timer();
      transactimer.schedule(new TxTimerTask(this), cnx.factory.txTimer * 1000);
    }
  }

  /** 
   * Method called by message consumers when receiving a message for
   * preparing the session to later acknowledge or deny it.
   *
   * @param name  Name of the destination or of the proxy subscription 
   *          the message comes from.
   * @param id  Identifier of the consumed message.
   */
  void prepareAck(String name, String id)
  {
    if (transactimer != null) 
      transactimer.cancel();

    Vector ids = (Vector) deliveries.get(name);
    if (ids == null) {
      ids = new Vector();
      deliveries.put(name, ids);
    }
    ids.add(id);

    if (cnx.factory.txTimer != 0) {
      transactimer = new Timer();
      transactimer.schedule(new TxTimerTask(this), cnx.factory.txTimer * 1000);
    }
  }

  /**
   * This abstract method purpose is to force the <code>QueueSession</code>
   * and <code>TopicSession</code> classes to specifically take care of
   * acknowledging the consumed messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  abstract void acknowledge() throws IllegalStateException;

  /**
   * This abstract method purpose is to force the <code>QueueSession</code>
   * and <code>TopicSession</code> classes to specifically take care of
   * denying the consumed messages.
   */
  abstract void deny();

  /**
   * This abstract method purpose is to force the <code>QueueSession</code>
   * and <code>TopicSession</code> classes to specifically take care of
   * distributing the asynchronous deliveries destinated to their consumers.
   */
  abstract void distribute(AbstractJmsReply reply);
}
