/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AckJoinQueueCluster;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.JoinQueueCluster;
import org.objectweb.joram.mom.notifications.LBCycleLife;
import org.objectweb.joram.mom.notifications.LBMessageGive;
import org.objectweb.joram.mom.notifications.LBMessageHope;
import org.objectweb.joram.mom.notifications.LeaveQueueCluster;
import org.objectweb.joram.mom.notifications.QueueClusterNot;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.SpecialAdminRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.admin.AddQueueCluster;
import org.objectweb.joram.shared.admin.ListClusterQueue;
import org.objectweb.joram.shared.admin.RemoveQueueCluster;
import org.objectweb.joram.shared.admin.SpecialAdmin;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownNotificationException;

/**
 * The <code>ClusterQueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests or
 * delivering to an other cluster queue.
 */
public class ClusterQueueImpl extends QueueImpl {
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
  private Hashtable timeTable;

  /**
   * key = msgId value = List (alreadyVisit)
   */
  private Hashtable visitTable;

  /** Number of message send to cluster */
  private long clusterDeliveryCount;

  /** Waiting after a cluster request */
  private long waitAfterClusterReq = -1;

  /**
   * Maximum period of time before forwarding a waiting message or request to
   * other queues of the cluster. By default it is set to
   * <code>QueueImpl.period</code>.
   */
  private long timeThreshold = -1L;

  /**
   * Constructs a <code>ClusterQueueImpl</code> instance.
   *
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public ClusterQueueImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);

    /** producer threshold */
    int producThreshold = -1;
    /** consumer threshold */
    int consumThreshold = -1;
    /** automatic eval threshold */
    boolean autoEvalThreshold = false;

    if (prop != null) {
      try {
        waitAfterClusterReq = 
          Long.valueOf(prop.getProperty("waitAfterClusterReq")).longValue();
      } catch (NumberFormatException exc) {
        waitAfterClusterReq = 60000;
      }
      try {
        producThreshold = 
          Integer.valueOf(prop.getProperty("producThreshold")).intValue();
      } catch (NumberFormatException exc) {
        producThreshold = 10000;
      }
      try {
        consumThreshold = 
          Integer.valueOf(prop.getProperty("consumThreshold")).intValue();
      } catch (NumberFormatException exc) {
        consumThreshold = 10000;
      }
      autoEvalThreshold =
        Boolean.valueOf(prop.getProperty("autoEvalThreshold")).booleanValue();
      try {
        timeThreshold = 
          Long.valueOf(prop.getProperty("timeThreshold")).longValue();
      } catch (NumberFormatException exc) {
        timeThreshold = period;
      }
    }
    
    loadingFactor = new LoadingFactor(this,
                                      producThreshold,
                                      consumThreshold,
                                      autoEvalThreshold,
                                      waitAfterClusterReq);
    timeTable = new Hashtable();
    visitTable = new Hashtable();
    clusterDeliveryCount = 0;
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

  public String toString() {
    return "ClusterQueueImpl:" + getId().toString();
  }
 
  /**
   * use to add or remove ClusterQueue to cluster. 
   * 
   * @param not
   */
  public Object specialAdminProcess(SpecialAdminRequest not) 
    throws RequestException {

    Object ret = null;
    try {
      SpecialAdmin req = not.getRequest();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "--- " + this + " specialAdminProcess : " + req);

      if (req instanceof AddQueueCluster) {
        addQueueCluster(((AddQueueCluster) req).joiningQueue,
                        loadingFactor.getRateOfFlow());
      } else if (req instanceof RemoveQueueCluster) {
        broadcastLeave(((RemoveQueueCluster) req).removeQueue);
        removeQueueCluster(((RemoveQueueCluster) req).removeQueue);
      } else if(req instanceof ListClusterQueue) {
        ret = doList((ListClusterQueue) req);
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, 
                   "--- " + this + " specialAdminProcess", exc);
      throw new RequestException(exc.getMessage());
    }
    return ret;
  }

  /**
   * return the cluster list.
   * 
   * @param req
   * @return the cluster list.
   */
  protected Object doList(ListClusterQueue req) {
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
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " ClusterQueueImpl.addQueueCluster in " + getId() + "\njoiningQueue=" + joiningQueue + "\nclusters=" + clusters);

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
  public void removeQueueCluster(String removeQueue) {
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
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " ClusterQueueImpl.removeQueueCluster in " + getId() + "\nremoveQueue=" + removeQueue + "\nclusters=" + clusters);
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
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      msg = new Message((org.objectweb.joram.shared.messages.Message) msgs.nextElement());
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
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " ClusterQueueImpl.wakeUpNot(" + not + ")");
    
    super.wakeUpNot(not);

    if (clusters.size() > 1)
      loadingFactor.factorCheck(clusters, getPendingMessageCount(), getWaitingRequestCount());

    // Check if there is message arrived before "timeThreshold".
    // if is true forwards message to the next (no visited) clusterQueue.
    List toGive = new ArrayList();
    long oldTime = System.currentTimeMillis() - timeThreshold;
    for (Enumeration e = timeTable.keys(); e.hasMoreElements(); ) {
      String msgId = (String) e.nextElement();
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
  public void lBCycleLife(AgentId from, LBCycleLife not) {

    clusters.put(from,new Float(not.getRateOfFlow()));

    Hashtable vT = not.getVisitTable();
    for (Enumeration e = vT.keys(); e.hasMoreElements(); ) {
      String msgId = (String) e.nextElement();
      visitTable.put(msgId,vT.get(msgId));
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this +
                 " ClusterQueueImpl.lBCycleLife(" + not + ")" +
                 "\nvisitTable=" + clusters);
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
  public void joinQueueCluster(JoinQueueCluster not) {
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
      logger.log(BasicLevel.DEBUG,
                 "--- " + this +
                 " ClusterQueueImpl.joinQueueCluster(" + not + ")" +
                 "\nclusters=" + clusters +
                 "\nclients=" + clients);
  }

  /**
   * 
   * @param not AckJoinQueueCluster
   */
  public void ackJoinQueueCluster(AckJoinQueueCluster not) {
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
  
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this +
                 " ClusterQueueImpl.ackJoinQueueCluster(" + not + ")" +
                 "\nclusters=" + clusters +
                 "\nclients=" + clients);
  }

  /**
   * 
   * @param not ReceiveRequest
   */
  public void receiveRequest(AgentId from, ReceiveRequest not) throws AccessException {
    super.receiveRequest(from, not);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + 
                 " ClusterQueueImpl.receiveRequest(" + not + ")");

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
  public void lBMessageGive(AgentId from, LBMessageGive not) 
    throws UnknownNotificationException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " ClusterQueueImpl.lBMessageGive(" + from + "," + not + ")");

    clusters.put(from,new Float(not.getRateOfFlow()));

    ClientMessages cm = not.getClientMessages();
    if (cm != null)
      doClientMessages(from, cm);
  }

  /**
   * load balancing message hope by the "from" queue.
   * 
   * @param from
   * @param not   LBMessageHope
   */
  public void lBMessageHope(AgentId from, LBMessageHope not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " ClusterQueueImpl.lBMessageHope(" + from + "," + not + ")");

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
        logger.log(BasicLevel.DEBUG, 
                   "--- " + this +
                   " ClusterQueueImpl.lBMessageHope LBMessageHope : nbMsgSend = " + 
                   cm.getMessages().size());
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
      for (Enumeration e = cm.getMessages().elements();
           e.hasMoreElements(); ) {
        org.objectweb.joram.shared.messages.Message message = 
          (org.objectweb.joram.shared.messages.Message) e.nextElement();
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
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + 
                 " ClusterQueueImpl.sendToCluster(" + not + ")");

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
