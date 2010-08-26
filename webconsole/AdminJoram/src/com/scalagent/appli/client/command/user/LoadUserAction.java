/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.LoadUserActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the list of devices from the server
 */
@CalledMethod(value=LoadUserActionImpl.class)
public class LoadUserAction implements Action<LoadUserResponse>{
 
  private boolean retrieveAll;
  private boolean forceUpdate;
  
  public LoadUserAction(){}
  
  public LoadUserAction(boolean retrieveAll, boolean forceUpdate){
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
