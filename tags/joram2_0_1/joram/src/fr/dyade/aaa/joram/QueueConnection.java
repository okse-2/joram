/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.joram;

import java.net.*;
import javax.jms.*;

/**
 * A <code>QueueConnection</code> is an active connection to a JMS PTP
 * provider.<br>
 * A client uses a <code>QueueConnection</code> to create one or more
 * <code>QueueSession</code>s for producing and consuming messages.
 *
 * @author Frederic Maistre
 */
public class QueueConnection extends Connection implements javax.jms.QueueConnection
{
  protected QueueConnectionListener queueConnectionListener = null;

  /** Constructor. */
  public QueueConnection(String proxyAgentIdString, InetAddress proxyAddress,
    int proxyPort, String login, String passwd) throws javax.jms.JMSException
  {
    super(proxyAgentIdString, proxyAddress, proxyPort, login, passwd);
  }


  /** Method creating a <code>QueueSession</code>. */
  public javax.jms.QueueSession createQueueSession(boolean transacted,
    int acknowledgeMode) throws JMSException
  {
    try {
      long sessionCounterNew = sessionCounter;
      sessionCounter = calculateMessageID(sessionCounter);

      fr.dyade.aaa.joram.QueueSession session =
        new fr.dyade.aaa.joram.QueueSession(transacted, acknowledgeMode,
        sessionCounter,this);

       if (session == null) {
         sessionCounter = sessionCounter - 1;
         throw (new javax.jms.JMSException("Error when creating the QueueSession"));
       }
       else {
         sessions.put(new Long(sessionCounterNew), session);
         return session;
       }

    } catch (JMSException jE) {
      throw (jE);
    } catch (Exception e) {
      javax.jms.JMSException jE = new JMSException("Internal error");
      jE.setLinkedException(e);
      throw(jE);
    }
  }


  /**
   * Method creating a <code>ConnectionConsumer</code> on this connection.
   * <br>
   * This is an expert facility not used by regular JMS clients.
   *
   * @param queue  The Queue which messages will be consumed by this consumer.
   * @param messageSelector  The selector to filter the consumed messages.
   * @param sessionPool  The pool from which Sessions are got.
   * @param maxMessages  The number of messages passed to a given Session.
   *
   * @author Frederic Maistre
   */
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Queue queue,
    String messageSelector, javax.jms.ServerSessionPool sessionPool, int maxMessages)
    throws JMSException
  {
    if (this.connectionConsumer != null)
      throw (new JMSException("A ConnectionConsumer already exists for this Connection"));
    if (sessionPool == null)
      throw (new JMSException("ServerSessionPool parameter is null!"));

    // Building the connectionConsumer.
    this.connectionConsumer =
      new ConnectionConsumer(queue, messageSelector, sessionPool, maxMessages);

    connectionConsumer.setConnection(this);

    // Launching the listening thread which gets the messages from the queue.
    queueConnectionListener = new QueueConnectionListener(this,
      connectionConsumer, queue, messageSelector);
    queueConnectionListener.setDaemon(true);
    queueConnectionListener.start();

    connectionConsumer.setConnection(this);

    return (javax.jms.ConnectionConsumer) connectionConsumer;
  }

  /** Closing method. */
  public void close() throws JMSException
  {
    if (queueConnectionListener != null)
      queueConnectionListener.stop();

    super.close();
  }

}
