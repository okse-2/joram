/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;
import java.util.Vector;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageHandler;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.event.message.DeletedMessageHandler;
import com.scalagent.appli.client.event.message.NewMessageHandler;
import com.scalagent.appli.client.event.message.QueueNotFoundHandler;
import com.scalagent.appli.client.event.message.UpdatedMessageHandler;
import com.scalagent.appli.client.event.queue.DeletedQueueHandler;
import com.scalagent.appli.client.event.queue.UpdatedQueueHandler;
import com.scalagent.appli.client.widget.QueueDetailWidget;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 * @author Florian Gimbert
 */
public class QueueDetailPresenter extends BasePresenter<QueueDetailWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewMessageHandler,
DeletedMessageHandler,
UpdatedMessageHandler,
UpdateCompleteHandler,
QueueNotFoundHandler,
DeletedQueueHandler,
UpdatedQueueHandler
{
	private QueueWTO queue;

	public QueueDetailPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus,RPCServiceCacheClient cache, QueueWTO queue) {

		super(serviceRPC, cache, eventBus);

		System.out.println("### appli.client.presenter.QueueDetailsPresenter loaded ");
		this.eventBus = eventBus;
		this.queue = queue;

		widget = new QueueDetailWidget(this);
		retrieveMessage(queue);

	}

	public QueueWTO getQueue() { return queue; }


	public void retrieveMessage(QueueWTO queue) {
		cache.retrieveMessageQueue(queue);
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveQueue(true);
		cache.retrieveMessageQueue(getQueue());
	}

	@Override
	public void onNewMessage(MessageWTO message, String queueName) {
		if(queue.getName().equals(queueName))
			getWidget().addMessage(new MessageListRecord(message));
	}

	@Override
	public void onMessageDeleted(MessageWTO message, String queueName) {
		if(queue.getName().equals(queueName))

			widget.removeMessage(new MessageListRecord(message));
	}

	@Override
	public void onMessageUpdated(MessageWTO message, String queueName) {
		if(queue.getName().equals(queueName))
			widget.updateMessage(message);
	}

	@Override
	public void onUpdateComplete(String info) {
		if(queue.getName().equals(info)) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	@Override
	public void onQueueNotFound(String queueName) {
		System.out.println("!!! QUEUE NOT FOUND : "+queueName);
		disableButtonRefresh(queueName);
	}


	@Override
	public void onQueueDeleted(QueueWTO queue) {
		System.out.println("!!! QUEUE DELETED : "+queue.getName());
		disableButtonRefresh(queue.getName());
	}

	public void disableButtonRefresh(String queueName) {
		if(queue.getName().equals(queueName)) {
			widget.getRefreshButton().disable();
			widget.getRefreshButton().setIcon("remove.png");
			widget.getRefreshButton().setTooltip(Application.messages.queueDetailWidget_refreshbutton_tooltip());
		}
	}

	@Override
	public void onQueueUpdated(QueueWTO queue) {
		if(this.queue.getName().equals(queue.getName())) {
			this.queue = queue;
			widget.updateQueue();
		}

	}

	public void deleteMessage(MessageWTO message, QueueWTO queue) {
		service.execute(new DeleteMessageAction(message.getIdS(), queue.getName()), new DeleteMessageHandler(eventBus) {
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


	public SortedMap<Date, int[]> getQueueHistory() {
		return cache.getSpecificHistory(queue.getName());
	}

	public void stopChart() {
		widget.stopChart();
	}

	public void initList() {
		Vector<String> vMessagesC = queue.getMessagesList();
		ArrayList<MessageWTO> listMessages = new ArrayList<MessageWTO>();
		for(String idMessage : vMessagesC) {
			listMessages.add(cache.getMessages().get(idMessage));
		}
		widget.setData(listMessages);
	}
}