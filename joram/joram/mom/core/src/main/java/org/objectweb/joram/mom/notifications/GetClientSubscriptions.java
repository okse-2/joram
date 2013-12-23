package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.Notification;

/**
 * Used by ElasticTopic.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class GetClientSubscriptions extends Notification {
	private static final long serialVersionUID = 1L;
	
	private FwdAdminRequestNot not;
	
	public GetClientSubscriptions(FwdAdminRequestNot not) {
		this.not = not;
	}
	
	public FwdAdminRequestNot getAdminNot() {
		return not;
	}
}
