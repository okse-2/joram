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
package com.scalagent.appli.client.event.common;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yohann CINTRE
 */
public class UpdateCompleteEvent extends GwtEvent<UpdateCompleteHandler> {

  public static Type<UpdateCompleteHandler> TYPE = new Type<UpdateCompleteHandler>();

  public static final int GENERIC_UPDATE = 0;

  public static final int USER_UPDATE = 1;

  public static final int TOPIC_UPDATE = 2;

  public static final int QUEUE_UPDATE = 3;

  public static final int SERVER_INFO_UPDATE = 4;

  public static final int SUBSCRIPTION_UPDATE = 5;

  private String info;

  private int updateType = GENERIC_UPDATE;

  public UpdateCompleteEvent(int updateType) {
    this.updateType = updateType;
  }

  public UpdateCompleteEvent(int updateType, String info) {
    this.info = info;
    this.updateType = updateType;
  }

  @Override
  public final Type<UpdateCompleteHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(UpdateCompleteHandler handler) {
    handler.onUpdateComplete(updateType, info);
  }

}
