/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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

import java.util.Vector;

import javax.jms.JMSException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Specializes the <code>ConnectionConsumer</code> implementation to the
 * PubSub mode.
 */
public class TopicConnectionConsumer extends ConnectionConsumer
{
  /** <code>true</code> if the consumer is durable. */
  private boolean durable;
  /** The name used for subscribing to the topic. */
  String subName;

  /**
   * Constructs a <code>TopicConnectionConsumer</code> instance.
   *
   * @param cnx  The topic connection the consumer belongs to.
   * @param topicName  The name of the topic where consuming messages.
   * @param selector  The selector for filtering messages.
   * @param sessionPool  The session pool provided by the application server.
   * @param maxMessages  The maximum number of messages to be passed at once
   *          to a session.
   * @param subName  The name of the subscription on the topic.
   * @param durable  <code>true<code> for a durable subscription.
   *
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the topic.
   * @exception JMSException  If the construction fails for any other reason.
   */
  TopicConnectionConsumer(TopicConnection cnx, String topicName,
                          String selector,
                          javax.jms.ServerSessionPool sessionPool,
                          int maxMessages, String subName,
                          boolean durable) throws JMSException
  {
    super(cnx, topicName, selector, sessionPool, maxMessages);
    this.subName = subName;
    this.durable = durable;

    ccDaemon = new TCCDaemon(this);
    ccDaemon.setDaemon(true);
    ccDaemon.start();

    // Subscribing to the topic and starting the deliveries:
    cnx.syncRequest(new TSessSubRequest(topicName, subName, selector,
                                        false, durable));
    currentReq = new TSubSetListRequest(subName);
    currentReq.setIdentifier(cnx.nextRequestId());
    cnx.requestsTable.put(currentReq.getRequestId(), this);
    cnx.asyncRequest(currentReq);
  }

  /**
   * Specializes this API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    cnx.requestsTable.remove(currentReq.getRequestId());
    ccDaemon.stop();

    try {
      cnx.syncRequest(new TSubUnsetListRequest(subName));
      if (durable) 
        cnx.syncRequest(new TSubCloseRequest(subName));
      else
        cnx.syncRequest(new TSessUnsubRequest(subName));
    }
    catch (JMSException jE) {}

    cnx.cconsumers.remove(this);
  }


  /** 
   * The <code>TCCDaemon</code> distributes the server's asynchronous
   * deliveries to the application server's sessions.
   */ 
  private class TCCDaemon extends fr.dyade.aaa.util.Daemon
  {
    /** The connection consumer the daemon belongs to. */
    private TopicConnectionConsumer tcc;

    /**
     * Constructs a <code>TCCDaemon</code> belonging to a given connection
     * consumer.
     */
    private TCCDaemon(TopicConnectionConsumer tcc)
    {
      super(tcc.toString());
      this.tcc = tcc;
    }


    /** The daemon's loop. */
    public void run()
    {
      SubMessages reply;
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
            reply = (SubMessages) repliesIn.pop();
            deliveries.addAll(reply.getMessages());
  
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + tcc
                                         + ": got a delivery.");

            // Getting a server's session:
            serverSess = sessionPool.getServerSession();
            sess = (fr.dyade.aaa.joram.Session) serverSess.getSession();
            sess.connectionConsumer = tcc;
            counter = 1;

            // As long as there are messages to deliver, passing to session(s)
            // as many messages as possible:
            while (! deliveries.isEmpty()) {
              while (counter <= maxMessages && ! deliveries.isEmpty()) {
                if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                  JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Passes a"
                                             + " message to a session.");
  
                sess.repliesIn.push(deliveries.remove(0));
                counter++;
              }
              // If the maximum number of messages is reached, starting the 
              // session and if ned, getting the next one for going on:
              if (counter > maxMessages) {
                if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                  JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Starts the"
                                             + " session.");
                serverSess.start();
                counter = 1;
                if (! deliveries.isEmpty() || repliesIn.size() > 0) {
                  serverSess = sessionPool.getServerSession();
                  sess = (fr.dyade.aaa.joram.Session) serverSess.getSession();
                  sess.connectionConsumer = tcc;
                }
              }
              // If there is no more message to deliver, getting the next
              // reply's, if any:
              if (deliveries.isEmpty() && repliesIn.size() > 0) {
                if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                  JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Gets the"
                                             + " next reply.");
                reply = (SubMessages) repliesIn.pop();
                deliveries.addAll(reply.getMessages());
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
              serverSess.start();
            }
          }
          // A JMSException will be caught if the application server failed
          // to provide a session: closing the consumer.
          catch (JMSException jE) {
            canStop = true;
            try {
              tcc.close();
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
    {}
  }
}
