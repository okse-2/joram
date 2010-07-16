/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.presenter;

import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.widget.ServerWidget;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.presenter.BasePresenter;

/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 * @author Florian Gimbert
 */
public class ServerPresenter extends BasePresenter<ServerWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements
UpdateCompleteHandler
{

	public ServerPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus,RPCServiceCacheClient cache) {

		super(serviceRPC, cache, eventBus);

		System.out.println("### appli.client.presenter.QueueDetailsPresenter loaded ");
		this.eventBus = eventBus;

		widget = new ServerWidget(this);

	}


	public SortedMap<Date,Integer> getQueuesHistory() {
		return cache.getQueuesHistory();
	}

	public SortedMap<Date,Integer> getTopicsHistory() {
		return cache.getTopicsHistory();
	}

	public SortedMap<Date,Integer> getUsersHistory() {
		return cache.getUsersHistory();
	}

	public SortedMap<Date,Integer> getSubsHistory() {
		return cache.getSubsHistory();
	}

	public SortedMap<Date, float[]> getEngineHistory() {
		return cache.getEngineHistory();
	}

	public SortedMap<Date, float[]> getNetworkHistory() {
		return cache.getNetworkHistory();
	}


	@Override
	public void onUpdateComplete(String info) {

		int lower = 0;
		int higher = 2;
		float e1 = (float)(Math.random() * (higher-lower)) + lower;
		float e2 = (float)(Math.random() * (higher-lower)) + lower;
		float e3 = (float)(Math.random() * (higher-lower)) + lower;
		float n1 = (float)(Math.random() * (higher-lower)) + lower;
		float n2 = (float)(Math.random() * (higher-lower)) + lower;
		float n3 = (float)(Math.random() * (higher-lower)) + lower;

		cache.addToHistory(RPCServiceCacheClient.QUEUE, cache.getQueues().size());
		cache.addToHistory(RPCServiceCacheClient.SUB, cache.getSubscriptions().size());
		cache.addToHistory(RPCServiceCacheClient.TOPIC, cache.getTopics().size());
		cache.addToHistory(RPCServiceCacheClient.USER, cache.getUsers().size());
		cache.addToHistory(RPCServiceCacheClient.ENGINE, e1, e2, e3);
		cache.addToHistory(RPCServiceCacheClient.NETWORK, n1, n2, n3);

		HashMap<String, QueueWTO> queues = cache.getQueues();
		for(String key : queues.keySet()) {
			QueueWTO queue = queues.get(key);
			cache.addToSpecificHistory(queue.getName(),
					(int) queue.getNbMsgsReceiveSinceCreation(),
					(int) queue.getNbMsgsDeliverSinceCreation(),
					(int) queue.getNbMsgsSentToDMQSinceCreation(),
					(int)queue.getPendingMessageCount());
		}

		HashMap<String, TopicWTO> topics = cache.getTopics();
		for(String key : topics.keySet()) {
			TopicWTO topic = topics.get(key);
			cache.addToSpecificHistory(topic.getName(),
					(int) topic.getNbMsgsReceiveSinceCreation(),
					(int) topic.getNbMsgsDeliverSinceCreation(),
					(int) topic.getNbMsgsSentToDMQSinceCreation());
		}

		HashMap<String, UserWTO> users = cache.getUsers();
		for(String key : users.keySet()) {
			UserWTO user = users.get(key);
			cache.addToSpecificHistory(user.getName(), 
					(int) user.getNbMsgsSentToDMQSinceCreation(),
					(int) user.getSubscriptionNames().length);
		}

		HashMap<String, SubscriptionWTO> subs = cache.getSubscriptions();
		for(String key : subs.keySet()) {
			SubscriptionWTO sub = subs.get(key);
			cache.addToSpecificHistory(sub.getName(),
					(int) sub.getPendingMessageCount(),
					(int) sub.getNbMsgsDeliveredSinceCreation(),
					(int) sub.getNbMsgsSentToDMQSinceCreation());
		}

	}

}