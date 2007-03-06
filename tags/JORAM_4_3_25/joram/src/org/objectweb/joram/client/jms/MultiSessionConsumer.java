/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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

import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;

import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.Queue;

/**
 * The MultiSessionConsumer is threaded (see MessageDispatcher)
 * because the session pool can hang if there is no more
 * available ServerSession.
 * 
 */
public class MultiSessionConsumer extends MessageConsumerListener
  implements javax.jms.ConnectionConsumer{
  
  private static final Logger logger = 
    Debug.getLogger(MultiSessionConsumer.class.getName());
  
  private ServerSessionPool sessPool;
  
  private Connection cnx;
  
  private int maxMsgs;
  
  private Queue repliesIn;
  
  /**
   * Number of simultaneously activated
   * listeners.
   */
  private int nbActivatedListeners;
  
  private MessageDispatcher msgDispatcher;

  /**
   * @param consumer
   * @param listener
   * @param ackMode
   * @param queueMessageReadMax
   * @param topicActivationThreshold
   * @param topicPassivationThreshold
   * @param topicAckBufferMax
   * @param reqMultiplexer
   */
  MultiSessionConsumer(
      boolean queueMode,
      boolean durable,
      String selector,
      String targetName,
      ServerSessionPool sessionPool,
      int queueMessageReadMax, 
      int topicActivationThreshold, int topicPassivationThreshold, 
      int topicAckBufferMax, 
      RequestMultiplexer reqMultiplexer,
      Connection connection,
      int maxMessages) {
    super(queueMode, durable, selector, targetName, 
        null, queueMessageReadMax,
        topicActivationThreshold,
        topicPassivationThreshold, topicAckBufferMax,
        reqMultiplexer);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MultiSessionConsumer.<init>(" +
          queueMode + ',' + durable + ',' + selector + ',' + 
          targetName + ',' + sessionPool + ',' +
          queueMessageReadMax + ',' +
          topicActivationThreshold + ',' + topicPassivationThreshold + ',' +
          topicAckBufferMax + ',' + 
          reqMultiplexer + ',' + maxMessages + ')');
    sessPool = sessionPool;
    cnx = connection;
    maxMsgs = maxMessages;
    msgDispatcher = new MessageDispatcher(
        "MessageDispatcher[" + reqMultiplexer.getDemultiplexerDaemonName() + ']');
    repliesIn = new Queue();
    msgDispatcher.setDaemon(true);
    msgDispatcher.start();
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.client.jms.MessageConsumerListener#pushMessages(org.objectweb.joram.shared.client.ConsumerMessages)
   */
  public void pushMessages(ConsumerMessages cm) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MultiSessionConsumer.pushMessages(" + cm + ')');
    repliesIn.push(cm);
  }  

  /* (non-Javadoc)
   * @see javax.jms.ConnectionConsumer#getServerSessionPool()
   */
  public ServerSessionPool getServerSessionPool() throws JMSException {
    return sessPool;
  }
  
  public void close() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MultiSessionConsumer.close()");
    msgDispatcher.stop();
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "MultiSessionConsumer -> dispatcher stopped");
    
    super.close();
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "MultiSessionConsumer -> close connection consumer");
    
    cnx.closeConnectionConsumer(this);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "MultiSessionConsumer -> connection consumer closed");
  }
  
  public void onMessage(
      Message msg, MessageListener listener, int ackMode)
      throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MultiSessionConsumer.onMessage(" + msg + ')');
    try {
      synchronized (this) {
        if (getStatus() == Status.CLOSE) {
          throw new javax.jms.IllegalStateException("Message listener closed");
        } else {
          if (nbActivatedListeners == 0) {
            setStatus(Status.ON_MSG);
          }
          nbActivatedListeners++;
        }
      }
      activateListener(msg, listener, ackMode);
    } finally {
      synchronized (this) {
        nbActivatedListeners--;
        if (nbActivatedListeners == 0) {
          setStatus(Status.RUN);
          // Notify threads trying to close the 
          // MessageConsumerListener.
          notifyAll();
        }        
      }
    }
  }
  
  class MessageDispatcher extends Daemon {
    
    MessageDispatcher(String name) {
      super(name);
    }

    /* (non-Javadoc)
     * @see fr.dyade.aaa.util.Daemon#close()
     */
    protected void close() {
      // TODO Auto-generated method stub
      
    }

    /* (non-Javadoc)
     * @see fr.dyade.aaa.util.Daemon#shutdown()
     */
    protected void shutdown() {
      // TODO Auto-generated method stub
      
    }
    
    /**
     * Enables the daemon to stop itself.
     */
    public void stop() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "MessageDispatcher.stop()");
      if (isCurrentThread()) { 
        finish();
      } else {
        super.stop();
      }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        while (running) {
          canStop = true;
          ConsumerMessages cm = (ConsumerMessages) repliesIn.get();
          canStop = false;

          Vector msgs = cm.getMessages();
          int sessionMsgCounter = maxMsgs + 1;
          ServerSession serverSess = null;
          Session sess = null;
          for (int i = 0; i < msgs.size(); i++) {
            if (sessionMsgCounter > maxMsgs) {
              if (serverSess != null)
                serverSess.start();
              serverSess = sessPool.getServerSession();
              // This can hang if there is no more sessions
              // in the pool
              Object obj = serverSess.getSession();
              if (obj instanceof Session) {
                sess = (Session) obj;
              } else if (obj instanceof XASession) {
                sess = ((XASession) obj).sess;
              } else {
                throw new Error("Unexpected session type: " + obj);
              }
              sess.setMessageConsumerListener(MultiSessionConsumer.this);
              sessionMsgCounter = 1;
            }
            sess.onMessage((org.objectweb.joram.shared.messages.Message) msgs.get(i));
            sessionMsgCounter++;
          }
          serverSess.start();
          repliesIn.pop();
        }
      } catch (InterruptedException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "", exc);
        }
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "", exc);
        }
        try {
          MultiSessionConsumer.this.close();
        } catch (JMSException exc2) {
        }
      } finally {
        finish();
      }
    } 
  }
}
