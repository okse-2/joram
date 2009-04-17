/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.LBMessageGive;
import org.objectweb.joram.mom.notifications.LBMessageHope;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.util.Debug;

public class LoadingFactor implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** logger */
  public static Logger logger = Debug.getLogger(LoadingFactor.class.getName());

  public static class Status {
    public final static int INIT = 0;
    public final static int RUN = 1;
    public final static int WAIT = 2;

    public final static String[] names = {"INIT", "RUN", "WAIT"};
  }

  public static class ConsumerStatus {
    public final static int CONSUMER_NO_ACTIVITY = 0;
    public final static int CONSUMER_HIGH_ACTIVITY = 1;
    public final static int CONSUMER_NORMAL_ACTIVITY = 2;

    public final static String[] names = {
                                          "CONSUMER_NO_ACTIVITY", 
                                          "CONSUMER_HIGH_ACTIVITY",
                                          "CONSUMER_NORMAL_ACTIVITY"
    };
  }

  public static class ProducerStatus {
    public final static int PRODUCER_NO_ACTIVITY = 0;
    public final static int PRODUCER_HIGH_ACTIVITY = 1;
    public final static int PRODUCER_NORMAL_ACTIVITY = 2;

    public final static String[] names =  {
                                           "PRODUCER_NO_ACTIVITY",
                                           "PRODUCER_HIGH_ACTIVITY",
                                           "PRODUCER_NORMAL_ACTIVITY"
    };
  }

  /** status */
  private int status;
  /** status time */
  private long statusTime;

  /** consumer status */
  private int consumerStatus = 0;
  /** producer status */
  private int producerStatus = 0;

  /** reference to clusterQueueImpl */
  public ClusterQueueImpl clusterQueueImpl;
  /** producer threshold */
  public int producThreshold = -1;
  /** consumer threshold */
  public int consumThreshold = -1;
  /** automatic eval threshold */
  public boolean autoEvalThreshold = false;
  /** validity period */
  public long validityPeriod = -1;

  private float rateOfFlow;
  private boolean overLoaded;
  private int nbOfPendingMessages;
  private int nbOfPendingRequests;

  public LoadingFactor(ClusterQueueImpl clusterQueueImpl,
                       int producThreshold,
                       int consumThreshold,
                       boolean autoEvalThreshold,
                       long validityPeriod) {
    this.clusterQueueImpl = clusterQueueImpl;
    this.producThreshold = producThreshold;
    this.consumThreshold = consumThreshold;
    this.autoEvalThreshold = autoEvalThreshold;
    this.validityPeriod = validityPeriod;
    rateOfFlow = 1;
    status = 0;
  }

  public void setRateOfFlow(float rateOfFlow) {
    this.rateOfFlow = rateOfFlow;
  }

  public float getRateOfFlow() {
    return rateOfFlow;
  }

  public void setWait() {
    status = Status.WAIT;
    statusTime = System.currentTimeMillis() + validityPeriod;
  }

  /**
   * this method eval the activity
   * of consumer and producer.
   */
  private void evalActivity() {
    if (nbOfPendingMessages == 0)
      producerStatus = ProducerStatus.PRODUCER_NO_ACTIVITY;
    else if (nbOfPendingMessages > producThreshold)
      producerStatus = ProducerStatus.PRODUCER_HIGH_ACTIVITY;
    else
      producerStatus = ProducerStatus.PRODUCER_NORMAL_ACTIVITY;

    if (nbOfPendingRequests == 0)
      consumerStatus = ConsumerStatus.CONSUMER_NO_ACTIVITY;
    else if (nbOfPendingRequests > consumThreshold)
      consumerStatus = ConsumerStatus.CONSUMER_HIGH_ACTIVITY;
    else
      consumerStatus = ConsumerStatus.CONSUMER_NORMAL_ACTIVITY;
  }

  /** 
   * update the threshol if autoEvalThreshold is true.
   */
  private void updateThreshol() {
    if (autoEvalThreshold) {

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.updateThreshol before" +
                   " rateOfFlow=" + rateOfFlow +
                   ", producThreshold=" + producThreshold +
                   ", consumThreshold=" + consumThreshold );

      int deltaProd;
      int deltaCons;

      if (rateOfFlow < 1) {
        deltaProd = (int) ((nbOfPendingMessages - producThreshold) * rateOfFlow);
        deltaCons = (int) ((nbOfPendingRequests - consumThreshold) * rateOfFlow);
      } else {
        deltaProd = (int) ((nbOfPendingMessages - producThreshold) / rateOfFlow);
        deltaCons = (int) ((nbOfPendingRequests - consumThreshold) / rateOfFlow);
      }

      if (nbOfPendingMessages > 0) {
        if (deltaProd < producThreshold)
          producThreshold = producThreshold + deltaProd;
        else
          producThreshold = deltaProd;
      }

      if (nbOfPendingRequests > 0) {
        if (deltaCons < consumThreshold)
          consumThreshold = consumThreshold + deltaCons;
        else
          consumThreshold = deltaCons;
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.updateThreshol after" +
                   " rateOfFlow=" + rateOfFlow +
                   ", producThreshold=" + producThreshold +
                   ", consumThreshold=" + consumThreshold );
    }
  }

  /**
   * eval the rate of flow (means).
   * if rateOfFlow > 1 the queue are more pending requests 
   * than pending messages.
   * else if rateOfFlow < 1 the queue are more pending messages 
   * than pending requests.
   * This value is set in all QueueClusterNot notification.
   * 
   * @param pendingMessages
   * @param pendingRequests
   * @return
   */
  public float evalRateOfFlow(int pendingMessages,
                              int pendingRequests) {
    float currentROF;
    nbOfPendingMessages = pendingMessages;
    nbOfPendingRequests = pendingRequests;

    if (pendingMessages == 0 && pendingRequests == 0) 
      currentROF = 1;
    else if (pendingMessages == 0 && pendingRequests != 0)
      currentROF = pendingRequests + 1;
    else
      currentROF = 
        new Float(pendingRequests).floatValue() /
        new Float(pendingMessages).floatValue();

    rateOfFlow = (currentROF + rateOfFlow ) / 2;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.evalRateOfFlow" +
                 " pendingMessages = " + pendingMessages +
                 ", pendingRequests = " + pendingRequests +
                 ", rateOfFlow = " + rateOfFlow + 
                 ", currentROF = " + currentROF);

    return rateOfFlow;
  }

  /**
   * this method eval the rate of flow and activity.
   * if necessary send "give or hope" messages, and
   * update threshol.
   * 
   * @param clusters
   * @param pendingMessages
   * @param pendingRequests
   */
  public void factorCheck(Hashtable clusters,
                          int pendingMessages,
                          int pendingRequests) {

    nbOfPendingMessages = pendingMessages;
    nbOfPendingRequests = pendingRequests;

    if (status == Status.WAIT && 
        statusTime < System.currentTimeMillis())
      status = Status.RUN;


    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 ">> LoadingFactor.factorCheck " +
                 this + "\nclusters = " + clusters);

    evalRateOfFlow(pendingMessages,
                   pendingRequests);

    evalActivity();

    if ( status == Status.INIT || status == Status.RUN) {
      if (isOverloaded()) {
        dispatchAndSendTo(clusters,
                          pendingMessages,
                          pendingRequests);
        status = Status.WAIT;
        statusTime = System.currentTimeMillis() + validityPeriod;
      }
    }

    updateThreshol();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "<< LoadingFactor.factorCheck " 
                 + this);
  }

  /**
   * true if cluster queue is overloaded.
   * depends on activity.
   * 
   * @return  true if cluster queue is overloaded.
   */
  public boolean isOverloaded() {
    overLoaded = false;
    if ((consumerStatus == 
      ConsumerStatus.CONSUMER_HIGH_ACTIVITY) ||
      (producerStatus == 
        ProducerStatus.PRODUCER_HIGH_ACTIVITY))
      overLoaded = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.isOverloaded " 
                 + overLoaded);
    return overLoaded;
  }

  /**
   * use to dispatch request hope or give messages
   * in clusters.
   * 
   * @param clusters
   * @param nbOfPendingMessages
   * @param nbOfPendingRequests
   */
  private void dispatchAndSendTo(Hashtable clusters,
                                 int nbOfPendingMessages,
                                 int nbOfPendingRequests) {
    int nbMsgHope = -1;
    int nbMsgGive = -1;

    if ((consumerStatus == ConsumerStatus.CONSUMER_NO_ACTIVITY) &&
        (producerStatus == ProducerStatus.PRODUCER_NO_ACTIVITY))
      return;

    if (producThreshold < nbOfPendingMessages)
      nbMsgGive = nbOfPendingMessages - producThreshold;

    if (consumThreshold < nbOfPendingRequests)
      nbMsgHope = nbOfPendingRequests;

    //      if (nbOfPendingRequests > nbOfPendingMessages)
    //        nbMsgHope = nbOfPendingRequests - nbOfPendingMessages;
    //      else
    //        nbMsgGive = nbOfPendingMessages - nbOfPendingRequests;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.dispatchAndSendTo" +
                 "\nnbMsgHope=" + nbMsgHope +
                 ", nbMsgGive=" + nbMsgGive);


    if (consumerStatus == ConsumerStatus.CONSUMER_HIGH_ACTIVITY)
      processHope(nbMsgHope,clusters);

    if (producerStatus == ProducerStatus.PRODUCER_HIGH_ACTIVITY)
      processGive(nbMsgGive,clusters);
  }

  /**
   * send nb messages on clusters.
   * 
   * @param nbMsgGive
   * @param clusters  Hashtable of cluster Queue
   */
  private void processGive(int nbMsgGive, Hashtable clusters) {
    if (nbMsgGive < 1) return;

    // select queue in cluster who have a rateOfFlow > 1
    Vector selected = new Vector();
    for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (((Float) clusters.get(id)).floatValue() >= 1 && 
          !id.equals(clusterQueueImpl.getId()))
        selected.add(id);
    }

    if (selected.size() == 0) return;

    int nbGivePerQueue = nbMsgGive / selected.size();
    LBMessageGive msgGive = new LBMessageGive(validityPeriod, rateOfFlow);

    if (nbGivePerQueue == 0 && nbMsgGive > 0) {
      // send all to the first element of clusterQueue.
      AgentId id = (AgentId) selected.get(0);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processGive" +
                   " nbMsgGive = " + nbMsgGive +
                   ", id = " + id);
      msgGive.setClientMessages(clusterQueueImpl.getClientMessages(nbMsgGive, null, true));
      clusterQueueImpl.forward(id,msgGive);
    } else {
      // dispatch to cluster.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processGive" +
                   " givePerQueue = " + nbGivePerQueue +
                   ", selected = " + selected);
      for (Enumeration e = selected.elements(); e.hasMoreElements(); ) {
        AgentId id = (AgentId) e.nextElement();
        msgGive.setClientMessages(clusterQueueImpl.getClientMessages(nbGivePerQueue, null, true));
        clusterQueueImpl.forward(id,msgGive);
      }
    }
  }

  /**
   * send a hope request on a cluster queue.
   * 
   * @param nbMsgHope
   * @param clusters   Hashtable of cluster Queue
   */
  private void processHope(int nbMsgHope, Hashtable clusters) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.processHope" +
                 " nbMsgHope = " + nbMsgHope);
    if (nbMsgHope < 1) return;

    Vector selected = new Vector();
    for (Enumeration e = clusters.keys(); e.hasMoreElements(); ) {
      AgentId id = (AgentId) e.nextElement();
      if (((Float) clusters.get(id)).floatValue() <= 1 && 
          !id.equals(clusterQueueImpl.getId()))
        selected.add(id);
    }

    if (selected.size() == 0) return;

    int nbHopePerQueue = nbMsgHope / selected.size();
    if (nbHopePerQueue == 0 && nbMsgHope > 0) {
      // send the hope request to the first element of clusterQueue.
      AgentId id = (AgentId) selected.get(0);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processHope" +
                   " nbMsgHope = " + nbMsgHope +
                   ", id = " + id);
      LBMessageHope msgHope = new LBMessageHope(validityPeriod,rateOfFlow);
      msgHope.setNbMsg(nbMsgHope);
      clusterQueueImpl.forward(id,msgHope);

    } else {
      // dispatch the hope request to clusterQueue.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processHope" +
                   " hopePerQueue = " + nbHopePerQueue +
                   ", selected = " + selected);

      LBMessageHope msgHope = new LBMessageHope(validityPeriod,rateOfFlow);
      for (Enumeration e = selected.elements(); e.hasMoreElements(); ) {
        AgentId id = (AgentId) e.nextElement();
        msgHope.setNbMsg(nbHopePerQueue);
        clusterQueueImpl.forward(id,msgHope);
      }
    }
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("LoadingFactor (status=");
    str.append(Status.names[status]);
    str.append(", consumerStatus=");
    str.append(ConsumerStatus.names[consumerStatus]);
    str.append(", producerStatus=");
    str.append(ProducerStatus.names[producerStatus]);
    str.append(", producThreshold=");
    str.append(producThreshold);
    str.append(", consumThreshold=");
    str.append(consumThreshold);
    str.append(", autoEvalThreshold=");
    str.append(autoEvalThreshold);
    str.append(", nbOfPendingMessages=");
    str.append(nbOfPendingMessages);
    str.append(", nbOfPendingRequests=");
    str.append(nbOfPendingRequests);
    str.append(", rateOfFlow=");
    str.append(rateOfFlow);
    str.append(", overLoaded=");
    str.append(overLoaded);
    str.append(")");
    return str.toString();
  }
}
