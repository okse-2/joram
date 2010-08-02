/**
 * (c)2010 Scalagent Distributed Technologies
 */


package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.LoadQueueActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the list of queue from the server
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=LoadQueueActionImpl.class)
public class LoadQueueAction implements Action<LoadQueueResponse>{
 
  private boolean retrieveAll;
  private boolean forceUpdate;
  
  public LoadQueueAction(){}
  
  public LoadQueueAction(boolean retrieveAll, boolean forceUpdate){
    this.retrieveAll = retrieveAll;
    this.forceUpdate = forceUpdate;
  }

  /**
   * @return the retrieveAll
   */
  public boolean isRetrieveAll() {
    return retrieveAll;
  }
 
  public boolean isforceUpdate() {
	  return forceUpdate;
  }
  
}
