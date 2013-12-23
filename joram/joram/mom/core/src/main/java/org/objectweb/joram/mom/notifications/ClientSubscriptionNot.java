package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.Notification;

/**
 * Used by ElasticTopic.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class ClientSubscriptionNot extends Notification {
	private static final long serialVersionUID = 1L;
	
	private String subName;
	
	public ClientSubscriptionNot(String subName) {
		this.subName = subName;
	}

	public String getSubName() {
		return subName;
	}
}
