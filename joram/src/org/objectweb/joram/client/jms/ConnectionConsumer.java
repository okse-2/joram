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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.client.jms.connection.ReplyListener;
import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.client.jms.connection.Requestor;

import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionConsumer</code> interface.
 */
public class ConnectionConsumer implements javax.jms.ConnectionConsumer {
  /**
   * This property allows to set the maximum number of messages get by
   * each request, its default value is 1.
   */
  public static final String QUEUE_MSG_COUNT = 
      "org.objectweb.joram.client.jms.queueMsgCount";

  private static int queueMsgCount =
      Integer.getInteger(QUEUE_MSG_COUNT, 1).intValue();

  private static class Status {
    public static final int OPEN = 0;
    public static final int CLOSE = 1;

    private static final String[] names = {
      "OPEN", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  /** The connection the consumer belongs to. */
  private Connection cnx;

  /** <code>true</code> if the consumer is a durable topic subscriber. */
  private boolean durable = false;

  /** The selector for filtering messages. */
  private String selector;

  /** The session pool provided by the application server. */
  private javax.jms.ServerSessionPool sessionPool;

  /** The maximum number of messages a session may process at once. */
  private int maxMessages;

  /**
   * The daemon taking care of distributing the asynchronous deliveries to
   * the sessions. 
   */
  private CCDaemon ccDaemon;

  /** The name of the queue or of the subscription the deliveries come from. */
  private String targetName;

  /** <code>true</code> if the deliveries come from a queue. */
  private boolean queueMode;

  /**
   * The FIFO queue where the connection pushes the asynchronous server
   * deliveries.
   */
  fr.dyade.aaa.util.Queue repliesIn;

  private RequestMultiplexer mtpx;

  private Requestor requestor;

  private int requestId;

  private int status;

  /**
   * Constructs a <code>ConnectionConsumer</code>.
   *
   * @param cnx  The connection the consumer belongs to.
   * @param dest  The destination where consuming messages.
   * @param subName  The durable consumer name, if any.
   * @param selector  The selector for filtering messages.
   * @param sessionPool  The session pool provided by the application server.
   * @param maxMessages  The maximum number of messages to be passed at once
   *          to a session.
   *
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the
   *              destination.
   * @exception JMSException  If one of the parameters is wrong.
   */
  ConnectionConsumer(Connection cnx, 
                     Destination dest, 
                     String subName,
                     String selector, 
                     javax.jms.ServerSessionPool sessionPool,
                     int maxMessages,
                     RequestMultiplexer mtpx) throws JMSException {
    try {
      org.objectweb.joram.shared.selectors.Selector.checks(selector);
    }
    catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    if (sessionPool == null)
      throw new JMSException("Invalid ServerSessionPool parameter: "
                             + sessionPool);
    if (maxMessages <= 0)
      throw new JMSException("Invalid maxMessages parameter: " + maxMessages);
    
    this.cnx = cnx;
    this.selector = selector;
    this.sessionPool = sessionPool;
    this.maxMessages = maxMessages;

    this.mtpx = mtpx;
    this.requestor = new Requestor(mtpx);

    setStatus(Status.OPEN);

    if (dest instanceof Queue) {
      queueMode = true;
      targetName = dest.getName();
    } else if (subName == null) {
      queueMode = false;
      targetName = cnx.nextSubName();
    } else {
      queueMode = false;
      targetName = subName;
      durable = true;
    }

    repliesIn = new fr.dyade.aaa.util.Queue();

    ccDaemon = new CCDaemon(toString());
    ccDaemon.setDaemon(true);
    ccDaemon.start();

    // If the consumer is a subscriber, subscribing to the target topic:
    if (! queueMode) {
      requestor.request(
        new ConsumerSubRequest(
          dest.getName(), targetName,
          selector, false, durable));
    }

    // Sending a listener request:
    subscribe();
  }

  /**
   * Constructs a <code>ConnectionConsumer</code>.
   *
   * @param cnx  The connection the consumer belongs to.
   * @param dest  The destination where consuming messages.
   * @param selector  The selector for filtering messages.
   * @param sessionPool  The session pool provided by the application server.
   * @param maxMessages  The maximum number of messages to be passed at once
   *          to a session.
   *
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the
   *              destination.
   * @exception JMSException  If one of the parameters is wrong.
   */
  ConnectionConsumer(Connection cnx, 
                     Destination dest, 
                     String selector,
                     javax.jms.ServerSessionPool sessionPool,
                     int maxMessages,
                     RequestMultiplexer mtpx) 
    throws JMSException {
    this(cnx, dest, null, selector, 
         sessionPool, maxMessages, mtpx);
  }

  public final String getTargetName() {
    return targetName;
  }

  public final boolean getQueueMode() {
    return queueMode;
  }

  /** Returns a string image of the connection consumer. */
  public String toString()
  {
    return "ConnCons:" + cnx.toString();
  }

  private void setStatus(int status) {
    this.status = status;
  }

  private synchronized void checkClosed() 
    throws IllegalStateException {
    if (status == Status.CLOSE)
      throw new IllegalStateException("Forbidden call on a closed session.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the ConnectionConsumer is closed.
   */
  public javax.jms.ServerSessionPool getServerSessionPool() throws JMSException {
    checkClosed();
    return sessionPool;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void close() throws JMSException {
    if (status == Status.CLOSE) return;

    mtpx.abortRequest(requestId);

    ccDaemon.stop();

    // If the consumer is a subscriber, managing the subscription closing: 
    if (! queueMode) {
      if (durable) {
        requestor.request(new ConsumerCloseSubRequest(targetName));
      } else {
        requestor.request(new ConsumerUnsubRequest(targetName));
      }
    }

    cnx.closeConnectionConsumer(this);

    setStatus(Status.CLOSE);
  }

  private void subscribe() throws JMSException {
    ConsumerSetListRequest req = 
      new ConsumerSetListRequest(
        targetName, selector, queueMode, null, queueMsgCount);
    mtpx.sendRequest(req, new ReplyListener() {
        public boolean replyReceived(AbstractJmsReply reply) {
          repliesIn.push(reply);
          return queueMode;
        }
        
        public void replyAborted(int requestId) {}
      });
    requestId = req.getRequestId();
  }

  /** 
   * The <code>CCDaemon</code> distributes the server's asynchronous
   * deliveries to the application server's sessions.
   */
  class CCDaemon extends fr.dyade.aaa.util.Daemon {

    /**
     * Constructs the <code>CCDaemon</code> belonging to this connection
     * consumer.
     */
    CCDaemon(String name){
      super(name);
    }

    /** The daemon's loop. */
    public void run() {
      Vector deliveries = new Vector();
      try {
        while (running) {
          canStop = true;
          try {
            repliesIn.get();
          } catch (Exception iE) {
            continue;
          }
          canStop = false;
          
          // Processing the delivery:
          try {
            javax.jms.ServerSession serverSess = sessionPool.getServerSession();

            Session sess;
            Object obj = serverSess.getSession();
            if (obj instanceof Session) {
              sess = (Session)obj;
            } else if (obj instanceof XASession) {
              sess = ((XASession)obj).sess;
            } else throw new Error("Unexpected session type: " + obj);

            sess.setConnectionConsumer(ConnectionConsumer.this);
            int counter = 1;
            
            // As long as there are messages to deliver, passing to session(s)
            // as many messages as possible:
            while (counter <= maxMessages && repliesIn.size() > 0) {
              
              // If the consumer is a queue consumer, sending a new request:
              if (queueMode) {
                subscribe();
              }
              
              ConsumerMessages reply = (ConsumerMessages) repliesIn.pop();
              Vector msgs = reply.getMessages();
              for (int i = 0; i < msgs.size(); i++) {
                deliveries.add(
                  (org.objectweb.joram.shared.messages.Message) msgs.get(i));
              }

              while (! deliveries.isEmpty()) {
                while (counter <= maxMessages && ! deliveries.isEmpty()) {
                  if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                    JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Passes a"
                                               + " message to a session.");
                  sess.onMessage(
                    (org.objectweb.joram.shared.messages.Message) deliveries.remove(0));
                  counter++;
                }

                if (counter > maxMessages) {
                  if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                    JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Starts the"
                                               + " session.");
                  serverSess.start(); 
                  counter = 1;

                  if (! deliveries.isEmpty() || repliesIn.size() > 0) {
                    serverSess = sessionPool.getServerSession();
                    obj = serverSess.getSession();
                    if (obj instanceof Session) {
                      sess = (Session)obj;
                    } else if (obj instanceof XASession) {
                      sess = ((XASession)obj).sess;
                    } else throw new Error("Unexpected session type: " + obj);
                    sess.setConnectionConsumer(ConnectionConsumer.this);
                  }
                }
              }
            }
            // There is no more message to deliver and no more delivery, 
            // starting the last session to which messages have been passed:
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG, "No more delivery.");
            if (counter > 1) {
              if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Starts the"
                                           + " session.");
              counter = 1;
              serverSess.start();
            }
          }
          // A JMSException will be caught if the application server failed
          // to provide a session: closing the consumer.
          catch (JMSException jE) {
            canStop = true;
            try {
              ConnectionConsumer.this.close();
            }
            catch (JMSException jE2) {}
          }
        }
      }
      finally {
        finish();
      }
    }

    protected void shutdown() {}

    protected void close() {}
  }
}
