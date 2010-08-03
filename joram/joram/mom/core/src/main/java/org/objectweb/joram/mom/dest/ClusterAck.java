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

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>ClusterAck</code> instance is a notification sent by a topic
 * requested to join a cluster.
 */
class ClusterAck extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** <code>true</code> if the topic can join the cluster. */
  boolean ok;
  /** Info. */
  String info;
  /** The JMS destination to send the reply */
  private AgentId replyTo = null;
  /** The JMS message id. of the request needed for the reply */
  private String requestMsgId;
  /** The generated JMS message id. for the reply */
  private String replyMsgId;

  /**
   * Constructs a <code>ClusterAck</code> instance.
   *
   * @param ok  <code>true</code> if the topic can join the cluster.
   * @param info  Related info.
   * @param replyTo       The JMS destination to send the reply.
   * @param requestMsgId  The JMS message id. of the request needed for the reply.
   * @param replyMsgId    The generated JMS message id. for the reply.
   */
  ClusterAck(boolean ok, String info,
             AgentId replyTo, String requestMsgId, String replyMsgId) {
    this.ok = ok;
    this.info = info;
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
