/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class DeletedMessageEvent extends GwtEvent<DeletedMessageHandler> {

  public static Type<DeletedMessageHandler> TYPE = new Type<DeletedMessageHandler>();
  private MessageWTO message;
  private String queueName;

  public DeletedMessageEvent(MessageWTO message, String queueName) {
    this.message = message;
    this.queueName = queueName;
  }

  @Override
  public final Type<DeletedMessageHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(DeletedMessageHandler handler) {
    handler.onMessageDeleted(message, queueName);
  }
}
