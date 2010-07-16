package com.scalagent.appli.client.presenter;


import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.queue.ClearPendingMessageAction;
import com.scalagent.appli.client.command.queue.ClearPendingMessageHandler;
import com.scalagent.appli.client.command.queue.ClearPendingMessageResponse;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestAction;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestHandler;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestResponse;
import com.scalagent.appli.client.command.queue.DeleteQueueAction;
import com.scalagent.appli.client.command.queue.DeleteQueueHandler;
import com.scalagent.appli.client.command.queue.DeleteQueueResponse;
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.event.queue.DeletedQueueHandler;
import com.scalagent.appli.client.event.queue.NewQueueHandler;
import com.scalagent.appli.client.event.queue.QueueDetailClickEvent;
import com.scalagent.appli.client.event.queue.UpdatedQueueHandler;
import com.scalagent.appli.client.widget.QueueListWidget;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;


/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 * @author Yohann CINTRE
 */
public class QueueListPresenter extends BasePresenter<QueueListWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewQueueHandler,
DeletedQueueHandler,
UpdatedQueueHandler,
UpdateCompleteHandler
{

	SortedMap<Date, Integer> chartHistory = new TreeMap<Date, Integer>();

	public QueueListPresenter(RPCServiceAsync testService, HandlerManager eventBus, RPCServiceCacheClient cache) {

		super(testService, cache, eventBus);

		System.out.println("### appli.client.presenter.QueuePresenter loaded ");

		this.eventBus = eventBus;
		widget = new QueueListWidget(this);
	}


	/**
	 * This method is called by the EventBus when a new topic has been created on the server.
	 * The widget is called to add it to the list.
	 * 
	 */	
	public void onNewQueue(QueueWTO queue) {
		getWidget().addQueue(new QueueListRecord(queue));
	}

	/**
	 * This method is called by the EventBus when a device has been deleted on the server.
	 * The widget is called to remove it from the list.
	 */
	public void onQueueDeleted(QueueWTO queue) {
		getWidget().removeQueue(new QueueListRecord(queue));
	}

	public void onQueueUpdated(QueueWTO queue) {
		getWidget().updateQueue(queue);	
	}

	public void deleteQueue(QueueWTO queue) {
		service.execute(new DeleteQueueAction(queue.getName()), new DeleteQueueHandler(eventBus) {
			@Override
			public void onSuccess(DeleteQueueResponse response) {
				if (response.isSuccess()) {
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();	
				}
			}
		});

	}

	public void clearPendingMessage(QueueWTO queue) {
		service.execute(new ClearPendingMessageAction(queue.getName()), new ClearPendingMessageHandler(eventBus) {
			@Override
			public void onSuccess(ClearPendingMessageResponse response) {
				if (response.isSuccess()) {
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();			
				}
			}
		});
	}

	public void clearWaintingRequest(QueueWTO queue) {
		service.execute(new ClearWaitingRequestAction(queue.getName()), new ClearWaitingRequestHandler(eventBus) {
			@Override
			public void onSuccess(ClearWaitingRequestResponse response) {
				if (response.isSuccess()) {
					fireRefreshAll();
				} else {
					SC.warn(response.getMessage());
					fireRefreshAll();
				}
			}
		});
	}

	public void fireQueueDetailsClick(QueueWTO queue) {
		eventBus.fireEvent(new QueueDetailClickEvent(queue));
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveQueue(true);
	}

	@Override
	public void onUpdateComplete(String info) {
		if(info.equals("queue")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	public SortedMap<Date, int[]> getQueueHistory(String name) {
		return cache.getSpecificHistory(name);
	}

}
