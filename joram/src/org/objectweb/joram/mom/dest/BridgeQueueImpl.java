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

import java.io.IOException;
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
import org.objectweb.joram.shared.selectors.Selector;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>BridgeQueueImpl</code> class implements a specific queue which
 * forwards the messages it receives to a foreign JMS destination, and 
 * gets the messages it is requested to deliver from the same foreign
 * destination.
 * <p>
 * This queue is in fact a bridge linking JORAM and a foreign JMS server,
 * and which is accessible through the PTP communication mode.
 */
public class BridgeQueueImpl extends QueueImpl {
  /** The JMS module for accessing the foreign JMS destination. */
  private BridgeUnifiedModule jmsModule;
  /**
   * Table persisting the outgoing messages until acknowledgement by the
   * bridge module.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private Hashtable outTable;

  /**
   * Constructs a <code>BridgeQueueImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public BridgeQueueImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);
    outTable = new Hashtable();

    String jmsMode = (String) prop.get("jmsMode");

    if (jmsMode == null)
      throw new IllegalArgumentException("Missing 'jmsMode' property");

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
    return "BridgeQueueImpl:" + destId.toString();
  }

  /**
   * Specializes this <code>QueueImpl</code> method for processing the
   * specific bridge notifications.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException {
    if (not instanceof BridgeDeliveryNot) {
      doReact((BridgeDeliveryNot) not);
    }
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
   * Method specializing the reaction to a <code>ReceiveRequest</code>
   * instance, requesting a message.
   * <p>
   * This method stores the request and requests a message through the JMS
   * interface.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  protected void doReact(AgentId from, ReceiveRequest not)
                 throws AccessException {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Storing the request:
    not.requester = from;
    not.setExpiration(System.currentTimeMillis());
    requests.add(not);

    // Launching a delivery sequence for this request:
    int reqIndex = requests.size() - 1;
    deliverMessages(reqIndex);

    // If the request has not been answered:
    if ((requests.size() - 1) == reqIndex) {
      // If it is an immediate delivery request, requesting the foreign JMS
      // destination for an immediate delivery.
      if (not.getTimeOut() == -1) {
        requests.remove(reqIndex);

        Message msg = null;

        try {
          msg = jmsModule.receiveNoWait();
        } catch (Exception exc) {
          // JMS module not properly initialized.
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                          "Failing receive request on remote "
                                          + "destination: " + exc);
        }

        // If message not null but not selected, setting it to null.
        if (msg != null && ! Selector.matches(msg, not.getSelector()))
          msg = null;

        QueueMsgReply reply = new QueueMsgReply(not);
        reply.addMessage(msg);
        Channel.sendTo(from, reply);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "Receive answered.");
      } else {
        // Else, requesting the foreign JMS destination for a delivery: 
        try {
          jmsModule.receive();
        } catch (Exception exc) {
          // JMS module not properly initialized.
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                          "Failing receive request on remote "
                                          + "destination: " + exc);
        }
      }
    }
  }

  /**
   * Method specializing the processing of a <code>ClientMessages</code>
   * instance.
   * <p>
   * This method sends the messages to the foreign JMS destination.
   */
  protected void doProcess(ClientMessages not) {
    // Sending each message:
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
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                        "Failing sending to remote "
                                        + "destination: "
                                        + exc);

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


  /** Deserializes a <code>BridgeQueueImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    messages = new Vector();
    deliveredMsgs = new Hashtable();

    // Retrieving the persisted messages, if any.
    Vector persistedMsgs = MessagePersistenceModule.loadAll(getDestinationId());

    // AF: This code is already in QueueImpl, it seems the message are
    // loaded 2 times !!
    if (persistedMsgs != null) {
      Message persistedMsg;
      AgentId consId;
      while (! persistedMsgs.isEmpty()) {
        persistedMsg = (Message) persistedMsgs.remove(0);
        consId = (AgentId) consumers.get(persistedMsg.getIdentifier());
        if (consId == null) {
          addMessage(persistedMsg);
        } else if (isLocal(consId)) {
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(
              BasicLevel.DEBUG, " -> deny " + persistedMsg.getIdentifier());
          consumers.remove(persistedMsg.getIdentifier());
          contexts.remove(persistedMsg.getIdentifier());
          addMessage(persistedMsg);
        } else {
          deliveredMsgs.put(persistedMsg.getIdentifier(), persistedMsg);
        }
      }
    }

    // Re-launching the JMS module.
    try {
      jmsModule.connect();

      // Re-emitting the receive requests:
      for (int i = 0; i < requests.size(); i++)
        jmsModule.receive();

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
