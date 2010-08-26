/**
 * (c)2010 Scalagent Distributed Technologies
 * (c)2010 Tagsys-RFID 
 */
package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.LoadSubscriptionActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the list of devices from the server
 */
@CalledMethod(value=LoadSubscriptionActionImpl.class)
public class LoadSubscriptionAction implements Action<LoadSubscriptionResponse>{
 
  private boolean retrieveAll;
  private boolean forceUpdate;
  
  public LoadSubscriptionAction(){}
  
  public LoadSubscriptionAction(boolean retrieveAll, boolean forceUpdate){
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
