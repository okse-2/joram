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

/**
 * A <code>Topic</code> agent is an agent which behaviour is provided
 * by a <code>TopicImpl</code> instance.
 *
 * @see TopicImpl
 */
public class Topic extends Agent
{
  /**
   * The reference to the <code>TopicImpl</code> object providing this
   * agent with its behaviour.
   */
  protected TopicImpl topicImpl;

  /**
   * Constructs a <code>Topic</code> agent. 
   * 
   * @param adminId  Identifier of the agent which will be the administrator
   *          of the topic.
   */ 
  public Topic(AgentId adminId) 
  {
    topicImpl = new TopicImpl(this.getId(), adminId);
  }

  /**
   * Empty constructor used by subclasses.
   *
   * @param fixed  <code>true</code> to pine agent in memory.
   */
  protected Topic(boolean fixed)
  {
    super(fixed);
  }


  /**
   * Reactions to notifications are implemented in the
   * <code>TopicImpl</code> class.
   * <p>
   * A <code>DeleteNot</code> notification is finally processed at the
   * <code>Agent</code> level when its processing went successful in
   * the <code>DestinationImpl</code> instance.
   *
   * @exception Exception  See superclass.
   */
  public void react(AgentId from, Notification not) throws Exception
  {
    try {
      topicImpl.react(from, not);

      if (not instanceof DeleteNot && topicImpl.canBeDeleted()) 
        super.react(from, not);
    }
    catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }
}
