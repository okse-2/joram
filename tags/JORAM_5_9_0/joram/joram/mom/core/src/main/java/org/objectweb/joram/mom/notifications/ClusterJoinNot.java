/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
package org.objectweb.joram.mom.notifications;

import java.util.Set;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

public class ClusterJoinNot extends Notification {
  
  private static final long serialVersionUID = 1L;

  private Set cluster;
  
  /** The JMS destination to send the reply */
  private AgentId replyTo = null;

  /** The JMS message id. of the request needed for the reply */
  private String requestMsgId;

  /** The generated JMS message id. for the reply */
  private String replyMsgId;

  public ClusterJoinNot(Set cluster, AgentId replyTo, String requestMsgId, String replyMsgId) {
    this.cluster = cluster;
    this.replyTo = replyTo;
    this.requestMsgId = requestMsgId;
    this.replyMsgId = replyMsgId;
  }

  /**
   * @return the cluster
   */
  public Set getCluster() {
    return cluster;
  }

  /**
   * Returns the JMS destination to send the reply.
   * 
   * @return The JMS destination to send the reply.
   */
  public AgentId getReplyTo() {
    return replyTo;
  }

  /**
   * Returns the JMS message id. of the request needed for the reply.
   * 
   * @return The JMS message id. of the request needed for the reply.
   */
  public String getRequestMsgId() {
    return requestMsgId;
  }

  /**
   * Returns the generated JMS message id. for the reply.
   * 
   * @return The generated JMS message id. for the reply.
   */
  public String getReplyMsgId() {
    return replyMsgId;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   * 
   * @param output
   *          buffer to fill in
   * @return
   *         <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",cluster=").append(cluster);
    output.append(')');

    return output;
  }
}
