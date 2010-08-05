/**
 * (c)2010 Scalagent Distributed Technologies
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
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.topic.DeletedTopicHandler;
import com.scalagent.appli.client.event.topic.NewTopicHandler;
import com.scalagent.appli.client.event.topic.UpdatedTopicHandler;
import com.scalagent.appli.client.widget.TopicListWidget;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of topics.
 * Its widget is TopicListWidget.
 * 
 * @author Yohann CINTRE
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
	 * This method is called by the EventBus when a topic has been deleted on the server.
	 * The widget is called to remove it from the list.
	 */
	public void onTopicDeleted(TopicWTO topic) {
		getWidget().removeTopic(new TopicListRecord(topic));
	}
	
	/**
	 * This method is called by the EventBus when a topic has been updated on the server.
	 * The widget is called to update the topic list.
	 */
	public void onTopicUpdated(TopicWTO topic) {
		getWidget().updateTopic(topic);	
	}

	/**
	 * This method is called by the the TopicListWidget when the user click 
	 * on the "Refresh" button.
	 * The "Refresh" button is disabled, the topic list is updated. 
	 */	
	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveTopic(true);
	}

	/**
	 * This method is called by the EventBus when the update is done.
	 * The refresh button is re-enabled and the chart redrawn
	 */
	public void onUpdateComplete(String info) {
		if(info.equals("topic")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	/**
	 * This method is called by the TopicListWidget when the updating the chart.
	 * @result A map containing the history of the current topic
	 */
	public SortedMap<Date, int[]> getTopicHistory(String name) {
		return cache.getSpecificHistory(name);
	}

	/**
	 * This method is called by the TopicListWidget when the user submit the new topic form.
	 * The form information are sent to the server.
	 */
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

	/**
	 * This method is called by the TopicListWidget when the user submit the edited topic form.
	 * The form information are sent to the server.
	 */
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

	/**
	 * This method is called by the TopicListWidget when the user click the "delete" button of a topic.
	 * The topic name is sent to the server which delete the subscription.
	 */
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
