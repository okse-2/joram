/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.util.ArrayList;
import java.util.Vector;

import javax.jms.MessageListener;
import javax.jms.JMSException;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.client.ConsumerSetListRequest;
import org.objectweb.joram.shared.client.ConsumerUnsetListRequest;
import org.objectweb.joram.shared.client.ConsumerAckRequest;
import org.objectweb.joram.shared.client.ActivateConsumerRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.client.jms.connection.ReplyListener;
import org.objectweb.joram.client.jms.connection.AbortedRequestException;
import org.objectweb.joram.client.jms.connection.RequestMultiplexer;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.StoppedQueueException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * This class listens to replies asynchronously returned by the user proxy 
 * for a message consumer.
 */
abstract class MessageConsumerListener implements ReplyListener {
  
  public static Logger logger = Debug.getLogger(MessageConsumerListener.class.getName());

  /**
   * Status of the message consumer listener.
   */
  protected static class Status {
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
  
  private static class ReceiveStatus {
    public static final int INIT = 0;
    
    public static final int WAIT_FOR_REPLY = 1;

    public static final int CONSUMING_REPLY = 2;

    private static final String[] names = { 
        "INIT", "WAIT_FOR_REPLY", "CONSUMING_REPLY" };

    public static String toString(int status) {
      return names[status];
    }
  }

  private boolean queueMode;
  
  private boolean durable;
  
  private String selector;
  
  private String destName;
  
  public final String getDestName() {
    return destName;
  }
  
  private String targetName;
  
  /**
   * The identifier of the subscription request.
   */ 
  private volatile int requestId;

  private int status;

  private Vector<String> messagesToAck;
  
  /**
   * The number of messages which are in queue (Session.qin)
   * waiting for being consumed.
   */
  private volatile int messageCount;
  
  /**
   * The receive status of this message listener:
   *  - WAIT_FOR_REPLY if a reply is expected from the destination
   *  - CONSUMING_REPLY if a reply is being consumed and no new request has
   *    been sent
   */
  private volatile int receiveStatus;
  
  /**
   * Indicates whether the topic message input has been passivated or not.
   */
  private boolean topicMsgInputPassivated;
  
  private int queueMessageReadMax;
  
  private RequestMultiplexer rm;
  
  private int topicActivationThreshold;
  
  private int topicPassivationThreshold;
  
  private int topicAckBufferMax;
  
  private ArrayList<MessageListener> listeners;
  private int listenerPosition = 0;
  
  MessageConsumerListener(boolean queueMode,
                          boolean durable,
                          String selector,
                          String destName,
                          String targetName,
                          MessageListener listener,
                          int queueMessageReadMax,
                          int topicActivationThreshold,
                          int topicPassivationThreshold,
                          int topicAckBufferMax,
                          RequestMultiplexer reqMultiplexer) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "MessageConsumerListener(" + queueMode +
                 ',' + durable + ',' + selector + ',' + destName  + ',' + targetName + 
                 ',' + listener + ',' + queueMessageReadMax + 
                 ',' + topicActivationThreshold +
                 ',' + topicPassivationThreshold +
                 ',' + topicAckBufferMax + ',' + reqMultiplexer + ')');
    this.queueMode = queueMode;
    this.durable = durable;
    this.selector = selector;
    this.destName = destName;
    this.targetName = targetName;
    this.queueMessageReadMax = queueMessageReadMax;
    this.topicActivationThreshold = topicActivationThreshold;
    this.topicPassivationThreshold = topicPassivationThreshold;
    this.topicAckBufferMax = topicAckBufferMax;
    rm = reqMultiplexer;
    messagesToAck = new Vector<String>(0);
    requestId = -1;
    messageCount = 0;
    topicMsgInputPassivated = false;
    listeners = new ArrayList<MessageListener>();
    listeners.add(listener);
    setStatus(Status.INIT);
    setReceiveStatus(ReceiveStatus.INIT);
  }
  
  protected final int getStatus() {
    return status;
  }

  protected void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MessageConsumerListener.setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }
  
  private void setReceiveStatus(int s) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MessageConsumerListener.setReceiveStatus(" + ReceiveStatus.toString(s) + ')');
    receiveStatus = s;
  }
  
  /**
   * Decrease the message count.
   * Synchronized with the method replyReceived() that increments the 
   * messageCount += cm.getMessageCount();
   * 
   * @return the decreased value
   */
  private int decreaseMessageCount(int ackMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MessageConsumerListener.decreaseMessageCount()");
    
    synchronized (this) {
      messageCount--;
    }
    
    if (queueMode) {
      boolean subscribe = false;
      String[] toAck = null;
      synchronized (this) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> messageCount = " + messageCount);
        // Consume in advance (default is one message in advance)
        if ((messageCount < queueMessageReadMax || (queueMessageReadMax == 0))
            && receiveStatus == ReceiveStatus.CONSUMING_REPLY) {
          subscribe = true;
          if (ackMode == javax.jms.Session.DUPS_OK_ACKNOWLEDGE) {
            synchronized (messagesToAck) {
              if (messagesToAck.size() > 0) {
                toAck = new String[messagesToAck.size()];
                messagesToAck.copyInto(toAck);
                messagesToAck.clear();
              }
            }
          }
        }
      }
      // out of the synchronized block
      if (subscribe) {
        if (queueMessageReadMax == 0)
          subscribe(toAck, 1);
        else 
          subscribe(toAck, queueMessageReadMax);
      }
    } else {
      synchronized (this) {
        if (topicMsgInputPassivated) {
          if (messageCount < topicActivationThreshold) {
            activateMessageInput();
            topicMsgInputPassivated = false;
          }
        } else {
          if (messageCount > topicPassivationThreshold) {
            passivateMessageInput();
            topicMsgInputPassivated = true;
          }
        }
      }
    }
    
    if (ackMode == javax.jms.Session.DUPS_OK_ACKNOWLEDGE
        && messageCount == 0) {
      // Need to acknowledge the received messages
      // if we are in lazy mode (DUPS_OK)
      acknowledge(0);
    }
    
    return messageCount;
  }
 
  /**
   * Called by Session.
   */
  synchronized void start() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, "MessageConsumerListener.start()");
    if (status == Status.INIT) {
      if (queueMessageReadMax == 0)
        subscribe(null, 1);
      else
        subscribe(null, queueMessageReadMax);
      setStatus(Status.RUN);
    } else {
      // Should not happen
      throw new IllegalStateException("Status error");
    }
  }

  private void subscribe(String[] toAck, int msgCount) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.subscribe()");
    
    ConsumerSetListRequest req = 
      new ConsumerSetListRequest(
        targetName,
        selector, 
        queueMode,
        toAck,
        msgCount);
    
    // Change the receive status before sending
    // the request. subscribe() is not synchronized
    // so the reply can be received before the end
    // of this method.
    setReceiveStatus(ReceiveStatus.WAIT_FOR_REPLY);
    rm.sendRequest(req, this, null);
    requestId = req.getRequestId();
  }

  /**
   * @return true if the currentThread is the SessionThread.
   */
  abstract protected boolean checkSessionThread();
  
  /**
   * Called by Session.
   */
  public void close() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.close()");

    if (queueMode) {
      // Out of the synchronized block because it could
      // lead to a dead lock with
      // the connection driver thread calling replyReceived.
      ConsumerUnsetListRequest unsetLR = new ConsumerUnsetListRequest(
          queueMode);
      unsetLR.setTarget(targetName);
      unsetLR.setCancelledRequestId(requestId);
      rm.sendRequest(unsetLR);
    }
    // else useless for a topic 
    // because the subscription
    // is deleted (see MessageConsumer.close())
    
    synchronized (this) {
      while (status == Status.ON_MSG) {
        try {
          // Wait for the message listener to return from 
          // onMessage()
          if (!checkSessionThread()) {
            wait();
          } else {
            setStatus(Status.CLOSE);
            break;
          }
        } catch (InterruptedException exc) {}
      }
      
      if (status == Status.INIT ||
          status == Status.CLOSE) return;
      
      rm.abortRequest(requestId);

      // If session ack mode is DUPS_OK
      acknowledge(0);

      setStatus(Status.CLOSE);
    }
  }

  private void acknowledge(int threshold) {
    try {
      synchronized (messagesToAck) {
        if (messagesToAck.size() > threshold) {
          ConsumerAckRequest ack = new ConsumerAckRequest(
              targetName,
              queueMode);
          for (int i = 0; i < messagesToAck.size(); i++) {
            String msgId = (String) messagesToAck.elementAt(i);
            ack.addId(msgId);
          }
          rm.sendRequest(ack);
          messagesToAck.clear();
        }
      }
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(
          BasicLevel.ERROR, "", exc); 
    }
  }
  
  /**
   * Called by RequestMultiplexer.
   */
  public synchronized boolean replyReceived(AbstractJmsReply reply) 
    throws AbortedRequestException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.replyReceived(" + reply + ')');
    
    if (status == Status.CLOSE)
      throw new AbortedRequestException();

    if (queueMode) {
      // 1- Change the status before pushing the  messages into the session queue.
      setReceiveStatus(ReceiveStatus.CONSUMING_REPLY);
    }
    
    try {
      ConsumerMessages cm = (ConsumerMessages)reply;
      // 2- increment messageCount (synchronized)
      messageCount += cm.getMessageCount();
      
      // verify the server activity
      if (!cm.isActive()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "MessageConsumerListener.replyReceived: the server passivate.");
        topicMsgInputPassivated = true;
      }
          
      pushMessages(cm);
    } catch (StoppedQueueException exc) {
      throw new AbortedRequestException();
    } catch (JMSException exc) {
      throw new AbortedRequestException();
    }
    
    if (queueMode) {
      return true;
    }
    return false;
  }
  
  /**
   * Pushes the received messages.
   * Currently two behaviors:
   * 1- SingleSessionConsumer pushes the message
   * in a single session (standard JMS)
   * 2- MultiSessionConsumer pushes the message
   * in several session (from a session pool)
   * 
   * @param cm
   */
  public abstract void pushMessages(ConsumerMessages cm) throws JMSException;
  
  public void replyAborted(int requestId) {
    // Nothing to do.
  }
  
  public void errorReceived(int requestId, MomExceptionReply exc) {
    // Nothing to do.
  }

  public synchronized boolean isClosed() {
    return (status == Status.CLOSE);
  }
  
  public final MessageListener getMessageListener() {
    //TODO: select the good listenerPosition for shared consumer
    return listeners.get(listenerPosition);
  }
  
  public final boolean getQueueMode() {
    return queueMode;
  }
  
  public final String getTargetName() {
    return targetName;
  }

  protected void activateListener(Message msg,
                                  MessageListener listener,
                                  int ackMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.onMessage(" +  msg + ')');
    
    // if queueMessageReadMax == 0 (disable the mechanism of pre-requesting of the messages), 
    // consume the next message after onMessage.
    if (queueMessageReadMax > 0) {
    // Consume one message
    decreaseMessageCount(ackMode);
    }

    try {
      listener.onMessage(msg);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   " -> consumer.onMessage(" + msg + ") returned");
    } catch (RuntimeException re) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", re);
      JMSException exc = new JMSException(re.toString());
      exc.setLinkedException(re);
      throw exc;
    } 
    
    if (queueMessageReadMax == 0) {
      // Consume one message
      decreaseMessageCount(ackMode);
    }
  }
  
  public abstract void onMessage(Message msg,
                                 MessageListener listener,
                                 int ackMode) throws JMSException;
  
  synchronized void addMessageListener(MessageListener messageListener) {
    listeners.add(messageListener);
  }
  
  synchronized boolean removeMessageListener(MessageListener messageListener) {
    return listeners.remove(messageListener);
  }
  
  public int getMessageListenersSize() {
    return listeners.size();
  }
  
  synchronized private MessageListener getNextlistener() {
    int size = listeners.size();
    if (size == 1)
      return listeners.get(0);
    else if (size < 1)
      return null;
    
    // shared ConsumerListener
    listenerPosition++;
    if (listenerPosition >= size)
      listenerPosition = 0;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.getNextlistener() listenerPosition = " + listenerPosition);
    return listeners.get(listenerPosition);
  }
  
  /**
   * Called by Session (standard JMS, mono-threaded)
   */
  public void onMessage(Message msg, int ackMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.onMessage(" + msg + ')');
    
    MessageListener listener = getNextlistener();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MessageConsumerListener.onMessage listener = " + listener);
    
    if (listener != null) {
      synchronized (this) {
        if (status == Status.RUN) {
          setStatus(Status.ON_MSG);
        } else {
          // Notify threads trying to close the listener.
          notifyAll();
          throw new javax.jms.IllegalStateException("Message listener closed");
        }
      }

      try {
        activateListener(msg, listener, ackMode);
      } finally {
        synchronized (this) {
          if (status == Status.ON_MSG)
            setStatus(Status.RUN);

          // Notify threads trying to close the listener.
          notifyAll();
        }
      }
    } else {
      throw new JMSException("Null listener");
    }
  }

  void ack(String msgId, int ackMode)
      throws JMSException {
    if (ackMode == javax.jms.Session.DUPS_OK_ACKNOWLEDGE) {
      // All the operations on messagesToAck are synchronized
      // on the vector (see subscribe() and acknowledge()).
      messagesToAck.addElement(msgId);
      if (! queueMode) {
        acknowledge(topicAckBufferMax);
      }
    } else {
      ConsumerAckRequest ack = new ConsumerAckRequest(targetName, queueMode);
      ack.addId(msgId);
      rm.sendRequest(ack);
    }
  }

  void activateMessageInput() throws JMSException {
    rm.sendRequest(new ActivateConsumerRequest(targetName,
                                               topicPassivationThreshold - topicActivationThreshold));
  }

  void passivateMessageInput() throws JMSException {
    rm.sendRequest(new ActivateConsumerRequest(targetName, 0));
  }
}
