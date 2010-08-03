package org.ow2.joram.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;

public class SysoListener implements DestinationListener {
	private boolean printTrace = false;

	private Map<String, DestinationImplMBean> destinations = new HashMap<String, DestinationImplMBean>();
	private Map<String, ProxyImplMBean> users = new HashMap<String, ProxyImplMBean>();
	private List<ClientSubscriptionMBean> subscriptions = new ArrayList<ClientSubscriptionMBean>();

	public void onQueueAdded(String queueName, QueueImplMBean queue) {
		if(printTrace) System.out.println();
		if(printTrace) System.out.println(" +++ Queue creation : " + queueName);
		destinations.put(queueName, queue);
		if(printTrace) printStats();
	}

	public void onQueueRemoved(String queueName, QueueImplMBean queue) {
		if(printTrace) System.out.println();
		if(printTrace) System.out.println(" --- Queue deletion : " + queueName);
		destinations.remove(queueName);
	}

	public void onTopicAdded(String topicName, TopicImplMBean topic) {
		if(printTrace) System.out.println();
		if(printTrace) System.out.println(" +++ Topic creation : " + topicName);
		destinations.put(topicName, topic);
		if(printTrace) printStats();
		if(printTrace) System.out.println();
	}

	public void onTopicRemoved(String topicName, TopicImplMBean topic) {
		if(printTrace) System.out.println();
		if(printTrace) System.out.println(" --- Topic deletion : " + topicName);
		destinations.remove(topicName);
	}

	private void printStats() {
		Iterator<Entry<String, DestinationImplMBean>> iterator = destinations.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, DestinationImplMBean> entry = iterator.next();
			System.out.println("     -> " + entry.getKey() + " -> "
					+ entry.getValue().getNbMsgsDeliverSinceCreation() + " messages delivered.");
			if (entry.getValue() instanceof QueueImplMBean) {
				List msgs = ((QueueImplMBean) entry.getValue()).getMessagesView();
				Iterator<?> iter = msgs.iterator();
				while (iter.hasNext()) {
					MessageView msg = (MessageView) iter.next();
					System.out.println("          " + msg);
				}
			}
		}
	}

	public void onSubscriptionAdded(String userName, ClientSubscriptionMBean subscription) {
		if(printTrace) System.out.println(" **** Subscription creation for " + userName + " : " + subscription.getName());
		subscriptions.add(subscription);
	}

	public void onSubscriptionRemoved(String userName, ClientSubscriptionMBean subscription) {
		if(printTrace) System.out.println(" **** Subscription deletion for " + userName + " : " + subscription.getName());
		subscriptions.remove(subscription);
	}

	public void onUserAdded(String userName, ProxyImplMBean user) {
		if(printTrace) System.out.println();
		if(printTrace) System.out.println(" ** User creation : " + userName);
		users.put(userName, user);
	}

	public void onUserRemoved(String userName, ProxyImplMBean user) {
		if(printTrace) System.out.println(" ** User deletion : " + userName);
		if(printTrace) System.out.println();
		users.remove(userName);
	}

	public Map<String, DestinationImplMBean> getDestinations() {
		return destinations;
	}
	

	public Map<String, ProxyImplMBean> getUsers() {
		return users;
	}
	
	public List<ClientSubscriptionMBean> getSubscription() {
		return subscriptions;
	}
	
}
