/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import org.objectweb.joram.mom.notifications.AbstractRequest;
import org.objectweb.joram.mom.notifications.ClusterRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_GetCluster;
import org.objectweb.joram.mom.notifications.Monit_GetFather;
import org.objectweb.joram.mom.notifications.Monit_GetSubscriptions;
import org.objectweb.joram.mom.notifications.SetFatherRequest;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.UnclusterRequest;
import org.objectweb.joram.mom.notifications.UnsetFatherRequest;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>Topic</code> agent is an agent hosting a MOM topic, and which
 * behaviour is provided by a <code>TopicImpl</code> instance.
 *
 * @see TopicImpl
 */
public class Topic extends Destination {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static final String TOPIC_TYPE = "topic";
  
  public static String getDestinationType() {
    return TOPIC_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public Topic() {}

  /**
   *  Constructor with parameters for fixing the topic and specifying its
   * identifier.
   *  It is uniquely used by the AdminTopic agent.
   */
  protected Topic(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /**
   * Creates the <tt>TopicImpl</tt>.
   *
   * @param adminId  Identifier of the topic administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new TopicImpl(adminId, prop);
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    ((TopicImpl)destImpl).setAlreadySentLocally(false);
    int reqId = -1;
    if (not instanceof AbstractRequest)
      reqId = ((AbstractRequest) not).getRequestId();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + ": got " + not.getClass().getName()+ " with id: " + reqId + " from: " + from.toString());
    try {
      if (not instanceof ClusterRequest)
        ((TopicImpl)destImpl).clusterRequest(from, (ClusterRequest) not);
      else if (not instanceof ClusterTest)
        ((TopicImpl)destImpl).clusterTest(from, (ClusterTest) not);
      else if (not instanceof ClusterAck)
        ((TopicImpl)destImpl).clusterAck(from, (ClusterAck) not);
      else if (not instanceof ClusterNot)
        ((TopicImpl)destImpl).clusterNot(from, (ClusterNot) not);
      else if (not instanceof UnclusterRequest)
        ((TopicImpl)destImpl).unclusterRequest(from, (UnclusterRequest) not);
      else if (not instanceof UnclusterNot)
        ((TopicImpl)destImpl).unclusterNot(from, (UnclusterNot) not);
      else if (not instanceof SetFatherRequest)
        ((TopicImpl)destImpl).setFatherRequest(from, (SetFatherRequest) not);
      else if (not instanceof FatherTest)
        ((TopicImpl)destImpl).fatherTest(from, (FatherTest) not);
      else if (not instanceof FatherAck)
        ((TopicImpl)destImpl).fatherAck(from, (FatherAck) not);
      else if (not instanceof UnsetFatherRequest)
        ((TopicImpl)destImpl).unsetFatherRequest(from, (UnsetFatherRequest) not);
      else if (not instanceof Monit_GetSubscriptions)
        ((TopicImpl)destImpl).monitGetSubscriptions(from, (Monit_GetSubscriptions) not);
      else if (not instanceof Monit_GetFather)
        ((TopicImpl)destImpl).monitGetFather(from, (Monit_GetFather) not);
      else if (not instanceof Monit_GetCluster)
        ((TopicImpl)destImpl).monitGetCluster(from, (Monit_GetCluster) not);
      else if (not instanceof SubscribeRequest)
        ((TopicImpl)destImpl).subscribeRequest(from, (SubscribeRequest) not);
      else if (not instanceof UnsubscribeRequest)
        ((TopicImpl)destImpl).unsubscribeRequest(from, (UnsubscribeRequest) not);
      else if (not instanceof TopicForwardNot)
        ((TopicImpl)destImpl).topicForwardNot(from, (TopicForwardNot) not);
//      else if (not instanceof DestinationAdminRequestNot)
//        ((TopicImpl)destImpl).destinationAdminRequestNot(from, (DestinationAdminRequestNot) not);
      else
        super.react(from, not);
    } catch (MomException exc) {
      // MOM exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      AbstractRequest req = (AbstractRequest) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    }
  }

}
