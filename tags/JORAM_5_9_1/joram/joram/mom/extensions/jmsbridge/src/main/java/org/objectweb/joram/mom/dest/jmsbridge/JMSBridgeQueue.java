/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.dest.Queue;
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
import fr.dyade.aaa.agent.Notification;

/**
 * The <code>BridgeQueue</code> class implements a specific queue which
 * forwards the messages it receives to a foreign JMS destination, and
 * gets the messages it is requested to deliver from the same foreign
 * destination.
 * <p>
 * This queue is in fact a bridge linking JORAM and a foreign JMS server.
 */
@Deprecated
public class JMSBridgeQueue extends Queue {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The JMS module for accessing the foreign JMS destination. */
  private JMSBridgeModule jmsModule;

  /**
   * Table persisting the outgoing messages until acknowledgment by the
   * bridge module.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Value:</b> message
   */
  private Hashtable outTable;

  public JMSBridgeQueue() {
    fixed = true;
    // creates the table for outgoing messages.
    outTable = new Hashtable();
  }

  /**
   * Configures a <code>BridgeQueue</code> instance.
   * 
   * @param prop The initial set of properties.
   */
  public void setProperties(Properties prop, boolean firstTime) throws Exception {
    super.setProperties(prop, firstTime);
    // creates the JMS module for communication with external provider
    jmsModule = new JMSBridgeModule(prop);
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) throws Exception {
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

  /**
   * Specializes this <code>Queue</code> method for processing the
   * specific bridge notifications.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof JMSBridgeDeliveryNot) {
      bridgeDelivery(from, (JMSBridgeDeliveryNot) not);
    } else if (not instanceof JMSBridgeAckNot)
      bridgeAck((JMSBridgeAckNot) not);
    else
      super.react(from, not);
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    close();
  }

  public String toString() {
    return "BridgeQueue:" + getId().toString();
  }

  /**
   * Reacts to <code>BridgeDeliveryNot</code> notifications holding a message
   * received from the foreign JMS server.
   * 
   * @param from  AgentId
   * @param not   BridgeDeliveryNot
   */
  private void bridgeDelivery(AgentId from, JMSBridgeDeliveryNot not) {
    ClientMessages clientMessages = new ClientMessages();
    clientMessages.addMessage(not.getMessage());
    // it come from bridge, so set destId for from 
    //(do not preProcess this ClientMessage).
    try {
      super.doClientMessages(getId(), clientMessages, false);
    } catch (AccessException e) {/* never happens*/}
  }

  /**
   * Reacts to <code>BridgeAckNot</code> notifications holding the identifier
   * of a message successfully delivered to the foreign JMS server.
   * 
   * @param not BridgeAckNot
   */
  private void bridgeAck(JMSBridgeAckNot not) {
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
  public void receiveRequest(AgentId from, ReceiveRequest not) throws AccessException {
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

    if ((requests.size() - 1) == reqIndex) {
      // If the request has not been answered:
      if (not.getTimeOut() == -1) {
        // If it is an immediate delivery request, requesting the foreign JMS
        // destination for an immediate delivery.
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
        if (message != null)
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
    for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
      message = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
      message.order = arrivalState.getAndIncrementArrivalCount(message.isPersistent());

      outTable.put(message.getId(), message);

      try {
        jmsModule.send(message.getFullMessage());
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR,
                     "Failing sending to remote  destination: ", exc);

        outTable.remove(message.getId());
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
  
  private void close() {
    jmsModule.close(); 
  }
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }
  
}
