/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.shared.admin.AdminRequest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

/**
 * An <code>AdminRequest</code> is used by a client agent for sending an
 * administration request to a destination agent.
 */
public final class FwdAdminRequestNot extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The request */
  private AdminRequest request = null;
  /** The JMS destination to send the reply */
  private AgentId replyTo = null;
  /** The JMS message id. of the request needed for the reply */
  private String requestMsgId;
  /** The generated JMS message id. for the reply */
  private String replyMsgId;

  /**
   *  Creates a new <code>FwdAdminRequestNot</code> to send to an administered
   * target (user or destination).
   * 
   * @param request       The request.
   * @param replyTo       The JMS destination to send the reply.
   * @param requestMsgId  The JMS message id. of the request needed for the reply.
   * @param replyMsgId    The generated JMS message id. for the reply.
   */
  public FwdAdminRequestNot(AdminRequest request,
                            AgentId replyTo,
                            String requestMsgId,
                            String replyMsgId) {
    this.request = request;
    this.replyTo = replyTo;
    this.requestMsgId = requestMsgId;
    this.replyMsgId = replyMsgId;
  }

  /**
   *  Creates a new <code>FwdAdminRequestNot</code> to send to a remote AdminTopic.
   * 
   * @param request       The request.
   * @param replyTo       The JMS destination to send the reply.
   * @param requestMsgId  The JMS message id. of the request needed for the reply.
   */
  public FwdAdminRequestNot(AdminRequest request,
                            AgentId replyTo,
                            String requestMsgId) {
    this.request = request;
    this.replyTo = replyTo;
    this.requestMsgId = requestMsgId;
    this.replyMsgId = null;
  }
  
  /**
   * Returns the original request.
   * 
   * @return  The original request.
   */
  public AdminRequest getRequest() {
    return request;
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
