/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.collector;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.notifications.AbstractRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>CollectorTopic</code> agent is an agent hosting a MOM topic.
 */
public class CollectorTopic extends Topic {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(CollectorTopic.class.getName());

  public static final String COLLECTOR_TOPIC_TYPE = "topic.collector";

  public static String getDestinationType() {
    return COLLECTOR_TOPIC_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public CollectorTopic() {}

  /**
   * Creates the <tt>CollectorTopicImpl</tt>.
   *
   * @param adminId  Identifier of the topic administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new CollectorTopicImpl(adminId, prop);
  }
  
  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime   true when first called by the factory
   *
   * @exception Exception
   *  unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopic.react(" + from + ',' + not + ')');

    try {     
      if (not instanceof CollectorWakeUpNot) {
        ((CollectorTopicImpl)destImpl).collectorWakeUp();
      } else
        super.react(from, not);

    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      if (not instanceof AbstractRequest) {
        AbstractRequest req = (AbstractRequest) not;
        Channel.sendTo(from, new ExceptionReply(req, exc));
      }
    }
  }
}
