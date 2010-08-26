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
import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageHandler;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.event.message.DeletedMessageHandler;
import com.scalagent.appli.client.event.message.NewMessageHandler;
import com.scalagent.appli.client.event.message.UpdatedMessageHandler;
import com.scalagent.appli.client.widget.SubscriptionDetailWidget;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 */
public class SubscriptionDetailPresenter extends BasePresenter<SubscriptionDetailWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewMessageHandler,
DeletedMessageHandler,
UpdatedMessageHandler,
UpdateCompleteHandler
//QueueNotFoundHandler,
//DeletedQueueHandler,
//UpdatedQueueHandler
{
	private SubscriptionWTO sub;

	public SubscriptionDetailPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus,RPCServiceCacheClient cache, SubscriptionWTO sub) {

		super(serviceRPC, cache, eventBus);

		System.out.println("### appli.client.presenter.SubscriptionDetailsPresenter loaded ");
		this.eventBus = eventBus;
		this.sub = sub;

		widget = new SubscriptionDetailWidget(this);
		retrieveMessage(sub);

	}

	public SubscriptionWTO getSubscription() { return sub; }


	public void retrieveMessage(SubscriptionWTO subscription) {
		cache.retrieveMessageSub(subscription);
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		//		cache.retrieveMessage(getQueue());
		cache.retrieveQueue(true);
	}
	
	@Override
	public void onNewMessage(MessageWTO message, String subName) {
		if(sub.getName().equals(subName))
			getWidget().addMessage(new MessageListRecord(message));
	}

	@Override
	public void onMessageDeleted(MessageWTO message, String subName) {
		if(sub.getName().equals(subName))

			widget.removeMessage(new MessageListRecord(message));
	}

	@Override
	public void onMessageUpdated(MessageWTO message, String subName) {
		if(sub.getName().equals(subName))
			widget.updateMessage(message);
	}

	@Override
	public void onUpdateComplete(String info) {
		if(sub.getName().equals(info)) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

//	@Override
//	public void onQueueNotFound(String queueName) {
//		System.out.println("!!! QUEUE NOT FOUND : "+queueName);
//		disableButtonRefresh(queueName);
//	}

//	@Override
//	public void onQueueDeleted(QueueWTO queue) {
//		System.out.println("!!! QUEUE DELETED : "+queue.getName());
//		disableButtonRefresh(queue.getName());
//	}

	public void disableButtonRefresh(String queueName) {
		if(sub.getName().equals(queueName)) {
			widget.getRefreshButton().disable();
			widget.getRefreshButton().setIcon("remove.png");
			widget.getRefreshButton().setTooltip("This queue no longer exists on JORAM");
		}
	}

//	@Override
//	public void onQueueUpdated(QueueWTO queue) {
//		if(this.queue.getName().equals(queue.getName())) {
//			this.queue = queue;
//			widget.updateQueue();
//		}
//
//	}

	public void deleteMessage(MessageWTO message, SubscriptionWTO sub) {
		service.execute(new DeleteMessageAction(message.getIdS(), sub.getName()), new DeleteMessageHandler(eventBus) {
			@Override
			public void onSuccess(DeleteMessageResponse response) {
				if (response.isSuccess()) {
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();	
				}
			}
		});
	}


	public SortedMap<Date, int[]> getSubHistory() {
		return cache.getSpecificHistory(sub.getName());
	}

	public void stopChart() {
		widget.stopChart();
	}
}