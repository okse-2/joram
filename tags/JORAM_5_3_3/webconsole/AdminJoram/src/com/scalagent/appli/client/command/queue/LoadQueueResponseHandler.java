/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.client.command.queue;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;


/**
 *
 * @author sgonzalez
 */
public abstract class LoadQueueResponseHandler extends Handler<LoadQueueResponse>{
  
  public LoadQueueResponseHandler(HandlerManager eventBus){
    super(eventBus);
  }

}
