/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.SubscribeRequest;
import org.objectweb.joram.mom.notifications.UnsubscribeRequest;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>Topic</code> agent is an agent hosting a MOM topic, and which
 * behavior is provided by a <code>TopicImpl</code> instance.
 *
 * @see TopicImpl
 */
public class Topic extends Destination {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public final byte getType() {
    return DestinationConstants.TOPIC_TYPE;
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
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) throws RequestException {
    return new TopicImpl(adminId, prop);
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    ((TopicImpl)destImpl).setAlreadySentLocally(false);
    int reqId = -1;
    if (not instanceof AbstractRequestNot)
      reqId = ((AbstractRequestNot) not).getRequestId();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + ": got " + not.getClass().getName()+ " with id: " + reqId + " from: " + from.toString());
    try {
      if (not instanceof ClusterTest)
        ((TopicImpl)destImpl).clusterTest(from, (ClusterTest) not);
      else if (not instanceof ClusterAck)
        ((TopicImpl)destImpl).clusterAck(from, (ClusterAck) not);
      else if (not instanceof ClusterNot)
        ((TopicImpl)destImpl).clusterNot(from, (ClusterNot) not);
      else if (not instanceof SubscribeRequest)
        ((TopicImpl)destImpl).subscribeRequest(from, (SubscribeRequest) not);
      else if (not instanceof UnsubscribeRequest)
        ((TopicImpl)destImpl).unsubscribeRequest(from);
      else if (not instanceof TopicForwardNot)
        ((TopicImpl)destImpl).topicForwardNot((TopicForwardNot) not);
      else
        super.react(from, not);
    } catch (MomException exc) {
      // MOM exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      AbstractRequestNot req = (AbstractRequestNot) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    }
  }

}
