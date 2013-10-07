/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.mom.util;

import java.util.Date;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;

/**
 * The <code>DMQManager</code> is made to stock the dead messages before sending
 * them to the dead message queue, only if such a queue is defined.
 */
public class DMQManager {

  private ClientMessages deadMessages = null;
  
  private AgentId destDmqId = null;
  
  private AgentId senderId = null;
  
  public static Logger logger = Debug.getLogger(DMQManager.class.getName());

  /**
   * Creates a DMQManager. The <code>specificDmq</code> is used in priority. If
   * <code>null</code>, destination DMQ is used if it exists, else default DMQ
   * is used. If none exists, dead messages will be lost.
   * 
   * @param specificDmq
   *          Identifier of the dead message queue to use in priority.
   * @param currentDestDmq
   *          The DMQ of the destination
   * @param senderId
   *          The id of the destination. This is used to avoid sending to
   *          itself.
   */
  public DMQManager(AgentId specificDmq, AgentId currentDestDmq, AgentId senderId) {
    if (specificDmq != null) {
      // Sending the dead messages to the provided DMQ
      destDmqId = specificDmq;
    } else if (currentDestDmq != null) {
      // Sending the dead messages to the destination's DMQ
      destDmqId = currentDestDmq;
    } else {
      // Sending the dead messages to the server's default DMQ
      destDmqId = Queue.getDefaultDMQId();
    }
    this.senderId = senderId;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this.getClass().getName() + " created, destDmqId: " + destDmqId);
  }

  /**
   * Creates a DMQManager. Destination DMQ is used if it exists, else default
   * DMQ is used. If none exists, dead messages will be lost
   * 
   * @param currentDestDmq
   *          The DMQ of the destination
   * @param senderId
   *          The id of the destination. This is used to avoid sending to
   *          itself.
   */
  public DMQManager(AgentId currentDestDmq, AgentId senderId) {
    this(null, currentDestDmq, senderId);
  }

  /**
   * Stocks a dead message waiting to be sent to the DMQ. If no DMQ was found at
   * creation time, the message is lost.
   * 
   * @param mess
   *          The message to stock
   * @param reason
   *          The reason explaining why the message has to be send to the DMQ.
   *          It can be one of the following: <code>EXPIRED</code>,
   *          <code>NOT_WRITEABLE</code>, <code>UNDELIVERABLE</code>,
   *          <code>ADMIN_DELETED</code>, <code>DELETED_DEST</code>,
   *          <code>QUEUE_FULL</code> or <code>UNEXPECTED_ERROR</code>.
   */
  public void addDeadMessage(Message mess, short reason) {

    if (destDmqId != null) {
      String ERROR_COUNT = "JMS_JORAM_ERRORCOUNT";
      Integer errorCount = (Integer) mess.getProperty(ERROR_COUNT);
      if (errorCount == null) {
        errorCount = new Integer(1);
      } else {
        errorCount = new Integer(errorCount.intValue() + 1);
      }
      String causePropertyName = "JMS_JORAM_ERRORCAUSE_" + errorCount;
      String codePropertyName = "JMS_JORAM_ERRORCODE_" + errorCount;
      mess.setProperty(ERROR_COUNT, errorCount);

      switch (reason) {
      case MessageErrorConstants.EXPIRED:
        mess.setProperty(causePropertyName, "Expired at " + new Date(mess.expiration));
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.EXPIRED));
        break;
      case MessageErrorConstants.NOT_WRITEABLE:
        mess.setProperty(causePropertyName, "Destination is not writable");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.NOT_WRITEABLE));
        break;
      case MessageErrorConstants.UNDELIVERABLE:
        mess.setProperty(causePropertyName, "Undeliverable after " + mess.deliveryCount + " tries");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.UNDELIVERABLE));
        break;
      case MessageErrorConstants.ADMIN_DELETED:
        mess.setProperty(causePropertyName, "Message deleted by an admin");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.ADMIN_DELETED));
        break;
      case MessageErrorConstants.DELETED_DEST:
        mess.setProperty(causePropertyName, "Deleted destination");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.DELETED_DEST));
        break;
      case MessageErrorConstants.QUEUE_FULL:
        mess.setProperty(causePropertyName, "Queue full");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.QUEUE_FULL));
        break;
      case MessageErrorConstants.UNEXPECTED_ERROR:
        mess.setProperty(causePropertyName, "Unexpected error");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.UNEXPECTED_ERROR));
        break;
      case MessageErrorConstants.INTERCEPTORS:
        mess.setProperty(causePropertyName, "Interceptors");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.INTERCEPTORS));
        break;
      case MessageErrorConstants.NOT_ALLOWED:
        mess.setProperty(causePropertyName, "Operation is not allowed");
        mess.setProperty(codePropertyName, new Short(MessageErrorConstants.NOT_ALLOWED));
        break;
      default:
        break;
      }

      if (deadMessages == null) {
        deadMessages = new ClientMessages();
      }
      mess.expiration = 0;
      deadMessages.addMessage(mess);
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this.getClass().getName() + ", addDeadMessage for dmq: " + destDmqId + ". Msg: " + mess);
  }

  /**
   * Sends previously stocked messages to the appropriate DMQ.
   */
  public void sendToDMQ() {
    if (deadMessages != null) {
      deadMessages.setExpiration(0);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this.getClass().getName() + ", sendToDMQ " + destDmqId);
      if (destDmqId != null && !destDmqId.equals(senderId)) {
        Channel.sendTo(destDmqId, deadMessages);
      } else {
        // Else it means that the dead message queue is
        // the queue itself: drop the messages.
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, this.getClass().getName() + ", can't send to itself, messages dropped");
      }
    }
  }
}