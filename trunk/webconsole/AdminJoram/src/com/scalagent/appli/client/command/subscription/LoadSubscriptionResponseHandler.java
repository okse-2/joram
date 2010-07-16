/**
 * (c)2010 Scalagent Distributed Technologies
 * (c)2010 Tagsys-RFID 
 */
package com.scalagent.appli.client.command.subscription;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

public abstract class LoadSubscriptionResponseHandler extends Handler<LoadSubscriptionResponse>{
  
  public LoadSubscriptionResponseHandler(HandlerManager eventBus){
    super(eventBus);
  }

}
