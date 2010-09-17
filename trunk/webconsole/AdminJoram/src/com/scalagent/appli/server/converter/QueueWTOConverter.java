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
package com.scalagent.appli.server.converter;

import java.util.Date;
import java.util.Map;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.QueueImplMBean;

import com.scalagent.appli.shared.QueueWTO;

/**
 * @author Yohann CINTRE
 */
public class QueueWTOConverter {

  /**
   * @param key
   *          The ID of the queue
   * @param queue
   *          A DestinationImplMBean containing the queue info
   * @return A QueueWTO object created from the DestinationImplMBean object
   */
  public static QueueWTO getQueueWTO(String key, DestinationImplMBean queue) {
    QueueWTO result = new QueueWTO(key, new Date(queue.getCreationTimeInMillis()), queue.getDMQId(),
        queue.getDestinationId(), queue.getNbMsgsDeliverSinceCreation(),
        queue.getNbMsgsReceiveSinceCreation(), queue.getNbMsgsSentToDMQSinceCreation(), queue.getPeriod(),
        queue.getRights(), queue.isFreeReading(), queue.isFreeWriting(),
        ((QueueImplMBean) queue).getThreshold(), ((QueueImplMBean) queue).getWaitingRequestCount(),
        ((QueueImplMBean) queue).getPendingMessageCount(),
        ((QueueImplMBean) queue).getDeliveredMessageCount(), ((QueueImplMBean) queue).getNbMaxMsg());
    return result;
  }

  /**
   * @param map
   *          Map of DestinationImplMBean
   * @return An Array of QueueWTO
   */
  public static QueueWTO[] getQueueWTOArray(Map<String, DestinationImplMBean> map) {

    int nbQueue = 0;
    for (String mapKey : map.keySet()) {

      if (map.get(mapKey) instanceof QueueImplMBean) {
        nbQueue++;
      }
    }

    QueueWTO[] newQueuesWTO = new QueueWTO[nbQueue];

    int i = 0;
    for (String mapKey : map.keySet()) {
      if (map.get(mapKey) instanceof QueueImplMBean) {
        newQueuesWTO[i] = QueueWTOConverter.getQueueWTO(mapKey, map.get(mapKey));
        i++;
      }
    }

    return newQueuesWTO;
  }

}