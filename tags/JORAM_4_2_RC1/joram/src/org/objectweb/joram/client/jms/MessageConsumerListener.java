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
    public static final int CLOSE = 2;

    private static final String[] names = {
      "INIT", "RUN", "CLOSE"};

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
  
  MessageConsumerListener(MessageConsumer consumer,
                          Session session) {
    this.consumer = consumer;
    this.session = session;
    status = Status.INIT;
  }

  /**
   * Called by Session.
   */
  synchronized void start() throws JMSException {
    if (status == Status.INIT) {
      subscribe();
      status = Status.RUN;
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
  synchronized void close() throws JMSException {
    if (status == Status.RUN) {
      session.getRequestMultiplexer().abortRequest(requestId);
      
      ConsumerUnsetListRequest unsetLR = 
        new ConsumerUnsetListRequest(
          consumer.queueMode);
      unsetLR.setTarget(consumer.targetName);
      if (consumer.queueMode) {
        unsetLR.setCancelledRequestId(requestId);
      }

      session.syncRequest(unsetLR);

      status = Status.CLOSE;
    } else {
      // Should not happen
      throw new IllegalStateException("Status error");
    }
  }
  
  /**
   * Called by RequestMultiplexer.
   */
  public synchronized boolean replyReceived(AbstractJmsReply reply) 
    throws AbortedRequestException {
    if (status == Status.RUN) {
      try {
        session.pushMessages(consumer, (ConsumerMessages)reply);
      } catch (StoppedQueueException exc) {
        throw new AbortedRequestException();
      }
      if (consumer.queueMode) {
        try {
          subscribe();
        } catch (JMSException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
            JoramTracing.dbgClient.log(
              BasicLevel.ERROR, "", exc); 
        }
        return true;
      } else {
        return false;
      }
    } else {
      // It is closed
      throw new AbortedRequestException();
    }
  }
  
  public void replyAborted(int requestId) {
    // Nothing to do.
  }
}
