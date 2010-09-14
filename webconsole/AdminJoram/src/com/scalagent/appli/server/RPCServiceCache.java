/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;
import org.ow2.joram.admin.Activator;
import org.ow2.joram.admin.AdminListener;
import org.ow2.joram.admin.JoramAdmin;
import org.ow2.joram.admin.JoramAdminOSGi;

import com.scalagent.appli.server.converter.MessageWTOConverter;
import com.scalagent.appli.server.converter.QueueWTOConverter;
import com.scalagent.appli.server.converter.SubscriptionWTOConverter;
import com.scalagent.appli.server.converter.TopicWTOConverter;
import com.scalagent.appli.server.converter.UserWTOConverter;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.server.BaseRPCServiceCache;

/**
 * This class is used as a cache. Periodically, it retrieves data from the
 * server, compares it with stored data (in session) and send diff to the
 * client.
 * 
 * It handles:
 *    - queues
 *    - topics
 *    - users
 *    - subscriptions
 *    - messages
 *    
 *    @author Yohann CINTRE
 */

public class RPCServiceCache extends BaseRPCServiceCache {

	private static boolean isConnected = false;
	private static JoramAdmin joramAdmin;
	private LiveListener listener = new LiveListener();

	private static final String SESSION_TOPICS = "topicsList";
	private static final String SESSION_QUEUES = "queuesList";
	private static final String SESSION_MESSAGES = "messagesList";
	private static final String SESSION_USERS = "usersList";
	private static final String SESSION_SUBSCRIPTION = "subscriptionList";


	private Map<String, DestinationImplMBean> mapDestinations;
	private Map<String, ProxyImplMBean> mapUsers;
	private List<ClientSubscriptionMBean> listSubscriptions;


	GregorianCalendar lastupdate = new GregorianCalendar(1970, 1, 1);

	@SuppressWarnings("unchecked")
	public List<TopicWTO> getTopics(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

		synchWithJORAM(forceUpdate);

		TopicWTO[] newTopics = TopicWTOConverter.getTopicWTOArray(mapDestinations);

		// retrieve previous devices list from session
		HashMap<String, TopicWTO> sessionTopics = (HashMap<String, TopicWTO>) session
		.getAttribute(RPCServiceCache.SESSION_TOPICS);

		if (sessionTopics == null) {
			sessionTopics = new HashMap<String, TopicWTO>();
		}

		List<TopicWTO> toReturn = null;
		toReturn = compareEntities(newTopics, sessionTopics);

		if (retrieveAll) {
			toReturn = this.retrieveAll(sessionTopics);
		}

		// save devices in session
		session.setAttribute(RPCServiceCache.SESSION_TOPICS, sessionTopics);

		return toReturn;

	}

	@SuppressWarnings("unchecked")
	public List<QueueWTO> getQueues(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

		synchWithJORAM(forceUpdate);

		QueueWTO[] newQueues = QueueWTOConverter.getQueueWTOArray(mapDestinations);

		// retrieve previous devices list from session
		HashMap<String, QueueWTO> sessionQueues = (HashMap<String, QueueWTO>) session.getAttribute(RPCServiceCache.SESSION_QUEUES);

		if (sessionQueues == null) {
			sessionQueues = new HashMap<String, QueueWTO>();
		}

		List<QueueWTO> toReturn = null;
		toReturn = compareEntities(newQueues, sessionQueues);

		if (retrieveAll) {
			toReturn = this.retrieveAll(sessionQueues);
		}

		// save devices in session
		session.setAttribute(RPCServiceCache.SESSION_QUEUES, sessionQueues);

		return toReturn;
	}

	@SuppressWarnings("unchecked")
	public List<MessageWTO> getMessages(HttpSession session, String queueName) throws Exception {

		synchWithJORAM(true);

		QueueImplMBean queue = (QueueImplMBean) mapDestinations.get(queueName);

		if(queue == null) {
			throw new Exception("Queue not found");
		}

		//		List<Message> listMessage = queue.getMessagesView();
		List<MessageView> listMessage = queue.getMessagesView();

		MessageWTO[] newMessages = MessageWTOConverter.getMessageWTOArray(listMessage);

		// retrieve previous devices list from session

		HashMap<String, HashMap<String, MessageWTO>> sessionMessagesAll = (HashMap<String, HashMap<String, MessageWTO>>) session
		.getAttribute(RPCServiceCache.SESSION_MESSAGES);
		if (sessionMessagesAll == null) {
			sessionMessagesAll = new HashMap<String, HashMap<String, MessageWTO>>();
		}

		HashMap<String, MessageWTO> sessionMessagesQueue = sessionMessagesAll
		.get(queueName);
		if (sessionMessagesQueue == null) {
			sessionMessagesQueue = new HashMap<String, MessageWTO>();
		}

		List<MessageWTO> toReturn = null;
		toReturn = compareEntities(newMessages, sessionMessagesQueue);
		// save devices in session
		sessionMessagesAll.put(queueName, sessionMessagesQueue);
		session.setAttribute(RPCServiceCache.SESSION_MESSAGES, sessionMessagesAll);

		return toReturn;
	}

	@SuppressWarnings("unchecked")
	public List<UserWTO> getUsers(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

		synchWithJORAM(forceUpdate);

		UserWTO[] newUsers = UserWTOConverter.getUserWTOArray(mapUsers);

		// retrieve previous devices list from session
		HashMap<String, UserWTO> sessionUsers = (HashMap<String, UserWTO>) session.getAttribute(RPCServiceCache.SESSION_USERS);

		if (sessionUsers == null) {
			sessionUsers = new HashMap<String, UserWTO>();
		}

		List<UserWTO> toReturn = null;
		toReturn = compareEntities(newUsers, sessionUsers);

		if (retrieveAll) {
			toReturn = this.retrieveAll(sessionUsers);
		}

		// save devices in session
		session.setAttribute(RPCServiceCache.SESSION_USERS, sessionUsers);

		return toReturn;
	}

	@SuppressWarnings("unchecked")
	public List<SubscriptionWTO> getSubscriptions(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

		synchWithJORAM(forceUpdate);


		SubscriptionWTO[] newSubscriptions = SubscriptionWTOConverter.getSubscriptionWTOArray(listSubscriptions);

		// retrieve previous devices list from session
		HashMap<String, SubscriptionWTO> sessionSubscriptions = (HashMap<String, SubscriptionWTO>) session.getAttribute(RPCServiceCache.SESSION_SUBSCRIPTION);

		if (sessionSubscriptions == null) {
			sessionSubscriptions = new HashMap<String, SubscriptionWTO>();
		}

		List<SubscriptionWTO> toReturn = null;
		toReturn = compareEntities(newSubscriptions, sessionSubscriptions);

		if (retrieveAll) {
			toReturn = this.retrieveAll(sessionSubscriptions);
		}

		// save devices in session
		session.setAttribute(RPCServiceCache.SESSION_SUBSCRIPTION, sessionSubscriptions);

		return toReturn;
	}
	
	public float[] getInfos(boolean isforceUpdate) {
		
		synchWithJORAM(isforceUpdate);
		
		int lower = 0;
		int higher = 2;
		float e1 = (float)(Math.random() * (higher-lower)) + lower;
		float n1 = (float)(Math.random() * (higher-lower)) + lower;
		float n2 = (float)(Math.random() * (higher-lower)) + lower;
		float n3 = (float)(Math.random() * (higher-lower)) + lower;
		float n4 = (float)(Math.random() * (higher-lower)) + lower;
		
		float[] vInfos = new float[5];
		vInfos[0] = e1;
		vInfos[1] = n1;
		vInfos[2] = n2;
		vInfos[3] = n3;
		vInfos[4] = n4;
		return vInfos;
	}

	public boolean connectJORAM(String login, String password) {
        joramAdmin = new JoramAdminOSGi(Activator.getContext());
	    boolean connected = joramAdmin.connect(login, password);
	    if (connected) {
	      joramAdmin.start(listener);
	    }
	    return connected;
	}

	public void synchWithJORAM(boolean forceUpdate) {

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar lastupdatePlus5 = (GregorianCalendar) lastupdate.clone();
		lastupdatePlus5.add(Calendar.SECOND, 9);

		if (now.after(lastupdatePlus5) || mapDestinations == null || forceUpdate) {

			mapDestinations = listener.getDestinations();
			mapUsers = listener.getUsers();
			listSubscriptions = listener.getSubscription();
			lastupdate = new GregorianCalendar();
		}

	}

	
	/** QUEUES **/
	
	public boolean createNewQueue(QueueWTO queue) {
		if (!isConnected) { return false; }
		return joramAdmin.createNewQueue(queue.getName(), queue.getDMQId(), queue.getDestinationId(), queue.getPeriod(), queue.getThreshold(), queue.getNbMaxMsg(), queue.isFreeReading(), queue.isFreeWriting());
	}
	
	public boolean editQueue(QueueWTO queue) {
		if (!isConnected) { return false; }
		return joramAdmin.editQueue(queue.getName(), queue.getDMQId(), queue.getDestinationId(), queue.getPeriod(), queue.getThreshold(), queue.getNbMaxMsg(), queue.isFreeReading(), queue.isFreeWriting());
	}
	
	public boolean deleteQueue(String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.deleteQueue(queueName);
	}

	public boolean cleanWaitingRequest(String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.cleanWaitingRequest(queueName);
	}

	public boolean cleanPendingMessage(String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.cleanPendingMessage(queueName);
	}

	
	/** USERS **/
	
	public boolean createNewUser(UserWTO user) {
		if (!isConnected) { return false; }
		return joramAdmin.createNewUser(user.getName(), user.getPeriod());
	}

	public boolean editUser(UserWTO user) {
		if (!isConnected) { return false; }
		return joramAdmin.editUser(user.getName(), user.getPeriod());
	}

	public boolean deleteUser(String userName) {
		if (!isConnected) { return false; }
		return joramAdmin.deleteUser(userName);
	}
	
	
	/** MESSAGES **/
	
	public boolean createNewMessage(MessageWTO message, String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.createNewMessage(queueName, 
				message.getId(), 
				message.getExpiration(),
				message.getTimestamp(),
				message.getPriority(),
				message.getText(),
				message.getType());
	}

	public boolean editMessage(MessageWTO message, String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.editMessage(queueName, 
				message.getId(), 
				message.getExpiration(),
				message.getTimestamp(),
				message.getPriority(),
				message.getText(),
				message.getType());
	}

	public boolean deleteMessage(String messageName, String queueName) {
		if (!isConnected) { return false; }
		return joramAdmin.deleteMessage(messageName, queueName);
	}
	

	/** TOPICS **/
	
	public boolean createNewTopic(TopicWTO topic) {
		if (!isConnected) { return false; }
		return joramAdmin.createNewTopic(
				topic.getName(), 
				topic.getDMQId(),
				topic.getDestinationId(),
				topic.getPeriod(),
				topic.isFreeReading(),
				topic.isFreeWriting());
	}

	public boolean editTopic(TopicWTO topic) {
		if (!isConnected) { return false; }
		return joramAdmin.editTopic(
				topic.getName(), 
				topic.getDMQId(),
				topic.getDestinationId(),
				topic.getPeriod(),
				topic.isFreeReading(),
				topic.isFreeWriting());
	}

	public boolean deleteTopic(String topicName) {
		if (!isConnected) { return false; }
		return joramAdmin.deleteTopic(topicName);
	}
	
	
	/** SUBSCRIPTIONS **/

	public boolean createNewSubscription(SubscriptionWTO sub) {
		if (!isConnected) { return false; }
		return joramAdmin.createNewSubscription(sub.getName(), sub.getNbMaxMsg(), sub.getContextId(), sub.getSelector(), sub.getSubRequestId(), sub.isActive(), sub.isDurable());
	}

	public boolean editSubscription(SubscriptionWTO sub) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		return joramAdmin.editSubscription(sub.getName(), sub.getNbMaxMsg(), sub.getContextId(), sub.getSelector(), sub.getSubRequestId(), sub.isActive(), sub.isDurable());
	}

	public boolean deleteSubscription(String subName) {
		if (!isConnected) { return false; }
		return joramAdmin.deleteSubscription(subName);
	}

  static class LiveListener implements AdminListener {

    private Map<String, DestinationImplMBean> destinations = new HashMap<String, DestinationImplMBean>();
    private Map<String, ProxyImplMBean> users = new HashMap<String, ProxyImplMBean>();
    private List<ClientSubscriptionMBean> subscriptions = new ArrayList<ClientSubscriptionMBean>();

    public void onQueueAdded(String queueName, QueueImplMBean queue) {
      destinations.put(queueName, queue);
    }

    public void onQueueRemoved(String queueName, QueueImplMBean queue) {
      destinations.remove(queueName);
    }

    public void onTopicAdded(String topicName, TopicImplMBean topic) {
      destinations.put(topicName, topic);
    }

    public void onTopicRemoved(String topicName, TopicImplMBean topic) {
      destinations.remove(topicName);
    }

    public void onSubscriptionAdded(String userName, ClientSubscriptionMBean subscription) {
      subscriptions.add(subscription);
    }

    public void onSubscriptionRemoved(String userName, ClientSubscriptionMBean subscription) {
      subscriptions.remove(subscription);
    }

    public void onUserAdded(String userName, ProxyImplMBean user) {
      users.put(userName, user);
    }

    public void onUserRemoved(String userName, ProxyImplMBean user) {
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

}
