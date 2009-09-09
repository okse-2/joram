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
 * Initial developer(s): ScalAgent DT
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.shared.admin.AdminRequest;

import fr.dyade.aaa.agent.*;

public class DestinationAdminRequestNot 
    extends fr.dyade.aaa.agent.Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AdminRequest request;

  private AgentId replyTo;

  private String requestMsgId;

  private String replyMsgId;

  public DestinationAdminRequestNot(
    AdminRequest request,
    AgentId replyTo,
    String requestMsgId,
    String replyMsgId) {
    this.request = request;
    this.replyTo = replyTo;
    this.requestMsgId = requestMsgId;
    this.replyMsgId = replyMsgId;
  }

  public final AdminRequest getRequest() {
    return request;
  }

  public final AgentId getReplyTo() {
    return replyTo;
  }

  public final String getRequestMsgId() {
    return requestMsgId;
  }

  public final String getReplyMsgId() {
    return replyMsgId;
  }
}
