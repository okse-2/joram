/**
 * (c)2010 Scalagent Distributed Technologies
 */
package com.scalagent.appli.client.command.subscription;

import java.util.List;

import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadSubscriptionAction
 * 
 * @author Yohann CINTRE
 */
public class LoadSubscriptionResponse implements Response{
 
  private List<SubscriptionWTO> subs;
  
  public LoadSubscriptionResponse(){}
  
  public LoadSubscriptionResponse(List<SubscriptionWTO> subs) {
    this.subs = subs;
  }
  
  public List<SubscriptionWTO> getSubscriptions() {
    return subs;
  }

}
