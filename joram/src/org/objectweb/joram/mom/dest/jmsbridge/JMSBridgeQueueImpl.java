/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;

/**
 * The <code>BridgeQueueImpl</code> class implements a specific queue which
 * forwards the messages it receives to a foreign JMS destination, and 
 * gets the messages it is requested to deliver from the same foreign
 * destination.
 * <p>
 * This queue is in fact a bridge linking JORAM and a foreign JMS server.
 */
public class JMSBridgeQueueImpl extends QueueImpl {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The JMS module for accessing the foreign JMS destination. */
  private JMSBridgeModule jmsModule;

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
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public JMSBridgeQueueImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    // creates the table for outgoing messages.
    outTable = new Hashtable();
    // creates the JMS module for communication with external provider
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

      // Re-emitting the receive requests:
      for (int i = 0; i < requests.size(); i++)
        jmsModule.receive();

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

  public String toString() {
    return "BridgeQueueImpl:" + getId().toString();
  }

  /**
   * Reacts to <code>BridgeDeliveryNot</code> notifications holding a message
   * received from the foreign JMS server.
   * 
   * @param from  AgentId
   * @param not   BridgeDeliveryNot
   */
  public void bridgeDelivery(AgentId from, JMSBridgeDeliveryNot not) {
    ClientMessages clientMessages = new ClientMessages();
    clientMessages.addMessage(not.getMessage());
    // it come from bridge, so set destId for from 
    //(do not preProcess this ClientMessage).
    super.doClientMessages(getId(), clientMessages);
  }

  /**
   * Reacts to <code>BridgeAckNot</code> notifications holding the identifier
   * of a message successfuly delivered to the foreign JMS server.
   * 
   * @param not BridgeAckNot
   */
  public void bridgeAck(JMSBridgeAckNot not) {
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
  public void receiveRequest(AgentId from, ReceiveRequest not)
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

        org.objectweb.joram.shared.messages.Message message = null;

        try {
          message = jmsModule.receiveNoWait();
        } catch (Exception exc) {
          // JMS module not properly initialized.
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR,
                       "Failing receive request on remote destination: ", exc);
        }

        // If message not null but not selected, setting it to null.
        if ((message != null) &&
            ! Selector.matches(message, not.getSelector()))
          message = null;

        QueueMsgReply reply = new QueueMsgReply(not);
        reply.addMessage(message);
        forward(from, reply);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
          "Receive answered.");
      } else {
        // Else, requesting the foreign JMS destination for a delivery: 
        try {
          jmsModule.receive();
        } catch (Exception exc) {
          // JMS module not properly initialized.
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR,
                       "Failing receive request on remote destination: ", exc);
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
  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    if (getId().equals(from))
      return not;

    // Sending each message:
    Message message;
    for (Enumeration msgs = not.getMessages().elements();
    msgs.hasMoreElements();) {
      message = new Message((org.objectweb.joram.shared.messages.Message) msgs.nextElement());
      message.order = arrivalsCounter++;

      outTable.put(message.getIdentifier(), message);

      try {
        jmsModule.send(message.getFullMessage());
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR,
                     "Failing sending to remote  destination: ", exc);

        outTable.remove(message.getIdentifier());
        DMQManager dmqManager = new DMQManager(not.getDMQId(), dmqId, getId());
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNEXPECTED_ERROR);
        dmqManager.sendToDMQ();
      }
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
}
