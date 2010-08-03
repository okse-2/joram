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
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest;

import java.util.Set;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>ClusterTest</code> instance is a notification sent by a topic 
 * to another topic for checking if it might be part of a cluster.
 */
class ClusterTest extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Set containing AgentId of topics already in the cluster */
  Set friends;
  /** The JMS destination to send the reply */
  private AgentId replyTo = null;
  /** The JMS message id. of the request needed for the reply */
  private String requestMsgId;
  /** The generated JMS message id. for the reply */
  private String replyMsgId;

  /**
   * Constructs a <code>ClusterTest</code> instance.
   *
   * @param requester     The original requester.
   * @param friends       The current members of the cluster.
   * @param replyTo       The JMS destination to send the reply.
   * @param requestMsgId  The JMS message id. of the request needed for the reply.
   * @param replyMsgId    The generated JMS message id. for the reply.
   */
  ClusterTest(Set friends,
              AgentId replyTo, String requestMsgId, String replyMsgId) {
    this.friends = friends;
    this.replyTo = replyTo;
    this.requestMsgId = requestMsgId;
    this.replyMsgId = replyMsgId;
  }

  /**
   * Returns the JMS destination to send the reply.
   * 
   * @return  The JMS destination to send the reply.
   */
  public AgentId getReplyTo() {
    return replyTo;
  }

  /**
   * Returns the JMS message id. of the request needed for the reply.
   * 
   * @return  The JMS message id. of the request needed for the reply.
   */
  public String getRequestMsgId() {
    return requestMsgId;
  }

  /**
   * Returns the generated JMS message id. for the reply.
   * 
   * @return  The generated JMS message id. for the reply.
   */
  public String getReplyMsgId() {
    return replyMsgId;
  }
}
