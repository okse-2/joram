/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.mail;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Topic;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.WakeUpTask;

/**
 * A <code>JavaMailTopic</code> agent is an agent hosting a MOM queue, and
 * which behaviour is provided by a <code>JavaMailTopicImpl</code> instance.
 *
 * @see JavaMailTopicImpl
 */
public class JavaMailTopic extends Topic {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final String MAIL_TOPIC_TYPE = "topic.mail";

  public static String getDestinationType() {
    return MAIL_TOPIC_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public JavaMailTopic() {}

  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    JavaMailTopicImpl topicImpl = new JavaMailTopicImpl(adminId, prop);
    return topicImpl;
  }

  private transient WakeUpTask poptask;

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    poptask = new WakeUpTask(getId(), WakeUpPopNot.class);
    poptask.schedule(((JavaMailTopicImpl) destImpl).getPopPeriod());
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof WakeUpPopNot) {
      if (poptask == null)
        poptask = new WakeUpTask(getId(), WakeUpPopNot.class);
      poptask.schedule(((JavaMailTopicImpl) destImpl).getPopPeriod());
      ((JavaMailTopicImpl) destImpl).doPop();
    } else {
      super.react(from, not);
    }
  }
}

