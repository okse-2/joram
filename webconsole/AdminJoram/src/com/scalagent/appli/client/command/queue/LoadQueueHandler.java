/**
 * (c)2010 Scalagent Distributed Technologies
 */


package com.scalagent.appli.client.command.queue;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class LoadQueueHandler extends Handler<LoadQueueResponse>{
  
  public LoadQueueHandler(HandlerManager eventBus){
    super(eventBus);
  }

}
