/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.client.*;

import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionConsumer</code> interface.
 */
public class ConnectionConsumer implements javax.jms.ConnectionConsumer
{
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
  /** The current consuming request. */
  private org.objectweb.joram.shared.client.AbstractJmsRequest currentReq = null;
  /** <code>true</code> if the connection consumer is closed. */
  private boolean closed = false;

  /** The name of the queue or of the subscription the deliveries come from. */
  String targetName;
  /** <code>true</code> if the deliveries come from a queue. */
  boolean queueMode = true;
  /**
   * The FIFO queue where the connection pushes the asynchronous server
   * deliveries.
   */
  fr.dyade.aaa.util.Queue repliesIn;


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
  ConnectionConsumer(Connection cnx, Destination dest, String subName,
                     String selector, javax.jms.ServerSessionPool sessionPool,
                     int maxMessages) throws JMSException
  {
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

    if (dest instanceof Queue)
      targetName = dest.getName();
    else if (subName == null) {
      queueMode = false;
      targetName = cnx.nextSubName();
    }
    else {
      queueMode = false;
      targetName = subName;
      durable = true;
    }

    repliesIn = new fr.dyade.aaa.util.Queue();

    if (cnx.cconsumers == null)
      cnx.cconsumers = new Vector();
 
    cnx.cconsumers.add(this);

    ccDaemon = new CCDaemon(this);
    ccDaemon.setDaemon(true);
    ccDaemon.start();

    // If the consumer is a subscriber, subscribing to the target topic:
    if (! queueMode) 
      cnx.syncRequest(new ConsumerSubRequest(dest.getName(), targetName,
                                             selector, false, durable));

    // Sending a listener request:
    currentReq = new ConsumerSetListRequest(targetName, selector, queueMode);
    currentReq.setRequestId(cnx.nextRequestId());
    cnx.requestsTable.put(currentReq.getKey(), this);
    cnx.asyncRequest(currentReq);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
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
  ConnectionConsumer(Connection cnx, Destination dest, String selector,
                     javax.jms.ServerSessionPool sessionPool,
                     int maxMessages) throws JMSException
  {
    this(cnx, dest, null, selector, sessionPool, maxMessages);
  }

  /** Returns a string image of the connection consumer. */
  public String toString()
  {
    return "ConnCons:" + cnx.toString();
  }


  /**
   * API method.
   *
   * @exception IllegalStateException  If the ConnectionConsumer is closed.
   */
  public javax.jms.ServerSessionPool getServerSessionPool() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " ConnectionConsumer.");
    return sessionPool;
  }


  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    cnx.requestsTable.remove(currentReq.getKey());
    ccDaemon.stop();

    // If the consumer is a subscriber, managing the subscription closing: 
    if (! queueMode) {
      try {
        if (durable) 
          cnx.syncRequest(new ConsumerCloseSubRequest(targetName));
        else
          cnx.syncRequest(new ConsumerUnsubRequest(targetName));
      }
      // A JMSException might be caught if the connection is broken.
      catch (JMSException jE) {}
    }
    cnx.cconsumers.remove(this);
  }

/** 
 * The <code>CCDaemon</code> distributes the server's asynchronous
 * deliveries to the application server's sessions.
 */ 
class CCDaemon extends fr.dyade.aaa.util.Daemon
{
  /** The connection consumer the daemon belongs to. */
  private ConnectionConsumer cc;

  /**
   * Constructs the <code>CCDaemon</code> belonging to this connection
   * consumer.
   */
  CCDaemon(ConnectionConsumer cc)
  {
    super(cc.toString());
    this.cc = cc;
  }

  /** The daemon's loop. */
  public void run()
  {
    ConsumerMessages reply;
    Vector deliveries = new Vector();
    javax.jms.ServerSession serverSess;
    Session sess;
    int counter;

    try {
      while (running) {
        canStop = true; 

        try {
          // Expecting a reply:
          repliesIn.get();
        }
        catch (Exception iE) {
          continue;
        }
        canStop = false;

        // Processing the delivery:
        try {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + cc
                                       + ": got a delivery.");

          // Getting a server's session:
          serverSess = sessionPool.getServerSession();
          sess = (org.objectweb.joram.client.jms.Session) serverSess.getSession();
          sess.connectionConsumer = cc;
          counter = 1;

          // As long as there are messages to deliver, passing to session(s)
          // as many messages as possible:
          while (counter <= maxMessages && repliesIn.size() > 0) {
            
            // If the consumer is a queue consumer, sending a new request:
            if (queueMode) {
              cnx.requestsTable.remove(currentReq.getKey());
              currentReq = new ConsumerSetListRequest(targetName, selector,
                                                      queueMode);
              currentReq.setRequestId(cnx.nextRequestId());
              cnx.requestsTable.put(currentReq.getKey(), cc);
              cnx.asyncRequest(currentReq);
            }

            reply = (ConsumerMessages) repliesIn.pop();
            Vector msgs = reply.getMessages();
            for (int i = 0; i < msgs.size(); i++)
              deliveries.add((org.objectweb.joram.shared.messages.Message) msgs.get(i));

            while (! deliveries.isEmpty()) {
              while (counter <= maxMessages && ! deliveries.isEmpty()) {
                if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                  JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Passes a"
                                             + " message to a session.");
                sess.repliesIn.push(deliveries.remove(0));
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
                  sess =
                    (org.objectweb.joram.client.jms.Session) serverSess.getSession();
                  sess.connectionConsumer = cc;
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
            cc.close();
          }
          catch (JMSException jE2) {}
        }
      }
    }
    finally {
      finish();
    }
  }

  /** Shuts the daemon down. */
  public void shutdown()
  {}

  /** Releases the daemon's resources. */
  public void close()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "CCDaemon finished.");
  }
}
}
