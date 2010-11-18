/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
package org.objectweb.joram.mom.dest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AckJoinQueueCluster;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.JoinQueueCluster;
import org.objectweb.joram.mom.notifications.LBCycleLife;
import org.objectweb.joram.mom.notifications.LBMessageGive;
import org.objectweb.joram.mom.notifications.LBMessageHope;
import org.objectweb.joram.mom.notifications.LeaveQueueCluster;
import org.objectweb.joram.mom.notifications.QueueClusterNot;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.admin.AddQueueCluster;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ListClusterQueue;
import org.objectweb.joram.shared.admin.RemoveQueueCluster;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

/**
 * The <code>ClusterQueue</code> class implements the cluster queue behavior.
 */
public class ClusterQueue extends Queue {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** 
   * key = agentId of ClusterQueue 
   * value = rateOfFlow (Float)
   */
  protected Hashtable clusters;

  /** to evaluate the loading factor, overloading, ... */
  protected LoadingFactor loadingFactor;

  /**
   * key = msgId
   * value = date 
   */
  private LinkedHashMap timeTable = new LinkedHashMap();

  /**
   * key = msgId value = List (alreadyVisit)
   */
  private Hashtable visitTable = new Hashtable();

  /** Number of message send to cluster */
  private long clusterDeliveryCount = 0;

  /** Waiting after a cluster request */
  private long waitAfterClusterReq = -1;

  /**
   * Maximum period of time before forwarding a waiting message or request to
   * other queues of the cluster. By default it is set to
   * <code>Queue.period</code>.
   */
  private long timeThreshold = -1L;

  /**
   * Configures a <code>ClusterQueue</code> instance.
   * 
   * @param prop The initial set of properties.
   */
  public void setProperties(Properties prop) throws RequestException {
    super.setProperties(prop);

    /** producer threshold */
    int producThreshold = -1;
    /** consumer threshold */
    int consumThreshold = -1;
    /** automatic eval threshold */
    boolean autoEvalThreshold = false;

    if (prop != null) {
      try {
        waitAfterClusterReq = Long.valueOf(prop.getProperty("waitAfterClusterReq")).longValue();
      } catch (NumberFormatException exc) {
        waitAfterClusterReq = 60000;
      }
      try {
        producThreshold = Integer.valueOf(prop.getProperty("producThreshold")).intValue();
      } catch (NumberFormatException exc) {
        producThreshold = 10000;
      }
      try {
        consumThreshold = Integer.valueOf(prop.getProperty("consumThreshold")).intValue();
      } catch (NumberFormatException exc) {
        consumThreshold = 10000;
      }
      autoEvalThreshold = Boolean.valueOf(prop.getProperty("autoEvalThreshold")).booleanValue();
      try {
        timeThreshold = Long.valueOf(prop.getProperty("timeThreshold")).longValue();
      } catch (NumberFormatException exc) {
        timeThreshold = getPeriod();
      }
    }

    loadingFactor = new LoadingFactor(this, producThreshold, consumThreshold, autoEvalThreshold,
        waitAfterClusterReq);
  }
  
  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "initialize(" + firstTime + ')');
    
    super.initialize(firstTime);

    if (firstTime) {
      clusters = new Hashtable();
      clusters.put(getId(), new Float(1));
    }
  }

  public void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {

    setSave(); // state change, so save.

    AdminRequest adminRequest = not.getRequest();
    String info = strbuf.append("Request [").append(not.getClass().getName())
        .append("], sent to Destination [").append(getId()).append("], successful [true] ").toString();

    if (adminRequest instanceof ListClusterQueue) {
      List list = doList((ListClusterQueue) adminRequest);
      replyToTopic(new AdminReply(true, info, list), not.getReplyTo(), not.getRequestMsgId(),
          not.getReplyMsgId());
    } else if (adminRequest instanceof AddQueueCluster) {
      addQueueCluster(((AddQueueCluster) adminRequest).joiningQueue, loadingFactor.getRateOfFlow());
      replyToTopic(new AdminReply(true, info), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof RemoveQueueCluster) {
      broadcastLeave(((RemoveQueueCluster) adminRequest).removeQueue);
      removeQueueCluster(((RemoveQueueCluster) adminRequest).removeQueue);
      replyToTopic(new AdminReply(true, info), not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else {
      super.handleAdminRequestNot(from, not);
    }
    strbuf.setLength(0);
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " react(" + from + "," + not + ")");

    if (not instanceof AckJoinQueueCluster)
      ackJoinQueueCluster((AckJoinQueueCluster) not);
    else if (not instanceof JoinQueueCluster)
      joinQueueCluster((JoinQueueCluster) not);
    else if (not instanceof LeaveQueueCluster)
      removeQueueCluster(((LeaveQueueCluster) not).removeQueue);
    else if (not instanceof LBMessageGive)
      lBMessageGive(from, (LBMessageGive) not);
    else if (not instanceof LBMessageHope)
      lBMessageHope(from, (LBMessageHope) not);
    else if (not instanceof LBCycleLife)
      lBCycleLife(from, (LBCycleLife) not);
    else {
      super.react(from, not);
    }
  }

  public String toString() {
    return "ClusterQueue:" + getId().toString();
  }

  /**
   * return the cluster list.
   * 
   * @param req
   * @return the cluster list.
   */
  protected List doList(ListClusterQueue req) {
    List vect = new ArrayList();
    for (Enumeration e = clusters.keys(); e.hasMoreElements(); )
      vect.add(e.nextElement().toString());
    return vect;
  }
 
  /**
   * send to joiningQueue a JoinQueueCluster not.
   * 
   * @param joiningQueue
   * @param rateOfFlow
   */
  protected void addQueueCluster(String joiningQueue, float rateOfFlow) {
    AgentId id = AgentId.fromString(joiningQueue);
    if (clusters.containsKey(id)) return;

    //    clusters.put(id,new Float(rateOfFlow));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.addQueueCluster in " + getId()
          + "\njoiningQueue=" + joiningQueue + "\nclusters=" + clusters);

    forward(id,
            new JoinQueueCluster(loadingFactor.getRateOfFlow(),
                                 clusters,
                                 clients,
                                 freeReading,
                                 freeWriting));
  }
  
  /**
   * broadcast to cluster the removeQueue. 
   * 
   * @param removeQueue
   */
  protected void broadcastLeave(String removeQueue) {
    sendToCluster(new LeaveQueueCluster(removeQueue));
  }

  /**
   * removeQueue leave the cluster.
   * 
   * @param removeQueue
   */
  private void removeQueueCluster(String removeQueue) {
    AgentId id = AgentId.fromString(removeQueue);
    if (getId().equals(id)) {
      clusters.clear();
    } else
      clusters.remove(id);

    for (Enumeration e = visitTable.elements(); e.hasMoreElements(); ) {
      List visit = (List) e.nextElement();
      if (visit.contains(id))
        visit.remove(id);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.removeQueueCluster in " + getId()
          + "\nremoveQueue=" + removeQueue + "\nclusters=" + clusters);
  }
  
  /**
   * overload preProcess(AgentId, ClientMessages)
   * store all msgId in timeTable and visitTable.
   * 
   * @param from
   * @param not
   */
  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this +  " " + not);
    receiving = true;
    long date = System.currentTimeMillis();
    
    Message msg;
    // Storing each received message:
    for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
      msg = new Message((org.objectweb.joram.shared.messages.Message) msgs.next());
      msg.order = arrivalsCounter++;
      storeMsgIdInTimeTable(msg.getIdentifier(),
                            new Long(date));
      //storeMsgIdInVisitTable(msg.getIdentifier(), destId);
    }
    return not;
  }
  
  /**
   * call factorCheck to evaluate the loading factor,
   * activity, ... and send message to cluster if need.
   * 
   * @param not
   */
  public void postProcess(ClientMessages not) {
    if (getPendingMessageCount() > loadingFactor.producThreshold)
      loadingFactor.factorCheck(clusters, getPendingMessageCount(), getWaitingRequestCount());
    else
      loadingFactor.evalRateOfFlow(getPendingMessageCount(), getWaitingRequestCount());
    receiving = false;
  }

  /**
   * wake up, and call factorCheck to evaluate the loading factor... if a message
   * stays more than a period of time in timeTable, it is sent to an other (not
   * visited) queue in cluster.
   * 
   * @param not
   */
  public void wakeUpNot(WakeUpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.wakeUpNot(" + not + ")");
    
    super.wakeUpNot(not);

    if (clusters.size() > 1)
      loadingFactor.factorCheck(clusters, getPendingMessageCount(), getWaitingRequestCount());

    // Check if there is message arrived before "timeThreshold".
    // if is true forwards message to the next (no visited) clusterQueue.
    List toGive = new ArrayList();
    long oldTime = System.currentTimeMillis() - timeThreshold;
    
    Set keySet = timeTable.keySet();
    Iterator it = keySet.iterator();
    while (it.hasNext()) {
      String msgId = (String) it.next();
      if (((Long) timeTable.get(msgId)).longValue() < oldTime) {
        toGive.add(msgId);
        storeMsgIdInVisitTable(msgId, getId());
      }
    }
    
    if (toGive.isEmpty()) return;

    Hashtable table = new Hashtable();
    for (int i = 0; i < toGive.size(); i++) {
      String msgId = (String) toGive.get(i);
      List visit = (List) visitTable.get(msgId);
      boolean transmitted = false;
      for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
        AgentId id = (AgentId) e.nextElement();
        if (! visit.contains(id)) {
          Message message = getQueueMessage(msgId, true);
          if (message != null) {
            LBCycleLife cycle = (LBCycleLife) table.get(id);
            if (cycle == null) {
              cycle = new LBCycleLife(loadingFactor.getRateOfFlow());
              cycle.setClientMessages(new ClientMessages());
            }
            ClientMessages cm = cycle.getClientMessages();
            cm.addMessage(message.getFullMessage());
            cycle.putInVisitTable(msgId,visit);
            table.put(id,cycle);
            transmitted = true;
            break;
          }
        }
      }
      if (!transmitted) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " All queues already visited. Re-initialize visitTable.");
        ((List) visitTable.get(msgId)).clear();
      }
    }

    for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      forward(id,(LBCycleLife) table.get(id));
    }
  }

  /**
   * If the messages are not consumed by an other cluster's queue
   * in a period of time, try to consume in this queue.
   * update visitTable, and process clientMessages. 
   * 
   * @param from
   * @param not
   */
  private void lBCycleLife(AgentId from, LBCycleLife not) {

    clusters.put(from,new Float(not.getRateOfFlow()));

    Hashtable vT = not.getVisitTable();
    for (Enumeration e = vT.keys(); e.hasMoreElements(); ) {
      String msgId = (String) e.nextElement();
      visitTable.put(msgId,vT.get(msgId));
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.lBCycleLife(" + not + ")" + "\nvisitTable="
          + clusters);
    ClientMessages cm = not.getClientMessages();
    if (cm != null)
      doClientMessages(from, cm);
  }

  /**
   * new queue come in cluster, update clusters.
   * and spread to clusters the AckjoiningQueue.
   * 
   * @param not JoinQueueCluster
   */
  private void joinQueueCluster(JoinQueueCluster not) {
    for (Enumeration e = not.clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (! clusters.containsKey(id))
        clusters.put(id,not.clusters.get(id));
    }
    for (Enumeration e = not.clients.keys(); e.hasMoreElements(); ) {
      AgentId user = (AgentId) e.nextElement();
      if (clients.containsKey(user)) {
        Integer right = (Integer) not.clients.get(user);
        if (right.compareTo((Integer) clients.get(user)) > 0)
          clients.put(user,right);
      } else
        clients.put(user,not.clients.get(user));
    }

    freeReading = freeReading | not.freeReading;
    freeWriting = freeWriting | not.freeWriting;

    sendToCluster(
                  new AckJoinQueueCluster(loadingFactor.getRateOfFlow(),
                                          clusters,
                                          clients,
                                          freeReading,
                                          freeWriting));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.joinQueueCluster(" + not + ")"
          + "\nclusters=" + clusters + "\nclients=" + clients);
  }

  /**
   * 
   * @param not AckJoinQueueCluster
   */
  private void ackJoinQueueCluster(AckJoinQueueCluster not) {
    boolean update = false;
    for (Enumeration e = not.clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (! clusters.containsKey(id)) {
        clusters.put(id,not.clusters.get(id));
        update = true;
      }
    }
    for (Enumeration e = not.clients.keys(); e.hasMoreElements(); ) {
      AgentId user = (AgentId) e.nextElement();
      if (clients.containsKey(user)) {
        Integer right = (Integer) not.clients.get(user);
        if (right.compareTo((Integer) clients.get(user)) > 0)
          clients.put(user,right);
      } else
        clients.put(user,not.clients.get(user));
    }

    freeReading = freeReading | not.freeReading;
    freeWriting = freeWriting | not.freeWriting;
  
    if (update) {
      sendToCluster(
          new AckJoinQueueCluster(loadingFactor.getRateOfFlow(),
                                  clusters,
                                  clients,
                                  freeReading,
                                  freeWriting));
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.ackJoinQueueCluster(" + not + ")"
          + "\nclusters=" + clusters + "\nclients=" + clients);
  }

  /**
   * 
   * @param not ReceiveRequest
   */
  public void receiveRequest(AgentId from, ReceiveRequest not) throws AccessException {
    super.receiveRequest(from, not);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.receiveRequest(" + not + ")");

    //loadingFactor.setWait();

    if (getWaitingRequestCount() > loadingFactor.consumThreshold)
      loadingFactor.factorCheck(clusters, getPendingMessageCount(), getWaitingRequestCount());
  }

  /**
   * load balancing message give by an other cluster queue. process
   * ClientMessages, no need to check if sender is writer.
   * 
   * @param from
   *            AgentId
   * @param not
   *            LBMessageGive
   * @throws UnknownNotificationException
   */
  private void lBMessageGive(AgentId from, LBMessageGive not)
    throws UnknownNotificationException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.lBMessageGive(" + from + "," + not + ")");

    clusters.put(from,new Float(not.getRateOfFlow()));

    ClientMessages cm = not.getClientMessages();
    if (cm != null) {
      doClientMessages(from, cm);
    }
  }

  /**
   * load balancing message hope by the "from" queue.
   * 
   * @param from
   * @param not   LBMessageHope
   */
  private void lBMessageHope(AgentId from, LBMessageHope not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.lBMessageHope(" + from + "," + not + ")");

    clusters.put(from,new Float(not.getRateOfFlow()));

    int hope = not.getNbMsg();

    long current = System.currentTimeMillis();
    // Cleaning the possible expired messages.
    DMQManager dmqManager = cleanPendingMessage(current);
    // If needed, sending the dead messages to the DMQ:
    if (dmqManager != null)
      dmqManager.sendToDMQ();
    
    if (loadingFactor.getRateOfFlow() < 1) {
      int possibleGive = getPendingMessageCount() - getWaitingRequestCount();
      LBMessageGive msgGive = 
        new LBMessageGive(waitAfterClusterReq,loadingFactor.getRateOfFlow());
      
      // get client messages, hope or possible give.
      ClientMessages cm = null;
      if (possibleGive > hope) {
        cm = getClientMessages(hope, null, true);
      } else {
        cm = getClientMessages(possibleGive, null, true);
      }

      msgGive.setClientMessages(cm);
      msgGive.setRateOfFlow(loadingFactor.evalRateOfFlow(getPendingMessageCount(), getWaitingRequestCount()));

      // send notification contains ClientMessages.
      forward(from, msgGive);
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this
            + " ClusterQueue.lBMessageHope LBMessageHope : nbMsgSend = " + cm.getMessages().size());
    }
  }

  /**
   *  get a client message contain nb messages.
   *  add cluster monitoring value.
   *  
   * @param nb        number of messages returned in ClientMessage.
   * @param selector  jms selector
   * @param remove    delete all messages returned if true
   * @return ClientMessages (contains nb Messages)
   */
  protected ClientMessages getClientMessages(int nb, String selector, boolean remove) {
    ClientMessages cm = super.getClientMessages(nb, selector, remove);
    if (cm != null) {
      // set information in cluster
      for (Iterator e = cm.getMessages().iterator(); e.hasNext();) {
        org.objectweb.joram.shared.messages.Message message = 
          (org.objectweb.joram.shared.messages.Message) e.next();
        monitoringMsgSendToCluster(message.id);
      }
    }
    return cm;
  }
  
  /**
   * get mom message, delete if remove = true.
   * add cluster monitoring value.
   * 
   * @param msgId   message identification
   * @param remove  if true delete message
   * @return mom message
   */
  protected Message getQueueMessage(String msgId, boolean remove) {  
    Message msg = super.getQueueMessage(msgId, remove);
    if (msg != null) {
      monitoringMsgSendToCluster(msg.getIdentifier());
    }
    return msg;
  }
  
  /**
   * send to all queue in cluster.
   * 
   * @param not
   */
  protected void sendToCluster(QueueClusterNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " ClusterQueue.sendToCluster(" + not + ")");

    if (clusters.size() < 2) return;

    for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (! id.equals(getId()))
        forward(id,not);
    }
  }

  /** 
   * return the number of Message send to cluster.
   */
  public long getClusterDeliveryCount() {
    return clusterDeliveryCount;
  }

  /**
   * 
   * @param msgId
   * @param date
   */
  private void storeMsgIdInTimeTable(String msgId, Long date) {
    try {
      timeTable.put(msgId, date);
    } catch (NullPointerException exc) {}
  }

  /**
   * 
   * @param msgId
   * @param destId
   */
  private void storeMsgIdInVisitTable(String msgId, AgentId destId) {
    List alreadyVisit = (List) visitTable.get(msgId);
    if (alreadyVisit == null) alreadyVisit = new ArrayList();
    alreadyVisit.add(destId);
    visitTable.put(msgId, alreadyVisit);
  }

  /**
   * 
   * @param msgId
   */
  protected void messageDelivered(String msgId) {
    timeTable.remove(msgId);
    visitTable.remove(msgId);
  }

  /**
   * 
   * @param msgId
   */
  protected void monitoringMsgSendToCluster(String msgId) {
    timeTable.remove(msgId);
    visitTable.remove(msgId);
    clusterDeliveryCount++;
  }
  
  /**
   * 
   * @param waitAfterClusterReq
   */
  public void setWaitAfterClusterReq(long waitAfterClusterReq) {
    this.waitAfterClusterReq = waitAfterClusterReq;
    loadingFactor.validityPeriod = waitAfterClusterReq;
  }
  
  /**
   * 
   * @param producThreshold
   */
  public void setProducThreshold(int producThreshold) {
    loadingFactor.producThreshold = producThreshold;
  }
  
  /**
   * 
   * @param consumThreshold
   */
  public void setConsumThreshold(int consumThreshold) {
    loadingFactor.consumThreshold = consumThreshold;
  }
  
  /**
   * 
   * @param autoEvalThreshold
   */
  public void setAutoEvalThreshold(boolean autoEvalThreshold) {
    loadingFactor.autoEvalThreshold = autoEvalThreshold;
  }
}
