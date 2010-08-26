/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.client.command.user;

import java.util.List;

import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadDevicesAction
 */
public class LoadUserResponse implements Response{
 
  private List<UserWTO> users;
  
  public LoadUserResponse(){}
  
  public LoadUserResponse(List<UserWTO> users) {
    this.users = users;
  }
  
  public List<UserWTO> getUsers() {
    return users;
  }

}
