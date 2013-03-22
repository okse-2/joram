/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2013 ScalAgent Distributed Technologies
 * Copyright (C) 2012 - 2013 Universite Joseph Fourier
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
 * Contributor(s): Ahmed El Rheddane
 */
package org.objectweb.joram.mom.dest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.PingNot;
import org.objectweb.joram.mom.notifications.PongNot;
import org.objectweb.joram.mom.notifications.WakeUpNot;

import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.SendDestinationsWeights;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.common.Debug;

import org.objectweb.joram.shared.admin.AddRemoteDestination;
import org.objectweb.joram.shared.admin.DelRemoteDestination;

/**
 * The {@link AliasInQueue} class forwards messages to a destination in an other
 * Joram server using the destination ID.
 */
public class AliasInQueue extends Queue {

	/** define serialVersionUID for interoperability */
	private static final long serialVersionUID = 1L;

	public static Logger logger = Debug.getLogger(AliasInQueue.class.getName());

	public static final String REMOTE_AGENT_OPTION = "remoteAgentID";

	//** The queue to send the notification to */
	//private AgentId remoteDestinationID = null;

	/** The maximum time the notification can wait in the network before expiration. */
	//private long expiration = 1000;
	public AliasInQueue() {
		super();
	}

	/**
	 * Configures an {@link AliasInQueue} instance.
	 * 
	 * @param properties
	 *          The initial set of properties.
	 */
	public void setProperties(Properties properties, boolean firstTime) throws Exception {
		super.setProperties(properties, firstTime);

		if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "AliasInQueue.<init> prop = " + properties);
		}

		if (properties != null && properties.containsKey(REMOTE_AGENT_OPTION)) {
			try {
				String[] agents = properties.getProperty(REMOTE_AGENT_OPTION).split(";");
				
				destinations = new ArrayList<AgentId>();
				oldmetrics = new ArrayList<Long>();
				newmetrics = new ArrayList<Long>();
				metrics = new ArrayList<Long>();
				weights = new ArrayList<Long>();

				for (String str : agents) {
					destinations.add(AgentId.fromString(str));
					oldmetrics.add(new Long(0l));
					newmetrics.add(new Long(0l));
					metrics.add(new Long(0l));
					weights.add(new Long(1l));
				}

			} catch (IllegalArgumentException exc) {
				logger.log(BasicLevel.ERROR,
				           "AliasInQueue: can't parse '" + REMOTE_AGENT_OPTION + " property -> " +
				               properties.getProperty(REMOTE_AGENT_OPTION), exc);
			}
		}

		if (destinations == null) {
			throw new Exception("Remote agent identifier is null or invalid." + " The property '"
					+ REMOTE_AGENT_OPTION + "' of the Alias queue has not been set properly.");
		}
	}

	public ClientMessages preProcess(AgentId from, ClientMessages cm) {
		if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "AliasInQueue.preProcess(" + from + ", " + cm + ')');
		}
		if (messages.size() > 0) {
			if (logger.isLoggable(BasicLevel.DEBUG)) {
				logger.log(BasicLevel.DEBUG, "Messages are already waiting, enqueue the new ones");
			}
			return cm;
		}

		ClientMessages forward = new ClientMessages(-1, -1, cm.getMessages());
		//forward.setExpiration(System.currentTimeMillis() + expiration);
		forward.setDeadNotificationAgentId(getId());
		forward.setAsyncSend(true);

		/* sending the notification */
		sendNot(forward);
		nbMsgsDeliverSinceCreation += forward.getMessageCount();
		return null;
	}

	public void react(AgentId from, Notification not) throws Exception {
		if (logger.isLoggable(BasicLevel.DEBUG))
			logger.log(BasicLevel.DEBUG, "AliasInQueue.react(" + from + ',' + not + ')');

		if (not instanceof PongNot) {
			handlePongNot(from,(PongNot) not);
		} else {
			super.react(from, not);
		}
	}

	protected void handleExpiredNot(AgentId from, ExpiredNot not) {
		if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "ExpiredNot received, messages will be queued.");
		}
		Notification expiredNot = not.getExpiredNot();
		if (expiredNot instanceof ClientMessages) {
			nbMsgsDeliverSinceCreation -= ((ClientMessages) expiredNot).getMessageCount();
			try {
        addClientMessages(((ClientMessages) expiredNot), false);
      } catch (AccessException e) {/* never happens */}

			ClientMessages cm = new ClientMessages();
			//cm.setExpiration(System.currentTimeMillis() + expiration);
			cm.setDeadNotificationAgentId(getId());

			for (Iterator ite = messages.iterator(); ite.hasNext();) {
				org.objectweb.joram.mom.messages.Message msg = (org.objectweb.joram.mom.messages.Message) ite.next();
				cm.addMessage(msg.getFullMessage());
				ite.remove();
				msg.delete();
			}

			/* sending the notification */
			sendNot(cm);
			nbMsgsDeliverSinceCreation += cm.getMessageCount();

		} else {
			super.handleExpiredNot(from, not);
		}
	}

	protected void doUnknownAgent(UnknownAgent uA) {
		// TODO (AF): We should remove the bad destination from the destination list.
		if (uA.not instanceof ClientMessages) {
			logger.log(BasicLevel.ERROR, "Remote agent refers to an unknown agent.");

			nbMsgsDeliverSinceCreation -= ((ClientMessages) uA.not).getMessageCount();
			try {
        addClientMessages(((ClientMessages) uA.not), false);
      } catch (AccessException e) {/* never happens */}
		} else if (uA.not instanceof PingNot) {
			logger.log(BasicLevel.ERROR,
			           "Unknown agent. '" + REMOTE_AGENT_OPTION+ "' property refers to an unknown agent.");
		} else {
			super.doUnknownAgent(uA);
		}
	}

	public String toString() {
		return "AliasInQueue:" + getId().toString();
	}

	protected void processSetRight(AgentId user, int right) throws RequestException {
		if (right == READ) {
			throw new RequestException("An alias queue can't be set readable.");
		}
		super.processSetRight(user, right);
	}

	public void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {
		AdminRequest adminRequest = not.getRequest();
		AgentId dest;
		int index;

		//		if (adminRequest instanceof SetRemoteDestination) {
		//			setSave(); // state change, so save.
		//
		//			dest = AgentId.fromString(((SetRemoteDestination) adminRequest).getNewId());
		//			index = destinations.indexOf(dest);
		//			if (index == -1) {
		//				destinations.add(dest);
		//				metrics.add(new Long(0l));
		//				weights.add(new Long(1l));
		//
		//				currentDestination = destinations.size() - 1;
		//				//loadBalancing = false;
		//			} else {
		//				currentDestination = index;
		//			}
		//
		//			replyToTopic(new AdminReply(true, null),
		//			             not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
		//		} else 
		if (adminRequest instanceof AddRemoteDestination) {
			setSave(); // state change, so save.

			dest = AgentId.fromString(((AddRemoteDestination) adminRequest).getNewId());
			if (!destinations.contains(dest)) {
				destinations.add(dest);
				metrics.add(new Long(0l));
				weights.add(5l); // the max weight being 10l
			}
			replyToTopic(new AdminReply(true, null),
					not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
		} else if (adminRequest instanceof DelRemoteDestination) {
			// The AQ should have at least one remote destination
			if (destinations.size() > 1) {
				setSave(); // state change, so save.

				dest = AgentId.fromString(((DelRemoteDestination) adminRequest).getNewId());
				index = destinations.indexOf(dest);

				if (index != -1) {
					destinations.remove(index);
					metrics.remove(index);
					weights.remove(index);

					if (currentDestination > index) {
						currentDestination--;
					} else if (currentDestination == destinations.size()) {
						currentDestination = 0;
					}
				}
				replyToTopic(new AdminReply(true, null),
						not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
			} else {
				replyToTopic(new AdminReply(AdminReply.ILLEGAL_STATE, "Can't remove last destination"),
						not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
			}
		} else if (adminRequest instanceof SendDestinationsWeights) {
			setSave(); // state change, so save.
			
			int[] newWeights = ((SendDestinationsWeights) adminRequest).getWeights();
			String weightStr = "";
			for (int i = 0; i < newWeights.length; i++) {
				weightStr = weightStr + " " + newWeights[i];
				weights.set(i,(long)newWeights[i]);
			}
			
			logger.log(BasicLevel.ERROR,"Received weights:" + weightStr);
			
			replyToTopic(new AdminReply(true, null),
					not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
		} else {
			super.handleAdminRequestNot(from, not);
		}
	}

	//// LOAD BALANCING AND FLOW CONTROL SPECIFIC ELEMENTS ////
	private ArrayList<AgentId> destinations = null;
	private ArrayList<Long> oldmetrics = null;
	private ArrayList<Long> newmetrics = null;
	private ArrayList<Long> metrics = null;
	private ArrayList<Long> weights = null;
	private int currentDestination = 0;
	private int receivedMetrics = 0;
	private long minMetrics = Long.MAX_VALUE;
	private long weightLeft = 1;

	

	private static long pendingMessagesThreshold = 3000;

	private void sendNot(Notification not) {
		Channel.sendTo(destinations.get(currentDestination),not);
		if (--weightLeft <= 0) {
			currentDestination = (currentDestination + 1) % destinations.size();
			weightLeft = weights.get(currentDestination);
		}
	}


	public void wakeUpNot(WakeUpNot not) {
		for(AgentId id : destinations)
			Channel.sendTo(id,new PingNot());
	}

	protected void handlePongNot(AgentId from, PongNot not) {
		int dest = destinations.indexOf(from);

		oldmetrics.set(dest, newmetrics.get(dest));
		Long x1 = (Long) not.get("NbMsgsDeliverSinceCreation");
		if (x1 != null)
			newmetrics.set(dest, x1.longValue());
		metrics.set(dest, newmetrics.get(dest) - oldmetrics.get(dest));
		Integer x2 = (Integer) not.get("PendingMessageCount");
		int pending = 0;
		if (x2 != null)
			pending = x2.intValue();
		logger.log(BasicLevel.ERROR, "Pending: " + pending + " from: " + dest);
		// Correction (consider only 80% of the actual reception rate) 
		if (pending > pendingMessagesThreshold) {
			metrics.set(dest, metrics.get(dest)*80/100);
			logger.log(BasicLevel.ERROR, "Metric got altered for: " + dest);
		}

		logger.log(BasicLevel.ERROR, "Received: " + metrics.get(dest) + " from: " + dest);

		if (metrics.get(dest) < minMetrics) 
			minMetrics = metrics.get(dest);

		if (++receivedMetrics == destinations.size()) {
			if (minMetrics <= 0)
				minMetrics = 1;

			int base  = (int)Math.pow(10.0,Math.floor(Math.log10(minMetrics)));
			logger.log(BasicLevel.ERROR,"Base: " + base);

			ArrayList<Long> newWeights = new ArrayList<Long>();
			for (int i = 0; i < receivedMetrics; i++) { 	
				long weight = (long)Math.round((double)metrics.get(i)/(double)base);

				if (weight <= 0)
					weight = 1;

				newWeights.add(weight);
				logger.log(BasicLevel.ERROR,"Computed: " + newWeights.get(i) + " for: " + i);
			}
			weights = newWeights;
			weightLeft = weights.get(currentDestination);
			receivedMetrics = 0;
			minMetrics = Long.MAX_VALUE;
		}

	}
}
