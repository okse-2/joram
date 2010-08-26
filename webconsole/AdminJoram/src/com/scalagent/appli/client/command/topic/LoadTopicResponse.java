/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.client.command.topic;

import java.util.List;

import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.command.Response;


/**
 * Response to the action LoadDevicesAction
 */
public class LoadTopicResponse implements Response{
 
  private List<TopicWTO> topics;
  
  public LoadTopicResponse(){}
  
  public LoadTopicResponse(List<TopicWTO> topics) {
    this.topics = topics;
  }
  
  public List<TopicWTO> getTopics() {
    return topics;
  }

}
