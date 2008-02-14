/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

public class GetSubscriptionMessage extends SubscriptionAdminRequest {
  private static final long serialVersionUID = -6415435591049976630L;


  private String subName;

  private String msgId;

  public GetSubscriptionMessage(
    String userId,
    String subName,
    String msgId) {
    super(userId);
    this.subName = subName;
    this.msgId = msgId;
  }

  public final String getSubscriptionName() {
    return subName;
  }

  public final String getMessageId() {
    return msgId;
  }
}
