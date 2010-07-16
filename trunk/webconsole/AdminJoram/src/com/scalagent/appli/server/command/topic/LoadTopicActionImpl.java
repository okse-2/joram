/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.command.topic;

import java.util.List;

import com.scalagent.appli.client.command.topic.LoadTopicAction;
import com.scalagent.appli.client.command.topic.LoadTopicResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.server.command.ActionImpl;



/**
 *
 * @author sgonzalez
 */
public class LoadTopicActionImpl 
extends ActionImpl<LoadTopicResponse, LoadTopicAction, RPCServiceCache> {


  @Override
  public LoadTopicResponse execute(RPCServiceCache cache, LoadTopicAction action) {
	  
	  List<TopicWTO> topics = cache.getTopics(this.getHttpSession(), action.isRetrieveAll(), action.isforceUpdate());
	  System.out.println("### engine.server.command.topic.LoadTopicActionImpl.execute : "+topics.size()+" topics recupérés sur le serveur");
	  return new LoadTopicResponse(topics);
  }

  
}