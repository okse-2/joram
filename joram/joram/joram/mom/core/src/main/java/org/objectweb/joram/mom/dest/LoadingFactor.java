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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.joram.mom.notifications.LBMessageGive;
import org.objectweb.joram.mom.notifications.LBMessageHope;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.Debug;

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

  /** reference to clusterQueue */
  public ClusterQueue clusterQueue;

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

  public LoadingFactor(ClusterQueue clusterQueue,
                       int producThreshold,
                       int consumThreshold,
                       boolean autoEvalThreshold,
                       long validityPeriod) {
    this.clusterQueue = clusterQueue;
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

  public String getStatus() {
    return Status.names[status];
  }

  public String getProducerStatus() {
    return ProducerStatus.names[producerStatus];
  }

  public String getConsumerStatus() {
    return ConsumerStatus.names[consumerStatus];
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
   * update the threshold if autoEvalThreshold is true.
   */
  private void updateThreshold() {
    if (autoEvalThreshold) {

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.updateThreshold before" +
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
   * Evaluates the average rate of flow.
   * If rateOfFlow is greater than 1 the queue are more pending requests 
   * than pending messages else if rateOfFlow is lower than 1 the queue are
   * more pending messages than pending requests.
   * This value is set in all QueueClusterNot notification.
   * 
   * @param pendingMessages   the number of pending messages.
   * @param pendingRequests   the number of pending requests.
   * @return the rate of flow
   */
  public float evalRateOfFlow(int pendingMessages,
                              int pendingRequests) {
    float currentROF;
    nbOfPendingMessages = pendingMessages;
    nbOfPendingRequests = pendingRequests;

    // TODO (AF): Be careful this evaluation is not really useful as either pendingMessages
    // or pendingRequests is equal to zero!
    
    if (pendingMessages == 0 && pendingRequests == 0) 
      currentROF = 1;
    else if (pendingMessages == 0 && pendingRequests != 0)
      currentROF = pendingRequests + 1;
    else
      currentROF = (float) pendingRequests / (float) pendingMessages;

    rateOfFlow = (currentROF + rateOfFlow ) / 2;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.evalRateOfFlow" +
                 " pendingMessages = " + pendingMessages + ", pendingRequests = " + pendingRequests +
                 ", rateOfFlow = " + rateOfFlow + ", currentROF = " + currentROF);

    return rateOfFlow;
  }

  /**
   * This method evaluates the rate of flow and activity.
   * If necessary send "give" or "hope" messages, and update threshold.
   * 
   * @param clusters
   * @param pendingMessages
   * @param pendingRequests
   */
  public void factorCheck(Map clusters,
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

    evalRateOfFlow(pendingMessages, pendingRequests);

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

    updateThreshold();

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
  private void dispatchAndSendTo(Map clusters,
                                 int nbOfPendingMessages,
                                 int nbOfPendingRequests) {
    int nbMsgHope = -1;
    int nbMsgGive = -1;

    if ((consumerStatus == ConsumerStatus.CONSUMER_NO_ACTIVITY) &&
        (producerStatus == ProducerStatus.PRODUCER_NO_ACTIVITY))
      return;

    if (producThreshold < nbOfPendingMessages) {
      nbMsgGive = nbOfPendingMessages - producThreshold;
      if (nbOfPendingRequests < 1)
        nbMsgGive = nbOfPendingMessages;
    }
    
    if (consumThreshold < nbOfPendingRequests)
      nbMsgHope = 10 * nbOfPendingRequests;

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
   * @param clusters Map of cluster Queue
   */
  private void processGive(int nbMsgGive, Map clusters) {
    if (nbMsgGive < 1) return;

    // select queue in cluster who have a rateOfFlow > 1
    List selected = new ArrayList();
    for (Iterator e = clusters.keySet().iterator(); e.hasNext();) {
      AgentId id = (AgentId) e.next();
      if (((Float) clusters.get(id)).floatValue() >= 1 && !id.equals(clusterQueue.getId()))
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
      msgGive.setClientMessages(clusterQueue.getClientMessages(nbMsgGive, null, true));
      clusterQueue.forward(id, msgGive);
    } else {
      // dispatch to cluster.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processGive" +
                   " givePerQueue = " + nbGivePerQueue +
                   ", selected = " + selected);
      for (Iterator e = selected.iterator(); e.hasNext();) {
        AgentId id = (AgentId) e.next();
        msgGive.setClientMessages(clusterQueue.getClientMessages(nbGivePerQueue, null, true));
        clusterQueue.forward(id, msgGive);
      }
    }
  }

  /**
   * send a hope request on a cluster queue.
   * 
   * @param nbMsgHope
   * @param clusters Map of cluster Queue
   */
  private void processHope(int nbMsgHope, Map clusters) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "LoadingFactor.processHope" +
                 " nbMsgHope = " + nbMsgHope);
    if (nbMsgHope < 1) return;

    List selected = new ArrayList();
    for (Iterator e = clusters.keySet().iterator(); e.hasNext();) {
      AgentId id = (AgentId) e.next();
      if (((Float) clusters.get(id)).floatValue() <= 1 && !id.equals(clusterQueue.getId()))
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
      clusterQueue.forward(id, msgHope);

    } else {
      // dispatch the hope request to clusterQueue.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "LoadingFactor.processHope" +
                   " hopePerQueue = " + nbHopePerQueue +
                   ", selected = " + selected);

      LBMessageHope msgHope = new LBMessageHope(validityPeriod,rateOfFlow);
      for (Iterator e = selected.iterator(); e.hasNext();) {
        AgentId id = (AgentId) e.next();
        msgHope.setNbMsg(nbHopePerQueue);
        clusterQueue.forward(id, msgHope);
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
