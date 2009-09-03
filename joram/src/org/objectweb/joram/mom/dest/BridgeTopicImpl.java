/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.mom.util.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.Message;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>BridgeTopicImpl</code> class implements a specific topic which
 * forwards the messages it receives to a foreign JMS destination, and 
 * gets the messages it is requested to deliver from the same foreign
 * destination.
 * <p>
 * This topic is in fact a bridge linking JORAM and a foreign JMS server,
 * and which is accessible through the Pub/Sub communication mode.
 */
public class BridgeTopicImpl extends TopicImpl {
  /** The JMS module for accessing the foreign JMS destination. */
  private BridgeUnifiedModule jmsModule;

  /** Counter for keeping the original delivery order. */
  private long arrivalsCounter = 0;

  /**
   * Table persisting the outgoing messages until acknowledgement by the
   * bridge module.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private Hashtable outTable;

  /**
   * Constructs a <code>BridgeTopicImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the topic.
   * @param adminId  Identifier of the administrator of the topic.
   * @param prop     The initial set of properties.
   */
  public BridgeTopicImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);
    outTable = new Hashtable();

    String jmsMode = (String) prop.get("jmsMode");

    if (jmsMode == null)
      throw new IllegalArgumentException("Missing jmsMode property");

    if (jmsMode.equalsIgnoreCase("PTP"))
      jmsModule = new BridgePtpModule();
    else if (jmsMode.equalsIgnoreCase("PubSub"))
      jmsModule = new BridgePubSubModule();
    else if (jmsMode.equalsIgnoreCase("Unified"))
      jmsModule = new BridgeUnifiedModule();
    else
      throw new IllegalArgumentException("Invalid jmsMode value: " + jmsMode);

    // Initializing the JMS module.
    jmsModule.init(destId, prop);
  }

  public String toString() {
    return "BridgeTopicImpl:" + destId.toString();
  }

  /**
   * Specializes this <code>TopicImpl</code> method for processing the
   * specific bridge notifications.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException {
    if (not instanceof BridgeDeliveryNot)
      doReact((BridgeDeliveryNot) not);
    else if (not instanceof BridgeAckNot)
      doReact((BridgeAckNot) not);
    else
      super.react(from, not);
  }

  /**
   * Reacts to <code>BridgeDeliveryNot</code> notifications holding a message
   * received from the foreign JMS server.
   */
  protected void doReact(BridgeDeliveryNot not) {
    ClientMessages clientMessages = new ClientMessages();
    clientMessages.addMessage(not.getMessage());
    super.doProcess(clientMessages);
  }

  /**
   * Reacts to <code>BridgeAckNot</code> notifications holding the identifier
   * of a message successfuly delivered to the foreign JMS server.
   */
  protected void doReact(BridgeAckNot not) {
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
  protected void doReact(AgentId from, SubscribeRequest not)
                 throws AccessException {
    super.doReact(from, not);

    // First subscription: setting a listener on the foreign JMS consumer.
    try {
      if (subscribers.size() == 1) 
        jmsModule.setMessageListener();
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                      "Failing subscribe request on remote "
                                      + "destination: " + exc);
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
  protected void doReact(AgentId from, UnsubscribeRequest not) {
    // Last subscription: removing the JMS listener.
    if (subscribers.isEmpty())
      jmsModule.unsetMessageListener();

    super.doReact(from, not);
  } 

  /**
   * Method specializing the reaction to a <code>TopicForwardNot</code>
   * instance, carrying messages forwarded by a cluster fellow or a
   * hierarchical son.
   * <p>
   * This method forwards the messages, if needed, to the hierarchical father,
   * and to the foreign JMS destination.
   */
  protected void doReact(AgentId from, TopicForwardNot not) {
    // If the forward comes from a son, forwarding it to the father, if any.
    if (not.toFather && fatherId != null)
      Channel.sendTo(fatherId, not);
    
    // Sending the received messages to the foreign JMS destination:
    Message msg;
    for (Enumeration msgs = not.messages.getMessages().elements();
         msgs.hasMoreElements();) {

      if (arrivalsCounter == Long.MAX_VALUE)
        arrivalsCounter = 0;

      msg = (Message) msgs.nextElement();
      msg.order = arrivalsCounter++;

      outTable.put(msg.getIdentifier(), msg);

      try  {
        jmsModule.send(msg);
      } catch (Exception exc) {
        outTable.remove(msg.getIdentifier());
        ClientMessages deadM = new ClientMessages();
        deadM.addMessage(msg);
        sendToDMQ(deadM, null);
      }
    }
  }

  /**
   * Method specializing the reaction to a <code>ClientMessages</code>
   * instance.
   * <p>
   * This method may forward the messages to the topic father if any, or
   * to the cluster fellows if any, and to the foreign JMS destination.
   */
  protected void doProcess(ClientMessages not) {
    // Forwarding the messages to the father or the cluster fellows, if any:
    forwardMessages(not);

    // Sending the received messages to the foreign JMS destination:
    Message msg;
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      
      if (arrivalsCounter == Long.MAX_VALUE)
        arrivalsCounter = 0;

      msg = (Message) msgs.nextElement();
      msg.order = arrivalsCounter++;

      outTable.put(msg.getIdentifier(), msg);

      try {
        jmsModule.send(msg);
      } catch (Exception exc) {
        outTable.remove(msg.getIdentifier());
        ClientMessages deadM;
        deadM = new ClientMessages(not.getClientContext(), not.getRequestId());
        deadM.addMessage(msg);
        sendToDMQ(deadM, not.getDMQId());
      }
    }
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * This method closes the JMS resources used for connecting to the foreign
   * JMS server.
   */
  protected void doProcess(DeleteNot not) {
    jmsModule.close(); 
    super.doProcess(not);
  }

  /** Deserializes a <code>BridgeTopicImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();

    // Re-launching the JMS module.
    try {
      jmsModule.connect();

      if (! subscribers.isEmpty())
        jmsModule.setMessageListener();

      // Re-emetting the pending messages:
      Message msg;
      Vector outMessages = new Vector();
      Message currentMsg;
      for (Enumeration keys = outTable.keys(); keys.hasMoreElements();) {
        msg = (Message) outTable.get(keys.nextElement());
  
        int i = 0;
        while (i < outMessages.size()) {
          currentMsg = (Message) outMessages.get(i);
  
          if (msg.order < currentMsg.order)
            break;
  
          i++;
        }
        outMessages.insertElementAt(msg, i);
      }

      while (! outMessages.isEmpty()) {
        msg = (Message) outMessages.remove(0);
        jmsModule.send(msg);
      }
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR, "" + exc);
    }
  }
}
