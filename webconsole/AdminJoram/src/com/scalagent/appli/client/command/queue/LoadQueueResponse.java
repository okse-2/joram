/**
 * (c)2010 Scalagent Distributed Technologies
 */


package com.scalagent.appli.client.command.queue;

import java.util.List;

import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadQueueAction
 * 
 * @author Yohann CINTRE
 */
public class LoadQueueResponse implements Response{
 
  private List<QueueWTO> queues;
  
  public LoadQueueResponse(){}
  
  public LoadQueueResponse(List<QueueWTO> queue) {
    this.queues = queue;
  }
  
  public List<QueueWTO> getQueues() {
    return queues;
  }

}
