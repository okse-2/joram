/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.dest.LoadingFactor.ConsumerStatus;
import org.objectweb.joram.mom.dest.LoadingFactor.ProducerStatus;
import org.objectweb.joram.mom.dest.LoadingFactor.Status;

public interface ClusterQueueMBean extends QueueMBean, ClusterDestinationMBean {

  /**
   * Gets the number of messages above which a queue is considered loaded.
   * 
   * @return the produce threshold
   */
  public int getProducThreshold();

  /**
   * Sets the number of messages above which a queue is considered loaded.
   * 
   * @param producThreshold the new threshold
   */
  public void setProducThreshold(int producThreshold);

  /**
   * Gets the number of pending "receive" requests above which a queue will
   * request messages from the other queues of the cluster.
   * 
   * @return the consume threshold
   */
  public int getConsumThreshold();

  /**
   * Sets the number of pending "receive" requests above which a queue will
   * request messages from the other queues of the cluster.
   * 
   * @param consumThreshold the new threshold
   */
  public void setConsumThreshold(int consumThreshold);

  /**
   * True if an automatic reevaluation of the queues' thresholds values is
   * allowed according to their activity.
   * 
   * @return true if auto evaluation of thresholds is allowed.
   */
  public boolean isAutoEvalThreshold();

  /**
   * Automatic reevaluation of the queues' thresholds can be done according to
   * their activity.
   * 
   * @param autoEvalThreshold true to enable auto evaluation of thresholds
   */
  public void setAutoEvalThreshold(boolean autoEvalThreshold);

  /**
   * Gets the time (in ms) during which a queue which requested something from
   * the cluster is not authorized to do it again.
   * 
   * @return the minimum time to wait before another cluster request.
   */
  public long getWaitAfterClusterReq();

  /**
   * Sets the time (in ms) during which a queue which requested something from
   * the cluster is not authorized to do it again.
   * 
   * @param waitAfterClusterReq the minimum time to wait before another cluster
   *          request.
   */
  public void setWaitAfterClusterReq(long waitAfterClusterReq);

  /**
   * Gets an evaluation of the flow of messages handled by the queue.
   * 
   * @return the rate of flow
   */
  public float getRateOfFlow();

  /**
   * Tells if the queue is overloaded.
   * 
   * @return true if the queue is overloaded
   */
  public boolean isOverloaded();

  /**
   * Gets the status of the queue (RUN, INIT or WAIT).
   * 
   * @return the status of the queue
   * @see Status
   */
  public String getStatus();

  /**
   * Gets consumer status (NO, NORMAL, HIGH).
   * 
   * @return consumer status
   * @see ConsumerStatus
   */
  public String getConsumerStatus();

  /**
   * Gets producer status (NO, NORMAL, HIGH).
   * 
   * @return producer status
   * @see ProducerStatus
   */
  public String getProducerStatus();

}
