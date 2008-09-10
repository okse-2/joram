/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.jms.GetAdminTopicRequest;
import com.scalagent.kjoram.jms.GetAdminTopicReply;

import java.util.Vector;
import java.util.Hashtable;
 
import com.scalagent.kjoram.excepts.JMSException;


public class Topic extends Destination
{
  /**
   * Constructs a topic.
   *
   * @param agentId  Identifier of the topic agent.
   */
  public Topic(String agentId)
  {
    super(agentId);
  }

  /**
   * Constructs an empty topic.
   */
  public Topic()
  {}

  /** Returns a String image of the topic. */
  public String toString()
  {
    return "Topic:" + agentId;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getTopicName() throws JMSException
  {
    return agentId;
  }

  public Hashtable code() {
     return super.code();
  }

  public static Object decode(Hashtable h) {
    Topic ret = 
      new Topic((String) h.get("agentId"));
    //ret.setId(ret.getClass().getName() + ":" + agentId);
    ret.addInstanceTable(ret.getId(), ret);
    return ret;
  }
}
