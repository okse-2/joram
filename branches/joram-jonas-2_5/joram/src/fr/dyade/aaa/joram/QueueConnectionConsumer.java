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

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Specializes the <code>ConnectionConsumer</code> implementation to the
 * PTP mode.
 */
public class QueueConnectionConsumer extends ConnectionConsumer
{
  /**
   * Constructs a <code>QueueConnectionConsumer</code> instance.
   *
   * @param cnx  The queue connection the consumer belongs to.
   * @param queueName  The name of the queue where consuming messages.
   * @param selector  The selector for filtering messages.
   * @param sessionPool  The session pool provided by the application server.
   * @param maxMessages  The maximum number of messages to be passed at once
   *          to a session.
   *
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target queue does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the queue.
   * @exception JMSException  If one of the parameters is wrong.
   */
  QueueConnectionConsumer(QueueConnection cnx, String queueName,
                          String selector,
                          javax.jms.ServerSessionPool sessionPool,
                          int maxMessages) throws JMSException
  {
    super(cnx, queueName, selector, sessionPool, maxMessages);

    ccDaemon = new QCCDaemon(this);
    ccDaemon.setDaemon(true);
    ccDaemon.start();

    // Sending the first receive request:
    currentReq = new QRecReceiveRequest(queueName, selector, -1);
    currentReq.setIdentifier(cnx.nextRequestId());
    cnx.requestsTable.put(currentReq.getRequestId(), this);
    cnx.asyncRequest(currentReq);
  }

  /**
   * Specializes this API method to the PTP mode.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    cnx.requestsTable.remove(currentReq.getRequestId());
    ccDaemon.stop();

    cnx.cconsumers.remove(this);
  }


  /** 
   * The <code>QCCDaemon</code> distributes the server's asynchronous
   * deliveries to the application server's sessions.
   */ 
  private class QCCDaemon extends fr.dyade.aaa.util.Daemon
  {
    /** The connection consumer the daemon belongs to. */
    private QueueConnectionConsumer qcc;

    /**
     * Constructs the <code>QCCDaemon</code> belonging to this connection
     * consumer.
     */
    private QCCDaemon(QueueConnectionConsumer qcc)
    {
      super(qcc.toString());
      this.qcc = qcc;
    }

    /** The daemon's loop. */
    public void run()
    {
      javax.jms.ServerSession serverSess;
      Session sess;
      int counter;
      QueueMessage reply;

      try {
        while (running) {
          canStop = true; 

          try {
            // Expecting a reply:
            repliesIn.get();
            cnx.requestsTable.remove(currentReq.getRequestId());

            // Sending a new request:
            currentReq = new QRecReceiveRequest(destName, selector, -1);
            currentReq.setIdentifier(cnx.nextRequestId());
            cnx.requestsTable.put(currentReq.getRequestId(), qcc);
            cnx.asyncRequest(currentReq);
          }
          catch (Exception iE) {
            continue;
          }
          canStop = false;

          // Processing the delivery:
          try {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + qcc
                                         + ": got a delivery.");

            // Getting a server's session:
            serverSess = sessionPool.getServerSession();
            sess = (fr.dyade.aaa.joram.Session) serverSess.getSession();
            sess.connectionConsumer = qcc;
            counter = 1;

            // As long as there are messages to deliver, passing to session(s)
            // as many messages as possible:
            while (repliesIn.size() > 0) {
              reply = (QueueMessage) repliesIn.pop();
              if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
                JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Passes a"
                                           + " message to a session.");
              sess.repliesIn.push(reply.getMessage());
              counter++;
  
              // If the maximum number of messages is reached, starting the 
              // session and if ned, getting the next one for going on:
              if (counter > maxMessages) {
                serverSess.start(); 
                counter = 1;
                if (repliesIn.size() > 0) {
                  serverSess = sessionPool.getServerSession();
                  sess = (fr.dyade.aaa.joram.Session) serverSess.getSession();
                  sess.connectionConsumer = qcc;
                }
              }
            }
            // There is no more message to deliver and no more delivery, 
            // starting the last session to which messages have been passed:
            if (counter > 1)
              serverSess.start();
          }
          // A JMSException will be caught if the application server failed
          // to provide a session: closing the consumer.
          catch (JMSException jE) {
            canStop = true;
            try {
              qcc.close();
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
