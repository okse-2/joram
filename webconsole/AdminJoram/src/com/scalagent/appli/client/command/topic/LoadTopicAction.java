/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.client.command.topic;

import com.scalagent.appli.server.command.topic.LoadTopicActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the list of devices from the server
 */
@CalledMethod(value=LoadTopicActionImpl.class)
public class LoadTopicAction implements Action<LoadTopicResponse>{
 
  private boolean retrieveAll;
  private boolean forceUpdate;
  
  public LoadTopicAction(){}
  
  public LoadTopicAction(boolean retrieveAll, boolean forceUpdate){
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
