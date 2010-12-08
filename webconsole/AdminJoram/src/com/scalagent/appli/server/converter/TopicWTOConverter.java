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

import java.util.Collection;
import java.util.Date;

import org.objectweb.joram.mom.dest.TopicMBean;

import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public class TopicWTOConverter {

  /**
   * @param topic A TopicMBean containing the topic info
   * @return A TopicWTO object created from the TopicMBean object
   */
  public static TopicWTO getDeviceWTO(TopicMBean topic) {
    TopicWTO result = new TopicWTO(topic.getName(), new Date(topic.getCreationTimeInMillis()),
        topic.getSubscriberIds(), topic.getDMQId(), topic.getDestinationId(),
        topic.getNbMsgsDeliverSinceCreation(), topic.getNbMsgsReceiveSinceCreation(),
        topic.getNbMsgsSentToDMQSinceCreation(), topic.getPeriod(), topic.getRights(), topic.isFreeReading(),
        topic.isFreeWriting());
    return result;
  }

  /**
   * @param topics A colelction of TopicMBean
   * @return An Array of TopicWTO
   */
  public static TopicWTO[] getTopicWTOArray(Collection<TopicMBean> topics) {

    TopicWTO[] newTopicsWTO = new TopicWTO[topics.size()];

    int i = 0;
    for (TopicMBean topic : topics) {
      newTopicsWTO[i] = TopicWTOConverter.getDeviceWTO(topic);
      i++;
    }

    return newTopicsWTO;
  }

}