/**
 * (c)2010 Scalagent Distributed Technologies
 */
package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.LoadSubscriptionActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the list of subscriptions from the server
 * 
 * @author Yohann CINTRE
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
