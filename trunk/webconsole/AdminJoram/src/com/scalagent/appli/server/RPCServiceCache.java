/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;
import org.ow2.joram.admin.LaunchInterface;

import com.google.gwt.core.ext.typeinfo.NotFoundException;
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
 * It handles: - devices - ECWSpecifications - test results
 * 
 * @author Florian Gimbert
 */
public class RPCServiceCache extends BaseRPCServiceCache {

	public static boolean isConnected = false;
	public static LaunchInterface JORAMInterface;

	public static final String SESSION_TOPICS = "topicsList";
	public static final String SESSION_QUEUES = "queuesList";
	public static final String SESSION_MESSAGES = "messagesList";
	private static final String SESSION_USERS = "usersList";
	private static final String SESSION_SUBSCRIPTION = "subscriptionList";

	/**
	 * These attributes are used to store and retrieve information in the
	 * session.
	 */
	public static final String SESSION_USER_LOGIN = "userLogin";


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
			throw new NotFoundException("Queue not found");
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

	public boolean connectJORAM(String login, String password) {
		try {
			if (!isConnected) {
				JORAMInterface = new LaunchInterface(login, password);
				isConnected = true;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void synchWithJORAM(boolean forceUpdate) {

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar lastupdatePlus5 = (GregorianCalendar) lastupdate.clone();
		lastupdatePlus5.add(Calendar.SECOND, 9);

		if (now.after(lastupdatePlus5) || mapDestinations == null || forceUpdate) {

			mapDestinations = JORAMInterface.getListener().getDestinations();
			mapUsers = JORAMInterface.getListener().getUsers();
			listSubscriptions = JORAMInterface.getListener().getSubscription();
			lastupdate = new GregorianCalendar();
		}

	}

	
	/** QUEUES **/
	
	public boolean createNewQueue(QueueWTO queue) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : creer une queue
		System.out.println("!!! TODO JORAM : Creation de queue : "+queue);

		return true;
	}
	
	public boolean editQueue(QueueWTO queue) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : editer une queue
		System.out.println("!!! TODO JORAM : Edition de queue : "+queue);

		return true;
	}
	
	public boolean deleteQueue(String queueName) {
		if (!isConnected) { return false; }

		// TODO JORAM : supprimer une queue
		System.out.println("!!! TODO JORAM : Suppression de la queue : "+queueName);

		return true;
	}

	public boolean cleanWaitingRequest(String queueName) {
		if (!isConnected) { return false; }

		Map<String, DestinationImplMBean> mapTmp = JORAMInterface.getListener().getDestinations();
		((QueueImplMBean) mapTmp.get(queueName)).cleanWaitingRequest();

		return true;
	}

	public boolean cleanPendingMessage(String queueName) {
		if (!isConnected) { return false; }

		Map<String, DestinationImplMBean> mapTmp = JORAMInterface.getListener().getDestinations();
		((QueueImplMBean) mapTmp.get(queueName)).cleanPendingMessage();

		return true;
	}

	
	/** USERS **/
	
	public boolean createNewUser(UserWTO user) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : creer un user
		System.out.println("!!! TODO JORAM : Creation d'un user : "+user);

		return true;
	}

	public boolean editUser(UserWTO user) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : editer un user
		System.out.println("!!! TODO JORAM : Edition de user : "+user);

		return true;
	}

	public boolean deleteUser(String userName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : supprimer un user
		System.out.println("!!! TODO JORAM : Suppression de user : "+userName);
		
		return true;
	}
	
	
	/** MESSAGES **/
	
	public boolean createNewMessage(MessageWTO message, String queueName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : creer un message
		System.out.println("!!! TODO JORAM : Creation du message : "+message.getIdS()+" dans "+queueName);
		
		return true;
	}

	public boolean editMessage(MessageWTO message, String queueName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : editer un message
		System.out.println("!!! TODO JORAM : Edition du message : "+message.getIdS()+" dans "+queueName);
		
		return true;
	}

	public boolean deleteMessage(String messageName, String queueName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : supprimer un message
		System.out.println("!!! TODO JORAM : Suppression du message : "+queueName+"/"+messageName);
		
		return true;
	}
	

	/** TOPICS **/
	
	public boolean createNewTopic(TopicWTO topic) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : creer un topic
		System.out.println("!!! TODO JORAM : Creation du topic : "+topic.getName());
		
		return true;
	}

	public boolean editTopic(TopicWTO topic) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : editer un topic
		System.out.println("!!! TODO JORAM : Edition du topic : "+topic.getName());
		
		return true;
	}

	public boolean deleteTopic(String topicName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : supprimer un topic
		System.out.println("!!! TODO JORAM : Suppression du topic : "+topicName);
		
		return true;
	}
	
	
	/** SUBSCRIPTIONS **/

	public boolean createNewSubscription(SubscriptionWTO sub) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : creer une subscription
		System.out.println("!!! TODO JORAM : Creation de la subscription : "+sub.getName());
		
		return true;
	}

	public boolean editSubscription(SubscriptionWTO sub) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : editer une subscription
		System.out.println("!!! TODO JORAM : Edition de la subscription : "+sub.getName());
		
		return true;
	}

	public boolean deleteSubscription(String subName) {
		if (!isConnected) { return false; }
		synchWithJORAM(true);
		
		// TODO JORAM : supprimer une subscription
		System.out.println("!!! TODO JORAM : Suppression de la subscription : "+subName);
		
		return true;
	}
}
