/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.info;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class LoadServerInfoHandler extends Handler<LoadServerInfoResponse>{
  
  public LoadServerInfoHandler(HandlerManager eventBus){
    super(eventBus);
  }
}
