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
 * A <code>TopicConnection</code> is an active connection to a JMS Pub/Sub
 * provider.<br>
 * A client uses a <code>TopicConnection</code> to create one or more
 * <code>TopicSession</code>s for producing and consuming messages.
 *
 * @author Frederic Maistre
 */
public class TopicConnection extends Connection implements javax.jms.TopicConnection
{
  /** Constructor. */
  public TopicConnection(String proxyAgentIdString, InetAddress proxyAddress,
    int proxyPort, String login, String passwd) throws JMSException
  {
    super(proxyAgentIdString, proxyAddress, proxyPort, login, passwd);
  }


  /** Method creating a <code>TopicSession</code>. */
  public javax.jms.TopicSession createTopicSession(boolean transacted,
    int acknowledgeMode) throws JMSException
  {
    try {
      long sessionCounterNew = sessionCounter;
      sessionCounter = calculateMessageID(sessionCounter);
 
      fr.dyade.aaa.joram.TopicSession session =
        new fr.dyade.aaa.joram.TopicSession(transacted, acknowledgeMode,
        sessionCounterNew, this);

      if (session == null) {
        sessionCounter = sessionCounter - 1;
        throw (new javax.jms.JMSException("Error when creating the TopicSession"));
      }
      else {
        sessions.put(new Long(sessionCounterNew), session);
        return session;
      }

    } catch (JMSException jE) {
      throw(jE);
    } catch (Exception e) {
      javax.jms.JMSException jE = new JMSException("Internal error");
      jE.setLinkedException(e);
      throw(jE);
    }
  }


  /**
   * Method creating a non durable <code>ConnectionConsumer</code> for this
   * connection.
   * <br>
   * This is an expert facility not used by regular JMS clients.
   *
   * @param topic  The Topic which messages will be consumed by this consumer.
   * @param messageSelector  The selector to filter the consumed messages.
   * @param sessionPool  The pool from which Sessions are got.
   * @param maxMessages  The number of messages passed to a given Session.
   */
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Topic topic,
    String messageSelector, javax.jms.ServerSessionPool sessionPool, int maxMessages)
    throws JMSException
  {
    if (this.connectionConsumer != null)
      throw (new JMSException("A ConnectionConsumer already exists for this Connection"));
    if (sessionPool == null)
      throw (new JMSException("ServerSessionPool parameter is null!"));

    try {
      if (messageSelector != null && ! messageSelector.equals("")) {
        fr.dyade.aaa.mom.selectors.checkParser parser =
          new fr.dyade.aaa.mom.selectors.checkParser(
          new fr.dyade.aaa.mom.selectors.Lexer(messageSelector));

        // If syntax is wrong, throws a javax.jms.InvalidSelectorException.
        Object result = parser.parse().value;
       }

      boolean durable = false;
      String subName = "";
      // Building the connectionConsumer.
      this.connectionConsumer = doCreateConnectionConsumer(topic, messageSelector,
        sessionPool, maxMessages, durable, subName);
      connectionConsumer.setConnection(this);
    } catch (JMSException jE) {
      throw(jE);
    } catch (Exception e) {
      javax.jms.JMSException jE = new javax.jms.JMSException("Internal error");
      jE.setLinkedException(e);
      throw jE;
    }

    return (javax.jms.ConnectionConsumer) this.connectionConsumer;
  }

  /**
   * Method creating a durable <code>ConnectionConsumer</code> for this
   * connection.
   * <br>
   * This is an expert facility not used by regular JMS clients.
   *
   * @param topic  The Topic which messages will be consumed by this consumer.
   * @param subscriptionName  The name of this consumer subscription.
   * @param messageSelector  The selector to filter the consumed messages.
   * @param sessionPool  The pool from which Sessions are got.
   * @param maxMessages  The number of messages passed to a given Session.
   *
   * @author Frederic Maistre
   */
  public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic,
    String subscriptionName, String messageSelector, javax.jms.ServerSessionPool sessionPool,
    int maxMessages) throws javax.jms.JMSException
  {
    if (sessionPool == null)
      throw (new JMSException("ServerSessionPool parameter is null!"));

    try {
      if (messageSelector != null && ! messageSelector.equals("")) {
        fr.dyade.aaa.mom.selectors.checkParser parser =
          new fr.dyade.aaa.mom.selectors.checkParser(
          new fr.dyade.aaa.mom.selectors.Lexer(messageSelector));

        // If syntax is wrong, throws a javax.jms.InvalidSelectorException.
        Object result = parser.parse().value;
      }

      boolean durable = true;
      // Building the connectionConsumer.
      this.connectionConsumer = doCreateConnectionConsumer(topic, messageSelector,
        sessionPool, maxMessages, durable, subscriptionName);
      connectionConsumer.setConnection(this);
    } catch (JMSException jE) {
      throw(jE);
    } catch (Exception e) {
      javax.jms.JMSException jE = new javax.jms.JMSException("Internal error");
      jE.setLinkedException(e);
      throw jE;
    }
 
    return (javax.jms.ConnectionConsumer) this.connectionConsumer;
  }
  

  /**
   * Method constructing a durable or non durable <code>ConnectionConsumer</code>.
   */
  private ConnectionConsumer doCreateConnectionConsumer (javax.jms.Topic topic,
    String selector, javax.jms.ServerSessionPool ssp, int maxMessages, boolean durable,
    String subscriptionName) throws JMSException
  {
    fr.dyade.aaa.mom.MessageMOMExtern subMsg;

    // Building the connectionConsumer.
    ConnectionConsumer cCons = new ConnectionConsumer(topic, selector, ssp, maxMessages);

    long requestID = super.getMessageMOMID();
    Long longRequestID = new Long(requestID); 
    boolean noLocal = false;
    String sessionID = "";
    int ackMode = 0;
    boolean cConsBool = true;

    // Creating a ConnectionConsumer on a Topic is as subscribing to this
    // Topic.
    if (! durable) {
      subMsg =
        new fr.dyade.aaa.mom.SubscriptionNoDurableMOMExtern(requestID,
        subscriptionName, (fr.dyade.aaa.mom.TopicNaming) topic, selector);
    }
    else {
      subMsg =
        new fr.dyade.aaa.mom.SubscriptionMessageMOMExtern(requestID,
        subscriptionName, (fr.dyade.aaa.mom.TopicNaming) topic, selector);
    }

    Object lock = new Object();
    synchronized (lock) {
      super.waitThreadTable.put(longRequestID, lock);
      this.sendMsgToAgentClient(subMsg);
      try {
        lock.wait();
      } catch (InterruptedException iE) {
        JMSException jE = new JMSException("Error while waiting for MOM acknowledgement");
        jE.setLinkedException(iE);
        throw (jE);
      }
    }

    fr.dyade.aaa.mom.MessageMOMExtern momMsg;

    if (!super.messageJMSMOMTable.containsKey(longRequestID))
      throw (new JMSException("Error in MOM's acknowledgement"));

    momMsg = (fr.dyade.aaa.mom.MessageMOMExtern) messageJMSMOMTable.remove(longRequestID);

    if (momMsg instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
      return cCons;
    }
    else
      throw (new JMSException("Error in MOM's acknowledgement"));  
  }

}
