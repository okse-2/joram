/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AbortReceiveRequest;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.AdminReply;
import org.objectweb.joram.mom.notifications.BrowseReply;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.DestinationAdminRequestNot;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettings;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettingsRep;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsg;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsgRep;
import org.objectweb.joram.mom.notifications.Monit_GetNumberRep;
import org.objectweb.joram.mom.notifications.Monit_GetPendingMessages;
import org.objectweb.joram.mom.notifications.Monit_GetPendingRequests;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.SetNbMaxMsgRequest;
import org.objectweb.joram.mom.notifications.SetRightRequest;
import org.objectweb.joram.mom.notifications.SetThreshRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.admin.ClearQueue;
import org.objectweb.joram.shared.admin.DeleteQueueMessage;
import org.objectweb.joram.shared.admin.GetQueueMessage;
import org.objectweb.joram.shared.admin.GetQueueMessageIds;
import org.objectweb.joram.shared.admin.GetQueueMessageIdsRep;
import org.objectweb.joram.shared.admin.GetQueueMessageRep;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.DestinationException;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.util.Debug;

/**
 * The <code>QueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class QueueImpl extends DestinationImpl implements QueueImplMBean {
  public static Logger logger = Debug.getLogger(QueueImpl.class.getName());

  /** period to run task at regular interval: cleaning, load-balancing, etc. */
  protected long period = -1;

  /**
   * Returns  the period value of this queue, -1 if not set.
   *
   * @return the period value of this queue; -1 if not set.
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets or unsets the period for this queue.
   *
   * @param period The period value to be set or -1 for unsetting previous
   *               value.
   */
  public void setPeriod(long period) {
    if ((this.period == -1L) && (period != -1L)) {
      // Schedule the CleaningTask.
      forward(destId, new WakeUpNot());
    }
    this.period = period;
  }

  /**
   * Threshold above which messages are considered as undeliverable because
   * constantly denied; 0 stands for no threshold, <code>null</code> for value
   * not set.
   */
  private Integer threshold = null;

  /**
   * Returns  the threshold value of this queue, -1 if not set.
   *
   * @return the threshold value of this queue; -1 if not set.
   */
  public int getThreshold() {
    if (threshold == null)
      return -1;
    else
      return threshold.intValue();
  }

  /**
   * Sets or unsets the threshold for this queue.
   *
   * @param The threshold value to be set (-1 for unsetting previous value).
   */
  public void setThreshold(int threshold) {
    if (threshold < 0)
      this.threshold = null;
    else
      this.threshold = new Integer(threshold);
  }

  /** <code>true</code> if all the stored messages have the same priority. */
  private boolean samePriorities;
  /** Common priority value. */
  private int priority; 

  /** Table keeping the messages' consumers identifiers. */
  protected Hashtable consumers;
  /** Table keeping the messages' consumers contexts. */
  protected Hashtable contexts;

  /** Counter of messages arrivals. */
  protected long arrivalsCounter = 0;

  /**
   * Returns the number of messages received since creation time.
   *
   * @return The number of received messages.
   */
  public int getMessageCounter() {
    if (messages != null) {
      return messages.size();
    }
    return 0;
  }

  /** Vector holding the requests before reply or expiry. */
  protected Vector requests;

  /**
   * Cleans the waiting request list.
   * Removes all request that the expiration time is less than the time
   * given in parameter.
   *
   * @param currentTime The current time.
   */
  protected void cleanWaitingRequest(long currentTime) {
    int index = 0;
    while (index < requests.size()) {
      if (! ((ReceiveRequest) requests.get(index)).isValid(currentTime)) {
        // Request expired: removing it
        requests.remove(index);
        // It's not really necessary to save its state, in case of failure
        // a similar work will be done at restart.
      } else {
        index++;
      }
    }
  }

  /**
   * Returns the number of waiting requests in the queue.
   *
   * @return The number of waiting requests.
   */
  public int getWaitingRequestCount() {
    if (requests != null) { 
      cleanWaitingRequest(System.currentTimeMillis());
      return requests.size();
    }
    return 0;
  }

  /** <code>true</code> if the queue is currently receiving messages. */
  protected transient boolean receiving = false;

  /** Vector holding the messages before delivery. */
  protected transient Vector messages;

  /**
   * Cleans the pending messages list.
   * Removes all messages that the expiration time is less than the time
   * given in parameter.
   *
   * @param currentTime The current time.
   * @return		A vector of all expired messages.
   */
  protected ClientMessages cleanPendingMessage(long currentTime) {
    int index = 0;

    ClientMessages deadMessages = null;

    Message message = null;
    while (index < messages.size()) {
      message = (Message) messages.get(index);
      if (! message.isValid(currentTime)) {
        messages.remove(index);
        message.delete();
        
        message.msg.expired = true;

        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Removes expired message " + message.getIdentifier());
      } else {
        index++;
      }
    }
    return deadMessages;
  }

  /**
   * Returns the number of pending messages in the queue.
   *
   * @return The number of pending messages.
   */
  public int getPendingMessageCount() {
    if (messages != null) {
      return messages.size();
    }
    return 0;
  }

  /** Table holding the delivered messages before acknowledgement. */
  protected transient Hashtable deliveredMsgs;

  /**
   * Returns the number of messages delivered and waiting for acknowledge.
   *
   * @return The number of messages delivered.
   */
  public int getDeliveredMessageCount() {
    if (deliveredMsgs != null) {
      return deliveredMsgs.size();
    }
    return 0;
  }

  /** nb Max of Message store in queue (-1 no limit). */
  protected int nbMaxMsg = -1;

  /**
   * Returns the maximum number of message for the destination.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message for subscription if set;
   *	     -1 otherwise.
   */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /**
   * Sets the maximum number of message for the destination.
   *
   * @param nbMaxMsg the maximum number of message (-1 set no limit).
   */
  public void setNbMaxMsg(int nbMaxMsg) {
    // state change, so save.
    setSave();
    this.nbMaxMsg = nbMaxMsg;
  }

  /**
   * Constructs a <code>QueueImpl</code> instance.
   *
   * @param destId   Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public QueueImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);

    try {
      if (prop != null)
        period = Long.valueOf(prop.getProperty("period")).longValue();
    } catch (NumberFormatException exc) {
      period = -1L;
    }

    consumers = new Hashtable();
    contexts = new Hashtable();
    requests = new Vector();
  }

  /**
   * Returns a string representation of this destination.
   */
  public String toString() {
    return "QueueImpl:" + (destId == null ? "null" : destId.toString());
  }

  /**
   * wake up, and cleans the queue.
   */
  public void wakeUpNot(WakeUpNot not) {
    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);
     // Cleaning the possible expired messages.
    ClientMessages deadMessages = cleanPendingMessage(current);
    // If needed, sending the dead messages to the DMQ:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);
  }

  /**
   * Method implementing the reaction to a <code>SetThreshRequest</code>
   * instance setting the threshold value for this queue.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void setThreshRequest(AgentId from, SetThreshRequest req) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    // state change, so save.
    setSave();

    threshold = req.getThreshold();
    
    String info = strbuf.append("Request [").append(req.getClass().getName())
      .append("], sent to Queue [").append(destId)
      .append("], successful [true]: threshold [")
      .append(threshold).append("] set").toString();
    strbuf.setLength(0);
    forward(from, new AdminReply(req, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Method implementing the reaction to a <code>SetNbMaxMsgRequest</code>
   * instance setting the NbMaxMsg value for this queue.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void setNbMaxMsgRequest(AgentId from, SetNbMaxMsgRequest req) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    nbMaxMsg = req.getNbMaxMsg();
    
    String info = strbuf.append("Request [").append(req.getClass().getName())
      .append("], sent to Queue [").append(destId)
      .append("], successful [true]: nbMaxMsg [")
      .append(nbMaxMsg).append("] set").toString();
    strbuf.setLength(0);
    forward(from, new AdminReply(req, true, info));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, info);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method for sending back
   * the threshold along with the DMQ id.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void MonitGetDMQSettings(AgentId from, Monit_GetDMQSettings not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String id = null;
    if (dmqId != null)
      id = dmqId.toString();
    forward(from, new Monit_GetDMQSettingsRep(not, id, threshold));
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetPendingMessages</code> notification requesting the
   * number of pending messages.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetPendingMessages(AgentId from, Monit_GetPendingMessages not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    // Cleaning the possible expired messages.
    ClientMessages deadMessages = cleanPendingMessage(System.currentTimeMillis());
    // Sending the dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    forward(from, new Monit_GetNumberRep(not, messages.size()));
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetPendingRequests</code> notification requesting the
   * number of pending requests.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetPendingRequests(AgentId from, Monit_GetPendingRequests not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");


    forward(from, new Monit_GetNumberRep(not, getWaitingRequestCount()));
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetNbMaxMsg</code> notification requesting the
   * number max of messages in this queue.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  public void monitGetNbMaxMsg(AgentId from, Monit_GetNbMaxMsg not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    forward(from, new Monit_GetNbMaxMsgRep(not,nbMaxMsg));
  }

  /**
   * Method implementing the reaction to a <code>ReceiveRequest</code>
   * instance, requesting a message.
   * <p>
   * This method stores the request and launches a delivery sequence.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  public void receiveRequest(AgentId from, ReceiveRequest not) throws AccessException {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    String[] toAck = not.getMessageIds();
    if (toAck != null) {
      for (int i = 0; i < toAck.length; i++) {
        acknowledge(toAck[i]);
      }
    }

    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);
    // Storing the request:
    not.requester = from;
    not.setExpiration(current);
    if (not.isPersistent()) {
      // state change, so save.
      setSave();
    }
    requests.add(not);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> requests count = " + requests.size());

    // Launching a delivery sequence for this request:
    int reqIndex = requests.size() - 1;
    deliverMessages(reqIndex);
    
    // If the request has not been answered and if it is an immediate
    // delivery request, sending a null:
    if ((requests.size() - 1) == reqIndex && not.getTimeOut() == -1) {
      requests.remove(reqIndex);
      QueueMsgReply reply = new QueueMsgReply(not);
      if (isLocal(from)) {
        reply.setPersistent(false);
      }
      forward(from, reply);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Receive answered by a null.");
    }
  }

  /**
   * Method implementing the queue reaction to a <code>BrowseRequest</code>
   * instance, requesting an enumeration of the messages on the queue.
   * <p>
   * The method sends a <code>BrowseReply</code> back to the client. Expired
   * messages are sent to the DMQ.
   *
   * @exception AccessException  If the requester is not a reader.
   */
  public void browseRequest(AgentId from, BrowseRequest not) throws AccessException {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Building the reply:
    BrowseReply rep = new BrowseReply(not);
    
    // Cleaning the possible expired messages.
    ClientMessages deadMessages = cleanPendingMessage(System.currentTimeMillis());
    // Adding the deliverable messages to it:
    int i = 0;
    Message message;
    while (i < messages.size()) {
      message = (Message) messages.get(i);
      if (Selector.matches(message.msg, not.getSelector())) {
        // Matching selector: adding the message:
        rep.addMessage(message.msg);
      }
      i++;
    }

    // Sending the dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    // Delivering the reply:
    forward(from, rep);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Method implementing the reaction to an <code>AcknowledgeRequest</code>
   * instance, requesting messages to be acknowledged.
   */
  public void acknowledgeRequest(AgentId from, AcknowledgeRequest not) {
    for (Enumeration ids = not.getIds(); ids.hasMoreElements();) {
      String msgId = (String) ids.nextElement();
      acknowledge(msgId);
    }
  }
  
  private void acknowledge(String msgId) {
    Message msg = (Message) deliveredMsgs.remove(msgId);
    if ((msg != null) && msg.getPersistent()) {
      // state change, so save.
      setSave();
    }
    consumers.remove(msgId);
    contexts.remove(msgId);
    if (msg != null) {
      msg.delete();

      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Message " + msgId + " acknowledged.");
      }
    } else if (logger.isLoggable(BasicLevel.WARN)) {
      logger.log(BasicLevel.WARN,
                 "Message " + msgId + " not found for acknowledgement.");
    }
  }

  /**
   * Method implementing the reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * This method denies the messages and launches a delivery sequence.
   * Messages considered as undeliverable are sent to the DMQ.
   */
  public void denyRequest(AgentId from, DenyRequest not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "QueueImpl.DenyRequest(" + from + ',' + not + ')');
    
    Enumeration ids = not.getIds();

    String msgId;
    Message message;
    AgentId consId;
    int consCtx;
    ClientMessages deadMessages = null;

    // If the deny request is empty, the denying is a contextual one: it
    // requests the denying of all the messages consumed by the denier in
    // the denying context:
    if (! ids.hasMoreElements()) {
      // Browsing the delivered messages:
      for (Enumeration delIds = deliveredMsgs.keys();
           delIds.hasMoreElements();) {
        msgId = (String) delIds.nextElement();

        message = (Message) deliveredMsgs.get(msgId);
        consId = (AgentId) consumers.get(msgId);
        consCtx = ((Integer) contexts.get(msgId)).intValue();

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     " -> deny msg " + msgId + "(consId = " + consId + ')');

        // If the current message has been consumed by the denier in the same
        // context: denying it.
        if (consId.equals(from) && consCtx == not.getClientContext()) {
          // state change, so save.
          setSave();
          consumers.remove(msgId);
          contexts.remove(msgId);
          deliveredMsgs.remove(msgId);
          message.msg.redelivered = true;

          // If message considered as undeliverable, adding
          // it to the vector of dead messages:
          if (isUndeliverable(message)) {
            message.delete();
            message.msg.undeliverable = true;

            if (deadMessages == null)
              deadMessages = new ClientMessages();
            deadMessages.addMessage(message.msg);
          } else {
            // Else, putting the message back into the deliverables vector:
            storeMessage(message);
          }

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Message " + msgId + " denied.");
        }
      }
    }

    // For a non empty request, browsing the denied messages:
    for (ids = not.getIds(); ids.hasMoreElements();) {
      msgId = (String) ids.nextElement();
      message = (Message) deliveredMsgs.remove(msgId);

      // Message may have already been denied. For example, a proxy may deny
      // a message twice, first when detecting a connection failure - and
      // in that case it sends a contextual denying -, then when receiving 
      // the message from the queue - and in that case it also sends an
      // individual denying.
      if (message == null) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, " -> already denied message " + msgId);
        break;
      }

      message.msg.redelivered = true;


      if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> deny " + msgId);

      // state change, so save.
      setSave();
      consumers.remove(msgId);
      contexts.remove(msgId);

      // If message considered as undeliverable, adding it
      // to the vector of dead messages:
      if (isUndeliverable(message)) {
        message.delete();

        message.msg.undeliverable = true;

        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);
      } else {
        // Else, putting the message back into the deliverables vector:
        storeMessage(message);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message " + msgId + " denied.");
    }
    // Sending the dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  public void abortReceiveRequest(AgentId from, 
                         AbortReceiveRequest not) {
    for (int i = 0; i < requests.size(); i++) {
      ReceiveRequest request = (ReceiveRequest) requests.get(i);
      if (request.requester.equals(from) &&
          request.getClientContext() == not.getClientContext() &&
          request.getRequestId() == not.getAbortedRequestId()) {
        if (not.isPersistent()) {
          // state change, so save.
          setSave();
        }
        requests.remove(i);
        break;
      }
    }
  }

  public void destinationAdminRequestNot(AgentId from, DestinationAdminRequestNot not) {
    org.objectweb.joram.shared.admin.AdminRequest adminRequest = 
      not.getRequest();
    if (adminRequest instanceof GetQueueMessageIds) {
      getQueueMessageIds((GetQueueMessageIds)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof GetQueueMessage) {
      getQueueMessage((GetQueueMessage)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof DeleteQueueMessage) {
      deleteQueueMessage((DeleteQueueMessage)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    } else if (adminRequest instanceof ClearQueue) {
      clearQueue((ClearQueue)adminRequest,
              not.getReplyTo(),
              not.getRequestMsgId(),
              not.getReplyMsgId());
    }
  }

  private void getQueueMessageIds(GetQueueMessageIds request,
                                  AgentId replyTo,
                                  String requestMsgId,
                                  String replyMsgId) {
    String[] res = new String[messages.size()];
    for (int i = 0; i < messages.size(); i++) {
      Message msg = (Message)messages.elementAt(i);
      res[i] = msg.getIdentifier();
    }
    GetQueueMessageIdsRep reply = 
      new GetQueueMessageIdsRep(res);
    replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
  }

  private void getQueueMessage(GetQueueMessage request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    Message message = null;
    for (int i = 0; i < messages.size(); i++) {
      message = (Message) messages.elementAt(i);
      if (message.getIdentifier().equals(request.getMessageId()))
        break;
    }
    if (message != null) {
      replyToTopic(new GetQueueMessageRep(message.msg),
                   replyTo, requestMsgId, replyMsgId);
    } else {
      
      replyToTopic(
        new org.objectweb.joram.shared.admin.AdminReply(
          false, "Message not found: " + message.getIdentifier()),
        replyTo, requestMsgId, replyMsgId);
    }
  }

  private void deleteQueueMessage(DeleteQueueMessage request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    for (int i = 0; i < messages.size(); i++) {
      Message message = (Message) messages.elementAt(i);
      if (message.getIdentifier().equals(request.getMessageId())) {
        messages.removeElementAt(i);
        message.delete();
        ClientMessages deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);
        sendToDMQ(deadMessages, null);
        break;
      }
    }
    replyToTopic(new org.objectweb.joram.shared.admin.AdminReply(true, null),
                 replyTo, requestMsgId, replyMsgId);
  }

  private void clearQueue(ClearQueue request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    if (messages.size() > 0) {
      ClientMessages deadMessages = new ClientMessages();
      for (int i = 0; i < messages.size(); i++) {
        Message message = (Message) messages.elementAt(i);
        message.delete();
        deadMessages.addMessage(message.msg);
      }
      sendToDMQ(deadMessages, null);
      messages.clear();
    }
    replyToTopic(new org.objectweb.joram.shared.admin.AdminReply(true, null),
                 replyTo, requestMsgId, replyMsgId);
  }
  
  /**
   * Method specifically processing a <code>SetRightRequest</code> instance.
   * <p>
   * When a reader is removed, and receive requests of this reader are still
   * on the queue, they are replied to by an <code>ExceptionReply</code>.
   */
  protected void doRightRequest(SetRightRequest not) {
    // If the request does not unset a reader, doing nothing.
    if (not.getRight() != -READ)
      return;

    SetRightRequest rightRequest = preProcess(not);
    if (rightRequest != null) {
      AgentId user = rightRequest.getClient();

      ReceiveRequest request;
      AccessException exc;
      ExceptionReply reply;

      // Free reading right has been removed; replying to the non readers
      // requests.
      if (user == null) {
        for (int i = 0; i < requests.size(); i++) {
          request = (ReceiveRequest) requests.get(i);
          if (! isReader(request.requester)) {
            exc = new AccessException("Free READ access removed");
            reply = new ExceptionReply(request, exc);
            forward(request.requester, reply);
            // state change, so save.
            setSave();
            requests.remove(i);
            i--;
          }
        }
      } else {
        // Reading right of a given user has been removed; replying to its
        // requests.
        for (int i = 0; i < requests.size(); i++) {
          request = (ReceiveRequest) requests.get(i);
          if (user.equals(request.requester)) {
            exc = new AccessException("READ right removed");
            reply = new ExceptionReply(request, exc);
            forward(request.requester, reply);
            // state change, so save.
            setSave();
            requests.remove(i);
            i--;
          }
        }
      }
      postProcess(rightRequest);
    }
  }
  
  /**
   * Method specifically processing a <code>ClientMessages</code> instance.
   * <p>
   * This method stores the messages and launches a delivery sequence.
   */
  protected void doClientMessages(AgentId from, ClientMessages not) {
    receiving = true;
    ClientMessages clientMsgs = preProcess(from, not);
    
    if (clientMsgs != null) {
      Message msg;
      // Storing each received message:
      for (Enumeration msgs = clientMsgs.getMessages().elements();
           msgs.hasMoreElements();) {

        msg = new Message((org.objectweb.joram.shared.messages.Message) msgs.nextElement());

        msg.order = arrivalsCounter++;
        storeMessage(msg);
        setSave();
      }
    }
    
    // Lauching a delivery sequence:
    deliverMessages(0);

    if (clientMsgs != null)
      postProcess(clientMsgs);
    
    receiving = false;
  }

  /**
   * Method specifically processing an <code>UnknownAgent</code> instance.
   * <p>
   * The specific processing is done when a <code>QueueMsgReply</code> was 
   * sent to a requester which does not exist anymore. In that case, the
   * messages sent to this requester and not yet acknowledged are marked as
   * "denied" for delivery to an other requester, and a new delivery sequence
   * is launched. Messages considered as undeliverable are removed and sent to
   * the DMQ.
   */ 
  protected void doUnknownAgent(UnknownAgent uA) {
    AgentId client = uA.agent;
    Notification not = uA.not;

    // If the notification is not a delivery, doing nothing. 
    if (! (not instanceof QueueMsgReply))
      return;

    String msgId;
    Message message;
    AgentId consId;
    ClientMessages deadMessages = null;
    for (Enumeration e = deliveredMsgs.keys(); e.hasMoreElements();) {
      msgId = (String) e.nextElement();
      message = (Message) deliveredMsgs.get(msgId);
      consId = (AgentId) consumers.get(msgId);
      // Delivered message has been delivered to the deleted client:
      // denying it.
      if (consId.equals(client)) {
        deliveredMsgs.remove(msgId);
        message.msg.redelivered = true;

        // state change, so save.
        setSave();
        consumers.remove(msgId);
        contexts.remove(msgId);

        // If message considered as undeliverable, adding it to the
        // vector of dead messages:
        if (isUndeliverable(message)) {
          message.delete();
          message.msg.undeliverable = true;
          if (deadMessages == null)
            deadMessages = new ClientMessages();
          deadMessages.addMessage(message.msg);
        } else {
          // Else, putting it back into the deliverables vector:
          storeMessage(message);
        }

        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN,
                     "Message " + message.getIdentifier() + " denied.");
      }
    }
    // Sending dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    // Launching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * <code>ExceptionReply</code> replies are sent to the pending receivers,
   * and the remaining messages are sent to the DMQ and deleted.
   */
  protected void doDeleteNot(DeleteNot not) {
    // Building the exception to send to the pending receivers:
    DestinationException exc = new DestinationException("Queue " + destId
                                                        + " is deleted.");
    ReceiveRequest rec;
    ExceptionReply excRep;
    // Sending it to the pending receivers:
    cleanWaitingRequest(System.currentTimeMillis());
    for (int i = 0; i < requests.size(); i++) {
      rec = (ReceiveRequest) requests.elementAt(i);

      excRep = new ExceptionReply(rec, exc);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Requester " + rec.requester +
                   " notified of the queue deletion.");
      forward(rec.requester, excRep);
    }
    // Sending the remaining messages to the DMQ, if needed:
    if (! messages.isEmpty()) {
      Message message;
      ClientMessages deadMessages = new ClientMessages();
      while (! messages.isEmpty()) {
        message = (Message) messages.remove(0);
        message.msg.deletedDest = true;
        deadMessages.addMessage(message.msg);
      }
      sendToDMQ(deadMessages, null);
    }

    // Deleting the messages:
    Message.deleteAll(getMsgTxname());
  }

  transient String msgTxname = null;

  protected final String getMsgTxname() {
    if (msgTxname == null)
      msgTxname = 'M' + getDestinationId() + '_';
    return msgTxname;
  }

  protected final void setMsgTxName(Message msg) {
    if (msg.getTxName() == null)
      msg.setTxName(getMsgTxname() + msg.order);
  }

  /**
   * Actually stores a message in the deliverables vector.
   *
   * @param message  The message to store.
   */
  protected final synchronized void storeMessage(Message message) {   
    addMessage(message);

    // Persisting the message.
    setMsgTxName(message);
    message.save();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Message " + message.getIdentifier() + " stored.");
    
  }

  protected final synchronized void addMessage(Message message) {
    nbMsgsReceiveSinceCreation++;

    if (nbMaxMsg > -1 && nbMaxMsg <= messages.size()) {
      ClientMessages deadMessages = new ClientMessages();
      deadMessages.addMessage(message.msg);
      sendToDMQ(deadMessages, null);
      return;
    }

    if (messages.isEmpty()) {
      samePriorities = true;
      priority = message.getPriority();
    } else if (samePriorities && priority != message.getPriority()) {
      samePriorities = false;
    }

    if (samePriorities) {
      // Constant priorities: no need to insert the message according to
      // its priority.
      if (receiving) {
        // Message being received: adding it at the end of the queue.
        messages.add(message);
      } else {
        // Denying or recovery: adding the message according to its original
        // arrival order.
        long currentO;
        int i = 0;
        for (Enumeration e = messages.elements(); e.hasMoreElements();) {
          currentO = ((Message) e.nextElement()).order;
          if (currentO > message.order) break;
          i++;
        }
        messages.insertElementAt(message, i);
      }
    } else {
      // Non constant priorities: inserting the message according to its 
      // priority.
      Message currentMsg;
      int currentP;
      long currentO;
      int i = 0;
      for (Enumeration e = messages.elements(); e.hasMoreElements();) {
        currentMsg = (Message) e.nextElement();
        currentP = currentMsg.getPriority();
        currentO = currentMsg.order;

        if (! receiving && currentP == message.getPriority()) {
          // Message denied or recovered, priorities are equal: inserting the
          // message according to its original arrival order.
          if (currentO > message.order) break;
        } else if (currentP < message.getPriority()) {
          // Current priority lower than the message to store: inserting it.
          break;
        }
        i++;
      }
      messages.insertElementAt(message, i);
    }
  }
  
  /**
   *  get a client message contain nb messages.
   *  
   * @param nb        number of messages returned in ClientMessage.
   * @param selector  jms selector
   * @param remove    delete all messages returned if true
   * @return ClientMessages (contains nb Messages)
   */
  protected ClientMessages getClientMessages(int nb, String selector, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.getClientMessages(" + nb + ',' + selector + ',' + remove + ')');

    ClientMessages cm = null ;
    List lsMessages = getMessages(nb, selector, remove);
    if (lsMessages.size() > 0) {
     cm = new ClientMessages();
    }
    Message message = null;
    Iterator itMessages = lsMessages.iterator();
    while (itMessages.hasNext()) {
      message = (Message) itMessages.next();
      cm.addMessage(message.msg);
    }
    return cm;
  }
  
  protected ClientMessages getClientMessages(List lsMsgId, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.getClientMessages(" + lsMsgId + ',' + remove + ')');

    ClientMessages cm = new ClientMessages();
    Message message = null;
    String msgId = null;
    Iterator itMsgId = lsMsgId.iterator();
    // Checking the deliverable messages:
    while (itMsgId.hasNext()) {
      msgId = (String) itMsgId.next();
      message = getMessage(msgId, remove);

      if (checkDelivery(message.msg)) {
        message.msg.deliveryCount++;
        nbMsgsDeliverSinceCreation++;
        
        // use in sub class see ClusterQueueImpl
        messageDelivered(message.getIdentifier());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Message " + msgId);

        cm.addMessage(message.msg);
      }
    }
    return cm;
  }

  /**
   * List of message to be removed.
   * 
   * @param msgIds  List of message id.
   */
  protected void removeMessages(List msgIds) {
    String id = null;
    Iterator itMessages = msgIds.iterator();
    while (itMessages.hasNext()) {
      id = (String) itMessages.next();
      int i = 0;
      Message message = null;
      while (i < messages.size()) {
        message = (Message) messages.get(i);
        if (id.equals(message.getIdentifier())) {
          messages.remove(i);
          message.delete();
          break;
        }
      }
    }
  }
  
  /**
   * get nb messages, if it's possible.
   * 
   * @param nb        -1 return all messages.
   * @param selector  jms selector.
   * @return  List of mom messages.
   */
  private List getMessages(int nb, String selector, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.getMessages(" + nb + ',' + selector + ',' + remove +')');

    List lsMessages = new ArrayList();
    Message message;
    int j = 0;
    // Checking the deliverable messages:
    while ((lsMessages.size() < nb || nb == -1) &&  j < messages.size()) {
      message = (Message) messages.get(j);

      // If selector matches, sending the message:
      if (Selector.matches(message.msg, selector) &&
          checkDelivery(message.msg)) {
        message.msg.deliveryCount++;
        nbMsgsDeliverSinceCreation++;
        
        // use in sub class see ClusterQueueImpl
        messageDelivered(message.getIdentifier());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Message " + message.msg.id);

        lsMessages.add(message);
        
        if (remove) {
          messages.remove(message);
          message.delete();
        } else {
          // message not remove: going on.
          j++;
        }
        
      } else {
        // If message delivered or selector does not match: going on
        j++;
      }
    }
    return lsMessages;
  }
  
  private Message getMomMessage(String msgId) {
    Message msg = null;
    for (Enumeration e = messages.elements(); e.hasMoreElements(); ) {
      msg = (Message) e.nextElement();
      if (msgId.equals(msg.getIdentifier()))
        return msg;
    }
    return msg;
  }
  
  /**
   * get mom message, delete if remove = true.
   * 
   * @param msgId   message identification
   * @param remove  if true delete message
   * @return mom message
   */
  protected Message getMessage(String msgId, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.getMessage(" + msgId + ',' + remove + ')');

    Message message =  getMomMessage(msgId);
      if (checkDelivery(message.msg)) {
        message.msg.deliveryCount++;
        nbMsgsDeliverSinceCreation++;
        
        // use in sub class see ClusterQueueImpl
        messageDelivered(message.getIdentifier());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Message " + msgId);
        
        if (remove) {
          messages.remove(message);
          message.delete();
        } 
      }
    return message;
  }
  
  /**
   * Actually tries to answer the pending "receive" requests.
   * <p>
   * The method may send <code>QueueMsgReply</code> replies to clients.
   *
   * @param index  Index where starting to "browse" the requests.
   */
  protected void deliverMessages(int index) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.deliverMessages(" + index + ')');

    ReceiveRequest notRec = null;
    Message message;
    QueueMsgReply notMsg;
    ClientMessages deadMessages = null;
    List lsMessages = null;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> requests = " + requests + ')');

    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);
    // Cleaning the possible expired messages.
    deadMessages = cleanPendingMessage(current);
   
    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      notMsg = new QueueMsgReply(notRec);

      lsMessages = getMessages(notRec.getMessageCount(), notRec.getSelector(), notRec.getAutoAck());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "QueueImpl.deliverMessages: notRec.getAutoAck() = " + notRec.getAutoAck()+
            "lsMessages = " + lsMessages);
      
      Iterator itMessages = lsMessages.iterator();
      while (itMessages.hasNext()) {
        message = (Message) itMessages.next();
        notMsg.addMessage(message.msg);
        if (!notRec.getAutoAck()) {
          // putting the message in the delivered messages table:
          consumers.put(message.getIdentifier(), notRec.requester);
          contexts.put(message.getIdentifier(),
              new Integer(notRec.getClientContext()));
          deliveredMsgs.put(message.getIdentifier(), message);
          messages.remove(message);
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
              "Message " + message.msg.id + " to " + notRec.requester +
              " as reply to " + notRec.getRequestId());
      }

      if (isLocal(notRec.requester)) {
        notMsg.setPersistent(false);
      }

      if (notMsg.isPersistent() && !notRec.getAutoAck()) {
        // state change, so save.
        setSave();
      }

      // Next request:
      if (notMsg.getSize() > 0) {
        requests.remove(index);
        forward(notRec.requester, notMsg);
      } else {
        index++;
      }
    }
    // If needed, sending the dead messages to the DMQ:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);
  }
  
//  /**
//   * Actually tries to answer the pending "receive" requests.
//   * <p>
//   * The method may send <code>QueueMsgReply</code> replies to clients.
//   *
//   * @param index  Index where starting to "browse" the requests.
//   */
//  protected void deliverMessages(int index) {
//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG, "QueueImpl.deliverMessages(" + index + ')');
//
//    ReceiveRequest notRec = null;
//    boolean replied;
//    int j = 0;
//    Message message;
//    QueueMsgReply notMsg;
//    ClientMessages deadMessages = null;
//
//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG, " -> requests = " + requests + ')');
//
//    long current = System.currentTimeMillis();
//    cleanWaitingRequest(current);
//    // Cleaning the possible expired messages.
//    deadMessages = cleanPendingMessage(current);
//   
//    // Processing each request as long as there are deliverable messages:
//    while (! messages.isEmpty() && index < requests.size()) { 
//      notRec = (ReceiveRequest) requests.get(index);
//      replied = false;
//      notMsg = new QueueMsgReply(notRec);
//
//      // Checking the deliverable messages:
//      while (j < messages.size()) {
//        message = (Message) messages.get(j);
//
//        // If selector matches, sending the message:
//        if (Selector.matches(message.msg, notRec.getSelector()) &&
//            checkDelivery(message.msg)) {
//          messages.remove(j);
//          message.msg.deliveryCount++;
//          notMsg.addMessage(message.msg);
//              
//          if (isLocal(notRec.requester))
//            notMsg.setPersistent(false);
//
//          nbMsgsDeliverSinceCreation++;
//
//          // use in sub class see ClusterQueueImpl
//          messageDelivered(message.getIdentifier());
//
//          if (logger.isLoggable(BasicLevel.DEBUG))
//            logger.log(BasicLevel.DEBUG,
//                       "Message " + message.msg.id + " to " + notRec.requester +
//                       " as reply to " + notRec.getRequestId());
//                                          
//          if (notRec.getAutoAck()) {
//            // Removing the message if request in auto ack mode:
//            message.delete();
//          } else {
//            // Else, putting the message in the delivered messages table:
//            if (notMsg.isPersistent()) {
//              // state change, so save.
//              setSave();
//            }
//            consumers.put(message.getIdentifier(), notRec.requester);
//            contexts.put(message.getIdentifier(),
//                         new Integer(notRec.getClientContext()));
//            deliveredMsgs.put(message.getIdentifier(), message);
//          }
//              
//          if (notMsg.getSize() == notRec.getMessageCount()) {
//            break;
//          }
//        } else {
//          // If message delivered or selector does not match: going on
//          j++;
//        }
//      }
//
//      // Next request:
//      if (notMsg.getSize() > 0) {
//        requests.remove(index);
//        forward(notRec.requester, notMsg);
//      } else {
//        index++;
//      }
//
//      j = 0;
//    }
//    // If needed, sending the dead messages to the DMQ:
//    if (deadMessages != null)
//      sendToDMQ(deadMessages, null);
//  }

  protected boolean checkDelivery(org.objectweb.joram.shared.messages.Message msg) {
    return true;
  }

  /** 
   * call in deliverMessages just after forward(msg),
   * overload this methode to process a specific treatment.
   */
  protected void messageDelivered(String msgId) {}

  /** 
   * call in deliverMessages just after a remove message (invalid),
   * overload this methode to process a specific treatment.
   */
  protected void messageRemoved(String msgId) {}
  
  /**
   * Returns <code>true</code> if a given message is considered as 
   * undeliverable, because its delivery count matches the queue's 
   * threshold, if any, or the server's default threshold value (if any).
   */
  protected boolean isUndeliverable(Message message) {
    if (threshold != null)
      return message.msg.deliveryCount == threshold.intValue();
    else if (DeadMQueueImpl.threshold != null)
      return message.msg.deliveryCount == DeadMQueueImpl.threshold.intValue();
    return false;
  }

  /** Deserializes a <code>QueueImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueImpl.readObject()");
    in.defaultReadObject();

    cleanWaitingRequest(System.currentTimeMillis());

    receiving = false;
    messages = new Vector();
    deliveredMsgs = new Hashtable();

    // Retrieving the persisted messages, if any.
    Vector persistedMsgs = null;
    persistedMsgs = Message.loadAll(getMsgTxname());

    if (persistedMsgs != null) {
      Message persistedMsg;
      AgentId consId;
      while (! persistedMsgs.isEmpty()) {
        persistedMsg = (Message) persistedMsgs.remove(0);
        consId = (AgentId) consumers.get(persistedMsg.getIdentifier());
        if (consId == null) {
          addMessage(persistedMsg);
        } else if (isLocal(consId)) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,
                       " -> deny " + persistedMsg.getIdentifier());
          consumers.remove(persistedMsg.getIdentifier());
          contexts.remove(persistedMsg.getIdentifier());
          addMessage(persistedMsg);
        } else {
          deliveredMsgs.put(persistedMsg.getIdentifier(), persistedMsg);
        }
      }
    }
  }

  public void readBag(ObjectInputStream in) throws IOException, ClassNotFoundException {
    receiving = in.readBoolean();
    messages = (Vector) in.readObject();
    deliveredMsgs = (Hashtable) in.readObject();

    for (int i = 0; i < messages.size(); i++) {
      Message message = (Message)messages.elementAt(i);
      // Persisting the message.
      setMsgTxName(message);
      message.save();
    }
  }

  public void writeBag(ObjectOutputStream out) throws IOException {
    out.writeBoolean(receiving);
    out.writeObject(messages);
    out.writeObject(deliveredMsgs);
  }
}
