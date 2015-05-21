/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2015 ScalAgent Distributed Technologies
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.messages.MessageJMXWrapper;
import org.objectweb.joram.mom.notifications.AbortReceiveRequest;
import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.BrowseReply;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.QueueDeliveryTimeNot;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.QueueDeliveryTimeTask;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ClearQueue;
import org.objectweb.joram.shared.admin.DeleteQueueMessage;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetDeliveredMessages;
import org.objectweb.joram.shared.admin.GetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetPendingMessages;
import org.objectweb.joram.shared.admin.GetPendingRequests;
import org.objectweb.joram.shared.admin.GetQueueMessage;
import org.objectweb.joram.shared.admin.GetQueueMessageIds;
import org.objectweb.joram.shared.admin.GetQueueMessageIdsRep;
import org.objectweb.joram.shared.admin.GetQueueMessageRep;
import org.objectweb.joram.shared.admin.SetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.SetSyncExceptionOnFullDestRequest;
import org.objectweb.joram.shared.admin.SetThresholdRequest;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.DestinationException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.scalagent.scheduler.ScheduleEvent;
import com.scalagent.scheduler.Scheduler;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * The <code>Queue</code> class implements the MOM queue behavior,
 * basically storing messages and delivering them upon clients requests.
 */
public class Queue extends Destination implements QueueMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(Queue.class.getName());
  public static Logger logmsg = Debug.getLogger(Queue.class.getName() + ".TraceMsg");

  /** Static value holding the default DMQ identifier for a server. */
  static AgentId defaultDMQId = null;

  /**
   * Threshold above which messages are considered as undeliverable because
   * constantly denied; 0 stands for no threshold, -1 for value not set.
   */
  private int threshold = -1;

  /** Static value holding the default threshold for a server. */
  static int defaultThreshold = -1;

  /**
   * Returns  the threshold value of this queue, -1 if not set.
   *
   * @return the threshold value of this queue; -1 if not set.
   */
  public int getThreshold() {
    return threshold;
  }

  /**
   * Sets or unsets the threshold for this queue.
   *
   * @param threshold The threshold value to be set (-1 for unsetting previous value).
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  /** Static method returning the default threshold. */
  public static int getDefaultThreshold() {
    return defaultThreshold;
  }

  /** Static method returning the default DMQ identifier. */
  public static AgentId getDefaultDMQId() {
    return defaultDMQId;
  }

  /** <code>true</code> if all the stored messages have the same priority. */
  private boolean samePriorities;

  /** Common priority value. */
  private int priority; 

  /** Table keeping the messages' consumers identifiers. */
  protected Map consumers = new Hashtable();

  /** Table keeping the messages' consumers contexts. */
  protected Map contexts = new Hashtable();

  /** Counter of messages arrivals. */
  protected long arrivalsCounter = 0;

  /** List holding the requests before reply or expiry. */
  protected List<ReceiveRequest> requests = new Vector();
  
  protected transient Scheduler deliveryScheduler = null;
  
  public Queue() {
    super();
  }
  
  /**
   * Used by the Encodable framework
   */
  protected Queue(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.react(" + from + ',' + not + ')');

    // set agent no save (this is the default).
    setNoSave();

    try {
      if (not instanceof ReceiveRequest)
        receiveRequest(from, (ReceiveRequest) not);
      else if (not instanceof BrowseRequest)
        browseRequest(from, (BrowseRequest) not);
      else if (not instanceof AcknowledgeRequest)
        acknowledgeRequest((AcknowledgeRequest) not);
      else if (not instanceof DenyRequest)
        denyRequest(from, (DenyRequest) not);
      else if (not instanceof AbortReceiveRequest)
        abortReceiveRequest(from, (AbortReceiveRequest) not);
      else if (not instanceof ExpiredNot)
        handleExpiredNot(from, (ExpiredNot) not);
      else if (not instanceof QueueDeliveryTimeNot)
        doStoreMessageAfterDeliveryTime(from, (QueueDeliveryTimeNot) not);
      else
        super.react(from, not);

    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      if (not instanceof AbstractRequestNot) {
        AbstractRequestNot req = (AbstractRequestNot) not;
        Channel.sendTo(from, new ExceptionReply(req, exc));
      }
    }
  }

  /**
   * Removes all request that the expiration time is expired.
   */
  public void cleanWaitingRequest() {
    cleanWaitingRequest(System.currentTimeMillis());
  }

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
  public final int getWaitingRequestCount() {
    if (requests != null) { 
      cleanWaitingRequest(System.currentTimeMillis());
      return requests.size();
    }
    return 0;
  }

  /** <code>true</code> if the queue is currently receiving messages. */
  protected transient boolean receiving = false;

  /** List holding the messages before delivery. */
  protected transient List messages;

  /**
   * Removes all messages that the time-to-live is expired.
   */
  public void cleanPendingMessage() {
    cleanPendingMessage(System.currentTimeMillis());
  }

  public byte getType() {
    return DestinationConstants.QUEUE_TYPE;
  }

  /**
   * Cleans the pending messages list. Removes all messages which expire before
   * the date given in parameter.
   * 
   * @param currentTime
   *          The current time.
   * @return A <code>DMQManager</code> which contains the expired messages.
   *         <code>null</code> if there wasn't any.
   */
  protected DMQManager cleanPendingMessage(long currentTime) {
    int index = 0;

    DMQManager dmqManager = null;

    Message message = null;
    while (index < messages.size()) {
      message = (Message) messages.get(index);
      if (! message.isValid(currentTime)) {
        messages.remove(index);

        if (dmqManager == null)
          dmqManager = new DMQManager(dmqId, getId());
        nbMsgsSentToDMQSinceCreation++;
        message.delete();
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Removes expired message " + message.getId(), new Exception());
      } else {
        index++;
      }
    }
    return dmqManager;
  }

  /**
   * Returns the number of pending messages in the queue.
   *
   * @return The number of pending messages.
   */
  public final int getPendingMessageCount() {
    if (messages != null) {
      return messages.size();
    }
    return 0;
  }

//  /**
//   * This task allow to compute the average load of the queue.
//   */
//  transient QueueAverageLoadTask averageLoadTask = null;
//  
//  /**
//   * Returns the load averages for the last minute.
//   * @return the load averages for the last minute.
//   */
//  public float getAverageLoad1() {
//    return (averageLoadTask==null)?0:averageLoadTask.getAverageLoad1();
//  }
//
//  /**
//   * Returns the load averages for the past 5 minutes.
//   * @return the load averages for the past 5 minutes.
//   */
//  public float getAverageLoad5() {
//    return (averageLoadTask==null)?0:averageLoadTask.getAverageLoad5();
//  }
//  
//  /**
//   * Returns the load averages for the past 15 minutes.
//   * @return the load averages for the past 15 minutes.
//   */
//  public float getAverageLoad15() {
//    return (averageLoadTask==null)?0:averageLoadTask.getAverageLoad15();
//  }
//
//  class QueueAverageLoadTask extends AverageLoadTask {
//    Queue queue = null;
//    
//    public QueueAverageLoadTask(Timer timer, Queue queue) {
//      this.queue = queue;
//      start(timer);
//    }
//    
//    /**
//     * Returns the number of waiting messages in the engine.
//     * 
//     * @see fr.dyade.aaa.common.AverageLoadTask#countActiveTasks()
//     */
//    @Override
//    protected long countActiveTasks() {
//      return queue.getPendingMessageCount();
//    }
//  }

  /** Table holding the delivered messages before acknowledgment. */
  protected transient Map deliveredMsgs;

  /**
   * Returns the number of messages delivered and waiting for acknowledge.
   *
   * @return The number of messages delivered.
   */
  public final int getDeliveredMessageCount() {
    if (deliveredMsgs != null) {
      return deliveredMsgs.size();
    }
    return 0;
  }

  public long getNbMsgsReceiveSinceCreation() {
    return nbMsgsSentToDMQSinceCreation + nbMsgsDeliverSinceCreation + getPendingMessageCount();
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
  public final int getNbMaxMsg() {
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
   * Initializes the destination.
   * 
   * @param firstTime		true when first called by the factory
   */
  protected void initialize(boolean firstTime) {
    cleanWaitingRequest(System.currentTimeMillis());

    receiving = false;
    messages = new Vector();
    deliveredMsgs = new Hashtable();
    
//    averageLoadTask = new QueueAverageLoadTask(AgentServer.getTimer(), this);

    if (firstTime) return;

    // Retrieving the persisted messages, if any.
    List persistedMsgs = Message.loadAll(getMsgTxPrefix().toString());

    if (persistedMsgs != null) {
      Message persistedMsg;
      AgentId consId;
      
      while (! persistedMsgs.isEmpty()) {
        persistedMsg = (Message) persistedMsgs.remove(0);
        consId = (AgentId) consumers.get(persistedMsg.getId());

        if (logmsg.isLoggable(BasicLevel.INFO))
          logmsg.log(BasicLevel.INFO, getName() + ": retrieves message " + persistedMsg.getId());

        try {
          if (consId == null) {
            if (!addMessage(persistedMsg, false)) {
              persistedMsg.delete();
            }
          } else if (isLocal(consId)) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> deny " + persistedMsg.getId());
            consumers.remove(persistedMsg.getId());
            contexts.remove(persistedMsg.getId());
            if (!addMessage(persistedMsg, false)) {
              persistedMsg.delete();
            }
          } else {
            deliveredMsgs.put(persistedMsg.getId(), persistedMsg);
          }
        } catch (AccessException e) {/*never happens*/}
      }
    }
  }

  /**
   * Finalizes the destination before it is garbaged.
   * 
   * @param lastime true if the destination is deleted
   */
  protected void finalize(boolean lastTime) {
//    averageLoadTask.cancel();
//    averageLoadTask = null;
  }

  /**
   * Returns a string representation of this destination.
   */
  public String toString() {
    return "Queue:" + getId().toString();
  }

  long hprod, hcons;
  int pload = -1;
  int cload = -1;

  /**
   * wake up, and cleans the queue.
   */
  public void wakeUpNot(WakeUpNot not) {
    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);
    // Cleaning the possibly expired messages.
    DMQManager dmqManager = cleanPendingMessage(current);
    // If needed, sending the dead messages to the DMQ:
    if (dmqManager != null) {
      setSave();
      dmqManager.sendToDMQ();
    }

    long prod = getNbMsgsReceiveSinceCreation();
    long cons = getNbMsgsDeliverSinceCreation();
//    if ((hprod == 0) && (hcons == 0)) {
//      hprod = prod;
//      hcons = cons;
//    }
    pload = (pload + 2*(int)((1000L*(prod-hprod))/getPeriod()))/3;
    cload = (cload + 2*(int)((1000L*(cons-hcons))/getPeriod()))/3;
    hprod = prod;
    hcons = cons;
  }
  
  /**
   * Return the average producer's load during last moments.
   */
  public long getProducerLoad() {
    return pload;
  }

  /**
   * Return the average consumer's load during last moments.
   */
  public long getConsumerLoad() {
    return cload;
  }

  /**
   * This method allows to exclude some JMX attribute of getJMXStatistics method.
   * It excludes.
   * 
   * @param attrName name of attribute to test.
   * @return true if the attribute is a valid one.
   */
  protected boolean isValidJMXAttribute(String attrName) {
    if ("Messages".equals(attrName))
      return false;
    return super.isValidJMXAttribute(attrName);
  }

  /**
   * Method implementing the reaction to a <code>ReceiveRequest</code>
   * instance, requesting a message.
   * <p>
   * This method stores the request and launches a delivery sequence.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  protected void receiveRequest(AgentId from, ReceiveRequest not) throws AccessException {
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
  protected void browseRequest(AgentId from, BrowseRequest not) throws AccessException {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Building the reply:
    BrowseReply rep = new BrowseReply(not);

    // Cleaning the possibly expired messages.
    DMQManager dmqManager = cleanPendingMessage(System.currentTimeMillis());
    // Adding the deliverable messages to it:
    Message message;
    for (int i = 0; i < messages.size(); i++) {
      message = (Message) messages.get(i);
      if (Selector.matches(message.getHeaderMessage(), not.getSelector())) {
        // Matching selector: adding the message:
        rep.addMessage(message.getFullMessage());
      }
    }

    // Sending the dead messages to the DMQ, if needed:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

    // Delivering the reply:
    forward(from, rep);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Method implementing the reaction to an <code>AcknowledgeRequest</code>
   * instance, requesting messages to be acknowledged.
   */
  protected void acknowledgeRequest(AcknowledgeRequest not) {
    for (Enumeration ids = not.getIds(); ids.hasMoreElements();) {
      String msgId = (String) ids.nextElement();
      acknowledge(msgId);
    }
  }

  private void acknowledge(String msgId) {
    if (logmsg.isLoggable(BasicLevel.INFO))
      logmsg.log(BasicLevel.INFO, getName() + ": acknowledges message " + msgId);
    
    Message msg = (Message) deliveredMsgs.remove(msgId);
    if ((msg != null) && msg.isPersistent()) {
      // state change, so save.
      setSave();
    }
    consumers.remove(msgId);
    contexts.remove(msgId);
    if (msg != null) {
      msg.delete();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message " + msgId + " acknowledged.");
    } else if ((logger.isLoggable(BasicLevel.WARN) || logmsg.isLoggable(BasicLevel.WARN))) {
      logger.log(BasicLevel.WARN, "Message " + msgId + " not found for acknowledgement.");
      logmsg.log(BasicLevel.WARN, getName() + ": message " + msgId + " not found for acknowledgement.");
    }
  }

  /**
   * Method implementing the reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * This method denies the messages and launches a delivery sequence.
   * Messages considered as undeliverable are sent to the DMQ.
   */
  protected void denyRequest(AgentId from, DenyRequest not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.DenyRequest(" + from + ',' + not + ')');

    Enumeration ids = not.getIds();

    String msgId;
    Message message;
    AgentId consId;
    int consCtx;
    DMQManager dmqManager = null;

    if (! ids.hasMoreElements()) {
      // If the deny request is empty, the denying is a contextual one: it
      // requests the denying of all the messages consumed by the client in
      // the denying context:
      for (Iterator entries = deliveredMsgs.entrySet().iterator(); entries.hasNext();) {
        // Browsing the delivered messages:
        Map.Entry entry = (Map.Entry) entries.next();

        msgId = (String) entry.getKey();
        message = (Message) entry.getValue();

        consId = (AgentId) consumers.get(msgId);
        consCtx = ((Integer) contexts.get(msgId)).intValue();

        // If the current message has been consumed by the denier in the same
        // context: denying it.
        if (consId.equals(from) && consCtx == not.getClientContext()) {
          // state change, so save.
          setSave();

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, " -> deny msg " + msgId + "(consId = " + consId + ')');

          // The message is no longer delivered
          nbMsgsDeliverSinceCreation -= 1;
          
          consumers.remove(msgId);
          contexts.remove(msgId);
          entries.remove();
          if (not.isRedelivered())
            message.setRedelivered();
          else
            message.setDeliveryCount(message.getDeliveryCount()-1);

          // If message considered as undeliverable, adding
          // it to the list of dead messages:
          if (isUndeliverable(message)) {
            message.delete();
            if (dmqManager == null)
              dmqManager = new DMQManager(dmqId, getId());
            nbMsgsSentToDMQSinceCreation++;
            dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
          } else {
            try {
              // Else, putting the message back into the deliverables list:
              storeMessageHeader(message, false);
            } catch (AccessException e) { /* never happens */ }
          }

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Message " + msgId + " denied.");
          if (logmsg.isLoggable(BasicLevel.INFO))
            logmsg.log(BasicLevel.INFO, getName() + ": denies message " + msgId);
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
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, " -> already denied message " + msgId);
        if (logmsg.isLoggable(BasicLevel.WARN))
          logmsg.log(BasicLevel.WARN, getName() + ": already denied message " + msgId);
        break;
      }

      // The message is no longer delivered
      nbMsgsDeliverSinceCreation -= 1;

      if (not.isRedelivered())
        message.setRedelivered();
      else
        message.setDeliveryCount(message.getDeliveryCount()-1);


      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> deny " + msgId);
      if (logmsg.isLoggable(BasicLevel.INFO))
        logmsg.log(BasicLevel.INFO, getName() + ": denies message " + msgId);

      // state change, so save.
      setSave();
      consumers.remove(msgId);
      contexts.remove(msgId);

      // If message considered as undeliverable, adding it
      // to the list of dead messages:
      if (isUndeliverable(message)) {
        message.delete();
        if (dmqManager == null)
          dmqManager = new DMQManager(dmqId, getId());
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
      } else {
        try {
          // Else, putting the message back into the deliverables list:
          storeMessageHeader(message, false);
        } catch (AccessException e) {/* never happens */}
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message " + msgId + " denied.");
    }
    // Sending the dead messages to the DMQ, if needed:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

    // Launching a delivery sequence:
    deliverMessages(0);
  }

  protected void abortReceiveRequest(AgentId from,
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

  /**
   * @see org.objectweb.joram.mom.dest.Destination#handleAdminRequestNot(fr.dyade.aaa.agent.AgentId, org.objectweb.joram.mom.notifications.FwdAdminRequestNot)
   */
  public void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {
    AdminRequest adminRequest = not.getRequest();
    
    if (adminRequest instanceof GetQueueMessageIds) {
      getQueueMessageIds(not.getReplyTo(),
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
      clearQueue(not.getReplyTo(),
                 not.getRequestMsgId(),
                 not.getReplyMsgId());
    } else if (adminRequest instanceof GetNbMaxMsgRequest) {
      replyToTopic(new GetNumberReply(getNbMaxMsg()),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof GetPendingMessages) {
      // Cleaning of the possibly expired messages.
      DMQManager dmqManager = cleanPendingMessage(System.currentTimeMillis());
      // Sending the dead messages to the DMQ, if needed:
      if (dmqManager != null) dmqManager.sendToDMQ();
      
      replyToTopic(new GetNumberReply(getPendingMessageCount()),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof GetPendingRequests) {
      // Cleaning of the possibly expired requests.
      cleanWaitingRequest(System.currentTimeMillis());
      replyToTopic(new GetNumberReply(getWaitingRequestCount()),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof GetDMQSettingsRequest) {
      replyToTopic(new GetDMQSettingsReply((dmqId != null)?dmqId.toString():null, threshold),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof SetThresholdRequest) {
      setSave(); // state change, so save.
      threshold = ((SetThresholdRequest) adminRequest).getThreshold();
      
      replyToTopic(new AdminReply(true, null),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof SetNbMaxMsgRequest) {
      setSave(); // state change, so save.
      nbMaxMsg = ((SetNbMaxMsgRequest) adminRequest).getNbMaxMsg();

      replyToTopic(new AdminReply(true, null),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof SetSyncExceptionOnFullDestRequest) {
      setSave(); // state change, so save.
      syncExceptionOnFullDest = ((SetSyncExceptionOnFullDestRequest) adminRequest).isSyncExceptionOnFullDest();

      replyToTopic(new AdminReply(true, null),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
    } else if (adminRequest instanceof GetDeliveredMessages) {
        replyToTopic(new GetNumberReply((int)nbMsgsDeliverSinceCreation),
                not.getReplyTo(),
                not.getRequestMsgId(),
                not.getReplyMsgId());
    } else {
    	super.handleAdminRequestNot(from, not);
    }
  }

  private void getQueueMessageIds(AgentId replyTo,
                                  String requestMsgId,
                                  String replyMsgId) {
    String[] res = new String[messages.size()];
    for (int i = 0; i < messages.size(); i++) {
      Message msg = (Message) messages.get(i);
      res[i] = msg.getId();
    }
    replyToTopic(new GetQueueMessageIdsRep(res), replyTo, requestMsgId, replyMsgId);
  }

  private void getQueueMessage(GetQueueMessage request,
                               AgentId replyTo,
                               String requestMsgId,
                               String replyMsgId) {
    Message message = null;

    for (int i = 0; i < messages.size(); i++) {
      message = (Message) messages.get(i);
      if (message.getId().equals(request.getMessageId())) break;
      message = null;
    }

    if (message != null) {
      GetQueueMessageRep reply = null;
      if (request.getFullMessage()) {
        reply = new GetQueueMessageRep(message.getFullMessage());
      } else {
        reply = new GetQueueMessageRep(message.getHeaderMessage());
      }
      replyToTopic(reply, replyTo, requestMsgId, replyMsgId);
    } else {
      replyToTopic(new AdminReply(false, "Unknown message " + request.getMessageId()), replyTo, requestMsgId,
          replyMsgId);
    }
  }

  private void deleteQueueMessage(DeleteQueueMessage request,
                                  AgentId replyTo,
                                  String requestMsgId,
                                  String replyMsgId) {
    for (int i = 0; i < messages.size(); i++) {
      Message message = (Message) messages.get(i);
      if (message.getId().equals(request.getMessageId())) {
        messages.remove(i);
        message.delete();
        DMQManager dmqManager = new DMQManager(dmqId, getId());
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.ADMIN_DELETED);
        dmqManager.sendToDMQ();
        break;
      }
    }
    replyToTopic(new AdminReply(true, null), replyTo, requestMsgId, replyMsgId);
  }

  private void clearQueue(AgentId replyTo,
                          String requestMsgId,
                          String replyMsgId) {
    if (messages.size() > 0) {
      DMQManager dmqManager = new DMQManager(dmqId, getId());
      for (int i = 0; i < messages.size(); i++) {
        Message message = (Message) messages.get(i);
        message.delete();
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.ADMIN_DELETED);
      }
      dmqManager.sendToDMQ();
      messages.clear();
    }
    replyToTopic(new AdminReply(true, null), replyTo, requestMsgId, replyMsgId);
  }

  /**
   * Method specifically processing a <code>SetRightRequest</code> instance.
   * <p>
   * When a reader is removed, and receive requests of this reader are still
   * on the queue, they are replied to by an <code>ExceptionReply</code>.
   */
  protected void doRightRequest(AgentId user, int right) {
    // If the request does not unset a reader, doing nothing.
    if (right != -READ) return;

    ReceiveRequest request;

    if (user == null) {
      // Free reading right has been removed, reject the non readers requests.
      for (int i = 0; i < requests.size(); i++) {
        request = (ReceiveRequest) requests.get(i);
        if (! isReader(request.requester)) {
          forward(request.requester,
                  new ExceptionReply(request, new AccessException("Free READ access removed")));
          setSave(); // state change, so save.
          requests.remove(i);
          i--;
        }
      }
    } else {
      // Reading right of a given user has been removed; replying to its requests.
      for (int i = 0; i < requests.size(); i++) {
        request = (ReceiveRequest) requests.get(i);
        if (user.equals(request.requester)) {
          forward(request.requester,
                  new ExceptionReply(request, new AccessException("READ right removed")));
          setSave(); // state change, so save.
          requests.remove(i);
          i--;
        }
      }
    }
  }

  /**
   * Method specifically processing a <code>ClientMessages</code> instance.
   * <p>
   * This method stores the messages and launches a delivery sequence.
   */
  protected void doClientMessages(AgentId from, ClientMessages not, boolean throwsExceptionOnFullDest) throws AccessException {
    receiving = true;
    ClientMessages cm = null;
    
    // interceptors
    if (interceptorsAvailable()) {
    	// new client message
    	cm = new ClientMessages(not.getClientContext(), not.getRequestId());
    	cm.setAsyncSend(not.getAsyncSend());
    	cm.setDMQId(not.getDMQId());
    	
    	for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
    		org.objectweb.joram.shared.messages.Message message = (org.objectweb.joram.shared.messages.Message) msgs.next();
    		// set the destination name
    		message.setProperty("JoramDestinationName", getName());
    		// interceptors process
    		org.objectweb.joram.shared.messages.Message m = processInterceptors(message);
    		if (m == null) {
    			// send message to the DMQ
    			DMQManager dmqManager = new DMQManager(dmqId, getId());
    			nbMsgsSentToDMQSinceCreation++;
    			dmqManager.addDeadMessage(message, MessageErrorConstants.INTERCEPTORS);
    			dmqManager.sendToDMQ();
    			new Message(message).releaseFullMessage();
    		} else {
    			// add message to the client message 
    			cm.addMessage(m);
    		}
    	}
    	// test client message size.
    	if (cm.getMessageCount() == 0) {
    		receiving = false;
    		return;
    	}
    } else {
    	cm = not;
    }
    
    // pre process the client message
    ClientMessages clientMsgs = preProcess(from, cm);
   
    long currentTime = System.currentTimeMillis();
    
    if (clientMsgs != null) {
      Message msg;
      // Storing each received message:
      for (Iterator msgs = clientMsgs.getMessages().iterator(); msgs.hasNext();) {

        org.objectweb.joram.shared.messages.Message sharedMsg = (org.objectweb.joram.shared.messages.Message) msgs.next();
        msg = new Message(sharedMsg);

        if (sharedMsg.deliveryTime > currentTime) {
          scheduleDeliveryTimeMessage(sharedMsg, throwsExceptionOnFullDest);
        } else {
          msg.order = arrivalsCounter++;
          storeMessage(msg, throwsExceptionOnFullDest);
          if (msg.isPersistent()) setSave();
        }
      }
    }

    // Launching a delivery sequence:
    deliverMessages(0);

    if (clientMsgs != null)
      postProcess(clientMsgs);

    receiving = false;
  }

  void scheduleDeliveryTimeMessage(org.objectweb.joram.shared.messages.Message msg, boolean throwsExceptionOnFullDest) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.scheduleDeliveryTimeMessage(" + msg + ", " + throwsExceptionOnFullDest + ')');

    if (deliveryScheduler == null) {
      try {
        deliveryScheduler = new Scheduler(AgentServer.getTimer());
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "Queue.scheduleDeliveryTimeMessage", exc);
      }
    }
    // schedule a task
    try {
      deliveryScheduler.scheduleEvent(new ScheduleEvent(msg.id, new Date(msg.deliveryTime)), 
                              new QueueDeliveryTimeTask(getId(), msg, throwsExceptionOnFullDest));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Queue.scheduleDeliveryTimeMessage(" + msg + ')', e);
    }
  }
  
  void doStoreMessageAfterDeliveryTime(AgentId from, QueueDeliveryTimeNot not) throws AccessException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.doStoreMessageAfterDeliveryTime(" + from + ", " + not + ')');

    if (not.msg == null) return;
    org.objectweb.joram.shared.messages.Message sharedMsg = not.msg;
    Message msg = new Message(sharedMsg);
    msg.order = arrivalsCounter++;
    storeMessage(msg, not.throwsExceptionOnFullDest);
    if (msg.isPersistent()) setSave();
    
    // Launching a delivery sequence:
    deliverMessages(0);

    ClientMessages clientMsgs = new ClientMessages();
    clientMsgs.addMessage(sharedMsg);
    if (clientMsgs != null)
      postProcess(clientMsgs);
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
    DMQManager dmqManager = null;
    for (Iterator entries = deliveredMsgs.entrySet().iterator(); entries.hasNext();) {
      Map.Entry entry = (Map.Entry) entries.next();

      msgId = (String) entry.getKey();
      message = (Message) entry.getValue();

      consId = (AgentId) consumers.get(msgId);
      // Delivered message has been delivered to the deleted client:
      // denying it.
      if (consId.equals(client)) {
        entries.remove();
        message.setRedelivered();

        // state change, so save.
        setSave();
        consumers.remove(msgId);
        contexts.remove(msgId);

        // If message considered as undeliverable, adding it to the
        // list of dead messages:
        if (isUndeliverable(message)) {
          message.delete();
          if (dmqManager == null)
            dmqManager = new DMQManager(dmqId, getId());
          nbMsgsSentToDMQSinceCreation++;
          dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
        } else {
          try {
            // Else, putting it back into the deliverables list:
            storeMessageHeader(message, false);
          } catch (AccessException e) {/* never happens */  }
        }

        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN,
                     "Message " + message.getId() + " denied.");
      }
    }
    // Sending dead messages to the DMQ, if needed:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

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
    DestinationException exc = new DestinationException("Queue " + getId() + " is deleted.");
    ReceiveRequest rec;
    ExceptionReply excRep;
    // Sending it to the pending receivers:
    cleanWaitingRequest(System.currentTimeMillis());
    for (int i = 0; i < requests.size(); i++) {
      rec = (ReceiveRequest) requests.get(i);

      excRep = new ExceptionReply(rec, exc);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Requester " + rec.requester + " notified of the queue deletion.");
      forward(rec.requester, excRep);
    }
    // Sending the remaining messages to the DMQ, if needed:
    if (! messages.isEmpty()) {
      Message message;
      DMQManager dmqManager = new DMQManager(dmqId, getId());
      while (! messages.isEmpty()) {
        message = (Message) messages.remove(0);
        message.delete();
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.DELETED_DEST);
      }
      dmqManager.sendToDMQ();
    }

    // Deleting the messages:
    Message.deleteAll(getMsgTxPrefix().toString());
  }

  transient StringBuffer msgTxPrefix = null;
  transient int msgTxPrefixLength = 0;

  protected final StringBuffer getMsgTxPrefix() {
    if (msgTxPrefix == null) {
      msgTxPrefix = new StringBuffer(18).append('M').append(getId().toString()).append('_');
      msgTxPrefixLength = msgTxPrefix.length();
    }
    return msgTxPrefix;
  }

  protected final void setMsgTxName(Message msg) {
    if (msg.getTxName() == null) {
      msg.setTxName(getMsgTxPrefix().append(msg.order).toString());
      msgTxPrefix.setLength(msgTxPrefixLength);
    }
  }

  /**
   * Actually stores a message in the deliverables list.
   * 
   * @param msg The message to store.
   * @param throwsExceptionOnFullDest true, can throws an exception on sending message on full destination
   * @throws AccessException
   */
  protected final synchronized void storeMessage(Message msg, boolean throwsExceptionOnFullDest) throws AccessException {
    if (addMessage(msg, throwsExceptionOnFullDest)) {
      if (logmsg.isLoggable(BasicLevel.INFO))
        logmsg.log(BasicLevel.INFO, getName() + ": adds new message " + msg.getId() + ", " + msg.order);
      
      if (msg.isPersistent()) {
        // Persisting the message.
        setMsgTxName(msg);
        msg.save();
        msg.releaseFullMessage();
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message " + msg.getId() + " stored.");
    }
  }
  
  /**
   * Actually stores a message header in the deliverables list.
   * 
   * @param message The message to store.
   * @param throwsExceptionOnFullDest true, can throws an exception on sending message on full destination
   * @throws AccessException
   */
  protected final synchronized void storeMessageHeader(Message message,  boolean throwsExceptionOnFullDest) throws AccessException {
    if (addMessage(message, throwsExceptionOnFullDest)) {
      // Persisting the message.
      message.saveHeader();
      message.releaseFullMessage();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message " + message.getId() + " stored.");
    }
  }

  /** if true, throws an exception on sending message on full destination. */
  private boolean syncExceptionOnFullDest = false;
  
  /**
   * @return the syncExceptionOnFullDest
   */
  public boolean isSyncExceptionOnFullDest() {
    return syncExceptionOnFullDest;
  }

  /**
   * @param syncExceptionOnFullDest the syncExceptionOnFullDest to set
   */
  public void setSyncExceptionOnFullDest(boolean syncExceptionOnFullDest) {
    this.syncExceptionOnFullDest = syncExceptionOnFullDest;
  }

  /**
   * Adds a message in the list of messages to deliver.
   * 
   * @param message the message to add.
   * @param throwsExceptionOnFullDest true, can throws an exception on sending message on full destination
   * @return true if the message has been added. false if the queue is full.
   * @throws AccessException If syncExceptionOnFullDest and the queue isFull
   */
  protected final synchronized boolean addMessage(Message message, boolean throwsExceptionOnFullDest) throws AccessException {

    if (nbMaxMsg > -1 && nbMaxMsg <= (messages.size() + deliveredMsgs.size())) {
      
      if (throwsExceptionOnFullDest && isSyncExceptionOnFullDest()) {
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, "addMessage " + message.getId() + " throws Exception: The queue \"" + getName() + "\" is full (syncExceptionOnFullDest).");
        throw new AccessException("The queue \"" + getName() + "\" is full.");
      }
      
      DMQManager dmqManager = new DMQManager(dmqId, getId());
      nbMsgsSentToDMQSinceCreation++;
      dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.QUEUE_FULL);
      dmqManager.sendToDMQ();
      return false;
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
        for (Iterator ite = messages.iterator(); ite.hasNext();) {
          currentO = ((Message) ite.next()).order;
          if (currentO > message.order) break;
          i++;
        }
        messages.add(i, message);
      }
    } else {
      // Non constant priorities: inserting the message according to its 
      // priority.
      Message currentMsg;
      int currentP;
      long currentO;
      int i = 0;
      for (Iterator ite = messages.iterator(); ite.hasNext();) {
        currentMsg = (Message) ite.next();
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
      messages.add(i, message);
    }
    return true;
  }

  /**
   * Get a client message contain <code>nb</code> messages.
   * Only used in ClusterQueue.
   *  
   * @param nb        number of messages returned in ClientMessage.
   * @param selector  jms selector
   * @param remove    delete all messages returned if true
   * @return ClientMessages (contains nb Messages)
   */
  protected ClientMessages getClientMessages(int nb, String selector, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.getClientMessages(" + nb + ',' + selector + ',' + remove + ')');

    ClientMessages cm = null ;
    List lsMessages = getMessages(nb, selector, remove);
    if (lsMessages.size() > 0) {
      cm = new ClientMessages();
    }
    Message message = null;
    Iterator itMessages = lsMessages.iterator();
    while (itMessages.hasNext()) {
      message = (Message) itMessages.next();
      cm.addMessage(message.getFullMessage());
    }
    return cm;
  }

  /**
   * List of message to be removed from messages vector.
   * No message.delete() call.
   * 
   * @param msgIds  List of message id.
   */
  protected void removeMessages(List msgIds) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.removeMessages(" + msgIds + ')');
    String id = null;
    Iterator itMessages = msgIds.iterator();
    while (itMessages.hasNext()) {
      id = (String) itMessages.next();
      int i = 0;
      Message message = null;
      while (i < messages.size()) {
        message = (Message) messages.get(i);
        if (id.equals(message.getId())) {
          messages.remove(i);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Queue.removeMessages msgId = " + id);
          break;
        }
      }
    }
  }

  /**
   * get messages, if it's possible.
   * 
   * @param nb
   *            -1 return all messages.
   * @param selector
   *            jms selector.
   * @return List of mom messages.
   */
  private List getMessages(int nb, String selector, boolean remove) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.getMessages(" + nb + ',' + selector + ',' + remove + ')');

    List lsMessages = new ArrayList();
    Message message;
    int j = 0;
    // Checking the deliverable messages:
    while ((lsMessages.size() < nb || nb == -1) &&  j < messages.size()) {
      message = (Message) messages.get(j);

      // If selector matches, sending the message:
      if (Selector.matches(message.getHeaderMessage(), selector) &&
          checkDelivery(message.getHeaderMessage())) {
        message.incDeliveryCount();
        nbMsgsDeliverSinceCreation++;

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Queue.getMessages() -> " + j + ',' + message.getId());

        // use in sub class see ClusterQueue
        messageDelivered(message.getId());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Message " + message.getId());

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
    for (Iterator ite = messages.iterator(); ite.hasNext();) {
      msg = (Message) ite.next();
      if (msgId.equals(msg.getId()))
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
  protected Message getQueueMessage(String msgId, boolean remove) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.getMessage(" + msgId + ',' + remove + ')');

    Message message =  getMomMessage(msgId);
    if (checkDelivery(message.getHeaderMessage())) {
    	message.incDeliveryCount();
    	nbMsgsDeliverSinceCreation++;

      // use in sub class see ClusterQueue
      messageDelivered(message.getId());

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
   * Returns the description of a particular pending message. The message is
   * pointed out through its unique identifier.
   * 
   * @param msgId The unique message's identifier.
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public CompositeData getMessage(String msgId) throws Exception {
    Message msg = getQueueMessage(msgId, false);
    if (msg == null) return null;
    
    return MessageJMXWrapper.createCompositeDataSupport(msg);
  }

  /**
   * Returns the description of all pending messages.
   * 
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public TabularData getMessages() throws Exception {
    return MessageJMXWrapper.createTabularDataSupport(messages);
  }

  public List getMessagesView() {
    return messages;
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
      logger.log(BasicLevel.DEBUG, "Queue.deliverMessages(" + index + ')');

    ReceiveRequest notRec = null;
    Message message;
    QueueMsgReply notMsg;
    List lsMessages = null;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> requests = " + requests + ')');

    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);
    // Cleaning the possibly expired messages.
    DMQManager dmqManager = cleanPendingMessage(current);

    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) {
      notRec = (ReceiveRequest) requests.get(index);
      notMsg = new QueueMsgReply(notRec);

      lsMessages = getMessages(notRec.getMessageCount(), notRec.getSelector(), notRec.getAutoAck());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Queue.deliverMessages: notRec.getAutoAck() = " + notRec.getAutoAck() + ", lsMessages = " + lsMessages);

      Iterator itMessages = lsMessages.iterator();
      while (itMessages.hasNext()) {
        message = (Message) itMessages.next();
        notMsg.addMessage(message.getFullMessage());
        if (!notRec.getAutoAck()) {
          // putting the message in the delivered messages table:
          consumers.put(message.getId(), notRec.requester);
          contexts.put(message.getId(),
                       new Integer(notRec.getClientContext()));
          deliveredMsgs.put(message.getId(), message);
          messages.remove(message);
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Message " + message.getId() + " to " + notRec.requester + " as reply to " + notRec.getRequestId());
        if (logmsg.isLoggable(BasicLevel.INFO))
          logmsg.log(BasicLevel.INFO,
                     getName() + ": delivers message " + message.getId()  + " to " + notRec.requester + " / " + notRec.getRequestId());
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
    if (dmqManager != null)
      dmqManager.sendToDMQ();
  }

  /**
   * Returns true if conditions are ok to deliver the message.
   * This method must be overloaded in subclasses.
   * Be careful only the message header is accessible.
   */
  protected boolean checkDelivery(org.objectweb.joram.shared.messages.Message msg) {
    return true;
  }

  /** 
   * call in deliverMessages just after forward(msg),
   * overload this method to process a specific treatment.
   */
  protected void messageDelivered(String msgId) {}

  /** 
   * call in deliverMessages just after a remove message (invalid),
   * overload this method to process a specific treatment.
   */
  protected void messageRemoved(String msgId) {}

  /**
   * Returns <code>true</code> if a given message is considered as  undeliverable,
   * because its delivery count matches the queue's threshold, if any, or the
   * server's default threshold value (if any).
   */
  protected boolean isUndeliverable(Message message) {
    if (threshold == 0) return false;
    if (threshold > 0)
      return (message.getDeliveryCount() >= threshold);
    else if (Queue.getDefaultThreshold() > 0)
      return (message.getDeliveryCount() >= Queue.getDefaultThreshold());
    return false;
  }

  /**
   * Adds the client messages in the queue.
   * 
   * @param clientMsgs client message notification.
   * @param throwsExceptionOnFullDest true, can throws an exception on sending message on full destination
   * @throws AccessException
   */
  public void addClientMessages(ClientMessages clientMsgs, boolean throwsExceptionOnFullDest) throws AccessException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.addClientMessage(" + clientMsgs + ')');
    
    if (clientMsgs != null) {
      Message msg;
      // Storing each received message:
      for (Iterator msgs = clientMsgs.getMessages().iterator(); msgs.hasNext();) {
        msg = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
        msg.order = arrivalsCounter++;
        
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Queue.addClientMessage() -> " + msg.getId() + ',' + msg.order);

        if (interceptorsAvailable()) {
        	// get the shared message
        	org.objectweb.joram.shared.messages.Message message = msg.getFullMessage();
        	// set the destination name
        	message.setProperty("JoramDestinationName", getName());
        	// interceptors process
      		org.objectweb.joram.shared.messages.Message m = processInterceptors(message);
      		if (m == null) {
      			// send message to the DMQ
      			DMQManager dmqManager = new DMQManager(dmqId, getId());
            nbMsgsSentToDMQSinceCreation++;
            dmqManager.addDeadMessage(msg.getFullMessage(), MessageErrorConstants.INTERCEPTORS);
            dmqManager.sendToDMQ();
      			msg.releaseFullMessage();
      			continue;
      		} else {
      			msg = new org.objectweb.joram.mom.messages.Message(m);
      		}
      	}
        
        // store message
        storeMessage(msg, throwsExceptionOnFullDest);
      }
    }
    // Launching a delivery sequence:
    deliverMessages(0);
  }

  protected void handleExpiredNot(AgentId from, ExpiredNot not) {
    Notification expiredNot = not.getExpiredNot();
    List messages;
    // ClientMessages and TopicMsgsReply are the notifications which can expire in the networks.
    // QueueMsgReply can't expire due to protocol limitations
    if (expiredNot instanceof ClientMessages) {
      messages = ((ClientMessages) expiredNot).getMessages();
    } else if (expiredNot instanceof TopicMsgsReply) {
      messages = ((TopicMsgsReply) expiredNot).getMessages();
    } else {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
                   "Expired notification holds an unknown notification: " + expiredNot.getClass().getName());
      return;
    }

    // Let senderId to null because we want to explicitly send messages to the queue itself.
    DMQManager dmqManager = new DMQManager(getId(), null);
    Iterator iterator = messages.iterator();
    while (iterator.hasNext()) {
      dmqManager.addDeadMessage((org.objectweb.joram.shared.messages.Message) iterator.next(),
                                MessageErrorConstants.EXPIRED);
    }
    dmqManager.sendToDMQ();
  }

	// Get flow Control related informations.
	// TODO AF: may be we can use generic Destination.getJMXStatistics method.
	protected fr.dyade.aaa.common.stream.Properties getStats() {
	  fr.dyade.aaa.common.stream.Properties stats = new fr.dyade.aaa.common.stream.Properties();
	  
		stats.put("NbMsgsDeliverSinceCreation", nbMsgsDeliverSinceCreation);
		stats.put("PendingMessageCount", getPendingMessageCount());
		
		return stats;
	}
	
	public int getEncodableClassId() {
    return JoramHelper.QUEUE_CLASS_ID;
  }
  
  public int getEncodedSize() throws Exception {
    int encodedSize = super.getEncodedSize();
    encodedSize += LONG_ENCODED_SIZE;
    encodedSize += INT_ENCODED_SIZE;
    Iterator<Entry<String, AgentId>> consumerIterator = consumers.entrySet().iterator();
    while (consumerIterator.hasNext()) {
      Entry<String, AgentId> consumer = consumerIterator.next();
      encodedSize += EncodableHelper.getStringEncodedSize(consumer.getKey());
      encodedSize += consumer.getValue().getEncodedSize();
    }
    encodedSize += INT_ENCODED_SIZE;
    Iterator<Entry<String, Integer>> contextIterator = contexts.entrySet().iterator();
    while (contextIterator.hasNext()) {
      Entry<String, Integer> context = contextIterator.next();
      encodedSize += EncodableHelper.getStringEncodedSize(context.getKey());
      encodedSize += INT_ENCODED_SIZE;
    }
    encodedSize += INT_ENCODED_SIZE * 3;
    for (ReceiveRequest request : requests) {
      encodedSize += request.getEncodedSize();
    }
    return encodedSize;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encodeUnsignedLong(arrivalsCounter);
    encoder.encodeUnsignedInt(consumers.size());
    Iterator<Entry<String, AgentId>> consumerIterator = consumers.entrySet().iterator();
    while (consumerIterator.hasNext()) {
      Entry<String, AgentId> consumer = consumerIterator.next();
      encoder.encodeString(consumer.getKey());
      consumer.getValue().encode(encoder);
    }
    encoder.encodeUnsignedInt(contexts.size());
    Iterator<Entry<String, Integer>> contextIterator = contexts.entrySet().iterator();
    while (contextIterator.hasNext()) {
      Entry<String, Integer> context = contextIterator.next();
      encoder.encodeString(context.getKey());
      encoder.encodeUnsignedInt(context.getValue());
    }
    encoder.encodeUnsignedInt(nbMaxMsg);
    encoder.encodeUnsignedInt(priority);
    encoder.encodeUnsignedInt(requests.size());
    for (ReceiveRequest request : requests) {
      request.encode(encoder);
    }
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    arrivalsCounter = decoder.decodeUnsignedLong();
    int consumersSize = decoder.decodeUnsignedInt();
    consumers = new Hashtable<String, AgentId>(consumersSize);
    for (int i = 0; i < consumersSize; i++) {
      String key = decoder.decodeString();
      AgentId value = new AgentId((short) 0, (short) 0, 0);
      value.decode(decoder);
      consumers.put(key, value);
    }
    int contextsSize = decoder.decodeUnsignedInt();
    contexts = new Hashtable<String, Integer>(contextsSize);
    for (int i = 0; i < contextsSize; i++) {
      String key = decoder.decodeString();
      Integer value = decoder.decodeUnsignedInt();
      contexts.put(key, value);
    }
    nbMaxMsg = decoder.decodeUnsignedInt();
    priority = decoder.decodeUnsignedInt();
    int requestsSize = decoder.decodeUnsignedInt();
    requests = new Vector<ReceiveRequest>(requestsSize);
    for (int i = 0; i < requestsSize; i++) {
      ReceiveRequest request = new ReceiveRequest();
      request.decode(decoder);
      requests.add(request);
    }
  }
  
  public static class QueueFactory implements EncodableFactory {

    public Encodable createEncodable() {
      // These are just initial values to be changed when decoding the agent
      return new Queue(null, false, AgentId.MinWKSIdStamp);
    }
    
  }
  
}
