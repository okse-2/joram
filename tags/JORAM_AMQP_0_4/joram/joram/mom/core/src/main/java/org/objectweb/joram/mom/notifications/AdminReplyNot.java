/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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

/**
 * An <code>AdminReply</code> is used by a destination agent for replying to
 * a client administration request.
 */
public class AdminReplyNot extends AbstractNotification {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Field identifying the original request. */
  private String requestId;

  /** <code>true</code> if the request succeeded. */
  private boolean success;

  /** Info related to the processing of the request. */
  private String info;

  /**
   * Constructs an <code>AdminReply</code>.
   */
  public AdminReplyNot(AdminRequestNot request, boolean success, String info) {
    requestId = request.getId();
    this.success = success;
    this.info = info;
  }

  /** Returns the request identifier. */
  public String getRequestId() {
    return requestId;
  }

  /** Returns <code>true</code> if the request was successful. */
  public boolean getSuccess() {
    return success;
  }

  /** Returns the info related to the processing of the request. */
  public String getInfo() {
    return info;
  }

}
