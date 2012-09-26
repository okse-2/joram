/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest.jmsbridge;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.TopicForwardNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;

/**
 * The <code>BridgeTopic</code> class implements a specific topic which
 * forwards the messages it receives to a foreign JMS destination, and
 * gets the messages it is requested to deliver from the same foreign
 * destination.
 * <p>
 * This topic is in fact a bridge linking JORAM and a foreign JMS server, and
 * which is accessible through the Pub/Sub communication mode.
 */
public class JMSBridgeTopic extends Topic {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The JMS module for accessing the foreign JMS destination. */
  private JMSBridgeModule jmsModule;

  /** Counter for keeping the original delivery order. */
  private long arrivalsCounter = 0;

  /**
   * Table persisting the outgoing messages until acknowledgment by the
   * bridge module.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private Hashtable outTable;

  public JMSBridgeTopic() {
    fixed = true;
    // creates the table for outgoing messages.
    outTable = new Hashtable();
  }

  /**
   * Configures a <code>BridgeTopic</code> instance.
   * 
   * @param prop The initial set of properties.
   */
  public void setProperties(Properties prop, boolean firstTime) throws Exception {
    super.setProperties(prop, firstTime);
    // creates the table for outgoing messages.
    jmsModule = new JMSBridgeModule(prop);
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "initialize(" + firstTime + ')');

    // initialize the destination
    super.initialize(firstTime);

    // Initializing the JMS module.
    jmsModule.init(getId());

    // Re-launching the JMS module.
    try {
      jmsModule.connect();

      if (! subscribers.isEmpty())
        jmsModule.setMessageListener();

      // Re-emetting the pending messages:
      Message momMsg;
      Vector outMessages = new Vector();
      Message currentMsg;
      for (Enumeration keys = outTable.keys(); keys.hasMoreElements();) {
        momMsg = (Message) outTable.get(keys.nextElement());

        int i = 0;
        while (i < outMessages.size()) {
          currentMsg = (Message) outMessages.get(i);

          if (momMsg.order < currentMsg.order)
            break;

          i++;
        }
        outMessages.insertElementAt(momMsg, i);
      }

      while (! outMessages.isEmpty()) {
        momMsg = (Message) outMessages.remove(0);
        jmsModule.send(momMsg.getFullMessage());
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
    }
  }

  /**
   * Specializes this <code>Topic</code> method for processing the
   * specific bridge notifications.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof JMSBridgeDeliveryNot)
      bridgeDeliveryNot(from, (JMSBridgeDeliveryNot) not);
    else if (not instanceof JMSBridgeAckNot)
      bridgeAckNot((JMSBridgeAckNot) not);
    else
      super.react(from, not);
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    close();
  }

  public String toString() {
    return "BridgeTopic:" + getId().toString();
  }

  /**
   * Reacts to <code>BridgeDeliveryNot</code> notifications holding a message
   * received from the foreign JMS server.
   */
  private void bridgeDeliveryNot(AgentId from, JMSBridgeDeliveryNot not) {
    ClientMessages clientMessages = new ClientMessages();
    clientMessages.addMessage(not.getMessage());
    super.doClientMessages(getId(), clientMessages);
  }

  /**
   * Reacts to <code>BridgeAckNot</code> notifications holding the identifier
   * of a message successfuly delivered to the foreign JMS server.
   */
  private void bridgeAckNot(JMSBridgeAckNot not) {
    outTable.remove(not.getIdentifier());
  }

  /**
   * Method specializing the reaction to a <code>SubscribeRequest</code>
   * instance.
   * <p>
   * This method sets, if needed, a JMS listener on the foreign JMS consumer.
   *
   * @exception AccessException  If the sender is not a READER.
   */
  public void postSubscribe() {
    // First subscription: setting a listener on the foreign JMS consumer.
    try {
      if (subscribers.size() == 1) 
        jmsModule.setMessageListener();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
                   "Failing subscribe request on remote destination: ", exc);
    }
  }

  /**
   * Method specializing the reaction to an <code>UnsubscribeRequest</code>
   * instance.
   * <p>
   * This method unsets, if needed, the JMS listener on the foreign
   * JMS consumer.
   *
   */
  public void preUnsubscribe() {
    // Last subscription: removing the JMS listener.
    if (subscribers.size() == 1)
      jmsModule.unsetMessageListener();
  } 

  /**
   * Method specializing the reaction to a <code>TopicForwardNot</code>
   * instance, carrying messages forwarded by a cluster fellow or a
   * hierarchical son.
   * <p>
   * This method forwards the messages, if needed, to the hierarchical father,
   * and to the foreign JMS destination.
   */
  public void topicForwardNot(AgentId from, TopicForwardNot not) {
    // If the forward comes from a son, forwarding it to the father, if any.
    if (not.fromCluster && fatherId != null)
      forward(fatherId, not);

    // Sending the received messages to the foreign JMS destination:
    Message message;
    DMQManager dmqManager = null;
    for (Iterator msgs = not.messages.getMessages().iterator(); msgs.hasNext();) {
      // AF: TODO it seems not useful to transform the message !!
      message = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
      message.order = arrivalsCounter++;

      outTable.put(message.getIdentifier(), message);

      try {
        jmsModule.send(message.getFullMessage());
      } catch (Exception exc) {
        outTable.remove(message.getIdentifier());
        if (dmqManager == null) {
          dmqManager = new DMQManager(dmqId, getId());
        }
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNEXPECTED_ERROR);
      }
    }
    if (dmqManager != null) {
      dmqManager.sendToDMQ();
    }
  }

  /**
   * Method specializing the reaction to a <code>ClientMessages</code>
   * instance.
   * <p>
   * This method may forward the messages to the topic father if any, or
   * to the cluster fellows if any, and to the foreign JMS destination.
   */
  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    if (getId().equals(from)) return not;

    // Forwarding the messages to the father or the cluster fellows, if any:
    forwardMessages(not);

    // Sending the received messages to the foreign JMS destination:
    Message message;
    DMQManager dmqManager = null;
    for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
      message = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
      message.order = arrivalsCounter++;
      outTable.put(message.getIdentifier(), message);

      try {
        jmsModule.send(message.getFullMessage());
      } catch (Exception exc) {
        outTable.remove(message.getIdentifier());
        if (dmqManager == null) {
          dmqManager = new DMQManager(not.getDMQId(), dmqId, getId());
        }
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
      }
    }
    if (dmqManager != null) {
      dmqManager.sendToDMQ();
    }
    return null;
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * This method closes the JMS resources used for connecting to the foreign
   * JMS server.
   */
  protected void doDeleteNot(DeleteNot not) {
    jmsModule.close(); 
    super.doDeleteNot(not);
  }
  

  private void close() {
    jmsModule.close();
  }
}
