/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Contributor(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.shared.admin.*;

import java.net.ConnectException;


/**
 * The <code>AdminHelper</code> class is a utility class providing methods
 * for building special configurations such as topics cluster or hierarchy,
 * queues cluster, etc.
 * 
 * @deprecated
 */
public class AdminHelper {
  /**
   * Links two given topics in a cluster relationship.
   * <p>
   * The request fails if one or both of the topics are deleted, or
   * can't belong to a cluster.
   *
   * @param clusterTopic  Topic part of the cluster, or chosen as the 
   *          initiator of the cluster.
   * @param joiningTopic  Topic joining the cluster.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void setClusterLink(Topic clusterTopic,
                                    Topic joiningTopic) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetCluster(clusterTopic.getName(), joiningTopic.getName()));
  }

  /**
   * Removes a topic from the cluster it is part of.
   * <p>
   * The request fails if the topic does not exist or is not part of any 
   * cluster.
   *
   * @param topic  Topic leaving the cluster it is part of.
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void unsetClusterLink(Topic topic) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetCluster(topic.getName()));
  }

  /**
   * Links two given topics in a hierarchical relationship.
   * <p>
   * The request fails if one of the topics does not exist or can't be part
   * of a hierarchy.
   *
   * @param father  Father.
   * @param son  Son.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void setHierarchicalLink(Topic father,
                                         Topic son) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetFather(father.getName(), son.getName()));
  }

  /**
   * Unsets the father of a given topic.
   * <p>
   * The request fails if the topic does not exist or is not part of any
   * hierarchy.
   *
   * @param topic  Topic which father is unset.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void unsetHierarchicalLink(Topic topic) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetFather(topic.getName()));
  }

  /**
   * Adds a queue to a cluster.
   * <p>
   * The request fails if one or both of the queues are deleted, or
   * can't belong to a cluster.
   *
   * @param clusterQueue  Queue part of the cluster, or chosen as the 
   *          initiator of the cluster.
   * @param joiningQueue  Queue joining the cluster.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void setQueueCluster(Queue clusterQueue,
                                     Queue joiningQueue) throws ConnectException, AdminException {
    AdminModule.doRequest(new AddQueueCluster(clusterQueue.getName(), joiningQueue.getName()));
  }

  /**
   * Adds a queue to a cluster.
   * <p>
   * The request fails if one or both of the queues are deleted, or
   * can't belong to a cluster.
   *
   * @param clusterQueue  Queue part of the cluster, or chosen as the 
   *          initiator of the cluster.
   * @param joiningQueue  Queue joining the cluster.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void setQueueCluster(Destination clusterQueue,
                                     Queue joiningQueue) throws ConnectException, AdminException {
    AdminModule.doRequest(new AddQueueCluster(clusterQueue.getName(), joiningQueue.getName()));
  }

  /**
   * Removes a queue from the cluster Queue it is part of.
   * <p>
   * The request fails if the queue does not exist or is not part of any 
   * cluster.
   *
   * @param clusterQueue  the cluster Queue.
   * @param leaveQueue    Queue leaving the cluster Queue it is part of.
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static void leaveQueueCluster(Queue clusterQueue, Queue leaveQueue) throws ConnectException, AdminException {
    AdminModule.doRequest(new RemoveQueueCluster(clusterQueue.getName(), leaveQueue.getName()));
  }

  /**
   * List a cluster queue.
   *
   * @param clusterQueue  the cluster Queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * @deprecated
   */
  public static AdminReply listQueueCluster(Queue clusterQueue) throws ConnectException, AdminException {
    return AdminModule.doRequest( new ListClusterQueue(clusterQueue.getName()));
  }
} 
