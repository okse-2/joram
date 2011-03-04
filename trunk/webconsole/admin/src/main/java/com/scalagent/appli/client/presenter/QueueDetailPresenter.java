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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageHandler;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.client.command.message.SendEditedMessageAction;
import com.scalagent.appli.client.command.message.SendEditedMessageHandler;
import com.scalagent.appli.client.command.message.SendEditedMessageResponse;
import com.scalagent.appli.client.command.message.SendNewMessageAction;
import com.scalagent.appli.client.command.message.SendNewMessageHandler;
import com.scalagent.appli.client.command.message.SendNewMessageResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
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
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the details about a queue.
 * Its widget is QueueDetailWidget.
 * 
 * @author Yohann CINTRE
 */
public class QueueDetailPresenter extends
    BasePresenter<QueueDetailWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements
    NewMessageHandler, DeletedMessageHandler, UpdatedMessageHandler, UpdateCompleteHandler,
    QueueNotFoundHandler, DeletedQueueHandler, UpdatedQueueHandler {

  private static final String logCategory = QueueDetailPresenter.class.getName();

  private QueueWTO queue;

  public QueueDetailPresenter(BaseRPCServiceAsync serviceRPC, SimpleEventBus eventBus,
      RPCServiceCacheClient cache, QueueWTO queue) {

    super(serviceRPC, cache, eventBus);

    this.eventBus = eventBus;
    this.queue = queue;

    widget = new QueueDetailWidget(this);

    queue.clearMessagesList();
    cache.retrieveMessageQueue(queue, true);

  }

  /**
   * @return The queue displayed
   */
  public QueueWTO getQueue() {
    return queue;
  }

  /**
   * This method is called by the the QueueDetailWidget when the user click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the queues list and messages for the
   * displayed queue are updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();
    cache.retrieveQueue(true);
    cache.retrieveMessageQueue(getQueue(), false);
  }

  /**
   * This method is called by the EventBus when a new message has been created
   * on the server.
   * The widget is called to add it to the list if the message belong to the
   * displayed queue
   */
  public void onNewMessage(MessageWTO message, String queueName) {
    if (queue.getId().equals(queueName)) {
      try {
        Log.debug(logCategory, "** NewMessage " + message + " on " + queueName);
        getWidget().addMessage(new MessageListRecord(message));
      } catch (Throwable exc) {
        Log.error(logCategory, "** NewMessage ERROR", exc);
      }
    }
  }

  /**
   * This method is called by the EventBus when a message has been deleted on
   * the server.
   * The widget is called to remove it from the list if the message belong to
   * the displayed queue
   */
  public void onMessageDeleted(MessageWTO message, String queueName) {
    if (queue.getId().equals(queueName))
      widget.removeMessage(new MessageListRecord(message));
  }

  /**
   * This method is called by the EventBus when a message has been updated on
   * the server.
   * The widget is called to update it if the message belong to the displayed
   * queue.
   */
  public void onMessageUpdated(MessageWTO message, String queueName) {
    if (queue.getId().equals(queueName))
      widget.updateMessage(message);
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.QUEUE_UPDATE) {
      widget.getRefreshButton().enable();
      widget.redrawChart();
    }
  }

  /**
   * This method is called by the EventBus when the queue no longer exist on the
   * server.
   * The refresh button is disabled.
   */
  public void onQueueNotFound(String queueName) {
    disableButtonRefresh(queueName);
  }

  /**
   * This method is called by the EventBus when a queue has been deleted on the
   * server.
   * The refresh button is disabled.
   */
  public void onQueueDeleted(QueueWTO queue) {
    disableButtonRefresh(queue.getId());
  }

  /**
   * This method disable the refresh button on the widget
   */
  public void disableButtonRefresh(String queueName) {
    if (queue.getId().equals(queueName)) {
      widget.getRefreshButton().disable();
      widget.getRefreshButton().setIcon("remove.png");
      widget.getRefreshButton().setTooltip(Application.messages.queueDetailWidget_refreshbutton_tooltip());
    }
  }

  /**
   * This method is called by the EventBus when a queue has been updated on the
   * server.
   * The widget is called to update it if the queue is currently displayed.
   */
  public void onQueueUpdated(QueueWTO queue) {
    if (this.queue.getId().equals(queue.getId())) {
      this.queue = queue;
      widget.updateQueue();
    }

  }

  /**
   * This method is called by the QueueDetailWidget when the updating the chart.
   * 
   * @result A map containing the history of the current queue
   */
  public List<HistoryData> getQueueHistory() {
    return cache.getSpecificHistory(queue.getId());
  }

  /**
   * This method is called by the MainPresenter when the user close a tab.
   * The widget is called to stop updating the non-displayed chart to avoid an
   * exception.
   */
  public void stopChart() {
    widget.stopChart();
  }

  /**
   * This method is called by the QueueDetailWidget when the widget is
   * initialized.
   * It retrieve the messages for the current queue add them to the message list
   * of the widget.
   */
  public void initList() {
    List<String> vMessagesC = queue.getMessagesList();
    ArrayList<MessageWTO> listMessages = new ArrayList<MessageWTO>();
    for (String idMessage : vMessagesC) {
      listMessages.add(cache.getMessages().get(idMessage));
    }
    widget.setData(listMessages);
  }

  /**
   * This method is called by the QueueDetailWidget when the user submit the new
   * message form.
   * The form information are sent to the server.
   */
  public void createNewMessage(MessageWTO message, String queueName) {
    service.execute(new SendNewMessageAction(message, queueName), new SendNewMessageHandler(eventBus) {
      @Override
      public void onSuccess(SendNewMessageResponse response) {
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
   * This method is called by the QueueDetailWidget when the user submit the
   * edited message form.
   * The form information are sent to the server.
   */
  public void editMessage(MessageWTO message, String queueName) {
    service.execute(new SendEditedMessageAction(message, queueName), new SendEditedMessageHandler(eventBus) {
      @Override
      public void onSuccess(SendEditedMessageResponse response) {
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
   * This method is called by the QueueDetailWidget when the user click the
   * "delete" button of a message.
   * The message ID and the queue name are sent to the server which delete the
   * message.
   */
  public void deleteMessage(MessageWTO message, QueueWTO queue) {
    service.execute(new DeleteMessageAction(message.getId(), queue.getId(), true),
        new DeleteMessageHandler(eventBus) {
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

  /**
   * This method is called by the QueueDetailWidget when updating the chart.
   * 
   * @return A map of the queues in the client side cache.
   */
  public Map<String, QueueWTO> getQueues() {
    return cache.getQueues();
  }

}