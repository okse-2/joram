/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;

/**
 * A <code>Topic</code> agent is an agent behaving as a MOM topic.
 * <p>
 * Its behaviour is provided by a <code>TopicImpl</code> instance.
 *
 * @see TopicImpl
 */
public class Topic extends Agent
{
  /**
   * The reference to the <code>TopicImpl</code> object providing this
   * agent with its behaviour.
   */
  private TopicImpl topicImpl;

  /**
   * Constructs a <code>Topic</code> agent. 
   *
   * @param creator  The identifier of the agent creating the topic, and which
   *          will be its original admin.
   */ 
  public Topic(AgentId creator) 
  {
    topicImpl = new TopicImpl(this.getId(), creator);
  }


  /**
   * Overrides the <code>Agent.react(...)</code> method for providing
   * topic agents with their specific behaviour.
   * <p>
   * Topic agents accept:
   * <ul>
   * <li><code>AbstractRequest</code> MOM requests,</li>
   * <li><code>ExceptionReply</code> MOM reply,</li>
   * <li><code>fr.dyade.aaa.agent.UnknownAgent</code> notifications,</li>
   * <li><code>fr.dyade.aaa.agent.DeleteNot</code> notifications.</li>
   * </ul>
   * <p>
   * Reactions to these notifications are implemented in the
   * <code>TopicImpl</code> class.
   *
   * @exception Exception  Thrown at super class level.
   */
  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof AbstractRequest)
      topicImpl.doReact(from, (AbstractRequest) not);
    else if (not instanceof ExceptionReply)
      topicImpl.removeTopic(from);
    else if (not instanceof UnknownAgent)
      topicImpl.removeDeadClient((UnknownAgent) not);
    else if (not instanceof DeleteNot) {
      topicImpl.delete(from);
      if (topicImpl.canBeDeleted())
        super.react(from, not);
    }
  }
}
