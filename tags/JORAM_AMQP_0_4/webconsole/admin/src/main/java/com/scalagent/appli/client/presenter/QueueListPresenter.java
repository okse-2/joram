/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.appli.client.presenter;

import java.util.List;

import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.command.queue.ClearPendingMessageAction;
import com.scalagent.appli.client.command.queue.ClearPendingMessageHandler;
import com.scalagent.appli.client.command.queue.ClearPendingMessageResponse;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestAction;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestHandler;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestResponse;
import com.scalagent.appli.client.command.queue.DeleteQueueAction;
import com.scalagent.appli.client.command.queue.DeleteQueueHandler;
import com.scalagent.appli.client.command.queue.DeleteQueueResponse;
import com.scalagent.appli.client.command.queue.SendEditedQueueAction;
import com.scalagent.appli.client.command.queue.SendEditedQueueHandler;
import com.scalagent.appli.client.command.queue.SendEditedQueueResponse;
import com.scalagent.appli.client.command.queue.SendNewQueueAction;
import com.scalagent.appli.client.command.queue.SendNewQueueHandler;
import com.scalagent.appli.client.command.queue.SendNewQueueResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.queue.DeletedQueueHandler;
import com.scalagent.appli.client.event.queue.NewQueueHandler;
import com.scalagent.appli.client.event.queue.QueueDetailClickEvent;
import com.scalagent.appli.client.event.queue.UpdatedQueueHandler;
import com.scalagent.appli.client.widget.QueueListWidget;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of queues.
 * Its widget is QueueListWidget.
 * 
 * @author Yohann CINTRE
 */
public class QueueListPresenter extends
    BasePresenter<QueueListWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements NewQueueHandler,
    DeletedQueueHandler, UpdatedQueueHandler, UpdateCompleteHandler {

  public QueueListPresenter(BaseRPCServiceAsync testService, SimpleEventBus eventBus,
      RPCServiceCacheClient cache) {

    super(testService, cache, eventBus);

    this.eventBus = eventBus;
    widget = new QueueListWidget(this);
  }

  /**
   * This method is called by the EventBus when a new queue has been created on
   * the server.
   * The widget is called to add it to the list.
   */
  public void onNewQueue(QueueWTO queue) {
    getWidget().addQueue(new QueueListRecord(queue));
  }

  /**
   * This method is called by the EventBus when a queue has been deleted on the
   * server.
   * The widget is called to remove it from the list.
   */
  public void onQueueDeleted(QueueWTO queue) {
    getWidget().removeQueue(new QueueListRecord(queue));
  }

  /**
   * This method is called by the EventBus when a queue has been updated on the
   * server.
   * The widget is called to update it.
   */
  public void onQueueUpdated(QueueWTO queue) {
    getWidget().updateQueue(queue);
  }

  /**
   * This method is called by the QueueListWidge when the user click on the
   * "Clear Pending Messages" button.
   * The name of the queue is sent to the server which clears the messages for
   * the selected queue.
   */
  public void clearPendingMessage(QueueWTO queue) {
    service.execute(new ClearPendingMessageAction(queue.getId()), new ClearPendingMessageHandler(eventBus) {
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

  /**
   * This method is called by the QueueListWidge when the user click on the
   * "Clear Waiting Requests" button.
   * The name of the queue is sent to the server which clears the requests for
   * the selected queue.
   */
  public void clearWaintingRequest(QueueWTO queue) {
    service.execute(new ClearWaitingRequestAction(queue.getId()), new ClearWaitingRequestHandler(eventBus) {
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

  /**
   * This method is called by the QueueListWidget when the user click on
   * "browse" button of a queue.
   * An event is fired to the EventBus.
   */
  public void fireQueueDetailsClick(QueueWTO queue) {
    eventBus.fireEvent(new QueueDetailClickEvent(queue));
  }

  /**
   * This method is called by the the QueueListWidget when the user click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the queues list is updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();
    cache.retrieveQueue(true);
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.QUEUE_UPDATE) {
      widget.getRefreshButton().enable();
      widget.redrawChart(true);
    }
  }

  /**
   * This method is called by the QueueListWidget when the updating the chart.
   * 
   * @result A map containing the history of the current queue
   */
  public List<HistoryData> getQueueHistory(String name) {
    return cache.getSpecificHistory(name);
  }

  /**
   * This method is called by the QueueListWidget when the user submit the new
   * queue form.
   * The form information are sent to the server.
   */
  public void createNewQueue(QueueWTO newQueue) {
    service.execute(new SendNewQueueAction(newQueue), new SendNewQueueHandler(eventBus) {
      @Override
      public void onSuccess(SendNewQueueResponse response) {
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
   * This method is called by the QueueListWidget when the user submit the
   * edited queue form.
   * The form information are sent to the server.
   */
  public void editQueue(QueueWTO queue) {
    service.execute(new SendEditedQueueAction(queue), new SendEditedQueueHandler(eventBus) {
      @Override
      public void onSuccess(SendEditedQueueResponse response) {
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
   * This method is called by the QueueListWidget when the user click the
   * "delete" button of a queue.
   * The queue name is sent to the server which delete the queue.
   */
  public void deleteQueue(QueueWTO queue) {
    service.execute(new DeleteQueueAction(queue.getId()), new DeleteQueueHandler(eventBus) {
      @Override
      public void onSuccess(DeleteQueueResponse response) {
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
