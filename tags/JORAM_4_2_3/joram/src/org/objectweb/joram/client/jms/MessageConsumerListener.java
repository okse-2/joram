/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.client.ConsumerSetListRequest;
import org.objectweb.joram.shared.client.ConsumerUnsetListRequest;
import org.objectweb.joram.client.jms.connection.ReplyListener;
import org.objectweb.joram.client.jms.connection.AbortedRequestException;

import fr.dyade.aaa.util.StoppedQueueException;

import javax.jms.MessageListener;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * This class listens to replies 
 * asynchronously returned by the user proxy for
 * a message consumer.
 */
class MessageConsumerListener implements ReplyListener {

  /**
   * Status of the message consumer listener.
   */
  private static class Status {
    public static final int INIT = 0;
    public static final int RUN = 1;
    public static final int ON_MSG = 2;
    public static final int CLOSE = 3;

    private static final String[] names = {
      "INIT", "RUN", "ON_MSG", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The message consumer listening to the replies.
   */
  private MessageConsumer consumer;

  /**
   * The session that owns the message consumer.
   */
  private Session session;

  /**
   * The identifier of the subscription request.
   */ 
  private int requestId;

  private int status;

  private MessageListener listener;
  
  MessageConsumerListener(MessageConsumer consumer,
                          Session session,
                          MessageListener listener) {
    this.consumer = consumer;
    this.session = session;
    this.listener = listener;
    requestId = -1;
    setStatus(Status.INIT);
  }

  private void setStatus(int status) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "MessageConsumerListener.setStatus(" +
        Status.toString(status) + ')');
    this.status = status;
  }

  /**
   * Called by Session.
   */
  synchronized void start() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "MessageConsumerListener.start()");
    if (status == Status.INIT) {
      subscribe();
      setStatus(Status.RUN);
    } else {
      // Should not happen
      throw new IllegalStateException("Status error");
    }
  }

  private void subscribe() throws JMSException {
    ConsumerSetListRequest req = 
      new ConsumerSetListRequest(
        consumer.targetName, 
        consumer.selector, 
        consumer.queueMode);
    session.getRequestMultiplexer().sendRequest(req, this);
    requestId = req.getRequestId();
  }

  /**
   * Called by Session.
   */
  void close() throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "MessageConsumerListener.close()");

    synchronized (this) {
      while (status == Status.ON_MSG) {
        try {
          // Wait for the message listener to return from 
          // onMessage()
          wait();
        } catch (InterruptedException exc) {}
      }
      
      if (status == Status.INIT ||
          status == Status.CLOSE) return;
      
      session.getRequestMultiplexer().abortRequest(requestId);
      setStatus(Status.CLOSE);
    }
    
    // Out of the synchronized block because it could 
    // lead to a dead lock with 
    // the connection driver thread calling replyReceived.
    ConsumerUnsetListRequest unsetLR = 
      new ConsumerUnsetListRequest(
        consumer.queueMode);
    unsetLR.setTarget(consumer.targetName);
    if (consumer.queueMode) {
      unsetLR.setCancelledRequestId(requestId);
    }
    session.syncRequest(unsetLR);
  }
  
  /**
   * Called by RequestMultiplexer.
   */
  public synchronized boolean replyReceived(AbstractJmsReply reply) 
    throws AbortedRequestException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                 "MessageConsumerListener.replyReceived(" + 
                                 reply + ')');
    if (status == Status.CLOSE) {
      throw new AbortedRequestException();
    } else {
      try {
        session.pushMessages(this, (ConsumerMessages)reply);
      } catch (StoppedQueueException exc) {
        throw new AbortedRequestException();
      }
      if (consumer.queueMode) {
        return true;
      } else {
        return false;
      }
    }
  }
  
  public void replyAborted(int requestId) {
    // Nothing to do.
  }

  public synchronized boolean isClosed() {
    return (status == Status.CLOSE);
  }

  public final MessageConsumer getMessageConsumer() {
    return consumer;
  }

  public final MessageListener getMessageListener() {
    return listener;
  }

  /**
   * Called by Session.
   */
  public void onMessage(Message msg) throws JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                 "MessageConsumerListener.onMessage(" + 
                                 msg + ')');

    if (consumer.queueMode) {
      // Consume one message in advance.
      subscribe();
    }

    synchronized (this) {
      if (status == Status.RUN) {
        setStatus(Status.ON_MSG);
      } else {
        throw new javax.jms.IllegalStateException("Status error");
      }
    }

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                 " -> consumer.onMessage(" + msg + ')');

    try {
      listener.onMessage(msg);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   " -> consumer.onMessage(" + 
                                   msg + ") returned");
    } catch (RuntimeException re) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", re);
      JMSException exc = new JMSException(re.toString());
      exc.setLinkedException(re);
      throw exc;
    } finally {
      synchronized (this) {
        setStatus(Status.RUN);
        
        // Notify threads trying to close the listener.
        notifyAll();
      }
    }
  }
}
