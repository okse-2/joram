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
package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.QueueWTO;

/**
 * @author Yohann CINTRE
 */
public class UpdatedQueueEvent extends GwtEvent<UpdatedQueueHandler> {

  public static Type<UpdatedQueueHandler> TYPE = new Type<UpdatedQueueHandler>();
  private QueueWTO queue;

  public UpdatedQueueEvent(QueueWTO queue) {
    this.queue = queue;
  }

  @Override
  public final Type<UpdatedQueueHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(UpdatedQueueHandler handler) {
    handler.onQueueUpdated(queue);
  }

}
