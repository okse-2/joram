/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.presenter;


import java.util.Date;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.topic.DeleteTopicAction;
import com.scalagent.appli.client.command.topic.DeleteTopicHandler;
import com.scalagent.appli.client.command.topic.DeleteTopicResponse;
import com.scalagent.appli.client.command.topic.SendEditedTopicAction;
import com.scalagent.appli.client.command.topic.SendEditedTopicHandler;
import com.scalagent.appli.client.command.topic.SendEditedTopicResponse;
import com.scalagent.appli.client.command.topic.SendNewTopicAction;
import com.scalagent.appli.client.command.topic.SendNewTopicHandler;
import com.scalagent.appli.client.command.topic.SendNewTopicResponse;
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.event.topic.DeletedTopicHandler;
import com.scalagent.appli.client.event.topic.NewTopicHandler;
import com.scalagent.appli.client.event.topic.UpdatedTopicHandler;
import com.scalagent.appli.client.widget.TopicListWidget;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;



/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 */
public class TopicListPresenter extends BasePresenter<TopicListWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewTopicHandler,
DeletedTopicHandler,
UpdatedTopicHandler,
UpdateCompleteHandler
{


	public TopicListPresenter(RPCServiceAsync testService, HandlerManager eventBus, RPCServiceCacheClient cache) {

		super(testService, cache, eventBus);

		System.out.println("### appli.client.presenter.TopicPresenter loaded ");
		this.eventBus = eventBus;
		widget = new TopicListWidget(this);
	}

	/**
	 * This method is called by the EventBus when a new topic has been created on the server.
	 * The widget is called to add it to the list.
	 */	
	public void onNewTopic(TopicWTO topic) {
		getWidget().addTopic(new TopicListRecord(topic));
	}

	/**
	 * This method is called by the EventBus when a device has been deleted on the server.
	 * The widget is called to remove it from the list.
	 */
	public void onTopicDeleted(TopicWTO topic) {
		getWidget().removeTopic(new TopicListRecord(topic));
	}


	public void onTopicUpdated(TopicWTO topic) {
		getWidget().updateTopic(topic);	
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveTopic(true);
	}


	
	public void onUpdateComplete(String info) {
		if(info.equals("topic")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
		
		
	}

	public void fireQueueDetailsClick(TopicWTO topic) {
		System.out.println("!!! TopicDetail : "+topic.getName());
		// TODO : topic detail!
		
	}
	
	public SortedMap<Date, int[]> getTopicHistory(String name) {
		return cache.getSpecificHistory(name);
	}

	public void createNewTopic(TopicWTO newTopic) {
		service.execute(new SendNewTopicAction(newTopic), new SendNewTopicHandler(eventBus) {
			@Override
			public void onSuccess(SendNewTopicResponse response) {
				if (response.isSuccess()) {
					SC.say(response.getMessage());
					widget.destroyForm();
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();
				}
			}
		});
	}
	
	public void editTopic(TopicWTO topic) {
		service.execute(new SendEditedTopicAction(topic), new SendEditedTopicHandler(eventBus) {
			@Override
			public void onSuccess(SendEditedTopicResponse response) {
				if (response.isSuccess()) {
					SC.say(response.getMessage());
					widget.destroyForm();
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();
				}
			}
		});
	}
	
	public void deleteTopic(TopicWTO topic) {
		service.execute(new DeleteTopicAction(topic.getName()), new DeleteTopicHandler(eventBus) {
			@Override
			public void onSuccess(DeleteTopicResponse response) {
				if (response.isSuccess()) {
					SC.say(response.getMessage());
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();	
				}
			}
		});
	}

}
