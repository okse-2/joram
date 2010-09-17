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
import org.objectweb.joram.mom.dest.TopicImplMBean;

import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public class TopicWTOConverter {

  /**
   * @param key
   *          The ID of the queue
   * @param queue
   *          A DestinationImplMBean containing the queue info
   * @return A QueueWTO object created from the DestinationImplMBean object
   */

  /**
   * @param key The ID of the topic
   * @param topic A DestinationImplMBean containing the topic info
   * @return A TopicWTO object created from the DestinationImplMBean object
   */
  public static TopicWTO getDeviceWTO(String key, DestinationImplMBean topic) {
    TopicWTO result = new TopicWTO(key, new Date(topic.getCreationTimeInMillis()),
        ((TopicImplMBean) topic).getSubscriberIds(), topic.getDMQId(), topic.getDestinationId(),
        topic.getNbMsgsDeliverSinceCreation(), topic.getNbMsgsReceiveSinceCreation(),
        topic.getNbMsgsSentToDMQSinceCreation(), topic.getPeriod(), topic.getRights(), topic.isFreeReading(),
        topic.isFreeWriting());
    return result;
  }

  /**
   * @param map Map of DestinationImplMBean
   * @return An Array of TopicWTO
   */
  public static TopicWTO[] getTopicWTOArray(Map<String, DestinationImplMBean> map) {

    int nbTopic = 0;
    for (String mapKey : map.keySet()) {

      if (map.get(mapKey) instanceof TopicImplMBean) {
        nbTopic++;
      }
    }

    TopicWTO[] newTopicsWTO = new TopicWTO[nbTopic];

    int i = 0;
    for (String mapKey : map.keySet()) {

      if (map.get(mapKey) instanceof TopicImplMBean) {
        newTopicsWTO[i] = TopicWTOConverter.getDeviceWTO(mapKey, map.get(mapKey));
        i++;
      }
    }

    return newTopicsWTO;
  }

}