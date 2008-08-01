/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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

import java.io.*;
import java.util.*;

import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;
import org.objectweb.joram.shared.selectors.*;
import org.objectweb.joram.mom.dest.*;
import org.objectweb.joram.shared.admin.*;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.UnknownNotificationException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;

/**
 * The <code>ClusterQueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests or
 * delivering to an other cluster queue.
 */
public class ClusterQueueImpl extends QueueImpl {
  /** 
   * key = agentId of ClusterQueue 
   * value = rateOfFlow (Float)
   */
  protected Hashtable clusters;

  /** to calcul the loading factor, overloaded, ... */
  protected LoadingFactor loadingFactor;

  /** key = msgId
   * value = date 
   */
  private Hashtable timeTable;

  /** key = msgId
   * value = Vector (alreadyVisit)
   */
  private Hashtable visitTable;

  /** number of message send to cluster */
  private long clusterDeliveryCount;

  /** waiting after a cluster request */
  private long waitAfterClusterReq = -1;

  /**
   * Constructs a <code>ClusterQueueImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   */
  public ClusterQueueImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);

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
    }

    clusters = new Hashtable();
    clusters.put(destId, new Float(1));

    loadingFactor = new LoadingFactor(this,
                                      producThreshold,
                                      consumThreshold,
                                      autoEvalThreshold,
                                      waitAfterClusterReq);
    timeTable = new Hashtable();
    visitTable = new Hashtable();
    clusterDeliveryCount = 0;

  }

  public String toString() {
    return "ClusterQueueImpl:" + destId.toString();
  }

  /** 
   * implement special process (see QueueImpl). 
   */
  protected void specialProcess(Notification not) {
    if (not instanceof ClientMessages)
      doProcess((ClientMessages) not);
    else if (not instanceof SetRightRequest)
      doProcess((SetRightRequest) not);
    else
      super.specialProcess(not);
  }

  /** propagate right to all cluster. */
  protected void doProcess(SetRightRequest not) {
    super.doProcess(not);
    sendToCluster(
      new SetRightQueueCluster(
        loadingFactor.getRateOfFlow(),
        not,
        clients));
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")" +
                                    "\nclients=" + clients);
  }

  /** 
   * use to add or remove ClusterQueue to cluster. 
   */
  public Object specialAdminProcess(SpecialAdminRequest not) 
    throws RequestException {

    Object ret = null;
    try {
      SpecialAdmin req = not.getRequest();
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- " + this +
                                      " specialAdminProcess : " +
                                      req);

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
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, 
                                      "--- " + this +
                                      " specialAdminProcess",
                                      exc);
      throw new RequestException(exc.getMessage());
    }
    return ret;
  }
  
  /** return the cluster list (vector). */
  protected Object doList(ListClusterQueue req) {
    Vector vect = new Vector();
    for (Enumeration e = clusters.keys(); e.hasMoreElements(); )
      vect.add(e.nextElement().toString());
    return vect;
  }
 
  /**
   *  send to joiningQueue a JoinQueueCluster not.
   */
  protected void addQueueCluster(String joiningQueue, float rateOfFlow) {
    AgentId id = AgentId.fromString(joiningQueue);
    if (clusters.containsKey(id)) return;

//    clusters.put(id,new Float(rateOfFlow));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.addQueueCluster in " + destId +
                                    "\njoiningQueue=" + joiningQueue +
                                    "\nclusters=" + clusters);

    Channel.sendTo(id,
                   new JoinQueueCluster(loadingFactor.getRateOfFlow(),
                                        clusters,
                                        clients,
                                        freeReading,
                                        freeWriting));
  }
  
  /** 
   * broadcast to cluster the removeQueue. 
   */
  protected void broadcastLeave(String removeQueue) {
    sendToCluster(new LeaveQueueCluster(removeQueue));
  }

  /** 
   * removeQueue leave the cluster.
   */
  protected void removeQueueCluster(String removeQueue) {
    AgentId id = AgentId.fromString(removeQueue);
    if (destId.equals(id)) {
      clusters.clear();
    } else
      clusters.remove(id);

    for (Enumeration e = visitTable.elements(); e.hasMoreElements(); ) {
      Vector visit = (Vector) e.nextElement();
      if (visit.contains(id))
        visit.remove(id);
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.removeQueueCluster in " + destId +
                                    "\nremoveQueue=" + removeQueue +
                                    "\nclusters=" + clusters);
  }
  
  /** 
   * overload doProcess(ClientMessages)
   * store all msgId in timeTable and visitTable,
   * store message and deliver message if consumer
   * wait.
   * call factorCheck to evaluate the loading factor,
   * activity, ... and send message to cluster if need.
   */
  protected void doProcess(ClientMessages not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this + 
                                    " " + not);

    receiving = true;

    long date = System.currentTimeMillis();
    
    Message msg;
    // Storing each received message:
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {

      if (arrivalsCounter == Long.MAX_VALUE)
        arrivalsCounter = 0;

      msg = (Message) msgs.nextElement();
      msg.order = arrivalsCounter++;
      storeMessage(msg);
      storeMsgIdInTimeTable(msg.getIdentifier(),
                            new Long(date));
//        storeMsgIdInVisitTable(msg.getIdentifier(),
//                               destId);
    }

    // Lauching a delivery sequence:
    deliverMessages(0);

    if (getNumberOfPendingMessages() > loadingFactor.producThreshold)
      loadingFactor.factorCheck(clusters,
                                getNumberOfPendingMessages(),
                                getNumberOfPendingRequests());
    else
      loadingFactor.evalRateOfFlow(getNumberOfPendingMessages(),
                                   getNumberOfPendingRequests());
    receiving = false;
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  When receiving an unexpected
   *              notification.
   */
  public void react(AgentId from, Notification not)
    throws UnknownNotificationException {

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " react(" + from + "," + not + ")");

    if (not instanceof AckJoinQueueCluster)
      doReact((AckJoinQueueCluster) not);
    else if (not instanceof JoinQueueCluster)
      doReact((JoinQueueCluster) not);
    else if (not instanceof LeaveQueueCluster)
      removeQueueCluster(((LeaveQueueCluster) not).removeQueue);
    else if (not instanceof ReceiveRequest) {
      super.react(from, not);
      doReact((ReceiveRequest) not);
    } else if (not instanceof LBMessageGive)
      doReact(from, (LBMessageGive) not);
    else if (not instanceof LBMessageHope)
      doReact(from, (LBMessageHope) not);
    else if (not instanceof LBCycleLife)
      doReact(from, (LBCycleLife) not);
    else if (not instanceof WakeUpNot)
      doReact((WakeUpNot) not);
    else if (not instanceof SetRightQueueCluster)
      doReact((SetRightQueueCluster) not);
    else
      super.react(from, not);
  }

  /** set the same right to all cluster */
  protected void doReact(SetRightQueueCluster not) {
    try {
      AgentId user = not.setRightRequest.getClient();
      int right = not.setRightRequest.getRight();
      super.processSetRight(user,right);
    } catch (RequestException exc) {}
    super.doProcess(not.setRightRequest);
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")" +
                                    "\nclients=" + clients);
  }

  /**
   * wake up, and call factorCheck to evaluate the loading factor...
   * if msg stay more a periode time in timeTable send to an other
   * (no visited) queue in cluster.
   */
  protected void doReact(WakeUpNot not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")");

    super.doReact(not);

    if (clusters.size() > 1)
      loadingFactor.factorCheck(clusters,
                                getNumberOfPendingMessages(),
                                getNumberOfPendingRequests());

    // check if msg arrived befor "period".
    // if is true send msg to the next (no visited) clusterQueue.
    Vector toGive = new Vector();
    long oldTime = System.currentTimeMillis() - period;
    for (Enumeration e = timeTable.keys(); e.hasMoreElements(); ) {
      String msgId = (String) e.nextElement();
      if (((Long) timeTable.get(msgId)).longValue() < oldTime) {
        toGive.add(msgId);
        storeMsgIdInVisitTable(msgId,destId);
      }
    }

    if (toGive.isEmpty()) return;

    Hashtable table = new Hashtable();
    for (int i = 0; i < toGive.size(); i++) {
      String msgId = (String) toGive.get(i);
      Vector visit = (Vector) visitTable.get(msgId);
      for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
        AgentId id = (AgentId) e.nextElement();
        if (! visit.contains(id)) {
          LBCycleLife cycle = (LBCycleLife) table.get(id);
          if (cycle == null) {
            cycle = new LBCycleLife(loadingFactor.getRateOfFlow());
            cycle.setClientMessages(new ClientMessages());
          }
          ClientMessages cm = cycle.getClientMessages();
          Message msg = removeMessage(msgId);
          if (msg != null) {
            cm.addMessage(msg);
            cycle.putInVisitTable(msgId,visit);
            table.put(id,cycle);
            break;
          }
        }
      }
    }

    for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      loadingFactor.processGive(id,(LBCycleLife) table.get(id));
    }
  }

  /**
   * The messages are not consumed by an other cluster's queue
   * in a periode time, try to consume in this queue.
   * update visitTable, and process clientMessages. 
   */
  protected void doReact(AgentId from, LBCycleLife not) {

    clusters.put(from,new Float(not.getRateOfFlow()));

    Hashtable vT = not.getVisitTable();
    for (Enumeration e = vT.keys(); e.hasMoreElements(); ) {
      String msgId = (String) e.nextElement();
      visitTable.put(msgId,vT.get(msgId));
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")" +
                                    "\nvisitTable=" + clusters);
    ClientMessages cm = not.getClientMessages();
    if (cm != null)
      doProcess(cm);
  }

  /**
   * new queue come in cluster, update clusters.
   * and spread to clusters the AckjoiningQueue.
   */
  protected void doReact(JoinQueueCluster not) {
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
  
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")" +
                                    "\nclusters=" + clusters +
                                    "\nclients=" + clients);
  }

  protected void doReact(AckJoinQueueCluster not) {
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
  
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + not + ")" +
                                    "\nclusters=" + clusters +
                                    "\nclients=" + clients);
  }

  /**
   * 
   */
  protected void doReact(ReceiveRequest not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this + 
                                    " ClusterQueueImpl.doReact(" + not + ")");

    //loadingFactor.setWait();

    if (getNumberOfPendingRequests() > loadingFactor.consumThreshold)
      loadingFactor.factorCheck(clusters,
                                getNumberOfPendingMessages(),
                                getNumberOfPendingRequests());
  }

  /** 
   * load balancing message give by an other cluster queue.
   * process ClientMessages, no need to check if sender is writer.
   */
  protected void doReact(AgentId from, LBMessageGive not) 
    throws UnknownNotificationException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " ClusterQueueImpl.doReact(" + from + "," + not + ")");

    clusters.put(from,new Float(not.getRateOfFlow()));

    ClientMessages cm = not.getClientMessages();
    if (cm != null)
      doProcess(cm);
  }

  /** 
   * load balancing message hope by the "from" queue.
   */
  protected void doReact(AgentId from, LBMessageHope not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this + 
                                    " ClusterQueueImpl.doReact(" + from + "," + not + ")");
    
    clusters.put(from,new Float(not.getRateOfFlow()));

    int hope = not.getNbMsg();
    // TODO: if validityperiod
    if (loadingFactor.getRateOfFlow() < 1) {
      int possibleGive = getNumberOfPendingMessages() - getNumberOfPendingRequests();
      LBMessageGive msgGive = 
        new LBMessageGive(waitAfterClusterReq,loadingFactor.getRateOfFlow());
      ClientMessages cm = new ClientMessages();
      for (int i = 0; (i < possibleGive) && (i < hope); i++) {
        if (! messagesIsEmpty()) {
          Message msg = removeMessage(0);
          cm.addMessage(msg);
        } else 
          break;
      }
      msgGive.setClientMessages(cm);
      msgGive.setRateOfFlow(
        loadingFactor.evalRateOfFlow(getNumberOfPendingMessages(),
                                     getNumberOfPendingRequests()));
      Channel.sendTo(from,msgGive);
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- " + this +
                                      " ClusterQueueImpl.doReact LBMessageHope : nbMsgSend = " + 
                                      cm.getMessages().size());

      for (Enumeration e = cm.getMessages().elements(); e.hasMoreElements(); ) {
        Message msg = (Message) e.nextElement();
        messageSendToCluster(msg.getIdentifier());
        deletePersistenceMessage(msg);
      }
    }
  }

  /**
   * send to all queue in cluster.
   */
  protected void sendToCluster(QueueClusterNot not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this + 
                                    " ClusterQueueImpl.sendToCluster(" + not + ")");

    if (clusters.size() < 2) return;

    for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (! id.equals(destId))
        Channel.sendTo(id,not);
    }
  }

  /** 
   * return the number of Message send to cluster.
   */
  public long getClusterDeliveryCount() {
    return clusterDeliveryCount;
  }

  /**
   * return index of message.
   */
  private int getIndexOfMessage(String msgId) {
    for (int i = 0; i < messages.size(); i++) {
      Message msg = (Message) messages.get(i);
      if (msgId.equals(msg.getIdentifier()))
        return i;
    }
    return -1;
  }

  private void storeMsgIdInTimeTable(String msgId, Long date) {
    try {
      timeTable.put(msgId,date);
    } catch (NullPointerException exc) {}
  }

  private void storeMsgIdInVisitTable(String msgId, AgentId destId) {
    Vector alreadyVisit = (Vector) visitTable.get(msgId);
    if (alreadyVisit == null) alreadyVisit = new Vector();
    alreadyVisit.add(destId);
    visitTable.put(msgId,alreadyVisit);
  }

  protected void messageDelivered(String msgId) {
    timeTable.remove(msgId);
    visitTable.remove(msgId);
  }

  protected void messageRemoved(String msgId) {
    timeTable.remove(msgId);
    visitTable.remove(msgId);
  }

  protected void messageSendToCluster(String msgId) {
    timeTable.remove(msgId);
    visitTable.remove(msgId);
    clusterDeliveryCount++;
  }

  Message removeMessage(String msgId) {
    for (Enumeration e = messages.elements(); e.hasMoreElements(); ) {
      Message msg = (Message) e.nextElement();
      if (msgId.equals(msg.getIdentifier())) {
        // fix bug for softRefMessage
        msg.setPin(true);
        if (messages.remove(msg))
          return msg;
        else 
          return null;
      }
    }
    return null;
  }

  Message removeMessage(int index) {
    Message msg = (Message) messages.remove(0);
    // fix bug for softRefMessage
    msg.setPin(true);
    return msg;
  }

  boolean messagesIsEmpty() {
   return messages.isEmpty();
  }

  void deletePersistenceMessage(Message msg) {
    msg.delete();
  }

  AgentId getDestId() {
    return destId;
  }

  public int getNumberOfPendingMessages() {
    return messages.size();
  }
  
  public int getNumberOfPendingRequests() {
    return requests.size();
  }
  
  public void setWaitAfterClusterReq(long waitAfterClusterReq) {
    this.waitAfterClusterReq = waitAfterClusterReq;
    loadingFactor.validityPeriod = waitAfterClusterReq;
  }
  
  public void setProducThreshold(int producThreshold) {
    loadingFactor.producThreshold = producThreshold;
  }
  
  public void setConsumThreshold(int consumThreshold) {
    loadingFactor.consumThreshold = consumThreshold;
  }
  
  public void setAutoEvalThreshold(boolean autoEvalThreshold) {
    loadingFactor.autoEvalThreshold = autoEvalThreshold;
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {

    in.defaultReadObject();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "--- " + this + 
                                    " ClusterQueueImpl.readObject" +
                                    " loadingFactor = " + loadingFactor);
  }
}
