package org.objectweb.joram.mom.dest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ClientSubscriptionNot;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.GetClientSubscriptions;
import org.objectweb.joram.mom.notifications.ReconnectSubscribersNot;
import org.objectweb.joram.mom.notifications.TopicForwardNot;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetSubscriptionsRequest;
import org.objectweb.joram.shared.admin.ScaleRequest;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;

/**
 * Class describing a node of a scalable topic tree.
 * The difference with Topic is that it allows more than one father.
 * 
 * @author Ahmed El Rheddane
 */
public class ElasticTopic extends Topic {

	class TopicDesc implements Serializable {
		private static final long serialVersionUID = 5983962604141303712L;

		AgentId id;
		String server;
		int port;
	}

	private static final long serialVersionUID = 3074584772834111626L;

	/**
	 * Pool of topics to forward msgs to.
	 */
	private List<TopicDesc> pool = new ArrayList<TopicDesc>();

	/**
	 * True if topic is head of the elastic topic tree.
	 */
	private boolean isRoot = false;

	/**
	 * Index of topic to forward next subscription to.
	 */
	private int subId = 0;

	public void setProperties(Properties properties, boolean firstTime) throws Exception {
		super.setProperties(properties, firstTime);

		if (properties != null && properties.containsKey("root")) {
			isRoot = true;
		}
	}

	/**
	 * This method handles the scaling operations.
	 */
	public void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {
		AdminRequest adminRequest = not.getRequest();

		if (adminRequest instanceof GetSubscriptionsRequest) {
			handleGetSubscriptionsRequest(not);
		} else if (adminRequest instanceof ScaleRequest) {
			handleScaleRequest(not);
		} else {
			super.handleAdminRequestNot(from, not);
		}
	}

	/**
	 * 
	 */
	public void react(AgentId from, Notification not) throws Exception {
		if (not instanceof ClientSubscriptionNot) {
			handleClientSubscriptionNot(from, (ClientSubscriptionNot) not);
		} else if (not instanceof ReconnectSubscribersNot) {
			// Forward to local default user agent.
			Channel.sendTo((AgentId) subscribers.get(0), not);
		} else {
			super.react(from, not);
		}
	}

	/**
	 * Forward incoming publications to all fathers.
	 * 
	 * @param messages
	 * @param fromCluster
	 */
	protected void doClientMessages(AgentId from, ClientMessages not, boolean throwsExceptionOnFullDest) {
		ClientMessages clientMsgs = preProcess(from, not);
		if (clientMsgs != null) {
			for (TopicDesc td : pool) {
				forward(td.id, new TopicForwardNot(clientMsgs, false));
				if (logger.isLoggable(BasicLevel.DEBUG))
					logger.log(BasicLevel.DEBUG, "Messages forwarded to topic " + td.id.toString());
			}

			/*processMessages(clientMsgs);
			postProcess(clientMsgs);*/
		}
	}

	private void handleGetSubscriptionsRequest(FwdAdminRequestNot not) {
		if (subscribers.isEmpty()) {
			replyToTopic(new GetNumberReply(getNumberOfSubscribers()),
					not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
		} else {
			Channel.sendTo((AgentId) subscribers.get(0),
					new GetClientSubscriptions(not));
		}
	}

	private void handleScaleRequest(FwdAdminRequestNot not) {
		ScaleRequest sr = (ScaleRequest) not.getRequest();

		setSave(); // state change, so save.
		String[] param;
		int op = sr.getOperation();
		switch(op) {
		case ScaleRequest.SCALE_OUT:
			/* Add a new topic to the pool.
			   param should be: "agent_id;server;port" */
			param = sr.getParameter().split(";");
			TopicDesc td = new TopicDesc();
			td.id = AgentId.fromString(param[0]);
			td.server = param[1];
			td.port = Integer.parseInt(param[2]);
			pool.add(td);
			break;
		case ScaleRequest.SCALE_IN:
			/* remove last added topic.
			   param is not used. */
			pool.remove(pool.size() - 1);
			subId = subId % pool.size();
			break;
		case ScaleRequest.BALANCE:
			/* Reconnect a given number of subscribers.
			 * param should be: "init_topic:topic_index1;number_of_subscribers1;topic_index1;..." */
			param = sr.getParameter().split(":");
			AgentId topic = pool.get(Integer.parseInt(param[0])).id;
			String[] param1 = param[1].split(";");
			ArrayList<Integer> subs = new ArrayList<Integer>();
			ArrayList<Message> msgs = new ArrayList<Message>();
			for (int i = 0; i < param1.length; i += 2) {
				msgs.add(createReconnectionMessage(Integer.parseInt(param1[i])));
				subs.add(Integer.parseInt(param1[i + 1]));
			}
			ReconnectSubscribersNot rsn = 
					new ReconnectSubscribersNot(subs, msgs);
			Channel.sendTo(topic,rsn);
			
			break;
		default:
			// Should never happen.
		}

		replyToTopic(new AdminReply(true, null),
				not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
	}	

	/**
	 * If root, redirects subscriptions to proper topic.
	 * 
	 * @param from shoud be the local default user agent.
	 * @param not Notification of a new client subscriptions.
	 */
	private void handleClientSubscriptionNot(AgentId from, ClientSubscriptionNot not) {
		if (!isRoot)
			return;

		Message msg = createReconnectionMessage(subId);
		subId = (subId + 1) % pool.size();

		ReconnectSubscribersNot rsn = 
				new ReconnectSubscribersNot(not.getSubName(), msg);
		Channel.sendTo(from,rsn);
	}

	private Message createReconnectionMessage(int tid) {
		TopicDesc td = pool.get(tid);
		Message msg = new Message();
		msg.id = "Reconnection Message";
		msg.setProperty("reconnect", td.id.toString());
		msg.setProperty("server", td.server);
		msg.setProperty("port", td.port);

		return msg;
	}
}
